package com.magscene.magsav.desktop.core.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Gestionnaire des catégories pour MAGSAV 3.0
 * 
 * Gère les catégories pour tous les modules :
 * - Équipements, Clients, Projets, etc.
 * 
 * @version 3.0.0-refactored
 */
public class CategoryManager {
    
    private static CategoryManager instance;
    
    private final ObservableList<Category> equipmentCategories = FXCollections.observableArrayList();
    private final ObservableList<Category> clientCategories = FXCollections.observableArrayList();
    private final ObservableList<Category> projectCategories = FXCollections.observableArrayList();
    private final ObservableList<Category> savCategories = FXCollections.observableArrayList();
    
    private final List<Consumer<CategoryChangeEvent>> listeners = new CopyOnWriteArrayList<>();
    
    private CategoryManager() {
        initializeDefaultCategories();
    }
    
    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }
    
    /**
     * Initialise les catégories par défaut
     */
    private void initializeDefaultCategories() {
        // Catégories d'équipements
        equipmentCategories.addAll(List.of(
            new Category("Audio", "Équipements audio (micros, enceintes, consoles)", "#FF6B6B", true),
            new Category("Éclairage", "Équipements d'éclairage (projecteurs, LED, gradateurs)", "#4ECDC4", true),
            new Category("Vidéo", "Équipements vidéo (écrans, caméras, projecteurs)", "#45B7D1", true),
            new Category("Structure", "Structures scéniques (podiums, barres, trépieds)", "#96CEB4", true),
            new Category("Accessoires", "Accessoires divers (câbles, supports, housses)", "#FFEAA7", true),
            new Category("Informatique", "Équipements informatiques (ordinateurs, réseaux)", "#DDA0DD", true)
        ));
        
        // Catégories de clients
        clientCategories.addAll(List.of(
            new Category("Entreprise", "Clients entreprises", "#3498DB", true),
            new Category("Particulier", "Clients particuliers", "#E74C3C", true),
            new Category("Association", "Associations et organismes", "#2ECC71", true),
            new Category("Collectivité", "Collectivités territoriales", "#F39C12", true),
            new Category("Production", "Sociétés de production", "#9B59B6", true)
        ));
        
        // Catégories de projets
        projectCategories.addAll(List.of(
            new Category("Concert", "Concerts et spectacles musicaux", "#E91E63", true),
            new Category("Théâtre", "Pièces de théâtre", "#9C27B0", true),
            new Category("Conférence", "Conférences et séminaires", "#3F51B5", true),
            new Category("Mariage", "Mariages et cérémonies", "#FF9800", true),
            new Category("Festival", "Festivals et événements", "#4CAF50", true),
            new Category("Corporate", "Événements d'entreprise", "#607D8B", true)
        ));
        
        // Catégories SAV
        savCategories.addAll(List.of(
            new Category("Panne", "Pannes et dysfonctionnements", "#F44336", true),
            new Category("Maintenance", "Maintenance préventive", "#FF9800", true),
            new Category("Installation", "Installation d'équipements", "#4CAF50", true),
            new Category("Formation", "Formation utilisateur", "#2196F3", true),
            new Category("Conseil", "Conseil technique", "#9C27B0", true)
        ));
    }
    
    /**
     * Obtient les catégories d'équipements
     */
    public ObservableList<Category> getEquipmentCategories() {
        return FXCollections.unmodifiableObservableList(equipmentCategories);
    }
    
    /**
     * Obtient les catégories de clients
     */
    public ObservableList<Category> getClientCategories() {
        return FXCollections.unmodifiableObservableList(clientCategories);
    }
    
    /**
     * Obtient les catégories de projets
     */
    public ObservableList<Category> getProjectCategories() {
        return FXCollections.unmodifiableObservableList(projectCategories);
    }
    
    /**
     * Obtient les catégories SAV
     */
    public ObservableList<Category> getSavCategories() {
        return FXCollections.unmodifiableObservableList(savCategories);
    }
    
    /**
     * Ajoute une catégorie d'équipement
     */
    public void addEquipmentCategory(Category category) {
        equipmentCategories.add(category);
        notifyListeners(new CategoryChangeEvent(CategoryType.EQUIPMENT, CategoryAction.ADDED, category));
    }
    
    /**
     * Supprime une catégorie d'équipement
     */
    public boolean removeEquipmentCategory(Category category) {
        if (category.isSystemCategory()) {
            return false; // Ne pas supprimer les catégories système
        }
        boolean removed = equipmentCategories.remove(category);
        if (removed) {
            notifyListeners(new CategoryChangeEvent(CategoryType.EQUIPMENT, CategoryAction.REMOVED, category));
        }
        return removed;
    }
    
    /**
     * Modifie une catégorie d'équipement
     */
    public void updateEquipmentCategory(Category oldCategory, Category newCategory) {
        int index = equipmentCategories.indexOf(oldCategory);
        if (index >= 0) {
            equipmentCategories.set(index, newCategory);
            notifyListeners(new CategoryChangeEvent(CategoryType.EQUIPMENT, CategoryAction.UPDATED, newCategory));
        }
    }
    
    /**
     * Ajoute un écouteur de changements de catégories
     */
    public void addCategoryChangeListener(Consumer<CategoryChangeEvent> listener) {
        listeners.add(listener);
    }
    
    /**
     * Supprime un écouteur de changements de catégories
     */
    public void removeCategoryChangeListener(Consumer<CategoryChangeEvent> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifie tous les écouteurs d'un changement
     */
    private void notifyListeners(CategoryChangeEvent event) {
        listeners.forEach(listener -> listener.accept(event));
    }
    
    /**
     * Obtient toutes les catégories par type
     */
    public ObservableList<Category> getCategoriesByType(CategoryType type) {
        return switch (type) {
            case EQUIPMENT -> getEquipmentCategories();
            case CLIENT -> getClientCategories();
            case PROJECT -> getProjectCategories();
            case SAV -> getSavCategories();
        };
    }
    
    /**
     * Recherche une catégorie par nom dans un type donné
     */
    public Category findCategoryByName(CategoryType type, String name) {
        return getCategoriesByType(type).stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Exporte les catégories vers un format JSON (simulation)
     */
    public String exportCategoriesToJson() {
        // Simulation d'export JSON
        return "{\n" +
               "  \"equipment\": " + equipmentCategories.size() + " catégories,\n" +
               "  \"clients\": " + clientCategories.size() + " catégories,\n" +
               "  \"projects\": " + projectCategories.size() + " catégories,\n" +
               "  \"sav\": " + savCategories.size() + " catégories\n" +
               "}";
    }
}