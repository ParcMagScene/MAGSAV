package com.magsav.imports;

import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.IdService;
import com.magsav.util.AppLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Importeur CSV avec logs automatiques détaillés et callbacks pour le suivi
 * de progression en temps réel. Support des entêtes français.
 */
public class CsvImporter {
    private final ProductRepository productRepo;
    private final InterventionRepository interRepo;
    private final SocieteRepository socRepo;
    
    // Callbacks pour le suivi de progression
    private Consumer<String> logCallback = null;
    private Consumer<ImportProgress> progressCallback = null;
    
    public CsvImporter(ProductRepository productRepo, InterventionRepository interRepo, SocieteRepository socRepo) {
        this.productRepo = productRepo;
        this.interRepo = interRepo;
        this.socRepo = socRepo;
    }
    
    /**
     * Définit un callback pour recevoir les messages de log
     */
    public void setLogCallback(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }
    
    /**
     * Définit un callback pour recevoir les mises à jour de progression
     */
    public void setProgressCallback(Consumer<ImportProgress> progressCallback) {
        this.progressCallback = progressCallback;
    }
    
    /**
     * Résultat d'import avec logs détaillés
     */
    public static record Result(
        int rows, 
        int interventions, 
        int products, 
        List<String> errors, 
        List<String> logs,
        boolean dryRun,
        long durationMs,
        String summary
    ) {}
    
    /**
     * Informations de progression pour l'interface
     */
    public static record ImportProgress(
        int totalRows,
        int currentRow,
        int productsCreated,
        int interventionsCreated,
        int errorsCount,
        String currentOperation,
        double progressPercentage
    ) {}
    
    public Result importFile(Path path) throws IOException { 
        return importFile(path, false); 
    }
    
    public Result importFile(Path path, boolean dryRun) throws IOException {
        long startTime = System.currentTimeMillis();
        int rows = 0, interCreated = 0, prodCreated = 0;
        List<String> errors = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        
        log("=== DÉBUT DE L'IMPORT CSV ===", logs);
        log("Fichier: " + path.getFileName(), logs);
        log("Mode: " + (dryRun ? "SIMULATION" : "IMPORT RÉEL"), logs);
        log("Heure de début: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), logs);
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                String error = "Fichier vide";
                log("ERREUR: " + error, logs);
                throw new IOException(error);
            }
            
            log("En-tête détecté: " + headerLine, logs);
            
            char sep = detectSeparator(headerLine);
            log("Séparateur détecté: '" + sep + "'", logs);
            
            List<String> headers = parseLine(headerLine, sep);
            log("Colonnes détectées: " + headers.size() + " - " + headers, logs);
            
            Map<String, Integer> idx = mapHeaders(headers);
            log("Mapping des colonnes:", logs);
            for (Map.Entry<String, Integer> entry : idx.entrySet()) {
                log("  " + entry.getKey() + " -> colonne " + entry.getValue(), logs);
            }
            
            if (!idx.containsKey("produit") && !idx.containsKey("n_de_serie")) {
                String error = "Le CSV doit contenir au moins une colonne 'produit' ou 'n_de_serie'";
                log("ERREUR: " + error, logs);
                throw new IOException(error);
            }
            
            // Compter le nombre total de lignes pour la progression
            long totalLines = br.lines().count();
            log("Nombre total de lignes de données: " + totalLines, logs);
            
            // Relire le fichier pour le traitement
            try (BufferedReader br2 = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                br2.readLine(); // Skip header
                
                String line;
                while ((line = br2.readLine()) != null) {
                    rows++;
                    if (line.trim().isEmpty()) {
                        log("Ligne " + rows + ": ignorée (vide)", logs);
                        continue;
                    }
                    
                    // Mise à jour de la progression
                    updateProgress(totalLines, rows, prodCreated, interCreated, errors.size(), 
                                 "Traitement ligne " + rows);
                    
                    List<String> cols = parseLine(line, sep);
                    
                    try {
                        log("Ligne " + rows + ": début du traitement", logs);
                        
                        String produit = get(cols, idx, "produit").trim();
                        String sn = get(cols, idx, "n_de_serie").trim();
                        String fabricant = opt(cols, idx, "fabricant", "").trim();
                        String proprietaire = get(cols, idx, "proprietaire").trim();
                        String panne = get(cols, idx, "panne").trim();
                        String statutRaw = opt(cols, idx, "status", "Ouverte").trim();
                        String statut = mapInterventionStatus(statutRaw);
                        String situationRaw = opt(cols, idx, "situation", "").trim();
                        String situation = mapProductSituation(situationRaw);
                        String detecteur = opt(cols, idx, "detecteur", "").trim();
                        String dateEntreeRaw = opt(cols, idx, "date_entree", "").trim();
                        String dateSortieRaw = opt(cols, idx, "date_sortie", "").trim();
                        String suiviNo = opt(cols, idx, "n_suivi", "").trim();
                        
                        log("  Données extraites: produit='" + produit + "', sn='" + sn + "', fabricant='" + fabricant + "', proprietaire='" + proprietaire + "'", logs);
                        
                        if (produit.isEmpty() && sn.isEmpty()) {
                            String error = "PRODUIT ou N° de série manquant";
                            errors.add("Ligne " + rows + ": " + error);
                            log("  ERREUR: " + error, logs);
                            continue;
                        }
                        
                        String uid = IdService.generateUniqueUid(productRepo);
                        log("  UID généré: " + uid, logs);
                        
                        Long productId = null;
                        if (!sn.isEmpty()) {
                            var bySn = productRepo.findBySN(sn);
                            if (bySn.isPresent()) {
                                productId = bySn.get().id();
                                log("  Produit existant trouvé par SN: ID=" + productId, logs);
                            } else {
                                if (!dryRun) {
                                    productId = productRepo.insert(produit.isEmpty() ? "(inconnu)" : produit, sn, fabricant.isEmpty() ? null : fabricant, uid, situation);
                                    log("  Nouveau produit créé: ID=" + productId, logs);
                                } else {
                                    productId = -1L;
                                    log("  Nouveau produit simulé (dry-run)", logs);
                                }
                                prodCreated++;
                            }
                        } else {
                            if (!dryRun) {
                                productId = productRepo.insert(produit, sn, fabricant.isEmpty() ? null : fabricant, uid, situation);
                                log("  Produit créé sans SN: ID=" + productId, logs);
                            } else {
                                productId = -1L;
                                log("  Produit simulé sans SN (dry-run)", logs);
                            }
                            prodCreated++;
                        }
                        
                        String ownerType;
                        Long ownerSocieteId = null;
                        String pnorm = normalize(proprietaire);
                        if (pnorm.isEmpty() || pnorm.equals("mag_scene") || pnorm.equals("magscene")) {
                            ownerType = "MAG_SCENE";
                            log("  Propriétaire: MAG_SCENE", logs);
                        } else if (pnorm.equals("particulier")) {
                            ownerType = "PARTICULIER";
                            log("  Propriétaire: PARTICULIER", logs);
                        } else {
                            ownerType = "SOCIETE";
                            var found = socRepo.findByNameAndType(proprietaire, "CLIENT");
                            if (found.isPresent()) {
                                ownerSocieteId = found.get().id();
                                log("  Société existante trouvée: " + proprietaire + " (ID=" + ownerSocieteId + ")", logs);
                            } else {
                                if (!dryRun) {
                                    ownerSocieteId = socRepo.insert("CLIENT", proprietaire, null, null, null, null);
                                    log("  Nouvelle société créée: " + proprietaire + " (ID=" + ownerSocieteId + ")", logs);
                                } else {
                                    ownerSocieteId = -1L;
                                    log("  Nouvelle société simulée: " + proprietaire + " (dry-run)", logs);
                                }
                            }
                        }
                        
                        String dateEntree = parseDate(dateEntreeRaw);
                        String dateSortie = parseDate(dateSortieRaw);
                        
                        if (!dateEntreeRaw.isEmpty()) {
                            log("  Date d'entrée: " + dateEntreeRaw + " -> " + dateEntree, logs);
                        }
                        if (!dateSortieRaw.isEmpty()) {
                            log("  Date de sortie: " + dateSortieRaw + " -> " + dateSortie, logs);
                        }
                        
                        if (!dryRun) {
                            interRepo.insertFromImport(productId, statut, panne, detecteur, dateEntree, dateSortie, suiviNo, ownerType, ownerSocieteId);
                            log("  Intervention créée avec succès", logs);
                        } else {
                            log("  Intervention simulée (dry-run)", logs);
                        }
                        interCreated++;
                        
                        log("Ligne " + rows + ": traitement terminé avec succès", logs);
                        
                    } catch (Exception ex) {
                        String error = "Ligne " + rows + ": " + ex.getMessage();
                        errors.add(error);
                        log("  ERREUR: " + ex.getMessage(), logs);
                        AppLogger.error("CsvImporter", "Erreur ligne " + rows, ex);
                    }
                }
            }
        } catch (IOException e) {
            log("ERREUR I/O: " + e.getMessage(), logs);
            throw e;
        } catch (Exception e) {
            log("ERREUR INATTENDUE: " + e.getMessage(), logs);
            throw new IOException("Erreur import: " + e.getMessage(), e);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log("=== FIN DE L'IMPORT CSV ===", logs);
        log("Heure de fin: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), logs);
        log("Durée totale: " + duration + " ms (" + (duration / 1000.0) + " secondes)", logs);
        log("Lignes traitées: " + rows, logs);
        log("Produits créés: " + prodCreated, logs);
        log("Interventions créées: " + interCreated, logs);
        log("Erreurs: " + errors.size(), logs);
        
        String summary = String.format(
            "Import %s terminé en %.2f secondes\n" +
            "• %d lignes traitées\n" +
            "• %d produits créés\n" + 
            "• %d interventions créées\n" +
            "• %d erreurs rencontrées",
            dryRun ? "(simulation)" : "",
            duration / 1000.0,
            rows, prodCreated, interCreated, errors.size()
        );
        
        log("Résumé: " + summary, logs);
        
        // Notification finale de progression
        updateProgress(rows, rows, prodCreated, interCreated, errors.size(), "Import terminé");
        
        return new Result(rows, interCreated, prodCreated, errors, logs, dryRun, duration, summary);
    }
    
    private void log(String message, List<String> logs) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String logEntry = "[" + timestamp + "] " + message;
        logs.add(logEntry);
        
        // Callback vers l'interface si défini
        if (logCallback != null) {
            logCallback.accept(logEntry);
        }
        
        // Log vers le système
        AppLogger.info("CsvImport", message);
    }
    
    private void updateProgress(long total, int current, int productsCreated, int interventionsCreated, 
                               int errorsCount, String operation) {
        if (progressCallback != null) {
            double percentage = total > 0 ? (current * 100.0 / total) : 0;
            ImportProgress progress = new ImportProgress(
                (int) total, current, productsCreated, interventionsCreated, 
                errorsCount, operation, percentage
            );
            progressCallback.accept(progress);
        }
    }
    
    // === Méthodes utilitaires (copiées de CsvImporter original) ===
    
    private static String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }
    
    private static char detectSeparator(String line) {
        long commas = line.chars().filter(ch -> ch == ',').count();
        long semicolons = line.chars().filter(ch -> ch == ';').count();
        return semicolons > commas ? ';' : ',';
    }
    
    private static String mapInterventionStatus(String s) {
        if (s == null || s.trim().isEmpty()) return "Ouverte";
        String norm = normalize(s);
        return switch (norm) {
            case "ouverte", "ouvert", "en_cours", "encours", "pending", "open" -> "Ouverte";
            case "fermee", "ferme", "termine", "terminee", "closed", "done", "fini", "finie" -> "Fermée";
            case "suspendue", "suspendu", "pause", "paused", "attente", "en_attente" -> "Suspendue";
            default -> "Ouverte";
        };
    }
    
    private static String mapProductSituation(String s) {
        if (s == null || s.trim().isEmpty()) return "En stock";
        String norm = normalize(s);
        return switch (norm) {
            case "en_stock", "stock", "disponible", "magasin" -> "En stock";
            case "sav_mag", "sav_interne", "atelier", "reparation", "sav_mag_scene" -> "SAV Mag Scene";
            case "sav_externe", "externe", "sous_traitant" -> "SAV Externe";
            case "vendu", "vendue", "client", "livre" -> "Vendu";
            case "perdu", "perdue", "casse", "hs" -> "Déchet";
            default -> "En stock";
        };
    }
    
    private static List<String> parseLine(String line, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == separator && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }
    
    private static String get(List<String> cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        return (i != null && i < cols.size()) ? cols.get(i) : "";
    }
    
    private static String opt(List<String> cols, Map<String, Integer> idx, String key, String defaultValue) {
        String val = get(cols, idx, key);
        return val.isEmpty() ? defaultValue : val;
    }
    
    private static String headerKey(String h) {
        return normalize(h);
    }
    
    private static void alias(Map<String, Integer> idx, List<String> headers, String canonical, List<String> aliases) {
        for (String alias : aliases) {
            String key = headerKey(alias);
            if (!idx.containsKey(key)) {
                for (int i = 0; i < headers.size(); i++) {
                    if (headerKey(headers.get(i)).equals(key)) {
                        idx.put(canonical, i);
                        break;
                    }
                }
            }
        }
    }
    
    private static Map<String, Integer> mapHeaders(List<String> headers) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            idx.putIfAbsent(headerKey(headers.get(i)), i);
        }
        
        alias(idx, headers, "produit", List.of("product", "nom_produit", "nom", "libelle", "designation"));
        alias(idx, headers, "n_de_serie", List.of("no_de_serie", "sn", "serial", "n_serie", "numero_serie", "serie", "code_produit"));
        alias(idx, headers, "proprietaire", List.of("owner", "client", "societe"));
        alias(idx, headers, "panne", List.of("defaut", "probleme", "description", "desc"));
        alias(idx, headers, "n_suivi", List.of("no_suivi", "n°_suivi", "tracking", "tracking_no", "suivi"));
        alias(idx, headers, "status", List.of("statut", "etat", "state"));
        alias(idx, headers, "situation", List.of("stock", "localisation", "emplacement"));
        alias(idx, headers, "detecteur", List.of("technicien", "operateur", "responsable"));
        alias(idx, headers, "date_entree", List.of("entree", "date_debut", "debut"));
        alias(idx, headers, "date_sortie", List.of("sortie", "date_fin", "fin"));
        alias(idx, headers, "fabricant", List.of("manufacturer", "marque", "brand"));
        
        return idx;
    }
    
    private static String parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        String[] patterns = {
            "dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "dd.MM.yyyy", "d.M.yyyy"
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
                return date.toString(); // Format ISO (yyyy-MM-dd)
            } catch (DateTimeParseException ignored) {
                // Continue avec le pattern suivant
            }
        }
        
        return null; // Date non parseable
    }
}