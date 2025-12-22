package com.magscene.magsav.desktop.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("Accept-Charset", "UTF-8")
            .GET()
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
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
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
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
            System.out.println("🔄 PUT " + endpoint);
            System.out.println("   Body: " + (jsonBody.length() > 500 ? jsonBody.substring(0, 500) + "..." : jsonBody));
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(response -> {
                        System.out.println("✅ PUT " + endpoint + " -> réponse reçue");
                        return response;
                    })
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
            .header("Content-Type", "application/json; charset=UTF-8")
            .DELETE()
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
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