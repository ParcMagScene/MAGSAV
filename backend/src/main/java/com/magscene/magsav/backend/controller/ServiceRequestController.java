package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.ServiceRequest;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.backend.entity.ServiceRequest.Priority;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des demandes de service (SAV)
 * Utilise les fonctionnalitÃƒÂ©s Java 21 pour l'optimisation
 */
@RestController
@RequestMapping("/api/service-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:8080"})
public class ServiceRequestController {
    
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re toutes les demandes SAV
     * GET /api/sav
     */
    @GetMapping
    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findAll();
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re une demande SAV par son ID
     * GET /api/sav/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getServiceRequestById(@PathVariable Long id) {
        Optional<ServiceRequest> request = serviceRequestRepository.findById(id);
        return request.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * CrÃƒÂ©e une nouvelle demande SAV
     * POST /api/sav
     */
    @PostMapping
    public ResponseEntity<ServiceRequest> createServiceRequest(@Valid @RequestBody ServiceRequest serviceRequest) {
        // Auto-gÃƒÂ©nÃƒÂ©ration de l'ID de demande
        serviceRequest.setCreatedAt(LocalDateTime.now());
        serviceRequest.setUpdatedAt(LocalDateTime.now());
        
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        return ResponseEntity.ok(savedRequest);
    }
    
    /**
     * Met ÃƒÂ  jour une demande SAV
     * PUT /api/sav/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequest> updateServiceRequest(
            @PathVariable Long id, 
            @Valid @RequestBody ServiceRequest serviceRequest) {
        
        return serviceRequestRepository.findById(id)
            .map(existingRequest -> {
                existingRequest.setTitle(serviceRequest.getTitle());
                existingRequest.setDescription(serviceRequest.getDescription());
                existingRequest.setPriority(serviceRequest.getPriority());
                existingRequest.setStatus(serviceRequest.getStatus());
                existingRequest.setType(serviceRequest.getType());
                existingRequest.setRequesterName(serviceRequest.getRequesterName());
                existingRequest.setRequesterEmail(serviceRequest.getRequesterEmail());
                existingRequest.setAssignedTechnician(serviceRequest.getAssignedTechnician());
                existingRequest.setEstimatedCost(serviceRequest.getEstimatedCost());
                existingRequest.setActualCost(serviceRequest.getActualCost());
                existingRequest.setResolutionNotes(serviceRequest.getResolutionNotes());
                existingRequest.setUpdatedAt(LocalDateTime.now());
                
                return ResponseEntity.ok(serviceRequestRepository.save(existingRequest));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Supprime une demande SAV
     * DELETE /api/sav/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteServiceRequest(@PathVariable Long id) {
        return serviceRequestRepository.findById(id)
            .map(request -> {
                serviceRequestRepository.delete(request);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re les demandes SAV par statut
     * GET /api/sav/status/{status}
     */
    @GetMapping("/status/{status}")
    public List<ServiceRequest> getServiceRequestsByStatus(@PathVariable ServiceRequestStatus status) {
        return serviceRequestRepository.findByStatus(status);
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re les demandes SAV par prioritÃƒÂ©
     * GET /api/sav/priority/{priority}
     */
    @GetMapping("/priority/{priority}")
    public List<ServiceRequest> getServiceRequestsByPriority(@PathVariable Priority priority) {
        return serviceRequestRepository.findByPriority(priority);
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re les demandes SAV assignÃƒÂ©es ÃƒÂ  un technicien
     * GET /api/sav/technician/{technician}
     */
    @GetMapping("/technician/{technician}")
    public List<ServiceRequest> getServiceRequestsByTechnician(@PathVariable String technician) {
        return serviceRequestRepository.findByAssignedTechnician(technician);
    }
    
    /**
     * Statistiques du SAV
     * GET /api/sav/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getSavStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Comptages par statut
        stats.put("total", serviceRequestRepository.count());
        stats.put("open", serviceRequestRepository.countByStatus(ServiceRequestStatus.OPEN));
        stats.put("inProgress", serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS));
        stats.put("resolved", serviceRequestRepository.countByStatus(ServiceRequestStatus.RESOLVED));
        stats.put("closed", serviceRequestRepository.countByStatus(ServiceRequestStatus.CLOSED));
        
        // Comptages par prioritÃƒÂ©
        Map<String, Long> priorityStats = new HashMap<>();
        priorityStats.put("high", serviceRequestRepository.countByPriority(Priority.HIGH));
        priorityStats.put("medium", serviceRequestRepository.countByPriority(Priority.MEDIUM));
        priorityStats.put("low", serviceRequestRepository.countByPriority(Priority.LOW));
        stats.put("priorities", priorityStats);
        
        // Demandes urgentes (prioritÃƒÂ© haute et non rÃƒÂ©solues)
        stats.put("urgent", serviceRequestRepository.countByPriorityAndStatusNot(Priority.HIGH, ServiceRequestStatus.RESOLVED));
        
        return stats;
    }
    
    /**
     * Assigner un technicien ÃƒÂ  une demande
     * PUT /api/sav/{id}/assign/{technician}
     */
    @PutMapping("/{id}/assign/{technician}")
    public ResponseEntity<ServiceRequest> assignTechnician(
            @PathVariable Long id, 
            @PathVariable String technician) {
        
        return serviceRequestRepository.findById(id)
            .map(request -> {
                request.setAssignedTechnician(technician);
                request.setStatus(ServiceRequestStatus.IN_PROGRESS);
                request.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(serviceRequestRepository.save(request));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * RÃƒÂ©soudre une demande SAV
     * PUT /api/sav/{id}/resolve
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ServiceRequest> resolveServiceRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> resolution) {
        
        return serviceRequestRepository.findById(id)
            .map(request -> {
                request.setStatus(ServiceRequestStatus.RESOLVED);
                request.setResolvedAt(LocalDateTime.now());
                request.setResolutionNotes((String) resolution.get("notes"));
                if (resolution.containsKey("actualCost")) {
                    request.setActualCost(Double.valueOf(resolution.get("actualCost").toString()));
                }
                request.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(serviceRequestRepository.save(request));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}

