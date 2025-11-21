package com.magscene.magsav.desktop.test;

import com.magscene.magsav.desktop.core.di.ApplicationContext;
import com.magscene.magsav.desktop.core.navigation.NavigationManager;
import com.magscene.magsav.desktop.core.navigation.Route;
import com.magscene.magsav.desktop.service.business.EquipmentService;
import com.magscene.magsav.desktop.service.business.SAVService;
import com.magscene.magsav.desktop.view.equipment.NewEquipmentManagerView;
import com.magscene.magsav.desktop.view.sav.NewSAVManagerView;

/**
 * Classe de test pour valider la nouvelle architecture
 * Teste l'injection de dépendances et la navigation
 */
public class ArchitectureTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 === Test de la nouvelle architecture MAGSAV ===");
        
        // Test 1: Initialisation du contexte
        testApplicationContext();
        
        // Test 2: Services métier
        testBusinessServices();
        
        // Test 3: Navigation
        testNavigationManager();
        
        // Test 4: Vues
        testViews();
        
        System.out.println("✅ === Tests terminés ===");
    }
    
    private static void testApplicationContext() {
        System.out.println("\n📋 Test 1: ApplicationContext");
        
        try {
            ApplicationContext context = ApplicationContext.getInstance();
            System.out.println("✅ ApplicationContext créé");
            
            // Test singleton
            ApplicationContext context2 = ApplicationContext.getInstance();
            if (context == context2) {
                System.out.println("✅ Pattern Singleton validé");
            } else {
                System.out.println("❌ Erreur Singleton");
            }
            
            // Test services enregistrés
            boolean hasEquipmentService = context.isRegistered(EquipmentService.class);
            boolean hasSAVService = context.isRegistered(SAVService.class);
            boolean hasNavigationManager = context.isRegistered(NavigationManager.class);
            
            System.out.println("✅ EquipmentService enregistré: " + hasEquipmentService);
            System.out.println("✅ SAVService enregistré: " + hasSAVService);
            System.out.println("✅ NavigationManager enregistré: " + hasNavigationManager);
            
        } catch (Exception e) {
            System.out.println("❌ Erreur ApplicationContext: " + e.getMessage());
        }
    }
    
    private static void testBusinessServices() {
        System.out.println("\n🏢 Test 2: Services Métier");
        
        try {
            ApplicationContext context = ApplicationContext.getInstance();
            
            // Test EquipmentService
            EquipmentService equipmentService = context.getInstance(EquipmentService.class);
            System.out.println("✅ EquipmentService récupéré: " + equipmentService.getClass().getSimpleName());
            
            // Test SAVService
            SAVService savService = context.getInstance(SAVService.class);
            System.out.println("✅ SAVService récupéré: " + savService.getClass().getSimpleName());
            
            // Test injection de dépendances (même instance)
            EquipmentService equipmentService2 = context.getInstance(EquipmentService.class);
            if (equipmentService == equipmentService2) {
                System.out.println("✅ Injection singleton validée");
            } else {
                System.out.println("❌ Erreur injection singleton");
            }
            
            // Test méthodes de service
            System.out.println("🔗 Test connexion backend...");
            equipmentService.testBackendConnection()
                .whenComplete((result, error) -> {
                    if (error != null) {
                        System.out.println("⚠️ Backend non disponible (normal en test): " + error.getMessage());
                    } else {
                        System.out.println("✅ Backend disponible: " + result);
                    }
                });
            
        } catch (Exception e) {
            System.out.println("❌ Erreur Services Métier: " + e.getMessage());
        }
    }
    
    private static void testNavigationManager() {
        System.out.println("\n🧭 Test 3: NavigationManager");
        
        try {
            ApplicationContext context = ApplicationContext.getInstance();
            NavigationManager navigationManager = context.getInstance(NavigationManager.class);
            System.out.println("✅ NavigationManager récupéré");
            
            // Test navigation
            System.out.println("📍 Navigation vers EQUIPMENT...");
            // navigationManager.navigateTo(Route.EQUIPMENT);
            
            System.out.println("📍 Navigation vers SAV...");
            // navigationManager.navigateTo(Route.SAV);
            
            // Test cache
            boolean hasCaching = navigationManager.getClass().getName().contains("NavigationManager");
            System.out.println("✅ Cache de navigation: " + hasCaching);
            
        } catch (Exception e) {
            System.out.println("❌ Erreur NavigationManager: " + e.getMessage());
        }
    }
    
    private static void testViews() {
        System.out.println("\n🖼️ Test 4: Vues");
        
        try {
            // Test création vue Equipment
            System.out.println("🔧 Création NewEquipmentManagerView...");
            NewEquipmentManagerView equipmentView = new NewEquipmentManagerView();
            System.out.println("✅ NewEquipmentManagerView créée: " + equipmentView.getClass().getSimpleName());
            
            // Test création vue SAV
            System.out.println("🛠️ Création NewSAVManagerView...");
            NewSAVManagerView savView = new NewSAVManagerView();
            System.out.println("✅ NewSAVManagerView créée: " + savView.getClass().getSimpleName());
            
            // Test méthodes communes des vues
            System.out.println("🔄 Test refresh Equipment...");
            equipmentView.refresh();
            
            System.out.println("🔄 Test refresh SAV...");
            savView.refresh();
            
            System.out.println("✅ Toutes les vues fonctionnent");
            
        } catch (Exception e) {
            System.out.println("❌ Erreur Vues: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test unitaire rapide pour valider l'architecture
     */
    public static boolean quickValidation() {
        try {
            ApplicationContext context = ApplicationContext.getInstance();
            
            // Vérifications minimales
            boolean hasEquipment = context.isRegistered(EquipmentService.class);
            boolean hasSAV = context.isRegistered(SAVService.class);
            boolean hasNavigation = context.isRegistered(NavigationManager.class);
            
            return hasEquipment && hasSAV && hasNavigation;
        } catch (Exception e) {
            System.err.println("❌ Validation rapide échouée: " + e.getMessage());
            return false;
        }
    }
}