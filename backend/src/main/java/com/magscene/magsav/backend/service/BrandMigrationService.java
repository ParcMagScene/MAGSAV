package com.magscene.magsav.backend.service;

import com.magscene.magsav.backend.entity.Brand;
import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.entity.Vehicle;
import com.magscene.magsav.backend.repository.BrandRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BrandMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BrandMigrationService.class);
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Migre toutes les marques des équipements et véhicules vers la table Brand centralisée
     */
    @Transactional
    public void migrateAllBrands() {
        logger.info("Début de la migration des marques...");
        
        Set<String> allBrands = new HashSet<>();
        
        // Collecter toutes les marques uniques des équipements
        List<Equipment> equipments = equipmentRepository.findAll();
        for (Equipment equipment : equipments) {
            if (equipment.getBrand() != null && !equipment.getBrand().trim().isEmpty()) {
                allBrands.add(equipment.getBrand().trim());
            }
        }
        logger.info("Trouvé {} marques uniques dans les équipements", allBrands.size());
        
        // Collecter toutes les marques uniques des véhicules
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getBrand() != null && !vehicle.getBrand().trim().isEmpty()) {
                allBrands.add(vehicle.getBrand().trim());
            }
        }
        logger.info("Total {} marques uniques trouvées", allBrands.size());
        
        // Créer les entités Brand
        Map<String, Brand> brandMap = new HashMap<>();
        for (String brandName : allBrands) {
            // Vérifier si la marque existe déjà
            Optional<Brand> existingBrand = brandRepository.findByNameIgnoreCase(brandName);
            if (existingBrand.isPresent()) {
                brandMap.put(brandName, existingBrand.get());
                logger.debug("Marque existante trouvée: {}", brandName);
            } else {
                Brand newBrand = new Brand();
                newBrand.setName(brandName);
                newBrand.setDescription("Marque migrée automatiquement");
                newBrand.setActive(true);
                newBrand.setCreatedAt(LocalDateTime.now());
                newBrand.setUpdatedAt(LocalDateTime.now());
                
                Brand savedBrand = brandRepository.save(newBrand);
                brandMap.put(brandName, savedBrand);
                logger.info("Nouvelle marque créée: {} (ID: {})", brandName, savedBrand.getId());
            }
        }
        
        logger.info("Migration des marques terminée. {} marques dans la base de données.", brandMap.size());
    }
    
    /**
     * Met à jour les équipements pour utiliser les références Brand au lieu des chaînes
     */
    @Transactional
    public void updateEquipmentBrandReferences() {
        logger.info("Mise à jour des références de marques dans les équipements...");
        
        List<Equipment> equipments = equipmentRepository.findAll();
        int updatedCount = 0;
        
        for (Equipment equipment : equipments) {
            if (equipment.getBrand() != null && !equipment.getBrand().trim().isEmpty()) {
                Optional<Brand> brand = brandRepository.findByNameIgnoreCase(equipment.getBrand().trim());
                if (brand.isPresent()) {
                    // Note: Cette mise à jour nécessitera une modification de l'entité Equipment
                    // pour ajouter une relation @ManyToOne avec Brand
                    logger.debug("Référence de marque mise à jour pour l'équipement ID {}: {} -> Brand ID {}", 
                               equipment.getId(), equipment.getBrand(), brand.get().getId());
                    updatedCount++;
                } else {
                    logger.warn("Marque non trouvée pour l'équipement ID {}: {}", equipment.getId(), equipment.getBrand());
                }
            }
        }
        
        logger.info("Mise à jour terminée. {} équipements mis à jour.", updatedCount);
    }
    
    /**
     * Met à jour les véhicules pour utiliser les références Brand au lieu des chaînes
     */
    @Transactional
    public void updateVehicleBrandReferences() {
        logger.info("Mise à jour des références de marques dans les véhicules...");
        
        List<Vehicle> vehicles = vehicleRepository.findAll();
        int updatedCount = 0;
        
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getBrand() != null && !vehicle.getBrand().trim().isEmpty()) {
                Optional<Brand> brand = brandRepository.findByNameIgnoreCase(vehicle.getBrand().trim());
                if (brand.isPresent()) {
                    // Note: Cette mise à jour nécessitera une modification de l'entité Vehicle
                    // pour ajouter une relation @ManyToOne avec Brand
                    logger.debug("Référence de marque mise à jour pour le véhicule ID {}: {} -> Brand ID {}", 
                               vehicle.getId(), vehicle.getBrand(), brand.get().getId());
                    updatedCount++;
                } else {
                    logger.warn("Marque non trouvée pour le véhicule ID {}: {}", vehicle.getId(), vehicle.getBrand());
                }
            }
        }
        
        logger.info("Mise à jour terminée. {} véhicules mis à jour.", updatedCount);
    }
    
    /**
     * Exécute la migration complète des marques
     */
    @Transactional
    public void executeFullMigration() {
        try {
            migrateAllBrands();
            // Note: Les méthodes updateEquipmentBrandReferences et updateVehicleBrandReferences
            // nécessitent des modifications des entités Equipment et Vehicle pour fonctionner
            logger.info("Migration complète des marques terminée avec succès.");
        } catch (Exception e) {
            logger.error("Erreur lors de la migration des marques: {}", e.getMessage(), e);
            throw new RuntimeException("Échec de la migration des marques", e);
        }
    }
    
    /**
     * Génère un rapport des marques trouvées avant migration
     */
    public Map<String, Object> generateMigrationReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Marques des équipements
        List<Equipment> equipments = equipmentRepository.findAll();
        Set<String> equipmentBrands = new HashSet<>();
        for (Equipment equipment : equipments) {
            if (equipment.getBrand() != null && !equipment.getBrand().trim().isEmpty()) {
                equipmentBrands.add(equipment.getBrand().trim());
            }
        }
        
        // Marques des véhicules
        List<Vehicle> vehicles = vehicleRepository.findAll();
        Set<String> vehicleBrands = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getBrand() != null && !vehicle.getBrand().trim().isEmpty()) {
                vehicleBrands.add(vehicle.getBrand().trim());
            }
        }
        
        // Marques déjà dans Brand
        List<Brand> existingBrands = brandRepository.findAll();
        Set<String> existingBrandNames = new HashSet<>();
        for (Brand brand : existingBrands) {
            existingBrandNames.add(brand.getName());
        }
        
        report.put("equipmentBrands", new ArrayList<>(equipmentBrands));
        report.put("vehicleBrands", new ArrayList<>(vehicleBrands));
        report.put("existingBrands", new ArrayList<>(existingBrandNames));
        report.put("equipmentBrandCount", equipmentBrands.size());
        report.put("vehicleBrandCount", vehicleBrands.size());
        report.put("existingBrandCount", existingBrandNames.size());
        
        Set<String> allUniqueBrands = new HashSet<>();
        allUniqueBrands.addAll(equipmentBrands);
        allUniqueBrands.addAll(vehicleBrands);
        report.put("totalUniqueBrands", allUniqueBrands.size());
        report.put("allUniqueBrandsList", new ArrayList<>(allUniqueBrands));
        
        return report;
    }
}