package com.magsav.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Entité Société pour le POC
 * Migration de l'ancien modèle vers JPA moderne
 */
@Entity
@Table(name = "societes")
public class Societe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la société est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    @Column(name = "nom_societe", nullable = false)
    private String nomSociete;

    @Column(name = "adresse_societe", columnDefinition = "TEXT")
    private String adresseSociete;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "telephone", length = 20)
    private String telephone;

    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    @Column(name = "email")
    private String email;

    @Size(max = 14, message = "Le SIRET ne peut pas dépasser 14 caractères")
    @Column(name = "siret", length = 14, unique = true)
    private String siret;

    @Size(max = 20, message = "Le code NAF ne peut pas dépasser 20 caractères")
    @Column(name = "code_naf", length = 20)
    private String codeNaf;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_societe", nullable = false)
    private TypeSociete typeSociete;

    @Column(name = "notes_societe", columnDefinition = "TEXT")
    private String notesSociete;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @Column(name = "date_creation", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Énumération pour les types de société
    public enum TypeSociete {
        CLIENT("Client"),
        FOURNISSEUR("Fournisseur"),
        FABRICANT("Fabricant"),
        PRESTATAIRE("Prestataire"),
        PARTENAIRE("Partenaire"),
        CONCURRENT("Concurrent"),
        SOUS_TRAITANT("Sous-traitant");

        private final String libelle;

        TypeSociete(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public Societe() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.dateCreation = LocalDateTime.now();
    }

    public Societe(String nomSociete, TypeSociete typeSociete) {
        this();
        this.nomSociete = nomSociete;
        this.typeSociete = typeSociete;
    }

    // Méthodes de cycle de vie JPA
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public boolean isClient() {
        return TypeSociete.CLIENT.equals(this.typeSociete);
    }

    public boolean isFournisseur() {
        return TypeSociete.FOURNISSEUR.equals(this.typeSociete);
    }

    public boolean isFabricant() {
        return TypeSociete.FABRICANT.equals(this.typeSociete);
    }

    public String getAdresseComplete() {
        if (adresseSociete != null && !adresseSociete.trim().isEmpty()) {
            return adresseSociete.trim();
        }
        return "Adresse non renseignée";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomSociete() { return nomSociete; }
    public void setNomSociete(String nomSociete) { this.nomSociete = nomSociete; }

    public String getAdresseSociete() { return adresseSociete; }
    public void setAdresseSociete(String adresseSociete) { this.adresseSociete = adresseSociete; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }

    public String getCodeNaf() { return codeNaf; }
    public void setCodeNaf(String codeNaf) { this.codeNaf = codeNaf; }

    public TypeSociete getTypeSociete() { return typeSociete; }
    public void setTypeSociete(TypeSociete typeSociete) { this.typeSociete = typeSociete; }

    public String getNotesSociete() { return notesSociete; }
    public void setNotesSociete(String notesSociete) { this.notesSociete = notesSociete; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Societe{" +
                "id=" + id +
                ", nomSociete='" + nomSociete + '\'' +
                ", typeSociete=" + typeSociete +
                ", siret='" + siret + '\'' +
                ", active=" + active +
                '}';
    }
}