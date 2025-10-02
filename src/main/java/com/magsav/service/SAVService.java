package com.magsav.service;

import com.magsav.label.LabelService;
import com.magsav.model.*;
import com.magsav.pagination.PageRequest;
import com.magsav.pagination.PageResult;
import com.magsav.qr.QRCodeService;
import com.magsav.repo.*;
import com.magsav.validation.ValidationUtils;
import com.magsav.workflow.StatutWorkflow;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class SAVService {
  private final ClientRepository clientRepo;
  private final AppareilRepository appareilRepo;
  private final DossierRepository dossierRepo;
  private final QRCodeService qrService;
  private final LabelService labelService;

  public SAVService(DataSource ds) {
    this.clientRepo = new ClientRepository(ds);
    this.appareilRepo = new AppareilRepository(ds);
    this.dossierRepo = new DossierRepository(ds);
    this.qrService = new QRCodeService();
    this.labelService = new LabelService();
  }

  public static class DossierSAV {
    public final Client client;
    public final Appareil appareil;
    public final Dossier dossier;

    public DossierSAV(Client client, Appareil appareil, Dossier dossier) {
      this.client = client;
      this.appareil = appareil;
      this.dossier = dossier;
    }
  }

  /** Créer un nouveau dossier SAV complet : client, appareil et dossier */
  public DossierSAV creerDossierSAV(
      String nomClient,
      String prenomClient,
      String emailClient,
      String telClient,
      String adresseClient,
      String marqueAppareil,
      String modeleAppareil,
      String snAppareil,
      String accessoires,
      String symptome,
      String commentaire)
      throws Exception {

    // Validation des données d'entrée
    if (!ValidationUtils.isNotEmpty(nomClient) || !ValidationUtils.isValidName(nomClient)) {
      throw new IllegalArgumentException("Nom client invalide");
    }

    if (!ValidationUtils.isNotEmpty(prenomClient) || !ValidationUtils.isValidName(prenomClient)) {
      throw new IllegalArgumentException("Prénom client invalide");
    }

    if (!ValidationUtils.isNotEmpty(emailClient) || !ValidationUtils.isValidEmail(emailClient)) {
      throw new IllegalArgumentException("Email client invalide");
    }

    if (!ValidationUtils.isValidPhone(telClient)) {
      throw new IllegalArgumentException("Numéro de téléphone invalide");
    }

    if (!ValidationUtils.isValidLength(adresseClient, ValidationUtils.MAX_ADDRESS_LENGTH)) {
      throw new IllegalArgumentException("Adresse trop longue");
    }

    if (!ValidationUtils.isNotEmpty(marqueAppareil)
        || !ValidationUtils.isValidLength(marqueAppareil, ValidationUtils.MAX_BRAND_LENGTH)) {
      throw new IllegalArgumentException("Marque appareil invalide");
    }

    if (!ValidationUtils.isNotEmpty(modeleAppareil)
        || !ValidationUtils.isValidLength(modeleAppareil, ValidationUtils.MAX_MODEL_LENGTH)) {
      throw new IllegalArgumentException("Modèle appareil invalide");
    }

    if (!ValidationUtils.isNotEmpty(snAppareil)
        || !ValidationUtils.isValidSerialNumber(snAppareil)) {
      throw new IllegalArgumentException("Numéro de série invalide");
    }

    if (!ValidationUtils.isValidLength(symptome, ValidationUtils.MAX_DESCRIPTION_LENGTH)) {
      throw new IllegalArgumentException("Symptôme trop long");
    }

    if (!ValidationUtils.isValidLength(commentaire, ValidationUtils.MAX_DESCRIPTION_LENGTH)) {
      throw new IllegalArgumentException("Commentaire trop long");
    }

    try (var conn = clientRepo.getDataSource().getConnection()) {
      boolean prevAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try {
        // 1. Créer ou récupérer le client
        Client client =
            new Client(null, nomClient, prenomClient, emailClient, telClient, adresseClient);
        client = clientRepo.upsertByEmail(client);

        // 2. Créer l'appareil
        Appareil appareil =
            new Appareil(
                null, client.id(), marqueAppareil, modeleAppareil, snAppareil, accessoires, null);
        appareil = appareilRepo.save(appareil);

        // 3. Créer le dossier SAV
        Dossier dossier =
            new Dossier(
                null, appareil.id(), "recu", symptome, commentaire, LocalDate.now(), null, null);
        dossier = dossierRepo.save(dossier);

        conn.commit();
        conn.setAutoCommit(prevAutoCommit);
        return new DossierSAV(client, appareil, dossier);
      } catch (Exception e) {
        conn.rollback();
        conn.setAutoCommit(prevAutoCommit);
        throw e;
      }
    }
  }

  /** Rechercher des dossiers SAV par numéro de série */
  public List<DossierSAV> rechercherParSN(String sn) throws Exception {
    // Validation du numéro de série
    if (sn == null || sn.trim().isEmpty()) {
      throw new IllegalArgumentException("Le numéro de série ne peut pas être vide");
    }

    if (!ValidationUtils.isValidSerialNumber(sn)) {
      throw new IllegalArgumentException("Format de numéro de série invalide");
    }

    List<Appareil> appareils = appareilRepo.findBySerialNumber(sn);
    return appareils.stream()
        .flatMap(
            appareil -> {
              try {
                List<Dossier> dossiers = dossierRepo.findByAppareilId(appareil.id());
                Client client = clientRepo.findById(appareil.clientId());
                return dossiers.stream().map(dossier -> new DossierSAV(client, appareil, dossier));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .toList();
  }

  /** Rechercher des dossiers SAV par email client */
  public List<DossierSAV> rechercherParEmailClient(String email) throws Exception {
    // Validation de l'email
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("L'email ne peut pas être vide");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      throw new IllegalArgumentException("Format d'email invalide");
    }

    Client client = clientRepo.findByEmail(email);
    if (client == null) {
      return List.of();
    }

    List<Appareil> appareils = appareilRepo.findByClientId(client.id());
    return appareils.stream()
        .flatMap(
            appareil -> {
              try {
                List<Dossier> dossiers = dossierRepo.findByAppareilId(appareil.id());
                return dossiers.stream().map(dossier -> new DossierSAV(client, appareil, dossier));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .toList();
  }

  /** Lister tous les dossiers avec pagination */
  public PageResult<DossierSAV> listerDossiers(PageRequest pageRequest) throws Exception {
    // Compter le total
    long totalElements = dossierRepo.count();

    // Récupérer les dossiers paginés
    List<Dossier> dossiers =
        dossierRepo.findPaginated(
            pageRequest.getOffset(), pageRequest.getSize(), pageRequest.getSortClause());

    // Convertir en DossierSAV
    List<DossierSAV> dossiersComplets =
        dossiers.stream()
            .map(
                dossier -> {
                  try {
                    Appareil appareil = appareilRepo.findById(dossier.appareilId());
                    Client client = clientRepo.findById(appareil.clientId());
                    return new DossierSAV(client, appareil, dossier);
                  } catch (Exception e) {
                    throw new RuntimeException(
                        "Erreur lors de la récupération du dossier complet", e);
                  }
                })
            .toList();

    return new PageResult<>(
        dossiersComplets,
        pageRequest.getPage(),
        pageRequest.getSize(),
        totalElements,
        pageRequest.getSortBy(),
        pageRequest.getSortDirection());
  }

  /** Rechercher des dossiers avec pagination */
  public PageResult<DossierSAV> rechercherDossiers(String terme, PageRequest pageRequest)
      throws Exception {
    if (terme == null || terme.trim().isEmpty()) {
      return listerDossiers(pageRequest);
    }

    // Compter le total pour la recherche
    long totalElements = dossierRepo.countBySearch(terme.trim());

    // Récupérer les dossiers correspondants
    List<Dossier> dossiers =
        dossierRepo.findBySearchPaginated(
            terme.trim(), pageRequest.getOffset(),
            pageRequest.getSize(), pageRequest.getSortClause());

    // Convertir en DossierSAV
    List<DossierSAV> dossiersComplets =
        dossiers.stream()
            .map(
                dossier -> {
                  try {
                    Appareil appareil = appareilRepo.findById(dossier.appareilId());
                    Client client = clientRepo.findById(appareil.clientId());
                    return new DossierSAV(client, appareil, dossier);
                  } catch (Exception e) {
                    throw new RuntimeException(
                        "Erreur lors de la récupération du dossier complet", e);
                  }
                })
            .toList();

    return new PageResult<>(
        dossiersComplets,
        pageRequest.getPage(),
        pageRequest.getSize(),
        totalElements,
        pageRequest.getSortBy(),
        pageRequest.getSortDirection());
  }

  /** Lister tous les dossiers par statut avec pagination */
  public PageResult<DossierSAV> listerParStatut(String statut, PageRequest pageRequest)
      throws Exception {
    // Validation du statut
    if (statut == null || statut.trim().isEmpty()) {
      throw new IllegalArgumentException("Le statut ne peut pas être vide");
    }

    if (!ValidationUtils.isValidStatut(statut)) {
      throw new IllegalArgumentException("Statut invalide : " + statut);
    }

    // Compter le total par statut
    long totalElements = dossierRepo.countByStatut(statut);

    // Récupérer les dossiers paginés
    List<Dossier> dossiers =
        dossierRepo.findByStatutPaginated(
            statut, pageRequest.getOffset(), pageRequest.getSize(), pageRequest.getSortClause());

    // Convertir en DossierSAV
    List<DossierSAV> dossiersComplets =
        dossiers.stream()
            .map(
                dossier -> {
                  try {
                    Appareil appareil = appareilRepo.findById(dossier.appareilId());
                    Client client = clientRepo.findById(appareil.clientId());
                    return new DossierSAV(client, appareil, dossier);
                  } catch (Exception e) {
                    throw new RuntimeException(
                        "Erreur lors de la récupération du dossier complet", e);
                  }
                })
            .toList();

    return new PageResult<>(
        dossiersComplets,
        pageRequest.getPage(),
        pageRequest.getSize(),
        totalElements,
        pageRequest.getSortBy(),
        pageRequest.getSortDirection());
  }

  /**
   * Lister tous les dossiers par statut (méthode sans pagination - maintenue pour compatibilité)
   */
  public List<DossierSAV> listerParStatut(String statut) throws Exception {
    // Validation du statut
    if (statut == null || statut.trim().isEmpty()) {
      throw new IllegalArgumentException("Le statut ne peut pas être vide");
    }

    if (!ValidationUtils.isValidStatut(statut)) {
      throw new IllegalArgumentException("Statut invalide : " + statut);
    }

    List<Dossier> dossiers = dossierRepo.findByStatut(statut);
    return dossiers.stream()
        .map(
            dossier -> {
              try {
                Appareil appareil = appareilRepo.findById(dossier.appareilId());
                Client client = clientRepo.findById(appareil.clientId());
                return new DossierSAV(client, appareil, dossier);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .toList();
  }

  /** Changer le statut d'un dossier */
  public DossierSAV changerStatut(Long dossierId, String nouveauStatut) throws Exception {
    Dossier dossier = dossierRepo.findById(dossierId);
    if (dossier == null) {
      throw new IllegalArgumentException("Dossier introuvable : " + dossierId);
    }

    dossier = dossier.withStatut(nouveauStatut);
    if ("termine".equals(nouveauStatut) && dossier.dateSortie() == null) {
      dossier = dossier.withDateSortie(LocalDate.now());
    }

    dossier = dossierRepo.save(dossier);

    Appareil appareil = appareilRepo.findById(dossier.appareilId());
    Client client = clientRepo.findById(appareil.clientId());

    return new DossierSAV(client, appareil, dossier);
  }

  /** Générer une étiquette QR pour un dossier */
  public void genererEtiquette(Long dossierId, Path outputDir) throws Exception {
    DossierSAV dossierSAV = getDossierSAV(dossierId);

    String qrContent = String.format("MAGSAV:%d:%s", dossierId, dossierSAV.appareil.sn());
    Path qrPath = outputDir.resolve("qr-dossier-" + dossierId + ".png");
    Path pdfPath = outputDir.resolve("etiquette-dossier-" + dossierId + ".pdf");

    qrService.generateToFile(qrContent, 256, qrPath);

    String titre =
        String.format(
            "Dossier SAV #%d%n%s %s%n%s %s - SN:%s",
            dossierId,
            dossierSAV.client.prenom(),
            dossierSAV.client.nom(),
            dossierSAV.appareil.marque(),
            dossierSAV.appareil.modele(),
            dossierSAV.appareil.sn());

    labelService.createSimpleLabel(pdfPath, titre, qrPath);
  }

  /** Récupérer un dossier SAV complet */
  public DossierSAV getDossierSAV(Long dossierId) throws Exception {
    Dossier dossier = dossierRepo.findById(dossierId);
    if (dossier == null) {
      throw new IllegalArgumentException("Dossier introuvable : " + dossierId);
    }

    Appareil appareil = appareilRepo.findById(dossier.appareilId());
    Client client = clientRepo.findById(appareil.clientId());

    return new DossierSAV(client, appareil, dossier);
  }

  /** Changer le statut d'un dossier avec validation du workflow */
  public StatutWorkflow.ValidationResult changerStatutDossier(
      Long dossierId, String nouveauStatut, String roleUtilisateur) throws Exception {
    Dossier dossier = dossierRepo.findById(dossierId);
    if (dossier == null) {
      return new StatutWorkflow.ValidationResult(false, "Dossier introuvable : " + dossierId);
    }

    // Valider la transition selon le workflow
    StatutWorkflow.ValidationResult validation =
        StatutWorkflow.validerTransition(dossier.statut(), nouveauStatut, roleUtilisateur);

    if (validation.isValide()) {
      // Effectuer le changement avec les bons noms de champs
      Dossier dossierMaj =
          new Dossier(
              dossier.id(),
              dossier.appareilId(),
              nouveauStatut,
              dossier.symptome(),
              dossier.commentaire(),
              dossier.dateEntree(),
              dossier.dateSortie(),
              dossier.createdAt());

      dossierRepo.save(dossierMaj); // save gère insert/update selon l'ID
    }

    return validation;
  }

  /** Obtenir les statuts accessibles pour un dossier selon le rôle de l'utilisateur */
  public List<StatutWorkflow.Statut> getStatutsAccessibles(Long dossierId, String roleUtilisateur)
      throws Exception {
    Dossier dossier = dossierRepo.findById(dossierId);
    if (dossier == null) {
      throw new IllegalArgumentException("Dossier introuvable : " + dossierId);
    }

    return StatutWorkflow.getStatutsAccessibles(dossier.statut(), roleUtilisateur);
  }
}
