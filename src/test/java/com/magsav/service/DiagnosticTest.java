package com.magsav.service;

import static org.junit.jupiter.api.Assertions.*;

import com.magsav.db.DB;
import com.magsav.repo.ClientRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

/** Test de diagnostic pour comprendre les problèmes SQLite */
public class DiagnosticTest {

  private DataSource ds;
  private ClientRepository clientRepo;

  @BeforeEach
  void setUp() throws Exception {
    // Utiliser un fichier temporaire unique pour chaque test
    String dbName = "magsav-test-" + System.currentTimeMillis() + ".db";
    ds = DB.init("jdbc:sqlite:/tmp/" + dbName);
    DB.migrate(ds);
    clientRepo = new ClientRepository(ds);
  }

  @Test
  void testConnexionBasique() throws Exception {
    try (Connection conn = ds.getConnection()) {
      var rs = conn.createStatement().executeQuery("SELECT 1");
      assertTrue(rs.next());
      assertEquals(1, rs.getInt(1));
    }
  }

  @Test
  void testInsertionSQL() throws Exception {
    try (Connection conn = ds.getConnection()) {
      PreparedStatement ps =
          conn.prepareStatement(
              "INSERT INTO clients(nom, prenom, email, tel, adresse) VALUES(?,?,?,?,?)");
      ps.setString(1, "TestNom");
      ps.setString(2, "TestPrenom");
      ps.setString(3, "test@example.com");
      ps.setString(4, "0123456789");
      ps.setString(5, "TestAdresse");

      int result = ps.executeUpdate();
      assertEquals(1, result);
    }
  }

  @Test
  void testMemeConnexion() throws Exception {
    try (Connection conn = ds.getConnection()) {
      // Vérifier que la table existe
      var rs =
          conn.createStatement()
              .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='clients'");
      assertTrue(rs.next(), "La table clients devrait exister");

      // Insérer directement
      PreparedStatement ps =
          conn.prepareStatement(
              "INSERT INTO clients(nom, prenom, email, tel, adresse) VALUES(?,?,?,?,?)");
      ps.setString(1, "TestNom");
      ps.setString(2, "TestPrenom");
      ps.setString(3, "test@direct.com");
      ps.setString(4, "0123456789");
      ps.setString(5, "TestAdresse");

      int result = ps.executeUpdate();
      assertEquals(1, result);

      // Vérifier la lecture avec la même connexion
      PreparedStatement psSelect =
          conn.prepareStatement(
              "SELECT id, nom, prenom, email, tel, adresse FROM clients WHERE email=?");
      psSelect.setString(1, "test@direct.com");
      var rsSelect = psSelect.executeQuery();
      assertTrue(rsSelect.next());
      assertEquals("TestNom", rsSelect.getString(2));
    }
  }

  @Test
  void testTableExiste() throws Exception {
    try (Connection conn = ds.getConnection()) {
      var rs =
          conn.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
      boolean clientsTableExists = false;
      while (rs.next()) {
        if ("clients".equals(rs.getString(1))) {
          clientsTableExists = true;
          break;
        }
      }
      assertTrue(clientsTableExists, "La table clients devrait exister");
    }
  }
}
