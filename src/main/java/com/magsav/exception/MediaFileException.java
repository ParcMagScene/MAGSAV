package com.magsav.exception;

/**
 * Exception lancée lors d'erreurs de manipulation de fichiers médias
 */
public class MediaFileException extends BusinessException {
    
    public MediaFileException(String operation, String filePath) {
        super("Erreur lors de l'opération '" + operation + "' sur le fichier: " + filePath);
    }
    
    public MediaFileException(String operation, String filePath, Throwable cause) {
        super("Erreur lors de l'opération '" + operation + "' sur le fichier: " + filePath, cause);
    }
    
    public MediaFileException(String message) {
        super(message);
    }
    
    public MediaFileException(String message, Throwable cause) {
        super(message, cause);
    }
}