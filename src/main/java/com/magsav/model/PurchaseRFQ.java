package com.magsav.model;

import java.time.LocalDateTime;

public record PurchaseRFQ(
    Long id,
    Long providerId,
    Long providerServiceId,
    String produit,
    String partNumber,
    Integer quantity,
    String status,
    LocalDateTime requestedAt,
    LocalDateTime respondedAt,
    Double price,
    String currency,
    String notes) {}
