package com.magscene.magsav.backend.service;

import com.magsav.entities.*;
import com.magsav.enums.RequestContext;
import com.magsav.enums.RequestStatus;
import com.magsav.enums.RequestUrgency;
import com.magscene.magsav.backend.entity.Project;
import com.magscene.magsav.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des demandes de matériel
 */
@Service
@Transactional
public class MaterialRequestService {
    
    @Autowired
    private MaterialRequestRepository materialRequestRepository;
    
    @Autowired
    private MaterialRequestItemRepository materialRequestItemRepository;
    
    @Autowired
    private OrderAllocationRepository orderAllocationRepository;
    
    @Autowired
    private GroupedOrderService groupedOrderService;
    
    @Autowired
    private NotificationService notificationService;
    
    // *** GESTION DES DEMANDES ***
    
    public List<MaterialRequest> findAll() {
        return materialRequestRepository.findAll();
    }
    
    public List<MaterialRequest> findByRequester(String requesterEmail) {
        return materialRequestRepository.findByRequesterEmailOrderByCreatedAtDesc(requesterEmail);
    }
    
    public List<MaterialRequest> findPendingApproval() {
        return materialRequestRepository.findPendingApprovalRequests();
    }
    
    public List<MaterialRequest> findApprovedRequests() {
        return materialRequestRepository.findByStatus(RequestStatus.APPROVED);
    }
    
    public Optional<MaterialRequest> findById(Long id) {
        return materialRequestRepository.findById(id);
    }
    
    public Optional<MaterialRequest> findByRequestNumber(String requestNumber) {
        return materialRequestRepository.findByRequestNumber(requestNumber);
    }
    
    public MaterialRequest save(MaterialRequest request) {
        return materialRequestRepository.save(request);
    }
    
    public MaterialRequest createRequest(String requesterName, String requesterEmail, 
                                        RequestContext context, String description) {
        MaterialRequest request = new MaterialRequest(requesterName, requesterEmail, context);
        request.setDescription(description);
        return materialRequestRepository.save(request);
    }
    
    // *** WORKFLOW D'APPROBATION ***
    
    public void submitForApproval(Long requestId) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.canBeModified()) {
            throw new IllegalStateException("Cette demande ne peut plus être modifiée");
        }
        
        if (request.getItems().isEmpty()) {
            throw new IllegalStateException("Impossible de soumettre une demande sans articles");
        }
        
        request.submitForApproval();
        materialRequestRepository.save(request);
        
        // Notification aux administrateurs
        notificationService.sendApprovalNotification(request);
    }
    
    public void approve(Long requestId, String approver) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.isPendingApproval()) {
            throw new IllegalStateException("Cette demande n'est pas en attente d'approbation");
        }
        
        request.approve(approver);
        materialRequestRepository.save(request);
        
        // Intégration automatique aux commandes groupées
        integrateToGroupedOrders(requestId);
        
        // Notification au demandeur
        notificationService.sendApprovalConfirmation(request);
    }
    
    public void reject(Long requestId, String rejectionReason) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.isPendingApproval()) {
            throw new IllegalStateException("Cette demande n'est pas en attente d'approbation");
        }
        
        request.reject(rejectionReason);
        materialRequestRepository.save(request);
        
        // Notification au demandeur
        notificationService.sendRejectionNotification(request);
    }
    
    // *** GESTION DES ARTICLES ***
    
    public MaterialRequestItem addCatalogItem(Long requestId, Long catalogItemId, Integer quantity) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.canBeModified()) {
            throw new IllegalStateException("Cette demande ne peut plus être modifiée");
        }
        
        // TODO: Récupérer le CatalogItem
        // Pour l'instant, créer un item libre
        MaterialRequestItem item = new MaterialRequestItem(request, quantity);
        item.setFreeReference("CAT-" + catalogItemId);
        item.setFreeName("Article de catalogue");
        
        request.addItem(item);
        materialRequestRepository.save(request);
        
        return item;
    }
    
    public MaterialRequestItem addFreeItem(Long requestId, String reference, String name, 
                                          Integer quantity, java.math.BigDecimal estimatedPrice) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.canBeModified()) {
            throw new IllegalStateException("Cette demande ne peut plus être modifiée");
        }
        
        MaterialRequestItem item = new MaterialRequestItem(request, reference, name, quantity, estimatedPrice);
        request.addItem(item);
        materialRequestRepository.save(request);
        
        return item;
    }
    
    public void removeItem(Long requestId, Long itemId) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.canBeModified()) {
            throw new IllegalStateException("Cette demande ne peut plus être modifiée");
        }
        
        Optional<MaterialRequestItem> itemOpt = materialRequestItemRepository.findById(itemId);
        if (itemOpt.isPresent() && itemOpt.get().getMaterialRequest().equals(request)) {
            request.removeItem(itemOpt.get());
            materialRequestRepository.save(request);
        }
    }
    
    // *** INTÉGRATION AUX COMMANDES GROUPÉES ***
    
    public void integrateToGroupedOrders(Long requestId) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Demande introuvable : " + requestId);
        }
        
        MaterialRequest request = requestOpt.get();
        if (!request.isApproved()) {
            throw new IllegalStateException("Seules les demandes approuvées peuvent être intégrées");
        }
        
        // Regrouper les items par fournisseur (via catalogue)
        for (MaterialRequestItem item : request.getItems()) {
            if (item.getQuantityPending() > 0) {
                // TODO: Identifier le fournisseur via le catalogue
                // Pour l'instant, créer une allocation générique
                groupedOrderService.allocateItem(item, item.getQuantityPending());
            }
        }
        
        request.setStatus(RequestStatus.INTEGRATED);
        materialRequestRepository.save(request);
    }
    
    public List<OrderAllocation> getAllocations(Long requestId) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            return orderAllocationRepository.findByMaterialRequest(requestOpt.get());
        }
        return List.of();
    }
    
    // *** RECHERCHE ET FILTRES ***
    
    public List<MaterialRequest> searchRequests(String searchTerm) {
        return materialRequestRepository.searchRequests(searchTerm);
    }
    
    public List<MaterialRequest> findByContext(RequestContext context) {
        return materialRequestRepository.findByContext(context);
    }
    
    public List<MaterialRequest> findByProject(Project project) {
        return materialRequestRepository.findByProject(project);
    }
    
    public List<MaterialRequest> findRecentRequests(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return materialRequestRepository.findRecentRequests(since);
    }
    
    public List<MaterialRequest> findRequestsWithUnallocatedItems() {
        return materialRequestRepository.findRequestsWithUnallocatedItems();
    }
    
    public List<MaterialRequest> findPartiallyDeliveredRequests() {
        return materialRequestRepository.findPartiallyDeliveredRequests();
    }
    
    public List<MaterialRequest> findUrgentRequests() {
        return materialRequestRepository.findUrgentRequests();
    }
    
    // *** STATISTIQUES ***
    
    public List<Object[]> getRequestsByRequesterStats() {
        return materialRequestRepository.countRequestsByRequester();
    }
    
    public List<Object[]> getRequestsByContextStats() {
        return materialRequestRepository.countRequestsByContext();
    }
    
    public List<Object[]> getRequestsByStatusStats() {
        return materialRequestRepository.countRequestsByStatus();
    }
    
    public Double getAverageApprovalTimeInHours() {
        return materialRequestRepository.averageApprovalTimeInHours();
    }
    
    public List<Object[]> getRequestsByDayStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return materialRequestRepository.countRequestsByDay(since);
    }
    
    // *** MÉTHODES UTILITAIRES ***
    
    public boolean canUserApprove(String userEmail) {
        // TODO: Vérifier les droits d'approbation
        return userEmail.contains("admin") || userEmail.contains("manager");
    }
    
    public void updateRequestPriority(Long requestId, RequestUrgency urgency) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            requestOpt.get().setUrgency(urgency);
            materialRequestRepository.save(requestOpt.get());
        }
    }
    
    public void addAdminNotes(Long requestId, String notes) {
        Optional<MaterialRequest> requestOpt = materialRequestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            requestOpt.get().setAdminNotes(notes);
            materialRequestRepository.save(requestOpt.get());
        }
    }
    
    public boolean hasUnprocessedRequests() {
        return !materialRequestRepository.findPendingApprovalRequests().isEmpty();
    }
}