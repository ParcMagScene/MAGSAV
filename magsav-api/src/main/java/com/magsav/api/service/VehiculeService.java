package com.magsav.api.service;

import com.magsav.api.entity.Vehicule;
import com.magsav.api.repository.VehiculeRepository;
import com.magsav.api.controller.VehiculeController.VehiculeStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service pour la gestion des véhicules
 * Couche métier pour les opérations sur les véhicules
 */
@Service
@Transactional
public class VehiculeService {

    @Autowired
    private VehiculeRepository vehiculeRepository;

    /**
     * Récupère tous les véhicules avec pagination
     */
    @Transactional(readOnly = true)
    public Page<Vehicule> findAll(Pageable pageable) {
        return vehiculeRepository.findAll(pageable);
    }

    /**
     * Recherche des véhicules par critères
     */
    @Transactional(readOnly = true)
    public Page<Vehicule> searchVehicules(String search, Pageable pageable) {
        return vehiculeRepository.findByImmatriculationContainingIgnoreCaseOrMarqueContainingIgnoreCaseOrModeleContainingIgnoreCase(
            search, search, search, pageable);
    }

    /**
     * Récupère un véhicule par son ID
     */
    @Transactional(readOnly = true)
    public Optional<Vehicule> findById(Long id) {
        return vehiculeRepository.findById(id);
    }

    /**
     * Vérifie si un véhicule existe
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return vehiculeRepository.existsById(id);
    }

    /**
     * Sauvegarde un véhicule
     */
    public Vehicule save(Vehicule vehicule) {
        // Validation métier
        if (vehicule.getImmatriculation() != null) {
            vehicule.setImmatriculation(vehicule.getImmatriculation().toUpperCase().trim());
        }
        
        return vehiculeRepository.save(vehicule);
    }

    /**
     * Supprime un véhicule par son ID
     */
    public void deleteById(Long id) {
        vehiculeRepository.deleteById(id);
    }

    /**
     * Récupère les véhicules par statut
     */
    @Transactional(readOnly = true)
    public Page<Vehicule> findByStatut(Vehicule.StatutVehicule statut, Pageable pageable) {
        return vehiculeRepository.findByStatut(statut, pageable);
    }

    /**
     * Récupère les véhicules par type
     */
    @Transactional(readOnly = true)
    public Page<Vehicule> findByType(Vehicule.TypeVehicule type, Pageable pageable) {
        return vehiculeRepository.findByTypeVehicule(type, pageable);
    }

    /**
     * Compte les véhicules par statut
     */
    @Transactional(readOnly = true)
    public long countByStatut(Vehicule.StatutVehicule statut) {
        return vehiculeRepository.countByStatut(statut);
    }

    /**
     * Récupère les statistiques des véhicules
     */
    @Transactional(readOnly = true)
    public VehiculeStats getStatistiques() {
        long total = vehiculeRepository.count();
        long disponibles = countByStatut(Vehicule.StatutVehicule.DISPONIBLE);
        long enService = countByStatut(Vehicule.StatutVehicule.EN_SERVICE);
        long enMaintenance = countByStatut(Vehicule.StatutVehicule.MAINTENANCE);
        long horsService = countByStatut(Vehicule.StatutVehicule.HORS_SERVICE);

        return new VehiculeStats(total, disponibles, enService, enMaintenance, horsService);
    }

    /**
     * Vérifie si une immatriculation existe déjà
     */
    @Transactional(readOnly = true)
    public boolean existsByImmatriculation(String immatriculation) {
        return vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation);
    }

    /**
     * Récupère les véhicules disponibles pour une période
     */
    @Transactional(readOnly = true)
    public Page<Vehicule> findVehiculesDisponibles(Pageable pageable) {
        return vehiculeRepository.findByStatut(Vehicule.StatutVehicule.DISPONIBLE, pageable);
    }

    /**
     * Met à jour le kilométrage d'un véhicule
     */
    public Optional<Vehicule> updateKilometrage(Long id, Integer nouveauKilometrage) {
        Optional<Vehicule> vehiculeOpt = vehiculeRepository.findById(id);
        if (vehiculeOpt.isPresent()) {
            Vehicule vehicule = vehiculeOpt.get();
            
            // Validation: le nouveau kilométrage doit être supérieur à l'ancien
            if (vehicule.getKilometrage() != null && nouveauKilometrage < vehicule.getKilometrage()) {
                throw new IllegalArgumentException("Le nouveau kilométrage ne peut pas être inférieur à l'ancien");
            }
            
            vehicule.setKilometrage(nouveauKilometrage);
            return Optional.of(vehiculeRepository.save(vehicule));
        }
        return Optional.empty();
    }

    /**
     * Change le statut d'un véhicule avec validation métier
     */
    public Optional<Vehicule> changerStatut(Long id, Vehicule.StatutVehicule nouveauStatut) {
        Optional<Vehicule> vehiculeOpt = vehiculeRepository.findById(id);
        if (vehiculeOpt.isPresent()) {
            Vehicule vehicule = vehiculeOpt.get();
            
            // Validation métier des transitions de statut
            if (!isTransitionStatutValide(vehicule.getStatut(), nouveauStatut)) {
                throw new IllegalStateException("Transition de statut non autorisée: " + 
                    vehicule.getStatut() + " -> " + nouveauStatut);
            }
            
            vehicule.setStatut(nouveauStatut);
            return Optional.of(vehiculeRepository.save(vehicule));
        }
        return Optional.empty();
    }

    /**
     * Vérifie si une transition de statut est valide
     */
    private boolean isTransitionStatutValide(Vehicule.StatutVehicule statutActuel, Vehicule.StatutVehicule nouveauStatut) {
        // Logique métier pour les transitions autorisées
        // Pour le POC, toutes les transitions sont autorisées
        // En production, on pourrait implémenter des règles spécifiques
        return true;
    }
}