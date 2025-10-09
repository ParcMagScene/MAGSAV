-- Script d'initialisation pour les tests de scraping d'images
-- Crée les tables nécessaires pour les tests en mémoire

-- Table des produits (version simplifiée pour tests)
CREATE TABLE IF NOT EXISTS produits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT,
    nom TEXT NOT NULL,
    sn TEXT,
    fabricant TEXT,
    fabricant_id INTEGER,
    uid TEXT UNIQUE,
    situation TEXT DEFAULT 'En stock',
    photo TEXT,
    category TEXT,
    subcategory TEXT,
    description TEXT,
    date_achat TEXT,
    client TEXT,
    prix TEXT,
    garantie TEXT,
    sav_externe_id INTEGER,
    categorieId INTEGER,
    sousCategorieId INTEGER,
    scraped_images TEXT
);

-- Index pour les images scrapées
CREATE INDEX IF NOT EXISTS idx_produits_scraped_images ON produits(scraped_images);

-- Quelques données de test (optionnel)
INSERT OR IGNORE INTO produits (nom, sn, fabricant, uid, situation) VALUES 
('Test Mixer', 'MX-001', 'TestBrand', 'TEST001', 'En stock');

-- Vérifier la structure
PRAGMA table_info(produits);