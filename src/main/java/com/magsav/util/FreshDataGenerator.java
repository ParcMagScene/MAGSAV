package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Générateur de données fraîches
 * Vide toutes les tables et génère des données complètement nouvelles
 */
public class FreshDataGenerator {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("      🧹 GÉNÉRATION DE DONNÉES FRAÎCHES       ");  
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
        
        try {
            // Initialiser la base de données d'abord
            System.out.println("🔧 Initialisation de la base de données...");
            DB.init();
            System.out.println("✅ Base de données initialisée");
            System.out.println();
            
            // Vider toutes les tables
            System.out.println("🗑️ Suppression des données existantes...");
            clearAllTables();
            System.out.println("✅ Données supprimées");
            System.out.println();
            
            // Générer les nouvelles données
            System.out.println("🎯 Génération de nouvelles données...");
            TestDataGenerator.generateCompleteTestData();
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("    🎉 DONNÉES FRAÎCHES GÉNÉRÉES AVEC SUCCÈS ! ");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println();
            System.out.println("🔄 La base de données a été complètement régénérée");
            System.out.println("   avec des données fraîches et réalistes !");
            System.out.println();
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ ERREUR CRITIQUE lors de la génération :");
            System.err.println("   " + e.getMessage());
            System.err.println();
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Vide toutes les tables de données (préserve la structure)
     */
    private static void clearAllTables() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                
                // Désactiver les contraintes FK temporairement
                stmt.execute("PRAGMA foreign_keys = OFF");
                
            // Lister toutes les tables de données à vider (pas les tables système)
            String[] tables = {
                "request_items", "requests", "communications", "disponibilites_techniciens", "alertes_stock", 
                "mouvements_stock", "lignes_commandes", "commandes", "planifications",
                "sav_history", "interventions", "produits", "vehicules", "techniciens",
                "categories", "societes", "sync_history"
            };                for (String table : tables) {
                    try {
                        stmt.execute("DELETE FROM " + table);
                        stmt.execute("DELETE FROM sqlite_sequence WHERE name = '" + table + "'");
                        System.out.println("   ✓ Table " + table + " vidée");
                    } catch (SQLException e) {
                        // Table might not exist, ignore
                        System.out.println("   ⚠️ Table " + table + " ignorée (" + e.getMessage() + ")");
                    }
                }
                
                // Réactiver les contraintes FK
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Vider aussi les tables qui peuvent avoir des données par défaut
                try {
                    stmt.execute("DELETE FROM email_templates WHERE nom_template NOT IN ('intervention_planifiee', 'livraison_prevue')");
                } catch (SQLException e) {
                    // Table might not exist
                }
                
                // Préserver Mag Scène dans companies mais supprimer les autres
                try {
                    stmt.execute("DELETE FROM companies WHERE type != 'OWN_COMPANY'");
                } catch (SQLException e) {
                    // Table might not exist
                }
            }
        }
    }
}