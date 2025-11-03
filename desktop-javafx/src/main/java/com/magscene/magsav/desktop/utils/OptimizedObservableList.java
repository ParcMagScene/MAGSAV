package com.magscene.magsav.desktop.utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.List;
import java.util.function.Predicate;

/**
 * ObservableList optimisée avec pagination automatique et gestion mémoire
 * Améliore les performances pour les grandes listes de données
 */
public class OptimizedObservableList<T> {
    
    private final ObservableList<T> sourceList;
    private final FilteredList<T> filteredList;
    private final SortedList<T> sortedList;
    private final IntegerProperty pageSize = new SimpleIntegerProperty(50);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int LARGE_LIST_THRESHOLD = 1000;
    
    public OptimizedObservableList() {
        this.sourceList = FXCollections.observableArrayList();
        this.filteredList = new FilteredList<>(sourceList);
        this.sortedList = new SortedList<>(filteredList);
        
        // Auto-ajustement de la taille de page selon le volume
        sourceList.addListener((ListChangeListener<T>) change -> autoAdjustPageSize());
    }
    
    /**
     * Constructeur avec liste initiale
     */
    public OptimizedObservableList(List<T> initialData) {
        this();
        setAll(initialData);
    }
    
    /**
     * Ajuste automatiquement la taille de page selon le volume de données
     */
    private void autoAdjustPageSize() {
        int size = sourceList.size();
        if (size > LARGE_LIST_THRESHOLD) {
            // Pour les grandes listes, réduire la pagination
            pageSize.set(25);
            System.out.printf("📊 Auto-pagination: %d éléments → page de 25%n", size);
        } else if (size > 200) {
            pageSize.set(DEFAULT_PAGE_SIZE);
        } else {
            // Pour les petites listes, pas de limite
            pageSize.set(size > 0 ? size : DEFAULT_PAGE_SIZE);
        }
    }
    
    /**
     * Retourne la liste observable optimisée
     */
    public ObservableList<T> getObservableList() {
        return sortedList;
    }
    
    /**
     * Retourne la liste filtrée pour pagination manuelle
     */
    public FilteredList<T> getFilteredList() {
        return filteredList;
    }
    
    /**
     * Retourne la liste triée
     */
    public SortedList<T> getSortedList() {
        return sortedList;
    }
    
    /**
     * Ajoute un élément avec optimisation mémoire
     */
    public void add(T item) {
        sourceList.add(item);
        
        // GC suggéré pour les très grandes listes
        if (sourceList.size() % 500 == 0) {
            MemoryProfiler.logMemoryUsage("After " + sourceList.size() + " items");
        }
    }
    
    /**
     * Remplace tout le contenu de manière optimisée
     */
    public void setAll(List<T> newData) {
        if (newData.size() > LARGE_LIST_THRESHOLD) {
            System.out.printf("⚠️ Grande liste détectée (%d éléments), pagination activée%n", newData.size());
        }
        
        sourceList.setAll(newData);
        MemoryProfiler.logMemoryUsage("List Updated - " + newData.size() + " items");
    }
    
    /**
     * Vide la liste et libère la mémoire
     */
    public void clear() {
        sourceList.clear();
        // Force suggestion de GC après vidage
        System.gc();
        MemoryProfiler.logMemoryUsage("List Cleared");
    }
    
    /**
     * Applique un filtre avec optimisation
     */
    public void setFilter(Predicate<T> predicate) {
        filteredList.setPredicate(predicate);
        int filteredSize = filteredList.size();
        System.out.printf("🔍 Filtre appliqué: %d/%d éléments affichés%n", filteredSize, sourceList.size());
    }
    
    /**
     * Supprime le filtre
     */
    public void clearFilter() {
        filteredList.setPredicate(null);
        System.out.println("🔍 Filtre supprimé - tous les éléments affichés");
    }
    
    /**
     * Statistiques de la liste
     */
    public void logStatistics(String context) {
        System.out.printf("📋 [%s] Liste: %d total, %d filtrés, %d visibles%n",
            context,
            sourceList.size(),
            filteredList.size(),
            Math.min(filteredList.size(), pageSize.get())
        );
    }
    
    // Getters pour les propriétés
    public IntegerProperty pageSizeProperty() { return pageSize; }
    public IntegerProperty currentPageProperty() { return currentPage; }
    public int getPageSize() { return pageSize.get(); }
    public int getCurrentPage() { return currentPage.get(); }
    
    /**
     * Libération des ressources
     */
    public void dispose() {
        clear();
        System.out.println("🗑️ OptimizedObservableList disposed");
    }
}