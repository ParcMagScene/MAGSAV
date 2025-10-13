package com.magsav.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité Catégorie pour le POC
 * Représente les catégories de produits
 */
@Entity
@Table(name = "categories")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    @Column(name = "nom_categorie", nullable = false, unique = true)
    private String nomCategorie;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "couleur", length = 7)
    private String couleur; // Code couleur hex (#FFFFFF)

    @Column(name = "icone", length = 50)
    private String icone; // Nom de l'icône FontAwesome ou autre

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @Column(name = "ordre_affichage", columnDefinition = "INTEGER DEFAULT 0")
    private Integer ordreAffichage = 0;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Constructeurs
    public Categorie() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Categorie(String nomCategorie) {
        this();
        this.nomCategorie = nomCategorie;
    }

    public Categorie(String nomCategorie, String description, String couleur) {
        this(nomCategorie);
        this.description = description;
        this.couleur = couleur;
    }

    // Méthodes de cycle de vie JPA
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public String getCouleurOuDefaut() {
        return couleur != null && !couleur.trim().isEmpty() ? couleur : "#6c757d";
    }

    public String getIconeOuDefaut() {
        return icone != null && !icone.trim().isEmpty() ? icone : "fa-box";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer ordreAffichage) { this.ordreAffichage = ordreAffichage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", nomCategorie='" + nomCategorie + '\'' +
                ", couleur='" + couleur + '\'' +
                ", active=" + active +
                '}';
    }
}