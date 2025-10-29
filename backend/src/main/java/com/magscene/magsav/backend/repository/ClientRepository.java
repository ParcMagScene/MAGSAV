package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des clients
 * MÃƒÂ©thodes de recherche et statistiques pour les clients
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Recherches de base
    Optional<Client> findBySiretNumber(String siretNumber);
    
    List<Client> findByCompanyNameContainingIgnoreCase(String companyName);
    
    List<Client> findByEmailContainingIgnoreCase(String email);
    
    List<Client> findByStatus(Client.ClientStatus status);
    
    List<Client> findByType(Client.ClientType type);
    
    List<Client> findByCategory(Client.ClientCategory category);
    
    List<Client> findByAssignedSalesRep(String salesRep);

    // Recherches par localisation
    List<Client> findByCityIgnoreCase(String city);
    
    List<Client> findByCountryIgnoreCase(String country);
    
    List<Client> findByPostalCodeStartingWith(String postalCodePrefix);

    // Recherches financiÃƒÂ¨res
    List<Client> findByCreditLimitGreaterThan(BigDecimal amount);
    
    List<Client> findByOutstandingAmountGreaterThan(BigDecimal amount);
    
    @Query("SELECT c FROM Client c WHERE c.creditLimit - c.outstandingAmount < :availableCredit")
    List<Client> findByAvailableCreditLessThan(@Param("availableCredit") BigDecimal availableCredit);

    // Recherches par secteur d'activitÃƒÂ©
    List<Client> findByBusinessSectorContainingIgnoreCase(String businessSector);
    
    List<Client> findByEmployeeCountBetween(Integer minEmployees, Integer maxEmployees);
    
    List<Client> findByAnnualRevenueBetween(BigDecimal minRevenue, BigDecimal maxRevenue);

    // Recherches temporelles
    List<Client> findByCreatedAtAfter(LocalDateTime date);
    
    List<Client> findByUpdatedAtAfter(LocalDateTime date);

    // Recherches combinÃƒÂ©es avec requÃƒÂªtes personnalisÃƒÂ©es
    @Query("SELECT c FROM Client c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:salesRep IS NULL OR c.assignedSalesRep = :salesRep)")
    List<Client> findByMultipleCriteria(
        @Param("status") Client.ClientStatus status,
        @Param("type") Client.ClientType type,
        @Param("category") Client.ClientCategory category,
        @Param("salesRep") String salesRep
    );

    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.businessSector) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> searchByTerm(@Param("searchTerm") String searchTerm);

    // Statistiques et analyses
    @Query("SELECT COUNT(c) FROM Client c WHERE c.status = :status")
    Long countByStatus(@Param("status") Client.ClientStatus status);

    @Query("SELECT c.type, COUNT(c) FROM Client c GROUP BY c.type")
    List<Object[]> countByType();

    @Query("SELECT c.category, COUNT(c) FROM Client c GROUP BY c.category")
    List<Object[]> countByCategory();

    @Query("SELECT c.assignedSalesRep, COUNT(c) FROM Client c WHERE c.assignedSalesRep IS NOT NULL GROUP BY c.assignedSalesRep")
    List<Object[]> countBySalesRep();

    @Query("SELECT SUM(c.outstandingAmount) FROM Client c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingAmount();

    @Query("SELECT SUM(c.creditLimit) FROM Client c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalCreditLimit();

    @Query("SELECT AVG(c.annualRevenue) FROM Client c WHERE c.annualRevenue IS NOT NULL")
    BigDecimal getAverageAnnualRevenue();

    // Clients avec des projets rÃƒÂ©cents
    @Query("SELECT DISTINCT c FROM Client c " +
           "JOIN c.projects p " +
           "WHERE p.createdAt > :since")
    List<Client> findClientsWithRecentProjects(@Param("since") LocalDateTime since);

    // Clients avec contrats actifs
    @Query("SELECT DISTINCT c FROM Client c " +
           "JOIN c.contracts ct " +
           "WHERE ct.status = 'ACTIVE'")
    List<Client> findClientsWithActiveContracts();

    // Clients sans activitÃƒÂ© rÃƒÂ©cente
    @Query("SELECT c FROM Client c WHERE " +
           "c.updatedAt < :since AND " +
           "NOT EXISTS (SELECT p FROM Project p WHERE p.client = c AND p.createdAt > :since) AND " +
           "NOT EXISTS (SELECT ct FROM Contract ct WHERE ct.client = c AND ct.updatedAt > :since)")
    List<Client> findInactiveClients(@Param("since") LocalDateTime since);

    // Top clients par chiffre d'affaires
    @Query("SELECT c FROM Client c " +
           "LEFT JOIN c.projects p " +
           "WHERE p.finalAmount IS NOT NULL " +
           "GROUP BY c " +
           "ORDER BY SUM(p.finalAmount) DESC")
    List<Client> findTopClientsByRevenue();

    // Clients par rÃƒÂ©gion (basÃƒÂ© sur code postal)
    @Query("SELECT SUBSTRING(c.postalCode, 1, 2) as region, COUNT(c) FROM Client c " +
           "WHERE c.postalCode IS NOT NULL AND LENGTH(c.postalCode) >= 2 " +
           "GROUP BY SUBSTRING(c.postalCode, 1, 2)")
    List<Object[]> countByRegion();

    // VÃƒÂ©rification d'unicitÃƒÂ©
    boolean existsBySiretNumber(String siretNumber);
    
    boolean existsByVatNumber(String vatNumber);

    // Clients crÃƒÂ©ÃƒÂ©s dans une pÃƒÂ©riode
    @Query("SELECT c FROM Client c WHERE " +
           "c.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.createdAt DESC")
    List<Client> findClientsCreatedBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    // Performance commerciale
    @Query("SELECT c, COUNT(p), SUM(COALESCE(p.finalAmount, 0)) FROM Client c " +
           "LEFT JOIN c.projects p " +
           "GROUP BY c " +
           "HAVING COUNT(p) > 0 " +
           "ORDER BY SUM(COALESCE(p.finalAmount, 0)) DESC")
    List<Object[]> getClientPerformanceStats();
}

