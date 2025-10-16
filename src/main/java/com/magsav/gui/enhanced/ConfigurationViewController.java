package com.magsav.gui.enhanced;

import com.magsav.ui.components.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Interface de configuration de l'application MAGSAV
 * Utilise ConfigurationManager pour gérer les paramètres
 */
public class ConfigurationViewController {
    
    // Éléments de l'interface
    @FXML private GridPane configGrid;
    @FXML private CheckBox debugModeCheck;
    @FXML private CheckBox cacheEnabledCheck;
    @FXML private CheckBox performanceMonitoringCheck;
    @FXML private CheckBox autoSaveCheck;
    @FXML private TextField databasePathField;
    @FXML private Button databasePathButton;
    @FXML private TextField mediaDirectoryField;
    @FXML private Button mediaDirectoryButton;
    @FXML private ComboBox<String> logLevelCombo;
    @FXML private TextArea configReportArea;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Button applyButton;
    
    private FormValidator validator;
    
    @FXML
    private void initialize() {
        setupBindings();
        setupValidation();
        setupComboBoxes();
        loadConfiguration();
        
        NotificationManager.showInfo("Interface de configuration chargée");
    }
    
    /**
     * Configure les bindings avec ConfigurationManager
     */
    private void setupBindings() {
        // Lier les propriétés JavaFX avec ConfigurationManager
        debugModeCheck.selectedProperty().bindBidirectional(ConfigurationManager.debugModeProperty());
        cacheEnabledCheck.selectedProperty().bindBidirectional(ConfigurationManager.cacheEnabledProperty());
        performanceMonitoringCheck.selectedProperty().bindBidirectional(ConfigurationManager.performanceMonitoringProperty());
        autoSaveCheck.selectedProperty().bindBidirectional(ConfigurationManager.autoSaveProperty());
        databasePathField.textProperty().bindBidirectional(ConfigurationManager.databasePathProperty());
        mediaDirectoryField.textProperty().bindBidirectional(ConfigurationManager.mediaDirectoryProperty());
        logLevelCombo.valueProperty().bindBidirectional(ConfigurationManager.logLevelProperty());
    }
    
    /**
     * Configuration de la validation
     */
    private void setupValidation() {
        validator = new FormValidator.Builder()
            .requiredTextField(databasePathField, "Chemin de la base de données")
            .textField(mediaDirectoryField, "Répertoire des médias", 0, 500)
            .requiredComboBox(logLevelCombo, "Niveau de log")
            .build();
    }
    
    /**
     * Configuration des ComboBox
     */
    private void setupComboBoxes() {
        logLevelCombo.getItems().addAll("DEBUG", "INFO", "WARN", "ERROR");
    }
    
    /**
     * Charge la configuration actuelle
     */
    private void loadConfiguration() {
        ErrorManager.MAGSAV.safeExecute("Chargement configuration", () -> {
            ConfigurationManager.loadConfiguration();
            updateConfigReport();
        });
    }
    
    /**
     * Met à jour le rapport de configuration
     */
    private void updateConfigReport() {
        configReportArea.setText(ConfigurationManager.getConfigurationReport());
    }
    
    /**
     * Sauvegarde la configuration
     */
    @FXML
    private void onSaveConfiguration() {
        if (!validator.validateAndShow()) {
            return;
        }
        
        ErrorManager.MAGSAV.safeExecute("Sauvegarde configuration", () -> {
            ConfigurationManager.saveConfiguration();
            updateConfigReport();
            NotificationManager.MAGSAV.operationSuccess("Configuration sauvegardée");
        });
    }
    
    /**
     * Applique la configuration
     */
    @FXML
    private void onApplyConfiguration() {
        if (!validator.validateAndShow()) {
            return;
        }
        
        ErrorManager.MAGSAV.safeExecute("Application configuration", () -> {
            ConfigurationManager.applyConfiguration();
            updateConfigReport();
            NotificationManager.MAGSAV.operationSuccess("Configuration appliquée");
        });
    }
    
    /**
     * Remet la configuration par défaut
     */
    @FXML
    private void onResetConfiguration() {
        if (AlertManager.showYesNoConfirmation(
            "Confirmation",
            "Êtes-vous sûr de vouloir remettre la configuration par défaut ?\n\n" +
            "Toutes les modifications seront perdues.")) {
            
            ErrorManager.MAGSAV.safeExecute("Remise à zéro configuration", () -> {
                ConfigurationManager.resetToDefaults();
                updateConfigReport();
                NotificationManager.MAGSAV.operationSuccess("Configuration remise par défaut");
            });
        }
    }
    
    /**
     * Sélectionne le chemin de la base de données
     */
    @FXML
    private void onSelectDatabasePath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner la base de données");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Base de données H2", "*.mv.db", "*.h2.db")
        );
        
        // Définir le répertoire initial
        String currentPath = databasePathField.getText();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
                fileChooser.setInitialDirectory(currentFile.getParentFile());
            }
        }
        
        File selectedFile = fileChooser.showOpenDialog(databasePathButton.getScene().getWindow());
        if (selectedFile != null) {
            databasePathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * Sélectionne le répertoire des médias
     */
    @FXML
    private void onSelectMediaDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Sélectionner le répertoire des médias");
        
        // Définir le répertoire initial
        String currentPath = mediaDirectoryField.getText();
        if (!currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists() && currentDir.isDirectory()) {
                dirChooser.setInitialDirectory(currentDir);
            }
        }
        
        File selectedDir = dirChooser.showDialog(mediaDirectoryButton.getScene().getWindow());
        if (selectedDir != null) {
            mediaDirectoryField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    /**
     * Valide la configuration
     */
    @FXML
    private void onValidateConfiguration() {
        ErrorManager.MAGSAV.safeExecute("Validation configuration", () -> {
            boolean valid = ConfigurationManager.validateConfiguration();
            
            if (valid) {
                AlertManager.showInfo("Validation", "Configuration valide !");
                NotificationManager.showSuccess("Configuration validée");
            } else {
                AlertManager.showWarning("Validation", 
                    "La configuration contient des erreurs. Consultez les logs pour plus de détails.");
                NotificationManager.showWarning("Erreurs de configuration détectées");
            }
        });
    }
    
    /**
     * Affiche les métriques de performance
     */
    @FXML
    private void onShowPerformanceMetrics() {
        ErrorManager.MAGSAV.safeExecute("Affichage métriques", () -> {
            var report = com.magsav.service.PerformanceMetricsService.generateReport();
            String reportText = report.toString(); // Convertir en String
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Métriques de Performance");
            alert.setHeaderText("Rapport des performances");
            
            TextArea textArea = new TextArea(reportText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setExpandableContent(textArea);
            alert.getDialogPane().setExpanded(true);
            alert.showAndWait();
        });
    }
    
    /**
     * Nettoie le cache
     */
    @FXML
    private void onClearCache() {
        if (AlertManager.showYesNoConfirmation(
            "Confirmation",
            "Êtes-vous sûr de vouloir vider le cache ?\n\n" +
            "Cela peut temporairement ralentir l'application.")) {
            
            ErrorManager.MAGSAV.safeExecute("Nettoyage cache", () -> {
                com.magsav.service.DataCacheService.invalidateAllCache();
                NotificationManager.MAGSAV.cacheRefreshed();
            });
        }
    }
}