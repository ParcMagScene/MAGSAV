package com.magscene.magsav.desktop.view.planning;

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
        
        // Titre
        Label titleLabel = new Label("📅 Gestion du Planning");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.getStyleClass().add("module-title");
        
        // Barre d'outils
        HBox toolbar = createToolbar();
        
        // Contenu principal
        TabPane mainContent = createMainContent();
        
        getChildren().addAll(titleLabel, toolbar, mainContent);
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
    
    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        
        // Onglet Événements
        Tab eventsTab = new Tab("🗓️ Événements");
        eventsTab.setContent(createEventsView());
        eventsTab.setClosable(false);
        
        // Onglet Ressources
        Tab resourcesTab = new Tab("🚐 Ressources");
        resourcesTab.setContent(createResourcesView());
        resourcesTab.setClosable(false);
        
        // Onglet Personnel
        Tab personnelTab = new Tab("👥 Personnel");
        personnelTab.setContent(createPersonnelView());
        personnelTab.setClosable(false);
        
        tabPane.getTabs().addAll(eventsTab, resourcesTab, personnelTab);
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