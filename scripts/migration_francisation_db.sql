-- ==================================================================
-- SCRIPT DE MIGRATION - FRANCISATION ET NORMALISATION BASE MAGSAV
-- ==================================================================
-- Date: 10 octobre 2025
-- Objectif: Standardiser et franciser tous les noms de colonnes
-- 
-- ATTENTION: Faire une sauvegarde avant exécution !
-- ==================================================================

-- ================================================
-- 1. MIGRATION TABLE CATEGORIES
-- ================================================
-- Déjà cohérente, juste ajustement mineur
ALTER TABLE categories RENAME COLUMN parent_id TO id_parent;

-- ================================================
-- 2. UNIFICATION COMPANIES → SOCIETES 
-- ================================================
-- Copier les données de companies vers societes unifiée
CREATE TABLE societes_nouvelle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom_commercial TEXT NOT NULL,
    nom_legal TEXT,
    type_societe TEXT NOT NULL DEFAULT 'ENTREPRISE',
    siret TEXT,
    adresse TEXT,
    code_postal TEXT,
    ville TEXT,
    pays TEXT DEFAULT 'France',
    telephone TEXT,
    email TEXT,
    site_web TEXT,
    description TEXT,
    chemin_logo TEXT,
    secteur_activite TEXT,
    est_active INTEGER DEFAULT 1,
    date_creation TEXT NOT NULL,
    date_modification TEXT NOT NULL
);

-- Migrer données companies vers societes_nouvelle
INSERT INTO societes_nouvelle (
    id, nom_commercial, nom_legal, type_societe, siret, adresse, 
    code_postal, ville, pays, telephone, email, site_web, 
    description, chemin_logo, secteur_activite, est_active, 
    date_creation, date_modification
)
SELECT 
    id, name, legal_name, 
    CASE 
        WHEN type = 'MANUFACTURER' THEN 'FABRICANT'
        WHEN type = 'SUPPLIER' THEN 'FOURNISSEUR' 
        WHEN type = 'CLIENT' THEN 'CLIENT'
        WHEN type = 'SAV' THEN 'SAV_EXTERNE'
        ELSE 'ENTREPRISE'
    END,
    siret, address, postal_code, city, country, phone, email, 
    website, description, logo_path, sector, is_active, 
    created_at, updated_at
FROM companies;

-- Migrer aussi les données de l'ancienne table societes
INSERT INTO societes_nouvelle (
    nom_commercial, type_societe, email, telephone, adresse, 
    description, date_creation
)
SELECT 
    nom,
    CASE 
        WHEN type = 'manufacturer' THEN 'FABRICANT'
        WHEN type = 'supplier' THEN 'FOURNISSEUR'
        WHEN type = 'client' THEN 'CLIENT'
        WHEN type = 'sav' THEN 'SAV_EXTERNE'
        ELSE 'ENTREPRISE'
    END,
    email, phone, adresse, notes, created_at
FROM societes
WHERE nom NOT IN (SELECT nom_commercial FROM societes_nouvelle);

-- ================================================
-- 3. FRANCISATION TABLE PRODUITS
-- ================================================
CREATE TABLE produits_nouvelle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code_produit TEXT,
    nom TEXT,
    numero_serie TEXT,
    id_fabricant INTEGER,
    uid_unique TEXT,
    statut_produit TEXT,
    chemin_photo TEXT,
    description TEXT,
    date_achat TEXT,
    prix_achat TEXT,
    duree_garantie TEXT,
    id_sav_externe INTEGER,
    id_categorie INTEGER,
    id_sous_categorie INTEGER,
    images_scrapees TEXT,
    -- Champs dépréciés gardés temporairement
    nom_fabricant TEXT,
    nom_categorie TEXT,
    nom_sous_categorie TEXT,
    nom_client TEXT,
    FOREIGN KEY (id_fabricant) REFERENCES societes_nouvelle(id),
    FOREIGN KEY (id_sav_externe) REFERENCES societes_nouvelle(id),
    FOREIGN KEY (id_categorie) REFERENCES categories(id)
);

-- Migrer données produits
INSERT INTO produits_nouvelle (
    id, code_produit, nom, numero_serie, id_fabricant, uid_unique,
    statut_produit, chemin_photo, description, date_achat, prix_achat,
    duree_garantie, id_sav_externe, id_categorie, id_sous_categorie,
    images_scrapees, nom_fabricant, nom_categorie, nom_sous_categorie, nom_client
)
SELECT 
    id, code, nom, sn, fabricant_id, uid,
    CASE 
        WHEN situation IS NULL OR situation = '' THEN 'EN_STOCK'
        ELSE situation
    END,
    photo, description, date_achat, prix, garantie, sav_externe_id,
    categorieId, sousCategorieId, scraped_images,
    fabricant, category, subcategory, client
FROM produits;

-- ================================================
-- 4. FRANCISATION TABLE INTERVENTIONS
-- ================================================
CREATE TABLE interventions_nouvelle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_produit INTEGER,
    statut TEXT,
    description_panne TEXT,
    numero_serie TEXT,
    note_client TEXT,
    description_defaut TEXT,
    id_societe_detecteur INTEGER,
    date_entree TEXT,
    date_sortie TEXT,
    type_proprietaire TEXT,
    id_societe_proprietaire INTEGER,
    nom_detecteur TEXT,
    FOREIGN KEY (id_produit) REFERENCES produits_nouvelle(id),
    FOREIGN KEY (id_societe_detecteur) REFERENCES societes_nouvelle(id),
    FOREIGN KEY (id_societe_proprietaire) REFERENCES societes_nouvelle(id)
);

-- Migrer données interventions
INSERT INTO interventions_nouvelle (
    id, id_produit, statut, description_panne, numero_serie,
    note_client, description_defaut, id_societe_detecteur,
    date_entree, date_sortie, type_proprietaire, 
    id_societe_proprietaire, nom_detecteur
)
SELECT 
    id, product_id, statut, panne, serial_number,
    client_note, defect_description, detector_societe_id,
    date_entree, date_sortie, owner_type,
    owner_societe_id, detecteur
FROM interventions;

-- ================================================
-- 5. FRANCISATION TABLE USERS → UTILISATEURS
-- ================================================
CREATE TABLE utilisateurs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom_utilisateur TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    hash_mot_de_passe TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'UTILISATEUR',
    nom_complet TEXT,
    telephone TEXT,
    est_actif BOOLEAN DEFAULT 1,
    date_creation TEXT DEFAULT (datetime('now')),
    derniere_connexion TEXT,
    token_reset TEXT,
    expiration_token_reset TEXT
);

-- Migrer données users
INSERT INTO utilisateurs (
    id, nom_utilisateur, email, hash_mot_de_passe, role,
    nom_complet, telephone, est_actif, date_creation,
    derniere_connexion, token_reset, expiration_token_reset
)
SELECT 
    id, username, email, password_hash,
    CASE 
        WHEN role = 'ADMIN' THEN 'ADMINISTRATEUR'
        WHEN role = 'MANAGER' THEN 'GESTIONNAIRE'
        WHEN role = 'USER' THEN 'UTILISATEUR'
        ELSE 'UTILISATEUR'
    END,
    full_name, phone, is_active, created_at,
    last_login, reset_token, reset_token_expires
FROM users;

-- ================================================
-- 6. FRANCISATION TABLES DEMANDES
-- ================================================
CREATE TABLE demandes_interventions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    statut TEXT NOT NULL DEFAULT 'EN_ATTENTE',
    type_demande TEXT NOT NULL,
    id_produit INTEGER,
    nom_produit TEXT,
    numero_serie_produit TEXT,
    uid_produit TEXT,
    fabricant_produit TEXT,
    categorie_produit TEXT,
    sous_categorie_produit TEXT,
    description_produit TEXT,
    description_panne TEXT NOT NULL,
    note_client TEXT,
    nom_detecteur TEXT,
    id_societe_detecteur INTEGER,
    nom_demandeur TEXT,
    date_demande TEXT DEFAULT (datetime('now')),
    date_validation TEXT,
    nom_validateur TEXT,
    notes_validation TEXT,
    id_intervention INTEGER,
    type_proprietaire TEXT,
    id_proprietaire INTEGER,
    id_demande_creation_proprietaire INTEGER,
    nom_proprietaire_temp TEXT,
    details_proprietaire_temp TEXT,
    FOREIGN KEY (id_produit) REFERENCES produits_nouvelle(id),
    FOREIGN KEY (id_societe_detecteur) REFERENCES societes_nouvelle(id),
    FOREIGN KEY (id_intervention) REFERENCES interventions_nouvelle(id)
);

-- Migrer données demandes_intervention
INSERT INTO demandes_interventions SELECT * FROM demandes_intervention;

CREATE TABLE demandes_elevations_privilege (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_utilisateur INTEGER NOT NULL,
    nom_utilisateur TEXT NOT NULL,
    nom_complet TEXT,
    role_actuel TEXT NOT NULL,
    role_demande TEXT NOT NULL,
    justification TEXT NOT NULL,
    statut TEXT NOT NULL DEFAULT 'EN_ATTENTE',
    cree_par TEXT,
    date_creation TEXT DEFAULT (datetime('now')),
    date_validation TEXT,
    valide_par TEXT,
    notes_validation TEXT,
    date_expiration TEXT,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id)
);

-- Migrer données demandes_elevation_privilege
INSERT INTO demandes_elevations_privilege (
    id, id_utilisateur, nom_utilisateur, nom_complet, role_actuel,
    role_demande, justification, statut, cree_par, date_creation,
    date_validation, valide_par, notes_validation, date_expiration
)
SELECT 
    id, user_id, username, full_name, role_actuel, role_demande,
    justification, statut, created_by, created_at, validated_at,
    validated_by, notes_validation, expires_at
FROM demandes_elevation_privilege;

-- ================================================
-- 7. FRANCISATION TABLE SAV_HISTORY
-- ================================================
CREATE TABLE historique_sav (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_produit INTEGER,
    id_sav_externe INTEGER,
    date_debut TEXT,
    date_fin TEXT,
    statut TEXT,
    notes TEXT,
    date_creation TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (id_produit) REFERENCES produits_nouvelle(id),
    FOREIGN KEY (id_sav_externe) REFERENCES societes_nouvelle(id)
);

-- Migrer données sav_history
INSERT INTO historique_sav SELECT * FROM sav_history;

-- ================================================
-- 8. REMPLACEMENT DES ANCIENNES TABLES
-- ================================================
-- Supprimer anciennes tables et renommer nouvelles
DROP TABLE companies;
DROP TABLE societes;
ALTER TABLE societes_nouvelle RENAME TO societes;

DROP TABLE produits;
ALTER TABLE produits_nouvelle RENAME TO produits;

DROP TABLE interventions;
ALTER TABLE interventions_nouvelle RENAME TO interventions;

DROP TABLE users;
ALTER TABLE utilisateurs RENAME TO users;

DROP TABLE demandes_intervention;
ALTER TABLE demandes_interventions RENAME TO demandes_intervention;

DROP TABLE demandes_elevation_privilege;
ALTER TABLE demandes_elevations_privilege RENAME TO demandes_elevation_privilege;

DROP TABLE sav_history;
ALTER TABLE historique_sav RENAME TO sav_history;

-- ================================================
-- 9. CRÉATION D'INDEX POUR PERFORMANCE
-- ================================================
CREATE INDEX idx_produits_fabricant ON produits(id_fabricant);
CREATE INDEX idx_produits_categorie ON produits(id_categorie);
CREATE INDEX idx_interventions_produit ON interventions(id_produit);
CREATE INDEX idx_demandes_produit ON demandes_intervention(id_produit);
CREATE INDEX idx_societes_type ON societes(type_societe);

-- ================================================
-- VALIDATION POST-MIGRATION
-- ================================================
SELECT 'VALIDATION - Nombre de sociétés:' as info, COUNT(*) as total FROM societes;
SELECT 'VALIDATION - Nombre de produits:' as info, COUNT(*) as total FROM produits;
SELECT 'VALIDATION - Nombre interventions:' as info, COUNT(*) as total FROM interventions;
SELECT 'VALIDATION - Nombre utilisateurs:' as info, COUNT(*) as total FROM users;