package com.magsav.web.controller;

import com.magsav.model.Client;
import com.magsav.model.Fournisseur;
import com.magsav.repo.ClientRepository;
import com.magsav.repo.FournisseurRepository;
import com.magsav.validation.ValidationUtils;
import com.magsav.web.exception.MagsavException;
import com.magsav.web.exception.ValidationException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnnuaireController {
  private final ClientRepository clientRepo;
  private final FournisseurRepository fournisseurRepo;

  @Autowired
  public AnnuaireController(DataSource ds) {
    this.clientRepo = new ClientRepository(ds);
    this.fournisseurRepo = new FournisseurRepository(ds);
  }

  @GetMapping("/clients")
  public String clients(Model model, @RequestParam(required = false) String q)
      throws ValidationException, MagsavException {

    // Validation du paramètre de recherche
    if (q != null && !ValidationUtils.isValidSearchQuery(q)) {
      throw new ValidationException(
          "q", q, "Le paramètre de recherche contient des caractères non autorisés");
    }

    // Nettoyer la requête de recherche
    q = ValidationUtils.sanitizeInput(q);

    try {
      List<Client> clients =
          q == null || q.isBlank() ? clientRepo.findAll() : clientRepo.search(q);
      model.addAttribute("clients", clients);
      model.addAttribute("q", q);
      model.addAttribute("total", clients.size());

    } catch (Exception e) {
      throw new MagsavException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la recherche de clients", e);
    }

    return "clients";
  }

  @GetMapping("/fournisseurs")
  public String fournisseurs(Model model, @RequestParam(required = false) String q)
      throws ValidationException, MagsavException {

    // Validation du paramètre de recherche
    if (q != null && !ValidationUtils.isValidSearchQuery(q)) {
      throw new ValidationException(
          "q", q, "Le paramètre de recherche contient des caractères non autorisés");
    }

    // Nettoyer la requête de recherche
    q = ValidationUtils.sanitizeInput(q);

    try {
      List<Fournisseur> fournisseurs =
          q == null || q.isBlank() ? fournisseurRepo.findAll() : fournisseurRepo.search(q);
      model.addAttribute("fournisseurs", fournisseurs);
      model.addAttribute("q", q);
      model.addAttribute("total", fournisseurs.size());

    } catch (Exception e) {
      throw new MagsavException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la recherche de fournisseurs", e);
    }

    return "fournisseurs";
  }
}
