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
            
            // Vérifier si le fichier existe et n'est pas vide
            if (!logoFile.exists() || logoFile.length() < 100) {
                AppLogger.warn("Logo GIF de Mag Scène non trouvé ou trop petit: " + MAG_SCENE_LOGO_PATH);
                // Essayer de charger le logo SVG de fallback
                return loadFallbackLogo(imageView);
            }
            
            // Charger le GIF animé
            Image gifImage = new Image(logoFile.toURI().toString());
            if (gifImage.isError()) {
                AppLogger.warn("Erreur lors du chargement du GIF, utilisation du fallback");
                return loadFallbackLogo(imageView);
            }
            
            imageView.setImage(gifImage);
            imageView.setPreserveRatio(true);
            
            AppLogger.info("Logo GIF animé de Mag Scène chargé avec succès");
            return true;
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement du logo GIF animé de Mag Scène", e);
            return loadFallbackLogo(imageView);
        }
    }
    
    /**
     * Charge un logo de fallback (SVG créé dynamiquement)
     */
    private static boolean loadFallbackLogo(ImageView imageView) {
        try {
            // Utiliser le logo SVG créé précédemment
            String logoSvgPath = "src/main/resources/images/logo-mag-scene.svg";
            File svgFile = new File(logoSvgPath);
            
            if (svgFile.exists()) {
                Image svgImage = new Image(svgFile.toURI().toString());
                if (!svgImage.isError()) {
                    imageView.setImage(svgImage);
                    imageView.setPreserveRatio(true);
                    AppLogger.info("Logo SVG de fallback chargé avec succès");
                    return true;
                }
            }
            
            // Si le SVG n'est pas disponible, créer un logo dynamique
            Image dynamicLogo = createDynamicLogo();
            if (dynamicLogo != null) {
                imageView.setImage(dynamicLogo);
                imageView.setPreserveRatio(true);
                AppLogger.info("Logo dynamique de fallback créé et chargé");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement du logo de fallback", e);
            return false;
        }
    }
    
    /**
     * Crée un logo dynamique simple
     */
    private static Image createDynamicLogo() {
        try {
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(120, 40);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Gradient de fond
            javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0.0, javafx.scene.paint.Color.web("#2E86AB")),
                new javafx.scene.paint.Stop(1.0, javafx.scene.paint.Color.web("#A23B72"))
            );
            
            // Fond arrondi
            gc.setFill(gradient);
            gc.fillRoundRect(0, 0, 120, 40, 8, 8);
            
            // Texte "Mag Scène"
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
            gc.fillText("Mag Scène", 15, 25);
            
            // Texte "SAV"
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
            gc.fillText("SAV", 85, 28);
            
            return canvas.snapshot(null, null);
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la création du logo dynamique", e);
            return null;
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