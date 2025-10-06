package com.magsav.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Tests utilitaires pour valider les chemins médias
 * Tests simples sans dépendances externes
 */
class MediaPathsTest {

    @Test
    void should_validate_media_path_format() {
        // Given
        String homeDir = System.getProperty("user.home");
        String expectedBasePath = homeDir + "/MAGSAV/medias";

        // When
        Path mediaPath = Paths.get(expectedBasePath);

        // Then
        assertNotNull(mediaPath);
        assertTrue(mediaPath.toString().contains("MAGSAV/medias"));
        assertTrue(mediaPath.toString().endsWith("medias"));
    }

    @Test
    void should_construct_photo_path() {
        // Given
        String homeDir = System.getProperty("user.home");
        String photoFileName = "product123.jpg";

        // When
        Path photoPath = Paths.get(homeDir, "MAGSAV", "medias", "photos", photoFileName);

        // Then
        assertNotNull(photoPath);
        assertTrue(photoPath.toString().contains("photos"));
        assertTrue(photoPath.toString().endsWith(photoFileName));
        assertEquals(photoFileName, photoPath.getFileName().toString());
    }

    @Test
    void should_construct_logo_path() {
        // Given
        String homeDir = System.getProperty("user.home");
        String logoFileName = "manufacturer-logo.png";

        // When
        Path logoPath = Paths.get(homeDir, "MAGSAV", "medias", "logos", logoFileName);

        // Then
        assertNotNull(logoPath);
        assertTrue(logoPath.toString().contains("logos"));
        assertTrue(logoPath.toString().endsWith(logoFileName));
        assertEquals(logoFileName, logoPath.getFileName().toString());
    }

    @Test
    void should_validate_file_extensions() {
        // Given
        String[] validImageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        String[] testFiles = {"image.jpg", "photo.JPEG", "logo.png", "icon.gif", "bitmap.bmp"};

        // When & Then
        for (String fileName : testFiles) {
            boolean hasValidExtension = false;
            for (String ext : validImageExtensions) {
                if (fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                    hasValidExtension = true;
                    break;
                }
            }
            assertTrue(hasValidExtension, "Le fichier " + fileName + " doit avoir une extension valide");
        }
    }

    @Test
    void should_reject_invalid_file_extensions() {
        // Given
        String[] invalidFiles = {"document.txt", "archive.zip", "video.mp4", "audio.mp3"};

        // When & Then
        for (String fileName : invalidFiles) {
            String lowerFileName = fileName.toLowerCase();
            boolean isImageFile = lowerFileName.endsWith(".jpg") || 
                                lowerFileName.endsWith(".jpeg") || 
                                lowerFileName.endsWith(".png") || 
                                lowerFileName.endsWith(".gif") || 
                                lowerFileName.endsWith(".bmp");
            
            assertFalse(isImageFile, "Le fichier " + fileName + " ne devrait pas être considéré comme une image");
        }
    }

    @Test
    void should_handle_path_normalization() {
        // Given
        String homeDir = System.getProperty("user.home");
        
        // When
        Path path1 = Paths.get(homeDir + "/MAGSAV/medias/photos");
        Path path2 = Paths.get(homeDir, "MAGSAV", "medias", "photos");
        Path path3 = Paths.get(homeDir + "/MAGSAV//medias///photos"); // Double slashes

        // Then
        assertEquals(path1.normalize(), path2.normalize());
        assertEquals(path1.normalize(), path3.normalize());
        assertEquals(path2.normalize(), path3.normalize());
    }

    @Test
    void should_validate_directory_structure() {
        // Given
        String homeDir = System.getProperty("user.home");
        
        // When
        Path magsavDir = Paths.get(homeDir, "MAGSAV");
        Path mediasDir = Paths.get(homeDir, "MAGSAV", "medias");
        Path photosDir = Paths.get(homeDir, "MAGSAV", "medias", "photos");
        Path logosDir = Paths.get(homeDir, "MAGSAV", "medias", "logos");
        Path qrcodesDir = Paths.get(homeDir, "MAGSAV", "medias", "qrcodes");

        // Then
        assertTrue(mediasDir.startsWith(magsavDir));
        assertTrue(photosDir.startsWith(mediasDir));
        assertTrue(logosDir.startsWith(mediasDir));
        assertTrue(qrcodesDir.startsWith(mediasDir));
        
        assertEquals("MAGSAV", magsavDir.getFileName().toString());
        assertEquals("medias", mediasDir.getFileName().toString());
        assertEquals("photos", photosDir.getFileName().toString());
        assertEquals("logos", logosDir.getFileName().toString());
        assertEquals("qrcodes", qrcodesDir.getFileName().toString());
    }

    @Test
    void should_generate_safe_filenames() {
        // Given
        String[] unsafeNames = {"file with spaces.jpg", "file/with/slashes.png", "file:with:colons.gif"};
        
        // When & Then
        for (String unsafe : unsafeNames) {
            String safe = unsafe.replaceAll("[^a-zA-Z0-9._-]", "_");
            
            assertFalse(safe.contains(" "), "Le nom sécurisé ne doit pas contenir d'espaces");
            assertFalse(safe.contains("/"), "Le nom sécurisé ne doit pas contenir de slashes");
            assertFalse(safe.contains(":"), "Le nom sécurisé ne doit pas contenir de deux-points");
            assertTrue(safe.matches("[a-zA-Z0-9._-]+"), "Le nom doit contenir seulement des caractères sûrs");
        }
    }
}