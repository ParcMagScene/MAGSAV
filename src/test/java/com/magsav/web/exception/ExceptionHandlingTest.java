package com.magsav.web.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Tests pour les exceptions personnalisées MAGSAV */
public class ExceptionHandlingTest {

  @Test
  void testMagsavExceptionBasic() {
    MagsavException exception = new MagsavException(HttpStatus.BAD_REQUEST, "Message utilisateur");

    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    assertEquals("Message utilisateur", exception.getUserMessage());
    assertEquals("Message utilisateur", exception.getTechnicalMessage());
    assertEquals("Message utilisateur", exception.getMessage());
  }

  @Test
  void testMagsavExceptionWithTechnicalMessage() {
    MagsavException exception =
        new MagsavException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Message simple pour l'utilisateur",
            "Message technique détaillé avec stack trace");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    assertEquals("Message simple pour l'utilisateur", exception.getUserMessage());
    assertEquals("Message technique détaillé avec stack trace", exception.getTechnicalMessage());
    assertEquals("Message technique détaillé avec stack trace", exception.getMessage());
  }

  @Test
  void testMagsavExceptionWithCause() {
    IllegalArgumentException cause = new IllegalArgumentException("Argument invalide");
    MagsavException exception =
        new MagsavException(HttpStatus.BAD_REQUEST, "Données invalides", cause);

    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    assertEquals("Données invalides", exception.getUserMessage());
    assertTrue(exception.getTechnicalMessage().contains("Données invalides"));
    assertTrue(exception.getTechnicalMessage().contains("Argument invalide"));
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testResourceNotFoundException() {
    ResourceNotFoundException exception = new ResourceNotFoundException("Dossier SAV", "123");

    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("Dossier SAV non trouvé", exception.getUserMessage());
    assertTrue(exception.getTechnicalMessage().contains("123"));
  }

  @Test
  void testResourceNotFoundExceptionSimple() {
    ResourceNotFoundException exception = new ResourceNotFoundException("Ressource introuvable");

    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("Ressource introuvable", exception.getUserMessage());
  }

  @Test
  void testValidationException() {
    ValidationException exception = new ValidationException("Le champ est requis");

    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    assertEquals("Le champ est requis", exception.getUserMessage());
  }

  @Test
  void testValidationExceptionWithDetails() {
    ValidationException exception =
        new ValidationException("email", "invalid-email", "Le format de l'email est invalide");

    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    assertTrue(exception.getUserMessage().contains("Données invalides"));
    assertTrue(exception.getTechnicalMessage().contains("email"));
    assertTrue(exception.getTechnicalMessage().contains("invalid-email"));
    assertTrue(exception.getTechnicalMessage().contains("Le format de l'email est invalide"));
  }
}
