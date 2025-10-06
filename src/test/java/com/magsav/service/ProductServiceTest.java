package com.magsav.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ProductService
 * Tests simples des méthodes utilitaires
 */
class ProductServiceTest {

    private ProductService productService = new ProductService(null); // Repository pas utilisé pour ces tests

    @Test
    void should_validate_correct_uid_format() {
        // When & Then
        assertTrue(productService.isValidUidFormat("ABC1234"));
        assertTrue(productService.isValidUidFormat("XYZ9876"));
        assertTrue(productService.isValidUidFormat("DEF0000"));
        assertTrue(productService.isValidUidFormat("GHI9999"));
    }

    @Test
    void should_reject_invalid_uid_formats() {
        // When & Then
        assertFalse(productService.isValidUidFormat("ABC12345")); // Too many digits
        assertFalse(productService.isValidUidFormat("AB1234"));   // Too few letters
        assertFalse(productService.isValidUidFormat("ABCD123"));  // Too many letters
        assertFalse(productService.isValidUidFormat("123ABCD"));  // Wrong format
        assertFalse(productService.isValidUidFormat("abc1234"));  // Lowercase
        assertFalse(productService.isValidUidFormat("AB C123"));  // Space
        assertFalse(productService.isValidUidFormat("AB-1234"));  // Dash
        assertFalse(productService.isValidUidFormat(null));       // Null
        assertFalse(productService.isValidUidFormat(""));         // Empty
        assertFalse(productService.isValidUidFormat("   "));      // Whitespace only
    }

    @Test
    void should_validate_uid_with_edge_cases() {
        // When & Then
        assertTrue(productService.isValidUidFormat("AAA0000")); // Min digits
        assertTrue(productService.isValidUidFormat("ZZZ9999")); // Max digits
        
        assertFalse(productService.isValidUidFormat("AA0000"));  // One letter missing
        assertFalse(productService.isValidUidFormat("AAAA000")); // One digit missing
        assertFalse(productService.isValidUidFormat("AAA000"));  // One digit missing
        assertFalse(productService.isValidUidFormat("AAA00000")); // One digit too many
    }

    @Test
    void should_throw_exception_for_invalid_uid() {
        // Given
        String[] invalidUids = {"INVALID", "abc1234", "123456", "", null, "AB-123"};

        // When & Then
        for (String invalidUid : invalidUids) {
            assertThrows(IllegalArgumentException.class, () -> {
                productService.validateUidOrThrow(invalidUid);
            }, "Devrait lever une exception pour l'UID invalide: " + invalidUid);
        }
    }

    @Test
    void should_not_throw_exception_for_valid_uid() {
        // Given
        String[] validUids = {"ABC1234", "XYZ0000", "DEF9999"};

        // When & Then
        for (String validUid : validUids) {
            assertDoesNotThrow(() -> {
                productService.validateUidOrThrow(validUid);
            }, "Ne devrait pas lever d'exception pour l'UID valide: " + validUid);
        }
    }

    @Test
    void should_validate_confirmation_logic() {
        // When & Then
        assertFalse(productService.updateManufacturerWithConfirmation(1L, "Test", false));
        // Note: Le test avec confirmation=true nécessiterait un mock du repository
    }

    @Test
    void should_handle_case_sensitivity_in_uid_validation() {
        // When & Then
        assertTrue(productService.isValidUidFormat("ABC1234"));   // All uppercase
        assertFalse(productService.isValidUidFormat("abc1234"));  // All lowercase
        assertFalse(productService.isValidUidFormat("Abc1234"));  // Mixed case
        assertFalse(productService.isValidUidFormat("ABc1234"));  // Mixed case
        assertFalse(productService.isValidUidFormat("ABC1234a")); // Letter in digits
    }

    @Test
    void should_validate_digit_requirements() {
        // When & Then
        assertTrue(productService.isValidUidFormat("ABC0123"));   // Leading zero
        assertTrue(productService.isValidUidFormat("ABC9876"));   // No leading zero
        
        assertFalse(productService.isValidUidFormat("ABCABCD"));  // Letters instead of digits
        assertFalse(productService.isValidUidFormat("ABC12AB"));  // Mixed letters and digits
        assertFalse(productService.isValidUidFormat("ABC-123"));  // Special character
    }

    @Test
    void should_validate_letter_requirements() {
        // When & Then
        assertTrue(productService.isValidUidFormat("XYZ1234"));   // Standard letters
        
        assertFalse(productService.isValidUidFormat("12Y1234"));  // Digit in letters
        assertFalse(productService.isValidUidFormat("X2Z1234"));  // Digit in letters
        assertFalse(productService.isValidUidFormat("XY21234"));  // Digit in letters
        assertFalse(productService.isValidUidFormat("A B1234"));  // Space in letters
    }
}