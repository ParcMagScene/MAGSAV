package com.magsav.web.exception;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gestionnaire global des exceptions pour l'application web MAGSAV */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

  /** Gestion des exceptions MAGSAV personnalisées */
  @ExceptionHandler(MagsavException.class)
  public String handleMagsavException(
      MagsavException ex, Model model, RedirectAttributes redirectAttributes) {

    // Log selon la criticité
    if (ex.getHttpStatus().is5xxServerError()) {
      logger.log(Level.SEVERE, "Erreur serveur: " + ex.getTechnicalMessage(), ex);
    } else if (ex.getHttpStatus().is4xxClientError()) {
      logger.log(Level.WARNING, "Erreur client: " + ex.getTechnicalMessage());
    }

    // Pour les erreurs de validation (400), rester sur la page avec le message
    if (ex.getHttpStatus() == HttpStatus.BAD_REQUEST) {
      model.addAttribute("error", ex.getUserMessage());
      return determineViewFromException(ex);
    }

    // Pour les autres erreurs, rediriger avec un flash message
    redirectAttributes.addFlashAttribute("error", ex.getUserMessage());
    return "redirect:/";
  }

  /** Gestion des exceptions non prévues */
  @ExceptionHandler(Exception.class)
  public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {

    logger.log(Level.SEVERE, "Erreur non gérée: " + ex.getMessage(), ex);

    // Message générique pour l'utilisateur
    redirectAttributes.addFlashAttribute(
        "error",
        "Une erreur inattendue s'est produite. Veuillez réessayer ou contacter le support.");

    return "redirect:/";
  }

  /** Gestion des IllegalArgumentException (souvent liées à la validation) */
  @ExceptionHandler(IllegalArgumentException.class)
  public String handleValidationException(
      IllegalArgumentException ex, Model model, RedirectAttributes redirectAttributes) {

    logger.log(Level.WARNING, "Validation échouée: " + ex.getMessage());

    model.addAttribute("error", "Données invalides : " + ex.getMessage());

    // Rester sur la page d'origine si possible
    return "error";
  }

  /** Détermine la vue à retourner en fonction du contexte de l'exception */
  private String determineViewFromException(MagsavException ex) {
    // Par défaut, retourner une page d'erreur générique
    // Ici on pourrait analyser la stack trace pour déterminer le contrôleur d'origine
    return "error";
  }
}
