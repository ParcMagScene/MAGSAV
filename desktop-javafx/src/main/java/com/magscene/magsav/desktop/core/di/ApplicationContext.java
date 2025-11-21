package com.magscene.magsav.desktop.core.di;

import com.magscene.magsav.desktop.core.navigation.NavigationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Conteneur d'injection de dépendances simple pour l'application JavaFX
 * Implémente le pattern Service Locator avec injection automatique
 */
public class ApplicationContext {
    private static ApplicationContext instance;
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    
    private ApplicationContext() {
        registerDefaultServices();
    }
    
    /**
     * Enregistre tous les services par défaut de l'application
     */
    private void registerDefaultServices() {
        // Services de navigation
        registerFactory(NavigationManager.class, () -> NavigationManager.getInstance());
        
        // API Clients
        registerFactory(
            com.magscene.magsav.desktop.service.api.EquipmentApiClient.class, 
            () -> new com.magscene.magsav.desktop.service.api.EquipmentApiClient()
        );
        registerFactory(
            com.magscene.magsav.desktop.service.api.SAVApiClient.class, 
            () -> new com.magscene.magsav.desktop.service.api.SAVApiClient()
        );
        
        // Services métier avec injection des API clients
        registerFactory(
            com.magscene.magsav.desktop.service.business.EquipmentService.class, 
            () -> new com.magscene.magsav.desktop.service.business.EquipmentService(
                getInstance(com.magscene.magsav.desktop.service.api.EquipmentApiClient.class)
            )
        );
        registerFactory(
            com.magscene.magsav.desktop.service.business.SAVService.class, 
            () -> new com.magscene.magsav.desktop.service.business.SAVService(
                getInstance(com.magscene.magsav.desktop.service.api.SAVApiClient.class)
            )
        );
        
        System.out.println("🏗️ ApplicationContext initialisé avec services:");
        System.out.println("  📡 API Clients: Equipment, SAV");
        System.out.println("  🏢 Business Services: Equipment, SAV"); 
        System.out.println("  🧭 Navigation: NavigationManager");
    }
    
    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }
    
    /**
     * Enregistre un singleton
     */
    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
    }
    
    /**
     * Enregistre une factory pour création à la demande
     */
    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }
    
    /**
     * Récupère une instance (singleton ou nouvelle instance)
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> type) {
        // Vérifier d'abord les singletons
        Object singleton = singletons.get(type);
        if (singleton != null) {
            return (T) singleton;
        }
        
        // Puis les factories
        Supplier<?> factory = factories.get(type);
        if (factory != null) {
            return (T) factory.get();
        }
        
        throw new IllegalArgumentException("No registration found for type: " + type.getName());
    }
    
    /**
     * Vérifie si un type est enregistré
     */
    public boolean isRegistered(Class<?> type) {
        return singletons.containsKey(type) || factories.containsKey(type);
    }
    
    /**
     * Nettoie le contexte
     */
    public void clear() {
        singletons.clear();
        factories.clear();
    }
}