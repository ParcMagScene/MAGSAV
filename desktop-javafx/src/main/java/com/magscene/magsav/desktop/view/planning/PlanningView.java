package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService;
import com.magscene.magsav.desktop.service.planning.PlanningConflictService;
import com.magscene.magsav.desktop.view.planning.calendar.CalendarSelectionPanel;
import com.magscene.magsav.desktop.view.planning.calendar.WeekCalendarView;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.magscene.magsav.desktop.theme.ThemeManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import java.util.Optional;

import java.util.Optional;

/**
 * Interface JavaFX moderne pour le module Planning 
 * Vue calendaire visuelle type Google Agenda avec interaction souris
 */
public class PlanningView extends BorderPane {
    
    private ApiService apiService;
    private SpecialtyPlanningService specialtyService;
    private PlanningConflictService conflictService;
    
    // Composants de navigation
    private Label currentPeriodLabel;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnToday;
    private ComboBox<ViewMode> viewModeCombo;
    private DatePicker datePicker;
    
    // Vues calendaires
    private WeekCalendarView weekCalendarView;
    private DayCalendarView dayCalendarView;
    private MonthCalendarView monthCalendarView;
    private Region currentCalendarView;
    private CalendarSelectionPanel calendarSelectionPanel;
    private ConflictNotificationPanel conflictNotificationPanel;
    
    // Boutons d'action
    private Button btnCreateEvent;
    private Button btnRefresh;
    private Button btnSettings;
    
    // Status
    private Label statusLabel;
    
    // État actuel
    private LocalDate currentDate = LocalDate.now();
    private ViewMode currentViewMode = ViewMode.WEEK;
    
    public enum ViewMode {
        DAY("Jour"),
        WEEK("Semaine"), 
        MONTH("Mois");
        
        private final String label;
        ViewMode(String label) { this.label = label; }
        
        @Override
        public String toString() { return label; }
    }
    
    public PlanningView(ApiService apiService) {
        this.apiService = apiService;
        this.specialtyService = new SpecialtyPlanningService(apiService);
        initializeComponents();
        setupLayout();
        loadStylesheet();
        setupEventHandlers();
        loadInitialData();
    }
    
    private void initializeComponents() {
        // Navigation temporelle
        currentPeriodLabel = new Label();
        currentPeriodLabel.getStyleClass().add("period-label");
        
        btnPrevious = new Button("◀");
        btnPrevious.getStyleClass().addAll("nav-button", "nav-previous");
        
        btnNext = new Button("▶");
        btnNext.getStyleClass().addAll("nav-button", "nav-next");
        
        btnToday = new Button("Aujourd'hui");
        btnToday.getStyleClass().addAll("nav-button", "nav-today");
        
        // Sélecteur de mode de vue
        viewModeCombo = new ComboBox<>();
        viewModeCombo.getItems().addAll(ViewMode.values());
        viewModeCombo.setValue(ViewMode.WEEK);
        viewModeCombo.getStyleClass().add("view-mode-combo");
        
        // DatePicker pour navigation rapide
        datePicker = new DatePicker(LocalDate.now());
        
        // Actions principales
        btnCreateEvent = new Button("+ Nouvel Événement");
        btnCreateEvent.getStyleClass().addAll("action-button", "create-event-button");
        
        btnRefresh = new Button("🔄");
        btnRefresh.getStyleClass().addAll("nav-button", "refresh-button");
        
        btnSettings = new Button("⚙️");
        btnSettings.getStyleClass().addAll("nav-button", "settings-button");
        
        // Vues calendaires
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        weekCalendarView = new WeekCalendarView(startOfWeek);
        dayCalendarView = new DayCalendarView(currentDate);
        monthCalendarView = new MonthCalendarView(YearMonth.from(currentDate));
        currentCalendarView = weekCalendarView;
        
        // Panneaux de sélection
        calendarSelectionPanel = new CalendarSelectionPanel();
        // TODO: Réintégrer SpecialtyFilterPanel
        // specialtyFilterPanel = new SpecialtyFilterPanel(specialtyService);
        
        // Initialiser le service de détection de conflits
        conflictService = new PlanningConflictService(specialtyService);
        conflictNotificationPanel = new ConflictNotificationPanel(conflictService);
        
        // Status
        statusLabel = new Label("Planning prêt");
        statusLabel.getStyleClass().add("status-label");
        
        updatePeriodLabel();
    }
    
    private void setupLayout() {
        getStyleClass().add("planning-view");
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Header avec navigation et contrôles
        VBox header = createHeaderSection();
        setTop(header);
        
        // Contenu principal : sidebar + calendrier
        HBox mainContent = new HBox();
        mainContent.getStyleClass().add("planning-main-content");
        
        // Sidebar avec calendriers et filtres
        VBox sidebar = createSidebar();
        sidebar.getStyleClass().add("planning-sidebar");
        
        // Zone centrale avec le calendrier
        VBox centerArea = createCenterArea();
        centerArea.getStyleClass().add("planning-main-area");
        HBox.setHgrow(centerArea, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(sidebar, centerArea);
        setCenter(mainContent);
        
        // Footer avec status
        HBox footer = createFooter();
        setBottom(footer);
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox(15);
        header.getStyleClass().add("planning-header");
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Titre et actions principales
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📅 Planning");
        title.getStyleClass().add("module-title");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.getChildren().addAll(btnCreateEvent, btnRefresh, btnSettings);
        
        titleRow.getChildren().addAll(title, spacer, actionButtons);
        
        // Navigation temporelle
        HBox navigationRow = new HBox(15);
        navigationRow.setAlignment(Pos.CENTER_LEFT);
        navigationRow.getStyleClass().add("time-navigation");
        
        HBox navControls = new HBox(8);
        navControls.setAlignment(Pos.CENTER_LEFT);
        navControls.getChildren().addAll(btnPrevious, currentPeriodLabel, btnNext, btnToday);
        
        HBox viewControls = new HBox(10);
        viewControls.setAlignment(Pos.CENTER_LEFT);
        viewControls.getChildren().addAll(
            new Label("Vue:"), viewModeCombo,
            new Label("Aller à:"), datePicker
        );
        
        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);
        
        navigationRow.getChildren().addAll(navControls, navSpacer, viewControls);
        
        header.getChildren().addAll(titleRow, navigationRow);
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(350);
        sidebar.setMinWidth(350);
        sidebar.setMaxWidth(350);
        sidebar.setPadding(new Insets(15));
        
        // Onglets pour organiser les panneaux
        TabPane sidebarTabs = new TabPane();
        sidebarTabs.getStyleClass().add("sidebar-tabs");
        sidebarTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Onglet Calendriers (existant)
        ScrollPane calendarScrollPane = new ScrollPane(calendarSelectionPanel);
        calendarScrollPane.setFitToWidth(true);
        calendarScrollPane.getStyleClass().add("calendar-scroll-pane");
        
        Tab calendarsTab = new Tab("📅 Calendriers", calendarScrollPane);
        calendarsTab.getStyleClass().add("sidebar-tab");
        
        sidebarTabs.getTabs().add(calendarsTab);
        VBox.setVgrow(sidebarTabs, Priority.ALWAYS);
        
        // Forcer le style des boutons de navigation des onglets
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceTabNavigationButtonsStyle(sidebarTabs);
        
        // Section mini-calendrier pour navigation rapide
        VBox miniCalendarSection = createMiniCalendarSection();
        
        sidebar.getChildren().addAll(sidebarTabs, miniCalendarSection);
        return sidebar;
    }
    
    private VBox createMiniCalendarSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("mini-calendar-section");
        
        Label title = new Label("🗓️ Navigation Rapide");
        title.getStyleClass().add("section-title");
        
        // Raccourcis de dates utiles
        VBox quickLinks = new VBox(5);
        
        Button todayLink = new Button("📍 Aujourd'hui");
        todayLink.getStyleClass().add("quick-date-link");
        todayLink.setOnAction(e -> navigateToDate(LocalDate.now()));
        
        Button thisWeekLink = new Button("📅 Cette semaine");
        thisWeekLink.getStyleClass().add("quick-date-link");
        thisWeekLink.setOnAction(e -> navigateToDate(LocalDate.now().with(DayOfWeek.MONDAY)));
        
        Button nextWeekLink = new Button("⏭️ Semaine prochaine");
        nextWeekLink.getStyleClass().add("quick-date-link");
        nextWeekLink.setOnAction(e -> navigateToDate(LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)));
        
        quickLinks.getChildren().addAll(todayLink, thisWeekLink, nextWeekLink);
        
        section.getChildren().addAll(title, quickLinks);
        return section;
    }
    
    private VBox createCenterArea() {
        VBox centerArea = new VBox(10);
        centerArea.setPadding(new Insets(20));
        
        // Panneau de notification des conflits (masqué par défaut)
        conflictNotificationPanel.setVisible(false);
        
        // Barre d'outils du calendrier
        HBox calendarToolbar = new HBox(10);
        calendarToolbar.setAlignment(Pos.CENTER_LEFT);
        
        Label eventsCount = new Label("📊 Événements visibles: 0");
        eventsCount.getStyleClass().add("events-count-label");
        
        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);
        
        Button btnMiniMode = new Button("📱 Vue Compacte");
        btnMiniMode.getStyleClass().add("toolbar-button");
        
        calendarToolbar.getChildren().addAll(eventsCount, toolbarSpacer, btnMiniMode);
        
        // Vue calendaire (container qui peut changer)
        VBox.setVgrow(currentCalendarView, Priority.ALWAYS);
        
        centerArea.getChildren().addAll(conflictNotificationPanel, calendarToolbar, currentCalendarView);
        return centerArea;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.getStyleClass().add("planning-footer");
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        
        Label syncStatus = new Label("🔄 Dernière sync: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        syncStatus.getStyleClass().add("sync-status-label");
        
        footer.getChildren().addAll(statusLabel, footerSpacer, syncStatus);
        return footer;
    }
    
    private void setupEventHandlers() {
        // Navigation temporelle
        btnPrevious.setOnAction(e -> navigatePrevious());
        btnNext.setOnAction(e -> navigateNext());
        btnToday.setOnAction(e -> navigateToToday());
        
        // Changement de mode de vue
        viewModeCombo.setOnAction(e -> changeViewMode(viewModeCombo.getValue()));
        
        // Navigation par DatePicker
        datePicker.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                navigateToDate(selectedDate);
            }
        });
        
        // Création d'événement
        btnCreateEvent.setOnAction(e -> showEventCreationDialog(null, null));
        
        // Rafraîchissement
        btnRefresh.setOnAction(e -> refreshCalendar());
        
        // Sélection de créneaux dans les calendriers
        weekCalendarView.setOnTimeSlotSelected(this::showEventCreationDialog);
        dayCalendarView.setOnTimeSlotSelected(this::showEventCreationDialog);
        monthCalendarView.setOnDaySelected(this::showEventCreationDialog);
        
        // Toggle de calendriers et spécialités
        calendarSelectionPanel.setOnCalendarToggled(this::toggleCalendarVisibility);
        // TODO: Réintégrer gestion spécialités
        // specialtyFilterPanel.setOnSpecialtyToggled(this::toggleSpecialtyVisibility);
    }
    
    private void loadStylesheet() {
        try {
            // Chargement du CSS de base du planning
            String cssPath = getClass().getResource("/styles/planning-calendar.css").toExternalForm();
            getStylesheets().add(cssPath);
            
            // Forcer la réapplication du thème pour override les CSS spécifiques
            ThemeManager.getInstance().reapplyCurrentTheme();
            
        } catch (Exception e) {
            System.err.println("Impossible de charger le CSS du planning: " + e.getMessage());
        }
    }
    
    private void loadInitialData() {
        statusLabel.setText("Chargement du planning...");
        
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulation du chargement des données
                Thread.sleep(800);
                
                // Ajout d'événements d'exemple
                javafx.application.Platform.runLater(() -> {
                    addSampleEvents();
                    statusLabel.setText("Planning chargé - Vue " + currentViewMode.label.toLowerCase());
                });
                
                return null;
            }
        };
        
        loadTask.setOnFailed(e -> {
            statusLabel.setText("Erreur de chargement du planning");
            e.getSource().getException().printStackTrace();
        });
        
        Thread.ofVirtual().name("PlanningDataLoader").start(loadTask);
    }
    
    private void addSampleEvents() {
        LocalDate today = LocalDate.now();
        
        // Événements de cette semaine - ajouter à toutes les vues
        addEventToAllViews(
            "Installation éclairage - Théâtre Municipal",
            today.atTime(9, 0),
            today.atTime(17, 0),
            "installation"
        );
        
        addEventToAllViews(
            "Maintenance console - Studio A",
            today.plusDays(1).atTime(14, 0),
            today.plusDays(1).atTime(16, 30),
            "maintenance"
        );
        
        addEventToAllViews(
            "Formation nouveaux techniciens",
            today.plusDays(2).atTime(10, 0),
            today.plusDays(2).atTime(12, 0),
            "formation"
        );
        
        addEventToAllViews(
            "Réunion équipe technique",
            today.plusDays(3).atTime(16, 0),
            today.plusDays(3).atTime(17, 0),
            "reunion"
        );
    }
    
    private void addEventToAllViews(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        weekCalendarView.addEvent(title, startTime, endTime, category);
        dayCalendarView.addEvent(title, startTime, endTime, category);
        monthCalendarView.addEvent(title, startTime, endTime, category);
    }
    

    

    
    private void refreshEventsForCurrentView() {
        // Nettoyer les événements existants
        clearAllEvents();
        
        // Recharger les événements de base
        addSampleEvents();
    }
    
    private void clearAllEvents() {
        weekCalendarView.clearEvents();
        dayCalendarView.clearEvents();
        monthCalendarView.clearEvents();
    }
    
    // === MÉTHODES DE NAVIGATION ===
    
    private void navigatePrevious() {
        switch (currentViewMode) {
            case DAY -> currentDate = currentDate.minusDays(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case MONTH -> currentDate = currentDate.minusMonths(1);
        }
        updateView();
    }
    
    private void navigateNext() {
        switch (currentViewMode) {
            case DAY -> currentDate = currentDate.plusDays(1);
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case MONTH -> currentDate = currentDate.plusMonths(1);
        }
        updateView();
    }
    
    private void navigateToToday() {
        navigateToDate(LocalDate.now());
    }
    
    private void navigateToDate(LocalDate date) {
        currentDate = date;
        datePicker.setValue(date);
        updateView();
    }
    
    private void changeViewMode(ViewMode newMode) {
        if (newMode != null && newMode != currentViewMode) {
            currentViewMode = newMode;
            updateView();
        }
    }
    
    private void updateView() {
        updatePeriodLabel();
        switchCalendarView();
        
        switch (currentViewMode) {
            case WEEK -> {
                LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
                weekCalendarView.setStartOfWeek(startOfWeek);
                statusLabel.setText("Vue semaine");
            }
            case DAY -> {
                dayCalendarView.setCurrentDate(currentDate);
                statusLabel.setText("Vue jour");
            }
            case MONTH -> {
                monthCalendarView.setCurrentMonth(YearMonth.from(currentDate));
                statusLabel.setText("Vue mois");
            }
        }
        
        // Recharger les événements pour la nouvelle vue
        refreshEventsForCurrentView();
    }
    
    private void switchCalendarView() {
        // Sélectionner la nouvelle vue
        Region newView = switch (currentViewMode) {
            case DAY -> dayCalendarView;
            case WEEK -> weekCalendarView;
            case MONTH -> monthCalendarView;
        };
        
        // Ne rien faire si c'est déjà la vue actuelle
        if (newView == currentCalendarView) {
            return;
        }
        
        // Rechercher le container central
        VBox centerArea = findCenterArea();
        if (centerArea != null) {
            // Supprimer l'ancienne vue si elle existe
            centerArea.getChildren().remove(currentCalendarView);
            
            // Configurer la nouvelle vue
            VBox.setVgrow(newView, Priority.ALWAYS);
            
            // Ajouter la nouvelle vue
            if (centerArea.getChildren().size() >= 1) {
                // Après la toolbar (index 1)
                centerArea.getChildren().add(newView);
            } else {
                // Fallback si pas de toolbar
                centerArea.getChildren().add(newView);
            }
            
            // Mettre à jour la référence
            currentCalendarView = newView;
        }
    }
    
    private VBox findCenterArea() {
        // Rechercher la zone centrale dans la structure
        if (getCenter() instanceof HBox mainContent) {
            for (javafx.scene.Node child : mainContent.getChildren()) {
                if (child instanceof VBox vbox && vbox.getStyleClass().contains("planning-main-area")) {
                    return vbox;
                }
            }
        }
        return null;
    }
    
    private void updatePeriodLabel() {
        String periodText = switch (currentViewMode) {
            case DAY -> currentDate.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy"));
            case WEEK -> {
                LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                yield startOfWeek.format(DateTimeFormatter.ofPattern("dd MMM")) + 
                      " - " + 
                      endOfWeek.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            }
            case MONTH -> currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        };
        
        currentPeriodLabel.setText(periodText);
    }
    
    // === GESTION DES ÉVÉNEMENTS ===
    
    private void showEventCreationDialog(LocalDateTime defaultStart, LocalDateTime defaultEnd) {
        EventCreationDialog dialog = new EventCreationDialog(specialtyService);
        dialog.showAndWait();
        
        Optional<EventCreationDialog.EventResult> result = dialog.getResult();
        if (result.isPresent()) {
            EventCreationDialog.EventResult eventData = result.get();
            
            // Ajouter l'événement à tous les calendriers
            addEventToAllViews(
                eventData.getTitle(),
                eventData.getStartDateTime(),
                eventData.getEndDateTime(),
                eventData.getCategory()
            );
            
            // Afficher les détails du personnel assigné
            String personnelInfo = "";
            if (!eventData.getSelectedPersonnel().isEmpty()) {
                personnelInfo = " - Personnel: " + eventData.getSelectedPersonnel().size() + " technicien(s)";
            }
            
            statusLabel.setText("Événement créé: " + eventData.getTitle() + personnelInfo);
            
            // TODO: Sauvegarder en base de données via ApiService
            System.out.println("Événement créé avec spécialité: " + eventData.getSpecialty());
            eventData.getSelectedPersonnel().forEach(p -> 
                System.out.println("  - " + p.getPersonnelName() + " (" + p.getSpecialty() + ", niveau " + p.getProficiencyLevel() + ")")
            );
        }
    }
    

    
    private void toggleCalendarVisibility(CalendarSelectionPanel.CalendarItem calendarItem) {
        statusLabel.setText("Calendrier '" + calendarItem.getName() + "' " + 
                          (calendarItem.isVisible() ? "affiché" : "masqué"));
        
        // TODO: Implémenter le filtrage des événements selon les calendriers visibles
        refreshCalendar();
    }
    

    
    private void refreshCalendar() {
        statusLabel.setText("Actualisation du calendrier...");
        
        Task<Void> refreshTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(500);
                return null;
            }
        };
        
        refreshTask.setOnSucceeded(e -> {
            // TODO: Recharger les données depuis l'API
            statusLabel.setText("Planning actualisé");
        });
        
        Thread.ofVirtual().name("CalendarRefresh").start(refreshTask);
    }
    
    /**
     * Méthode publique pour actualiser depuis l'extérieur
     */
    public void refresh() {
        refreshCalendar();
    }
}
