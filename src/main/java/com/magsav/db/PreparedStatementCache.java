package com.magsav.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire de PreparedStatement réutilisables pour optimiser les performances
 */
public class PreparedStatementCache {
    private static final PreparedStatementCache INSTANCE = new PreparedStatementCache();
    
    private final ConcurrentHashMap<String, PreparedStatementEntry> cache = new ConcurrentHashMap<>();
    
    private PreparedStatementCache() {}
    
    public static PreparedStatementCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Récupère un PreparedStatement depuis le cache ou le crée
     */
    public PreparedStatement getStatement(Connection conn, String sql) throws SQLException {
        String key = generateKey(conn, sql);
        
        PreparedStatementEntry entry = cache.get(key);
        if (entry != null && entry.isValid()) {
            return entry.getStatement();
        }
        
        // Créer un nouveau PreparedStatement
        PreparedStatement stmt = conn.prepareStatement(sql);
        cache.put(key, new PreparedStatementEntry(stmt, conn));
        
        return stmt;
    }
    
    /**
     * Récupère un PreparedStatement avec auto-generated keys
     */
    public PreparedStatement getStatementWithGeneratedKeys(Connection conn, String sql) throws SQLException {
        String key = generateKey(conn, sql + "_WITH_KEYS");
        
        PreparedStatementEntry entry = cache.get(key);
        if (entry != null && entry.isValid()) {
            return entry.getStatement();
        }
        
        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        cache.put(key, new PreparedStatementEntry(stmt, conn));
        
        return stmt;
    }
    
    private String generateKey(Connection conn, String sql) {
        return conn.hashCode() + ":" + sql.hashCode();
    }
    
    /**
     * Nettoie le cache des statements invalides
     */
    public void cleanup() {
        cache.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
    
    /**
     * Vide complètement le cache
     */
    public void clear() {
        cache.values().forEach(entry -> {
            try {
                entry.getStatement().close();
            } catch (SQLException e) {
                // Ignorer les erreurs de fermeture
            }
        });
        cache.clear();
    }
    
    /**
     * Statistiques du cache
     */
    public CacheStats getStats() {
        int total = cache.size();
        long invalid = cache.values().stream().mapToLong(entry -> entry.isValid() ? 0 : 1).sum();
        return new CacheStats(total, (int) invalid, total - (int) invalid);
    }
    
    /**
     * Entrée de cache pour PreparedStatement
     */
    private static class PreparedStatementEntry {
        private final PreparedStatement statement;
        private final Connection connection;
        
        public PreparedStatementEntry(PreparedStatement statement, Connection connection) {
            this.statement = statement;
            this.connection = connection;
        }
        
        public PreparedStatement getStatement() {
            return statement;
        }
        
        public boolean isValid() {
            try {
                return statement != null && !statement.isClosed() && 
                       connection != null && !connection.isClosed();
            } catch (SQLException e) {
                return false;
            }
        }
    }
    
    /**
     * Statistiques du cache de PreparedStatement
     */
    public static class CacheStats {
        private final int totalStatements;
        private final int invalidStatements;
        private final int validStatements;
        
        public CacheStats(int totalStatements, int invalidStatements, int validStatements) {
            this.totalStatements = totalStatements;
            this.invalidStatements = invalidStatements;
            this.validStatements = validStatements;
        }
        
        public int getTotalStatements() { return totalStatements; }
        public int getInvalidStatements() { return invalidStatements; }
        public int getValidStatements() { return validStatements; }
        
        @Override
        public String toString() {
            return String.format("PreparedStatements: %d total (%d valides, %d invalides)", 
                totalStatements, validStatements, invalidStatements);
        }
    }
}