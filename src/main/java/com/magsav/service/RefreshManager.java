package com.magsav.service;

import com.magsav.util.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestionnaire centralisé pour le rafraîchissement des composants UI.
 * Ce service maintient une liste de tous les composants Refreshable et 
 * permet de les rafraîchir automatiquement après des changements de données.
 * 
 * Pattern Singleton pour garantir l'unicité du gestionnaire.
 */
public class RefreshManager {
    
    private static RefreshManager instance;
    
    // Utilisation de CopyOnWriteArrayList pour la thread-safety
    private final List<Refreshable> refreshableComponents = new CopyOnWriteArrayList<>();
    
    private RefreshManager() {
        AppLogger.info("🔄 RefreshManager initialisé");
    }
    
    /**
     * Récupère l'instance unique du RefreshManager.
     */
    public static synchronized RefreshManager getInstance() {
        if (instance == null) {
            instance = new RefreshManager();
        }
        return instance;
    }
    
    /**
     * Enregistre un composant pour qu'il soit automatiquement rafraîchi.
     * @param component le composant à enregistrer
     */
    public void registerRefreshable(Refreshable component) {
        if (component == null) {
            AppLogger.warn("Tentative d'enregistrement d'un composant null dans RefreshManager");
            return;
        }
        
        // Éviter les doublons
        if (!refreshableComponents.contains(component)) {
            refreshableComponents.add(component);
            AppLogger.info("📝 Composant enregistré pour rafraîchissement: " + component.getComponentName());
        } else {
            AppLogger.debug("Composant déjà enregistré: " + component.getComponentName());
        }
    }
    
    /**
     * Désenregistre un composant (utile lors de la fermeture d'onglets par exemple).
     * @param component le composant à désenregistrer
     */
    public void unregisterRefreshable(Refreshable component) {
        if (component != null && refreshableComponents.remove(component)) {
            AppLogger.info("🗑️ Composant désenregistré: " + component.getComponentName());
        }
    }
    
    /**
     * Rafraîchit tous les composants enregistrés.
     * Cette méthode est appelée après génération de données de test, 
     * invalidation de cache, etc.
     */
    public void refreshAll() {
        AppLogger.info("🔄 RefreshManager - Rafraîchissement global demandé");
        
        if (refreshableComponents.isEmpty()) {
            AppLogger.warn("Aucun composant enregistré pour rafraîchissement");
            return;
        }
        
        int successCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        for (Refreshable component : refreshableComponents) {
            try {
                if (component.isReadyForRefresh()) {
                    AppLogger.debug("🔄 Rafraîchissement: " + component.getComponentName());
                    component.refreshAllTables();
                    successCount++;
                } else {
                    AppLogger.debug("⏭️ Composant non prêt, ignoré: " + component.getComponentName());
                    skippedCount++;
                }
            } catch (Exception e) {
                AppLogger.error("❌ Erreur lors du rafraîchissement de " + component.getComponentName() + ": " + e.getMessage(), e);
                errorCount++;
            }
        }
        
        AppLogger.info("✅ RefreshManager - Rafraîchissement terminé: " + 
                      successCount + " réussis, " + 
                      skippedCount + " ignorés, " + 
                      errorCount + " erreurs");
    }
    
    /**
     * Rafraîchit seulement les composants spécifiés par nom.
     * @param componentNames noms des composants à rafraîchir
     */
    public void refreshSpecific(String... componentNames) {
        if (componentNames == null || componentNames.length == 0) {
            AppLogger.warn("Aucun nom de composant spécifié pour rafraîchissement sélectif");
            return;
        }
        
        AppLogger.info("🎯 RefreshManager - Rafraîchissement sélectif: " + String.join(", ", componentNames));
        
        List<String> targetNames = List.of(componentNames);
        int refreshedCount = 0;
        
        for (Refreshable component : refreshableComponents) {
            if (targetNames.contains(component.getComponentName())) {
                try {
                    if (component.isReadyForRefresh()) {
                        component.refreshAllTables();
                        refreshedCount++;
                        AppLogger.debug("✅ Rafraîchi: " + component.getComponentName());
                    } else {
                        AppLogger.debug("⏭️ Non prêt: " + component.getComponentName());
                    }
                } catch (Exception e) {
                    AppLogger.error("❌ Erreur rafraîchissement " + component.getComponentName() + ": " + e.getMessage(), e);
                }
            }
        }
        
        AppLogger.info("🎯 Rafraîchissement sélectif terminé: " + refreshedCount + " composants rafraîchis");
    }
    
    /**
     * Retourne la liste des composants enregistrés (pour debugging).
     */
    public List<String> getRegisteredComponents() {
        List<String> names = new ArrayList<>();
        for (Refreshable component : refreshableComponents) {
            names.add(component.getComponentName());
        }
        return names;
    }
    
    /**
     * Retourne le nombre de composants enregistrés.
     */
    public int getRegisteredCount() {
        return refreshableComponents.size();
    }
    
    /**
     * Vide tous les composants enregistrés (utile pour les tests ou reset).
     */
    public void clearAll() {
        int count = refreshableComponents.size();
        refreshableComponents.clear();
        AppLogger.info("🗑️ RefreshManager - Tous les composants désernregistrés (" + count + ")");
    }
}