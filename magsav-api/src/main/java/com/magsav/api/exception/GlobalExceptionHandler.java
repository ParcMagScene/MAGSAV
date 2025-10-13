package com.magsav.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global d'exceptions pour l'API MAGSAV 1.3
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestion des erreurs de ressource non trouvée
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.NOT_FOUND.value());
        errorDetails.put("error", "Resource Not Found");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Validation Error");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        if (ex.getErrors() != null) {
            errorDetails.put("validation_errors", ex.getErrors());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    /**
     * Gestion des erreurs de service Google
     */
    @ExceptionHandler(GoogleServiceException.class)
    public ResponseEntity<Map<String, Object>> handleGoogleServiceException(
            GoogleServiceException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorDetails.put("error", "Google Service Error");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        errorDetails.put("service", ex.getServiceName());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorDetails);
    }

    /**
     * Gestion des erreurs d'accès non autorisé
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("error", "Unauthorized Access");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    /**
     * Gestion générique des exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", ex.getMessage() != null ? ex.getMessage() : "Une erreur inattendue s'est produite");
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        // En mode développement, inclure la stack trace
        if (isDevelopmentMode()) {
            errorDetails.put("debug_info", ex.getClass().getSimpleName());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    /**
     * Gestion des erreurs d'argument invalide
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Invalid Argument");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    /**
     * Gestion des erreurs de base de données
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseException(
            DatabaseException ex, WebRequest request) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Database Error");
        errorDetails.put("message", "Erreur lors de l'accès à la base de données");
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        // En mode développement, inclure le message détaillé
        if (isDevelopmentMode()) {
            errorDetails.put("debug_message", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    /**
     * Vérifie si l'application est en mode développement
     */
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local");
    }
}