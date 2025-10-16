package com.magsav.service;

/**
 * Interface pour les composants qui peuvent être rafraîchis après des changements de données.
 * Cette interface standardise le rafraîchissement des contrôleurs UI.
 */
public interface Refreshable {
    
    /**
     * Rafraîchit toutes les données affichées par ce composant.
     * Cette méthode est appelée automatiquement après génération de données de test,
     * changements de cache, ou autres modifications importantes des données.
     */
    void refreshAllTables();
    
    /**
     * Retourne un nom descriptif pour ce composant, utilisé pour le logging et debugging.
     * @return nom du composant (ex: "DemandesController", "InterventionsController")
     */
    String getComponentName();
    
    /**
     * Indique si ce composant est actuellement prêt à être rafraîchi.
     * @return true si le composant peut être rafraîchi, false si par exemple
     *         les tables ne sont pas encore initialisées
     */
    default boolean isReadyForRefresh() {
        return true;
    }
}