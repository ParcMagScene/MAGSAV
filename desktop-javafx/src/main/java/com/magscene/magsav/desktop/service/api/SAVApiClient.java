package com.magscene.magsav.desktop.service.api;

import java.util.concurrent.CompletableFuture;

/**
 * Client API spécialisé pour la gestion du SAV
 */
public class SAVApiClient extends BaseApiClient {

    public SAVApiClient() {
        super();
    }

    /**
     * Récupère toutes les demandes SAV
     */
    public CompletableFuture<String> getAllSAVRequests() {
        return getAsync("/api/service-requests");
    }

    /**
     * Récupère une demande SAV par son ID
     */
    public CompletableFuture<String> getSAVRequestById(Long id) {
        return getAsync("/api/service-requests/" + id);
    }

    /**
     * Crée une nouvelle demande SAV
     */
    public CompletableFuture<String> createSAVRequest(Object savRequest) {
        return postAsync("/api/service-requests", savRequest);
    }

    /**
     * Met à jour une demande SAV
     */
    public CompletableFuture<String> updateSAVRequest(Long id, Object savRequest) {
        return putAsync("/api/service-requests/" + id, savRequest);
    }

    /**
     * Supprime une demande SAV
     */
    public CompletableFuture<String> deleteSAVRequest(Long id) {
        return deleteAsync("/api/service-requests/" + id);
    }

    /**
     * Change le statut d'une demande SAV
     */
    public CompletableFuture<String> updateSAVStatus(Long id, String status) {
        return putAsync("/api/sav/" + id + "/status", status);
    }

    /**
     * Récupère les statistiques SAV
     */
    public CompletableFuture<String> getSAVStatistics() {
        return getAsync("/api/sav/statistics");
    }

    /**
     * Recherche des demandes SAV par critères
     */
    public CompletableFuture<String> searchSAVRequests(String query, String status, String priority) {
        StringBuilder endpoint = new StringBuilder("/api/sav/search?");

        if (query != null && !query.trim().isEmpty()) {
            endpoint.append("q=").append(query).append("&");
        }
        if (status != null && !status.equals("Tous statuts")) {
            endpoint.append("status=").append(status).append("&");
        }
        if (priority != null && !priority.equals("Toutes priorités")) {
            endpoint.append("priority=").append(priority).append("&");
        }

        // Supprimer le dernier & si présent
        String finalEndpoint = endpoint.toString();
        if (finalEndpoint.endsWith("&")) {
            finalEndpoint = finalEndpoint.substring(0, finalEndpoint.length() - 1);
        }

        return getAsync(finalEndpoint);
    }
}