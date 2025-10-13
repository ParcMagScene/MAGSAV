package com.magsav.api.repository;

import com.magsav.api.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des véhicules
 * Interface JPA pour les opérations CRUD
 */
@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    /**
     * Recherche par immatriculation (insensible à la casse)
     */
    Optional<Vehicule> findByImmatriculationIgnoreCase(String immatriculation);

    /**
     * Vérifie l'existence par immatriculation
     */
    boolean existsByImmatriculationIgnoreCase(String immatriculation);

    /**
     * Recherche par statut avec pagination
     */
    Page<Vehicule> findByStatut(Vehicule.StatutVehicule statut, Pageable pageable);

    /**
     * Recherche par type de véhicule avec pagination
     */
    Page<Vehicule> findByTypeVehicule(Vehicule.TypeVehicule typeVehicule, Pageable pageable);

    /**
     * Compte les véhicules par statut
     */
    long countByStatut(Vehicule.StatutVehicule statut);

    /**
     * Recherche multicritères
     */
    Page<Vehicule> findByImmatriculationContainingIgnoreCaseOrMarqueContainingIgnoreCaseOrModeleContainingIgnoreCase(
        String immatriculation, String marque, String modele, Pageable pageable);

    /**
     * Recherche par marque
     */
    Page<Vehicule> findByMarqueContainingIgnoreCase(String marque, Pageable pageable);

    /**
     * Recherche par modèle
     */
    Page<Vehicule> findByModeleContainingIgnoreCase(String modele, Pageable pageable);

    /**
     * Recherche par année
     */
    Page<Vehicule> findByAnnee(Integer annee, Pageable pageable);

    /**
     * Recherche par plage d'années
     */
    Page<Vehicule> findByAnneeBetween(Integer anneeMin, Integer anneeMax, Pageable pageable);

    /**
     * Recherche des véhicules avec kilométrage élevé
     */
    @Query("SELECT v FROM Vehicule v WHERE v.kilometrage > :kilometrage ORDER BY v.kilometrage DESC")
    Page<Vehicule> findByKilometrageGreaterThan(@Param("kilometrage") Integer kilometrage, Pageable pageable);

    /**
     * Recherche des véhicules disponibles
     */
    @Query("SELECT v FROM Vehicule v WHERE v.statut = 'DISPONIBLE' ORDER BY v.immatriculation")
    List<Vehicule> findAllDisponibles();

    /**
     * Recherche des véhicules en maintenance
     */
    @Query("SELECT v FROM Vehicule v WHERE v.statut = 'MAINTENANCE' ORDER BY v.updatedAt DESC")
    List<Vehicule> findAllEnMaintenance();

    /**
     * Statistiques par type de véhicule
     */
    @Query("SELECT v.typeVehicule, COUNT(v) FROM Vehicule v GROUP BY v.typeVehicule")
    List<Object[]> getStatistiquesParType();

    /**
     * Statistiques par statut
     */
    @Query("SELECT v.statut, COUNT(v) FROM Vehicule v GROUP BY v.statut")
    List<Object[]> getStatistiquesParStatut();

    /**
     * Recherche des véhicules récents (créés dans les 30 derniers jours)
     */
    @Query("SELECT v FROM Vehicule v ORDER BY v.createdAt DESC")
    List<Vehicule> findVehiculesRecents();

    /**
     * Recherche par location externe
     */
    Page<Vehicule> findByLocationExterne(Boolean locationExterne, Pageable pageable);

    /**
     * Recherche avancée avec critères multiples
     */
    @Query("SELECT v FROM Vehicule v WHERE " +
           "(:immatriculation IS NULL OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :immatriculation, '%'))) AND " +
           "(:marque IS NULL OR LOWER(v.marque) LIKE LOWER(CONCAT('%', :marque, '%'))) AND " +
           "(:modele IS NULL OR LOWER(v.modele) LIKE LOWER(CONCAT('%', :modele, '%'))) AND " +
           "(:typeVehicule IS NULL OR v.typeVehicule = :typeVehicule) AND " +
           "(:statut IS NULL OR v.statut = :statut) AND " +
           "(:anneeMin IS NULL OR v.annee >= :anneeMin) AND " +
           "(:anneeMax IS NULL OR v.annee <= :anneeMax)")
    Page<Vehicule> findByMultipleCriteria(
        @Param("immatriculation") String immatriculation,
        @Param("marque") String marque,
        @Param("modele") String modele,
        @Param("typeVehicule") Vehicule.TypeVehicule typeVehicule,
        @Param("statut") Vehicule.StatutVehicule statut,
        @Param("anneeMin") Integer anneeMin,
        @Param("anneeMax") Integer anneeMax,
        Pageable pageable
    );
}