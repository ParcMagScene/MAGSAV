package com.magsav.util;

import com.magsav.util.AppLogger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * Utilitaire pour la gestion des imports de médias (images, logos, documents)
 */
public class MediaImporter {
    
    private static final String MEDIA_DIR = "media";
    private static final String LOGOS_DIR = "media/logos";
    private static final String IMAGES_DIR = "media/images";
    private static final String DOCUMENTS_DIR = "media/documents";
    
    // Extensions de fichiers supportées
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff"};
    private static final String[] DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"};
    
    static {
        createDirectories();
    }
    
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(MEDIA_DIR));
            Files.createDirectories(Paths.get(LOGOS_DIR));
            Files.createDirectories(Paths.get(IMAGES_DIR));
            Files.createDirectories(Paths.get(DOCUMENTS_DIR));
        } catch (IOException e) {
            AppLogger.error("Erreur lors de la création des répertoires média: " + e.getMessage(), e);
        }
    }
    
    /**
     * Importe une liste de fichiers dans le répertoire approprié
     */
    public static void importFiles(List<File> files, MediaType mediaType) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        String targetDir = getTargetDirectory(mediaType);
        int successCount = 0;
        int errorCount = 0;
        
        for (File file : files) {
            try {
                if (isValidFileType(file, mediaType)) {
                    Path targetPath = Paths.get(targetDir, file.getName());
                    
                    // Vérifier si le fichier existe déjà
                    if (Files.exists(targetPath)) {
                        Optional<ButtonType> result = showOverwriteDialog(file.getName());
                        if (result.isEmpty() || result.get() != ButtonType.YES) {
                            continue; // Passer au fichier suivant
                        }
                    }
                    
                    Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    successCount++;
                    AppLogger.info("Fichier importé: " + file.getName() + " -> " + targetPath);
                    
                } else {
                    AppLogger.warn("Type de fichier non supporté: " + file.getName());
                    errorCount++;
                }
            } catch (IOException e) {
                AppLogger.error("Erreur lors de l'import de " + file.getName() + ": " + e.getMessage(), e);
                errorCount++;
            }
        }
        
        // Afficher un résumé
        showImportSummary(successCount, errorCount);
    }
    
    /**
     * Ouvre un sélecteur de fichiers pour importer des médias
     */
    public static void showFileChooser(Window parent, MediaType mediaType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner des " + mediaType.getDisplayName().toLowerCase());
        
        // Configurer les filtres selon le type de média
        switch (mediaType) {
            case IMAGE:
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
                );
                break;
            case LOGO:
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Logos", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.svg"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
                );
                break;
            case DOCUMENT:
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.xls", "*.xlsx", "*.txt"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
                );
                break;
        }
        
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(parent);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            importFiles(selectedFiles, mediaType);
        }
    }
    
    private static String getTargetDirectory(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> IMAGES_DIR;
            case LOGO -> LOGOS_DIR;
            case DOCUMENT -> DOCUMENTS_DIR;
        };
    }
    
    private static boolean isValidFileType(File file, MediaType mediaType) {
        String fileName = file.getName().toLowerCase();
        
        return switch (mediaType) {
            case IMAGE, LOGO -> {
                for (String ext : IMAGE_EXTENSIONS) {
                    if (fileName.endsWith(ext)) yield true;
                }
                yield false;
            }
            case DOCUMENT -> {
                for (String ext : DOCUMENT_EXTENSIONS) {
                    if (fileName.endsWith(ext)) yield true;
                }
                yield false;
            }
        };
    }
    
    private static Optional<ButtonType> showOverwriteDialog(String fileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fichier existant");
        alert.setHeaderText("Le fichier existe déjà");
        alert.setContentText("Le fichier '" + fileName + "' existe déjà.\nVoulez-vous le remplacer ?");
        
        return alert.showAndWait();
    }
    
    private static void showImportSummary(int successCount, int errorCount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Import terminé");
        alert.setHeaderText("Résumé de l'import");
        
        StringBuilder content = new StringBuilder();
        content.append(successCount).append(" fichier(s) importé(s) avec succès");
        if (errorCount > 0) {
            content.append("\n").append(errorCount).append(" erreur(s) rencontrée(s)");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * Types de médias supportés
     */
    public enum MediaType {
        IMAGE("Images"),
        LOGO("Logos"),
        DOCUMENT("Documents");
        
        private final String displayName;
        
        MediaType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}