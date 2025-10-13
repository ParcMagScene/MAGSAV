package com.magsav.api.controller;

import com.magsav.api.exception.ResourceNotFoundException;
import com.magsav.api.exception.ValidationException;
import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.google.GoogleIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des sociétés/clients dans MAGSAV 1.3
 */
@RestController
@RequestMapping("/api/v1/societes")
@CrossOrigin(origins = "*")
public class SocieteController {

    private final SocieteRepository societeRepository;
    private final GoogleIntegrationService googleService;

    public SocieteController() {
        this.societeRepository = new SocieteRepository();
        this.googleService = GoogleIntegrationService.getInstance();
    }

    /**
     * Récupère toutes les sociétés
     */
    @GetMapping
    public ResponseEntity<List<Societe>> getAllSocietes() {
        List<Societe> societes = societeRepository.findAll();
        return ResponseEntity.ok(societes);
    }

    /**
     * Récupère une société par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Societe> getSocieteById(@PathVariable Long id) {
        Societe societe = societeRepository.findById(id);
        
        if (societe == null) {
            throw new ResourceNotFoundException("Société", "id", id);
        }
        
        return ResponseEntity.ok(societe);
    }

    /**
     * Crée une nouvelle société
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSociete(@RequestBody Societe societe) {
        // Validation des données
        validateSociete(societe);
        
        boolean saved = societeRepository.save(societe);
        Map<String, Object> response = new HashMap<>();
        
        if (saved) {
            // Ajouter aux contacts Google si disponible
            if (googleService.isContactsAvailable() && 
                societe.email() != null && !societe.email().trim().isEmpty()) {
                
                addToGoogleContacts(societe);
            }
            
            response.put("success", true);
            response.put("message", "Société créée avec succès");
            response.put("id", societe.id());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la société");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Met à jour une société existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSociete(
            @PathVariable Long id, @RequestBody Societe societe) {
        
        // Vérifier que la société existe
        Societe existing = societeRepository.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Société", "id", id);
        }
        
        // Validation des données
        validateSociete(societe);
        
        // Créer une nouvelle instance avec l'ID correct
        Societe updatedSociete = new Societe(id, societe.type(), societe.nom(), 
                                           societe.email(), societe.phone(), 
                                           societe.adresse(), societe.notes(), 
                                           societe.createdAt());
        
        boolean updated = societeRepository.save(updatedSociete);
        Map<String, Object> response = new HashMap<>();
        
        if (updated) {
            response.put("success", true);
            response.put("message", "Société mise à jour avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour de la société");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Supprime une société
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSociete(@PathVariable Long id) {
        Societe societe = societeRepository.findById(id);
        
        if (societe == null) {
            throw new ResourceNotFoundException("Société", "id", id);
        }
        
        boolean deleted = societeRepository.delete(id);
        Map<String, Object> response = new HashMap<>();
        
        if (deleted) {
            response.put("success", true);
            response.put("message", "Société supprimée avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression de la société");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Récupère les sociétés par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Societe>> getSocietesByType(@PathVariable String type) {
        List<Societe> societes = societeRepository.findByType(type);
        return ResponseEntity.ok(societes);
    }

    /**
     * Recherche des sociétés par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<Societe>> searchSocietes(@RequestParam String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le paramètre 'nom' est requis pour la recherche");
        }
        
        List<Societe> societes = societeRepository.findByNomContaining(nom.trim());
        return ResponseEntity.ok(societes);
    }

    /**
     * Récupère uniquement les clients
     */
    @GetMapping("/clients")
    public ResponseEntity<List<Societe>> getClients() {
        List<Societe> clients = societeRepository.findByType("client");
        return ResponseEntity.ok(clients);
    }

    /**
     * Récupère uniquement les fournisseurs
     */
    @GetMapping("/fournisseurs")  
    public ResponseEntity<List<Societe>> getFournisseurs() {
        List<Societe> fournisseurs = societeRepository.findByType("fournisseur");
        return ResponseEntity.ok(fournisseurs);
    }

    /**
     * Synchronise toutes les sociétés avec Google Contacts
     */
    @PostMapping("/sync-google-contacts")
    public ResponseEntity<Map<String, Object>> syncWithGoogleContacts() {
        Map<String, Object> response = new HashMap<>();
        
        if (!googleService.isContactsAvailable()) {
            response.put("success", false);
            response.put("message", "Google Contacts non disponible");
            return ResponseEntity.badRequest().body(response);
        }
        
        List<Societe> societes = societeRepository.findAll();
        int syncCount = 0;
        
        for (Societe societe : societes) {
            if (societe.email() != null && !societe.email().trim().isEmpty()) {
                try {
                    addToGoogleContacts(societe);
                    syncCount++;
                } catch (Exception e) {
                    System.err.println("Erreur sync contact " + societe.nom() + ": " + e.getMessage());
                }
            }
        }
        
        response.put("success", true);
        response.put("message", syncCount + " contacts synchronisés avec Google");
        response.put("synchronized_count", syncCount);
        response.put("total_count", societes.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Ajoute une société spécifique à Google Contacts
     */
    @PostMapping("/{id}/add-to-google-contacts")
    public ResponseEntity<Map<String, Object>> addSocieteToGoogleContacts(@PathVariable Long id) {
        Societe societe = societeRepository.findById(id);
        
        if (societe == null) {
            throw new ResourceNotFoundException("Société", "id", id);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (!googleService.isContactsAvailable()) {
            response.put("success", false);
            response.put("message", "Google Contacts non disponible");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (societe.email() == null || societe.email().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email manquant pour la société");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean added = addToGoogleContacts(societe);
        
        if (added) {
            response.put("success", true);
            response.put("message", "Société ajoutée aux contacts Google");
            response.put("nom", societe.nom());
            response.put("email", societe.email());
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de l'ajout aux contacts Google");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques des sociétés
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSocietesStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Societe> toutes = societeRepository.findAll();
        stats.put("total", toutes.size());
        
        long clients = toutes.stream()
            .filter(s -> "client".equalsIgnoreCase(s.type()))
            .count();
        stats.put("clients", clients);
        
        long fournisseurs = toutes.stream()
            .filter(s -> "fournisseur".equalsIgnoreCase(s.type()))
            .count();
        stats.put("fournisseurs", fournisseurs);
        
        long manufactures = toutes.stream()
            .filter(s -> "manufacturier".equalsIgnoreCase(s.type()))
            .count();
        stats.put("manufacturiers", manufactures);
        
        long avecEmail = toutes.stream()
            .filter(s -> s.email() != null && !s.email().trim().isEmpty())
            .count();
        stats.put("avec_email", avecEmail);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Ajoute une société aux contacts Google (méthode privée)
     */
    private boolean addToGoogleContacts(Societe societe) {
        try {
            googleService.addClientToGoogleContacts(
                societe.nom(),
                societe.email(),
                societe.phone(),
                societe.type()
            ).get();
            
            return true;
        } catch (Exception e) {
            System.err.println("Erreur ajout contact Google: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valide les données d'une société
     */
    private void validateSociete(Societe societe) {
        Map<String, String> errors = new HashMap<>();
        
        if (societe.nom() == null || societe.nom().trim().isEmpty()) {
            errors.put("nom", "Le nom de la société est requis");
        }
        
        if (societe.type() == null || societe.type().trim().isEmpty()) {
            errors.put("type", "Le type de société est requis");
        } else {
            String type = societe.type().toLowerCase();
            if (!type.equals("client") && !type.equals("fournisseur") && !type.equals("manufacturier")) {
                errors.put("type", "Le type doit être 'client', 'fournisseur' ou 'manufacturier'");
            }
        }
        
        if (societe.email() != null && !societe.email().trim().isEmpty()) {
            if (!societe.email().contains("@")) {
                errors.put("email", "Format d'email invalide");
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation", errors);
        }
    }
}