package com.magscene.magsav.desktop.core.search;

/**
 * Interface pour les fournisseurs de recherche des modules MAGSAV
 * 
 * Chaque module peut implémenter cette interface pour fournir
 * des capacités de recherche au gestionnaire global
 * 
 * @version 3.0.0-refactored
 */
public interface SearchProvider {
    
    /**
     * Effectue une recherche dans le module
     * 
     * @param searchTerm terme de recherche
     */
    void performSearch(String searchTerm);
    
    /**
     * Obtient le nom du module
     * 
     * @return nom du module (ex: "Équipements", "SAV", etc.)
     */
    String getModuleName();
    
    /**
     * Obtient le nombre de résultats de la dernière recherche
     * 
     * @return nombre de résultats
     */
    int getLastResultCount();
    
    /**
     * Indique si le module supporte la recherche
     * 
     * @return true si la recherche est supportée
     */
    default boolean isSearchSupported() {
        return true;
    }
    
    /**
     * Obtient les types de données que le module peut rechercher
     * 
     * @return tableau des types (ex: ["nom", "référence", "description"])
     */
    default String[] getSearchableFields() {
        return new String[]{"nom", "description"};
    }
    
    /**
     * Efface les résultats de recherche du module
     */
    default void clearSearchResults() {
        performSearch("");
    }
}