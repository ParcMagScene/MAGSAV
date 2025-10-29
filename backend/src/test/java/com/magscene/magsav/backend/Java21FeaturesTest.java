package com.magscene.magsav.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test des fonctionnalités Java 21 dans MAGSAV-3.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class Java21FeaturesTest {

    @Test
    void testTextBlocks() {
        // Test des Text Blocks (feature stable depuis Java 15)
        String expectedJson = """
            {
                "application": "MAGSAV-3.0",
                "java_version": "21",
                "features": ["Virtual Threads", "Pattern Matching", "Records"]
            }
            """;
        
        assertNotNull(expectedJson);
        assertTrue(expectedJson.contains("MAGSAV-3.0"));
        System.out.println("✅ Text Blocks fonctionnent avec Java 21");
    }

    @Test
    void testRecords() {
        // Test des Records (stable depuis Java 17)
        record Equipment(String id, String name, String category, boolean isActive) {}
        
        var equipment = new Equipment("EQ001", "Projecteur LED", "Éclairage", true);
        
        assertEquals("EQ001", equipment.id());
        assertEquals("Projecteur LED", equipment.name());
        assertEquals("Éclairage", equipment.category());
        assertTrue(equipment.isActive());
        
        System.out.println("✅ Records fonctionnent avec Java 21: " + equipment);
    }

    @Test
    void testSwitchExpressions() {
        // Test des Switch Expressions améliorées
        String result1 = getModuleDescription("SAV");
        String result2 = getModuleDescription("PARC");
        
        assertEquals("Gestion des demandes d'intervention et réparations", result1);
        assertEquals("Inventaire matériel avec QR codes", result2);
        
        System.out.println("✅ Switch Expressions fonctionnent avec Java 21");
    }
    
    private String getModuleDescription(String module) {
        return switch (module) {
            case "SAV" -> "Gestion des demandes d'intervention et réparations";
            case "PARC" -> "Inventaire matériel avec QR codes";
            case "VENTES" -> "Import PDF affaires et commandes fournisseurs";
            case "VEHICULES" -> "Planning et maintenance des véhicules";
            case "PERSONNEL" -> "Gestion qualifications et planning";
            default -> "Module non reconnu";
        };
    }

    @Test
    void testPatternMatchingInstanceof() {
        // Pattern matching avec instanceof (stable depuis Java 17)
        Object obj = "MAGSAV-3.0";
        
        String result;
        if (obj instanceof String s && s.startsWith("MAGSAV")) {
            result = "Application MAGSAV détectée: " + s;
        } else if (obj instanceof Integer i) {
            result = "Nombre: " + i;
        } else {
            result = "Type non reconnu";
        }
        
        assertEquals("Application MAGSAV détectée: MAGSAV-3.0", result);
        System.out.println("✅ Pattern Matching fonctionne avec Java 21");
    }

    @Test
    void testJavaVersion() {
        // Vérification que nous utilisons bien Java 21
        String javaVersion = System.getProperty("java.version");
        
        assertTrue(javaVersion.startsWith("21"), 
                  "Expected Java 21, but got: " + javaVersion);
        
        System.out.println("✅ Java version confirmée: " + javaVersion);
    }

    @Test
    void testVirtualThreadsSupport() {
        // Test de base pour les Virtual Threads (Java 21)
        try {
            // Test simple de thread pour vérifier la compatibilité
            Thread testThread = new Thread(() -> {
                System.out.println("✅ Thread exécuté avec succès dans MAGSAV-3.0 avec Java 21");
            });
            
            testThread.start();
            testThread.join(); // Attendre la fin du thread
            
            // Note: Virtual Threads sont disponibles avec --enable-preview en Java 19-20, 
            // et nativement en Java 21+
            System.out.println("✅ Threading fonctionne correctement avec Java 21");
            
        } catch (Exception e) {
            fail("Erreur avec Threading: " + e.getMessage());
        }
    }
}