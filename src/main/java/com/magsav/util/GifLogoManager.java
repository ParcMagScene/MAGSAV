package com.magsav.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.FileInputStream;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire pour les logos GIF animés de Mag Scène
 * Permet de charger des GIFs animés et de créer des icônes statiques pour les listes
 */
public class GifLogoManager {
    
    private static final String MAG_SCENE_LOGO_PATH = "data/logos/mag_scene_logo.gif";
    private static final ConcurrentHashMap<String, Image> iconCache = new ConcurrentHashMap<>();
    
    /**
     * Charge le logo GIF animé de Mag Scène
     * @param imageView L'ImageView où afficher le logo
     * @return true si le logo a été chargé avec succès
     */
    public static boolean loadMagSceneAnimatedLogo(ImageView imageView) {
        try {
            File logoFile = new File(MAG_SCENE_LOGO_PATH);
            if (!logoFile.exists()) {
                AppLogger.warn("Logo GIF de Mag Scène non trouvé: " + MAG_SCENE_LOGO_PATH);
                return false;
            }
            
            // Charger le GIF animé
            Image gifImage = new Image(logoFile.toURI().toString());
            imageView.setImage(gifImage);
            imageView.setPreserveRatio(true);
            
            AppLogger.info("Logo GIF animé de Mag Scène chargé avec succès");
            return true;
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement du logo GIF animé de Mag Scène", e);
            return false;
        }
    }
    
    /**
     * Crée une icône statique à partir du GIF animé pour utilisation dans les listes
     * Version simplifiée utilisant seulement JavaFX
     * @param size Taille de l'icône (largeur et hauteur en pixels)
     * @return Image redimensionnée ou null si erreur
     */
    public static Image createMagSceneListIcon(int size) {
        String cacheKey = "mag_scene_icon_" + size;
        
        // Vérifier le cache
        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }
        
        try {
            File logoFile = new File(MAG_SCENE_LOGO_PATH);
            if (!logoFile.exists()) {
                AppLogger.warn("Logo GIF de Mag Scène non trouvé pour création d'icône: " + MAG_SCENE_LOGO_PATH);
                return null;
            }
            
            // Charger l'image avec JavaFX (supportera le GIF mais prendra la première frame pour les icônes)
            try (FileInputStream fis = new FileInputStream(logoFile)) {
                Image originalImage = new Image(fis, size, size, true, true);
                
                // Mettre en cache
                iconCache.put(cacheKey, originalImage);
                
                AppLogger.info("Icône statique de Mag Scène créée (taille: " + size + "px)");
                return originalImage;
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la création de l'icône statique de Mag Scène", e);
            return null;
        }
    }
    
    /**
     * Crée le répertoire des logos s'il n'existe pas
     */
    public static void ensureLogoDirectoryExists() {
        File logoDir = new File("data/logos");
        if (!logoDir.exists()) {
            logoDir.mkdirs();
            AppLogger.info("Répertoire des logos créé: " + logoDir.getAbsolutePath());
        }
    }
    
    /**
     * Vérifie si le logo GIF de Mag Scène existe
     */
    public static boolean isMagSceneLogoAvailable() {
        return new File(MAG_SCENE_LOGO_PATH).exists();
    }
    
    /**
     * Vide le cache des icônes
     */ 
    public static void clearIconCache() {
        iconCache.clear();
        AppLogger.info("Cache des icônes vidé");
    }
}