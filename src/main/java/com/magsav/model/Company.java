package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Modèle unifié représentant toutes les entités organisationnelles :
 * - Fabricants (Yamaha, HP, etc.)
 * - Sociétés (clients, partenaires, Mag Scène)
 * - Administrations (mairies, hôpitaux, écoles)
 */
public class Company {
    
    /**
     * Types d'entités organisationnelles
     */
    public enum CompanyType {
        MANUFACTURER("Fabricant", "🏭"),
        COMPANY("Société", "🏢"),
        ADMINISTRATION("Administration", "🏛️"),
        SUPPLIER("Fournisseur", "📦"),
        CLIENT("Client", "👥"),
        PARTNER("Partenaire", "🤝"),
        OWN_COMPANY("Notre Société", "🏠");
        
        private final String displayName;
        private final String icon;
        
        CompanyType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        
        @Override
        public String toString() { return icon + " " + displayName; }
    }
    
    private Long id;
    private String name;
    private String legalName;
    private CompanyType type;
    private String siret;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private String phone;
    private String email;
    private String website;
    private String description;
    private String logoPath;
    private String sector; // Secteur d'activité
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public Company() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.country = "France";
    }

    public Company(String name, CompanyType type) {
        this();
        this.name = name;
        this.legalName = name;
        this.type = type;
    }

    public Company(String name, String legalName, CompanyType type) {
        this();
        this.name = name;
        this.legalName = legalName;
        this.type = type;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { 
        this.legalName = legalName; 
        this.updatedAt = LocalDateTime.now();
    }

    public CompanyType getType() { return type; }
    public void setType(CompanyType type) { 
        this.type = type; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { 
        this.siret = siret; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { 
        this.address = address; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { 
        this.postalCode = postalCode; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getCity() { return city; }
    public void setCity(String city) { 
        this.city = city; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { 
        this.country = country; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { 
        this.website = website; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { 
        this.logoPath = logoPath; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { 
        this.sector = sector; 
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        this.isActive = active; 
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name != null ? name : "Société sans nom";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Company company = (Company) obj;
        return id != null && id.equals(company.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}