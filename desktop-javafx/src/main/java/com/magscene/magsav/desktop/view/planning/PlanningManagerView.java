package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.component.CustomTabPane;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue de gestion du planning pour MAGSAV 3.0
 */
public class PlanningManagerView extends VBox {

    public PlanningManagerView() {
        initializeView();
    }

    private void initializeView() {
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("planning-manager-view");

        // Barre d'outils
        HBox toolbar = createToolbar();

        // Contenu principal
        CustomTabPane mainContent = createMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Pas de titre - déjà dans le header principal de l'application
        getChildren().addAll(toolbar, mainContent);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("module-toolbar");

        Button newEventBtn = new Button("➕ Nouvel événement");
        Button viewCalendarBtn = new Button("📅 Calendrier");
        Button reportsBtn = new Button("📊 Rapports");

        newEventBtn.setOnAction(e -> createNewEvent());
        viewCalendarBtn.setOnAction(e -> showCalendarView());
        reportsBtn.setOnAction(e -> showReports());

        toolbar.getChildren().addAll(newEventBtn, viewCalendarBtn, reportsBtn);
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

        // Onglet Ressources
        CustomTabPane.CustomTab resourcesTab = new CustomTabPane.CustomTab(
                "Ressources",
                createResourcesView(),
                "🚐");
        tabPane.addTab(resourcesTab);

        // Onglet Personnel
        CustomTabPane.CustomTab personnelTab = new CustomTabPane.CustomTab(
                "Personnel",
                createPersonnelView(),
                "👥");
        tabPane.addTab(personnelTab);

        return tabPane;
    }

    private VBox createEventsView() {
        VBox eventsView = new VBox(10);
        eventsView.setPadding(new Insets(15));

        Label placeholder = new Label("📋 Liste des événements planifiés");
        placeholder.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Table des événements (placeholder)
        TableView<String> eventsTable = new TableView<>();
        TableColumn<String, String> dateCol = new TableColumn<>("Date");
        TableColumn<String, String> titleCol = new TableColumn<>("Titre");
        TableColumn<String, String> statusCol = new TableColumn<>("Statut");

        eventsTable.getColumns().addAll(dateCol, titleCol, statusCol);
        eventsTable.setPlaceholder(new Label("Aucun événement planifié"));

        eventsView.getChildren().addAll(placeholder, eventsTable);
        return eventsView;
    }

    private VBox createResourcesView() {
        VBox resourcesView = new VBox(10);
        resourcesView.setPadding(new Insets(15));

        Label placeholder = new Label("🚐 Gestion des ressources (véhicules, équipements)");
        placeholder.setFont(Font.font("System", FontWeight.NORMAL, 14));

        resourcesView.getChildren().add(placeholder);
        return resourcesView;
    }

    private VBox createPersonnelView() {
        VBox personnelView = new VBox(10);
        personnelView.setPadding(new Insets(15));

        Label placeholder = new Label("👥 Planning du personnel");
        placeholder.setFont(Font.font("System", FontWeight.NORMAL, 14));

        personnelView.getChildren().add(placeholder);
        return personnelView;
    }

    private void createNewEvent() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nouvel événement");
        alert.setHeaderText("Création d'événement");
        alert.setContentText("Fonctionnalité à implémenter : Création d'un nouvel événement");
        alert.showAndWait();
    }

    private void showCalendarView() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vue calendrier");
        alert.setHeaderText("Calendrier");
        alert.setContentText("Fonctionnalité à implémenter : Vue calendrier interactive");
        alert.showAndWait();
    }

    private void showReports() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapports");
        alert.setHeaderText("Rapports de planning");
        alert.setContentText("Fonctionnalité à implémenter : Génération de rapports");
        alert.showAndWait();
    }
}