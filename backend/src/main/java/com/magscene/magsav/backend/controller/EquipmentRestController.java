package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.dto.EquipmentDTO;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * Met ÃƒÆ’Ã‚Â  jour un ÃƒÆ’Ã‚Â©quipement existant
     * PUT /api/equipment/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        try {
            Optional<Equipment> existingEquipment = equipmentRepository.findById(id);
            if (existingEquipment.isPresent()) {
                equipment.setId(id);
                Equipment savedEquipment = equipmentRepository.save(equipment);
                return ResponseEntity.ok(new EquipmentDTO(savedEquipment));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Supprime un ÃƒÆ’Ã‚Â©quipement
     * DELETE /api/equipment/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            Optional<Equipment> equipment = equipmentRepository.findById(id);
            if (equipment.isPresent()) {
                equipmentRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


