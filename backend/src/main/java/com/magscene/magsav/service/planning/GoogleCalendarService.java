package com.magscene.magsav.service.planning;

import com.magscene.magsav.model.planning.Event;
import com.magscene.magsav.model.planning.GoogleCalendarAccount;
import com.magscene.magsav.model.planning.GoogleCalendarAccount.SyncDirection;
import com.magscene.magsav.model.planning.GoogleCalendarAccount.SyncStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service de synchronisation avec Google Calendar
 * Gère l'authentification OAuth2, la synchronisation bidirectionnelle et multi-comptes
 */
@Service
public class GoogleCalendarService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    
    @Value("${google.oauth.client-id:}")
    private String clientId;
    
    @Value("${google.oauth.client-secret:}")
    private String clientSecret;
    
    @Value("${google.oauth.redirect-uri:http://localhost:8080/oauth/google/callback}")
    private String redirectUri;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<Long, SyncStatus> syncStatuses = new ConcurrentHashMap<>();
    
    // URLs Google Calendar API
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    @SuppressWarnings("unused")
    private static final String GOOGLE_CALENDAR_API_BASE = "https://www.googleapis.com/calendar/v3";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    
    // Scopes Google Calendar
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    
    public GoogleCalendarService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    // === AUTHENTIFICATION OAUTH2 ===
    
    /**
     * Génère l'URL d'authentification Google OAuth2
     */
    public String generateAuthUrl(String state) {
        String scopes = CALENDAR_SCOPE + " " + USERINFO_SCOPE;
        return GOOGLE_AUTH_URL + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scopes.replace(" ", "%20") +
                "&response_type=code" +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=" + state;
    }
    
    /**
     * Échange le code d'autorisation contre des tokens d'accès
     */
    public CompletableFuture<GoogleTokenResponse> exchangeCodeForTokens(String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> params = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", code,
                    "grant_type", "authorization_code",
                    "redirect_uri", redirectUri
                );
                
                String body = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    return new GoogleTokenResponse(
                        json.get("access_token").asText(),
                        json.has("refresh_token") ? json.get("refresh_token").asText() : null,
                        json.get("expires_in").asInt()
                    );
                } else {
                    logger.error("Erreur lors de l'échange de code: {}", response.body());
                    throw new RuntimeException("Échec de l'authentification Google");
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de l'échange de code", e);
                throw new RuntimeException("Erreur d'authentification", e);
            }
        });
    }
    
    /**
     * Actualise le token d'accès avec le refresh token
     */
    public CompletableFuture<GoogleTokenResponse> refreshAccessToken(String refreshToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> params = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "refresh_token", refreshToken,
                    "grant_type", "refresh_token"
                );
                
                String body = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    return new GoogleTokenResponse(
                        json.get("access_token").asText(),
                        refreshToken, // Garder le même refresh token
                        json.get("expires_in").asInt()
                    );
                } else {
                    logger.error("Erreur lors du refresh token: {}", response.body());
                    throw new RuntimeException("Échec du refresh token");
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors du refresh token", e);
                throw new RuntimeException("Erreur de refresh token", e);
            }
        });
    }
    
    /**
     * Récupère les informations de l'utilisateur Google
     */
    public CompletableFuture<GoogleUserInfo> getUserInfo(String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_USERINFO_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    return new GoogleUserInfo(
                        json.get("email").asText(),
                        json.has("name") ? json.get("name").asText() : json.get("email").asText()
                    );
                } else {
                    throw new RuntimeException("Impossible de récupérer les infos utilisateur");
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération des infos utilisateur", e);
                throw new RuntimeException("Erreur info utilisateur", e);
            }
        });
    }
    
    // === SYNCHRONISATION ÉVÉNEMENTS ===
    
    /**
     * Synchronise un compte Google Calendar
     */
    public CompletableFuture<SyncResult> synchronizeAccount(GoogleCalendarAccount account) {
        syncStatuses.put(account.getId(), SyncStatus.IN_PROGRESS);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Vérifier et actualiser le token si nécessaire
                if (account.isTokenExpired() && account.getRefreshToken() != null) {
                    GoogleTokenResponse newTokens = refreshAccessToken(account.getRefreshToken()).get();
                    account.setAccessToken(newTokens.getAccessToken());
                    account.setTokenExpiry(LocalDateTime.now().plusSeconds(newTokens.getExpiresIn()));
                }
                
                SyncResult result = new SyncResult();
                
                // Synchronisation selon la direction configurée
                if (account.getSyncDirection() == SyncDirection.MAGSAV_TO_GOOGLE || 
                    account.getSyncDirection() == SyncDirection.BIDIRECTIONAL) {
                    syncMagsavToGoogle(account, result);
                }
                
                if (account.getSyncDirection() == SyncDirection.GOOGLE_TO_MAGSAV || 
                    account.getSyncDirection() == SyncDirection.BIDIRECTIONAL) {
                    syncGoogleToMagsav(account, result);
                }
                
                // Mettre à jour le statut de synchronisation
                account.updateSyncStatus(SyncStatus.SUCCESS, null);
                syncStatuses.put(account.getId(), SyncStatus.SUCCESS);
                
                logger.info("Synchronisation réussie pour le compte {}: {}", 
                           account.getGoogleAccountEmail(), result);
                
                return result;
                
            } catch (Exception e) {
                logger.error("Erreur lors de la synchronisation du compte " + account.getGoogleAccountEmail(), e);
                account.updateSyncStatus(SyncStatus.FAILED, e.getMessage());
                syncStatuses.put(account.getId(), SyncStatus.FAILED);
                
                SyncResult errorResult = new SyncResult();
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });
    }
    
    /**
     * Synchronise les événements de MAGSAV vers Google Calendar
     */
    private void syncMagsavToGoogle(GoogleCalendarAccount account, SyncResult result) throws Exception {
        // Récupérer les événements MAGSAV à synchroniser
        Set<Event> eventsToSync = account.getSynchronizedEvents();
        
        for (Event event : eventsToSync) {
            try {
                // Vérifier si l'événement correspond aux spécialités à synchroniser
                if (!shouldSyncEvent(event, account)) {
                    continue;
                }
                
                // Convertir l'événement MAGSAV en format Google
                GoogleCalendarEvent googleEvent = convertToGoogleEvent(event, account);
                
                // Créer ou mettre à jour l'événement dans Google Calendar
                String googleEventId = createOrUpdateGoogleEvent(account, googleEvent);
                
                // Mettre à jour l'ID Google dans l'événement MAGSAV
                updateEventGoogleId(event, account.getId(), googleEventId);
                
                result.incrementCreatedOrUpdated();
                
            } catch (Exception e) {
                logger.warn("Erreur lors de la synchronisation de l'événement {} vers Google", event.getTitle(), e);
                result.incrementErrors();
            }
        }
    }
    
    /**
     * Synchronise les événements de Google Calendar vers MAGSAV
     */
    private void syncGoogleToMagsav(GoogleCalendarAccount account, SyncResult result) throws Exception {
        // Récupérer les événements Google Calendar
        List<GoogleCalendarEvent> googleEvents = fetchGoogleEvents(account);
        
        for (GoogleCalendarEvent googleEvent : googleEvents) {
            try {
                // Convertir l'événement Google en format MAGSAV
                @SuppressWarnings("unused")
                Event magsavEvent = convertToMagsavEvent(googleEvent, account);
                
                // Créer ou mettre à jour l'événement dans MAGSAV
                // (Cette partie nécessitera l'intégration avec les repositories)
                
                result.incrementImported();
                
            } catch (Exception e) {
                logger.warn("Erreur lors de l'import de l'événement Google {}", googleEvent.getSummary(), e);
                result.incrementErrors();
            }
        }
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Vérifie si un événement doit être synchronisé selon les paramètres du compte
     */
    private boolean shouldSyncEvent(Event event, GoogleCalendarAccount account) {
        if (!account.getSyncEnabled()) {
            return false;
        }
        
        // Vérifier les spécialités si configurées
        Set<String> accountSpecialties = account.getSyncSpecialtiesSet();
        if (!accountSpecialties.isEmpty()) {
            Set<String> eventSpecialties = event.getRequiredSpecialtiesSet();
            return eventSpecialties.stream().anyMatch(accountSpecialties::contains);
        }
        
        return true;
    }
    
    /**
     * Convertit un événement MAGSAV en événement Google Calendar
     */
    private GoogleCalendarEvent convertToGoogleEvent(Event event, GoogleCalendarAccount account) {
        GoogleCalendarEvent googleEvent = new GoogleCalendarEvent();
        
        String title = event.getTitle();
        if (account.getEventTitlePrefix() != null && !account.getEventTitlePrefix().isEmpty()) {
            title = account.getEventTitlePrefix() + " " + title;
        }
        
        googleEvent.setSummary(title);
        googleEvent.setDescription(event.getDescription());
        googleEvent.setLocation(event.getLocation());
        googleEvent.setColorId(account.getDefaultColorId());
        
        // Convertir les dates/heures
        googleEvent.setStartDateTime(event.getStartDateTime());
        googleEvent.setEndDateTime(event.getEndDateTime());
        
        return googleEvent;
    }
    
    /**
     * Convertit un événement Google Calendar en événement MAGSAV
     */
    private Event convertToMagsavEvent(GoogleCalendarEvent googleEvent, GoogleCalendarAccount account) {
        Event event = new Event();
        
        String title = googleEvent.getSummary();
        // Enlever le préfixe si présent
        if (account.getEventTitlePrefix() != null && title.startsWith(account.getEventTitlePrefix())) {
            title = title.substring(account.getEventTitlePrefix().length()).trim();
        }
        
        event.setTitle(title);
        event.setDescription(googleEvent.getDescription());
        event.setLocation(googleEvent.getLocation());
        event.setStartDateTime(googleEvent.getStartDateTime());
        event.setEndDateTime(googleEvent.getEndDateTime());
        event.setType(Event.EventType.AUTRE); // Type par défaut
        
        return event;
    }
    
    // Ces méthodes nécessiteraient l'implémentation complète de l'API Google Calendar
    // Pour l'instant, elles sont simulées
    
    private String createOrUpdateGoogleEvent(GoogleCalendarAccount account, GoogleCalendarEvent event) throws Exception {
        // TODO: Implémentation réelle de l'API Google Calendar
        return "google-event-" + UUID.randomUUID().toString();
    }
    
    private List<GoogleCalendarEvent> fetchGoogleEvents(GoogleCalendarAccount account) throws Exception {
        // TODO: Implémentation réelle de l'API Google Calendar
        return new ArrayList<>();
    }
    
    private void updateEventGoogleId(Event event, Long accountId, String googleEventId) {
        // TODO: Mettre à jour l'ID Google dans l'événement
        // Format: "idCompteGoogle:idEvenementGoogle"
        String currentIds = event.getGoogleEventIds();
        if (currentIds == null) {
            currentIds = "";
        }
        
        // Ajouter ou mettre à jour l'ID pour ce compte
        event.setGoogleEventIds(currentIds + ";" + accountId + ":" + googleEventId);
    }
    
    /**
     * Retourne le statut de synchronisation d'un compte
     */
    public SyncStatus getSyncStatus(Long accountId) {
        return syncStatuses.getOrDefault(accountId, SyncStatus.NEVER_SYNCED);
    }
    
    // === CLASSES DE DONNÉES ===
    
    public static class GoogleTokenResponse {
        private final String accessToken;
        private final String refreshToken;
        private final int expiresIn;
        
        public GoogleTokenResponse(String accessToken, String refreshToken, int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public int getExpiresIn() { return expiresIn; }
    }
    
    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        
        public GoogleUserInfo(String email, String name) {
            this.email = email;
            this.name = name;
        }
        
        public String getEmail() { return email; }
        public String getName() { return name; }
    }
    
    public static class GoogleCalendarEvent {
        private String summary;
        private String description;
        private String location;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String colorId;
        
        // Getters et setters
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public LocalDateTime getStartDateTime() { return startDateTime; }
        public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
        public LocalDateTime getEndDateTime() { return endDateTime; }
        public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
        public String getColorId() { return colorId; }
        public void setColorId(String colorId) { this.colorId = colorId; }
    }
    
    public static class SyncResult {
        private int createdOrUpdated = 0;
        private int imported = 0;
        private int errors = 0;
        private String errorMessage;
        
        public void incrementCreatedOrUpdated() { createdOrUpdated++; }
        public void incrementImported() { imported++; }
        public void incrementErrors() { errors++; }
        
        public int getCreatedOrUpdated() { return createdOrUpdated; }
        public int getImported() { return imported; }
        public int getErrors() { return errors; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        @Override
        public String toString() {
            return String.format("SyncResult{created/updated: %d, imported: %d, errors: %d}", 
                               createdOrUpdated, imported, errors);
        }
    }
}
