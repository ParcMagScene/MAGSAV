package com.magsav.ui.components;

import com.magsav.util.AppLogger;
import javafx.application.Platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Gestionnaire global des erreurs de l'application
 * Centralise la gestion, le logging et l'affichage des erreurs
 */
public final class ErrorManager {
    
    private static final ConcurrentLinkedQueue<ErrorInfo> recentErrors = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECENT_ERRORS = 50;
    private static Consumer<ErrorInfo> errorHandler;
    private static boolean debugMode = false;
    
    private ErrorManager() {}
    
    /**
     * Information sur une erreur
     */
    public static class ErrorInfo {
        private final LocalDateTime timestamp;
        private final String operation;
        private final Throwable exception;
        private final ErrorLevel level;
        private final String context;
        
        public ErrorInfo(String operation, Throwable exception, ErrorLevel level, String context) {
            this.timestamp = LocalDateTime.now();
            this.operation = operation;
            this.exception = exception;
            this.level = level;
            this.context = context;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getOperation() { return operation; }
        public Throwable getException() { return exception; }
        public ErrorLevel getLevel() { return level; }
        public String getContext() { return context; }
        
        public String getFormattedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("] ");
            sb.append(operation);
            if (context != null && !context.isEmpty()) {
                sb.append(" (").append(context).append(")");
            }
            sb.append(": ").append(exception.getMessage());
            return sb.toString();
        }
        
        public String getStackTrace() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            return sw.toString();
        }
    }
    
    /**
     * Niveaux d'erreur
     */
    public enum ErrorLevel {
        DEBUG,      // Informations de débogage
        INFO,       // Informations générales
        WARNING,    // Avertissements
        ERROR,      // Erreurs non critiques
        CRITICAL    // Erreurs critiques
    }
    
    /**
     * Définit le gestionnaire d'erreurs personnalisé
     */
    public static void setErrorHandler(Consumer<ErrorInfo> handler) {
        errorHandler = handler;
    }
    
    /**
     * Active/désactive le mode debug
     */
    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }
    
    /**
     * Rapporte une erreur critique
     */
    public static void reportCritical(String operation, Throwable exception) {
        reportError(operation, exception, ErrorLevel.CRITICAL, null);
    }
    
    /**
     * Rapporte une erreur critique avec contexte
     */
    public static void reportCritical(String operation, Throwable exception, String context) {
        reportError(operation, exception, ErrorLevel.CRITICAL, context);
    }
    
    /**
     * Rapporte une erreur standard
     */
    public static void reportError(String operation, Throwable exception) {
        reportError(operation, exception, ErrorLevel.ERROR, null);
    }
    
    /**
     * Rapporte une erreur standard avec contexte
     */
    public static void reportError(String operation, Throwable exception, String context) {
        reportError(operation, exception, ErrorLevel.ERROR, context);
    }
    
    /**
     * Rapporte un avertissement
     */
    public static void reportWarning(String operation, Exception exception) {
        reportError(operation, exception, ErrorLevel.WARNING, null);
    }
    
    /**
     * Rapporte un avertissement avec contexte
     */
    public static void reportWarning(String operation, Exception exception, String context) {
        reportError(operation, exception, ErrorLevel.WARNING, context);
    }
    
    /**
     * Méthode principale de rapport d'erreur
     */
    public static void reportError(String operation, Throwable exception, ErrorLevel level, String context) {
        ErrorInfo errorInfo = new ErrorInfo(operation, exception, level, context);
        
        // Ajouter aux erreurs récentes
        recentErrors.offer(errorInfo);
        if (recentErrors.size() > MAX_RECENT_ERRORS) {
            recentErrors.poll();
        }
        
        // Logger l'erreur
        logError(errorInfo);
        
        // Afficher à l'utilisateur selon le niveau
        if (level.ordinal() >= ErrorLevel.ERROR.ordinal()) {
            showErrorToUser(errorInfo);
        }
        
        // Appeler le gestionnaire personnalisé
        if (errorHandler != null) {
            try {
                errorHandler.accept(errorInfo);
            } catch (Exception e) {
                AppLogger.error("error-manager", "Erreur dans le gestionnaire d'erreurs personnalisé", e);
            }
        }
    }
    
    /**
     * Logue l'erreur avec AppLogger
     */
    private static void logError(ErrorInfo errorInfo) {
        String message = errorInfo.getFormattedMessage();
        
        switch (errorInfo.getLevel()) {
            case DEBUG:
                if (debugMode) {
                    AppLogger.debug("error", message, errorInfo.getException());
                }
                break;
            case INFO:
                AppLogger.info("error", message);
                break;
            case WARNING:
                AppLogger.warn("error", message, errorInfo.getException());
                break;
            case ERROR:
                AppLogger.error("error", message, errorInfo.getException());
                break;
            case CRITICAL:
                AppLogger.error("critical", message, errorInfo.getException());
                break;
        }
    }
    
    /**
     * Affiche l'erreur à l'utilisateur
     */
    private static void showErrorToUser(ErrorInfo errorInfo) {
        Platform.runLater(() -> {
            String title = getErrorTitle(errorInfo.getLevel());
            String message = formatUserMessage(errorInfo);
            
            switch (errorInfo.getLevel()) {
                case WARNING:
                    NotificationManager.showWarning(message);
                    break;
                case ERROR:
                    NotificationManager.showError(message);
                    AlertManager.showError(title, message);
                    break;
                case CRITICAL:
                    NotificationManager.showError(message);
                    AlertManager.showError(title, formatCriticalMessage(errorInfo));
                    break;
                default:
                    break;
            }
        });
    }
    
    /**
     * Formate le message pour l'utilisateur
     */
    private static String formatUserMessage(ErrorInfo errorInfo) {
        String message = errorInfo.getOperation();
        if (errorInfo.getException().getMessage() != null) {
            message += " : " + errorInfo.getException().getMessage();
        }
        return message;
    }
    
    /**
     * Formate un message d'erreur critique
     */
    private static String formatCriticalMessage(ErrorInfo errorInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Une erreur critique s'est produite :\n\n");
        sb.append("Opération : ").append(errorInfo.getOperation()).append("\n");
        if (errorInfo.getContext() != null) {
            sb.append("Contexte : ").append(errorInfo.getContext()).append("\n");
        }
        sb.append("Erreur : ").append(errorInfo.getException().getMessage()).append("\n\n");
        sb.append("Veuillez redémarrer l'application si le problème persiste.\n");
        
        if (debugMode) {
            sb.append("\nDétails techniques :\n").append(errorInfo.getStackTrace());
        }
        
        return sb.toString();
    }
    
    /**
     * Obtient le titre selon le niveau d'erreur
     */
    private static String getErrorTitle(ErrorLevel level) {
        switch (level) {
            case WARNING: return "Avertissement";
            case ERROR: return "Erreur";
            case CRITICAL: return "Erreur Critique";
            default: return "Information";
        }
    }
    
    /**
     * Récupère les erreurs récentes
     */
    public static ErrorInfo[] getRecentErrors() {
        return recentErrors.toArray(new ErrorInfo[0]);
    }
    
    /**
     * Vide la liste des erreurs récentes
     */
    public static void clearRecentErrors() {
        recentErrors.clear();
    }
    
    /**
     * Gère les exceptions non capturées
     */
    public static void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            reportCritical("Exception non capturée", exception, "Thread: " + thread.getName());
        });
        
        // Pour JavaFX
        if (Platform.isImplicitExit()) {
            Platform.setImplicitExit(false);
        }
    }
    
    /**
     * Utilitaires pour MAGSAV
     */
    public static class MAGSAV {
        
        public static void databaseError(String operation, Exception ex) {
            reportError("Erreur base de données - " + operation, ex, "Database");
        }
        
        public static void fileError(String operation, Exception ex, String filePath) {
            reportError("Erreur fichier - " + operation, ex, "Fichier: " + filePath);
        }
        
        public static void importError(String operation, Exception ex, int lineNumber) {
            reportError("Erreur import - " + operation, ex, "Ligne: " + lineNumber);
        }
        
        public static void validationError(String field, Exception ex) {
            reportWarning("Erreur validation - " + field, ex);
        }
        
        public static void networkError(String operation, Exception ex) {
            reportError("Erreur réseau - " + operation, ex);
        }
        
        public static void configurationError(String setting, Exception ex) {
            reportCritical("Erreur configuration - " + setting, ex);
        }
        
        public static void serviceError(String serviceName, String operation, Exception ex) {
            reportError("Service " + serviceName + " - " + operation, ex);
        }
        
        public static void uiError(String component, Exception ex) {
            reportError("Erreur interface - " + component, ex);
        }
        
        /**
         * Wrapper pour exécuter du code avec gestion d'erreur automatique
         */
        public static void safeExecute(String operation, Runnable code) {
            try {
                code.run();
            } catch (Exception ex) {
                reportError(operation, ex);
            }
        }
        
        /**
         * Wrapper pour exécuter du code avec gestion d'erreur et contexte
         */
        public static void safeExecute(String operation, String context, Runnable code) {
            try {
                code.run();
            } catch (Exception ex) {
                reportError(operation, ex, context);
            }
        }
        
        /**
         * Wrapper pour fonction avec valeur de retour et gestion d'erreur
         */
        public static <T> T safeExecute(String operation, java.util.function.Supplier<T> supplier, T defaultValue) {
            try {
                return supplier.get();
            } catch (Exception ex) {
                reportError(operation, ex);
                return defaultValue;
            }
        }
    }
}