package com.magsav.model;

public record RequestItem(
    long id,
    long requestId,
    String ref,
    int qty,
    String description
) {}