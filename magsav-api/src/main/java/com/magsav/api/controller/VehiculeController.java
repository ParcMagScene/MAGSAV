package com.magsav.api.controller;

import com.magsav.api.entity.Vehicule;
import com.magsav.api.service.VehiculeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des véhicules
 * Démontre l'API REST pour MAGSAV-1.3
 */
@RestController
@RequestMapping("/api/vehicules")
@CrossOrigin(origins = "*") // Pour le POC, à sécuriser en production
public class VehiculeController {

    @Autowired
    private VehiculeService vehiculeService;

    /**
     * Récupère tous les véhicules avec pagination
     */
    @GetMapping
    public ResponseEntity<Page<Vehicule>> getAllVehicules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        Page<Vehicule> vehicules;
        if (search != null && !search.trim().isEmpty()) {
            vehicules = vehiculeService.searchVehicules(search, pageable);
        } else {
            vehicules = vehiculeService.findAll(pageable);
        }
        
        return ResponseEntity.ok(vehicules);
    }

    /**
     * Récupère un véhicule par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> getVehiculeById(@PathVariable Long id) {
        Optional<Vehicule> vehicule = vehiculeService.findById(id);
        return vehicule.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau véhicule
     */
    @PostMapping
    public ResponseEntity<Vehicule> createVehicule(@Valid @RequestBody Vehicule vehicule) {
        try {
            Vehicule nouveauVehicule = vehiculeService.save(vehicule);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouveauVehicule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour un véhicule existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Vehicule> updateVehicule(@PathVariable Long id, 
                                                  @Valid @RequestBody Vehicule vehicule) {
        if (!vehiculeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        vehicule.setId(id);
        Vehicule vehiculeMisAJour = vehiculeService.save(vehicule);
        return ResponseEntity.ok(vehiculeMisAJour);
    }

    /**
     * Supprime un véhicule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        if (!vehiculeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        vehiculeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les véhicules par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<Page<Vehicule>> getVehiculesByStatut(
            @PathVariable Vehicule.StatutVehicule statut,
            Pageable pageable) {
        Page<Vehicule> vehicules = vehiculeService.findByStatut(statut, pageable);
        return ResponseEntity.ok(vehicules);
    }

    /**
     * Récupère les véhicules par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<Vehicule>> getVehiculesByType(
            @PathVariable Vehicule.TypeVehicule type,
            Pageable pageable) {
        Page<Vehicule> vehicules = vehiculeService.findByType(type, pageable);
        return ResponseEntity.ok(vehicules);
    }

    /**
     * Met à jour le kilométrage d'un véhicule
     */
    @PatchMapping("/{id}/kilometrage")
    public ResponseEntity<Vehicule> updateKilometrage(@PathVariable Long id, 
                                                     @RequestParam Integer kilometrage) {
        Optional<Vehicule> vehiculeOpt = vehiculeService.findById(id);
        if (vehiculeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Vehicule vehicule = vehiculeOpt.get();
        vehicule.setKilometrage(kilometrage);
        Vehicule vehiculeMisAJour = vehiculeService.save(vehicule);
        
        return ResponseEntity.ok(vehiculeMisAJour);
    }

    /**
     * Change le statut d'un véhicule
     */
    @PatchMapping("/{id}/statut")
    public ResponseEntity<Vehicule> updateStatut(@PathVariable Long id, 
                                               @RequestParam Vehicule.StatutVehicule statut) {
        Optional<Vehicule> vehiculeOpt = vehiculeService.findById(id);
        if (vehiculeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Vehicule vehicule = vehiculeOpt.get();
        vehicule.setStatut(statut);
        Vehicule vehiculeMisAJour = vehiculeService.save(vehicule);
        
        return ResponseEntity.ok(vehiculeMisAJour);
    }

    /**
     * Récupère les statistiques des véhicules
     */
    @GetMapping("/stats")
    public ResponseEntity<VehiculeStats> getVehiculeStats() {
        VehiculeStats stats = vehiculeService.getStatistiques();
        return ResponseEntity.ok(stats);
    }

    /**
     * Classe interne pour les statistiques
     */
    public static class VehiculeStats {
        private long totalVehicules;
        private long vehiculesDisponibles;
        private long vehiculesEnService;
        private long vehiculesEnMaintenance;
        private long vehiculesHorsService;

        // Constructeurs
        public VehiculeStats() {}

        public VehiculeStats(long totalVehicules, long vehiculesDisponibles, 
                           long vehiculesEnService, long vehiculesEnMaintenance, 
                           long vehiculesHorsService) {
            this.totalVehicules = totalVehicules;
            this.vehiculesDisponibles = vehiculesDisponibles;
            this.vehiculesEnService = vehiculesEnService;
            this.vehiculesEnMaintenance = vehiculesEnMaintenance;
            this.vehiculesHorsService = vehiculesHorsService;
        }

        // Getters et Setters
        public long getTotalVehicules() { return totalVehicules; }
        public void setTotalVehicules(long totalVehicules) { this.totalVehicules = totalVehicules; }

        public long getVehiculesDisponibles() { return vehiculesDisponibles; }
        public void setVehiculesDisponibles(long vehiculesDisponibles) { this.vehiculesDisponibles = vehiculesDisponibles; }

        public long getVehiculesEnService() { return vehiculesEnService; }
        public void setVehiculesEnService(long vehiculesEnService) { this.vehiculesEnService = vehiculesEnService; }

        public long getVehiculesEnMaintenance() { return vehiculesEnMaintenance; }
        public void setVehiculesEnMaintenance(long vehiculesEnMaintenance) { this.vehiculesEnMaintenance = vehiculesEnMaintenance; }

        public long getVehiculesHorsService() { return vehiculesHorsService; }
        public void setVehiculesHorsService(long vehiculesHorsService) { this.vehiculesHorsService = vehiculesHorsService; }
    }
}