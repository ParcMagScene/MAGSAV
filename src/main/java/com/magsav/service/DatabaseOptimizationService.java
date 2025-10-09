package com.magsav.service;

import com.magsav.db.DB;
import com.magsav.util.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Service d'optimisation de la base de données
 * Gère les index et l'optimisation des performances
 */
public final class DatabaseOptimizationService {
    
    /**
     * Index recommandés pour optimiser les requêtes fréquentes
     */
    private static final List<IndexDefinition> RECOMMENDED_INDEXES = List.of(
        // Index pour produits
        new IndexDefinition("idx_produits_uid", "produits", "uid", "Recherche par UID"),
        new IndexDefinition("idx_produits_sn", "produits", "sn", "Recherche par numéro de série"),
        new IndexDefinition("idx_produits_fabricant", "produits", "UPPER(fabricant)", "Recherche par fabricant"),
        new IndexDefinition("idx_produits_category", "produits", "category", "Filtrage par catégorie"),
        new IndexDefinition("idx_produits_situation", "produits", "situation", "Filtrage par situation"),
        
        // Index pour interventions
        new IndexDefinition("idx_interventions_product_id", "interventions", "product_id", "Historique des interventions par produit"),
        new IndexDefinition("idx_interventions_statut", "interventions", "statut", "Filtrage par statut"),
        new IndexDefinition("idx_interventions_date_entree", "interventions", "date_entree", "Tri par date d'entrée"),
        new IndexDefinition("idx_interventions_date_sortie", "interventions", "date_sortie", "Tri par date de sortie"),
        
        // Index pour sociétés
        new IndexDefinition("idx_societes_type_nom", "societes", "type, UPPER(nom)", "Recherche par type et nom"),
        new IndexDefinition("idx_societes_nom", "societes", "UPPER(nom)", "Recherche par nom")
    );
    
    /**
     * Applique tous les index recommandés
     */
    public static OptimizationResult applyRecommendedIndexes() {
        AppLogger.info("database", "DatabaseOptimizationService: Début de l'optimisation des index");
        
        List<String> createdIndexes = new ArrayList<>();
        List<String> skippedIndexes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            for (IndexDefinition index : RECOMMENDED_INDEXES) {
                try {
                    if (indexExists(conn, index.name())) {
                        skippedIndexes.add(index.name() + " (existe déjà)");
                        AppLogger.debug("Index {} existe déjà", index.name());
                    } else {
                        createIndex(conn, index);
                        createdIndexes.add(index.name());
                        AppLogger.info("database", "Index créé: " + index.name());
                    }
                } catch (SQLException e) {
                    String errorMsg = "Erreur création index " + index.name() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    AppLogger.error("database", errorMsg);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Erreur connexion base de données: " + e.getMessage();
            errors.add(errorMsg);
            AppLogger.error("database", errorMsg);
        }
        
        OptimizationResult result = new OptimizationResult(createdIndexes, skippedIndexes, errors);
        AppLogger.info("database", "Optimisation terminée - Créés: {}, Ignorés: {}, Erreurs: {}", 
                      result.createdIndexes().size(), result.skippedIndexes().size(), result.errors().size());
        
        return result;
    }
    
    /**
     * Vérifie si un index existe déjà
     */
    private static boolean indexExists(Connection conn, String indexName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='index' AND name=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Crée un index
     */
    private static void createIndex(Connection conn, IndexDefinition index) throws SQLException {
        String sql = String.format("CREATE INDEX %s ON %s (%s)", 
                                   index.name(), index.table(), index.columns());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
    
    /**
     * Analyse les performances des requêtes
     */
    public static QueryAnalysisResult analyzeQueryPerformance() {
        AppLogger.info("database", "DatabaseOptimizationService: Analyse des performances");
        
        List<QueryPerformance> results = new ArrayList<>();
        
        try (Connection conn = DB.getConnection()) {
            // Test des requêtes principales
            results.add(analyzeQuery(conn, "Tous les produits", 
                "SELECT id, nom, fabricant, uid, situation FROM produits"));
            
            results.add(analyzeQuery(conn, "Produits par fabricant", 
                "SELECT id, nom FROM produits WHERE UPPER(fabricant) = UPPER('Dell')"));
            
            results.add(analyzeQuery(conn, "Interventions par produit", 
                "SELECT id, statut, date_entree FROM interventions WHERE product_id = 1"));
            
            results.add(analyzeQuery(conn, "Fabricants distincts", 
                "SELECT DISTINCT fabricant FROM produits WHERE COALESCE(TRIM(fabricant),'') <> ''"));
                
        } catch (SQLException e) {
            AppLogger.error("database", "Erreur analyse performances: " + e.getMessage());
        }
        
        return new QueryAnalysisResult(results);
    }
    
    /**
     * Analyse une requête spécifique
     */
    private static QueryPerformance analyzeQuery(Connection conn, String name, String sql) {
        try {
            long startTime = System.nanoTime();
            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }
                
                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                
                return new QueryPerformance(name, sql, durationMs, rowCount, true, null);
            }
        } catch (SQLException e) {
            return new QueryPerformance(name, sql, -1, 0, false, e.getMessage());
        }
    }
    
    /**
     * Optimise la base de données (VACUUM, ANALYZE)
     */
    public static DatabaseMaintenanceResult performMaintenance() {
        AppLogger.info("database", "DatabaseOptimizationService: Maintenance de la base");
        
        List<String> operations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = DB.getConnection()) {
            // VACUUM pour défragmenter
            try (PreparedStatement ps = conn.prepareStatement("VACUUM")) {
                ps.execute();
                operations.add("VACUUM exécuté");
                AppLogger.info("database", "VACUUM terminé");
            } catch (SQLException e) {
                errors.add("Erreur VACUUM: " + e.getMessage());
            }
            
            // ANALYZE pour mettre à jour les statistiques
            try (PreparedStatement ps = conn.prepareStatement("ANALYZE")) {
                ps.execute();
                operations.add("ANALYZE exécuté");
                AppLogger.info("database", "ANALYZE terminé");
            } catch (SQLException e) {
                errors.add("Erreur ANALYZE: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            errors.add("Erreur connexion: " + e.getMessage());
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        DatabaseMaintenanceResult result = new DatabaseMaintenanceResult(operations, errors, duration);
        AppLogger.info("database", "Maintenance terminée en {}ms - Opérations: {}, Erreurs: {}", 
                      duration, operations.size(), errors.size());
        
        return result;
    }
    
    /**
     * Définition d'un index
     */
    public record IndexDefinition(
        String name,
        String table,
        String columns,
        String description
    ) {}
    
    /**
     * Résultat de l'optimisation
     */
    public record OptimizationResult(
        List<String> createdIndexes,
        List<String> skippedIndexes,
        List<String> errors
    ) {}
    
    /**
     * Performance d'une requête
     */
    public record QueryPerformance(
        String name,
        String sql,
        long durationMs,
        int rowCount,
        boolean success,
        String error
    ) {}
    
    /**
     * Résultat de l'analyse des requêtes
     */
    public record QueryAnalysisResult(
        List<QueryPerformance> queryResults
    ) {}
    
    /**
     * Résultat de la maintenance
     */
    public record DatabaseMaintenanceResult(
        List<String> operations,
        List<String> errors,
        long durationMs
    ) {}

    private DatabaseOptimizationService() {}
}