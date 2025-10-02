package com.magsav.model;

public record InterventionRow(
    long id,
    String produitNom,
    String statut,
    String panne,
    String dateEntree,
    String dateSortie) {}