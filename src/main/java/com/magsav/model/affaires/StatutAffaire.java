package com.magsav.model.affaires;

/**
 * Énumération des statuts d'affaires
 */
public enum StatutAffaire {
    PROSPECTION("Prospection", "#FFA500"),
    QUALIFIEE("Qualifiée", "#4169E1"),
    EN_COURS("En cours", "#32CD32"),
    NEGOCIE("Négocié", "#FF6347"),
    GAGNEE("Gagnée", "#228B22"),
    PERDUE("Perdue", "#DC143C"),
    ANNULEE("Annulée", "#808080");
    
    private final String libelle;
    private final String couleur;
    
    StatutAffaire(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }
    
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }
}