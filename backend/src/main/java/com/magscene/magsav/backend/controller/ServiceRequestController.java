
package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.ServiceRequest;
import com.magscene.magsav.backend.dto.ServiceRequestDTO;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.backend.entity.ServiceRequest.Priority;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.entity.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des demandes de service (SAV)
 * Utilise les fonctionnalités Java 21 pour l'optimisation
 */
@RestController
@RequestMapping("/api/service-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:8080"})
public class ServiceRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestController.class);
    
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    
    @SuppressWarnings("unused")
    @Autowired
    private EquipmentRepository equipmentRepository;



    
    /**
     * RÃƒÂ©cupÃƒÂ¨re toutes les demandes SAV
     * GET /api/sav
     */
    @GetMapping
    public List<ServiceRequestDTO> getAllServiceRequests() {
        List<ServiceRequest> requests = serviceRequestRepository.findAllWithEquipment();
        return requests.stream().map(sr -> {
            ServiceRequestDTO dto = new ServiceRequestDTO();
            dto.id = sr.getId();
            dto.title = sr.getTitle();
            dto.description = sr.getDescription();
            dto.priority = sr.getPriority() != null ? sr.getPriority().name() : null;
            dto.status = sr.getStatus() != null ? sr.getStatus().name() : null;
            dto.type = sr.getType() != null ? sr.getType().name() : null;
            dto.requesterName = sr.getRequesterName();
            dto.requesterEmail = sr.getRequesterEmail();
            dto.assignedTechnician = sr.getAssignedTechnician();
            dto.estimatedCost = sr.getEstimatedCost();
            dto.actualCost = sr.getActualCost();
            dto.resolutionNotes = sr.getResolutionNotes();
            dto.createdAt = sr.getCreatedAt();
            dto.updatedAt = sr.getUpdatedAt();
            dto.resolvedAt = sr.getResolvedAt();
            if (sr.getEquipment() != null) {
                ServiceRequestDTO.EquipmentDTO eqDto = new ServiceRequestDTO.EquipmentDTO();
                eqDto.id = sr.getEquipment().getId();
                eqDto.name = sr.getEquipment().getName();
                eqDto.brand = sr.getEquipment().getBrand();
                eqDto.category = sr.getEquipment().getCategory();
                eqDto.serialNumber = sr.getEquipment().getSerialNumber();
                eqDto.locmatCode = sr.getEquipment().getInternalReference();
                eqDto.model = sr.getEquipment().getModel();
                dto.equipment = eqDto;
            }
            return dto;
        }).toList();
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re une demande SAV par son ID
     * GET /api/sav/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestDTO> getServiceRequestById(@PathVariable Long id) {
        Optional<ServiceRequest> request = serviceRequestRepository.findById(id);
        return request.map(sr -> {
            ServiceRequestDTO dto = new ServiceRequestDTO();
            dto.id = sr.getId();
            dto.title = sr.getTitle();
            dto.description = sr.getDescription();
            dto.priority = sr.getPriority() != null ? sr.getPriority().name() : null;
            dto.status = sr.getStatus() != null ? sr.getStatus().name() : null;
            dto.type = sr.getType() != null ? sr.getType().name() : null;
            dto.requesterName = sr.getRequesterName();
            dto.requesterEmail = sr.getRequesterEmail();
            dto.assignedTechnician = sr.getAssignedTechnician();
            dto.estimatedCost = sr.getEstimatedCost();
            dto.actualCost = sr.getActualCost();
            dto.resolutionNotes = sr.getResolutionNotes();
            dto.createdAt = sr.getCreatedAt();
            dto.updatedAt = sr.getUpdatedAt();
            dto.resolvedAt = sr.getResolvedAt();
            if (sr.getEquipment() != null) {
                ServiceRequestDTO.EquipmentDTO eqDto = new ServiceRequestDTO.EquipmentDTO();
                eqDto.id = sr.getEquipment().getId();
                eqDto.name = sr.getEquipment().getName();
                eqDto.brand = sr.getEquipment().getBrand();
                eqDto.category = sr.getEquipment().getCategory();
                eqDto.serialNumber = sr.getEquipment().getSerialNumber();
                eqDto.locmatCode = sr.getEquipment().getInternalReference();
                eqDto.model = sr.getEquipment().getModel();
                dto.equipment = eqDto;
            }
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * CrÃƒÂ©e une nouvelle demande SAV
     * POST /api/sav
     */
    @PostMapping
    public ResponseEntity<ServiceRequestDTO> createServiceRequest(@Valid @RequestBody ServiceRequest serviceRequest) {
        logger.info("[SAV] JSON reçu pour création: {}", serviceRequest);
        serviceRequest.setCreatedAt(LocalDateTime.now());
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        if (serviceRequest.getEquipment() != null && serviceRequest.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(serviceRequest.getEquipment().getId()).orElse(null);
            if (eq != null) {
                logger.info("[SAV] Equipment lié: id={}, name={}", eq.getId(), eq.getName());
                serviceRequest.setEquipment(eq);
            } else {
                serviceRequest.setEquipment(null);
            }
        } else {
            serviceRequest.setEquipment(null);
        }

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        // Forcer le fetch de l'équipement juste après la sauvegarde
        if (savedRequest.getEquipment() != null && savedRequest.getEquipment().getId() != null) {
            Equipment eq = equipmentRepository.findById(savedRequest.getEquipment().getId()).orElse(null);
            savedRequest.setEquipment(eq);
        }
        logger.info("[SAV] ServiceRequest sauvegardé: id={}, equipment={}", savedRequest.getId(), savedRequest.getEquipment());
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.id = savedRequest.getId();
        dto.title = savedRequest.getTitle();
        dto.description = savedRequest.getDescription();
        dto.priority = savedRequest.getPriority() != null ? savedRequest.getPriority().name() : null;
        dto.status = savedRequest.getStatus() != null ? savedRequest.getStatus().name() : null;
        dto.type = savedRequest.getType() != null ? savedRequest.getType().name() : null;
        dto.requesterName = savedRequest.getRequesterName();
        dto.requesterEmail = savedRequest.getRequesterEmail();
        dto.assignedTechnician = savedRequest.getAssignedTechnician();
        dto.estimatedCost = savedRequest.getEstimatedCost();
        dto.actualCost = savedRequest.getActualCost();
        dto.resolutionNotes = savedRequest.getResolutionNotes();
        dto.createdAt = savedRequest.getCreatedAt();
        dto.updatedAt = savedRequest.getUpdatedAt();
        dto.resolvedAt = savedRequest.getResolvedAt();
        if (savedRequest.getEquipment() != null) {
            ServiceRequestDTO.EquipmentDTO eqDto = new ServiceRequestDTO.EquipmentDTO();
            eqDto.id = savedRequest.getEquipment().getId();
            eqDto.name = savedRequest.getEquipment().getName();
            eqDto.brand = savedRequest.getEquipment().getBrand();
            eqDto.category = savedRequest.getEquipment().getCategory();
            eqDto.serialNumber = savedRequest.getEquipment().getSerialNumber();
            eqDto.locmatCode = savedRequest.getEquipment().getInternalReference();
            eqDto.model = savedRequest.getEquipment().getModel();
            dto.equipment = eqDto;
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Handler global pour afficher les erreurs de validation (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        logger.error("[SAV] Erreur de validation lors de la création/modification : {}", errors);
        return errors;
    }
    
    /**
     * Met ÃƒÂ  jour une demande SAV
     * PUT /api/sav/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequestDTO> updateServiceRequest(
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
                
                // Mise à jour de l'équipement si fourni
                if (serviceRequest.getEquipment() != null && serviceRequest.getEquipment().getId() != null) {
                    Equipment eq = equipmentRepository.findById(serviceRequest.getEquipment().getId()).orElse(null);
                    if (eq != null) {
                        logger.info("[SAV] Equipment mis à jour: id={}, name={}", eq.getId(), eq.getName());
                        existingRequest.setEquipment(eq);
                    }
                }
                
                ServiceRequest saved = serviceRequestRepository.save(existingRequest);
                ServiceRequestDTO dto = new ServiceRequestDTO();
                dto.id = saved.getId();
                dto.title = saved.getTitle();
                dto.description = saved.getDescription();
                dto.priority = saved.getPriority() != null ? saved.getPriority().name() : null;
                dto.status = saved.getStatus() != null ? saved.getStatus().name() : null;
                dto.type = saved.getType() != null ? saved.getType().name() : null;
                dto.requesterName = saved.getRequesterName();
                dto.requesterEmail = saved.getRequesterEmail();
                dto.assignedTechnician = saved.getAssignedTechnician();
                dto.estimatedCost = saved.getEstimatedCost();
                dto.actualCost = saved.getActualCost();
                dto.resolutionNotes = saved.getResolutionNotes();
                dto.createdAt = saved.getCreatedAt();
                dto.updatedAt = saved.getUpdatedAt();
                dto.resolvedAt = saved.getResolvedAt();
                if (saved.getEquipment() != null) {
                    ServiceRequestDTO.EquipmentDTO eqDto = new ServiceRequestDTO.EquipmentDTO();
                    eqDto.id = saved.getEquipment().getId();
                    eqDto.name = saved.getEquipment().getName();
                    eqDto.brand = saved.getEquipment().getBrand();
                    eqDto.category = saved.getEquipment().getCategory();
                    eqDto.serialNumber = saved.getEquipment().getSerialNumber();
                    eqDto.locmatCode = saved.getEquipment().getInternalReference();
                    eqDto.model = saved.getEquipment().getModel();
                    dto.equipment = eqDto;
                }
                return ResponseEntity.ok(dto);
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

    /**
     * Valide une demande SAV et crée une intervention (statut IN_PROGRESS)
     * POST /api/service-requests/{id}/validate
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateAndCreateIntervention(@PathVariable Long id) {
        Optional<ServiceRequest> optRequest = serviceRequestRepository.findById(id);
        if (optRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Demande non trouvée"));
        }
        ServiceRequest demande = optRequest.get();
        
        // Vérifier que la demande est bien au statut OPEN (seules les demandes OPEN peuvent être validées)
        if (demande.getStatus() != ServiceRequest.ServiceRequestStatus.OPEN) {
            String currentStatus = demande.getStatus() != null ? demande.getStatus().getDisplayName() : "inconnu";
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Cette demande ne peut pas être validée (statut actuel: " + currentStatus + ")"));
        }
        
        // 1. Passer la demande au statut VALIDATED
        demande.setStatus(ServiceRequest.ServiceRequestStatus.VALIDATED);
        demande.setUpdatedAt(LocalDateTime.now());
        serviceRequestRepository.save(demande);
        
        // 2. Créer une intervention liée (copie des infos principales)
        ServiceRequest intervention = new ServiceRequest();
        intervention.setTitle("[INTERVENTION] " + demande.getTitle());
        intervention.setDescription(demande.getDescription());
        intervention.setPriority(demande.getPriority());
        intervention.setType(demande.getType());
        intervention.setEquipment(demande.getEquipment());
        intervention.setRequesterName(demande.getRequesterName());
        intervention.setRequesterEmail(demande.getRequesterEmail());
        intervention.setStatus(ServiceRequest.ServiceRequestStatus.IN_PROGRESS);
        intervention.setCreatedAt(LocalDateTime.now());
        intervention.setUpdatedAt(LocalDateTime.now());
        serviceRequestRepository.save(intervention);
        
        // 3. Retourner les deux objets (demande validée + intervention créée)
        Map<String, Object> result = new HashMap<>();
        result.put("validatedRequestId", demande.getId());
        result.put("interventionId", intervention.getId());
        result.put("interventionStatus", intervention.getStatus().name());
        result.put("interventionTitle", intervention.getTitle());
        return ResponseEntity.ok(result);
    }
}