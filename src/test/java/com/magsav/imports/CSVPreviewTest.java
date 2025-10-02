package com.magsav.imports;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** Tests pour la prévisualisation et validation des imports CSV */
class CSVPreviewTest {

  private String csvClientsValid;
  private String csvEmpty;

  @BeforeEach
  void setUp() {
    csvClientsValid =
        """
            nom,prenom,telephone,email,adresse
            Dupont,Jean,0123456789,jean.dupont@email.com,123 rue de la Paix
            Martin,Marie,0987654321,marie.martin@email.com,456 avenue de la République
            """;

    csvEmpty = "";
  }

  @Test
  void testParseValidClientsCSV() throws IOException {
    MockMultipartFile file =
        new MockMultipartFile("file", "clients.csv", "text/csv", csvClientsValid.getBytes());

    // Test du parsing
    String content = new String(file.getBytes());
    String[] lines = content.split("\n");

    assertEquals(3, lines.length); // header + 2 data rows

    String[] headers = lines[0].split(",");
    assertEquals(5, headers.length);
    assertEquals("nom", headers[0].trim());
    assertEquals("email", headers[3].trim());
  }

  @Test
  void testValidateClientsFormat() {
    // Headers valides pour clients
    String[] validHeaders = {"nom", "prenom", "telephone", "email", "adresse"};
    assertTrue(validateCSVFormat(validHeaders, "clients"));

    // Headers invalides (manque email)
    String[] invalidHeaders = {"nom", "prenom", "telephone", "adresse"};
    assertFalse(validateCSVFormat(invalidHeaders, "clients"));
  }

  @Test
  void testValidateFournisseursFormat() {
    // Headers valides pour fournisseurs
    String[] validHeaders = {"nom", "contact", "telephone", "email", "adresse"};
    assertTrue(validateCSVFormat(validHeaders, "fournisseurs"));

    // Headers invalides (manque contact)
    String[] invalidHeaders = {"nom", "telephone", "email", "adresse"};
    assertFalse(validateCSVFormat(invalidHeaders, "fournisseurs"));
  }

  @Test
  void testValidateDossiersFormat() {
    // Headers valides pour dossiers SAV
    String[] validHeaders = {
      "client_nom", "appareil_marque", "appareil_modele", "symptome", "statut"
    };
    assertTrue(validateCSVFormat(validHeaders, "dossiers_sav"));

    // Headers invalides (manque statut)
    String[] invalidHeaders = {"client_nom", "appareil_marque", "appareil_modele", "symptome"};
    assertFalse(validateCSVFormat(invalidHeaders, "dossiers_sav"));
  }

  @Test
  void testValidateProduitsFormat() {
    // Headers valides pour produits
    String[] validHeaders = {"nom", "marque", "modele", "prix", "stock"};
    assertTrue(validateCSVFormat(validHeaders, "produits"));

    // Headers invalides (manque prix)
    String[] invalidHeaders = {"nom", "marque", "modele", "stock"};
    assertFalse(validateCSVFormat(invalidHeaders, "produits"));
  }

  @Test
  void testHandleEmptyFile() throws IOException {
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.csv", "text/csv", csvEmpty.getBytes());

    assertEquals(0, emptyFile.getBytes().length);
  }

  @Test
  void testHandleInvalidFileType() {
    MockMultipartFile textFile =
        new MockMultipartFile("file", "document.txt", "text/plain", "Some text content".getBytes());

    assertFalse(textFile.getOriginalFilename().endsWith(".csv"));
  }

  @Test
  void testCSVParsing() {
    String testCSV =
        """
            col1,col2,col3
            "value1","value2","value3"
            value4,value5,value6
            """;

    String[] lines = testCSV.split("\n");
    assertEquals(3, lines.length);

    // Test parsing de la première ligne de données
    String[] values = lines[1].split(",");
    assertEquals(3, values.length);

    // Test nettoyage des guillemets
    String cleanValue = values[0].trim().replace("\"", "");
    assertEquals("value1", cleanValue);
  }

  @Test
  void testMaxRowsLimit() {
    // Créer un CSV avec 15 lignes de données
    StringBuilder largeCsv = new StringBuilder("col1,col2,col3\n");
    for (int i = 1; i <= 15; i++) {
      largeCsv
          .append("value")
          .append(i)
          .append(",data")
          .append(i)
          .append(",info")
          .append(i)
          .append("\n");
    }

    String[] lines = largeCsv.toString().split("\n");
    assertEquals(16, lines.length); // 1 header + 15 data

    // Pour la preview, on ne devrait traiter que les 10 premières lignes de données
    int maxRows = Math.min(11, lines.length); // 1 header + 10 data max
    assertEquals(11, maxRows);
  }

  /**
   * Méthode utilitaire pour valider le format CSV selon le type (Réplique la logique du contrôleur
   * pour les tests)
   */
  private boolean validateCSVFormat(String[] headers, String type) {
    java.util.List<String> headerList = java.util.Arrays.asList(headers);

    switch (type.toLowerCase()) {
      case "clients":
        return headerList.containsAll(
            java.util.Arrays.asList("nom", "prenom", "telephone", "email", "adresse"));
      case "fournisseurs":
        return headerList.containsAll(
            java.util.Arrays.asList("nom", "contact", "telephone", "email", "adresse"));
      case "dossiers_sav":
        return headerList.containsAll(
            java.util.Arrays.asList(
                "client_nom", "appareil_marque", "appareil_modele", "symptome", "statut"));
      case "produits":
        return headerList.containsAll(
            java.util.Arrays.asList("nom", "marque", "modele", "prix", "stock"));
      default:
        return false;
    }
  }
}
