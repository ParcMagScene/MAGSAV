package com.magscene.magsav.model.planning;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un compte Google Calendar d'un utilisateur
 * Permet la synchronisation avec plusieurs agendas Google par utilisateur
 */
@Entity
@Table(name = "google_calendar_accounts", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "google_account_email"}))
public class GoogleCalendarAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID de l'utilisateur MAGSAV propriétaire de ce compte Google
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    /**
     * Email du compte Google
     */
    @Column(name = "google_account_email", nullable = false)
    private String googleAccountEmail;
    
    /**
     * Nom d'affichage du compte (ex: "Compte perso", "Mag Scène Pro")
     */
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    /**
     * ID du calendrier Google principal
     */
    @Column(name = "google_calendar_id")
    private String googleCalendarId;
    
    /**
     * Nom du calendrier Google
     */
    @Column(name = "google_calendar_name")
    private String googleCalendarName;
    
    // === TOKENS D'AUTHENTIFICATION ===
    
    /**
     * Token d'accès Google OAuth2
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    /**
     * Token de rafraîchissement Google OAuth2
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    /**
     * Date d'expiration du token d'accès
     */
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;
    
    // === CONFIGURATION SYNCHRONISATION ===
    
    /**
     * Synchronisation activée ou non
     */
    @Column(name = "sync_enabled")
    private Boolean syncEnabled = true;
    
    /**
     * Direction de synchronisation
     */
    @Column(name = "sync_direction")
    @Enumerated(EnumType.STRING)
    private SyncDirection syncDirection = SyncDirection.BIDIRECTIONAL;
    
    /**
     * Spécialités à synchroniser pour ce compte
     * Si null/vide, toutes les spécialités sont synchronisées
     */
    @Column(name = "sync_specialties", columnDefinition = "TEXT")
    private String syncSpecialties;
    
    /**
     * Préfixe à ajouter aux titres d'événements synchronisés vers Google
     */
    @Column(name = "event_title_prefix")
    private String eventTitlePrefix = "[MAGSAV]";
    
    /**
     * Couleur par défaut des événements synchronisés (ID couleur Google)
     */
    @Column(name = "default_color_id")
    private String defaultColorId = "1"; // Bleu par défaut
    
    // === RELATIONS ===
    
    /**
     * Événements synchronisés avec ce compte Google
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "google_sync_events",
        joinColumns = @JoinColumn(name = "google_account_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> synchronizedEvents = new HashSet<>();
    
    // === MÉTADONNÉES ===
    
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;
    
    @Column(name = "last_sync_status")
    @Enumerated(EnumType.STRING)
    private SyncStatus lastSyncStatus = SyncStatus.NEVER_SYNCED;
    
    @Column(name = "sync_error_message", columnDefinition = "TEXT")
    private String syncErrorMessage;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // === ENUMS ===
    
    public enum SyncDirection {
        MAGSAV_TO_GOOGLE("MAGSAV → Google"),
        GOOGLE_TO_MAGSAV("Google → MAGSAV"),
        BIDIRECTIONAL("Bidirectionnel");
        
        private final String label;
        
        SyncDirection(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum SyncStatus {
        NEVER_SYNCED("Jamais synchronisé"),
        SUCCESS("Succès"),
        PARTIAL_SUCCESS("Succès partiel"),
        FAILED("Échec"),
        IN_PROGRESS("En cours"),
        TOKEN_EXPIRED("Token expiré"),
        QUOTA_EXCEEDED("Quota dépassé");
        
        private final String label;
        
        SyncStatus(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // === CONSTRUCTEURS ===
    
    public GoogleCalendarAccount() {}
    
    public GoogleCalendarAccount(String userId, String googleAccountEmail, String displayName) {
        this.userId = userId;
        this.googleAccountEmail = googleAccountEmail;
        this.displayName = displayName;
        this.createdDate = LocalDateTime.now();
    }
    
    // === GETTERS ET SETTERS ===
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getGoogleAccountEmail() {
        return googleAccountEmail;
    }
    
    public void setGoogleAccountEmail(String googleAccountEmail) {
        this.googleAccountEmail = googleAccountEmail;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getGoogleCalendarId() {
        return googleCalendarId;
    }
    
    public void setGoogleCalendarId(String googleCalendarId) {
        this.googleCalendarId = googleCalendarId;
    }
    
    public String getGoogleCalendarName() {
        return googleCalendarName;
    }
    
    public void setGoogleCalendarName(String googleCalendarName) {
        this.googleCalendarName = googleCalendarName;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }
    
    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
    
    public Boolean getSyncEnabled() {
        return syncEnabled;
    }
    
    public void setSyncEnabled(Boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
    
    public SyncDirection getSyncDirection() {
        return syncDirection;
    }
    
    public void setSyncDirection(SyncDirection syncDirection) {
        this.syncDirection = syncDirection;
    }
    
    public String getSyncSpecialties() {
        return syncSpecialties;
    }
    
    public void setSyncSpecialties(String syncSpecialties) {
        this.syncSpecialties = syncSpecialties;
    }
    
    public String getEventTitlePrefix() {
        return eventTitlePrefix;
    }
    
    public void setEventTitlePrefix(String eventTitlePrefix) {
        this.eventTitlePrefix = eventTitlePrefix;
    }
    
    public String getDefaultColorId() {
        return defaultColorId;
    }
    
    public void setDefaultColorId(String defaultColorId) {
        this.defaultColorId = defaultColorId;
    }
    
    public Set<Event> getSynchronizedEvents() {
        return synchronizedEvents;
    }
    
    public void setSynchronizedEvents(Set<Event> synchronizedEvents) {
        this.synchronizedEvents = synchronizedEvents;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastSyncDate() {
        return lastSyncDate;
    }
    
    public void setLastSyncDate(LocalDateTime lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }
    
    public SyncStatus getLastSyncStatus() {
        return lastSyncStatus;
    }
    
    public void setLastSyncStatus(SyncStatus lastSyncStatus) {
        this.lastSyncStatus = lastSyncStatus;
    }
    
    public String getSyncErrorMessage() {
        return syncErrorMessage;
    }
    
    public void setSyncErrorMessage(String syncErrorMessage) {
        this.syncErrorMessage = syncErrorMessage;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Vérifie si le token d'accès est expiré
     */
    public boolean isTokenExpired() {
        return tokenExpiry != null && LocalDateTime.now().isAfter(tokenExpiry);
    }
    
    /**
     * Retourne les spécialités à synchroniser sous forme de Set
     */
    public Set<String> getSyncSpecialtiesSet() {
        Set<String> specialties = new HashSet<>();
        if (syncSpecialties != null && !syncSpecialties.trim().isEmpty()) {
            String[] parts = syncSpecialties.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    specialties.add(part.trim());
                }
            }
        }
        return specialties;
    }
    
    /**
     * Définit les spécialités à synchroniser à partir d'un Set
     */
    public void setSyncSpecialtiesSet(Set<String> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            this.syncSpecialties = "";
        } else {
            this.syncSpecialties = String.join(";", specialties);
        }
    }
    
    /**
     * Vérifie si une spécialité doit être synchronisée
     */
    public boolean shouldSyncSpecialty(String specialty) {
        if (syncSpecialties == null || syncSpecialties.trim().isEmpty()) {
            return true; // Synchroniser toutes les spécialités si rien n'est spécifié
        }
        return getSyncSpecialtiesSet().contains(specialty);
    }
    
    /**
     * Ajoute un événement à synchroniser
     */
    public void addSynchronizedEvent(Event event) {
        this.synchronizedEvents.add(event);
        event.getGoogleAccounts().add(this);
    }
    
    /**
     * Supprime un événement de la synchronisation
     */
    public void removeSynchronizedEvent(Event event) {
        this.synchronizedEvents.remove(event);
        event.getGoogleAccounts().remove(this);
    }
    
    /**
     * Met à jour le statut de synchronisation
     */
    public void updateSyncStatus(SyncStatus status, String errorMessage) {
        this.lastSyncStatus = status;
        this.lastSyncDate = LocalDateTime.now();
        this.syncErrorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "GoogleCalendarAccount{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", googleAccountEmail='" + googleAccountEmail + '\'' +
                ", syncEnabled=" + syncEnabled +
                ", lastSyncStatus=" + lastSyncStatus +
                '}';
    }
}
