package com.magsav.exception;

/**
 * Exception lancée lorsqu'un UID est invalide ou malformé
 */
public class InvalidUidException extends BusinessException {
    
    public InvalidUidException(String uid) {
        super("UID invalide: '" + uid + "'. Format attendu: 3 lettres + 4 chiffres (ex: ABC1234)");
    }
    
    public InvalidUidException(String uid, String reason) {
        super("UID invalide: '" + uid + "' - " + reason);
    }
    
    public InvalidUidException(String message, Throwable cause) {
        super(message, cause);
    }
}