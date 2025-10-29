package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Vehicle;
import com.magscene.magsav.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des vÃƒÂ©hicules
 */
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re tous les vÃƒÂ©hicules
     */
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re un vÃƒÂ©hicule par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        return vehicle.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * CrÃƒÂ©e un nouveau vÃƒÂ©hicule
     */
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        try {
            // VÃƒÂ©rifier l'unicitÃƒÂ© de la plaque d'immatriculation
            if (vehicle.getLicensePlate() != null && 
                vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            // VÃƒÂ©rifier l'unicitÃƒÂ© du VIN
            if (vehicle.getVin() != null && 
                vehicleRepository.findByVin(vehicle.getVin()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Met ÃƒÂ  jour un vÃƒÂ©hicule existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, 
                                               @Valid @RequestBody Vehicle vehicleDetails) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
        if (optionalVehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Vehicle vehicle = optionalVehicle.get();
        
        // VÃƒÂ©rifier l'unicitÃƒÂ© de la plaque si elle a changÃƒÂ©
        if (vehicleDetails.getLicensePlate() != null && 
            !vehicleDetails.getLicensePlate().equals(vehicle.getLicensePlate())) {
            Optional<Vehicle> existingWithPlate = vehicleRepository.findByLicensePlate(vehicleDetails.getLicensePlate());
            if (existingWithPlate.isPresent() && !existingWithPlate.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        
        // VÃƒÂ©rifier l'unicitÃƒÂ© du VIN si il a changÃƒÂ©
        if (vehicleDetails.getVin() != null && 
            !vehicleDetails.getVin().equals(vehicle.getVin())) {
            Optional<Vehicle> existingWithVin = vehicleRepository.findByVin(vehicleDetails.getVin());
            if (existingWithVin.isPresent() && !existingWithVin.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        
        // Mise ÃƒÂ  jour des champs
        vehicle.setName(vehicleDetails.getName());
        vehicle.setBrand(vehicleDetails.getBrand());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setVin(vehicleDetails.getVin());
        vehicle.setType(vehicleDetails.getType());
        vehicle.setStatus(vehicleDetails.getStatus());
        vehicle.setFuelType(vehicleDetails.getFuelType());
        vehicle.setYearManufactured(vehicleDetails.getYearManufactured());
        vehicle.setMileage(vehicleDetails.getMileage());
        vehicle.setMaxPayload(vehicleDetails.getMaxPayload());
        vehicle.setDimensions(vehicleDetails.getDimensions());
        vehicle.setInsuranceNumber(vehicleDetails.getInsuranceNumber());
        vehicle.setInsuranceExpiration(vehicleDetails.getInsuranceExpiration());
        vehicle.setTechnicalControlExpiration(vehicleDetails.getTechnicalControlExpiration());
        vehicle.setLastMaintenanceDate(vehicleDetails.getLastMaintenanceDate());
        vehicle.setNextMaintenanceDate(vehicleDetails.getNextMaintenanceDate());
        vehicle.setMaintenanceIntervalKm(vehicleDetails.getMaintenanceIntervalKm());
        vehicle.setPurchaseDate(vehicleDetails.getPurchaseDate());
        vehicle.setPurchasePrice(vehicleDetails.getPurchasePrice());
        vehicle.setDailyRentalRate(vehicleDetails.getDailyRentalRate());
        vehicle.setCurrentLocation(vehicleDetails.getCurrentLocation());
        vehicle.setAssignedDriver(vehicleDetails.getAssignedDriver());
        vehicle.setNotes(vehicleDetails.getNotes());
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(updatedVehicle);
    }
    
    /**
     * Supprime un vÃƒÂ©hicule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Recherche de vÃƒÂ©hicules
     */
    @GetMapping("/search")
    public List<Vehicle> searchVehicles(@RequestParam String query) {
        return vehicleRepository.searchVehicles(query);
    }
    
    /**
     * Recherche par type
     */
    @GetMapping("/type/{type}")
    public List<Vehicle> getVehiclesByType(@PathVariable Vehicle.VehicleType type) {
        return vehicleRepository.findByType(type);
    }
    
    /**
     * Recherche par statut
     */
    @GetMapping("/status/{status}")
    public List<Vehicle> getVehiclesByStatus(@PathVariable Vehicle.VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }
    
    /**
     * VÃƒÂ©hicules disponibles
     */
    @GetMapping("/available")
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles();
    }
    
    /**
     * VÃƒÂ©hicules nÃƒÂ©cessitant une maintenance
     */
    @GetMapping("/maintenance-needed")
    public List<Vehicle> getVehiclesNeedingMaintenance() {
        LocalDate alertDate = LocalDate.now().plusDays(30); // Alerte 30 jours ÃƒÂ  l'avance
        return vehicleRepository.findVehiclesNeedingMaintenance(alertDate);
    }
    
    /**
     * VÃƒÂ©hicules avec documents expirÃƒÂ©s
     */
    @GetMapping("/expired-documents")
    public List<Vehicle> getVehiclesWithExpiredDocuments() {
        LocalDate today = LocalDate.now();
        return vehicleRepository.findVehiclesWithExpiredDocuments(today);
    }
    
    /**
     * VÃƒÂ©hicules en maintenance
     */
    @GetMapping("/in-maintenance")
    public List<Vehicle> getVehiclesInMaintenance() {
        return vehicleRepository.findVehiclesInMaintenance();
    }
    
    /**
     * VÃƒÂ©hicules disponibles pour location externe
     */
    @GetMapping("/available-for-rental")
    public List<Vehicle> getAvailableForRental() {
        return vehicleRepository.findAvailableForRental();
    }
    
    /**
     * Statistiques des vÃƒÂ©hicules
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getVehicleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques par statut
        List<Object[]> statusCounts = vehicleRepository.countVehiclesByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", statusStats);
        
        // Statistiques par type
        List<Object[]> typeCounts = vehicleRepository.countVehiclesByType();
        Map<String, Long> typeStats = new HashMap<>();
        for (Object[] row : typeCounts) {
            typeStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeStats);
        
        // Nombres totaux
        long total = vehicleRepository.count();
        long available = vehicleRepository.findAvailableVehicles().size();
        long inMaintenance = vehicleRepository.findVehiclesInMaintenance().size();
        long needingMaintenance = getVehiclesNeedingMaintenance().size();
        long expiredDocuments = getVehiclesWithExpiredDocuments().size();
        
        stats.put("total", total);
        stats.put("available", available);
        stats.put("inMaintenance", inMaintenance);
        stats.put("needingMaintenance", needingMaintenance);
        stats.put("expiredDocuments", expiredDocuments);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Met ÃƒÂ  jour le statut d'un vÃƒÂ©hicule
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Vehicle> updateVehicleStatus(@PathVariable Long id, 
                                                     @RequestBody Map<String, String> payload) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
        if (optionalVehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Vehicle vehicle = optionalVehicle.get();
        String statusStr = payload.get("status");
        
        try {
            Vehicle.VehicleStatus newStatus = Vehicle.VehicleStatus.valueOf(statusStr);
            vehicle.setStatus(newStatus);
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Met ÃƒÂ  jour le kilomÃƒÂ©trage d'un vÃƒÂ©hicule
     */
    @PatchMapping("/{id}/mileage")
    public ResponseEntity<Vehicle> updateVehicleMileage(@PathVariable Long id, 
                                                      @RequestBody Map<String, Integer> payload) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
        if (optionalVehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Vehicle vehicle = optionalVehicle.get();
        Integer newMileage = payload.get("mileage");
        
        if (newMileage != null && newMileage >= 0) {
            vehicle.setMileage(newMileage);
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        }
        
        return ResponseEntity.badRequest().build();
    }
}

