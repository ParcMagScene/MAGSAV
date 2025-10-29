package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Contact;
import com.magscene.magsav.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des contacts clients
 * MÃƒÂ©thodes de recherche et statistiques pour les contacts
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Recherches par client
    List<Contact> findByClient(Client client);
    
    List<Contact> findByClientId(Long clientId);
    
    List<Contact> findByClientAndStatus(Client client, Contact.ContactStatus status);

    // Recherches par informations personnelles
    List<Contact> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    Optional<Contact> findByEmail(String email);
    
    List<Contact> findByEmailContainingIgnoreCase(String email);
    
    List<Contact> findByPhoneOrMobileOrDirectPhone(String phone, String mobile, String directPhone);

    // Recherches par rÃƒÂ´le et statut
    List<Contact> findByStatus(Contact.ContactStatus status);
    
    List<Contact> findByType(Contact.ContactType type);
    
    List<Contact> findByIsPrimaryTrue();
    
    List<Contact> findByIsDecisionMakerTrue();
    
    List<Contact> findByReceiveMarketingTrue();

    // Recherches par poste et dÃƒÂ©partement
    List<Contact> findByJobTitleContainingIgnoreCase(String jobTitle);
    
    List<Contact> findByDepartmentContainingIgnoreCase(String department);

    // Recherches combinÃƒÂ©es
    @Query("SELECT c FROM Contact c WHERE " +
           "(:clientId IS NULL OR c.client.id = :clientId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:isPrimary IS NULL OR c.isPrimary = :isPrimary)")
    List<Contact> findByMultipleCriteria(
        @Param("clientId") Long clientId,
        @Param("status") Contact.ContactStatus status,
        @Param("type") Contact.ContactType type,
        @Param("isPrimary") Boolean isPrimary
    );

    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Contact> searchByTerm(@Param("searchTerm") String searchTerm);

    // Contact principal par client
    @Query("SELECT c FROM Contact c WHERE c.client = :client AND c.isPrimary = true")
    Optional<Contact> findPrimaryContactByClient(@Param("client") Client client);
    
    @Query("SELECT c FROM Contact c WHERE c.client.id = :clientId AND c.isPrimary = true")
    Optional<Contact> findPrimaryContactByClientId(@Param("clientId") Long clientId);

    // DÃƒÂ©cisionnaires par client
    @Query("SELECT c FROM Contact c WHERE c.client = :client AND c.isDecisionMaker = true")
    List<Contact> findDecisionMakersByClient(@Param("client") Client client);

    // Contacts actifs pour marketing
    @Query("SELECT c FROM Contact c WHERE " +
           "c.receiveMarketing = true AND " +
           "c.status = 'ACTIVE' AND " +
           "c.email IS NOT NULL")
    List<Contact> findActiveMarketingContacts();

    // Statistiques par type
    @Query("SELECT c.type, COUNT(c) FROM Contact c GROUP BY c.type")
    List<Object[]> countByType();

    @Query("SELECT c.status, COUNT(c) FROM Contact c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("SELECT c.department, COUNT(c) FROM Contact c WHERE c.department IS NOT NULL GROUP BY c.department")
    List<Object[]> countByDepartment();

    // Contacts rÃƒÂ©cents
    List<Contact> findByCreatedAtAfter(LocalDateTime date);
    
    List<Contact> findByUpdatedAtAfter(LocalDateTime date);

    // Contacts par entreprise
    @Query("SELECT c.client.companyName, COUNT(c) FROM Contact c GROUP BY c.client.companyName")
    List<Object[]> countByCompany();

    // VÃƒÂ©rification d'unicitÃƒÂ©
    boolean existsByEmail(String email);

    // Contacts sans activitÃƒÂ© rÃƒÂ©cente
    @Query("SELECT c FROM Contact c WHERE c.updatedAt < :since")
    List<Contact> findContactsNotUpdatedSince(@Param("since") LocalDateTime since);

    // Contacts avec informations incomplÃƒÂ¨tes
    @Query("SELECT c FROM Contact c WHERE " +
           "c.email IS NULL OR " +
           "c.phone IS NULL OR " +
           "c.jobTitle IS NULL")
    List<Contact> findContactsWithIncompleteInfo();

    // Recherche avancÃƒÂ©e avec jointure sur client
    @Query("SELECT c FROM Contact c " +
           "JOIN c.client cl " +
           "WHERE (:clientStatus IS NULL OR cl.status = :clientStatus) AND " +
           "(:clientType IS NULL OR cl.type = :clientType) AND " +
           "(:contactStatus IS NULL OR c.status = :contactStatus)")
    List<Contact> findByClientAndContactCriteria(
        @Param("clientStatus") Client.ClientStatus clientStatus,
        @Param("clientType") Client.ClientType clientType,
        @Param("contactStatus") Contact.ContactStatus contactStatus
    );

    // Contacts techniques par secteur d'activitÃƒÂ©
    @Query("SELECT c FROM Contact c " +
           "JOIN c.client cl " +
           "WHERE c.type = 'TECHNICAL' AND " +
           "LOWER(cl.businessSector) LIKE LOWER(CONCAT('%', :sector, '%'))")
    List<Contact> findTechnicalContactsBySector(@Param("sector") String sector);

    // Contacts commerciaux
    @Query("SELECT c FROM Contact c WHERE c.type IN ('COMMERCIAL', 'DECISION_MAKER')")
    List<Contact> findCommercialContacts();

    // Compter les contacts par client
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.client.id = :clientId")
    Long countByClientId(@Param("clientId") Long clientId);

    // Contacts crÃƒÂ©ÃƒÂ©s dans une pÃƒÂ©riode spÃƒÂ©cifique
    @Query("SELECT c FROM Contact c WHERE " +
           "c.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.createdAt DESC")
    List<Contact> findContactsCreatedBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}

