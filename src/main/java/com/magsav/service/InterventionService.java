package com.magsav.service;

import com.magsav.repo.InterventionRepository;
import com.magsav.model.InterventionRow;
import com.magsav.util.AppLogger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des interventions
 * Fournit une couche d'abstraction avec validation et statistiques
 */
public final class InterventionService {
    
    private final InterventionRepository interventionRepository;
    
    public InterventionService() {
        this.interventionRepository = new InterventionRepository();
    }
    
    public InterventionService(InterventionRepository interventionRepository) {
        this.interventionRepository = interventionRepository;
    }
    
    /**
     * Récupère toutes les interventions avec nom du produit
     */
    public List<InterventionRow> findAllInterventions() {
        AppLogger.info("service", "InterventionService: Récupération de toutes les interventions");
        return interventionRepository.findAllWithProductName();
    }
    
    /**
     * Récupère les interventions pour un produit spécifique
     */
    public List<InterventionRow> findInterventionsByProduct(long productId) {
        AppLogger.info("service", "InterventionService: Récupération interventions pour produit ID: " + productId);
        return interventionRepository.findByProductId(productId);
    }
    
    /**
     * Vérifie si une intervention est fermée
     */
    public boolean isInterventionClosed(InterventionRow intervention) {
        if (intervention == null) return false;
        String dateSortie = intervention.dateSortie();
        return dateSortie != null && !dateSortie.trim().isEmpty();
    }
    
    /**
     * Compte les interventions ouvertes pour un produit
     */
    public long countOpenInterventions(long productId) {
        List<InterventionRow> interventions = findInterventionsByProduct(productId);
        long openCount = interventions.stream()
            .mapToLong(intervention -> isInterventionClosed(intervention) ? 0 : 1)
            .sum();
        
        AppLogger.info("service", "InterventionService: " + openCount + 
                      " interventions ouvertes pour produit ID: " + productId);
        return openCount;
    }
    
    /**
     * Calcule les statistiques générales des interventions
     */
    public InterventionStatistics calculateStatistics() {
        AppLogger.info("service", "InterventionService: Calcul des statistiques");
        
        List<InterventionRow> allInterventions = findAllInterventions();
        
        long total = allInterventions.size();
        long closed = allInterventions.stream()
            .mapToLong(intervention -> isInterventionClosed(intervention) ? 1 : 0)
            .sum();
        long open = total - closed;
        
        // Statistiques par statut
        Map<String, Long> byStatus = allInterventions.stream()
            .collect(Collectors.groupingBy(
                intervention -> intervention.statut() != null ? intervention.statut() : "Inconnu",
                Collectors.counting()
            ));
        
        AppLogger.info("service", "InterventionService: Statistiques calculées - Total: " + total + 
                      ", Ouvertes: " + open + ", Fermées: " + closed);
        
        return new InterventionStatistics(total, open, closed, byStatus);
    }
    
    /**
     * Trouve les interventions récentes (dernières créées)
     */
    public List<InterventionRow> findRecentInterventions(int limit) {
        AppLogger.info("service", "InterventionService: Récupération des " + limit + " interventions récentes");
        
        List<InterventionRow> allInterventions = findAllInterventions();
        return allInterventions.stream()
            .sorted((a, b) -> Long.compare(b.id(), a.id())) // Tri par ID décroissant
            .limit(limit)
            .toList();
    }
    
    /**
     * Trouve les produits avec le plus d'interventions
     */
    public Map<String, Long> findProductsWithMostInterventions(int limit) {
        AppLogger.info("service", "InterventionService: Recherche des produits avec le plus d'interventions");
        
        List<InterventionRow> allInterventions = findAllInterventions();
        
        return allInterventions.stream()
            .filter(intervention -> intervention.produitNom() != null)
            .collect(Collectors.groupingBy(
                InterventionRow::produitNom,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }
    
    /**
     * Valide les données d'une intervention
     */
    public ValidationResult validateIntervention(String statut, String panne, String dateEntree) {
        if (statut == null || statut.trim().isEmpty()) {
            return new ValidationResult(false, "Le statut est obligatoire");
        }
        
        if (panne == null || panne.trim().isEmpty()) {
            return new ValidationResult(false, "La description de la panne est obligatoire");
        }
        
        if (dateEntree == null || dateEntree.trim().isEmpty()) {
            return new ValidationResult(false, "La date d'entrée est obligatoire");
        }
        
        return new ValidationResult(true, "Intervention valide");
    }
    
    /**
     * Record pour les statistiques des interventions
     */
    public record InterventionStatistics(
        long total,
        long open,
        long closed,
        Map<String, Long> byStatus
    ) {}
    
    /**
     * Record pour les résultats de validation
     */
    public record ValidationResult(
        boolean isValid,
        String message
    ) {}
}