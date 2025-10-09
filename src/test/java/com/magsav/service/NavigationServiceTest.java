package com.magsav.service;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour NavigationService
 * Tests désactivés car ils nécessitent un environnement graphique complet
 */
@Disabled("Tests JavaFX nécessitent un environnement graphique complet - à exécuter manuellement")
public class NavigationServiceTest {

    @Test
    @DisplayName("Doit ouvrir la fenêtre des catégories")
    void testOpenCategories() {
        // Given - Aucune préparation nécessaire pour les méthodes statiques
        
        // When
        assertDoesNotThrow(() -> NavigationService.openCategories());
        
        // Then - Vérifier que l'appel ne lève pas d'exception
        // Note: Sans mocking de Views, on ne peut que vérifier l'absence d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre des fabricants")
    void testOpenManufacturers() {
        // When
        assertDoesNotThrow(() -> NavigationService.openManufacturers());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre des fournisseurs")
    void testOpenSuppliers() {
        // When
        assertDoesNotThrow(() -> NavigationService.openSuppliers());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre des clients")
    void testOpenClients() {
        // When
        assertDoesNotThrow(() -> NavigationService.openClients());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre SAV externe")
    void testOpenExternalSav() {
        // When
        assertDoesNotThrow(() -> NavigationService.openExternalSav());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre des demandes de pièces")
    void testOpenRequestsParts() {
        // When
        assertDoesNotThrow(() -> NavigationService.openRequestsParts());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir la fenêtre des demandes d'équipement")
    void testOpenRequestsEquipment() {
        // When
        assertDoesNotThrow(() -> NavigationService.openRequestsEquipment());
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir le détail produit avec ID valide")
    void testOpenProductDetailWithValidId() {
        // Given
        Long productId = 123L;
        
        // When
        assertDoesNotThrow(() -> NavigationService.openProductDetail(productId));
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit gérer gracieusement un ID produit null")
    void testOpenProductDetailWithNullId() {
        // When
        assertDoesNotThrow(() -> NavigationService.openProductDetail(null));
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception même avec null
    }

    @Test
    @DisplayName("Doit ouvrir nouvelle intervention avec ID produit")
    void testOpenNewInterventionWithProductId() {
        // Given
        Long productId = 456L;
        
        // When
        assertDoesNotThrow(() -> NavigationService.openNewIntervention(productId));
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit ouvrir nouvelle intervention sans ID produit")
    void testOpenNewInterventionWithoutProductId() {
        // When
        assertDoesNotThrow(() -> NavigationService.openNewIntervention(null));
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception
    }

    @Test
    @DisplayName("Doit gérer la fermeture de fenêtre avec node null")
    void testCloseCurrentWindowWithNullNode() {
        // When
        assertDoesNotThrow(() -> NavigationService.closeCurrentWindow(null));
        
        // Then
        // Le test vérifie que l'appel ne lève pas d'exception avec null
    }

    @Test
    @DisplayName("Doit confirmer la fermeture sans changements non sauvegardés")
    void testConfirmCloseWithoutUnsavedChanges() {
        // When
        boolean result = NavigationService.confirmClose("Test Window", false);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Doit confirmer la fermeture avec changements non sauvegardés")
    void testConfirmCloseWithUnsavedChanges() {
        // When
        boolean result = NavigationService.confirmClose("Test Window", true);
        
        // Then
        // Pour l'instant, retourne toujours true (TODO implémenté plus tard)
        assertTrue(result);
    }
}