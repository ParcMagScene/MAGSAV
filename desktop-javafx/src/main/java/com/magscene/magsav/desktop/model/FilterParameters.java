package com.magscene.magsav.desktop.model;

import com.magscene.magsav.desktop.model.EventItem.EventType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Paramètres de filtrage pour les événements du planning
 */
public class FilterParameters {
    
    // Filtres par spécialités
    private final ObservableSet<String> selectedSpecialties = FXCollections.observableSet();
    
    // Filtres par types d'événements
    private final ObservableSet<EventType> selectedEventTypes = FXCollections.observableSet();
    
    // Filtre par période
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    
    // Filtre par texte
    private final StringProperty searchText = new SimpleStringProperty("");
    
    // Filtre par statut
    private final ObservableSet<EventItem.EventStatus> selectedStatuses = FXCollections.observableSet();
    
    // Filtre par priorité
    private final ObservableSet<EventItem.PriorityLevel> selectedPriorities = FXCollections.observableSet();
    
    // Filtre par personnel assigné
    private final ObservableSet<String> selectedPersonnel = FXCollections.observableSet();
    
    // Autres filtres
    private final BooleanProperty showOnlyMyEvents = new SimpleBooleanProperty(false);
    private final BooleanProperty showOnlySyncedEvents = new SimpleBooleanProperty(false);
    private final BooleanProperty showRecurringEvents = new SimpleBooleanProperty(true);
    
    // Prédicat combiné pour le filtrage
    private final ObservableValue<Predicate<EventItem>> combinedPredicate;
    
    public FilterParameters() {
        // Créer le prédicat combiné qui se met à jour automatiquement
        combinedPredicate = Bindings.createObjectBinding(
            this::createCombinedPredicate,
            selectedSpecialties,
            selectedEventTypes,
            startDate,
            endDate,
            searchText,
            selectedStatuses,
            selectedPriorities,
            selectedPersonnel,
            showOnlyMyEvents,
            showOnlySyncedEvents,
            showRecurringEvents
        );
    }
    
    private Predicate<EventItem> createCombinedPredicate() {
        return event -> {
            // Filtre par spécialités
            if (!selectedSpecialties.isEmpty()) {
                boolean hasMatchingSpecialty = event.getRequiredSpecialties().stream()
                    .anyMatch(selectedSpecialties::contains);
                if (!hasMatchingSpecialty) return false;
            }
            
            // Filtre par types
            if (!selectedEventTypes.isEmpty() && !selectedEventTypes.contains(event.getType())) {
                return false;
            }
            
            // Filtre par statuts
            if (!selectedStatuses.isEmpty() && !selectedStatuses.contains(event.getStatus())) {
                return false;
            }
            
            // Filtre par priorités
            if (!selectedPriorities.isEmpty() && !selectedPriorities.contains(event.getPriority())) {
                return false;
            }
            
            // Filtre par période
            if (startDate.get() != null || endDate.get() != null) {
                LocalDateTime eventStart = event.getStartDateTime();
                LocalDateTime eventEnd = event.getEndDateTime();
                
                if (eventStart == null) return false;
                
                if (startDate.get() != null) {
                    LocalDateTime filterStart = startDate.get().atStartOfDay();
                    if (eventEnd == null || eventEnd.isBefore(filterStart)) {
                        return false;
                    }
                }
                
                if (endDate.get() != null) {
                    LocalDateTime filterEnd = endDate.get().plusDays(1).atStartOfDay();
                    if (eventStart.isAfter(filterEnd)) {
                        return false;
                    }
                }
            }
            
            // Filtre par texte
            String search = searchText.get();
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                boolean matchesText = 
                    (event.getTitle() != null && event.getTitle().toLowerCase().contains(searchLower)) ||
                    (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchLower)) ||
                    (event.getLocation() != null && event.getLocation().toLowerCase().contains(searchLower));
                
                if (!matchesText) return false;
            }
            
            // Filtre par personnel
            if (!selectedPersonnel.isEmpty()) {
                boolean hasMatchingPersonnel = event.getAssignedPersonnel().stream()
                    .anyMatch(selectedPersonnel::contains);
                if (!hasMatchingPersonnel) return false;
            }
            
            // Filtre événements synchronisés seulement
            if (showOnlySyncedEvents.get() && !event.isSyncEnabled()) {
                return false;
            }
            
            // Filtre événements récurrents
            if (!showRecurringEvents.get() && event.isRecurring()) {
                return false;
            }
            
            return true;
        };
    }
    
    // === GETTERS ET SETTERS ===
    
    public ObservableSet<String> getSelectedSpecialties() {
        return selectedSpecialties;
    }
    
    public void setSelectedSpecialties(Set<String> specialties) {
        selectedSpecialties.clear();
        if (specialties != null) {
            selectedSpecialties.addAll(specialties);
        }
    }
    
    public ObservableSet<EventType> getSelectedEventTypes() {
        return selectedEventTypes;
    }
    
    public void setSelectedEventTypes(Set<EventType> types) {
        selectedEventTypes.clear();
        if (types != null) {
            selectedEventTypes.addAll(types);
        }
    }
    
    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate startDate) { this.startDate.set(startDate); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    
    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate endDate) { this.endDate.set(endDate); }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    
    public void setDateRange(LocalDate start, LocalDate end) {
        setStartDate(start);
        setEndDate(end);
    }
    
    public String getSearchText() { return searchText.get(); }
    public void setSearchText(String searchText) { this.searchText.set(searchText); }
    public StringProperty searchTextProperty() { return searchText; }
    
    public ObservableSet<EventItem.EventStatus> getSelectedStatuses() {
        return selectedStatuses;
    }
    
    public ObservableSet<EventItem.PriorityLevel> getSelectedPriorities() {
        return selectedPriorities;
    }
    
    public ObservableSet<String> getSelectedPersonnel() {
        return selectedPersonnel;
    }
    
    public boolean isShowOnlyMyEvents() { return showOnlyMyEvents.get(); }
    public void setShowOnlyMyEvents(boolean show) { this.showOnlyMyEvents.set(show); }
    public BooleanProperty showOnlyMyEventsProperty() { return showOnlyMyEvents; }
    
    public boolean isShowOnlySyncedEvents() { return showOnlySyncedEvents.get(); }
    public void setShowOnlySyncedEvents(boolean show) { this.showOnlySyncedEvents.set(show); }
    public BooleanProperty showOnlySyncedEventsProperty() { return showOnlySyncedEvents; }
    
    public boolean isShowRecurringEvents() { return showRecurringEvents.get(); }
    public void setShowRecurringEvents(boolean show) { this.showRecurringEvents.set(show); }
    public BooleanProperty showRecurringEventsProperty() { return showRecurringEvents; }
    
    public ObservableValue<Predicate<EventItem>> combinedPredicateProperty() {
        return combinedPredicate;
    }
    
    /**
     * Réinitialise tous les filtres
     */
    public void clear() {
        selectedSpecialties.clear();
        selectedEventTypes.clear();
        setStartDate(null);
        setEndDate(null);
        setSearchText("");
        selectedStatuses.clear();
        selectedPriorities.clear();
        selectedPersonnel.clear();
        setShowOnlyMyEvents(false);
        setShowOnlySyncedEvents(false);
        setShowRecurringEvents(true);
    }
    
    /**
     * Vérifie si des filtres sont actifs
     */
    public boolean hasActiveFilters() {
        return !selectedSpecialties.isEmpty() ||
               !selectedEventTypes.isEmpty() ||
               getStartDate() != null ||
               getEndDate() != null ||
               (getSearchText() != null && !getSearchText().trim().isEmpty()) ||
               !selectedStatuses.isEmpty() ||
               !selectedPriorities.isEmpty() ||
               !selectedPersonnel.isEmpty() ||
               isShowOnlyMyEvents() ||
               isShowOnlySyncedEvents() ||
               !isShowRecurringEvents();
    }
}
