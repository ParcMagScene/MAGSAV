package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.ContractItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité ContractItem
 */
@Repository
public interface ContractItemRepository extends JpaRepository<ContractItem, Long> {
    
    /**
     * Recherche par équipement
     */
    List<ContractItem> findByEquipmentId(Long equipmentId);
    
    /**
     * Recherche par contrat
     */
    List<ContractItem> findByContractId(Long contractId);
}
