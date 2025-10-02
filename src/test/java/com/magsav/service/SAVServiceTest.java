package com.magsav.service;

import static org.junit.jupiter.api.Assertions.*;

import com.magsav.db.DB;
import com.magsav.imports.CSVImporter;
import com.magsav.model.*;
import com.magsav.repo.*;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

/**
 * Tests refactorisés pour le modèle unifié SAVService. Tests couvrent les cas normaux, d'erreur,
 * import CSV, et sécurité.
 */
public class SAVServiceTest {

  private DataSource ds;
  private SAVService savService;

  @BeforeEach
  void setUp() throws Exception {
    // Utiliser un fichier temporaire au lieu de la mémoire pour éviter les problèmes de partage
    // avec HikariCP
    ds = DB.init("jdbc:sqlite:/tmp/magsav-service-test.db");
    DB.migrate(ds);
    savService = new SAVService(ds);
  }

  @AfterEach
  void tearDown() throws Exception {
    // Nettoyer la base entre chaque test
    try (Connection conn = ds.getConnection()) {
      conn.createStatement().execute("DELETE FROM dossiers");
      conn.createStatement().execute("DELETE FROM appareils");
      conn.createStatement().execute("DELETE FROM clients");
    }
    // Fermer le pool de connexions
    if (ds instanceof HikariDataSource) {
      ((HikariDataSource) ds).close();
    }
  }

  @Test
  void creerDossierSAVComplet() throws Exception {
    var dossierSAV =
        savService.creerDossierSAV(
            "Durand",
            "Marie",
            "marie.durand@test.com",
            "0600000001",
            "10 rue Test",
            "Apple",
            "iPhone 14",
            "ABC123456",
            "Chargeur inclus",
            "Écran cassé",
            "Chute depuis 1m");

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
  void creerDossierSAV_AvecClientExistant() throws Exception {
    // Créer un premier dossier
    savService.creerDossierSAV(
        "Dupont",
        "Jean",
        "jean.dupont@test.com",
        "0123456789",
        "5 avenue Test",
        "Samsung",
        "Galaxy S23",
        "SAM001",
        "",
        "Batterie défaillante",
        "");

    // Créer un second dossier avec même client (email identique)
    var dossierSAV2 =
        savService.creerDossierSAV(
            "Dupont",
            "Jean",
            "jean.dupont@test.com",
            "0123456789",
            "5 avenue Test",
            "Apple",
            "iPad",
            "IPD002",
            "Smart Cover",
            "Écran rayé",
            "Rayure importante");

    // Vérifier qu'un seul client existe
    assertEquals("jean.dupont@test.com", dossierSAV2.client.email());
    assertEquals("Apple", dossierSAV2.appareil.marque());
    assertEquals("iPad", dossierSAV2.appareil.modele());
  }

  @Test
  void creerDossierSAV_EchecTransactionnel() throws Exception {
    // Test avec des données invalides qui devraient provoquer une exception
    assertThrows(
        Exception.class,
        () ->
          savService.creerDossierSAV(
              null,
              "Prenom",
              "email@test.com",
              null,
              null, // nom null
              "Marque",
              "Modele",
              "SN123",
              "",
              "Symptome",
              ""));

    // Vérifier qu'aucune donnée n'a été persistée
    List<SAVService.DossierSAV> dossiers = savService.listerParStatut("recu");
    assertEquals(0, dossiers.size());
  }

  @Test
  void rechercherParSN() throws Exception {
    // Créer deux dossiers avec SN similaires
    savService.creerDossierSAV(
        "Dupont",
        "Jean",
        "client1@test.com",
        null,
        null,
        "Apple",
        "iPhone",
        "ABC123",
        "",
        "Ecran cassé",
        "");

    savService.creerDossierSAV(
        "Martin",
        "Paul",
        "client2@test.com",
        null,
        null,
        "Samsung",
        "Galaxy",
        "ABC456",
        "",
        "Batterie défaillante",
        "");

    List<SAVService.DossierSAV> resultats = savService.rechercherParSN("ABC123");
    assertEquals(1, resultats.size());
    assertEquals("Dupont", resultats.get(0).client.nom());

    resultats = savService.rechercherParSN("ABC456");
    assertEquals(1, resultats.size());
    assertEquals("client2@test.com", resultats.get(0).client.email());
  }

  @Test
  void rechercherParSN_AucunResultat() throws Exception {
    List<SAVService.DossierSAV> resultats = savService.rechercherParSN("INEXISTANT");
    assertEquals(0, resultats.size());
  }

  @Test
  void rechercherParEmailClient() throws Exception {
    savService.creerDossierSAV(
        "Martin",
        "Paul",
        "paul.martin@test.com",
        null,
        null,
        "Dell",
        "Laptop",
        "DEL789",
        "",
        "Lent au démarrage",
        "");

    List<SAVService.DossierSAV> resultats =
        savService.rechercherParEmailClient("paul.martin@test.com");
    assertEquals(1, resultats.size());
    assertEquals("Martin", resultats.get(0).client.nom());
    assertEquals("Dell", resultats.get(0).appareil.marque());
  }

  @Test
  void rechercherParEmailClient_AucunResultat() throws Exception {
    List<SAVService.DossierSAV> resultats =
        savService.rechercherParEmailClient("inexistant@test.com");
    assertEquals(0, resultats.size());
  }

  @Test
  void changerStatutDossier() throws Exception {
    var dossierSAV =
        savService.creerDossierSAV(
            "Test",
            "User",
            "test@test.com",
            null,
            null,
            "HP",
            "Printer",
            "HP123",
            "",
            "Bourrage papier",
            "");

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
  void changerStatut_DossierInexistant() throws Exception {
    assertThrows(
        IllegalArgumentException.class,
        () ->
          savService.changerStatut(999L, "diagnostic"));
  }

  @Test
  void listerParStatut() throws Exception {
    savService.creerDossierSAV(
        "Dupont",
        "Jean",
        "c1@test.com",
        null,
        null,
        "Apple",
        "iPhone",
        "SN1",
        "",
        "Ecran cassé",
        "");
    savService.creerDossierSAV(
        "Martin",
        "Paul",
        "c2@test.com",
        null,
        null,
        "Samsung",
        "Galaxy",
        "SN2",
        "",
        "Batterie HS",
        "");

    var dossier3 =
        savService.creerDossierSAV(
            "Durand", "Marie", "c3@test.com", null, null, "Dell", "Laptop", "SN3", "", "Lent", "");
    savService.changerStatut(dossier3.dossier.id(), "diagnostic");

    List<SAVService.DossierSAV> recus = savService.listerParStatut("recu");
    assertEquals(2, recus.size());

    List<SAVService.DossierSAV> diagnostics = savService.listerParStatut("diagnostic");
    assertEquals(1, diagnostics.size());
    assertEquals("Durand", diagnostics.get(0).client.nom());
  }

  @Test
  void genererEtiquetteAvecQR() throws Exception {
    var dossierSAV =
        savService.creerDossierSAV(
            "Etiquette",
            "Test",
            "etiquette@test.com",
            null,
            null,
            "Canon",
            "Printer",
            "CAN789",
            "",
            "Pas d'encre",
            "");

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
  void genererEtiquette_DossierInexistant() throws Exception {
    Path tempDir = Files.createTempDirectory("magsav-test");

    assertThrows(
        IllegalArgumentException.class,
        () ->
          savService.genererEtiquette(999L, tempDir));

    Files.deleteIfExists(tempDir);
  }

  @Test
  void importCSVEtCreationDossier() throws Exception {
    // Test d'intégration: import CSV puis création dossier
    Path tempClients = Files.createTempFile("clients", ".csv");
    Files.writeString(
        tempClients,
        "nom,prenom,email,tel,adresse\n"
            + "ImportTest,Jean,jean.import@test.com,0123456789,Adresse Import\n");

    CSVImporter importer = new CSVImporter(ds);
    importer.importClients(tempClients);

    // Le client devrait être réutilisé lors de la création du dossier
    var dossierSAV =
        savService.creerDossierSAV(
            "ImportTest",
            "Jean",
            "jean.import@test.com",
            "0123456789",
            "Adresse Import",
            "Asus",
            "Laptop",
            "ASUS999",
            "",
            "Clavier défaillant",
            "");

    assertEquals("jean.import@test.com", dossierSAV.client.email());
    assertEquals("0123456789", dossierSAV.client.tel());

    Files.deleteIfExists(tempClients);
  }

  @Test
  void importCSV_FichierInvalide() throws Exception {
    Path tempClients = Files.createTempFile("clients_invalide", ".csv");
    Files.writeString(
        tempClients, "nom,prenom,email,tel,adresse\n" + "TestIncomplete,Jean,\n" // Email manquant
        );

    CSVImporter importer = new CSVImporter(ds);

    // L'import devrait échouer avec un email manquant
    assertThrows(
        Exception.class,
        () ->
          importer.importClients(tempClients));

    Files.deleteIfExists(tempClients);
  }

  @Test
  void importCSV_TransactionRollback() throws Exception {
    Path tempClients = Files.createTempFile("clients_rollback", ".csv");
    Files.writeString(
        tempClients,
        "nom,prenom,email,tel,adresse\n"
            + "Valid,Client,valid@test.com,0123456789,Valid Address\n"
            + "Invalid,Client,,0987654321,Invalid Address\n" // Email vide
        );

    CSVImporter importer = new CSVImporter(ds);

    assertThrows(
        Exception.class,
        () ->
          importer.importClients(tempClients));

    // Vérifier qu'aucun client n'a été importé (rollback)
    var resultats = savService.rechercherParEmailClient("valid@test.com");
    assertEquals(0, resultats.size());

    Files.deleteIfExists(tempClients);
  }

  @Test
  void getDossierSAV() throws Exception {
    var dossierSAV =
        savService.creerDossierSAV(
            "GetTest",
            "User",
            "get@test.com",
            null,
            null,
            "Sony",
            "TV",
            "SONY123",
            "",
            "Pas d'image",
            "");

    Long dossierId = dossierSAV.dossier.id();
    var retrieved = savService.getDossierSAV(dossierId);

    assertEquals(dossierSAV.client.email(), retrieved.client.email());
    assertEquals(dossierSAV.appareil.marque(), retrieved.appareil.marque());
    assertEquals(dossierSAV.dossier.symptome(), retrieved.dossier.symptome());
  }

  @Test
  void getDossierSAV_Inexistant() throws Exception {
    assertThrows(
        IllegalArgumentException.class,
        () ->
          savService.getDossierSAV(999L));
  }

  @Test
  void validationParametresEntree() throws Exception {
    // Test avec des valeurs nulles/vides pour différents paramètres
    assertDoesNotThrow(
        () -> {
          savService.creerDossierSAV(
              "Nom",
              "Prenom",
              "test@email.com",
              null,
              null, // tel et adresse null
              "Marque",
              "Modele",
              "SN123",
              "", // accessoires vide
              "Symptome",
              "" // commentaire vide
              );
        });
  }

  @Test
  void gestionConcurrence_MultiplesDossiers() throws Exception {
    // Créer plusieurs dossiers rapidement pour tester la concurrence
    String[] noms = {"Dupont", "Martin", "Durand", "Bernard", "Thomas"};
    String[] prenoms = {"Jean", "Paul", "Marie", "Pierre", "Sophie"};

    for (int i = 1; i <= 5; i++) {
      savService.creerDossierSAV(
          noms[i - 1],
          prenoms[i - 1],
          "client" + i + "@test.com",
          null,
          null,
          "Marque",
          "Modele",
          "SN" + i,
          "",
          "Symptome " + i,
          "");
    }

    List<SAVService.DossierSAV> tous = savService.listerParStatut("recu");
    assertEquals(5, tous.size());
  }

  @Test
  void securite_InjectionSQL() throws Exception {
    // Tenter des injections SQL dans différents champs
    String maliciousInput = "'; DROP TABLE clients; --";

    // Avec notre validation renforcée, l'injection SQL est bloquée en amont
    assertThrows(
        IllegalArgumentException.class,
        () ->
          savService.creerDossierSAV(
              maliciousInput,
              "Prenom",
              "evil@test.com",
              null,
              null,
              maliciousInput,
              maliciousInput,
              maliciousInput,
              maliciousInput,
              maliciousInput,
              maliciousInput));

    // La validation empêche l'insertion, donc aucune donnée ne devrait être créée
    var resultats = savService.listerParStatut("recu");
    assertEquals(0, resultats.size());
  }
}
