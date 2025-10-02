package com.magsav.model;

import java.time.LocalDateTime;

public record DocumentEntry(
    Long id,
    String type,
    String originalName,
    String normalizedName,
    String path,
    String linkedProductCode,
    String linkedNumeroSerie,
    Long linkedDossierId,
    Long linkedRfqId,
    Long linkedRmaId,
    LocalDateTime createdAt) {}
