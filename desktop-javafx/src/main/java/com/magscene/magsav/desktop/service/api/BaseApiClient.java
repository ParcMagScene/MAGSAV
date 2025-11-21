package com.magscene.magsav.desktop.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Client API de base fournissant les fonctionnalités communes
 * pour tous les clients API spécialisés
 */
public abstract class BaseApiClient {
    protected static final String BASE_URL = "http://localhost:8080";
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    
    public BaseApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        this.objectMapper = new ObjectMapper();
        // TODO: Ajouter JavaTimeModule quand la dépendance sera disponible
    }
    
    /**
     * Effectue une requête GET asynchrone
     */
    protected CompletableFuture<String> getAsync(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Content-Type", "application/json")
            .GET()
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    
    /**
     * Effectue une requête POST asynchrone
     */
    protected CompletableFuture<String> postAsync(String endpoint, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Effectue une requête PUT asynchrone
     */
    protected CompletableFuture<String> putAsync(String endpoint, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Effectue une requête DELETE asynchrone
     */
    protected CompletableFuture<String> deleteAsync(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Content-Type", "application/json")
            .DELETE()
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    
    /**
     * Teste la connexion au backend
     */
    public CompletableFuture<Boolean> testConnection() {
        return getAsync("/api/health")
            .thenApply(response -> !response.isEmpty())
            .exceptionally(throwable -> false);
    }
    
    /**
     * Convertit une réponse JSON en objet
     */
    protected <T> T parseResponse(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}