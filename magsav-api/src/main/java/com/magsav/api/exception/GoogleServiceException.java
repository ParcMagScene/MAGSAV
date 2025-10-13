package com.magsav.api.exception;

/**
 * Exception levée lors d'erreurs avec les services Google
 */
public class GoogleServiceException extends RuntimeException {
    
    private final String serviceName;
    
    public GoogleServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
    
    public GoogleServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}