package com.magsav.service;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service de normalisation des images pour l'application MAGSAV
 * Garantit un affichage cohérent et optimisé des images dans toute l'application
 */
public class ImageNormalizationService {
    
    // Tailles standards pour l'affichage
    public static final int THUMBNAIL_SIZE = 150;      // Vignettes pour listes et mosaïques
    public static final int MEDIUM_SIZE = 400;         // Affichage dans les volets de détail
    public static final int LARGE_SIZE = 800;          // Affichage plein écran
    
    private final Path photosDir;
    private final Path thumbsDir;
    private final Path mediumDir;
    private final Path logosDir;
    
    public ImageNormalizationService() {
        Path baseDir = Paths.get(com.magsav.config.AppConfig.getMediaDirectory());
        this.photosDir = baseDir.resolve("photos");
        this.thumbsDir = baseDir.resolve("thumbs");
        this.mediumDir = baseDir.resolve("medium");
        this.logosDir = baseDir.resolve("logos");
        
        // Créer les dossiers s'ils n'existent pas
        try {
            Files.createDirectories(photosDir);
            Files.createDirectories(thumbsDir);
            Files.createDirectories(mediumDir);
            Files.createDirectories(logosDir);
        } catch (IOException e) {
            System.err.println("Erreur lors de la création des dossiers d'images: " + e.getMessage());
        }
    }
    
    /**
     * Normalise une image importée en créant toutes les tailles nécessaires
     * @param originalPath Chemin vers l'image originale
     * @param targetName Nom de fichier cible (sans extension)
     * @return Le nom du fichier normalisé
     */
    public String normalizeImage(Path originalPath, String targetName) throws IOException {
        if (!Files.exists(originalPath)) {
            throw new IOException("Fichier source introuvable: " + originalPath);
        }
        
        // Charger l'image originale
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("Impossible de lire l'image: " + originalPath);
        }
        
        // Noms de fichiers normalisés
        String baseFileName = cleanFileName(targetName);
        String thumbFileName = baseFileName + "_thumb.png";
        String mediumFileName = baseFileName + "_medium.png";
        String largeFileName = baseFileName + "_large.png";
        
        // Créer les différentes tailles
        createThumbnail(originalImage, thumbsDir.resolve(thumbFileName));
        createMediumImage(originalImage, mediumDir.resolve(mediumFileName));
        createLargeImage(originalImage, photosDir.resolve(largeFileName));
        
        System.out.println("Image normalisée: " + baseFileName);
        System.out.println("  - Vignette: " + thumbFileName);
        System.out.println("  - Moyenne: " + mediumFileName);
        System.out.println("  - Grande: " + largeFileName);
        
        return largeFileName; // Retourner le nom de l'image principale
    }
    
    /**
     * Crée une vignette (150x150 max)
     */
    private void createThumbnail(BufferedImage original, Path targetPath) throws IOException {
        BufferedImage thumbnail = resizeImage(original, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        saveAsPNG(thumbnail, targetPath);
    }
    
    /**
     * Crée une image moyenne (400px max sur le côté le plus long)
     */
    private void createMediumImage(BufferedImage original, Path targetPath) throws IOException {
        BufferedImage medium = resizeImageProportional(original, MEDIUM_SIZE);
        saveAsPNG(medium, targetPath);
    }
    
    /**
     * Crée une grande image (800px max sur le côté le plus long)
     */
    private void createLargeImage(BufferedImage original, Path targetPath) throws IOException {
        BufferedImage large = resizeImageProportional(original, LARGE_SIZE);
        saveAsPNG(large, targetPath);
    }
    
    /**
     * Redimensionne une image en conservant les proportions
     */
    private BufferedImage resizeImageProportional(BufferedImage original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Calculer les nouvelles dimensions en gardant les proportions
        double ratio = Math.min((double) maxSize / width, (double) maxSize / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);
        
        return resizeImage(original, newWidth, newHeight);
    }
    
    /**
     * Redimensionne une image aux dimensions exactes en préservant la transparence
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        // Détecter si l'image originale a de la transparence
        boolean hasAlpha = original.getColorModel().hasAlpha();
        int imageType = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2d = resized.createGraphics();
        
        // Configuration pour une qualité optimale
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Si l'image a de la transparence, configurer la composition
        if (hasAlpha) {
            g2d.setComposite(AlphaComposite.Src);
        }
        
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return resized;
    }
    
    /**
     * Sauvegarde une image au format PNG en préservant la transparence
     */
    private void saveAsPNG(BufferedImage image, Path targetPath) throws IOException {
        // Créer le dossier parent si nécessaire
        Files.createDirectories(targetPath.getParent());
        
        // Sauvegarder en PNG (supporte la transparence nativement)
        ImageIO.write(image, "png", targetPath.toFile());
    }
    
    /**
     * Nettoie un nom de fichier pour éviter les caractères problématiques
     */
    private String cleanFileName(String fileName) {
        return fileName.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
    
    /**
     * Retourne le chemin vers la vignette d'une image
     */
    public Path getThumbnailPath(String baseFileName) {
        String thumbName = generateThumbnailName(baseFileName);
        return thumbsDir.resolve(thumbName);
    }
    
    /**
     * Retourne le chemin vers l'image moyenne
     */
    public Path getMediumPath(String baseFileName) {
        String mediumName = generateMediumName(baseFileName);
        return mediumDir.resolve(mediumName);
    }
    
    /**
     * Retourne le chemin vers l'image grande
     */
    public Path getLargePath(String baseFileName) {
        String largeName = generateLargeName(baseFileName);
        return photosDir.resolve(largeName);
    }
    
    /**
     * Génère le nom de fichier pour la vignette
     */
    private String generateThumbnailName(String originalName) {
        if (originalName.endsWith("_thumb.png")) {
            return originalName;
        }
        String baseName = removeExtension(originalName);
        baseName = removeSizeSuffix(baseName);
        return baseName + "_thumb.png";
    }
    
    /**
     * Génère le nom de fichier pour l'image moyenne
     */
    private String generateMediumName(String originalName) {
        if (originalName.endsWith("_medium.png")) {
            return originalName;
        }
        String baseName = removeExtension(originalName);
        baseName = removeSizeSuffix(baseName);
        return baseName + "_medium.png";
    }
    
    /**
     * Génère le nom de fichier pour l'image grande
     */
    private String generateLargeName(String originalName) {
        if (originalName.endsWith("_large.png")) {
            return originalName;
        }
        String baseName = removeExtension(originalName);
        baseName = removeSizeSuffix(baseName);
        return baseName + "_large.png";
    }
    
    /**
     * Retire l'extension d'un nom de fichier
     */
    private String removeExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
    
    /**
     * Retire les suffixes de taille existants (_thumb, _medium, _large)
     */
    private String removeSizeSuffix(String baseName) {
        if (baseName.endsWith("_thumb") || baseName.endsWith("_medium") || baseName.endsWith("_large")) {
            int lastUnderscore = baseName.lastIndexOf('_');
            return baseName.substring(0, lastUnderscore);
        }
        return baseName;
    }
    
    /**
     * Vérifie si toutes les tailles d'une image existent
     */
    public boolean isImageNormalized(String baseFileName) {
        return Files.exists(getThumbnailPath(baseFileName)) &&
               Files.exists(getMediumPath(baseFileName)) &&
               Files.exists(getLargePath(baseFileName));
    }
    
    /**
     * Charge une image JavaFX optimisée pour l'affichage
     */
    public Image loadImageForDisplay(String baseFileName, ImageSize size) {
        try {
            Path imagePath;
            switch (size) {
                case THUMBNAIL -> imagePath = getThumbnailPath(baseFileName);
                case MEDIUM -> imagePath = getMediumPath(baseFileName);
                case LARGE -> imagePath = getLargePath(baseFileName);
                default -> imagePath = getMediumPath(baseFileName);
            }
            
            System.out.println("DEBUG: Recherche image " + size + " pour '" + baseFileName + "' dans: " + imagePath);
            
            if (Files.exists(imagePath)) {
                System.out.println("DEBUG: Image trouvée: " + imagePath);
                return new Image(imagePath.toUri().toString(), true);
            } else {
                System.out.println("DEBUG: Image non trouvée: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Normalise un logo en créant toutes les tailles nécessaires dans le dossier logos
     * @param originalPath Chemin vers le logo original
     * @param targetName Nom de fichier cible (sans extension)
     * @return Le nom du fichier logo normalisé
     */
    public String normalizeImageToLogos(Path originalPath, String targetName) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            throw new IOException("Impossible de lire l'image: " + originalPath);
        }
        
        String finalName = targetName.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        
        // Créer les 3 tailles dans le dossier logos
        BufferedImage thumbnail = resizeImage(originalImage, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        BufferedImage medium = resizeImage(originalImage, MEDIUM_SIZE, MEDIUM_SIZE);
        BufferedImage large = resizeImage(originalImage, LARGE_SIZE, LARGE_SIZE);
        
        // Sauvegarder les versions normalisées en JPEG
        ImageIO.write(thumbnail, "jpg", logosDir.resolve(finalName + "_thumb.jpg").toFile());
        ImageIO.write(medium, "jpg", logosDir.resolve(finalName + "_medium.jpg").toFile());
        ImageIO.write(large, "jpg", logosDir.resolve(finalName + "_large.jpg").toFile());
        
        System.out.println("Logo normalisé: " + finalName);
        System.out.println("  - Vignette: " + finalName + "_thumb.jpg");
        System.out.println("  - Moyenne: " + finalName + "_medium.jpg");
        System.out.println("  - Grande: " + finalName + "_large.jpg");
        
        return finalName + "_large.jpg";
    }
    
    /**
     * Vérifie si un logo est déjà normalisé
     * @param logoName Nom du logo (avec extension)
     * @return true si le logo est normalisé
     */
    public boolean isLogoNormalized(String logoName) {
        String baseName = logoName.replaceFirst("\\.[^.]+$", "");
        baseName = baseName.replaceFirst("_(thumb|medium|large)$", "");
        
        Path thumbPath = logosDir.resolve(baseName + "_thumb.png");
        Path mediumPath = logosDir.resolve(baseName + "_medium.png");
        Path largePath = logosDir.resolve(baseName + "_large.png");
        
        return Files.exists(thumbPath) && Files.exists(mediumPath) && Files.exists(largePath);
    }
    
    /**
     * Énumération des tailles d'images disponibles
     */
    public enum ImageSize {
        THUMBNAIL,  // 150px max
        MEDIUM,     // 400px max  
        LARGE       // 800px max
    }
}