package com.magscene.magsav.desktop.service.business;

import com.magscene.magsav.desktop.service.api.SAVApiClient;
import java.util.concurrent.CompletableFuture;

/**
 * Service métier pour la gestion du SAV
 * Encapsule la logique business et les appels API
 */
public class SAVService {
    private final SAVApiClient apiClient;
    
    public SAVService() {
        this.apiClient = new SAVApiClient();
    }
    
    public SAVService(SAVApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Charge toutes les demandes SAV
     */
    public CompletableFuture<String> loadAllSAVRequests() {
        return apiClient.getAllSAVRequests()
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors du chargement SAV: " + error.getMessage());
                    return "[]";
                }
                System.out.println("✅ Demandes SAV chargées avec succès");
                return result;
            });
    }
    
    /**
     * Crée une nouvelle demande SAV
     */
    public CompletableFuture<String> createSAVRequest(Object savRequest) {
        if (savRequest == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("La demande SAV ne peut pas être null")
            );
        }
        
        return apiClient.createSAVRequest(savRequest)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur création SAV: " + error.getMessage());
                    throw new RuntimeException("Erreur création SAV: " + error.getMessage(), error);
                }
                System.out.println("➕ Demande SAV créée avec succès");
                return result;
            });
    }
    
    /**
     * Met à jour le statut d'une demande SAV
     */
    public CompletableFuture<String> updateSAVStatus(Long id, String newStatus) {
        if (id == null || id <= 0) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("L'ID de la demande SAV doit être valide")
            );
        }
        
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Le nouveau statut ne peut pas être vide")
            );
        }
        
        return apiClient.updateSAVStatus(id, newStatus)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur mise à jour statut: " + error.getMessage());
                    throw new RuntimeException("Erreur statut: " + error.getMessage(), error);
                }
                System.out.println("🔄 Statut SAV mis à jour: " + newStatus);
                return result;
            });
    }
    
    /**
     * Recherche des demandes SAV
     */
    public CompletableFuture<String> searchSAVRequests(String query, String status, String priority) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanStatus = (status != null && !status.equals("Tous statuts")) ? status : null;
        String cleanPriority = (priority != null && !priority.equals("Toutes priorités")) ? priority : null;
        
        return apiClient.searchSAVRequests(cleanQuery, cleanStatus, cleanPriority)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur recherche SAV: " + error.getMessage());
                    return "[]";
                }
                System.out.println("🔍 Recherche SAV effectuée");
                return result;
            });
    }
    
    /**
     * Récupère les statistiques SAV
     */
    public CompletableFuture<String> getSAVStatistics() {
        return apiClient.getSAVStatistics()
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur statistiques SAV: " + error.getMessage());
                    return "{\"error\": \"Impossible de charger les statistiques\"}";
                }
                System.out.println("📊 Statistiques SAV chargées");
                return result;
            });
    }
    
    /**
     * Supprime une demande SAV par son ID
     */
    public java.util.concurrent.CompletableFuture<String> deleteSAVRequest(Long id) {
        return apiClient.deleteSAVRequest(id);
    }
}