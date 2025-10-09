package com.magsav.gui.management;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import com.magsav.util.AppLogger;
import com.magsav.imports.MediaImporter;
import com.magsav.imports.MediaImporter.MediaType;
import com.magsav.service.ImageMaintenanceService;
import com.magsav.ui.components.AlertManager;
import com.magsav.ui.components.NotificationManager;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion unifée des médias
 * Combine import, normalisation et maintenance en une seule interface
 */
public class MediaManagementController implements Initializable {
    
    @FXML private VBox mainContainer;
    
    // Boutons Import
    @FXML private Button btnImportPhotos;
    @FXML private Button btnImportLogos;
    

    
    // Boutons Maintenance
    @FXML private Button btnRemoveDuplicates;
    @FXML private Button btnFullMaintenance;
    
    // Gestion des fichiers - Photos
    @FXML private Button btnLoadPhotos;
    @FXML private Button btnSelectAllPhotos;
    @FXML private Button btnSelectNonePhotos;
    @FXML private Button btnDeletePhotos;
    @FXML private ListView<String> listViewPhotos;
    
    // Gestion des fichiers - Logos
    @FXML private Button btnLoadLogos;
    @FXML private Button btnSelectAllLogos;
    @FXML private Button btnSelectNoneLogos;
    @FXML private Button btnDeleteLogos;
    @FXML private ListView<String> listViewLogos;
    
    // Log
    @FXML private TextArea textAreaLog;
    
    private ObservableList<String> photosList = FXCollections.observableArrayList();
    private ObservableList<String> logosList = FXCollections.observableArrayList();
    
    private final MediaImporter mediaImporter = new MediaImporter();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppLogger.info("gui", "MediaManagementController: Initialisation");
        
        // Configuration des listes
        listViewPhotos.setItems(photosList);
        listViewLogos.setItems(logosList);
        
        // Configuration de la sélection multiple
        listViewPhotos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewLogos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Configuration du glisser-déposer
        setupDragAndDrop();
        
        logMessage("Interface de gestion des médias initialisée.");
        
        // Charger automatiquement les listes de fichiers
        loadPhotosList();
        loadLogosList();
    }
    
    // === MÉTHODES D'IMPORT ===
    
    @FXML
    private void importPhotos() {
        logMessage("Ouverture du sélecteur de fichiers pour les photos...");
        AppLogger.info("gui", "MediaManagement: Import photos demandé");
        
        // Créer un FileChooser pour sélectionner les images
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner des photos à importer");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new javafx.stage.FileChooser.ExtensionFilter("PNG", "*.png"),
            new javafx.stage.FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new javafx.stage.FileChooser.ExtensionFilter("Tous fichiers", "*.*")
        );
        
        // Permettre la sélection multiple
        java.util.List<java.io.File> selectedFiles = fileChooser.showOpenMultipleDialog(
            mainContainer.getScene().getWindow()
        );
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            logMessage("Fichiers sélectionnés: " + selectedFiles.size());
            for (java.io.File file : selectedFiles) {
                logMessage("- " + file.getName() + " (" + formatFileSize(file.length()) + ")");
            }
            
            // Import asynchrone
            importFilesAsync(selectedFiles, MediaType.PHOTOS);
            
        } else {
            logMessage("Aucun fichier sélectionné.");
        }
    }
    
    @FXML
    private void importLogos() {
        logMessage("Ouverture du sélecteur de fichiers pour les logos...");
        AppLogger.info("gui", "MediaManagement: Import logos demandé");
        
        // Créer un FileChooser pour sélectionner les logos
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner des logos à importer");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new javafx.stage.FileChooser.ExtensionFilter("PNG", "*.png"),
            new javafx.stage.FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new javafx.stage.FileChooser.ExtensionFilter("Tous fichiers", "*.*")
        );
        
        // Permettre la sélection multiple
        java.util.List<java.io.File> selectedFiles = fileChooser.showOpenMultipleDialog(
            mainContainer.getScene().getWindow()
        );
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            logMessage("Fichiers sélectionnés: " + selectedFiles.size());
            for (java.io.File file : selectedFiles) {
                logMessage("- " + file.getName() + " (" + formatFileSize(file.length()) + ")");
            }
            
            // Import asynchrone
            importFilesAsync(selectedFiles, MediaType.LOGOS);
            
        } else {
            logMessage("Aucun fichier sélectionné.");
        }
    }
    
    // === MÉTHODES DE NORMALISATION ===
    

    

    

    
    // === MÉTHODES DE MAINTENANCE ===
    
    @FXML
    private void removeDuplicates() {
        logMessage("Suppression des doublons - Fonctionnalité en cours de développement");
        AppLogger.info("gui", "MediaManagement: Suppression doublons demandée");
    }
    
    @FXML
    private void performFullMaintenance() {
        logMessage("Maintenance complète - Fonctionnalité en cours de développement");
        AppLogger.info("gui", "MediaManagement: Maintenance complète demandée");
    }
    
    // === MÉTHODES DE GESTION DES FICHIERS - PHOTOS ===
    
    @FXML
    private void loadPhotosList() {
        logMessage("Chargement de la liste des photos...");
        photosList.clear();
        
        // Utiliser le vrai répertoire de photos du MediaImporter
        File photosDir = mediaImporter.getMediaDirectory(MediaType.PHOTOS).toFile();
        if (photosDir.exists() && photosDir.isDirectory()) {
            File[] files = photosDir.listFiles((dir, name) -> 
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|tiff)$"));
            if (files != null) {
                for (File file : files) {
                    photosList.add(file.getName());
                }
            }
        }
        
        logMessage("Liste des photos chargée: " + photosList.size() + " fichiers trouvés.");
        logMessage("Répertoire: " + photosDir.getAbsolutePath());
    }
    
    @FXML
    private void selectAllPhotos() {
        listViewPhotos.getSelectionModel().selectAll();
        logMessage("Toutes les photos sélectionnées.");
    }
    
    @FXML
    private void selectNonePhotos() {
        listViewPhotos.getSelectionModel().clearSelection();
        logMessage("Sélection des photos effacée.");
    }
    
    @FXML
    private void deleteSelectedPhotos() {
        var selected = listViewPhotos.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            logMessage("Aucune photo sélectionnée pour suppression.");
            return;
        }
        
        // Confirmation de suppression
        if (!AlertManager.MAGSAV.confirmDelete("les photos sélectionnées", String.valueOf(selected.size()) + " fichiers")) {
            logMessage("Suppression annulée par l'utilisateur.");
            return;
        }
        
        AppLogger.info("gui", "MediaManagement: Suppression photos demandée - " + selected.size() + " fichiers");
        logMessage("Suppression en cours de " + selected.size() + " photos...");
        
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> photoNames = new ArrayList<>(selected);
                ImageMaintenanceService maintenanceService = new ImageMaintenanceService();
                
                try {
                    var report = maintenanceService.deleteSelectedPhotos(photoNames);
                    
                    Platform.runLater(() -> {
                        if (report.normalizedImages > 0) {
                            logMessage("Suppression réussie: " + report.normalizedImages + " photo(s) supprimée(s).");
                            NotificationManager.MAGSAV.itemDeleted("Photos (" + report.normalizedImages + " fichiers)");
                            // Recharger la liste des photos
                            loadPhotosList();
                        } else {
                            logMessage("Aucun fichier n'a pu être supprimé.");
                        }
                        
                        if (report.errors > 0) {
                            logMessage("Erreurs rencontrées (" + report.errors + "):");
                            for (String message : report.getMessages()) {
                                if (message.startsWith("✗")) {
                                    logMessage("- " + message.substring(2));
                                }
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        logMessage("Erreur lors de la suppression: " + e.getMessage());
                        AppLogger.error("MediaManagement", "Erreur suppression photos", e);
                    });
                }
                
                return null;
            }
        };
        
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }
    
    // === MÉTHODES DE GESTION DES FICHIERS - LOGOS ===
    
    @FXML
    private void loadLogosList() {
        logMessage("Chargement de la liste des logos...");
        logosList.clear();
        
        // Utiliser le vrai répertoire de logos du MediaImporter
        File logosDir = mediaImporter.getMediaDirectory(MediaType.LOGOS).toFile();
        if (logosDir.exists() && logosDir.isDirectory()) {
            File[] files = logosDir.listFiles((dir, name) -> 
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|svg|pdf)$"));
            if (files != null) {
                for (File file : files) {
                    logosList.add(file.getName());
                }
            }
        }
        
        logMessage("Liste des logos chargée: " + logosList.size() + " fichiers trouvés.");
        logMessage("Répertoire: " + logosDir.getAbsolutePath());
    }
    
    @FXML
    private void selectAllLogos() {
        listViewLogos.getSelectionModel().selectAll();
        logMessage("Tous les logos sélectionnés.");
    }
    
    @FXML
    private void selectNoneLogos() {
        listViewLogos.getSelectionModel().clearSelection();
        logMessage("Sélection des logos effacée.");
    }
    
    @FXML
    private void deleteSelectedLogos() {
        var selected = listViewLogos.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            logMessage("Aucun logo sélectionné pour suppression.");
            return;
        }
        
        // Confirmation de suppression
        if (!AlertManager.MAGSAV.confirmDelete("les logos sélectionnés", String.valueOf(selected.size()) + " fichiers")) {
            logMessage("Suppression annulée par l'utilisateur.");
            return;
        }
        
        AppLogger.info("gui", "MediaManagement: Suppression logos demandée - " + selected.size() + " fichiers");
        logMessage("Suppression en cours de " + selected.size() + " logo(s)...");
        
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> logoNames = new ArrayList<>(selected);
                ImageMaintenanceService maintenanceService = new ImageMaintenanceService();
                
                try {
                    var report = maintenanceService.deleteSelectedLogos(logoNames);
                    
                    Platform.runLater(() -> {
                        if (report.normalizedImages > 0) {
                            logMessage("Suppression réussie: " + report.normalizedImages + " logo(s) supprimé(s).");
                            NotificationManager.MAGSAV.itemDeleted("Logos (" + report.normalizedImages + " fichiers)");
                            // Recharger la liste des logos
                            loadLogosList();
                        } else {
                            logMessage("Aucun fichier n'a pu être supprimé.");
                        }
                        
                        if (report.errors > 0) {
                            logMessage("Erreurs rencontrées (" + report.errors + "):");
                            for (String message : report.getMessages()) {
                                if (message.startsWith("✗")) {
                                    logMessage("- " + message.substring(2));
                                }
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        logMessage("Erreur lors de la suppression: " + e.getMessage());
                        AppLogger.error("MediaManagement", "Erreur suppression logos", e);
                    });
                }
                
                return null;
            }
        };
        
        Thread deleteThread = new Thread(deleteTask);
        deleteThread.setDaemon(true);
        deleteThread.start();
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private void logMessage(String message) {
        String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        String formattedMessage = "[" + timestamp + "] " + message + "\n";
        
        if (textAreaLog != null) {
            textAreaLog.appendText(formattedMessage);
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int unit = 1024;
        if (bytes < unit * unit) return String.format("%.1f KB", bytes / (double) unit);
        if (bytes < unit * unit * unit) return String.format("%.1f MB", bytes / (double) (unit * unit));
        return String.format("%.1f GB", bytes / (double) (unit * unit * unit));
    }
    
    private void importFilesAsync(List<File> files, MediaType mediaType) {
        String typeLabel = mediaType == MediaType.PHOTOS ? "photos" : "logos";
        logMessage("Début de l'import des " + typeLabel + "...");
        
        // Désactiver les boutons pendant l'import
        btnImportPhotos.setDisable(true);
        btnImportLogos.setDisable(true);
        
        Task<MediaImporter.Result> importTask = new Task<MediaImporter.Result>() {
            @Override
            protected MediaImporter.Result call() throws Exception {
                List<Path> filePaths = files.stream()
                    .map(File::toPath)
                    .collect(Collectors.toList());
                    
                return mediaImporter.importFiles(filePaths, mediaType);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    MediaImporter.Result result = getValue();
                    
                    logMessage("Import terminé:");
                    logMessage("- Fichiers traités: " + result.filesProcessed());
                    logMessage("- Fichiers importés: " + result.filesImported());
                    logMessage("- Répertoire: " + result.targetDirectory());
                    
                    if (!result.errors().isEmpty()) {
                        logMessage("Erreurs rencontrées:");
                        for (String error : result.errors()) {
                            logMessage("- " + error);
                        }
                    }
                    
                    if (result.filesImported() > 0) {
                        logMessage("✓ Import réussi de " + result.filesImported() + " " + typeLabel);
                        AppLogger.info("gui", "Import réussi: " + result.filesImported() + " " + typeLabel);
                        
                        // Normalisation automatique après import réussi
                        logMessage("Début de la normalisation automatique...");
                        
                        Task<ImageMaintenanceService.MaintenanceReport> normalizeTask = new Task<ImageMaintenanceService.MaintenanceReport>() {
                            @Override
                            protected ImageMaintenanceService.MaintenanceReport call() throws Exception {
                                ImageMaintenanceService maintenanceService = new ImageMaintenanceService();
                                if (mediaType == MediaType.PHOTOS) {
                                    return maintenanceService.normalizeExistingImages();
                                } else {
                                    return maintenanceService.normalizeExistingLogos();
                                }
                            }
                            
                            @Override
                            protected void succeeded() {
                                Platform.runLater(() -> {
                                    ImageMaintenanceService.MaintenanceReport normalizeReport = getValue();
                                    if (normalizeReport.normalizedImages > 0) {
                                        logMessage("✓ Normalisation automatique terminée: " + normalizeReport.normalizedImages + " fichier(s) normalisé(s)");
                                    } else {
                                        logMessage("✓ Normalisation automatique terminée: aucun fichier à normaliser");
                                    }
                                    
                                    // Actualiser la liste appropriée après normalisation
                                    if (mediaType == MediaType.PHOTOS) {
                                        loadPhotosList();
                                    } else {
                                        loadLogosList();
                                    }
                                });
                            }
                            
                            @Override
                            protected void failed() {
                                Platform.runLater(() -> {
                                    logMessage("⚠ Erreur lors de la normalisation automatique: " + getException().getMessage());
                                    
                                    // Actualiser la liste même en cas d'erreur de normalisation
                                    if (mediaType == MediaType.PHOTOS) {
                                        loadPhotosList();
                                    } else {
                                        loadLogosList();
                                    }
                                });
                            }
                        };
                        
                        Thread normalizeThread = new Thread(normalizeTask);
                        normalizeThread.setDaemon(true);
                        normalizeThread.start();
                    }
                    
                    // Réactiver les boutons
                    btnImportPhotos.setDisable(false);
                    btnImportLogos.setDisable(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable exception = getException();
                    logMessage("Erreur lors de l'import: " + exception.getMessage());
                    AppLogger.error("gui", "Erreur import " + typeLabel, exception);
                    
                    // Réactiver les boutons
                    btnImportPhotos.setDisable(false);
                    btnImportLogos.setDisable(false);
                });
            }
        };
        
        // Exécuter la tâche en arrière-plan
        Thread importThread = new Thread(importTask);
        importThread.setDaemon(true);
        importThread.start();
    }
    
    /**
     * Configure le support du glisser-déposer pour l'import de fichiers
     */
    private void setupDragAndDrop() {
        if (mainContainer == null) return;
        
        // Gérer les événements de glisser-déposer sur le conteneur principal
        mainContainer.setOnDragOver(event -> {
            if (event.getGestureSource() != mainContainer && event.getDragboard().hasFiles()) {
                // Vérifier si ce sont des fichiers images
                List<File> files = event.getDragboard().getFiles();
                boolean hasImageFiles = files.stream()
                    .anyMatch(file -> isImageFile(file.getName().toLowerCase()));
                
                if (hasImageFiles) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                    
                    // Feedback visuel
                    mainContainer.setStyle("-fx-background-color: rgba(0, 120, 215, 0.1); -fx-border-color: #0078d7; -fx-border-width: 2; -fx-border-style: dashed;");
                }
            }
            event.consume();
        });
        
        mainContainer.setOnDragExited(event -> {
            // Supprimer le feedback visuel
            mainContainer.setStyle("");
            event.consume();
        });
        
        mainContainer.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasFiles()) {
                List<File> droppedFiles = dragboard.getFiles();
                List<File> imageFiles = droppedFiles.stream()
                    .filter(file -> isImageFile(file.getName().toLowerCase()))
                    .collect(Collectors.toList());
                
                if (!imageFiles.isEmpty()) {
                    // Afficher une boîte de dialogue pour choisir le type d'import
                    javafx.scene.control.ButtonType photos = new javafx.scene.control.ButtonType("Photos", javafx.scene.control.ButtonBar.ButtonData.LEFT);
                    javafx.scene.control.ButtonType logos = new javafx.scene.control.ButtonType("Logos", javafx.scene.control.ButtonBar.ButtonData.RIGHT);
                    javafx.scene.control.ButtonType annuler = javafx.scene.control.ButtonType.CANCEL;
                    
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.NONE, "", photos, logos, annuler);
                    alert.setTitle("Import par glisser-déposer");
                    alert.setHeaderText("Choisir le type d'import pour " + imageFiles.size() + " fichier(s)");
                    alert.setContentText("Souhaitez-vous importer ces fichiers comme photos ou logos ?");
                    
                    var result = alert.showAndWait().orElse(annuler);
                    if (result == photos) {
                        logMessage("Import par glisser-déposer: " + imageFiles.size() + " photo(s)");
                        importFilesAsync(imageFiles, MediaType.PHOTOS);
                        success = true;
                    } else if (result == logos) {
                        logMessage("Import par glisser-déposer: " + imageFiles.size() + " logo(s)");
                        importFilesAsync(imageFiles, MediaType.LOGOS);
                        success = true;
                    }
                }
            }
            
            // Supprimer le feedback visuel
            mainContainer.setStyle("");
            event.setDropCompleted(success);
            event.consume();
        });
        
        logMessage("✓ Support glisser-déposer activé");
    }
    
    /**
     * Vérifie si un fichier est une image basé sur son extension
     */
    private boolean isImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || 
               lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif") || 
               lowerName.endsWith(".bmp") || lowerName.endsWith(".svg");
    }
}