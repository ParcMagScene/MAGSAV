package com.magscene.magsav.backend.controller;

import com.magsav.entities.*;
import com.magsav.enums.RequestContext;
import com.magsav.enums.RequestUrgency;
import com.magscene.magsav.backend.dto.MaterialRequestDTO;
import com.magscene.magsav.backend.service.MaterialRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des demandes de matériel
 */
@RestController
@RequestMapping("/api/material-requests")
@CrossOrigin(origins = "*")
public class MaterialRequestController {

    @Autowired
    private MaterialRequestService materialRequestService;

    // *** CRUD DEMANDES DE MATÉRIEL ***

    @GetMapping
    public ResponseEntity<List<MaterialRequestDTO>> getAllRequests() {
        List<MaterialRequest> requests = materialRequestService.findAll();
        List<MaterialRequestDTO> dtos = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialRequestDTO> getRequestById(@PathVariable Long id) {
        Optional<MaterialRequest> request = materialRequestService.findById(id);
        return request.map(r -> ResponseEntity.ok(convertToDTO(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    private MaterialRequestDTO convertToDTO(MaterialRequest request) {
        MaterialRequestDTO dto = new MaterialRequestDTO();
        dto.setId(request.getId());
        dto.setRequestNumber(request.getRequestNumber());
        dto.setRequesterName(request.getRequesterName());
        dto.setRequesterEmail(request.getRequesterEmail());
        dto.setRequesterDepartment(request.getRequesterDepartment());
        dto.setContext(request.getContext());
        dto.setUrgency(request.getUrgency());
        dto.setDescription(request.getDescription());
        dto.setJustification(request.getJustification());
        dto.setDeliveryAddress(request.getDeliveryAddress());
        dto.setDeliveryDate(request.getDeliveryDate());
        dto.setStatus(request.getStatus());
        dto.setApprovedBy(request.getApprovedBy());
        dto.setApprovedAt(request.getApprovedAt());
        dto.setRejectionReason(request.getRejectionReason());
        dto.setAdminNotes(request.getAdminNotes());
        dto.setSubmittedAt(request.getSubmittedAt());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        return dto;
    }

    @GetMapping("/number/{requestNumber}")
    public ResponseEntity<MaterialRequest> getRequestByNumber(@PathVariable String requestNumber) {
        Optional<MaterialRequest> request = materialRequestService.findByRequestNumber(requestNumber);
        return request.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MaterialRequest> createRequest(@Valid @RequestBody CreateRequestDTO requestDTO) {
        MaterialRequest request = materialRequestService.createRequest(
                requestDTO.requesterName,
                requestDTO.requesterEmail,
                requestDTO.context,
                requestDTO.description);

        if (requestDTO.justification != null) {
            request.setJustification(requestDTO.justification);
        }
        if (requestDTO.urgency != null) {
            request.setUrgency(requestDTO.urgency);
        }
        if (requestDTO.deliveryAddress != null) {
            request.setDeliveryAddress(requestDTO.deliveryAddress);
        }

        MaterialRequest savedRequest = materialRequestService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialRequest> updateRequest(@PathVariable Long id,
            @Valid @RequestBody MaterialRequest request) {
        Optional<MaterialRequest> existingRequest = materialRequestService.findById(id);
        if (!existingRequest.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        MaterialRequest existing = existingRequest.get();
        if (!existing.canBeModified()) {
            return ResponseEntity.badRequest().build();
        }

        request.setId(id);
        MaterialRequest updatedRequest = materialRequestService.save(request);
        return ResponseEntity.ok(updatedRequest);
    }

    // *** RECHERCHE DEMANDES ***

    @GetMapping("/search")
    public ResponseEntity<List<MaterialRequest>> searchRequests(@RequestParam String q) {
        List<MaterialRequest> requests = materialRequestService.searchRequests(q);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/by-requester")
    public ResponseEntity<List<MaterialRequest>> getRequestsByRequester(@RequestParam String email) {
        List<MaterialRequest> requests = materialRequestService.findByRequester(email);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/by-context")
    public ResponseEntity<List<MaterialRequest>> getRequestsByContext(@RequestParam RequestContext context) {
        List<MaterialRequest> requests = materialRequestService.findByContext(context);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MaterialRequest>> getRecentRequests(@RequestParam(defaultValue = "30") int days) {
        List<MaterialRequest> requests = materialRequestService.findRecentRequests(days);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/urgent")
    public ResponseEntity<List<MaterialRequest>> getUrgentRequests() {
        List<MaterialRequest> requests = materialRequestService.findUrgentRequests();
        return ResponseEntity.ok(requests);
    }

    // *** WORKFLOW D'APPROBATION ***

    @GetMapping("/pending-approval")
    public ResponseEntity<List<MaterialRequest>> getPendingApprovalRequests() {
        List<MaterialRequest> requests = materialRequestService.findPendingApproval();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<MaterialRequest>> getApprovedRequests() {
        List<MaterialRequest> requests = materialRequestService.findApprovedRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submitForApproval(@PathVariable Long id) {
        try {
            materialRequestService.submitForApproval(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id,
            @RequestBody ApprovalDTO approvalDTO) {
        try {
            materialRequestService.approve(id, approvalDTO.approver);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id,
            @RequestBody RejectionDTO rejectionDTO) {
        try {
            materialRequestService.reject(id, rejectionDTO.reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // *** GESTION DES ARTICLES ***

    @PostMapping("/{id}/items/catalog")
    public ResponseEntity<MaterialRequestItem> addCatalogItem(@PathVariable Long id,
            @RequestBody AddCatalogItemDTO itemDTO) {
        try {
            MaterialRequestItem item = materialRequestService.addCatalogItem(
                    id, itemDTO.catalogItemId, itemDTO.quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/items/free")
    public ResponseEntity<MaterialRequestItem> addFreeItem(@PathVariable Long id,
            @RequestBody AddFreeItemDTO itemDTO) {
        try {
            MaterialRequestItem item = materialRequestService.addFreeItem(
                    id, itemDTO.reference, itemDTO.name, itemDTO.quantity, itemDTO.estimatedPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        materialRequestService.removeItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    // *** SUIVI ET ALLOCATIONS ***

    @GetMapping("/{id}/allocations")
    public ResponseEntity<List<OrderAllocation>> getRequestAllocations(@PathVariable Long id) {
        List<OrderAllocation> allocations = materialRequestService.getAllocations(id);
        return ResponseEntity.ok(allocations);
    }

    @PutMapping("/{id}/integrate")
    public ResponseEntity<Void> integrateToGroupedOrders(@PathVariable Long id) {
        try {
            materialRequestService.integrateToGroupedOrders(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unallocated")
    public ResponseEntity<List<MaterialRequest>> getRequestsWithUnallocatedItems() {
        List<MaterialRequest> requests = materialRequestService.findRequestsWithUnallocatedItems();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/partially-delivered")
    public ResponseEntity<List<MaterialRequest>> getPartiallyDeliveredRequests() {
        List<MaterialRequest> requests = materialRequestService.findPartiallyDeliveredRequests();
        return ResponseEntity.ok(requests);
    }

    // *** ADMINISTRATION ***

    @PutMapping("/{id}/priority")
    public ResponseEntity<Void> updateRequestPriority(@PathVariable Long id,
            @RequestBody PriorityDTO priorityDTO) {
        materialRequestService.updateRequestPriority(id, priorityDTO.urgency);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/admin-notes")
    public ResponseEntity<Void> addAdminNotes(@PathVariable Long id,
            @RequestBody AdminNotesDTO notesDTO) {
        materialRequestService.addAdminNotes(id, notesDTO.notes);
        return ResponseEntity.ok().build();
    }

    // *** STATISTIQUES ***

    @GetMapping("/stats/by-requester")
    public ResponseEntity<List<Object[]>> getRequestsByRequesterStats() {
        List<Object[]> stats = materialRequestService.getRequestsByRequesterStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/by-context")
    public ResponseEntity<List<Object[]>> getRequestsByContextStats() {
        List<Object[]> stats = materialRequestService.getRequestsByContextStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<List<Object[]>> getRequestsByStatusStats() {
        List<Object[]> stats = materialRequestService.getRequestsByStatusStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/approval-time")
    public ResponseEntity<Double> getAverageApprovalTime() {
        Double avgTime = materialRequestService.getAverageApprovalTimeInHours();
        return ResponseEntity.ok(avgTime);
    }

    @GetMapping("/stats/by-day")
    public ResponseEntity<List<Object[]>> getRequestsByDayStats(@RequestParam(defaultValue = "30") int days) {
        List<Object[]> stats = materialRequestService.getRequestsByDayStats(days);
        return ResponseEntity.ok(stats);
    }

    // *** UTILITAIRES ***

    @GetMapping("/has-unprocessed")
    public ResponseEntity<Boolean> hasUnprocessedRequests() {
        boolean hasUnprocessed = materialRequestService.hasUnprocessedRequests();
        return ResponseEntity.ok(hasUnprocessed);
    }

    // *** CLASSES DTO ***

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
        public Integer quantity;
    }

    public static class AddFreeItemDTO {
        public String reference;
        public String name;
        public Integer quantity;
        public java.math.BigDecimal estimatedPrice;
    }

    public static class PriorityDTO {
        public RequestUrgency urgency;
    }

    public static class AdminNotesDTO {
        public String notes;
    }
}