package com.magsav.imports;

import com.magsav.model.Client;
import com.magsav.model.DossierSAV;
import com.magsav.model.Fournisseur;
import com.magsav.repo.ClientRepository;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.repo.FournisseurRepository;
import com.magsav.validation.ValidationUtils;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class CSVImporter {
  private final ClientRepository clientRepo;
  private final FournisseurRepository fournisseurRepo;
  private final DossierSAVRepository dossierSAVRepo;

  // Formats de date supportés (configurables)
  private final List<DateTimeFormatter> dateFormats;

  public CSVImporter(DataSource ds) {
    this.clientRepo = new ClientRepository(ds);
    this.fournisseurRepo = new FournisseurRepository(ds);
    this.dossierSAVRepo = new DossierSAVRepository(ds);
    this.dateFormats =
        List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"));
  }

  public CSVImporter(DataSource ds, List<String> patterns) {
    this.clientRepo = new ClientRepository(ds);
    this.fournisseurRepo = new FournisseurRepository(ds);
    this.dossierSAVRepo = new DossierSAVRepository(ds);
    if (patterns == null || patterns.isEmpty()) {
      this.dateFormats = List.of(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    } else {
      this.dateFormats =
          patterns.stream()
              .map(
                  p -> {
                    try {
                      return DateTimeFormatter.ofPattern(p);
                    } catch (Exception e) {
                      return null;
                    }
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
    }
  }

  public int importClients(Path csvPath) throws Exception {
    int importedCount = 0;
    try (var conn = clientRepo.getDataSource().getConnection()) {
      boolean prevAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try (CSVReader rdr = new CSVReader(new FileReader(csvPath.toFile()))) {
        String[] row;
        boolean header = true;
        while ((row = rdr.readNext()) != null) {
          if (header) {
            header = false;
            continue;
          }
          String nom = safe(row, 0);
          String prenom = safe(row, 1);
          String email = safe(row, 2);
          String tel = safe(row, 3);
          String adresse = safe(row, 4);

          // Validation des données avant import
          if (!ValidationUtils.isValidName(nom)) {
            throw new IllegalArgumentException(
                "Nom invalide à la ligne " + (importedCount + 2) + ": " + nom);
          }

          if (!ValidationUtils.isValidName(prenom)) {
            throw new IllegalArgumentException(
                "Prénom invalide à la ligne " + (importedCount + 2) + ": " + prenom);
          }

          if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException(
                "Email invalide à la ligne " + (importedCount + 2) + ": " + email);
          }

          if (!ValidationUtils.isValidPhone(tel)) {
            throw new IllegalArgumentException(
                "Téléphone invalide à la ligne " + (importedCount + 2) + ": " + tel);
          }

          if (!ValidationUtils.isValidLength(adresse, ValidationUtils.MAX_ADDRESS_LENGTH)) {
            throw new IllegalArgumentException(
                "Adresse trop longue à la ligne " + (importedCount + 2));
          }

          clientRepo.upsertByEmail(new Client(null, nom, prenom, email, tel, adresse));
          importedCount++;
        }
        conn.commit();
        conn.setAutoCommit(prevAutoCommit);
      } catch (Exception e) {
        conn.rollback();
        conn.setAutoCommit(prevAutoCommit);
        throw e;
      }
    }
    return importedCount;
  }

  public int importFournisseurs(Path csvPath) throws Exception {
    int importedCount = 0;
    try (var conn = fournisseurRepo.getDataSource().getConnection()) {
      boolean prevAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try (CSVReader rdr = new CSVReader(new FileReader(csvPath.toFile()))) {
        String[] row;
        boolean header = true;
        while ((row = rdr.readNext()) != null) {
          if (header) {
            header = false;
            continue;
          }
          String nom = safe(row, 0);
          String email = safe(row, 1);
          String tel = safe(row, 2);
          String siret = safe(row, 3);
          fournisseurRepo.upsertByEmail(new Fournisseur(null, nom, email, tel, siret));
          importedCount++;
        }
        conn.commit();
        conn.setAutoCommit(prevAutoCommit);
      } catch (Exception e) {
        conn.rollback();
        conn.setAutoCommit(prevAutoCommit);
        throw e;
      }
    }
    return importedCount;
  }

  /**
   * Import simple d'un CSV Produits (produit, numero_serie, proprietaire, panne, statut, detecteur,
   * date_entree, date_sortie) S'appuie sur la logique dossiers SAV pour réutiliser le même
   * repository.
   */
  public int importProduits(Path csvPath) throws Exception {
    // Alias vers importDossiersSAV (structure identique)
    return importDossiersSAV(csvPath);
  }

  /**
   * Import du CSV principal avec structure : PRODUIT, N° DE SERIE, PROPRIETAIRE, PANNE, STATUT,
   * DETECTEUR, DATE ENTREE, DATE SORTIE
   */
  public int importDossiersSAV(Path csvPath) throws Exception {
    int importedCount = 0;
    try (CSVReader rdr = new CSVReader(new FileReader(csvPath.toFile()))) {
      String[] headers = rdr.readNext(); // Lire l'en-tête
      if (headers != null) {
        System.out.println("En-têtes détectés : " + String.join(", ", headers));
      }

      String[] row;
      int lineNumber = 1;
      while ((row = rdr.readNext()) != null) {
        lineNumber++;
        try {
          String produit = safe(row, 0);
          String numeroSerie = safe(row, 1);
          String proprietaire = safe(row, 2);
          String panne = safe(row, 3);
          String statut = safe(row, 4);
          String detecteur = safe(row, 5);
          String dateEntreeStr = safe(row, 6);
          String dateSortieStr = safe(row, 7);

          // Validation des champs obligatoires
          if (isEmpty(produit) || isEmpty(numeroSerie) || isEmpty(proprietaire)) {
            System.err.printf(
                "Ligne %d ignorée : champs obligatoires manquants (PRODUIT, N° DE SERIE, PROPRIETAIRE)%n",
                lineNumber);
            continue;
          }

          // Parsing des dates
          LocalDate dateEntree = parseDate(dateEntreeStr);
          LocalDate dateSortie = parseDate(dateSortieStr);

          // Statut par défaut
          if (isEmpty(statut)) {
            statut = "recu";
          }

          DossierSAV dossier =
              new DossierSAV(
                  null,
                  null,
                  produit,
                  numeroSerie,
                  proprietaire,
                  panne,
                  statut,
                  detecteur,
                  dateEntree,
                  dateSortie,
                  null);
          // Chaque ligne représente une intervention distincte
          // On insère systématiquement pour conserver l’historique au niveau intervention
          dossierSAVRepo.save(dossier);
          importedCount++;

        } catch (Exception e) {
          System.err.printf("Erreur ligne %d : %s%n", lineNumber, e.getMessage());
        }
      }
    }
    return importedCount;
  }

  private LocalDate parseDate(String dateStr) {
    if (isEmpty(dateStr)) {
      return null;
    }

    for (DateTimeFormatter formatter : dateFormats) {
      try {
        return LocalDate.parse(dateStr.trim(), formatter);
      } catch (DateTimeParseException ignored) {
        // Essayer le format suivant
      }
    }

    System.err.printf("Format de date non reconnu : '%s'%n", dateStr);
    return null;
  }

  private boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  private static String safe(String[] arr, int idx) {
    return idx < arr.length && arr[idx] != null ? arr[idx].trim() : null;
  }
}
