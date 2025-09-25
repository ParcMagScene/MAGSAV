package com.magsav.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DossierSAV(
    Long id,
    String produit,
    String numeroSerie,
    String proprietaire,
    String panne,
    String statut,
    String detecteur,
    LocalDate dateEntree,
    LocalDate dateSortie,
    LocalDateTime createdAt
) {
    public DossierSAV withId(Long id) {
        return new DossierSAV(id, produit, numeroSerie, proprietaire, panne, statut, detecteur, dateEntree, dateSortie, createdAt);
    }
    
    public DossierSAV withStatut(String statut) {
        return new DossierSAV(id, produit, numeroSerie, proprietaire, panne, statut, detecteur, dateEntree, dateSortie, createdAt);
    }
    
    public DossierSAV withDateSortie(LocalDate dateSortie) {
        return new DossierSAV(id, produit, numeroSerie, proprietaire, panne, statut, detecteur, dateEntree, dateSortie, createdAt);
    }
    
    // Constructeur pour création sans dates
    public static DossierSAV nouveau(String produit, String numeroSerie, String proprietaire, String panne, String detecteur) {
        return new DossierSAV(null, produit, numeroSerie, proprietaire, panne, "recu", detecteur, LocalDate.now(), null, null);
    }
}