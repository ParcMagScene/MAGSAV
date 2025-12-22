package com.magscene.magsav.desktop.manager;

import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.model.EventItem;
import com.magscene.magsav.desktop.model.CalendarItem;
import com.magscene.magsav.desktop.model.GoogleAccountItem;
import com.magscene.magsav.desktop.model.FilterParameters;
import com.magscene.magsav.desktop.model.ViewParameters;
import com.magscene.magsav.desktop.model.SyncResultItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Gestionnaire centralisé du planning avec intégration spécialités et Google Calendar
 * Gère les événements, filtres, synchronisation et configuration locale
 */
public class PlanningManager {
    
    private static PlanningManager instance;
    
    // Configuration
    private final Preferences prefs;
    private final SpecialtiesConfigManager specialtiesManager;
    
    // Données planning
    private final ObservableList<EventItem> allEvents;
    private final FilteredList<EventItem> filteredEvents;
    private final SortedList<EventItem> sortedEvents;
    
    // Comptes Google Calendar
    private final ObservableList<GoogleAccountItem> googleAccounts;
    private final FilteredList<GoogleAccountItem> activeGoogleAccounts;
    
    // Calendriers
    private final ObservableList<CalendarItem> calendars;
    private final FilteredList<CalendarItem> visibleCalendars;
    
    // Filtres et vues
    private final FilterParameters filterParams = new FilterParameters();
    private final ViewParameters viewParams = new ViewParameters();
    
    // Configuration locale
    private static final String PREFS_FILTER_SPECIALTIES = "planning.filter.specialties";
    private static final String PREFS_FILTER_TYPES = "planning.filter.types";
    private static final String PREFS_VIEW_MODE = "planning.view.mode";
    private static final String PREFS_VISIBLE_CALENDARS = "planning.calendars.visible";
    private static final String PREFS_GOOGLE_SYNC_ENABLED = "planning.google.sync.enabled";
    
    @SuppressWarnings("unused")
    public PlanningManager(ApiService apiService) {
        // apiService parameter kept for future backend integration
        this.prefs = Preferences.userNodeForPackage(PlanningManager.class);
        this.specialtiesManager = SpecialtiesConfigManager.getInstance();
        
        // Initialiser les listes observables
        this.allEvents = FXCollections.observableArrayList();
        this.filteredEvents = new FilteredList<>(allEvents);
        this.sortedEvents = new SortedList<>(filteredEvents);
        
        this.googleAccounts = FXCollections.observableArrayList();
        this.activeGoogleAccounts = new FilteredList<>(googleAccounts, 
            account -> account.isActive() && account.isSyncEnabled());
        
        this.calendars = FXCollections.observableArrayList();
        this.visibleCalendars = new FilteredList<>(calendars, CalendarItem::isVisible);
        
        // Configurer les filtres
        setupFilters();
        
        // Charger la configuration
        loadConfiguration();
        
        // Initialiser avec des données par défaut
        initializeDefaultData();
    }
    
    public static synchronized PlanningManager getInstance(ApiService apiService) {
        if (instance == null) {
            instance = new PlanningManager(apiService);
        }
        return instance;
    }
    
    public static PlanningManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlanningManager non initialisé. Utiliser getInstance(ApiService) d'abord.");
        }
        return instance;
    }
    
    // === CONFIGURATION DES FILTRES ===
    
    private void setupFilters() {
        filteredEvents.predicateProperty().bind(filterParams.combinedPredicateProperty());
        
        // Tri par date de début par défaut
        sortedEvents.setComparator((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
    }
    
    // === GESTION DES ÉVÉNEMENTS ===
    
    /**
     * Ajoute un événement au planning
     */
    public void addEvent(EventItem event) {
        allEvents.add(event);
        saveConfiguration(); // Sauvegarde automatique
    }
    
    /**
     * Supprime un événement du planning
     */
    public void removeEvent(EventItem event) {
        allEvents.remove(event);
        saveConfiguration();
    }
    
    /**
     * Met à jour un événement existant
     */
    public void updateEvent(EventItem event) {
        // L'événement est déjà dans la liste observable, 
        // les propriétés JavaFX se mettent à jour automatiquement
        saveConfiguration();
    }
    
    /**
     * Crée un nouvel événement avec les spécialités sélectionnées
     */
    public EventItem createEvent(String title, LocalDateTime start, LocalDateTime end, 
                                EventItem.EventType type, Set<String> requiredSpecialties) {
        EventItem event = new EventItem();
        event.setTitle(title);
        event.setStartDateTime(start);
        event.setEndDateTime(end);
        event.setType(type);
        event.setRequiredSpecialties(new HashSet<>(requiredSpecialties));
        event.setCreatedDate(LocalDateTime.now());
        event.setStatus(EventItem.EventStatus.PLANNED);
        
        addEvent(event);
        return event;
    }
    
    // === GESTION DES COMPTES GOOGLE CALENDAR ===
    
    /**
     * Ajoute un compte Google Calendar
     */
    public void addGoogleAccount(GoogleAccountItem account) {
        googleAccounts.add(account);
        saveConfiguration();
    }
    
    /**
     * Supprime un compte Google Calendar
     */
    public void removeGoogleAccount(GoogleAccountItem account) {
        googleAccounts.remove(account);
        saveConfiguration();
    }
    
    /**
     * Synchronise tous les comptes Google actifs
     */
    public CompletableFuture<List<SyncResultItem>> synchronizeAllGoogleAccounts() {
        return CompletableFuture.supplyAsync(() -> {
            List<SyncResultItem> results = new ArrayList<>();
            
            for (GoogleAccountItem account : activeGoogleAccounts) {
                try {
                    // TODO: Intégration avec GoogleCalendarService
                    SyncResultItem result = new SyncResultItem(account.getDisplayName(), true, 
                        "Synchronisation simulée réussie");
                    results.add(result);
                    
                } catch (Exception e) {
                    SyncResultItem result = new SyncResultItem(account.getDisplayName(), false, 
                        e.getMessage());
                    results.add(result);
                }
            }
            
            return results;
        });
    }
    
    // === GESTION DES CALENDRIERS ===
    
    /**
     * Crée un calendrier pour une spécialité
     */
    public CalendarItem createSpecialtyCalendar(String specialtyName, String colorCode) {
        CalendarItem calendar = new CalendarItem();
        calendar.setName("Planning " + specialtyName);
        calendar.setType(CalendarItem.CalendarType.SPECIALTY);
        calendar.setAssociatedSpecialties(Set.of(specialtyName));
        calendar.setColorCode(colorCode);
        calendar.setVisible(true);
        
        calendars.add(calendar);
        saveConfiguration();
        return calendar;
    }
    
    /**
     * Met à jour la visibilité des calendriers
     */
    public void setCalendarVisibility(CalendarItem calendar, boolean visible) {
        calendar.setVisible(visible);
        saveConfiguration();
    }
    
    // === FILTRES ET RECHERCHE ===
    
    /**
     * Filtre par spécialités sélectionnées
     */
    public void setSpecialtyFilter(Set<String> specialties) {
        filterParams.setSelectedSpecialties(specialties);
    }
    
    /**
     * Filtre par types d'événements
     */
    public void setEventTypeFilter(Set<EventItem.EventType> types) {
        filterParams.setSelectedEventTypes(types);
    }
    
    /**
     * Filtre par période
     */
    public void setDateRangeFilter(LocalDate startDate, LocalDate endDate) {
        filterParams.setDateRange(startDate, endDate);
    }
    
    /**
     * Filtre par texte libre
     */
    public void setTextFilter(String searchText) {
        filterParams.setSearchText(searchText);
    }
    
    /**
     * Réinitialise tous les filtres
     */
    public void clearAllFilters() {
        filterParams.clear();
    }
    
    // === VUES ET AFFICHAGE ===
    
    /**
     * Change le mode de vue (jour, semaine, mois)
     */
    public void setViewMode(ViewParameters.ViewMode mode) {
        viewParams.setViewMode(mode);
        saveConfiguration();
    }
    
    /**
     * Navigue à une date spécifique
     */
    public void navigateToDate(LocalDate date) {
        viewParams.setCurrentDate(date);
    }
    
    // === SAUVEGARDE ET CHARGEMENT ===
    
    /**
     * Sauvegarde la configuration locale
     */
    public void saveConfiguration() {
        try {
            // Sauvegarder les filtres
            String specialtiesFilter = String.join(";", filterParams.getSelectedSpecialties());
            prefs.put(PREFS_FILTER_SPECIALTIES, specialtiesFilter);
            
            String typesFilter = filterParams.getSelectedEventTypes().stream()
                .map(Enum::name)
                .collect(Collectors.joining(";"));
            prefs.put(PREFS_FILTER_TYPES, typesFilter);
            
            // Sauvegarder la vue
            prefs.put(PREFS_VIEW_MODE, viewParams.getViewMode().name());
            
            // Sauvegarder les calendriers visibles
            String visibleCalendars = calendars.stream()
                .filter(CalendarItem::isVisible)
                .map(c -> c.getId().toString())
                .collect(Collectors.joining(";"));
            prefs.put(PREFS_VISIBLE_CALENDARS, visibleCalendars);
            
            // Sauvegarder l'état de synchronisation Google
            boolean googleSyncEnabled = activeGoogleAccounts.size() > 0;
            prefs.putBoolean(PREFS_GOOGLE_SYNC_ENABLED, googleSyncEnabled);
            
            prefs.flush();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de la configuration planning: " + e.getMessage());
        }
    }
    
    /**
     * Charge la configuration locale
     */
    private void loadConfiguration() {
        try {
            // Charger les filtres
            String specialtiesFilter = prefs.get(PREFS_FILTER_SPECIALTIES, "");
            if (!specialtiesFilter.isEmpty()) {
                Set<String> specialties = Arrays.stream(specialtiesFilter.split(";"))
                    .filter(s -> !s.trim().isEmpty())
                    .collect(Collectors.toSet());
                filterParams.setSelectedSpecialties(specialties);
            }
            
            String typesFilter = prefs.get(PREFS_FILTER_TYPES, "");
            if (!typesFilter.isEmpty()) {
                Set<EventItem.EventType> types = Arrays.stream(typesFilter.split(";"))
                    .filter(s -> !s.trim().isEmpty())
                    .map(EventItem.EventType::valueOf)
                    .collect(Collectors.toSet());
                filterParams.setSelectedEventTypes(types);
            }
            
            // Charger la vue
            String viewModeStr = prefs.get(PREFS_VIEW_MODE, ViewParameters.ViewMode.WEEK.name());
            ViewParameters.ViewMode viewMode = ViewParameters.ViewMode.valueOf(viewModeStr);
            viewParams.setViewMode(viewMode);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la configuration planning: " + e.getMessage());
            // Utiliser les valeurs par défaut en cas d'erreur
        }
    }
    
    /**
     * Initialise les données par défaut
     */
    private void initializeDefaultData() {
        // Créer des calendriers par défaut pour chaque spécialité
        ObservableList<String> specialties = specialtiesManager.getAvailableSpecialties();
        String[] colors = {"#e74c3c", "#3498db", "#2ecc71", "#f39c12", "#9b59b6", 
                          "#1abc9c", "#34495e", "#e67e22", "#95a5a6", "#f1c40f"};
        
        for (int i = 0; i < specialties.size(); i++) {
            String specialty = specialties.get(i);
            String color = colors[i % colors.length];
            
            CalendarItem calendar = new CalendarItem();
            calendar.setId((long) (i + 1));
            calendar.setName("Planning " + specialty);
            calendar.setType(CalendarItem.CalendarType.SPECIALTY);
            calendar.setAssociatedSpecialties(Set.of(specialty));
            calendar.setColorCode(color);
            calendar.setVisible(true);
            calendar.setIsDefault(true);
            
            calendars.add(calendar);
        }
        
        // Créer quelques événements d'exemple
        createSampleEvents();
    }
    
    private void createSampleEvents() {
        LocalDateTime now = LocalDateTime.now();
        
        // Événement intervention Son
        EventItem event1 = new EventItem();
        event1.setId(1L);
        event1.setTitle("Installation sonorisation concert");
        event1.setDescription("Installation complète du système de sonorisation pour le concert de ce soir");
        event1.setStartDateTime(now.plusDays(1).withHour(14).withMinute(0));
        event1.setEndDateTime(now.plusDays(1).withHour(18).withMinute(0));
        event1.setType(EventItem.EventType.INSTALLATION);
        event1.setRequiredSpecialties(Set.of("Son", "Électricité"));
        event1.setLocation("Salle de spectacle");
        event1.setStatus(EventItem.EventStatus.PLANNED);
        
        // Événement maintenance Éclairage
        EventItem event2 = new EventItem();
        event2.setId(2L);
        event2.setTitle("Maintenance préventive éclairage");
        event2.setDescription("Vérification et maintenance du matériel d'éclairage");
        event2.setStartDateTime(now.plusDays(2).withHour(9).withMinute(0));
        event2.setEndDateTime(now.plusDays(2).withHour(12).withMinute(0));
        event2.setType(EventItem.EventType.MAINTENANCE);
        event2.setRequiredSpecialties(Set.of("Éclairage"));
        event2.setLocation("Atelier technique");
        event2.setStatus(EventItem.EventStatus.PLANNED);
        
        // Événement formation
        EventItem event3 = new EventItem();
        event3.setId(3L);
        event3.setTitle("Formation sécurité");
        event3.setDescription("Formation obligatoire sur les consignes de sécurité");
        event3.setStartDateTime(now.plusDays(3).withHour(10).withMinute(0));
        event3.setEndDateTime(now.plusDays(3).withHour(16).withMinute(0));
        event3.setType(EventItem.EventType.FORMATION);
        event3.setRequiredSpecialties(Set.of("Sécurité"));
        event3.setLocation("Salle de formation");
        event3.setStatus(EventItem.EventStatus.PLANNED);
        
        allEvents.addAll(Arrays.asList(event1, event2, event3));
    }
    
    // === GETTERS POUR L'INTERFACE ===
    
    public ObservableList<EventItem> getAllEvents() {
        return allEvents;
    }
    
    public FilteredList<EventItem> getFilteredEvents() {
        return filteredEvents;
    }
    
    public SortedList<EventItem> getSortedEvents() {
        return sortedEvents;
    }
    
    public ObservableList<GoogleAccountItem> getGoogleAccounts() {
        return googleAccounts;
    }
    
    public FilteredList<GoogleAccountItem> getActiveGoogleAccounts() {
        return activeGoogleAccounts;
    }
    
    public ObservableList<CalendarItem> getCalendars() {
        return calendars;
    }
    
    public FilteredList<CalendarItem> getVisibleCalendars() {
        return visibleCalendars;
    }
    
    public FilterParameters getFilterParameters() {
        return filterParams;
    }
    
    public ViewParameters getViewParameters() {
        return viewParams;
    }
    
    public SpecialtiesConfigManager getSpecialtiesManager() {
        return specialtiesManager;
    }
}
