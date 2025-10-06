package com.magsav.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour IdService
 * Valide la génération d'UID avec des tests simples
 */
class IdServiceTest {

    private static final Pattern UID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");

    @Test
    void should_generate_uid_with_correct_format() {
        // When
        String uid = IdService.generateUid();

        // Then
        assertNotNull(uid);
        assertTrue(UID_PATTERN.matcher(uid).matches(), 
                  "L'UID généré doit respecter le format ABC1234: " + uid);
        assertEquals(7, uid.length(), "L'UID doit faire exactement 7 caractères");
        
        // Vérifie que les 3 premiers caractères sont des lettres
        assertTrue(uid.substring(0, 3).matches("[A-Z]{3}"), 
                  "Les 3 premiers caractères doivent être des lettres majuscules");
        
        // Vérifie que les 4 derniers caractères sont des chiffres
        assertTrue(uid.substring(3).matches("\\d{4}"), 
                  "Les 4 derniers caractères doivent être des chiffres");
    }

    @Test
    void should_generate_different_uids_on_multiple_calls() {
        // When
        String uid1 = IdService.generateUid();
        String uid2 = IdService.generateUid();
        String uid3 = IdService.generateUid();

        // Then
        assertNotEquals(uid1, uid2, "Chaque UID généré doit être différent");
        assertNotEquals(uid2, uid3, "Chaque UID généré doit être différent");
        assertNotEquals(uid1, uid3, "Chaque UID généré doit être différent");
        
        // Tous doivent respecter le format
        assertTrue(UID_PATTERN.matcher(uid1).matches());
        assertTrue(UID_PATTERN.matcher(uid2).matches());
        assertTrue(UID_PATTERN.matcher(uid3).matches());
    }

    @Test
    void should_validate_letters_are_uppercase() {
        // When
        String uid = IdService.generateUid();

        // Then
        String letters = uid.substring(0, 3);
        assertEquals(letters.toUpperCase(), letters, "Les lettres doivent être en majuscules");
        
        // Vérifie que chaque lettre est bien une lettre majuscule
        for (char c : letters.toCharArray()) {
            assertTrue(Character.isLetter(c), "Chaque caractère doit être une lettre");
            assertTrue(Character.isUpperCase(c), "Chaque lettre doit être en majuscule");
        }
    }

    @Test
    void should_validate_digits_are_numeric() {
        // When
        String uid = IdService.generateUid();

        // Then
        String digits = uid.substring(3);
        assertDoesNotThrow(() -> {
            Integer.parseInt(digits);
        }, "Les 4 derniers caractères doivent être des chiffres valides");
        
        int numericValue = Integer.parseInt(digits);
        assertTrue(numericValue >= 0 && numericValue <= 9999, 
                  "Les chiffres doivent être entre 0000 et 9999");
        
        // Vérifie que chaque caractère est bien un chiffre
        for (char c : digits.toCharArray()) {
            assertTrue(Character.isDigit(c), "Chaque caractère doit être un chiffre");
        }
    }

    @Test
    void should_generate_statistically_distributed_uids() {
        // When - génère plusieurs UIDs pour vérifier la distribution
        Set<String> generatedUids = new HashSet<>();
        Set<String> letterCombinations = new HashSet<>();
        Set<String> numberCombinations = new HashSet<>();
        
        int numberOfTests = 100;
        for (int i = 0; i < numberOfTests; i++) {
            String uid = IdService.generateUid();
            generatedUids.add(uid);
            letterCombinations.add(uid.substring(0, 3));
            numberCombinations.add(uid.substring(3));
        }

        // Then
        // La plupart des UIDs devraient être uniques
        assertTrue(generatedUids.size() > numberOfTests * 0.8, 
                  "Au moins 80% des UIDs générés devraient être uniques");
        
        // Devrait avoir des combinaisons de lettres variées
        assertTrue(letterCombinations.size() > 10, 
                  "Devrait générer au moins 10 combinaisons de lettres différentes");
        
        // Devrait avoir des combinaisons de chiffres variées
        assertTrue(numberCombinations.size() > 10, 
                  "Devrait générer au moins 10 combinaisons de chiffres différentes");
    }

    @Test
    void should_validate_four_digit_padding() {
        // When - teste spécifiquement le padding avec de petits nombres
        for (int i = 0; i < 50; i++) {
            String uid = IdService.generateUid();
            String digits = uid.substring(3);
            
            // Then
            assertEquals(4, digits.length(), "Les chiffres doivent toujours faire 4 caractères");
            assertTrue(digits.matches("\\d{4}"), "Doit être exactement 4 chiffres");
            
            // Vérifie que même les petits nombres sont bien paddés
            int value = Integer.parseInt(digits);
            if (value < 10) {
                assertTrue(digits.startsWith("000"), "Les nombres < 10 doivent commencer par 000");
            } else if (value < 100) {
                assertTrue(digits.startsWith("00"), "Les nombres < 100 doivent commencer par 00");
            } else if (value < 1000) {
                assertTrue(digits.startsWith("0"), "Les nombres < 1000 doivent commencer par 0");
            }
        }
    }

    @Test
    void should_use_only_alphabetic_letters() {
        // When
        for (int i = 0; i < 20; i++) {
            String uid = IdService.generateUid();
            String letters = uid.substring(0, 3);
            
            // Then
            for (char c : letters.toCharArray()) {
                assertTrue(c >= 'A' && c <= 'Z', 
                          "Chaque lettre doit être entre A et Z: " + c);
                assertFalse(Character.isDigit(c), 
                          "Les lettres ne doivent pas être des chiffres");
                assertFalse(Character.isLowerCase(c), 
                          "Les lettres ne doivent pas être en minuscule");
            }
        }
    }

    @Test
    void should_validate_complete_uid_structure() {
        // When
        String uid = IdService.generateUid();

        // Then
        assertNotNull(uid, "L'UID ne doit pas être null");
        assertFalse(uid.isEmpty(), "L'UID ne doit pas être vide");
        assertEquals(7, uid.length(), "L'UID doit faire exactement 7 caractères");
        
        // Structure complète: 3 lettres + 4 chiffres
        assertTrue(uid.matches("^[A-Z]{3}\\d{4}$"), 
                  "L'UID doit correspondre exactement au pattern ^[A-Z]{3}\\d{4}$");
        
        // Pas d'espaces ou autres caractères
        assertFalse(uid.contains(" "), "L'UID ne doit pas contenir d'espaces");
        assertFalse(uid.contains("-"), "L'UID ne doit pas contenir de tirets");
        assertFalse(uid.contains("_"), "L'UID ne doit pas contenir d'underscores");
    }
}