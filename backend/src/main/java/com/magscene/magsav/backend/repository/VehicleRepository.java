package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des vÃƒÂ©hicules
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Recherche par nom (insensible ÃƒÂ  la casse)
     */
    List<Vehicle> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche par plaque d'immatriculation
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Recherche par numÃƒÂ©ro VIN
     */
    Optional<Vehicle> findByVin(String vin);
    
    /**
     * Recherche par type de vÃƒÂ©hicule
     */
    List<Vehicle> findByType(Vehicle.VehicleType type);
    
    /**
     * Recherche par statut
     */
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);
    
    /**
     * Recherche par marque
     */
    List<Vehicle> findByBrandContainingIgnoreCase(String brand);
    
    /**
     * Recherche par modÃƒÂ¨le
     */
    List<Vehicle> findByModelContainingIgnoreCase(String model);
    
    /**
     * Recherche par conducteur assignÃƒÂ©
     */
    List<Vehicle> findByAssignedDriverContainingIgnoreCase(String driver);
    
    /**
     * VÃƒÂ©hicules disponibles pour une pÃƒÂ©riode donnÃƒÂ©e
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE' OR v.status = 'RESERVED'")
    List<Vehicle> findAvailableVehicles();
    
    /**
     * VÃƒÂ©hicules nÃƒÂ©cessitant une maintenance
     */
    @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenanceDate <= :date")
    List<Vehicle> findVehiclesNeedingMaintenance(@Param("date") LocalDate date);
    
    /**
     * VÃƒÂ©hicules avec documents expirÃƒÂ©s
     */
    @Query("SELECT v FROM Vehicle v WHERE v.insuranceExpiration <= :date OR v.technicalControlExpiration <= :date")
    List<Vehicle> findVehiclesWithExpiredDocuments(@Param("date") LocalDate date);
    
    /**
     * VÃƒÂ©hicules en maintenance
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'MAINTENANCE' OR v.status = 'OUT_OF_ORDER'")
    List<Vehicle> findVehiclesInMaintenance();
    
    /**
     * Recherche globale sur plusieurs champs
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.vin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.assignedDriver) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.currentLocation) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Vehicle> searchVehicles(@Param("search") String search);
    
    /**
     * Compte les vÃƒÂ©hicules par statut
     */
    @Query("SELECT v.status, COUNT(v) FROM Vehicle v GROUP BY v.status")
    List<Object[]> countVehiclesByStatus();
    
    /**
     * Compte les vÃƒÂ©hicules par type
     */
    @Query("SELECT v.type, COUNT(v) FROM Vehicle v GROUP BY v.type")
    List<Object[]> countVehiclesByType();
    
    /**
     * VÃƒÂ©hicules avec kilomÃƒÂ©trage ÃƒÂ©levÃƒÂ©
     */
    List<Vehicle> findByMileageGreaterThan(Integer mileage);
    
    /**
     * VÃƒÂ©hicules par annÃƒÂ©e de fabrication
     */
    List<Vehicle> findByYearManufacturedBetween(Integer yearStart, Integer yearEnd);
    
    /**
     * VÃƒÂ©hicules disponibles pour location externe
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE' AND v.dailyRentalRate IS NOT NULL")
    List<Vehicle> findAvailableForRental();
}

