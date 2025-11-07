// Test simple pour vérifier les données d'équipement
// Ce fichier peut être supprimé après les tests

package com.magscene.magsav.desktop.test;

import com.magscene.magsav.desktop.service.ApiService;
import java.util.List;

public class EquipmentDataTest {
    public static void main(String[] args) {
        ApiService apiService = new ApiService();
        apiService.getEquipments().thenAccept(equipments -> {
            System.out.println("=== TEST DONNÉES ÉQUIPEMENT ===");
            System.out.println("Nombre d'équipements: " + equipments.size());
            for (Object equipment : equipments) {
                if (equipment instanceof java.util.Map<?, ?> map) {
                    String name = (String) map.get("name");
                    String category = (String) map.get("category");
                    System.out.println("- " + name + " (" + category + ")");
                    
                    if (name != null && name.contains("Yamaha")) {
                        System.out.println("  ✅ TROUVÉ: " + name);
                    }
                }
            }
            System.out.println("=== FIN TEST ===");
        });
        
        // Attendre un peu pour que l'opération asynchrone se termine
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}