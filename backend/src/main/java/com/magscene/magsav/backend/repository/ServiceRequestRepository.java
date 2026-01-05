package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.ServiceRequest;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.backend.entity.ServiceRequest.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Repository pour l'entitÃƒÂ© ServiceRequest avec Spring Data JPA
 * Utilise les nouvelles fonctionnalitÃƒÂ©s Java 21 pour les requÃƒÂªtes
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

        /**
         * Récupère toutes les demandes avec l'équipement chargé (join fetch)
         */
        @Query("SELECT sr FROM ServiceRequest sr LEFT JOIN FETCH sr.equipment")
        List<ServiceRequest> findAllWithEquipment();
    
    /**
     * Recherche par statut
     */
    List<ServiceRequest> findByStatus(ServiceRequestStatus status);
    
    /**
     * Recherche par prioritÃƒÂ©
     */
    List<ServiceRequest> findByPriority(Priority priority);
    
    /**
     * Recherche par technicien assignÃƒÂ©
     */
    List<ServiceRequest> findByAssignedTechnician(String assignedTechnician);
    
    /**
     * Recherche par nom du demandeur
     */
    List<ServiceRequest> findByRequesterNameContainingIgnoreCase(String requesterName);
    
    /**
     * Recherche par titre (insensible ÃƒÂ  la casse)
     */
    List<ServiceRequest> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Recherche par ÃƒÂ©quipement
     */
    List<ServiceRequest> findByEquipmentId(Long equipmentId);
    
    /**
     * Comptages par statut
     */
    Long countByStatus(ServiceRequestStatus status);
    
    /**
     * Comptages par prioritÃƒÂ©
     */
    Long countByPriority(Priority priority);
    
    /**
     * Comptage des demandes urgentes (prioritÃƒÂ© haute non rÃƒÂ©solues)
     */
    Long countByPriorityAndStatusNot(Priority priority, ServiceRequestStatus status);
    
    /**
     * Demandes crÃƒÂ©ÃƒÂ©es entre deux dates
     */
    List<ServiceRequest> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Demandes rÃƒÂ©solues entre deux dates
     */
    List<ServiceRequest> findByResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Demandes non assignÃƒÂ©es (technicien vide ou null)
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.assignedTechnician IS NULL OR sr.assignedTechnician = ''")
    List<ServiceRequest> findUnassignedRequests();
    
    /**
     * Demandes en retard (crÃƒÂ©ÃƒÂ©es il y a plus de X jours et non rÃƒÂ©solues)
     */
    @Query("""
        SELECT sr FROM ServiceRequest sr 
        WHERE sr.createdAt < :cutoffDate 
        AND sr.status NOT IN ('RESOLVED', 'CLOSED')
        ORDER BY sr.createdAt ASC
        """)
    List<ServiceRequest> findOverdueRequests(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Statistiques par technicien
     */
    @Query("SELECT sr.assignedTechnician, COUNT(sr) FROM ServiceRequest sr WHERE sr.assignedTechnician IS NOT NULL GROUP BY sr.assignedTechnician")
    List<Object[]> getRequestCountByTechnician();
    
    /**
     * Temps moyen de rÃƒÂ©solution par technicien - Temporairement dÃƒÂ©sactivÃƒÂ©
     */
    // @Query("""
    //     SELECT sr.assignedTechnician, 
    //            AVG(DATEDIFF('HOUR', sr.createdAt, sr.resolvedAt)) as avgHours
    //     FROM ServiceRequest sr 
    //     WHERE sr.assignedTechnician IS NOT NULL 
    //     AND sr.resolvedAt IS NOT NULL
    //     GROUP BY sr.assignedTechnician
    //     """)
    // List<Object[]> getAverageResolutionTimeByTechnician();
    
    /**
     * CoÃƒÂ»t total par technicien
     */
    @Query("SELECT sr.assignedTechnician, SUM(sr.actualCost) FROM ServiceRequest sr WHERE sr.assignedTechnician IS NOT NULL AND sr.actualCost IS NOT NULL GROUP BY sr.assignedTechnician")
    List<Object[]> getTotalCostByTechnician();
    
    /**
     * Demandes liÃƒÂ©es ÃƒÂ  un ÃƒÂ©quipement spÃƒÂ©cifique (par nom d'ÃƒÂ©quipement)
     */
    @Query("SELECT sr FROM ServiceRequest sr JOIN sr.equipment e WHERE e.name LIKE %:equipmentName%")
    List<ServiceRequest> findByEquipmentNameContaining(@Param("equipmentName") String equipmentName);
    
    /**
     * Top 5 des ÃƒÂ©quipements avec le plus de demandes SAV
     */
    @Query("""
        SELECT e.name, COUNT(sr) as requestCount 
        FROM ServiceRequest sr JOIN sr.equipment e 
        GROUP BY e.id, e.name 
        ORDER BY COUNT(sr) DESC
        """)
    List<Object[]> getTopEquipmentWithMostRequests();
}

