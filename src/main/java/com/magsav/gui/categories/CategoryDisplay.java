package com.magsav.gui.categories;

/**
 * Classe pour l'affichage des catégories dans la TableView
 */
public class CategoryDisplay {
    private final long id;
    private final String hierarchie;
    private final String nom;
    private final String type;
    private final String parent;

    public CategoryDisplay(long id, String hierarchie, String nom, String type, String parent) {
        this.id = id;
        this.hierarchie = hierarchie;
        this.nom = nom;
        this.type = type;
        this.parent = parent;
    }

    public long getId() { return id; }
    public String getHierarchie() { return hierarchie; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getParent() { return parent; }
}