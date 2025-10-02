package com.magsav.validation;

import com.magsav.workflow.StatutWorkflow;
import java.util.regex.Pattern;

/** Utilitaire de validation pour les données d'entrée */
public class ValidationUtils {

  // Regex pour validation email (RFC 5322 simplifié)
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  // Regex pour validation des noms (lettres, espaces, tirets, apostrophes)
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$");

  // Regex pour validation numéro de téléphone français
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("^(?:(?:\\+33|0)[1-9](?:[0-9]{8}))$");

  // Regex pour validation numéro de série (lettres + chiffres)
  private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-_]+$");

  // Constantes pour les tailles maximales
  public static final int MAX_NAME_LENGTH = 100;
  public static final int MAX_EMAIL_LENGTH = 255;
  public static final int MAX_PHONE_LENGTH = 20;
  public static final int MAX_ADDRESS_LENGTH = 500;
  public static final int MAX_DESCRIPTION_LENGTH = 2000;
  public static final int MAX_SERIAL_LENGTH = 50;
  public static final int MAX_BRAND_LENGTH = 100;
  public static final int MAX_MODEL_LENGTH = 100;
  public static final int MAX_SIRET_LENGTH = 14;

  // Taille maximale des fichiers uploadés (5MB)
  public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  // Statuts autorisés pour les dossiers SAV (utilisé pour la validation de base)
  public static final String[] STATUTS_AUTORISES = StatutWorkflow.Statut.getAllCodes();

  /** Valide une adresse email */
  public static boolean isValidEmail(String email) {
    return email != null
        && !email.trim().isEmpty()
        && email.length() <= MAX_EMAIL_LENGTH
        && EMAIL_PATTERN.matcher(email.trim()).matches();
  }

  /** Valide un nom ou prénom */
  public static boolean isValidName(String name) {
    return name != null
        && !name.trim().isEmpty()
        && name.length() <= MAX_NAME_LENGTH
        && NAME_PATTERN.matcher(name.trim()).matches();
  }

  /** Valide un numéro de téléphone */
  public static boolean isValidPhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      return true; // Optionnel
    }
    return phone.length() <= MAX_PHONE_LENGTH
        && PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
  }

  /** Valide un numéro de série */
  public static boolean isValidSerialNumber(String serial) {
    return serial != null
        && !serial.trim().isEmpty()
        && serial.length() <= MAX_SERIAL_LENGTH
        && SERIAL_NUMBER_PATTERN.matcher(serial.trim()).matches();
  }

  /** Valide la longueur d'un champ texte */
  public static boolean isValidLength(String text, int maxLength) {
    return text == null || text.length() <= maxLength;
  }

  /** Valide qu'un champ requis n'est pas vide */
  public static boolean isNotEmpty(String text) {
    return text != null && !text.trim().isEmpty();
  }

  /** Valide un statut SAV */
  public static boolean isValidStatut(String statut) {
    if (statut == null || statut.trim().isEmpty()) {
      return false;
    }
    for (String statutAutorise : STATUTS_AUTORISES) {
      if (statutAutorise.equals(statut.trim())) {
        return true;
      }
    }
    return false;
  }

  /** Valide un fichier CSV uploadé */
  public static boolean isValidCSVFile(String filename, long size) {
    if (filename == null || filename.trim().isEmpty()) {
      return false;
    }

    // Vérifier l'extension
    if (!filename.toLowerCase().endsWith(".csv")) {
      return false;
    }

    // Vérifier la taille
    return size <= MAX_FILE_SIZE;
  }

  /** Nettoie une chaîne pour éviter les injections XSS basiques */
  public static String sanitizeInput(String input) {
    if (input == null) {
      return null;
    }

    String trimmed = input.trim();
    if (trimmed.isEmpty()) {
      return "";
    }

    // L'ordre est important : & doit être remplacé en PREMIER
    return trimmed
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#x27;");
  }

  /** Valide un paramètre de recherche */
  public static boolean isValidSearchQuery(String query) {
    if (query == null || query.trim().isEmpty()) {
      return true; // Recherche vide = autorisée
    }

    // Longueur maximale pour éviter les abus
    if (query.length() > 100) {
      return false;
    }

    // Interdire certains caractères dangereux
    String dangerous = "<>\"';&|()@"; // Ajout de @ pour bloquer les emails
    for (char c : dangerous.toCharArray()) {
      if (query.indexOf(c) != -1) {
        return false;
      }
    }

    return true;
  }

  /** Classe pour encapsuler le résultat de validation */
  public static class ValidationResult {
    private final boolean valid;
    private final String errorMessage;

    public ValidationResult(boolean valid, String errorMessage) {
      this.valid = valid;
      this.errorMessage = errorMessage;
    }

    public static ValidationResult success() {
      return new ValidationResult(true, null);
    }

    public static ValidationResult error(String message) {
      return new ValidationResult(false, message);
    }

    public boolean isValid() {
      return valid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
