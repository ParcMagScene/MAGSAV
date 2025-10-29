package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Contract;
import com.magscene.magsav.backend.entity.Client;
import com.magscene.magsav.backend.repository.ContractRepository;
import com.magscene.magsav.backend.repository.ClientRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * ContrÃƒÂ´leur REST pour la gestion des contrats
 */
@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ClientRepository clientRepository;

    // CRUD Operations
    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        try {
            List<Contract> contracts = contractRepository.findAll();
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} contrats", contracts.size());
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id) {
        try {
            Optional<Contract> contract = contractRepository.findById(id);
            if (contract.isPresent()) {
                return ResponseEntity.ok(contract.get());
            } else {
                logger.warn("Aucun contrat trouvÃƒÂ© avec l'ID : {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du contrat {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Contract> createContract(@Valid @RequestBody Contract contract) {
        try {
            // VÃƒÂ©rifier l'unicitÃƒÂ© du numÃƒÂ©ro de contrat
            if (contractRepository.existsByContractNumber(contract.getContractNumber())) {
                logger.warn("NumÃƒÂ©ro de contrat dÃƒÂ©jÃƒÂ  existant : {}", contract.getContractNumber());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // VÃƒÂ©rifier que le client existe
            if (contract.getClient() != null && contract.getClient().getId() != null) {
                Optional<Client> client = clientRepository.findById(contract.getClient().getId());
                if (!client.isPresent()) {
                    logger.warn("Client inexistant pour le contrat : {}", contract.getClient().getId());
                    return ResponseEntity.badRequest().build();
                }
                contract.setClient(client.get());
            } else {
                logger.warn("Tentative de crÃƒÂ©ation d'un contrat sans client associÃƒÂ©");
                return ResponseEntity.badRequest().build();
            }

            Contract savedContract = contractRepository.save(contract);
            logger.info("Contrat crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s : {} (ID: {})", 
                       savedContract.getContractNumber(), savedContract.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContract);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation du contrat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> updateContract(@PathVariable Long id, 
                                                  @Valid @RequestBody Contract contractDetails) {
        try {
            Optional<Contract> optionalContract = contractRepository.findById(id);
            if (!optionalContract.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Contract contract = optionalContract.get();
            
            // Mise ÃƒÂ  jour des champs
            contract.setTitle(contractDetails.getTitle());
            contract.setType(contractDetails.getType());
            contract.setStatus(contractDetails.getStatus());
            contract.setStartDate(contractDetails.getStartDate());
            contract.setEndDate(contractDetails.getEndDate());
            contract.setSignatureDate(contractDetails.getSignatureDate());
            contract.setTotalAmount(contractDetails.getTotalAmount());
            contract.setMonthlyAmount(contractDetails.getMonthlyAmount());
            contract.setInvoicedAmount(contractDetails.getInvoicedAmount());
            contract.setBillingFrequency(contractDetails.getBillingFrequency());
            contract.setPaymentTerms(contractDetails.getPaymentTerms());
            contract.setIsAutoRenewable(contractDetails.getIsAutoRenewable());
            contract.setRenewalPeriodMonths(contractDetails.getRenewalPeriodMonths());
            contract.setNoticePeriodDays(contractDetails.getNoticePeriodDays());
            contract.setDescription(contractDetails.getDescription());
            contract.setTermsAndConditions(contractDetails.getTermsAndConditions());
            contract.setNotes(contractDetails.getNotes());
            contract.setClientSignatory(contractDetails.getClientSignatory());
            contract.setMagsceneSignatory(contractDetails.getMagsceneSignatory());
            contract.setContractFilePath(contractDetails.getContractFilePath());

            Contract updatedContract = contractRepository.save(contract);
            logger.info("Contrat mis ÃƒÂ  jour : {} (ID: {})", 
                       updatedContract.getContractNumber(), updatedContract.getId());
            return ResponseEntity.ok(updatedContract);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du contrat {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        try {
            if (!contractRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            contractRepository.deleteById(id);
            logger.info("Contrat supprimÃƒÂ© : {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du contrat {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Recherches spÃƒÂ©cialisÃƒÂ©es
    @GetMapping("/search")
    public ResponseEntity<List<Contract>> searchContracts(@RequestParam String term) {
        try {
            List<Contract> contracts = contractRepository.searchByTerm(term);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de contrats avec '{}'", term, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Contract>> getContractsByClient(@PathVariable Long clientId) {
        try {
            List<Contract> contracts = contractRepository.findByClientId(clientId);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats du client {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Contract>> getContractsByType(@PathVariable Contract.ContractType type) {
        try {
            List<Contract> contracts = contractRepository.findByType(type);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats par type {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Contract>> getContractsByStatus(@PathVariable Contract.ContractStatus status) {
        try {
            List<Contract> contracts = contractRepository.findByStatus(status);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats par statut {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Contract>> getExpiringContracts(@RequestParam(defaultValue = "30") int days) {
        try {
            LocalDate futureDate = LocalDate.now().plusDays(days);
            List<Contract> contracts = contractRepository.findExpiringContracts(futureDate);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats expirant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/expired")
    public ResponseEntity<List<Contract>> getExpiredContracts() {
        try {
            List<Contract> contracts = contractRepository.findExpiredContracts();
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats expirÃƒÂ©s", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/renewable")
    public ResponseEntity<List<Contract>> getRenewableContracts(@RequestParam(defaultValue = "60") int days) {
        try {
            LocalDate futureDate = LocalDate.now().plusDays(days);
            List<Contract> contracts = contractRepository.findContractsForRenewal(futureDate);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des contrats renouvelables", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pipeline")
    public ResponseEntity<List<Contract>> getContractPipeline() {
        try {
            List<Contract> contracts = contractRepository.getContractPipeline();
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du pipeline de contrats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistiques et analyses
    @GetMapping("/stats/count-by-type")
    public ResponseEntity<List<Object[]>> getCountByType() {
        try {
            List<Object[]> stats = contractRepository.countByType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques par type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/count-by-status")
    public ResponseEntity<List<Object[]>> getCountByStatus() {
        try {
            List<Object[]> stats = contractRepository.countByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques par statut", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/total-active-value")
    public ResponseEntity<BigDecimal> getTotalActiveValue() {
        try {
            BigDecimal total = contractRepository.getTotalActiveContractsValue();
            return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul de la valeur totale active", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/pipeline-value")
    public ResponseEntity<BigDecimal> getPipelineValue() {
        try {
            BigDecimal value = contractRepository.getPipelineValue();
            return ResponseEntity.ok(value != null ? value : BigDecimal.ZERO);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul de la valeur du pipeline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/performance-by-client")
    public ResponseEntity<List<Object[]>> getPerformanceByClient() {
        try {
            List<Object[]> stats = contractRepository.getContractPerformanceByClient();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des performances par client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/by-month")
    public ResponseEntity<List<Object[]>> getStatsByMonth() {
        try {
            List<Object[]> stats = contractRepository.getContractStatsByMonth();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des statistiques mensuelles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Validation
    @GetMapping("/validate-number/{contractNumber}")
    public ResponseEntity<Boolean> validateContractNumber(@PathVariable String contractNumber) {
        try {
            boolean exists = contractRepository.existsByContractNumber(contractNumber);
            return ResponseEntity.ok(!exists);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du numÃƒÂ©ro de contrat {}", contractNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

