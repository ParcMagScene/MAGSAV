package com.magsav.model;

public record Company(
    Long id,
    String name,
    String siret,
    String email,
    String phone,
    String website,
    String addressLine1,
    String addressLine2,
    String postalCode,
    String city,
    String country) {}
