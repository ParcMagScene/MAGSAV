package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Modèle représentant un utilisateur du système MAGSAV
 * Les utilisateurs peuvent être des ADMIN (accès complet via JavaFX) 
 * ou des USER (accès web pour créer des demandes d'intervention)
 */
public record User(
    Integer id,
    String username,
    String email,
    String passwordHash,
    Role role,
    String fullName,
    String phone,
    Long companyId, // Référence vers la société (Mag Scène ou autre)
    String position, // Poste/fonction dans l'entreprise
    String avatarPath, // Chemin vers la photo de profil
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime lastLogin,
    String resetToken,
    LocalDateTime resetTokenExpires
) {
    
    /**
     * Rôles disponibles dans le système MAGSAV
     */
    public enum Role {
        ADMIN("Administrateur", "Accès complet application desktop + API complète"),
        TECHNICIEN_MAG_SCENE("Technicien Mag Scène", "Visualisation complète + demandes d'intervention/pièces/matériel"),
        INTERMITTENT("Intermittent", "Visualisation seule + possibilité de demander élévation des droits");
        
        private final String label;
        private final String description;
        
        Role(String label, String description) {
            this.label = label;
            this.description = description;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * Priorité du rôle (plus élevé = plus de privilèges)
         */
        public int getPriority() {
            return switch (this) {
                case INTERMITTENT -> 1;
                case TECHNICIEN_MAG_SCENE -> 2;
                case ADMIN -> 3;
            };
        }
        
        @Override
        public String toString() {
            return label;
        }
        
        public static Role fromString(String str) {
            if (str == null) return INTERMITTENT;
            for (Role role : values()) {
                if (role.name().equals(str) || role.label.equals(str)) {
                    return role;
                }
            }
            return INTERMITTENT;
        }
    }
    
    /**
     * Constructeur pour création d'un nouvel utilisateur
     */
    public User(String username, String email, String passwordHash, Role role, String fullName, String phone) {
        this(null, username, email, passwordHash, role, fullName, phone, null, null, null, true, 
             LocalDateTime.now(), null, null, null);
    }
    
    /**
     * Constructeur pour création d'un utilisateur simple (INTERMITTENT par défaut)
     */
    public User(String username, String email, String passwordHash, String fullName) {
        this(username, email, passwordHash, Role.INTERMITTENT, fullName, null);
    }
    
    /**
     * Constructeur pour création d'un utilisateur avec société et poste
     */
    public User(String username, String email, String passwordHash, Role role, String fullName, String phone, Long companyId, String position) {
        this(null, username, email, passwordHash, role, fullName, phone, companyId, position, null, true, 
             LocalDateTime.now(), null, null, null);
    }
    
    // Méthodes utilitaires
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isTechnicienMagScene() {
        return role == Role.TECHNICIEN_MAG_SCENE;
    }
    
    public boolean isIntermittent() {
        return role == Role.INTERMITTENT;
    }
    
    public boolean canAccessDesktop() {
        return isAdmin();
    }
    
    public boolean canAccessWeb() {
        return isActive; // Tous les utilisateurs actifs peuvent accéder au web
    }
    
    // Permissions spécifiques par rôle
    public boolean canViewAllProducts() {
        return isAdmin() || isTechnicienMagScene();
    }
    
    public boolean canViewAllInterventions() {
        return isAdmin() || isTechnicienMagScene();
    }
    
    public boolean canCreateDemandeIntervention() {
        return isAdmin() || isTechnicienMagScene();
    }
    
    public boolean canCreateDemandePieces() {
        return isAdmin() || isTechnicienMagScene();
    }
    
    public boolean canCreateDemandeMateriel() {
        return isAdmin() || isTechnicienMagScene();
    }
    
    public boolean canValidateDemandes() {
        return isAdmin(); // Seuls les admins peuvent valider
    }
    
    public boolean canManageUsers() {
        return isAdmin(); // Seuls les admins peuvent gérer les utilisateurs
    }
    
    public boolean canRequestPrivilegeElevation() {
        return isIntermittent(); // Seuls les intermittents peuvent demander une élévation
    }
    
    public boolean canGrantPrivileges() {
        return isAdmin(); // Seuls les admins peuvent accorder des privilèges
    }
    
    public boolean hasFullApiAccess() {
        return isAdmin(); // API complète uniquement pour les admins
    }
    
    /**
     * Vérifie si le token de reset est valide
     */
    public boolean isResetTokenValid() {
        return resetToken != null && 
               resetTokenExpires != null && 
               resetTokenExpires.isAfter(LocalDateTime.now());
    }
    
    /**
     * Crée une copie avec une nouvelle date de dernière connexion
     */
    public User withLastLogin(LocalDateTime lastLogin) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, resetToken, resetTokenExpires);
    }
    
    /**
     * Crée une copie avec un nouveau token de reset
     */
    public User withResetToken(String resetToken, LocalDateTime expires) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, resetToken, expires);
    }
    
    /**
     * Crée une copie avec un nouveau hash de mot de passe
     */
    public User withNewPassword(String newPasswordHash) {
        return new User(id, username, email, newPasswordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, null, null);
    }
    
    /**
     * Crée une copie avec un statut actif/inactif modifié
     */
    public User withActiveStatus(boolean active) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, active, createdAt, lastLogin, resetToken, resetTokenExpires);
    }
    
    /**
     * Crée une copie avec une nouvelle société
     */
    public User withCompany(Long companyId) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, resetToken, resetTokenExpires);
    }
    
    /**
     * Crée une copie avec un nouveau poste
     */
    public User withPosition(String position) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, resetToken, resetTokenExpires);
    }
    
    /**
     * Crée une copie avec un nouvel avatar
     */
    public User withAvatar(String avatarPath) {
        return new User(id, username, email, passwordHash, role, fullName, phone, 
                       companyId, position, avatarPath, isActive, createdAt, lastLogin, resetToken, resetTokenExpires);
    }
    
    /**
     * Représentation sécurisée pour les logs (sans mot de passe)
     */
    public String toSafeString() {
        return String.format("User{id=%d, username='%s', email='%s', role=%s, active=%b}", 
                           id, username, email, role, isActive);
    }
}