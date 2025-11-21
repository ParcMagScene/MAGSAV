package com.magscene.magsav.backend.repository;

import com.magsav.entities.MaterialRequest;
import com.magsav.enums.RequestStatus;
import com.magsav.enums.RequestContext;
import com.magscene.magsav.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des demandes de matériel
 */
@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    
    // Recherche par numéro de demande
    Optional<MaterialRequest> findByRequestNumber(String requestNumber);
    
    // Recherche par demandeur
    List<MaterialRequest> findByRequesterEmail(String requesterEmail);
    
    List<MaterialRequest> findByRequesterEmailOrderByCreatedAtDesc(String requesterEmail);
    
    List<MaterialRequest> findByRequesterName(String requesterName);
    
    List<MaterialRequest> findByRequesterNameContainingIgnoreCase(String requesterName);
    
    // Recherche par statut
    List<MaterialRequest> findByStatus(RequestStatus status);
    
    List<MaterialRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    List<MaterialRequest> findByStatusIn(List<RequestStatus> statuses);
    
    // Demandes en attente d'approbation
    List<MaterialRequest> findByStatusOrderBySubmittedAtAsc(RequestStatus status);
    
    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.status = 'PENDING_APPROVAL' ORDER BY mr.submittedAt ASC")
    List<MaterialRequest> findPendingApprovalRequests();
    
    // Recherche par contexte
    List<MaterialRequest> findByContext(RequestContext context);
    
    List<MaterialRequest> findByContextAndStatus(RequestContext context, RequestStatus status);
    
    // Recherche par projet
    List<MaterialRequest> findByProject(Project project);
    
    List<MaterialRequest> findByProjectAndStatus(Project project, RequestStatus status);
    
    // Recherche par approbateur
    List<MaterialRequest> findByApprovedBy(String approvedBy);
    
    List<MaterialRequest> findByApprovedByOrderByApprovedAtDesc(String approvedBy);
    
    // Recherche par période
    List<MaterialRequest> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<MaterialRequest> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<MaterialRequest> findByApprovedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Demandes récentes
    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.createdAt >= :since ORDER BY mr.createdAt DESC")
    List<MaterialRequest> findRecentRequests(@Param("since") LocalDateTime since);
    
    // Demandes avec items non alloués
    @Query("SELECT DISTINCT mr FROM MaterialRequest mr JOIN mr.items item WHERE " +
           "item.requestedQuantity > item.quantityAllocated AND mr.status = 'APPROVED'")
    List<MaterialRequest> findRequestsWithUnallocatedItems();
    
    // Demandes partiellement livrées
    @Query("SELECT DISTINCT mr FROM MaterialRequest mr JOIN mr.items item WHERE " +
           "item.quantityDelivered > 0 AND item.quantityDelivered < item.requestedQuantity")
    List<MaterialRequest> findPartiallyDeliveredRequests();
    
    // Recherche textuelle
    @Query("SELECT mr FROM MaterialRequest mr WHERE " +
           "LOWER(mr.requestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.requesterName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.justification) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MaterialRequest> searchRequests(@Param("searchTerm") String searchTerm);
    
    // Statistiques par demandeur
    @Query("SELECT mr.requesterName, COUNT(mr) FROM MaterialRequest mr GROUP BY mr.requesterName ORDER BY COUNT(mr) DESC")
    List<Object[]> countRequestsByRequester();
    
    @Query("SELECT mr.requesterName, COUNT(mr) FROM MaterialRequest mr WHERE mr.status = :status GROUP BY mr.requesterName")
    List<Object[]> countRequestsByRequesterAndStatus(@Param("status") RequestStatus status);
    
    // Statistiques par contexte
    @Query("SELECT mr.context, COUNT(mr) FROM MaterialRequest mr GROUP BY mr.context")
    List<Object[]> countRequestsByContext();
    
    // Statistiques par statut
    @Query("SELECT mr.status, COUNT(mr) FROM MaterialRequest mr GROUP BY mr.status")
    List<Object[]> countRequestsByStatus();
    
    // Statistiques temporelles
    @Query("SELECT DATE(mr.createdAt), COUNT(mr) FROM MaterialRequest mr WHERE mr.createdAt >= :since GROUP BY DATE(mr.createdAt) ORDER BY DATE(mr.createdAt)")
    List<Object[]> countRequestsByDay(@Param("since") LocalDateTime since);
    
    @Query("SELECT YEAR(mr.createdAt), MONTH(mr.createdAt), COUNT(mr) FROM MaterialRequest mr GROUP BY YEAR(mr.createdAt), MONTH(mr.createdAt) ORDER BY YEAR(mr.createdAt) DESC, MONTH(mr.createdAt) DESC")
    List<Object[]> countRequestsByMonth();
    
    // Temps moyen d'approbation
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, mr.submittedAt, mr.approvedAt)) FROM MaterialRequest mr WHERE mr.approvedAt IS NOT NULL AND mr.submittedAt IS NOT NULL")
    Double averageApprovalTimeInHours();
    
    // Demandes urgentes
    @Query("SELECT mr FROM MaterialRequest mr WHERE mr.urgency = 'URGENT' AND mr.status IN ('PENDING_APPROVAL', 'APPROVED') ORDER BY mr.submittedAt ASC")
    List<MaterialRequest> findUrgentRequests();
}