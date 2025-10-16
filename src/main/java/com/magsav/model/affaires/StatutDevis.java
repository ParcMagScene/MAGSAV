package com.magsav.model.affaires;

/**
 * Énumération des statuts de devis
 */
public enum StatutDevis {
    BROUILLON("Brouillon", "#808080"),
    EN_ATTENTE("En attente", "#FFA500"),
    ENVOYE("Envoyé", "#4169E1"),
    ACCEPTE("Accepté", "#228B22"),
    REFUSE("Refusé", "#DC143C"),
    EXPIRE("Expiré", "#8B4513"),
    ANNULE("Annulé", "#696969");
    
    private final String libelle;
    private final String couleur;
    
    StatutDevis(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }
    
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }
}