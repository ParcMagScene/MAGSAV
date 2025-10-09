package com.magsav.util;

import com.magsav.service.MediaMaintenanceService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Outil en ligne de commande pour la maintenance des médias
 */
public class MediaMaintenanceTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0].toLowerCase();
        boolean dryRun = args.length > 1 && "--dry-run".equals(args[1]);
        
        MediaMaintenanceService service = new MediaMaintenanceService();
        
        // Configuration des callbacks pour affichage console
        service.setLogCallback(System.out::println);
        service.setProgressCallback(progress -> {
            int percent = (int) (progress.getProgress() * 100);
            System.out.printf("\r[%3d%%] %s - Doublons trouvés: %d - Espace économisé: %s", 
                percent, progress.getCurrentFile(), 
                progress.getDuplicatesFound().size(),
                formatBytes(progress.getSpaceSaved()));
            if (percent == 100) System.out.println();
        });
        
        // Répertoires de médias MAGSAV
        Path[] mediaDirs = {
            Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "photos"),
            Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "logos"),
            Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "medium"),
            Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "thumbnails")
        };
        
        try {
            switch (command) {
                case "scan" -> {
                    System.out.println("🔍 Analyse des doublons dans les médias MAGSAV...");
                    List<MediaMaintenanceService.DuplicateGroup> duplicates = 
                        service.scanForDuplicates(mediaDirs).join();
                    
                    if (duplicates.isEmpty()) {
                        System.out.println("✅ Aucun doublon trouvé !");
                    } else {
                        System.out.println("\n📊 Résultats de l'analyse:");
                        long totalWasted = 0;
                        for (MediaMaintenanceService.DuplicateGroup group : duplicates) {
                            System.out.println("📁 Groupe de " + group.getCount() + " fichiers identiques (" + 
                                formatBytes(group.getSize()) + " chacun):");
                            group.getFiles().forEach(file -> System.out.println("  • " + file));
                            System.out.println("  → Garder: " + group.getBestFile().getFileName());
                            totalWasted += group.getTotalWastedSpace();
                        }
                        System.out.println("\n💾 Espace total récupérable: " + formatBytes(totalWasted));
                    }
                }
                
                case "clean" -> {
                    System.out.println(dryRun ? 
                        "🧪 Simulation de nettoyage des doublons..." : 
                        "🗑️  Nettoyage des doublons...");
                    
                    long spaceSaved = service.performMaintenance(dryRun, mediaDirs).join();
                    
                    System.out.println("\n✅ Maintenance terminée !");
                    System.out.println("💾 Espace " + (dryRun ? "récupérable" : "récupéré") + 
                        ": " + formatBytes(spaceSaved));
                }
                
                case "help" -> printUsage();
                
                default -> {
                    System.err.println("❌ Commande inconnue: " + command);
                    printUsage();
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("🛠️  MAGSAV Media Maintenance Tool");
        System.out.println();
        System.out.println("Usage: java " + MediaMaintenanceTool.class.getName() + " <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  scan          Analyse les doublons sans les supprimer");
        System.out.println("  clean         Supprime automatiquement les doublons");
        System.out.println("  help          Affiche cette aide");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --dry-run     Simulation sans modification (pour clean)");
        System.out.println();
        System.out.println("Exemples:");
        System.out.println("  gradle run --args=\"com.magsav.util.MediaMaintenanceTool scan\"");
        System.out.println("  gradle run --args=\"com.magsav.util.MediaMaintenanceTool clean --dry-run\"");
        System.out.println("  gradle run --args=\"com.magsav.util.MediaMaintenanceTool clean\"");
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}