package com.magsav.util;

import com.magsav.db.DB;

/**
 * Générateur complet de données de test
 * Utilise le générateur principal mais force la génération même avec des données existantes
 */
public class ComprehensiveTestDataGenerator {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("    🚀 GÉNÉRATEUR COMPLET DE DONNÉES MAGSAV     ");  
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
        
        try {
            // Initialiser la base de données d'abord
            System.out.println("🔧 Initialisation de la base de données...");
            DB.init();
            System.out.println("✅ Base de données initialisée");
            System.out.println();
            
            // Générer les données de test
            TestDataGenerator.generateCompleteTestData();
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("       🎉 GÉNÉRATION COMPLÈTE TERMINÉE !       ");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println();
            System.out.println("📊 Résumé des données générées :");
            System.out.println("   • 20 catégories hiérarchiques avec emojis");
            System.out.println("   • 50 sociétés françaises réalistes");
            System.out.println("   • 10 techniciens avec spécialités variées");  
            System.out.println("   • 8 véhicules de la flotte MAGSAV");
            System.out.println("   • 100 produits audiovisuels professionnels");
            System.out.println("   • 30 interventions avec différents statuts");
            System.out.println("   • 25 planifications d'interventions");
            System.out.println("   • 15 commandes fournisseurs avec détails");
            System.out.println("   • 45 lignes de commandes détaillées");
            System.out.println("   • 80 mouvements de stock réalistes");
            System.out.println("   • 12 alertes de stock configurées");
            System.out.println("   • 20 disponibilités de techniciens");
            System.out.println("   • 35 communications diverses (email, SMS, etc.)");
            System.out.println("   • 40 entrées d'historique SAV");
            System.out.println("   • Templates d'emails préconfiguré");
            System.out.println();
            System.out.println("🎯 La base de données est maintenant peuplée avec des données");
            System.out.println("   réalistes pour développement et tests !");
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
}