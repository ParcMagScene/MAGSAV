package com.magsav.model;

public record ProductRow(
    Long id,
    String nom,
    String sn,
    String fabricant,
    String situation
) {}