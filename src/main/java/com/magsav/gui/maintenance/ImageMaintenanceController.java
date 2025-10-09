package com.magsav.gui.maintenance;

import com.magsav.service.ImageMaintenanceService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.ArrayList;

/**
 * Contrôleur pour la maintenance des images
 */
public class ImageMaintenanceController {
    
    @FXML private Button btnNormalizeImages;
    @FXML private Button btnNormalizeLogos;
    @FXML private Button btnNormalizeAll;
    @FXML private Button btnRemoveDuplicates;
    @FXML private Button btnFullMaintenance;
    @FXML private TextArea textAreaLog;
    @FXML private ProgressBar progressBar;
    @FXML private Label labelStatus;
    @FXML private VBox mainContainer;
    
    // Nouveaux composants pour la suppression
    @FXML private ListView<String> listViewPhotos;
    @FXML private ListView<String> listViewLogos;
    @FXML private Button btnLoadPhotos;
    @FXML private Button btnSelectAllPhotos;
    @FXML private Button btnSelectNonePhotos;
    @FXML private Button btnDeletePhotos;
    @FXML private Button btnLoadLogos;
    @FXML private Button btnSelectAllLogos;
    @FXML private Button btnSelectNoneLogos;
    @FXML private Button btnDeleteLogos;
    
    private ImageMaintenanceService maintenanceService;
    
    @FXML
    public void initialize() {
        maintenanceService = new ImageMaintenanceService();
        setupUI();
    }
    
    private void setupUI() {
        // Configuration de l'interface
        textAreaLog.setEditable(false);
        textAreaLog.setWrapText(true);
        progressBar.setVisible(false);
        labelStatus.setText("Prêt pour la maintenance des images");
        
        // Configuration des boutons
        btnNormalizeImages.setOnAction(e -> normalizeImages());
        btnNormalizeLogos.setOnAction(e -> normalizeLogos());
        btnNormalizeAll.setOnAction(e -> normalizeAll());
        btnRemoveDuplicates.setOnAction(e -> removeDuplicates());
        btnFullMaintenance.setOnAction(e -> performFullMaintenance());
        
        // Configuration des nouveaux boutons de suppression
        btnLoadPhotos.setOnAction(e -> loadPhotosList());
        btnSelectAllPhotos.setOnAction(e -> selectAllPhotos());
        btnSelectNonePhotos.setOnAction(e -> selectNonePhotos());
        btnDeletePhotos.setOnAction(e -> deleteSelectedPhotos());
        
        btnLoadLogos.setOnAction(e -> loadLogosList());
        btnSelectAllLogos.setOnAction(e -> selectAllLogos());
        btnSelectNoneLogos.setOnAction(e -> selectNoneLogos());
        btnDeleteLogos.setOnAction(e -> deleteSelectedLogos());
        
        // Configuration des ListView avec des cellules à cases à cocher
        setupListViewWithCheckboxes(listViewPhotos);
        setupListViewWithCheckboxes(listViewLogos);
    }
    
    @FXML
    private void normalizeImages() {
        runMaintenanceTask(
            "Normalisation des images",
            () -> maintenanceService.normalizeExistingImages()
        );
    }
    
    @FXML
    private void normalizeLogos() {
        runMaintenanceTask(
            "Normalisation des logos",
            () -> maintenanceService.normalizeExistingLogos()
        );
    }
    
    @FXML
    private void normalizeAll() {
        runMaintenanceTask(
            "Normalisation complète (Images & Logos)",
            () -> {
                var imagesReport = maintenanceService.normalizeExistingImages();
                var logosReport = maintenanceService.normalizeExistingLogos();
                imagesReport.merge(logosReport);
                return imagesReport;
            }
        );
    }
    
    @FXML
    private void removeDuplicates() {
        // Demander confirmation avant suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Suppression des doublons");
        confirmation.setContentText(
            "Cette opération va supprimer définitivement les images dupliquées.\n" +
            "Voulez-vous continuer ?"
        );
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runMaintenanceTask(
                    "Suppression des doublons",
                    () -> maintenanceService.detectAndRemoveDuplicates()
                );
            }
        });
    }
    
    @FXML
    private void performFullMaintenance() {
        // Demander confirmation pour maintenance complète
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Maintenance complète");
        confirmation.setContentText(
            "Cette opération va :\n" +
            "1. Normaliser toutes les images\n" +
            "2. Supprimer les doublons\n\n" +
            "Voulez-vous continuer ?"
        );
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runMaintenanceTask(
                    "Maintenance complète",
                    () -> maintenanceService.performFullMaintenance()
                );
            }
        });
    }
    
    private void runMaintenanceTask(String taskName, MaintenanceTaskSupplier taskSupplier) {
        // Désactiver les boutons pendant le traitement
        setButtonsDisabled(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Mode indéterminé
        labelStatus.setText(taskName + " en cours...");
        textAreaLog.clear();
        
        Task<ImageMaintenanceService.MaintenanceReport> task = new Task<>() {
            @Override
            protected ImageMaintenanceService.MaintenanceReport call() throws Exception {
                return taskSupplier.get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ImageMaintenanceService.MaintenanceReport report = getValue();
                    displayReport(report);
                    setButtonsDisabled(false);
                    progressBar.setVisible(false);
                    labelStatus.setText("Maintenance terminée");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    textAreaLog.appendText("Erreur lors de la maintenance: " + getException().getMessage());
                    setButtonsDisabled(false);
                    progressBar.setVisible(false);
                    labelStatus.setText("Erreur");
                });
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void displayReport(ImageMaintenanceService.MaintenanceReport report) {
        StringBuilder sb = new StringBuilder();
        
        // Résumé en haut
        sb.append("=== RÉSUMÉ ===\n");
        sb.append("Images totales: ").append(report.totalImages).append("\n");
        sb.append("Images normalisées: ").append(report.normalizedImages).append("\n");
        sb.append("Groupes de doublons: ").append(report.duplicateGroups).append("\n");
        sb.append("Doublons supprimés: ").append(report.removedDuplicates).append("\n");
        sb.append("Erreurs: ").append(report.errors).append("\n\n");
        
        // Détails
        sb.append("=== DÉTAILS ===\n");
        for (String message : report.getMessages()) {
            sb.append(message).append("\n");
        }
        
        textAreaLog.setText(sb.toString());
        
        // Faire défiler vers le haut pour voir le résumé
        textAreaLog.setScrollTop(0);
    }
    
    // ===== MÉTHODES DE SUPPRESSION =====
    
    private void setupListViewWithCheckboxes(ListView<String> listView) {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    private void loadPhotosList() {
        try {
            var photos = maintenanceService.listAvailablePhotos();
            listViewPhotos.getItems().clear();
            listViewPhotos.getItems().addAll(photos);
            textAreaLog.appendText("Photos chargées: " + photos.size() + " fichiers trouvés\n");
        } catch (Exception e) {
            textAreaLog.appendText("Erreur lors du chargement des photos: " + e.getMessage() + "\n");
        }
    }
    
    private void loadLogosList() {
        try {
            var logos = maintenanceService.listAvailableLogos();
            listViewLogos.getItems().clear();
            listViewLogos.getItems().addAll(logos);
            textAreaLog.appendText("Logos chargés: " + logos.size() + " fichiers trouvés\n");
        } catch (Exception e) {
            textAreaLog.appendText("Erreur lors du chargement des logos: " + e.getMessage() + "\n");
        }
    }
    
    private void selectAllPhotos() {
        listViewPhotos.getSelectionModel().selectAll();
    }
    
    private void selectNonePhotos() {
        listViewPhotos.getSelectionModel().clearSelection();
    }
    
    private void selectAllLogos() {
        listViewLogos.getSelectionModel().selectAll();
    }
    
    private void selectNoneLogos() {
        listViewLogos.getSelectionModel().clearSelection();
    }
    
    private void deleteSelectedPhotos() {
        var selectedPhotos = getSelectedItems(listViewPhotos);
        if (selectedPhotos.isEmpty()) {
            textAreaLog.appendText("Aucune photo sélectionnée pour suppression\n");
            return;
        }
        
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer les photos sélectionnées");
        confirmation.setContentText(
            "Vous êtes sur le point de supprimer " + selectedPhotos.size() + " photo(s).\n" +
            "Cette action est IRRÉVERSIBLE et supprimera également toutes les versions normalisées.\n\n" +
            "Voulez-vous continuer ?"
        );
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runDeletionTask("Suppression de photos", () -> {
                    return maintenanceService.deleteSelectedPhotos(selectedPhotos);
                }, () -> loadPhotosList());
            }
        });
    }
    
    private void deleteSelectedLogos() {
        var selectedLogos = getSelectedItems(listViewLogos);
        if (selectedLogos.isEmpty()) {
            textAreaLog.appendText("Aucun logo sélectionné pour suppression\n");
            return;
        }
        
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer les logos sélectionnés");
        confirmation.setContentText(
            "Vous êtes sur le point de supprimer " + selectedLogos.size() + " logo(s).\n" +
            "Cette action est IRRÉVERSIBLE et supprimera également toutes les versions normalisées.\n\n" +
            "Voulez-vous continuer ?"
        );
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runDeletionTask("Suppression de logos", () -> {
                    return maintenanceService.deleteSelectedLogos(selectedLogos);
                }, () -> loadLogosList());
            }
        });
    }
    
    private List<String> getSelectedItems(ListView<String> listView) {
        return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
    }
    
    private void runDeletionTask(String taskName, MaintenanceTaskSupplier taskSupplier, Runnable onSuccess) {
        setButtonsDisabled(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        labelStatus.setText(taskName + " en cours...");
        
        Task<ImageMaintenanceService.MaintenanceReport> task = new Task<>() {
            @Override
            protected ImageMaintenanceService.MaintenanceReport call() throws Exception {
                return taskSupplier.get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ImageMaintenanceService.MaintenanceReport report = getValue();
                    displayReport(report);
                    setButtonsDisabled(false);
                    progressBar.setVisible(false);
                    labelStatus.setText("Suppression terminée");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    textAreaLog.appendText("Erreur lors de la suppression: " + getException().getMessage() + "\n");
                    setButtonsDisabled(false);
                    progressBar.setVisible(false);
                    labelStatus.setText("Erreur");
                });
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void setButtonsDisabled(boolean disabled) {
        btnNormalizeImages.setDisable(disabled);
        btnNormalizeLogos.setDisable(disabled);
        btnNormalizeAll.setDisable(disabled);
        btnRemoveDuplicates.setDisable(disabled);
        btnFullMaintenance.setDisable(disabled);
        
        // Nouveaux boutons de suppression
        btnLoadPhotos.setDisable(disabled);
        btnSelectAllPhotos.setDisable(disabled);
        btnSelectNonePhotos.setDisable(disabled);
        btnDeletePhotos.setDisable(disabled);
        btnLoadLogos.setDisable(disabled);
        btnSelectAllLogos.setDisable(disabled);
        btnSelectNoneLogos.setDisable(disabled);
        btnDeleteLogos.setDisable(disabled);
    }
    
    @FunctionalInterface
    private interface MaintenanceTaskSupplier {
        ImageMaintenanceService.MaintenanceReport get() throws Exception;
    }
}