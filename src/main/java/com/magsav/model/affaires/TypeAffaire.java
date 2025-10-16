package com.magsav.model.affaires;

/**
 * Énumération des types d'affaires
 */
public enum TypeAffaire {
    VENTE_MATERIEL("Vente de matériel"),
    MAINTENANCE("Contrat de maintenance"),
    FORMATION("Formation"),
    CONSEIL("Conseil/Audit"),
    PROJET("Projet sur mesure"),
    SAV("Service après-vente");
    
    private final String libelle;
    
    TypeAffaire(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() { return libelle; }
}