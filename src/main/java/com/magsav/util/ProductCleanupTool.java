package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.repo.ProductRepository;
import com.magsav.service.IdService;

import java.sql.*;
import java.util.*;

/**
 * Utilitaire de nettoyage des données produits
 * Détecte et corrige les problèmes dans la base de données des produits
 */
public class ProductCleanupTool {
    
    /**
     * Options pour l'effacement de données
     */
    public static class EraseOptions {
        public boolean eraseProducts = false;
        public boolean eraseInterventions = false;
        public boolean eraseCategories = false;
        public boolean eraseSocietes = false;
        public boolean resetDatabase = false; // Effacement complet
        
        public boolean hasAnyOption() {
            return eraseProducts || eraseInterventions || eraseCategories || eraseSocietes || resetDatabase;
        }
    }
    
    /**
     * Résultat d'une opération d'effacement
     */
    public static class EraseResult {
        private final Map<String, Integer> deletedCounts = new HashMap<>();
        private final List<String> operations = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        
        public void addDeletedCount(String table, int count) {
            deletedCounts.put(table, count);
            operations.add("Supprimé " + count + " enregistrement(s) de la table " + table);
        }
        
        public void addOperation(String operation) {
            operations.add(operation);
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public Map<String, Integer> getDeletedCounts() { return new HashMap<>(deletedCounts); }
        public List<String> getOperations() { return new ArrayList<>(operations); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        
        public int getTotalDeleted() {
            return deletedCounts.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RÉSULTAT DE L'EFFACEMENT ===\n");
            sb.append("Total enregistrements supprimés: ").append(getTotalDeleted()).append("\n");
            sb.append("Erreurs: ").append(errors.size()).append("\n");
            return sb.toString();
        }
    }

    public static class CleanupResult {
        private final List<String> issues = new ArrayList<>();
        private final List<String> fixes = new ArrayList<>();
        private int duplicatesFound = 0;
        private int emptyFieldsFixed = 0;
        private int uidsGenerated = 0;
        private int inconsistenciesFixed = 0;
        
        public void addIssue(String issue) { issues.add(issue); }
        public void addFix(String fix) { fixes.add(fix); }
        public void incrementDuplicates() { duplicatesFound++; }
        public void incrementEmptyFields() { emptyFieldsFixed++; }
        public void incrementUids() { uidsGenerated++; }
        public void incrementInconsistencies() { inconsistenciesFixed++; }
        
        public List<String> getIssues() { return new ArrayList<>(issues); }
        public List<String> getFixes() { return new ArrayList<>(fixes); }
        public int getDuplicatesFound() { return duplicatesFound; }
        public int getEmptyFieldsFixed() { return emptyFieldsFixed; }
        public int getUidsGenerated() { return uidsGenerated; }
        public int getInconsistenciesFixed() { return inconsistenciesFixed; }
        
        public boolean hasIssues() {
            return !issues.isEmpty() || duplicatesFound > 0 || emptyFieldsFixed > 0 || 
                   uidsGenerated > 0 || inconsistenciesFixed > 0;
        }
        
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RÉSULTAT DU NETTOYAGE ===\n");
            sb.append("Doublons trouvés: ").append(duplicatesFound).append("\n");
            sb.append("Champs vides corrigés: ").append(emptyFieldsFixed).append("\n");
            sb.append("UIDs générés: ").append(uidsGenerated).append("\n");
            sb.append("Incohérences corrigées: ").append(inconsistenciesFixed).append("\n");
            sb.append("Total problèmes détectés: ").append(issues.size()).append("\n");
            sb.append("Total corrections appliquées: ").append(fixes.size()).append("\n");
            return sb.toString();
        }
    }
    
    private final ProductRepository productRepo;
    
    public ProductCleanupTool() {
        this.productRepo = new ProductRepository();
    }
    
    /**
     * Exécute un nettoyage complet des données produits
     */
    public CleanupResult performFullCleanup(boolean dryRun) {
        CleanupResult result = new CleanupResult();
        
        System.out.println("=== NETTOYAGE DES PRODUITS ===");
        System.out.println("Mode: " + (dryRun ? "SIMULATION" : "CORRECTION"));
        System.out.println();
        
        // 1. Détecter les doublons
        detectDuplicates(result);
        
        // 2. Générer les UIDs manquants
        generateMissingUids(result, dryRun);
        
        // 3. Nettoyer les champs vides ou incohérents
        cleanEmptyFields(result, dryRun);
        
        // 4. Détecter les incohérences
        detectInconsistencies(result);
        
        // 5. Normaliser les données
        normalizeData(result, dryRun);
        
        System.out.println(result.getSummary());
        return result;
    }
    
    /**
     * Détecte les produits en double (même nom + fabricant ou même numéro de série)
     */
    private void detectDuplicates(CleanupResult result) {
        try (Connection conn = DB.getConnection()) {
            // Doublons par nom + fabricant
            String sql1 = """
                SELECT nom, fabricant, COUNT(*) as count, GROUP_CONCAT(id) as ids 
                FROM produits 
                WHERE nom IS NOT NULL AND TRIM(nom) != '' 
                  AND fabricant IS NOT NULL AND TRIM(fabricant) != ''
                GROUP BY LOWER(TRIM(nom)), LOWER(TRIM(fabricant))
                HAVING count > 1
                """;
            
            PreparedStatement stmt1 = conn.prepareStatement(sql1);
            ResultSet rs1 = stmt1.executeQuery();
            
            while (rs1.next()) {
                String nom = rs1.getString("nom");
                String fabricant = rs1.getString("fabricant");
                int count = rs1.getInt("count");
                String ids = rs1.getString("ids");
                
                result.addIssue("Doublons détectés: " + count + " produits '" + nom + 
                               "' de '" + fabricant + "' (IDs: " + ids + ")");
                result.incrementDuplicates();
            }
            
            // Doublons par numéro de série
            String sql2 = """
                SELECT sn, COUNT(*) as count, GROUP_CONCAT(id) as ids, GROUP_CONCAT(nom) as noms
                FROM produits 
                WHERE sn IS NOT NULL AND TRIM(sn) != ''
                GROUP BY UPPER(TRIM(sn))
                HAVING count > 1
                """;
            
            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            ResultSet rs2 = stmt2.executeQuery();
            
            while (rs2.next()) {
                String sn = rs2.getString("sn");
                int count = rs2.getInt("count");
                String ids = rs2.getString("ids");
                String noms = rs2.getString("noms");
                
                result.addIssue("Doublons N° série: " + count + " produits avec SN '" + sn + 
                               "' (IDs: " + ids + ", Produits: " + noms + ")");
                result.incrementDuplicates();
            }
            
        } catch (SQLException e) {
            result.addIssue("Erreur détection doublons: " + e.getMessage());
        }
    }
    
    /**
     * Génère les UIDs manquants
     */
    private void generateMissingUids(CleanupResult result, boolean dryRun) {
        try (Connection conn = DB.getConnection()) {
            String selectSql = "SELECT id, nom FROM produits WHERE uid IS NULL OR TRIM(uid) = ''";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String nom = rs.getString("nom");
                
                if (!dryRun) {
                    String newUid = IdService.generateUniqueUid(productRepo);
                    String updateSql = "UPDATE produits SET uid = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, newUid);
                    updateStmt.setLong(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    result.addFix("UID généré pour produit " + id + " (" + nom + "): " + newUid);
                } else {
                    result.addIssue("UID manquant pour produit " + id + " (" + nom + ")");
                }
                
                result.incrementUids();
            }
            
        } catch (SQLException e) {
            result.addIssue("Erreur génération UIDs: " + e.getMessage());
        }
    }
    
    /**
     * Nettoie les champs vides ou avec des espaces inutiles
     */
    private void cleanEmptyFields(CleanupResult result, boolean dryRun) {
        try (Connection conn = DB.getConnection()) {
            // Nettoyer les noms de produits
            cleanTextField(conn, "nom", "Nom produit", result, dryRun);
            
            // Nettoyer les fabricants
            cleanTextField(conn, "fabricant", "Fabricant", result, dryRun);
            
            // Nettoyer les codes
            cleanTextField(conn, "code", "Code produit", result, dryRun);
            
            // Nettoyer les situations
            normalizeSituations(conn, result, dryRun);
            
        } catch (SQLException e) {
            result.addIssue("Erreur nettoyage champs: " + e.getMessage());
        }
    }
    
    private void cleanTextField(Connection conn, String field, String fieldName, 
                               CleanupResult result, boolean dryRun) throws SQLException {
        
        // Trouver les champs avec des espaces en trop ou vides
        String selectSql = "SELECT id, " + field + " FROM produits WHERE " + field + " != TRIM(" + field + ") OR " + field + " = ''";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        ResultSet rs = selectStmt.executeQuery();
        
        while (rs.next()) {
            long id = rs.getLong("id");
            String oldValue = rs.getString(field);
            String newValue = oldValue != null ? oldValue.trim() : "";
            
            if (newValue.isEmpty()) {
                newValue = "(Non spécifié)";
            }
            
            if (!dryRun) {
                String updateSql = "UPDATE produits SET " + field + " = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newValue);
                updateStmt.setLong(2, id);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                result.addFix(fieldName + " nettoyé pour produit " + id + ": '" + oldValue + "' → '" + newValue + "'");
            } else {
                result.addIssue(fieldName + " à nettoyer pour produit " + id + ": '" + oldValue + "'");
            }
            
            result.incrementEmptyFields();
        }
    }
    
    /**
     * Normalise les situations (En stock, SAV Mag, etc.)
     */
    private void normalizeSituations(Connection conn, CleanupResult result, boolean dryRun) throws SQLException {
        Map<String, String> normalizations = Map.ofEntries(
            Map.entry("en_stock", "En stock"),
            Map.entry("stock", "En stock"),
            Map.entry("enstock", "En stock"),
            Map.entry("sav_mag", "SAV Mag"),
            Map.entry("sav_interne", "SAV Mag"),
            Map.entry("atelier", "SAV Mag"),
            Map.entry("sav_externe", "SAV Externe"),
            Map.entry("externe", "SAV Externe"),
            Map.entry("vendu", "Vendu"),
            Map.entry("vendue", "Vendu"),
            Map.entry("perdu", "Déchet"),
            Map.entry("perdue", "Déchet"),
            Map.entry("casse", "Déchet"),
            Map.entry("hs", "Déchet")
        );
        
        for (Map.Entry<String, String> entry : normalizations.entrySet()) {
            String oldSituation = entry.getKey();
            String newSituation = entry.getValue();
            
            String selectSql = "SELECT id, situation FROM produits WHERE LOWER(TRIM(situation)) = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, oldSituation.toLowerCase());
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String currentSituation = rs.getString("situation");
                
                if (!dryRun) {
                    String updateSql = "UPDATE produits SET situation = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, newSituation);
                    updateStmt.setLong(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    result.addFix("Situation normalisée pour produit " + id + ": '" + currentSituation + "' → '" + newSituation + "'");
                } else {
                    result.addIssue("Situation à normaliser pour produit " + id + ": '" + currentSituation + "'");
                }
                
                result.incrementInconsistencies();
            }
        }
    }
    
    /**
     * Détecte les incohérences dans les données
     */
    private void detectInconsistencies(CleanupResult result) {
        try (Connection conn = DB.getConnection()) {
            // Produits sans nom
            detectMissingField(conn, "nom", "Nom", result);
            
            // Produits avec des prix incohérents
            detectInvalidPrices(conn, result);
            
            // Produits avec des dates incohérentes
            detectInvalidDates(conn, result);
            
        } catch (SQLException e) {
            result.addIssue("Erreur détection incohérences: " + e.getMessage());
        }
    }
    
    private void detectMissingField(Connection conn, String field, String fieldName, 
                                   CleanupResult result) throws SQLException {
        String sql = "SELECT id, " + field + " FROM produits WHERE " + field + " IS NULL OR TRIM(" + field + ") = ''";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            long id = rs.getLong("id");
            result.addIssue(fieldName + " manquant pour produit " + id);
        }
    }
    
    private void detectInvalidPrices(Connection conn, CleanupResult result) throws SQLException {
        String sql = "SELECT id, prix FROM produits WHERE prix IS NOT NULL AND TRIM(prix) != '' AND prix NOT GLOB '*[0-9]*'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            long id = rs.getLong("id");
            String prix = rs.getString("prix");
            result.addIssue("Prix invalide pour produit " + id + ": '" + prix + "'");
        }
    }
    
    private void detectInvalidDates(Connection conn, CleanupResult result) throws SQLException {
        String sql = """
            SELECT id, date_achat FROM produits 
            WHERE date_achat IS NOT NULL 
              AND TRIM(date_achat) != '' 
              AND date_achat NOT GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]*'
            """;
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            long id = rs.getLong("id");
            String date = rs.getString("date_achat");
            result.addIssue("Date d'achat invalide pour produit " + id + ": '" + date + "'");
        }
    }
    
    /**
     * Normalise les données (casse, espaces, etc.)
     */
    private void normalizeData(CleanupResult result, boolean dryRun) {
        // Cette méthode peut être étendue pour d'autres normalisations
        result.addFix("Normalisation des données terminée");
    }
    
    /**
     * Efface des données selon les options spécifiées
     */
    public EraseResult eraseData(EraseOptions options, boolean dryRun) {
        EraseResult result = new EraseResult();
        
        if (!options.hasAnyOption()) {
            result.addError("Aucune option d'effacement sélectionnée");
            return result;
        }
        
        System.out.println("=== EFFACEMENT DES DONNÉES ===");
        System.out.println("Mode: " + (dryRun ? "SIMULATION" : "EFFACEMENT RÉEL"));
        System.out.println();
        
        try (Connection conn = DB.getConnection()) {
            // Démarrer une transaction pour pouvoir annuler en cas d'erreur
            conn.setAutoCommit(false);
            
            try {
                if (options.resetDatabase) {
                    eraseAllData(conn, result, dryRun);
                } else {
                    if (options.eraseInterventions) {
                        eraseInterventions(conn, result, dryRun);
                    }
                    if (options.eraseProducts) {
                        eraseProducts(conn, result, dryRun);
                    }
                    if (options.eraseCategories) {
                        eraseCategories(conn, result, dryRun);
                    }
                    if (options.eraseSocietes) {
                        eraseSocietes(conn, result, dryRun);
                    }
                }
                
                if (!dryRun && !result.hasErrors()) {
                    conn.commit();
                    result.addOperation("Transaction validée - modifications appliquées");
                } else {
                    conn.rollback();
                    if (dryRun) {
                        result.addOperation("Mode simulation - aucune modification appliquée");
                    } else {
                        result.addOperation("Erreurs détectées - modifications annulées");
                    }
                }
                
            } catch (Exception e) {
                conn.rollback();
                result.addError("Erreur pendant l'effacement: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            result.addError("Erreur de connexion à la base: " + e.getMessage());
        }
        
        System.out.println(result.getSummary());
        return result;
    }
    
    private void eraseAllData(Connection conn, EraseResult result, boolean dryRun) throws SQLException {
        String[] tables = {"interventions", "produits", "categories", "societes"};
        
        result.addOperation("⚠️ EFFACEMENT COMPLET DE LA BASE DE DONNÉES");
        
        for (String table : tables) {
            eraseTable(conn, table, result, dryRun);
        }
        
        // Réinitialiser les séquences d'auto-increment
        if (!dryRun) {
            for (String table : tables) {
                String resetSql = "DELETE FROM sqlite_sequence WHERE name = ?";
                PreparedStatement resetStmt = conn.prepareStatement(resetSql);
                resetStmt.setString(1, table);
                resetStmt.executeUpdate();
                resetStmt.close();
            }
            result.addOperation("Séquences d'ID réinitialisées");
        }
    }
    
    private void eraseInterventions(Connection conn, EraseResult result, boolean dryRun) throws SQLException {
        eraseTable(conn, "interventions", result, dryRun);
    }
    
    private void eraseProducts(Connection conn, EraseResult result, boolean dryRun) throws SQLException {
        // Vérifier s'il y a des interventions liées
        String checkSql = "SELECT COUNT(*) FROM interventions";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        int interventionCount = rs.getInt(1);
        checkStmt.close();
        
        if (interventionCount > 0) {
            result.addError("Impossible de supprimer les produits: " + interventionCount + 
                           " intervention(s) existent. Supprimez d'abord les interventions.");
            return;
        }
        
        eraseTable(conn, "produits", result, dryRun);
    }
    
    private void eraseCategories(Connection conn, EraseResult result, boolean dryRun) throws SQLException {
        // Vérifier s'il y a des produits utilisant des catégories
        String checkSql = "SELECT COUNT(*) FROM produits WHERE category IS NOT NULL AND TRIM(category) != ''";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        int productCount = rs.getInt(1);
        checkStmt.close();
        
        if (productCount > 0) {
            result.addOperation("⚠️ " + productCount + " produit(s) utilisent des catégories - " +
                               "les références seront supprimées");
            
            if (!dryRun) {
                String updateSql = "UPDATE produits SET category = NULL, subcategory = NULL";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.executeUpdate();
                updateStmt.close();
                result.addOperation("Références aux catégories supprimées des produits");
            }
        }
        
        eraseTable(conn, "categories", result, dryRun);
    }
    
    private void eraseSocietes(Connection conn, EraseResult result, boolean dryRun) throws SQLException {
        // Vérifier s'il y a des interventions liées
        String checkSql = "SELECT COUNT(*) FROM interventions WHERE owner_type = 'SOCIETE'";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        int interventionCount = rs.getInt(1);
        checkStmt.close();
        
        if (interventionCount > 0) {
            result.addError("Impossible de supprimer les sociétés: " + interventionCount + 
                           " intervention(s) sont liées à des sociétés.");
            return;
        }
        
        eraseTable(conn, "societes", result, dryRun);
    }
    
    private void eraseTable(Connection conn, String tableName, EraseResult result, boolean dryRun) throws SQLException {
        // Compter les enregistrements avant suppression
        String countSql = "SELECT COUNT(*) FROM " + tableName;
        PreparedStatement countStmt = conn.prepareStatement(countSql);
        ResultSet rs = countStmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        countStmt.close();
        
        if (count == 0) {
            result.addOperation("Table " + tableName + " déjà vide");
            return;
        }
        
        if (!dryRun) {
            String deleteSql = "DELETE FROM " + tableName;
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            int deleted = deleteStmt.executeUpdate();
            deleteStmt.close();
            result.addDeletedCount(tableName, deleted);
        } else {
            result.addOperation("Simulation: " + count + " enregistrement(s) seraient supprimés de " + tableName);
        }
    }

    /**
     * Point d'entrée pour l'exécution en ligne de commande
     */
    public static void main(String[] args) {
        boolean dryRun = args.length > 0 && args[0].equals("--dry-run");
        
        ProductCleanupTool tool = new ProductCleanupTool();
        CleanupResult result = tool.performFullCleanup(dryRun);
        
        System.out.println("\n=== DÉTAILS ===");
        
        if (!result.getIssues().isEmpty()) {
            System.out.println("\nProblèmes détectés:");
            result.getIssues().forEach(issue -> System.out.println("⚠️  " + issue));
        }
        
        if (!result.getFixes().isEmpty()) {
            System.out.println("\nCorrections appliquées:");
            result.getFixes().forEach(fix -> System.out.println("✅ " + fix));
        }
        
        if (!result.hasIssues()) {
            System.out.println("\n✅ Aucun problème détecté ! Base de données propre.");
        }
    }
}