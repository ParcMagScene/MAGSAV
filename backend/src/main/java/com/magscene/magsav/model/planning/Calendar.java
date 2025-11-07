package com.magscene.magsav.model.planning;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un calendrier de planning
 * Organisé par spécialités et équipes
 */
@Entity
@Table(name = "planning_calendars")
public class Calendar {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Spécialités associées à ce calendrier
     * Séparées par des points-virgules (ex: "Son;Éclairage;Vidéo")
     */
    @Column(name = "associated_specialties", columnDefinition = "TEXT")
    private String associatedSpecialties;
    
    /**
     * Type de calendrier
     */
    @Column(name = "calendar_type")
    @Enumerated(EnumType.STRING)
    private CalendarType type = CalendarType.SPECIALTY;
    
    /**
     * Propriétaire du calendrier (utilisateur ou équipe)
     */
    @Column(name = "owner_id")
    private String ownerId;
    
    @Column(name = "owner_type")
    @Enumerated(EnumType.STRING)
    private OwnerType ownerType = OwnerType.USER;
    
    // === CONFIGURATION VISUELLE ===
    
    @Column(name = "color_code")
    private String colorCode = "#3498db"; // Bleu par défaut
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_visible")
    private Boolean isVisible = true;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    // === RELATIONS ===
    
    /**
     * Événements de ce calendrier
     */
    @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Event> events = new HashSet<>();
    
    // === PERMISSIONS ===
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "allowed_viewers", columnDefinition = "TEXT")
    private String allowedViewers; // IDs utilisateurs séparés par ";"
    
    @Column(name = "allowed_editors", columnDefinition = "TEXT") 
    private String allowedEditors; // IDs utilisateurs séparés par ";"
    
    // === SYNCHRONISATION ===
    
    @Column(name = "sync_with_google")
    private Boolean syncWithGoogle = false;
    
    @Column(name = "google_calendar_mapping", columnDefinition = "TEXT")
    private String googleCalendarMapping; // Format: "idCompteGoogle:idCalendrierGoogle"
    
    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;
    
    // === MÉTADONNÉES ===
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "modified_by")
    private String modifiedBy;
    
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate = LocalDateTime.now();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // === ENUMS ===
    
    public enum CalendarType {
        SPECIALTY("Spécialité"),
        TEAM("Équipe"),
        PERSONAL("Personnel"),
        PROJECT("Projet"),
        RESOURCE("Ressource"),
        GENERAL("Général");
        
        private final String label;
        
        CalendarType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum OwnerType {
        USER("Utilisateur"),
        TEAM("Équipe"), 
        SYSTEM("Système");
        
        private final String label;
        
        OwnerType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // === CONSTRUCTEURS ===
    
    public Calendar() {}
    
    public Calendar(String name, CalendarType type, String ownerId) {
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
    }
    
    // === GETTERS ET SETTERS ===
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.modifiedDate = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAssociatedSpecialties() {
        return associatedSpecialties;
    }
    
    public void setAssociatedSpecialties(String associatedSpecialties) {
        this.associatedSpecialties = associatedSpecialties;
    }
    
    public CalendarType getType() {
        return type;
    }
    
    public void setType(CalendarType type) {
        this.type = type;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public OwnerType getOwnerType() {
        return ownerType;
    }
    
    public void setOwnerType(OwnerType ownerType) {
        this.ownerType = ownerType;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getIsVisible() {
        return isVisible;
    }
    
    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Set<Event> getEvents() {
        return events;
    }
    
    public void setEvents(Set<Event> events) {
        this.events = events;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getAllowedViewers() {
        return allowedViewers;
    }
    
    public void setAllowedViewers(String allowedViewers) {
        this.allowedViewers = allowedViewers;
    }
    
    public String getAllowedEditors() {
        return allowedEditors;
    }
    
    public void setAllowedEditors(String allowedEditors) {
        this.allowedEditors = allowedEditors;
    }
    
    public Boolean getSyncWithGoogle() {
        return syncWithGoogle;
    }
    
    public void setSyncWithGoogle(Boolean syncWithGoogle) {
        this.syncWithGoogle = syncWithGoogle;
    }
    
    public String getGoogleCalendarMapping() {
        return googleCalendarMapping;
    }
    
    public void setGoogleCalendarMapping(String googleCalendarMapping) {
        this.googleCalendarMapping = googleCalendarMapping;
    }
    
    public LocalDateTime getLastSyncDate() {
        return lastSyncDate;
    }
    
    public void setLastSyncDate(LocalDateTime lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getModifiedBy() {
        return modifiedBy;
    }
    
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Retourne les spécialités associées sous forme de Set
     */
    public Set<String> getAssociatedSpecialtiesSet() {
        Set<String> specialties = new HashSet<>();
        if (associatedSpecialties != null && !associatedSpecialties.trim().isEmpty()) {
            String[] parts = associatedSpecialties.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    specialties.add(part.trim());
                }
            }
        }
        return specialties;
    }
    
    /**
     * Définit les spécialités associées à partir d'un Set
     */
    public void setAssociatedSpecialtiesSet(Set<String> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            this.associatedSpecialties = "";
        } else {
            this.associatedSpecialties = String.join(";", specialties);
        }
    }
    
    /**
     * Vérifie si une spécialité est associée à ce calendrier
     */
    public boolean hasSpecialty(String specialty) {
        return getAssociatedSpecialtiesSet().contains(specialty);
    }
    
    /**
     * Ajoute un événement à ce calendrier
     */
    public void addEvent(Event event) {
        this.events.add(event);
        event.setCalendar(this);
    }
    
    /**
     * Supprime un événement de ce calendrier
     */
    public void removeEvent(Event event) {
        this.events.remove(event);
        if (event.getCalendar() == this) {
            event.setCalendar(null);
        }
    }
    
    /**
     * Retourne les utilisateurs autorisés à voir ce calendrier
     */
    public Set<String> getAllowedViewersSet() {
        Set<String> viewers = new HashSet<>();
        if (allowedViewers != null && !allowedViewers.trim().isEmpty()) {
            String[] parts = allowedViewers.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    viewers.add(part.trim());
                }
            }
        }
        return viewers;
    }
    
    /**
     * Retourne les utilisateurs autorisés à modifier ce calendrier
     */
    public Set<String> getAllowedEditorsSet() {
        Set<String> editors = new HashSet<>();
        if (allowedEditors != null && !allowedEditors.trim().isEmpty()) {
            String[] parts = allowedEditors.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    editors.add(part.trim());
                }
            }
        }
        return editors;
    }
    
    /**
     * Vérifie si un utilisateur peut voir ce calendrier
     */
    public boolean canUserView(String userId) {
        if (isPublic || ownerId.equals(userId)) {
            return true;
        }
        return getAllowedViewersSet().contains(userId) || getAllowedEditorsSet().contains(userId);
    }
    
    /**
     * Vérifie si un utilisateur peut modifier ce calendrier
     */
    public boolean canUserEdit(String userId) {
        if (ownerId.equals(userId)) {
            return true;
        }
        return getAllowedEditorsSet().contains(userId);
    }
    
    @Override
    public String toString() {
        return "Calendar{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", ownerId='" + ownerId + '\'' +
                ", isVisible=" + isVisible +
                ", eventsCount=" + (events != null ? events.size() : 0) +
                '}';
    }
}