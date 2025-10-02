package com.magsav.model;

public record ManufacturerServiceLink(
    Long id,
    Long manufacturerId,
    Long serviceId,
    String relationType // SUPPLIER | SAV | DISTRIBUTOR | OTHER
    ) {}
