-- Schema unifié pour la gestion des entités
-- Remplace et étend la table 'societes' actuelle

-- Table principale des entités
CREATE TABLE IF NOT EXISTS entities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL, -- 'PARTICULIER', 'SOCIETE', 'ADMINISTRATION'
    nom TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    adresse TEXT,
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now'))
);

-- Table des services (pour les sociétés et administrations)
CREATE TABLE IF NOT EXISTS services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_id INTEGER NOT NULL,
    type TEXT NOT NULL, -- 'VENTE', 'SAV', 'PIECES', 'FABRICATION', 'AUTRE'
    nom TEXT, -- Nom optionnel du service
    description TEXT,
    actif INTEGER DEFAULT 1, -- 1 = actif, 0 = inactif
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (entity_id) REFERENCES entities(id) ON DELETE CASCADE
);

-- Table des contacts (pour tous types d'entités)
CREATE TABLE IF NOT EXISTS contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_id INTEGER, -- NULL si contact rattaché à un service
    service_id INTEGER, -- NULL si contact rattaché directement à l'entité
    prenom TEXT,
    nom TEXT NOT NULL,
    fonction TEXT, -- Fonction dans l'entité/service
    email TEXT,
    phone_fixe TEXT,
    phone_mobile TEXT,
    notes TEXT,
    principal INTEGER DEFAULT 0, -- 1 = contact principal
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (entity_id) REFERENCES entities(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CHECK ((entity_id IS NOT NULL AND service_id IS NULL) OR (entity_id IS NULL AND service_id IS NOT NULL))
);

-- Index pour les performances
CREATE INDEX IF NOT EXISTS idx_entities_type ON entities(type);
CREATE INDEX IF NOT EXISTS idx_entities_nom ON entities(UPPER(nom));
CREATE INDEX IF NOT EXISTS idx_services_entity ON services(entity_id);
CREATE INDEX IF NOT EXISTS idx_services_type ON services(type);
CREATE INDEX IF NOT EXISTS idx_contacts_entity ON contacts(entity_id);
CREATE INDEX IF NOT EXISTS idx_contacts_service ON contacts(service_id);
CREATE INDEX IF NOT EXISTS idx_contacts_principal ON contacts(principal);

-- Vue pour simplifier les requêtes de compatibilité
CREATE VIEW IF NOT EXISTS societes_view AS
SELECT 
    e.id,
    CASE 
        WHEN e.type = 'SOCIETE' AND s.type = 'FABRICATION' THEN 'FABRICANT'
        WHEN e.type = 'SOCIETE' AND s.type = 'VENTE' THEN 'FOURNISSEUR'
        WHEN e.type = 'SOCIETE' AND s.type = 'SAV' THEN 'SAV_EXTERNE'
        WHEN e.type = 'SOCIETE' THEN 'CLIENT'
        WHEN e.type = 'PARTICULIER' THEN 'CLIENT'
        WHEN e.type = 'ADMINISTRATION' THEN 'CLIENT'
        ELSE 'CLIENT'
    END as type,
    e.nom,
    e.email,
    e.phone,
    e.adresse,
    e.notes,
    e.created_at
FROM entities e
LEFT JOIN services s ON e.id = s.entity_id;