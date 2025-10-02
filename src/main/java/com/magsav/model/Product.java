package com.magsav.model;

public record Product(
    long id,
    String code,
    String nom,
    String sn,
    String fabricant,
    Long categorieId,
    Long sousCategorieId,
    String createdAt) {}