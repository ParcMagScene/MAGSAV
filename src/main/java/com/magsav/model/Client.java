package com.magsav.model;

public record Client(
    long id,
    String nom,
    String email,
    String telephone,
    String adresse,
    String createdAt
) {}