package com.magscene.magsav.desktop.core.search;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Gestionnaire de recherche globale pour MAGSAV 3.0
 * 
 * Permet de rechercher dans tous les modules :
 * - Équipements, SAV, Clients, Contrats, Ventes, Véhicules, Personnel, Planning
 * 
 * @version 3.0.0-refactored
 */
public class GlobalSearchManager {
    
    private static GlobalSearchManager instance;
    
    private final StringProperty searchTermProperty = new SimpleStringProperty("");
    private final List<Consumer<String>> searchListeners = new CopyOnWriteArrayList<>();
    private final List<SearchProvider> searchProviders = new CopyOnWriteArrayList<>();
    
    private GlobalSearchManager() {
        // Singleton
    }
    
    public static GlobalSearchManager getInstance() {
        if (instance == null) {
            instance = new GlobalSearchManager();
        }
        return instance;
    }
    
    /**
     * Enregistre un fournisseur de recherche (module)
     */
    public void registerSearchProvider(SearchProvider provider) {
        searchProviders.add(provider);
    }
    
    /**
     * Supprime un fournisseur de recherche
     */
    public void unregisterSearchProvider(SearchProvider provider) {
        searchProviders.remove(provider);
    }
    
    /**
     * Effectue une recherche globale
     */
    public void performGlobalSearch(String searchTerm) {
        searchTermProperty.set(searchTerm);
        
        // Notifier tous les écouteurs
        searchListeners.forEach(listener -> listener.accept(searchTerm));
        
        // Notifier tous les fournisseurs de recherche
        searchProviders.forEach(provider -> provider.performSearch(searchTerm));
    }
    
    /**
     * Ajoute un écouteur de recherche
     */
    public void addSearchListener(Consumer<String> listener) {
        searchListeners.add(listener);
    }
    
    /**
     * Supprime un écouteur de recherche
     */
    public void removeSearchListener(Consumer<String> listener) {
        searchListeners.remove(listener);
    }
    
    /**
     * Obtient le terme de recherche actuel
     */
    public String getCurrentSearchTerm() {
        return searchTermProperty.get();
    }
    
    /**
     * Propriété observable du terme de recherche
     */
    public StringProperty searchTermProperty() {
        return searchTermProperty;
    }
    
    /**
     * Efface la recherche actuelle
     */
    public void clearSearch() {
        performGlobalSearch("");
    }
    
    /**
     * Obtient tous les fournisseurs de recherche enregistrés
     */
    public List<SearchProvider> getSearchProviders() {
        return List.copyOf(searchProviders);
    }
    
    /**
     * Obtient le nombre de résultats pour tous les modules
     */
    public int getTotalResultCount() {
        return searchProviders.stream()
                .mapToInt(SearchProvider::getLastResultCount)
                .sum();
    }
    
    /**
     * Obtient les résultats groupés par module
     * @return Map avec le nom du module comme clé et la liste des résultats
     */
    public java.util.Map<String, java.util.List<SearchProvider.SearchResult>> getResultsByModule() {
        java.util.Map<String, java.util.List<SearchProvider.SearchResult>> results = new java.util.LinkedHashMap<>();
        
        for (SearchProvider provider : searchProviders) {
            if (provider.getLastResultCount() > 0) {
                java.util.List<SearchProvider.SearchResult> moduleResults = provider.getLastResults();
                if (!moduleResults.isEmpty()) {
                    results.put(provider.getModuleName(), moduleResults);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Obtient un résumé des résultats par module (nom du module -> nombre de résultats)
     */
    public java.util.Map<String, Integer> getResultCountByModule() {
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        
        for (SearchProvider provider : searchProviders) {
            int count = provider.getLastResultCount();
            if (count > 0) {
                counts.put(provider.getModuleName(), count);
            }
        }
        
        return counts;
    }
}