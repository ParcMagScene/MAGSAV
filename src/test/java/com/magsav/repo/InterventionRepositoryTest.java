package com.magsav.repo;

import com.magsav.util.TestDatabaseConfig;
import com.magsav.model.InterventionRow;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterventionRepositoryTest {
  static Connection keeper;
  ProductRepository prodRepo;
  InterventionRepository interRepo;

  @BeforeAll
  static void keepMemoryDb() throws Exception {
    keeper = TestDatabaseConfig.setupSharedInMemoryDb("InterventionRepositoryTest");
  }

  @AfterAll
  static void closeKeeper() throws Exception {
    TestDatabaseConfig.cleanupKeeper(keeper);
  }

  @BeforeEach
  void setUp() {
    prodRepo = new ProductRepository();
    interRepo = new InterventionRepository();
  }

  @Test
  void insert_find_close() {
    long pid = prodRepo.insert("PROD-X", "SNX", "FAB2", null, "En stock");
    assertTrue(pid > 0);

    long iid = interRepo.insert(pid, "SERIAL-1", null, "Panne X");
    assertTrue(iid > 0);

    List<InterventionRow> hist = interRepo.findByProductId(pid);
    assertFalse(hist.isEmpty());
    assertEquals("PROD-X", hist.get(0).produitNom());

    assertTrue(interRepo.close(iid));
    List<InterventionRow> all = interRepo.findAllWithProductName();
    assertTrue(all.stream().anyMatch(r -> r.id() == iid));
  }
}