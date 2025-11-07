package com.magscene.magsav.desktop.model;

/**
 * Classe pour encapsuler les résultats de synchronisation Google Calendar
 */
public class SyncResultItem {
    private final String accountName;
    private final boolean success;
    private final String message;
    
    public SyncResultItem(String accountName, boolean success, String message) {
        this.accountName = accountName;
        this.success = success;
        this.message = message;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return accountName + ": " + (success ? "Succès" : "Échec") + " - " + message;
    }
}