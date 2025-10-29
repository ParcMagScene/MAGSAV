package com.magscene.magsav.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Client pour l'interface JavaFX Desktop
 * Correspond aux données de l'entité Client du backend
 */
public class Client {
    
    // Énumérations pour les propriétés du client
    public enum ClientType {
        CORPORATE("Entreprise"),
        GOVERNMENT("Administration"),
        ASSOCIATION("Association"),
        INDIVIDUAL("Particulier");
        
        private final String displayName;
        
        ClientType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
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
        
        @Override
        public String toString() {
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
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum PreferredPaymentMethod {
        BANK_TRANSFER("Virement bancaire"),
        CHECK("Chèque"),
        CREDIT_CARD("Carte de crédit"),
        CASH("Espèces"),
        DIRECT_DEBIT("Prélèvement automatique");
        
        private final String displayName;
        
        PreferredPaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Propriétés du client
    private Long id;
    private String companyName;
    private ClientType type;
    private ClientStatus status;
    private ClientCategory category;
    
    // Informations de contact
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phone;
    private String fax;
    private String email;
    private String website;
    
    // Informations administratives
    private String siretNumber;
    private String vatNumber;
    private String businessSector;
    private Integer employeeCount;
    private BigDecimal annualRevenue;
    
    // Informations commerciales
    private String assignedSalesRep;
    private PreferredPaymentMethod preferredPaymentMethod;
    private Integer paymentTermsDays;
    private BigDecimal creditLimit;
    private BigDecimal outstandingAmount;
    
    // Métadonnées
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Client() {}
    
    public Client(String companyName, ClientType type, ClientStatus status, ClientCategory category) {
        this.companyName = companyName;
        this.type = type;
        this.status = status;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
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
    
    public String getBusinessSector() {
        return businessSector;
    }
    
    public void setBusinessSector(String businessSector) {
        this.businessSector = businessSector;
    }
    
    public Integer getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public BigDecimal getAnnualRevenue() {
        return annualRevenue;
    }
    
    public void setAnnualRevenue(BigDecimal annualRevenue) {
        this.annualRevenue = annualRevenue;
    }
    
    public String getAssignedSalesRep() {
        return assignedSalesRep;
    }
    
    public void setAssignedSalesRep(String assignedSalesRep) {
        this.assignedSalesRep = assignedSalesRep;
    }
    
    public PreferredPaymentMethod getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }
    
    public void setPreferredPaymentMethod(PreferredPaymentMethod preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }
    
    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }
    
    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    // Méthodes utilitaires
    public String getDisplayName() {
        return companyName != null ? companyName : "Client sans nom";
    }
    
    public String getStatusIcon() {
        if (status == null) return "❓";
        switch (status) {
            case ACTIVE: return "✅";
            case INACTIVE: return "❌";
            case PROSPECT: return "🎯";
            case SUSPENDED: return "⏸️";
            default: return "❓";
        }
    }
    
    public String getCategoryIcon() {
        if (category == null) return "❓";
        switch (category) {
            case PREMIUM: return "🏆";
            case VIP: return "⭐";
            case STANDARD: return "📋";
            case BASIC: return "📝";
            default: return "❓";
        }
    }
    
    @Override
    public String toString() {
        return String.format("Client{id=%d, companyName='%s', type=%s, status=%s}", 
                            id, companyName, type, status);
    }
}