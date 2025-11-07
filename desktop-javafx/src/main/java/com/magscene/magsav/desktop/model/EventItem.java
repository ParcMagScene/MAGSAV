package com.magscene.magsav.desktop.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Item représentant un événement de planning pour l'interface JavaFX
 * Utilise des propriétés observables pour la liaison de données
 */
public class EventItem {
    
    // Propriétés de base
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> startDateTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> endDateTime = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    
    // Spécialités et personnel
    private Set<String> requiredSpecialties = new HashSet<>();
    private Set<String> assignedPersonnel = new HashSet<>();
    
    // Type et statut
    private final ObjectProperty<EventType> type = new SimpleObjectProperty<>(EventType.AUTRE);
    private final ObjectProperty<EventStatus> status = new SimpleObjectProperty<>(EventStatus.PLANNED);
    private final ObjectProperty<PriorityLevel> priority = new SimpleObjectProperty<>(PriorityLevel.MEDIUM);
    
    // Synchronisation Google
    private final BooleanProperty syncEnabled = new SimpleBooleanProperty(true);
    private Set<Long> googleAccountIds = new HashSet<>();
    private final ObjectProperty<LocalDateTime> lastSyncDate = new SimpleObjectProperty<>();
    
    // Métadonnées
    private final ObjectProperty<LocalDateTime> createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
    private final ObjectProperty<LocalDateTime> modifiedDate = new SimpleObjectProperty<>(LocalDateTime.now());
    private final StringProperty createdBy = new SimpleStringProperty();
    private final StringProperty colorCode = new SimpleStringProperty("#3498db");
    
    // Récurrence
    private final BooleanProperty isRecurring = new SimpleBooleanProperty(false);
    private final StringProperty recurrencePattern = new SimpleStringProperty();
    
    // === ENUMS ===
    
    public enum EventType {
        INTERVENTION("Intervention"),
        INSTALLATION("Installation"),
        MAINTENANCE("Maintenance"),
        FORMATION("Formation"),
        REUNION("Réunion"),
        PRESTATION("Prestation"),
        TRANSPORT("Transport"),
        AUTRE("Autre");
        
        private final String label;
        
        EventType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
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
        
        @Override
        public String toString() {
            return label;
        }
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
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    // === CONSTRUCTEURS ===
    
    public EventItem() {}
    
    public EventItem(String title, LocalDateTime start, LocalDateTime end, EventType type) {
        setTitle(title);
        setStartDateTime(start);
        setEndDateTime(end);
        setType(type);
    }
    
    // === GETTERS ET SETTERS PROPRIÉTÉS ===
    
    // ID
    public Long getId() { return id.get(); }
    public void setId(Long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    // Titre
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); updateModifiedDate(); }
    public StringProperty titleProperty() { return title; }
    
    // Description
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); updateModifiedDate(); }
    public StringProperty descriptionProperty() { return description; }
    
    // Date/heure début
    public LocalDateTime getStartDateTime() { return startDateTime.get(); }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime.set(startDateTime); updateModifiedDate(); }
    public ObjectProperty<LocalDateTime> startDateTimeProperty() { return startDateTime; }
    
    // Date/heure fin
    public LocalDateTime getEndDateTime() { return endDateTime.get(); }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime.set(endDateTime); updateModifiedDate(); }
    public ObjectProperty<LocalDateTime> endDateTimeProperty() { return endDateTime; }
    
    // Lieu
    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); updateModifiedDate(); }
    public StringProperty locationProperty() { return location; }
    
    // Type
    public EventType getType() { return type.get(); }
    public void setType(EventType type) { this.type.set(type); updateModifiedDate(); }
    public ObjectProperty<EventType> typeProperty() { return type; }
    
    // Statut
    public EventStatus getStatus() { return status.get(); }
    public void setStatus(EventStatus status) { this.status.set(status); updateModifiedDate(); }
    public ObjectProperty<EventStatus> statusProperty() { return status; }
    
    // Priorité
    public PriorityLevel getPriority() { return priority.get(); }
    public void setPriority(PriorityLevel priority) { this.priority.set(priority); updateModifiedDate(); }
    public ObjectProperty<PriorityLevel> priorityProperty() { return priority; }
    
    // Synchronisation
    public boolean isSyncEnabled() { return syncEnabled.get(); }
    public void setSyncEnabled(boolean syncEnabled) { this.syncEnabled.set(syncEnabled); }
    public BooleanProperty syncEnabledProperty() { return syncEnabled; }
    
    // Dernière synchronisation
    public LocalDateTime getLastSyncDate() { return lastSyncDate.get(); }
    public void setLastSyncDate(LocalDateTime lastSyncDate) { this.lastSyncDate.set(lastSyncDate); }
    public ObjectProperty<LocalDateTime> lastSyncDateProperty() { return lastSyncDate; }
    
    // Date création
    public LocalDateTime getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate.set(createdDate); }
    public ObjectProperty<LocalDateTime> createdDateProperty() { return createdDate; }
    
    // Date modification
    public LocalDateTime getModifiedDate() { return modifiedDate.get(); }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate.set(modifiedDate); }
    public ObjectProperty<LocalDateTime> modifiedDateProperty() { return modifiedDate; }
    
    // Créé par
    public String getCreatedBy() { return createdBy.get(); }
    public void setCreatedBy(String createdBy) { this.createdBy.set(createdBy); }
    public StringProperty createdByProperty() { return createdBy; }
    
    // Couleur
    public String getColorCode() { return colorCode.get(); }
    public void setColorCode(String colorCode) { this.colorCode.set(colorCode); updateModifiedDate(); }
    public StringProperty colorCodeProperty() { return colorCode; }
    
    // Récurrence
    public boolean isRecurring() { return isRecurring.get(); }
    public void setRecurring(boolean recurring) { this.isRecurring.set(recurring); updateModifiedDate(); }
    public BooleanProperty recurringProperty() { return isRecurring; }
    
    public String getRecurrencePattern() { return recurrencePattern.get(); }
    public void setRecurrencePattern(String pattern) { this.recurrencePattern.set(pattern); updateModifiedDate(); }
    public StringProperty recurrencePatternProperty() { return recurrencePattern; }
    
    // === GESTION DES SPÉCIALITÉS ===
    
    public Set<String> getRequiredSpecialties() {
        return new HashSet<>(requiredSpecialties);
    }
    
    public void setRequiredSpecialties(Set<String> specialties) {
        this.requiredSpecialties = specialties != null ? new HashSet<>(specialties) : new HashSet<>();
        updateModifiedDate();
    }
    
    public void addRequiredSpecialty(String specialty) {
        this.requiredSpecialties.add(specialty);
        updateModifiedDate();
    }
    
    public void removeRequiredSpecialty(String specialty) {
        this.requiredSpecialties.remove(specialty);
        updateModifiedDate();
    }
    
    public boolean hasRequiredSpecialty(String specialty) {
        return requiredSpecialties.contains(specialty);
    }
    
    // === GESTION DU PERSONNEL ===
    
    public Set<String> getAssignedPersonnel() {
        return new HashSet<>(assignedPersonnel);
    }
    
    public void setAssignedPersonnel(Set<String> personnel) {
        this.assignedPersonnel = personnel != null ? new HashSet<>(personnel) : new HashSet<>();
        updateModifiedDate();
    }
    
    public void addAssignedPersonnel(String personnelId) {
        this.assignedPersonnel.add(personnelId);
        updateModifiedDate();
    }
    
    public void removeAssignedPersonnel(String personnelId) {
        this.assignedPersonnel.remove(personnelId);
        updateModifiedDate();
    }
    
    // === GESTION COMPTES GOOGLE ===
    
    public Set<Long> getGoogleAccountIds() {
        return new HashSet<>(googleAccountIds);
    }
    
    public void setGoogleAccountIds(Set<Long> accountIds) {
        this.googleAccountIds = accountIds != null ? new HashSet<>(accountIds) : new HashSet<>();
    }
    
    public void addGoogleAccount(Long accountId) {
        this.googleAccountIds.add(accountId);
    }
    
    public void removeGoogleAccount(Long accountId) {
        this.googleAccountIds.remove(accountId);
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private void updateModifiedDate() {
        setModifiedDate(LocalDateTime.now());
    }
    
    /**
     * Vérifie si l'événement est en conflit avec un autre
     */
    public boolean isConflictingWith(EventItem other) {
        if (other == null) return false;
        
        LocalDateTime thisStart = this.getStartDateTime();
        LocalDateTime thisEnd = this.getEndDateTime();
        LocalDateTime otherStart = other.getStartDateTime();
        LocalDateTime otherEnd = other.getEndDateTime();
        
        if (thisStart == null || thisEnd == null || otherStart == null || otherEnd == null) {
            return false;
        }
        
        return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }
    
    /**
     * Calcule la durée de l'événement en minutes
     */
    public long getDurationMinutes() {
        if (startDateTime.get() == null || endDateTime.get() == null) {
            return 0;
        }
        return java.time.Duration.between(startDateTime.get(), endDateTime.get()).toMinutes();
    }
    
    /**
     * Vérifie si l'événement est dans une période donnée
     */
    public boolean isInDateRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime eventStart = getStartDateTime();
        LocalDateTime eventEnd = getEndDateTime();
        
        if (eventStart == null || eventEnd == null) {
            return false;
        }
        
        return eventStart.isBefore(end) && eventEnd.isAfter(start);
    }
    
    @Override
    public String toString() {
        return String.format("EventItem{id=%d, title='%s', start=%s, type=%s, status=%s}",
                getId(), getTitle(), getStartDateTime(), getType(), getStatus());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EventItem eventItem = (EventItem) obj;
        return Objects.equals(getId(), eventItem.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
