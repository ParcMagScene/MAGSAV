package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.VehicleDialog;
import com.magscene.magsav.desktop.theme.ThemeManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Vue de gestion des vehicules
 * Interface principale pour CRUD vehicules avec filtres et recherche
 */
public class VehicleManagerView extends BorderPane {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleManagerView.class);
    
    // Services
    private final ApiService apiService;
    
    // Composants UI
    private TableView<Map<String, Object>> vehicleTable;
    private ObservableList<Map<String, Object>> vehicleData;
    
    // Filtres et recherche
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private CheckBox maintenanceAlertFilter;
    private CheckBox documentsExpiredFilter;
    
    // Références aux boutons pour configuration des listeners  
    private Button editVehicleBtn;
    private Button deleteVehicleBtn;
    private Button statusVehicleBtn;
    private Button mileageVehicleBtn;
    
    // Labels de statistiques
    private Label totalLabel;
    private Label availableLabel;
    private Label maintenanceLabel;
    private Label alertsLabel;
    
    public VehicleManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.vehicleData = FXCollections.observableArrayList();
        
        initializeUI();
        setupEventHandlers();
        loadVehicleData();
        loadStatistics();
    }
    
    private void initializeUI() {
        // BorderPane n'a pas de setSpacing - architecture comme Ventes et Installations
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Header
        VBox header = createHeader();
        
        // Barre de statistiques
        HBox statsBox = createStatisticsBar();
        
        // Barre de recherche et filtres (avec actions intégrées)
        HBox filtersBox = createFiltersBar();
        
        // Table des vehicules
        vehicleTable = createVehicleTable();
        
        // Layout principal - EXACTEMENT comme Ventes et Installations
        VBox topContainer = new VBox(header, statsBox, filtersBox);
        
        setTop(topContainer);
        // Créer le conteneur avec volet de détails
        DetailPanelContainer tableContainer = createVehicleTableWithDetailPanel();
        setCenter(tableContainer);
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);
        
        // Configuration finale après création de tous les composants
        setupButtonActivation();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10); // EXACTEMENT comme Ventes et Installations
        header.setPadding(new Insets(0, 0, 20, 0)); // EXACTEMENT comme Ventes et Installations
        
        Label title = new Label("🚐 Véhicules");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().add(title);
        return header;
    }
    
    private HBox createStatisticsBar() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-background-radius: 5;");
        
        totalLabel = new Label("Total: 0");
        availableLabel = new Label("Disponibles: 0");
        maintenanceLabel = new Label("Maintenance: 0");
        alertsLabel = new Label("Alertes: 0");
        
        // Style des labels
        for (Label label : List.of(totalLabel, availableLabel, maintenanceLabel, alertsLabel)) {
            label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
            label.setTextFill(Color.web("#34495e"));
        }
        
        // Couleurs specifiques
        availableLabel.setTextFill(Color.web("#27ae60"));
        maintenanceLabel.setTextFill(Color.web("#f39c12"));
        alertsLabel.setTextFill(Color.web("#e74c3c"));
        
        statsBox.getChildren().addAll(totalLabel, new Separator(), 
                                     availableLabel, new Separator(),
                                     maintenanceLabel, new Separator(), 
                                     alertsLabel);
        
        return statsBox;
    }
    
    private HBox createFiltersBar() {
        HBox filtersBox = new HBox(10); // EXACTEMENT comme Ventes & Installations
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(0, 10, 10, 10)); // EXACTEMENT comme filterBar Ventes & Installations
        filtersBox.setStyle("-fx-background-color: #142240; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Recherche globale
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Rechercher vehicule, marque, plaque...");
        searchField.setPrefWidth(250);
        // Style supprimé - géré par forceSearchFieldColors
        // Force agressive des couleurs pour contrer le CSS global
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("🚗 Type");
        typeLabel.setStyle("-fx-text-fill: #6B71F2;");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "VAN", "TRUCK", "TRAILER", "CAR", "MOTORCYCLE", "OTHER");
        typeFilter.setValue("Tous types");
        typeFilter.setPrefWidth(120);
        typeFilter.setStyle("-fx-background-color: #142240; -fx-text-fill: #6B71F2;");
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("📊 Statut");
        statusLabel.setStyle("-fx-text-fill: #6B71F2;");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous statuts", "AVAILABLE", "IN_USE", "MAINTENANCE", 
                                      "OUT_OF_ORDER", "RENTED_OUT", "RESERVED");
        statusFilter.setValue("Tous statuts");
        statusFilter.setPrefWidth(140);
        statusFilter.setStyle("-fx-background-color: #142240; -fx-text-fill: #6B71F2;");
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Filtres speciaux
        VBox specialBox = new VBox(5);
        Label specialLabel = new Label("🔧 Filtres");
        specialLabel.setStyle("-fx-text-fill: #6B71F2;");
        specialLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        maintenanceAlertFilter = new CheckBox("Maintenance requise");
        maintenanceAlertFilter.setStyle("-fx-text-fill: #6B71F2;");
        documentsExpiredFilter = new CheckBox("Documents expirés");
        documentsExpiredFilter.setStyle("-fx-text-fill: #6B71F2;");
        specialBox.getChildren().addAll(specialLabel, maintenanceAlertFilter, documentsExpiredFilter);
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button addVehicleBtn = new Button("➕ Ajouter");
        addVehicleBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4;");
        addVehicleBtn.setOnAction(e -> addVehicle());
        
        editVehicleBtn = new Button("✏️ Modifier");
        editVehicleBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4;");
        editVehicleBtn.setOnAction(e -> editVehicle());
        
        deleteVehicleBtn = new Button("🗑️ Supprimer");
        deleteVehicleBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4;");
        deleteVehicleBtn.setOnAction(e -> deleteVehicle());
        
        statusVehicleBtn = new Button("📊 Statut");
        statusVehicleBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 4;");
        statusVehicleBtn.setOnAction(e -> changeVehicleStatus());
        
        mileageVehicleBtn = new Button("🔢 Kilomètres");
        mileageVehicleBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 4;");
        mileageVehicleBtn.setOnAction(e -> updateVehicleMileage());
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4;");
        refreshBtn.setOnAction(e -> loadVehicleData());
        
        // Les boutons seront activés/désactivés après la création de la table
        editVehicleBtn.setDisable(true);
        deleteVehicleBtn.setDisable(true);
        statusVehicleBtn.setDisable(true);
        mileageVehicleBtn.setDisable(true);
        
        buttonRow.getChildren().addAll(addVehicleBtn, editVehicleBtn, deleteVehicleBtn, statusVehicleBtn, mileageVehicleBtn, refreshBtn);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        filtersBox.getChildren().addAll(searchBox, typeBox, statusBox, specialBox, spacer, actionsBox);
        
        return filtersBox;
    }
    
    // Méthode createButtonsBar() supprimée - Les boutons sont maintenant
    // intégrés dans la toolbar unifiée createFiltersBar() pour éviter les doublons
    
    private DetailPanelContainer createVehicleTableWithDetailPanel() {
        vehicleTable = createVehicleTable();
        
        // Créer le conteneur
        DetailPanelContainer container = new DetailPanelContainer(vehicleTable);
        
        // Ajouter notre propre listener pour gérer les Map
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Convertir les données Map en GenericDetailItem
                com.magscene.magsav.desktop.component.GenericDetailItem detailItem = 
                    createVehicleDetailItem(newSelection);
                
                // Mettre à jour le contenu du volet
                container.getDetailPanel().updateContent(
                    detailItem.getDetailTitle(),
                    detailItem.getDetailSubtitle(),
                    detailItem.getDetailImage(),
                    null, // QR Code sera généré automatiquement
                    detailItem.getDetailInfoContent()
                );
                container.getDetailPanel().show();
            } else {
                container.getDetailPanel().hide();
            }
        });
        
        return container;
    }
    
    private com.magscene.magsav.desktop.component.GenericDetailItem createVehicleDetailItem(Map<String, Object> vehicleData) {
        String brand = getStringValue(vehicleData, "brand");
        String model = getStringValue(vehicleData, "model");
        String licensePlate = getStringValue(vehicleData, "licensePlate");
        
        // Titre : Marque + Modèle
        String title = "";
        if (!brand.isEmpty() && !model.isEmpty()) {
            title = brand + " " + model;
        } else if (!brand.isEmpty()) {
            title = brand;
        } else if (!model.isEmpty()) {
            title = model;
        } else {
            title = "Véhicule";
        }
        
        // Sous-titre : Plaque + Type
        String subtitle = "";
        if (!licensePlate.isEmpty()) {
            subtitle = "🚗 " + licensePlate;
        }
        String type = getStringValue(vehicleData, "type");
        if (!type.isEmpty()) {
            if (!subtitle.isEmpty()) subtitle += " • ";
            subtitle += type;
        }
        
        // ID
        String id = String.valueOf(vehicleData.get("id"));
        
        // Propriétés pour les détails
        Map<String, String> properties = new java.util.HashMap<>();
        
        String status = getStringValue(vehicleData, "status");
        if (!status.isEmpty()) {
            properties.put("Statut", convertVehicleStatusToDisplay(status));
        }
        
        String location = getStringValue(vehicleData, "location");
        if (!location.isEmpty()) {
            properties.put("Localisation", location);
        }
        
        String fuelType = getStringValue(vehicleData, "fuelType");
        if (!fuelType.isEmpty()) {
            properties.put("Carburant", fuelType);
        }
        
        Object mileageObj = vehicleData.get("mileage");
        if (mileageObj instanceof Number) {
            double mileage = ((Number) mileageObj).doubleValue();
            if (mileage > 0) {
                properties.put("Kilométrage", String.format("%.0f km", mileage));
            }
        }
        
        Object dailyRateObj = vehicleData.get("dailyRate");
        if (dailyRateObj instanceof Number) {
            double dailyRate = ((Number) dailyRateObj).doubleValue();
            if (dailyRate > 0) {
                properties.put("Tarif journalier", String.format("%.2f €", dailyRate));
            }
        }
        
        String lastMaintenance = getStringValue(vehicleData, "lastMaintenance");
        if (!lastMaintenance.isEmpty()) {
            properties.put("Dernière maintenance", lastMaintenance);
        }
        
        String nextMaintenance = getStringValue(vehicleData, "nextMaintenance");
        if (!nextMaintenance.isEmpty()) {
            properties.put("Prochaine maintenance", nextMaintenance);
        }
        
        String insuranceExpiry = getStringValue(vehicleData, "insuranceExpiry");
        if (!insuranceExpiry.isEmpty()) {
            properties.put("Expiration assurance", insuranceExpiry);
        }
        
        String notes = getStringValue(vehicleData, "notes");
        if (!notes.isEmpty()) {
            properties.put("Notes", notes);
        }
        
        return new com.magscene.magsav.desktop.component.GenericDetailItem(
            title, subtitle, id, properties, "VEHICLE"
        );
    }
    
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String convertVehicleStatusToDisplay(String status) {
        if (status == null || status.isEmpty()) return "Inconnu";
        
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> "Disponible";
            case "IN_USE" -> "En cours d'utilisation";
            case "MAINTENANCE" -> "En maintenance";
            case "OUT_OF_ORDER" -> "Hors service";
            case "RESERVED" -> "Réservé";
            default -> status;
        };
    }
    
    private TableView<Map<String, Object>> createVehicleTable() {
        TableView<Map<String, Object>> table = new TableView<>();
        table.setItems(vehicleData);
        
        // Colonnes de la table
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("name"))));
        nameCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> brandModelCol = new TableColumn<>("Marque/Modele");
        brandModelCol.setCellValueFactory(data -> {
            String brand = String.valueOf(data.getValue().get("brand"));
            String model = String.valueOf(data.getValue().get("model"));
            return new SimpleStringProperty((brand != null ? brand : "") + " " + (model != null ? model : ""));
        });
        brandModelCol.setPrefWidth(180);
        
        TableColumn<Map<String, Object>, String> plateCol = new TableColumn<>("Plaque");
        plateCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("licensePlate"))));
        plateCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("type"))));
        typeCol.setPrefWidth(80);
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("status"))));
        statusCol.setPrefWidth(120);
        
        // Style conditionnel pour le statut
        statusCol.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(getStatusDisplayName(status));
                    setStyle(getStatusStyle(status));
                }
            }
        });
        
        TableColumn<Map<String, Object>, String> mileageCol = new TableColumn<>("Kilometrage");
        mileageCol.setCellValueFactory(data -> {
            Object mileage = data.getValue().get("mileage");
            return new SimpleStringProperty(mileage != null ? 
                String.format("%,d km", ((Number) mileage).intValue()) : "N/A");
        });
        mileageCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> driverCol = new TableColumn<>("Conducteur");
        driverCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("assignedDriver"))));
        driverCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> locationCol = new TableColumn<>("Localisation");
        locationCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("currentLocation"))));
        locationCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> alertsCol = new TableColumn<>("Alertes");
        alertsCol.setCellValueFactory(data -> {
            return new SimpleStringProperty(getVehicleAlerts(data.getValue()));
        });
        alertsCol.setPrefWidth(100);
        
        // Couleur des alertes
        alertsCol.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String alerts, boolean empty) {
                super.updateItem(alerts, empty);
                
                if (empty || alerts == null || alerts.trim().isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(alerts);
                    if (alerts.contains("URGENT")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (alerts.contains("WARNING")) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
        
        table.getColumns().add(nameCol);
        table.getColumns().add(brandModelCol);
        table.getColumns().add(plateCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(mileageCol);
        table.getColumns().add(driverCol);
        table.getColumns().add(locationCol);
        table.getColumns().add(alertsCol);
        
        // Selection simple
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        return table;
    }
    
    private void setupEventHandlers() {
        // Double-clic pour modifier (conservé)
        vehicleTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection prioritaire (#142240)
                    row.setStyle("-fx-background-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editVehicle();
                }
            });
            return row;
        });
        
        // Filtres temps reel
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        maintenanceAlertFilter.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        documentsExpiredFilter.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Note: Les boutons d'action sont maintenant gérés directement dans createFiltersBar()
        // Mais nous devons configurer l'activation/désactivation après création de la table
        setupButtonActivation();
    }
    
    /**
     * Configure l'activation/désactivation des boutons selon la sélection
     */
    private void setupButtonActivation() {
        // Configuration du listener de sélection après création de tous les composants
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            editVehicleBtn.setDisable(!hasSelection);
            deleteVehicleBtn.setDisable(!hasSelection);
            statusVehicleBtn.setDisable(!hasSelection);
            mileageVehicleBtn.setDisable(!hasSelection);
        });
    }
    
    @SuppressWarnings("unchecked")
    private void loadVehicleData() {
        apiService.getAllVehicles().thenAccept(vehicles -> {
            Platform.runLater(() -> {
                vehicleData.clear();
                for (Object vehicle : vehicles) {
                    vehicleData.add((Map<String, Object>) vehicle);
                }
                applyFilters();
                logger.info("Charge {} vehicules", vehicles.size());
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logger.error("Erreur chargement vehicules: {}", throwable.getMessage());
                showError("Erreur de chargement", "Impossible de charger les vehicules:\n" + throwable.getMessage());
            });
            return null;
        });
    }
    
    private void loadStatistics() {
        apiService.getVehicleStatistics().thenAccept(stats -> {
            Platform.runLater(() -> {
                if (stats instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> statsMap = (Map<String, Object>) stats;
                    updateStatisticsLabels(statsMap);
                }
            });
        }).exceptionally(throwable -> {
            logger.error("Erreur chargement statistiques vehicules: {}", throwable.getMessage());
            return null;
        });
    }
    
    private void updateStatisticsLabels(Map<String, Object> stats) {
        int total = ((Number) stats.getOrDefault("total", 0)).intValue();
        int available = ((Number) stats.getOrDefault("available", 0)).intValue();
        int maintenance = ((Number) stats.getOrDefault("inMaintenance", 0)).intValue();
        int alerts = ((Number) stats.getOrDefault("needingMaintenance", 0)).intValue() + 
                    ((Number) stats.getOrDefault("expiredDocuments", 0)).intValue();
        
        totalLabel.setText("Total: " + total);
        availableLabel.setText("Disponibles: " + available);
        maintenanceLabel.setText("Maintenance: " + maintenance);
        alertsLabel.setText("Alertes: " + alerts);
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String typeValue = typeFilter.getValue();
        String statusValue = statusFilter.getValue();
        boolean showMaintenanceAlerts = maintenanceAlertFilter.isSelected();
        boolean showDocumentsExpired = documentsExpiredFilter.isSelected();
        
        ObservableList<Map<String, Object>> filteredData = FXCollections.observableArrayList();
        
        for (Map<String, Object> vehicle : vehicleData) {
            // Filtre de recherche
            boolean matchesSearch = searchText.isEmpty();
            if (!matchesSearch) {
                String brand = (String) vehicle.get("brand");
                String model = (String) vehicle.get("model");
                String licensePlate = (String) vehicle.get("licensePlate");
                String vin = (String) vehicle.get("vin");
                
                matchesSearch = (brand != null && brand.toLowerCase().contains(searchText)) ||
                              (model != null && model.toLowerCase().contains(searchText)) ||
                              (licensePlate != null && licensePlate.toLowerCase().contains(searchText)) ||
                              (vin != null && vin.toLowerCase().contains(searchText));
            }
                
            // Filtre par type
            boolean matchesType = "Tous types".equals(typeValue);
            if (!matchesType) {
                String vehicleType = (String) vehicle.get("type");
                matchesType = typeValue.equals(vehicleType);
            }
                
            // Filtre par statut  
            boolean matchesStatus = "Tous statuts".equals(statusValue);
            if (!matchesStatus) {
                String vehicleStatus = (String) vehicle.get("status");
                matchesStatus = statusValue.equals(vehicleStatus);
            }
                
            // Filtre alertes maintenance
            boolean matchesMaintenanceAlert = !showMaintenanceAlerts;
            if (showMaintenanceAlerts) {
                // Vérifier si le véhicule nécessite une maintenance
                Object lastMaintenance = vehicle.get("lastMaintenanceDate");
                if (lastMaintenance != null) {
                    // Logique d'alerte maintenance (ex: > 6 mois)
                    matchesMaintenanceAlert = true; // À adapter selon la logique métier
                }
            }
                
            // Filtre documents expirés
            boolean matchesDocumentsExpired = !showDocumentsExpired;
            if (showDocumentsExpired) {
                // Vérifier si des documents sont expirés
                Object insuranceExpiry = vehicle.get("insuranceExpiryDate");
                Object inspectionExpiry = vehicle.get("technicalInspectionDate");
                if (insuranceExpiry != null || inspectionExpiry != null) {
                    // Logique documents expirés
                    matchesDocumentsExpired = true; // À adapter selon la logique métier
                }
            }
                
            if (matchesSearch && matchesType && matchesStatus && 
                matchesMaintenanceAlert && matchesDocumentsExpired) {
                filteredData.add(vehicle);
            }
        }
        
        vehicleTable.setItems(filteredData);
        loadStatistics(); // Mettre à jour les statistiques
    }
    
    private void addVehicle() {
        VehicleDialog dialog = new VehicleDialog(null);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            apiService.createVehicle(result.get()).thenAccept(newVehicle -> {
                Platform.runLater(() -> {
                    loadVehicleData(); // Recharger pour avoir l'ID
                    loadStatistics();
                    showInfo("Vehicule cree", "Le vehicule a ete cree avec succes.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de creation", "Impossible de creer le vehicule:\n" + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    private void editVehicle() {
        Map<String, Object> selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        VehicleDialog dialog = new VehicleDialog(selected);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            Long vehicleId = ((Number) selected.get("id")).longValue();
            apiService.updateVehicle(vehicleId, result.get()).thenAccept(updatedVehicle -> {
                Platform.runLater(() -> {
                    loadVehicleData();
                    loadStatistics();
                    showInfo("Vehicule modifie", "Le vehicule a ete modifie avec succes.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de modification", "Impossible de modifier le vehicule:\n" + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    private void deleteVehicle() {
        Map<String, Object> selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        String vehicleName = String.valueOf(selected.get("name"));
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le vehicule");
        alert.setContentText("Etes-vous sur de vouloir supprimer le vehicule:\n" + vehicleName);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long vehicleId = ((Number) selected.get("id")).longValue();
            
            apiService.deleteVehicle(vehicleId).thenRun(() -> {
                Platform.runLater(() -> {
                    loadVehicleData();
                    loadStatistics();
                    showInfo("Vehicule supprime", "Le vehicule a ete supprime avec succes.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de suppression", "Impossible de supprimer le vehicule:\n" + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    private void changeVehicleStatus() {
        Map<String, Object> selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Dialog simple pour changer le statut
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Modifier le statut du vehicule");
        dialog.setContentText("Nouveau statut:");
        
        dialog.getItems().addAll("AVAILABLE", "IN_USE", "MAINTENANCE", 
                                "OUT_OF_ORDER", "RENTED_OUT", "RESERVED");
        dialog.setSelectedItem(String.valueOf(selected.get("status")));
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Long vehicleId = ((Number) selected.get("id")).longValue();
            
            apiService.updateVehicleStatus(vehicleId, result.get()).thenAccept(updatedVehicle -> {
                Platform.runLater(() -> {
                    loadVehicleData();
                    loadStatistics();
                    showInfo("Statut modifie", "Le statut du vehicule a ete modifie avec succes.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur", "Impossible de modifier le statut:\n" + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    private void updateVehicleMileage() {
        Map<String, Object> selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Dialog pour mettre a jour le kilometrage
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mettre a jour le kilometrage");
        dialog.setHeaderText("Modifier le kilometrage du vehicule");
        dialog.setContentText("Nouveau kilometrage (km):");
        
        Object currentMileage = selected.get("mileage");
        if (currentMileage != null) {
            dialog.getEditor().setText(currentMileage.toString());
        }
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Integer newMileage = Integer.valueOf(result.get());
                Long vehicleId = ((Number) selected.get("id")).longValue();
                
                apiService.updateVehicleMileage(vehicleId, newMileage).thenAccept(updatedVehicle -> {
                    Platform.runLater(() -> {
                        loadVehicleData();
                        showInfo("Kilometrage modifie", "Le kilometrage a ete mis a jour avec succes.");
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showError("Erreur", "Impossible de modifier le kilometrage:\n" + throwable.getMessage());
                    });
                    return null;
                });
            } catch (NumberFormatException e) {
                showError("Erreur de saisie", "Veuillez saisir un nombre valide pour le kilometrage.");
            }
        }
    }
    
    private String getStatusDisplayName(String status) {
        return switch (status) {
            case "AVAILABLE" -> "Disponible";
            case "IN_USE" -> "En utilisation";
            case "MAINTENANCE" -> "Maintenance";
            case "OUT_OF_ORDER" -> "Hors service";
            case "RENTED_OUT" -> "Loue externe";
            case "RESERVED" -> "Reserve";
            default -> status;
        };
    }
    
    private String getStatusStyle(String status) {
        return switch (status) {
            case "AVAILABLE" -> "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "IN_USE" -> "-fx-text-fill: #3498db; -fx-font-weight: bold;";
            case "MAINTENANCE", "OUT_OF_ORDER" -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
            case "RENTED_OUT" -> "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
            case "RESERVED" -> "-fx-text-fill: #9b59b6; -fx-font-weight: bold;";
            default -> "";
        };
    }
    
    private String getVehicleAlerts(Map<String, Object> vehicle) {
        StringBuilder alerts = new StringBuilder();
        
        // Verifier maintenance
        String nextMaintenance = (String) vehicle.get("nextMaintenanceDate");
        if (nextMaintenance != null) {
            LocalDate maintenanceDate = LocalDate.parse(nextMaintenance);
            if (maintenanceDate.isBefore(LocalDate.now().plusDays(30))) {
                alerts.append("🚡M ");
            }
        }
        
        // Verifier documents
        String insuranceExp = (String) vehicle.get("insuranceExpiration");
        String technicalExp = (String) vehicle.get("technicalControlExpiration");
        
        if (insuranceExp != null && LocalDate.parse(insuranceExp).isBefore(LocalDate.now())) {
            alerts.append("🚴A ");
        }
        if (technicalExp != null && LocalDate.parse(technicalExp).isBefore(LocalDate.now())) {
            alerts.append("🚴CT ");
        }
        
        return alerts.toString().trim();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

