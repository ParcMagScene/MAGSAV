package com.magsav.db;

import com.magsav.util.AppLogger;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public final class DB {
  
  /**
   * Initialisation H2 Database
   */
  public static synchronized void init() {
    AppLogger.info("🔄 Initialisation H2 Database...");
    H2DB.init();
    AppLogger.info("✅ H2 Database initialisé avec succès");
  }

  public static synchronized void initForProduction() {
    // Force l'utilisation de H2 en production
    AppLogger.info("🚀 Initialisation H2 pour production");
    H2DB.init();
  }

  public static synchronized void resetForTesting() {
    // Réinitialise H2 pour les tests
    AppLogger.info("🧪 Réinitialisation H2 pour tests");
    H2DB.shutdown();
    H2DB.init();
  }

  public static String getCurrentUrl() {
    return H2DB.getDatabaseInfo();
  }

  public static void diagnose() {
    System.out.println("=== DIAGNOSTIC BASE DE DONNÉES H2 ===");
    System.out.println("Info: " + H2DB.getDatabaseInfo());
    try (Connection c = getConnection()) {
      System.out.println("Connexion H2: OK");
      try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produits")) {
        if (rs.next()) {
          System.out.println("Produits dans la DB: " + rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      System.out.println("Erreur connexion H2: " + e.getMessage());
    }
    System.out.println("=====================================");
  }

  public static Connection getConnection() throws SQLException {
    return H2DB.getConnection();
  }
  
  public static void shutdown() {
    H2DB.shutdown();
  }
  
  public static ConnectionPool.Stats getConnectionStats() {
    // H2 gère ses propres connexions avec HikariCP
    return new ConnectionPool.Stats(0, 0, 0);
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




  private DB() {}
}