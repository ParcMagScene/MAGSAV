package com.magsav.gui.dialogs;

import com.magsav.imports.CsvImporter;
import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.DataChangeNotificationService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la boîte de dialogue d'import CSV avec suivi en temps réel
 */
public class CsvImportDialog implements Initializable {
    
    @FXML private TextField tfFilePath;
    @FXML private Button btnChooseFile;
    @FXML private CheckBox cbDryRun;
    @FXML private CheckBox cbShowDetailedLogs;
    @FXML private CheckBox cbStopOnFirstError;
    
    @FXML private Label lblStatus;
    @FXML private Label lblProgressText;
    @FXML private ProgressBar progressBar;
    
    @FXML private Label lblRowsProcessed;
    @FXML private Label lblProductsCreated;
    @FXML private Label lblInterventionsCreated;
    @FXML private Label lblErrors;
    
    @FXML private TextArea taLogs;
    
    private File selectedFile;
    private CsvImporter importer;
    private CsvImporter.Result lastResult;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser l'importeur
        importer = new CsvImporter(
            new ProductRepository(),
            new InterventionRepository(),
            new SocieteRepository()
        );
        
        // Configurer les callbacks pour les logs et la progression
        importer.setLogCallback(this::onLogMessage);
        importer.setProgressCallback(this::onProgressUpdate);
        
        // État initial
        resetUI();
        
        // Listeners
        cbDryRun.selectedProperty().addListener((obs, oldVal, newVal) -> updateStatus());
        cbShowDetailedLogs.selectedProperty().addListener((obs, oldVal, newVal) -> updateLogsFilter());
        
        // Retarder la configuration du bouton d'import
        Platform.runLater(this::setupImportButton);
    }
    
    @FXML
    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner un fichier CSV à importer");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        Stage stage = (Stage) btnChooseFile.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        
        if (file != null) {
            selectedFile = file;
            tfFilePath.setText(file.getAbsolutePath());
            updateStatus();
            
            // Log de sélection
            addLog("📁 Fichier sélectionné: " + file.getName());
            addLog("   Taille: " + (file.length() / 1024) + " KB");
        }
    }
    
    private void setupImportButton() {
        try {
            // Vérifier que la scène est disponible
            if (taLogs.getScene() == null) {
                // Retenter plus tard si la scène n'est pas encore prête
                Platform.runLater(this::setupImportButton);
                return;
            }
            
            // Ajouter dynamiquement le bouton d'import
            DialogPane dialogPane = (DialogPane) taLogs.getScene().getRoot();
            
            ButtonType importButton = new ButtonType("🚀 Démarrer l'import", ButtonBar.ButtonData.OK_DONE);
            dialogPane.getButtonTypes().add(0, importButton);
            
            Button btnImport = (Button) dialogPane.lookupButton(importButton);
            if (btnImport != null) {
                btnImport.setDisable(true);
                
                // Action du bouton
                btnImport.setOnAction(e -> startImport());
                
                // Mettre à jour l'état du bouton
                tfFilePath.textProperty().addListener((obs, oldVal, newVal) -> {
                    btnImport.setDisable(newVal == null || newVal.trim().isEmpty());
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du bouton d'import: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startImport() {
        if (selectedFile == null || !selectedFile.exists()) {
            showAlert("Erreur", "Veuillez sélectionner un fichier CSV valide.");
            return;
        }
        
        // Réinitialiser l'interface
        resetCounters();
        taLogs.clear();
        
        // Désactiver les contrôles pendant l'import
        setControlsEnabled(false);
        
        boolean dryRun = cbDryRun.isSelected();
        addLog("🚀 Démarrage de l'import " + (dryRun ? "(SIMULATION)" : "(RÉEL)"));
        addLog("📁 Fichier: " + selectedFile.getName());
        addLog("");
        
        // Créer une tâche en arrière-plan
        Task<CsvImporter.Result> importTask = new Task<>() {
            @Override
            protected CsvImporter.Result call() throws Exception {
                return importer.importFile(selectedFile.toPath(), dryRun);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    lastResult = getValue();
                    onImportComplete(lastResult);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable exception = getException();
                    onImportError(exception);
                });
            }
        };
        
        // Lancer la tâche
        Thread importThread = new Thread(importTask);
        importThread.setDaemon(true);
        importThread.start();
    }
    
    private void onLogMessage(String logMessage) {
        Platform.runLater(() -> {
            if (cbShowDetailedLogs.isSelected()) {
                addLog(logMessage);
            } else {
                // Filtrer pour ne montrer que les messages importants
                if (logMessage.contains("ERREUR") || 
                    logMessage.contains("créé") || 
                    logMessage.contains("FIN") || 
                    logMessage.contains("DÉBUT")) {
                    addLog(logMessage);
                }
            }
        });
    }
    
    private void onProgressUpdate(CsvImporter.ImportProgress progress) {
        Platform.runLater(() -> {
            // Mettre à jour la barre de progression
            progressBar.setProgress(progress.progressPercentage() / 100.0);
            
            // Mettre à jour les labels
            lblProgressText.setText(progress.currentRow() + "/" + progress.totalRows());
            lblRowsProcessed.setText(String.valueOf(progress.currentRow()));
            lblProductsCreated.setText(String.valueOf(progress.productsCreated()));
            lblInterventionsCreated.setText(String.valueOf(progress.interventionsCreated()));
            lblErrors.setText(String.valueOf(progress.errorsCount()));
            
            // Mettre à jour le statut
            lblStatus.setText(progress.currentOperation());
            
            // Appliquer des couleurs selon les erreurs
            if (progress.errorsCount() > 0) {
                lblErrors.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
            } else {
                lblErrors.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
            }
        });
    }
    
    private void onImportComplete(CsvImporter.Result result) {
        setControlsEnabled(true);
        
        // Afficher le résumé
        addLog("");
        addLog("✅ Import terminé!");
        addLog("📊 " + result.summary());
        
        if (!result.errors().isEmpty()) {
            addLog("");
            addLog("⚠️ Erreurs rencontrées:");
            for (String error : result.errors()) {
                addLog("   " + error);
            }
        }
        
        // Mettre à jour le statut final
        lblStatus.setText("Import terminé");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        // Notifier le système de changement de données pour rafraîchissement automatique
        if (result.products() > 0) {
            DataChangeNotificationService.getInstance().notifyProductsImported(result.products());
        }
        
        // Proposer de fermer ou relancer
        if (result.errors().isEmpty()) {
            showAlert("Import réussi", result.summary());
        } else {
            showAlert("Import terminé avec erreurs", 
                     result.summary() + "\n\nVeuillez consulter les logs pour plus de détails.");
        }
    }
    
    private void onImportError(Throwable error) {
        setControlsEnabled(true);
        
        addLog("");
        addLog("❌ Erreur critique pendant l'import:");
        addLog("   " + error.getMessage());
        
        lblStatus.setText("Erreur d'import");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
        
        showAlert("Erreur d'import", 
                 "Une erreur critique s'est produite:\n" + error.getMessage());
    }
    
    private void addLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        taLogs.appendText("[" + timestamp + "] " + message + "\n");
        
        // Auto-scroll vers le bas
        taLogs.positionCaret(taLogs.getLength());
        taLogs.setScrollTop(Double.MAX_VALUE);
    }
    
    private void resetUI() {
        selectedFile = null;
        tfFilePath.clear();
        resetCounters();
        taLogs.clear();
        lblStatus.setText("En attente...");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #6c757d;");
        progressBar.setProgress(0);
    }
    
    private void resetCounters() {
        lblProgressText.setText("0/0");
        lblRowsProcessed.setText("0");
        lblProductsCreated.setText("0");
        lblInterventionsCreated.setText("0");
        lblErrors.setText("0");
        lblErrors.setStyle("-fx-font-weight: bold; -fx-text-fill: #6c757d;");
    }
    
    private void setControlsEnabled(boolean enabled) {
        btnChooseFile.setDisable(!enabled);
        cbDryRun.setDisable(!enabled);
        cbShowDetailedLogs.setDisable(!enabled);
        cbStopOnFirstError.setDisable(!enabled);
        
        // Activer/désactiver le bouton d'import seulement si la scene est disponible
        if (taLogs.getScene() != null) {
            DialogPane dialogPane = (DialogPane) taLogs.getScene().getRoot();
            if (dialogPane != null) {
                dialogPane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst()
                    .ifPresent(bt -> {
                        Button btn = (Button) dialogPane.lookupButton(bt);
                        if (btn != null) {
                            btn.setDisable(!enabled || selectedFile == null);
                        }
                    });
            }
        }
    }
    
    private void updateStatus() {
        if (selectedFile != null) {
            String mode = cbDryRun.isSelected() ? "simulation" : "import réel";
            lblStatus.setText("Prêt pour " + mode);
            lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
        }
    }
    
    private void updateLogsFilter() {
        // TODO: Implémenter le filtrage des logs existants si nécessaire
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Obtient le résultat du dernier import
     */
    public CsvImporter.Result getLastResult() {
        return lastResult;
    }
}