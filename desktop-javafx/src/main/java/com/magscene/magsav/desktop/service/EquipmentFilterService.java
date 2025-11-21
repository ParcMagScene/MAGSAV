package com.magscene.magsav.desktop.service;

import javafx.collections.transformation.FilteredList;

/**
 * Service pour gérer les filtres spéciaux des équipements LOCMAT
 */
public class EquipmentFilterService {
    
    /**
     * Filtre pour afficher les équipements non sérialisés
     */
    public static void filterNonSerialized(FilteredList<?> filteredList) {
        // TODO: Implémenter le filtre pour les équipements non sérialisés; // Chercher les équipements avec serialNumber commençant par "NON-SERIALISE-"
    }
    
    /**
     * Filtre pour afficher uniquement les équipements appartenant à Mag Scène
     */
    public static void filterMagSceneOnly(FilteredList<?> filteredList) {
        // TODO: Implémenter le filtre pour les équipements Mag Scène; // Chercher dans les notes ou propriétaire "Mag Scene" ou "Mag Scène"
    }
    
    /**
     * Filtre pour afficher les équipements importés de LOCMAT
     */
    public static void filterLocmatImported(FilteredList<?> filteredList) {
        // TODO: Implémenter le filtre pour les équipements LOCMAT; // Chercher dans les notes "IMPORT LOCMAT" ou internalReference avec format LOCMAT
    }
}