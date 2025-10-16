package com.magsav.util;

import com.magsav.exception.BusinessException;
import com.magsav.exception.DatabaseException;

/**
 * Gestionnaire centralisé d'erreurs pour l'application MAGSAV
 * Standardise la gestion et l'affichage des erreurs
 */
public final class ErrorHandler {
    
    private ErrorHandler() {
        // Classe utilitaire
    }
    
    /**
     * Gère une exception de base de données
     */
    public static void handleDatabaseError(String operation, DatabaseException e) {
        AppLogger.error("database", "Erreur " + operation + ": " + e.getMessage(), e);
        AlertUtils.showError("Erreur de base de données", 
                           "Une erreur s'est produite lors de l'opération: " + operation, e);
    }
    
    /**
     * Gère une exception métier
     */
    public static void handleBusinessError(String operation, BusinessException e) {
        AppLogger.warn("business", "Erreur métier " + operation + ": " + e.getMessage());
        AlertUtils.showWarning("Attention", e.getMessage());
    }
    
    /**
     * Gère une exception générale non prévue
     */
    public static void handleUnexpectedError(String operation, Exception e) {
        AppLogger.error("application", "Erreur inattendue " + operation + ": " + e.getMessage(), e);
        AlertUtils.showError("Erreur inattendue", 
                           "Une erreur inattendue s'est produite: " + operation, e);
    }
    
    /**
     * Gère une erreur de validation avec message personnalisé
     */
    public static void handleValidationError(String message) {
        AppLogger.info("validation", "Erreur de validation: " + message);
        AlertUtils.showWarning("Validation", message);
    }
    
    /**
     * Affiche un message de fonctionnalité non implémentée
     */
    public static void handleNotImplemented(String feature) {
        AppLogger.info("feature", "Fonctionnalité non implémentée: " + feature);
        AlertUtils.showNotImplemented(feature);
    }
    
    /**
     * Gère les erreurs de façon silencieuse (log seulement)
     */
    public static void logError(String operation, Exception e) {
        AppLogger.error("silent", "Erreur silencieuse " + operation + ": " + e.getMessage(), e);
    }
    
    /**
     * Gère les erreurs de façon silencieuse avec message simple
     */
    public static void logError(String message) {
        AppLogger.error("silent", message);
    }
    
    /**
     * Gère une erreur avec confirmation utilisateur pour continuer
     */
    public static boolean handleErrorWithConfirmation(String operation, Exception e, String continueMessage) {
        AppLogger.error("confirmation", "Erreur avec confirmation " + operation + ": " + e.getMessage(), e);
        
        AlertUtils.showError("Erreur", 
                           "Une erreur s'est produite lors de l'opération: " + operation, e);
        
        return AlertUtils.showConfirmation("Continuer ?", continueMessage);
    }
    
    /**
     * Wrapper pour exécuter du code avec gestion d'erreur automatique
     */
    @FunctionalInterface
    public interface RiskyOperation {
        void execute() throws Exception;
    }
    
    /**
     * Wrapper pour exécuter du code avec gestion d'erreur automatique et valeur de retour
     */
    @FunctionalInterface
    public interface RiskyOperationWithReturn<T> {
        T execute() throws Exception;
    }
    
    /**
     * Exécute une opération avec gestion d'erreur automatique
     */
    public static void executeWithHandling(String operationName, RiskyOperation operation) {
        try {
            operation.execute();
        } catch (DatabaseException e) {
            handleDatabaseError(operationName, e);
        } catch (BusinessException e) {
            handleBusinessError(operationName, e);
        } catch (Exception e) {
            handleUnexpectedError(operationName, e);
        }
    }
    
    /**
     * Exécute une opération avec gestion d'erreur automatique et valeur de retour
     */
    public static <T> T executeWithHandling(String operationName, RiskyOperationWithReturn<T> operation, T defaultValue) {
        try {
            return operation.execute();
        } catch (DatabaseException e) {
            handleDatabaseError(operationName, e);
            return defaultValue;
        } catch (BusinessException e) {
            handleBusinessError(operationName, e);
            return defaultValue;
        } catch (Exception e) {
            handleUnexpectedError(operationName, e);
            return defaultValue;
        }
    }
    
    /**
     * Version silencieuse qui ne fait que logger
     */
    public static <T> T executeWithSilentHandling(String operationName, RiskyOperationWithReturn<T> operation, T defaultValue) {
        try {
            return operation.execute();
        } catch (Exception e) {
            logError(operationName, e);
            return defaultValue;
        }
    }
}