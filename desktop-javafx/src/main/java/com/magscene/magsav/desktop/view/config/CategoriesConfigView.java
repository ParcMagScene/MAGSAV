package com.magscene.magsav.desktop.view.config;

import com.magscene.magsav.desktop.config.CategoriesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.util.List;

/**
 * Vue de configuration des catégories d'équipement
 * Permet de gérer les catégories hiérarchiques avec couleurs et icônes
 */
public class CategoriesConfigView extends VBox {
    
    private final CategoriesConfigManager configManager;
    private final ApiService apiService;
    
    // Composants UI - Gestion des catégories
    private TreeView<CategoriesConfigManager.CategoryItem> categoriesTree;
    private TextField newCategoryField;
    private TextField colorField;
    private TextField iconField;
    private ColorPicker colorPicker;
    private Button addRootBtn;
    private Button addSubBtn;
    private Button editBtn;
    private Button removeBtn;
    
    // Composants UI - Attribution équipement
    private ComboBox<String> categorySelector;
    private ListView<EquipmentItem> availableEquipmentList;
    private ListView<EquipmentItem> assignedEquipmentList;
    private Button assignEquipmentBtn;
    private Button unassignEquipmentBtn;
    
    public CategoriesConfigView(ApiService apiService) {
        this.apiService = apiService;
        this.configManager = CategoriesConfigManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }
    
    private void initializeComponents() {
        // === SECTION GESTION CATÉGORIES ===
        categoriesTree = new TreeView<>();
        categoriesTree.setPrefHeight(350);
        categoriesTree.setCellFactory(tv -> new CategoryTreeCell());
        
        newCategoryField = new TextField();
        newCategoryField.setPromptText("Nom de la nouvelle catégorie...");
        newCategoryField.setPrefWidth(200);
        
        colorField = new TextField();
        colorField.setPromptText("#FF6B35");
        colorField.setPrefWidth(100);
        
        colorPicker = new ColorPicker(Color.web("#FF6B35"));
        colorPicker.setPrefWidth(50);
        
        iconField = new TextField();
        iconField.setPromptText("lightbulb");
        iconField.setPrefWidth(150);
        
        addRootBtn = new Button("📁 Ajouter Racine");
        addRootBtn.getStyleClass().add("action-button-primary");
        
        addSubBtn = new Button("📂 Ajouter Sous-Cat");
        addSubBtn.getStyleClass().add("action-button-primary");
        addSubBtn.setDisable(true);
        
        editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("action-button-secondary");
        editBtn.setDisable(true);
        
        removeBtn = new Button("🗑️ Supprimer");
        removeBtn.getStyleClass().add("action-button-danger");
        removeBtn.setDisable(true);
        
        // === SECTION ATTRIBUTION ÉQUIPEMENT ===
        categorySelector = new ComboBox<>();
        categorySelector.setPromptText("Sélectionner une catégorie...");
        categorySelector.setPrefWidth(300);
        
        availableEquipmentList = new ListView<>();
        availableEquipmentList.setPrefHeight(200);
        availableEquipmentList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        assignedEquipmentList = new ListView<>();
        assignedEquipmentList.setPrefHeight(200);
        assignedEquipmentList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        assignEquipmentBtn = new Button("→ Attribuer");
        assignEquipmentBtn.getStyleClass().add("action-button-primary");
        assignEquipmentBtn.setDisable(true);
        
        unassignEquipmentBtn = new Button("← Retirer");
        unassignEquipmentBtn.getStyleClass().add("action-button-secondary");
        unassignEquipmentBtn.setDisable(true);
    }
    
    private void setupLayout() {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        
        // Plus de titre principal ici - déjà affiché dans l'onglet; // === SECTION 1: GESTION DES CATÉGORIES ===
        VBox categoriesSection = createCategoriesManagementSection();
        
        // === SECTION 2: ATTRIBUTION ÉQUIPEMENT ===
        VBox assignmentSection = createEquipmentAssignmentSection();
        
        // === ACTIONS GÉNÉRALES ===
        HBox globalActions = createGlobalActionsBar();
        
        this.getChildren().addAll(
            categoriesSection,
            new Separator(),
            assignmentSection,
            new Separator(),
            globalActions
        );
    }
    
    private VBox createCategoriesManagementSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label sectionTitle = new Label("🗂️ Arborescence des Catégories");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Barre d'ajout
        VBox addSection = createAddCategorySection();
        
        // Arbre avec boutons
        HBox treeSection = new HBox(15);
        
        VBox treeContainer = new VBox(5);
        treeContainer.getChildren().addAll(
            new Label("Catégories existantes :"),
            categoriesTree
        );
        
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.TOP_CENTER);
        buttonContainer.setPadding(new Insets(30, 0, 0, 0));
        buttonContainer.getChildren().addAll(
            editBtn,
            removeBtn
        );
        
        treeSection.getChildren().addAll(treeContainer, buttonContainer);
        HBox.setHgrow(treeContainer, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, addSection, treeSection);
        return section;
    }
    
    private VBox createAddCategorySection() {
        VBox addSection = new VBox(10);
        
        // Ligne 1 : Nom
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getChildren().addAll(
            new Label("Nom :"),
            newCategoryField
        );
        
        // Ligne 2 : Couleur et Icône
        HBox detailsRow = new HBox(10);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        detailsRow.getChildren().addAll(
            new Label("Couleur :"),
            colorField,
            colorPicker,
            new Label("Icône :"),
            iconField
        );
        
        // Ligne 3 : Boutons
        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);
        buttonsRow.getChildren().addAll(
            addRootBtn,
            addSubBtn
        );
        
        addSection.getChildren().addAll(nameRow, detailsRow, buttonsRow);
        return addSection;
    }
    
    private VBox createEquipmentAssignmentSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label sectionTitle = new Label("📦 Attribution des Équipements");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Sélecteur de catégorie
        HBox selectorBar = new HBox(10);
        selectorBar.setAlignment(Pos.CENTER_LEFT);
        selectorBar.getChildren().addAll(
            new Label("Catégorie :"),
            categorySelector
        );
        
        // Zone d'attribution avec deux listes
        HBox assignmentArea = new HBox(15);
        assignmentArea.setAlignment(Pos.CENTER);
        
        // Liste équipement disponible
        VBox availableBox = new VBox(5);
        availableBox.getChildren().addAll(
            new Label("Équipements non catégorisés :"),
            availableEquipmentList
        );
        
        // Boutons d'attribution
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(50, 10, 0, 10));
        buttonsBox.getChildren().addAll(assignEquipmentBtn, unassignEquipmentBtn);
        
        // Liste équipement assigné
        VBox assignedBox = new VBox(5);
        assignedBox.getChildren().addAll(
            new Label("Équipements dans cette catégorie :"),
            assignedEquipmentList
        );
        
        assignmentArea.getChildren().addAll(availableBox, buttonsBox, assignedBox);
        HBox.setHgrow(availableBox, Priority.ALWAYS);
        HBox.setHgrow(assignedBox, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, selectorBar, assignmentArea);
        return section;
    }
    
    private HBox createGlobalActionsBar() {
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER);
        
        Button resetBtn = new Button("🔄 Réinitialiser");
        resetBtn.getStyleClass().add("action-button-warning");
        resetBtn.setOnAction(e -> resetToDefaults());
        
        Button importBtn = new Button("📥 Importer");
        importBtn.getStyleClass().add("action-button-secondary");
        importBtn.setOnAction(e -> importCategories());
        
        Button exportBtn = new Button("📤 Exporter");
        exportBtn.getStyleClass().add("action-button-secondary");
        exportBtn.setOnAction(e -> exportCategories());
        
        Button saveBtn = new Button("💾 Sauvegarder");
        saveBtn.getStyleClass().add("action-button-success");
        saveBtn.setOnAction(e -> saveConfiguration());
        
        actionsBar.getChildren().addAll(resetBtn, importBtn, exportBtn, saveBtn);
        return actionsBar;
    }
    
    private void setupEventHandlers() {
        // Gestion des catégories
        categoriesTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            editBtn.setDisable(!selected);
            removeBtn.setDisable(!selected);
            addSubBtn.setDisable(!selected);
            
            if (selected) {
                CategoriesConfigManager.CategoryItem item = newVal.getValue();
                newCategoryField.setText(item.getName());
                colorField.setText(item.getColor());
                iconField.setText(item.getIcon());
                colorPicker.setValue(Color.web(item.getColor()));
            }
        });
        
        addRootBtn.setOnAction(e -> addRootCategory());
        addSubBtn.setOnAction(e -> addSubCategory());
        editBtn.setOnAction(e -> editCategory());
        removeBtn.setOnAction(e -> removeCategory());
        
        newCategoryField.setOnAction(e -> addRootCategory());
        
        // Synchronisation couleur
        colorPicker.setOnAction(e -> {
            Color color = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X", 
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
            colorField.setText(hex);
        });
        
        colorField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.startsWith("#") && newVal.length() == 7) {
                try {
                    colorPicker.setValue(Color.web(newVal));
                } catch (Exception ignored) {}
            }
        });
        
        // Attribution équipement
        categorySelector.setOnAction(e -> loadEquipmentForCategory());
        
        availableEquipmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            assignEquipmentBtn.setDisable(newVal == null));
        
        assignedEquipmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            unassignEquipmentBtn.setDisable(newVal == null));
        
        assignEquipmentBtn.setOnAction(e -> assignEquipmentToCategory());
        unassignEquipmentBtn.setOnAction(e -> unassignEquipmentFromCategory());
    }
    
    private void loadData() {
        // Charger l'arbre des catégories
        buildCategoriesTree();
        
        // Charger les catégories dans le sélecteur
        updateCategorySelector();
    }
    
    private void buildCategoriesTree() {
        TreeItem<CategoriesConfigManager.CategoryItem> root = new TreeItem<>();
        root.setExpanded(true);
        
        for (CategoriesConfigManager.CategoryItem rootCategory : configManager.getRootCategories()) {
            TreeItem<CategoriesConfigManager.CategoryItem> rootItem = new TreeItem<>(rootCategory);
            rootItem.setExpanded(true);
            
            // Ajouter les sous-catégories
            buildSubCategoriesTree(rootItem, rootCategory);
            
            root.getChildren().add(rootItem);
        }
        
        categoriesTree.setRoot(root);
        categoriesTree.setShowRoot(false);
    }
    
    private void buildSubCategoriesTree(TreeItem<CategoriesConfigManager.CategoryItem> parent, 
                                       CategoriesConfigManager.CategoryItem parentCategory) {
        for (CategoriesConfigManager.CategoryItem subCategory : parentCategory.getSubCategories()) {
            TreeItem<CategoriesConfigManager.CategoryItem> subItem = new TreeItem<>(subCategory);
            subItem.setExpanded(true);
            
            // Récursion pour les sous-sous-catégories
            buildSubCategoriesTree(subItem, subCategory);
            
            parent.getChildren().add(subItem);
        }
    }
    
    private void updateCategorySelector() {
        categorySelector.getItems().clear();
        List<CategoriesConfigManager.CategoryItem> allCategories = configManager.getAllCategories();
        for (CategoriesConfigManager.CategoryItem category : allCategories) {
            categorySelector.getItems().add(category.getFullPath());
        }
    }
    
    // === ACTIONS CATÉGORIES ===
    
    private void addRootCategory() {
        String name = newCategoryField.getText().trim();
        String color = colorField.getText().trim();
        String icon = iconField.getText().trim();
        
        if (!name.isEmpty()) {
            if (configManager.addRootCategory(name, color, icon)) {
                clearFields();
                buildCategoriesTree();
                updateCategorySelector();
                showSuccessAlert("Catégorie ajoutée", "La catégorie '" + name + "' a été ajoutée avec succès.");
            } else {
                showErrorAlert("Erreur", "La catégorie '" + name + "' existe déjà ou est invalide.");
            }
        }
    }
    
    private void addSubCategory() {
        TreeItem<CategoriesConfigManager.CategoryItem> selected = categoriesTree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String name = newCategoryField.getText().trim();
            String color = colorField.getText().trim();
            String icon = iconField.getText().trim();
            
            if (!name.isEmpty()) {
                CategoriesConfigManager.CategoryItem parent = selected.getValue();
                if (configManager.addSubCategory(parent, name, color, icon)) {
                    clearFields();
                    buildCategoriesTree();
                    updateCategorySelector();
                    showSuccessAlert("Sous-catégorie ajoutée", 
                        "La sous-catégorie '" + name + "' a été ajoutée à '" + parent.getName() + "'.");
                } else {
                    showErrorAlert("Erreur", "La catégorie '" + name + "' existe déjà ou est invalide.");
                }
            }
        }
    }
    
    private void editCategory() {
        TreeItem<CategoriesConfigManager.CategoryItem> selected = categoriesTree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String name = newCategoryField.getText().trim();
            String color = colorField.getText().trim();
            String icon = iconField.getText().trim();
            
            CategoriesConfigManager.CategoryItem category = selected.getValue();
            if (configManager.updateCategory(category, name, color, icon)) {
                buildCategoriesTree();
                updateCategorySelector();
                showSuccessAlert("Catégorie modifiée", "La catégorie a été modifiée avec succès.");
            } else {
                showErrorAlert("Erreur", "Impossible de modifier la catégorie.");
            }
        }
    }
    
    private void removeCategory() {
        TreeItem<CategoriesConfigManager.CategoryItem> selected = categoriesTree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CategoriesConfigManager.CategoryItem category = selected.getValue();
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Supprimer la catégorie '" + category.getName() + "' ?");
            alert.setContentText("Cette action supprimera la catégorie et tous ses équipements seront déplacés vers 'Non catégorisé'.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (configManager.removeCategory(category)) {
                        clearFields();
                        buildCategoriesTree();
                        updateCategorySelector();
                        showSuccessAlert("Catégorie supprimée", "La catégorie '" + category.getName() + "' a été supprimée.");
                    }
                }
            });
        }
    }
    
    private void clearFields() {
        newCategoryField.clear();
        colorField.setText("#007bff");
        iconField.clear();
        colorPicker.setValue(Color.web("#007bff"));
    }
    
    // === ACTIONS ÉQUIPEMENT ===
    
    private void loadEquipmentForCategory() {
        String selectedCategory = categorySelector.getValue();
        if (selectedCategory != null) {
            // TODO: Charger l'équipement depuis l'ApiService; // Pour l'instant, on simule avec des données de test
            loadEquipmentLists(selectedCategory);
        }
    }
    
    private void loadEquipmentLists(String category) {
        // Simulation - à remplacer par l'appel API réel
        ObservableList<EquipmentItem> allEquipment = FXCollections.observableArrayList();
        ObservableList<EquipmentItem> assignedEquipment = FXCollections.observableArrayList();
        
        // Données de test
        allEquipment.addAll(
            new EquipmentItem("1", "Projecteur LED 200W", "Éclairage"),
            new EquipmentItem("2", "Enceinte L-Acoustics", "Son"),
            new EquipmentItem("3", "Câble XLR 10m", "Accessoires"),
            new EquipmentItem("4", "Console M32", "Son")
        );
        
        availableEquipmentList.setItems(allEquipment);
        assignedEquipmentList.setItems(assignedEquipment);
    }
    
    private void assignEquipmentToCategory() {
        EquipmentItem selected = availableEquipmentList.getSelectionModel().getSelectedItem();
        String category = categorySelector.getValue();
        
        if (selected != null && category != null) {
            // TODO: Mettre à jour via l'API
            assignedEquipmentList.getItems().add(selected);
            availableEquipmentList.getItems().remove(selected);
            showSuccessAlert("Attribution effectuée", 
                selected.getName() + " a été assigné à la catégorie '" + category + "'.");
        }
    }
    
    private void unassignEquipmentFromCategory() {
        EquipmentItem selected = assignedEquipmentList.getSelectionModel().getSelectedItem();
        String category = categorySelector.getValue();
        
        if (selected != null && category != null) {
            // TODO: Mettre à jour via l'API
            availableEquipmentList.getItems().add(selected);
            assignedEquipmentList.getItems().remove(selected);
            showSuccessAlert("Attribution supprimée", 
                selected.getName() + " n'est plus dans la catégorie '" + category + "'.");
        }
    }
    
    // === ACTIONS GLOBALES ===
    
    private void resetToDefaults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser les catégories");
        alert.setHeaderText("Voulez-vous vraiment réinitialiser toutes les catégories ?");
        alert.setContentText("Cette action supprimera toutes les catégories personnalisées et restaurera les valeurs par défaut.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                configManager.resetToDefaults();
                buildCategoriesTree();
                updateCategorySelector();
                showSuccessAlert("Réinitialisation effectuée", "Les catégories ont été réinitialisées.");
            }
        });
    }
    
    private void importCategories() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer Configuration des Catégories");
        fileChooser.setInitialFileName("categories_config.json");
        
        // Filtres d'extension
        FileChooser.ExtensionFilter jsonFilter = 
            new FileChooser.ExtensionFilter("Fichiers JSON (*.json)", "*.json");
        FileChooser.ExtensionFilter allFilesFilter = 
            new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*");
        fileChooser.getExtensionFilters().addAll(jsonFilter, allFilesFilter);
        
        // Obtenir le stage parent
        javafx.stage.Stage ownerStage = (javafx.stage.Stage) this.getScene().getWindow();
        java.io.File file = fileChooser.showOpenDialog(ownerStage);
        
        if (file != null) {
            importCategoriesFromFile(file);
        }
    }
    
    private void exportCategories() {
        if (categoriesTree.getRoot() == null || categoriesTree.getRoot().getChildren().isEmpty()) {
            showWarningAlert("Export Impossible", "Aucune catégorie à exporter", 
                           "La configuration des catégories est vide. Ajoutez des catégories avant d'exporter.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter Configuration des Catégories");
        fileChooser.setInitialFileName("categories_config_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".json");
        
        // Filtres d'extension
        FileChooser.ExtensionFilter jsonFilter = 
            new FileChooser.ExtensionFilter("Fichiers JSON (*.json)", "*.json");
        FileChooser.ExtensionFilter allFilesFilter = 
            new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*");
        fileChooser.getExtensionFilters().addAll(jsonFilter, allFilesFilter);
        
        // Obtenir le stage parent
        javafx.stage.Stage ownerStage = (javafx.stage.Stage) this.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(ownerStage);
        
        if (file != null) {
            exportCategoriesToFile(file);
        }
    }

    /**
     * Importe les catégories depuis un fichier JSON
     */
    private void importCategoriesFromFile(java.io.File file) {
        try {
            // Lire le contenu du fichier
            String content = java.nio.file.Files.readString(file.toPath());
            
            // Parser le JSON manuellement (simple format)
            java.util.Map<String, Object> configData = parseJsonConfig(content);
            
            if (configData.containsKey("categories")) {
                // Confirmer l'import
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmer l'Import");
                confirmAlert.setHeaderText("Remplacer la configuration actuelle ?");
                confirmAlert.setContentText("Cette action remplacera toutes les catégories actuelles par celles du fichier.\nCette action est irréversible.");
                
                java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    
                    // Importer les données
                    @SuppressWarnings("unchecked")
                    java.util.List<java.util.Map<String, Object>> categoriesList = 
                        (java.util.List<java.util.Map<String, Object>>) configData.get("categories");
                    
                    importCategoriesFromList(categoriesList);
                    
                    // Rafraîchir l'affichage
                    loadData();
                    
                    // Sauvegarder la nouvelle configuration
                    configManager.saveConfiguration();
                    
                    showSuccessAlert("Import Réussi", 
                                   String.format("✅ %d catégories importées depuis:\n%s", 
                                               categoriesList.size(), file.getName()));
                }
            } else {
                showErrorAlert("Format Invalide", 
                             "Le fichier ne contient pas de données de catégories valides.\n" +
                             "Vérifiez que le fichier est au bon format JSON.");
            }
            
        } catch (Exception e) {
            showErrorAlert("Erreur d'Import", 
                         "Impossible d'importer le fichier:\n" + e.getMessage());
        }
    }

    /**
     * Exporte les catégories vers un fichier JSON
     */
    private void exportCategoriesToFile(java.io.File file) {
        try {
            // Collecter toutes les catégories
            java.util.List<CategoriesConfigManager.CategoryItem> allCategories = configManager.getAllCategories();
            
            // Construire le JSON manuellement
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"exportDate\": \"").append(java.time.LocalDateTime.now().toString()).append("\",\n");
            json.append("  \"version\": \"MAGSAV-3.0\",\n");
            json.append("  \"totalCategories\": ").append(allCategories.size()).append(",\n");
            json.append("  \"categories\": [\n");
            
            for (int i = 0; i < allCategories.size(); i++) {
                CategoriesConfigManager.CategoryItem category = allCategories.get(i);
                json.append("    {\n");
                json.append("      \"name\": \"").append(escapeJson(category.getName())).append("\",\n");
                json.append("      \"color\": \"").append(escapeJson(category.getColor())).append("\",\n");
                json.append("      \"icon\": \"").append(escapeJson(category.getIcon())).append("\",\n");
                json.append("      \"parentId\": null,\n"); // Simplifié pour l'instant
                json.append("      \"displayOrder\": ").append(category.getDisplayOrder()).append(",\n");
                json.append("      \"id\": \"").append(escapeJson(category.getName())).append("\"\n"); // Utilise le nom comme ID
                json.append("    }");
                
                if (i < allCategories.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ]\n");
            json.append("}");
            
            // Écrire dans le fichier
            java.nio.file.Files.writeString(file.toPath(), json.toString());
            
            showSuccessAlert("Export Réussi", 
                           String.format("✅ %d catégories exportées vers:\n%s", 
                                       allCategories.size(), file.getName()));
            
        } catch (Exception e) {
            showErrorAlert("Erreur d'Export", 
                         "Impossible d'exporter vers le fichier:\n" + e.getMessage());
        }
    }

    /**
     * Parse un JSON simple manuellement (pour éviter les dépendances)
     */
    private java.util.Map<String, Object> parseJsonConfig(String json) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        // Parser basique pour notre format spécifique
        if (json.contains("\"categories\"")) {
            java.util.List<java.util.Map<String, Object>> categories = new java.util.ArrayList<>();
            
            // Extraire les objets catégories
            String[] parts = json.split("\\{");
            for (String part : parts) {
                if (part.contains("\"name\"")) {
                    java.util.Map<String, Object> category = new java.util.HashMap<>();
                    
                    // Extraire les champs
                    extractJsonField(part, "name", category);
                    extractJsonField(part, "color", category);
                    extractJsonField(part, "icon", category);
                    extractJsonField(part, "parentId", category);
                    extractJsonField(part, "id", category);
                    extractJsonNumberField(part, "displayOrder", category);
                    
                    if (!category.isEmpty()) {
                        categories.add(category);
                    }
                }
            }
            
            result.put("categories", categories);
        }
        
        return result;
    }

    /**
     * Extrait un champ JSON string
     */
    private void extractJsonField(String json, String fieldName, java.util.Map<String, Object> target) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            target.put(fieldName, m.group(1));
        }
    }

    /**
     * Extrait un champ JSON numérique
     */
    private void extractJsonNumberField(String json, String fieldName, java.util.Map<String, Object> target) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            target.put(fieldName, Integer.parseInt(m.group(1)));
        }
    }

    /**
     * Importe les catégories depuis une liste de Maps
     */
    private void importCategoriesFromList(java.util.List<java.util.Map<String, Object>> categoriesList) {
        // Supprimer toutes les catégories existantes
        clearAllCategories();
        
        // Ajouter chaque catégorie
        for (java.util.Map<String, Object> categoryData : categoriesList) {
            String name = (String) categoryData.get("name");
            String color = (String) categoryData.get("color");
            String icon = (String) categoryData.get("icon");
            String parentId = (String) categoryData.get("parentId");
            
            if (name != null && !name.trim().isEmpty()) {
                // Pour l'instant, on ajoute tout comme catégories racines; // TODO: Gérer la hiérarchie parent/enfant dans une version future
                configManager.addRootCategory(name.trim(), color != null ? color : "#3498db", icon != null ? icon : "📦");
            }
        }
    }

    /**
     * Supprime toutes les catégories existantes
     */
    private void clearAllCategories() {
        java.util.List<CategoriesConfigManager.CategoryItem> allCategories = 
            new java.util.ArrayList<>(configManager.getAllCategories());
        
        // Supprimer toutes les catégories racines (cela supprime aussi leurs sous-catégories)
        for (CategoriesConfigManager.CategoryItem category : allCategories) {
            if (isRootCategory(category)) {
                configManager.removeCategory(category);
            }
        }
    }

    /**
     * Vérifie si une catégorie est une catégorie racine
     */
    private boolean isRootCategory(CategoriesConfigManager.CategoryItem category) {
        return configManager.getRootCategories().contains(category);
    }

    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
    
    private void saveConfiguration() {
        configManager.saveConfiguration();
        showSuccessAlert("Configuration sauvegardée", "Toutes les modifications ont été sauvegardées.");
    }
    
    // === CELLULE D'ARBRE PERSONNALISÉE ===
    
    private class CategoryTreeCell extends TreeCell<CategoriesConfigManager.CategoryItem> {
        @Override
        protected void updateItem(CategoriesConfigManager.CategoryItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox content = new HBox(5);
                content.setAlignment(Pos.CENTER_LEFT);
                
                // Rectangle coloré
                Rectangle colorRect = new Rectangle(12, 12);
                colorRect.setFill(Color.web(item.getColor()));
                colorRect.setStroke(Color.GRAY);
                
                // Nom de la catégorie
                Label nameLabel = new Label(item.getName());
                nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
                
                // Icône (simulée par un emoji)
                Label iconLabel = new Label("📁");
                
                content.getChildren().addAll(colorRect, iconLabel, nameLabel);
                setGraphic(content);
                setText(null);
            }
        }
    }
    
    // === UTILITAIRES ===
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarningAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // === CLASSE INTERNE ===
    
    /**
     * Item représentant un équipement dans les listes
     */
    public static class EquipmentItem {
        private final String id;
        private final String name;
        private final String category;
        
        public EquipmentItem(String id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return name + " (" + category + ")";
        }
    }
}
