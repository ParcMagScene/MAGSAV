package com.magsav.db.migration;

import java.sql.*;
import java.util.List;

public class MigrationRunner {
  public static void migrate(Connection c) throws SQLException {
    ensureVersionTable(c);
    int current = currentVersion(c);
    for (Migration m : migrations()) {
      if (m.version() > current) {
        c.setAutoCommit(false);
        try {
          m.up(c);
          setVersion(c, m.version());
          c.commit();
          System.out.println("[MAGSAV] Migration " + m.version() + " - " + m.description());
        } catch (SQLException e) {
          c.rollback();
          throw e;
        } finally {
          c.setAutoCommit(true);
        }
      }
    }
  }

  private static List<Migration> migrations() {
    return List.of(
      new Migration() {
        public int version() { return 1; }
        public String description() { return "Baseline + colonnes canoniques"; }
        public void up(Connection c) throws SQLException {
          try (Statement st = c.createStatement()) {
            st.execute("""
              CREATE TABLE IF NOT EXISTS produits(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT, nom TEXT, sn TEXT,
                fabricant TEXT, uid TEXT, situation TEXT
              )
            """);
            st.execute("CREATE TABLE IF NOT EXISTS interventions(id INTEGER PRIMARY KEY AUTOINCREMENT)");
          }
          ensureCol(c, "interventions", "statut", "TEXT");
          ensureCol(c, "interventions", "panne", "TEXT");
          ensureCol(c, "interventions", "date_entree", "TEXT");
          ensureCol(c, "interventions", "date_sortie", "TEXT");
          ensureCol(c, "interventions", "product_id", "INTEGER");
          ensureCol(c, "interventions", "sn", "TEXT");
          ensureCol(c, "interventions", "produit", "TEXT");
          ensureCol(c, "interventions", "serial", "TEXT");
          ensureCol(c, "interventions", "suivi_no", "TEXT");
          ensureCol(c, "interventions", "owner_type", "TEXT");
          ensureCol(c, "interventions", "owner_societe_id", "INTEGER");
          ensureCol(c, "interventions", "detector_societe_id", "INTEGER");
        }
      },
      new Migration() {
        public int version() { return 2; }
        public String description() { return "Backfill product_id via SN/produit"; }
        public void up(Connection c) throws SQLException {
          try (Statement st = c.createStatement()) {
            st.execute("""
              UPDATE interventions
                 SET product_id = (
                   SELECT p.id FROM produits p
                    WHERE p.sn IS NOT NULL AND p.sn <> '' AND p.sn = interventions.sn
                    ORDER BY p.id LIMIT 1
                 )
               WHERE (product_id IS NULL OR product_id = 0)
                 AND sn IS NOT NULL AND sn <> ''
                 AND EXISTS (SELECT 1 FROM produits p WHERE p.sn = interventions.sn)
            """);
            st.execute("""
              UPDATE interventions
                 SET product_id = (
                   SELECT p.id FROM produits p
                    WHERE p.nom IS NOT NULL AND TRIM(p.nom) <> '' AND p.nom = interventions.produit
                    ORDER BY p.id LIMIT 1
                 )
               WHERE (product_id IS NULL OR product_id = 0)
                 AND produit IS NOT NULL AND TRIM(produit) <> ''
                 AND EXISTS (SELECT 1 FROM produits p WHERE p.nom = interventions.produit)
            """);
          }
        }
      },
      new Migration() {
        public int version() { return 3; }
        public String description() { return "Index + vue UI"; }
        public void up(Connection c) throws SQLException {
          try (Statement st = c.createStatement()) {
            st.execute("CREATE INDEX IF NOT EXISTS ix_prod_sn ON produits(sn)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ix_prod_uid ON produits(uid)");
            st.execute("CREATE INDEX IF NOT EXISTS ix_inter_product ON interventions(product_id)");
            st.execute("CREATE INDEX IF NOT EXISTS ix_inter_sortie ON interventions(date_sortie)");
            st.execute("""
              CREATE VIEW IF NOT EXISTS v_interventions_ui AS
              SELECT i.id,
                     COALESCE(p.nom, i.produit, '') AS produit_nom,
                     i.statut,
                     COALESCE(i.panne,'') AS panne,
                     COALESCE(strftime('%Y-%m-%d', i.date_entree), '') AS date_entree,
                     COALESCE(strftime('%Y-%m-%d', i.date_sortie), '') AS date_sortie,
                     i.product_id
              FROM interventions i
              LEFT JOIN produits p ON p.id = i.product_id
            """);
          }
        }
      },
      new Migration() {
        public int version() { return 4; }
        public String description() { return "Suppression champ code inutile"; }
        public void up(Connection c) throws SQLException {
          try (Statement st = c.createStatement()) {
            // Créer une nouvelle table sans le champ code
            st.execute("""
              CREATE TABLE produits_new(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT, sn TEXT, fabricant TEXT, fabricant_id INTEGER,
                uid TEXT, situation TEXT, photo TEXT,
                category TEXT, subcategory TEXT, description TEXT,
                date_achat TEXT, client TEXT, prix TEXT, garantie TEXT,
                sav_externe_id INTEGER, categorieId INTEGER, sousCategorieId INTEGER
              )
            """);
            
            // Copier les données (sans le champ code)
            st.execute("""
              INSERT INTO produits_new 
              SELECT id, nom, sn, fabricant, fabricant_id, uid, situation, photo,
                     category, subcategory, description, date_achat, client, prix, garantie,
                     sav_externe_id, categorieId, sousCategorieId
              FROM produits
            """);
            
            // Supprimer l'ancienne table et renommer
            st.execute("DROP TABLE produits");
            st.execute("ALTER TABLE produits_new RENAME TO produits");
            
            // Recréer les index
            st.execute("CREATE INDEX IF NOT EXISTS ix_prod_sn ON produits(sn)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ix_prod_uid ON produits(uid)");
          }
        }
      }
    );
  }

  private static void ensureVersionTable(Connection c) throws SQLException {
    try (Statement st = c.createStatement()) {
      st.execute("CREATE TABLE IF NOT EXISTS schema_version(version INTEGER NOT NULL)");
      try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM schema_version")) {
        if (rs.next() && rs.getInt(1) == 0) st.execute("INSERT INTO schema_version(version) VALUES (0)");
      }
    }
  }

  private static int currentVersion(Connection c) throws SQLException {
    try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT version FROM schema_version")) {
      return rs.next() ? rs.getInt(1) : 0;
    }
  }

  private static void setVersion(Connection c, int v) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("UPDATE schema_version SET version=?")) {
      ps.setInt(1, v);
      ps.executeUpdate();
    }
  }

  private static boolean hasColumn(Connection c, String table, String col) throws SQLException {
    try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
      while (rs.next()) if (col.equalsIgnoreCase(rs.getString("name"))) return true;
      return false;
    }
  }

  private static void ensureCol(Connection c, String table, String col, String type) throws SQLException {
    if (hasColumn(c, table, col)) return;
    try (Statement st = c.createStatement()) {
      st.execute("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type);
    }
  }
}