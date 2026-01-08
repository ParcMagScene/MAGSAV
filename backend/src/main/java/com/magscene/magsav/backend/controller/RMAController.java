package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.RMA;
import com.magscene.magsav.backend.entity.RMA.RMAStatus;
import com.magscene.magsav.backend.entity.RMA.RMAReasonType;
import com.magscene.magsav.backend.entity.RMA.RMAPriority;
import com.magscene.magsav.backend.repository.RMARepository;
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
@RequestMapping("/api/rmas")
@CrossOrigin(origins = "*")
@Tag(name = "RMAs", description = "API de gestion des RMA (Return Merchandise Authorization)")
public class RMAController {

    private static final Logger logger = LoggerFactory.getLogger(RMAController.class);

    @Autowired
    private RMARepository rmaRepository;

    /**
     * Récupérer tous les RMA
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les RMA", description = "Liste complète des RMA")
    public ResponseEntity<List<RMA>> getAllRMAs() {
        try {
            List<RMA> rmas = rmaRepository.findAll();
            logger.info("✅ {} RMA récupérés", rmas.size());
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des RMA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer un RMA par ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un RMA", description = "Récupère un RMA par son ID")
    public ResponseEntity<RMA> getRMAById(@PathVariable Long id) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        logger.info("✅ RMA {} récupéré", rma.getRmaNumber());
                        return ResponseEntity.ok(rma);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du RMA {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Créer un nouveau RMA
     */
    @PostMapping
    @Operation(summary = "Créer un RMA", description = "Crée un nouveau RMA")
    public ResponseEntity<RMA> createRMA(@Valid @RequestBody RMA rma) {
        try {
            // Générer un numéro RMA si non fourni
            if (rma.getRmaNumber() == null || rma.getRmaNumber().isEmpty()) {
                rma.setRmaNumber("RMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }

            RMA savedRMA = rmaRepository.save(rma);
            logger.info("✅ RMA {} créé", savedRMA.getRmaNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRMA);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création du RMA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mettre à jour un RMA
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un RMA", description = "Met à jour un RMA existant")
    public ResponseEntity<RMA> updateRMA(@PathVariable Long id, @Valid @RequestBody RMA rmaDetails) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        rma.setEquipmentName(rmaDetails.getEquipmentName());
                        rma.setEquipmentSerialNumber(rmaDetails.getEquipmentSerialNumber());
                        rma.setReason(rmaDetails.getReason());
                        rma.setStatus(rmaDetails.getStatus());
                        rma.setPriority(rmaDetails.getPriority());
                        rma.setDescription(rmaDetails.getDescription());
                        rma.setCustomerName(rmaDetails.getCustomerName());
                        rma.setCustomerContact(rmaDetails.getCustomerContact());
                        rma.setCustomerEmail(rmaDetails.getCustomerEmail());
                        rma.setShippingAddress(rmaDetails.getShippingAddress());
                        rma.setTrackingNumber(rmaDetails.getTrackingNumber());
                        rma.setCarrier(rmaDetails.getCarrier());
                        rma.setApprovalDate(rmaDetails.getApprovalDate());
                        rma.setShipmentDate(rmaDetails.getShipmentDate());
                        rma.setReturnDate(rmaDetails.getReturnDate());
                        rma.setCompletionDate(rmaDetails.getCompletionDate());
                        rma.setEstimatedValue(rmaDetails.getEstimatedValue());
                        rma.setShippingCost(rmaDetails.getShippingCost());
                        rma.setRepairCost(rmaDetails.getRepairCost());
                        rma.setRefundAmount(rmaDetails.getRefundAmount());
                        rma.setReplacementEquipment(rmaDetails.getReplacementEquipment());
                        rma.setAnalysisNotes(rmaDetails.getAnalysisNotes());
                        rma.setResolutionNotes(rmaDetails.getResolutionNotes());

                        RMA updatedRMA = rmaRepository.save(rma);
                        logger.info("✅ RMA {} mis à jour", updatedRMA.getRmaNumber());
                        return ResponseEntity.ok(updatedRMA);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour du RMA {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprimer un RMA
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un RMA", description = "Supprime un RMA par son ID")
    public ResponseEntity<Void> deleteRMA(@PathVariable Long id) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        rmaRepository.deleteById(id);
                        logger.info("✅ RMA {} supprimé", rma.getRmaNumber());
                        return ResponseEntity.noContent().<Void>build();
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression du RMA {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rechercher des RMA par mot-clé
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des RMA", description = "Recherche par équipement, numéro ou client")
    public ResponseEntity<List<RMA>> searchRMAs(@RequestParam String keyword) {
        try {
            List<RMA> rmas = rmaRepository.searchByKeyword(keyword);
            logger.info("✅ {} RMA trouvés pour '{}'", rmas.size(), keyword);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par statut
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut", description = "Récupère les RMA par statut")
    public ResponseEntity<List<RMA>> getRMAsByStatus(@PathVariable RMAStatus status) {
        try {
            List<RMA> rmas = rmaRepository.findByStatus(status);
            logger.info("✅ {} RMA avec statut {}", rmas.size(), status);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par motif
     */
    @GetMapping("/reason/{reason}")
    @Operation(summary = "Filtrer par motif", description = "Récupère les RMA par motif")
    public ResponseEntity<List<RMA>> getRMAsByReason(@PathVariable RMAReasonType reason) {
        try {
            List<RMA> rmas = rmaRepository.findByReason(reason);
            logger.info("✅ {} RMA avec motif {}", rmas.size(), reason);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par motif: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par priorité
     */
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Filtrer par priorité", description = "Récupère les RMA par priorité")
    public ResponseEntity<List<RMA>> getRMAsByPriority(@PathVariable RMAPriority priority) {
        try {
            List<RMA> rmas = rmaRepository.findByPriority(priority);
            logger.info("✅ {} RMA avec priorité {}", rmas.size(), priority);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par priorité: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Filtrer par période
     */
    @GetMapping("/period")
    @Operation(summary = "Filtrer par période", description = "Récupère les RMA d'une période")
    public ResponseEntity<List<RMA>> getRMAsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<RMA> rmas = rmaRepository.findByRequestDateBetween(startDate, endDate);
            logger.info("✅ {} RMA entre {} et {}", rmas.size(), startDate, endDate);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par période: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Tracking par numéro de suivi
     */
    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Tracking RMA", description = "Suivi d'un RMA par numéro de tracking")
    public ResponseEntity<List<RMA>> getRMAsByTracking(@PathVariable String trackingNumber) {
        try {
            List<RMA> rmas = rmaRepository.findByTrackingNumber(trackingNumber);
            logger.info("✅ {} RMA trouvés pour tracking {}", rmas.size(), trackingNumber);
            return ResponseEntity.ok(rmas);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du tracking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Statistiques des RMA
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des RMA", description = "Récupère les statistiques globales")
    public ResponseEntity<Map<String, Object>> getRMAStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", rmaRepository.count());
            stats.put("initiated", rmaRepository.countByStatus(RMAStatus.INITIATED));
            stats.put("authorized", rmaRepository.countByStatus(RMAStatus.AUTHORIZED));
            stats.put("inTransit", rmaRepository.countByStatus(RMAStatus.IN_TRANSIT_RETURN));
            stats.put("received", rmaRepository.countByStatus(RMAStatus.RECEIVED));
            stats.put("underAnalysis", rmaRepository.countByStatus(RMAStatus.UNDER_ANALYSIS));
            stats.put("completed", rmaRepository.countByStatus(RMAStatus.COMPLETED));
            stats.put("rejected", rmaRepository.countByStatus(RMAStatus.REJECTED));
            stats.put("totalValueCompleted", rmaRepository.getTotalValueCompleted());
            stats.put("averageRefundAmount", rmaRepository.getAverageRefundAmount());

            logger.info("✅ Statistiques des RMA générées");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du calcul des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Autoriser un RMA
     */
    @PutMapping("/{id}/authorize")
    @Operation(summary = "Autoriser un RMA", description = "Autorise un RMA")
    public ResponseEntity<RMA> authorizeRMA(@PathVariable Long id) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        rma.setStatus(RMAStatus.AUTHORIZED);
                        rma.setApprovalDate(LocalDate.now());
                        RMA updatedRMA = rmaRepository.save(rma);
                        logger.info("✅ RMA {} autorisé", rma.getRmaNumber());
                        return ResponseEntity.ok(updatedRMA);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'autorisation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marquer comme reçu
     */
    @PutMapping("/{id}/receive")
    @Operation(summary = "Marquer comme reçu", description = "Marque un RMA comme reçu")
    public ResponseEntity<RMA> receiveRMA(@PathVariable Long id) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        rma.setStatus(RMAStatus.RECEIVED);
                        rma.setReturnDate(LocalDate.now());
                        RMA updatedRMA = rmaRepository.save(rma);
                        logger.info("✅ RMA {} marqué comme reçu", rma.getRmaNumber());
                        return ResponseEntity.ok(updatedRMA);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marquer comme terminé
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "Marquer comme terminé", description = "Marque un RMA comme terminé")
    public ResponseEntity<RMA> completeRMA(@PathVariable Long id) {
        try {
            return rmaRepository.findById(id)
                    .map(rma -> {
                        rma.setStatus(RMAStatus.COMPLETED);
                        rma.setCompletionDate(LocalDate.now());
                        RMA updatedRMA = rmaRepository.save(rma);
                        logger.info("✅ RMA {} marqué comme terminé", rma.getRmaNumber());
                        return ResponseEntity.ok(updatedRMA);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la complétion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
