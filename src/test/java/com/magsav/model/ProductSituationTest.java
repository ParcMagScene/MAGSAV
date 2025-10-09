package com.magsav.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour l'énumération ProductSituation
 */
class ProductSituationTest {

    @Test
    @DisplayName("Doit valider toutes les situations autorisées")
    void testValidSituations() {
        String[] expectedSituations = {"En stock", "En service", "En réparation", "SAV Mag Scene", "SAV Externe", "Vendu", "Déchet"};
        
        for (String situation : expectedSituations) {
            assertTrue(ProductSituation.isValid(situation), 
                "La situation '" + situation + "' devrait être valide");
        }
    }

    @Test
    @DisplayName("Doit rejeter les situations non autorisées")
    void testInvalidSituations() {
        String[] invalidSituations = {"SAV Mag", "Perdu", "Stock", null, ""};
        
        for (String situation : invalidSituations) {
            assertFalse(ProductSituation.isValid(situation), 
                "La situation '" + situation + "' ne devrait pas être valide");
        }
    }

    @Test
    @DisplayName("Doit retourner la situation par défaut pour un label invalide")
    void testFromLabelWithInvalidInput() {
        assertEquals(ProductSituation.EN_STOCK, ProductSituation.fromLabel("Situation invalide"));
        assertEquals(ProductSituation.EN_STOCK, ProductSituation.fromLabel(null));
        assertEquals(ProductSituation.EN_STOCK, ProductSituation.fromLabel(""));
    }

    @Test
    @DisplayName("Doit retourner la situation correcte pour un label valide")
    void testFromLabelWithValidInput() {
        assertEquals(ProductSituation.EN_STOCK, ProductSituation.fromLabel("En stock"));
        assertEquals(ProductSituation.SAV_MAG_SCENE, ProductSituation.fromLabel("SAV Mag Scene"));
        assertEquals(ProductSituation.SAV_EXTERNE, ProductSituation.fromLabel("SAV Externe"));
        assertEquals(ProductSituation.VENDU, ProductSituation.fromLabel("Vendu"));
        assertEquals(ProductSituation.DECHET, ProductSituation.fromLabel("Déchet"));
    }

    @Test
    @DisplayName("Doit retourner tous les labels autorisés")
    void testGetAllLabels() {
        String[] labels = ProductSituation.getAllLabels();
        
        assertEquals(7, labels.length);
        assertArrayEquals(new String[]{"En stock", "En service", "En réparation", "SAV Mag Scene", "SAV Externe", "Vendu", "Déchet"}, labels);
    }

    @Test
    @DisplayName("Doit avoir les bons labels pour chaque situation")
    void testGetLabel() {
        assertEquals("En stock", ProductSituation.EN_STOCK.getLabel());
        assertEquals("En service", ProductSituation.EN_SERVICE.getLabel());
        assertEquals("En réparation", ProductSituation.EN_REPARATION.getLabel());
        assertEquals("SAV Mag Scene", ProductSituation.SAV_MAG_SCENE.getLabel());
        assertEquals("SAV Externe", ProductSituation.SAV_EXTERNE.getLabel());
        assertEquals("Vendu", ProductSituation.VENDU.getLabel());
        assertEquals("Déchet", ProductSituation.DECHET.getLabel());
    }

    @Test
    @DisplayName("Doit retourner le label dans toString()")
    void testToString() {
        assertEquals("En stock", ProductSituation.EN_STOCK.toString());
        assertEquals("En service", ProductSituation.EN_SERVICE.toString());
        assertEquals("En réparation", ProductSituation.EN_REPARATION.toString());
        assertEquals("SAV Mag Scene", ProductSituation.SAV_MAG_SCENE.toString());
        assertEquals("SAV Externe", ProductSituation.SAV_EXTERNE.toString());
        assertEquals("Vendu", ProductSituation.VENDU.toString());
        assertEquals("Déchet", ProductSituation.DECHET.toString());
    }
}