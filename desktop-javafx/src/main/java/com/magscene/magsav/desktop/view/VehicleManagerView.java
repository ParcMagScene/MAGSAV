package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.VehicleDialog;
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
 * Vue de gestion des vÃƒÂ©hicules
 * Interface principale pour CRUD vÃƒÂ©hicules avec filtres et recherche
 */
public class VehicleManagerView extends VBox {
    
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
    
    // Boutons d'action
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;
    private Button statusButton;
    private Button mileageButton;
    
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
        setSpacing(10);
        setPadding(new Insets(20));
        
        // Titre
        Label titleLabel = new Label("Gestion des VÃƒÂ©hicules");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Barre de statistiques
        HBox statsBox = createStatisticsBar();
        
        // Barre de recherche et filtres
        HBox filtersBox = createFiltersBar();
        
        // Barre de boutons d'actions
        HBox buttonsBox = createButtonsBar();
        
        // Table des vÃƒÂ©hicules
        vehicleTable = createVehicleTable();
        
        // Ajout ÃƒÂ  la vue principale
        getChildren().addAll(titleLabel, statsBox, filtersBox, buttonsBox, vehicleTable);
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);
    }
    
    private HBox createStatisticsBar() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        
        totalLabel = new Label("Total: 0");
        availableLabel = new Label("Disponibles: 0");
        maintenanceLabel = new Label("Maintenance: 0");
        alertsLabel = new Label("Alertes: 0");
        
        // Style des labels
        for (Label label : List.of(totalLabel, availableLabel, maintenanceLabel, alertsLabel)) {
            label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
            label.setTextFill(Color.web("#34495e"));
        }
        
        // Couleurs spÃƒÂ©cifiques
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
        HBox filtersBox = new HBox(10);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(10));
        
        // Recherche globale
        searchField = new TextField();
        searchField.setPromptText("Rechercher vÃƒÂ©hicule, marque, plaque...");
        searchField.setPrefWidth(250);
        
        // Filtre par type
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "VAN", "TRUCK", "TRAILER", "CAR", "MOTORCYCLE", "OTHER");
        typeFilter.setValue("Tous types");
        typeFilter.setPrefWidth(120);
        
        // Filtre par statut
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous statuts", "AVAILABLE", "IN_USE", "MAINTENANCE", 
                                      "OUT_OF_ORDER", "RENTED_OUT", "RESERVED");
        statusFilter.setValue("Tous statuts");
        statusFilter.setPrefWidth(140);
        
        // Filtres spÃƒÂ©ciaux
        maintenanceAlertFilter = new CheckBox("Maintenance requise");
        documentsExpiredFilter = new CheckBox("Documents expirÃƒÂ©s");
        
        Label filtersLabel = new Label("Filtres:");
        filtersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtersBox.getChildren().addAll(
            new Label("Recherche:"), searchField,
            new Separator(),
            filtersLabel, typeFilter, statusFilter,
            new Separator(),
            maintenanceAlertFilter, documentsExpiredFilter
        );
        
        return filtersBox;
    }
    
    private HBox createButtonsBar() {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        buttonsBox.setPadding(new Insets(10, 0, 10, 0));
        
        // Boutons principaux
        addButton = new Button("Nouveau VÃƒÂ©hicule");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        
        editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setDisable(true);
        
        deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setDisable(true);
        
        refreshButton = new Button("Actualiser");
        refreshButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        
        // Boutons actions rapides
        statusButton = new Button("Changer Statut");
        statusButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        statusButton.setDisable(true);
        
        mileageButton = new Button("Mettre ÃƒÂ  jour KM");
        mileageButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        mileageButton.setDisable(true);
        
        buttonsBox.getChildren().addAll(
            addButton, editButton, deleteButton,
            new Separator(),
            refreshButton,
            new Separator(),
            statusButton, mileageButton
        );
        
        return buttonsBox;
    }
    
    private TableView<Map<String, Object>> createVehicleTable() {
        TableView<Map<String, Object>> table = new TableView<>();
        table.setItems(vehicleData);
        
        // Colonnes de la table
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().get("name"))));
        nameCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> brandModelCol = new TableColumn<>("Marque/ModÃƒÂ¨le");
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
        
        TableColumn<Map<String, Object>, String> mileageCol = new TableColumn<>("KilomÃƒÂ©trage");
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
                    if (alerts.contains("Ã°Å¸â€Â´")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (alerts.contains("Ã°Å¸Å¸Â¡")) {
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
        
        // SÃƒÂ©lection simple
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        return table;
    }
    
    private void setupEventHandlers() {
        // SÃƒÂ©lection dans la table
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            statusButton.setDisable(!hasSelection);
            mileageButton.setDisable(!hasSelection);
        });
        
        // Double-clic pour modifier
        vehicleTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editVehicle();
                }
            });
            return row;
        });
        
        // Filtres temps rÃƒÂ©el
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        maintenanceAlertFilter.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        documentsExpiredFilter.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Boutons d'action
        addButton.setOnAction(e -> addVehicle());
        editButton.setOnAction(e -> editVehicle());
        deleteButton.setOnAction(e -> deleteVehicle());
        refreshButton.setOnAction(e -> {
            loadVehicleData();
            loadStatistics();
        });
        statusButton.setOnAction(e -> changeVehicleStatus());
        mileageButton.setOnAction(e -> updateVehicleMileage());
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
                logger.info("ChargÃƒÂ© {} vÃƒÂ©hicules", vehicles.size());
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logger.error("Erreur chargement vÃƒÂ©hicules: {}", throwable.getMessage());
                showError("Erreur de chargement", "Impossible de charger les vÃƒÂ©hicules:\n" + throwable.getMessage());
            });
            return null;
        });
    }
    
    private void loadStatistics() {
        apiService.getVehicleStatistics().thenAccept(stats -> {
            Platform.runLater(() -> {
                updateStatisticsLabels(stats);
            });
        }).exceptionally(throwable -> {
            logger.error("Erreur chargement statistiques vÃƒÂ©hicules: {}", throwable.getMessage());
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
        // Implementation des filtres sera ajoutÃƒÂ©e
        // Pour l'instant, on affiche tous les vÃƒÂ©hicules
    }
    
    private void addVehicle() {
        VehicleDialog dialog = new VehicleDialog(null, (Stage) getScene().getWindow());
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            apiService.createVehicle(result.get()).thenAccept(newVehicle -> {
                Platform.runLater(() -> {
                    loadVehicleData(); // Recharger pour avoir l'ID
                    loadStatistics();
                    showInfo("VÃƒÂ©hicule crÃƒÂ©ÃƒÂ©", "Le vÃƒÂ©hicule a ÃƒÂ©tÃƒÂ© crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de crÃƒÂ©ation", "Impossible de crÃƒÂ©er le vÃƒÂ©hicule:\n" + throwable.getMessage());
                });
                return null;
            });
        }
    }
    
    private void editVehicle() {
        Map<String, Object> selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        VehicleDialog dialog = new VehicleDialog(selected, (Stage) getScene().getWindow());
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            Long vehicleId = ((Number) selected.get("id")).longValue();
            apiService.updateVehicle(vehicleId, result.get()).thenAccept(updatedVehicle -> {
                Platform.runLater(() -> {
                    loadVehicleData();
                    loadStatistics();
                    showInfo("VÃƒÂ©hicule modifiÃƒÂ©", "Le vÃƒÂ©hicule a ÃƒÂ©tÃƒÂ© modifiÃƒÂ© avec succÃƒÂ¨s.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de modification", "Impossible de modifier le vÃƒÂ©hicule:\n" + throwable.getMessage());
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
        alert.setHeaderText("Supprimer le vÃƒÂ©hicule");
        alert.setContentText("ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer le vÃƒÂ©hicule:\n" + vehicleName);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long vehicleId = ((Number) selected.get("id")).longValue();
            
            apiService.deleteVehicle(vehicleId).thenRun(() -> {
                Platform.runLater(() -> {
                    loadVehicleData();
                    loadStatistics();
                    showInfo("VÃƒÂ©hicule supprimÃƒÂ©", "Le vÃƒÂ©hicule a ÃƒÂ©tÃƒÂ© supprimÃƒÂ© avec succÃƒÂ¨s.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    showError("Erreur de suppression", "Impossible de supprimer le vÃƒÂ©hicule:\n" + throwable.getMessage());
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
        dialog.setHeaderText("Modifier le statut du vÃƒÂ©hicule");
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
                    showInfo("Statut modifiÃƒÂ©", "Le statut du vÃƒÂ©hicule a ÃƒÂ©tÃƒÂ© modifiÃƒÂ© avec succÃƒÂ¨s.");
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
        
        // Dialog pour mettre ÃƒÂ  jour le kilomÃƒÂ©trage
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mettre ÃƒÂ  jour le kilomÃƒÂ©trage");
        dialog.setHeaderText("Modifier le kilomÃƒÂ©trage du vÃƒÂ©hicule");
        dialog.setContentText("Nouveau kilomÃƒÂ©trage (km):");
        
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
                        showInfo("KilomÃƒÂ©trage modifiÃƒÂ©", "Le kilomÃƒÂ©trage a ÃƒÂ©tÃƒÂ© mis ÃƒÂ  jour avec succÃƒÂ¨s.");
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showError("Erreur", "Impossible de modifier le kilomÃƒÂ©trage:\n" + throwable.getMessage());
                    });
                    return null;
                });
            } catch (NumberFormatException e) {
                showError("Erreur de saisie", "Veuillez saisir un nombre valide pour le kilomÃƒÂ©trage.");
            }
        }
    }
    
    private String getStatusDisplayName(String status) {
        return switch (status) {
            case "AVAILABLE" -> "Disponible";
            case "IN_USE" -> "En utilisation";
            case "MAINTENANCE" -> "Maintenance";
            case "OUT_OF_ORDER" -> "Hors service";
            case "RENTED_OUT" -> "LouÃƒÂ© externe";
            case "RESERVED" -> "RÃƒÂ©servÃƒÂ©";
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
        
        // VÃƒÂ©rifier maintenance
        String nextMaintenance = (String) vehicle.get("nextMaintenanceDate");
        if (nextMaintenance != null) {
            LocalDate maintenanceDate = LocalDate.parse(nextMaintenance);
            if (maintenanceDate.isBefore(LocalDate.now().plusDays(30))) {
                alerts.append("Ã°Å¸Å¸Â¡M ");
            }
        }
        
        // VÃƒÂ©rifier documents
        String insuranceExp = (String) vehicle.get("insuranceExpiration");
        String technicalExp = (String) vehicle.get("technicalControlExpiration");
        
        if (insuranceExp != null && LocalDate.parse(insuranceExp).isBefore(LocalDate.now())) {
            alerts.append("Ã°Å¸â€Â´A ");
        }
        if (technicalExp != null && LocalDate.parse(technicalExp).isBefore(LocalDate.now())) {
            alerts.append("Ã°Å¸â€Â´CT ");
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

