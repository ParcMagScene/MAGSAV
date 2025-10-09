package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Script pour ajouter le champ sav_externe_id à la table produits
 */
public class AddSavExterneIdToProduits {
    public static void main(String[] args) {
        try (Connection conn = DB.getConnection()) {
            // Ajouter la colonne sav_externe_id
            String sql = "ALTER TABLE produits ADD COLUMN sav_externe_id INTEGER";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            
            // Créer un index sur cette colonne
            String indexSql = "CREATE INDEX idx_produits_sav_externe ON produits(sav_externe_id)";
            PreparedStatement indexStmt = conn.prepareStatement(indexSql);
            indexStmt.executeUpdate();
            
            System.out.println("✅ Colonne sav_externe_id ajoutée avec succès à la table produits");
            
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate column name")) {
                System.out.println("ℹ️  La colonne sav_externe_id existe déjà");
            } else {
                System.err.println("❌ Erreur lors de l'ajout de la colonne: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}