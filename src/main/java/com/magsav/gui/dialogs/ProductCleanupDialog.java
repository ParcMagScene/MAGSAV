package com.magsav.gui.dialogs;

import com.magsav.util.ProductCleanupTool;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la boîte de dialogue de nettoyage des produits
 */
public class ProductCleanupDialog implements Initializable {
    
    @FXML private CheckBox cbDetectDuplicates;
    @FXML private CheckBox cbGenerateUids;
    @FXML private CheckBox cbCleanFields;
    @FXML private CheckBox cbNormalizeData;
    @FXML private CheckBox cbDetectInconsistencies;
    @FXML private CheckBox cbDryRun;
    
    // Options d'effacement
    @FXML private CheckBox cbEraseInterventions;
    @FXML private CheckBox cbEraseProducts;
    @FXML private CheckBox cbEraseCategories;
    @FXML private CheckBox cbEraseSocietes;
    @FXML private CheckBox cbResetDatabase;
    @FXML private VBox vboxEraseOptions;
    
    @FXML private Button btnAnalyze;
    @FXML private Button btnCleanup;
    @FXML private Button btnErase;
    @FXML private Button btnExport;
    @FXML private ProgressBar progressBar;
    @FXML private Label txtProgress;
    
    @FXML private GridPane statsGrid;
    @FXML private Label lblDuplicates;
    @FXML private Label lblUids;
    @FXML private Label lblFields;
    @FXML private Label lblInconsistencies;
    
    @FXML private TextArea taResults;
    
    private ProductCleanupTool cleanupTool;
    private ProductCleanupTool.CleanupResult lastResult;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cleanupTool = new ProductCleanupTool();
        
        // Listener pour le mode dry run
        cbDryRun.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStates();
        });
        
        // Listeners pour les options d'effacement
        cbResetDatabase.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Si effacement complet sélectionné, désélectionner les autres
                cbEraseInterventions.setSelected(false);
                cbEraseProducts.setSelected(false);
                cbEraseCategories.setSelected(false);
                cbEraseSocietes.setSelected(false);
                vboxEraseOptions.getChildren().stream()
                    .filter(node -> node != cbResetDatabase)
                    .forEach(node -> ((CheckBox) node).setDisable(true));
            } else {
                // Réactiver les autres options
                vboxEraseOptions.getChildren().stream()
                    .filter(node -> node != cbResetDatabase)
                    .forEach(node -> ((CheckBox) node).setDisable(false));
            }
            updateButtonStates();
        });
        
        // Listeners pour les autres options d'effacement
        cbEraseInterventions.selectedProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        cbEraseProducts.selectedProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        cbEraseCategories.selectedProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
        cbEraseSocietes.selectedProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
    }
    
    @FXML
    private void onAnalyze() {
        runAnalysis(true); // Toujours en mode simulation pour l'analyse
    }
    
    @FXML
    private void onCleanup() {
        if (cbDryRun.isSelected()) {
            showAlert("Mode simulation", "Vous êtes en mode simulation. Décochez 'Mode simulation' pour appliquer les corrections.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Appliquer les corrections?");
        confirmation.setContentText("Cette action modifiera définitivement votre base de données. Êtes-vous sûr?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runAnalysis(false); // Mode correction
            }
        });
    }
    
    @FXML
    private void onErase() {
        if (cbDryRun.isSelected()) {
            showAlert("Mode simulation", "Vous êtes en mode simulation. Décochez 'Mode simulation' pour appliquer l'effacement.");
            return;
        }
        
        // Vérifier qu'au moins une option d'effacement est sélectionnée
        if (!hasEraseOptions()) {
            showAlert("Aucune option", "Veuillez sélectionner au moins une option d'effacement.");
            return;
        }
        
        // Double confirmation pour l'effacement
        String warningMessage = buildEraseWarning();
        
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("⚠️ ATTENTION - EFFACEMENT DE DONNÉES");
        warning.setHeaderText("CETTE ACTION EST IRRÉVERSIBLE!");
        warning.setContentText(warningMessage);
        
        ButtonType confirmButton = new ButtonType("Je comprends, EFFACER", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        warning.getButtonTypes().setAll(confirmButton, cancelButton);
        
        warning.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                // Dernière confirmation
                Alert finalConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                finalConfirm.setTitle("Confirmation finale");
                finalConfirm.setHeaderText("Êtes-vous absolument certain?");
                finalConfirm.setContentText("Tapez 'EFFACER' pour confirmer l'effacement définitif des données.");
                
                TextInputDialog textInput = new TextInputDialog();
                textInput.setTitle("Confirmation textuelle");
                textInput.setHeaderText("Tapez 'EFFACER' pour confirmer:");
                textInput.setContentText("Confirmation:");
                
                textInput.showAndWait().ifPresent(input -> {
                    if ("EFFACER".equals(input.trim().toUpperCase())) {
                        runErase();
                    } else {
                        showAlert("Annulé", "Effacement annulé - confirmation incorrecte.");
                    }
                });
            }
        });
    }

    @FXML
    private void onExportReport() {
        if (lastResult == null) {
            showAlert("Aucun résultat", "Veuillez d'abord exécuter une analyse.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport de nettoyage");
        fileChooser.setInitialFileName("nettoyage_produits_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        
        Stage stage = (Stage) btnExport.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            exportReport(file);
        }
    }
    
    private void runAnalysis(boolean dryRun) {
        // Désactiver les boutons pendant l'analyse
        setButtonsEnabled(false);
        taResults.setText("Analyse en cours...\n");
        statsGrid.setVisible(false);
        
        Task<ProductCleanupTool.CleanupResult> task = new Task<ProductCleanupTool.CleanupResult>() {
            @Override
            protected ProductCleanupTool.CleanupResult call() throws Exception {
                return cleanupTool.performFullCleanup(dryRun);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    lastResult = getValue();
                    displayResults(lastResult);
                    setButtonsEnabled(true);
                    updateButtonStates();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    taResults.setText("Erreur pendant l'analyse: " + getException().getMessage());
                    setButtonsEnabled(true);
                });
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void displayResults(ProductCleanupTool.CleanupResult result) {
        // Mettre à jour les statistiques
        lblDuplicates.setText(String.valueOf(result.getDuplicatesFound()));
        lblUids.setText(String.valueOf(result.getUidsGenerated()));
        lblFields.setText(String.valueOf(result.getEmptyFieldsFixed()));
        lblInconsistencies.setText(String.valueOf(result.getInconsistenciesFixed()));
        
        statsGrid.setVisible(true);
        
        // Construire le rapport détaillé
        StringBuilder report = new StringBuilder();
        report.append(result.getSummary()).append("\n");
        
        if (!result.getIssues().isEmpty()) {
            report.append("\n=== PROBLÈMES DÉTECTÉS ===\n");
            result.getIssues().forEach(issue -> report.append("⚠️  ").append(issue).append("\n"));
        }
        
        if (!result.getFixes().isEmpty()) {
            report.append("\n=== CORRECTIONS APPLIQUÉES ===\n");
            result.getFixes().forEach(fix -> report.append("✅ ").append(fix).append("\n"));
        }
        
        if (!result.hasIssues()) {
            report.append("\n✅ Aucun problème détecté ! Votre base de données est propre.\n");
        }
        
        taResults.setText(report.toString());
        
        // Activer le bouton d'export
        btnExport.setDisable(false);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        btnAnalyze.setDisable(!enabled);
        btnCleanup.setDisable(!enabled);
        // btnExport reste dans son état actuel
    }
    
    private void exportReport(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("RAPPORT DE NETTOYAGE DES PRODUITS\n");
            writer.write("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")) + "\n");
            writer.write("=====================================\n\n");
            writer.write(taResults.getText());
            
            showAlert("Export réussi", "Le rapport a été exporté vers: " + file.getAbsolutePath());
            
        } catch (IOException e) {
            showAlert("Erreur d'export", "Impossible d'exporter le rapport: " + e.getMessage());
        }
    }
    
    private void updateButtonStates() {
        // Bouton Nettoyer - toujours disponible
        btnCleanup.setDisable(false);
        
        // Bouton Effacer - uniquement si au moins une option d'effacement est sélectionnée
        // et que nous ne sommes pas en mode dry run
        boolean hasEraseOption = cbEraseInterventions.isSelected() || 
                                cbEraseProducts.isSelected() || 
                                cbEraseCategories.isSelected() || 
                                cbEraseSocietes.isSelected() || 
                                cbResetDatabase.isSelected();
        
        btnErase.setDisable(!hasEraseOption);
    }
    
    private boolean hasEraseOptions() {
        return cbEraseInterventions.isSelected() || 
               cbEraseProducts.isSelected() || 
               cbEraseCategories.isSelected() || 
               cbEraseSocietes.isSelected() || 
               cbResetDatabase.isSelected();
    }
    
    private String buildEraseWarning() {
        StringBuilder warning = new StringBuilder();
        warning.append("ATTENTION: Cette action va DÉFINITIVEMENT SUPPRIMER les données suivantes:\n\n");
        
        if (cbResetDatabase.isSelected()) {
            warning.append("🔥 REMISE À ZÉRO COMPLÈTE DE LA BASE DE DONNÉES\n");
            warning.append("   → Toutes les tables seront vidées!\n\n");
        } else {
            if (cbEraseInterventions.isSelected()) {
                warning.append("• Toutes les interventions et demandes SAV\n");
            }
            if (cbEraseProducts.isSelected()) {
                warning.append("• Tous les produits\n");
            }
            if (cbEraseCategories.isSelected()) {
                warning.append("• Toutes les catégories\n");
            }
            if (cbEraseSocietes.isSelected()) {
                warning.append("• Toutes les sociétés (fabricants/fournisseurs)\n");
            }
        }
        
        warning.append("\n⚠️ AUCUNE SAUVEGARDE AUTOMATIQUE NE SERA CRÉÉE!");
        warning.append("\n⚠️ CES DONNÉES NE POURRONT PAS ÊTRE RÉCUPÉRÉES!");
        
        return warning.toString();
    }
    
    private void runErase() {
        try {
            progressBar.setVisible(true);
            txtProgress.setVisible(true);
            txtProgress.setText("Effacement en cours...");
            btnErase.setDisable(true);
            btnCleanup.setDisable(true);
            
            Platform.runLater(() -> {
                try {
                    ProductCleanupTool.EraseOptions options = new ProductCleanupTool.EraseOptions();
                    options.eraseInterventions = cbEraseInterventions.isSelected();
                    options.eraseProducts = cbEraseProducts.isSelected();
                    options.eraseCategories = cbEraseCategories.isSelected();
                    options.eraseSocietes = cbEraseSocietes.isSelected();
                    options.resetDatabase = cbResetDatabase.isSelected();
                    
                    ProductCleanupTool.EraseResult result = cleanupTool.eraseData(options, false);
                    
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        txtProgress.setVisible(false);
                        btnErase.setDisable(false);
                        btnCleanup.setDisable(false);
                        
                        StringBuilder message = new StringBuilder();
                        message.append("Effacement terminé!\n\n");
                        
                        if (result.getTotalDeleted() > 0) {
                            message.append("📊 Résumé de l'effacement:\n");
                            
                            // Afficher les comptes par table
                            Map<String, Integer> deletedCounts = result.getDeletedCounts();
                            for (Map.Entry<String, Integer> entry : deletedCounts.entrySet()) {
                                String table = entry.getKey();
                                int count = entry.getValue();
                                String tableFr = switch(table) {
                                    case "interventions" -> "interventions";
                                    case "demandes_sav" -> "demandes SAV";
                                    case "produits" -> "produits";
                                    case "categories" -> "catégories";
                                    case "fabricants" -> "fabricants";
                                    case "fournisseurs" -> "fournisseurs";
                                    default -> table;
                                };
                                message.append(String.format("• %d %s supprimé(e)s\n", count, tableFr));
                            }
                            
                            message.append(String.format("\n🗑️ Total: %d enregistrements supprimés", result.getTotalDeleted()));
                        } else {
                            message.append("Aucune donnée n'a été trouvée à supprimer.");
                        }
                        
                        if (result.hasErrors()) {
                            message.append("\n\n⚠️ Certaines erreurs se sont produites pendant l'effacement.");
                        }
                        
                        showAlert("Effacement terminé", message.toString());
                        
                        // Réinitialiser les options après effacement
                        resetEraseOptions();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        txtProgress.setVisible(false);
                        btnErase.setDisable(false);
                        btnCleanup.setDisable(false);
                        
                        showAlert("Erreur d'effacement", 
                                "Une erreur s'est produite pendant l'effacement: " + e.getMessage());
                    });
                }
            });
            
        } catch (Exception e) {
            progressBar.setVisible(false);
            txtProgress.setVisible(false);
            btnErase.setDisable(false);
            btnCleanup.setDisable(false);
            
            showAlert("Erreur", "Erreur lors de l'effacement: " + e.getMessage());
        }
    }
    
    private void resetEraseOptions() {
        cbEraseInterventions.setSelected(false);
        cbEraseProducts.setSelected(false);
        cbEraseCategories.setSelected(false);
        cbEraseSocietes.setSelected(false);
        cbResetDatabase.setSelected(false);
        updateButtonStates();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}