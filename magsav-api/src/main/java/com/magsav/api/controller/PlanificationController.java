package com.magsav.api.controller;

import com.magsav.api.exception.ResourceNotFoundException;
import com.magsav.api.exception.ValidationException;
import com.magsav.model.Planification;
import com.magsav.repo.PlanificationRepository;
import com.magsav.service.google.GoogleIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des planifications dans MAGSAV 1.3
 */
@RestController
@RequestMapping("/api/v1/planifications")
@CrossOrigin(origins = "*")
public class PlanificationController {

    private final PlanificationRepository planificationRepository;
    private final GoogleIntegrationService googleService;

    public PlanificationController() {
        this.planificationRepository = new PlanificationRepository();
        this.googleService = GoogleIntegrationService.getInstance();
    }

    /**
     * Récupère toutes les planifications
     */
    @GetMapping
    public ResponseEntity<List<Planification>> getAllPlanifications() {
        List<Planification> planifications = planificationRepository.findAll();
        return ResponseEntity.ok(planifications);
    }

    /**
     * Récupère une planification par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Planification> getPlanificationById(@PathVariable Long id) {
        Planification planification = planificationRepository.findById(id);
        
        if (planification == null) {
            throw new ResourceNotFoundException("Planification", "id", id);
        }
        
        return ResponseEntity.ok(planification);
    }

    /**
     * Crée une nouvelle planification
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlanification(@RequestBody Planification planification) {
        // Validation des données
        validatePlanification(planification);
        
        boolean saved = planificationRepository.save(planification);
        Map<String, Object> response = new HashMap<>();
        
        if (saved) {
            // Synchronisation avec Google Calendar si disponible
            if (googleService.isCalendarAvailable()) {
                googleService.syncPlanification(planification)
                    .whenComplete((success, ex) -> {
                        if (ex != null) {
                            System.err.println("Erreur sync Google Calendar: " + ex.getMessage());
                        }
                    });
            }
            
            response.put("success", true);
            response.put("message", "Planification créée avec succès");
            response.put("id", planification.getId());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la planification");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Met à jour une planification existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlanification(
            @PathVariable Long id, @RequestBody Planification planification) {
        
        // Vérifier que la planification existe
        Planification existing = planificationRepository.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Planification", "id", id);
        }
        
        // Validation des données
        validatePlanification(planification);
        
        // Mettre à jour l'ID
        planification.setId(id);
        
        boolean updated = planificationRepository.save(planification);
        Map<String, Object> response = new HashMap<>();
        
        if (updated) {
            // Synchronisation avec Google Calendar si disponible
            if (googleService.isCalendarAvailable()) {
                googleService.syncPlanification(planification)
                    .whenComplete((success, ex) -> {
                        if (ex != null) {
                            System.err.println("Erreur sync Google Calendar: " + ex.getMessage());
                        }
                    });
            }
            
            response.put("success", true);
            response.put("message", "Planification mise à jour avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour de la planification");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Supprime une planification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlanification(@PathVariable Long id) {
        Planification planification = planificationRepository.findById(id);
        
        if (planification == null) {
            throw new ResourceNotFoundException("Planification", "id", id);
        }
        
        // Supprimer de Google Calendar si nécessaire
        if (googleService.isCalendarAvailable() && 
            planification.getGoogleEventId() != null && 
            !planification.getGoogleEventId().isEmpty()) {
            
            googleService.deletePlanification(planification.getGoogleEventId())
                .whenComplete((success, ex) -> {
                    if (ex != null) {
                        System.err.println("Erreur suppression Google Calendar: " + ex.getMessage());
                    }
                });
        }
        
        boolean deleted = planificationRepository.delete(id);
        Map<String, Object> response = new HashMap<>();
        
        if (deleted) {
            response.put("success", true);
            response.put("message", "Planification supprimée avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression de la planification");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Récupère les planifications d'un technicien
     */
    @GetMapping("/technicien/{technicienId}")
    public ResponseEntity<List<Planification>> getPlanificationsByTechnicien(@PathVariable Long technicienId) {
        List<Planification> planifications = planificationRepository.findByTechnicienId(technicienId);
        return ResponseEntity.ok(planifications);
    }

    /**
     * Récupère les planifications par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Planification>> getPlanificationsByStatut(@PathVariable String statut) {
        try {
            Planification.StatutPlanification statutEnum = Planification.StatutPlanification.valueOf(statut.toUpperCase());
            List<Planification> planifications = planificationRepository.findByStatut(statutEnum);
            return ResponseEntity.ok(planifications);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Statut invalide: " + statut);
        }
    }

    /**
     * Marque une planification comme terminée
     */
    @PutMapping("/{id}/terminer")
    public ResponseEntity<Map<String, Object>> terminerPlanification(@PathVariable Long id) {
        Planification planification = planificationRepository.findById(id);
        
        if (planification == null) {
            throw new ResourceNotFoundException("Planification", "id", id);
        }
        
        planification.setStatut(Planification.StatutPlanification.TERMINE);
        planification.setDateFinReel(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        boolean updated = planificationRepository.save(planification);
        Map<String, Object> response = new HashMap<>();
        
        if (updated) {
            // Synchronisation avec Google Calendar
            if (googleService.isCalendarAvailable()) {
                googleService.syncPlanification(planification)
                    .whenComplete((success, ex) -> {
                        if (ex != null) {
                            System.err.println("Erreur sync Google Calendar: " + ex.getMessage());
                        }
                    });
            }
            
            response.put("success", true);
            response.put("message", "Planification marquée comme terminée");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour du statut");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Synchronise toutes les planifications avec Google Calendar
     */
    @PostMapping("/sync-google-calendar")
    public ResponseEntity<Map<String, Object>> syncWithGoogleCalendar() {
        Map<String, Object> response = new HashMap<>();
        
        if (!googleService.isCalendarAvailable()) {
            response.put("success", false);
            response.put("message", "Google Calendar non disponible");
            return ResponseEntity.badRequest().body(response);
        }
        
        List<Planification> planifications = planificationRepository.findAll();
        int syncCount = 0;
        
        for (Planification planification : planifications) {
            try {
                googleService.syncPlanification(planification).get();
                syncCount++;
            } catch (Exception e) {
                System.err.println("Erreur sync planification " + planification.getId() + ": " + e.getMessage());
            }
        }
        
        response.put("success", true);
        response.put("message", syncCount + " planifications synchronisées avec Google Calendar");
        response.put("synchronized_count", syncCount);
        response.put("total_count", planifications.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Valide les données d'une planification
     */
    private void validatePlanification(Planification planification) {
        Map<String, String> errors = new HashMap<>();
        
        if (planification.getClientNom() == null || planification.getClientNom().trim().isEmpty()) {
            errors.put("clientNom", "Le nom du client est requis");
        }
        
        if (planification.getTechnicienId() <= 0) {
            errors.put("technicienId", "L'ID du technicien est requis");
        }
        
        if (planification.getDatePrevue() == null || planification.getDatePrevue().trim().isEmpty()) {
            errors.put("datePrevue", "La date prévue est requise");
        }
        
        if (planification.getHeurePrevue() == null || planification.getHeurePrevue().trim().isEmpty()) {
            errors.put("heurePrevue", "L'heure prévue est requise");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation", errors);
        }
    }
}