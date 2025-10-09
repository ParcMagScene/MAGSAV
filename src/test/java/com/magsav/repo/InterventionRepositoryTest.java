package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.InterventionRow;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterventionRepositoryTest {
  static Connection keeper;
  ProductRepository prodRepo;
  InterventionRepository interRepo;

  @BeforeAll
  static void keepMemoryDb() throws Exception {
    System.setProperty("magsav.db.url", "jdbc:sqlite:file:inter_repo_test?mode=memory&cache=shared");
    keeper = DriverManager.getConnection(System.getProperty("magsav.db.url"));
    DB.resetForTesting();
    DB.init();
  }

  @AfterAll
  static void closeKeeper() throws Exception {
    if (keeper != null) keeper.close();
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