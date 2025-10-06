package com.magsav.model;

public record ProductRow(
    Long id,
    String nom,
    String code,
    String sn,
    String fabricant,
    String situation
) {}