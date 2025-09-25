package com.magsav.service;

import com.magsav.db.DB;
import com.magsav.model.*;
import com.magsav.repo.*;
import com.magsav.imports.CSVImporter;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Ces tests utilisent l'ancien modèle (tables clients/appareils/dossiers)
// À refactoriser pour utiliser le modèle unifié DossierSAV ou maintenir compatibilité

@Disabled("Tests à adapter au modèle unifié - utilise tables clients/appareils manquantes en DB test")
public class SAVServiceTest {
    
    private DataSource ds;
    private SAVService savService;
    
    @BeforeEach
    void setUp() throws Exception {
        // Base en mémoire pour les tests
        ds = DB.init("jdbc:sqlite::memory:");
        DB.migrate(ds);
        savService = new SAVService(ds);
    }
    
    @Test
    void creerDossierSAVComplet() throws Exception {
        var dossierSAV = savService.creerDossierSAV(
            "Durand", "Marie", "marie.durand@test.com", "0600000001", "10 rue Test",
            "Apple", "iPhone 14", "ABC123456", "Chargeur inclus",
            "Écran cassé", "Chute depuis 1m"
        );
        
        assertNotNull(dossierSAV.client.id());
        assertNotNull(dossierSAV.appareil.id());
        assertNotNull(dossierSAV.dossier.id());
        
        assertEquals("Marie", dossierSAV.client.prenom());
        assertEquals("Durand", dossierSAV.client.nom());
        assertEquals("marie.durand@test.com", dossierSAV.client.email());
        
        assertEquals("Apple", dossierSAV.appareil.marque());
        assertEquals("iPhone 14", dossierSAV.appareil.modele());
        assertEquals("ABC123456", dossierSAV.appareil.sn());
        
        assertEquals("recu", dossierSAV.dossier.statut());
        assertEquals("Écran cassé", dossierSAV.dossier.symptome());
        assertEquals(LocalDate.now(), dossierSAV.dossier.dateEntree());
    }
    
    @Test
    void rechercherParSN() throws Exception {
        // Créer deux dossiers avec SN similaires
        savService.creerDossierSAV(
            "Client1", "Test", "client1@test.com", null, null,
            "Apple", "iPhone", "ABC123", "",
            "Test", ""
        );
        
        savService.creerDossierSAV(
            "Client2", "Test", "client2@test.com", null, null,
            "Samsung", "Galaxy", "ABC456", "",
            "Test2", ""
        );
        
        List<SAVService.DossierSAV> resultats = savService.rechercherParSN("ABC");
        assertEquals(2, resultats.size());
        
        resultats = savService.rechercherParSN("123");
        assertEquals(1, resultats.size());
        assertEquals("client1@test.com", resultats.get(0).client.email());
    }
    
    @Test
    void rechercherParEmailClient() throws Exception {
        savService.creerDossierSAV(
            "Martin", "Paul", "paul.martin@test.com", null, null,
            "Dell", "Laptop", "DEL789", "",
            "Lent au démarrage", ""
        );
        
        List<SAVService.DossierSAV> resultats = savService.rechercherParEmailClient("paul.martin@test.com");
        assertEquals(1, resultats.size());
        assertEquals("Martin", resultats.get(0).client.nom());
        assertEquals("Dell", resultats.get(0).appareil.marque());
    }
    
    @Test
    void changerStatutDossier() throws Exception {
        var dossierSAV = savService.creerDossierSAV(
            "Test", "User", "test@test.com", null, null,
            "HP", "Printer", "HP123", "",
            "Bourrage papier", ""
        );
        
        Long dossierId = dossierSAV.dossier.id();
        
        // Changer vers diagnostic
        var updated = savService.changerStatut(dossierId, "diagnostic");
        assertEquals("diagnostic", updated.dossier.statut());
        assertNull(updated.dossier.dateSortie());
        
        // Changer vers terminé
        updated = savService.changerStatut(dossierId, "termine");
        assertEquals("termine", updated.dossier.statut());
        assertEquals(LocalDate.now(), updated.dossier.dateSortie());
    }
    
    @Test
    void listerParStatut() throws Exception {
        savService.creerDossierSAV("C1", "T1", "c1@test.com", null, null, "M1", "Model1", "SN1", "", "S1", "");
        savService.creerDossierSAV("C2", "T2", "c2@test.com", null, null, "M2", "Model2", "SN2", "", "S2", "");
        
        var dossier3 = savService.creerDossierSAV("C3", "T3", "c3@test.com", null, null, "M3", "Model3", "SN3", "", "S3", "");
        savService.changerStatut(dossier3.dossier.id(), "diagnostic");
        
        List<SAVService.DossierSAV> recus = savService.listerParStatut("recu");
        assertEquals(2, recus.size());
        
        List<SAVService.DossierSAV> diagnostics = savService.listerParStatut("diagnostic");
        assertEquals(1, diagnostics.size());
        assertEquals("C3", diagnostics.get(0).client.nom());
    }
    
    @Test 
    void genererEtiquetteAvecQR() throws Exception {
        var dossierSAV = savService.creerDossierSAV(
            "Etiquette", "Test", "etiquette@test.com", null, null,
            "Canon", "Printer", "CAN789", "",
            "Pas d'encre", ""
        );
        
        Path tempDir = Files.createTempDirectory("magsav-test");
        
        savService.genererEtiquette(dossierSAV.dossier.id(), tempDir);
        
        Path qrPath = tempDir.resolve("qr-dossier-" + dossierSAV.dossier.id() + ".png");
        Path pdfPath = tempDir.resolve("etiquette-dossier-" + dossierSAV.dossier.id() + ".pdf");
        
        assertTrue(Files.exists(qrPath));
        assertTrue(Files.exists(pdfPath));
        assertTrue(Files.size(qrPath) > 0);
        assertTrue(Files.size(pdfPath) > 0);
        
        // Nettoyage
        Files.deleteIfExists(qrPath);
        Files.deleteIfExists(pdfPath);
        Files.deleteIfExists(tempDir);
    }
    
    @Test
    void importCSVEtCreationDossier() throws Exception {
        // Test d'intégration: import CSV puis création dossier
        Path tempClients = Files.createTempFile("clients", ".csv");
        Files.writeString(tempClients, 
            "nom,prenom,email,tel,adresse\n" +
            "ImportTest,Jean,jean.import@test.com,0123456789,Adresse Import\n"
        );
        
        CSVImporter importer = new CSVImporter(ds);
        importer.importClients(tempClients);
        
        // Le client devrait être réutilisé lors de la création du dossier
        var dossierSAV = savService.creerDossierSAV(
            "ImportTest", "Jean", "jean.import@test.com", "0123456789", "Adresse Import",
            "Asus", "Laptop", "ASUS999", "",
            "Clavier défaillant", ""
        );
        
        assertEquals("jean.import@test.com", dossierSAV.client.email());
        assertEquals("0123456789", dossierSAV.client.tel());
        
        Files.deleteIfExists(tempClients);
    }
}