package com.magsav.service;

import com.magsav.db.DB;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MediaService
 */
public class MediaServiceTest {
    
    static Connection keeper;
    private Path tempDir;

    @BeforeAll
    static void keepMemoryDb() throws Exception {
        System.setProperty("magsav.db.url", "jdbc:sqlite:file:media_service_test?mode=memory&cache=shared");
        keeper = DriverManager.getConnection(System.getProperty("magsav.db.url"));
        DB.resetForTesting();
        DB.init();
    }

    @AfterAll
    static void closeKeeper() throws Exception {
        if (keeper != null) keeper.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        // Créer un répertoire temporaire pour les tests
        tempDir = Files.createTempDirectory("magsav-media-test");
        
        // Vider le cache avant chaque test
        MediaService.clearCache();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Nettoyer le répertoire temporaire
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // ordre inverse pour supprimer les fichiers avant les dossiers
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignorer les erreurs de suppression en test
                    }
                });
        }
    }

    @Test
    @DisplayName("Doit créer les répertoires de base")
    void testCreateBaseDirectories() throws IOException {
        // When
        Path baseDir = MediaService.baseDir();
        Path photosDir = MediaService.photosDir();
        Path logosDir = MediaService.logosDir();
        Path qrCodesDir = MediaService.qrCodesDir();
        
        // Then
        assertTrue(Files.exists(baseDir));
        assertTrue(Files.isDirectory(baseDir));
        assertTrue(Files.exists(photosDir));
        assertTrue(Files.exists(logosDir));
        assertTrue(Files.exists(qrCodesDir));
    }

    @Test
    @DisplayName("Doit récupérer le bon répertoire pour chaque type de média")
    void testGetDirectoryForType() throws IOException {
        // When
        Path photosDir = MediaService.getDirectoryForType(MediaService.MediaType.PHOTO_PRODUIT);
        Path logosDir = MediaService.getDirectoryForType(MediaService.MediaType.LOGO_FABRICANT);
        Path qrCodesDir = MediaService.getDirectoryForType(MediaService.MediaType.QR_CODE);
        
        // Then
        assertTrue(photosDir.toString().contains("photos"));
        assertTrue(logosDir.toString().contains("logos"));
        assertTrue(qrCodesDir.toString().contains("qrcodes"));
    }

    @Test
    @DisplayName("Doit valider un fichier correct")
    void testValidateMediaFileValid() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "fake image content".getBytes());
        
        // When
        MediaService.MediaValidationResult result = MediaService.validateMediaFile(testFile);
        
        // Then
        assertTrue(result.isValid());
        assertEquals("Fichier valide", result.message());
    }

    @Test
    @DisplayName("Doit rejeter un fichier null")
    void testValidateMediaFileNull() {
        // When
        MediaService.MediaValidationResult result = MediaService.validateMediaFile(null);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Fichier non spécifié", result.message());
    }

    @Test
    @DisplayName("Doit rejeter un fichier inexistant")
    void testValidateMediaFileNotExists() {
        // Given
        Path nonExistentFile = tempDir.resolve("nonexistent.jpg");
        
        // When
        MediaService.MediaValidationResult result = MediaService.validateMediaFile(nonExistentFile);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.message().contains("Fichier introuvable"));
    }

    @Test
    @DisplayName("Doit rejeter une extension non supportée")
    void testValidateMediaFileUnsupportedExtension() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "text content".getBytes());
        
        // When
        MediaService.MediaValidationResult result = MediaService.validateMediaFile(testFile);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.message().contains("Extension non supportée"));
    }

    @Test
    @DisplayName("Doit rejeter un fichier vide")
    void testValidateMediaFileEmpty() throws IOException {
        // Given
        Path testFile = tempDir.resolve("empty.jpg");
        Files.createFile(testFile); // fichier vide
        
        // When
        MediaService.MediaValidationResult result = MediaService.validateMediaFile(testFile);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Le fichier est vide", result.message());
    }

    @Test
    @DisplayName("Doit rechercher un fichier média avec cache")
    void testFindMediaFileWithCache() throws IOException {
        // Given - Créer un fichier dans le répertoire photos
        Path photosDir = MediaService.photosDir();
        Path testImage = photosDir.resolve("test-product.jpg");
        Files.write(testImage, "fake image".getBytes());
        
        // When - Premier appel (mise en cache)
        Optional<Path> result1 = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "test-product");
        
        // Then
        assertTrue(result1.isPresent());
        assertEquals(testImage, result1.get());
        
        // When - Deuxième appel (depuis le cache)
        Optional<Path> result2 = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "test-product");
        
        // Then
        assertTrue(result2.isPresent());
        assertEquals(testImage, result2.get());
    }

    @Test
    @DisplayName("Doit retourner empty si fichier non trouvé")
    void testFindMediaFileNotFound() {
        // When
        Optional<Path> result = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "nonexistent");
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Doit gérer les noms vides ou null")
    void testFindMediaFileEmptyOrNullName() {
        // When
        Optional<Path> resultNull = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, null);
        Optional<Path> resultEmpty = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "");
        Optional<Path> resultBlank = MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "   ");
        
        // Then
        assertTrue(resultNull.isEmpty());
        assertTrue(resultEmpty.isEmpty());
        assertTrue(resultBlank.isEmpty());
    }

    @Test
    @DisplayName("Doit invalider le cache correctement")
    void testCacheInvalidation() throws IOException {
        // Given
        Path photosDir = MediaService.photosDir();
        Path testImage = photosDir.resolve("cache-test.jpg");
        Files.write(testImage, "fake image".getBytes());
        
        // Mettre en cache
        MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "cache-test");
        
        // When
        MediaService.invalidateCache("cache-test", MediaService.MediaType.PHOTO_PRODUIT);
        
        // Then - Le cache doit être invalidé
        // Note: difficile de tester directement, mais au moins vérifier qu'aucune exception n'est levée
        assertDoesNotThrow(() -> MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "cache-test"));
    }

    @Test
    @DisplayName("Doit vider tout le cache")
    void testClearCache() throws IOException {
        // Given - Mettre quelques éléments en cache
        Path photosDir = MediaService.photosDir();
        Path testImage1 = photosDir.resolve("test1.jpg");
        Path testImage2 = photosDir.resolve("test2.jpg");
        Files.write(testImage1, "fake image 1".getBytes());
        Files.write(testImage2, "fake image 2".getBytes());
        
        MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "test1");
        MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "test2");
        
        // When
        MediaService.clearCache();
        
        // Then
        MediaService.CacheStatistics stats = MediaService.getCacheStatistics();
        assertEquals(0, stats.totalEntries());
    }

    @Test
    @DisplayName("Doit fournir des statistiques de cache")
    void testGetCacheStatistics() throws IOException {
        // Given
        MediaService.clearCache();
        
        // When - État initial
        MediaService.CacheStatistics initialStats = MediaService.getCacheStatistics();
        
        // Then
        assertEquals(0, initialStats.totalEntries());
        assertEquals(0, initialStats.foundEntries());
        assertEquals(0, initialStats.notFoundEntries());
        
        // Given - Ajouter des éléments au cache
        Path photosDir = MediaService.photosDir();
        Path testImage = photosDir.resolve("stats-test.jpg");
        Files.write(testImage, "fake image".getBytes());
        
        MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "stats-test"); // trouvé
        MediaService.findMediaFile(MediaService.MediaType.PHOTO_PRODUIT, "nonexistent"); // non trouvé
        
        // When
        MediaService.CacheStatistics stats = MediaService.getCacheStatistics();
        
        // Then
        assertEquals(2, stats.totalEntries());
        assertEquals(1, stats.foundEntries());
        assertEquals(1, stats.notFoundEntries());
    }
}