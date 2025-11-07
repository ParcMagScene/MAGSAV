package com.magscene.magsav.desktop.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Item représentant un calendrier de planning pour l'interface JavaFX
 */
public class CalendarItem {
    
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<CalendarType> type = new SimpleObjectProperty<>(CalendarType.SPECIALTY);
    private final StringProperty ownerId = new SimpleStringProperty();
    
    // Configuration visuelle
    private final StringProperty colorCode = new SimpleStringProperty("#3498db");
    private final IntegerProperty displayOrder = new SimpleIntegerProperty(0);
    private final BooleanProperty isVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty isDefault = new SimpleBooleanProperty(false);
    
    // Spécialités associées
    private Set<String> associatedSpecialties = new HashSet<>();
    
    // Synchronisation Google
    private final BooleanProperty syncWithGoogle = new SimpleBooleanProperty(false);
    private final ObjectProperty<LocalDateTime> lastSyncDate = new SimpleObjectProperty<>();
    
    // Permissions
    private final BooleanProperty isPublic = new SimpleBooleanProperty(false);
    private Set<String> allowedViewers = new HashSet<>();
    private Set<String> allowedEditors = new HashSet<>();
    
    // Métadonnées
    private final ObjectProperty<LocalDateTime> createdDate = new SimpleObjectProperty<>(LocalDateTime.now());
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);
    
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
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    // === CONSTRUCTEURS ===
    
    public CalendarItem() {}
    
    public CalendarItem(String name, CalendarType type) {
        setName(name);
        setType(type);
    }
    
    // === PROPRIÉTÉS ===
    
    public Long getId() { return id.get(); }
    public void setId(Long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
    
    public CalendarType getType() { return type.get(); }
    public void setType(CalendarType type) { this.type.set(type); }
    public ObjectProperty<CalendarType> typeProperty() { return type; }
    
    public String getOwnerId() { return ownerId.get(); }
    public void setOwnerId(String ownerId) { this.ownerId.set(ownerId); }
    public StringProperty ownerIdProperty() { return ownerId; }
    
    public String getColorCode() { return colorCode.get(); }
    public void setColorCode(String colorCode) { this.colorCode.set(colorCode); }
    public StringProperty colorCodeProperty() { return colorCode; }
    
    public Integer getDisplayOrder() { return displayOrder.get(); }
    public void setDisplayOrder(Integer order) { this.displayOrder.set(order); }
    public IntegerProperty displayOrderProperty() { return displayOrder; }
    
    public boolean isVisible() { return isVisible.get(); }
    public void setVisible(boolean visible) { this.isVisible.set(visible); }
    public BooleanProperty visibleProperty() { return isVisible; }
    
    public boolean isDefault() { return isDefault.get(); }
    public void setIsDefault(boolean isDefault) { this.isDefault.set(isDefault); }
    public BooleanProperty defaultProperty() { return isDefault; }
    
    public boolean isSyncWithGoogle() { return syncWithGoogle.get(); }
    public void setSyncWithGoogle(boolean sync) { this.syncWithGoogle.set(sync); }
    public BooleanProperty syncWithGoogleProperty() { return syncWithGoogle; }
    
    public LocalDateTime getLastSyncDate() { return lastSyncDate.get(); }
    public void setLastSyncDate(LocalDateTime date) { this.lastSyncDate.set(date); }
    public ObjectProperty<LocalDateTime> lastSyncDateProperty() { return lastSyncDate; }
    
    public boolean isPublic() { return isPublic.get(); }
    public void setPublic(boolean isPublic) { this.isPublic.set(isPublic); }
    public BooleanProperty publicProperty() { return isPublic; }
    
    public LocalDateTime getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(LocalDateTime date) { this.createdDate.set(date); }
    public ObjectProperty<LocalDateTime> createdDateProperty() { return createdDate; }
    
    public boolean isActive() { return isActive.get(); }
    public void setActive(boolean active) { this.isActive.set(active); }
    public BooleanProperty activeProperty() { return isActive; }
    
    // === SPÉCIALITÉS ASSOCIÉES ===
    
    public Set<String> getAssociatedSpecialties() {
        return new HashSet<>(associatedSpecialties);
    }
    
    public void setAssociatedSpecialties(Set<String> specialties) {
        this.associatedSpecialties = specialties != null ? new HashSet<>(specialties) : new HashSet<>();
    }
    
    public void addSpecialty(String specialty) {
        this.associatedSpecialties.add(specialty);
    }
    
    public void removeSpecialty(String specialty) {
        this.associatedSpecialties.remove(specialty);
    }
    
    public boolean hasSpecialty(String specialty) {
        return associatedSpecialties.contains(specialty);
    }
    
    // === PERMISSIONS ===
    
    public Set<String> getAllowedViewers() {
        return new HashSet<>(allowedViewers);
    }
    
    public void setAllowedViewers(Set<String> viewers) {
        this.allowedViewers = viewers != null ? new HashSet<>(viewers) : new HashSet<>();
    }
    
    public Set<String> getAllowedEditors() {
        return new HashSet<>(allowedEditors);
    }
    
    public void setAllowedEditors(Set<String> editors) {
        this.allowedEditors = editors != null ? new HashSet<>(editors) : new HashSet<>();
    }
    
    public boolean canUserView(String userId) {
        return isPublic() || Objects.equals(getOwnerId(), userId) || 
               allowedViewers.contains(userId) || allowedEditors.contains(userId);
    }
    
    public boolean canUserEdit(String userId) {
        return Objects.equals(getOwnerId(), userId) || allowedEditors.contains(userId);
    }
    
    @Override
    public String toString() {
        return getName() + " (" + getType().getLabel() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalendarItem that = (CalendarItem) obj;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}