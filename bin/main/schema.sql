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
  code TEXT UNIQUE,
  produit TEXT NOT NULL,
  numero_serie TEXT,
  proprietaire TEXT NOT NULL,
  panne TEXT,
  statut TEXT DEFAULT 'recu',
  detecteur TEXT,
  date_entree TEXT,
  date_sortie TEXT,
  category_id INTEGER,
  subcategory_id INTEGER,
  created_at TEXT DEFAULT (datetime('now')),
  UNIQUE(numero_serie, proprietaire)
);

-- Table des produits (identité par numéro de série) avec code unique au niveau produit
CREATE TABLE IF NOT EXISTS produits (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  produit TEXT,
  numero_serie TEXT UNIQUE,
  code TEXT UNIQUE,
  photo_path TEXT,
  manufacturer_id INTEGER,
  created_at TEXT DEFAULT (datetime('now'))
);

-- Catégories et sous-catégories (hiérarchie via parent_id)
CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  parent_id INTEGER,
  FOREIGN KEY(parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Fabricants (manufacturers)
CREATE TABLE IF NOT EXISTS manufacturers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  website TEXT,
  contact_email TEXT,
  contact_phone TEXT,
  created_at TEXT DEFAULT (datetime('now'))
);

-- SAV externes (service providers)
CREATE TABLE IF NOT EXISTS service_providers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  email TEXT,
  phone TEXT,
  address TEXT,
  created_at TEXT DEFAULT (datetime('now'))
);

-- Les index sont créés de manière idempotente lors de la migration (DB.migrate)
