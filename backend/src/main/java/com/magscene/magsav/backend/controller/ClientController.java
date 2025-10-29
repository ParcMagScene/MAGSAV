package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Client;
import com.magscene.magsav.backend.repository.ClientRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des clients
 * Endpoints pour CRUD complet et recherches avancÃƒÂ©es
 */
@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientRepository clientRepository;

    // CRUD Operations
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            List<Client> clients = clientRepository.findAll();
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} clients", clients.size());
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        try {
            Optional<Client> client = clientRepository.findById(id);
            if (client.isPresent()) {
                logger.info("Client trouvÃƒÂ© avec l'ID : {}", id);
                return ResponseEntity.ok(client.get());
            } else {
                logger.warn("Aucun client trouvÃƒÂ© avec l'ID : {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du client {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody Client client) {
        try {
            // VÃƒÂ©rifier l'unicitÃƒÂ© du SIRET
            if (client.getSiretNumber() != null && 
                clientRepository.existsBySiretNumber(client.getSiretNumber())) {
                logger.warn("Tentative de crÃƒÂ©ation d'un client avec un numÃƒÂ©ro SIRET existant: {}", 
                           client.getSiretNumber());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Client savedClient = clientRepository.save(client);
            logger.info("Client crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s : {} (ID: {})", 
                       savedClient.getCompanyName(), savedClient.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation du client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, 
                                              @Valid @RequestBody Client clientDetails) {
        try {
            Optional<Client> optionalClient = clientRepository.findById(id);
            if (!optionalClient.isPresent()) {
                logger.warn("Tentative de mise ÃƒÂ  jour d'un client inexistant : {}", id);
                return ResponseEntity.notFound().build();
            }

            Client client = optionalClient.get();
            
            // Mise ÃƒÂ  jour des champs
            client.setCompanyName(clientDetails.getCompanyName());
            client.setSiretNumber(clientDetails.getSiretNumber());
            client.setVatNumber(clientDetails.getVatNumber());
            client.setType(clientDetails.getType());
            client.setStatus(clientDetails.getStatus());
            client.setCategory(clientDetails.getCategory());
            client.setAddress(clientDetails.getAddress());
            client.setPostalCode(clientDetails.getPostalCode());
            client.setCity(clientDetails.getCity());
            client.setCountry(clientDetails.getCountry());
            client.setEmail(clientDetails.getEmail());
            client.setPhone(clientDetails.getPhone());
            client.setFax(clientDetails.getFax());
            client.setWebsite(clientDetails.getWebsite());
            client.setBusinessSector(clientDetails.getBusinessSector());
            client.setAnnualRevenue(clientDetails.getAnnualRevenue());
            client.setEmployeeCount(clientDetails.getEmployeeCount());
            client.setCreditLimit(clientDetails.getCreditLimit());
            client.setOutstandingAmount(clientDetails.getOutstandingAmount());
            client.setPaymentTermsDays(clientDetails.getPaymentTermsDays());
            client.setPreferredPaymentMethod(clientDetails.getPreferredPaymentMethod());
            client.setNotes(clientDetails.getNotes());
            client.setAssignedSalesRep(clientDetails.getAssignedSalesRep());

            Client updatedClient = clientRepository.save(client);
            logger.info("Client mis ÃƒÂ  jour avec succÃƒÂ¨s : {} (ID: {})", 
                       updatedClient.getCompanyName(), updatedClient.getId());
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du client {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            if (!clientRepository.existsById(id)) {
                logger.warn("Tentative de suppression d'un client inexistant : {}", id);
                return ResponseEntity.notFound().build();
            }

            clientRepository.deleteById(id);
            logger.info("Client supprimÃƒÂ© avec succÃƒÂ¨s : {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du client {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Recherches spÃƒÂ©cialisÃƒÂ©es
    @GetMapping("/search")
    public ResponseEntity<List<Client>> searchClients(@RequestParam String term) {
        try {
            List<Client> clients = clientRepository.searchByTerm(term);
            logger.info("Recherche de clients avec le terme '{}' : {} rÃƒÂ©sultats", term, clients.size());
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de clients avec le terme '{}'", term, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Client>> getClientsByStatus(@PathVariable Client.ClientStatus status) {
        try {
            List<Client> clients = clientRepository.findByStatus(status);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients par statut {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Client>> getClientsByType(@PathVariable Client.ClientType type) {
        try {
            List<Client> clients = clientRepository.findByType(type);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients par type {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Client>> getClientsByCategory(@PathVariable Client.ClientCategory category) {
        try {
            List<Client> clients = clientRepository.findByCategory(category);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients par catÃƒÂ©gorie {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sales-rep/{salesRep}")
    public ResponseEntity<List<Client>> getClientsBySalesRep(@PathVariable String salesRep) {
        try {
            List<Client> clients = clientRepository.findByAssignedSalesRep(salesRep);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients par commercial {}", salesRep, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Client>> getClientsByCity(@PathVariable String city) {
        try {
            List<Client> clients = clientRepository.findByCityIgnoreCase(city);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients par ville {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistiques et analyses
    @GetMapping("/stats/count-by-status")
    public ResponseEntity<List<Object[]>> getCountByStatus() {
        try {
            List<Object[]> stats = clientRepository.countByType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des statistiques par statut", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/count-by-type")
    public ResponseEntity<List<Object[]>> getCountByType() {
        try {
            List<Object[]> stats = clientRepository.countByType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des statistiques par type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/total-outstanding")
    public ResponseEntity<BigDecimal> getTotalOutstandingAmount() {
        try {
            BigDecimal total = clientRepository.getTotalOutstandingAmount();
            return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du montant total impayÃƒÂ©", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active-contracts")
    public ResponseEntity<List<Client>> getClientsWithActiveContracts() {
        try {
            List<Client> clients = clientRepository.findClientsWithActiveContracts();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients avec contrats actifs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent-projects")
    public ResponseEntity<List<Client>> getClientsWithRecentProjects(@RequestParam(defaultValue = "30") int days) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<Client> clients = clientRepository.findClientsWithRecentProjects(since);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des clients avec projets rÃƒÂ©cents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/top-clients")
    public ResponseEntity<List<Client>> getTopClientsByRevenue() {
        try {
            List<Client> clients = clientRepository.findTopClientsByRevenue();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des top clients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/performance")
    public ResponseEntity<List<Object[]>> getClientPerformanceStats() {
        try {
            List<Object[]> stats = clientRepository.getClientPerformanceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des statistiques de performance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Validation
    @GetMapping("/validate-siret/{siret}")
    public ResponseEntity<Boolean> validateSiretNumber(@PathVariable String siret) {
        try {
            boolean exists = clientRepository.existsBySiretNumber(siret);
            return ResponseEntity.ok(!exists);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du numÃƒÂ©ro SIRET {}", siret, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

