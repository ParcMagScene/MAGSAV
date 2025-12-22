package com.magscene.magsav.backend.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service de nettoyage des données au démarrage
 * Nettoie les codes LOCMAT en supprimant les caractères "*"
 */
@Service
public class DataCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCleanupService.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public DataCleanupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @PostConstruct
    public void cleanupData() {
        cleanLocmatCodes();
    }
    
    /**
     * Nettoie les codes LOCMAT en supprimant les caractères '*'
     */
    public int cleanLocmatCodes() {
        try {
            String sql = "UPDATE equipment SET internal_reference = TRIM(REPLACE(internal_reference, '*', '')) " +
                        "WHERE internal_reference IS NOT NULL AND internal_reference LIKE '%*%'";
            
            int updatedRows = jdbcTemplate.update(sql);
            
            if (updatedRows > 0) {
                logger.info("✅ Nettoyage LOCMAT: {} codes nettoyés (suppression des '*')", updatedRows);
            } else {
                logger.debug("✅ Nettoyage LOCMAT: aucun code à nettoyer");
            }
            
            return updatedRows;
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage des codes LOCMAT: {}", e.getMessage());
            return 0;
        }
    }
}
