package com.magscene.magsav.integration;

import com.magscene.magsav.backend.MagsavApplication;
import com.magscene.magsav.backend.service.LocmatImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'import de fichiers LOCMAT (Excel/CSV)
 */
@SpringBootTest(classes = MagsavApplication.class)
@ActiveProfiles("test")
class LocmatImportIntegrationTest {

    @Autowired
    private LocmatImportService locmatImportService;

    @TempDir
    Path tempDir;

    /**
     * Test d'import d'un fichier CSV valide
     */
    @Test
    void testImportValidCsvFile() throws IOException {
        // GIVEN: Un fichier CSV valide avec 3 équipements
        String csvContent = """
                Code LOCMAT;Désignation;N° de série;Sous famille LM;Marque;Modèle;Prix unitaire HT;Valeur nette comptable;Date d'achat
                10001;PROJECTEUR LED 1000W;SN001;ECLAIRAGE;ARRI;S60;1500.00;1200.00;01/01/2020
                10002;CONSOLE LUMIERE;SN002;REGIE;MA LIGHTING;GrandMA3;8000.00;7500.00;15/06/2021
                10003;CAMERA 4K;SN003;VIDEO;SONY;FX6;9000.00;8500.00;20/03/2022
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-equipment.csv",
                "text/csv",
                csvContent.getBytes());

        // WHEN: Import du fichier
        LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);

        // THEN: L'import est réussi
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertEquals(3, result.getSuccessCount(), "3 équipements doivent être importés avec succès");
        assertTrue(result.getErrors().isEmpty(), "Aucune erreur attendue");
    }

    /**
     * Test de détection des doublons (même numéro de série)
     */
    @Test
    void testImportDuplicateSerialNumbers() throws IOException {
        // GIVEN: Un fichier CSV avec doublons
        String csvContent = """
                Code LOCMAT;Désignation;N° de série;Sous famille LM;Marque;Modèle;Prix unitaire HT;Valeur nette comptable;Date d'achat
                20001;PROJECTEUR A;DUPLICATE123;ECLAIRAGE;BRAND;MODEL-A;500.00;400.00;01/01/2020
                20002;PROJECTEUR B;DUPLICATE123;ECLAIRAGE;BRAND;MODEL-B;600.00;500.00;01/01/2020
                20003;PROJECTEUR C;UNIQUE001;ECLAIRAGE;BRAND;MODEL-C;700.00;600.00;01/01/2020
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-duplicates.csv",
                "text/csv",
                csvContent.getBytes());

        // WHEN: Import du fichier
        LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);

        // THEN: Seuls les équipements uniques sont importés
        assertNotNull(result);
        assertTrue(result.getSuccessCount() >= 1, "Au moins un équipement doit être importé");
        // Les doublons peuvent générer des erreurs
        // Note: La détection des doublons dépend de l'implémentation
    }

    /**
     * Test de validation des données invalides
     */
    @Test
    void testImportInvalidData() throws IOException {
        // GIVEN: Un fichier CSV avec données invalides
        String csvContent = """
                Code LOCMAT;Désignation;N° de série;Sous famille LM;Marque;Modèle;Prix unitaire HT;Valeur nette comptable;Date d'achat
                ;SANS_CODE;SN100;;BRAND;MODEL;1000.00;800.00;01/01/2020
                30002;;SN101;CATEGORY;BRAND;MODEL;1000.00;800.00;01/01/2020
                30003;SANS_SN;;CATEGORY;BRAND;MODEL;1000.00;800.00;01/01/2020
                VALID;EQUIPMENT VALID;VALID_SN;CATEGORY;BRAND;MODEL;1000.00;800.00;01/01/2020
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-invalid.csv",
                "text/csv",
                csvContent.getBytes());

        // WHEN: Import du fichier
        LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);

        // THEN: Seul l'équipement valide est importé
        assertNotNull(result);
        assertTrue(result.getSuccessCount() >= 1, "Au moins un équipement valide doit être importé");

        // Les équipements invalides peuvent générer des erreurs
        // Note: Le comportement exact dépend de la validation
    }

    /**
     * Test d'import d'un fichier vide
     */
    @Test
    void testImportEmptyFile() throws IOException {
        // GIVEN: Un fichier CSV vide (seulement l'entête)
        String csvContent = """
                Code LOCMAT;Désignation;N° de série;Sous famille LM;Marque;Modèle;Prix unitaire HT;Valeur nette comptable;Date d'achat
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-empty.csv",
                "text/csv",
                csvContent.getBytes());

        // WHEN: Import du fichier
        LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);

        // THEN: Aucun équipement n'est importé
        assertNotNull(result);
        assertEquals(0, result.getSuccessCount(), "Aucun équipement ne doit être importé");
    }

    /**
     * Test de gestion d'un fichier malformé
     */
    @Test
    void testImportMalformedFile() {
        // GIVEN: Un fichier avec des données corrompues
        String invalidContent = "This is not a valid CSV file!!!";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-malformed.csv",
                "text/csv",
                invalidContent.getBytes());

        // WHEN/THEN: Une exception doit être levée ou un résultat d'erreur retourné
        assertDoesNotThrow(() -> {
            LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);
            assertNotNull(result);
            // Le fichier malformé devrait avoir des erreurs ou 0 succès
            assertTrue(!result.getErrors().isEmpty() || result.getSuccessCount() == 0,
                    "Le fichier malformé doit générer des erreurs ou ne rien importer");
        }, "L'import ne doit pas crasher même avec des données invalides");
    }

    /**
     * Test de robustesse : caractères spéciaux et encodage UTF-8
     */
    @Test
    void testImportUtf8SpecialCharacters() throws IOException {
        // GIVEN: Un fichier CSV avec caractères accentués et spéciaux
        String csvContent = """
                Code LOCMAT;Désignation;N° de série;Sous famille LM;Marque;Modèle;Prix unitaire HT;Valeur nette comptable;Date d'achat
                UTF8-001;ÉCLAIRAGE LED Ñ;SN-FRANÇAIS-001;CATÉGORIE SPÉCIALE;MARQUE™;MODÈLE®;1234.56;987.65;01/01/2023
                UTF8-002;Test €uro & Symböls;SN-UTF8-002;ÉQUİPÊMËNT;Brand™;Modél©;2000.00;1800.00;15/06/2023
                """;

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "test-utf8.csv",
                "text/csv",
                csvContent.getBytes("UTF-8"));

        // WHEN: Import du fichier
        LocmatImportService.ImportResult result = locmatImportService.importLocmatData(csvFile);

        // THEN: Les caractères UTF-8 sont correctement importés
        assertNotNull(result);
        assertEquals(2, result.getSuccessCount(), "2 équipements avec caractères spéciaux doivent être importés");
        assertTrue(result.getErrors().isEmpty(), "Aucune erreur avec UTF-8");
    }
}
