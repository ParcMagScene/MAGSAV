package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue de gestion du planning pour MAGSAV 3.0
 */
public class PlanningManagerView extends VBox {
    
    private final ApiService apiService;

    public PlanningManagerView(ApiService apiService) {
        this.apiService = apiService;
        initializeView();
    }

    private void initializeView() {
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("planning-manager-view");

        // Contenu principal
        CustomTabPane mainContent = createMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Pas de titre - déjà dans le header principal de l'application
        getChildren().add(mainContent);
    }

    private CustomTabPane createMainContent() {
        CustomTabPane tabPane = new CustomTabPane();

        // Onglet Événements
        CustomTabPane.CustomTab eventsTab = new CustomTabPane.CustomTab(
                "Événements",
                createEventsView(),
                "🗓️");
        tabPane.addTab(eventsTab);

        // Onglet Ressources → Disponibilités Véhicules
        CustomTabPane.CustomTab vehiclesTab = new CustomTabPane.CustomTab(
                "Disponibilités Véhicules",
                new VehicleAvailabilityView(apiService),
                "🚐");
        tabPane.addTab(vehiclesTab);

        // Onglet Personnel
        CustomTabPane.CustomTab personnelTab = new CustomTabPane.CustomTab(
                "Personnel",
                createPersonnelView(),
                "👥");
        tabPane.addTab(personnelTab);

        return tabPane;
    }

    private ComboBox<String> viewModeCombo;
    private StackPane calendarContainer;
    private Label periodLabel;
    
    private VBox createEventsView() {
        VBox eventsView = new VBox(10);
        eventsView.setPadding(new Insets(15));

        // Toolbar avec navigation et ComboBox de sélection de vue
        HBox toolbar = createCalendarToolbar();
        
        // Container pour les différentes vues calendaires
        calendarContainer = new StackPane();
        VBox.setVgrow(calendarContainer, Priority.ALWAYS);
        
        // Afficher la vue Semaine par défaut
        updateCalendarView("Semaine");
        
        eventsView.getChildren().addAll(toolbar, calendarContainer);
        
        return eventsView;
    }
    
    private HBox createCalendarToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("unified-toolbar");
        
        // Boutons de navigation
        Button prevButton = new Button("◀");
        prevButton.setOnAction(e -> System.out.println("Navigation précédent"));
        
        periodLabel = new Label("Semaine du 25 Nov 2025");
        periodLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Button nextButton = new Button("▶");
        nextButton.setOnAction(e -> System.out.println("Navigation suivant"));
        
        Button todayButton = new Button("Aujourd'hui");
        todayButton.setOnAction(e -> System.out.println("Aller à aujourd'hui"));
        
        // ComboBox pour choisir la vue
        viewModeCombo = new ComboBox<>();
        viewModeCombo.getItems().addAll("Jour", "Semaine", "Mois", "Année");
        viewModeCombo.setValue("Semaine");
        viewModeCombo.setOnAction(e -> updateCalendarView(viewModeCombo.getValue()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Boutons d'action
        Button newEventBtn = new Button("➕ Nouvel Événement");
        newEventBtn.setOnAction(e -> System.out.println("Créer événement"));
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setOnAction(e -> System.out.println("Actualiser calendrier"));
        
        toolbar.getChildren().addAll(
            prevButton, periodLabel, nextButton, todayButton, viewModeCombo,
            spacer, 
            newEventBtn, refreshBtn
        );
        
        return toolbar;
    }
    
    private void updateCalendarView(String viewMode) {
        VBox view = null;
        String period = "";
        
        switch (viewMode) {
            case "Jour":
                view = createDayCalendarPlaceholder();
                period = "26 Novembre 2025";
                break;
            case "Semaine":
                view = createWeekCalendarPlaceholder();
                period = "Semaine du 25 Nov 2025";
                break;
            case "Mois":
                view = createMonthCalendarPlaceholder();
                period = "Novembre 2025";
                break;
            case "Année":
                view = createYearCalendarPlaceholder();
                period = "Année 2025";
                break;
        }
        
        if (view != null && calendarContainer != null) {
            calendarContainer.getChildren().clear();
            calendarContainer.getChildren().add(view);
        }
        
        if (periodLabel != null) {
            periodLabel.setText(period);
        }
    }
    
    private VBox createDayCalendarPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setPadding(new Insets(30));
        placeholder.setAlignment(Pos.CENTER);
        
        Label title = new Label("📅 Vue Jour");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label info = new Label("Calendrier journalier avec créneaux horaires");
        info.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        placeholder.getChildren().addAll(title, info);
        return placeholder;
    }
    
    private VBox createWeekCalendarPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setPadding(new Insets(30));
        placeholder.setAlignment(Pos.CENTER);
        
        Label title = new Label("📆 Vue Semaine");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label info = new Label("Calendrier hebdomadaire avec 7 jours");
        info.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        placeholder.getChildren().addAll(title, info);
        return placeholder;
    }
    
    private VBox createMonthCalendarPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setPadding(new Insets(30));
        placeholder.setAlignment(Pos.CENTER);
        
        Label title = new Label("🗓️ Vue Mois");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label info = new Label("Calendrier mensuel avec grille complète");
        info.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        placeholder.getChildren().addAll(title, info);
        return placeholder;
    }
    
    private VBox createYearCalendarPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setPadding(new Insets(30));
        placeholder.setAlignment(Pos.CENTER);
        
        Label title = new Label("📊 Vue Année");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label info = new Label("Vue annuelle avec 12 mois");
        info.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        placeholder.getChildren().addAll(title, info);
        return placeholder;
    }

    private VBox createPersonnelView() {
        VBox personnelView = new VBox(10);
        personnelView.setPadding(new Insets(15));

        Label placeholder = new Label("👥 Planning du personnel");
        placeholder.setFont(Font.font("System", FontWeight.NORMAL, 14));

        personnelView.getChildren().add(placeholder);
        return personnelView;
    }
}