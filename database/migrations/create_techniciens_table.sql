-- Script de création/mise à jour de la table techniciens pour Mag Scene
-- Inclut toutes les informations nécessaires : contact, adresse, permis, habilitations

-- Supprimer la table si elle existe pour repartir proprement
DROP TABLE IF EXISTS techniciens;

-- Créer la table techniciens avec toutes les colonnes nécessaires
CREATE TABLE techniciens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Informations de base
    nom TEXT NOT NULL,
    prenom TEXT NOT NULL,
    fonction TEXT NOT NULL, -- Technicien Distribution, Technicien Lumière, etc.
    email TEXT UNIQUE,
    telephone TEXT,
    telephone_urgence TEXT,
    
    -- Adresse
    adresse TEXT,
    code_postal TEXT,
    ville TEXT,
    
    -- Permis et habilitations
    permis_conduire TEXT, -- VL, PL, CACES, etc.
    habilitations TEXT, -- JSON des habilitations avec dates
    date_obtention_permis DATE,
    date_validite_habilitations DATE,
    
    -- Spécialités et statut
    specialites TEXT, -- JSON des spécialités techniques
    statut TEXT DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'CONGE', 'INDISPONIBLE', 'INACTIF')),
    notes TEXT,
    
    -- Association société
    societe_id INTEGER,
    societe_nom TEXT,
    
    -- Intégration Google
    google_contact_id TEXT,
    google_calendar_id TEXT,
    sync_google_enabled INTEGER DEFAULT 0,
    last_google_sync TEXT,
    
    -- Métadonnées
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX idx_techniciens_nom ON techniciens(nom, prenom);
CREATE INDEX idx_techniciens_email ON techniciens(email);
CREATE INDEX idx_techniciens_societe ON techniciens(societe_id);
CREATE INDEX idx_techniciens_statut ON techniciens(statut);
CREATE INDEX idx_techniciens_fonction ON techniciens(fonction);

-- Trigger pour mettre à jour automatiquement la date de modification
CREATE TRIGGER update_techniciens_modification 
    AFTER UPDATE ON techniciens
    FOR EACH ROW
BEGIN
    UPDATE techniciens 
    SET date_modification = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Afficher la structure créée
.schema techniciens

-- Afficher un message de confirmation
SELECT '✅ Table techniciens créée avec succès' as message;