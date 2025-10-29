package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Contract;
import com.magscene.magsav.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des contrats
 * MÃƒÂ©thodes de recherche et statistiques pour les contrats
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Recherches de base
    Optional<Contract> findByContractNumber(String contractNumber);
    
    List<Contract> findByTitleContainingIgnoreCase(String title);
    
    List<Contract> findByType(Contract.ContractType type);
    
    List<Contract> findByStatus(Contract.ContractStatus status);

    // Recherches par client
    List<Contract> findByClient(Client client);
    
    List<Contract> findByClientId(Long clientId);
    
    List<Contract> findByClientAndStatus(Client client, Contract.ContractStatus status);

    // Recherches par dates
    List<Contract> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Contract> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Contract> findBySignatureDateBetween(LocalDate startDate, LocalDate endDate);

    // Contrats expirant bientÃƒÂ´t
    @Query("SELECT c FROM Contract c WHERE " +
           "c.status = 'ACTIVE' AND " +
           "c.endDate BETWEEN CURRENT_DATE AND :futureDate")
    List<Contract> findExpiringContracts(@Param("futureDate") LocalDate futureDate);

    // Contrats expirÃƒÂ©s
    @Query("SELECT c FROM Contract c WHERE " +
           "c.endDate < CURRENT_DATE AND " +
           "c.status IN ('ACTIVE', 'SUSPENDED')")
    List<Contract> findExpiredContracts();

    // Recherches par montants
    List<Contract> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Contract> findByTotalAmountGreaterThan(BigDecimal amount);
    
    List<Contract> findByRemainingAmountGreaterThan(BigDecimal amount);

    // Recherches par frÃƒÂ©quence de facturation
    List<Contract> findByBillingFrequency(Contract.BillingFrequency frequency);
    
    List<Contract> findByPaymentTerms(Contract.PaymentTerms paymentTerms);

    // Contrats renouvelables
    List<Contract> findByIsAutoRenewableTrue();
    
    @Query("SELECT c FROM Contract c WHERE " +
           "c.isAutoRenewable = true AND " +
           "c.status = 'ACTIVE' AND " +
           "c.endDate BETWEEN CURRENT_DATE AND :futureDate")
    List<Contract> findContractsForRenewal(@Param("futureDate") LocalDate futureDate);

    // Recherches combinÃƒÂ©es
    @Query("SELECT c FROM Contract c WHERE " +
           "(:clientId IS NULL OR c.client.id = :clientId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:minAmount IS NULL OR c.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR c.totalAmount <= :maxAmount)")
    List<Contract> findByMultipleCriteria(
        @Param("clientId") Long clientId,
        @Param("status") Contract.ContractStatus status,
        @Param("type") Contract.ContractType type,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount
    );

    @Query("SELECT c FROM Contract c WHERE " +
           "LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.client.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Contract> searchByTerm(@Param("searchTerm") String searchTerm);

    // Statistiques par type
    @Query("SELECT c.type, COUNT(c) FROM Contract c GROUP BY c.type")
    List<Object[]> countByType();

    @Query("SELECT c.status, COUNT(c) FROM Contract c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("SELECT c.billingFrequency, COUNT(c) FROM Contract c GROUP BY c.billingFrequency")
    List<Object[]> countByBillingFrequency();

    // Statistiques financiÃƒÂ¨res
    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalActiveContractsValue();

    @Query("SELECT SUM(c.invoicedAmount) FROM Contract c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalInvoicedAmount();

    @Query("SELECT SUM(c.remainingAmount) FROM Contract c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalRemainingAmount();

    @Query("SELECT AVG(c.totalAmount) FROM Contract c WHERE c.status = 'ACTIVE'")
    BigDecimal getAverageContractValue();

    // Revenus par pÃƒÂ©riode
    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE " +
           "c.signatureDate BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByPeriod(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    // Contrats par mois (pour graphiques)
    @Query("SELECT YEAR(c.signatureDate), MONTH(c.signatureDate), COUNT(c), SUM(c.totalAmount) " +
           "FROM Contract c " +
           "WHERE c.signatureDate IS NOT NULL " +
           "GROUP BY YEAR(c.signatureDate), MONTH(c.signatureDate) " +
           "ORDER BY YEAR(c.signatureDate), MONTH(c.signatureDate)")
    List<Object[]> getContractStatsByMonth();

    // Performance par client
    @Query("SELECT c.client, COUNT(c), SUM(c.totalAmount) FROM Contract c " +
           "WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.client " +
           "ORDER BY SUM(c.totalAmount) DESC")
    List<Object[]> getContractPerformanceByClient();

    // Taux de renouvellement
    @Query("SELECT COUNT(c) FROM Contract c WHERE " +
           "c.isAutoRenewable = true AND " +
           "c.endDate BETWEEN :startDate AND :endDate")
    Long countRenewableContractsInPeriod(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    // Contrats sans activitÃƒÂ© rÃƒÂ©cente
    @Query("SELECT c FROM Contract c WHERE c.updatedAt < :since")
    List<Contract> findContractsNotUpdatedSince(@Param("since") LocalDateTime since);

    // VÃƒÂ©rification d'unicitÃƒÂ©
    boolean existsByContractNumber(String contractNumber);

    // Contrats actifs d'un client
    @Query("SELECT COUNT(c) FROM Contract c WHERE " +
           "c.client.id = :clientId AND " +
           "c.status = 'ACTIVE'")
    Long countActiveContractsByClientId(@Param("clientId") Long clientId);

    // Contrats crÃƒÂ©ÃƒÂ©s dans une pÃƒÂ©riode
    @Query("SELECT c FROM Contract c WHERE " +
           "c.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.createdAt DESC")
    List<Contract> findContractsCreatedBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    // Contrats par commercial
    List<Contract> findByMagsceneSignatory(String signatory);

    @Query("SELECT c.magsceneSignatory, COUNT(c), SUM(c.totalAmount) " +
           "FROM Contract c " +
           "WHERE c.magsceneSignatory IS NOT NULL " +
           "GROUP BY c.magsceneSignatory")
    List<Object[]> getPerformanceBySalesRep();

    // Pipeline de contrats (en nÃƒÂ©gociation, en attente signature)
    @Query("SELECT c FROM Contract c WHERE " +
           "c.status IN ('DRAFT', 'PENDING_SIGNATURE') " +
           "ORDER BY c.totalAmount DESC")
    List<Contract> getContractPipeline();

    // Valeur du pipeline
    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE " +
           "c.status IN ('DRAFT', 'PENDING_SIGNATURE')")
    BigDecimal getPipelineValue();

    // Contrats par secteur d'activitÃƒÂ© du client
    @Query("SELECT cl.businessSector, COUNT(c), SUM(c.totalAmount) " +
           "FROM Contract c " +
           "JOIN c.client cl " +
           "WHERE cl.businessSector IS NOT NULL " +
           "GROUP BY cl.businessSector")
    List<Object[]> getContractStatsByBusinessSector();

    // Analyse de la rentabilitÃƒÂ©
    @Query("SELECT c.type, AVG(c.totalAmount), COUNT(c) " +
           "FROM Contract c " +
           "WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.type " +
           "ORDER BY AVG(c.totalAmount) DESC")
    List<Object[]> getProfitabilityByContractType();
}

