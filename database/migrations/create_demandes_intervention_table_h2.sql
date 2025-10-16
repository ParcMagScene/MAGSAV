-- Script de création de la table demandes_intervention pour H2
-- (au lieu de demande_intervention)

CREATE TABLE IF NOT EXISTS demandes_intervention (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    
    -- Informations de base
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'EN_COURS', 'VALIDEE', 'REFUSEE', 'TERMINEE', 'ANNULEE')),
    type_demande VARCHAR(50) NOT NULL DEFAULT 'INTERVENTIONS',
    product_id BIGINT,
    
    -- Informations produit
    produit_nom VARCHAR(255),
    produit_sn VARCHAR(100),
    produit_uid VARCHAR(100),
    produit_fabricant VARCHAR(255),
    produit_category VARCHAR(100),
    produit_subcategory VARCHAR(100),
    produit_description TEXT,
    
    -- Informations propriétaire
    type_proprietaire VARCHAR(50),
    proprietaire_id BIGINT,
    demande_creation_proprietaire_id BIGINT,
    proprietaire_nom_temp VARCHAR(255),
    proprietaire_details_temp TEXT,
    
    -- Détails de la demande
    panne_description TEXT,
    client_note TEXT,
    detecteur VARCHAR(255),
    detector_societe_id BIGINT,
    demandeur_nom VARCHAR(255),
    
    -- Dates
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_demandes_intervention_statut ON demandes_intervention(statut);
CREATE INDEX IF NOT EXISTS idx_demandes_intervention_type ON demandes_intervention(type_demande);
CREATE INDEX IF NOT EXISTS idx_demandes_intervention_date ON demandes_intervention(date_demande);