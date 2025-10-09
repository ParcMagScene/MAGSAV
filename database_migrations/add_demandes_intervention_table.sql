-- Migration pour ajouter la table des demandes d'intervention
-- Cette table stocke les demandes avant validation par l'administrateur

CREATE TABLE IF NOT EXISTS demandes_intervention (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Statut de la demande
    statut TEXT NOT NULL DEFAULT 'en_attente', -- 'en_attente', 'validee', 'rejetee'
    type_demande TEXT NOT NULL, -- 'produit_repertorie', 'produit_non_repertorie'
    
    -- Référence au produit existant (si applicable)
    product_id INTEGER,
    
    -- Données produit temporaires (pour les nouveaux produits)
    produit_nom TEXT,
    produit_sn TEXT,
    produit_uid TEXT, -- UID scanné ou fourni
    produit_fabricant TEXT,
    produit_category TEXT,
    produit_subcategory TEXT,
    produit_description TEXT,
    
    -- Détails de la demande d'intervention
    panne_description TEXT NOT NULL,
    client_note TEXT,
    detecteur TEXT, -- Nom de la personne qui a détecté le problème
    detector_societe_id INTEGER,
    
    -- Métadonnées
    demandeur_nom TEXT, -- Nom de l'utilisateur qui fait la demande
    date_demande TEXT DEFAULT (datetime('now')),
    date_validation TEXT,
    validateur_nom TEXT, -- Nom de l'administrateur qui valide
    notes_validation TEXT,
    
    -- Intervention créée après validation
    intervention_id INTEGER,
    
    FOREIGN KEY (product_id) REFERENCES produits(id),
    FOREIGN KEY (detector_societe_id) REFERENCES societes(id),
    FOREIGN KEY (intervention_id) REFERENCES interventions(id)
);

-- Index pour les performances
CREATE INDEX IF NOT EXISTS idx_demandes_statut ON demandes_intervention(statut);
CREATE INDEX IF NOT EXISTS idx_demandes_type ON demandes_intervention(type_demande);
CREATE INDEX IF NOT EXISTS idx_demandes_product ON demandes_intervention(product_id);
CREATE INDEX IF NOT EXISTS idx_demandes_date ON demandes_intervention(date_demande);