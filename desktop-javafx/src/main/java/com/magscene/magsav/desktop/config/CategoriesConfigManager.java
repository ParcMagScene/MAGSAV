package com.magscene.magsav.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Gestionnaire de configuration des catégories d'équipement
 * Permet la gestion centralisée des catégories hiérarchiques avec couleurs et icônes
 */
public class CategoriesConfigManager {
    
    private static final String CATEGORIES_KEY = "equipment.categories";
    private static final String CONFIG_FILE = "categories.properties";
    private static CategoriesConfigManager instance;
    
    private final Preferences prefs;
    private final ObservableList<CategoryItem> rootCategories;
    private final Properties configProperties;
    
    private CategoriesConfigManager() {
        this.prefs = Preferences.userNodeForPackage(CategoriesConfigManager.class);
        this.rootCategories = FXCollections.observableArrayList();
        this.configProperties = new Properties();
        loadConfiguration();
    }
    
    public static CategoriesConfigManager getInstance() {
        if (instance == null) {
            instance = new CategoriesConfigManager();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis les préférences et le fichier
     */
    private void loadConfiguration() {
        // Charger depuis le fichier de configuration local s'il existe
        loadFromConfigFile();
        
        // Charger depuis les préférences système ou valeurs par défaut
        String categoriesStr = prefs.get(CATEGORIES_KEY, getDefaultCategories());
        parseCategoriesFromString(categoriesStr);
    }
    
    /**
     * Charge la configuration depuis le fichier local
     */
    private void loadFromConfigFile() {
        Path configPath = getConfigFilePath();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                configProperties.load(input);
                
                String fileCategories = configProperties.getProperty(CATEGORIES_KEY);
                if (fileCategories != null && !fileCategories.isEmpty()) {
                    // Mise à jour des préférences avec les données du fichier
                    prefs.put(CATEGORIES_KEY, fileCategories);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement du fichier de configuration: " + e.getMessage());
            }
        }
    }
    
    /**
     * Parse les catégories depuis une chaîne formatée
     */
    private void parseCategoriesFromString(String categoriesStr) {
        rootCategories.clear();
        
        if (categoriesStr == null || categoriesStr.trim().isEmpty()) {
            return;
        }
        
        String[] categories = categoriesStr.split(";");
        for (String categoryData : categories) {
            CategoryItem item = CategoryItem.fromString(categoryData);
            if (item != null) {
                rootCategories.add(item);
            }
        }
        
        // Trier par ordre d'affichage
        rootCategories.sort(Comparator.comparing(CategoryItem::getDisplayOrder));
    }
    
    /**
     * Sauvegarde la configuration dans les préférences et le fichier
     */
    public void saveConfiguration() {
        String categoriesStr = categoriesToString();
        
        // Sauvegarder dans les préférences système
        prefs.put(CATEGORIES_KEY, categoriesStr);
        
        // Sauvegarder dans le fichier local
        configProperties.setProperty(CATEGORIES_KEY, categoriesStr);
        saveToConfigFile();
        
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }
    
    /**
     * Convertit les catégories en chaîne
     */
    private String categoriesToString() {
        return rootCategories.stream()
                .map(CategoryItem::toString)
                .reduce((a, b) -> a + ";" + b)
                .orElse("");
    }
    
    /**
     * Sauvegarde dans le fichier de configuration local
     */
    private void saveToConfigFile() {
        Path configPath = getConfigFilePath();
        try {
            // Créer le répertoire parent s'il n'existe pas
            Files.createDirectories(configPath.getParent());
            
            try (OutputStream output = Files.newOutputStream(configPath)) {
                configProperties.store(output, "Configuration des catégories d'équipement MAGSAV-3.0");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du fichier de configuration: " + e.getMessage());
        }
    }
    
    /**
     * Retourne le chemin vers le fichier de configuration
     */
    private Path getConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".magsav", CONFIG_FILE);
    }
    
    /**
     * Retourne les catégories par défaut
     */
    private String getDefaultCategories() {
        return "Éclairage:1:#FF6B35:lightbulb-outline;" +
               "Son:2:#4ECDC4:volume-high;" +
               "Vidéo:3:#45B7D1:video;" +
               "Structure:4:#96CEB4:hammer-wrench;" +
               "Transport:5:#FECA57:truck";
    }
    
    /**
     * Retourne la liste observable des catégories racines
     */
    public ObservableList<CategoryItem> getRootCategories() {
        return rootCategories;
    }
    
    /**
     * Retourne toutes les catégories (racines + sous-catégories) sous forme de liste plate
     */
    public List<CategoryItem> getAllCategories() {
        List<CategoryItem> allCategories = new ArrayList<>();
        for (CategoryItem root : rootCategories) {
            allCategories.add(root);
            addSubCategories(root, allCategories);
        }
        return allCategories;
    }
    
    private void addSubCategories(CategoryItem parent, List<CategoryItem> allCategories) {
        for (CategoryItem child : parent.getSubCategories()) {
            allCategories.add(child);
            addSubCategories(child, allCategories);
        }
    }
    
    /**
     * Ajoute une nouvelle catégorie racine
     */
    public boolean addRootCategory(String name, String color, String icon) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        if (findCategoryByName(trimmedName) != null) {
            return false; // Déjà existante
        }
        
        int nextOrder = rootCategories.size() + 1;
        CategoryItem newCategory = new CategoryItem(trimmedName, nextOrder, color, icon);
        rootCategories.add(newCategory);
        rootCategories.sort(Comparator.comparing(CategoryItem::getDisplayOrder));
        saveConfiguration();
        return true;
    }
    
    /**
     * Ajoute une sous-catégorie
     */
    public boolean addSubCategory(CategoryItem parent, String name, String color, String icon) {
        if (name == null || name.trim().isEmpty() || parent == null) {
            return false;
        }
        
        String trimmedName = name.trim();
        if (findCategoryByName(trimmedName) != null) {
            return false; // Déjà existante
        }
        
        int nextOrder = parent.getSubCategories().size() + 1;
        CategoryItem newSubCategory = new CategoryItem(trimmedName, nextOrder, color, icon);
        parent.addSubCategory(newSubCategory);
        saveConfiguration();
        return true;
    }
    
    /**
     * Supprime une catégorie
     */
    public boolean removeCategory(CategoryItem category) {
        if (category == null) return false;
        
        // Supprimer des racines si c'est une catégorie racine
        if (rootCategories.remove(category)) {
            saveConfiguration();
            return true;
        }
        
        // Chercher dans les sous-catégories
        for (CategoryItem root : rootCategories) {
            if (removeCategoryRecursive(root, category)) {
                saveConfiguration();
                return true;
            }
        }
        
        return false;
    }
    
    private boolean removeCategoryRecursive(CategoryItem parent, CategoryItem toRemove) {
        if (parent.getSubCategories().remove(toRemove)) {
            return true;
        }
        
        for (CategoryItem child : parent.getSubCategories()) {
            if (removeCategoryRecursive(child, toRemove)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Modifie une catégorie existante
     */
    public boolean updateCategory(CategoryItem category, String newName, String newColor, String newIcon) {
        if (category == null || newName == null || newName.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = newName.trim();
        
        // Vérifier que le nouveau nom n'existe pas déjà (sauf si c'est la même catégorie)
        CategoryItem existing = findCategoryByName(trimmedName);
        if (existing != null && existing != category) {
            return false;
        }
        
        category.setName(trimmedName);
        category.setColor(newColor);
        category.setIcon(newIcon);
        saveConfiguration();
        return true;
    }
    
    /**
     * Trouve une catégorie par nom (recherche récursive)
     */
    public CategoryItem findCategoryByName(String name) {
        for (CategoryItem root : rootCategories) {
            if (root.getName().equals(name)) {
                return root;
            }
            
            CategoryItem found = findCategoryByNameRecursive(root, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    private CategoryItem findCategoryByNameRecursive(CategoryItem parent, String name) {
        for (CategoryItem child : parent.getSubCategories()) {
            if (child.getName().equals(name)) {
                return child;
            }
            
            CategoryItem found = findCategoryByNameRecursive(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Réinitialise les catégories aux valeurs par défaut
     */
    public void resetToDefaults() {
        rootCategories.clear();
        parseCategoriesFromString(getDefaultCategories());
        saveConfiguration();
    }
    
    /**
     * Importe des catégories depuis une liste
     */
    public void importCategories(List<CategoryItem> categories) {
        rootCategories.clear();
        rootCategories.addAll(categories);
        rootCategories.sort(Comparator.comparing(CategoryItem::getDisplayOrder));
        saveConfiguration();
    }
    
    /**
     * Exporte les catégories actuelles
     */
    public List<CategoryItem> exportCategories() {
        return new ArrayList<>(rootCategories);
    }
    
    /**
     * Classe représentant une catégorie avec hiérarchie
     */
    public static class CategoryItem {
        private String name;
        private int displayOrder;
        private String color;
        private String icon;
        private final ObservableList<CategoryItem> subCategories;
        
        public CategoryItem(String name, int displayOrder, String color, String icon) {
            this.name = name;
            this.displayOrder = displayOrder;
            this.color = color != null ? color : "#007bff";
            this.icon = icon != null ? icon : "folder";
            this.subCategories = FXCollections.observableArrayList();
        }
        
        // Getters et Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public ObservableList<CategoryItem> getSubCategories() { return subCategories; }
        
        public void addSubCategory(CategoryItem subCategory) {
            subCategories.add(subCategory);
            subCategories.sort(Comparator.comparing(CategoryItem::getDisplayOrder));
        }
        
        /**
         * Obtient le chemin complet de la catégorie (ex: "Éclairage > Projecteurs > LED")
         */
        public String getFullPath() {
            return name; // Pour l'instant, juste le nom (peut être étendu pour la hiérarchie)
        }
        
        /**
         * Sérialise la catégorie en chaîne
         */
        @Override
        public String toString() {
            return name + ":" + displayOrder + ":" + color + ":" + icon;
        }
        
        /**
         * Désérialise une catégorie depuis une chaîne
         */
        public static CategoryItem fromString(String categoryStr) {
            if (categoryStr == null || categoryStr.trim().isEmpty()) {
                return null;
            }
            
            String[] parts = categoryStr.split(":");
            if (parts.length >= 2) {
                String name = parts[0];
                try {
                    int order = Integer.parseInt(parts[1]);
                    String color = parts.length > 2 ? parts[2] : "#007bff";
                    String icon = parts.length > 3 ? parts[3] : "folder";
                    
                    return new CategoryItem(name, order, color, icon);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            
            return null;
        }
    }
}