package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Repair;
import com.magscene.magsav.backend.entity.Repair.RepairStatus;
import com.magscene.magsav.backend.entity.Repair.RepairPriority;
import com.magscene.magsav.backend.repository.RepairRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = "*")
@Tag(name = "Repairs", description = "API de gestion des réparations SAV")
public class RepairController {

    private static final Logger logger = LoggerFactory.getLogger(RepairController.class);

    @Autowired
    private RepairRepository repairRepository;

    /**
     * Récupérer toutes les réparations
     */
    @GetMapping
    @Operation(summary = "Récupérer toutes les réparations", description = "Liste complète des réparations")
    public ResponseEntity<List<Repair>> getAllRepairs() {
        try {
            List<Repair> repairs = repairRepository.findAll();
            logger.info("✅ {} réparations récupérées", repairs.size());
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des réparations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer une réparation par ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réparation", description = "Récupère une réparation par son ID")
    public ResponseEntity<Repair> getRepairById(@PathVariable Long id) {
        try {
            return repairRepository.findById(id)
                    .map(repair -> {
                        logger.info("✅ Réparation {} récupérée", repair.getRepairNumber());
                        return ResponseEntity.ok(repair);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de la réparation {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Créer une nouvelle réparation
     */
    @PostMapping
    @Operation(summary = "Créer une réparation", description = "Crée une nouvelle réparation")
    public ResponseEntity<Repair> createRepair(@Valid @RequestBody Repair repair) {
        try {
            // Générer un numéro de réparation si non fourni
            if (repair.getRepairNumber() == null || repair.getRepairNumber().isEmpty()) {
                repair.setRepairNumber("REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }

            Repair savedRepair = repairRepository.save(repair);
            logger.info("✅ Réparation {} créée", savedRepair.getRepairNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRepair);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de la réparation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mettre à jour une réparation
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une réparation", description = "Met à jour une réparation existante")
    public ResponseEntity<Repair> updateRepair(@PathVariable Long id, @Valid @RequestBody Repair repairDetails) {
        try {
            return repairRepository.findById(id)
                    .map(repair -> {
                        repair.setEquipmentName(repairDetails.getEquipmentName());
                        repair.setEquipmentSerialNumber(repairDetails.getEquipmentSerialNumber());
                        repair.setStatus(repairDetails.getStatus());
                        repair.setPriority(repairDetails.getPriority());
                        repair.setProblemDescription(repairDetails.getProblemDescription());
                        repair.setDiagnosis(repairDetails.getDiagnosis());
                        repair.setSolution(repairDetails.getSolution());
                        repair.setTechnicianName(repairDetails.getTechnicianName());
                        repair.setCustomerName(repairDetails.getCustomerName());
                        repair.setCustomerContact(repairDetails.getCustomerContact());
                        repair.setEstimatedCost(repairDetails.getEstimatedCost());
                        repair.setActualCost(repairDetails.getActualCost());
                        repair.setEstimatedDurationHours(repairDetails.getEstimatedDurationHours());
                        repair.setActualDurationHours(repairDetails.getActualDurationHours());
                        repair.setStartDate(repairDetails.getStartDate());
                        repair.setCompletionDate(repairDetails.getCompletionDate());
                        repair.setPartsNeeded(repairDetails.getPartsNeeded());
                        repair.setWarrantyCovered(repairDetails.getWarrantyCovered());
                        repair.setNotes(repairDetails.getNotes());

                        Repair updatedRepair = repairRepository.save(repair);
                        logger.info("✅ Réparation {} mise à jour", updatedRepair.getRepairNumber());
                        return ResponseEntity.ok(updatedRepair);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de la réparation {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprimer une réparation
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réparation", description = "Supprime une réparation par son ID")
    public ResponseEntity<Void> deleteRepair(@PathVariable Long id) {
        try {
            return repairRepository.findById(id)
                    .map(repair -> {
                        repairRepository.deleteById(id);
                        logger.info("✅ Réparation {} supprimée", repair.getRepairNumber());
                        return ResponseEntity.noContent().<Void>build();
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression de la réparation {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rechercher des réparations par mot-clé
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des réparations", description = "Recherche par équipement, numéro ou client")
    public ResponseEntity<List<Repair>> searchRepairs(@RequestParam String keyword) {
        try {
            List<Repair> repairs = repairRepository.searchByKeyword(keyword);
            logger.info("✅ {} réparations trouvées pour '{}'", repairs.size(), keyword);
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par statut
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut", description = "Récupère les réparations par statut")
    public ResponseEntity<List<Repair>> getRepairsByStatus(@PathVariable RepairStatus status) {
        try {
            List<Repair> repairs = repairRepository.findByStatus(status);
            logger.info("✅ {} réparations avec statut {}", repairs.size(), status);
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par priorité
     */
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Filtrer par priorité", description = "Récupère les réparations par priorité")
    public ResponseEntity<List<Repair>> getRepairsByPriority(@PathVariable RepairPriority priority) {
        try {
            List<Repair> repairs = repairRepository.findByPriority(priority);
            logger.info("✅ {} réparations avec priorité {}", repairs.size(), priority);
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par priorité: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par technicien
     */
    @GetMapping("/technician/{technician}")
    @Operation(summary = "Filtrer par technicien", description = "Récupère les réparations d'un technicien")
    public ResponseEntity<List<Repair>> getRepairsByTechnician(@PathVariable String technician) {
        try {
            List<Repair> repairs = repairRepository.findByTechnicianName(technician);
            logger.info("✅ {} réparations pour le technicien {}", repairs.size(), technician);
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par technicien: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par période
     */
    @GetMapping("/period")
    @Operation(summary = "Filtrer par période", description = "Récupère les réparations d'une période")
    public ResponseEntity<List<Repair>> getRepairsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Repair> repairs = repairRepository.findByRequestDateBetween(startDate, endDate);
            logger.info("✅ {} réparations entre {} et {}", repairs.size(), startDate, endDate);
            return ResponseEntity.ok(repairs);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par période: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Statistiques des réparations
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des réparations", description = "Récupère les statistiques globales")
    public ResponseEntity<Map<String, Object>> getRepairStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", repairRepository.count());
            stats.put("initiated", repairRepository.countByStatus(RepairStatus.INITIATED));
            stats.put("inProgress", repairRepository.countByStatus(RepairStatus.IN_PROGRESS));
            stats.put("completed", repairRepository.countByStatus(RepairStatus.COMPLETED));
            stats.put("cancelled", repairRepository.countByStatus(RepairStatus.CANCELLED));
            stats.put("averageDuration", repairRepository.getAverageDuration());
            stats.put("averageCost", repairRepository.getAverageCost());

            logger.info("✅ Statistiques des réparations générées");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du calcul des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assigner un technicien
     */
    @PutMapping("/{id}/assign")
    @Operation(summary = "Assigner un technicien", description = "Assigne un technicien à une réparation")
    public ResponseEntity<Repair> assignTechnician(@PathVariable Long id, @RequestParam String technicianName) {
        try {
            return repairRepository.findById(id)
                    .map(repair -> {
                        repair.setTechnicianName(technicianName);
                        if (repair.getStatus() == RepairStatus.INITIATED) {
                            repair.setStatus(RepairStatus.DIAGNOSED);
                            repair.setStartDate(LocalDate.now());
                        }
                        Repair updatedRepair = repairRepository.save(repair);
                        logger.info("✅ Technicien {} assigné à la réparation {}", technicianName,
                                repair.getRepairNumber());
                        return ResponseEntity.ok(updatedRepair);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'assignation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marquer comme terminée
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "Marquer comme terminée", description = "Marque une réparation comme terminée")
    public ResponseEntity<Repair> completeRepair(@PathVariable Long id) {
        try {
            return repairRepository.findById(id)
                    .map(repair -> {
                        repair.setStatus(RepairStatus.COMPLETED);
                        repair.setCompletionDate(LocalDate.now());
                        Repair updatedRepair = repairRepository.save(repair);
                        logger.info("✅ Réparation {} marquée comme terminée", repair.getRepairNumber());
                        return ResponseEntity.ok(updatedRepair);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la complétion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
