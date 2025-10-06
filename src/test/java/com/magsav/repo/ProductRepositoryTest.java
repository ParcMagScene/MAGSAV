package com.magsav.repo;

import com.magsav.db.DB;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {
  static Connection keeper;
  ProductRepository repo;

  @BeforeAll
  static void keepMemoryDb() throws Exception {
    System.setProperty("magsav.db.url", "jdbc:sqlite:file:prod_repo_test?mode=memory&cache=shared");
    keeper = DriverManager.getConnection(System.getProperty("magsav.db.url"));
    DB.init();
  }

  @AfterAll
  static void closeKeeper() throws Exception {
    if (keeper != null) keeper.close();
  }

  @BeforeEach
  void setUp() {
    repo = new ProductRepository();
  }

  @Test
  void insert_find_update_existsUid() {
    long id = repo.insert("C1", "P1", "SN1", "FAB", null, "En stock");
    assertTrue(id > 0);

    var p = repo.findById(id).orElseThrow();
    assertEquals("P1", p.nom());

    // Note: updateName method doesn't exist - testing other update methods instead

    repo.updateSituation(id, "En service");
    assertEquals("En service", repo.findById(id).orElseThrow().situation());

    repo.updateUid(id, "UID123");
    assertTrue(repo.existsUid("UID123"));
    assertFalse(repo.existsUid("UID999"));
  }
}