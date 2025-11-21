package com.magscene.magsav.desktop.core.navigation;

import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gestionnaire de navigation centralisé pour l'application
 * Implémente le pattern Observer pour les changements de vue
 */
public class NavigationManager {
    private static NavigationManager instance;
    private final Map<Route, Supplier<Pane>> viewFactories = new HashMap<>();
    private final Map<Route, Pane> cachedViews = new HashMap<>();
    private Consumer<Pane> viewChangeHandler;
    private Route currentRoute;
    
    private NavigationManager() {}
    
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    /**
     * Enregistre une factory de vue pour une route
     */
    public void registerView(Route route, Supplier<Pane> viewFactory) {
        viewFactories.put(route, viewFactory);
    }
    
    /**
     * Définit le handler pour les changements de vue
     */
    public void setViewChangeHandler(Consumer<Pane> handler) {
        this.viewChangeHandler = handler;
    }
    
    /**
     * Navigue vers une route
     */
    public void navigateTo(Route route) {
        if (route == currentRoute) {
            return; // Déjà sur cette route
        }
        
        Pane view = getOrCreateView(route);
        if (view != null && viewChangeHandler != null) {
            currentRoute = route;
            viewChangeHandler.accept(view);
            
            // Émettre l'événement de navigation
            NavigationEvent event = new NavigationEvent(route, view);
            notifyNavigationListeners(event);
        }
    }
    
    /**
     * Récupère une vue depuis le cache ou la crée
     */
    private Pane getOrCreateView(Route route) {
        // Vérifier le cache d'abord
        Pane cached = cachedViews.get(route);
        if (cached != null) {
            System.out.println("⚡ Vue récupérée du cache: " + route);
            return cached;
        }
        
        // Créer une nouvelle vue
        Supplier<Pane> factory = viewFactories.get(route);
        if (factory != null) {
            Pane newView = factory.get();
            cachedViews.put(route, newView);
            System.out.println("✓ Nouvelle vue créée: " + route);
            return newView;
        }
        
        System.err.println("❌ Aucune factory trouvée pour la route: " + route);
        return null;
    }
    
    /**
     * Vide le cache d'une vue spécifique
     */
    public void clearViewCache(Route route) {
        cachedViews.remove(route);
    }
    
    /**
     * Vide tout le cache
     */
    public void clearAllCache() {
        cachedViews.clear();
    }
    
    /**
     * Récupère la route actuelle
     */
    public Route getCurrentRoute() {
        return currentRoute;
    }
    
    private void notifyNavigationListeners(NavigationEvent event) {
        // Pour l'instant, juste un log. À étendre avec un vrai système d'événements
        System.out.println("📍 Navigation: " + event.getRoute());
    }
}