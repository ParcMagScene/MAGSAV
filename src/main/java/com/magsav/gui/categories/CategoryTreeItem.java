package com.magsav.gui.categories;

import com.magsav.model.Category;
import javafx.scene.control.TreeItem;

/**
 * Élément d'arbre personnalisé pour représenter une catégorie dans la TreeView.
 * Contient une référence vers l'objet Category sous-jacent.
 */
public class CategoryTreeItem extends TreeItem<String> {
    
    private final Category category;
    
    public CategoryTreeItem(Category category) {
        super(category.nom());
        this.category = category;
        setExpanded(true); // Expand par défaut pour voir la hiérarchie
    }
    
    /**
     * @return La catégorie associée à cet élément d'arbre
     */
    public Category getCategory() {
        return category;
    }
    
    /**
     * @return true si c'est une catégorie principale (sans parent)
     */
    public boolean isMainCategory() {
        return category.parentId() == null;
    }
    
    /**
     * @return true si c'est une sous-catégorie (avec parent)
     */
    public boolean isSubcategory() {
        return category.parentId() != null;
    }
    
    /**
     * Trouve un élément enfant par ID de catégorie
     */
    public CategoryTreeItem findChild(long categoryId) {
        for (TreeItem<String> child : getChildren()) {
            if (child instanceof CategoryTreeItem) {
                CategoryTreeItem categoryChild = (CategoryTreeItem) child;
                if (categoryChild.getCategory().id() == categoryId) {
                    return categoryChild;
                }
            }
        }
        return null;
    }
    
    /**
     * Ajoute une sous-catégorie comme enfant
     */
    public void addSubcategory(Category subcategory) {
        CategoryTreeItem childItem = new CategoryTreeItem(subcategory);
        getChildren().add(childItem);
    }
    
    /**
     * Supprime une sous-catégorie enfant
     */
    public boolean removeSubcategory(long subcategoryId) {
        CategoryTreeItem toRemove = findChild(subcategoryId);
        if (toRemove != null) {
            getChildren().remove(toRemove);
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return category.nom() + " (ID: " + category.id() + ")";
    }
}