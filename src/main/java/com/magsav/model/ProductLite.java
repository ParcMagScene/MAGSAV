package com.magsav.model;
public record ProductLite(long id, String nom) { @Override public String toString() { return nom; } }