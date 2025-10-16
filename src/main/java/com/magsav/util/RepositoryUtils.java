package com.magsav.util;

import com.magsav.exception.DatabaseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utilitaires communs pour les opérations de base de données
 * Élimine la duplication dans tous les repositories
 */
public final class RepositoryUtils {
    
    private RepositoryUtils() {
        // Classe utilitaire
    }
    
    /**
     * Exécute une requête SELECT qui retourne un Optional
     */
    public static <T> Optional<T> findOne(PreparedStatement ps, Function<ResultSet, T> mapper, String operation) {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapper.apply(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException(operation + " failed", e);
        }
    }
    
    /**
     * Exécute une requête SELECT qui retourne une liste
     */
    public static <T> List<T> findAll(PreparedStatement ps, Function<ResultSet, T> mapper, String operation) {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.apply(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException(operation + " failed", e);
        }
    }
    
    /**
     * Exécute une requête SELECT qui retourne un seul résultat obligatoire
     */
    public static <T> T findOneRequired(PreparedStatement ps, Function<ResultSet, T> mapper, String operation) {
        Optional<T> result = findOne(ps, mapper, operation);
        return result.orElseThrow(() -> new DatabaseException(operation + " - Aucun résultat trouvé"));
    }
    
    /**
     * Exécute une requête COUNT
     */
    public static int count(PreparedStatement ps, String operation) {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException(operation + " failed", e);
        }
    }
    
    /**
     * Exécute une requête EXISTS
     */
    public static boolean exists(PreparedStatement ps, String operation) {
        return count(ps, operation) > 0;
    }
    
    /**
     * Définit un paramètre Long qui peut être null
     */
    public static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(index, value);
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }
    
    /**
     * Définit un paramètre Integer qui peut être null
     */
    public static void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }
    
    /**
     * Définit un paramètre String qui peut être null
     */
    public static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.trim().isEmpty()) {
            ps.setString(index, value.trim());
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }
    
    /**
     * Récupère un Long qui peut être null
     */
    public static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Récupère un Integer qui peut être null
     */
    public static Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Récupère un String qui peut être null (et trim les espaces)
     */
    public static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
    
    /**
     * Récupère un String avec une valeur par défaut
     */
    public static String getStringWithDefault(ResultSet rs, String column, String defaultValue) throws SQLException {
        String value = getStringOrNull(rs, column);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Wrapper pour les opérations de base de données avec gestion d'erreur standardisée
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute() throws SQLException;
    }
    
    /**
     * Exécute une opération de base de données avec gestion d'erreur centralisée
     */
    public static <T> T executeWithErrorHandling(DatabaseOperation<T> operation, String operationName) {
        try {
            return operation.execute();
        } catch (SQLException e) {
            throw new DatabaseException(operationName + " failed", e);
        }
    }
    
    /**
     * Version void de executeWithErrorHandling
     */
    public static void executeWithErrorHandling(DatabaseOperationVoid operation, String operationName) {
        try {
            operation.execute();
        } catch (SQLException e) {
            throw new DatabaseException(operationName + " failed", e);
        }
    }
    
    @FunctionalInterface
    public interface DatabaseOperationVoid {
        void execute() throws SQLException;
    }
}