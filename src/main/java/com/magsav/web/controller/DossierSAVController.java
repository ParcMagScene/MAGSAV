package com.magsav.web.controller;

import com.magsav.label.LabelService;
import com.magsav.model.DossierSAV;
import com.magsav.qr.QRCodeService;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.DossierSAVRepository;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DossierSAVController {
  private final DossierSAVRepository dossierRepo;
  private final QRCodeService qrCodeService;
  private final LabelService labelService;
  private final CategoryRepository categoryRepo;

  @Autowired
  public DossierSAVController(DataSource dataSource) {
    this.dossierRepo = new DossierSAVRepository(dataSource);
    this.qrCodeService = new QRCodeService();
    this.labelService = new LabelService();
    this.categoryRepo = new CategoryRepository(dataSource);
  }

  /** Page d'accueil : liste des dossiers SAV */
  @GetMapping({"/", "/index"})
  public String index(
      Model model,
      @RequestParam(required = false) String view,
      // Filtres produits
      @RequestParam(name = "p_produit", required = false) String pProduit,
      @RequestParam(name = "p_sn", required = false) String pSn,
      @RequestParam(name = "p_code", required = false) String pCode,
      @RequestParam(name = "p_statut", required = false) String pStatut,
      @RequestParam(name = "p_categoryId", required = false) Long pCategoryId,
      @RequestParam(name = "p_subcategoryId", required = false) Long pSubcategoryId,
      // Filtres interventions
      @RequestParam(name = "i_produit", required = false) String iProduit,
      @RequestParam(name = "i_sn", required = false) String iSn,
      @RequestParam(name = "i_code", required = false) String iCode,
      @RequestParam(name = "i_statut", required = false) String iStatut,
      @RequestParam(name = "i_categoryId", required = false) Long iCategoryId,
      @RequestParam(name = "i_subcategoryId", required = false) Long iSubcategoryId,
      @RequestParam(name = "i_entree_from", required = false) String iEntreeFrom,
      @RequestParam(name = "i_entree_to", required = false) String iEntreeTo,
      @RequestParam(name = "i_sortie_from", required = false) String iSortieFrom,
      @RequestParam(name = "i_sortie_to", required = false) String iSortieTo) {
    try {
      // Produits (agrégés)
      var products =
          dossierRepo.searchProducts(pProduit, pSn, pCode, pCategoryId, pSubcategoryId, pStatut);
      model.addAttribute("products", products);
      model.addAttribute("productsTotal", products != null ? products.size() : 0);
      model.addAttribute("p_produit", pProduit);
      model.addAttribute("p_sn", pSn);
      model.addAttribute("p_code", pCode);
      model.addAttribute("p_statut", pStatut);
      model.addAttribute("p_categoryId", pCategoryId);
      model.addAttribute("p_subcategoryId", pSubcategoryId);
      try {
        model.addAttribute("categoriesRoots", categoryRepo.findRoots());
        if (pCategoryId != null) {
          model.addAttribute("p_children", categoryRepo.findChildren(pCategoryId));
        }
      } catch (Exception ignored) {
      }

      // Interventions (liste)
      List<DossierSAV> dossiers =
          dossierRepo.searchInterventions(
              iProduit,
              iSn,
              iCode,
              iCategoryId,
              iSubcategoryId,
              iStatut,
              iEntreeFrom,
              iEntreeTo,
              iSortieFrom,
              iSortieTo);
      model.addAttribute("dossiers", dossiers);
      model.addAttribute("totalDossiers", dossiers.size());
      model.addAttribute("i_produit", iProduit);
      model.addAttribute("i_sn", iSn);
      model.addAttribute("i_code", iCode);
      model.addAttribute("i_statut", iStatut);
      model.addAttribute("i_categoryId", iCategoryId);
      model.addAttribute("i_subcategoryId", iSubcategoryId);
      model.addAttribute("i_entree_from", iEntreeFrom);
      model.addAttribute("i_entree_to", iEntreeTo);
      model.addAttribute("i_sortie_from", iSortieFrom);
      model.addAttribute("i_sortie_to", iSortieTo);
      try {
        if (!model.containsAttribute("categoriesRoots")) {
          model.addAttribute("categoriesRoots", categoryRepo.findRoots());
        }
        if (iCategoryId != null) {
          model.addAttribute("i_children", categoryRepo.findChildren(iCategoryId));
        }
      } catch (Exception ignored) {
      }

      // Statistiques par statut
      model.addAttribute("statsRecus", dossierRepo.countByStatut("recu"));
      model.addAttribute("statsEnCours", dossierRepo.countByStatut("en_cours"));
      model.addAttribute("statsTermines", dossierRepo.countByStatut("termine"));
    } catch (Exception e) {
      model.addAttribute("error", "Erreur lors du chargement : " + e.getMessage());
      model.addAttribute("dossiers", Collections.emptyList());
      model.addAttribute("products", Collections.emptyList());
    }
    return "index";
  }

  /** Détail d'un dossier SAV */
  @GetMapping({"/dossier/{id}", "/intervention/{id}"})
  public String detailDossier(@PathVariable Long id, Model model) throws Exception {
    if (id == null || id <= 0) {
      model.addAttribute("error", "L'identifiant doit être un nombre positif");
      return "detail";
    }
    DossierSAV dossier = dossierRepo.findById(id);
    if (dossier == null) {
      model.addAttribute("error", "Dossier SAV introuvable");
      return "detail";
    }
    model.addAttribute("dossier", dossier);
    try {
      model.addAttribute("categoriesRoots", categoryRepo.findRoots());
      if (dossier.getCategoryId() != null) {
        model.addAttribute(
            "categoriesChildren", categoryRepo.findChildren(dossier.getCategoryId()));
      }
    } catch (Exception ignore) {
    }
    // Historique des interventions pour le même produit (même numéro de série)
    try {
      String sn = dossier.getNumeroSerie();
      if (sn == null || sn.isBlank()) {
        // Si pas de numéro de série (nullable), on affiche uniquement l'intervention courante
        model.addAttribute("historiqueProduit", java.util.List.of(dossier));
      } else {
        List<DossierSAV> historique = dossierRepo.findAllByNumeroSerieExact(sn);
        // Si la requête ne retourne rien (possible si données hétérogènes), fallback sur l'élément
        // courant
        if (historique == null || historique.isEmpty()) {
          model.addAttribute("historiqueProduit", java.util.List.of(dossier));
        } else {
          model.addAttribute("historiqueProduit", historique);
        }
      }
    } catch (Exception ex) {
      model.addAttribute("historiqueProduit", java.util.List.of(dossier));
    }
    return "detail";
  }

  @PostMapping({"/dossier/{id}/categorie", "/intervention/{id}/categorie"})
  public String assignCategorie(
      @PathVariable Long id,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long subcategoryId,
      RedirectAttributes redirect)
      throws Exception {
    DossierSAV dossier = dossierRepo.findById(id);
    if (dossier == null) {
      redirect.addFlashAttribute("error", "Dossier introuvable");
      return "redirect:/";
    }
    try {
      dossierRepo.assignCategories(id, categoryId, subcategoryId);
      redirect.addFlashAttribute("success", "Catégorie mise à jour");
    } catch (Exception e) {
      redirect.addFlashAttribute("error", "Erreur d'affectation: " + e.getMessage());
    }
    return "redirect:/intervention/" + id;
  }

  /** Changement de statut d'un dossier SAV */
  @PostMapping({"/dossier/{id}/statut", "/intervention/{id}/statut"})
  public String changerStatut(
      @PathVariable Long id, @RequestParam String nouveauStatut, RedirectAttributes redirect)
      throws Exception {
    if (id == null || id <= 0) {
      redirect.addFlashAttribute("error", "Identifiant de dossier invalide");
      return "redirect:/dossier/" + id;
    }
    DossierSAV dossier = dossierRepo.findById(id);
    if (dossier == null) {
      redirect.addFlashAttribute("error", "Dossier SAV introuvable");
      return "redirect:/dossier/" + id;
    }
    // Validation du statut
    if (!List.of("recu", "en_cours", "termine").contains(nouveauStatut)) {
      redirect.addFlashAttribute("error", "Statut invalide");
      return "redirect:/dossier/" + id;
    }
    DossierSAV dossierMaj = dossier.withStatut(nouveauStatut);
    // Si passage à 'termine', renseigner la date de sortie
    if ("termine".equals(nouveauStatut) && dossierMaj.getDateSortie() == null) {
      dossierMaj = dossierMaj.withDateSortie(java.time.LocalDate.now());
    }
    dossierRepo.save(dossierMaj);
    redirect.addFlashAttribute("success", "Statut mis à jour avec succès");
    return "redirect:/intervention/" + id;
  }

  /** Générer le QR code d'un dossier SAV */
  @GetMapping({"/dossier/{id}/qr", "/intervention/{id}/qr"})
  public ResponseEntity<byte[]> qrDossier(@PathVariable Long id) throws Exception {
    DossierSAV dossier = dossierRepo.findById(id);
    if (dossier == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
    }
    byte[] qr = qrCodeService.generateQRCode(dossier.getNumeroSerie());
    return ResponseEntity.ok()
        .header("Content-Type", "image/png")
        .header("Content-Disposition", "inline; filename=qr-dossier-" + id + ".png")
        .body(qr);
  }

  /** Générer l'étiquette PDF d'un dossier SAV */
  @GetMapping({"/dossier/{id}/etiquette", "/intervention/{id}/etiquette"})
  public ResponseEntity<byte[]> etiquetteDossier(@PathVariable Long id) throws Exception {
    DossierSAV dossier = dossierRepo.findById(id);
    if (dossier == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
    }
    byte[] pdf = labelService.generateLabelPDF(dossier);
    return ResponseEntity.ok()
        .header("Content-Type", "application/pdf")
        .header("Content-Disposition", "inline; filename=etiquette-dossier-" + id + ".pdf")
        .body(pdf);
  }

  // ... Toutes les méthodes du contrôleur (voir version saine ci-dessus) ...

  // (Coller ici tout le code des méthodes du contrôleur, comme dans la version saine lue
  // précédemment)

}
