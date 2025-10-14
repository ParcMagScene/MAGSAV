package com.magsav.test;

import com.magsav.db.DB;
import com.magsav.model.Technicien;
import com.magsav.repo.TechnicienRepository;
import javafx.collections.ObservableList;

/**
 * Test simple pour vérifier les données des techniciens
 */
public class TestTechniciensData {
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Test des données techniciens Mag Scene");
            System.out.println("==========================================");
            
            // Initialiser la base de données
            DB.init();
            
            // Créer le repository
            TechnicienRepository repo = new TechnicienRepository();
            
            // Charger les techniciens
            ObservableList<Technicien> techniciens = repo.findAll();
            
            System.out.println("✅ " + techniciens.size() + " techniciens trouvés:");
            System.out.println();
            
            // Afficher les détails de chaque technicien
            for (Technicien tech : techniciens) {
                System.out.println("👤 " + tech.getNomComplet());
                System.out.println("   📧 " + tech.getEmail());
                System.out.println("   📞 " + tech.getTelephone());
                System.out.println("   🏢 " + tech.getFonction());
                System.out.println("   🏠 " + tech.getAdresse() + ", " + tech.getCodePostal() + " " + tech.getVille());
                System.out.println("   🚗 Permis: " + tech.getPermisConduire());
                System.out.println("   ⚙️ Spécialités: " + tech.getSpecialites());
                System.out.println("   📋 Statut: " + tech.getStatut().getDisplayName());
                System.out.println("   🏢 Société: " + tech.getSocieteNom());
                System.out.println();
            }
            
            // Test des filtres
            System.out.println("🔍 Test des filtres:");
            
            // Techniciens actifs
            long actifs = techniciens.stream()
                .filter(t -> t.getStatut() == Technicien.StatutTechnicien.ACTIF)
                .count();
            System.out.println("   ✅ Techniciens actifs: " + actifs);
            
            // Par fonction
            System.out.println("   📊 Répartition par fonction:");
            techniciens.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Technicien::getFonction,
                    java.util.stream.Collectors.counting()))
                .forEach((fonction, count) -> 
                    System.out.println("      • " + fonction + ": " + count));
            
            // Techniciens avec permis PL
            long avecPL = techniciens.stream()
                .filter(t -> t.getPermisConduire() != null && t.getPermisConduire().contains("PL"))
                .count();
            System.out.println("   🚛 Avec permis PL: " + avecPL);
            
            System.out.println();
            System.out.println("✅ Test terminé avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}