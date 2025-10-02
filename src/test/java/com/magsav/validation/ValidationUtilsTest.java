package com.magsav.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests de validation pour vérifier que les validations fonctionnent correctement */
public class ValidationUtilsTest {

  @Test
  void testEmailValidation() {
    // Emails valides
    assertTrue(ValidationUtils.isValidEmail("test@example.com"));
    assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.uk"));
    assertTrue(ValidationUtils.isValidEmail("contact+support@company.fr"));

    // Emails invalides
    assertFalse(ValidationUtils.isValidEmail(""));
    assertFalse(ValidationUtils.isValidEmail(null));
    assertFalse(ValidationUtils.isValidEmail("invalide"));
    assertFalse(ValidationUtils.isValidEmail("@domain.com"));
    assertFalse(ValidationUtils.isValidEmail("user@"));
    assertFalse(ValidationUtils.isValidEmail("user space@domain.com"));
  }

  @Test
  void testNameValidation() {
    // Noms valides
    assertTrue(ValidationUtils.isValidName("Dupont"));
    assertTrue(ValidationUtils.isValidName("Jean-Pierre"));
    assertTrue(ValidationUtils.isValidName("O'Connor"));
    assertTrue(ValidationUtils.isValidName("Marie Claire"));
    assertTrue(ValidationUtils.isValidName("José"));

    // Noms invalides
    assertFalse(ValidationUtils.isValidName(""));
    assertFalse(ValidationUtils.isValidName(null));
    assertFalse(ValidationUtils.isValidName("123"));
    assertFalse(ValidationUtils.isValidName("User@name"));
    assertFalse(ValidationUtils.isValidName("User<script>"));
  }

  @Test
  void testPhoneValidation() {
    // Numéros valides
    assertTrue(ValidationUtils.isValidPhone("0123456789"));
    assertTrue(ValidationUtils.isValidPhone("+33123456789"));
    assertTrue(ValidationUtils.isValidPhone("0623456789"));
    assertTrue(ValidationUtils.isValidPhone("")); // Optionnel
    assertTrue(ValidationUtils.isValidPhone(null)); // Optionnel

    // Numéros invalides
    assertFalse(ValidationUtils.isValidPhone("123"));
    assertFalse(ValidationUtils.isValidPhone("abcd"));
    assertFalse(ValidationUtils.isValidPhone("0023456789")); // Commence par 00
  }

  @Test
  void testSerialNumberValidation() {
    // Numéros de série valides
    assertTrue(ValidationUtils.isValidSerialNumber("ABC123"));
    assertTrue(ValidationUtils.isValidSerialNumber("SN-2024-001"));
    assertTrue(ValidationUtils.isValidSerialNumber("DEV_TEST_123"));

    // Numéros de série invalides
    assertFalse(ValidationUtils.isValidSerialNumber(""));
    assertFalse(ValidationUtils.isValidSerialNumber(null));
    assertFalse(ValidationUtils.isValidSerialNumber("ABC 123")); // Espace
    assertFalse(ValidationUtils.isValidSerialNumber("ABC@123")); // Caractère spécial
  }

  @Test
  void testStatutValidation() {
    // Statuts valides
    assertTrue(ValidationUtils.isValidStatut("recu"));
    assertTrue(ValidationUtils.isValidStatut("en_cours"));
    assertTrue(ValidationUtils.isValidStatut("termine"));
    assertTrue(ValidationUtils.isValidStatut("diagnostic"));

    // Statuts invalides
    assertFalse(ValidationUtils.isValidStatut(""));
    assertFalse(ValidationUtils.isValidStatut(null));
    assertFalse(ValidationUtils.isValidStatut("invalide"));
    assertFalse(ValidationUtils.isValidStatut("EN_COURS")); // Casse différente
  }

  @Test
  void testCSVFileValidation() {
    // Fichiers valides
    assertTrue(ValidationUtils.isValidCSVFile("test.csv", 1000));
    assertTrue(ValidationUtils.isValidCSVFile("import.CSV", 100000)); // Extension en majuscules

    // Fichiers invalides
    assertFalse(ValidationUtils.isValidCSVFile("", 1000));
    assertFalse(ValidationUtils.isValidCSVFile(null, 1000));
    assertFalse(ValidationUtils.isValidCSVFile("test.txt", 1000)); // Mauvaise extension
    assertFalse(
        ValidationUtils.isValidCSVFile("test.csv", ValidationUtils.MAX_FILE_SIZE + 1)); // Trop gros
  }

  @Test
  void testSearchQueryValidation() {
    // Requêtes valides
    assertTrue(ValidationUtils.isValidSearchQuery(""));
    assertTrue(ValidationUtils.isValidSearchQuery(null));
    assertTrue(ValidationUtils.isValidSearchQuery("Dupont"));
    assertTrue(ValidationUtils.isValidSearchQuery("Jean Pierre"));
    assertTrue(ValidationUtils.isValidSearchQuery("ABC123"));

    // Requêtes invalides
    assertFalse(ValidationUtils.isValidSearchQuery("<script>"));
    assertFalse(ValidationUtils.isValidSearchQuery("test@evil.com"));
    assertFalse(ValidationUtils.isValidSearchQuery("'; DROP TABLE--"));
    assertFalse(ValidationUtils.isValidSearchQuery("a".repeat(101))); // Trop long
  }

  @Test
  void testSanitizeInput() {
    assertEquals("Test", ValidationUtils.sanitizeInput("Test"));
    assertEquals("Test &amp; Co", ValidationUtils.sanitizeInput("Test & Co"));
    assertEquals(
        "&lt;script&gt;alert()&lt;/script&gt;",
        ValidationUtils.sanitizeInput("<script>alert()</script>"));
    assertEquals("&quot;Test&quot;", ValidationUtils.sanitizeInput("\"Test\""));
    assertNull(ValidationUtils.sanitizeInput(null));
    assertEquals("", ValidationUtils.sanitizeInput("   "));
  }

  @Test
  void testValidationResult() {
    ValidationUtils.ValidationResult success = ValidationUtils.ValidationResult.success();
    assertTrue(success.isValid());
    assertNull(success.getErrorMessage());

    ValidationUtils.ValidationResult error = ValidationUtils.ValidationResult.error("Test error");
    assertFalse(error.isValid());
    assertEquals("Test error", error.getErrorMessage());
  }

  @Test
  void testLengthValidation() {
    assertTrue(ValidationUtils.isValidLength(null, 100));
    assertTrue(ValidationUtils.isValidLength("", 100));
    assertTrue(ValidationUtils.isValidLength("Test", 100));
    assertTrue(ValidationUtils.isValidLength("Test", 4));

    assertFalse(ValidationUtils.isValidLength("Test", 3));
    assertFalse(ValidationUtils.isValidLength("Very long text", 10));
  }
}
