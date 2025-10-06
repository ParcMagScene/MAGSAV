package com.magsav.model;

public record Societe(
    long id,
    String type,
    String nom,
    String email,
    String phone,
    String adresse,
    String notes,
    String createdAt
) {}