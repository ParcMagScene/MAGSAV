package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.service.BrandMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/brand-migration")
public class BrandMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(BrandMigrationController.class);
    
    @Autowired
    private BrandMigrationService brandMigrationService;
    
    /**
     * Génère un rapport de pré-migration pour voir les marques existantes
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getMigrationReport() {
        try {
            Map<String, Object> report = brandMigrationService.generateMigrationReport();
            logger.info("Rapport de migration généré avec succès");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport de migration: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Échec de la génération du rapport");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Migre toutes les marques vers la table Brand centralisée
     */
    @PostMapping("/migrate-brands")
    public ResponseEntity<Map<String, Object>> migrateBrands() {
        try {
            brandMigrationService.migrateAllBrands();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Migration des marques effectuée avec succès");
            
            logger.info("Migration des marques effectuée avec succès via API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la migration des marques: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Échec de la migration des marques");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Met à jour les références des équipements vers Brand
     */
    @PostMapping("/update-equipment-references")
    public ResponseEntity<Map<String, Object>> updateEquipmentReferences() {
        try {
            brandMigrationService.updateEquipmentBrandReferences();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mise à jour des références d'équipements effectuée avec succès");
            
            logger.info("Mise à jour des références d'équipements effectuée avec succès via API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des références d'équipements: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Échec de la mise à jour des références d'équipements");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Met à jour les références des véhicules vers Brand
     */
    @PostMapping("/update-vehicle-references")
    public ResponseEntity<Map<String, Object>> updateVehicleReferences() {
        try {
            brandMigrationService.updateVehicleBrandReferences();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mise à jour des références de véhicules effectuée avec succès");
            
            logger.info("Mise à jour des références de véhicules effectuée avec succès via API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des références de véhicules: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Échec de la mise à jour des références de véhicules");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Exécute la migration complète (marques + références)
     */
    @PostMapping("/full-migration")
    public ResponseEntity<Map<String, Object>> executeFullMigration() {
        try {
            brandMigrationService.executeFullMigration();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Migration complète effectuée avec succès");
            
            logger.info("Migration complète effectuée avec succès via API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la migration complète: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Échec de la migration complète");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Endpoint de statut pour vérifier l'état de la migration
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        try {
            Map<String, Object> report = brandMigrationService.generateMigrationReport();
            
            Map<String, Object> status = new HashMap<>();
            status.put("isReady", true);
            status.put("totalBrandsToMigrate", report.get("totalUniqueBrands"));
            status.put("existingBrandsCount", report.get("existingBrandCount"));
            status.put("equipmentBrandsCount", report.get("equipmentBrandCount"));
            status.put("vehicleBrandsCount", report.get("vehicleBrandCount"));
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du statut de migration: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("isReady", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}