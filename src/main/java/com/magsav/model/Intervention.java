package com.magsav.model;

public record Intervention(
    long id,
    long productId,
    String serial,
    Long detectorSocieteId,
    String description,
    String status,
    String createdAt
) {}