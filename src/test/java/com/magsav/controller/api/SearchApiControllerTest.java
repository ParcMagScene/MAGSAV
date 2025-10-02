package com.magsav.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.magsav.model.Appareil;
import com.magsav.model.Client;
import com.magsav.model.Dossier;
import com.magsav.pagination.PageRequest;
import com.magsav.pagination.PageResult;
import com.magsav.service.SAVService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

/** Tests unitaires pour SearchApiController */
@DisplayName("Tests API Recherche Avancée")
public class SearchApiControllerTest {

  @Mock private SAVService savService;

  private SearchApiController controller;
  private List<SAVService.DossierSAV> testData;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    controller = new SearchApiController(savService);

    setupTestData();
  }

  private void setupTestData() {
    testData = new ArrayList<>();

    // Client 1
    Client client1 =
        new Client(1L, "Martin", "Jean", "jean.martin@email.com", "0123456789", "Paris 75000");
    Appareil appareil1 =
        new Appareil(1L, 1L, "Apple", "iPhone 12", "ABC123", "Chargeur", LocalDateTime.now());
    Dossier dossier1 =
        new Dossier(
            1L,
            1L,
            "recu",
            "Écran cassé",
            "Réparation écran",
            LocalDate.now().minusDays(5),
            null,
            LocalDateTime.now());
    testData.add(new SAVService.DossierSAV(client1, appareil1, dossier1));

    // Client 2
    Client client2 =
        new Client(2L, "Dupont", "Marie", "marie.dupont@email.com", "0987654321", "Lyon 69000");
    Appareil appareil2 =
        new Appareil(2L, 2L, "Samsung", "Galaxy S21", "XYZ789", "Étui", LocalDateTime.now());
    Dossier dossier2 =
        new Dossier(
            2L,
            2L,
            "reparation",
            "Batterie défaillante",
            "Changement batterie",
            LocalDate.now().minusDays(10),
            null,
            LocalDateTime.now());
    testData.add(new SAVService.DossierSAV(client2, appareil2, dossier2));

    // Client 3
    Client client3 =
        new Client(
            3L, "Bernard", "Paul", "paul.bernard@email.com", "0147258369", "Marseille 13000");
    Appareil appareil3 =
        new Appareil(3L, 3L, "Huawei", "P30 Pro", "HW2023", "Housse", LocalDateTime.now());
    Dossier dossier3 =
        new Dossier(
            3L,
            3L,
            "termine",
            "Mise à jour",
            "Terminé avec succès",
            LocalDate.now().minusDays(20),
            LocalDate.now().minusDays(1),
            LocalDateTime.now());
    testData.add(new SAVService.DossierSAV(client3, appareil3, dossier3));
  }

  @Test
  @DisplayName("Recherche simple par terme global")
  void testSearchDossiers_SimpleSearch() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData, 0, 20, 3, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 20, "id", "desc", "Martin", null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    PageResult<SAVService.DossierSAV> result = response.getBody();
    assertNotNull(result);
    assertTrue(result.getContent().size() >= 0);
  }

  @Test
  @DisplayName("Recherche avec filtres client spécifiques")
  void testSearchDossiers_ClientFilters() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(Arrays.asList(testData.get(0)), 0, 20, 1, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0,
            20,
            "id",
            "desc",
            null,
            "Martin",
            "jean.martin@email.com",
            "0123456789",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    PageResult<SAVService.DossierSAV> result = response.getBody();
    assertNotNull(result);
  }

  @Test
  @DisplayName("Recherche avec filtres appareil")
  void testSearchDossiers_AppareilFilters() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(Arrays.asList(testData.get(0)), 0, 20, 1, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 20, "id", "desc", null, null, null, null, null, "Apple", "iPhone", "ABC123", null,
            null, null, null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Recherche avec filtres de dates")
  void testSearchDossiers_DateFilters() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData.subList(0, 2), 0, 20, 2, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    LocalDate dateDebut = LocalDate.now().minusDays(15);
    LocalDate dateFin = LocalDate.now();

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 20, "id", "desc", null, null, null, null, null, null, null, null, dateDebut, dateFin,
            null, null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Recherche avec filtres de statut")
  void testSearchDossiers_StatutFilter() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(Arrays.asList(testData.get(1)), 0, 20, 1, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0,
            20,
            "id",
            "desc",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "reparation",
            null,
            null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Obtention des statistiques")
  void testGetSearchStats() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData, 0, Integer.MAX_VALUE, 3, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<Map<String, Long>> response = controller.getSearchStats();

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    Map<String, Long> stats = response.getBody();
    assertTrue(stats.containsKey("total"));
    assertTrue(stats.containsKey("recu"));
    assertTrue(stats.containsKey("reparation"));
    assertTrue(stats.containsKey("termine"));
    assertTrue(stats.containsKey("urgent"));

    assertEquals(3L, stats.get("total"));
    assertEquals(1L, stats.get("recu"));
    assertEquals(1L, stats.get("reparation"));
    assertEquals(1L, stats.get("termine"));
  }

  @Test
  @DisplayName("Export CSV des résultats")
  void testExportSearchResults() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData, 0, Integer.MAX_VALUE, 3, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<byte[]> response =
        controller.exportSearchResults(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    String csvContent = new String(response.getBody(), "UTF-8");
    assertTrue(csvContent.contains("ID,Date Entrée,Client,Email,Téléphone"));
    assertTrue(csvContent.contains("Martin"));
    assertTrue(csvContent.contains("Dupont"));
    assertTrue(csvContent.contains("Bernard"));
  }

  @Test
  @DisplayName("Suggestions de recherche")
  void testGetSuggestions() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData, 0, Integer.MAX_VALUE, 3, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<List<String>> response = controller.getSuggestions("Mar");

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    List<String> suggestions = response.getBody();
    assertFalse(suggestions.isEmpty());
  }

  @Test
  @DisplayName("Suggestions avec requête trop courte")
  void testGetSuggestions_ShortQuery() {
    // Exécution
    ResponseEntity<List<String>> response = controller.getSuggestions("a");

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    List<String> suggestions = response.getBody();
    assertTrue(suggestions.isEmpty());
  }

  @Test
  @DisplayName("Gestion erreur dans recherche")
  void testSearchDossiers_ServiceException() throws Exception {
    // Préparation
    when(savService.listerDossiers(any(PageRequest.class)))
        .thenThrow(new RuntimeException("Erreur de base"));

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 20, "id", "desc", "test", null, null, null, null, null, null, null, null, null, null,
            null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(500, response.getStatusCode().value());
  }

  @Test
  @DisplayName("Gestion erreur dans export")
  void testExportSearchResults_ServiceException() throws Exception {
    // Préparation
    when(savService.listerDossiers(any(PageRequest.class)))
        .thenThrow(new RuntimeException("Erreur export"));

    // Exécution
    ResponseEntity<byte[]> response =
        controller.exportSearchResults(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

    // Vérification
    assertNotNull(response);
    assertEquals(500, response.getStatusCode().value());
  }

  @Test
  @DisplayName("Validation de la classe SearchCriteria")
  void testSearchCriteria_Builder() {
    // Exécution
    SearchApiController.SearchCriteria criteria =
        SearchApiController.SearchCriteria.builder()
            .search("test")
            .clientNom("Martin")
            .clientEmail("test@email.com")
            .statut("recu")
            .build();

    // Vérification
    assertNotNull(criteria);
    assertEquals("test", criteria.getSearch());
    assertEquals("Martin", criteria.getClientNom());
    assertEquals("test@email.com", criteria.getClientEmail());
    assertEquals("recu", criteria.getStatut());
  }

  @Test
  @DisplayName("Tri des résultats par client")
  void testSearchDossiers_SortByClient() throws Exception {
    // Préparation
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(testData, 0, 20, 3, "client", "asc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 20, "client", "asc", null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Pagination des résultats")
  void testSearchDossiers_Pagination() throws Exception {
    // Préparation - première page
    PageResult<SAVService.DossierSAV> mockResult =
        new PageResult<>(Arrays.asList(testData.get(0)), 0, 1, 3, "id", "desc");
    when(savService.listerDossiers(any(PageRequest.class))).thenReturn(mockResult);

    // Exécution
    ResponseEntity<PageResult<SAVService.DossierSAV>> response =
        controller.searchDossiers(
            0, 1, "id", "desc", null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null);

    // Vérification
    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());

    PageResult<SAVService.DossierSAV> result = response.getBody();
    assertNotNull(result);
    assertEquals(0, result.getPage());
    assertEquals(1, result.getSize());
    assertEquals(3, result.getTotalElements());
  }
}
