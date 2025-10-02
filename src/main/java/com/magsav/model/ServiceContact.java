package com.magsav.model;

public record ServiceContact(
    Long id, Long serviceId, String name, String email, String phone, String role) {}
