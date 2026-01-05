package com.magscene.magsav.desktop.service;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service de gestion des médias (photos d'équipements et logos)
 * Gère les dossiers Medias MAGSAV/Photos et Medias MAGSAV/Logos
 */
public class MediaService {
    
    private static final String MEDIA_BASE_PATH = "Medias MAGSAV";
    private static final String PHOTOS_FOLDER = "Photos";
    private static final String LOGOS_FOLDER = "Logos";
    private static final String AVATARS_FOLDER = "Avatars";
    
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".avif"
    );
    
    private final Path basePath;
    private final Path photosPath;
    private final Path logosPath;
    private final Path avatarsPath;
    
    // Cache pour les images chargées
    private final Map<String, Image> imageCache = new HashMap<>();
    
    // Mapping nom de marque -> fichier logo
    private final Map<String, String> brandLogoMapping = new HashMap<>();
    
    // Instance singleton pour partager le cache entre toutes les instances
    private static MediaService instance;
    
    /**
     * Obtient l'instance singleton du MediaService
     */
    public static synchronized MediaService getInstance() {
        if (instance == null) {
            instance = new MediaService();
        }
        return instance;
    }
    
    public MediaService() {
        // Déterminer le chemin de base
        this.basePath = findMediaBasePath();
        this.photosPath = basePath.resolve(PHOTOS_FOLDER);
        this.logosPath = basePath.resolve(LOGOS_FOLDER);
        this.avatarsPath = basePath.resolve(AVATARS_FOLDER);
        
        // Initialiser le mapping des logos de marques
        initializeBrandLogoMapping();
        
        // Charger le mapping LOCMAT-photos
        loadLocmatPhotoMapping();
    }
    
    /**
     * Trouve le chemin de base des médias
     */
    private Path findMediaBasePath() {
        // Chercher dans le répertoire de travail courant
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path mediaPath = currentDir.resolve(MEDIA_BASE_PATH);
        
        if (Files.exists(mediaPath)) {
            return mediaPath;
        }
        
        // Chercher dans le répertoire parent (si lancé depuis un sous-dossier)
        Path parentMediaPath = currentDir.getParent().resolve(MEDIA_BASE_PATH);
        if (Files.exists(parentMediaPath)) {
            return parentMediaPath;
        }
        
        // Chercher dans le home directory de l'utilisateur
        Path homeMediaPath = Paths.get(System.getProperty("user.home"), "MAGSAV-3.0", MEDIA_BASE_PATH);
        if (Files.exists(homeMediaPath)) {
            return homeMediaPath;
        }
        
        // Fallback: créer le dossier dans le répertoire courant
        try {
            Files.createDirectories(mediaPath.resolve(PHOTOS_FOLDER));
            Files.createDirectories(mediaPath.resolve(LOGOS_FOLDER));
            Files.createDirectories(mediaPath.resolve(AVATARS_FOLDER));
        } catch (IOException e) {
            System.err.println("Impossible de créer les dossiers médias: " + e.getMessage());
        }
        
        return mediaPath;
    }
    
    /**
     * Initialise le mapping entre noms de marques et fichiers logos
     */
    private void initializeBrandLogoMapping() {
        // Mapping manuel des noms de marques vers leurs logos (noms de fichiers réels)
        brandLogoMapping.put("L-ACOUSTICS", "L-Acoustics.png");
        brandLogoMapping.put("L ACOUSTICS", "L-Acoustics.png");
        brandLogoMapping.put("L'ACOUSTICS", "L-Acoustics.png");
        brandLogoMapping.put("YAMAHA", "Yamaha.jpeg");
        brandLogoMapping.put("SHURE", "SHURE.png");
        brandLogoMapping.put("SENNHEISER", "Sennheiser.png");
        brandLogoMapping.put("CHAUVET", "Chauvet.png");
        brandLogoMapping.put("ROBE", "ROBE.png");
        brandLogoMapping.put("CLAY PAKY", "ClayPaky.jpg");
        brandLogoMapping.put("CLAYPAKY", "ClayPaky.jpg");
        brandLogoMapping.put("MARTIN", "Martin.svg");
        brandLogoMapping.put("ETC", "ETC_4c.jpg");
        brandLogoMapping.put("ADB", "ADB.jpeg");
        brandLogoMapping.put("STARWAY", "STARWAY.jpg");
        brandLogoMapping.put("DIMATEC", "DIMATEC-LOGO-250px-GIF-1.gif");
        brandLogoMapping.put("ALGAM", "Algam.jpg");
        brandLogoMapping.put("AXENTE", "Axente.png");
        brandLogoMapping.put("ESL", "ESL.png");
        brandLogoMapping.put("CSI", "CSI.jpeg");
        brandLogoMapping.put("DV2", "DV2.jpeg");
        brandLogoMapping.put("LAGOONA", "Lagoona.jpeg");
        brandLogoMapping.put("RJ", "RJ.png");
        brandLogoMapping.put("ROBERT JULIAT", "ROBERT JULIAT.png");
        brandLogoMapping.put("MAG SCENE", "MagSceneBLACK.gif");
        brandLogoMapping.put("MAGSCENE", "MagSceneBLACK.gif");
        brandLogoMapping.put("MAG SCÈNE", "MagSceneBLACK.gif");
        brandLogoMapping.put("MERCEDES", "MERCEDES.jpg");
        brandLogoMapping.put("MERCEDES-BENZ", "MERCEDES.jpg");
        brandLogoMapping.put("PEUGEOT", "Peugeot_2021_Logo.svg.png");
        brandLogoMapping.put("VOLVO", "VOLVO.jpg");
        brandLogoMapping.put("SCHMITZ", "SCHMITZ.png");
        brandLogoMapping.put("MOVING STAGE", "MOVING STAGE.jpg");
    }
    
    /**
     * Liste toutes les photos disponibles
     */
    public List<File> listPhotos() {
        return listMediaFiles(photosPath);
    }
    
    /**
     * Liste tous les logos disponibles
     */
    public List<File> listLogos() {
        return listMediaFiles(logosPath);
    }
    
    /**
     * Liste les fichiers médias d'un dossier
     */
    private List<File> listMediaFiles(Path folder) {
        if (!Files.exists(folder)) {
            return Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.list(folder)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    // Exclure les fichiers macOS ._ 
                    if (name.startsWith("._") || name.startsWith(".ds_store")) {
                        return false;
                    }
                    return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
                })
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Erreur lecture dossier " + folder + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Charge une image depuis le cache ou le système de fichiers
     */
    public Image loadImage(String path, double width, double height) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String cacheKey = path + "_" + (int)width + "x" + (int)height;
        
        return imageCache.computeIfAbsent(cacheKey, key -> {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    // Essayer dans le dossier Photos
                    file = photosPath.resolve(path).toFile();
                }
                if (!file.exists()) {
                    // Essayer dans le dossier Logos
                    file = logosPath.resolve(path).toFile();
                }
                if (file.exists()) {
                    return new Image(file.toURI().toString(), width, height, true, true, true);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image " + path + ": " + e.getMessage());
            }
            return null;
        });
    }
    
    /**
     * Charge une photo d'équipement
     * Cherche d'abord avec le nom exact, puis avec une recherche par préfixe
     */
    public Image loadEquipmentPhoto(String photoPath, double width, double height) {
        System.out.println("📷 loadEquipmentPhoto appelé avec: " + photoPath);
        if (photoPath == null || photoPath.isEmpty()) {
            System.out.println("📷 photoPath est null ou vide");
            return null;
        }
        
        // Si c'est juste un nom de fichier, chercher dans Photos
        File file = new File(photoPath);
        if (!file.isAbsolute()) {
            file = photosPath.resolve(photoPath).toFile();
            System.out.println("📷 Chemin résolu: " + file.getAbsolutePath());
        }
        
        if (file.exists()) {
            System.out.println("📷 Fichier existe: " + file.getAbsolutePath());
            final File finalFile = file;
            String cacheKey = "photo_" + file.getAbsolutePath() + "_" + (int)width + "x" + (int)height;
            return imageCache.computeIfAbsent(cacheKey, k -> {
                System.out.println("📷 Chargement nouvelle image: " + finalFile.toURI().toString());
                return new Image(finalFile.toURI().toString(), width, height, true, true, true);
            });
        } else {
            System.out.println("📷 Fichier N'EXISTE PAS: " + file.getAbsolutePath());
            
            // Recherche alternative: chercher un fichier commençant par le nom (sans extension)
            String baseName = photoPath;
            // Retirer l'extension si présente
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }
            final String searchPattern = baseName.toUpperCase();
            System.out.println("📷 Recherche alternative avec pattern: " + searchPattern);
            
            File photosDir = photosPath.toFile();
            if (photosDir.exists() && photosDir.isDirectory()) {
                File[] matches = photosDir.listFiles((dir, name) -> {
                    String upperName = name.toUpperCase();
                    // Ignorer les fichiers MacOS metadata
                    if (name.startsWith("._")) return false;
                    // Chercher les fichiers commençant par le pattern ou contenant le pattern
                    return upperName.startsWith(searchPattern) || 
                           (searchPattern.length() > 2 && upperName.contains(searchPattern));
                });
                
                if (matches != null && matches.length > 0) {
                    File foundFile = matches[0]; // Prendre le premier match
                    System.out.println("📷 Fichier trouvé par recherche: " + foundFile.getName());
                    String cacheKey = "photo_" + foundFile.getAbsolutePath() + "_" + (int)width + "x" + (int)height;
                    return imageCache.computeIfAbsent(cacheKey, k -> {
                        System.out.println("📷 Chargement image trouvée: " + foundFile.toURI().toString());
                        return new Image(foundFile.toURI().toString(), width, height, true, true, true);
                    });
                }
            }
        }
        
        return null;
    }
    
    /**
     * Invalide le cache d'une photo spécifique
     * @param photoPath le chemin de la photo à invalider
     */
    public void invalidatePhotoCache(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return;
        }
        
        // Construire le chemin complet si nécessaire
        File file = new File(photoPath);
        if (!file.isAbsolute()) {
            file = photosPath.resolve(photoPath).toFile();
        }
        
        // Supprimer toutes les entrées de cache pour ce fichier (toutes tailles)
        String absolutePath = file.getAbsolutePath();
        List<String> keysToRemove = imageCache.keySet().stream()
            .filter(key -> key.startsWith("photo_" + absolutePath))
            .toList();
        
        for (String key : keysToRemove) {
            imageCache.remove(key);
            System.out.println("📷 Cache invalidé pour: " + key);
        }
    }
    
    /**
     * Charge une photo de véhicule
     * Les photos de véhicules sont stockées dans le même dossier Photos
     */
    public Image loadVehiclePhoto(String photoPath, double width, double height) {
        System.out.println("🚗 loadVehiclePhoto appelé avec: " + photoPath);
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }
        
        // Si c'est juste un nom de fichier, chercher dans Photos
        File file = new File(photoPath);
        if (!file.isAbsolute()) {
            file = photosPath.resolve(photoPath).toFile();
            System.out.println("🚗 Chemin résolu: " + file.getAbsolutePath());
        }
        
        if (file.exists()) {
            final File finalFile = file;
            String cacheKey = "vehicle_photo_" + file.getAbsolutePath() + "_" + (int)width + "x" + (int)height;
            return imageCache.computeIfAbsent(cacheKey, k -> {
                System.out.println("🚗 Chargement photo véhicule: " + finalFile.toURI().toString());
                return new Image(finalFile.toURI().toString(), width, height, true, true, true);
            });
        } else {
            System.out.println("🚗 Photo véhicule non trouvée: " + file.getAbsolutePath());
        }
        
        return null;
    }
    
    /**
     * Vide complètement le cache des photos
     */
    public void clearPhotoCache() {
        List<String> keysToRemove = imageCache.keySet().stream()
            .filter(key -> key.startsWith("photo_"))
            .toList();
        
        for (String key : keysToRemove) {
            imageCache.remove(key);
        }
        System.out.println("📷 Cache des photos vidé (" + keysToRemove.size() + " entrées supprimées)");
    }

    /**
     * Charge le logo d'un client
     * Les logos de clients sont stockés dans le dossier Logos
     */
    public Image loadClientLogo(String logoPath, double width, double height) {
        System.out.println("🏢 loadClientLogo appelé avec: " + logoPath);
        if (logoPath == null || logoPath.isEmpty()) {
            return null;
        }
        
        // Si c'est juste un nom de fichier, chercher dans Logos
        File file = new File(logoPath);
        if (!file.isAbsolute()) {
            file = logosPath.resolve(logoPath).toFile();
            System.out.println("🏢 Chemin résolu: " + file.getAbsolutePath());
        }
        
        if (file.exists()) {
            final File finalFile = file;
            String cacheKey = "client_logo_" + file.getAbsolutePath() + "_" + (int)width + "x" + (int)height;
            return imageCache.computeIfAbsent(cacheKey, k -> {
                System.out.println("🏢 Chargement logo client: " + finalFile.toURI().toString());
                return new Image(finalFile.toURI().toString(), width, height, true, true, true);
            });
        } else {
            System.out.println("🏢 Logo client non trouvé: " + file.getAbsolutePath());
        }
        
        return null;
    }

    /**
     * Charge l'avatar d'une personne
     * Les avatars sont stockés dans le dossier Avatars
     */
    public Image loadAvatar(String avatarPath, double width, double height) {
        System.out.println("👤 loadAvatar appelé avec: " + avatarPath);
        if (avatarPath == null || avatarPath.isEmpty()) {
            return null;
        }
        
        // Si c'est juste un nom de fichier, chercher dans Avatars
        File file = new File(avatarPath);
        if (!file.isAbsolute()) {
            file = avatarsPath.resolve(avatarPath).toFile();
            System.out.println("👤 Chemin résolu: " + file.getAbsolutePath());
        }
        
        if (file.exists()) {
            final File finalFile = file;
            String cacheKey = "avatar_" + file.getAbsolutePath() + "_" + (int)width + "x" + (int)height;
            return imageCache.computeIfAbsent(cacheKey, k -> {
                System.out.println("👤 Chargement avatar: " + finalFile.toURI().toString());
                return new Image(finalFile.toURI().toString(), width, height, true, true, true);
            });
        } else {
            System.out.println("👤 Avatar non trouvé: " + file.getAbsolutePath());
        }
        
        return null;
    }
    
    /**
     * Récupère le logo d'une marque
     */
    public Image getBrandLogo(String brandName, double width, double height) {
        if (brandName == null || brandName.isEmpty()) {
            return null;
        }
        
        // Chercher dans le mapping
        String logoFile = brandLogoMapping.get(brandName.toUpperCase().trim());
        
        if (logoFile == null) {
            // Essayer de trouver un fichier correspondant
            logoFile = findLogoByBrandName(brandName);
        }
        
        if (logoFile != null) {
            Path logoPath = logosPath.resolve(logoFile);
            if (Files.exists(logoPath)) {
                String cacheKey = "logo_" + logoFile + "_" + (int)width + "x" + (int)height;
                return imageCache.computeIfAbsent(cacheKey, k ->
                    new Image(logoPath.toUri().toString(), width, height, true, true, true)
                );
            }
        }
        
        return null;
    }
    
    /**
     * Cherche un fichier logo par nom de marque (recherche approximative)
     */
    private String findLogoByBrandName(String brandName) {
        String searchName = brandName.toLowerCase().replaceAll("[^a-z0-9]", "");
        
        try (Stream<Path> paths = Files.list(logosPath)) {
            return paths
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(name -> {
                    String cleanName = name.toLowerCase().replaceAll("[^a-z0-9]", "");
                    return cleanName.contains(searchName) || searchName.contains(cleanName.split("\\.")[0]);
                })
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Copie une photo vers le dossier Photos avec un nouveau nom
     */
    public String copyPhotoToMedia(File sourceFile, String newName) throws IOException {
        String extension = getFileExtension(sourceFile.getName());
        String targetName = newName + extension;
        Path targetPath = photosPath.resolve(targetName);
        
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return targetName;
    }
    
    /**
     * Associe une photo existante à un nom d'équipement
     * Retourne le chemin relatif de la photo
     */
    public String getPhotoRelativePath(File photoFile) {
        if (photoFile == null) {
            return null;
        }
        
        // Si le fichier est dans le dossier Photos, retourner juste le nom
        if (photoFile.getParentFile().equals(photosPath.toFile())) {
            return photoFile.getName();
        }
        
        // Sinon retourner le chemin absolu
        return photoFile.getAbsolutePath();
    }
    
    /**
     * Recherche des photos correspondant à un nom d'équipement ou code LocMat
     */
    public List<File> findMatchingPhotos(String equipmentName, String locmatCode) {
        List<File> allPhotos = listPhotos();
        List<File> matches = new ArrayList<>();
        
        String searchName = normalizeForSearch(equipmentName);
        String searchCode = locmatCode != null ? normalizeForSearch(locmatCode) : "";
        
        for (File photo : allPhotos) {
            String photoName = normalizeForSearch(photo.getName());
            
            // Vérifier si le nom de la photo contient le nom de l'équipement ou le code
            if (!searchName.isEmpty() && photoName.contains(searchName)) {
                matches.add(photo);
            } else if (!searchCode.isEmpty() && photoName.contains(searchCode)) {
                matches.add(photo);
            }
        }
        
        return matches;
    }
    
    /**
     * Normalise une chaîne pour la recherche
     */
    private String normalizeForSearch(String input) {
        if (input == null) return "";
        return input.toLowerCase()
            .replaceAll("[^a-z0-9]", "")
            .trim();
    }
    
    /**
     * Récupère l'extension d'un fichier
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return "";
    }
    
    /**
     * Retourne le chemin du dossier Photos
     */
    public Path getPhotosPath() {
        return photosPath;
    }
    
    /**
     * Retourne le dossier Photos en tant que File
     */
    public java.io.File getPhotosDirectory() {
        return photosPath.toFile();
    }
    
    /**
     * Retourne le chemin du dossier Logos
     */
    public Path getLogosPath() {
        return logosPath;
    }
    
    /**
     * Vérifie si les dossiers médias existent
     */
    public boolean isMediaFolderAvailable() {
        return Files.exists(photosPath) && Files.exists(logosPath);
    }
    
    /**
     * Vide le cache d'images
     */
    public void clearCache() {
        imageCache.clear();
    }
    
    // ========================================
    // 👤 GESTION DES AVATARS
    // ========================================
    
    // Avatars par défaut disponibles
    private static final String[] DEFAULT_AVATARS = {
        "default-avatar-blue.png",
        "default-avatar-purple.png", 
        "default-avatar-green.png",
        "default-avatar-orange.png",
        "default-avatar-pink.png",
        "default-avatar-cyan.png",
        "default-avatar-yellow.png",
        "default-avatar-red.png",
        "default-avatar-teal.png",
        "default-avatar-indigo.png"
    };
    
    // Couleurs correspondant aux avatars par défaut
    private static final String[] DEFAULT_AVATAR_COLORS = {
        "#6B71F2", // blue
        "#9C27B0", // purple
        "#4CAF50", // green
        "#FF9800", // orange
        "#E91E63", // pink
        "#00BCD4", // cyan
        "#FFEB3B", // yellow
        "#F44336", // red
        "#009688", // teal
        "#3F51B5"  // indigo
    };
    
    /**
     * Initialise les avatars par défaut si le dossier est vide
     */
    public void initializeDefaultAvatars() {
        try {
            // Créer le dossier si nécessaire
            if (!Files.exists(avatarsPath)) {
                Files.createDirectories(avatarsPath);
            }
            
            // Vérifier si des avatars par défaut existent déjà
            boolean hasDefaultAvatars = listAvatars().stream()
                .anyMatch(f -> f.getName().startsWith("default-avatar-"));
            
            if (!hasDefaultAvatars) {
                System.out.println("👤 Génération des avatars par défaut...");
                generateAllDefaultAvatars();
            }
        } catch (IOException e) {
            System.err.println("Erreur initialisation avatars: " + e.getMessage());
        }
    }
    
    /**
     * Génère tous les avatars par défaut (fichiers PNG)
     */
    private void generateAllDefaultAvatars() {
        for (int i = 0; i < DEFAULT_AVATARS.length; i++) {
            generateDefaultAvatarFile(DEFAULT_AVATARS[i], DEFAULT_AVATAR_COLORS[i], i + 1);
        }
        System.out.println("✅ " + DEFAULT_AVATARS.length + " avatars par défaut générés");
    }
    
    /**
     * Génère un fichier avatar par défaut avec une icône utilisateur
     */
    private void generateDefaultAvatarFile(String fileName, String color, int index) {
        try {
            Path targetPath = avatarsPath.resolve(fileName);
            if (Files.exists(targetPath)) {
                return; // Déjà généré
            }
            
            // Créer une image SVG simple en PNG
            // On utilise JavaFX pour générer une image
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane();
                    pane.setPrefSize(128, 128);
                    
                    // Cercle de fond
                    javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(64);
                    circle.setFill(javafx.scene.paint.Color.web(color));
                    
                    // Icône utilisateur (silhouette simple)
                    javafx.scene.shape.Circle head = new javafx.scene.shape.Circle(20);
                    head.setFill(javafx.scene.paint.Color.WHITE);
                    head.setTranslateY(-15);
                    
                    javafx.scene.shape.Ellipse body = new javafx.scene.shape.Ellipse(30, 20);
                    body.setFill(javafx.scene.paint.Color.WHITE);
                    body.setTranslateY(25);
                    
                    javafx.scene.Group userIcon = new javafx.scene.Group(head, body);
                    
                    pane.getChildren().addAll(circle, userIcon);
                    
                    // Snapshot vers image
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    
                    // Scene nécessaire pour le rendu mais pas référencée directement
                    @SuppressWarnings("unused")
                    javafx.scene.Scene scene = new javafx.scene.Scene(pane, 128, 128);
                    javafx.scene.image.WritableImage writableImage = pane.snapshot(params, null);
                    
                    // Sauvegarder en PNG
                    java.awt.image.BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);
                    javax.imageio.ImageIO.write(bufferedImage, "png", targetPath.toFile());
                    
                    System.out.println("👤 Avatar généré: " + fileName);
                } catch (Exception e) {
                    System.err.println("Erreur génération avatar " + fileName + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur génération avatar: " + e.getMessage());
        }
    }
    
    /**
     * Liste les avatars par défaut disponibles
     */
    public List<String> getDefaultAvatarNames() {
        return Arrays.asList(DEFAULT_AVATARS);
    }
    
    /**
     * Vérifie si les avatars par défaut sont initialisés
     */
    public boolean hasDefaultAvatars() {
        if (!Files.exists(avatarsPath)) {
            return false;
        }
        return listAvatars().stream()
            .anyMatch(f -> f.getName().startsWith("default-avatar-"));
    }
    
    /**
     * Retourne le chemin du dossier Avatars
     */
    public Path getAvatarsPath() {
        return avatarsPath;
    }
    
    /**
     * Liste tous les avatars disponibles
     */
    public List<File> listAvatars() {
        return listMediaFiles(avatarsPath);
    }
    
    /**
     * Charge un avatar depuis le dossier Avatars
     * @param avatarPath chemin relatif ou absolu de l'avatar
     * @param size taille de l'avatar (carré)
     * @return Image de l'avatar ou null
     */
    public Image loadAvatar(String avatarPath, double size) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return null;
        }
        
        File file = new File(avatarPath);
        if (!file.isAbsolute()) {
            file = avatarsPath.resolve(avatarPath).toFile();
        }
        
        if (file.exists()) {
            String cacheKey = "avatar_" + file.getAbsolutePath() + "_" + (int)size;
            final File finalFile = file;
            return imageCache.computeIfAbsent(cacheKey, k ->
                new Image(finalFile.toURI().toString(), size, size, true, true, true)
            );
        }
        
        return null;
    }
    
    /**
     * Copie une photo pour en faire un avatar
     * @param sourceFile fichier source (photo)
     * @param personId identifiant de la personne
     * @param personName nom de la personne (pour le nom de fichier)
     * @return nom du fichier avatar créé
     */
    public String copyPhotoAsAvatar(File sourceFile, String personId, String personName) throws IOException {
        // Créer le dossier si nécessaire
        if (!Files.exists(avatarsPath)) {
            Files.createDirectories(avatarsPath);
        }
        
        String extension = getFileExtension(sourceFile.getName());
        String safeName = personName.replaceAll("[^a-zA-Z0-9]", "_");
        String targetName = "avatar_" + personId + "_" + safeName + extension;
        Path targetPath = avatarsPath.resolve(targetName);
        
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Invalider le cache
        invalidateAvatarCache(targetName);
        
        System.out.println("👤 Avatar créé: " + targetName);
        return targetName;
    }
    
    /**
     * Génère et sauvegarde un avatar par défaut basé sur les initiales
     * @param personId identifiant de la personne
     * @param firstName prénom
     * @param lastName nom
     * @return nom du fichier avatar créé
     */
    public String generateDefaultAvatar(String personId, String firstName, String lastName) {
        // Le nom du fichier sera basé sur l'ID et les initiales
        String initials = getInitials(firstName, lastName);
        String fileName = "default_" + personId + "_" + initials + ".png";
        
        // Vérifier si un avatar par défaut existe déjà
        Path avatarPath = avatarsPath.resolve(fileName);
        if (Files.exists(avatarPath)) {
            return fileName;
        }
        
        // Sinon, retourner le nom du fichier qui sera généré dynamiquement
        return fileName;
    }
    
    /**
     * Obtient les initiales d'une personne
     */
    public String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(Character.toUpperCase(firstName.charAt(0)));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(Character.toUpperCase(lastName.charAt(0)));
        }
        
        if (initials.length() == 0) {
            initials.append("?");
        }
        
        return initials.toString();
    }
    
    /**
     * Génère une couleur harmonique basée sur une chaîne (ID ou nom)
     * @param seed chaîne pour générer la couleur de manière déterministe
     * @return code couleur hexadécimal
     */
    public String generateAvatarColor(String seed) {
        // Palette de couleurs harmoniques MAGSAV
        String[] colors = {
            "#6B71F2", // Bleu violet principal
            "#F26BA6", // Rose
            "#A6F26B", // Vert lime
            "#6BF2A6", // Vert menthe
            "#8A7DD3", // Violet doux
            "#F2A66B", // Orange
            "#6BA6F2", // Bleu ciel
            "#D36BA6", // Magenta
            "#71C9CE", // Turquoise
            "#A06BF2", // Violet
            "#F2D36B", // Jaune doré
            "#6BF2D3"  // Cyan
        };
        
        if (seed == null || seed.isEmpty()) {
            return colors[0];
        }
        
        // Hash simple pour sélectionner une couleur de manière déterministe
        int hash = Math.abs(seed.hashCode());
        return colors[hash % colors.length];
    }
    
    /**
     * Supprime un avatar
     * @param avatarPath chemin relatif de l'avatar
     * @return true si suppression réussie
     */
    public boolean deleteAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return false;
        }
        
        // Ne pas supprimer les avatars par défaut générés
        if (avatarPath.startsWith("default_")) {
            return false;
        }
        
        try {
            Path fullPath = avatarsPath.resolve(avatarPath);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                invalidateAvatarCache(avatarPath);
                System.out.println("👤 Avatar supprimé: " + avatarPath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Erreur suppression avatar: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Invalide le cache d'un avatar spécifique
     */
    public void invalidateAvatarCache(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return;
        }
        
        File file = new File(avatarPath);
        if (!file.isAbsolute()) {
            file = avatarsPath.resolve(avatarPath).toFile();
        }
        
        String absolutePath = file.getAbsolutePath();
        List<String> keysToRemove = imageCache.keySet().stream()
            .filter(key -> key.startsWith("avatar_" + absolutePath))
            .toList();
        
        for (String key : keysToRemove) {
            imageCache.remove(key);
        }
    }
    
    /**
     * Vérifie si un avatar existe
     */
    public boolean avatarExists(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return false;
        }
        
        File file = new File(avatarPath);
        if (!file.isAbsolute()) {
            file = avatarsPath.resolve(avatarPath).toFile();
        }
        
        return file.exists();
    }
    
    // ========================================
    // 📝 RENOMMAGE DE FICHIERS
    // ========================================
    
    /**
     * Renomme un fichier média
     * @param file le fichier à renommer
     * @param newName le nouveau nom (sans extension)
     * @return le nouveau fichier ou null si échec
     */
    public File renameMediaFile(File file, String newName) {
        if (file == null || !file.exists() || newName == null || newName.trim().isEmpty()) {
            return null;
        }
        
        String extension = getFileExtension(file.getName());
        String newFileName = newName.trim() + extension;
        Path newPath = file.toPath().getParent().resolve(newFileName);
        
        // Vérifier si le nouveau nom existe déjà
        if (Files.exists(newPath) && !newPath.equals(file.toPath())) {
            System.err.println("⚠️ Un fichier avec ce nom existe déjà: " + newFileName);
            return null;
        }
        
        try {
            Files.move(file.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Fichier renommé: " + file.getName() + " → " + newFileName);
            
            // Invalider le cache
            invalidateAllCacheForFile(file.getAbsolutePath());
            
            return newPath.toFile();
        } catch (IOException e) {
            System.err.println("Erreur renommage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Invalide toutes les entrées de cache pour un fichier
     */
    private void invalidateAllCacheForFile(String absolutePath) {
        List<String> keysToRemove = imageCache.keySet().stream()
            .filter(key -> key.contains(absolutePath))
            .toList();
        
        for (String key : keysToRemove) {
            imageCache.remove(key);
        }
    }
    
    // ========================================
    // 🔗 ASSIGNATION MÉDIA-ÉQUIPEMENT
    // ========================================
    
    // Mapping code LOCMAT -> fichier photo
    private final Map<String, String> locmatPhotoMapping = new HashMap<>();
    
    /**
     * Assigne une photo à un code LOCMAT
     * @param locmatCode le code LOCMAT de l'équipement
     * @param photoFileName le nom du fichier photo
     */
    public void assignPhotoToLocmat(String locmatCode, String photoFileName) {
        if (locmatCode == null || locmatCode.isEmpty()) {
            return;
        }
        
        locmatPhotoMapping.put(locmatCode.toUpperCase().trim(), photoFileName);
        System.out.println("🔗 Photo assignée: " + locmatCode + " → " + photoFileName);
        
        // Sauvegarder le mapping
        saveLocmatPhotoMapping();
    }
    
    /**
     * Récupère la photo assignée à un code LOCMAT
     */
    public String getPhotoForLocmat(String locmatCode) {
        if (locmatCode == null || locmatCode.isEmpty()) {
            return null;
        }
        return locmatPhotoMapping.get(locmatCode.toUpperCase().trim());
    }
    
    /**
     * Supprime l'assignation d'une photo pour un code LOCMAT
     */
    public void removePhotoAssignment(String locmatCode) {
        if (locmatCode != null) {
            locmatPhotoMapping.remove(locmatCode.toUpperCase().trim());
            saveLocmatPhotoMapping();
        }
    }
    
    /**
     * Retourne tous les mappings LOCMAT -> photo
     */
    public Map<String, String> getAllLocmatPhotoMappings() {
        return Collections.unmodifiableMap(locmatPhotoMapping);
    }
    
    /**
     * Sauvegarde le mapping LOCMAT-photo dans un fichier
     */
    private void saveLocmatPhotoMapping() {
        try {
            Path mappingFile = basePath.resolve("locmat-photos.properties");
            Properties props = new Properties();
            props.putAll(locmatPhotoMapping);
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(mappingFile.toFile())) {
                props.store(fos, "Mapping LOCMAT -> Photo");
            }
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde mapping: " + e.getMessage());
        }
    }
    
    /**
     * Charge le mapping LOCMAT-photo depuis le fichier
     */
    public void loadLocmatPhotoMapping() {
        try {
            Path mappingFile = basePath.resolve("locmat-photos.properties");
            if (Files.exists(mappingFile)) {
                Properties props = new Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(mappingFile.toFile())) {
                    props.load(fis);
                }
                
                locmatPhotoMapping.clear();
                for (String key : props.stringPropertyNames()) {
                    locmatPhotoMapping.put(key, props.getProperty(key));
                }
                System.out.println("📂 Mapping LOCMAT-photos chargé: " + locmatPhotoMapping.size() + " entrées");
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement mapping: " + e.getMessage());
        }
    }
    
    /**
     * Renomme une photo pour qu'elle corresponde à un code LOCMAT
     * @param photoFile le fichier photo existant
     * @param locmatCode le code LOCMAT
     * @return le nouveau fichier ou null si échec
     */
    public File renamePhotoForLocmat(File photoFile, String locmatCode) {
        if (photoFile == null || locmatCode == null || locmatCode.isEmpty()) {
            return null;
        }
        
        // Créer un nom basé sur le code LOCMAT
        String cleanCode = locmatCode.replaceAll("[^a-zA-Z0-9-]", "_");
        File renamedFile = renameMediaFile(photoFile, cleanCode);
        
        if (renamedFile != null) {
            // Assigner automatiquement
            assignPhotoToLocmat(locmatCode, renamedFile.getName());
        }
        
        return renamedFile;
    }
}
