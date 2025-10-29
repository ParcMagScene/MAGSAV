package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Personnel;
import com.magscene.magsav.backend.repository.PersonnelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion du Personnel
 */
@RestController
@RequestMapping("/api/personnel")
@CrossOrigin(origins = "*")
public class PersonnelController {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonnelController.class);
    
    @Autowired
    private PersonnelRepository personnelRepository;
    
    /**
     * RÃƒÂ©cupÃƒÂ©rer tout le personnel
     */
    @GetMapping
    public List<Personnel> getAllPersonnel() {
        logger.info("RÃƒÂ©cupÃƒÂ©ration de tout le personnel");
        return personnelRepository.findAll();
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ©rer le personnel actif uniquement
     */
    @GetMapping("/active")
    public List<Personnel> getActivePersonnel() {
        logger.info("RÃƒÂ©cupÃƒÂ©ration du personnel actif");
        return personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE);
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ©rer un membre du personnel par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Personnel> getPersonnelById(@PathVariable Long id) {
        logger.info("RÃƒÂ©cupÃƒÂ©ration du personnel ID: {}", id);
        Optional<Personnel> personnel = personnelRepository.findById(id);
        
        if (personnel.isPresent()) {
            return ResponseEntity.ok(personnel.get());
        } else {
            logger.warn("Personnel non trouvÃƒÂ© avec l'ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * CrÃƒÂ©er un nouveau membre du personnel
     */
    @PostMapping
    public ResponseEntity<Personnel> createPersonnel(@Valid @RequestBody Personnel personnel) {
        logger.info("CrÃƒÂ©ation d'un nouveau membre du personnel: {}", personnel.getFullName());
        
        try {
            personnel.setCreatedAt(LocalDateTime.now());
            personnel.setUpdatedAt(LocalDateTime.now());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            logger.info("Personnel crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s - ID: {}", savedPersonnel.getId());
            return ResponseEntity.ok(savedPersonnel);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation du personnel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mettre ÃƒÂ  jour un membre du personnel
     */
    @PutMapping("/{id}")
    public ResponseEntity<Personnel> updatePersonnel(@PathVariable Long id, @Valid @RequestBody Personnel personnelDetails) {
        logger.info("Mise ÃƒÂ  jour du personnel ID: {}", id);
        
        Optional<Personnel> personnelOptional = personnelRepository.findById(id);
        if (personnelOptional.isPresent()) {
            Personnel personnel = personnelOptional.get();
            
            // Mise ÃƒÂ  jour des champs
            personnel.setFirstName(personnelDetails.getFirstName());
            personnel.setLastName(personnelDetails.getLastName());
            personnel.setEmail(personnelDetails.getEmail());
            personnel.setPhone(personnelDetails.getPhone());
            personnel.setType(personnelDetails.getType());
            personnel.setStatus(personnelDetails.getStatus());
            personnel.setJobTitle(personnelDetails.getJobTitle());
            personnel.setDepartment(personnelDetails.getDepartment());
            personnel.setHireDate(personnelDetails.getHireDate());
            personnel.setNotes(personnelDetails.getNotes());
            personnel.setUpdatedAt(LocalDateTime.now());
            
            Personnel updatedPersonnel = personnelRepository.save(personnel);
            logger.info("Personnel mis ÃƒÂ  jour avec succÃƒÂ¨s - ID: {}", updatedPersonnel.getId());
            return ResponseEntity.ok(updatedPersonnel);
        } else {
            logger.warn("Personnel non trouvÃƒÂ© pour mise ÃƒÂ  jour - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Supprimer un membre du personnel
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePersonnel(@PathVariable Long id) {
        logger.info("Suppression du personnel ID: {}", id);
        
        Optional<Personnel> personnelOptional = personnelRepository.findById(id);
        if (personnelOptional.isPresent()) {
            personnelRepository.delete(personnelOptional.get());
            logger.info("Personnel supprimÃƒÂ© avec succÃƒÂ¨s - ID: {}", id);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Personnel non trouvÃƒÂ© pour suppression - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Rechercher le personnel par nom
     */
    @GetMapping("/search")
    public List<Personnel> searchPersonnelByName(@RequestParam String query) {
        logger.info("Recherche de personnel avec: {}", query);
        return personnelRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }
}

