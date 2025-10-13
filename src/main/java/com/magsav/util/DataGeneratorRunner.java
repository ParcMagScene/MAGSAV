package com.magsav.util;

/**
 * Classe utilitaire pour lancer le générateur de données de test
 */
public class DataGeneratorRunner {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("         🎯 GÉNÉRATEUR DE DONNÉES MAGSAV        ");  
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
        
        try {
            // Appeler le générateur principal
            TestDataGenerator.generateCompleteTestData();
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("       ✅ GÉNÉRATION COMPLÈTE TERMINÉE !       ");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println();
            System.out.println("📊 Résumé des données générées :");
            System.out.println("   • 20 catégories hiérarchiques");
            System.out.println("   • 50 sociétés (clients, fournisseurs, etc.)");
            System.out.println("   • 10 techniciens avec spécialités");
            System.out.println("   • 8 véhicules de différents types");
            System.out.println("   • 100 produits audiovisuels");
            System.out.println("   • 30 interventions avec statuts variés");
            System.out.println("   • 25 planifications d'interventions");
            System.out.println("   • 15 commandes avec lignes détaillées");
            System.out.println("   • 80 mouvements de stock");
            System.out.println("   • 12 alertes de stock");
            System.out.println("   • 20 disponibilités de techniciens");
            System.out.println("   • 35 communications diverses");
            System.out.println("   • 40 entrées d'historique SAV");
            System.out.println("   • Templates d'emails préconfiguré");
            System.out.println();
            System.out.println("🎉 La base de données est maintenant prête pour les tests !");
            System.out.println("Vous pouvez relancer MAGSAV pour voir les données générées.");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la génération :");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}