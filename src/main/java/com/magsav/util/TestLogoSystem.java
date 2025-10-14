package com.magsav.util;

/**
 * Test simple pour vérifier le fonctionnement du système de logos GIF
 */
public class TestLogoSystem {
    
    public static void main(String[] args) {
        System.out.println("=== Test du système de logos GIF Mag Scène ===");
        
        // Vérifier l'existence du répertoire
        GifLogoManager.ensureLogoDirectoryExists();
        
        // Vérifier si le logo est disponible
        boolean logoExists = GifLogoManager.isMagSceneLogoAvailable();
        System.out.println("Logo GIF disponible: " + logoExists);
        
        if (logoExists) {
            // Tester la création d'icône
            try {
                javafx.scene.image.Image icon16 = GifLogoManager.createMagSceneListIcon(16);
                javafx.scene.image.Image icon32 = GifLogoManager.createMagSceneListIcon(32);
                
                System.out.println("Icône 16x16 créée: " + (icon16 != null));
                System.out.println("Icône 32x32 créée: " + (icon32 != null));
                
                if (icon16 != null) {
                    System.out.println("Dimensions icône 16: " + icon16.getWidth() + "x" + icon16.getHeight());
                }
                
            } catch (Exception e) {
                System.out.println("Erreur lors de la création des icônes: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Placez votre logo GIF animé dans: data/logos/mag_scene_logo.gif");
        }
        
        // Test du cache
        GifLogoManager.clearIconCache();
        System.out.println("Cache des icônes vidé");
        
        System.out.println("=== Fin du test ===");
    }
}