package com.magsav.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.magsav.util.AppLogger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service pour gérer les avatars par défaut des utilisateurs et les images de remplacement
 */
public class AvatarService {
    
    private static AvatarService instance;
    private final Map<String, Image> avatarCache = new HashMap<>();
    private final Map<String, Image> defaultImageCache = new HashMap<>();
    private final Random random = new Random();
    
    // Constantes pour les chemins des images
    private static final String AVATAR_BASE_PATH = "/images/avatars/";
    private static final String DEFAULT_BASE_PATH = "/images/defaults/";
    
    // Avatars disponibles
    private static final String[] AVAILABLE_AVATARS = {
        "avatar_default_1.svg",
        "avatar_default_2.svg", 
        "avatar_default_3.svg",
        "avatar_default_4.svg",
        "avatar_default_5.svg",
        "avatar_default_6.svg"
    };
    
    // Images par défaut
    private static final String COMPANY_LOGO_DEFAULT = "company_logo_default.svg";
    private static final String PRODUCT_IMAGE_DEFAULT = "product_image_default.svg";
    private static final String IMAGE_PLACEHOLDER = "image_placeholder.svg";
    
    private AvatarService() {
        preloadDefaultImages();
    }
    
    public static synchronized AvatarService getInstance() {
        if (instance == null) {
            instance = new AvatarService();
        }
        return instance;
    }
    
    /**
     * Précharge les images par défaut en mémoire pour de meilleures performances
     */
    private void preloadDefaultImages() {
        try {
            // Précharger les avatars
            for (String avatar : AVAILABLE_AVATARS) {
                loadImageToCache(AVATAR_BASE_PATH + avatar, avatarCache);
            }
            
            // Précharger les images par défaut
            loadImageToCache(DEFAULT_BASE_PATH + COMPANY_LOGO_DEFAULT, defaultImageCache);
            loadImageToCache(DEFAULT_BASE_PATH + PRODUCT_IMAGE_DEFAULT, defaultImageCache);
            loadImageToCache(DEFAULT_BASE_PATH + IMAGE_PLACEHOLDER, defaultImageCache);
            
            AppLogger.info("AvatarService initialisé avec succès - " + 
                         avatarCache.size() + " avatars et " + 
                         defaultImageCache.size() + " images par défaut chargées");
                         
        } catch (Exception e) {
            AppLogger.error("Erreur lors du préchargement des images par défaut", e);
        }
    }
    
    /**
     * Charge une image dans le cache spécifié
     */
    private void loadImageToCache(String resourcePath, Map<String, Image> cache) {
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                Image image = new Image(stream);
                cache.put(resourcePath, image);
                stream.close();
            } else {
                AppLogger.warn("Image non trouvée: " + resourcePath);
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement de l'image: " + resourcePath, e);
        }
    }
    
    /**
     * Obtient un avatar aléatoire pour un nouvel utilisateur
     */
    public Image getRandomAvatar() {
        String avatarPath = AVATAR_BASE_PATH + AVAILABLE_AVATARS[random.nextInt(AVAILABLE_AVATARS.length)];
        return avatarCache.get(avatarPath);
    }
    
    /**
     * Obtient un avatar spécifique par son index (0 à 5)
     */
    public Image getAvatarByIndex(int index) {
        if (index < 0 || index >= AVAILABLE_AVATARS.length) {
            return getRandomAvatar();
        }
        String avatarPath = AVATAR_BASE_PATH + AVAILABLE_AVATARS[index];
        return avatarCache.get(avatarPath);
    }
    
    /**
     * Obtient un avatar basé sur l'ID de l'utilisateur pour une cohérence
     */
    public Image getAvatarForUserId(Long userId) {
        if (userId == null) {
            return getRandomAvatar();
        }
        int index = (int) (userId % AVAILABLE_AVATARS.length);
        return getAvatarByIndex(index);
    }
    
    /**
     * Obtient un avatar basé sur le nom d'utilisateur pour une cohérence
     */
    public Image getAvatarForUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return getRandomAvatar();
        }
        int index = Math.abs(username.hashCode()) % AVAILABLE_AVATARS.length;
        return getAvatarByIndex(index);
    }
    
    /**
     * Crée un ImageView avec un avatar pour un utilisateur
     */
    public ImageView createAvatarImageView(String username, double size) {
        Image avatar = getAvatarForUsername(username);
        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    
    /**
     * Crée un ImageView avec un avatar pour un ID utilisateur
     */
    public ImageView createAvatarImageView(Long userId, double size) {
        Image avatar = getAvatarForUserId(userId);
        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    
    /**
     * Obtient l'image par défaut pour les logos d'entreprise
     */
    public Image getDefaultCompanyLogo() {
        return defaultImageCache.get(DEFAULT_BASE_PATH + COMPANY_LOGO_DEFAULT);
    }
    
    /**
     * Obtient l'image par défaut pour les produits
     */
    public Image getDefaultProductImage() {
        return defaultImageCache.get(DEFAULT_BASE_PATH + PRODUCT_IMAGE_DEFAULT);
    }
    
    /**
     * Obtient l'image de placeholder générique
     */
    public Image getImagePlaceholder() {
        return defaultImageCache.get(DEFAULT_BASE_PATH + IMAGE_PLACEHOLDER);
    }
    
    /**
     * Crée une grande icône par défaut avec du texte pour un produit sans photo
     * @param productName Nom du produit
     * @return ImageView avec une icône personnalisée
     */
    public ImageView createProductPlaceholderIcon(String productName, double size) {
            // Utiliser l'image par défaut du produit
        ImageView imageView = new ImageView(getDefaultProductImage());
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Ajouter style pour une grande icône
        imageView.getStyleClass().add("large-default-icon");
        
        // Ajouter une tooltip avec le nom du produit
        if (productName != null && !productName.trim().isEmpty()) {
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Aucune photo pour : " + productName);
            javafx.scene.control.Tooltip.install(imageView, tooltip);
        }
        
        return imageView;
    }
    
    /**
     * Crée une grande icône par défaut avec du texte pour un fabricant sans logo
     * @param manufacturerName Nom du fabricant
     * @return ImageView avec une icône personnalisée
     */
    public ImageView createManufacturerPlaceholderIcon(String manufacturerName, double size) {
        // Utiliser l'image par défaut du fabricant
        ImageView imageView = new ImageView(getDefaultCompanyLogo());
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Ajouter style pour une grande icône
        imageView.getStyleClass().add("large-default-icon");
        
        // Ajouter une tooltip avec le nom du fabricant
        if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Aucun logo pour : " + manufacturerName);
            javafx.scene.control.Tooltip.install(imageView, tooltip);
        }
        
        return imageView;
    }
    
    /**
     * Crée un ImageView avec une image par défaut
     */
    public ImageView createDefaultImageView(DefaultImageType type, double width, double height) {
        Image defaultImage;
        switch (type) {
            case COMPANY_LOGO:
                defaultImage = getDefaultCompanyLogo();
                break;
            case PRODUCT_IMAGE:
                defaultImage = getDefaultProductImage();
                break;
            case PLACEHOLDER:
            default:
                defaultImage = getImagePlaceholder();
                break;
        }
        
        ImageView imageView = new ImageView(defaultImage);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    
    /**
     * Charge une image personnalisée ou retourne l'image par défaut si échec
     */
    public Image loadImageOrDefault(String imagePath, DefaultImageType defaultType) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // Essayer de charger l'image personnalisée
                Image customImage = new Image(imagePath);
                if (!customImage.isError()) {
                    return customImage;
                }
            }
        } catch (Exception e) {
            AppLogger.warn("Impossible de charger l'image personnalisée: " + imagePath, e);
        }
        
        // Retourner l'image par défaut
        switch (defaultType) {
            case COMPANY_LOGO:
                return getDefaultCompanyLogo();
            case PRODUCT_IMAGE:
                return getDefaultProductImage();
            case PLACEHOLDER:
            default:
                return getImagePlaceholder();
        }
    }
    
    /**
     * Crée un ImageView qui charge une image personnalisée ou utilise l'image par défaut
     */
    public ImageView createImageViewOrDefault(String imagePath, DefaultImageType defaultType, 
                                            double width, double height) {
        Image image = loadImageOrDefault(imagePath, defaultType);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    
    /**
     * Types d'images par défaut disponibles
     */
    public enum DefaultImageType {
        COMPANY_LOGO,
        PRODUCT_IMAGE,
        PLACEHOLDER
    }
    
    /**
     * Obtient la liste des avatars disponibles
     */
    public String[] getAvailableAvatars() {
        return AVAILABLE_AVATARS.clone();
    }
    
    /**
     * Obtient le nombre d'avatars disponibles
     */
    public int getAvatarCount() {
        return AVAILABLE_AVATARS.length;
    }
}