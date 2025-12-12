package com.magscene.magsav.desktop.view.planning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.view.planning.calendar.WeekCalendarView;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Interface JavaFX moderne pour le module Planning
 * Vue calendaire visuelle type Google Agenda avec interaction souris
 */
public class PlanningView extends BorderPane {

    private static final Logger logger = Logger.getLogger(PlanningView.class.getName());

    private ApiService apiService;
    private SpecialtyPlanningService specialtyService;

    // Composants de navigation simplifiés
    private Label currentPeriodLabel;
    private ComboBox<ViewMode> viewModeCombo;

    // Vues calendaires
    private WeekCalendarView weekCalendarView;
    private DayCalendarView dayCalendarView;
    private MonthCalendarView monthCalendarView;
    private YearCalendarView yearCalendarView;
    private Region currentCalendarView;

    // État actuel
    private LocalDate currentDate = LocalDate.now();
    private ViewMode currentViewMode = ViewMode.WEEK;

    // CheckBox des agendas
    private CheckBox mainCalendar;
    private CheckBox technicianCalendar;
    private CheckBox vehicleCalendar;
    private CheckBox maintenanceCalendar;
    private CheckBox externalCalendar;

    // Système de couleurs personnalisables
    private Map<String, String> agendaColors;
    private Map<String, ColorPicker> colorPickers;

    // Volet rétractable des agendas
    private VBox calendarSidebar;
    private VBox toggleColumn;
    private HBox mainContainer;
    private Button toggleButton;
    private boolean sidebarExpanded = true;

    public enum ViewMode {
        DAY("Jour"),
        WEEK("Semaine"),
        MONTH("Mois"),
        YEAR("Année");

        private final String label;

        ViewMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public PlanningView(ApiService apiService) {
        this.apiService = apiService;
        this.specialtyService = new SpecialtyPlanningService(apiService);

        // Configuration d'expansion complète pour utiliser tout l'espace de mainContent
        this.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);

        initializeComponents();
        setupLayout();
        loadStylesheet();
        setupEventHandlers();
        loadInitialData();
    }

    private void initializeComponents() {
        // Initialiser les couleurs personnalisables par défaut
        initializeAgendaColors();

        // Navigation temporelle simplifiée
        currentPeriodLabel = new Label();
        // currentPeriodLabel supprimé - Style géré par CSS
        viewModeCombo = new ComboBox<>();
        viewModeCombo.getItems().addAll(ViewMode.values());
        viewModeCombo.setValue(ViewMode.WEEK);
        // viewModeCombo supprimé - Style géré par CSS
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        weekCalendarView = new WeekCalendarView(startOfWeek);
        dayCalendarView = new DayCalendarView();
        dayCalendarView.setCurrentDate(currentDate);
        monthCalendarView = new MonthCalendarView();
        monthCalendarView.setCurrentMonth(YearMonth.from(currentDate));
        yearCalendarView = new YearCalendarView();
        yearCalendarView.setCurrentYear(currentDate.getYear());
        currentCalendarView = weekCalendarView;

        updatePeriodLabel();
    }

    private void setupLayout() {
        getStyleClass().add("planning-view");
        setStyle(// Couleur debug supprimée - utilise maintenant les couleurs standardisées
                "-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

        // Toolbar standardisée (plus de header vide)
        HBox toolbar = createStandardToolbar();

        setTop(toolbar);

        // Zone centrale avec le calendrier (sans sidebar complexe)
        VBox centerArea = createSimplifiedCenterArea();
        centerArea.getStyleClass().add("planning-main-area");
        // Force l'expansion sur centerArea sans debug
        centerArea.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");
        setCenter(centerArea);
    }

    private HBox createStandardToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // toolbar supprimé - Style géré par CSS
        Button newEventButton = new Button("Nouvel Événement");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        newEventButton.setOnAction(e -> showEventCreationDialog(null, null));

        Button editButton = new Button("Modifier");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        Button deleteButton = new Button("Supprimer");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        // Navigation temporelle simplifiée
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navControls = new HBox(8);
        navControls.setAlignment(Pos.CENTER_RIGHT);

        Button prevButton = new Button("◀");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        prevButton.setOnAction(e -> navigatePrevious());

        Button nextButton = new Button("▶");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        nextButton.setOnAction(e -> navigateNext());

        Button todayButton = new Button("Aujourd'hui");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        todayButton.setOnAction(e -> navigateToToday());

        navControls.getChildren().addAll(prevButton, currentPeriodLabel, nextButton, todayButton, viewModeCombo);

        // Inversion : sélecteurs de mode à gauche, boutons d'action à droite
        toolbar.getChildren().addAll(navControls, spacer, newEventButton, editButton, deleteButton);
        return toolbar;
    }

    private VBox createSimplifiedCenterArea() {
        VBox centerArea = new VBox(0);
        centerArea.setPadding(new Insets(0)); // Suppression du padding pour éviter le rognage
        centerArea.setFillWidth(true); // FORCE l'expansion horizontale des enfants
        centerArea.getStyleClass().add("planning-main-area");

        // Container horizontal : Sidebar + Toggle Column + Calendrier
        mainContainer = new HBox(0); // Pas d'espacement entre les éléments
        mainContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        mainContainer.setMaxHeight(Double.MAX_VALUE);
        mainContainer.setFillHeight(true); // FORCE l'expansion verticale des enfants; // CSS pour l'expansion sans
                                           // debug
        mainContainer.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

        // IMPORTANT: Activer le clipping pour masquer les parties du volet qui
        // dépassent à gauche
        mainContainer.setClip(new Rectangle(0, 0, 1, 1)); // Rectangle sera redimensionné automatiquement; // Sidebar
                                                          // des agendas (sans bouton toggle)
        calendarSidebar = createCalendarSidebar();

        // Fine colonne toggle à droite du sidebar
        toggleColumn = createToggleColumn();

        // Vue calendaire principale prend TOUT l'espace disponible
        currentCalendarView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        currentCalendarView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        currentCalendarView.setMaxWidth(Double.MAX_VALUE);
        currentCalendarView.setMaxHeight(Double.MAX_VALUE);
        // CSS d'expansion sans debug
        currentCalendarView.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

        HBox.setHgrow(currentCalendarView, Priority.ALWAYS);
        VBox.setVgrow(currentCalendarView, Priority.ALWAYS);

        // Ajout conditionnel : Sidebar + Toggle column si étendu, ou juste Toggle
        // column si rétracté
        if (sidebarExpanded) {
            mainContainer.getChildren().addAll(calendarSidebar, toggleColumn, currentCalendarView);
        } else {
            mainContainer.getChildren().addAll(toggleColumn, currentCalendarView);
        }

        // Ajouter le mainContainer avec expansion verticale dans centerArea
        centerArea.getChildren().add(mainContainer);

        // CRITIQUE: Définir les propriétés d'expansion APRÈS avoir ajouté l'élément
        HBox.setHgrow(mainContainer, Priority.ALWAYS);
        VBox.setVgrow(mainContainer, Priority.ALWAYS); // mainContainer doit s'étendre verticalement; // Forcer
                                                       // l'expansion verticale du centerArea aussi
        centerArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
        centerArea.setMaxHeight(Double.MAX_VALUE);
        centerArea.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

        // Listener pour forcer l'expansion dynamique
        centerArea.heightProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("🟢 CenterArea hauteur changée: " + oldVal + " -> " + newVal);
            if (mainContainer != null) {
                mainContainer.setPrefHeight(newVal.doubleValue());
                mainContainer.setMinHeight(newVal.doubleValue());
                System.out.println("🔵 MainContainer forcé à hauteur: " + newVal.doubleValue());
            }
        });

        // Listener pour ajuster le rectangle de clipping dynamiquement
        mainContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (mainContainer.getClip() instanceof Rectangle) {
                Rectangle clip = (Rectangle) mainContainer.getClip();
                clip.setWidth(newVal.doubleValue());
            }
        });

        mainContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (mainContainer.getClip() instanceof Rectangle) {
                Rectangle clip = (Rectangle) mainContainer.getClip();
                clip.setHeight(newVal.doubleValue());
            }
        });

        return centerArea;
    }

    private VBox createToggleColumn() {
        VBox toggleCol = new VBox();
        toggleCol.setPrefWidth(20);
        toggleCol.setMinWidth(20);
        toggleCol.setMaxWidth(20);
        toggleCol.setPrefHeight(Region.USE_COMPUTED_SIZE);
        toggleCol.setMaxHeight(Double.MAX_VALUE); // FORCE l'expansion verticale
        toggleCol.setAlignment(Pos.CENTER);
        toggleCol.setPadding(new Insets(0));
        toggleCol.setStyle("-fx-background-color: " + StandardColors.DARK_SECONDARY + "; -fx-border-color: "
                + ThemeConstants.BACKGROUND_SECONDARY + "; -fx-border-width: 0 1 0 1;");

        // Bouton toggle qui occupe toute la hauteur
        toggleButton = new Button(sidebarExpanded ? "◀" : "▶");
        toggleButton.setPrefWidth(20);
        toggleButton.setMaxWidth(20);
        toggleButton.setPrefHeight(Double.MAX_VALUE);
        toggleButton.setMaxHeight(Double.MAX_VALUE);
        // toggleButton - Style géré par CSS automatiquement - Effets hover
        toggleButton.setOnMouseEntered(e -> {
            /* Style géré par CSS automatiquement */ });
        toggleButton.setOnMouseExited(e -> {
            /* Style géré par CSS automatiquement */ });
        toggleButton.setOnAction(e -> toggleSidebar());

        VBox.setVgrow(toggleButton, Priority.ALWAYS);
        toggleCol.getChildren().add(toggleButton);

        return toggleCol;
    }

    private VBox createCalendarSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(200);
        sidebar.setPrefHeight(Region.USE_COMPUTED_SIZE);
        sidebar.setMaxHeight(Double.MAX_VALUE); // FORCE l'expansion verticale
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-background-radius: 8 0 0 8; -fx-border-color: "
                + ThemeConstants.BACKGROUND_SECONDARY + "; -fx-border-width: 1 0 1 1;");

        // Header simple avec juste le titre
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(8);
        header.setPadding(new Insets(0, 0, 10, 0));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        Label title = new Label("📅 Agendas");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                + ThemeConstants.BACKGROUND_SECONDARY + "; -fx-background-color: "
                + ThemeConstants.BACKGROUND_PRIMARY + " !important;");

        header.getChildren().add(title);

        // Liste des agendas avec cases à cocher
        VBox calendarList = new VBox(2); // Espacement réduit pour éviter les zones vides
        // calendarList - Style géré par CSS automatiquement - Créer les agendas avec
        // sélecteurs de couleur
        mainCalendar = new CheckBox("Planning Principal");
        mainCalendar.setSelected(true);
        mainCalendar.getStyleClass().add("agenda-checkbox-main");
        mainCalendar.selectedProperty().addListener((obs, oldVal, newVal) -> refreshCalendarDisplay());

        technicianCalendar = new CheckBox("Techniciens");
        technicianCalendar.setSelected(true);
        technicianCalendar.getStyleClass().add("agenda-checkbox-technician");
        technicianCalendar.selectedProperty().addListener((obs, oldVal, newVal) -> refreshCalendarDisplay());

        vehicleCalendar = new CheckBox("Véhicules");
        vehicleCalendar.setSelected(true);
        vehicleCalendar.getStyleClass().add("agenda-checkbox-vehicle");
        vehicleCalendar.selectedProperty().addListener((obs, oldVal, newVal) -> refreshCalendarDisplay());

        maintenanceCalendar = new CheckBox("Maintenance");
        maintenanceCalendar.setSelected(false);
        maintenanceCalendar.getStyleClass().add("agenda-checkbox-maintenance");
        maintenanceCalendar.selectedProperty().addListener((obs, oldVal, newVal) -> refreshCalendarDisplay());

        externalCalendar = new CheckBox("Agenda Externe");
        externalCalendar.setSelected(false);
        externalCalendar.getStyleClass().add("agenda-checkbox-external");
        externalCalendar.selectedProperty().addListener((obs, oldVal, newVal) -> refreshCalendarDisplay());

        // Créer les conteneurs avec sélecteurs de couleur
        HBox mainContainer = createAgendaWithColorPicker(mainCalendar, "principal");
        HBox techContainer = createAgendaWithColorPicker(technicianCalendar, "technician");
        HBox vehicleContainer = createAgendaWithColorPicker(vehicleCalendar, "vehicle");
        HBox maintenanceContainer = createAgendaWithColorPicker(maintenanceCalendar, "maintenance");
        HBox externalContainer = createAgendaWithColorPicker(externalCalendar, "external");

        calendarList.getChildren().addAll(
                mainContainer,
                techContainer,
                vehicleContainer,
                maintenanceContainer,
                externalContainer);

        // Appliquer les couleurs initiales aux checkbox
        initializeCheckboxColors();

        // Boutons d'action
        Button syncButton = new Button("Synchroniser");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        syncButton.setPrefWidth(Double.MAX_VALUE);

        Button addCalendarButton = new Button("+ Ajouter Agenda");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        addCalendarButton.setPrefWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(header, calendarList, syncButton, addCalendarButton);
        return sidebar;
    }

    private void refreshCalendarDisplay() {
        System.out.println("🔄 refreshCalendarDisplay() appelée");

        // Synchroniser les couleurs personnalisées avec WeekCalendarView
        for (Map.Entry<String, String> entry : agendaColors.entrySet()) {
            WeekCalendarView.setAgendaColor(entry.getKey(), entry.getValue());
        }

        // Nettoyer tous les événements existants
        System.out.println("🧹 Nettoyage des événements...");
        weekCalendarView.clearEvents();
        dayCalendarView.clearEvents();
        monthCalendarView.clearEvents();
        yearCalendarView.clearEvents();

        // Ajouter les événements selon les agendas sélectionnés
        if (mainCalendar.isSelected()) {
            addMainCalendarEvents();
        }

        if (technicianCalendar.isSelected()) {
            addTechnicianEvents();
        }

        if (vehicleCalendar.isSelected()) {
            addVehicleEvents();
        }

        if (maintenanceCalendar.isSelected()) {
            addMaintenanceEvents();
        }

        if (externalCalendar.isSelected()) {
            addExternalEvents();
        }

        System.out.println("🔄 Calendrier mis à jour - Agendas affichés: " +
                "Principal=" + mainCalendar.isSelected() +
                ", Techniciens=" + technicianCalendar.isSelected() +
                ", Véhicules=" + vehicleCalendar.isSelected() +
                ", Maintenance=" + maintenanceCalendar.isSelected() +
                ", Externe=" + externalCalendar.isSelected());
    }

    private void setupEventHandlers() {
        // Changement de mode de vue
        viewModeCombo.setOnAction(e -> changeViewMode(viewModeCombo.getValue()));

        // Sélection de créneaux dans les calendriers
        weekCalendarView.setOnTimeSlotSelected(this::showEventCreationDialog);
        dayCalendarView.setOnTimeSlotSelected(this::showEventCreationDialog);
        monthCalendarView.setOnDaySelected(this::showEventCreationDialog);

        // Navigation depuis la vue année vers un mois
        yearCalendarView.setOnMonthSelected((yearMonth, date) -> {
            currentDate = date;
            currentViewMode = ViewMode.MONTH;
            viewModeCombo.setValue(ViewMode.MONTH);
            updateView();
        });
    }

    private void loadStylesheet() {
        try {
            // Forcer la réapplication du thème existant
            UnifiedThemeManager.getInstance().applyTheme(UnifiedThemeManager.getInstance().getCurrentTheme());

        } catch (Exception e) {
            System.err.println("Impossible de charger le CSS du planning: " + e.getMessage());
        }
    }

    private void loadInitialData() {
        System.out.println("📥 loadInitialData() appelée - Début du chargement");
        // Chargement du planning simplifié

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("⏳ Task.call() - Début du sleep");
                // Simulation du chargement des données
                Thread.sleep(800);
                System.out.println("⏳ Task.call() - Sleep terminé, appel de Platform.runLater");

                // Ajout d'événements d'exemple
                javafx.application.Platform.runLater(() -> {
                    System.out.println("▶️ Platform.runLater() exécutée - Appel de refreshCalendarDisplay");
                    refreshCalendarDisplay(); // Utiliser la nouvelle logique
                });

                return null;
            }
        };

        loadTask.setOnFailed(e -> {
            Throwable exception = e.getSource().getException();
            logger.log(Level.SEVERE, "Erreur lors du chargement du planning", exception);
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
                "installation");

        addEventToAllViews(
                "Maintenance console - Studio A",
                today.plusDays(1).atTime(14, 0),
                today.plusDays(1).atTime(16, 30),
                "maintenance");

        addEventToAllViews(
                "Formation nouveaux techniciens",
                today.plusDays(2).atTime(10, 0),
                today.plusDays(2).atTime(12, 0),
                "formation");

        addEventToAllViews(
                "Réunion équipe technique",
                today.plusDays(3).atTime(16, 0),
                today.plusDays(3).atTime(17, 0),
                "reunion");
    }

    private void addEventToAllViews(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        weekCalendarView.addEvent(title, startTime, endTime, category);
        dayCalendarView.addEvent(title, startTime, endTime, category);
        monthCalendarView.addEvent(title, startTime, endTime, category);
        yearCalendarView.addEvent(title, startTime, endTime, category);
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
        yearCalendarView.clearEvents();
    }

    // === MÉTHODES DE NAVIGATION ===

    private void navigatePrevious() {
        switch (currentViewMode) {
            case DAY -> currentDate = currentDate.minusDays(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case YEAR -> currentDate = currentDate.minusYears(1);
        }
        updateView();
    }

    private void navigateNext() {
        switch (currentViewMode) {
            case DAY -> currentDate = currentDate.plusDays(1);
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case MONTH -> currentDate = currentDate.plusMonths(1);
            case YEAR -> currentDate = currentDate.plusYears(1);
        }
        updateView();
    }

    private void navigateToToday() {
        navigateToDate(LocalDate.now());
    }

    private void navigateToDate(LocalDate date) {
        currentDate = date;
        // datePicker.setValue(date);
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
                // statusLabel.setText("Vue semaine");
            }
            case DAY -> {
                dayCalendarView.setCurrentDate(currentDate);
                // statusLabel.setText("Vue jour");
            }
            case MONTH -> {
                monthCalendarView.setCurrentMonth(YearMonth.from(currentDate));
                // statusLabel.setText("Vue mois");
            }
            case YEAR -> {
                yearCalendarView.setCurrentYear(currentDate.getYear());
                // statusLabel.setText("Vue année");
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
            case YEAR -> yearCalendarView;
        };

        // Ne rien faire si c'est déjà la vue actuelle
        if (newView == currentCalendarView) {
            return;
        }

        // Utiliser directement mainContainer qui est maintenant un attribut de classe
        if (mainContainer != null) {
            // Trouver l'index de l'ancienne vue dans le container
            int oldViewIndex = mainContainer.getChildren().indexOf(currentCalendarView);

            if (oldViewIndex >= 0) {
                // Supprimer l'ancienne vue
                mainContainer.getChildren().remove(currentCalendarView);

                // Configurer la nouvelle vue pour prendre tout l'espace disponible
                newView.setPrefWidth(Region.USE_COMPUTED_SIZE);
                newView.setPrefHeight(Region.USE_COMPUTED_SIZE);
                newView.setMaxWidth(Double.MAX_VALUE);
                newView.setMaxHeight(Double.MAX_VALUE);

                // CSS d'expansion forcée - identique aux autres vues
                newView.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

                // IMPORTANT: Le conteneur principal est un HBox, donc utiliser HBox.setHgrow
                HBox.setHgrow(newView, Priority.ALWAYS);

                // Forcer aussi l'expansion verticale directement sur la vue
                if (newView instanceof VBox vboxView) {
                    vboxView.setFillWidth(true);
                }

                // Ajouter la nouvelle vue à la même position
                mainContainer.getChildren().add(oldViewIndex, newView);

                // Mettre à jour la référence
                currentCalendarView = newView;

                System.out.println("🔄 Vue changée vers: " + currentViewMode + " (nouvelle vue: "
                        + newView.getClass().getSimpleName() + ")");
            }
        }
    }

    @SuppressWarnings("unused") // Réservé pour future utilisation
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
            case YEAR -> String.valueOf(currentDate.getYear());
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
                    eventData.getCategory());

            // Afficher les détails du personnel assigné
            String personnelInfo = "";
            if (!eventData.getSelectedPersonnel().isEmpty()) {
                personnelInfo = " - Personnel: " + eventData.getSelectedPersonnel().size() + " technicien(s)";
            }

            System.out.println("Événement créé: " + eventData.getTitle() + personnelInfo);

            // Sauvegarder en base de données via ApiService
            savePlanningEventAsync(eventData);
        }
    }

    private void refreshCalendar() {
        // statusLabel.setText("Actualisation du calendrier...");

        Task<Void> refreshTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(500);
                return null;
            }
        };

        refreshTask.setOnSucceeded(e -> {
            // TODO: Recharger les données depuis l'API; // statusLabel.setText("Planning
            // actualisé");
        });

        Thread.ofVirtual().name("CalendarRefresh").start(refreshTask);
    }

    /**
     * Sauvegarde asynchrone d'un événement de planning
     */
    private void savePlanningEventAsync(EventCreationDialog.EventResult eventData) {
        Task<Object> saveTask = new Task<>() {
            @Override
            protected Object call() throws Exception {
                // Conversion des données EventResult vers Map pour l'API
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("title", eventData.getTitle());
                eventMap.put("description", eventData.getDescription());
                eventMap.put("startDateTime", eventData.getStartDateTime().toString());
                eventMap.put("endDateTime", eventData.getEndDateTime().toString());
                eventMap.put("category", eventData.getCategory());
                eventMap.put("requiredSpecialties", eventData.getSpecialty());

                // Formatage du personnel assigné pour la base de données
                if (eventData.getSelectedPersonnel() != null && !eventData.getSelectedPersonnel().isEmpty()) {
                    StringBuilder personnelBuilder = new StringBuilder();
                    for (int i = 0; i < eventData.getSelectedPersonnel().size(); i++) {
                        var personnel = eventData.getSelectedPersonnel().get(i);
                        if (i > 0)
                            personnelBuilder.append(";");
                        personnelBuilder.append(personnel.getPersonnelId())
                                .append(":")
                                .append(personnel.getSpecialty());
                    }
                    eventMap.put("assignedPersonnel", personnelBuilder.toString());
                } else {
                    eventMap.put("assignedPersonnel", "");
                }

                eventMap.put("type", "SCHEDULED_WORK");
                eventMap.put("priority", "MEDIUM");
                eventMap.put("location", ""); // À ajouter si nécessaire dans le dialog
                eventMap.put("createdBy", "current_user"); // À récupérer depuis le contexte utilisateur

                return apiService.createPlanningEvent(eventMap).get();
            }
        };

        saveTask.setOnSucceeded(e -> {
            logger.log(Level.INFO, "Événement de planning sauvegardé avec succès: {0}", eventData.getTitle());

            Platform.runLater(() -> {
                // statusLabel.setText("✅ Événement '" + eventData.getTitle() + "' sauvegardé
                // avec succès");

                // Debug du personnel assigné
                if (eventData.getSelectedPersonnel() != null) {
                    eventData.getSelectedPersonnel().forEach(p -> {
                        logger.log(Level.INFO, "Personnel assigné: {0} ({1}, niveau {2})",
                                new Object[] { p.getPersonnelName(), p.getSpecialty(), p.getProficiencyLevel() });
                    });
                }

                // Actualiser la vue calendaire
                refreshCalendar();
            });
        });

        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            logger.log(Level.SEVERE, "Erreur lors de la sauvegarde de l'événement de planning", exception);

            Platform.runLater(() -> {
                // statusLabel.setText("❌ Erreur lors de la sauvegarde: " +
                // exception.getMessage());

                // Afficher une alerte détaillée
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de Sauvegarde");
                alert.setHeaderText("Impossible de sauvegarder l'événement");
                alert.setContentText("Erreur: " + exception.getMessage() +
                        "\n\nL'événement a été créé localement mais n'a pas pu être sauvegardé en base.");
                alert.showAndWait();
            });
        });

        // Feedback immédiat à l'utilisateur; // statusLabel.setText("💾 Sauvegarde de
        // l'événement '" + eventData.getTitle() + "' en cours...");

        // Exécution asynchrone
        Thread.ofVirtual().name("SavePlanningEvent").start(saveTask);
    }

    /**
     * Méthode publique pour actualiser depuis l'extérieur
     */
    public void refresh() {
        refreshCalendar();
    }

    // ========== MÉTHODES DE GÉNÉRATION D'ÉVÉNEMENTS DE TEST ==========

    private void addMainCalendarEvents() {
        System.out.println("➕ Ajout des événements du planning principal...");
        LocalDate today = LocalDate.now();

        // Événements du planning principal
        weekCalendarView.addEvent("Réunion équipe",
                today.atTime(9, 0), today.atTime(10, 30), "principal");
        weekCalendarView.addEvent("Formation sécurité",
                today.atTime(14, 0), today.atTime(16, 0), "principal");
        weekCalendarView.addEvent("Point projet",
                today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(12, 0), "principal");

        dayCalendarView.addEvent("Réunion équipe",
                today.atTime(9, 0), today.atTime(10, 30), "principal");
        dayCalendarView.addEvent("Formation sécurité",
                today.atTime(14, 0), today.atTime(16, 0), "principal");

        monthCalendarView.addEvent("Réunion équipe",
                today.atTime(9, 0), today.atTime(10, 30), "principal");
        monthCalendarView.addEvent("Formation sécurité",
                today.atTime(14, 0), today.atTime(16, 0), "principal");

        yearCalendarView.addEvent("Réunion équipe",
                today.atTime(9, 0), today.atTime(10, 30), "principal");
        yearCalendarView.addEvent("Formation sécurité",
                today.atTime(14, 0), today.atTime(16, 0), "principal");
        yearCalendarView.addEvent("Point projet",
                today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(12, 0), "principal");
    }

    private void addTechnicianEvents() {
        System.out.println("➕ Ajout des événements des techniciens...");
        LocalDate today = LocalDate.now();

        // Événements des techniciens
        weekCalendarView.addEvent("SAV Client A",
                today.atTime(8, 0), today.atTime(12, 0), "technician");
        weekCalendarView.addEvent("Installation Client B",
                today.atTime(13, 0), today.atTime(17, 0), "technician");
        weekCalendarView.addEvent("Maintenance préventive",
                today.plusDays(1).atTime(9, 0), today.plusDays(1).atTime(11, 0), "technician");
        weekCalendarView.addEvent("Dépannage urgent",
                today.plusDays(2).atTime(15, 0), today.plusDays(2).atTime(18, 0), "technician");

        dayCalendarView.addEvent("SAV Client A",
                today.atTime(8, 0), today.atTime(12, 0), "technician");
        dayCalendarView.addEvent("Installation Client B",
                today.atTime(13, 0), today.atTime(17, 0), "technician");

        monthCalendarView.addEvent("SAV Client A",
                today.atTime(8, 0), today.atTime(12, 0), "technician");
        monthCalendarView.addEvent("Installation Client B",
                today.atTime(13, 0), today.atTime(17, 0), "technician");

        yearCalendarView.addEvent("SAV Client A",
                today.atTime(8, 0), today.atTime(12, 0), "technician");
        yearCalendarView.addEvent("Installation Client B",
                today.atTime(13, 0), today.atTime(17, 0), "technician");
        yearCalendarView.addEvent("Maintenance préventive",
                today.plusDays(1).atTime(9, 0), today.plusDays(1).atTime(11, 0), "technician");
        yearCalendarView.addEvent("Dépannage urgent",
                today.plusDays(2).atTime(15, 0), today.plusDays(2).atTime(18, 0), "technician");
    }

    private void addVehicleEvents() {
        System.out.println("➕ Ajout des événements des véhicules...");
        LocalDate today = LocalDate.now();

        // Événements des véhicules
        weekCalendarView.addEvent("Véhicule 001 - Mission",
                today.atTime(8, 30), today.atTime(16, 30), "vehicle");
        weekCalendarView.addEvent("Véhicule 002 - Livraison",
                today.atTime(10, 0), today.atTime(14, 0), "vehicle");
        weekCalendarView.addEvent("Véhicule 003 - Déplacement",
                today.plusDays(1).atTime(7, 0), today.plusDays(1).atTime(19, 0), "vehicle");

        dayCalendarView.addEvent("Véhicule 001 - Mission",
                today.atTime(8, 30), today.atTime(16, 30), "vehicle");
        dayCalendarView.addEvent("Véhicule 002 - Livraison",
                today.atTime(10, 0), today.atTime(14, 0), "vehicle");

        monthCalendarView.addEvent("Véhicule 001 - Mission",
                today.atTime(8, 30), today.atTime(16, 30), "vehicle");
        monthCalendarView.addEvent("Véhicule 002 - Livraison",
                today.atTime(10, 0), today.atTime(14, 0), "vehicle");

        yearCalendarView.addEvent("Véhicule 001 - Mission",
                today.atTime(8, 30), today.atTime(16, 30), "vehicle");
        yearCalendarView.addEvent("Véhicule 002 - Livraison",
                today.atTime(10, 0), today.atTime(14, 0), "vehicle");
        yearCalendarView.addEvent("Véhicule 003 - Déplacement",
                today.plusDays(1).atTime(7, 0), today.plusDays(1).atTime(19, 0), "vehicle");
    }

    private void addMaintenanceEvents() {
        LocalDate today = LocalDate.now();

        // Événements de maintenance
        weekCalendarView.addEvent("Maintenance préventive",
                today.atTime(7, 0), today.atTime(9, 0), "maintenance");
        weekCalendarView.addEvent("Contrôle technique",
                today.plusDays(2).atTime(8, 0), today.plusDays(2).atTime(10, 0), "maintenance");
        weekCalendarView.addEvent("Révision équipement",
                today.plusDays(3).atTime(14, 0), today.plusDays(3).atTime(17, 0), "maintenance");

        dayCalendarView.addEvent("Maintenance préventive",
                today.atTime(7, 0), today.atTime(9, 0), "maintenance");

        monthCalendarView.addEvent("Maintenance préventive",
                today.atTime(7, 0), today.atTime(9, 0), "maintenance");
        monthCalendarView.addEvent("Contrôle technique",
                today.plusDays(2).atTime(8, 0), today.plusDays(2).atTime(10, 0), "maintenance");

        yearCalendarView.addEvent("Maintenance préventive",
                today.atTime(7, 0), today.atTime(9, 0), "maintenance");
        yearCalendarView.addEvent("Contrôle technique",
                today.plusDays(2).atTime(8, 0), today.plusDays(2).atTime(10, 0), "maintenance");
        yearCalendarView.addEvent("Révision équipement",
                today.plusDays(3).atTime(14, 0), today.plusDays(3).atTime(17, 0), "maintenance");
    }

    private void addExternalEvents() {
        LocalDate today = LocalDate.now();

        // Événements externes
        weekCalendarView.addEvent("RDV fournisseur",
                today.atTime(10, 0), today.atTime(11, 30), "external");
        weekCalendarView.addEvent("Conférence",
                today.plusDays(1).atTime(14, 0), today.plusDays(1).atTime(18, 0), "external");
        weekCalendarView.addEvent("Audit qualité",
                today.plusDays(4).atTime(9, 0), today.plusDays(4).atTime(12, 0), "external");

        dayCalendarView.addEvent("RDV fournisseur",
                today.atTime(10, 0), today.atTime(11, 30), "external");

        monthCalendarView.addEvent("RDV fournisseur",
                today.atTime(10, 0), today.atTime(11, 30), "external");
        monthCalendarView.addEvent("Conférence",
                today.plusDays(1).atTime(14, 0), today.plusDays(1).atTime(18, 0), "external");

        yearCalendarView.addEvent("RDV fournisseur",
                today.atTime(10, 0), today.atTime(11, 30), "external");
        yearCalendarView.addEvent("Conférence",
                today.plusDays(1).atTime(14, 0), today.plusDays(1).atTime(18, 0), "external");
        yearCalendarView.addEvent("Audit qualité",
                today.plusDays(4).atTime(9, 0), today.plusDays(4).atTime(12, 0), "external");
    }

    // ========== SYSTÈME DE COULEURS PERSONNALISABLES ==========

    /**
     * Initialise les couleurs par défaut pour chaque agenda
     */
    private void initializeAgendaColors() {
        agendaColors = new HashMap<>();
        colorPickers = new HashMap<>();

        // Couleurs par défaut (plus foncées et professionnelles)
        agendaColors.put("principal", StandardColors.getAgendaColor("principal"));
        agendaColors.put("technician", StandardColors.getAgendaColor("technician"));
        agendaColors.put("vehicle", StandardColors.getAgendaColor("vehicle"));
        agendaColors.put("maintenance", StandardColors.getAgendaColor("maintenance"));
        agendaColors.put("external", StandardColors.getAgendaColor("external"));
    }

    /**
     * Initialise les couleurs des checkbox après leur création
     */
    private void initializeCheckboxColors() {
        updateAgendaStyle(mainCalendar, agendaColors.get("principal"));
        updateAgendaStyle(technicianCalendar, agendaColors.get("technician"));
        updateAgendaStyle(vehicleCalendar, agendaColors.get("vehicle"));
        updateAgendaStyle(maintenanceCalendar, agendaColors.get("maintenance"));
        updateAgendaStyle(externalCalendar, agendaColors.get("external"));
    }

    /**
     * Crée un sélecteur de couleur pour un agenda spécifique
     */
    private HBox createAgendaWithColorPicker(CheckBox checkbox, String agendaKey) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        container.setPadding(new Insets(6, 8, 6, 8));
        container.setMinHeight(32);
        container.setPrefWidth(Double.MAX_VALUE);

        // checkbox - Style géré par CSS automatiquement - ColorPicker standard avec
        // wrapper
        ColorPicker colorPicker = new ColorPicker(Color.web(agendaColors.get(agendaKey)));
        colorPicker.setPrefSize(28, 22);
        colorPicker.setStyle("-fx-color-label-visible: false; -fx-background-color: "
                + ThemeConstants.BACKGROUND_PRIMARY + " !important;");
        colorPickers.put(agendaKey, colorPicker);

        // Wrapper avec fond parfaitement contrôlé
        HBox colorPickerWrapper = new HBox(colorPicker);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        colorPickerWrapper.setAlignment(Pos.CENTER);

        // Style pour uniformiser l'apparence
        Platform.runLater(() -> {
            colorPicker.applyCss();
            colorPicker.layout();

            // Tous les sélecteurs possibles de JavaFX ColorPicker
            String[] selectors = {
                    ".color-picker", ".arrow-button", ".arrow", ".button", ".region",
                    ".text-field", ".combo-box-base", ".combo-box", ".cell", ".list-cell",
                    ".color-palette", ".color-square", ".hyperlink", ".label",
                    ".custom-color-dialog", ".color-picker-label", ".picker-color"
            };

            for (String selector : selectors) {
                Node node = colorPicker.lookup(selector);
                if (node != null) {
                    // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                }
            }

            // Forcer aussi via getAllNodes() pour capturer tout
            colorPicker.lookupAll("*").forEach(node -> {
                if (node.getStyleClass().toString().contains("color") ||
                        node.getStyleClass().toString().contains("picker") ||
                        node.getStyleClass().toString().contains("button") ||
                        node.getStyleClass().toString().contains("region")) {
                    // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                }
            });

            // Le rectangle de couleur en transparent
            Node colorRect = colorPicker.lookup(".color-rect");
            if (colorRect != null) {
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
            }
        });

        // Action au changement de couleur (color grid standard JavaFX)
        colorPicker.setOnAction(event -> {
            Color newColor = colorPicker.getValue();
            String hexColor = toHexString(newColor);
            agendaColors.put(agendaKey, hexColor);
            updateAgendaStyle(checkbox, hexColor);
            WeekCalendarView.setAgendaColor(agendaKey, hexColor);
            weekCalendarView.refreshEvents();
        });

        Region spacer = new Region();
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(checkbox, spacer, colorPickerWrapper);
        return container;
    }

    /**
     * Met à jour le style d'une checkbox d'agenda avec la nouvelle couleur
     */
    private void updateAgendaStyle(CheckBox checkbox, String color) {
        // Style complet pour forcer la couleur du contour et du texte
        String style = "-fx-text-fill: " + color + "; -fx-background-color: "
                + ThemeConstants.BACKGROUND_PRIMARY + " !important;" +
                "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;";

        checkbox.setStyle(style);

        // Style CSS inline pour forcer la couleur du contour de la checkbox
        checkbox.applyCss();
        checkbox.layout();

        // Appliquer le style de la box avec la couleur spécifique
        Platform.runLater(() -> {
            Node box = checkbox.lookup(".box");
            if (box != null) {
                box.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                        + " !important; " +
                        "-fx-border-color: " + color + " !important; " +
                        "-fx-border-width: 2px !important;");
            }

            // Gérer la marque selon l'état de la checkbox
            updateCheckboxMark(checkbox, color);
        });

        // Ajouter un listener pour gérer l'état coché/décoché
        checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> updateCheckboxMark(checkbox, color));
        });
    }

    /**
     * Met à jour la marque de la checkbox selon son état
     */
    private void updateCheckboxMark(CheckBox checkbox, String color) {
        Node mark = checkbox.lookup(".mark");
        if (mark != null) {
            if (checkbox.isSelected()) {
                // Checkbox cochée : afficher la marque avec la couleur
                mark.setStyle("-fx-background-color: " + color + " !important; -fx-opacity: 1;");
                mark.setVisible(true);
            } else {
                // Checkbox décochée : cacher la marque
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                mark.setVisible(false);
            }
        }
    }

    /**
     * Convertit une couleur JavaFX en string hexadécimale
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Obtient la couleur personnalisée d'un agenda
     */
    public String getAgendaColor(String agendaKey) {
        return agendaColors.getOrDefault(agendaKey, StandardColors.PRIMARY_BLUE);
    }

    /**
     * Bascule la visibilité du volet des agendas avec animation coordonnée de tous
     * les éléments
     */
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;

        if (sidebarExpanded) {
            // Animation d'apparition : tous les éléments glissent ensemble vers la droite
            calendarSidebar.setVisible(true);

            // Largeur de la sidebar du volet (200px)
            double sidebarWidth = 200;

            // Position initiale : tous les éléments sont décalés vers la gauche
            calendarSidebar.setTranslateX(-sidebarWidth); // Sidebar cachée à gauche
            toggleColumn.setTranslateX(-sidebarWidth); // Toggle column aussi décalée
            currentCalendarView.setTranslateX(-sidebarWidth); // Planning aussi décalé

            // Nettoyer et ajouter tous les éléments
            mainContainer.getChildren().clear();
            mainContainer.getChildren().addAll(calendarSidebar, toggleColumn, currentCalendarView);

            // Animation coordonnée : tous les éléments glissent vers la droite ensemble
            TranslateTransition sidebarSlide = new TranslateTransition(Duration.millis(300), calendarSidebar);
            sidebarSlide.setFromX(-sidebarWidth);
            sidebarSlide.setToX(0);

            TranslateTransition toggleSlide = new TranslateTransition(Duration.millis(300), toggleColumn);
            toggleSlide.setFromX(-sidebarWidth);
            toggleSlide.setToX(0);

            TranslateTransition calendarSlide = new TranslateTransition(Duration.millis(300), currentCalendarView);
            calendarSlide.setFromX(-sidebarWidth);
            calendarSlide.setToX(0);

            // Animation parallèle coordonnée
            ParallelTransition expandAnimation = new ParallelTransition(sidebarSlide, toggleSlide, calendarSlide);
            expandAnimation.setInterpolator(Interpolator.EASE_OUT);

            // Changer le bouton à la fin de l'animation
            expandAnimation.setOnFinished(e -> toggleButton.setText("◀"));
            expandAnimation.play();

        } else {
            // Animation de disparition : tous les éléments glissent vers la gauche ensemble
            double sidebarWidth = 200;

            // Animation coordonnée : tous les éléments glissent vers la gauche ensemble
            TranslateTransition sidebarSlide = new TranslateTransition(Duration.millis(300), calendarSidebar);
            sidebarSlide.setFromX(0);
            sidebarSlide.setToX(-sidebarWidth);

            TranslateTransition toggleSlide = new TranslateTransition(Duration.millis(300), toggleColumn);
            toggleSlide.setFromX(0);
            toggleSlide.setToX(-sidebarWidth);

            TranslateTransition calendarSlide = new TranslateTransition(Duration.millis(300), currentCalendarView);
            calendarSlide.setFromX(0);
            calendarSlide.setToX(-sidebarWidth);

            // Animation parallèle coordonnée
            ParallelTransition collapseAnimation = new ParallelTransition(sidebarSlide, toggleSlide, calendarSlide);
            collapseAnimation.setInterpolator(Interpolator.EASE_IN);

            collapseAnimation.setOnFinished(e -> {
                // Nettoyer et réorganiser après l'animation
                mainContainer.getChildren().clear();
                mainContainer.getChildren().addAll(toggleColumn, currentCalendarView);
                calendarSidebar.setVisible(false);

                // Remettre toutes les positions normales pour la prochaine ouverture
                calendarSidebar.setTranslateX(0);
                toggleColumn.setTranslateX(0);
                currentCalendarView.setTranslateX(0);

                toggleButton.setText("▶");
            });

            collapseAnimation.play();
        }
    }
}
