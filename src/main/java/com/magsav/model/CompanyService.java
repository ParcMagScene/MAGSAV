package com.magsav.model;

public record CompanyService(
    Long id,
    Long companyId,
    String type, // SUPPLIER | SAV | SALES | SUPPORT | OTHER
    String name,
    String email,
    String phone,
    String addressLine1,
    String addressLine2,
    String postalCode,
    String city,
    String country,
    boolean active) {}
