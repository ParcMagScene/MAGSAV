package com.magscene.magsav.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de démarrage de l'application MAGSAV avec Java 21
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MagsavApplicationTest {

    @Test
    void contextLoads() {
        // Test que l'application démarre correctement avec Java 21
        System.out.println("✅ Application MAGSAV-3.0 testée avec Java " + 
                          System.getProperty("java.version"));
    }
}
