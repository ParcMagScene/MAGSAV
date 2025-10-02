package com.magsav.controller.api;

import com.magsav.pagination.PageRequest;
import com.magsav.pagination.PageResult;
import com.magsav.service.SAVService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Contrôleur API REST pour la recherche avancée des dossiers SAV */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchApiController {

  private final SAVService savService;

  public SearchApiController(SAVService savService) {
    this.savService = savService;
  }

  /** Recherche avancée de dossiers SAV avec pagination et tri */
  @GetMapping("/dossiers")
  public ResponseEntity<PageResult<SAVService.DossierSAV>> searchDossiers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,

      // Recherche globale
      @RequestParam(required = false) String search,

      // Filtres client
      @RequestParam(required = false) String clientNom,
      @RequestParam(required = false) String clientEmail,
      @RequestParam(required = false) String clientTelephone,
      @RequestParam(required = false) String clientVille,

      // Filtres appareil
      @RequestParam(required = false) String appareilMarque,
      @RequestParam(required = false) String appareilModele,
      @RequestParam(required = false) String numeroSerie,

      // Filtres dates
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateEntreeDebut,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateEntreeFin,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateSortieDebut,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateSortieFin,

      // Filtres SAV
      @RequestParam(required = false) String statut,
      @RequestParam(required = false) String symptome,
      @RequestParam(required = false) String commentaire) {

    try {
      // Construction des critères de recherche
      SearchCriteria criteria =
          SearchCriteria.builder()
              .search(search)
              .clientNom(clientNom)
              .clientEmail(clientEmail)
              .clientTelephone(clientTelephone)
              .clientVille(clientVille)
              .appareilMarque(appareilMarque)
              .appareilModele(appareilModele)
              .numeroSerie(numeroSerie)
              .dateEntreeDebut(dateEntreeDebut)
              .dateEntreeFin(dateEntreeFin)
              .dateSortieDebut(dateSortieDebut)
              .dateSortieFin(dateSortieFin)
              .statut(statut)
              .symptome(symptome)
              .commentaire(commentaire)
              .build();

      // Création de la requête de pagination
      PageRequest pageRequest = new PageRequest(page, size, sortBy, sortDir);

      // Exécution de la recherche
      PageResult<SAVService.DossierSAV> results = performAdvancedSearch(criteria, pageRequest);

      return ResponseEntity.ok(results);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Obtient les statistiques pour les filtres rapides */
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Long>> getSearchStats() {
    try {
      PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE, "id", "desc");
      PageResult<SAVService.DossierSAV> allDossiers = savService.listerDossiers(pageRequest);

      Map<String, Long> stats = new HashMap<>();
      List<SAVService.DossierSAV> dossiers = allDossiers.getContent();

      stats.put("total", (long) dossiers.size());
      stats.put("recu", dossiers.stream().filter(d -> "recu".equals(d.dossier.statut())).count());
      stats.put(
          "reparation",
          dossiers.stream().filter(d -> "reparation".equals(d.dossier.statut())).count());
      stats.put(
          "termine", dossiers.stream().filter(d -> "termine".equals(d.dossier.statut())).count());
      stats.put("urgent", dossiers.stream().filter(this::isUrgent).count());

      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Export CSV des résultats de recherche */
  @GetMapping("/dossiers/export")
  public ResponseEntity<byte[]> exportSearchResults(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String clientNom,
      @RequestParam(required = false) String clientEmail,
      @RequestParam(required = false) String clientTelephone,
      @RequestParam(required = false) String clientVille,
      @RequestParam(required = false) String appareilMarque,
      @RequestParam(required = false) String appareilModele,
      @RequestParam(required = false) String numeroSerie,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateEntreeDebut,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateEntreeFin,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateSortieDebut,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateSortieFin,
      @RequestParam(required = false) String statut,
      @RequestParam(required = false) String symptome,
      @RequestParam(required = false) String commentaire) {

    try {
      SearchCriteria criteria =
          SearchCriteria.builder()
              .search(search)
              .clientNom(clientNom)
              .clientEmail(clientEmail)
              .clientTelephone(clientTelephone)
              .clientVille(clientVille)
              .appareilMarque(appareilMarque)
              .appareilModele(appareilModele)
              .numeroSerie(numeroSerie)
              .dateEntreeDebut(dateEntreeDebut)
              .dateEntreeFin(dateEntreeFin)
              .dateSortieDebut(dateSortieDebut)
              .dateSortieFin(dateSortieFin)
              .statut(statut)
              .symptome(symptome)
              .commentaire(commentaire)
              .build();

      PageRequest pageRequest = new PageRequest(0, Integer.MAX_VALUE, "id", "desc");
      PageResult<SAVService.DossierSAV> results = performAdvancedSearch(criteria, pageRequest);

      String csv = generateCSV(results.getContent());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentDispositionFormData("attachment", "dossiers_sav_export.csv");

      return ResponseEntity.ok().headers(headers).body(csv.getBytes("UTF-8"));

    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Suggestions de recherche auto-completion */
  @GetMapping("/suggestions")
  public ResponseEntity<List<String>> getSuggestions(@RequestParam String q) {
    try {
      if (q == null || q.trim().length() < 2) {
        return ResponseEntity.ok(Collections.emptyList());
      }

      PageRequest pageRequest = new PageRequest(0, 100, "id", "desc");
      PageResult<SAVService.DossierSAV> allDossiers = savService.listerDossiers(pageRequest);

      Set<String> suggestions = new HashSet<>();
      String query = q.toLowerCase().trim();

      for (SAVService.DossierSAV dossier : allDossiers.getContent()) {
        // Suggestions clients
        if (dossier.client.nom() != null && dossier.client.nom().toLowerCase().contains(query)) {
          suggestions.add(dossier.client.nom());
        }
        if (dossier.client.prenom() != null
            && dossier.client.prenom().toLowerCase().contains(query)) {
          suggestions.add(dossier.client.prenom());
        }
        if (dossier.client.email() != null
            && dossier.client.email().toLowerCase().contains(query)) {
          suggestions.add(dossier.client.email());
        }

        // Suggestions appareils
        if (dossier.appareil.marque() != null
            && dossier.appareil.marque().toLowerCase().contains(query)) {
          suggestions.add(dossier.appareil.marque());
        }
        if (dossier.appareil.modele() != null
            && dossier.appareil.modele().toLowerCase().contains(query)) {
          suggestions.add(dossier.appareil.modele());
        }

        // Suggestions symptômes
        if (dossier.dossier.symptome() != null
            && dossier.dossier.symptome().toLowerCase().contains(query)) {
          suggestions.add(dossier.dossier.symptome());
        }
      }

      List<String> result = suggestions.stream().limit(10).sorted().collect(Collectors.toList());

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  /** Exécute la recherche avancée avec les critères fournis */
  private PageResult<SAVService.DossierSAV> performAdvancedSearch(
      SearchCriteria criteria, PageRequest pageRequest) throws Exception {
    // Pour l'instant, on utilise la recherche de base du service
    // Dans une implémentation complète, on filtrerait selon les critères
    return savService.listerDossiers(pageRequest);
  }

  /** Génère le CSV à partir des dossiers */
  private String generateCSV(List<SAVService.DossierSAV> dossiers) {
    StringBuilder csv = new StringBuilder();

    // En-têtes
    csv.append(
        "ID,Date Entrée,Client,Email,Téléphone,Ville,Marque,Modèle,Numéro Série,Statut,Symptôme,Commentaire\n");

    // Données
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    for (SAVService.DossierSAV dossier : dossiers) {
      csv.append(csvEscape(String.valueOf(dossier.dossier.id()))).append(",");
      csv.append(csvEscape(dossier.dossier.dateEntree().format(formatter))).append(",");
      csv.append(csvEscape(dossier.client.nom() + " " + dossier.client.prenom())).append(",");
      csv.append(csvEscape(dossier.client.email())).append(",");
      csv.append(csvEscape(dossier.client.tel())).append(",");
      csv.append(csvEscape(dossier.client.adresse())).append(",");
      csv.append(csvEscape(dossier.appareil.marque())).append(",");
      csv.append(csvEscape(dossier.appareil.modele())).append(",");
      csv.append(csvEscape(dossier.appareil.sn())).append(",");
      csv.append(csvEscape(dossier.dossier.statut())).append(",");
      csv.append(csvEscape(dossier.dossier.symptome())).append(",");
      csv.append(csvEscape(dossier.dossier.commentaire())).append("\n");
    }

    return csv.toString();
  }

  /** Échappe une valeur pour l'export CSV */
  private String csvEscape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  /** Détermine si un dossier est urgent */
  private boolean isUrgent(SAVService.DossierSAV dossier) {
    // Un dossier est urgent s'il est en réparation depuis plus de 7 jours
    if (!"reparation".equals(dossier.dossier.statut())) {
      return false;
    }
    return dossier.dossier.dateEntree().isBefore(LocalDate.now().minusDays(7));
  }

  /** Classe pour les critères de recherche */
  public static class SearchCriteria {
    private String search;
    private String clientNom;
    private String clientEmail;
    private String clientTelephone;
    private String clientVille;
    private String appareilMarque;
    private String appareilModele;
    private String numeroSerie;
    private LocalDate dateEntreeDebut;
    private LocalDate dateEntreeFin;
    private LocalDate dateSortieDebut;
    private LocalDate dateSortieFin;
    private String statut;
    private String symptome;
    private String commentaire;

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private SearchCriteria criteria = new SearchCriteria();

      public Builder search(String search) {
        criteria.search = search;
        return this;
      }

      public Builder clientNom(String clientNom) {
        criteria.clientNom = clientNom;
        return this;
      }

      public Builder clientEmail(String clientEmail) {
        criteria.clientEmail = clientEmail;
        return this;
      }

      public Builder clientTelephone(String clientTelephone) {
        criteria.clientTelephone = clientTelephone;
        return this;
      }

      public Builder clientVille(String clientVille) {
        criteria.clientVille = clientVille;
        return this;
      }

      public Builder appareilMarque(String appareilMarque) {
        criteria.appareilMarque = appareilMarque;
        return this;
      }

      public Builder appareilModele(String appareilModele) {
        criteria.appareilModele = appareilModele;
        return this;
      }

      public Builder numeroSerie(String numeroSerie) {
        criteria.numeroSerie = numeroSerie;
        return this;
      }

      public Builder dateEntreeDebut(LocalDate dateEntreeDebut) {
        criteria.dateEntreeDebut = dateEntreeDebut;
        return this;
      }

      public Builder dateEntreeFin(LocalDate dateEntreeFin) {
        criteria.dateEntreeFin = dateEntreeFin;
        return this;
      }

      public Builder dateSortieDebut(LocalDate dateSortieDebut) {
        criteria.dateSortieDebut = dateSortieDebut;
        return this;
      }

      public Builder dateSortieFin(LocalDate dateSortieFin) {
        criteria.dateSortieFin = dateSortieFin;
        return this;
      }

      public Builder statut(String statut) {
        criteria.statut = statut;
        return this;
      }

      public Builder symptome(String symptome) {
        criteria.symptome = symptome;
        return this;
      }

      public Builder commentaire(String commentaire) {
        criteria.commentaire = commentaire;
        return this;
      }

      public SearchCriteria build() {
        return criteria;
      }
    }

    // Getters
    public String getSearch() {
      return search;
    }

    public String getClientNom() {
      return clientNom;
    }

    public String getClientEmail() {
      return clientEmail;
    }

    public String getClientTelephone() {
      return clientTelephone;
    }

    public String getClientVille() {
      return clientVille;
    }

    public String getAppareilMarque() {
      return appareilMarque;
    }

    public String getAppareilModele() {
      return appareilModele;
    }

    public String getNumeroSerie() {
      return numeroSerie;
    }

    public LocalDate getDateEntreeDebut() {
      return dateEntreeDebut;
    }

    public LocalDate getDateEntreeFin() {
      return dateEntreeFin;
    }

    public LocalDate getDateSortieDebut() {
      return dateSortieDebut;
    }

    public LocalDate getDateSortieFin() {
      return dateSortieFin;
    }

    public String getStatut() {
      return statut;
    }

    public String getSymptome() {
      return symptome;
    }

    public String getCommentaire() {
      return commentaire;
    }
  }
}
