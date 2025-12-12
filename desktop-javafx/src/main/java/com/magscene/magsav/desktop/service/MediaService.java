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
    
    public MediaService() {
        // Déterminer le chemin de base
        this.basePath = findMediaBasePath();
        this.photosPath = basePath.resolve(PHOTOS_FOLDER);
        this.logosPath = basePath.resolve(LOGOS_FOLDER);
        this.avatarsPath = basePath.resolve(AVATARS_FOLDER);
        
        // Initialiser le mapping des logos de marques
        initializeBrandLogoMapping();
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
        // Mapping manuel des noms de marques vers leurs logos
        brandLogoMapping.put("L-ACOUSTICS", "L-Acoustics_Square_Logo.svg.png");
        brandLogoMapping.put("L ACOUSTICS", "L-Acoustics_Square_Logo.svg.png");
        brandLogoMapping.put("YAMAHA", "Yamaha.jpeg");
        brandLogoMapping.put("SHURE", "shure-logo-png_seeklogo-476684.png");
        brandLogoMapping.put("SENNHEISER", "Sennheiser.png");
        brandLogoMapping.put("CHAUVET", "Chauvet.png");
        brandLogoMapping.put("ROBE", "Robe_Lighting_logo.svg.png");
        brandLogoMapping.put("CLAY PAKY", "ClayPaky.jpg");
        brandLogoMapping.put("CLAYPAKY", "ClayPaky.jpg");
        brandLogoMapping.put("MARTIN", "Martin.svg");
        brandLogoMapping.put("ETC", "ETC_4c.jpg");
        brandLogoMapping.put("ADB", "ADB.jpeg");
        brandLogoMapping.put("STARWAY", "Starway-logo-carre-or-fond-bleu.jpg");
        brandLogoMapping.put("DIMATEC", "DIMATEC-LOGO-250px-GIF-1.gif");
        brandLogoMapping.put("ALGAM", "Algam.jpg");
        brandLogoMapping.put("AXENTE", "Axente.png");
        brandLogoMapping.put("ESL", "ESL.png");
        brandLogoMapping.put("CSI", "CSI.jpeg");
        brandLogoMapping.put("DV2", "DV2.jpeg");
        brandLogoMapping.put("LAGOONA", "Lagoona.jpeg");
        brandLogoMapping.put("RJ", "RJ.png");
        brandLogoMapping.put("MAG SCENE", "LogoMagSceneBLACK.gif");
        brandLogoMapping.put("MAGSCENE", "LogoMagSceneBLACK.gif");
        brandLogoMapping.put("MAG SCÈNE", "LogoMagSceneBLACK.gif");
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
}
