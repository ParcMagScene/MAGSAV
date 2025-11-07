package com.magscene.magsav.desktop.planning;

/**
 * Résultat d'une synchronisation Google Calendar
 */
public class SyncResultItem {
    
    private final String accountName;
    private final boolean success;
    private final String message;
    private final int createdOrUpdated;
    private final int imported;
    private final int errors;
    
    public SyncResultItem(String accountName, boolean success, String message, 
                         int createdOrUpdated, int imported, int errors) {
        this.accountName = accountName;
        this.success = success;
        this.message = message;
        this.createdOrUpdated = createdOrUpdated;
        this.imported = imported;
        this.errors = errors;
    }
    
    // === GETTERS ===
    
    public String getAccountName() {
        return accountName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getCreatedOrUpdated() {
        return createdOrUpdated;
    }
    
    public int getImported() {
        return imported;
    }
    
    public int getErrors() {
        return errors;
    }
    
    @Override
    public String toString() {
        return String.format("SyncResult{account='%s', success=%s, created/updated=%d, imported=%d, errors=%d}", 
                           accountName, success, createdOrUpdated, imported, errors);
    }
}