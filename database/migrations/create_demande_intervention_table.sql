-- Script de création de la table demande_intervention pour les demandes de test

-- Créer la table demande_intervention si elle n'existe pas
CREATE TABLE IF NOT EXISTS demande_intervention (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Informations de base
    type_demande TEXT NOT NULL DEFAULT 'TECHNIQUE',
    description TEXT NOT NULL,
    statut_demande TEXT DEFAULT 'EN_ATTENTE' CHECK (statut_demande IN ('EN_ATTENTE', 'EN_COURS', 'TERMINEE', 'ANNULEE')),
    priorite TEXT DEFAULT 'NORMALE' CHECK (priorite IN ('FAIBLE', 'NORMALE', 'HAUTE', 'URGENTE')),
    
    -- Dates
    date_demande DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_souhaite DATE,
    date_debut_intervention DATETIME,
    date_fin_intervention DATETIME,
    
    -- Attribution
    technicien_assigne INTEGER,
    client_nom TEXT,
    lieu_intervention TEXT,
    
    -- Détails techniques
    materiel_requis TEXT,
    duree_estimee_heures REAL,
    cout_estime REAL,
    
    -- Suivi
    notes TEXT,
    rapport_intervention TEXT,
    
    -- Métadonnées
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Clés étrangères
    FOREIGN KEY (technicien_assigne) REFERENCES techniciens(id)
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_demande_intervention_statut ON demande_intervention(statut_demande);
CREATE INDEX IF NOT EXISTS idx_demande_intervention_technicien ON demande_intervention(technicien_assigne);
CREATE INDEX IF NOT EXISTS idx_demande_intervention_date_demande ON demande_intervention(date_demande);
CREATE INDEX IF NOT EXISTS idx_demande_intervention_date_souhaite ON demande_intervention(date_souhaite);
CREATE INDEX IF NOT EXISTS idx_demande_intervention_priorite ON demande_intervention(priorite);

-- Trigger pour mettre à jour automatiquement la date de modification
CREATE TRIGGER IF NOT EXISTS update_demande_intervention_modification 
    AFTER UPDATE ON demande_intervention
    FOR EACH ROW
BEGIN
    UPDATE demande_intervention 
    SET date_modification = CURRENT_TIMESTAMP 
    WHERE id = NEW.id;
END;

-- Afficher un message de confirmation
SELECT '✅ Table demande_intervention créée avec succès' as message;