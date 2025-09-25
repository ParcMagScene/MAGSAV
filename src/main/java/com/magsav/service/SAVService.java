package com.magsav.service;

import com.magsav.model.*;
import com.magsav.repo.*;
import com.magsav.qr.QRCodeService;
import com.magsav.label.LabelService;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SAVService {
    private final ClientRepository clientRepo;
    private final AppareilRepository appareilRepo;
    private final DossierRepository dossierRepo;
    private final QRCodeService qrService;
    private final LabelService labelService;
    
    public SAVService(DataSource ds) {
        this.clientRepo = new ClientRepository(ds);
        this.appareilRepo = new AppareilRepository(ds);
        this.dossierRepo = new DossierRepository(ds);
        this.qrService = new QRCodeService();
        this.labelService = new LabelService();
    }
    
    public static class DossierSAV {
        public final Client client;
        public final Appareil appareil;
        public final Dossier dossier;
        
        public DossierSAV(Client client, Appareil appareil, Dossier dossier) {
            this.client = client;
            this.appareil = appareil;
            this.dossier = dossier;
        }
    }
    
    /**
     * Créer un nouveau dossier SAV complet : client, appareil et dossier
     */
    public DossierSAV creerDossierSAV(
            String nomClient, String prenomClient, String emailClient, String telClient, String adresseClient,
            String marqueAppareil, String modeleAppareil, String snAppareil, String accessoires,
            String symptome, String commentaire
    ) throws Exception {
        
    try (var conn = clientRepo.getDataSource().getConnection()) {
            boolean prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Créer ou récupérer le client
                Client client = new Client(null, nomClient, prenomClient, emailClient, telClient, adresseClient);
                client = clientRepo.upsertByEmail(client);

                // 2. Créer l'appareil
                Appareil appareil = new Appareil(null, client.id(), marqueAppareil, modeleAppareil, snAppareil, accessoires, null);
                appareil = appareilRepo.save(appareil);

                // 3. Créer le dossier SAV
                Dossier dossier = new Dossier(null, appareil.id(), "recu", symptome, commentaire, 
                        LocalDate.now(), null, null);
                dossier = dossierRepo.save(dossier);

                conn.commit();
                conn.setAutoCommit(prevAutoCommit);
                return new DossierSAV(client, appareil, dossier);
            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(prevAutoCommit);
                throw e;
            }
        }
    }
    
    /**
     * Rechercher des dossiers SAV par numéro de série
     */
    public List<DossierSAV> rechercherParSN(String sn) throws Exception {
        List<Appareil> appareils = appareilRepo.findBySerialNumber(sn);
        return appareils.stream()
                .flatMap(appareil -> {
                    try {
                        List<Dossier> dossiers = dossierRepo.findByAppareilId(appareil.id());
                        Client client = clientRepo.findById(appareil.clientId());
                        return dossiers.stream()
                                .map(dossier -> new DossierSAV(client, appareil, dossier));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
    
    /**
     * Rechercher des dossiers SAV par email client
     */
    public List<DossierSAV> rechercherParEmailClient(String email) throws Exception {
        Client client = clientRepo.findByEmail(email);
        if (client == null) {
            return List.of();
        }
        
        List<Appareil> appareils = appareilRepo.findByClientId(client.id());
        return appareils.stream()
                .flatMap(appareil -> {
                    try {
                        List<Dossier> dossiers = dossierRepo.findByAppareilId(appareil.id());
                        return dossiers.stream()
                                .map(dossier -> new DossierSAV(client, appareil, dossier));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
    
    /**
     * Lister tous les dossiers par statut
     */
    public List<DossierSAV> listerParStatut(String statut) throws Exception {
        List<Dossier> dossiers = dossierRepo.findByStatut(statut);
        return dossiers.stream()
                .map(dossier -> {
                    try {
                        Appareil appareil = appareilRepo.findById(dossier.appareilId());
                        Client client = clientRepo.findById(appareil.clientId());
                        return new DossierSAV(client, appareil, dossier);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
    
    /**
     * Changer le statut d'un dossier
     */
    public DossierSAV changerStatut(Long dossierId, String nouveauStatut) throws Exception {
        Dossier dossier = dossierRepo.findById(dossierId);
        if (dossier == null) {
            throw new IllegalArgumentException("Dossier introuvable : " + dossierId);
        }
        
        dossier = dossier.withStatut(nouveauStatut);
        if ("termine".equals(nouveauStatut) && dossier.dateSortie() == null) {
            dossier = dossier.withDateSortie(LocalDate.now());
        }
        
        dossier = dossierRepo.save(dossier);
        
        Appareil appareil = appareilRepo.findById(dossier.appareilId());
        Client client = clientRepo.findById(appareil.clientId());
        
        return new DossierSAV(client, appareil, dossier);
    }
    
    /**
     * Générer une étiquette QR pour un dossier
     */
    public void genererEtiquette(Long dossierId, Path outputDir) throws Exception {
        DossierSAV dossierSAV = getDossierSAV(dossierId);
        
        String qrContent = String.format("MAGSAV:%d:%s", dossierId, dossierSAV.appareil.sn());
        Path qrPath = outputDir.resolve("qr-dossier-" + dossierId + ".png");
        Path pdfPath = outputDir.resolve("etiquette-dossier-" + dossierId + ".pdf");
        
        qrService.generateToFile(qrContent, 256, qrPath);
        
        String titre = String.format("Dossier SAV #%d\n%s %s\n%s %s - SN:%s",
                dossierId,
                dossierSAV.client.prenom(), dossierSAV.client.nom(),
                dossierSAV.appareil.marque(), dossierSAV.appareil.modele(),
                dossierSAV.appareil.sn()
        );
        
        labelService.createSimpleLabel(pdfPath, titre, qrPath);
    }
    
    /**
     * Récupérer un dossier SAV complet
     */
    public DossierSAV getDossierSAV(Long dossierId) throws Exception {
        Dossier dossier = dossierRepo.findById(dossierId);
        if (dossier == null) {
            throw new IllegalArgumentException("Dossier introuvable : " + dossierId);
        }
        
        Appareil appareil = appareilRepo.findById(dossier.appareilId());
        Client client = clientRepo.findById(appareil.clientId());
        
        return new DossierSAV(client, appareil, dossier);
    }
}