package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Vehicle;
import com.magscene.magsav.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
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

    @PersistenceContext
    private EntityManager entityManager;

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
            Optional<Vehicle> existingWithPlate = vehicleRepository
                    .findByLicensePlate(vehicleDetails.getLicensePlate());
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
        vehicle.setColor(vehicleDetails.getColor());
        vehicle.setOwner(vehicleDetails.getOwner());

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

    /**
     * Migration du schéma pour les nouveaux types de véhicules
     */
    @PostMapping("/migrate-schema")
    @Transactional
    public ResponseEntity<Map<String, Object>> migrateSchema() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Liste des contraintes CHECK à supprimer (noms possibles générés par H2)
            String[] constraintNames = {
                    "CONSTRAINT_3D", "CONSTRAINT_3D1", "CONSTRAINT_3D2", "CONSTRAINT_3D3",
                    "VEHICLES_TYPE_CHECK", "CK_VEHICLES_TYPE"
            };

            int dropped = 0;
            for (String constraintName : constraintNames) {
                try {
                    entityManager.createNativeQuery(
                            "ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS \"" + constraintName + "\"")
                            .executeUpdate();
                    dropped++;
                } catch (Exception e) {
                    // Contrainte n'existe pas, on continue
                }
            }

            // Supprimer toutes les contraintes CHECK restantes
            try {
                entityManager.createNativeQuery(
                        "ALTER TABLE vehicles ALTER COLUMN type VARCHAR(255)").executeUpdate();
            } catch (Exception e) {
                System.out.println("Modification colonne type: " + e.getMessage());
            }

            result.put("success", true);
            result.put("message", "Migration terminée, contraintes supprimées: " + dropped);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Supprime tous les véhicules (pour import)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllVehicles() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = vehicleRepository.count();
            vehicleRepository.deleteAll();
            result.put("success", true);
            result.put("deleted", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Import en masse de véhicules
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importVehicles(@RequestBody List<Map<String, Object>> vehiclesData) {
        Map<String, Object> result = new HashMap<>();
        int imported = 0;
        int skipped = 0;

        System.out.println("Import véhicules - Reçu " + vehiclesData.size() + " éléments");

        for (Map<String, Object> data : vehiclesData) {
            try {
                System.out.println("Data reçue: " + data);
                Object nameObj = data.get("name");
                String name = nameObj != null ? nameObj.toString() : null;
                System.out.println("Name extrait: '" + name + "'");
                if (name == null || name.trim().isEmpty()) {
                    System.out.println("Name vide ou null, skip");
                    skipped++;
                    continue;
                }

                Vehicle vehicle = new Vehicle();
                vehicle.setName(name.trim());

                Object brandObj = data.get("brand");
                String brandStr = brandObj != null ? brandObj.toString().trim() : null;
                vehicle.setBrand(brandStr != null && !brandStr.isEmpty() ? brandStr : null);

                Object modelObj = data.get("model");
                String modelStr = modelObj != null ? modelObj.toString().trim() : null;
                vehicle.setModel(modelStr != null && !modelStr.isEmpty() ? modelStr : null);

                Object licensePlateObj = data.get("licensePlate");
                String licensePlateStr = licensePlateObj != null ? licensePlateObj.toString().trim() : null;
                vehicle.setLicensePlate(licensePlateStr != null && !licensePlateStr.isEmpty() ? licensePlateStr : null);

                Object colorObj = data.get("color");
                String colorStr = colorObj != null ? colorObj.toString().trim() : null;
                vehicle.setColor(colorStr != null && !colorStr.isEmpty() ? colorStr : null);

                Object ownerObj = data.get("owner");
                String ownerStr = ownerObj != null ? ownerObj.toString().trim() : null;
                vehicle.setOwner(ownerStr != null && !ownerStr.isEmpty() ? ownerStr : null);

                Object notesObj = data.get("notes");
                String notesStr = notesObj != null ? notesObj.toString().trim() : null;
                vehicle.setNotes(notesStr != null && !notesStr.isEmpty() ? notesStr : null);

                // Type de véhicule
                Object typeObj = data.get("type");
                String typeStr = typeObj != null ? typeObj.toString() : null;
                if (typeStr != null && !typeStr.trim().isEmpty()) {
                    vehicle.setType(Vehicle.VehicleType.fromDisplayName(typeStr));
                }

                // Statut par défaut
                vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);

                vehicleRepository.save(vehicle);
                System.out.println("Véhicule importé: " + vehicle.getName());
                imported++;
            } catch (Exception e) {
                System.err.println("Erreur import véhicule: " + e.getMessage());
                e.printStackTrace();
                skipped++;
            }
        }

        result.put("success", true);
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("total", vehicleRepository.count());

        return ResponseEntity.ok(result);
    }

    /**
     * Vérifier la disponibilité d'un véhicule pour une période
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = vehicleOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("vehicleId", id);
            result.put("vehicleName", vehicle.getName());
            result.put("available", vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE);
            result.put("status", vehicle.getStatus().name());
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Réserver un véhicule
     */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<Map<String, Object>> reserveVehicle(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reservationData) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = vehicleOpt.get();
            if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Véhicule non disponible");
                return ResponseEntity.badRequest().body(error);
            }

            vehicle.setStatus(Vehicle.VehicleStatus.IN_USE);
            vehicleRepository.save(vehicle);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("vehicleId", id);
            result.put("vehicleName", vehicle.getName());
            result.put("reservedBy", reservationData.get("reservedBy"));
            result.put("startDate", reservationData.get("startDate"));
            result.put("endDate", reservationData.get("endDate"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Libérer un véhicule
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<Map<String, Object>> releaseVehicle(@PathVariable Long id) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = vehicleOpt.get();
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("vehicleId", id);
            result.put("vehicleName", vehicle.getName());
            result.put("status", "AVAILABLE");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtenir le planning d'un véhicule
     */
    @GetMapping("/{id}/planning")
    public ResponseEntity<Map<String, Object>> getVehiclePlanning(@PathVariable Long id) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = vehicleOpt.get();
            Map<String, Object> planning = new HashMap<>();
            planning.put("vehicleId", id);
            planning.put("vehicleName", vehicle.getName());
            planning.put("currentStatus", vehicle.getStatus().name());
            planning.put("reservations", List.of()); // TODO: implémenter réservations
            planning.put("maintenance", List.of()); // TODO: implémenter maintenances

            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enregistrer un entretien
     */
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<Vehicle> recordMaintenance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> maintenanceData) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
            if (vehicleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = vehicleOpt.get();

            // Mettre à jour les données de maintenance
            if (maintenanceData.containsKey("lastMaintenanceDate")) {
                vehicle.setLastMaintenanceDate(LocalDate.parse(maintenanceData.get("lastMaintenanceDate").toString()));
            }

            if (maintenanceData.containsKey("nextMaintenanceDate")) {
                vehicle.setNextMaintenanceDate(LocalDate.parse(maintenanceData.get("nextMaintenanceDate").toString()));
            }

            if (maintenanceData.containsKey("mileage")) {
                vehicle.setMileage(Integer.parseInt(maintenanceData.get("mileage").toString()));
            }

            vehicleRepository.save(vehicle);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
