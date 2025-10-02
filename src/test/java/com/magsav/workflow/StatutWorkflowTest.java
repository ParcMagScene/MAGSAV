package com.magsav.workflow;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests pour le système de workflow de statuts */
class StatutWorkflowTest {

  @Test
  void testTransitionsAutorisees() {
    // Transitions normales autorisées
    assertTrue(StatutWorkflow.isTransitionAutorisee("recu", "diagnostic"));
    assertTrue(StatutWorkflow.isTransitionAutorisee("diagnostic", "en_cours"));
    assertTrue(StatutWorkflow.isTransitionAutorisee("en_cours", "reparation"));
    assertTrue(StatutWorkflow.isTransitionAutorisee("reparation", "teste"));
    assertTrue(StatutWorkflow.isTransitionAutorisee("teste", "pret"));
    assertTrue(StatutWorkflow.isTransitionAutorisee("pret", "termine"));

    // Transitions interdites
    assertFalse(StatutWorkflow.isTransitionAutorisee("recu", "termine"));
    assertFalse(StatutWorkflow.isTransitionAutorisee("termine", "recu"));
    assertFalse(StatutWorkflow.isTransitionAutorisee("pret", "diagnostic"));
  }

  @Test
  void testPermissionsParRole() {
    // Admin peut tout faire
    assertTrue(StatutWorkflow.peutChangerVersStatut("ROLE_ADMIN", "annule"));
    assertTrue(StatutWorkflow.peutChangerVersStatut("ROLE_ADMIN", "diagnostic"));

    // User ne peut pas annuler
    assertFalse(StatutWorkflow.peutChangerVersStatut("ROLE_USER", "annule"));
    assertTrue(StatutWorkflow.peutChangerVersStatut("ROLE_USER", "diagnostic"));

    // Viewer ne peut rien changer
    assertFalse(StatutWorkflow.peutChangerVersStatut("ROLE_VIEWER", "diagnostic"));
  }

  @Test
  void testValidationComplete() {
    // Transition valide avec admin
    StatutWorkflow.ValidationResult result1 =
        StatutWorkflow.validerTransition("recu", "diagnostic", "ROLE_ADMIN");
    assertTrue(result1.isValide());

    // Transition invalide (statut final)
    StatutWorkflow.ValidationResult result2 =
        StatutWorkflow.validerTransition("termine", "recu", "ROLE_ADMIN");
    assertFalse(result2.isValide());

    // Permission insuffisante
    StatutWorkflow.ValidationResult result3 =
        StatutWorkflow.validerTransition("recu", "annule", "ROLE_USER");
    assertFalse(result3.isValide());
    assertTrue(result3.getMessage().contains("autorisation"));
  }

  @Test
  void testStatutsAccessibles() {
    // Admin depuis "recu"
    var statutsAdmin = StatutWorkflow.getStatutsAccessibles("recu", "ROLE_ADMIN");
    assertTrue(statutsAdmin.stream().anyMatch(s -> "diagnostic".equals(s.getCode())));
    assertTrue(statutsAdmin.stream().anyMatch(s -> "annule".equals(s.getCode())));

    // User depuis "recu"
    var statutsUser = StatutWorkflow.getStatutsAccessibles("recu", "ROLE_USER");
    assertTrue(statutsUser.stream().anyMatch(s -> "diagnostic".equals(s.getCode())));
    assertFalse(statutsUser.stream().anyMatch(s -> "annule".equals(s.getCode())));

    // Viewer ne peut rien
    var statutsViewer = StatutWorkflow.getStatutsAccessibles("recu", "ROLE_VIEWER");
    assertTrue(statutsViewer.isEmpty());
  }

  @Test
  void testGestionErreurs() {
    // Statut inexistant
    StatutWorkflow.ValidationResult result1 =
        StatutWorkflow.validerTransition("inexistant", "recu", "ROLE_ADMIN");
    assertFalse(result1.isValide());
    assertTrue(result1.getMessage().contains("invalide"));

    // Même statut
    StatutWorkflow.ValidationResult result2 =
        StatutWorkflow.validerTransition("recu", "recu", "ROLE_ADMIN");
    assertFalse(result2.isValide());
    assertTrue(result2.getMessage().contains("déjà"));
  }

  @Test
  void testLibellesStatuts() {
    assertEquals("Reçu", StatutWorkflow.Statut.RECU.getLibelle());
    assertEquals("En cours", StatutWorkflow.Statut.EN_COURS.getLibelle());
    assertEquals("Terminé", StatutWorkflow.Statut.TERMINE.getLibelle());

    var tousStatuts = StatutWorkflow.getTousLesStatutsAvecLibelles();
    assertEquals("Reçu", tousStatuts.get("recu"));
    assertEquals("Diagnostic", tousStatuts.get("diagnostic"));
  }

  @Test
  void testFromCode() {
    assertEquals(StatutWorkflow.Statut.RECU, StatutWorkflow.Statut.fromCode("recu"));
    assertEquals(StatutWorkflow.Statut.DIAGNOSTIC, StatutWorkflow.Statut.fromCode("diagnostic"));

    assertThrows(
        IllegalArgumentException.class, () -> StatutWorkflow.Statut.fromCode("inexistant"));
  }
}
