package com.magsav.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class UidGenerationServiceTest {
    
    private UidGenerationService uidService;
    
    @BeforeEach
    void setUp() {
        uidService = new UidGenerationService();
    }
    
    @Test
    void testGenerateRandomUid_ValidFormat() {
        String uid = uidService.generateUniqueUid();
        
        assertNotNull(uid);
        assertTrue(uidService.isValidUidFormat(uid));
        assertEquals(7, uid.length());
        
        // Vérifier format: 3 lettres + 4 chiffres
        for (int i = 0; i < 3; i++) {
            assertTrue(Character.isLetter(uid.charAt(i)));
            assertTrue(Character.isUpperCase(uid.charAt(i)));
        }
        
        for (int i = 3; i < 7; i++) {
            assertTrue(Character.isDigit(uid.charAt(i)));
        }
    }
    
    @Test
    void testIsValidUidFormat() {
        // Formats valides
        assertTrue(uidService.isValidUidFormat("ABC1234"));
        assertTrue(uidService.isValidUidFormat("XYZ9999"));
        assertTrue(uidService.isValidUidFormat("AAA0000"));
        
        // Formats invalides
        assertFalse(uidService.isValidUidFormat("abc1234")); // minuscules
        assertFalse(uidService.isValidUidFormat("AB1234"));  // trop court
        assertFalse(uidService.isValidUidFormat("ABCD1234")); // trop long
        assertFalse(uidService.isValidUidFormat("12A1234"));  // chiffre au début
        assertFalse(uidService.isValidUidFormat("ABCDEFG"));  // pas de chiffres
        assertFalse(uidService.isValidUidFormat("1234567"));  // que des chiffres
        assertFalse(uidService.isValidUidFormat(null));       // null
        assertFalse(uidService.isValidUidFormat(""));         // vide
    }
    
    @Test
    void testNormalizeUid() {
        assertEquals("ABC1234", uidService.normalizeUid("abc1234"));
        assertEquals("XYZ9999", uidService.normalizeUid("  xyz9999  "));
        assertEquals("AAA0000", uidService.normalizeUid("aaa0000"));
        assertNull(uidService.normalizeUid(null));
    }
    
    @Test
    void testGetQrCodeFileName() {
        assertEquals("ABC1234.png", uidService.getQrCodeFileName("ABC1234"));
        assertEquals("XYZ9999.png", uidService.getQrCodeFileName("xyz9999"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            uidService.getQrCodeFileName("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            uidService.getQrCodeFileName(null);
        });
    }
    
    @Test
    void testGetQrCodePath() {
        assertEquals("medias/qrcodes/ABC1234.png", uidService.getQrCodePath("ABC1234"));
        assertEquals("medias/qrcodes/XYZ9999.png", uidService.getQrCodePath("xyz9999"));
    }
    
    @Test
    void testGenerateNewProductUid() {
        UidGenerationService.NewProductWithUid result = uidService.generateNewProductUid();
        
        assertNotNull(result);
        assertNotNull(result.uid());
        assertNotNull(result.qrCodeFileName());
        assertNotNull(result.qrCodePath());
        
        assertTrue(uidService.isValidUidFormat(result.uid()));
        assertTrue(result.qrCodeFileName().endsWith(".png"));
        assertTrue(result.qrCodePath().startsWith("medias/qrcodes/"));
        assertTrue(result.qrCodePath().endsWith(".png"));
    }
}