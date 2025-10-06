package com.magsav.exception;

/**
 * Exception lancée lors d'erreurs de base de données
 */
public class DatabaseException extends BusinessException {
    
    public DatabaseException(String operation, Throwable cause) {
        super("Erreur de base de données lors de l'opération: " + operation, cause);
    }
    
    public DatabaseException(String operation, String table, Throwable cause) {
        super("Erreur de base de données lors de '" + operation + "' sur la table '" + table + "'", cause);
    }
    
    public DatabaseException(String message) {
        super(message);
    }
}