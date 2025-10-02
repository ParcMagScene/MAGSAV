package com.magsav.web.exception;

import org.springframework.http.HttpStatus;

/** Exception pour les données invalides (400) */
public class ValidationException extends MagsavException {

  public ValidationException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }

  public ValidationException(String field, String value, String reason) {
    super(
        HttpStatus.BAD_REQUEST,
        String.format("Données invalides : %s", reason),
        String.format(
            "Validation échouée pour le champ '%s' avec valeur '%s' : %s", field, value, reason));
  }
}
