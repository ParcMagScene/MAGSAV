package com.magsav.dto;

/**
 * DTO représentant un élément de l'arborescence des catégories
 */
public class CategoryTreeItem {
    private final Long id;
    private final String nom;
    private final String description;
    private final Long parentId;
    private final int nbProduits;
    private final String dateCreation;
    private boolean hasChildren;
    
    public CategoryTreeItem(Long id, String nom, String description, Long parentId, int nbProduits, String dateCreation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.parentId = parentId;
        this.nbProduits = nbProduits;
        this.dateCreation = dateCreation;
        this.hasChildren = false;
    }
    
    public String getDisplayText() {
        String products = nbProduits > 0 ? " (" + nbProduits + " produits)" : "";
        return nom + products;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public Long getParentId() { return parentId; }
    public int getNbProduits() { return nbProduits; }
    public String getDateCreation() { return dateCreation; }
    public boolean hasChildren() { return hasChildren; }
    public void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }
}