-- Schéma SQLite MAGSAV
CREATE TABLE IF NOT EXISTS clients (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  prenom TEXT,
  email TEXT NOT NULL,
  tel TEXT,
  adresse TEXT,
  created_at TEXT DEFAULT (datetime('now')),
  UNIQUE(email)
);

CREATE TABLE IF NOT EXISTS fournisseurs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  email TEXT NOT NULL,
  tel TEXT,
  siret TEXT,
  created_at TEXT DEFAULT (datetime('now')),
  UNIQUE(email)
);

CREATE TABLE IF NOT EXISTS appareils (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  client_id INTEGER NOT NULL,
  marque TEXT,
  modele TEXT,
  sn TEXT,
  accessoires TEXT,
  FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dossiers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  appareil_id INTEGER NOT NULL,
  statut TEXT DEFAULT 'recu',
  symptome TEXT,
  commentaire TEXT,
  date_entree TEXT DEFAULT (date('now')),
  date_sortie TEXT,
  FOREIGN KEY(appareil_id) REFERENCES appareils(id) ON DELETE CASCADE
);

-- Table unifiée pour import CSV direct
CREATE TABLE IF NOT EXISTS dossiers_sav (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  produit TEXT NOT NULL,
  numero_serie TEXT NOT NULL,
  proprietaire TEXT NOT NULL,
  panne TEXT,
  statut TEXT DEFAULT 'recu',
  detecteur TEXT,
  date_entree TEXT,
  date_sortie TEXT,
  created_at TEXT DEFAULT (datetime('now')),
  UNIQUE(numero_serie, proprietaire)
);
