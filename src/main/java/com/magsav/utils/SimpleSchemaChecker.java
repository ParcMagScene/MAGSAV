package com.magsav.utils;

import com.magsav.db.H2DB;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Vérificateur simple de schéma H2
 */
public class SimpleSchemaChecker {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔄 Vérification simple du schéma H2...");
            
            // Initialiser H2
            H2DB.init();
            
            // Obtenir une connexion et lister toutes les tables
            Connection conn = H2DB.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("📋 Tables trouvées dans H2 :");
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            int tableCount = 0;
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("  ✅ " + tableName);
                tableCount++;
            }
            
            System.out.println("\n📊 Total tables : " + tableCount);
            
            tables.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}