package com.magscene.magsav.desktop.core.category;

/**
 * Classe représentant une catégorie MAGSAV
 * 
 * @version 3.0.0-refactored
 */
public class Category {
    
    private String name;
    private String description;
    private String color;
    private boolean systemCategory;
    
    public Category(String name, String description, String color, boolean systemCategory) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.systemCategory = systemCategory;
    }
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public boolean isSystemCategory() { return systemCategory; }
    public void setSystemCategory(boolean systemCategory) { this.systemCategory = systemCategory; }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return name.equals(category.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}