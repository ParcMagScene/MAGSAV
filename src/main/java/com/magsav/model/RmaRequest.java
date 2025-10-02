package com.magsav.model;

import java.time.LocalDateTime;

public record RmaRequest(
    Long id,
    Long providerId,
    Long providerServiceId,
    Long manufacturerId,
    String produit,
    String numeroSerie,
    String codeProduit,
    String reason,
    String status,
    String rmaNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
