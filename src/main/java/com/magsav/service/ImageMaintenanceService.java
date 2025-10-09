package com.magsav.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de maintenance des images pour MAGSAV
 * Gère la normalisation des images existantes et la détection des doublons
 */
public class ImageMaintenanceService {
    
    private final ImageNormalizationService normalizationService;
    private final Path photosDir;
    private final Path logosDir;
    private final Path thumbsDir;
    private final Path mediumDir;
    
    public ImageMaintenanceService() {
        this.normalizationService = new ImageNormalizationService();
        Path baseDir = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias");
        this.photosDir = baseDir.resolve("photos");
        this.logosDir = baseDir.resolve("logos");
        this.thumbsDir = baseDir.resolve("thumbs");
        this.mediumDir = baseDir.resolve("medium");
    }
    
    /**
     * Normalise toutes les images existantes qui ne le sont pas encore
     */
    public MaintenanceReport normalizeExistingImages() {
        MaintenanceReport report = new MaintenanceReport();
        
        try {
            if (!Files.exists(photosDir)) {
                report.addMessage("Dossier photos introuvable: " + photosDir);
                return report;
            }
            
            List<Path> imageFiles = Files.list(photosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(this::isNotNormalizedImage) // Éviter les images déjà normalisées
                .collect(Collectors.toList());
            
            report.totalImages = imageFiles.size();
            report.addMessage("Images à normaliser trouvées: " + imageFiles.size());
            
            for (Path imagePath : imageFiles) {
                try {
                    String originalName = imagePath.getFileName().toString();
                    String baseName = extractBaseName(originalName);
                    
                    // Vérifier si l'image n'est pas déjà normalisée
                    if (!normalizationService.isImageNormalized(baseName + "_large.jpg")) {
                        String normalizedName = normalizationService.normalizeImage(imagePath, baseName);
                        report.normalizedImages++;
                        report.addMessage("✓ Normalisé: " + originalName + " → " + normalizedName);
                        
                        // Optionnel: supprimer l'original après normalisation
                        // Files.delete(imagePath);
                    } else {
                        report.addMessage("⏭ Déjà normalisé: " + originalName);
                    }
                    
                } catch (Exception e) {
                    report.errors++;
                    report.addMessage("✗ Erreur pour " + imagePath.getFileName() + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            report.addMessage("Erreur lors de la lecture du dossier: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * Normalise tous les logos existants qui ne le sont pas encore
     */
    public MaintenanceReport normalizeExistingLogos() {
        MaintenanceReport report = new MaintenanceReport();
        
        try {
            if (!Files.exists(logosDir)) {
                report.addMessage("Dossier logos introuvable: " + logosDir);
                return report;
            }
            
            List<Path> logoFiles = Files.list(logosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(this::isNotNormalizedImage) // Éviter les logos déjà normalisés
                .collect(Collectors.toList());
            
            report.totalImages = logoFiles.size();
            report.addMessage("Logos à normaliser trouvés: " + logoFiles.size());
            
            for (Path logoPath : logoFiles) {
                try {
                    String originalName = logoPath.getFileName().toString();
                    String baseName = extractBaseName(originalName);
                    
                    // Vérifier si le logo n'est pas déjà normalisé
                    if (!normalizationService.isLogoNormalized(baseName + "_large.jpg")) {
                        String normalizedName = normalizationService.normalizeImageToLogos(logoPath, baseName);
                        report.normalizedImages++;
                        report.addMessage("✓ Logo normalisé: " + originalName + " → " + normalizedName);
                        
                        // Optionnel: supprimer l'original après normalisation
                        // Files.delete(logoPath);
                    } else {
                        report.addMessage("⏭ Logo déjà normalisé: " + originalName);
                    }
                    
                } catch (Exception e) {
                    report.errors++;
                    report.addMessage("✗ Erreur pour logo " + logoPath.getFileName() + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            report.addMessage("Erreur lors de la lecture du dossier logos: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * Détecte et supprime les images dupliquées basées sur leur contenu
     */
    public MaintenanceReport detectAndRemoveDuplicates() {
        MaintenanceReport report = new MaintenanceReport();
        
        try {
            if (!Files.exists(photosDir)) {
                report.addMessage("Dossier photos introuvable: " + photosDir);
                return report;
            }
            
            List<Path> imageFiles = Files.list(photosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .collect(Collectors.toList());
            
            report.totalImages = imageFiles.size();
            report.addMessage("Analyse de " + imageFiles.size() + " images pour détecter les doublons...");
            
            // Grouper les images par hash de contenu
            Map<String, List<Path>> imagesByHash = new HashMap<>();
            
            for (Path imagePath : imageFiles) {
                try {
                    String hash = calculateImageHash(imagePath);
                    imagesByHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(imagePath);
                } catch (Exception e) {
                    report.errors++;
                    report.addMessage("✗ Erreur calcul hash pour " + imagePath.getFileName() + ": " + e.getMessage());
                }
            }
            
            // Identifier et traiter les doublons
            for (Map.Entry<String, List<Path>> entry : imagesByHash.entrySet()) {
                List<Path> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    report.duplicateGroups++;
                    report.addMessage("📋 Groupe de " + duplicates.size() + " doublons détectés:");
                    
                    // Garder le premier (ou le plus récent), supprimer les autres
                    Path keeper = findBestImageToKeep(duplicates);
                    for (Path duplicate : duplicates) {
                        if (!duplicate.equals(keeper)) {
                            try {
                                Files.delete(duplicate);
                                report.removedDuplicates++;
                                report.addMessage("   ✗ Supprimé: " + duplicate.getFileName());
                            } catch (IOException e) {
                                report.errors++;
                                report.addMessage("   ✗ Erreur suppression " + duplicate.getFileName() + ": " + e.getMessage());
                            }
                        } else {
                            report.addMessage("   ✓ Conservé: " + keeper.getFileName());
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            report.addMessage("Erreur lors de l'analyse des doublons: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * Effectue un nettoyage complet : normalisation + suppression doublons
     */
    public MaintenanceReport performFullMaintenance() {
        MaintenanceReport fullReport = new MaintenanceReport();
        fullReport.addMessage("=== MAINTENANCE COMPLÈTE DES IMAGES ===");
        
        // Étape 1: Normalisation
        fullReport.addMessage("\n--- NORMALISATION DES IMAGES ---");
        MaintenanceReport normalizeReport = normalizeExistingImages();
        fullReport.merge(normalizeReport);
        
        // Étape 2: Suppression des doublons
        fullReport.addMessage("\n--- DÉTECTION ET SUPPRESSION DES DOUBLONS ---");
        MaintenanceReport duplicateReport = detectAndRemoveDuplicates();
        fullReport.merge(duplicateReport);
        
        fullReport.addMessage("\n=== RÉSUMÉ ===");
        fullReport.addMessage("Images normalisées: " + fullReport.normalizedImages);
        fullReport.addMessage("Groupes de doublons: " + fullReport.duplicateGroups);
        fullReport.addMessage("Doublons supprimés: " + fullReport.removedDuplicates);
        fullReport.addMessage("Erreurs: " + fullReport.errors);
        
        return fullReport;
    }
    
    /**
     * Calcule un hash MD5 du contenu de l'image pour détecter les doublons
     */
    private String calculateImageHash(Path imagePath) throws IOException, NoSuchAlgorithmException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        if (image == null) {
            throw new IOException("Impossible de lire l'image");
        }
        
        // Redimensionner à une taille fixe pour la comparaison (évite les différences de taille)
        BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(image, 0, 0, 64, 64, null);
        
        // Calculer le hash des pixels
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int rgb = resized.getRGB(x, y);
                md.update((byte) (rgb >> 16));
                md.update((byte) (rgb >> 8));
                md.update((byte) rgb);
            }
        }
        
        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Choisit la meilleure image à conserver parmi les doublons
     */
    private Path findBestImageToKeep(List<Path> duplicates) {
        // Critères de sélection:
        // 1. Image normalisée (se termine par _large.jpg)
        // 2. Plus récente
        // 3. Plus grande taille de fichier
        
        return duplicates.stream()
            .max(Comparator
                .<Path>comparingInt(path -> isNormalizedImage(path) ? 1 : 0)  // Préférer normalisées
                .thenComparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        return null;
                    }
                })  // Plus récente
                .thenComparing(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0L;
                    }
                })  // Plus grande
            )
            .orElse(duplicates.get(0));
    }
    
    private boolean isImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif") ||
               fileName.endsWith(".bmp") || fileName.endsWith(".tiff");
    }
    
    private boolean isNormalizedImage(Path path) {
        return path.getFileName().toString().endsWith("_large.jpg") ||
               path.getFileName().toString().endsWith("_medium.jpg") ||
               path.getFileName().toString().endsWith("_thumb.jpg");
    }
    
    private boolean isNotNormalizedImage(Path path) {
        return !isNormalizedImage(path);
    }
    
    private String extractBaseName(String fileName) {
        // Retirer l'extension et nettoyer le nom
        int lastDot = fileName.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        return nameWithoutExt.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
    
    /**
     * Liste toutes les photos disponibles (originals uniquement, pas les versions normalisées)
     */
    public List<String> listAvailablePhotos() {
        try {
            if (!Files.exists(photosDir)) {
                return new ArrayList<>();
            }
            
            return Files.list(photosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(this::isNotNormalizedImage)
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
                
        } catch (IOException e) {
            System.err.println("Erreur lors de la liste des photos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Liste tous les logos disponibles (originals uniquement, pas les versions normalisées)
     */
    public List<String> listAvailableLogos() {
        try {
            if (!Files.exists(logosDir)) {
                return new ArrayList<>();
            }
            
            return Files.list(logosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(this::isNotNormalizedImage)
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
                
        } catch (IOException e) {
            System.err.println("Erreur lors de la liste des logos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Supprime les photos sélectionnées et leurs versions normalisées
     */
    public MaintenanceReport deleteSelectedPhotos(List<String> photoNames) {
        MaintenanceReport report = new MaintenanceReport();
        
        for (String photoName : photoNames) {
            try {
                Path originalPath = photosDir.resolve(photoName);
                boolean deleted = false;
                
                // Supprimer l'image originale
                if (Files.exists(originalPath)) {
                    Files.delete(originalPath);
                    deleted = true;
                    report.addMessage("✓ Photo supprimée: " + photoName);
                }
                
                // Supprimer les versions normalisées
                String baseName = extractBaseName(photoName);
                String[] suffixes = {"_thumb.jpg", "_medium.jpg", "_large.jpg"};
                
                for (String suffix : suffixes) {
                    Path normalizedPath = photosDir.resolve(baseName + suffix);
                    if (Files.exists(normalizedPath)) {
                        Files.delete(normalizedPath);
                        deleted = true;
                        report.addMessage("  ✓ Version supprimée: " + baseName + suffix);
                    }
                    
                    // Vérifier aussi dans les dossiers thumbs et medium
                    Path thumbPath = thumbsDir.resolve(baseName + suffix);
                    if (Files.exists(thumbPath)) {
                        Files.delete(thumbPath);
                        report.addMessage("  ✓ Vignette supprimée: " + baseName + suffix);
                    }
                    
                    Path mediumPath = mediumDir.resolve(baseName + suffix);
                    if (Files.exists(mediumPath)) {
                        Files.delete(mediumPath);
                        report.addMessage("  ✓ Taille moyenne supprimée: " + baseName + suffix);
                    }
                }
                
                if (deleted) {
                    report.normalizedImages++; // Réutilisation du compteur pour les suppressions
                }
                
            } catch (IOException e) {
                report.errors++;
                report.addMessage("✗ Erreur suppression photo " + photoName + ": " + e.getMessage());
            }
        }
        
        return report;
    }
    
    /**
     * Supprime les logos sélectionnés et leurs versions normalisées
     */
    public MaintenanceReport deleteSelectedLogos(List<String> logoNames) {
        MaintenanceReport report = new MaintenanceReport();
        
        for (String logoName : logoNames) {
            try {
                Path originalPath = logosDir.resolve(logoName);
                boolean deleted = false;
                
                // Supprimer le logo original
                if (Files.exists(originalPath)) {
                    Files.delete(originalPath);
                    deleted = true;
                    report.addMessage("✓ Logo supprimé: " + logoName);
                }
                
                // Supprimer les versions normalisées
                String baseName = extractBaseName(logoName);
                String[] suffixes = {"_thumb.jpg", "_medium.jpg", "_large.jpg"};
                
                for (String suffix : suffixes) {
                    Path normalizedPath = logosDir.resolve(baseName + suffix);
                    if (Files.exists(normalizedPath)) {
                        Files.delete(normalizedPath);
                        deleted = true;
                        report.addMessage("  ✓ Version logo supprimée: " + baseName + suffix);
                    }
                }
                
                if (deleted) {
                    report.normalizedImages++; // Réutilisation du compteur pour les suppressions
                }
                
            } catch (IOException e) {
                report.errors++;
                report.addMessage("✗ Erreur suppression logo " + logoName + ": " + e.getMessage());
            }
        }
        
        return report;
    }
    
    /**
     * Classe pour le rapport de maintenance
     */
    public static class MaintenanceReport {
        public int totalImages = 0;
        public int normalizedImages = 0;
        public int duplicateGroups = 0;
        public int removedDuplicates = 0;
        public int errors = 0;
        private final List<String> messages = new ArrayList<>();
        
        public void addMessage(String message) {
            messages.add(message);
            System.out.println(message);
        }
        
        public List<String> getMessages() {
            return new ArrayList<>(messages);
        }
        
        public void merge(MaintenanceReport other) {
            this.totalImages += other.totalImages;
            this.normalizedImages += other.normalizedImages;
            this.duplicateGroups += other.duplicateGroups;
            this.removedDuplicates += other.removedDuplicates;
            this.errors += other.errors;
            this.messages.addAll(other.messages);
        }
        
        @Override
        public String toString() {
            return String.join("\n", messages);
        }
    }
}