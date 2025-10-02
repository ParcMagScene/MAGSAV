package com.magsav.workflow;

import java.util.*;

/**
 * Gestionnaire des transitions de statuts pour les dossiers SAV Implémente les règles métier et les
 * contrôles d'accès selon les rôles
 */
public class StatutWorkflow {

  /** Énumération des statuts possibles avec leurs libellés */
  public enum Statut {
    RECU("recu", "Reçu", "Le dossier a été reçu et attend traitement"),
    DIAGNOSTIC("diagnostic", "Diagnostic", "Diagnostic en cours"),
    EN_COURS("en_cours", "En cours", "Réparation en cours"),
    ATTENTE_PIECES("attente_pieces", "Attente pièces", "En attente de pièces détachées"),
    REPARATION("reparation", "Réparation", "Réparation en cours"),
    TESTE("teste", "Testé", "Réparation terminée, tests effectués"),
    PRET("pret", "Prêt", "Prêt à être récupéré"),
    TERMINE("termine", "Terminé", "Dossier terminé et récupéré"),
    ANNULE("annule", "Annulé", "Dossier annulé");

    private final String code;
    private final String libelle;
    private final String description;

    Statut(String code, String libelle, String description) {
      this.code = code;
      this.libelle = libelle;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getLibelle() {
      return libelle;
    }

    public String getDescription() {
      return description;
    }

    public static Statut fromCode(String code) {
      for (Statut statut : values()) {
        if (statut.code.equals(code)) {
          return statut;
        }
      }
      throw new IllegalArgumentException("Statut inconnu : " + code);
    }

    public static String[] getAllCodes() {
      return Arrays.stream(values()).map(Statut::getCode).toArray(String[]::new);
    }
  }

  /** Énumération des rôles utilisateur */
  public enum Role {
    ADMIN,
    USER,
    VIEWER
  }

  // Transitions autorisées depuis chaque statut
  private static final Map<Statut, Set<Statut>> TRANSITIONS_AUTORISEES = new HashMap<>();

  // Transitions autorisées par rôle
  private static final Map<Role, Set<Statut>> STATUTS_ACCESSIBLES_PAR_ROLE = new HashMap<>();

  static {
    // Définition des transitions autorisées
    TRANSITIONS_AUTORISEES.put(
        Statut.RECU, Set.of(Statut.DIAGNOSTIC, Statut.EN_COURS, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(
        Statut.DIAGNOSTIC,
        Set.of(Statut.EN_COURS, Statut.ATTENTE_PIECES, Statut.REPARATION, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(
        Statut.EN_COURS,
        Set.of(Statut.ATTENTE_PIECES, Statut.REPARATION, Statut.TESTE, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(
        Statut.ATTENTE_PIECES, Set.of(Statut.REPARATION, Statut.EN_COURS, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(
        Statut.REPARATION, Set.of(Statut.TESTE, Statut.ATTENTE_PIECES, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(Statut.TESTE, Set.of(Statut.PRET, Statut.REPARATION, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(Statut.PRET, Set.of(Statut.TERMINE, Statut.TESTE, Statut.ANNULE));

    TRANSITIONS_AUTORISEES.put(Statut.TERMINE, Set.of()); // Statut final - aucune transition

    TRANSITIONS_AUTORISEES.put(Statut.ANNULE, Set.of(Statut.RECU)); // Possibilité de réactiver

    // Définition des permissions par rôle
    STATUTS_ACCESSIBLES_PAR_ROLE.put(Role.VIEWER, Set.of()); // Lecture seule

    STATUTS_ACCESSIBLES_PAR_ROLE.put(
        Role.USER,
        Set.of(Statut.DIAGNOSTIC, Statut.EN_COURS, Statut.REPARATION, Statut.TESTE, Statut.PRET));

    STATUTS_ACCESSIBLES_PAR_ROLE.put(Role.ADMIN, Set.of(Statut.values())); // Tous les statuts
  }

  /** Vérifie si une transition est autorisée */
  public static boolean isTransitionAutorisee(String statutActuel, String nouveauStatut) {
    try {
      Statut actuel = Statut.fromCode(statutActuel);
      Statut nouveau = Statut.fromCode(nouveauStatut);
      return TRANSITIONS_AUTORISEES.get(actuel).contains(nouveau);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /** Vérifie si un utilisateur peut changer vers un statut donné */
  public static boolean peutChangerVersStatut(String role, String nouveauStatut) {
    try {
      Role userRole = Role.valueOf(role.toUpperCase().replace("ROLE_", ""));
      Statut statut = Statut.fromCode(nouveauStatut);
      return STATUTS_ACCESSIBLES_PAR_ROLE.get(userRole).contains(statut);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /** Récupère les statuts accessibles depuis un statut donné pour un rôle */
  public static List<Statut> getStatutsAccessibles(String statutActuel, String role) {
    try {
      Statut actuel = Statut.fromCode(statutActuel);
      Role userRole = Role.valueOf(role.toUpperCase().replace("ROLE_", ""));

      Set<Statut> transitionsPossibles = TRANSITIONS_AUTORISEES.get(actuel);
      Set<Statut> statutsAutorisesRole = STATUTS_ACCESSIBLES_PAR_ROLE.get(userRole);

      return transitionsPossibles.stream()
          .filter(statutsAutorisesRole::contains)
          .sorted(Comparator.comparing(Statut::getLibelle))
          .toList();

    } catch (IllegalArgumentException e) {
      return List.of();
    }
  }

  /** Récupère tous les statuts avec leurs libellés */
  public static Map<String, String> getTousLesStatutsAvecLibelles() {
    Map<String, String> result = new LinkedHashMap<>();
    for (Statut statut : Statut.values()) {
      result.put(statut.getCode(), statut.getLibelle());
    }
    return result;
  }

  /** Valide une transition complète (statut + rôle + règles métier) */
  public static ValidationResult validerTransition(
      String statutActuel, String nouveauStatut, String role) {
    // Vérifier que les statuts existent
    try {
      Statut.fromCode(statutActuel);
      Statut.fromCode(nouveauStatut);
    } catch (IllegalArgumentException e) {
      return new ValidationResult(false, "Statut invalide : " + e.getMessage());
    }

    // Vérifier si même statut
    if (statutActuel.equals(nouveauStatut)) {
      return new ValidationResult(
          false, "Le statut est déjà " + Statut.fromCode(statutActuel).getLibelle());
    }

    // Vérifier la transition
    if (!isTransitionAutorisee(statutActuel, nouveauStatut)) {
      return new ValidationResult(
          false,
          String.format(
              "Transition de '%s' vers '%s' non autorisée",
              Statut.fromCode(statutActuel).getLibelle(),
              Statut.fromCode(nouveauStatut).getLibelle()));
    }

    // Vérifier les permissions de rôle
    if (!peutChangerVersStatut(role, nouveauStatut)) {
      return new ValidationResult(
          false,
          String.format(
              "Vous n'avez pas l'autorisation de changer vers le statut '%s'",
              Statut.fromCode(nouveauStatut).getLibelle()));
    }

    return new ValidationResult(true, "Transition autorisée");
  }

  /** Classe de résultat de validation */
  public static class ValidationResult {
    private final boolean valide;
    private final String message;

    public ValidationResult(boolean valide, String message) {
      this.valide = valide;
      this.message = message;
    }

    public boolean isValide() {
      return valide;
    }

    public String getMessage() {
      return message;
    }
  }
}
