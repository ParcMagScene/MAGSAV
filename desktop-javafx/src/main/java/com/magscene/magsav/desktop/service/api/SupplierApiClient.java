package com.magscene.magsav.desktop.service.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.magsav.entities.*;
import com.magsav.enums.RequestContext;
import com.magsav.enums.RequestUrgency;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Client API pour la gestion des fournisseurs et demandes de matériel
 * Communique avec les endpoints REST du backend
 */
public class SupplierApiClient extends BaseApiClient {
    
    private static final String MATERIAL_REQUESTS_ENDPOINT = "/api/material-requests";
    private static final String SUPPLIERS_ENDPOINT = "/api/suppliers";
    private static final String GROUPED_ORDERS_ENDPOINT = "/api/grouped-orders";
    
    // ==================== DEMANDES DE MATÉRIEL ====================
    
    /**
     * Récupère toutes les demandes de matériel
     */
    public CompletableFuture<List<MaterialRequest>> getAllRequests() {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<MaterialRequest>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing demandes", e);
                }
            });
    }
    
    /**
     * Récupère une demande par son ID
     */
    public CompletableFuture<MaterialRequest> getRequestById(Long id) {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + id)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequest.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing demande", e);
                }
            });
    }
    
    /**
     * Récupère une demande par son numéro
     */
    public CompletableFuture<MaterialRequest> getRequestByNumber(String requestNumber) {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT + "/number/" + requestNumber)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequest.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing demande", e);
                }
            });
    }
    
    /**
     * Crée une nouvelle demande de matériel
     */
    public CompletableFuture<MaterialRequest> createRequest(CreateRequestDTO requestDTO) {
        return postAsync(MATERIAL_REQUESTS_ENDPOINT, requestDTO)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequest.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur création demande", e);
                }
            });
    }
    
    /**
     * Met à jour une demande existante
     */
    public CompletableFuture<MaterialRequest> updateRequest(Long id, MaterialRequest request) {
        return putAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + id, request)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequest.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur mise à jour demande", e);
                }
            });
    }
    
    /**
     * Recherche des demandes par mot-clé
     */
    public CompletableFuture<List<MaterialRequest>> searchRequests(String query) {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT + "/search?q=" + query)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<MaterialRequest>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur recherche demandes", e);
                }
            });
    }
    
    /**
     * Récupère les demandes en attente d'approbation
     */
    public CompletableFuture<List<MaterialRequest>> getPendingApprovalRequests() {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT + "/pending-approval")
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<MaterialRequest>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur récupération demandes en attente", e);
                }
            });
    }
    
    /**
     * Récupère les demandes approuvées
     */
    public CompletableFuture<List<MaterialRequest>> getApprovedRequests() {
        return getAsync(MATERIAL_REQUESTS_ENDPOINT + "/approved")
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<MaterialRequest>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur récupération demandes approuvées", e);
                }
            });
    }
    
    /**
     * Soumet une demande pour approbation
     */
    public CompletableFuture<Void> submitForApproval(Long id) {
        return putAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + id + "/submit", null)
            .thenApply(json -> null);
    }
    
    /**
     * Approuve une demande
     */
    public CompletableFuture<Void> approveRequest(Long id, String approver) {
        ApprovalDTO dto = new ApprovalDTO();
        dto.approver = approver;
        return putAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + id + "/approve", dto)
            .thenApply(json -> null);
    }
    
    /**
     * Rejette une demande
     */
    public CompletableFuture<Void> rejectRequest(Long id, String reason) {
        RejectionDTO dto = new RejectionDTO();
        dto.reason = reason;
        return putAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + id + "/reject", dto)
            .thenApply(json -> null);
    }
    
    /**
     * Ajoute un article du catalogue à une demande
     */
    public CompletableFuture<MaterialRequestItem> addCatalogItem(Long requestId, Long catalogItemId, int quantity) {
        AddCatalogItemDTO dto = new AddCatalogItemDTO();
        dto.catalogItemId = catalogItemId;
        dto.quantity = quantity;
        
        return postAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + requestId + "/items/catalog", dto)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequestItem.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur ajout article catalogue", e);
                }
            });
    }
    
    /**
     * Ajoute un article libre à une demande
     */
    public CompletableFuture<MaterialRequestItem> addFreeItem(Long requestId, String reference, String name, int quantity, Double estimatedPrice) {
        AddFreeItemDTO dto = new AddFreeItemDTO();
        dto.reference = reference;
        dto.name = name;
        dto.quantity = quantity;
        dto.estimatedPrice = estimatedPrice;
        
        return postAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + requestId + "/items/free", dto)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, MaterialRequestItem.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur ajout article libre", e);
                }
            });
    }
    
    /**
     * Supprime un article d'une demande
     */
    public CompletableFuture<Void> removeItem(Long requestId, Long itemId) {
        return deleteAsync(MATERIAL_REQUESTS_ENDPOINT + "/" + requestId + "/items/" + itemId)
            .thenApply(json -> null);
    }
    
    // ==================== FOURNISSEURS ====================
    
    /**
     * Récupère tous les fournisseurs
     */
    public CompletableFuture<List<Supplier>> getAllSuppliers() {
        return getAsync(SUPPLIERS_ENDPOINT)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<Supplier>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing fournisseurs", e);
                }
            });
    }
    
    /**
     * Récupère un fournisseur par ID
     */
    public CompletableFuture<Supplier> getSupplierById(Long id) {
        return getAsync(SUPPLIERS_ENDPOINT + "/" + id)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, Supplier.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing fournisseur", e);
                }
            });
    }
    
    /**
     * Crée un nouveau fournisseur
     */
    public CompletableFuture<Supplier> createSupplier(Supplier supplier) {
        return postAsync(SUPPLIERS_ENDPOINT, supplier)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, Supplier.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur création fournisseur", e);
                }
            });
    }
    
    /**
     * Met à jour un fournisseur
     */
    public CompletableFuture<Supplier> updateSupplier(Long id, Supplier supplier) {
        return putAsync(SUPPLIERS_ENDPOINT + "/" + id, supplier)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, Supplier.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur mise à jour fournisseur", e);
                }
            });
    }
    
    /**
     * Recherche des fournisseurs
     */
    public CompletableFuture<List<Supplier>> searchSuppliers(String query) {
        return getAsync(SUPPLIERS_ENDPOINT + "/search?q=" + query)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<Supplier>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur recherche fournisseurs", e);
                }
            });
    }
    
    // ==================== COMMANDES GROUPÉES ====================
    
    /**
     * Récupère toutes les commandes groupées
     */
    public CompletableFuture<List<GroupedOrder>> getAllGroupedOrders() {
        return getAsync(GROUPED_ORDERS_ENDPOINT)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<GroupedOrder>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur parsing commandes groupées", e);
                }
            });
    }
    
    /**
     * Crée une commande groupée
     */
    public CompletableFuture<GroupedOrder> createGroupedOrder(GroupedOrder order) {
        return postAsync(GROUPED_ORDERS_ENDPOINT, order)
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, GroupedOrder.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur création commande groupée", e);
                }
            });
    }
    
    /**
     * Récupère les commandes en attente
     */
    public CompletableFuture<List<GroupedOrder>> getPendingOrders() {
        return getAsync(GROUPED_ORDERS_ENDPOINT + "/pending")
            .thenApply(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<List<GroupedOrder>>() {});
                } catch (Exception e) {
                    throw new RuntimeException("Erreur récupération commandes en attente", e);
                }
            });
    }
    
    // ==================== DTOs INTERNES ====================
    
    public static class CreateRequestDTO {
        public String requesterName;
        public String requesterEmail;
        public RequestContext context;
        public String description;
        public String justification;
        public RequestUrgency urgency;
        public String deliveryAddress;
    }
    
    public static class ApprovalDTO {
        public String approver;
    }
    
    public static class RejectionDTO {
        public String reason;
    }
    
    public static class AddCatalogItemDTO {
        public Long catalogItemId;
        public int quantity;
    }
    
    public static class AddFreeItemDTO {
        public String reference;
        public String name;
        public int quantity;
        public Double estimatedPrice;
    }
}
