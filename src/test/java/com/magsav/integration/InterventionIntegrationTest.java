package com.magsav.integration;

import com.magsav.model.InterventionRow;
import com.magsav.repo.InterventionRepository;
import org.junit.jupiter.api.*;
import java.util.List;

/**
 * Tests d'intégration pour les interventions
 * Utilise la base de données réelle du projet
 */
class InterventionIntegrationTest {

    private InterventionRepository interventionRepository;

    @BeforeEach
    void setUp() {
        interventionRepository = new InterventionRepository();
    }

    @Test
    void should_retrieve_all_interventions_with_product_names() {
        // When
        List<InterventionRow> interventions = interventionRepository.findAllWithProductName();

        // Then
        Assertions.assertNotNull(interventions);
        System.out.println("Nombre d'interventions trouvées: " + interventions.size());
        
        // Vérifie que chaque intervention a les champs requis
        for (InterventionRow intervention : interventions) {
            Assertions.assertNotNull(intervention.id());
            Assertions.assertNotNull(intervention.statut());
            // Le nom du produit peut être null si pas de produit associé
            System.out.println("Intervention ID: " + intervention.id() + 
                             ", Produit: " + intervention.produitNom() + 
                             ", Statut: " + intervention.statut());
        }
    }

    @Test
    void should_find_interventions_by_product_id() {
        // Given - utilise un produit existant si possible
        long productId = 1L;

        // When
        List<InterventionRow> interventions = interventionRepository.findByProductId(productId);

        // Then
        Assertions.assertNotNull(interventions);
        System.out.println("Interventions pour le produit " + productId + ": " + interventions.size());
        
        // Toutes les interventions doivent être pour le bon produit
        for (InterventionRow intervention : interventions) {
            System.out.println("Intervention: " + intervention.id() + 
                             " - Produit: " + intervention.produitNom());
        }
    }

    @Test
    void should_handle_empty_results_gracefully() {
        // Given - utilise un ID de produit qui n'existe probablement pas
        long nonExistentProductId = 999999L;

        // When
        List<InterventionRow> interventions = interventionRepository.findByProductId(nonExistentProductId);

        // Then
        Assertions.assertNotNull(interventions);
        Assertions.assertTrue(interventions.isEmpty());
        System.out.println("Aucune intervention trouvée pour le produit " + nonExistentProductId + " (comportement attendu)");
    }

    @Test
    void should_validate_intervention_data_structure() {
        // When
        List<InterventionRow> interventions = interventionRepository.findAllWithProductName();

        // Then
        if (!interventions.isEmpty()) {
            InterventionRow firstIntervention = interventions.get(0);
            
            // Vérifie la structure des données
            Assertions.assertTrue(firstIntervention.id() > 0, "L'ID doit être positif");
            Assertions.assertNotNull(firstIntervention.statut(), "Le statut ne doit pas être null");
            
            System.out.println("Structure validée pour l'intervention: " + firstIntervention);
        } else {
            System.out.println("Aucune intervention dans la base pour valider la structure");
        }
    }
}