package com.magscene.magsav.desktop.view.vehicle;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.dialog.VehicleDialog;
import com.magscene.magsav.desktop.dialog.VehicleDetailDialog;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.view.VehicleItem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Vue de liste des véhicules avec tableau détaillé
 * Fonctionnalités : tableau, recherche, filtres, CRUD, statistiques
 */
public class VehicleListView extends BorderPane {
    
    private final ApiService apiService;
    private TableView<VehicleItem> vehicleTable;
    private ObservableList<VehicleItem> vehicleData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ProgressIndicator loadingIndicator;
    
    public VehicleListView(ApiService apiService) {
        this.apiService = apiService;
        this.vehicleData = FXCollections.observableArrayList();
        
        // Initialiser les composants de filtrage (même sans toolbar interne)
        initializeFilterComponents();
        
        initializeUI();
        loadVehicleData();
    }
    
    /**
     * Initialise les composants de filtrage pour synchronisation externe
     */
    private void initializeFilterComponents() {
        searchField = new TextField();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterVehicles());
        
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "CAMION", "FOURGON", "REMORQUE", "UTILITAIRE");
        typeFilter.setValue("Tous");
        typeFilter.setOnAction(e -> filterVehicles());
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Disponible", "En Mission", "En Maintenance", "Hors Service");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> filterVehicles());
        
        // Initialiser les composants UI basiques
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(20, 20);
        loadingIndicator.setVisible(false);
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Tableau central (sans toolbar - maintenant gérée par VehicleManagerView)
        DetailPanelContainer tableContainer = createTableContainer();
        setCenter(tableContainer);
    }
    
    // Toolbar supprimée - maintenant gérée par VehicleManagerView
    
    private DetailPanelContainer createTableContainer() {
        vehicleTable = new TableView<>();
        vehicleTable.setItems(vehicleData);
        vehicleTable.getStyleClass().add("vehicle-table");
        vehicleTable.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; -fx-border-color: " + StandardColors.getBorderColor() + "; -fx-border-width: 2px; -fx-border-radius: 8px;");
        
        createTableColumns();
        
        // Double-clic pour ouvrir la fiche détaillée en mode lecture seule
        vehicleTable.setRowFactory(tv -> {
            TableRow<VehicleItem> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + UnifiedThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + UnifiedThemeManager.getInstance().getSelectionBorderColor() + "; " +
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
            
            // Double-clic pour éditer le véhicule (comme les équipements)
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editVehicle();
                }
            });
            
            return row;
        });
        
        // Gestion d'état déplacée vers toolbar externe (VehicleManagerView)
        
        DetailPanelContainer container = DetailPanelContainer.wrapTableView(vehicleTable);
        
        return container;
    }
    
    private void createTableColumns() {
        // Immatriculation
        TableColumn<VehicleItem, String> plateCol = new TableColumn<>("Immatriculation");
        plateCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        plateCol.setPrefWidth(120);
        
        // Marque
        TableColumn<VehicleItem, String> brandCol = new TableColumn<>("Marque");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(120);
        
        // Nom/Désignation
        TableColumn<VehicleItem, String> nameCol = new TableColumn<>("Désignation");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);
        
        // Type
        TableColumn<VehicleItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        
        // Statut
        TableColumn<VehicleItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(column -> new TableCell<VehicleItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Disponible":
                            // Style gere par CSS
                            break;
                        case "En Mission":
                            // Style gere par CSS
                            break;
                        case "En Maintenance":
                            // Style gere par CSS
                            break;
                        case "Hors Service":
                            // Style gere par CSS
                            break;
                        default:
                            // Style gere par CSS
                    }
                }
            }
        });
        
        // Kilométrage
        TableColumn<VehicleItem, String> mileageCol = new TableColumn<>("Kilométrage");
        mileageCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getMileage() != null ? 
                String.format("%.0f km", data.getValue().getMileage()) : "-"));
        mileageCol.setPrefWidth(100);
        
        // Localisation
        TableColumn<VehicleItem, String> locationCol = new TableColumn<>("Localisation");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);
        
        // Propriétaire
        TableColumn<VehicleItem, String> ownerCol = new TableColumn<>("Propriétaire");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerCol.setPrefWidth(120);
        
        // Couleur
        TableColumn<VehicleItem, String> colorCol = new TableColumn<>("Couleur");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorCol.setPrefWidth(80);
        
        vehicleTable.getColumns().add(plateCol);
        vehicleTable.getColumns().add(nameCol);
        vehicleTable.getColumns().add(brandCol);
        vehicleTable.getColumns().add(typeCol);
        vehicleTable.getColumns().add(colorCol);
        vehicleTable.getColumns().add(ownerCol);
        vehicleTable.getColumns().add(statusCol);
        vehicleTable.getColumns().add(mileageCol);
        vehicleTable.getColumns().add(locationCol);
    }

    private void loadVehicleData() {
        setLoading(true);
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                apiService.getAllVehicles().thenAccept(vehicles -> {
                    Platform.runLater(() -> {
                        vehicleData.clear();
                        for (Object vehicleObj : vehicles) {
                            if (vehicleObj instanceof Map<?, ?> vehicleMap) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> vehicle = (Map<String, Object>) vehicleMap;
                                VehicleItem item = new VehicleItem(vehicle);
                                vehicleData.add(item);
                            }
                        }
                        setLoading(false);
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("Erreur lors du chargement des véhicules: " + throwable.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    private void filterVehicles() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedType = typeFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        
        vehicleTable.setItems(vehicleData.filtered(item -> {
            // Filtre texte
            boolean matchesSearch = searchText.isEmpty() ||
                (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                (item.getLicensePlate() != null && item.getLicensePlate().toLowerCase().contains(searchText)) ||
                (item.getBrand() != null && item.getBrand().toLowerCase().contains(searchText)) ||
                (item.getModel() != null && item.getModel().toLowerCase().contains(searchText));
            
            // Filtre type
            boolean matchesType = "Tous".equals(selectedType) || selectedType == null ||
                (item.getType() != null && item.getType().toUpperCase().contains(selectedType.toUpperCase()));
            
            // Filtre statut
            boolean matchesStatus = "Tous".equals(selectedStatus) || selectedStatus == null ||
                (item.getStatus() != null && item.getStatus().equals(selectedStatus));
            
            return matchesSearch && matchesType && matchesStatus;
        }));
    }
    
    private void setLoading(boolean loading) {
        // loadingIndicator géré par toolbar externe maintenant
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }
    
    // Actions CRUD
    private void addVehicle() {
        VehicleDialog dialog = new VehicleDialog(null);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(getScene().getWindow());
        
        Optional<Map<String, Object>> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Créer le véhicule via l'API
            apiService.createVehicle(result.get()).thenRun(() -> {
                Platform.runLater(this::refreshData);
            });
        }
    }
    
    private void editVehicle() {
        VehicleItem selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Veuillez sélectionner un véhicule à modifier");
            alert.showAndWait();
            return;
        }
        
        // Convertir VehicleItem vers Map<String, Object> pour le dialog
        Map<String, Object> vehicleData = new java.util.HashMap<>();
        vehicleData.put("id", selectedVehicle.getId());
        vehicleData.put("name", selectedVehicle.getName());
        vehicleData.put("brand", selectedVehicle.getBrand());
        vehicleData.put("model", selectedVehicle.getModel());
        vehicleData.put("licensePlate", selectedVehicle.getLicensePlate());
        vehicleData.put("type", selectedVehicle.getType());
        vehicleData.put("status", selectedVehicle.getStatus());
        vehicleData.put("mileage", selectedVehicle.getMileage());
        vehicleData.put("currentLocation", selectedVehicle.getLocation());
        vehicleData.put("fuelType", selectedVehicle.getFuelType());
        vehicleData.put("notes", selectedVehicle.getNotes());
        vehicleData.put("color", selectedVehicle.getColor());
        vehicleData.put("owner", selectedVehicle.getOwner());
        vehicleData.put("lastMaintenanceDate", selectedVehicle.getLastMaintenance());
        vehicleData.put("nextMaintenanceDate", selectedVehicle.getNextMaintenance());
        vehicleData.put("insuranceExpiration", selectedVehicle.getInsuranceExpiry());
        vehicleData.put("technicalControlExpiration", selectedVehicle.getTechnicalControlExpiry());
        vehicleData.put("dailyRentalRate", selectedVehicle.getDailyRate());
        
        // Ouvrir le dialogue en mode lecture seule (comme pour les équipements)
        VehicleDetailDialog detailDialog = new VehicleDetailDialog(apiService, vehicleData);
        detailDialog.initModality(Modality.WINDOW_MODAL);
        detailDialog.initOwner(getScene().getWindow());
        
        Optional<Map<String, Object>> result = detailDialog.showAndWait();
        if (result != null && result.isPresent()) {
            // Rafraîchir les données si modification
            Platform.runLater(() -> {
                refreshData();
            });
        }
    }
    
    private void deleteVehicle() {
        VehicleItem selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Veuillez sélectionner un véhicule à supprimer");
            alert.showAndWait();
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer le véhicule " + selectedVehicle.getLicensePlate() + " ?");
        confirmDialog.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            apiService.deleteVehicle(selectedVehicle.getId()).thenRun(() -> {
                Platform.runLater(this::refreshData);
            });
        }
    }
    
    private void refreshData() {
        loadVehicleData();
    }
    
    /**
     * Ouvre la fiche détaillée d'un véhicule en mode lecture seule
     */
    private void openVehicleDetails(VehicleItem item) {
        if (item == null) {
            return;
        }
        
        // Convertir VehicleItem vers Map<String, Object> pour le dialog
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("id", item.getId());
        vehicleData.put("name", item.getBrand() + " " + item.getModel());
        vehicleData.put("brand", item.getBrand());
        vehicleData.put("model", item.getModel());
        vehicleData.put("licensePlate", item.getLicensePlate());
        vehicleData.put("type", item.getType());
        vehicleData.put("status", item.getStatus());
        vehicleData.put("mileage", item.getMileage());
        vehicleData.put("location", item.getLocation());
        vehicleData.put("fuelType", item.getFuelType());
        vehicleData.put("notes", item.getNotes());
        
        // Ouvrir le dialogue en mode lecture seule
        VehicleDialog dialog = new VehicleDialog(vehicleData, true); // true = mode lecture seule
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(getScene().getWindow());
        
        dialog.showAndWait().ifPresent(result -> {
            // Si des modifications ont été apportées, rafraîchir la liste
            if (result != null) {
                refreshData(); // Recharger pour refléter les changements
            }
        });
    }
    
    // ========================================
    // 🔗 MÉTHODES PUBLIQUES POUR TOOLBAR EXTERNE; // ========================================
    
    /**
     * Interface publique pour filtrage depuis toolbar externe
     */
    public void setSearchFilter(String searchText) {
        if (searchField != null) {
            searchField.setText(searchText);
        }
        filterVehicles();
    }
    
    public void setTypeFilter(String typeValue) {
        if (typeFilter != null) {
            typeFilter.setValue(typeValue);
        }
        filterVehicles();
    }
    
    public void setStatusFilter(String statusValue) {
        if (statusFilter != null) {
            statusFilter.setValue(statusValue);
        }
        filterVehicles();
    }
    
    /**
     * Interface publique pour actions depuis toolbar externe
     */
    public void handleAddVehicle() {
        addVehicle();
    }
    
    public void handleEditVehicle() {
        editVehicle();
    }
    
    public void handleDeleteVehicle() {
        deleteVehicle();
    }
    
    public void handleRefreshData() {
        refreshData();
    }
    
    /**
     * Sélectionne un véhicule par son ID
     * Utilisé par la recherche globale
     */
    public boolean selectById(String id) {
        if (id == null || id.isEmpty() || vehicleData == null) {
            return false;
        }
        
        // Réinitialiser les filtres
        if (searchField != null) searchField.clear();
        if (typeFilter != null) typeFilter.setValue("Tous");
        if (statusFilter != null) statusFilter.setValue("Tous");
        
        for (VehicleItem vehicle : vehicleData) {
            if (id.equals(String.valueOf(vehicle.getId()))) {
                Platform.runLater(() -> {
                    vehicleTable.getSelectionModel().select(vehicle);
                    vehicleTable.scrollTo(vehicle);
                    System.out.println("✅ Véhicule sélectionné: " + vehicle.getDisplayName() + " (ID: " + id + ")");
                });
                return true;
            }
        }
        
        System.out.println("⚠️ Véhicule non trouvé avec ID: " + id);
        return false;
    }
}
