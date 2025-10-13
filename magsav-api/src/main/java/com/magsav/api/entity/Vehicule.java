package com.magsav.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité Véhicule pour le POC
 * Démontre la nouvelle structure JPA pour MAGSAV-1.3
 */
@Entity
@Table(name = "vehicules")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Size(max = 20, message = "L'immatriculation ne peut pas dépasser 20 caractères")
    @Column(name = "immatriculation", unique = true, nullable = false, length = 20)
    private String immatriculation;

    @NotNull(message = "Le type de véhicule est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_vehicule", nullable = false)
    private TypeVehicule typeVehicule;

    @Size(max = 100, message = "La marque ne peut pas dépasser 100 caractères")
    @Column(name = "marque", length = 100)
    private String marque;

    @Size(max = 100, message = "Le modèle ne peut pas dépasser 100 caractères")
    @Column(name = "modele", length = 100)
    private String modele;

    @Column(name = "annee")
    private Integer annee;

    @Column(name = "kilometrage", columnDefinition = "INTEGER DEFAULT 0")
    private Integer kilometrage = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", columnDefinition = "VARCHAR(20) DEFAULT 'DISPONIBLE'")
    private StatutVehicule statut = StatutVehicule.DISPONIBLE;

    @Column(name = "location_externe", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean locationExterne = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Énumération pour les types de véhicules
    public enum TypeVehicule {
        VL("Véhicule Léger"),
        PL("Poids Lourd"),
        SPL("Super Poids Lourd"),
        REMORQUE("Remorque"),
        SCENE_MOBILE("Scène Mobile");

        private final String libelle;

        TypeVehicule(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Énumération pour les statuts
    public enum StatutVehicule {
        DISPONIBLE("Disponible"),
        EN_SERVICE("En Service"),
        MAINTENANCE("En Maintenance"),
        HORS_SERVICE("Hors Service");

        private final String libelle;

        StatutVehicule(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public Vehicule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Vehicule(String immatriculation, TypeVehicule typeVehicule, String marque, String modele) {
        this();
        this.immatriculation = immatriculation;
        this.typeVehicule = typeVehicule;
        this.marque = marque;
        this.modele = modele;
    }

    // Méthodes de cycle de vie JPA
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public TypeVehicule getTypeVehicule() { return typeVehicule; }
    public void setTypeVehicule(TypeVehicule typeVehicule) { this.typeVehicule = typeVehicule; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public Integer getKilometrage() { return kilometrage; }
    public void setKilometrage(Integer kilometrage) { this.kilometrage = kilometrage; }

    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }

    public Boolean getLocationExterne() { return locationExterne; }
    public void setLocationExterne(Boolean locationExterne) { this.locationExterne = locationExterne; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", immatriculation='" + immatriculation + '\'' +
                ", typeVehicule=" + typeVehicule +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", statut=" + statut +
                '}';
    }
}