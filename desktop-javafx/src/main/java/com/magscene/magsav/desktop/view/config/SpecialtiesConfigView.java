package com.magscene.magsav.desktop.view.config;

import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Vue de configuration des spécialités personnel
 * Permet de gérer les spécialités disponibles et d'attribuer des personnels
 */
public class SpecialtiesConfigView extends VBox {
    
    private final SpecialtiesConfigManager configManager;
    @SuppressWarnings("unused")
    private final ApiService apiService;
    
    // Composants UI - Gestion des spécialités
    private TableView<String> specialtiesTable;
    private TextField newSpecialtyField;
    private Button addSpecialtyBtn;
    private Button removeSpecialtyBtn;
    private Button editSpecialtyBtn;
    
    // Composants UI - Attribution du personnel
    private ComboBox<String> specialtySelector;
    private ListView<PersonnelItem> availablePersonnelList;
    private ListView<PersonnelItem> assignedPersonnelList;
    private Button assignBtn;
    private Button unassignBtn;
    
    public SpecialtiesConfigView(ApiService apiService) {
        this.apiService = apiService;
        this.configManager = SpecialtiesConfigManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }
    
    private void initializeComponents() {
        // === SECTION GESTION SPÉCIALITÉS ===
        specialtiesTable = new TableView<>();
        specialtiesTable.setPrefHeight(300);
        
        TableColumn<String, String> nameColumn = new TableColumn<>("Spécialité");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        nameColumn.setPrefWidth(200);
        
        specialtiesTable.getColumns().add(nameColumn);
        
        newSpecialtyField = new TextField();
        newSpecialtyField.setPromptText("Nouvelle spécialité...");
        newSpecialtyField.setPrefWidth(200);
        
        addSpecialtyBtn = new Button("➕ Ajouter");
        addSpecialtyBtn.getStyleClass().add("action-button-primary");
        
        removeSpecialtyBtn = new Button("❌ Supprimer");
        removeSpecialtyBtn.getStyleClass().add("action-button-danger");
        removeSpecialtyBtn.setDisable(true);
        
        editSpecialtyBtn = new Button("✏️ Modifier");
        editSpecialtyBtn.getStyleClass().add("action-button-secondary");
        editSpecialtyBtn.setDisable(true);
        
        // === SECTION ATTRIBUTION PERSONNEL ===
        specialtySelector = new ComboBox<>();
        specialtySelector.setPromptText("Sélectionner une spécialité...");
        specialtySelector.setPrefWidth(250);
        
        availablePersonnelList = new ListView<>();
        availablePersonnelList.setPrefHeight(200);
        availablePersonnelList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        assignedPersonnelList = new ListView<>();
        assignedPersonnelList.setPrefHeight(200);
        assignedPersonnelList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        assignBtn = new Button("→ Attribuer");
        assignBtn.getStyleClass().add("action-button-primary");
        assignBtn.setDisable(true);
        
        unassignBtn = new Button("← Retirer");
        unassignBtn.getStyleClass().add("action-button-secondary");
        unassignBtn.setDisable(true);
    }
    
    private void setupLayout() {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        
        // Plus de titre principal ici - géré par l'onglet; // === SECTION 1: GESTION DES SPÉCIALITÉS ===
        VBox specialtiesSection = createSpecialtiesManagementSection();
        
        // === SECTION 2: ATTRIBUTION PERSONNEL ===
        VBox assignmentSection = createPersonnelAssignmentSection();
        
        // === ACTIONS GÉNÉRALES ===
        HBox globalActions = createGlobalActionsBar();
        
        this.getChildren().addAll(
            new Separator(),
            specialtiesSection,
            new Separator(),
            assignmentSection,
            new Separator(),
            globalActions
        );
    }
    
    private VBox createSpecialtiesManagementSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label sectionTitle = new Label("📋 Gestion des Spécialités");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Barre d'ajout
        HBox addBar = new HBox(10);
        addBar.setAlignment(Pos.CENTER_LEFT);
        addBar.getChildren().addAll(
            new Label("Nouvelle spécialité :"),
            newSpecialtyField,
            addSpecialtyBtn
        );
        
        // Tableau avec boutons
        HBox tableSection = new HBox(15);
        
        VBox tableContainer = new VBox(5);
        tableContainer.getChildren().addAll(
            new Label("Spécialités disponibles :"),
            specialtiesTable
        );
        
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.TOP_CENTER);
        buttonContainer.setPadding(new Insets(30, 0, 0, 0));
        buttonContainer.getChildren().addAll(
            editSpecialtyBtn,
            removeSpecialtyBtn
        );
        
        tableSection.getChildren().addAll(tableContainer, buttonContainer);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, addBar, tableSection);
        return section;
    }
    
    private VBox createPersonnelAssignmentSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label sectionTitle = new Label("👥 Attribution des Spécialités");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Sélecteur de spécialité
        HBox selectorBar = new HBox(10);
        selectorBar.setAlignment(Pos.CENTER_LEFT);
        selectorBar.getChildren().addAll(
            new Label("Spécialité :"),
            specialtySelector
        );
        
        // Zone d'attribution avec deux listes
        HBox assignmentArea = new HBox(15);
        assignmentArea.setAlignment(Pos.CENTER);
        
        // Liste personnel disponible
        VBox availableBox = new VBox(5);
        availableBox.getChildren().addAll(
            new Label("Personnel disponible :"),
            availablePersonnelList
        );
        
        // Boutons d'attribution
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(50, 10, 0, 10));
        buttonsBox.getChildren().addAll(assignBtn, unassignBtn);
        
        // Liste personnel assigné
        VBox assignedBox = new VBox(5);
        assignedBox.getChildren().addAll(
            new Label("Personnel avec cette spécialité :"),
            assignedPersonnelList
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
        importBtn.setOnAction(e -> importSpecialties());
        
        Button exportBtn = new Button("📤 Exporter");
        exportBtn.getStyleClass().add("action-button-secondary");
        exportBtn.setOnAction(e -> exportSpecialties());
        
        Button saveBtn = new Button("💾 Sauvegarder");
        saveBtn.getStyleClass().add("action-button-success");
        saveBtn.setOnAction(e -> saveConfiguration());
        
        actionsBar.getChildren().addAll(resetBtn, importBtn, exportBtn, saveBtn);
        return actionsBar;
    }
    
    private void setupEventHandlers() {
        // Gestion des spécialités
        specialtiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            removeSpecialtyBtn.setDisable(!selected);
            editSpecialtyBtn.setDisable(!selected);
        });
        
        addSpecialtyBtn.setOnAction(e -> addSpecialty());
        removeSpecialtyBtn.setOnAction(e -> removeSpecialty());
        editSpecialtyBtn.setOnAction(e -> editSpecialty());
        
        newSpecialtyField.setOnAction(e -> addSpecialty());
        
        // Attribution personnel
        specialtySelector.setOnAction(e -> loadPersonnelForSpecialty());
        
        availablePersonnelList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            assignBtn.setDisable(newVal == null));
        
        assignedPersonnelList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            unassignBtn.setDisable(newVal == null));
        
        assignBtn.setOnAction(e -> assignPersonnelToSpecialty());
        unassignBtn.setOnAction(e -> unassignPersonnelFromSpecialty());
    }
    
    private void loadData() {
        // Charger les spécialités dans la table
        specialtiesTable.setItems(configManager.getAvailableSpecialties());
        
        // Charger les spécialités dans le sélecteur
        specialtySelector.setItems(configManager.getAvailableSpecialties());
    }
    
    // === ACTIONS SPÉCIALITÉS ===
    
    private void addSpecialty() {
        String newSpecialty = newSpecialtyField.getText().trim();
        if (!newSpecialty.isEmpty()) {
            if (configManager.addSpecialty(newSpecialty)) {
                newSpecialtyField.clear();
                showSuccessAlert("Spécialité ajoutée", "La spécialité '" + newSpecialty + "' a été ajoutée avec succès.");
            } else {
                showErrorAlert("Erreur", "La spécialité '" + newSpecialty + "' existe déjà ou est invalide.");
            }
        }
    }
    
    private void removeSpecialty() {
        String selected = specialtiesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Supprimer la spécialité '" + selected + "' ?");
            alert.setContentText("Cette action supprimera la spécialité de tous les personnels qui l'ont.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    configManager.removeSpecialty(selected);
                    // TODO: Mettre à jour tous les personnels qui ont cette spécialité
                    showSuccessAlert("Spécialité supprimée", "La spécialité '" + selected + "' a été supprimée.");
                }
            });
        }
    }
    
    private void editSpecialty() {
        String selected = specialtiesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(selected);
            dialog.setTitle("Modifier la spécialité");
            dialog.setHeaderText("Modifier la spécialité");
            dialog.setContentText("Nouveau nom :");
            
            dialog.showAndWait().ifPresent(newName -> {
                if (configManager.updateSpecialty(selected, newName)) {
                    showSuccessAlert("Spécialité modifiée", "La spécialité a été modifiée avec succès.");
                } else {
                    showErrorAlert("Erreur", "Impossible de modifier la spécialité. Le nom existe déjà ou est invalide.");
                }
            });
        }
    }
    
    // === ACTIONS PERSONNEL ===
    
    private void loadPersonnelForSpecialty() {
        String selectedSpecialty = specialtySelector.getValue();
        if (selectedSpecialty != null) {
            // TODO: Charger le personnel depuis l'ApiService; // Pour l'instant, on simule avec des données de test
            loadPersonnelLists(selectedSpecialty);
        }
    }
    
    private void loadPersonnelLists(String specialty) {
        // Simulation - à remplacer par l'appel API réel
        ObservableList<PersonnelItem> allPersonnel = FXCollections.observableArrayList();
        ObservableList<PersonnelItem> assignedPersonnel = FXCollections.observableArrayList();
        
        // Données de test
        allPersonnel.addAll(
            new PersonnelItem("1", "Jean Dupont", "Technicien"),
            new PersonnelItem("2", "Marie Martin", "Ingénieur"),
            new PersonnelItem("3", "Pierre Durand", "Intermittent du spectacle"),
            new PersonnelItem("4", "Sophie Bernard", "Chef d'équipe")
        );
        
        availablePersonnelList.setItems(allPersonnel);
        assignedPersonnelList.setItems(assignedPersonnel);
    }
    
    private void assignPersonnelToSpecialty() {
        PersonnelItem selected = availablePersonnelList.getSelectionModel().getSelectedItem();
        String specialty = specialtySelector.getValue();
        
        if (selected != null && specialty != null) {
            // TODO: Mettre à jour via l'API
            assignedPersonnelList.getItems().add(selected);
            availablePersonnelList.getItems().remove(selected);
            showSuccessAlert("Attribution effectuée", 
                selected.getName() + " a été assigné à la spécialité '" + specialty + "'.");
        }
    }
    
    private void unassignPersonnelFromSpecialty() {
        PersonnelItem selected = assignedPersonnelList.getSelectionModel().getSelectedItem();
        String specialty = specialtySelector.getValue();
        
        if (selected != null && specialty != null) {
            // TODO: Mettre à jour via l'API
            availablePersonnelList.getItems().add(selected);
            assignedPersonnelList.getItems().remove(selected);
            showSuccessAlert("Attribution supprimée", 
                selected.getName() + " n'est plus assigné à la spécialité '" + specialty + "'.");
        }
    }
    
    // === ACTIONS GLOBALES ===
    
    private void resetToDefaults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser les spécialités");
        alert.setHeaderText("Voulez-vous vraiment réinitialiser toutes les spécialités ?");
        alert.setContentText("Cette action supprimera toutes les spécialités personnalisées et restaurera les valeurs par défaut.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                configManager.resetToDefaults();
                showSuccessAlert("Réinitialisation effectuée", "Les spécialités ont été réinitialisées.");
            }
        });
    }
    
    private void importSpecialties() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer Configuration des Spécialités");
        fileChooser.setInitialFileName("specialties_config.json");
        
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
            importSpecialtiesFromFile(file);
        }
    }
    
    private void exportSpecialties() {
        if (specialtiesTable.getItems() == null || specialtiesTable.getItems().isEmpty()) {
            showWarningAlert("Export Impossible", "Aucune spécialité à exporter", 
                           "La liste des spécialités est vide. Ajoutez des spécialités avant d'exporter.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter Configuration des Spécialités");
        fileChooser.setInitialFileName("specialties_config_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".json");
        
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
            exportSpecialtiesToFile(file);
        }
    }
    
    private void saveConfiguration() {
        configManager.saveConfiguration();
        showSuccessAlert("Configuration sauvegardée", "Toutes les modifications ont été sauvegardées.");
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
    
    @SuppressWarnings("unused")
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

    // === MÉTHODES D'IMPORT/EXPORT ===

    /**
     * Importe les spécialités depuis un fichier JSON
     */
    private void importSpecialtiesFromFile(java.io.File file) {
        try {
            // Lire le contenu du fichier
            String content = Files.readString(file.toPath());
            
            // Parser le JSON et importer les spécialités; // Pour l'instant, simuler l'import en parsant un format JSON simple
            if (parseAndImportSpecialties(content)) {
                // Recharger les données dans l'interface
                loadData();
                showSuccessAlert("Import Réussi", 
                    "Les spécialités ont été importées avec succès depuis : " + file.getName());
            } else {
                showErrorAlert("Erreur d'Import", 
                    "Le fichier JSON semble invalide ou incompatible.");
            }
            
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, 
                "Erreur lors de l'import des spécialités", e);
            showErrorAlert("Erreur d'Import", 
                "Impossible de lire le fichier : " + e.getMessage());
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, 
                "Erreur lors de l'import des spécialités", e);
            showErrorAlert("Erreur d'Import", 
                "Erreur lors de l'importation : " + e.getMessage());
        }
    }

    /**
     * Exporte les spécialités vers un fichier JSON
     */
    private void exportSpecialtiesToFile(java.io.File file) {
        try {
            // Générer le JSON des spécialités
            String jsonContent = generateSpecialtiesJson();
            
            // Écrire dans le fichier
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(jsonContent);
                writer.flush();
            }
            
            showSuccessAlert("Export Réussi", 
                "Les spécialités ont été exportées avec succès vers : " + file.getName());
                
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, 
                "Erreur lors de l'export des spécialités", e);
            showErrorAlert("Erreur d'Export", 
                "Impossible d'écrire le fichier : " + e.getMessage());
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, 
                "Erreur lors de l'export des spécialités", e);
            showErrorAlert("Erreur d'Export", 
                "Erreur lors de l'exportation : " + e.getMessage());
        }
    }

    /**
     * Parse et importe les spécialités depuis du contenu JSON
     */
    private boolean parseAndImportSpecialties(String jsonContent) {
        try {
            // Format JSON simple attendu : {"specialties": ["Spécialité 1", "Spécialité 2", ...]}
            if (jsonContent.contains("\"specialties\"") && jsonContent.contains("[")) {
                // Extraction simple des spécialités
                int start = jsonContent.indexOf("[");
                int end = jsonContent.indexOf("]");
                if (start >= 0 && end > start) {
                    String specialtiesArray = jsonContent.substring(start + 1, end);
                    String[] specialties = specialtiesArray.split(",");
                    
                    for (String specialty : specialties) {
                        String cleanSpecialty = specialty.trim()
                            .replace("\"", "");
                        if (!cleanSpecialty.isEmpty()) {
                            configManager.addSpecialty(cleanSpecialty);
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, 
                "Erreur lors du parsing du JSON", e);
            return false;
        }
    }

    /**
     * Génère le JSON des spécialités actuelles
     */
    private String generateSpecialtiesJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"exportDate\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("    \"version\": \"1.0\",\n");
        json.append("    \"type\": \"specialties_config\"\n");
        json.append("  },\n");
        json.append("  \"specialties\": [\n");
        
        ObservableList<String> specialties = specialtiesTable.getItems();
        for (int i = 0; i < specialties.size(); i++) {
            json.append("    \"").append(specialties.get(i)).append("\"");
            if (i < specialties.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        return json.toString();
    }
    
    // === CLASSE INTERNE ===
    
    /**
     * Item représentant un personnel dans les listes
     */
    public static class PersonnelItem {
        private final String id;
        private final String name;
        private final String type;
        
        public PersonnelItem(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        
        @Override
        public String toString() {
            return name + " (" + type + ")";
        }
    }
}
