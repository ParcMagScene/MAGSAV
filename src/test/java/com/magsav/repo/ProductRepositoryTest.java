package com.magsav.repo;

import com.magsav.util.TestDatabaseConfig;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {
  static Connection keeper;
  ProductRepository repo;

  @BeforeAll
  static void keepMemoryDb() throws Exception {
    keeper = TestDatabaseConfig.setupSharedInMemoryDb("ProductRepositoryTest");
  }

  @AfterAll
  static void closeKeeper() throws Exception {
    TestDatabaseConfig.cleanupKeeper(keeper);
  }

  @BeforeEach
  void setUp() {
    repo = new ProductRepository();
  }

  @Test
  void insert_find_update_existsUid() {
    long id = repo.insert("P1", "SN1", "FAB", null, "En stock");
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