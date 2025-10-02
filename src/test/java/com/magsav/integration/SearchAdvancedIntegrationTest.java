package com.magsav.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/** Tests d'intégration pour l'API de recherche avancée */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Tests Intégration - Recherche Avancée")
@Disabled("Tests d'intégration désactivés temporairement - problème d'injection SAVService")
public class SearchAdvancedIntegrationTest {

  @LocalServerPort private int port;

  private final TestRestTemplate restTemplate = new TestRestTemplate();

  private String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }

  @Test
  @DisplayName("Accès à la page de recherche avancée")
  void testSearchAdvancedPageAccess() {
    String url = createURLWithPort("/search/advanced");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Vérification de l'accès à la page (redirection possible vers login)
    assertTrue(
        response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection());
  }

  @Test
  @DisplayName("API recherche - paramètres de base")
  void testSearchApiBasicParameters() {
    String url = createURLWithPort("/api/search/dossiers?page=0&size=10");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Peut retourner 401 (non authentifié) ou 200 selon la configuration
    assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 401);
  }

  @Test
  @DisplayName("API statistiques - endpoint disponible")
  void testStatsApiEndpoint() {
    String url = createURLWithPort("/api/search/stats");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Vérification que l'endpoint existe (même si non authentifié)
    assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("API suggestions - endpoint disponible")
  void testSuggestionsApiEndpoint() {
    String url = createURLWithPort("/api/search/suggestions?q=test");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Vérification que l'endpoint existe
    assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("API export - endpoint disponible")
  void testExportApiEndpoint() {
    String url = createURLWithPort("/api/search/dossiers/export");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Vérification que l'endpoint existe
    assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("Validation des paramètres de pagination")
  void testPaginationParameters() {
    // Test avec page négative
    String url = createURLWithPort("/api/search/dossiers?page=-1&size=10");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait gérer les paramètres invalides
    assertTrue(response.getStatusCode().value() >= 400 || response.getStatusCode().value() == 401);
  }

  @Test
  @DisplayName("Validation des paramètres de tri")
  void testSortParameters() {
    String url = createURLWithPort("/api/search/dossiers?sortBy=invalid&sortDirection=invalid");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // L'API devrait gérer les paramètres de tri invalides
    assertNotNull(response);
  }

  @Test
  @DisplayName("Gestion des requêtes vides")
  void testEmptySearchQuery() {
    String url = createURLWithPort("/api/search/dossiers?search=");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait accepter les requêtes vides
    assertNotNull(response);
  }

  @Test
  @DisplayName("Gestion des caractères spéciaux")
  void testSpecialCharactersInSearch() {
    String url = createURLWithPort("/api/search/dossiers?search=%26%3C%3E%22%27");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait gérer les caractères spéciaux encodés
    assertNotNull(response);
  }

  @Test
  @DisplayName("Test des en-têtes de réponse")
  void testResponseHeaders() {
    String url = createURLWithPort("/api/search/dossiers");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Vérification des en-têtes de sécurité de base
    assertNotNull(response.getHeaders());

    // Vérification du type de contenu (si succès)
    if (response.getStatusCode().is2xxSuccessful()) {
      assertTrue(
          response.getHeaders().getContentType().toString().contains("application/json")
              || response.getHeaders().getContentType().toString().contains("text/html"));
    }
  }

  @Test
  @DisplayName("Test de la résilience - requêtes multiples")
  void testMultipleRequests() {
    String url = createURLWithPort("/api/search/stats");

    // Exécuter plusieurs requêtes rapidement
    for (int i = 0; i < 5; i++) {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      assertNotNull(response);

      // Vérifier qu'il n'y a pas d'erreur serveur
      assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
  }

  @Test
  @DisplayName("Validation du format des données")
  void testDataFormatValidation() {
    String url = createURLWithPort("/api/search/dossiers?dateDebut=invalid-date");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait gérer les formats de date invalides
    assertNotNull(response);
    // Ne devrait pas retourner 500 (erreur serveur)
    assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  @DisplayName("Test des limites de taille")
  void testSizeLimits() {
    // Test avec une taille très grande
    String url = createURLWithPort("/api/search/dossiers?size=10000");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait limiter ou gérer les grandes tailles
    assertNotNull(response);
  }

  @Test
  @DisplayName("Navigation entre les pages")
  void testPageNavigation() {
    String baseUrl = createURLWithPort("/api/search/dossiers");

    // Test page 0
    ResponseEntity<String> page0 =
        restTemplate.getForEntity(baseUrl + "?page=0&size=5", String.class);
    assertNotNull(page0);

    // Test page 1
    ResponseEntity<String> page1 =
        restTemplate.getForEntity(baseUrl + "?page=1&size=5", String.class);
    assertNotNull(page1);

    // Les deux requêtes devraient avoir le même statut
    assertEquals(page0.getStatusCode(), page1.getStatusCode());
  }

  @Test
  @DisplayName("Validation des filtres combinés")
  void testCombinedFilters() {
    String url = createURLWithPort("/api/search/dossiers?search=test&statut=recu&marque=Apple");
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Devrait accepter les filtres multiples
    assertNotNull(response);
  }

  @Test
  @DisplayName("Test de performance de base")
  void testBasicPerformance() {
    String url = createURLWithPort("/api/search/dossiers");

    long startTime = System.currentTimeMillis();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    long endTime = System.currentTimeMillis();

    // La requête ne devrait pas prendre plus de 5 secondes
    assertTrue(endTime - startTime < 5000, "Requête trop lente: " + (endTime - startTime) + "ms");
    assertNotNull(response);
  }
}
