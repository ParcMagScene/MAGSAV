package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * Repository pour l'entitÃƒÂ© Personnel
 */
@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
    
    /**
     * Trouve le personnel par statut
     */
    List<Personnel> findByStatus(Personnel.PersonnelStatus status);
    
    /**
     * Trouve le personnel par type
     */
    List<Personnel> findByType(Personnel.PersonnelType type);
    
    /**
     * Recherche par nom (prÃƒÂ©nom ou nom de famille)
     */
    List<Personnel> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    /**
     * Trouve par email
     */
    Personnel findByEmail(String email);
    
    /**
     * Trouve par dÃƒÂ©partement
     */
    List<Personnel> findByDepartment(String department);
}

