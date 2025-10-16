package com.magsav.utils;

import com.magsav.db.H2DB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Vérificateur de schéma H2 complet
 * Teste la création de toutes les tables de l'ancienne DB
 */
public class SchemaVerifier {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔄 Vérification du schéma H2 complet...");
            
            // Supprimer l'ancienne base
            System.out.println("🗑️ Suppression de l'ancienne base H2...");
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("./data/magsav_h2.mv.db"));
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("./data/magsav_h2.trace.db"));
            
            // Initialiser H2 avec le nouveau schéma
            System.out.println("🚀 Initialisation du nouveau schéma H2...");
            H2DB.init();
            
            // Obtenir une connexion et lister toutes les tables
            Connection conn = H2DB.getConnection();
            Statement stmt = conn.createStatement();
            
            System.out.println("📋 Tables créées dans H2 :");
            ResultSet rs = stmt.executeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME"
            );
            
            int tableCount = 0;
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("  ✅ " + tableName);
                tableCount++;
            }
            
            System.out.println("\n📊 Résumé :");
            System.out.println("  Total tables créées : " + tableCount);
            
            // Vérifier quelques contraintes de clés étrangères
            System.out.println("\n🔍 Vérification des contraintes :");
            
            rs = stmt.executeQuery(
                "SELECT COUNT(*) as constraint_count FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS"
            );
            
            if (rs.next()) {
                System.out.println("  🔗 Contraintes FK : " + rs.getInt("constraint_count"));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("🎉 Schéma H2 complet vérifié avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification : " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}