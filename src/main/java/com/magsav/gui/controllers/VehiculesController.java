package com.magsav.gui.controllers;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import com.magsav.model.Vehicule;
import com.magsav.util.AppLogger;
import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.repo.VehiculeRepository;
import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory.*;
import com.magsav.service.Refreshable;


/**
 * Contrôleur spécialisé pour la gestion des véhicules
 * Gère les listes, plannings et détails des véhicules
 */
public class VehiculesController implements Refreshable {
    
    public VehiculesController() {
        // Constructeur par défaut
    }
    
    /**
     * Crée l'onglet principal des véhicules avec sous-onglets
     */
    public Tab createVehiculesTab() {
        TabPane vehiculesTabPane = new TabPane();
        vehiculesTabPane.getStyleClass().add("sub-tab-pane");
        
        // Sous-onglets
        Tab listeTab = createVehiculesListTab();
        Tab planningTab = createVehiculesPlanningTab();
        
        vehiculesTabPane.getTabs().addAll(listeTab, planningTab);
        
        Tab mainTab = new Tab("🚗 Véhicules");
        mainTab.setClosable(false);
        mainTab.setContent(vehiculesTabPane);
        
        return mainTab;
    }
    
    /**
     * Crée l'onglet de liste des véhicules avec volet de détail
     */
    private Tab createVehiculesListTab() {
        Tab tab = new Tab("📋 Liste");
        tab.setClosable(false);
        
        VBox content = createVehiculesListContentWithDetailPanel();
        tab.setContent(content);
        
        return tab;
    }
    
    /**
     * Crée l'onglet de planning des véhicules
     */
    private Tab createVehiculesPlanningTab() {
        Tab tab = new Tab("📅 Planning");
        tab.setClosable(false);
        
        VBox content = createVehiculesPlanningContent();
        tab.setContent(content);
        
        return tab;
    }
    
    /**
     * Crée le contenu de la liste des véhicules
     */
    private VBox createVehiculesListContent() {
        VBox content = new VBox();
        content.setSpacing(0);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        // Métriques des véhicules
        HBox metricsBox = new HBox();
        metricsBox.setSpacing(20);
        metricsBox.getStyleClass().add("metrics-container");
        
        VBox totalBox = createVehiculeMetricBox("Total véhicules", "12", "#4a90e2");
        VBox disponiblesBox = createVehiculeMetricBox("Disponibles", "8", "#51cf66");
        VBox maintenanceBox = createVehiculeMetricBox("En maintenance", "3", "#ffd43b");
        VBox reparationBox = createVehiculeMetricBox("En réparation", "1", "#ff6b6b");
        
        metricsBox.getChildren().addAll(totalBox, disponiblesBox, maintenanceBox, reparationBox);
        
        // Filtres et recherche
        HBox filtersBox = new HBox();
        filtersBox.setSpacing(15);
        filtersBox.getStyleClass().add("filters-box");
        
        Label filterLabel = new Label("Filtres:");
        filterLabel.getStyleClass().add("filter-label");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Disponible", "En maintenance", "En réparation", "Hors service");
        statusFilter.setValue("Tous");
        statusFilter.getStyleClass().add("filter-combo");
        
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "Utilitaire", "Camion", "Fourgon", "Voiture");
        typeFilter.setValue("Tous types");
        typeFilter.getStyleClass().add("filter-combo");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par immatriculation...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        
        Button searchBtn = new Button("🔍");
        searchBtn.getStyleClass().addAll("button", "button-icon");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newVehiculeBtn = new Button("Nouveau véhicule");
        newVehiculeBtn.getStyleClass().addAll("button", "button-primary");
        newVehiculeBtn.setOnAction(e -> openNewVehiculeDialog());
        
        filtersBox.getChildren().addAll(filterLabel, statusFilter, typeFilter, searchField, searchBtn, spacer, newVehiculeBtn);
        
        // Table des véhicules
        TableView<Vehicule> vehiculesTable = new TableView<>();
        vehiculesTable.getStyleClass().add("table-view");
        
        // Colonnes
        TableColumn<Vehicule, String> immatriculationCol = new TableColumn<>("Immatriculation");
        immatriculationCol.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        immatriculationCol.setPrefWidth(120);
        
        TableColumn<Vehicule, String> marqueCol = new TableColumn<>("Marque");
        marqueCol.setCellValueFactory(new PropertyValueFactory<>("marque"));
        marqueCol.setPrefWidth(100);
        
        TableColumn<Vehicule, String> modeleCol = new TableColumn<>("Modèle");
        modeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        modeleCol.setPrefWidth(120);
        
        TableColumn<Vehicule, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
        typeCol.setPrefWidth(100);
        
        TableColumn<Vehicule, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setPrefWidth(120);
        
        TableColumn<Vehicule, Integer> kmCol = new TableColumn<>("Kilométrage");
        kmCol.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        kmCol.setPrefWidth(100);
        
        TableColumn<Vehicule, Integer> anneeCol = new TableColumn<>("Année");
        anneeCol.setCellValueFactory(new PropertyValueFactory<>("annee"));
        anneeCol.setPrefWidth(80);
        
        vehiculesTable.getColumns().addAll(java.util.Arrays.asList(
            immatriculationCol, marqueCol, modeleCol, typeCol, 
            statutCol, kmCol, anneeCol
        ));
        
        // Charger les données
        loadVehiculesData(vehiculesTable);
        
        VBox.setVgrow(vehiculesTable, Priority.ALWAYS);
        content.getChildren().addAll(metricsBox, filtersBox, vehiculesTable);
        
        return content;
    }
    
    /**
     * Crée le contenu du planning des véhicules
     */
    private VBox createVehiculesPlanningContent() {
        VBox content = new VBox();
        content.setSpacing(16);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        // En-tête avec filtres de dates
        HBox headerBox = new HBox();
        headerBox.setSpacing(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("header-box");
        
        Label title = new Label("Planning des véhicules");
        title.getStyleClass().add("content-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Date début");
        startDate.getStyleClass().add("date-picker");
        
        Label toLabel = new Label("à");
        toLabel.getStyleClass().add("text-label");
        
        DatePicker endDate = new DatePicker();
        endDate.setPromptText("Date fin");
        endDate.getStyleClass().add("date-picker");
        
        Button refreshBtn = new Button("Actualiser");
        refreshBtn.getStyleClass().addAll("button", "button-secondary");
        refreshBtn.setOnAction(e -> refreshPlanning());
        
        headerBox.getChildren().addAll(title, spacer, startDate, toLabel, endDate, refreshBtn);
        
        // Vue calendrier/planning (placeholder)
        VBox planningView = new VBox();
        planningView.getStyleClass().add("planning-view");
        planningView.setAlignment(Pos.CENTER);
        planningView.setSpacing(10);
        
        Label planningTitle = new Label("📅 Vue Planning");
        planningTitle.getStyleClass().add("planning-title");
        
        Label planningPlaceholder = new Label("Interface de planning des véhicules à implémenter");
        planningPlaceholder.getStyleClass().add("placeholder-text");
        
        // Grille temporaire pour simuler un planning
        GridPane planningGrid = new GridPane();
        planningGrid.getStyleClass().add("planning-grid");
        planningGrid.setHgap(10);
        planningGrid.setVgap(10);
        
        // En-têtes des jours
        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("planning-day-header");
            planningGrid.add(dayLabel, i, 0);
        }
        
        // Quelques créneaux exemple
        for (int row = 1; row <= 3; row++) {
            for (int col = 0; col < 7; col++) {
                VBox slotBox = new VBox();
                slotBox.getStyleClass().add("planning-slot");
                slotBox.setAlignment(Pos.CENTER);
                slotBox.setPrefSize(80, 60);
                
                if (Math.random() > 0.7) { // Quelques créneaux occupés
                    Label vehicleLabel = new Label("AB-123-CD");
                    vehicleLabel.getStyleClass().add("planning-vehicle");
                    slotBox.getChildren().add(vehicleLabel);
                    slotBox.getStyleClass().add("planning-slot-occupied");
                }
                
                planningGrid.add(slotBox, col, row);
            }
        }
        
        planningView.getChildren().addAll(planningTitle, planningPlaceholder, planningGrid);
        
        VBox.setVgrow(planningView, Priority.ALWAYS);
        content.getChildren().addAll(headerBox, planningView);
        
        return content;
    }
    
    /**
     * Crée une boîte de métrique pour les véhicules
     */
    private VBox createVehiculeMetricBox(String label, String value, String color) {
        VBox box = new VBox();
        box.setSpacing(4);
        box.getStyleClass().add("metric-box");
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        Label labelText = new Label(label);
        labelText.getStyleClass().add("metric-label");
        
        box.getChildren().addAll(valueLabel, labelText);
        
        return box;
    }
    
    /**
     * Charge les données des véhicules dans la table
     */
    private void loadVehiculesData(TableView<Vehicule> table) {
        try {
            // Créer des véhicules de test
            java.util.List<Vehicule> vehicules = new java.util.ArrayList<>();
            
            Vehicule v1 = new Vehicule("AB-123-CD", Vehicule.TypeVehicule.VL);
            v1.setMarque("Renault");
            v1.setModele("Master");
            v1.setAnnee(2020);
            v1.setKilometrage(145000);
            v1.setStatut(Vehicule.StatutVehicule.DISPONIBLE);
            vehicules.add(v1);
            
            Vehicule v2 = new Vehicule("EF-456-GH", Vehicule.TypeVehicule.PL);
            v2.setMarque("Ford");
            v2.setModele("Transit");
            v2.setAnnee(2019);
            v2.setKilometrage(89000);
            v2.setStatut(Vehicule.StatutVehicule.MAINTENANCE);
            vehicules.add(v2);
            
            Vehicule v3 = new Vehicule("IJ-789-KL", Vehicule.TypeVehicule.VL);
            v3.setMarque("Mercedes");
            v3.setModele("Sprinter");
            v3.setAnnee(2018);
            v3.setKilometrage(201000);
            v3.setStatut(Vehicule.StatutVehicule.DISPONIBLE);
            vehicules.add(v3);
            
            table.setItems(FXCollections.observableArrayList(vehicules));
            AppLogger.info("Données des véhicules chargées: " + vehicules.size() + " véhicules");
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des véhicules", e);
        }
    }
    
    /**
     * Ouvre le dialogue de création d'un nouveau véhicule
     */
    private void openNewVehiculeDialog() {
        showAlert("Info", "Création de nouveau véhicule à implémenter");
    }
    
    /**
     * Actualise la vue planning
     */
    private void refreshPlanning() {
        showAlert("Info", "Actualisation du planning à implémenter");
    }
    
    /**
     * Crée l'onglet véhicules avec volet de détail (nouveau pattern)
     */
    private VBox createVehiculesListContentWithDetailPanel() {
        VBox content = TabBuilderUtils.createTabContent();
        
        // Métriques des véhicules
        HBox metricsBox = createVehiculesMetricsBox();
        content.getChildren().add(metricsBox);
        
        // Filtres et recherche
        HBox filtersBox = createVehiculesFiltersBox();
        content.getChildren().add(filtersBox);
        
        // Créer le tableau des véhicules
        TableView<Vehicule> vehiculesTable = createVehiculesTable();
        
        // Panel de visualisation unifié
        DetailPane detailPane = DetailLayoutHelper.createVehiculeVisualizationPane(() -> {
            System.out.println("Ouverture des détails véhicule");
        });
        
        // SplitPane unifié
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(vehiculesTable, detailPane);
        splitPane.setDividerPositions(0.7);
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configurer les événements de sélection
        setupVehiculeTableSelection(vehiculesTable, detailPane);
        
        // Charger les données
        loadVehiculesData(vehiculesTable);
        
        return content;
    }
    
    /**
     * Crée la boîte de métriques des véhicules
     */
    private HBox createVehiculesMetricsBox() {
        HBox metricsBox = new HBox();
        metricsBox.setSpacing(20);
        metricsBox.getStyleClass().add("metrics-container");
        
        VBox totalBox = createVehiculeMetricBox("Total véhicules", "12", "#4a90e2");
        VBox disponiblesBox = createVehiculeMetricBox("Disponibles", "8", "#51cf66");
        VBox maintenanceBox = createVehiculeMetricBox("En maintenance", "3", "#ffd43b");
        VBox reparationBox = createVehiculeMetricBox("En réparation", "1", "#ff6b6b");
        
        metricsBox.getChildren().addAll(totalBox, disponiblesBox, maintenanceBox, reparationBox);
        return metricsBox;
    }
    
    /**
     * Crée la boîte de filtres des véhicules
     */
    private HBox createVehiculesFiltersBox() {
        HBox filtersBox = new HBox();
        filtersBox.setSpacing(15);
        filtersBox.getStyleClass().add("filters-box");
        
        Label filterLabel = new Label("Filtres:");
        filterLabel.getStyleClass().add("filter-label");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Disponible", "En maintenance", "En réparation", "Hors service");
        statusFilter.setValue("Tous");
        statusFilter.getStyleClass().add("filter-combo");
        
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "Utilitaire", "Camion", "Fourgon", "Voiture");
        typeFilter.setValue("Tous types");
        typeFilter.getStyleClass().add("filter-combo");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par immatriculation...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        
        Button searchBtn = new Button("🔍");
        searchBtn.getStyleClass().addAll("button", "button-icon");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newVehiculeBtn = new Button("Nouveau véhicule");
        newVehiculeBtn.getStyleClass().addAll("button", "button-primary");
        newVehiculeBtn.setOnAction(e -> openNewVehiculeDialog());
        
        filtersBox.getChildren().addAll(filterLabel, statusFilter, typeFilter, searchField, searchBtn, spacer, newVehiculeBtn);
        return filtersBox;
    }
    
    /**
     * Crée le tableau des véhicules configuré
     */
    private TableView<Vehicule> createVehiculesTable() {
        TableView<Vehicule> vehiculesTable = new TableView<>();
        TabBuilderUtils.configureBasicTable(vehiculesTable);
        
        // Colonnes
        TableColumn<Vehicule, String> immatriculationCol = new TableColumn<>("Immatriculation");
        immatriculationCol.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        immatriculationCol.setPrefWidth(120);
        
        TableColumn<Vehicule, String> marqueCol = new TableColumn<>("Marque");
        marqueCol.setCellValueFactory(new PropertyValueFactory<>("marque"));
        marqueCol.setPrefWidth(100);
        
        TableColumn<Vehicule, String> modeleCol = new TableColumn<>("Modèle");
        modeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        modeleCol.setPrefWidth(120);
        
        TableColumn<Vehicule, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
        typeCol.setPrefWidth(100);
        
        TableColumn<Vehicule, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setPrefWidth(120);
        
        TableColumn<Vehicule, Integer> kmCol = new TableColumn<>("Kilométrage");
        kmCol.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        kmCol.setPrefWidth(100);
        
        TableColumn<Vehicule, Integer> anneeCol = new TableColumn<>("Année");
        anneeCol.setCellValueFactory(new PropertyValueFactory<>("annee"));
        anneeCol.setPrefWidth(80);
        
        vehiculesTable.getColumns().clear();
        vehiculesTable.getColumns().add(immatriculationCol);
        vehiculesTable.getColumns().add(marqueCol);
        vehiculesTable.getColumns().add(modeleCol);
        vehiculesTable.getColumns().add(typeCol);
        vehiculesTable.getColumns().add(statutCol);
        vehiculesTable.getColumns().add(kmCol);
        vehiculesTable.getColumns().add(anneeCol);
        
        return vehiculesTable;
    }
    
    /**
     * Crée le volet de détail pour un véhicule
     */
    private VBox createVehiculeDetailPanel() {
        VBox detailPanel = new VBox(10);
        detailPanel.setPadding(new Insets(10));
        detailPanel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");
        detailPanel.setPrefWidth(350);
        
        // Titre du volet
        Label titleLabel = new Label("🚗 Détails du véhicule");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        // Conteneur pour les détails
        VBox detailsContainer = new VBox(8);
        detailsContainer.setPadding(new Insets(10));
        detailsContainer.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Labels d'information
        Label immatriculationLabel = new Label("Aucun véhicule sélectionné");
        immatriculationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        immatriculationLabel.setId("detail-immatriculation");
        
        Label marqueModeleLabel = new Label("");
        marqueModeleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        marqueModeleLabel.setWrapText(true);
        marqueModeleLabel.setId("detail-marque-modele");
        
        Label typeLabel = new Label("");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");
        typeLabel.setId("detail-type");
        
        Label statutLabel = new Label("");
        statutLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12; -fx-text-fill: white;");
        statutLabel.setId("detail-statut");
        
        Label anneeKmLabel = new Label("");
        anneeKmLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        anneeKmLabel.setId("detail-annee-km");
        
        Label infoTechLabel = new Label("");
        infoTechLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
        infoTechLabel.setWrapText(true);
        infoTechLabel.setMaxWidth(320);
        infoTechLabel.setId("detail-info-tech");
        
        detailsContainer.getChildren().addAll(
            immatriculationLabel, marqueModeleLabel, typeLabel, statutLabel, 
            anneeKmLabel, infoTechLabel
        );
        
        // Boutons d'actions
        VBox actionsBox = new VBox(5);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label actionsTitle = new Label("Actions rapides");
        actionsTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");
        
        Button modifierBtn = new Button("✏️ Modifier");
        modifierBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-cursor: hand;");
        modifierBtn.setPrefWidth(150);
        modifierBtn.setOnAction(e -> modifierVehicule());
        modifierBtn.setDisable(true);
        modifierBtn.setId("btn-modifier-vehicule");
        
        Button maintenanceBtn = new Button("🔧 Maintenance");
        maintenanceBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand;");
        maintenanceBtn.setPrefWidth(150);
        maintenanceBtn.setOnAction(e -> planifierMaintenance());
        maintenanceBtn.setDisable(true);
        maintenanceBtn.setId("btn-maintenance-vehicule");
        
        Button supprimerBtn = new Button("🗑️ Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
        supprimerBtn.setPrefWidth(150);
        supprimerBtn.setOnAction(e -> supprimerVehicule());
        supprimerBtn.setDisable(true);
        supprimerBtn.setId("btn-supprimer-vehicule");
        
        actionsBox.getChildren().addAll(actionsTitle, modifierBtn, maintenanceBtn, supprimerBtn);
        
        detailPanel.getChildren().addAll(titleLabel, detailsContainer, actionsBox);
        
        return detailPanel;
    }
    
    /**
     * Configure les événements de sélection du tableau
     */
    private void setupVehiculeTableEvents(TableView<Vehicule> table, VBox detailPanel) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateVehiculeDetailPanel(newSelection, detailPanel);
                detailPanel.setVisible(true);
                
                // Activer les boutons
                Button modifierBtn = (Button) detailPanel.lookup("#btn-modifier-vehicule");
                Button maintenanceBtn = (Button) detailPanel.lookup("#btn-maintenance-vehicule");
                Button supprimerBtn = (Button) detailPanel.lookup("#btn-supprimer-vehicule");
                
                if (modifierBtn != null) modifierBtn.setDisable(false);
                if (maintenanceBtn != null) maintenanceBtn.setDisable(false);
                if (supprimerBtn != null) supprimerBtn.setDisable(false);
            } else {
                detailPanel.setVisible(false);
            }
        });
        
        // Double-clic pour modifier
        table.setRowFactory(tv -> {
            TableRow<Vehicule> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierVehicule();
                }
            });
            return row;
        });
    }

    private void setupVehiculeTableSelection(TableView<Vehicule> table, DetailPane detailPane) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromVehicule(newSelection);
                detailPane.updateInfo(entityInfo);
                detailPane.setVisible(true);
            } else {
                detailPane.setVisible(false);
            }
        });
        
        // Double-clic pour modifier
        table.setRowFactory(tv -> {
            TableRow<Vehicule> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierVehicule();
                }
            });
            return row;
        });
    }
    
    /**
     * Met à jour le volet de détail avec les informations du véhicule
     */
    private void updateVehiculeDetailPanel(Vehicule vehicule, VBox detailPanel) {
        if (vehicule == null) return;
        
        Label immatriculationLabel = (Label) detailPanel.lookup("#detail-immatriculation");
        Label marqueModeleLabel = (Label) detailPanel.lookup("#detail-marque-modele");
        Label typeLabel = (Label) detailPanel.lookup("#detail-type");
        Label statutLabel = (Label) detailPanel.lookup("#detail-statut");
        Label anneeKmLabel = (Label) detailPanel.lookup("#detail-annee-km");
        Label infoTechLabel = (Label) detailPanel.lookup("#detail-info-tech");
        
        if (immatriculationLabel != null) {
            immatriculationLabel.setText("Immatriculation: " + (vehicule.getImmatriculation() != null ? vehicule.getImmatriculation() : "N/A"));
        }
        
        if (marqueModeleLabel != null) {
            String marqueModele = "";
            if (vehicule.getMarque() != null) marqueModele += vehicule.getMarque();
            if (vehicule.getModele() != null) {
                if (!marqueModele.isEmpty()) marqueModele += " ";
                marqueModele += vehicule.getModele();
            }
            marqueModeleLabel.setText(marqueModele.isEmpty() ? "Marque/Modèle non défini" : marqueModele);
        }
        
        if (typeLabel != null) {
            typeLabel.setText("Type: " + (vehicule.getTypeVehicule() != null ? vehicule.getTypeVehicule() : "N/A"));
        }
        
        if (statutLabel != null && vehicule.getStatut() != null) {
            statutLabel.setText(vehicule.getStatut().getDisplayName());
            // Couleur selon le statut
            String couleur = switch (vehicule.getStatut()) {
                case DISPONIBLE -> "#28a745";
                case MAINTENANCE -> "#ffc107";
                case HORS_SERVICE -> "#dc3545";
                case EN_SERVICE -> "#007bff";
                default -> "#6c757d";
            };
            statutLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12; -fx-text-fill: white; -fx-background-color: " + couleur + ";");
        }
        
        if (anneeKmLabel != null) {
            String anneeKm = "";
            if (vehicule.getAnnee() > 0) anneeKm += "Année: " + vehicule.getAnnee();
            if (vehicule.getKilometrage() > 0) {
                if (!anneeKm.isEmpty()) anneeKm += " • ";
                anneeKm += "Km: " + String.format("%,d", vehicule.getKilometrage());
            }
            anneeKmLabel.setText(anneeKm);
        }
        
        if (infoTechLabel != null) {
            String infoTech = "";
            if (vehicule.getTypeVehicule() != null) {
                infoTech += "Type: " + vehicule.getTypeVehicule().getDisplayName();
            }
            if (vehicule.isLocationExterne()) {
                if (!infoTech.isEmpty()) infoTech += "\n";
                infoTech += "Location externe";
            }
            infoTechLabel.setText(infoTech.isEmpty() ? "Aucune information supplémentaire" : infoTech);
        }
    }
    
    /**
     * Actions des boutons
     */
    private void modifierVehicule() {
        showAlert("Info", "Modification de véhicule à implémenter");
    }
    
    private void planifierMaintenance() {
        showAlert("Info", "Planification de maintenance à implémenter");
    }
    
    private void supprimerVehicule() {
        showAlert("Confirmation", "Suppression de véhicule à implémenter");
    }
    
    /**
     * Affiche une alerte simple
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void refreshAllTables() {
        // Rechargement des données véhicules - à implémenter selon les besoins
        System.out.println("Refresh des tables véhicules");
    }

    @Override
    public String getComponentName() {
        return "VehiculesController";
    }
}