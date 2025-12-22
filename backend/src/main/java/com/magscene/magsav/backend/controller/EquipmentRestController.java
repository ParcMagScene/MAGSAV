package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.dto.EquipmentDTO;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.repository.EquipmentPhotoRepository;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.ContractItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
/**
 * ContrÃƒÆ’Ã‚Â´leur REST pour la gestion des ÃƒÆ’Ã‚Â©quipements
 */
@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class EquipmentRestController {
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private EquipmentPhotoRepository equipmentPhotoRepository;
    
    @Autowired(required = false)
    private ServiceRequestRepository serviceRequestRepository;
    
    @Autowired(required = false)
    private ContractItemRepository contractItemRepository;
    
    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re tous les ÃƒÆ’Ã‚Â©quipements
     * GET /api/equipment
     */
    @GetMapping
    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepository.findAll()
                .stream()
                .map(EquipmentDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re un ÃƒÆ’Ã‚Â©quipement par son ID
     * GET /api/equipment/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentRepository.findById(id);
        return equipment.map(e -> ResponseEntity.ok(new EquipmentDTO(e)))
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re les ÃƒÆ’Ã‚Â©quipements par catÃƒÆ’Ã‚Â©gorie
     * GET /api/equipment/category/{category}
     */
    @GetMapping("/category/{category}")
    public List<EquipmentDTO> getEquipmentByCategory(@PathVariable String category) {
        return equipmentRepository.findByCategory(category)
                .stream()
                .map(EquipmentDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re les ÃƒÆ’Ã‚Â©quipements par statut
     * GET /api/equipment/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByStatus(@PathVariable String status) {
        try {
            Equipment.Status equipmentStatus = Equipment.Status.valueOf(status.toUpperCase());
            List<EquipmentDTO> equipment = equipmentRepository.findByStatus(equipmentStatus)
                    .stream()
                    .map(EquipmentDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(equipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Recherche d'ÃƒÆ’Ã‚Â©quipements par nom
     * GET /api/equipment/search?name=...
     */
    @GetMapping("/search")
    public List<Equipment> searchEquipment(@RequestParam String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re un ÃƒÆ’Ã‚Â©quipement par son QR Code
     * GET /api/equipment/qr/{qrCode}
     */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Equipment> getEquipmentByQrCode(@PathVariable String qrCode) {
        Optional<Equipment> equipment = equipmentRepository.findByQrCode(qrCode);
        return equipment.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Statistiques des ÃƒÆ’Ã‚Â©quipements
     * GET /api/equipment/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getEquipmentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = equipmentRepository.count();
        stats.put("total", totalCount);
        
        stats.put("available", equipmentRepository.countByStatus(Equipment.Status.AVAILABLE));
        stats.put("inUse", equipmentRepository.countByStatus(Equipment.Status.IN_USE));
        stats.put("maintenance", equipmentRepository.countByStatus(Equipment.Status.MAINTENANCE));
        stats.put("outOfOrder", equipmentRepository.countByStatus(Equipment.Status.OUT_OF_ORDER));
        
        Double totalValue = equipmentRepository.calculateTotalValue();
        stats.put("totalValue", totalValue != null ? totalValue : 0.0);
        
        // Statistiques par catÃƒÆ’Ã‚Â©gorie
        Map<String, Long> categoryStats = new HashMap<>();
        List<Object[]> categoryData = equipmentRepository.getEquipmentCountByCategory();
        for (Object[] row : categoryData) {
            categoryStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("categories", categoryStats);
        
        return stats;
    }
    
    /**
     * CrÃƒÆ’Ã‚Â©e un nouvel ÃƒÆ’Ã‚Â©quipement
     * POST /api/equipment
     */
    @PostMapping
    public ResponseEntity<EquipmentDTO> createEquipment(@RequestBody Equipment equipment) {
        try {
            Equipment savedEquipment = equipmentRepository.save(equipment);
            return ResponseEntity.ok(new EquipmentDTO(savedEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Met à jour un équipement existant (mise à jour partielle)
     * PUT /api/equipment/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id, @RequestBody EquipmentDTO dto) {
        try {
            Optional<Equipment> existingOpt = equipmentRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Equipment existing = existingOpt.get();
            
            // Mise à jour partielle - ne mettre à jour que les champs non-null du DTO
            if (dto.getName() != null) existing.setName(dto.getName());
            if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
            if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
            if (dto.getStatus() != null) {
                // Convertir le displayName en enum
                Equipment.Status status = Equipment.Status.fromDisplayName(dto.getStatus());
                if (status != null) existing.setStatus(status);
            }
            if (dto.getQrCode() != null) existing.setQrCode(dto.getQrCode());
            if (dto.getBrand() != null) existing.setBrand(dto.getBrand());
            if (dto.getModel() != null) existing.setModel(dto.getModel());
            if (dto.getSerialNumber() != null) existing.setSerialNumber(dto.getSerialNumber());
            if (dto.getPurchasePrice() != null) existing.setPurchasePrice(dto.getPurchasePrice());
            if (dto.getPurchaseDate() != null) existing.setPurchaseDate(dto.getPurchaseDate());
            if (dto.getLocation() != null) existing.setLocation(dto.getLocation());
            if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
            if (dto.getInternalReference() != null) existing.setInternalReference(dto.getInternalReference());
            if (dto.getWeight() != null) existing.setWeight(dto.getWeight());
            if (dto.getDimensions() != null) existing.setDimensions(dto.getDimensions());
            if (dto.getWarrantyExpiration() != null) existing.setWarrantyExpiration(dto.getWarrantyExpiration());
            if (dto.getSupplier() != null) existing.setSupplier(dto.getSupplier());
            if (dto.getInsuranceValue() != null) existing.setInsuranceValue(dto.getInsuranceValue());
            if (dto.getLastMaintenanceDate() != null) existing.setLastMaintenanceDate(dto.getLastMaintenanceDate());
            if (dto.getNextMaintenanceDate() != null) existing.setNextMaintenanceDate(dto.getNextMaintenanceDate());
            if (dto.getPhotoPath() != null) existing.setPhotoPath(dto.getPhotoPath());
            
            Equipment savedEquipment = equipmentRepository.save(existing);
            return ResponseEntity.ok(new EquipmentDTO(savedEquipment));
        } catch (Exception e) {
            System.err.println("Erreur mise à jour équipement " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Supprime un ÃƒÆ’Ã‚Â©quipement
     * DELETE /api/equipment/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            Optional<Equipment> equipment = equipmentRepository.findById(id);
            if (equipment.isPresent()) {
                // Supprimer les photos associées d'abord
                equipmentPhotoRepository.deleteByEquipmentId(id);
                // Puis supprimer l'équipement
                equipmentRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur suppression équipement " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Supprime tous les équipements n'appartenant pas à MAG SCENE
     * DELETE /api/equipment/cleanup/non-mag-scene
     */
    @DeleteMapping("/cleanup/non-mag-scene")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupNonMagSceneEquipment() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Equipment> allEquipment = equipmentRepository.findAll();
            int deletedCount = 0;
            int skippedCount = 0;
            
            for (Equipment eq : allEquipment) {
                String notes = eq.getNotes();
                boolean isMagScene = notes != null && notes.contains("MAG SCENE");
                
                if (!isMagScene) {
                    try {
                        // Dissocier les ServiceRequests de cet équipement
                        if (serviceRequestRepository != null) {
                            var serviceRequests = serviceRequestRepository.findByEquipmentId(eq.getId());
                            for (var sr : serviceRequests) {
                                sr.setEquipment(null);
                                serviceRequestRepository.save(sr);
                            }
                        }
                        // Dissocier les ContractItems de cet équipement
                        if (contractItemRepository != null) {
                            var contractItems = contractItemRepository.findByEquipmentId(eq.getId());
                            for (var ci : contractItems) {
                                ci.setEquipment(null);
                                contractItemRepository.save(ci);
                            }
                        }
                        // Supprimer les photos associées
                        equipmentPhotoRepository.deleteByEquipmentId(eq.getId());
                        // Puis supprimer l'équipement
                        equipmentRepository.delete(eq);
                        deletedCount++;
                        System.out.println("Supprimé équipement ID " + eq.getId() + ": " + eq.getName());
                    } catch (Exception e) {
                        System.err.println("Erreur suppression équipement " + eq.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        skippedCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("deleted", deletedCount);
            result.put("skipped", skippedCount);
            result.put("remaining", equipmentRepository.count());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}


