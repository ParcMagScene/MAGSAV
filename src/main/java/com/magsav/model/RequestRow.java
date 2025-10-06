package com.magsav.model;

public record RequestRow(
    long id,
    String type,
    String status,
    String fournisseurNom,
    String commentaire,
    String createdAt,
    String validatedAt
) {}