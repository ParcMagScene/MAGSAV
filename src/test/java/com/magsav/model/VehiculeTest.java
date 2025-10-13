package com.magsav.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle Vehicule
 */
public class VehiculeTest {
    
    @Test
    public void testVehiculeCreation() {
        Vehicule vehicule = new Vehicule();
        assertNotNull(vehicule);
        assertEquals(0, vehicule.getId());
        assertNull(vehicule.getImmatriculation());
    }
    
    @Test
    public void testVehiculeWithData() {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(1);
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setTypeVehicule(Vehicule.TypeVehicule.VL);
        vehicule.setMarque("Renault");
        vehicule.setModele("Kangoo");
        vehicule.setAnnee(2023);
        vehicule.setKilometrage(15000);
        vehicule.setStatut(Vehicule.StatutVehicule.DISPONIBLE);
        vehicule.setLocationExterne(false);
        vehicule.setNotes("Test vehicle");
        
        assertEquals(1, vehicule.getId());
        assertEquals("AB-123-CD", vehicule.getImmatriculation());
        assertEquals(Vehicule.TypeVehicule.VL, vehicule.getTypeVehicule());
        assertEquals("Renault", vehicule.getMarque());
        assertEquals("Kangoo", vehicule.getModele());
        assertEquals(2023, vehicule.getAnnee());
        assertEquals(15000, vehicule.getKilometrage());
        assertEquals(Vehicule.StatutVehicule.DISPONIBLE, vehicule.getStatut());
        assertFalse(vehicule.isLocationExterne());
        assertEquals("Test vehicle", vehicule.getNotes());
    }
    
    @Test
    public void testDisplayName() {
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Renault");
        vehicule.setModele("Kangoo");
        
        String expected = "AB-123-CD - Renault Kangoo";
        assertEquals(expected, vehicule.getDisplayName());
    }
    
    @Test
    public void testDisplayNameWithNulls() {
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("AB-123-CD");
        
        String expected = "AB-123-CD";
        assertEquals(expected, vehicule.getDisplayName());
    }
    
    @Test
    public void testTypeVehiculeEnum() {
        assertEquals("Véhicule Léger", Vehicule.TypeVehicule.VL.getDisplayName());
        assertEquals("Poids Lourd", Vehicule.TypeVehicule.PL.getDisplayName());
        assertEquals("Super Poids Lourd", Vehicule.TypeVehicule.SPL.getDisplayName());
        assertEquals("Remorque", Vehicule.TypeVehicule.REMORQUE.getDisplayName());
        assertEquals("Scène Mobile", Vehicule.TypeVehicule.SCENE_MOBILE.getDisplayName());
    }
    
    @Test
    public void testStatutVehiculeEnum() {
        assertEquals("Disponible", Vehicule.StatutVehicule.DISPONIBLE.getDisplayName());
        assertEquals("En Service", Vehicule.StatutVehicule.EN_SERVICE.getDisplayName());
        assertEquals("En Maintenance", Vehicule.StatutVehicule.MAINTENANCE.getDisplayName());
        assertEquals("Hors Service", Vehicule.StatutVehicule.HORS_SERVICE.getDisplayName());
    }
    
    @Test
    public void testToString() {
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Renault");
        vehicule.setModele("Kangoo");
        
        String result = vehicule.toString();
        assertTrue(result.contains("AB-123-CD"));
        assertTrue(result.contains("Renault"));
        assertTrue(result.contains("Kangoo"));
    }
}