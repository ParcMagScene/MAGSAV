package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue de gestion du planning pour MAGSAV 3.0
 */
public class PlanningManagerView extends VBox {

    private final ApiService apiService;
    private HBox adaptiveToolbar;
    private CustomTabPane tabPane;

    public PlanningManagerView(ApiService apiService) {
        this.apiService = apiService;
        initializeView();
    }

    private void initializeView() {
        setPadding(new Insets(7));
        setSpacing(0);
        getStyleClass().add("planning-manager-view");

        // Toolbar adaptative en haut
        adaptiveToolbar = new HBox();
        adaptiveToolbar.setAlignment(Pos.CENTER_LEFT);
        adaptiveToolbar.setPadding(new Insets(10));
        adaptiveToolbar.getStyleClass().add("unified-toolbar");

        // Onglets en dessous
        tabPane = createMainContent();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Écouter les changements d'onglet
        tabPane.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            updateToolbarForSelectedTab(newTab);
        });

        // Initialiser la toolbar avec le premier onglet
        updateToolbarForSelectedTab(tabPane.getSelectedTab());

        // Assemblage : Toolbar adaptative puis TabPane
        getChildren().addAll(adaptiveToolbar, tabPane);
    }

    private void updateToolbarForSelectedTab(CustomTabPane.CustomTab selectedTab) {
        if (selectedTab == null)
            return;

        adaptiveToolbar.getChildren().clear();

        String tabText = selectedTab.getText();
        if (tabText.contains("Événements")) {
            // PlanningView a déjà sa propre toolbar intégrée, on masque celle du manager
            adaptiveToolbar.setVisible(false);
            adaptiveToolbar.setManaged(false);
        } else if (tabText.contains("Véhicules")) {
            // Réafficher la toolbar pour les autres onglets
            adaptiveToolbar.setVisible(true);
            adaptiveToolbar.setManaged(true);
            // Toolbar simple pour Véhicules
            HBox toolbar = createSimpleToolbar("Actualiser");
            adaptiveToolbar.getChildren().add(toolbar);
            HBox.setHgrow(toolbar, Priority.ALWAYS);
        } else if (tabText.contains("Personnel")) {
            // Réafficher la toolbar pour les autres onglets
            adaptiveToolbar.setVisible(true);
            adaptiveToolbar.setManaged(true);
            // Toolbar simple pour Personnel
            HBox toolbar = createSimpleToolbar("Actualiser");
            adaptiveToolbar.getChildren().add(toolbar);
            HBox.setHgrow(toolbar, Priority.ALWAYS);
        }
    }

    private HBox createSimpleToolbar(String refreshLabel) {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 " + refreshLabel);
        refreshBtn.getStyleClass().add("action-button-secondary");
        refreshBtn.setOnAction(e -> System.out.println(refreshLabel));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshBtn, spacer);
        return toolbar;
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

    private PlanningView planningView;

    private VBox createEventsView() {
        // Utiliser la vraie PlanningView au lieu d'un placeholder
        planningView = new PlanningView(apiService);

        VBox eventsView = new VBox(0);
        eventsView.setPadding(new Insets(0));
        VBox.setVgrow(planningView, Priority.ALWAYS);

        eventsView.getChildren().add(planningView);

        return eventsView;
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