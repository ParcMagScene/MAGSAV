package com.magscene.magsav.desktop.view.dialog;

import com.magscene.magsav.desktop.service.LocmatImportService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

/**
 * Dialogue pour l'import des données LOCMAT depuis Excel
 */
public class LocmatImportDialog extends Stage {
    
    private final LocmatImportService importService;
    private final TextField filePathField;
    private final Button browseButton;
    private final Button importButton;
    private final Button cancelButton;
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final TextArea resultArea;
    
    private File selectedFile;
    
    public LocmatImportDialog() {
        this.importService = new LocmatImportService();
        
        setTitle("Import LOCMAT - Données Excel");
        // setModal(true); // Not needed with initModality
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setWidth(600);
        setHeight(500);
        
        // Champs d'interface
        this.filePathField = new TextField();
        this.browseButton = new Button("Parcourir...");
        this.importButton = new Button("Importer");
        this.cancelButton = new Button("Fermer");
        this.progressBar = new ProgressBar();
        this.progressLabel = new Label("Prêt pour l'import");
        this.resultArea = new TextArea();
        
        setupUI();
        setupEventHandlers();
        
        // Style initial
        filePathField.setEditable(false);
        importButton.setDisable(true);
        progressBar.setVisible(false);
        resultArea.setEditable(false);
    }
    
    private void setupUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Section sélection de fichier
        VBox fileSection = createFileSelectionSection();
        
        // Section informations
        VBox infoSection = createInfoSection();
        
        // Section progression
        VBox progressSection = createProgressSection();
        
        // Section résultats
        VBox resultSection = createResultSection();
        
        // Boutons d'action
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(
            fileSection,
            new Separator(),
            infoSection,
            new Separator(),
            progressSection,
            resultSection,
            buttonBox
        );
        
        setScene(new Scene(root));
    }
    
    private VBox createFileSelectionSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Sélection du fichier Excel LOCMAT");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        filePathField.setPromptText("Sélectionnez le fichier LOCMAT_Materiel.xlsx...");
        HBox.setHgrow(filePathField, Priority.ALWAYS);
        
        browseButton.setMinWidth(100);
        
        fileBox.getChildren().addAll(filePathField, browseButton);
        
        section.getChildren().addAll(titleLabel, fileBox);
        return section;
    }
    
    private VBox createInfoSection() {
        VBox section = new VBox(10);
        
        Label infoLabel = new Label("Informations sur l'import LOCMAT");
        infoLabel.setStyle("-fx-font-weight: bold;");
        
        Label descriptionLabel = new Label(
            "• Format attendu: Fichier Excel (.xlsx)\n" +
            "• Colonnes: Code Locmat, Catégorie, Sous-catégorie, Description, Marque, Propriétaire, NumSerie, Quantité\n" +
            "• L'import génère automatiquement un UID et un code-barres par équipement\n" +
            "• Les catégories et sous-catégories seront créées automatiquement\n" +
            "• Les équipements non sérialisés recevront un identifiant unique"
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #666;");
        
        section.getChildren().addAll(infoLabel, descriptionLabel);
        return section;
    }
    
    private VBox createProgressSection() {
        VBox section = new VBox(10);
        
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setProgress(0);
        
        progressLabel.setStyle("-fx-font-size: 12px;");
        
        section.getChildren().addAll(progressLabel, progressBar);
        return section;
    }
    
    private VBox createResultSection() {
        VBox section = new VBox(5);
        
        Label resultLabel = new Label("Résultats de l'import");
        resultLabel.setStyle("-fx-font-weight: bold;");
        
        resultArea.setPrefRowCount(8);
        resultArea.setPromptText("Les résultats de l'import s'afficheront ici...");
        resultArea.setWrapText(true);
        
        section.getChildren().addAll(resultLabel, resultArea);
        VBox.setVgrow(resultArea, Priority.ALWAYS);
        
        return section;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        importButton.setMinWidth(100);
        
        cancelButton.setMinWidth(100);
        
        buttonBox.getChildren().addAll(importButton, cancelButton);
        return buttonBox;
    }
    
    private void setupEventHandlers() {
        // Bouton parcourir
        browseButton.setOnAction(e -> selectFile());
        
        // Bouton importer
        importButton.setOnAction(e -> startImport());
        
        // Bouton fermer
        cancelButton.setOnAction(e -> close());
        
        // Fermeture de la fenêtre
        setOnCloseRequest(e -> {
            if (progressBar.isVisible()) {
                e.consume(); // Empêcher la fermeture pendant l'import
                showWarning("Import en cours", "Veuillez attendre la fin de l'import avant de fermer cette fenêtre.");
            }
        });
    }
    
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier Excel LOCMAT");
        
        // Filtres de fichiers
        FileChooser.ExtensionFilter excelFilter = 
            new FileChooser.ExtensionFilter("Fichiers Excel (*.xlsx)", "*.xlsx");
        FileChooser.ExtensionFilter allFilter = 
            new FileChooser.ExtensionFilter("Tous les fichiers (*.*)", "*.*");
        
        fileChooser.getExtensionFilters().addAll(excelFilter, allFilter);
        
        // Répertoire initial (LOCMAT s'il existe)
        File locmatDir = new File("LOCMAT");
        if (locmatDir.exists() && locmatDir.isDirectory()) {
            fileChooser.setInitialDirectory(locmatDir);
        }
        
        File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            selectedFile = file;
            filePathField.setText(file.getAbsolutePath());
            importButton.setDisable(false);
            
            // Vérifier le nom du fichier
            if (!file.getName().toLowerCase().contains("locmat")) {
                showWarning("Attention", 
                    "Le fichier sélectionné ne semble pas être un fichier LOCMAT.\n" +
                    "Assurez-vous que c'est le bon fichier avant de continuer.");
            }
        }
    }
    
    private void startImport() {
        if (selectedFile == null || !selectedFile.exists()) {
            showError("Erreur", "Veuillez sélectionner un fichier valide.");
            return;
        }
        
        // Désactiver les contrôles
        browseButton.setDisable(true);
        importButton.setDisable(true);
        progressBar.setVisible(true);
        resultArea.clear();
        
        // Créer et lancer la tâche d'import
        Task<LocmatImportService.ImportResult> importTask = importService.createImportTask(selectedFile);
        
        // Binding des propriétés de progression
        progressBar.progressProperty().bind(importTask.progressProperty());
        progressLabel.textProperty().bind(importTask.messageProperty());
        
        // Gestionnaire de fin de tâche
        importTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                LocmatImportService.ImportResult result = importTask.getValue();
                handleImportResult(result);
                resetUI();
            });
        });
        
        importTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = importTask.getException();
                handleImportError(exception);
                resetUI();
            });
        });
        
        // Lancer la tâche dans un thread séparé
        Thread importThread = new Thread(importTask);
        importThread.setDaemon(true);
        importThread.start();
    }
    
    private void handleImportResult(LocmatImportService.ImportResult result) {
        StringBuilder resultText = new StringBuilder();
        
        if (result.success) {
            resultText.append("✅ IMPORT RÉUSSI\n\n");
            resultText.append(String.format("Équipements importés: %d\n", result.equipmentCount));
            
            if (result.hasErrors()) {
                resultText.append(String.format("Avertissements: %d\n", result.errorCount));
            }
            
            resultText.append("\n").append(result.message);
        } else {
            resultText.append("❌ ÉCHEC DE L'IMPORT\n\n");
            resultText.append(result.message);
        }
        
        // Ajouter les détails des erreurs
        if (result.hasErrors()) {
            resultText.append("\n\n").append(result.getDetailedMessage());
        }
        
        resultArea.setText(resultText.toString());
        
        // Faire défiler vers le haut
        resultArea.setScrollTop(0);
        
        // Notification
        if (result.success) {
            showInfo("Import terminé", 
                String.format("Import réussi: %d équipements créés", result.equipmentCount));
        }
    }
    
    private void handleImportError(Throwable exception) {
        String errorMessage = "Erreur durant l'import:\n" + 
                            exception.getMessage();
        
        resultArea.setText("❌ ERREUR D'IMPORT\n\n" + errorMessage);
        
        showError("Erreur d'import", errorMessage);
    }
    
    private void resetUI() {
        browseButton.setDisable(false);
        importButton.setDisable(selectedFile == null);
        progressBar.setVisible(false);
        
        // Débinder les propriétés AVANT de modifier le texte
        progressBar.progressProperty().unbind();
        progressLabel.textProperty().unbind();
        
        // Maintenant on peut modifier le texte
        progressLabel.setText("Import terminé");
    }
    
    // Méthodes utilitaires pour les alertes
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}