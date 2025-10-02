package com.magsav.web.exception;

import org.springframework.http.HttpStatus;

/** Exception pour les ressources non trouvées (404) */
public class ResourceNotFoundException extends MagsavException {

  public ResourceNotFoundException(String resource, String identifier) {
    super(
        HttpStatus.NOT_FOUND,
        String.format("%s non trouvé", resource),
        String.format("%s avec identifiant '%s' non trouvé", resource, identifier));
  }

  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
