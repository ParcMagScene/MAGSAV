package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Interface JavaFX complète pour la gestion du parc matériel
 * Fonctionnalités : tableau détaillé, recherche, filtres, CRUD, statistiques
 */
public class EquipmentManagerView extends VBox {
    
    private final ApiService apiService;
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentData;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;
    
    public EquipmentManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.equipmentData = FXCollections.observableArrayList();
        initializeUI();
        loadEquipmentData();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        VBox header = createHeader();
        
        // Table des équipements (créer AVANT les boutons)
        VBox tableContainer = createTableContainer();
        
        // Toolbar avec recherche et filtres (créer APRÈS la table)
        HBox toolbar = createToolbar();
        
        // Footer avec statistiques
        HBox footer = createFooter();
        
        getChildren().addAll(header, toolbar, tableContainer, footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("📦 Gestion du Parc Matériel");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Inventaire complet • Suivi en temps réel • Maintenance préventive");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Recherche
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Nom, modèle, numéro de série...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterEquipment());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par catégorie
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("📁 Catégorie");
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Toutes", "Audio", "Éclairage", "Vidéo", "Structures", "Câblage", "Transport");
        categoryFilter.setValue("Toutes");
        categoryFilter.setPrefWidth(150);
        categoryFilter.setOnAction(e -> filterEquipment());
        categoryBox.getChildren().addAll(categoryLabel, categoryFilter);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("🔄 Statut");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Disponible", "En cours d'utilisation", "En maintenance", "Hors service");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(180);
        statusFilter.setOnAction(e -> filterEquipment());
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button addButton = new Button("➕ Ajouter");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4;");
        addButton.setOnAction(e -> addEquipment());
        
        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4;");
        editButton.setOnAction(e -> editEquipment());
        editButton.disableProperty().bind(equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4;");
        deleteButton.setOnAction(e -> deleteEquipment());
        deleteButton.disableProperty().bind(equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4;");
        refreshButton.setOnAction(e -> refreshData());
        
        buttonRow.getChildren().addAll(addButton, editButton, deleteButton, refreshButton);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);
        
        toolbar.getChildren().addAll(searchBox, categoryBox, statusBox, actionsBox, spacer, loadingIndicator);
        return toolbar;
    }
    
    private VBox createTableContainer() {
        VBox container = new VBox(10);
        
        // Configuration de la table
        equipmentTable = new TableView<>();
        equipmentTable.setItems(equipmentData);
        equipmentTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        equipmentTable.setPrefHeight(400);
        
        // Colonnes de la table
        createTableColumns();
        
        // Style des lignes
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    switch (newItem.getStatus()) {
                        case "Disponible":
                            row.setStyle("-fx-background-color: #d5f4e6;");
                            break;
                        case "En cours d'utilisation":
                            row.setStyle("-fx-background-color: #fff3cd;");
                            break;
                        case "En maintenance":
                            row.setStyle("-fx-background-color: #f8d7da;");
                            break;
                        case "Hors service":
                            row.setStyle("-fx-background-color: #f5c6cb;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });
        
        container.getChildren().add(equipmentTable);
        VBox.setVgrow(equipmentTable, Priority.ALWAYS);
        
        return container;
    }
    
    private void createTableColumns() {
        // Colonne QR Code
        TableColumn<EquipmentItem, String> qrCol = new TableColumn<>("QR");
        qrCol.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        qrCol.setPrefWidth(60);
        
        // Colonne Nom
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        // Colonne Marque/Modèle
        TableColumn<EquipmentItem, String> brandModelCol = new TableColumn<>("Marque/Modèle");
        brandModelCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getBrand() + " " + data.getValue().getModel()));
        brandModelCol.setPrefWidth(180);
        
        // Colonne Catégorie
        TableColumn<EquipmentItem, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        // Colonne Statut
        TableColumn<EquipmentItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(column -> new TableCell<EquipmentItem, String>() {
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
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "En cours d'utilisation":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "En maintenance":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Hors service":
                            setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        // Colonne Prix
        TableColumn<EquipmentItem, String> priceCol = new TableColumn<>("Prix");
        priceCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("%.0f €", data.getValue().getPurchasePrice())));
        priceCol.setPrefWidth(100);
        
        // Colonne Numéro de série
        TableColumn<EquipmentItem, String> serialCol = new TableColumn<>("N° Série");
        serialCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        serialCol.setPrefWidth(120);
        
        var columns = equipmentTable.getColumns();
        columns.add(qrCol);
        columns.add(nameCol);
        columns.add(brandModelCol);
        columns.add(categoryCol);
        columns.add(statusCol);
        columns.add(priceCol);
        columns.add(serialCol);
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(15, 0, 0, 0));
        footer.setAlignment(Pos.CENTER_LEFT);
        
        statsLabel = new Label("📊 Chargement des statistiques...");
        statsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statsLabel.setTextFill(Color.web("#7f8c8d"));
        
        footer.getChildren().add(statsLabel);
        return footer;
    }
    
    private void loadEquipmentData() {
        loadingIndicator.setVisible(true);
        statsLabel.setText("📊 Chargement des données...");
        
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiService.getEquipments().thenAccept(equipments -> {
                    Platform.runLater(() -> {
                        equipmentData.clear();
                        for (Object equipmentObj : equipments) {
                            if (equipmentObj instanceof Map<?, ?> equipmentMap) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> equipment = (Map<String, Object>) equipmentMap;
                                EquipmentItem item = new EquipmentItem(equipment);
                                equipmentData.add(item);
                            }
                        }
                        updateStatistics();
                        loadingIndicator.setVisible(false);
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showError("Erreur de chargement", "Impossible de charger les équipements: " + throwable.getMessage());
                        loadingIndicator.setVisible(false);
                    });
                    return null;
                });
                return null;
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void filterEquipment() {
        String searchText = searchField.getText().toLowerCase();
        String categoryValue = categoryFilter.getValue();
        String statusValue = statusFilter.getValue();
        
        ObservableList<EquipmentItem> filteredData = FXCollections.observableArrayList();
        
        for (EquipmentItem item : equipmentData) {
            boolean matchesSearch = searchText.isEmpty() || 
                item.getName().toLowerCase().contains(searchText) ||
                item.getBrand().toLowerCase().contains(searchText) ||
                item.getModel().toLowerCase().contains(searchText) ||
                item.getSerialNumber().toLowerCase().contains(searchText);
                
            boolean matchesCategory = "Toutes".equals(categoryValue) || 
                item.getCategory().equals(categoryValue);
                
            boolean matchesStatus = "Tous".equals(statusValue) || 
                item.getStatus().equals(statusValue);
                
            if (matchesSearch && matchesCategory && matchesStatus) {
                filteredData.add(item);
            }
        }
        
        equipmentTable.setItems(filteredData);
        updateStatistics();
    }
    
    /**
     * Ajouter un nouvel équipement
     */
    private void addEquipment() {
        EquipmentDialog dialog = new EquipmentDialog(apiService, null);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            Map<String, Object> equipmentData = result.get();
            apiService.createEquipment(equipmentData).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.containsKey("error")) {
                        showError("Erreur", "Impossible de créer l'équipement: " + response.get("error"));
                    } else {
                        showInfo("Succès", "Équipement créé avec succès !");
                        refreshData();
                    }
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showError("Erreur", "Erreur lors de la création: " + throwable.getMessage()));
                return null;
            });
        }
    }
    
    /**
     * Modifier l'équipement sélectionné
     */
    private void editEquipment() {
        EquipmentItem selectedItem = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        
        // Récupérer les données complètes de l'équipement
        Map<String, Object> equipmentData = convertEquipmentItemToMap(selectedItem);
        
        EquipmentDialog dialog = new EquipmentDialog(apiService, equipmentData);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            Map<String, Object> updatedData = result.get();
            Long equipmentId = Long.valueOf(selectedItem.getId());
            
            apiService.updateEquipment(equipmentId, updatedData).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.containsKey("error")) {
                        showError("Erreur", "Impossible de modifier l'équipement: " + response.get("error"));
                    } else {
                        showInfo("Succès", "Équipement modifié avec succès !");
                        refreshData();
                    }
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showError("Erreur", "Erreur lors de la modification: " + throwable.getMessage()));
                return null;
            });
        }
    }
    
    /**
     * Supprimer l'équipement sélectionné
     */
    private void deleteEquipment() {
        EquipmentItem selectedItem = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'équipement");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'équipement '" + selectedItem.getName() + "' ?\n\nCette action est irréversible.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long equipmentId = Long.valueOf(selectedItem.getId());
            
            apiService.deleteEquipment(equipmentId).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        showInfo("Succès", "Équipement supprimé avec succès !");
                        refreshData();
                    } else {
                        showError("Erreur", "Impossible de supprimer l'équipement.");
                    }
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showError("Erreur", "Erreur lors de la suppression: " + throwable.getMessage()));
                return null;
            });
        }
    }
    
    /**
     * Actualiser les données
     */
    private void refreshData() {
        loadEquipmentData();
    }
    
    /**
     * Convertir EquipmentItem en Map pour édition
     */
    private Map<String, Object> convertEquipmentItemToMap(EquipmentItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("brand", item.getBrand());
        map.put("model", item.getModel());
        map.put("category", item.getCategory());
        map.put("status", item.getStatus());
        map.put("qrCode", item.getQrCode());
        map.put("serialNumber", item.getSerialNumber());
        map.put("purchasePrice", item.getPurchasePrice());
        return map;
    }
    
    /**
     * Afficher un message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Afficher un message d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void updateStatistics() {
        int total = equipmentTable.getItems().size();
        long available = equipmentTable.getItems().stream()
            .filter(item -> "Disponible".equals(item.getStatus())).count();
        long inUse = equipmentTable.getItems().stream()
            .filter(item -> "En cours d'utilisation".equals(item.getStatus())).count();
        long maintenance = equipmentTable.getItems().stream()
            .filter(item -> "En maintenance".equals(item.getStatus())).count();
        long outOfService = equipmentTable.getItems().stream()
            .filter(item -> "Hors service".equals(item.getStatus())).count();
            
        double totalValue = equipmentTable.getItems().stream()
            .mapToDouble(EquipmentItem::getPurchasePrice).sum();
        
        statsLabel.setText(String.format(
            "📊 Total: %d • ✅ Disponible: %d • 🔄 En cours: %d • 🔧 Maintenance: %d • ❌ HS: %d • 💰 Valeur: %.0f €",
            total, available, inUse, maintenance, outOfService, totalValue
        ));
    }
}