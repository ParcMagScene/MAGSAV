package com.magsav.web.controller;

import com.magsav.model.DossierSAV;
import com.magsav.qr.QRCodeService;
import com.magsav.label.LabelService;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.imports.CSVImporter;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Contrôleur web principal pour la gestion des dossiers SAV
 */
@Controller
public class DossierSAVController {
    
    private final DossierSAVRepository dossierRepo;
    private final CSVImporter csvImporter;
    private final QRCodeService qrCodeService;
    private final LabelService labelService;
    
    public DossierSAVController(DataSource dataSource) {
        this.dossierRepo = new DossierSAVRepository(dataSource);
        this.csvImporter = new CSVImporter(dataSource);
        this.qrCodeService = new QRCodeService();
        this.labelService = new LabelService();
    }
    
    /**
     * Page d'accueil avec liste des dossiers
     */
    @GetMapping("/")
    public String index(Model model, 
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String statut) {
        try {
            List<DossierSAV> dossiers;
            
            if (search != null && !search.trim().isEmpty()) {
                // Recherche par numéro de série ou propriétaire
                dossiers = dossierRepo.findByNumeroSerie(search);
                if (dossiers.isEmpty()) {
                    dossiers = dossierRepo.findByProprietaire(search);
                }
                model.addAttribute("search", search);
            } else if (statut != null && !statut.trim().isEmpty()) {
                // Filtrage par statut
                dossiers = dossierRepo.findByStatut(statut);
                model.addAttribute("statut", statut);
            } else {
                // Tous les dossiers
                dossiers = dossierRepo.findAll();
            }
            
            model.addAttribute("dossiers", dossiers);
            model.addAttribute("totalDossiers", dossiers.size());
            
            // Statistiques par statut
            long enCours = dossiers.stream().filter(d -> "en_cours".equals(d.statut())).count();
            long termines = dossiers.stream().filter(d -> "termine".equals(d.statut())).count();
            long recus = dossiers.stream().filter(d -> "recu".equals(d.statut())).count();
            
            model.addAttribute("statsEnCours", enCours);
            model.addAttribute("statsTermines", termines);
            model.addAttribute("statsRecus", recus);
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des dossiers : " + e.getMessage());
        }
        
        return "index";
    }
    
    /**
     * Détails d'un dossier SAV
     */
    @GetMapping("/dossier/{id}")
    public String detailDossier(@PathVariable Long id, Model model) {
        try {
            DossierSAV dossier = dossierRepo.findById(id);
            if (dossier == null) {
                model.addAttribute("error", "Dossier introuvable");
                return "redirect:/";
            }
            model.addAttribute("dossier", dossier);
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du dossier : " + e.getMessage());
        }
        return "detail";
    }
    
    /**
     * Changer le statut d'un dossier
     */
    @PostMapping("/dossier/{id}/statut")
    public String changerStatut(@PathVariable Long id, 
                               @RequestParam String nouveauStatut,
                               RedirectAttributes redirectAttributes) {
        try {
            DossierSAV dossier = dossierRepo.findById(id);
            if (dossier != null) {
                DossierSAV dossierMisAJour = dossier.withStatut(nouveauStatut);
                if ("termine".equals(nouveauStatut) && dossier.dateSortie() == null) {
                    dossierMisAJour = dossierMisAJour.withDateSortie(java.time.LocalDate.now());
                }
                dossierRepo.update(dossierMisAJour);
                redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/dossier/" + id;
    }
    
    /**
     * Page d'import CSV
     */
    @GetMapping("/import")
    public String pageImport() {
        return "import";
    }
    
    /**
     * Import de fichier CSV
     */
    @PostMapping("/import")
    public String importCSV(@RequestParam("file") MultipartFile file,
                           @RequestParam("type") String type,
                           RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
            return "redirect:/import";
        }
        
        try {
            // Sauvegarder le fichier temporairement
            Path tempFile = Files.createTempFile("import-", ".csv");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            int imported = 0;
            switch (type) {
                case "dossiers":
                case "dossiers_sav":
                    imported = csvImporter.importDossiersSAV(tempFile);
                    break;
                case "produits":
                    imported = csvImporter.importProduits(tempFile);
                    break;
                case "clients":
                    imported = csvImporter.importClients(tempFile);
                    break;
                case "fournisseurs":
                    imported = csvImporter.importFournisseurs(tempFile);
                    break;
                default:
                    throw new IllegalArgumentException("Type d'import non supporté : " + type);
            }
            
            // Nettoyer le fichier temporaire
            Files.deleteIfExists(tempFile);
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("Import réussi : %d enregistrements traités", imported));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur d'import : " + e.getMessage());
        }
        
        return "redirect:/";
    }

    /**
     * Génération du QR code pour un dossier (retourne le fichier PNG)
     */
    @GetMapping(value = "/dossier/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> genererQR(@PathVariable Long id,
                                            @RequestParam(defaultValue = "300") int size,
                                            @RequestParam(required = false) String download) {
        try {
            DossierSAV dossier = dossierRepo.findById(id);
            if (dossier == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
            }
            Path out = Path.of("output", "qr-dossier-" + id + ".png");
            qrCodeService.generateToFile("DOSSIER:" + id + "|SERIE:" + dossier.numeroSerie(), size, out);
            byte[] bytes = Files.readAllBytes(out);
            HttpHeaders headers = new HttpHeaders();
            if (download != null) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr-dossier-" + id + ".png");
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qr-dossier-" + id + ".png");
            }
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[0]);
        }
    }

    /**
     * Génération de l'étiquette PDF (retourne le PDF)
     */
    @GetMapping(value = "/dossier/{id}/etiquette", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> genererEtiquette(@PathVariable Long id,
                                                   @RequestParam(required = false) String download) {
        try {
            DossierSAV dossier = dossierRepo.findById(id);
            if (dossier == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
            }
            Path qr = Path.of("output", "qr-dossier-" + id + ".png");
            if (!Files.exists(qr)) {
                qrCodeService.generateToFile("DOSSIER:" + id + "|SERIE:" + dossier.numeroSerie(), 300, qr);
            }
            Path pdf = Path.of("output", "etiquette-dossier-" + id + ".pdf");
            String title = "Dossier SAV #" + id + "\n" + dossier.produit() + "\n" + dossier.numeroSerie();
            labelService.createSimpleLabel(pdf, title, qr);
            byte[] bytes = Files.readAllBytes(pdf);
            HttpHeaders headers = new HttpHeaders();
            if (download != null) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=etiquette-dossier-" + id + ".pdf");
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=etiquette-dossier-" + id + ".pdf");
            }
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[0]);
        }
    }
}