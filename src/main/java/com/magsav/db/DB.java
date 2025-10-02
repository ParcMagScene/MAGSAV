package com.magsav.db;

import java.nio.file.*;
import java.sql.*;

public final class DB {
  private static Path dbPath() {
    return Paths.get(System.getProperty("user.home"), "MAGSAV", "MAGSAV.db");
  }

  private static String dbUrl() {
    String override = System.getProperty("magsav.db.url");
    if (override != null && !override.isBlank()) return override;
    return "jdbc:sqlite:" + dbPath();
  }

  public static void init() {
    try {
      Path dir = dbPath().getParent();
      if (dir != null) Files.createDirectories(dir);
      try (Connection c = getConnection(); Statement st = c.createStatement()) {
        st.execute("PRAGMA foreign_keys = ON");
        st.execute("""
          CREATE TABLE IF NOT EXISTS categories(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nom TEXT NOT NULL,
            parent_id INTEGER REFERENCES categories(id) ON DELETE SET NULL
          );
        """);
        st.execute("""
          CREATE TABLE IF NOT EXISTS produits(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            code TEXT,
            nom TEXT NOT NULL,
            sn TEXT,
            fabricant TEXT,
            categorie_id INTEGER REFERENCES categories(id),
            sous_categorie_id INTEGER REFERENCES categories(id),
            created_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%S','now'))
          );
        """);
        st.execute("""
          CREATE TABLE IF NOT EXISTS interventions(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            produit_id INTEGER NOT NULL REFERENCES produits(id) ON DELETE CASCADE,
            statut TEXT,
            panne TEXT,
            date_entree TEXT,
            date_sortie TEXT
          );
        """);
        // Index
        st.execute("CREATE INDEX IF NOT EXISTS idx_produits_nom ON produits(nom)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_produits_sn ON produits(sn)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_interventions_produit_id ON interventions(produit_id)");
      }
    } catch (Exception e) {
      throw new RuntimeException("Init DB failed", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(dbUrl());
  }

  private DB() {}
}