package com.magsav.config;

/**
 * Configuration pour l'intégration avec les services Google
 */
public class GoogleServicesConfig {
    
    // URLs des API Google
    public static final String GOOGLE_OAUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_CALENDAR_API_URL = "https://www.googleapis.com/calendar/v3";
    public static final String GOOGLE_GMAIL_API_URL = "https://gmail.googleapis.com/gmail/v1";
    public static final String GOOGLE_CONTACTS_API_URL = "https://people.googleapis.com/v1";
    public static final String GOOGLE_DRIVE_API_URL = "https://www.googleapis.com/drive/v3";
    
    // Scopes par service
    public static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    public static final String GMAIL_SCOPE_MODIFY = "https://www.googleapis.com/auth/gmail.modify";
    public static final String GMAIL_SCOPE_READONLY = "https://www.googleapis.com/auth/gmail.readonly";
    public static final String CONTACTS_SCOPE = "https://www.googleapis.com/auth/contacts";
    public static final String DRIVE_SCOPE_FILE = "https://www.googleapis.com/auth/drive.file";
    
    // Configuration OAuth
    public static final String REDIRECT_URI = "http://localhost:8080/oauth/callback";
    public static final String CLIENT_ID_KEY = "google.client.id";
    public static final String CLIENT_SECRET_KEY = "google.client.secret";
    
    // Types de synchronisation
    public enum SyncType {
        MANUAL("Manuel"),
        AUTO("Automatique"),
        SCHEDULED("Programmé");
        
        private final String displayName;
        
        SyncType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Status de synchronisation
    public enum SyncStatus {
        SUCCESS("Succès"),
        ERROR("Erreur"),
        PARTIAL("Partiel"),
        IN_PROGRESS("En cours");
        
        private final String displayName;
        
        SyncStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Services Google disponibles
    public enum GoogleService {
        CALENDAR("Google Calendar", "Synchronisation des planifications avec Google Calendar"),
        GMAIL("Gmail", "Envoi et réception d'emails via Gmail"),
        CONTACTS("Google Contacts", "Synchronisation des contacts clients et techniciens"),
        DRIVE("Google Drive", "Stockage des documents dans Google Drive");
        
        private final String displayName;
        private final String description;
        
        GoogleService(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Configuration par défaut
    public static final int DEFAULT_SYNC_FREQUENCY_MINUTES = 15;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long TOKEN_REFRESH_BUFFER_MINUTES = 5; // Rafraîchir le token 5 min avant expiration
    
    // Formats de date pour les API Google
    public static final String GOOGLE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String GOOGLE_DATE_FORMAT = "yyyy-MM-dd";
    
    // Limites des API
    public static final int CALENDAR_EVENTS_MAX_RESULTS = 250;
    public static final int GMAIL_MESSAGES_MAX_RESULTS = 100;
    public static final int CONTACTS_MAX_RESULTS = 100;
    
    private GoogleServicesConfig() {
        // Classe utilitaire - ne pas instancier
    }
}