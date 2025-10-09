-- Migration pour ajouter le support des images scrapées
-- MAGSAV v1.3 - Image Scraping Feature

-- Ajouter la colonne scraped_images à la table produits
ALTER TABLE produits ADD COLUMN scraped_images TEXT;

-- Créer un index pour optimiser les recherches sur les produits avec/sans images
CREATE INDEX IF NOT EXISTS idx_produits_scraped_images ON produits(scraped_images);

-- Mettre à jour les statistiques
ANALYZE produits;

-- Insertion de données de test (optionnel - à adapter selon vos besoins)
-- UPDATE produits SET scraped_images = 'https://example.com/image1.jpg,https://example.com/image2.jpg' WHERE id = 1;

PRAGMA table_info(produits);