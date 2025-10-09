package com.magsav.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migration pour ajouter les tables unifiées des entités
 */
public class EntityMigration {

    public static void migrate() throws SQLException {
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Créer la table entities si elle n'existe pas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS entities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    nom TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    adresse TEXT,
                    siret TEXT,
                    tva_number TEXT,
                    actif BOOLEAN DEFAULT 1,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """);
            
            // Créer la table services si elle n'existe pas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS services (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entity_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    nom TEXT,
                    description TEXT,
                    actif BOOLEAN DEFAULT 1,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (entity_id) REFERENCES entities (id) ON DELETE CASCADE
                )
                """);
            
            // Créer la table contacts si elle n'existe pas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contacts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entity_id INTEGER,
                    service_id INTEGER,
                    prenom TEXT,
                    nom TEXT,
                    fonction TEXT,
                    email TEXT,
                    phone_fixe TEXT,
                    phone_mobile TEXT,
                    notes TEXT,
                    principal BOOLEAN DEFAULT 0,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (entity_id) REFERENCES entities (id) ON DELETE CASCADE,
                    FOREIGN KEY (service_id) REFERENCES services (id) ON DELETE CASCADE,
                    CHECK ((entity_id IS NOT NULL AND service_id IS NULL) OR (entity_id IS NULL AND service_id IS NOT NULL))
                )
                """);
            
            // Index pour les performances
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_entities_type ON entities (type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_entities_nom ON entities (nom)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_services_entity_id ON services (entity_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_services_type ON services (type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contacts_entity_id ON contacts (entity_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contacts_service_id ON contacts (service_id)");
            
            System.out.println("Migration des entités unifiées terminée avec succès");
        }
    }
}