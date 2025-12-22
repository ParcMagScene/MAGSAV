package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * EntitÃƒÂ© reprÃƒÂ©sentant un client (entreprise) de Mag ScÃƒÂ¨ne
 * Gestion complÃƒÂ¨te des informations commerciales et contractuelles
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 200, message = "Le nom ne peut pas dÃƒÂ©passer 200 caractÃƒÂ¨res")
    @Column(nullable = false, length = 200)
    private String companyName;

    @Size(max = 20, message = "Le numÃƒÂ©ro SIRET ne peut pas dÃƒÂ©passer 20 caractÃƒÂ¨res")
    @Column(length = 20, unique = true)
    private String siretNumber;

    @Size(max = 15, message = "Le numÃƒÂ©ro TVA ne peut pas dÃƒÂ©passer 15 caractÃƒÂ¨res")
    @Column(length = 15)
    private String vatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType type = ClientType.CORPORATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientCategory category = ClientCategory.STANDARD;

    // Informations de contact principales
    @Size(max = 500, message = "L'adresse ne peut pas dÃƒÂ©passer 500 caractÃƒÂ¨res")
    @Column(length = 500)
    private String address;

    @Size(max = 10, message = "Le code postal ne peut pas dÃƒÂ©passer 10 caractÃƒÂ¨res")
    @Column(length = 10)
    private String postalCode;

    @Size(max = 100, message = "La ville ne peut pas dÃƒÂ©passer 100 caractÃƒÂ¨res")
    @Column(length = 100)
    private String city;

    @Size(max = 100, message = "Le pays ne peut pas dÃƒÂ©passer 100 caractÃƒÂ¨res")
    @Column(length = 100)
    private String country = "France";

    @Email(message = "L'email doit ÃƒÂªtre valide")
    @Size(max = 200, message = "L'email ne peut pas dÃƒÂ©passer 200 caractÃƒÂ¨res")
    @Column(length = 200)
    private String email;

    @Size(max = 20, message = "Le tÃƒÂ©lÃƒÂ©phone ne peut pas dÃƒÂ©passer 20 caractÃƒÂ¨res")
    @Column(length = 20)
    private String phone;

    @Size(max = 20, message = "Le fax ne peut pas dÃƒÂ©passer 20 caractÃƒÂ¨res")
    @Column(length = 20)
    private String fax;

    @Size(max = 200, message = "Le site web ne peut pas dÃƒÂ©passer 200 caractÃƒÂ¨res")
    @Column(length = 200)
    private String website;

    // Informations commerciales
    @Size(max = 100, message = "Le secteur d'activitÃƒÂ© ne peut pas dÃƒÂ©passer 100 caractÃƒÂ¨res")
    @Column(length = 100)
    private String businessSector;

    @Column(precision = 12, scale = 2)
    private BigDecimal annualRevenue;

    @Min(value = 0, message = "Le nombre d'employÃƒÂ©s doit ÃƒÂªtre positif")
    private Integer employeeCount;

    // Informations financiÃƒÂ¨res
    @Column(precision = 12, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Min(value = 0, message = "Les dÃƒÂ©lais de paiement doivent ÃƒÂªtre positifs")
    private Integer paymentTermsDays = 30;

    @Enumerated(EnumType.STRING)
    private PaymentMethod preferredPaymentMethod = PaymentMethod.BANK_TRANSFER;

    // Informations de gestion
    @Column(length = 1000)
    private String notes;

    @Column(length = 100)
    private String assignedSalesRep;

    @Column(name = "logo_path")
    private String logoPath; // Chemin vers le logo dans le dossier Logos

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Project> projects = new ArrayList<>();

    // Enums
    public enum ClientType {
        CORPORATE("Entreprise"),
        GOVERNMENT("Organisme public"),
        ASSOCIATION("Association"),
        INDIVIDUAL("Particulier");

        private final String displayName;

        ClientType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ClientStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        PROSPECT("Prospect"),
        SUSPENDED("Suspendu");

        private final String displayName;

        ClientStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ClientCategory {
        PREMIUM("Premium"),
        STANDARD("Standard"),
        BASIC("Basique"),
        VIP("VIP");

        private final String displayName;

        ClientCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentMethod {
        BANK_TRANSFER("Virement bancaire"),
        CHECK("ChÃƒÂ¨que"),
        CREDIT_CARD("Carte bancaire"),
        CASH("EspÃƒÂ¨ces"),
        DIRECT_DEBIT("PrÃƒÂ©lÃƒÂ¨vement automatique");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public Client() {
    }

    public Client(String companyName, String email, String phone) {
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSiretNumber() {
        return siretNumber;
    }

    public void setSiretNumber(String siretNumber) {
        this.siretNumber = siretNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public ClientCategory getCategory() {
        return category;
    }

    public void setCategory(ClientCategory category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBusinessSector() {
        return businessSector;
    }

    public void setBusinessSector(String businessSector) {
        this.businessSector = businessSector;
    }

    public BigDecimal getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(BigDecimal annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public PaymentMethod getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(PaymentMethod preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAssignedSalesRep() {
        return assignedSalesRep;
    }

    public void setAssignedSalesRep(String assignedSalesRep) {
        this.assignedSalesRep = assignedSalesRep;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    // MÃƒÂ©thodes utilitaires
    public String getDisplayName() {
        return companyName;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            sb.append(address);
        }
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(postalCode);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }

    public BigDecimal getAvailableCredit() {
        if (creditLimit == null) return BigDecimal.ZERO;
        if (outstandingAmount == null) return creditLimit;
        return creditLimit.subtract(outstandingAmount);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", category=" + category +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

