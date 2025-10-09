package com.magsav.service;

import com.magsav.model.DemandeElevationPrivilege;
import com.magsav.model.User;
import com.magsav.repo.DemandeElevationPrivilegeRepository;
import com.magsav.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les demandes d'élévation de privilèges
 */
public class DemandeElevationPrivilegeService {
    
    private final DemandeElevationPrivilegeRepository demandeRepository;
    private final UserRepository userRepository;
    
    public DemandeElevationPrivilegeService() {
        this.demandeRepository = new DemandeElevationPrivilegeRepository();
        this.userRepository = new UserRepository();
    }
    
    /**
     * Créer une nouvelle demande d'élévation de privilèges
     */
    public DemandeElevationPrivilege creerDemande(int userId, User.Role roleDemande, 
                                                String justification, String createdBy) {
        // Récupérer l'utilisateur
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
        }
        
        User user = userOpt.get();
        
        // Vérifier que l'utilisateur peut demander cette élévation
        if (!peutDemanderElevation(user, roleDemande)) {
            throw new IllegalArgumentException(
                String.format("L'utilisateur %s (rôle: %s) ne peut pas demander le rôle %s", 
                             user.username(), user.role().getLabel(), roleDemande.getLabel()));
        }
        
        // Vérifier qu'il n'y a pas déjà une demande en attente
        List<DemandeElevationPrivilege> demandesEnAttente = 
            demandeRepository.findDemandesByUserId(userId).stream()
                .filter(DemandeElevationPrivilege::isEnAttente)
                .toList();
                
        if (!demandesEnAttente.isEmpty()) {
            throw new IllegalStateException("Une demande d'élévation est déjà en attente pour cet utilisateur");
        }
        
        // Créer la demande
        DemandeElevationPrivilege demande = new DemandeElevationPrivilege(
            userId, user.username(), user.fullName(),
            user.role(), roleDemande, justification, createdBy
        );
        
        // Sauvegarder
        if (demandeRepository.creerDemande(demande)) {
            System.out.printf("Demande d'élévation créée: %s → %s pour %s%n", 
                            user.role().getLabel(), roleDemande.getLabel(), user.username());
            return demande;
        } else {
            throw new RuntimeException("Erreur lors de la création de la demande d'élévation");
        }
    }
    
    /**
     * Créer une demande avec expiration (privilèges temporaires)
     */
    public DemandeElevationPrivilege creerDemandeTemporaire(int userId, User.Role roleDemande, 
                                                          String justification, String createdBy,
                                                          LocalDateTime expiresAt) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
        }
        
        User user = userOpt.get();
        
        DemandeElevationPrivilege demande = new DemandeElevationPrivilege(
            userId, user.username(), user.fullName(),
            user.role(), roleDemande, justification, createdBy, expiresAt
        );
        
        if (demandeRepository.creerDemande(demande)) {
            System.out.printf("Demande d'élévation temporaire créée: %s → %s pour %s (expire: %s)%n", 
                            user.role().getLabel(), roleDemande.getLabel(), 
                            user.username(), expiresAt);
            return demande;
        } else {
            throw new RuntimeException("Erreur lors de la création de la demande d'élévation temporaire");
        }
    }
    
    /**
     * Approuver une demande d'élévation
     */
    public boolean approuverDemande(int demandeId, String validatedBy, String notes) {
        Optional<DemandeElevationPrivilege> demandeOpt = demandeRepository.findById(demandeId);
        if (demandeOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande non trouvée avec l'ID: " + demandeId);
        }
        
        DemandeElevationPrivilege demande = demandeOpt.get();
        
        if (!demande.isEnAttente()) {
            throw new IllegalStateException("Cette demande n'est plus en attente");
        }
        
        // Mettre à jour le statut de la demande
        boolean demandeUpdated = demandeRepository.validerDemande(
            demandeId, DemandeElevationPrivilege.StatutDemande.APPROUVEE, validatedBy, notes
        );
        
        if (demandeUpdated) {
            // Mettre à jour le rôle de l'utilisateur
            boolean userUpdated = userRepository.updateUserRole(demande.getUserId(), demande.getRoleDemande());
            
            if (userUpdated) {
                System.out.printf("Demande d'élévation approuvée: %s a maintenant le rôle %s%n", 
                                demande.getUsername(), demande.getRoleDemande().getLabel());
                return true;
            } else {
                // Rollback du statut de la demande si l'update du user a échoué
                demandeRepository.validerDemande(
                    demandeId, DemandeElevationPrivilege.StatutDemande.EN_ATTENTE, null, null
                );
                throw new RuntimeException("Erreur lors de la mise à jour du rôle utilisateur");
            }
        }
        
        return false;
    }
    
    /**
     * Rejeter une demande d'élévation
     */
    public boolean rejeterDemande(int demandeId, String validatedBy, String notes) {
        Optional<DemandeElevationPrivilege> demandeOpt = demandeRepository.findById(demandeId);
        if (demandeOpt.isEmpty()) {
            throw new IllegalArgumentException("Demande non trouvée avec l'ID: " + demandeId);
        }
        
        DemandeElevationPrivilege demande = demandeOpt.get();
        
        if (!demande.isEnAttente()) {
            throw new IllegalStateException("Cette demande n'est plus en attente");
        }
        
        boolean updated = demandeRepository.validerDemande(
            demandeId, DemandeElevationPrivilege.StatutDemande.REJETEE, validatedBy, notes
        );
        
        if (updated) {
            System.out.printf("Demande d'élévation rejetée: %s pour %s - Raison: %s%n", 
                            demande.getRoleDemande().getLabel(), 
                            demande.getUsername(), 
                            notes != null ? notes : "Non spécifiée");
        }
        
        return updated;
    }
    
    /**
     * Récupérer toutes les demandes en attente
     */
    public List<DemandeElevationPrivilege> getDemandesEnAttente() {
        return demandeRepository.findDemandesEnAttente();
    }
    
    /**
     * Récupérer les demandes d'un utilisateur
     */
    public List<DemandeElevationPrivilege> getDemandesUtilisateur(int userId) {
        return demandeRepository.findDemandesByUserId(userId);
    }
    
    /**
     * Récupérer toutes les demandes avec pagination
     */
    public List<DemandeElevationPrivilege> getAllDemandes(int limit, int offset) {
        return demandeRepository.findAllDemandes(limit, offset);
    }
    
    /**
     * Compter le total des demandes
     */
    public int countDemandes() {
        return demandeRepository.countDemandes();
    }
    
    /**
     * Marquer les demandes expirées
     */
    public int marquerDemandesExpirees() {
        int count = demandeRepository.marquerDemandesExpirees();
        if (count > 0) {
            System.out.printf("%d demande(s) d'élévation marquée(s) comme expirée(s)%n", count);
        }
        return count;
    }
    
    /**
     * Vérifier si un utilisateur peut demander une élévation vers un rôle spécifique
     */
    private boolean peutDemanderElevation(User user, User.Role roleDemande) {
        User.Role roleActuel = user.role();
        
        // Un utilisateur ne peut pas demander un rôle inférieur ou égal au sien
        if (roleActuel.getPriority() >= roleDemande.getPriority()) {
            return false;
        }
        
        // Règles spécifiques
        switch (roleActuel) {
            case INTERMITTENT:
                // Les intermittents peuvent seulement demander le rôle technicien
                return roleDemande == User.Role.TECHNICIEN_MAG_SCENE;
                
            case TECHNICIEN_MAG_SCENE:
                // Les techniciens peuvent demander le rôle admin
                return roleDemande == User.Role.ADMIN;
                
            case ADMIN:
                // Les admins ont déjà le rôle le plus élevé
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * Obtenir les rôles disponibles pour une demande d'élévation selon le rôle actuel
     */
    public List<User.Role> getRolesDisponiblesPourElevation(User.Role roleActuel) {
        return switch (roleActuel) {
            case INTERMITTENT -> List.of(User.Role.TECHNICIEN_MAG_SCENE);
            case TECHNICIEN_MAG_SCENE -> List.of(User.Role.ADMIN);
            case ADMIN -> List.of(); // Aucune élévation possible
        };
    }
}