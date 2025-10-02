package com.magsav.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Contrôleur d'authentification: sert la page de connexion personnalisée. */
@Controller
public class AuthController {

  @GetMapping("/login")
  public String login(
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      Model model) {
    if (error != null) {
      model.addAttribute("errorMessage", "Identifiants invalides. Veuillez réessayer.");
    } else if (logout != null) {
      model.addAttribute("errorMessage", "Vous avez été déconnecté.");
    }
    return "login";
  }
}
