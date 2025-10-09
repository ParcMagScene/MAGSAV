package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Modèle pour les demandes d'élévation de privilèges
 * Permet aux intermittents de demander des droits de technicien
 */
public class DemandeElevationPrivilege {
    private Integer id;
    private Integer userId;
    private String username;
    private String fullName;
    private User.Role roleActuel;
    private User.Role roleDemande;
    private String justification;
    private StatutDemande statut;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private String validatedBy;
    private String notesValidation;
    private LocalDateTime expiresAt;
    
    /**
     * Énumération des statuts possibles pour une demande d'élévation
     */
    public enum StatutDemande {
        EN_ATTENTE("En attente", "La demande est en cours d'examen"),
        APPROUVEE("Approuvée", "La demande a été approuvée"),
        REJETEE("Rejetée", "La demande a été rejetée"),
        EXPIREE("Expirée", "La demande a expiré");
        
        private final String label;
        private final String description;
        
        StatutDemande(String label, String description) {
            this.label = label;
            this.description = description;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return label;
        }
        
        public static StatutDemande fromString(String str) {
            if (str == null) return EN_ATTENTE;
            for (StatutDemande statut : values()) {
                if (statut.name().equalsIgnoreCase(str) || statut.label.equals(str)) {
                    return statut;
                }
            }
            return EN_ATTENTE;
        }
    }
    
    // Constructeur par défaut
    public DemandeElevationPrivilege() {
    }
    
    // Constructeur complet
    public DemandeElevationPrivilege(Integer id, Integer userId, String username, String fullName,
                                   User.Role roleActuel, User.Role roleDemande, String justification,
                                   StatutDemande statut, String createdBy, LocalDateTime createdAt,
                                   LocalDateTime validatedAt, String validatedBy, String notesValidation,
                                   LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.roleActuel = roleActuel;
        this.roleDemande = roleDemande;
        this.justification = justification;
        this.statut = statut;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.validatedAt = validatedAt;
        this.validatedBy = validatedBy;
        this.notesValidation = notesValidation;
        this.expiresAt = expiresAt;
    }
    
    /**
     * Constructeur pour création d'une nouvelle demande
     */
    public DemandeElevationPrivilege(Integer userId, String username, String fullName, 
                                   User.Role roleActuel, User.Role roleDemande, 
                                   String justification, String createdBy) {
        this(null, userId, username, fullName, roleActuel, roleDemande, justification,
             StatutDemande.EN_ATTENTE, createdBy, LocalDateTime.now(), 
             null, null, null, null);
    }
    
    /**
     * Constructeur pour demande avec expiration (privilèges temporaires)
     */
    public DemandeElevationPrivilege(Integer userId, String username, String fullName, 
                                   User.Role roleActuel, User.Role roleDemande, 
                                   String justification, String createdBy, 
                                   LocalDateTime expiresAt) {
        this(null, userId, username, fullName, roleActuel, roleDemande, justification,
             StatutDemande.EN_ATTENTE, createdBy, LocalDateTime.now(), 
             null, null, null, expiresAt);
    }
    
    // Méthodes utilitaires
    public boolean isEnAttente() {
        return statut == StatutDemande.EN_ATTENTE;
    }
    
    public boolean isApprouvee() {
        return statut == StatutDemande.APPROUVEE;
    }
    
    public boolean isRejetee() {
        return statut == StatutDemande.REJETEE;
    }
    
    public boolean isExpiree() {
        return statut == StatutDemande.EXPIREE || 
               (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()));
    }
    
    public boolean isTemporaire() {
        return expiresAt != null;
    }
    
    public String getDisplayName() {
        return String.format("%s → %s (%s)", 
                           roleActuel.getLabel(), 
                           roleDemande.getLabel(), 
                           statut.getLabel());
    }
    
    /**
     * Crée une copie avec un nouveau statut
     */
    public DemandeElevationPrivilege withStatut(StatutDemande nouveauStatut, String validatedBy, String notes) {
        return new DemandeElevationPrivilege(
            id, userId, username, fullName, roleActuel, roleDemande, justification,
            nouveauStatut, createdBy, createdAt, LocalDateTime.now(), validatedBy, notes, expiresAt
        );
    }
    
    /**
     * Vérifie si la demande concerne une élévation vers technicien
     */
    public boolean isElevationVersTechnicien() {
        return roleDemande == User.Role.TECHNICIEN_MAG_SCENE;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public User.Role getRoleActuel() {
        return roleActuel;
    }
    
    public void setRoleActuel(User.Role roleActuel) {
        this.roleActuel = roleActuel;
    }
    
    public User.Role getRoleDemande() {
        return roleDemande;
    }
    
    public void setRoleDemande(User.Role roleDemande) {
        this.roleDemande = roleDemande;
    }
    
    public String getJustification() {
        return justification;
    }
    
    public void setJustification(String justification) {
        this.justification = justification;
    }
    
    public StatutDemande getStatut() {
        return statut;
    }
    
    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }
    
    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
    
    public String getValidatedBy() {
        return validatedBy;
    }
    
    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;  
    }
    
    public String getNotesValidation() {
        return notesValidation;
    }
    
    public void setNotesValidation(String notesValidation) {
        this.notesValidation = notesValidation;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @Override
    public String toString() {
        return String.format("DemandeElevationPrivilege{id=%d, username='%s', %s→%s, statut=%s}",
                           id, username, roleActuel, roleDemande, statut);
    }
}