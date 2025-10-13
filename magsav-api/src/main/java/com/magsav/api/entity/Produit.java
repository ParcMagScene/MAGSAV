package com.magsav.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité Produit pour le POC
 * Migration de l'ancien modèle vers JPA moderne
 */
@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    @Column(name = "nom_produit", nullable = false)
    private String nomProduit;

    @Size(max = 100, message = "Le numéro de série ne peut pas dépasser 100 caractères")
    @Column(name = "numero_serie", unique = true)
    private String numeroSerie;

    @NotNull(message = "Le fabricant est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabricant_id", nullable = false)
    private Societe fabricant;

    @Size(max = 100, message = "L'UID unique ne peut pas dépasser 100 caractères")
    @Column(name = "uid_unique", unique = true)
    private String uidUnique;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_produit", nullable = false)
    private StatutProduit statutProduit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Size(max = 50, message = "La référence ne peut pas dépasser 50 caractères")
    @Column(name = "reference", length = 50)
    private String reference;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prix_achat")
    private Double prixAchat;

    @Column(name = "prix_vente")
    private Double prixVente;

    @Column(name = "stock_minimum", columnDefinition = "INTEGER DEFAULT 0")
    private Integer stockMinimum = 0;

    @Column(name = "stock_actuel", columnDefinition = "INTEGER DEFAULT 0")
    private Integer stockActuel = 0;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Énumération pour les statuts de produit
    public enum StatutProduit {
        DISPONIBLE("Disponible"),
        EN_COMMANDE("En Commande"),
        INSTALLE("Installé"),
        EN_MAINTENANCE("En Maintenance"),
        HORS_SERVICE("Hors Service"),
        VENDU("Vendu");

        private final String libelle;

        StatutProduit(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public Produit() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Produit(String nomProduit, Societe fabricant, StatutProduit statutProduit) {
        this();
        this.nomProduit = nomProduit;
        this.fabricant = fabricant;
        this.statutProduit = statutProduit;
    }

    // Méthodes de cycle de vie JPA
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public boolean isStockFaible() {
        return stockActuel != null && stockMinimum != null && stockActuel <= stockMinimum;
    }

    public Double getMarge() {
        if (prixAchat != null && prixVente != null && prixAchat > 0) {
            return ((prixVente - prixAchat) / prixAchat) * 100;
        }
        return null;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public Societe getFabricant() { return fabricant; }
    public void setFabricant(Societe fabricant) { this.fabricant = fabricant; }

    public String getUidUnique() { return uidUnique; }
    public void setUidUnique(String uidUnique) { this.uidUnique = uidUnique; }

    public StatutProduit getStatutProduit() { return statutProduit; }
    public void setStatutProduit(StatutProduit statutProduit) { this.statutProduit = statutProduit; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrixAchat() { return prixAchat; }
    public void setPrixAchat(Double prixAchat) { this.prixAchat = prixAchat; }

    public Double getPrixVente() { return prixVente; }
    public void setPrixVente(Double prixVente) { this.prixVente = prixVente; }

    public Integer getStockMinimum() { return stockMinimum; }
    public void setStockMinimum(Integer stockMinimum) { this.stockMinimum = stockMinimum; }

    public Integer getStockActuel() { return stockActuel; }
    public void setStockActuel(Integer stockActuel) { this.stockActuel = stockActuel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nomProduit='" + nomProduit + '\'' +
                ", numeroSerie='" + numeroSerie + '\'' +
                ", statutProduit=" + statutProduit +
                ", reference='" + reference + '\'' +
                '}';
    }
}