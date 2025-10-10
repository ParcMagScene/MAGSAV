-- MIGRATION COMPLÈTE VERS NOMS FRANÇAIS
-- Sauvegarde automatique avant migration

-- ====================================
-- MIGRATION TABLE PRODUITS
-- ====================================
BEGIN TRANSACTION;

-- Créer nouvelle table avec colonnes françaises
CREATE TABLE produits_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code_produit TEXT,
    nom_produit TEXT,
    numero_serie TEXT,
    nom_fabricant TEXT,
    fabricant_id INTEGER,
    uid_unique TEXT,
    statut_produit TEXT,
    photo_produit TEXT,
    categorie_principale TEXT,
    sous_categorie TEXT,
    description_produit TEXT,
    date_achat TEXT,
    nom_client TEXT,
    prix_achat TEXT,
    duree_garantie TEXT,
    sav_externe_id INTEGER,
    categorieId INTEGER,
    sousCategorieId INTEGER,
    scraped_images TEXT
);

-- Copier les données en mappant les colonnes
INSERT INTO produits_new (
    id, code_produit, nom_produit, numero_serie, nom_fabricant, fabricant_id,
    uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie,
    description_produit, date_achat, nom_client, prix_achat, duree_garantie,
    sav_externe_id, categorieId, sousCategorieId, scraped_images
)
SELECT 
    id, code, nom, sn, fabricant, fabricant_id,
    uid, situation, photo, category, subcategory,
    description, date_achat, client, prix, garantie,
    sav_externe_id, categorieId, sousCategorieId, scraped_images
FROM produits;

-- Remplacer ancienne table
DROP TABLE produits;
ALTER TABLE produits_new RENAME TO produits;

COMMIT;

-- ====================================
-- MIGRATION TABLE SOCIETES
-- ====================================
BEGIN TRANSACTION;

-- Créer nouvelle table societes avec colonnes françaises
CREATE TABLE societes_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type_societe TEXT,
    nom_societe TEXT,
    email_societe TEXT,
    telephone_societe TEXT,
    adresse_societe TEXT,
    notes_societe TEXT,
    date_creation TEXT DEFAULT (datetime('now'))
);

-- Copier les données
INSERT INTO societes_new (
    id, type_societe, nom_societe, email_societe, telephone_societe,
    adresse_societe, notes_societe, date_creation
)
SELECT 
    id, type, nom, email, phone,
    adresse, notes, created_at
FROM societes;

-- Remplacer ancienne table
DROP TABLE societes;
ALTER TABLE societes_new RENAME TO societes;

COMMIT;

-- ====================================
-- MIGRATION TABLE INTERVENTIONS
-- ====================================
BEGIN TRANSACTION;

-- Créer nouvelle table interventions avec colonnes françaises
CREATE TABLE interventions_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    produit_id INTEGER,
    statut_intervention TEXT,
    description_panne TEXT,
    numero_serie_intervention TEXT,
    note_client TEXT,
    description_defaut TEXT,
    detecteur_societe_id INTEGER,
    nom_detecteur TEXT,
    date_entree TEXT,
    date_sortie TEXT,
    type_proprietaire TEXT,
    proprietaire_societe_id INTEGER
);

-- Copier les données
INSERT INTO interventions_new (
    id, produit_id, statut_intervention, description_panne, numero_serie_intervention,
    note_client, description_defaut, detecteur_societe_id, nom_detecteur,
    date_entree, date_sortie, type_proprietaire, proprietaire_societe_id
)
SELECT 
    id, product_id, statut, panne, serial_number,
    client_note, defect_description, detector_societe_id, detecteur,
    date_entree, date_sortie, owner_type, owner_societe_id
FROM interventions;

-- Remplacer ancienne table
DROP TABLE interventions;
ALTER TABLE interventions_new RENAME TO interventions;

COMMIT;

-- ====================================
-- MIGRATION TABLE CATEGORIES
-- ====================================
BEGIN TRANSACTION;

-- Créer nouvelle table categories avec colonnes françaises
CREATE TABLE categories_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom_categorie TEXT,
    parent_id INTEGER
);

-- Copier les données
INSERT INTO categories_new (id, nom_categorie, parent_id)
SELECT id, nom, parent_id
FROM categories;

-- Remplacer ancienne table
DROP TABLE categories;
ALTER TABLE categories_new RENAME TO categories;

COMMIT;

-- ====================================
-- RECRÉER TOUS LES INDEX
-- ====================================
CREATE INDEX IF NOT EXISTS idx_prod_nom ON produits(UPPER(nom_produit));
CREATE INDEX IF NOT EXISTS idx_prod_uid ON produits(uid_unique);
CREATE INDEX IF NOT EXISTS idx_prod_fabricant ON produits(nom_fabricant);
CREATE INDEX IF NOT EXISTS idx_prod_category ON produits(categorie_principale);
CREATE INDEX IF NOT EXISTS idx_intervention_product ON interventions(produit_id);
CREATE INDEX IF NOT EXISTS idx_intervention_date_entree ON interventions(date_entree);
CREATE INDEX IF NOT EXISTS idx_societes_type ON societes(type_societe);
CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_prod_sav_externe ON produits(sav_externe_id);

-- Affichage final
SELECT 'MIGRATION TERMINÉE - Vérifications:' as status;
SELECT 'Produits migrés:' as status, COUNT(*) as nombre FROM produits;
SELECT 'Sociétés migrées:' as status, COUNT(*) as nombre FROM societes;
SELECT 'Interventions migrées:' as status, COUNT(*) as nombre FROM interventions;
SELECT 'Catégories migrées:' as status, COUNT(*) as nombre FROM categories;