package com.magscene.magsav.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Générateur de données de test pour MAGSAV 3.0
 * DÉSACTIVÉ temporairement en raison de conflits d'entités JPA
 * 
 * Architecture à résoudre :
 * - Phase 1-5 : Entités Supplier/MaterialRequest dans common-models
 * - Backend original : Entités Project/SupplierOrder dans backend.entity
 * - 3 entités dupliquées : Project, SupplierOrder, SupplierOrderItem
 * 
 * Solutions possibles :
 * 1. Renommer les entités dupliquées dans common-models
 * 2. Migrer Phase 1-5 vers backend.entity
 * 3. Consolider les deux systèmes
 */
@Component
public class TestDataGenerator implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    
    @Override
    public void run(String... args) {
        logger.info("⚠️ TestDataGenerator désactivé - Résolution conflits d'entités requise");
        logger.info("   📚 Documentation: Voir commentaires de classe");
    }
}
