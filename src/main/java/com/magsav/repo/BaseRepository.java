package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;

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
}