package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Project;
import com.magscene.magsav.backend.entity.Project.ProjectStatus;
import com.magscene.magsav.backend.entity.Project.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des projets
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Trouve un projet par son numÃƒÂ©ro
     */
    Optional<Project> findByProjectNumber(String projectNumber);

    /**
     * Trouve tous les projets d'un statut donnÃƒÂ©
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Trouve tous les projets d'un type donnÃƒÂ©
     */
    List<Project> findByType(ProjectType type);

    /**
     * Trouve tous les projets d'un client
     */
    List<Project> findByClientNameContainingIgnoreCase(String clientName);

    /**
     * Trouve tous les projets actifs (non terminÃƒÂ©s, non annulÃƒÂ©s)
     */
    @Query("SELECT p FROM Project p WHERE p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findActiveProjects();

    /**
     * Trouve tous les projets en cours
     */
    @Query("SELECT p FROM Project p WHERE p.status IN ('CONFIRMED', 'IN_PROGRESS', 'INSTALLED')")
    List<Project> findProjectsInProgress();

    /**
     * Trouve les projets par gestionnaire de projet
     */
    List<Project> findByProjectManagerContainingIgnoreCase(String projectManager);

    /**
     * Trouve les projets par commercial
     */
    List<Project> findBySalesRepresentativeContainingIgnoreCase(String salesRepresentative);

    /**
     * Trouve les projets avec installation prÃƒÂ©vue dans une pÃƒÂ©riode
     */
    @Query("SELECT p FROM Project p WHERE p.installationDate BETWEEN :startDate AND :endDate")
    List<Project> findByInstallationDateBetween(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * Trouve les projets avec livraison prÃƒÂ©vue dans une pÃƒÂ©riode
     */
    @Query("SELECT p FROM Project p WHERE p.deliveryDate BETWEEN :startDate AND :endDate")
    List<Project> findByDeliveryDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    /**
     * Recherche globale dans les projets
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.projectNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.clientName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.venue) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Project> searchProjects(@Param("searchTerm") String searchTerm);

    /**
     * Compte les projets par statut
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countProjectsByStatus();

    /**
     * Compte les projets par type
     */
    @Query("SELECT p.type, COUNT(p) FROM Project p GROUP BY p.type")
    List<Object[]> countProjectsByType();

    /**
     * Trouve les projets rÃƒÂ©cents (crÃƒÂ©ÃƒÂ©s dans les N derniers jours)
     */
    @Query("SELECT p FROM Project p WHERE p.createdAt >= :sinceDate ORDER BY p.createdAt DESC")
    List<Project> findRecentProjects(@Param("sinceDate") LocalDate sinceDate);

    /**
     * Trouve les projets nÃƒÂ©cessitant une attention (dates dÃƒÂ©passÃƒÂ©es, statut critique)
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(p.installationDate < CURRENT_DATE AND p.status IN ('CONFIRMED', 'IN_PROGRESS')) OR " +
           "(p.deliveryDate < CURRENT_DATE AND p.status IN ('CONFIRMED', 'IN_PROGRESS')) OR " +
           "(p.endDate < CURRENT_DATE AND p.status NOT IN ('COMPLETED', 'CANCELLED'))")
    List<Project> findProjectsNeedingAttention();

    /**
     * Calcule le chiffre d'affaires total par pÃƒÂ©riode
     */
    @Query("SELECT SUM(p.finalAmount) FROM Project p WHERE " +
           "p.status = 'COMPLETED' AND p.endDate BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenue(@Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);
}

