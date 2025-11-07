package com.magscene.magsav.desktop.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Item représentant un compte Google Calendar pour l'interface JavaFX
 */
public class GoogleAccountItem {
    
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty userId = new SimpleStringProperty();
    private final StringProperty googleAccountEmail = new SimpleStringProperty();
    private final StringProperty displayName = new SimpleStringProperty();
    private final StringProperty googleCalendarId = new SimpleStringProperty();
    private final StringProperty googleCalendarName = new SimpleStringProperty();
    
    // Configuration synchronisation
    private final BooleanProperty syncEnabled = new SimpleBooleanProperty(true);
    private final ObjectProperty<SyncDirection> syncDirection = new SimpleObjectProperty<>(SyncDirection.BIDIRECTIONAL);
    private Set<String> syncSpecialties = new HashSet<>();
    private final StringProperty eventTitlePrefix = new SimpleStringProperty("[MAGSAV]");
    private final StringProperty defaultColorId = new SimpleStringProperty("1");
    
    // Statut
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> lastSyncDate = new SimpleObjectProperty<>();
    private final ObjectProperty<SyncStatus> lastSyncStatus = new SimpleObjectProperty<>(SyncStatus.NEVER_SYNCED);
    private final StringProperty syncErrorMessage = new SimpleStringProperty();
    
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
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    public enum SyncStatus {
        NEVER_SYNCED("Jamais synchronisé"),
        SUCCESS("Succès"),
        PARTIAL_SUCCESS("Succès partiel"),
        FAILED("Échec"),
        IN_PROGRESS("En cours"),
        TOKEN_EXPIRED("Token expiré");
        
        private final String label;
        
        SyncStatus(String label) {
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
    
    public GoogleAccountItem() {}
    
    public GoogleAccountItem(String userId, String email, String displayName) {
        setUserId(userId);
        setGoogleAccountEmail(email);
        setDisplayName(displayName);
    }
    
    // === PROPRIÉTÉS ===
    
    public Long getId() { return id.get(); }
    public void setId(Long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    public String getUserId() { return userId.get(); }
    public void setUserId(String userId) { this.userId.set(userId); }
    public StringProperty userIdProperty() { return userId; }
    
    public String getGoogleAccountEmail() { return googleAccountEmail.get(); }
    public void setGoogleAccountEmail(String email) { this.googleAccountEmail.set(email); }
    public StringProperty googleAccountEmailProperty() { return googleAccountEmail; }
    
    public String getDisplayName() { return displayName.get(); }
    public void setDisplayName(String name) { this.displayName.set(name); }
    public StringProperty displayNameProperty() { return displayName; }
    
    public String getGoogleCalendarId() { return googleCalendarId.get(); }
    public void setGoogleCalendarId(String id) { this.googleCalendarId.set(id); }
    public StringProperty googleCalendarIdProperty() { return googleCalendarId; }
    
    public String getGoogleCalendarName() { return googleCalendarName.get(); }
    public void setGoogleCalendarName(String name) { this.googleCalendarName.set(name); }
    public StringProperty googleCalendarNameProperty() { return googleCalendarName; }
    
    public boolean isSyncEnabled() { return syncEnabled.get(); }
    public void setSyncEnabled(boolean enabled) { this.syncEnabled.set(enabled); }
    public BooleanProperty syncEnabledProperty() { return syncEnabled; }
    
    public SyncDirection getSyncDirection() { return syncDirection.get(); }
    public void setSyncDirection(SyncDirection direction) { this.syncDirection.set(direction); }
    public ObjectProperty<SyncDirection> syncDirectionProperty() { return syncDirection; }
    
    public String getEventTitlePrefix() { return eventTitlePrefix.get(); }
    public void setEventTitlePrefix(String prefix) { this.eventTitlePrefix.set(prefix); }
    public StringProperty eventTitlePrefixProperty() { return eventTitlePrefix; }
    
    public String getDefaultColorId() { return defaultColorId.get(); }
    public void setDefaultColorId(String colorId) { this.defaultColorId.set(colorId); }
    public StringProperty defaultColorIdProperty() { return defaultColorId; }
    
    public boolean isActive() { return isActive.get(); }
    public void setActive(boolean active) { this.isActive.set(active); }
    public BooleanProperty activeProperty() { return isActive; }
    
    public LocalDateTime getLastSyncDate() { return lastSyncDate.get(); }
    public void setLastSyncDate(LocalDateTime date) { this.lastSyncDate.set(date); }
    public ObjectProperty<LocalDateTime> lastSyncDateProperty() { return lastSyncDate; }
    
    public SyncStatus getLastSyncStatus() { return lastSyncStatus.get(); }
    public void setLastSyncStatus(SyncStatus status) { this.lastSyncStatus.set(status); }
    public ObjectProperty<SyncStatus> lastSyncStatusProperty() { return lastSyncStatus; }
    
    public String getSyncErrorMessage() { return syncErrorMessage.get(); }
    public void setSyncErrorMessage(String message) { this.syncErrorMessage.set(message); }
    public StringProperty syncErrorMessageProperty() { return syncErrorMessage; }
    
    // === SPÉCIALITÉS ===
    
    public Set<String> getSyncSpecialties() {
        return new HashSet<>(syncSpecialties);
    }
    
    public void setSyncSpecialties(Set<String> specialties) {
        this.syncSpecialties = specialties != null ? new HashSet<>(specialties) : new HashSet<>();
    }
    
    public boolean shouldSyncSpecialty(String specialty) {
        return syncSpecialties.isEmpty() || syncSpecialties.contains(specialty);
    }
    
    @Override
    public String toString() {
        return getDisplayName() + " (" + getGoogleAccountEmail() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GoogleAccountItem that = (GoogleAccountItem) obj;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}