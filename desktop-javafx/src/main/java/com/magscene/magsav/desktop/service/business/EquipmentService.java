package com.magscene.magsav.desktop.service.business;

import com.magscene.magsav.desktop.service.api.EquipmentApiClient;
import java.util.concurrent.CompletableFuture;

/**
 * Service métier pour la gestion des équipements
 * Encapsule la logique business et les appels API
 */
public class EquipmentService {
    private final EquipmentApiClient apiClient;
    
    public EquipmentService() {
        this.apiClient = new EquipmentApiClient();
    }
    
    public EquipmentService(EquipmentApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Charge tous les équipements avec gestion d'erreur
     */
    public CompletableFuture<String> loadAllEquipments() {
        return apiClient.getAllEquipments()
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors du chargement des équipements: " + error.getMessage());
                    return "[]";
                }
                System.out.println("✅ Équipements chargés avec succès");
                return result;
            });
    }
    
    /**
     * Recherche des équipements avec validation des paramètres
     */
    public CompletableFuture<String> searchEquipments(String query, String category, String status) {
        // Validation et nettoyage des paramètres
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanCategory = (category != null && !category.equals("Toutes catégories")) ? category : null;
        String cleanStatus = (status != null && !status.equals("Tous statuts")) ? status : null;
        
        return apiClient.searchEquipments(cleanQuery, cleanCategory, cleanStatus)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors de la recherche: " + error.getMessage());
                    return "[]";
                }
                System.out.println("🔍 Recherche effectuée avec succès");
                return result;
            });
    }
    
    /**
     * Import LOCMAT avec validation du fichier
     */
    public CompletableFuture<String> importLocmatFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Le chemin du fichier ne peut pas être vide")
            );
        }
        
        // Vérifier l'extension du fichier
        if (!filePath.toLowerCase().endsWith(".xlsx") && !filePath.toLowerCase().endsWith(".xls")) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Le fichier doit être au format Excel (.xlsx ou .xls)")
            );
        }
        
        return apiClient.importLocmat(filePath)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors de l'import LOCMAT: " + error.getMessage());
                    throw new RuntimeException("Erreur import LOCMAT: " + error.getMessage(), error);
                }
                System.out.println("📥 Import LOCMAT réussi");
                return result;
            });
    }
    
    /**
     * Création d'équipement avec validation
     */
    public CompletableFuture<String> createEquipment(Object equipment) {
        if (equipment == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("L'équipement ne peut pas être null")
            );
        }
        
        return apiClient.createEquipment(equipment)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors de la création: " + error.getMessage());
                    throw new RuntimeException("Erreur création: " + error.getMessage(), error);
                }
                System.out.println("➕ Équipement créé avec succès");
                return result;
            });
    }
    
    /**
     * Suppression d'équipement avec confirmation
     */
    public CompletableFuture<String> deleteEquipment(Long id) {
        if (id == null || id <= 0) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("L'ID de l'équipement doit être valide")
            );
        }
        
        return apiClient.deleteEquipment(id)
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Erreur lors de la suppression: " + error.getMessage());
                    throw new RuntimeException("Erreur suppression: " + error.getMessage(), error);
                }
                System.out.println("🗑️ Équipement supprimé avec succès");
                return result;
            });
    }
    
    /**
     * Test de connexion au backend
     */
    public CompletableFuture<Boolean> testBackendConnection() {
        return apiClient.testConnection()
            .handle((result, error) -> {
                if (error != null) {
                    System.err.println("❌ Backend non disponible: " + error.getMessage());
                    return false;
                }
                System.out.println("✅ Backend disponible");
                return result;
            });
    }
}