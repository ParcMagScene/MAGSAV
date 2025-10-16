package com.magsav.model.affaires;

/**
 * Énumération des priorités d'affaires
 */
public enum PrioriteAffaire {
    FAIBLE("Faible", "#90EE90"),
    NORMALE("Normale", "#87CEEB"),
    HAUTE("Haute", "#FFA500"),
    CRITIQUE("Critique", "#FF4500");
    
    private final String libelle;
    private final String couleur;
    
    PrioriteAffaire(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }
    
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }
}