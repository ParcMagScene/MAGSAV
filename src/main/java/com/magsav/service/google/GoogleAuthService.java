package com.magsav.service.google;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.util.AppLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Service d'authentification OAuth2 pour Google APIs
 */
public class GoogleAuthService {
    
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";
    
    private final GoogleServicesConfig config;
    private final HttpClient httpClient;
    
    public GoogleAuthService(GoogleServicesConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Génère l'URL d'autorisation OAuth2
     */
    public String getAuthorizationUrl() {
        if (!config.isConfigured()) {
            throw new IllegalStateException("Configuration Google non complète");
        }
        
        StringJoiner url = new StringJoiner("&", GOOGLE_AUTH_URL + "?", "");
        url.add("client_id=" + config.getClientId());
        url.add("redirect_uri=" + config.getRedirectUri());
        url.add("scope=" + String.join(" ", config.getScopesArray()));
        url.add("response_type=code");
        url.add("access_type=offline");
        url.add("prompt=consent");
        url.add("state=" + generateState());
        
        AppLogger.info("google", "URL d'autorisation générée");
        return url.toString();
    }
    
    /**
     * Échange le code d'autorisation contre des tokens
     */
    public boolean exchangeCodeForTokens(String authorizationCode) {
        try {
            AppLogger.info("google", "Échange du code d'autorisation contre les tokens");
            
            Map<String, String> params = new HashMap<>();
            params.put("client_id", config.getClientId());
            params.put("client_secret", config.getClientSecret());
            params.put("code", authorizationCode);
            params.put("grant_type", "authorization_code");
            params.put("redirect_uri", config.getRedirectUri());
            
            String response = makeTokenRequest(params);
            return parseTokenResponse(response);
            
        } catch (Exception e) {
            AppLogger.error("Erreur échange code autorisation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rafraîchit les tokens d'accès
     */
    public boolean refreshTokens() {
        if (config.getRefreshToken().isEmpty()) {
            AppLogger.warn("google", "Pas de refresh token disponible");
            return false;
        }
        
        try {
            AppLogger.info("google", "Rafraîchissement des tokens");
            
            Map<String, String> params = new HashMap<>();
            params.put("client_id", config.getClientId());
            params.put("client_secret", config.getClientSecret());
            params.put("refresh_token", config.getRefreshToken());
            params.put("grant_type", "refresh_token");
            
            String response = makeTokenRequest(params);
            return parseTokenResponse(response);
            
        } catch (Exception e) {
            AppLogger.error("Erreur rafraîchissement tokens: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Révoque les tokens (déconnexion)
     */
    public boolean revokeTokens() {
        if (config.getAccessToken().isEmpty()) {
            return true; // Déjà déconnecté
        }
        
        try {
            AppLogger.info("google", "Révocation des tokens");
            
            String url = GOOGLE_REVOKE_URL + "?token=" + config.getAccessToken();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Nettoyer les tokens locaux
                config.setAccessToken("");
                config.setRefreshToken("");
                config.setTokenExpiresIn(0);
                
                AppLogger.info("google", "Tokens révoqués avec succès");
                return true;
            } else {
                AppLogger.warn("google", "Erreur révocation tokens: " + response.statusCode());
                return false;
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur révocation tokens: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si l'authentification est valide
     */
    public boolean isAuthenticated() {
        if (config.getAccessToken().isEmpty()) {
            return false;
        }
        
        // Vérifier si le token doit être rafraîchi
        if (config.needsTokenRefresh()) {
            AppLogger.info("google", "Token proche de l'expiration, tentative de rafraîchissement");
            return refreshTokens();
        }
        
        return true;
    }
    
    /**
     * Obtient un token d'accès valide (rafraîchit si nécessaire)
     */
    public String getValidAccessToken() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Authentification Google requise");
        }
        
        return config.getAccessToken();
    }
    
    // Méthodes privées
    
    private String makeTokenRequest(Map<String, String> params) throws IOException, InterruptedException {
        String formData = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Erreur API Google: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }
    
    private boolean parseTokenResponse(String response) {
        try {
            // Simulation parsing JSON (remplacer par vraie lib JSON)
            AppLogger.info("google", "Parsing réponse tokens: " + response.substring(0, Math.min(100, response.length())));
            
            // TODO: Parser la vraie réponse JSON de Google
            // Pour l'instant, simulation
            config.setAccessToken("simulated_access_token_" + System.currentTimeMillis());
            if (response.contains("refresh_token")) {
                config.setRefreshToken("simulated_refresh_token_" + System.currentTimeMillis());
            }
            config.setTokenType("Bearer");
            config.setTokenExpiresIn(System.currentTimeMillis() + 3600000); // 1 heure
            
            AppLogger.info("google", "Tokens mis à jour avec succès");
            return true;
            
        } catch (Exception e) {
            AppLogger.error("Erreur parsing réponse tokens: " + e.getMessage());
            return false;
        }
    }
    
    private String generateState() {
        // Génère un état aléatoire pour la sécurité OAuth2
        return Base64.getEncoder().encodeToString(
                ("magsav_" + System.currentTimeMillis()).getBytes()
        );
    }
    
    // Getters
    public GoogleServicesConfig getConfig() {
        return config;
    }
    
    public boolean isConfigured() {
        return config.isConfigured();
    }
}