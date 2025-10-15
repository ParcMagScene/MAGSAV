package com.magsav.service;

import com.magsav.util.AppLogger;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service pour gérer l'upload et la gestion des avatars personnalisés des utilisateurs
 */
public class UserAvatarUploadService {
    
    private static UserAvatarUploadService instance;
    private static final String AVATARS_DIRECTORY = "medias/avatars/users/";
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    
    private UserAvatarUploadService() {
        // Créer le dossier des avatars s'il n'existe pas
        try {
            Path avatarsPath = Paths.get(AVATARS_DIRECTORY);
            Files.createDirectories(avatarsPath);
        } catch (IOException e) {
            AppLogger.error("Erreur lors de la création du dossier avatars", e);
        }
    }
    
    public static synchronized UserAvatarUploadService getInstance() {
        if (instance == null) {
            instance = new UserAvatarUploadService();
        }
        return instance;
    }
    
    /**
     * Ouvre une boîte de dialogue pour sélectionner un fichier image pour l'avatar
     */
    public File selectAvatarFile(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un avatar");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        // Filtres pour les types de fichiers supportés
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Images (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
            "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(imageFilter);
        
        return fileChooser.showOpenDialog(ownerWindow);
    }
    
    /**
     * Sauvegarde un fichier avatar pour un utilisateur spécifique
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur (pour le nom de fichier)
     * @param sourceFile Fichier source à copier
     * @return Path vers le fichier sauvegardé, ou null si erreur
     */
    public Path saveUserAvatar(Long userId, String username, File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            AppLogger.warn("Fichier avatar source invalide");
            return null;
        }
        
        if (!isValidImageFile(sourceFile)) {
            AppLogger.warn("Type de fichier avatar non supporté: " + sourceFile.getName());
            return null;
        }
        
        try {
            // Créer le nom de fichier (userId_username.extension)
            String extension = getFileExtension(sourceFile.getName());
            String safeUsername = sanitizeFilename(username);
            String filename = userId + "_" + safeUsername + extension;
            
            Path targetPath = Paths.get(AVATARS_DIRECTORY, filename);
            
            // Copier le fichier
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            AppLogger.info("Avatar sauvegardé pour l'utilisateur " + username + ": " + targetPath);
            return targetPath;
            
        } catch (IOException e) {
            AppLogger.error("Erreur lors de la sauvegarde de l'avatar pour " + username, e);
            return null;
        }
    }
    
    /**
     * Récupère le chemin de l'avatar personnalisé d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur
     * @return Path vers l'avatar personnalisé, ou null si pas trouvé
     */
    public Path getUserAvatarPath(Long userId, String username) {
        if (userId == null || username == null) return null;
        
        String safeUsername = sanitizeFilename(username);
        
        // Essayer avec chaque extension supportée
        for (String ext : SUPPORTED_EXTENSIONS) {
            String filename = userId + "_" + safeUsername + ext;
            Path avatarPath = Paths.get(AVATARS_DIRECTORY, filename);
            
            if (Files.exists(avatarPath)) {
                return avatarPath;
            }
        }
        
        return null;
    }
    
    /**
     * Charge l'avatar d'un utilisateur (personnalisé ou par défaut)
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur
     * @return Image de l'avatar
     */
    public Image loadUserAvatar(Long userId, String username) {
        // Essayer de charger l'avatar personnalisé
        Path customAvatarPath = getUserAvatarPath(userId, username);
        if (customAvatarPath != null && Files.exists(customAvatarPath)) {
            try {
                return new Image(customAvatarPath.toUri().toString());
            } catch (Exception e) {
                AppLogger.warn("Erreur lors du chargement de l'avatar personnalisé pour " + username, e);
            }
        }
        
        // Utiliser l'avatar par défaut
        return AvatarService.getInstance().getAvatarForUsername(username);
    }
    
    /**
     * Supprime l'avatar personnalisé d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param username Nom d'utilisateur
     * @return true si supprimé avec succès
     */
    public boolean deleteUserAvatar(Long userId, String username) {
        Path avatarPath = getUserAvatarPath(userId, username);
        if (avatarPath != null && Files.exists(avatarPath)) {
            try {
                Files.delete(avatarPath);
                AppLogger.info("Avatar personnalisé supprimé pour " + username);
                return true;
            } catch (IOException e) {
                AppLogger.error("Erreur lors de la suppression de l'avatar pour " + username, e);
                return false;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un fichier est une image valide
     */
    private boolean isValidImageFile(File file) {
        String filename = file.getName().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtient l'extension d'un fichier
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot).toLowerCase();
        }
        return ".jpg"; // Extension par défaut
    }
    
    /**
     * Nettoie un nom de fichier pour éviter les caractères problématiques
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Valide les dimensions et la taille d'une image
     * @param file Fichier image à valider
     * @return true si l'image est valide
     */
    public boolean validateImageFile(File file) {
        if (!isValidImageFile(file)) {
            return false;
        }
        
        // Vérifier la taille du fichier (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.length() > maxSize) {
            AppLogger.warn("Fichier avatar trop volumineux: " + file.length() + " bytes");
            return false;
        }
        
        try {
            // Vérifier que l'image peut être chargée
            Image testImage = new Image(file.toURI().toString());
            if (testImage.isError()) {
                AppLogger.warn("Image avatar corrompue: " + file.getName());
                return false;
            }
            
            // Vérifier les dimensions raisonnables
            double width = testImage.getWidth();
            double height = testImage.getHeight();
            
            if (width < 32 || height < 32) {
                AppLogger.warn("Image avatar trop petite: " + width + "x" + height);
                return false;
            }
            
            if (width > 2048 || height > 2048) {
                AppLogger.warn("Image avatar trop grande: " + width + "x" + height);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la validation de l'image avatar", e);
            return false;
        }
    }
}