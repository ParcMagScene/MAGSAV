package com.magsav.service;

/**
 * Événements de changement de données dans l'application
 */
public class DataChangeEvent {
    
    public enum Type {
        PRODUCTS_IMPORTED,    // Produits importés via CSV
        PRODUCT_CREATED,      // Nouveau produit créé
        PRODUCT_UPDATED,      // Produit modifié
        PRODUCT_DELETED,      // Produit supprimé
        CATEGORIES_CHANGED,   // Catégories modifiées (ancien)
        CATEGORY_CREATED,     // Nouvelle catégorie créée
        CATEGORY_UPDATED,     // Catégorie modifiée
        CATEGORY_DELETED,     // Catégorie supprimée
        INTERVENTIONS_CHANGED,// Interventions modifiées
        COMPANY_UPDATED,      // Informations société modifiées
        DATABASE_CLEANED      // Base de données nettoyée (doublons supprimés)
    }
    
    private final Type type;
    private final String details;
    private final Object data;
    
    public DataChangeEvent(Type type, String details) {
        this(type, details, null);
    }
    
    public DataChangeEvent(Type type, String details, Object data) {
        this.type = type;
        this.details = details;
        this.data = data;
    }
    
    public Type getType() { return type; }
    public String getDetails() { return details; }
    public Object getData() { return data; }
    
    @Override
    public String toString() {
        return "DataChangeEvent{type=" + type + ", details='" + details + "'}";
    }
}