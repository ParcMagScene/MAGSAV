package com.magsav.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import javax.sql.DataSource;

public final class DB implements AutoCloseable {
  private final HikariDataSource ds;

  private DB(HikariDataSource ds) {
    this.ds = ds;
  }

  public static HikariDataSource init(String jdbcUrl) {
    HikariConfig cfg = new HikariConfig();
    cfg.setJdbcUrl(jdbcUrl);
    cfg.setMaximumPoolSize(4);
    cfg.setPoolName("magsav-pool");
    return new HikariDataSource(cfg);
  }

  public static void migrate(DataSource ds) throws Exception {
    try (Connection c = ds.getConnection()) {
      c.createStatement().execute("PRAGMA foreign_keys = ON");
      String sql = readResource("schema.sql");
      try (var st = c.createStatement()) {
        for (String chunk : sql.split(";\\s*\n")) {
          // Retirer les lignes de commentaires et espaces
          StringBuilder b = new StringBuilder();
          for (String line : chunk.split("\n")) {
            String l = line.trim();
            if (l.startsWith("--") || l.isEmpty()) {
              continue;
            }
            b.append(l).append('\n');
          }
          String cleaned = b.toString().trim();
          if (cleaned.isEmpty()) {
            continue;
          }
          try {
            st.execute(cleaned);
          } catch (Exception ex) {
            throw new RuntimeException("Erreur SQL pendant migration sur: " + cleaned, ex);
          }
        }
      }

      // Migration conditionnelle: ajouter colonne 'code' si absente et la remplir
      try (var rs = c.createStatement().executeQuery("PRAGMA table_info(dossiers_sav)")) {
        boolean hasCode = false;
        while (rs.next()) {
          if ("code".equalsIgnoreCase(rs.getString("name"))) {
            hasCode = true;
            break;
          }
        }
        if (!hasCode) {
          c.createStatement().execute("ALTER TABLE dossiers_sav ADD COLUMN code TEXT");
          c.createStatement()
              .execute(
                  "CREATE UNIQUE INDEX IF NOT EXISTS ux_dossiers_sav_code ON dossiers_sav(code)");
          // Backfill: générer un code pour les lignes sans code
          try (var rs2 =
              c.createStatement().executeQuery("SELECT id FROM dossiers_sav WHERE code IS NULL")) {
            while (rs2.next()) {
              long id = rs2.getLong(1);
              String code;
              do {
                code = generateCode();
              } while (existsCode(c, code));
              try (var ps = c.prepareStatement("UPDATE dossiers_sav SET code=? WHERE id=?")) {
                ps.setString(1, code);
                ps.setLong(2, id);
                ps.executeUpdate();
              }
            }
          }
        }
      }

      // Ajouter colonnes category_id et subcategory_id si absentes
      boolean hasCat = false;
      boolean hasSubcat = false;
      try (var rs3 = c.createStatement().executeQuery("PRAGMA table_info(dossiers_sav)")) {
        while (rs3.next()) {
          String name = rs3.getString("name");
          if ("category_id".equalsIgnoreCase(name)) {
            hasCat = true;
          }
          if ("subcategory_id".equalsIgnoreCase(name)) {
            hasSubcat = true;
          }
        }
      }
      if (!hasCat) {
        c.createStatement().execute("ALTER TABLE dossiers_sav ADD COLUMN category_id INTEGER");
      }
      if (!hasSubcat) {
        c.createStatement().execute("ALTER TABLE dossiers_sav ADD COLUMN subcategory_id INTEGER");
      }

      // Créer systématiquement les index (idempotent) après avoir garanti l'existence des colonnes
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_dossiers_sav_category ON dossiers_sav(category_id)");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_dossiers_sav_subcategory ON dossiers_sav(subcategory_id)");

      // Créer la table categories si absente
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, parent_id INTEGER, FOREIGN KEY(parent_id) REFERENCES categories(id) ON DELETE SET NULL)");

      // Migration: rendre numero_serie nullable dans dossiers_sav si NOT NULL existait
      try (var rsTbl = c.createStatement().executeQuery("PRAGMA table_info(dossiers_sav)")) {
        boolean needRecreate = false;
        while (rsTbl.next()) {
          if ("numero_serie".equalsIgnoreCase(rsTbl.getString("name"))
              && rsTbl.getInt("notnull") == 1) {
            needRecreate = true;
            break;
          }
        }
        if (needRecreate) {
          c.createStatement().execute("ALTER TABLE dossiers_sav RENAME TO _old_dossiers_sav");
          c.createStatement()
              .execute(
                  "CREATE TABLE dossiers_sav (\n"
                      + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                      + "  code TEXT UNIQUE,\n"
                      + "  produit TEXT NOT NULL,\n"
                      + "  numero_serie TEXT,\n"
                      + "  proprietaire TEXT NOT NULL,\n"
                      + "  panne TEXT,\n"
                      + "  statut TEXT DEFAULT 'recu',\n"
                      + "  detecteur TEXT,\n"
                      + "  date_entree TEXT,\n"
                      + "  date_sortie TEXT,\n"
                      + "  category_id INTEGER,\n"
                      + "  subcategory_id INTEGER,\n"
                      + "  created_at TEXT DEFAULT (datetime('now')),\n"
                      + "  UNIQUE(numero_serie, proprietaire)\n"
                      + ")");
          c.createStatement()
              .execute(
                  "INSERT INTO dossiers_sav(id, code, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie, category_id, subcategory_id, created_at)\n"
                      + " SELECT id, code, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie, category_id, subcategory_id, created_at FROM _old_dossiers_sav");
          c.createStatement().execute("DROP TABLE _old_dossiers_sav");
        }
      }

      // Créer la table produits si absente (numero_serie désormais nullable)
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS produits (id INTEGER PRIMARY KEY AUTOINCREMENT, produit TEXT, numero_serie TEXT UNIQUE, code TEXT UNIQUE, photo_path TEXT, manufacturer_id INTEGER, created_at TEXT DEFAULT (datetime('now')))");

      // Migration: rendre numero_serie nullable dans produits si NOT NULL existait
      try (var rsTbl = c.createStatement().executeQuery("PRAGMA table_info(produits)")) {
        boolean needRecreate = false;
        while (rsTbl.next()) {
          if ("numero_serie".equalsIgnoreCase(rsTbl.getString("name"))
              && rsTbl.getInt("notnull") == 1) {
            needRecreate = true;
            break;
          }
        }
        if (needRecreate) {
          c.createStatement().execute("ALTER TABLE produits RENAME TO _old_produits");
          c.createStatement()
              .execute(
                  "CREATE TABLE produits (id INTEGER PRIMARY KEY AUTOINCREMENT, produit TEXT, numero_serie TEXT UNIQUE, code TEXT UNIQUE, photo_path TEXT, manufacturer_id INTEGER, created_at TEXT DEFAULT (datetime('now')))");
          c.createStatement()
              .execute(
                  "INSERT INTO produits(id, produit, numero_serie, code, photo_path, manufacturer_id, created_at) SELECT id, produit, numero_serie, code, photo_path, manufacturer_id, created_at FROM _old_produits");
          c.createStatement().execute("DROP TABLE _old_produits");
        }
      }

      // Migration conditionnelle: ajouter colonne photo_path si absente dans produits
      boolean hasPhoto = false;
      try (var rsP = c.createStatement().executeQuery("PRAGMA table_info(produits)")) {
        while (rsP.next()) {
          if ("photo_path".equalsIgnoreCase(rsP.getString("name"))) {
            hasPhoto = true;
            break;
          }
        }
      }
      if (!hasPhoto) {
        c.createStatement().execute("ALTER TABLE produits ADD COLUMN photo_path TEXT");
      }

      // Ajouter colonne manufacturer_id si absente
      boolean hasManu = false;
      try (var rsPm = c.createStatement().executeQuery("PRAGMA table_info(produits)")) {
        while (rsPm.next()) {
          if ("manufacturer_id".equalsIgnoreCase(rsPm.getString("name"))) {
            hasManu = true;
            break;
          }
        }
      }
      if (!hasManu) {
        c.createStatement().execute("ALTER TABLE produits ADD COLUMN manufacturer_id INTEGER");
      }

      // Créer tables manufacturers et service_providers si absentes
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS manufacturers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, website TEXT, contact_email TEXT, contact_phone TEXT, logo_path TEXT, created_at TEXT DEFAULT (datetime('now')))");
      // Migration conditionnelle: ajouter colonne logo_path si absente
      try (var rsM = c.createStatement().executeQuery("PRAGMA table_info(manufacturers)")) {
        boolean hasLogo = false;
        while (rsM.next()) {
          if ("logo_path".equalsIgnoreCase(rsM.getString("name"))) {
            hasLogo = true;
            break;
          }
        }
        if (!hasLogo) {
          c.createStatement().execute("ALTER TABLE manufacturers ADD COLUMN logo_path TEXT");
        }
      }
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS service_providers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, email TEXT, phone TEXT, address TEXT, created_at TEXT DEFAULT (datetime('now')))");

      // Module Achats: demandes de devis/prix (RFQ)
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS purchases_rfq (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  provider_id INTEGER,\n"
                  + "  produit TEXT,\n"
                  + "  part_number TEXT,\n"
                  + "  quantity INTEGER,\n"
                  + "  status TEXT,\n"
                  + "  requested_at TEXT DEFAULT (datetime('now')),\n"
                  + "  responded_at TEXT,\n"
                  + "  price REAL,\n"
                  + "  currency TEXT,\n"
                  + "  notes TEXT,\n"
                  + "  created_at TEXT DEFAULT (datetime('now')),\n"
                  + "  FOREIGN KEY(provider_id) REFERENCES service_providers(id) ON DELETE SET NULL\n"
                  + ")");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_purchases_rfq_provider ON purchases_rfq(provider_id)");

      // Module RMA: demandes de réparation/retour
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS rma_requests (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  provider_id INTEGER,\n"
                  + "  manufacturer_id INTEGER,\n"
                  + "  produit TEXT,\n"
                  + "  numero_serie TEXT,\n"
                  + "  code_produit TEXT,\n"
                  + "  reason TEXT,\n"
                  + "  status TEXT,\n"
                  + "  rma_number TEXT,\n"
                  + "  created_at TEXT DEFAULT (datetime('now')),\n"
                  + "  updated_at TEXT,\n"
                  + "  FOREIGN KEY(provider_id) REFERENCES service_providers(id) ON DELETE SET NULL,\n"
                  + "  FOREIGN KEY(manufacturer_id) REFERENCES manufacturers(id) ON DELETE SET NULL\n"
                  + ")");
      c.createStatement()
          .execute("CREATE INDEX IF NOT EXISTS idx_rma_provider ON rma_requests(provider_id)");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_rma_manufacturer ON rma_requests(manufacturer_id)");

      // Module Documents: index des documents importés/produits
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS documents (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  type TEXT NOT NULL,\n"
                  + "  original_name TEXT,\n"
                  + "  normalized_name TEXT,\n"
                  + "  path TEXT NOT NULL,\n"
                  + "  linked_product_code TEXT,\n"
                  + "  linked_numero_serie TEXT,\n"
                  + "  linked_dossier_id INTEGER,\n"
                  + "  linked_rfq_id INTEGER,\n"
                  + "  linked_rma_id INTEGER,\n"
                  + "  created_at TEXT DEFAULT (datetime('now'))\n"
                  + ")");
      c.createStatement()
          .execute("CREATE INDEX IF NOT EXISTS idx_documents_type ON documents(type)");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_documents_links ON documents(linked_product_code, linked_numero_serie, linked_dossier_id, linked_rfq_id, linked_rma_id)");

      // Sociétés, services et contacts
      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS companies (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  name TEXT NOT NULL UNIQUE,\n"
                  + "  siret TEXT,\n"
                  + "  email TEXT,\n"
                  + "  phone TEXT,\n"
                  + "  website TEXT,\n"
                  + "  address_line1 TEXT,\n"
                  + "  address_line2 TEXT,\n"
                  + "  postal_code TEXT,\n"
                  + "  city TEXT,\n"
                  + "  country TEXT,\n"
                  + "  created_at TEXT DEFAULT (datetime('now'))\n"
                  + ")");
      c.createStatement()
          .execute("CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name)");

      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS company_services (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  company_id INTEGER NOT NULL,\n"
                  + "  type TEXT NOT NULL, -- SUPPLIER | SAV | SALES | SUPPORT | OTHER\n"
                  + "  name TEXT,\n"
                  + "  email TEXT,\n"
                  + "  phone TEXT,\n"
                  + "  address_line1 TEXT,\n"
                  + "  address_line2 TEXT,\n"
                  + "  postal_code TEXT,\n"
                  + "  city TEXT,\n"
                  + "  country TEXT,\n"
                  + "  active INTEGER DEFAULT 1,\n"
                  + "  created_at TEXT DEFAULT (datetime('now')),\n"
                  + "  FOREIGN KEY(company_id) REFERENCES companies(id) ON DELETE CASCADE\n"
                  + ")");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_company_services_company ON company_services(company_id)");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_company_services_type ON company_services(type)");

      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS service_contacts (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  service_id INTEGER NOT NULL,\n"
                  + "  name TEXT NOT NULL,\n"
                  + "  email TEXT,\n"
                  + "  phone TEXT,\n"
                  + "  role TEXT,\n"
                  + "  created_at TEXT DEFAULT (datetime('now')),\n"
                  + "  FOREIGN KEY(service_id) REFERENCES company_services(id) ON DELETE CASCADE\n"
                  + ")");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_service_contacts_service ON service_contacts(service_id)");

      c.createStatement()
          .execute(
              "CREATE TABLE IF NOT EXISTS manufacturer_services (\n"
                  + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                  + "  manufacturer_id INTEGER NOT NULL,\n"
                  + "  service_id INTEGER NOT NULL,\n"
                  + "  relation_type TEXT NOT NULL, -- SUPPLIER | SAV | DISTRIBUTOR | OTHER\n"
                  + "  created_at TEXT DEFAULT (datetime('now')),\n"
                  + "  FOREIGN KEY(manufacturer_id) REFERENCES manufacturers(id) ON DELETE CASCADE,\n"
                  + "  FOREIGN KEY(service_id) REFERENCES company_services(id) ON DELETE CASCADE\n"
                  + ")");
      c.createStatement()
          .execute(
              "CREATE UNIQUE INDEX IF NOT EXISTS ux_manufacturer_services ON manufacturer_services(manufacturer_id, service_id, relation_type)");
      c.createStatement()
          .execute(
              "CREATE INDEX IF NOT EXISTS idx_manufacturer_services_service ON manufacturer_services(service_id)");

      // Ajouter provider_service_id si absent dans purchases_rfq et rma_requests
      try (var rsfs = c.createStatement().executeQuery("PRAGMA table_info(purchases_rfq)")) {
        boolean has = false;
        while (rsfs.next()) {
          if ("provider_service_id".equalsIgnoreCase(rsfs.getString("name"))) {
            has = true;
            break;
          }
        }
        if (!has) {
          c.createStatement()
              .execute("ALTER TABLE purchases_rfq ADD COLUMN provider_service_id INTEGER");
          c.createStatement()
              .execute(
                  "CREATE INDEX IF NOT EXISTS idx_purchases_rfq_provider_service ON purchases_rfq(provider_service_id)");
        }
      }
      try (var rsfs2 = c.createStatement().executeQuery("PRAGMA table_info(rma_requests)")) {
        boolean has = false;
        while (rsfs2.next()) {
          if ("provider_service_id".equalsIgnoreCase(rsfs2.getString("name"))) {
            has = true;
            break;
          }
        }
        if (!has) {
          c.createStatement()
              .execute("ALTER TABLE rma_requests ADD COLUMN provider_service_id INTEGER");
          c.createStatement()
              .execute(
                  "CREATE INDEX IF NOT EXISTS idx_rma_provider_service ON rma_requests(provider_service_id)");
        }
      }

      // Backfill produits: un enregistrement par numero_serie non vide avec un code unique au
      // niveau produit
      try (var rs =
          c.createStatement()
              .executeQuery(
                  "SELECT DISTINCT numero_serie FROM dossiers_sav WHERE numero_serie IS NOT NULL AND TRIM(numero_serie) <> ''")) {
        while (rs.next()) {
          String sn = rs.getString(1);
          // Si produit inexistant, le créer
          boolean exists;
          try (var ps = c.prepareStatement("SELECT 1 FROM produits WHERE numero_serie=? LIMIT 1")) {
            ps.setString(1, sn);
            try (var r2 = ps.executeQuery()) {
              exists = r2.next();
            }
          }
          if (!exists) {
            String code;
            do {
              code = generateCode();
            } while (productCodeExists(c, code));
            try (var ps =
                c.prepareStatement(
                    "INSERT INTO produits(produit, numero_serie, code) VALUES(?,?,?)")) {
              // Choisir un libellé produit arbitraire depuis dossiers_sav (le plus récent)
              String prod = null;
              try (var ps2 =
                  c.prepareStatement(
                      "SELECT produit FROM dossiers_sav WHERE numero_serie=? AND produit IS NOT NULL AND TRIM(produit)<>'' ORDER BY date_entree DESC LIMIT 1")) {
                ps2.setString(1, sn);
                try (var r3 = ps2.executeQuery()) {
                  if (r3.next()) {
                    prod = r3.getString(1);
                  }
                }
              }
              ps.setString(1, prod);
              ps.setString(2, sn);
              ps.setString(3, code);
              ps.executeUpdate();
            }
          }
        }
      }

      // Synchroniser dossiers_sav.code manquant avec le code du produit (au lieu d'en générer un
      // par intervention) quand numero_serie est présent
      try (var rs =
          c.createStatement()
              .executeQuery(
                  "SELECT d.id, p.code FROM dossiers_sav d JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.numero_serie IS NOT NULL AND TRIM(d.numero_serie)<>'' AND (d.code IS NULL OR TRIM(d.code)='') AND p.code IS NOT NULL")) {
        while (rs.next()) {
          long did = rs.getLong(1);
          String pcode = rs.getString(2);
          try (var ps = c.prepareStatement("UPDATE dossiers_sav SET code=? WHERE id=?")) {
            ps.setString(1, pcode);
            ps.setLong(2, did);
            ps.executeUpdate();
          }
        }
      }
    }
  }

  // Génère un code au format AA1234 (2 lettres majuscules + 4 chiffres)
  private static String generateCode() {
    String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    java.util.Random rnd = new java.util.Random();
    char a = letters.charAt(rnd.nextInt(letters.length()));
    char b = letters.charAt(rnd.nextInt(letters.length()));
    int num = rnd.nextInt(10000); // 0000-9999
    return String.valueOf(a) + b + String.format("%04d", num);
  }

  private static boolean existsCode(Connection c, String code) throws Exception {
    try (var ps = c.prepareStatement("SELECT 1 FROM dossiers_sav WHERE code=? LIMIT 1")) {
      ps.setString(1, code);
      try (var rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static boolean productCodeExists(Connection c, String code) throws Exception {
    try (var ps = c.prepareStatement("SELECT 1 FROM produits WHERE code=? LIMIT 1")) {
      ps.setString(1, code);
      try (var rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static String readResource(String name) throws Exception {
    try (var in = DB.class.getClassLoader().getResourceAsStream(name)) {
      if (in == null) {
        throw new IllegalStateException("Ressource introuvable: " + name);
      }
      try (var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line).append('\n');
        }
        return sb.toString();
      }
    }
  }

  public static DataSource wrap(HikariDataSource ds) {
    return ds;
  }

  @Override
  public void close() {
    ds.close();
  }
}
