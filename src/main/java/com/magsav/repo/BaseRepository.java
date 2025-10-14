package com.magsav.repo;

import com.magsav.cache.CacheManager;
import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe de base pour tous les repositories
 * Fournit des méthodes communes avec gestion d'erreurs et logging automatique
 */
public abstract class BaseRepository {
    
    /**
     * Exécute une opération sur la base de données avec gestion automatique des ressources,
     * logging des performances et gestion d'erreurs standardisée.
     * 
     * @param operation Nom de l'opération pour le logging
     * @param table Nom de la table concernée
     * @param function Fonction à exécuter avec la connexion
     * @return Résultat de l'opération
     * @throws DatabaseException Si une erreur SQL survient
     */
    protected <T> T executeWithMetrics(String operation, String table, 
                                     ConnectionFunction<T> function) {
        long startTime = System.currentTimeMillis();
        try (Connection conn = DB.getConnection()) {
            T result = function.apply(conn);
            long duration = System.currentTimeMillis() - startTime;
            AppLogger.logDbPerformance(operation + " on " + table, duration);
            return result;
        } catch (SQLException e) {
            AppLogger.logDbError(operation, table, e);
            throw new DatabaseException(operation + " failed on " + table, e);
        }
    }
    
    /**
     * Version simplifiée pour les opérations qui ne retournent rien
     */
    protected void executeVoidWithMetrics(String operation, String table, 
                                        VoidConnectionFunction function) {
        long startTime = System.currentTimeMillis();
        try (Connection conn = DB.getConnection()) {
            function.apply(conn);
            long duration = System.currentTimeMillis() - startTime;
            AppLogger.logDbPerformance(operation + " on " + table, duration);
        } catch (SQLException e) {
            AppLogger.logDbError(operation, table, e);
            throw new DatabaseException(operation + " failed on " + table, e);
        }
    }
    
    /**
     * Interface fonctionnelle pour les opérations avec connexion retournant un résultat
     */
    @FunctionalInterface
    protected interface ConnectionFunction<T> {
        T apply(Connection conn) throws SQLException;
    }
    
    /**
     * Interface fonctionnelle pour les opérations avec connexion sans retour
     */
    @FunctionalInterface
    protected interface VoidConnectionFunction {
        void apply(Connection conn) throws SQLException;
    }
    
    /**
     * Utilitaire pour logger les requêtes SQL en mode debug
     */
    protected void logSql(String operation, String sql, Object... params) {
        AppLogger.logSql(operation, sql, params);
    }
    
    // === MÉTHODES OPTIMISÉES AVEC CACHE ===
    
    protected static final CacheManager cache = CacheManager.getInstance();
    
    /**
     * Exécute une requête SELECT avec cache automatique
     */
    protected <T> Optional<T> findWithCache(String cacheKey, String sql, 
                                          ResultSetMapper<T> mapper, Object... parameters) {
        return cache.get(cacheKey, () -> {
            return executeWithMetrics("SELECT", extractTableName(sql), conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameters(stmt, parameters);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(mapper.map(rs));
                        }
                    }
                }
                return Optional.<T>empty();
            });
        });
    }
    
    /**
     * Exécute une requête SELECT multiple avec cache automatique
     */
    protected <T> List<T> findAllWithCache(String cacheKey, String sql, 
                                         ResultSetMapper<T> mapper, Object... parameters) {
        return cache.get(cacheKey, () -> {
            return executeWithMetrics("SELECT_ALL", extractTableName(sql), conn -> {
                List<T> results = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameters(stmt, parameters);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            results.add(mapper.map(rs));
                        }
                    }
                }
                return results;
            });
        }, 2); // TTL de 2 minutes pour les listes
    }
    
    /**
     * Invalide le cache basé sur un préfixe
     */
    protected void invalidateCache(String prefix) {
        cache.invalidatePrefix(prefix);
    }
    
    /**
     * Définit les paramètres d'un PreparedStatement automatiquement
     */
    private void setParameters(PreparedStatement stmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            if (param == null) {
                stmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(i + 1, (Long) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) param);
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
    
    /**
     * Extrait le nom de table d'une requête SQL (approximatif)
     */
    private String extractTableName(String sql) {
        // Simple extraction pour les métriques
        String[] words = sql.toUpperCase().split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("FROM") || words[i].equals("UPDATE") || 
                words[i].equals("INTO") || words[i].equals("DELETE")) {
                return words[i + 1].replaceAll("[^A-Za-z0-9_]", "");
            }
        }
        return "unknown";
    }
    
    /**
     * Interface pour mapper les ResultSet
     */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
    
    /**
     * Fonctions utilitaires
     */
    protected static String nvl(String value, String defaultValue) {
        return value != null ? value.trim() : defaultValue;
    }
    
    protected static String nvl(String value) {
        return nvl(value, "");
    }
}