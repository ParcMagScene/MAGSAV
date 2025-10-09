package com.magsav.service;

import com.magsav.util.AppLogger;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service de gestion des médias avec détection et suppression automatique des doublons
 */
public class MediaMaintenanceService {
    
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp",
        ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".mp3", ".wav", ".m4a"
    );
    
    public static class DuplicateGroup {
        private final String hash;
        private final List<Path> files;
        private final long size;
        
        public DuplicateGroup(String hash, List<Path> files, long size) {
            this.hash = hash;
            this.files = new ArrayList<>(files);
            this.size = size;
        }
        
        public String getHash() { return hash; }
        public List<Path> getFiles() { return new ArrayList<>(files); }
        public long getSize() { return size; }
        public int getCount() { return files.size(); }
        public long getTotalWastedSpace() { return size * (files.size() - 1); }
        
        public Path getBestFile() {
            // Garde le fichier avec le nom le plus court ou le plus récent
            return files.stream()
                .min(Comparator.<Path>comparingInt(p -> p.getFileName().toString().length())
                     .thenComparing(p -> {
                         try {
                             return Files.getLastModifiedTime(p);
                         } catch (IOException e) {
                             return FileTime.fromMillis(0);
                         }
                     }))
                .orElse(files.get(0));
        }
        
        public List<Path> getDuplicatesToDelete() {
            Path best = getBestFile();
            return files.stream()
                .filter(p -> !p.equals(best))
                .collect(Collectors.toList());
        }
    }
    
    public static class MaintenanceProgress {
        private final int totalFiles;
        private final int processedFiles;
        private final String currentFile;
        private final List<DuplicateGroup> duplicatesFound;
        private final long spaceSaved;
        
        public MaintenanceProgress(int totalFiles, int processedFiles, String currentFile, 
                                 List<DuplicateGroup> duplicatesFound, long spaceSaved) {
            this.totalFiles = totalFiles;
            this.processedFiles = processedFiles;
            this.currentFile = currentFile;
            this.duplicatesFound = new ArrayList<>(duplicatesFound);
            this.spaceSaved = spaceSaved;
        }
        
        public int getTotalFiles() { return totalFiles; }
        public int getProcessedFiles() { return processedFiles; }
        public String getCurrentFile() { return currentFile; }
        public List<DuplicateGroup> getDuplicatesFound() { return new ArrayList<>(duplicatesFound); }
        public long getSpaceSaved() { return spaceSaved; }
        public double getProgress() { return totalFiles > 0 ? (double) processedFiles / totalFiles : 0; }
    }
    
    private Consumer<MaintenanceProgress> progressCallback;
    private Consumer<String> logCallback;
    
    public void setProgressCallback(Consumer<MaintenanceProgress> callback) {
        this.progressCallback = callback;
    }
    
    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }
    
    private void log(String message) {
        AppLogger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
    
    /**
     * Analyse et détecte les doublons dans les répertoires de médias
     */
    public CompletableFuture<List<DuplicateGroup>> scanForDuplicates(Path... directories) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("🔍 Début de l'analyse des doublons...");
                
                Map<String, List<Path>> hashToFiles = new HashMap<>();
                List<Path> allFiles = new ArrayList<>();
                
                // Collecte tous les fichiers média
                for (Path dir : directories) {
                    if (Files.exists(dir)) {
                        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                if (isMediaFile(file)) {
                                    allFiles.add(file);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }
                }
                
                log("📁 " + allFiles.size() + " fichiers média trouvés");
                
                // Calcul des hash pour chaque fichier
                int processed = 0;
                List<DuplicateGroup> duplicates = new ArrayList<>();
                
                for (Path file : allFiles) {
                    try {
                        String hash = calculateFileHash(file);
                        hashToFiles.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
                        
                        processed++;
                        
                        if (progressCallback != null) {
                            progressCallback.accept(new MaintenanceProgress(
                                allFiles.size(), processed, file.getFileName().toString(), 
                                duplicates, 0));
                        }
                        
                    } catch (Exception e) {
                        log("⚠️  Erreur lecture fichier " + file + ": " + e.getMessage());
                    }
                }
                
                // Identification des doublons
                for (Map.Entry<String, List<Path>> entry : hashToFiles.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        try {
                            long size = Files.size(entry.getValue().get(0));
                            DuplicateGroup group = new DuplicateGroup(entry.getKey(), entry.getValue(), size);
                            duplicates.add(group);
                        } catch (IOException e) {
                            log("⚠️  Erreur taille fichier: " + e.getMessage());
                        }
                    }
                }
                
                log("✅ Analyse terminée: " + duplicates.size() + " groupes de doublons trouvés");
                
                return duplicates;
                
            } catch (Exception e) {
                log("❌ Erreur lors de l'analyse: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Supprime automatiquement les doublons en gardant le meilleur fichier de chaque groupe
     */
    public CompletableFuture<Long> removeDuplicates(List<DuplicateGroup> duplicateGroups, boolean dryRun) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log(dryRun ? "🧪 Simulation de suppression des doublons..." : "🗑️  Suppression des doublons...");
                
                long totalSpaceSaved = 0;
                int groupsProcessed = 0;
                
                for (DuplicateGroup group : duplicateGroups) {
                    List<Path> toDelete = group.getDuplicatesToDelete();
                    Path kept = group.getBestFile();
                    
                    log("📂 Groupe " + (groupsProcessed + 1) + "/" + duplicateGroups.size() + 
                        " - Gardé: " + kept.getFileName() + ", Supprimés: " + toDelete.size());
                    
                    for (Path duplicate : toDelete) {
                        try {
                            if (!dryRun) {
                                Files.delete(duplicate);
                                log("  🗑️  Supprimé: " + duplicate.getFileName());
                            } else {
                                log("  🧪 À supprimer: " + duplicate.getFileName());
                            }
                            totalSpaceSaved += group.getSize();
                        } catch (IOException e) {
                            log("  ⚠️  Erreur suppression " + duplicate + ": " + e.getMessage());
                        }
                    }
                    
                    groupsProcessed++;
                    
                    if (progressCallback != null) {
                        progressCallback.accept(new MaintenanceProgress(
                            duplicateGroups.size(), groupsProcessed, "", 
                            duplicateGroups, totalSpaceSaved));
                    }
                }
                
                log("✅ " + (dryRun ? "Simulation" : "Suppression") + " terminée. " +
                    formatBytes(totalSpaceSaved) + " d'espace " + (dryRun ? "récupérable" : "récupéré"));
                
                return totalSpaceSaved;
                
            } catch (Exception e) {
                log("❌ Erreur lors de la suppression: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Maintenance complète : scan + suppression automatique
     */
    public CompletableFuture<Long> performMaintenance(boolean dryRun, Path... directories) {
        return scanForDuplicates(directories)
            .thenCompose(duplicates -> {
                if (duplicates.isEmpty()) {
                    log("✅ Aucun doublon trouvé, maintenance terminée");
                    return CompletableFuture.completedFuture(0L);
                }
                return removeDuplicates(duplicates, dryRun);
            });
    }
    
    private boolean isMediaFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    private String calculateFileHash(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(file);
        byte[] hash = md.digest(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}