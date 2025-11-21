package com.magscene.magsav.desktop.view.supplier.dialog;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Dialogue spécialisé pour l'import de catalogues fournisseur
 * Gestion des différents formats et mapping des colonnes
 */
public class CatalogImportDialog extends Dialog<CatalogImportDialog.ImportResult> {
    
    // Contrôles de sélection de fichier
    private TextField filePathField;
    private ComboBox<String> formatCombo;
    private ComboBox<String> encodingCombo;
    private CheckBox hasHeaderCheckBox;
    
    // Contrôles de mapping
    private ComboBox<String> referenceColumnCombo;
    private ComboBox<String> nameColumnCombo;
    private ComboBox<String> descriptionColumnCombo;
    private ComboBox<String> priceColumnCombo;
    private ComboBox<String> categoryColumnCombo;
    private ComboBox<String> availabilityColumnCombo;
    
    // Aperçu des données
    private TableView<Map<String, String>> previewTable;
    private ObservableList<Map<String, String>> previewData;
    
    // Progression de l'import
    private ProgressBar progressBar;
    private Label statusLabel;
    
    // Fournisseur cible
    private String supplierName;
    private File selectedFile;
    
    /**
     * Constructor
     */
    public CatalogImportDialog(Stage owner, String supplierName) {
        this.supplierName = supplierName;
        
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Import catalogue - " + supplierName);
        setResizable(true);
        
        setupUI();
        setupResultConverter();
        setupValidation();
    }
    
    private void setupUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(800);
        root.setPrefHeight(600);
        
        // Sections
        root.getChildren().addAll(
            createFileSelectionSection(),
            createMappingSection(),
            createPreviewSection(),
            createProgressSection()
        );
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        
        getDialogPane().setContent(scrollPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Renommer les boutons
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Importer");
        
        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Annuler");
        
        // Style
        getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/supplier-system.css").toExternalForm()
        );
        getDialogPane().getStyleClass().add("catalog-import-dialog");
    }
    
    private VBox createFileSelectionSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");
        
        Label titleLabel = new Label("1. Sélection du fichier");
        titleLabel.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Sélection de fichier
        grid.add(new Label("Fichier:"), 0, 0);
        
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        filePathField = new TextField();
        filePathField.setPromptText("Aucun fichier sélectionné");
        filePathField.setEditable(false);
        filePathField.setPrefWidth(400);
        
        Button browseButton = new Button("Parcourir...");
        browseButton.setOnAction(e -> selectFile());
        
        fileBox.getChildren().addAll(filePathField, browseButton);
        grid.add(fileBox, 1, 0, 2, 1);
        
        // Format de fichier
        grid.add(new Label("Format:"), 0, 1);
        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("Excel (.xlsx/.xls)", "CSV", "XML", "JSON");
        formatCombo.setValue("Excel (.xlsx/.xls)");
        formatCombo.setOnAction(e -> updateFormatOptions());
        grid.add(formatCombo, 1, 1);
        
        // Encodage (pour CSV)
        grid.add(new Label("Encodage:"), 0, 2);
        encodingCombo = new ComboBox<>();
        encodingCombo.getItems().addAll("UTF-8", "ISO-8859-1", "Windows-1252");
        encodingCombo.setValue("UTF-8");
        grid.add(encodingCombo, 1, 2);
        
        // En-têtes
        hasHeaderCheckBox = new CheckBox("Le fichier contient des en-têtes de colonnes");
        hasHeaderCheckBox.setSelected(true);
        grid.add(hasHeaderCheckBox, 1, 3);
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createMappingSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");
        
        Label titleLabel = new Label("2. Mapping des colonnes");
        titleLabel.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Colonnes obligatoires
        Label requiredLabel = new Label("Colonnes obligatoires:");
        requiredLabel.getStyleClass().add("subsection-title");
        grid.add(requiredLabel, 0, 0, 3, 1);
        
        grid.add(new Label("Référence *:"), 0, 1);
        referenceColumnCombo = new ComboBox<>();
        referenceColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(referenceColumnCombo, 1, 1);
        
        grid.add(new Label("Désignation *:"), 0, 2);
        nameColumnCombo = new ComboBox<>();
        nameColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(nameColumnCombo, 1, 2);
        
        grid.add(new Label("Prix unitaire *:"), 0, 3);
        priceColumnCombo = new ComboBox<>();
        priceColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(priceColumnCombo, 1, 3);
        
        // Colonnes optionnelles
        Label optionalLabel = new Label("Colonnes optionnelles:");
        optionalLabel.getStyleClass().add("subsection-title");
        grid.add(optionalLabel, 0, 4, 3, 1);
        
        grid.add(new Label("Description:"), 0, 5);
        descriptionColumnCombo = new ComboBox<>();
        descriptionColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(descriptionColumnCombo, 1, 5);
        
        grid.add(new Label("Catégorie:"), 0, 6);
        categoryColumnCombo = new ComboBox<>();
        categoryColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(categoryColumnCombo, 1, 6);
        
        grid.add(new Label("Disponibilité:"), 0, 7);
        availabilityColumnCombo = new ComboBox<>();
        availabilityColumnCombo.setPromptText("Sélectionner la colonne...");
        grid.add(availabilityColumnCombo, 1, 7);
        
        // Bouton d'analyse
        Button analyzeButton = new Button("Analyser le fichier");
        analyzeButton.getStyleClass().add("btn-primary");
        analyzeButton.setOnAction(e -> analyzeFile());
        grid.add(analyzeButton, 2, 1, 1, 3);
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createPreviewSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");
        
        Label titleLabel = new Label("3. Aperçu des données");
        titleLabel.getStyleClass().add("section-title");
        
        // Table de prévisualisation
        previewData = FXCollections.observableArrayList();
        previewTable = new TableView<>(previewData);
        previewTable.setPrefHeight(200);
        previewTable.setPlaceholder(new Label("Aucune donnée à afficher. Analysez d'abord le fichier."));
        
        // Info sur la prévisualisation
        Label infoLabel = new Label("Aperçu des 10 premières lignes après mapping");
        infoLabel.getStyleClass().add("info-text");
        
        section.getChildren().addAll(titleLabel, infoLabel, previewTable);
        return section;
    }
    
    private VBox createProgressSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");
        
        Label titleLabel = new Label("4. Progression de l'import");
        titleLabel.getStyleClass().add("section-title");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);
        
        statusLabel = new Label("Prêt à importer");
        statusLabel.getStyleClass().add("status-label");
        
        section.getChildren().addAll(titleLabel, progressBar, statusLabel);
        return section;
    }
    
    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return performImport();
            }
            return null;
        });
    }
    
    private void setupValidation() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        
        BooleanProperty validProperty = new SimpleBooleanProperty();
        validProperty.bind(
            filePathField.textProperty().isEmpty().not()
            .and(referenceColumnCombo.valueProperty().isNotNull())
            .and(nameColumnCombo.valueProperty().isNotNull())
            .and(priceColumnCombo.valueProperty().isNotNull())
        );
        
        okButton.disableProperty().bind(validProperty.not());
    }
    
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier catalogue");
        
        // Filtres selon le format sélectionné
        String selectedFormat = formatCombo.getValue();
        if (selectedFormat.contains("Excel")) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx", "*.xls")
            );
        } else if (selectedFormat.contains("CSV")) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
            );
        } else if (selectedFormat.contains("XML")) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers XML", "*.xml")
            );
        } else if (selectedFormat.contains("JSON")) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
            );
        }
        
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            // Auto-détection du format si possible
            autoDetectFormat();
        }
    }
    
    private void updateFormatOptions() {
        String format = formatCombo.getValue();
        
        // L'encodage n'est pertinent que pour CSV
        encodingCombo.setDisable(!format.contains("CSV"));
        
        // Réinitialiser les combos de colonnes
        clearColumnCombos();
    }
    
    private void autoDetectFormat() {
        if (selectedFile == null) return;
        
        String fileName = selectedFile.getName().toLowerCase();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            formatCombo.setValue("Excel (.xlsx/.xls)");
        } else if (fileName.endsWith(".csv")) {
            formatCombo.setValue("CSV");
        } else if (fileName.endsWith(".xml")) {
            formatCombo.setValue("XML");
        } else if (fileName.endsWith(".json")) {
            formatCombo.setValue("JSON");
        }
        
        updateFormatOptions();
    }
    
    private void analyzeFile() {
        if (selectedFile == null) {
            showError("Aucun fichier sélectionné", "Veuillez d'abord sélectionner un fichier.");
            return;
        }
        
        statusLabel.setText("Analyse du fichier en cours...");
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        
        // Simulation de l'analyse - dans la vraie implémentation, 
        // cela ferait appel à un service d'analyse de fichier
        Task<Void> analyzeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000); // Simulation
                return null;
            }
            
            @Override
            protected void succeeded() {
                // Simulation des colonnes détectées
                populateColumnCombos();
                populatePreviewData();
                
                progressBar.setVisible(false);
                statusLabel.setText("Analyse terminée. " + previewData.size() + " lignes détectées.");
            }
            
            @Override
            protected void failed() {
                progressBar.setVisible(false);
                statusLabel.setText("Erreur lors de l'analyse du fichier.");
                showError("Erreur d'analyse", "Impossible d'analyser le fichier sélectionné.");
            }
        };
        
        new Thread(analyzeTask).start();
    }
    
    private void populateColumnCombos() {
        // Simulation des colonnes détectées
        List<String> columns = Arrays.asList(
            "Colonne A", "Colonne B", "Colonne C", "Colonne D", "Colonne E",
            "REF", "DESIGNATION", "PRIX", "DESCRIPTION", "CATEGORIE", "STOCK"
        );
        
        ObservableList<String> columnList = FXCollections.observableArrayList(columns);
        
        referenceColumnCombo.setItems(columnList);
        nameColumnCombo.setItems(columnList);
        descriptionColumnCombo.setItems(columnList);
        priceColumnCombo.setItems(columnList);
        categoryColumnCombo.setItems(columnList);
        availabilityColumnCombo.setItems(columnList);
        
        // Auto-mapping intelligent basé sur les noms
        for (String column : columns) {
            String columnLower = column.toLowerCase();
            if (columnLower.contains("ref") && referenceColumnCombo.getValue() == null) {
                referenceColumnCombo.setValue(column);
            } else if (columnLower.contains("designation") || columnLower.contains("nom")) {
                nameColumnCombo.setValue(column);
            } else if (columnLower.contains("prix") || columnLower.contains("price")) {
                priceColumnCombo.setValue(column);
            } else if (columnLower.contains("description") || columnLower.contains("desc")) {
                descriptionColumnCombo.setValue(column);
            } else if (columnLower.contains("categorie") || columnLower.contains("category")) {
                categoryColumnCombo.setValue(column);
            } else if (columnLower.contains("stock") || columnLower.contains("dispo")) {
                availabilityColumnCombo.setValue(column);
            }
        }
    }
    
    private void populatePreviewData() {
        previewData.clear();
        
        // Simulation de données de prévisualisation
        for (int i = 1; i <= 5; i++) {
            Map<String, String> row = new HashMap<>();
            row.put("REF", "REF00" + i);
            row.put("DESIGNATION", "Produit exemple " + i);
            row.put("PRIX", (100.0 + i * 50) + "€");
            row.put("DESCRIPTION", "Description du produit " + i);
            row.put("CATEGORIE", "Catégorie " + (i % 3 + 1));
            row.put("STOCK", i % 2 == 0 ? "Disponible" : "Rupture");
            previewData.add(row);
        }
        
        // Créer les colonnes de la table dynamiquement
        previewTable.getColumns().clear();
        if (!previewData.isEmpty()) {
            Map<String, String> firstRow = previewData.get(0);
            for (String columnName : firstRow.keySet()) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(cellData -> 
                    new SimpleStringProperty(cellData.getValue().get(columnName))
                );
                column.setPrefWidth(120);
                previewTable.getColumns().add(column);
            }
        }
    }
    
    private void clearColumnCombos() {
        referenceColumnCombo.setItems(FXCollections.observableArrayList());
        nameColumnCombo.setItems(FXCollections.observableArrayList());
        descriptionColumnCombo.setItems(FXCollections.observableArrayList());
        priceColumnCombo.setItems(FXCollections.observableArrayList());
        categoryColumnCombo.setItems(FXCollections.observableArrayList());
        availabilityColumnCombo.setItems(FXCollections.observableArrayList());
        
        referenceColumnCombo.setValue(null);
        nameColumnCombo.setValue(null);
        descriptionColumnCombo.setValue(null);
        priceColumnCombo.setValue(null);
        categoryColumnCombo.setValue(null);
        availabilityColumnCombo.setValue(null);
    }
    
    private ImportResult performImport() {
        statusLabel.setText("Import en cours...");
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        
        // Dans une vraie implémentation, ceci serait fait dans un Task séparé
        // Pour la démonstration, on simule juste le succès
        
        ImportResult result = new ImportResult();
        result.setSuccessful(true);
        result.setSupplierName(supplierName);
        result.setFilePath(selectedFile.getAbsolutePath());
        result.setImportedCount(previewData.size());
        result.setMapping(createColumnMapping());
        
        return result;
    }
    
    private Map<String, String> createColumnMapping() {
        Map<String, String> mapping = new HashMap<>();
        
        if (referenceColumnCombo.getValue() != null) {
            mapping.put("reference", referenceColumnCombo.getValue());
        }
        if (nameColumnCombo.getValue() != null) {
            mapping.put("name", nameColumnCombo.getValue());
        }
        if (descriptionColumnCombo.getValue() != null) {
            mapping.put("description", descriptionColumnCombo.getValue());
        }
        if (priceColumnCombo.getValue() != null) {
            mapping.put("price", priceColumnCombo.getValue());
        }
        if (categoryColumnCombo.getValue() != null) {
            mapping.put("category", categoryColumnCombo.getValue());
        }
        if (availabilityColumnCombo.getValue() != null) {
            mapping.put("availability", availabilityColumnCombo.getValue());
        }
        
        return mapping;
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Classe de résultat
    public static class ImportResult {
        private boolean successful;
        private String supplierName;
        private String filePath;
        private int importedCount;
        private int errorCount;
        private Map<String, String> mapping;
        private String errorMessage;
        
        public ImportResult() {
            this.mapping = new HashMap<>();
        }
        
        // Getters et setters
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
        
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public int getImportedCount() { return importedCount; }
        public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        
        public Map<String, String> getMapping() { return mapping; }
        public void setMapping(Map<String, String> mapping) { this.mapping = mapping; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}