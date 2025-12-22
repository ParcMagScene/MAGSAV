package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Contact;
import com.magscene.magsav.backend.entity.Client;
import com.magscene.magsav.backend.repository.ContactRepository;
import com.magscene.magsav.backend.repository.ClientRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des contacts clients
 */
@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ClientRepository clientRepository;

    // CRUD Operations
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        try {
            List<Contact> contacts = contactRepository.findAll();
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} contacts", contacts.size());
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        try {
            Optional<Contact> contact = contactRepository.findById(id);
            if (contact.isPresent()) {
                return ResponseEntity.ok(contact.get());
            } else {
                logger.warn("Aucun contact trouvÃƒÂ© avec l'ID : {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du contact {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@Valid @RequestBody Contact contact) {
        try {
            // VÃƒÂ©rifier que le client existe
            if (contact.getClient() != null && contact.getClient().getId() != null) {
                Optional<Client> client = clientRepository.findById(contact.getClient().getId());
                if (!client.isPresent()) {
                    logger.warn("Client inexistant pour le contact : {}", contact.getClient().getId());
                    return ResponseEntity.badRequest().build();
                }
                contact.setClient(client.get());
            } else {
                logger.warn("Tentative de crÃƒÂ©ation d'un contact sans client associÃƒÂ©");
                return ResponseEntity.badRequest().build();
            }

            Contact savedContact = contactRepository.save(contact);
            logger.info("Contact crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s : {} {} (ID: {})", 
                       savedContact.getFirstName(), savedContact.getLastName(), savedContact.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation du contact", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, 
                                                @Valid @RequestBody Contact contactDetails) {
        try {
            Optional<Contact> optionalContact = contactRepository.findById(id);
            if (!optionalContact.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Contact contact = optionalContact.get();
            
            // Mise ÃƒÂ  jour des champs
            contact.setFirstName(contactDetails.getFirstName());
            contact.setLastName(contactDetails.getLastName());
            contact.setJobTitle(contactDetails.getJobTitle());
            contact.setDepartment(contactDetails.getDepartment());
            contact.setEmail(contactDetails.getEmail());
            contact.setPhone(contactDetails.getPhone());
            contact.setMobile(contactDetails.getMobile());
            contact.setDirectPhone(contactDetails.getDirectPhone());
            contact.setType(contactDetails.getType());
            contact.setStatus(contactDetails.getStatus());
            contact.setIsPrimary(contactDetails.getIsPrimary());
            contact.setIsDecisionMaker(contactDetails.getIsDecisionMaker());
            contact.setReceiveMarketing(contactDetails.getReceiveMarketing());
            contact.setBirthDate(contactDetails.getBirthDate());
            contact.setNotes(contactDetails.getNotes());
            contact.setLinkedinProfile(contactDetails.getLinkedinProfile());

            Contact updatedContact = contactRepository.save(contact);
            logger.info("Contact mis ÃƒÂ  jour : {} {} (ID: {})", 
                       updatedContact.getFirstName(), updatedContact.getLastName(), updatedContact.getId());
            return ResponseEntity.ok(updatedContact);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du contact {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        try {
            if (!contactRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            contactRepository.deleteById(id);
            logger.info("Contact supprimÃƒÂ© : {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du contact {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Recherches spÃƒÂ©cialisÃƒÂ©es
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Contact>> getContactsByClient(@PathVariable Long clientId) {
        try {
            List<Contact> contacts = contactRepository.findByClientId(clientId);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts du client {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String term) {
        try {
            List<Contact> contacts = contactRepository.searchByTerm(term);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de contacts avec '{}'", term, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Contact>> getContactsByType(@PathVariable Contact.ContactType type) {
        try {
            List<Contact> contacts = contactRepository.findByType(type);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts par type {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Contact>> getContactsByStatus(@PathVariable Contact.ContactStatus status) {
        try {
            List<Contact> contacts = contactRepository.findByStatus(status);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts par statut {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/primary")
    public ResponseEntity<List<Contact>> getPrimaryContacts() {
        try {
            List<Contact> contacts = contactRepository.findByIsPrimaryTrue();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts principaux", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/decision-makers")
    public ResponseEntity<List<Contact>> getDecisionMakers() {
        try {
            List<Contact> contacts = contactRepository.findByIsDecisionMakerTrue();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des dÃƒÂ©cisionnaires", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/marketing")
    public ResponseEntity<List<Contact>> getMarketingContacts() {
        try {
            List<Contact> contacts = contactRepository.findActiveMarketingContacts();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contacts marketing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}/primary")
    public ResponseEntity<Contact> getPrimaryContactByClient(@PathVariable Long clientId) {
        try {
            Optional<Contact> contact = contactRepository.findPrimaryContactByClientId(clientId);
            if (contact.isPresent()) {
                return ResponseEntity.ok(contact.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du contact principal du client {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistiques
    @GetMapping("/stats/count-by-type")
    public ResponseEntity<List<Object[]>> getCountByType() {
        try {
            List<Object[]> stats = contactRepository.countByType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques par type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/count-by-status")
    public ResponseEntity<List<Object[]>> getCountByStatus() {
        try {
            List<Object[]> stats = contactRepository.countByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques par statut", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Validation
    @GetMapping("/validate-email/{email}")
    public ResponseEntity<Boolean> validateEmail(@PathVariable String email) {
        try {
            boolean exists = contactRepository.existsByEmail(email);
            return ResponseEntity.ok(!exists);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation de l'email {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

