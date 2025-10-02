package com.magsav.model;

public record Manufacturer(
    Long id,
    String name,
    String website,
    String contactEmail,
    String contactPhone,
    String logoPath) {}
