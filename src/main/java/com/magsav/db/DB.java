package com.magsav.db;

import com.magsav.exception.DatabaseException;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public final class DB {
  private static String URL;

  static { try { Class.forName("org.sqlite.JDBC"); } catch (Throwable ignore) {} }

  public static synchronized void init() {
    // Support des tests avec base en mémoire - vérifier la propriété à chaque fois
    String dbUrl = System.getProperty("magsav.db.url");
    if (dbUrl != null && !dbUrl.isEmpty()) {
      // Pour les tests, toujours utiliser l'URL spécifiée dans les propriétés système
      URL = dbUrl;
    } else if (URL == null || dbUrl == null) {
      // Utiliser une base de données locale dans le dossier du projet
      Path projectDir = Paths.get("").toAbsolutePath(); // Dossier courant du projet
      Path dataDir = projectDir.resolve("data");
      Path db = dataDir.resolve("MAGSAV.db");
      try { Files.createDirectories(dataDir); } catch (Exception ignore) {}
      URL = "jdbc:sqlite:" + db.toAbsolutePath();
    }
    ensureSchema();
  }

  public static synchronized void initForProduction() {
    // Force l'utilisation de la base de données de production
    System.clearProperty("magsav.db.url");
    URL = null;
    init();
  }

  public static synchronized void resetForTesting() {
    URL = null;
  }

  public static String getCurrentUrl() {
    return URL;
  }

  public static void diagnose() {
    System.out.println("=== DIAGNOSTIC BASE DE DONNÉES ===");
    System.out.println("URL actuelle: " + URL);
    System.out.println("Propriété système: " + System.getProperty("magsav.db.url"));
    try (Connection c = getConnection()) {
      System.out.println("Connexion: OK");
      try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produits")) {
        if (rs.next()) {
          System.out.println("Produits dans la DB: " + rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      System.out.println("Erreur connexion: " + e.getMessage());
    }
    System.out.println("=====================================");
  }

  public static Connection getConnection() throws SQLException {
    if (URL == null) init();
    return DriverManager.getConnection(URL);
  }

  public static Path dataDir() {
    // Utiliser un dossier medias local dans le projet
    Path projectDir = Paths.get("").toAbsolutePath();
    Path p = projectDir.resolve("medias");
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
          photo TEXT, category TEXT, subcategory TEXT,
          description TEXT,
          date_achat TEXT,
          client TEXT,
          prix TEXT,
          garantie TEXT,
          sav_externe_id INTEGER
        )
      """);
      
      // Migration: Ajouter la colonne sav_externe_id si elle n'existe pas
      try {
        st.execute("ALTER TABLE produits ADD COLUMN sav_externe_id INTEGER");
      } catch (SQLException e) {
        // Column already exists, ignore
      }
      
      // Migration: Ajouter la colonne detecteur si elle n'existe pas
      try {
        st.execute("ALTER TABLE interventions ADD COLUMN detecteur TEXT");
      } catch (SQLException e) {
        // Column already exists, ignore
      }
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
          serial_number TEXT,
          client_note TEXT,
          defect_description TEXT,
          detector_societe_id INTEGER,
          detecteur TEXT,
          date_entree TEXT, date_sortie TEXT,
          owner_type TEXT, owner_societe_id INTEGER
        )
      """);
      st.execute("""
        CREATE TABLE IF NOT EXISTS categories(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          nom TEXT,
          parent_id INTEGER
        )
      """);
      
      st.execute("""
        CREATE TABLE IF NOT EXISTS sav_history(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          product_id INTEGER,
          sav_externe_id INTEGER,
          date_debut TEXT,
          date_fin TEXT,
          statut TEXT,
          notes TEXT,
          created_at TEXT DEFAULT (datetime('now'))
        )
      """);
      
      // Index pour optimiser les requêtes fréquentes
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_nom ON produits(UPPER(nom))");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_uid ON produits(uid)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_fabricant ON produits(fabricant)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_category ON produits(category)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_intervention_product ON interventions(product_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_intervention_date_entree ON interventions(date_entree)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_societes_type ON societes(type)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_sav_externe ON produits(sav_externe_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sav_history_product ON sav_history(product_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sav_history_sav_externe ON sav_history(sav_externe_id)");
    } catch (SQLException e) { throw new DatabaseException("DB ensureSchema failed", e); }
  }

  private DB() {}
}