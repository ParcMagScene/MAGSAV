package com.magsav.exception;

/**
 * Exception lancée lorsqu'un produit n'est pas trouvé
 */
public class ProductNotFoundException extends BusinessException {
    
    public ProductNotFoundException(long productId) {
        super("Produit non trouvé avec l'ID: " + productId);
    }
    
    public ProductNotFoundException(String uid) {
        super("Produit non trouvé avec l'UID: " + uid);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}