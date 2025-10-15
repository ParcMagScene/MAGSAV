package com.magsav.service;

import com.magsav.model.DemandeElevationPrivilege.StatutDemande;
import com.magsav.model.User.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du système d'élévation de privilèges")
class DemandeElevationPrivilegeServiceTest {

    @Test
    @DisplayName("Test de création du service")
    void testCreationService() {
        DemandeElevationPrivilegeService service = new DemandeElevationPrivilegeService();
        assertNotNull(service);
    }

    @Test
    @DisplayName("Validation de la hiérarchie des rôles par priorité")
    void testHierarchieRoles() {
        assertEquals(1, Role.INTERMITTENT.getPriority());
        assertEquals(2, Role.CHAUFFEUR_PL.getPriority());
        assertEquals(2, Role.CHAUFFEUR_SPL.getPriority());
        assertEquals(3, Role.TECHNICIEN_MAG_SCENE.getPriority());
        assertEquals(4, Role.ADMIN.getPriority());
        
        // Les rôles avec une priorité plus élevée ont plus de privilèges
        assertTrue(Role.ADMIN.getPriority() > Role.TECHNICIEN_MAG_SCENE.getPriority());
        assertTrue(Role.TECHNICIEN_MAG_SCENE.getPriority() > Role.CHAUFFEUR_PL.getPriority());
        assertTrue(Role.CHAUFFEUR_PL.getPriority() > Role.INTERMITTENT.getPriority());
    }

    @Test
    @DisplayName("Validation des labels de rôles français")
    void testLabelsRolesFrancais() {
        assertEquals("Intermittent", Role.INTERMITTENT.getLabel());
        assertEquals("Technicien Mag Scène", Role.TECHNICIEN_MAG_SCENE.getLabel());
        assertEquals("Administrateur", Role.ADMIN.getLabel());
    }

    @Test
    @DisplayName("Validation des statuts de demande")
    void testStatutsDemande() {
        assertEquals("En attente", StatutDemande.EN_ATTENTE.getLabel());
        assertEquals("Approuvée", StatutDemande.APPROUVEE.getLabel());
        assertEquals("Rejetée", StatutDemande.REJETEE.getLabel());
        assertEquals("Expirée", StatutDemande.EXPIREE.getLabel());
    }
}