package com.magsav.web.controller;

import com.magsav.repo.ClientRepository;
import com.magsav.repo.FournisseurRepository;
import com.magsav.model.Client;
import com.magsav.model.Fournisseur;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.util.List;

@Controller
public class AnnuaireController {
    private final ClientRepository clientRepo;
    private final FournisseurRepository fournisseurRepo;

    public AnnuaireController(DataSource ds) {
        this.clientRepo = new ClientRepository(ds);
        this.fournisseurRepo = new FournisseurRepository(ds);
    }

    @GetMapping("/clients")
    public String clients(Model model, @RequestParam(required = false) String q) {
        try {
            List<Client> clients = (q == null || q.isBlank()) ? clientRepo.findAll() : clientRepo.search(q);
            model.addAttribute("clients", clients);
            model.addAttribute("q", q);
            model.addAttribute("total", clients.size());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "clients";
    }

    @GetMapping("/fournisseurs")
    public String fournisseurs(Model model, @RequestParam(required = false) String q) {
        try {
            List<Fournisseur> fournisseurs = (q == null || q.isBlank()) ? fournisseurRepo.findAll() : fournisseurRepo.search(q);
            model.addAttribute("fournisseurs", fournisseurs);
            model.addAttribute("q", q);
            model.addAttribute("total", fournisseurs.size());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "fournisseurs";
    }
}
