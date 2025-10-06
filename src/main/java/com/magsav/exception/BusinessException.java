package com.magsav.exception;

/**
 * Exception de base pour toutes les erreurs métier de l'application MAGSAV
 * 
 * Cette exception remplace l'utilisation de RuntimeException générique
 * et permet une gestion d'erreurs plus précise et typée.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(Throwable cause) {
        super(cause);
    }
}