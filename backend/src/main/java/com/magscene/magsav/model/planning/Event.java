package com.magscene.magsav.model.planning;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un événement de planning
 * Peut être synchronisé avec Google Calendar
 */
@Entity
@Table(name = "planning_events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, name = "start_date_time")
    private LocalDateTime startDateTime;
    
    @Column(nullable = false, name = "end_date_time")
    private LocalDateTime endDateTime;
    
    @Column(name = "location")
    private String location;
    
    /**
     * Spécialités requises pour cet événement
     * Séparées par des points-virgules (ex: "Son;Éclairage;Vidéo")
     */
    @Column(name = "required_specialties", columnDefinition = "TEXT")
    private String requiredSpecialties;
    
    /**
     * Personnel assigné à cet événement
     * Format: "idPersonnel:spécialité;idPersonnel:spécialité"
     */
    @Column(name = "assigned_personnel", columnDefinition = "TEXT")
    private String assignedPersonnel;
    
    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType type;
    
    @Column(name = "priority_level")
    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.MEDIUM;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PLANNED;
    
    // === RELATIONS ===
    
    /**
     * Calendrier auquel appartient cet événement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;
    
    // === SYNCHRONISATION GOOGLE CALENDAR ===
    
    /**
     * Comptes Google Calendar associés à cet événement
     */
    @ManyToMany(mappedBy = "synchronizedEvents", fetch = FetchType.LAZY)
    private Set<GoogleCalendarAccount> googleAccounts = new HashSet<>();
    
    /**
     * ID de l'événement dans Google Calendar (pour la synchronisation)
     * Format: "idCompteGoogle:idEvenementGoogle"
     */
    @Column(name = "google_event_ids", columnDefinition = "TEXT")
    private String googleEventIds;
    
    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;
    
    @Column(name = "sync_enabled")
    private Boolean syncEnabled = true;
    
    // === MÉTADONNÉES ===
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "modified_by")
    private String modifiedBy;
    
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate = LocalDateTime.now();
    
    @Column(name = "color_code")
    private String colorCode; // Code couleur hexadecimal
    
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern; // Format iCal RRULE
    
    // === ENUMS ===
    
    public enum EventType {
        INTERVENTION,    // Intervention SAV
        INSTALLATION,    // Installation matériel
        MAINTENANCE,     // Maintenance préventive
        FORMATION,       // Formation personnel
        REUNION,         // Réunion d'équipe
        PRESTATION,      // Prestation client
        TRANSPORT,       // Transport/livraison
        AUTRE           // Autre type
    }
    
    public enum PriorityLevel {
        LOW("Basse"),
        MEDIUM("Moyenne"), 
        HIGH("Haute"),
        URGENT("Urgente");
        
        private final String label;
        
        PriorityLevel(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum EventStatus {
        PLANNED("Planifié"),
        IN_PROGRESS("En cours"),
        COMPLETED("Terminé"),
        CANCELLED("Annulé"),
        POSTPONED("Reporté");
        
        private final String label;
        
        EventStatus(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // === CONSTRUCTEURS ===
    
    public Event() {}
    
    public Event(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, EventType type) {
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.type = type;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }
    
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    
    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getRequiredSpecialties() {
        return requiredSpecialties;
    }
    
    public void setRequiredSpecialties(String requiredSpecialties) {
        this.requiredSpecialties = requiredSpecialties;
    }
    
    public String getAssignedPersonnel() {
        return assignedPersonnel;
    }
    
    public void setAssignedPersonnel(String assignedPersonnel) {
        this.assignedPersonnel = assignedPersonnel;
    }
    
    public EventType getType() {
        return type;
    }
    
    public void setType(EventType type) {
        this.type = type;
    }
    
    public PriorityLevel getPriority() {
        return priority;
    }
    
    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }
    
    public EventStatus getStatus() {
        return status;
    }
    
    public void setStatus(EventStatus status) {
        this.status = status;
    }
    
    public Set<GoogleCalendarAccount> getGoogleAccounts() {
        return googleAccounts;
    }
    
    public void setGoogleAccounts(Set<GoogleCalendarAccount> googleAccounts) {
        this.googleAccounts = googleAccounts;
    }
    
    public String getGoogleEventIds() {
        return googleEventIds;
    }
    
    public void setGoogleEventIds(String googleEventIds) {
        this.googleEventIds = googleEventIds;
    }
    
    public LocalDateTime getLastSyncDate() {
        return lastSyncDate;
    }
    
    public void setLastSyncDate(LocalDateTime lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }
    
    public Boolean getSyncEnabled() {
        return syncEnabled;
    }
    
    public void setSyncEnabled(Boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
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
    
    public String getColorCode() {
        return colorCode;
    }
    
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public String getRecurrencePattern() {
        return recurrencePattern;
    }
    
    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    
    public Calendar getCalendar() {
        return calendar;
    }
    
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Ajoute un compte Google Calendar à synchroniser
     */
    public void addGoogleAccount(GoogleCalendarAccount account) {
        this.googleAccounts.add(account);
        account.getSynchronizedEvents().add(this);
    }
    
    /**
     * Supprime un compte Google Calendar de la synchronisation
     */
    public void removeGoogleAccount(GoogleCalendarAccount account) {
        this.googleAccounts.remove(account);
        account.getSynchronizedEvents().remove(this);
    }
    
    /**
     * Retourne les spécialités requises sous forme de liste
     */
    public Set<String> getRequiredSpecialtiesSet() {
        Set<String> specialties = new HashSet<>();
        if (requiredSpecialties != null && !requiredSpecialties.trim().isEmpty()) {
            String[] parts = requiredSpecialties.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    specialties.add(part.trim());
                }
            }
        }
        return specialties;
    }
    
    /**
     * Définit les spécialités requises à partir d'un Set
     */
    public void setRequiredSpecialtiesSet(Set<String> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            this.requiredSpecialties = "";
        } else {
            this.requiredSpecialties = String.join(";", specialties);
        }
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}