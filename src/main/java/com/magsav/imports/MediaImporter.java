package com.magsav.imports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MediaImporter {
    
    public enum MediaType {
        PHOTOS("photos"),
        LOGOS("logos");
        
        private final String folderName;
        
        MediaType(String folderName) {
            this.folderName = folderName;
        }
        
        public String getFolderName() {
            return folderName;
        }
    }
    
    public record Result(int filesProcessed, int filesImported, List<String> errors, Path targetDirectory) {}
    
    private final Path baseMediaDirectory;
    
    public MediaImporter() {
        // Créer le répertoire media de base s'il n'existe pas
        this.baseMediaDirectory = Path.of(com.magsav.config.AppConfig.getMediaDirectory());
        try {
            Files.createDirectories(baseMediaDirectory);
            // Créer les sous-dossiers photos et logos
            Files.createDirectories(baseMediaDirectory.resolve("photos"));
            Files.createDirectories(baseMediaDirectory.resolve("logos"));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer les répertoires médias: " + e.getMessage(), e);
        }
    }
    
    public Result importFiles(List<Path> files, MediaType mediaType) {
        int processed = 0;
        int imported = 0;
        List<String> errors = new ArrayList<>();
        
        Path targetDirectory = baseMediaDirectory.resolve(mediaType.getFolderName());
        
        for (Path file : files) {
            processed++;
            try {
                if (isValidMediaFile(file, mediaType)) {
                    // Générer un nom unique pour éviter les conflits
                    String fileName = generateUniqueFileName(file);
                    Path targetPath = targetDirectory.resolve(fileName);
                    
                    // Copier le fichier
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    imported++;
                    
                    System.out.println("Fichier " + mediaType.getFolderName() + " importé: " + fileName + " -> " + targetPath);
                } else {
                    errors.add("Format de fichier non supporté pour " + mediaType.getFolderName() + ": " + file.getFileName());
                }
            } catch (IOException e) {
                errors.add("Erreur lors de l'import de " + file.getFileName() + ": " + e.getMessage());
            }
        }
        
        return new Result(processed, imported, errors, targetDirectory);
    }
    
    private boolean isValidMediaFile(Path file, MediaType mediaType) {
        String fileName = file.getFileName().toString().toLowerCase();
        
        if (mediaType == MediaType.PHOTOS) {
            // Photos: formats d'image uniquement
            return fileName.endsWith(".jpg") || 
                   fileName.endsWith(".jpeg") || 
                   fileName.endsWith(".png") || 
                   fileName.endsWith(".gif") ||
                   fileName.endsWith(".bmp") ||
                   fileName.endsWith(".tiff");
        } else if (mediaType == MediaType.LOGOS) {
            // Logos: images vectorielles et haute qualité
            return fileName.endsWith(".png") || 
                   fileName.endsWith(".svg") || 
                   fileName.endsWith(".jpg") || 
                   fileName.endsWith(".jpeg") ||
                   fileName.endsWith(".gif") ||
                   fileName.endsWith(".pdf"); // PDF vectoriel pour logos
        }
        
        return false;
    }
    
    private String generateUniqueFileName(Path originalFile) {
        String fileName = originalFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        // Ajouter timestamp pour l'unicité
        long timestamp = System.currentTimeMillis();
        return baseName + "_" + timestamp + extension;
    }
    
    public Path getBaseMediaDirectory() {
        return baseMediaDirectory;
    }
    
    public Path getMediaDirectory(MediaType mediaType) {
        return baseMediaDirectory.resolve(mediaType.getFolderName());
    }
    
    public List<Path> listImportedFiles(MediaType mediaType) {
        try {
            Path typeDirectory = getMediaDirectory(mediaType);
            return Files.list(typeDirectory)
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}