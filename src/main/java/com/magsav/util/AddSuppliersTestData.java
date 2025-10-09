package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Script pour ajouter des fournisseurs de test
 */
public class AddSuppliersTestData {
    public static void main(String[] args) {
        try (Connection conn = DB.getConnection()) {
            // Vérifier s'il y a déjà des fournisseurs
            String checkSql = "SELECT COUNT(*) FROM societes WHERE type = 'FOURNISSEUR'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            
            if (rs.getInt(1) > 0) {
                System.out.println("ℹ️  Des fournisseurs existent déjà (" + rs.getInt(1) + ")");
                return;
            }
            
            // Données de test pour les fournisseurs
            String[][] fournisseurs = {
                {
                    "SonoMat Distribution", 
                    "commandes@sonomat.fr", 
                    "01.42.36.78.90", 
                    "45 Avenue des Fournisseurs, 93200 Saint-Denis",
                    "Spécialités: Consoles, amplificateurs, enceintes pro\nDélais: 24-48h (stock), 5-10j (commande spéciale)\nConditions: Remise 15% > 1000€, Franco 500€\nContact commercial: Jean Dupont (jdupont@sonomat.fr)"
                },
                {
                    "ElectroAudio Supply", 
                    "info@electroaudio.com", 
                    "04.76.82.15.44", 
                    "Zone Industrielle Les Glières, 38240 Meylan",
                    "Spécialités: Câblage, connectiques, accessoires\nDélais: Livraison express 24h disponible\nConditions: Remise quantité, SAV 2 ans\nContact: Marie Durand (marie@electroaudio.com)"
                },
                {
                    "LightTech Components", 
                    "sales@lighttech.eu", 
                    "03.88.45.67.23", 
                    "12 Rue de l'Innovation, 67000 Strasbourg",
                    "Spécialités: Éclairage LED, gradateurs, DMX\nDélais: 3-5 jours ouvrés standard\nConditions: Garantie constructeur étendue\nContact: Pierre Martin (p.martin@lighttech.eu)"
                },
                {
                    "Parts & Repairs Pro", 
                    "support@partsrepairs.fr", 
                    "05.61.28.94.17", 
                    "38 Boulevard Technique, 31000 Toulouse",
                    "Spécialités: Pièces détachées, composants électroniques\nDélais: Stock permanent pièces courantes\nConditions: Devis gratuit, conseil technique\nContact SAV: tech@partsrepairs.fr"
                }
            };
            
            String insertSql = "INSERT INTO societes (type, nom, email, phone, adresse, notes) VALUES ('FOURNISSEUR', ?, ?, ?, ?, ?)";
            
            for (String[] fournisseur : fournisseurs) {
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, fournisseur[0]); // nom
                insertStmt.setString(2, fournisseur[1]); // email
                insertStmt.setString(3, fournisseur[2]); // phone
                insertStmt.setString(4, fournisseur[3]); // adresse
                insertStmt.setString(5, fournisseur[4]); // notes
                
                insertStmt.executeUpdate();
                System.out.println("✅ Fournisseur ajouté: " + fournisseur[0]);
            }
            
            System.out.println("✅ " + fournisseurs.length + " fournisseurs créés avec succès");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}