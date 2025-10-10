package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilitaire pour vider complètement la base de données
 */
public class DatabaseCleaner {
    
    public static void main(String[] args) {
        cleanDatabase();
    }
    
    /**
     * Vide toutes les tables de la base de données
     */
    public static void cleanDatabase() {
        try {
            System.out.println("🧹 VIDAGE COMPLET DE LA BASE DE DONNÉES...");
            
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                
                // Désactiver les contraintes de clés étrangères temporairement
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // Vider toutes les tables dans l'ordre approprié
                String[] tables = {
                    "interventions",
                    "demandes_intervention", 
                    "produits",
                    "categories",
                    "companies",
                    "users",
                    "sessions"
                };
                
                for (String table : tables) {
                    try {
                        int deleted = stmt.executeUpdate("DELETE FROM " + table);
                        System.out.println("   🗑️ Table '" + table + "': " + deleted + " enregistrements supprimés");
                    } catch (Exception e) {
                        System.out.println("   ⚠️ Erreur vidage table '" + table + "': " + e.getMessage());
                    }
                }
                
                // Réinitialiser les compteurs d'auto-increment
                for (String table : tables) {
                    try {
                        stmt.execute("DELETE FROM sqlite_sequence WHERE name='" + table + "'");
                    } catch (Exception e) {
                        // Ignorez si la table n'a pas d'auto-increment
                    }
                }
                
                // Réactiver les contraintes de clés étrangères
                stmt.execute("PRAGMA foreign_keys = ON");
                
                System.out.println("✅ Base de données vidée avec succès !");
                
                // Afficher les comptages finaux
                System.out.println("\n📊 VÉRIFICATION POST-VIDAGE :");
                for (String table : tables) {
                    try {
                        var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table);
                        if (rs.next()) {
                            System.out.println("   📋 " + table + ": " + rs.getInt("count") + " enregistrements");
                        }
                    } catch (Exception e) {
                        System.out.println("   ❌ Erreur vérification '" + table + "': " + e.getMessage());
                    }
                }
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du vidage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}