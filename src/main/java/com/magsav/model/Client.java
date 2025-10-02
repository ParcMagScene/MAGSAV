package com.magsav.model;

public record Client(
    Long id, String nom, String prenom, String email, String tel, String adresse) {}
