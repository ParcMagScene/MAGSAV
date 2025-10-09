package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Script pour créer la table d'historique des mouvements SAV
 */
public class CreateSavHistoryTable {
    public static void main(String[] args) {
        try (Connection conn = DB.getConnection()) {
            // Créer la table d'historique SAV
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS sav_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    sav_externe_id INTEGER NOT NULL,
                    date_debut TEXT NOT NULL,
                    date_fin TEXT,
                    statut TEXT DEFAULT 'En cours',
                    notes TEXT,
                    created_at TEXT DEFAULT (datetime('now')),
                    updated_at TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (product_id) REFERENCES produits(id),
                    FOREIGN KEY (sav_externe_id) REFERENCES societes(id)
                )
            """;
            
            PreparedStatement createStmt = conn.prepareStatement(createTableSql);
            createStmt.executeUpdate();
            
            // Créer les index
            String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_sav_history_product ON sav_history(product_id)",
                "CREATE INDEX IF NOT EXISTS idx_sav_history_sav_externe ON sav_history(sav_externe_id)",
                "CREATE INDEX IF NOT EXISTS idx_sav_history_dates ON sav_history(date_debut, date_fin)"
            };
            
            for (String indexSql : indexes) {
                PreparedStatement indexStmt = conn.prepareStatement(indexSql);
                indexStmt.executeUpdate();
            }
            
            // Créer un trigger pour mettre à jour updated_at
            String triggerSql = """
                CREATE TRIGGER IF NOT EXISTS update_sav_history_timestamp 
                AFTER UPDATE ON sav_history
                BEGIN
                    UPDATE sav_history SET updated_at = datetime('now') WHERE id = NEW.id;
                END
            """;
            
            PreparedStatement triggerStmt = conn.prepareStatement(triggerSql);
            triggerStmt.executeUpdate();
            
            System.out.println("✅ Table sav_history créée avec succès");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}