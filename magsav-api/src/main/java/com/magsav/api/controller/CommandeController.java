package com.magsav.api.controller;

import com.magsav.api.exception.ResourceNotFoundException;
import com.magsav.api.exception.ValidationException;
import com.magsav.model.Commande;
import com.magsav.repo.CommandeRepository;
import com.magsav.service.google.GoogleIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des commandes dans MAGSAV 1.3
 */
@RestController
@RequestMapping("/api/v1/commandes")
@CrossOrigin(origins = "*")
public class CommandeController {

    private final CommandeRepository commandeRepository;
    private final GoogleIntegrationService googleService;

    public CommandeController() {
        this.commandeRepository = new CommandeRepository();
        this.googleService = GoogleIntegrationService.getInstance();
    }

    /**
     * Récupère toutes les commandes
     */
    @GetMapping
    public ResponseEntity<List<Commande>> getAllCommandes() {
        List<Commande> commandes = commandeRepository.findAll();
        return ResponseEntity.ok(commandes);
    }

    /**
     * Récupère une commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommandeById(@PathVariable Long id) {
        Commande commande = commandeRepository.findById(id);
        
        if (commande == null) {
            throw new ResourceNotFoundException("Commande", "id", id);
        }
        
        return ResponseEntity.ok(commande);
    }

    /**
     * Crée une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCommande(@RequestBody Commande commande) {
        // Validation des données
        validateCommande(commande);
        
        boolean saved = commandeRepository.save(commande);
        Map<String, Object> response = new HashMap<>();
        
        if (saved) {
            // Envoi email de confirmation si Gmail est disponible
            if (googleService.isGmailAvailable() && 
                commande.getFournisseur() != null && 
                commande.getFournisseur().email() != null) {
                
                sendOrderConfirmationEmail(commande);
            }
            
            response.put("success", true);
            response.put("message", "Commande créée avec succès");
            response.put("id", commande.getId());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la commande");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Met à jour une commande existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCommande(
            @PathVariable Long id, @RequestBody Commande commande) {
        
        // Vérifier que la commande existe
        Commande existing = commandeRepository.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Commande", "id", id);
        }
        
        // Validation des données
        validateCommande(commande);
        
        // Mettre à jour l'ID
        commande.setId(id);
        
        boolean updated = commandeRepository.save(commande);
        Map<String, Object> response = new HashMap<>();
        
        if (updated) {
            response.put("success", true);
            response.put("message", "Commande mise à jour avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour de la commande");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Supprime une commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCommande(@PathVariable Long id) {
        Commande commande = commandeRepository.findById(id);
        
        if (commande == null) {
            throw new ResourceNotFoundException("Commande", "id", id);
        }
        
        boolean deleted = commandeRepository.delete(id);
        Map<String, Object> response = new HashMap<>();
        
        if (deleted) {
            response.put("success", true);
            response.put("message", "Commande supprimée avec succès");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression de la commande");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Récupère les commandes par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Commande>> getCommandesByStatut(@PathVariable String statut) {
        try {
            Commande.StatutCommande statutEnum = Commande.StatutCommande.valueOf(statut.toUpperCase());
            List<Commande> commandes = commandeRepository.findByStatut(statutEnum);
            return ResponseEntity.ok(commandes);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Statut invalide: " + statut);
        }
    }

    /**
     * Récupère les commandes par fournisseur
     */
    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<Commande>> getCommandesByFournisseur(@PathVariable Long fournisseurId) {
        List<Commande> commandes = commandeRepository.findByFournisseurId(fournisseurId);
        return ResponseEntity.ok(commandes);
    }

    /**
     * Change le statut d'une commande
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, Object>> changeStatutCommande(
            @PathVariable Long id, @RequestBody Map<String, String> statutData) {
        
        Commande commande = commandeRepository.findById(id);
        if (commande == null) {
            throw new ResourceNotFoundException("Commande", "id", id);
        }
        
        String nouveauStatut = statutData.get("statut");
        if (nouveauStatut == null) {
            throw new ValidationException("Le statut est requis");
        }
        
        try {
            Commande.StatutCommande statutEnum = Commande.StatutCommande.valueOf(nouveauStatut.toUpperCase());
            commande.setStatut(statutEnum);
            
            boolean updated = commandeRepository.save(commande);
            Map<String, Object> response = new HashMap<>();
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Statut de la commande mis à jour");
                response.put("nouveau_statut", statutEnum.toString());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Erreur lors de la mise à jour du statut");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Statut invalide: " + nouveauStatut);
        }
    }

    /**
     * Envoie un email de confirmation de commande
     */
    @PostMapping("/{id}/send-confirmation")
    public ResponseEntity<Map<String, Object>> sendConfirmationEmail(@PathVariable Long id) {
        Commande commande = commandeRepository.findById(id);
        
        if (commande == null) {
            throw new ResourceNotFoundException("Commande", "id", id);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (!googleService.isGmailAvailable()) {
            response.put("success", false);
            response.put("message", "Service Gmail non disponible");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (commande.getFournisseur() == null || commande.getFournisseur().email() == null) {
            response.put("success", false);
            response.put("message", "Email du fournisseur non disponible");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean sent = sendOrderConfirmationEmail(commande);
        
        if (sent) {
            response.put("success", true);
            response.put("message", "Email de confirmation envoyé");
            response.put("destinataire", commande.getFournisseur().email());
        } else {
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi de l'email");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques des commandes
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCommandesStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Commande> toutes = commandeRepository.findAll();
        stats.put("total", toutes.size());
        
        long brouillons = toutes.stream()
            .filter(c -> c.getStatut() == Commande.StatutCommande.BROUILLON)
            .count();
        stats.put("brouillons", brouillons);
        
        long envoyees = toutes.stream()
            .filter(c -> c.getStatut() == Commande.StatutCommande.ENVOYEE)
            .count();
        stats.put("envoyees", envoyees);
        
        long recues = toutes.stream()
            .filter(c -> c.getStatut() == Commande.StatutCommande.RECUE)
            .count();
        stats.put("recues", recues);
        
        long facturees = toutes.stream()
            .filter(c -> c.getStatut() == Commande.StatutCommande.FACTUREE)
            .count();
        stats.put("facturees", facturees);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Envoie un email de confirmation de commande (méthode privée)
     */
    private boolean sendOrderConfirmationEmail(Commande commande) {
        try {
            String fournisseurEmail = commande.getFournisseur().email();
            String fournisseurNom = commande.getFournisseur().nom();
            String numeroCommande = commande.getNumeroCommande();
            String montantTotal = commande.getMontantTTC() != null ? 
                commande.getMontantTTC().toString() + " €" : "N/A";
            String dateCommande = commande.getDateCommande() != null ? 
                commande.getDateCommande().toString() : "N/A";
            
            googleService.sendOrderConfirmation(
                fournisseurEmail, fournisseurNom, numeroCommande, montantTotal, dateCommande
            ).get();
            
            return true;
        } catch (Exception e) {
            System.err.println("Erreur envoi email commande: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valide les données d'une commande
     */
    private void validateCommande(Commande commande) {
        Map<String, String> errors = new HashMap<>();
        
        if (commande.getNumeroCommande() == null || commande.getNumeroCommande().trim().isEmpty()) {
            errors.put("numeroCommande", "Le numéro de commande est requis");
        }
        
        if (commande.getType() == null) {
            errors.put("type", "Le type de commande est requis");
        }
        
        if (commande.getStatut() == null) {
            errors.put("statut", "Le statut de commande est requis");
        }
        
        if (commande.getDateCommande() == null) {
            errors.put("dateCommande", "La date de commande est requise");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation", errors);
        }
    }
}