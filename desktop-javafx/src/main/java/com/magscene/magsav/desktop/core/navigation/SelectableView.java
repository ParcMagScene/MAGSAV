package com.magscene.magsav.desktop.core.navigation;

/**
 * Interface pour les vues qui supportent la sélection d'un élément par ID
 * Utilisé par la recherche globale pour naviguer et sélectionner un résultat
 */
public interface SelectableView {
    
    /**
     * Sélectionne un élément dans la vue par son ID
     * @param id L'identifiant de l'élément à sélectionner
     * @return true si l'élément a été trouvé et sélectionné, false sinon
     */
    boolean selectById(String id);
    
    /**
     * Retourne le nom de la vue pour les logs
     */
    default String getViewName() {
        return getClass().getSimpleName();
    }
}
