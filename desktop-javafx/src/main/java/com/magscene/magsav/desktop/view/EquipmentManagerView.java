package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.dialog.EquipmentDialog;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
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
public class EquipmentManagerView extends BorderPane {
    
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
        // BorderPane n'a pas de setSpacing - architecture comme Ventes et Installations
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Table des équipements (créer EN PREMIER pour être disponible dans la toolbar)
        DetailPanelContainer tableContainer = createTableContainer();
        
        // Header avec titre
        VBox header = createHeader();
        
        // Toolbar séparée comme dans la référence
        HBox toolbar = createToolbar();
        
        // Footer avec statistiques
        HBox footer = createFooter();
        
        // Layout principal - EXACTEMENT comme Ventes et Installations
        VBox topContainer = new VBox(header, toolbar);
        
        setTop(topContainer);
        setCenter(tableContainer);
        setBottom(footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10); // STANDARD : 10px spacing comme référence
        header.setPadding(new Insets(0, 0, 20, 0)); // STANDARD : padding comme référence
        
        Label title = new Label("📦 Parc Matériel");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().add(title); // SEUL le titre dans header
        return header;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10); // EXACTEMENT comme Ventes & Installations
        toolbar.setPadding(new Insets(10)); // EXACTEMENT comme Ventes & Installations
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Recherche
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Nom, modèle, numéro de série...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                            "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                            "-fx-border-color: " + ThemeManager.getInstance().getSelectionTextColor() + "; -fx-border-radius: 4;");
        searchField.textProperty().addListener((obs, oldText, newText) -> filterEquipment());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par catégorie
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("📁 Catégorie");
        categoryLabel.setStyle("-fx-text-fill: #6B71F2;");
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("Toutes"); // Valeur par défaut, sera mis à jour dynamiquement
        categoryFilter.setValue("Toutes");
        categoryFilter.setPrefWidth(150);
        categoryFilter.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                              "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + ";");
        categoryFilter.setOnAction(e -> filterEquipment());
        categoryBox.getChildren().addAll(categoryLabel, categoryFilter);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("🔄 Statut");
        statusLabel.setStyle("-fx-text-fill: #6B71F2;");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().add("Tous"); // Valeur par défaut, sera mis à jour dynamiquement
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(180);
        statusFilter.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                           "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + ";");
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
    
    private DetailPanelContainer createTableContainer() {
        // Configuration de la table
        equipmentTable = new TableView<>();
        equipmentTable.setItems(equipmentData);
        // Style appliqué via CSS pour permettre la sélection MAGSAV
        equipmentTable.getStyleClass().add("equipment-table");
        equipmentTable.setPrefHeight(400);
        
        // Colonnes de la table
        createTableColumns();
        
        // Style des lignes avec gestion de la sélection
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<EquipmentItem>();
            
            // Méthode pour appliquer le style approprié
            Runnable updateStyle = () -> {
                if (row.isEmpty() || row.getItem() == null) {
                    row.setStyle("");
                    return;
                }
                
                // Priorité 1: Si sélectionné, couleur de sélection MAGSAV
                if (row.isSelected()) {
                    // Style de sélection plus visible avec bordure
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                    return;
                }
                
                // Priorité 2: Couleur selon le statut (seulement si pas sélectionné)
                EquipmentItem item = row.getItem();
                switch (item.getStatus()) {
                    case "Disponible":
                        row.setStyle("-fx-background-color: rgba(213, 244, 230, 0.3);");
                        break;
                    case "En cours d'utilisation":
                        row.setStyle("-fx-background-color: rgba(255, 243, 205, 0.3);");
                        break;
                    case "En maintenance":
                        row.setStyle("-fx-background-color: rgba(248, 215, 218, 0.3);");
                        break;
                    case "En SAV":
                        row.setStyle("-fx-background-color: rgba(107, 113, 242, 0.2);");
                        break;
                    case "Hors service":
                        row.setStyle("-fx-background-color: rgba(245, 198, 203, 0.3);");
                        break;
                    default:
                        row.setStyle("");
                }
            };
            
            // Mise à jour du style quand l'item change
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            // Mise à jour du style quand la sélection change
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> updateStyle.run());
            
            return row;
        });
        
        // Double-clic pour ouvrir la fiche de modification
        equipmentTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                EquipmentItem selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();
                if (selectedEquipment != null) {
                    editEquipment();
                }
            }
        });
        
        // Créer le conteneur avec volet de détails
        DetailPanelContainer container = DetailPanelContainer.wrapTableView(equipmentTable);
        
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
                            setStyle("-fx-text-fill: #27ae60;");
                            break;
                        case "En Cours D'utilisation":
                            setStyle("-fx-text-fill: #f39c12;");
                            break;
                        case "En Maintenance":
                            setStyle("-fx-text-fill: #e74c3c;");
                            break;
                        case "Hors Service":
                            setStyle("-fx-text-fill: #c0392b;");
                            break;
                        case "En SAV":
                            setStyle("-fx-text-fill: #9b59b6;");
                            break;
                        case "Retiré Du Service":
                            setStyle("-fx-text-fill: #7f8c8d;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #34495e;");
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
                        updateCategoryFilter();
                        updateStatusFilter();
                        updateStatistics();
                        loadingIndicator.setVisible(false);
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        // En cas d'échec, charger des données de démo
                        loadDemoData();
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

    private void loadDemoData() {
        equipmentData.clear();
        
        // Données de démonstration pour tester le volet de détails
        Map<String, Object> demo1 = new java.util.HashMap<>();
        demo1.put("id", 1L);
        demo1.put("name", "Projecteur LED 500W");
        demo1.put("brand", "ARRI");
        demo1.put("model", "SkyPanel S60-C");
        demo1.put("serialNumber", "SP60C-2023-001");
        demo1.put("category", "Éclairage");
        demo1.put("status", "AVAILABLE");
        demo1.put("location", "Hangar A - Rack 3");
        demo1.put("description", "Projecteur LED haute puissance avec contrôle couleur");
        demo1.put("purchasePrice", 2500.0);
        demo1.put("notes", "Révision annuelle effectuée");
        
        Map<String, Object> demo2 = new java.util.HashMap<>();
        demo2.put("id", 2L);
        demo2.put("name", "Console Audio Numérique");
        demo2.put("brand", "Yamaha");
        demo2.put("model", "CL5");
        demo2.put("serialNumber", "CL5-2022-078");
        demo2.put("category", "Audio");
        demo2.put("status", "IN_USE");
        demo2.put("location", "Régie Son - Position 1");
        demo2.put("description", "Console numérique 72 canaux avec processeurs intégrés");
        demo2.put("purchasePrice", 15000.0);
        demo2.put("notes", "En cours d'utilisation pour le concert du 15/11");
        
        Map<String, Object> demo3 = new java.util.HashMap<>();
        demo3.put("id", 3L);
        demo3.put("name", "Caméra Broadcast 4K");
        demo3.put("brand", "Sony");
        demo3.put("model", "PXW-FX9");
        demo3.put("serialNumber", "FX9-2023-142");
        demo3.put("category", "Vidéo");
        demo3.put("status", "MAINTENANCE");
        demo3.put("location", "Atelier Technique");
        demo3.put("description", "Caméra professionnelle 4K avec optiques interchangeables");
        demo3.put("purchasePrice", 8500.0);
        demo3.put("notes", "Maintenance préventive en cours - Retour prévu le 20/11");
        
        equipmentData.add(new EquipmentItem(demo1));
        equipmentData.add(new EquipmentItem(demo2));
        equipmentData.add(new EquipmentItem(demo3));
        
        updateCategoryFilter();
        updateStatusFilter();
        updateStatistics();
    }
    
    private void filterEquipment() {
        String searchText = searchField.getText().toLowerCase();
        String categoryValue = categoryFilter.getValue();
        String statusValue = statusFilter.getValue();
        
        ObservableList<EquipmentItem> filteredData = FXCollections.observableArrayList();
        
        for (EquipmentItem item : equipmentData) {
            boolean matchesSearch = searchText.isEmpty() || 
                (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                (item.getBrand() != null && item.getBrand().toLowerCase().contains(searchText)) ||
                (item.getModel() != null && item.getModel().toLowerCase().contains(searchText)) ||
                (item.getSerialNumber() != null && item.getSerialNumber().toLowerCase().contains(searchText));
                
            boolean matchesCategory = "Toutes".equals(categoryValue) || 
                (item.getCategory() != null && item.getCategory().equals(categoryValue));
                
            boolean matchesStatus = "Tous".equals(statusValue) || 
                (item.getStatus() != null && item.getStatus().equals(statusValue));
                
            if (matchesSearch && matchesCategory && matchesStatus) {
                filteredData.add(item);
            }
        }
        
        equipmentTable.setItems(filteredData);
        updateStatistics();
    }
    
    /**
     * Met à jour dynamiquement le filtre des catégories avec les valeurs réelles
     */
    private void updateCategoryFilter() {
        String selectedCategory = categoryFilter.getValue();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("Toutes");
        
        // Récupérer toutes les catégories uniques des données
        equipmentData.stream()
            .map(EquipmentItem::getCategory)
            .filter(category -> category != null && !category.trim().isEmpty())
            .distinct()
            .sorted()
            .forEach(category -> categoryFilter.getItems().add(category));
        
        // Restaurer la sélection si elle existe toujours
        if (categoryFilter.getItems().contains(selectedCategory)) {
            categoryFilter.setValue(selectedCategory);
        } else {
            categoryFilter.setValue("Toutes");
        }
    }
    
    /**
     * Met à jour dynamiquement le filtre des statuts avec les valeurs réelles
     */
    private void updateStatusFilter() {
        String selectedStatus = statusFilter.getValue();
        statusFilter.getItems().clear();
        statusFilter.getItems().add("Tous");
        
        // Récupérer tous les statuts uniques des données (déjà convertis en français)
        equipmentData.stream()
            .map(EquipmentItem::getStatus)
            .filter(status -> status != null && !status.trim().isEmpty())
            .distinct()
            .sorted()
            .forEach(status -> statusFilter.getItems().add(status));
        
        // Restaurer la sélection si elle existe toujours
        if (statusFilter.getItems().contains(selectedStatus)) {
            statusFilter.setValue(selectedStatus);
        } else {
            statusFilter.setValue("Tous");
        }
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
                    if (response instanceof Map && ((Map<?, ?>) response).containsKey("error")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = (Map<String, Object>) response;
                        showError("Erreur", "Impossible de créer l'équipement: " + responseMap.get("error"));
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
                    if (response instanceof Map && ((Map<?, ?>) response).containsKey("error")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = (Map<String, Object>) response;
                        showError("Erreur", "Impossible de modifier l'équipement: " + responseMap.get("error"));
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
     * Sélectionne un équipement par nom et ouvre sa fiche de modification
     * Méthode publique appelée depuis la recherche globale
     */
    public void selectAndViewEquipment(String equipmentName) {
        System.out.println("🔍 Recherche équipement: " + equipmentName + " dans " + equipmentData.size() + " éléments");
        
        // Attendre que les données soient chargées si nécessaire
        if (equipmentData.isEmpty()) {
            System.out.println("⏳ Données non chargées, attente...");
            // Programmer une vérification périodique
            scheduleDataCheck(equipmentName, 0);
            return;
        }
        
        Platform.runLater(() -> {
            // Rechercher l'équipement dans la liste
            boolean found = false;
            for (EquipmentItem equipment : equipmentData) {
                if (equipment.getName() != null && 
                    equipment.getName().toLowerCase().contains(equipmentName.toLowerCase())) {
                    // Sélectionner l'équipement dans la table
                    equipmentTable.getSelectionModel().select(equipment);
                    equipmentTable.scrollTo(equipment);
                    
                    System.out.println("✅ Équipement trouvé et sélectionné: " + equipment.getName());
                    
                    // Ouvrir automatiquement la fiche de modification avec délai
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(200); // Petit délai pour la sélection
                            editEquipment();
                        } catch (InterruptedException e) {
                            editEquipment();
                        }
                    });
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("❌ Équipement non trouvé: " + equipmentName);
            }
        });
    }
    
    /**
     * Vérifie périodiquement si les données sont chargées pour la sélection automatique
     */
    private void scheduleDataCheck(String equipmentName, int attempt) {
        if (attempt > 10) { // Maximum 10 tentatives (5 secondes)
            System.out.println("❌ Timeout: Équipement non trouvé après 10 tentatives: " + equipmentName);
            return;
        }
        
        Platform.runLater(() -> {
            if (!equipmentData.isEmpty()) {
                System.out.println("✅ Données chargées, nouvelle tentative de sélection");
                selectAndViewEquipment(equipmentName);
            } else {
                // Réessayer après 500ms
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        scheduleDataCheck(equipmentName, attempt + 1);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }).start();
            }
        });
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