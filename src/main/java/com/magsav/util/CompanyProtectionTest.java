package com.magsav.util;

import com.magsav.model.Company;
import com.magsav.repo.CompanyRepository;
import com.magsav.db.DB;

/**
 * Test simple pour vérifier le système de protection des sociétés
 */
public class CompanyProtectionTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Test du système de protection des sociétés ===");
            
            // Test 1: Création d'une société "Mag Scène"
            Company magScene = new Company("Mag Scène", Company.CompanyType.OWN_COMPANY);
            System.out.println("Test 1 - Société Mag Scène créée");
            System.out.println("Est protégée: " + CompanyProtectionManager.isProtectedCompany(magScene));
            
            // Test 2: Création d'une société normale
            Company normalCompany = new Company("Sony", Company.CompanyType.MANUFACTURER);
            System.out.println("\nTest 2 - Société Sony créée");
            System.out.println("Est protégée: " + CompanyProtectionManager.isProtectedCompany(normalCompany));
            
            // Test 3: Test de validation de modification
            System.out.println("\n=== Test de validation de modifications ===");
            
            try {
                CompanyProtectionManager.validateCompanyModification(magScene);
                System.out.println("Modification de Mag Scène autorisée (ne devrait pas arriver hors préférences)");
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                System.out.println("Modification de Mag Scène bloquée: " + e.getMessage());
            }
            
            try {
                CompanyProtectionManager.validateCompanyModification(normalCompany);
                System.out.println("Modification de Sony autorisée");
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                System.out.println("Modification de Sony bloquée: " + e.getMessage());
            }
            
            // Test 4: Test de validation de suppression
            System.out.println("\n=== Test de validation de suppressions ===");
            
            try {
                CompanyProtectionManager.validateCompanyDeletion(magScene);
                System.out.println("Suppression de Mag Scène autorisée (ne devrait jamais arriver)");
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                System.out.println("Suppression de Mag Scène bloquée: " + e.getMessage());
            }
            
            try {
                CompanyProtectionManager.validateCompanyDeletion(normalCompany);
                System.out.println("Suppression de Sony autorisée");
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                System.out.println("Suppression de Sony bloquée: " + e.getMessage());
            }
            
            System.out.println("\n=== Test terminé ===");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}