package com.magscene.magsav.desktop.service.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Client API spécialisé pour la gestion des équipements
 */
public class EquipmentApiClient extends BaseApiClient {
    
    public EquipmentApiClient() {
        super();
    }
    
    /**
     * Récupère tous les équipements
     */
    public CompletableFuture<String> getAllEquipments() {
        return getAsync("/api/equipment");
    }
    
    /**
     * Récupère un équipement par son ID
     */
    public CompletableFuture<String> getEquipmentById(Long id) {
        return getAsync("/api/equipment/" + id);
    }
    
    /**
     * Crée un nouvel équipement
     */
    public CompletableFuture<String> createEquipment(Object equipment) {
        return postAsync("/api/equipment", equipment);
    }
    
    /**
     * Met à jour un équipement existant
     */
    public CompletableFuture<String> updateEquipment(Long id, Object equipment) {
        return putAsync("/api/equipment/" + id, equipment);
    }
    
    /**
     * Supprime un équipement
     */
    public CompletableFuture<String> deleteEquipment(Long id) {
        return deleteAsync("/api/equipment/" + id);
    }
    
    /**
     * Recherche des équipements par critères
     */
    public CompletableFuture<String> searchEquipments(String query, String category, String status) {
        StringBuilder endpoint = new StringBuilder("/api/equipment/search?");
        
        if (query != null && !query.trim().isEmpty()) {
            endpoint.append("q=").append(query).append("&");
        }
        if (category != null && !category.equals("Toutes catégories")) {
            endpoint.append("category=").append(category).append("&");
        }
        if (status != null && !status.equals("Tous statuts")) {
            endpoint.append("status=").append(status).append("&");
        }
        
        // Supprimer le dernier & si présent
        String finalEndpoint = endpoint.toString();
        if (finalEndpoint.endsWith("&")) {
            finalEndpoint = finalEndpoint.substring(0, finalEndpoint.length() - 1);
        }
        
        return getAsync(finalEndpoint);
    }
    
    /**
     * Import LOCMAT
     */
    public CompletableFuture<String> importLocmat(String filePath) {
        return postAsync("/api/locmat/import", filePath);
    }
    
    /**
     * Génère les QR codes pour les équipements
     */
    public CompletableFuture<String> generateQRCodes(List<Long> equipmentIds) {
        return postAsync("/api/equipment/qrcodes", equipmentIds);
    }
    
    /**
     * Exporte les équipements
     */
    public CompletableFuture<String> exportEquipments(String format) {
        return getAsync("/api/equipment/export?format=" + format);
    }
}