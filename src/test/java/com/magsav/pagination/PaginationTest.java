package com.magsav.pagination;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests pour les classes de pagination */
public class PaginationTest {

  @Test
  public void testPageRequest_ConstructeurSimple() {
    PageRequest request = new PageRequest(0, 10);

    assertEquals(0, request.getPage());
    assertEquals(10, request.getSize());
    assertEquals("id", request.getSortBy());
    assertEquals("asc", request.getSortDirection());
    assertEquals(0, request.getOffset());
    assertEquals("id ASC", request.getSortClause());
  }

  @Test
  public void testPageRequest_ConstructeurComplet() {
    PageRequest request = new PageRequest(2, 25, "nom", "desc");

    assertEquals(2, request.getPage());
    assertEquals(25, request.getSize());
    assertEquals("nom", request.getSortBy());
    assertEquals("desc", request.getSortDirection());
    assertEquals(50, request.getOffset()); // page 2 * size 25 = 50
    assertEquals("nom DESC", request.getSortClause());
  }

  @Test
  public void testPageRequest_ValidationLimites() {
    // Page négative -> 0
    PageRequest request1 = new PageRequest(-1, 10);
    assertEquals(0, request1.getPage());

    // Taille négative -> 1
    PageRequest request2 = new PageRequest(0, -5);
    assertEquals(1, request2.getSize());

    // Taille trop grande -> limitée à 100
    PageRequest request3 = new PageRequest(0, 500);
    assertEquals(100, request3.getSize());

    // Direction invalide -> asc par défaut
    PageRequest request4 = new PageRequest(0, 10, "id", "invalid");
    assertEquals("asc", request4.getSortDirection());
  }

  @Test
  public void testPageResult_ConstructionEtCalculs() {
    List<String> content = List.of("item1", "item2", "item3");
    PageResult<String> result = new PageResult<>(content, 1, 10, 25L, "nom", "asc");

    assertEquals(content, result.getContent());
    assertEquals(1, result.getPage());
    assertEquals(10, result.getSize());
    assertEquals(25L, result.getTotalElements());
    assertEquals(3, result.getTotalPages()); // ceil(25/10) = 3
    assertTrue(result.hasNext()); // page 1 < totalPages-1 (2)
    assertTrue(result.hasPrevious()); // page 1 > 0
    assertFalse(result.isFirst()); // page 1 != 0
    assertFalse(result.isLast()); // page 1 < totalPages-1
    assertEquals(3, result.getNumberOfElements());
    assertFalse(result.isEmpty());
    assertEquals(2, result.getNextPage());
    assertEquals(0, result.getPreviousPage());
  }

  @Test
  public void testPageResult_PremierePage() {
    List<String> content = List.of("item1", "item2");
    PageResult<String> result = new PageResult<>(content, 0, 10, 25L, "id", "desc");

    assertTrue(result.hasNext()); // Avec 25 éléments et page 0, il y a une page suivante
    assertFalse(result.hasPrevious());
    assertTrue(result.isFirst());
    assertFalse(result.isLast());
  }

  @Test
  public void testPageResult_DernierePage() {
    List<String> content = List.of("item1");
    PageResult<String> result = new PageResult<>(content, 2, 10, 21L, "id", "asc");
    // 21 éléments avec page size 10 = 3 pages (0, 1, 2)
    // Page 2 est la dernière page

    assertFalse(result.hasNext());
    assertTrue(result.hasPrevious());
    assertFalse(result.isFirst());
    assertTrue(result.isLast());
  }

  @Test
  public void testPageResult_PageVide() {
    List<String> content = new ArrayList<>();
    PageResult<String> result = new PageResult<>(content, 0, 10, 0L, "id", "asc");

    assertTrue(result.isEmpty());
    assertEquals(0, result.getNumberOfElements());
    assertEquals(0, result.getTotalPages());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
    assertTrue(result.isFirst());
    assertTrue(result.isLast());
  }

  @Test
  public void testPageResult_TailleExacte() {
    List<String> content = List.of("item1", "item2", "item3", "item4", "item5");
    PageResult<String> result = new PageResult<>(content, 0, 5, 10L, "id", "asc");

    assertEquals(2, result.getTotalPages()); // 10 éléments / 5 par page = 2 pages exactes
    assertTrue(result.hasNext());
    assertFalse(result.hasPrevious());
    assertTrue(result.isFirst());
    assertFalse(result.isLast());
  }

  @Test
  public void testPageResult_UnSeulElement() {
    List<String> content = List.of("seul");
    PageResult<String> result = new PageResult<>(content, 0, 10, 1L, "id", "asc");

    assertEquals(1, result.getTotalPages());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
    assertTrue(result.isFirst());
    assertTrue(result.isLast());
    assertEquals(1, result.getNumberOfElements());
  }
}
