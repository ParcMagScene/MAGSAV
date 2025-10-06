package com.magsav.db;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public final class DB {
  private static String URL;

  static { try { Class.forName("org.sqlite.JDBC"); } catch (Throwable ignore) {} }

  public static synchronized void init() {
    if (URL != null) return;
    Path db = Paths.get(System.getProperty("user.home"), "MAGSAV", "MAGSAV.db");
    try { Files.createDirectories(db.getParent()); } catch (Exception ignore) {}
    URL = "jdbc:sqlite:" + db.toAbsolutePath();
    ensureSchema();
  }

  public static Connection getConnection() throws SQLException {
    if (URL == null) init();
    return DriverManager.getConnection(URL);
  }

  public static Path dataDir() {
    Path p = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias");
    try {
      Files.createDirectories(p);
    } catch (IOException e) {
      throw new RuntimeException("Impossible de créer le dossier de données", e);
    }
    return p;
  }

  private static void ensureSchema() {
    try (Connection c = getConnection(); Statement st = c.createStatement()) {
      st.execute("""
        CREATE TABLE IF NOT EXISTS produits(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          code TEXT, nom TEXT, sn TEXT,
          fabricant TEXT, fabricant_id INTEGER,
          uid TEXT, situation TEXT,
          photo TEXT, category TEXT, subcategory TEXT
        )
      """);
      st.execute("""
        CREATE TABLE IF NOT EXISTS societes(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          type TEXT, nom TEXT,
          email TEXT, phone TEXT,
          adresse TEXT, notes TEXT,
          created_at TEXT DEFAULT (datetime('now'))
        )
      """);
      st.execute("""
        CREATE TABLE IF NOT EXISTS interventions(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          product_id INTEGER,
          statut TEXT, panne TEXT,
          serial TEXT,
          detector_societe_id INTEGER,
          date_entree TEXT, date_sortie TEXT,
          owner_type TEXT, owner_societe_id INTEGER
        )
      """);
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_nom ON produits(UPPER(nom))");
    } catch (SQLException e) { throw new RuntimeException("DB ensureSchema failed", e); }
  }

  private DB() {}
}