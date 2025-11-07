package com.magscene.magsav.desktop.view.inventory;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// import com.magscene.magsav.desktop.model.Equipment; // Supprimé après refactoring
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.AlertUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Interface avancée de scanner QR codes pour inventaire matériel
 * Permet scan rapide, identification automatique, et mise à jour inventaire
 */
public class QRCodeScannerView extends VBox {
    
    private final ApiService apiService;
    private final ObservableList<InventoryItem> scannedItems;
    private final TableView<InventoryItem> inventoryTable;
    
    // Composants de scanning
    private final TextField manualCodeField;
    private final Button scanButton;
    private final Button cameraButton;
    private final TextArea scanResultArea;
    
    // Statistiques de session
    private int totalScanned = 0;
    private int newItemsFound = 0;
    private int updatedItems = 0;
    private int errorCount = 0;
    
    // Labels de statistiques
    private final Label statsLabel;
    private final Label sessionTimeLabel;
    private final LocalDateTime sessionStart;
    
    // Filtres et recherche rapide
    private final TextField quickSearchField;
    private final ComboBox<String> categoryFilter;
    private final ComboBox<String> statusFilter;
    
    public QRCodeScannerView() {
        this.apiService = new ApiService();
        this.scannedItems = FXCollections.observableArrayList();
        this.sessionStart = LocalDateTime.now();
        
        // Configuration principale
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Initialisation des composants
        this.manualCodeField = new TextField();
        this.scanButton = new Button("📷 Scanner QR Code");
        this.cameraButton = new Button("📹 Activer Caméra");
        this.scanResultArea = new TextArea();
        this.inventoryTable = createInventoryTable();
        this.statsLabel = new Label();
        this.sessionTimeLabel = new Label();
        this.quickSearchField = new TextField();
        this.categoryFilter = new ComboBox<>();
        this.statusFilter = new ComboBox<>();
        
        // Construction de l'interface
        setupScannerInterface();
        setupScannerEventHandlers();
        
        // Chargement initial
        loadInventoryItems();
        startSessionTimer();
    }
    
    private void setupScannerInterface() {
        // En-tête avec scanner principal
        HBox headerBox = createScannerHeaderSection();
        
        // Section de scanning et saisie
        VBox scanningSection = createScanningSection();
        
        // Section principale avec résultats et inventaire
        HBox mainSection = createScannerMainSection();
        
        // Barre d'actions et statistiques
        HBox actionsBar = createScannerActionsBar();
        
        this.getChildren().addAll(headerBox, scanningSection, mainSection, actionsBar);
    }
    
    private HBox createScannerHeaderSection() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        // Titre avec icône de scanner
        Label titleLabel = new Label("📱 Scanner QR Codes - Inventaire Matériel");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Informations de session
        VBox sessionBox = new VBox(3);
        sessionBox.setAlignment(Pos.CENTER_RIGHT);
        sessionBox.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 8px 12px; -fx-background-radius: 6px;");
        
        statsLabel.setText(String.format("📊 Scannés: %d | Nouveaux: %d | Erreurs: %d", totalScanned, newItemsFound, errorCount));
        statsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2c3e50;");
        
        sessionTimeLabel.setText("⏱️ Session: 00:00:00");
        sessionTimeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        sessionBox.getChildren().addAll(statsLabel, sessionTimeLabel);
        
        headerBox.getChildren().addAll(titleLabel, spacer, sessionBox);
        return headerBox;
    }
    
    private VBox createScanningSection() {
        VBox scanningSection = new VBox(15);
        scanningSection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 8, 0, 0, 2);");
        
        Label scanningTitle = new Label("📷 Zone de Scanning");
        scanningTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        // Zone de saisie manuelle et scanning
        HBox scanInputBox = createScanInputBox();
        
        // Résultats de scan en temps réel
        VBox scanResultsBox = createScanResultsBox();
        
        // Instructions d'utilisation
        VBox instructionsBox = createInstructionsBox();
        
        scanningSection.getChildren().addAll(scanningTitle, scanInputBox, scanResultsBox, instructionsBox);
        return scanningSection;
    }
    
    private HBox createScanInputBox() {
        HBox scanInputBox = new HBox(15);
        scanInputBox.setAlignment(Pos.CENTER_LEFT);
        
        // Saisie manuelle du code
        VBox manualInputBox = new VBox(5);
        Label manualLabel = new Label("⌨️ Saisie manuelle du code :");
        manualLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
        
        manualCodeField.setPromptText("Scannez ou tapez le code QR/barres...");
        manualCodeField.setPrefWidth(300);
        manualCodeField.setStyle("-fx-font-size: 14px; -fx-background-radius: 6px; -fx-border-color: #27ae60; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-padding: 8px;");
        
        manualInputBox.getChildren().addAll(manualLabel, manualCodeField);
        
        // Boutons de scanning
        VBox buttonsBox = new VBox(8);
        
        scanButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 10px 15px; -fx-font-size: 12px;");
        scanButton.setPrefWidth(150);
        
        cameraButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-font-size: 11px;");
        cameraButton.setPrefWidth(150);
        
        Button batchScanBtn = new Button("📋 Scan en lot");
        batchScanBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px; -fx-font-size: 11px;");
        batchScanBtn.setPrefWidth(150);
        
        buttonsBox.getChildren().addAll(scanButton, cameraButton, batchScanBtn);
        
        // Indicateur de statut de caméra
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("📡 État de la caméra :");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        Label cameraStatusLabel = new Label("🔴 Déconnectée");
        cameraStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        Button testCameraBtn = new Button("🔍 Test caméra");
        testCameraBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        statusBox.getChildren().addAll(statusLabel, cameraStatusLabel, testCameraBtn);
        
        scanInputBox.getChildren().addAll(manualInputBox, buttonsBox, statusBox);
        return scanInputBox;
    }
    
    private VBox createScanResultsBox() {
        VBox resultsBox = new VBox(8);
        
        Label resultsLabel = new Label("📄 Résultats de scan :");
        resultsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        scanResultArea.setPrefHeight(120);
        scanResultArea.setEditable(false);
        scanResultArea.setWrapText(true);
        scanResultArea.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-color: #27ae60; -fx-border-radius: 4px; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-padding: 8px;");
        scanResultArea.setPromptText("Les résultats de scan apparaîtront ici en temps réel...");
        
        // Boutons d'action sur les résultats
        HBox resultsActionsBox = new HBox(8);
        resultsActionsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button clearResultsBtn = new Button("🗑️ Vider");
        clearResultsBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 4px 8px;");
        clearResultsBtn.setOnAction(e -> scanResultArea.clear());
        
        Button copyResultsBtn = new Button("📋 Copier");
        copyResultsBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        Button exportResultsBtn = new Button("💾 Exporter");
        exportResultsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        resultsActionsBox.getChildren().addAll(clearResultsBtn, copyResultsBtn, exportResultsBtn);
        
        resultsBox.getChildren().addAll(resultsLabel, scanResultArea, resultsActionsBox);
        return resultsBox;
    }
    
    private VBox createInstructionsBox() {
        VBox instructionsBox = new VBox(5);
        instructionsBox.setStyle("-fx-background-color: #e8f8f5; -fx-padding: 10px; -fx-background-radius: 6px;");
        
        Label instructionsTitle = new Label("💡 Instructions d'utilisation");
        instructionsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #16a085;");
        
        String[] instructions = {
            "• Placez le QR code devant la caméra ou utilisez la saisie manuelle",
            "• Les codes sont automatiquement validés et recherchés dans l'inventaire", 
            "• Les nouveaux équipements sont marqués pour ajout à l'inventaire",
            "• Utilisez le scan en lot pour traiter plusieurs équipements rapidement",
            "• Les erreurs de scan sont affichées avec suggestions de correction"
        };
        
        for (String instruction : instructions) {
            Label instrLabel = new Label(instruction);
            instrLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #2d8659;");
            instructionsBox.getChildren().add(instrLabel);
        }
        
        return instructionsBox;
    }
    
    private HBox createScannerMainSection() {
        HBox mainSection = new HBox(15);
        mainSection.setAlignment(Pos.TOP_LEFT);
        
        // Tableau d'inventaire (70% de la largeur)
        VBox inventorySection = createInventoryTableSection();
        
        // Panneau de détails d'équipement (30% de la largeur)
        VBox detailsSection = createEquipmentDetailsSection();
        
        HBox.setHgrow(inventorySection, Priority.ALWAYS);
        detailsSection.setPrefWidth(350);
        
        mainSection.getChildren().addAll(inventorySection, detailsSection);
        return mainSection;
    }
    
    private VBox createInventoryTableSection() {
        VBox inventorySection = new VBox(10);
        inventorySection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // En-tête avec recherche rapide
        HBox tableHeaderBox = new HBox(15);
        tableHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("📦 Inventaire Scanné");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // Recherche rapide
        quickSearchField.setPromptText("Recherche rapide...");
        quickSearchField.setPrefWidth(200);
        quickSearchField.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC; -fx-border-color: #7DD3FC; -fx-border-radius: 4;");
        
        tableHeaderBox.getChildren().addAll(tableTitle, headerSpacer, quickSearchField);
        
        // Filtres rapides
        HBox filtersBox = createInventoryFiltersBox();
        
        // Tableau d'inventaire
        inventoryTable.setPrefHeight(350);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        inventorySection.getChildren().addAll(tableHeaderBox, filtersBox, inventoryTable);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        
        return inventorySection;
    }
    
    private HBox createInventoryFiltersBox() {
        HBox filtersBox = new HBox(10);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(5, 0, 10, 0));
        
        // Configuration des filtres
        categoryFilter.getItems().addAll("Toutes catégories", "Audio", "Vidéo", "Éclairage", "Mécanique", "Réseau", "Accessoires");
        categoryFilter.setValue("Toutes catégories");
        categoryFilter.setPrefWidth(130);
        categoryFilter.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        
        statusFilter.getItems().addAll("Tous statuts", "Disponible", "En service", "En réparation", "Hors service", "Nouveau");
        statusFilter.setValue("Tous statuts");
        statusFilter.setPrefWidth(120);
        statusFilter.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");
        
        Button resetFiltersBtn = new Button("🔄 Reset");
        resetFiltersBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 4px 8px;");
        resetFiltersBtn.setOnAction(e -> resetFilters());
        
        filtersBox.getChildren().addAll(
            new Label("Catégorie:"), categoryFilter,
            new Label("Statut:"), statusFilter,
            resetFiltersBtn
        );
        
        return filtersBox;
    }
    
    private VBox createEquipmentDetailsSection() {
        VBox detailsSection = new VBox(15);
        detailsSection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 6, 0, 0, 2);");
        
        Label detailsTitle = new Label("🔍 Détails Équipement");
        detailsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        // Zone d'image/QR code
        VBox imageBox = new VBox(5);
        Label imageLabel = new Label("📸 Photo/QR Code :");
        imageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        ImageView equipmentImageView = new ImageView();
        equipmentImageView.setFitWidth(150);
        equipmentImageView.setFitHeight(100);
        equipmentImageView.setPreserveRatio(true);
        equipmentImageView.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        imageBox.getChildren().addAll(imageLabel, equipmentImageView);
        
        // Informations détaillées
        TextArea equipmentDetailsArea = new TextArea();
        equipmentDetailsArea.setPrefHeight(180);
        equipmentDetailsArea.setEditable(false);
        equipmentDetailsArea.setWrapText(true);
        equipmentDetailsArea.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-color: #27ae60; -fx-border-radius: 4px; -fx-font-family: 'Segoe UI'; -fx-font-size: 11px;");
        equipmentDetailsArea.setPromptText("Sélectionnez un équipement dans le tableau pour voir les détails...");
        
        // Actions sur l'équipement sélectionné
        VBox actionsBox = new VBox(5);
        
        Button editEquipmentBtn = new Button("✏️ Modifier");
        editEquipmentBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px; -fx-padding: 6px 12px;");
        editEquipmentBtn.setPrefWidth(120);
        
        Button printQRBtn = new Button("🖨️ Imprimer QR");
        printQRBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px; -fx-padding: 6px 12px;");
        printQRBtn.setPrefWidth(120);
        
        Button moveEquipmentBtn = new Button("📦 Déplacer");
        moveEquipmentBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px; -fx-padding: 6px 12px;");
        moveEquipmentBtn.setPrefWidth(120);
        
        actionsBox.getChildren().addAll(editEquipmentBtn, printQRBtn, moveEquipmentBtn);
        
        detailsSection.getChildren().addAll(detailsTitle, imageBox, equipmentDetailsArea, actionsBox);
        VBox.setVgrow(equipmentDetailsArea, Priority.ALWAYS);
        
        return detailsSection;
    }
    
    private HBox createScannerActionsBar() {
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(15, 0, 0, 0));
        
        Button newInventoryBtn = new Button("➕ Ajouter équipement");
        newInventoryBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        
        Button generateQRBtn = new Button("📱 Générer QR Code");
        generateQRBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        
        Button exportInventoryBtn = new Button("📊 Exporter inventaire");
        exportInventoryBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        
        Button syncInventoryBtn = new Button("🔄 Synchroniser");
        syncInventoryBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        syncInventoryBtn.setOnAction(e -> syncInventoryWithBackend());
        
        Button resetSessionBtn = new Button("🗑️ Nouvelle session");
        resetSessionBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        resetSessionBtn.setOnAction(e -> resetScanningSession());
        
        actionsBar.getChildren().addAll(newInventoryBtn, generateQRBtn, exportInventoryBtn, syncInventoryBtn, resetSessionBtn);
        return actionsBar;
    }
    
    private TableView<InventoryItem> createInventoryTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(scannedItems);
        
        // Colonne Code avec indicateur de type
        TableColumn<InventoryItem, String> codeCol = new TableColumn<>("Code");
        codeCol.setPrefWidth(100);
        codeCol.setCellValueFactory(data -> {
            InventoryItem item = data.getValue();
            String typeIcon = getCodeTypeIcon(item.getCodeType());
            return new javafx.beans.property.SimpleStringProperty(typeIcon + " " + item.getCode());
        });
        
        // Colonne Nom/Modèle
        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Équipement");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        
        // Colonne Catégorie
        TableColumn<InventoryItem, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setPrefWidth(100);
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        
        // Colonne Statut avec couleur
        TableColumn<InventoryItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            String icon = getStatusIcon(status);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + status);
        });
        
        // Colonne Localisation
        TableColumn<InventoryItem, String> locationCol = new TableColumn<>("Lieu");
        locationCol.setPrefWidth(120);
        locationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getLocation() != null ? data.getValue().getLocation() : "Non défini"));
        
        // Colonne Dernière mise à jour
        TableColumn<InventoryItem, String> updateCol = new TableColumn<>("Mis à jour");
        updateCol.setPrefWidth(90);
        updateCol.setCellValueFactory(data -> {
            LocalDateTime lastUpdate = data.getValue().getLastUpdate();
            String formatted = lastUpdate.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        
        // Colonne Action de scan
        TableColumn<InventoryItem, String> scanActionCol = new TableColumn<>("Action");
        scanActionCol.setPrefWidth(80);
        scanActionCol.setCellValueFactory(data -> {
            String action = data.getValue().getScanAction();
            String icon = getScanActionIcon(action);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + action);
        });
        
        table.getColumns().addAll(codeCol, nameCol, categoryCol, statusCol, locationCol, updateCol, scanActionCol);
        
        // Style conditionnel des lignes
        table.setRowFactory(tv -> {
            TableRow<InventoryItem> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String backgroundColor = getInventoryRowBackgroundColor(newItem.getScanAction(), newItem.getStatus());
                    row.setStyle(backgroundColor + "; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void setupScannerEventHandlers() {
        // Gestionnaire de saisie manuelle
        manualCodeField.setOnAction(e -> processScannedCode(manualCodeField.getText()));
        
        // Gestionnaire du bouton de scan
        scanButton.setOnAction(e -> processScannedCode(manualCodeField.getText()));
        
        // Gestionnaire de caméra
        cameraButton.setOnAction(e -> activateCamera());
        
        // Gestionnaires de filtres
        quickSearchField.textProperty().addListener((obs, oldText, newText) -> applyInventoryFilters());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyInventoryFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyInventoryFilters());
        
        // Gestionnaire de sélection dans le tableau
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayEquipmentDetails(newSelection);
            }
        });
    }
    
    private void processScannedCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            AlertUtil.showWarning("Code Vide", "Veuillez saisir ou scanner un code QR/barres");
            return;
        }
        
        // Validation du format du code
        if (!isValidCode(code)) {
            appendScanResult("❌ ERREUR: Code invalide '" + code + "'");
            errorCount++;
            updateStatistics();
            return;
        }
        
        // Traitement asynchrone du code scanné
        Task<InventoryItem> processTask = new Task<InventoryItem>() {
            @Override
            protected InventoryItem call() throws Exception {
                // Recherche dans l'inventaire existant (simulation)
                // CompletableFuture<List<Equipment>> searchFuture = apiService.getAllEquipment();
                // List<Equipment> allEquipment = searchFuture.get();
                
                // Simulation de recherche d'équipement
                boolean equipmentFound = Math.random() > 0.7; // 30% chance de trouver l'équipement
                
                if (equipmentFound) {
                    // Équipement trouvé - mise à jour
                    return new InventoryItem(code, "Projecteur LED XR-300", "Éclairage", 
                                           "Disponible", "Stockage principal", LocalDateTime.now(), 
                                           "Trouvé", "QR Code");
                } else {
                    // Nouvel équipement - marquer pour ajout
                    return new InventoryItem(code, "Équipement non identifié", "À classifier", 
                                           "Nouveau", "À localiser", LocalDateTime.now(), 
                                           "Nouveau", detectCodeType(code));
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    InventoryItem item = getValue();
                    
                    // Ajouter ou mettre à jour dans la liste
                    boolean found = false;
                    for (int i = 0; i < scannedItems.size(); i++) {
                        if (scannedItems.get(i).getCode().equals(code)) {
                            scannedItems.set(i, item);
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        scannedItems.add(0, item); // Ajouter en haut de liste
                        if (item.getScanAction().equals("Nouveau")) {
                            newItemsFound++;
                        }
                    } else {
                        updatedItems++;
                    }
                    
                    totalScanned++;
                    updateStatistics();
                    
                    // Afficher le résultat
                    String resultMessage = String.format("✅ %s: %s (%s)", 
                        item.getScanAction(), item.getName(), item.getCategory());
                    appendScanResult(resultMessage);
                    
                    // Vider le champ pour le prochain scan
                    manualCodeField.clear();
                    manualCodeField.requestFocus();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    appendScanResult("❌ ERREUR: Impossible de traiter le code '" + code + "'");
                    errorCount++;
                    updateStatistics();
                });
            }
        };
        
        Thread processThread = new Thread(processTask);
        processThread.setDaemon(true);
        processThread.start();
    }
    
    private boolean isValidCode(String code) {
        // Validation basique des formats de codes courants
        if (code.length() < 3 || code.length() > 50) {
            return false;
        }
        
        // Patterns courants pour QR codes et codes-barres
        Pattern qrPattern = Pattern.compile("^[A-Z0-9\\-_]+$", Pattern.CASE_INSENSITIVE);
        Pattern upcPattern = Pattern.compile("^\\d{8,14}$");
        Pattern customPattern = Pattern.compile("^MAG-[A-Z0-9]{4,8}$", Pattern.CASE_INSENSITIVE);
        
        return qrPattern.matcher(code).matches() || 
               upcPattern.matcher(code).matches() || 
               customPattern.matcher(code).matches();
    }
    
    private String detectCodeType(String code) {
        if (code.matches("^\\d{8,14}$")) {
            return "Code-barres";
        } else if (code.toUpperCase().startsWith("MAG-")) {
            return "QR MagScène";
        } else {
            return "QR Code";
        }
    }
    
    private void appendScanResult(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String resultLine = String.format("[%s] %s%n", timestamp, message);
        scanResultArea.appendText(resultLine);
        
        // Auto-scroll vers le bas
        scanResultArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private void updateStatistics() {
        statsLabel.setText(String.format("📊 Scannés: %d | Nouveaux: %d | Mis à jour: %d | Erreurs: %d", 
                                        totalScanned, newItemsFound, updatedItems, errorCount));
    }
    
    private void startSessionTimer() {
        // Timer pour afficher la durée de session
        Task<Void> timerTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    Platform.runLater(() -> {
                        long secondsElapsed = ChronoUnit.SECONDS.between(sessionStart, LocalDateTime.now());
                        long hours = secondsElapsed / 3600;
                        long minutes = (secondsElapsed % 3600) / 60;
                        long seconds = secondsElapsed % 60;
                        
                        sessionTimeLabel.setText(String.format("⏱️ Session: %02d:%02d:%02d", hours, minutes, seconds));
                    });
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        
        Thread timerThread = new Thread(timerTask);
        timerThread.setDaemon(true);
        timerThread.start();
    }
    
    private void activateCamera() {
        // Simulation de l'activation de caméra
        AlertUtil.showInfo("Caméra", 
            "Fonctionnalité de caméra en développement.\n\n" +
            "Intégration prévue :\n" +
            "• Détection automatique QR codes\n" +
            "• Support caméras USB et webcam\n" +
            "• Scan en temps réel\n" +
            "• Multi-codes simultanés");
    }
    
    private void applyInventoryFilters() {
        // Implémentation du filtrage en temps réel
        String searchText = quickSearchField.getText().toLowerCase();
        String categoryValue = categoryFilter.getValue();
        String statusValue = statusFilter.getValue();
        
        // Logique de filtrage (à implémenter)
    }
    
    private void resetFilters() {
        quickSearchField.clear();
        categoryFilter.setValue("Toutes catégories");
        statusFilter.setValue("Tous statuts");
        applyInventoryFilters();
    }
    
    private void displayEquipmentDetails(InventoryItem item) {
        // Afficher les détails de l'équipement sélectionné
        // Cette méthode pourrait afficher des informations détaillées dans le panneau droit
    }
    
    private void syncInventoryWithBackend() {
        // Synchronisation avec le backend
        AlertUtil.showInfo("Synchronisation", 
            String.format("Synchronisation en cours...\n\n" +
            "• %d équipements à synchroniser\n" +
            "• %d nouveaux équipements à ajouter\n" +
            "• %d mises à jour de statut\n\n" +
            "Cette opération peut prendre quelques minutes.", 
            scannedItems.size(), newItemsFound, updatedItems));
    }
    
    private void resetScanningSession() {
        // Réinitialiser la session de scan
        scannedItems.clear();
        scanResultArea.clear();
        manualCodeField.clear();
        totalScanned = 0;
        newItemsFound = 0;
        updatedItems = 0;
        errorCount = 0;
        updateStatistics();
        
        AlertUtil.showInfo("Nouvelle Session", "Session de scan réinitialisée avec succès !");
    }
    
    private void loadInventoryItems() {
        // Charger les équipements déjà scannés ou en cours
        // Cette méthode pourrait charger les données depuis une session précédente
    }
    
    // Méthodes utilitaires pour les icônes
    private String getCodeTypeIcon(String type) {
        switch (type.toLowerCase()) {
            case "qr code": return "📱";
            case "code-barres": return "📊";
            case "qr magscène": return "🏷️";
            default: return "🔍";
        }
    }
    
    private String getStatusIcon(String status) {
        switch (status.toLowerCase()) {
            case "disponible": return "🟢";
            case "en service": return "🟡";
            case "en réparation": return "🔧";
            case "hors service": return "🔴";
            case "nouveau": return "🆕";
            default: return "⚪";
        }
    }
    
    private String getScanActionIcon(String action) {
        switch (action.toLowerCase()) {
            case "trouvé": return "✅";
            case "nouveau": return "🆕";
            case "mis à jour": return "🔄";
            case "erreur": return "❌";
            default: return "❓";
        }
    }
    
    private String getInventoryRowBackgroundColor(String action, String status) {
        if (action.equals("Nouveau")) {
            return "-fx-background-color: #e8f5e8";
        } else if (status.equals("En réparation")) {
            return "-fx-background-color: #fff3cd";
        } else if (status.equals("Hors service")) {
            return "-fx-background-color: #f8d7da";
        }
        return "-fx-background-color: white";
    }
    
    /**
     * Classe interne pour représenter un élément d'inventaire scanné
     */
    public static class InventoryItem {
        private String code;
        private String name;
        private String category;
        private String status;
        private String location;
        private LocalDateTime lastUpdate;
        private String scanAction;
        private String codeType;
        
        public InventoryItem(String code, String name, String category, String status, 
                           String location, LocalDateTime lastUpdate, String scanAction, String codeType) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.status = status;
            this.location = location;
            this.lastUpdate = lastUpdate;
            this.scanAction = scanAction;
            this.codeType = codeType;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getLocation() { return location; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public String getScanAction() { return scanAction; }
        public String getCodeType() { return codeType; }
        
        // Setters
        public void setCode(String code) { this.code = code; }
        public void setName(String name) { this.name = name; }
        public void setCategory(String category) { this.category = category; }
        public void setStatus(String status) { this.status = status; }
        public void setLocation(String location) { this.location = location; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
        public void setScanAction(String scanAction) { this.scanAction = scanAction; }
        public void setCodeType(String codeType) { this.codeType = codeType; }
    }
}