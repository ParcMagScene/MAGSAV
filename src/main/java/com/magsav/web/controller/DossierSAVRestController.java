package com.magsav.web.controller;

import com.magsav.model.DossierSAV;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.validation.ValidationUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST pour la gestion des dossiers SAV Démontre la gestion appropriée des codes de statut HTTP
 */
@RestController
@RequestMapping("/api/dossiers")
public class DossierSAVRestController {

  private final DossierSAVRepository dossierRepo;

  @Autowired
  public DossierSAVRestController(DataSource dataSource) {
    this.dossierRepo = new DossierSAVRepository(dataSource);
  }

  /** Lister les dossiers avec filtrage GET /api/dossiers?statut=recu&search=test */
  @GetMapping
  public ResponseEntity<Map<String, Object>> listerDossiers(
      @RequestParam(required = false) String statut,
      @RequestParam(required = false) String search) {

    try {
      // Validation des paramètres
      if (search != null && !ValidationUtils.isValidSearchQuery(search)) {
        return ResponseEntity.badRequest()
            .body(
                Map.of(
                    "error", "Paramètre de recherche invalide",
                    "code", "INVALID_SEARCH_QUERY"));
      }

      if (statut != null && !ValidationUtils.isValidStatut(statut)) {
        return ResponseEntity.badRequest()
            .body(
                Map.of(
                    "error", "Statut invalide",
                    "code", "INVALID_STATUS",
                    "validStatuses", ValidationUtils.STATUTS_AUTORISES));
      }

      // Nettoyer les paramètres
      search = ValidationUtils.sanitizeInput(search);
      statut = ValidationUtils.sanitizeInput(statut);

      List<DossierSAV> dossiers;

      if (search != null && !search.trim().isEmpty()) {
        dossiers = dossierRepo.findByNumeroSerie(search);
      } else if (statut != null && !statut.trim().isEmpty()) {
        dossiers = dossierRepo.findByStatut(statut);
      } else {
        dossiers = dossierRepo.findAll();
      }

      Map<String, Object> response = new HashMap<>();
      response.put("dossiers", dossiers);
      response.put("total", dossiers.size());
      response.put(
          "filters",
          Map.of(
              "search", search != null ? search : "",
              "statut", statut != null ? statut : ""));

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "error",
                  "Erreur interne du serveur",
                  "code",
                  "INTERNAL_ERROR",
                  "message",
                  "Une erreur inattendue s'est produite"));
    }
  }

  /** Récupérer un dossier par son ID GET /api/dossiers/123 */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> obtenirDossier(@PathVariable Long id) {

    try {
      // Validation de l'ID
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest()
            .body(
                Map.of(
                    "error", "Identifiant invalide",
                    "code", "INVALID_ID",
                    "provided", String.valueOf(id)));
      }

      DossierSAV dossier = dossierRepo.findById(id);

      if (dossier == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                Map.of(
                    "error", "Dossier non trouvé",
                    "code", "DOSSIER_NOT_FOUND",
                    "id", id));
      }

      return ResponseEntity.ok(Map.of("dossier", dossier));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Erreur interne du serveur", "code", "INTERNAL_ERROR"));
    }
  }

  /** Changer le statut d'un dossier PUT /api/dossiers/123/statut */
  @PutMapping("/{id}/statut")
  public ResponseEntity<Map<String, Object>> changerStatut(
      @PathVariable Long id, @RequestBody Map<String, String> body) {

    try {
      // Validation de l'ID
      if (id == null || id <= 0) {
        return ResponseEntity.badRequest()
            .body(
                Map.of(
                    "error", "Identifiant invalide",
                    "code", "INVALID_ID"));
      }

      String nouveauStatut = body.get("statut");

      // Validation du nouveau statut
      if (nouveauStatut == null || !ValidationUtils.isValidStatut(nouveauStatut)) {
        return ResponseEntity.badRequest()
            .body(
                Map.of(
                    "error",
                    "Statut invalide",
                    "code",
                    "INVALID_STATUS",
                    "provided",
                    nouveauStatut,
                    "validStatuses",
                    ValidationUtils.STATUTS_AUTORISES));
      }

      // Vérifier que le dossier existe
      DossierSAV dossierActuel = dossierRepo.findById(id);
      if (dossierActuel == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                Map.of(
                    "error", "Dossier non trouvé",
                    "code", "DOSSIER_NOT_FOUND",
                    "id", id));
      }

      // Mettre à jour le statut
      DossierSAV dossierMisAJour = dossierActuel.withStatut(nouveauStatut);
      if ("termine".equals(nouveauStatut) && dossierActuel.getDateSortie() == null) {
        dossierMisAJour = dossierMisAJour.withDateSortie(java.time.LocalDate.now());
      }

      dossierRepo.update(dossierMisAJour);

      return ResponseEntity.ok(
          Map.of(
              "message",
              "Statut mis à jour avec succès",
              "dossier",
              dossierMisAJour,
              "previousStatus",
              dossierActuel.getStatut()));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Erreur lors de la mise à jour", "code", "UPDATE_ERROR"));
    }
  }
}
