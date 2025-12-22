package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * EntitÃƒÂ© reprÃƒÂ©sentant un contrat avec un client
 * Gestion des contrats de maintenance, location, prestations
 */
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numÃƒÂ©ro de contrat est obligatoire")
    @Size(max = 50, message = "Le numÃƒÂ©ro de contrat ne peut pas dÃƒÂ©passer 50 caractÃƒÂ¨res")
    @Column(nullable = false, length = 50, unique = true)
    private String contractNumber;

    @NotBlank(message = "Le titre du contrat est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dÃƒÂ©passer 200 caractÃƒÂ¨res")
    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @NotNull(message = "La date de dÃƒÂ©but est obligatoire")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate signatureDate;

    @NotNull(message = "Le montant total est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit ÃƒÂªtre positif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal invoicedAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    private BillingFrequency billingFrequency = BillingFrequency.MONTHLY;

    @Enumerated(EnumType.STRING)
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @Column(nullable = false)
    private Boolean isAutoRenewable = false;

    @Min(value = 1, message = "La pÃƒÂ©riode de renouvellement doit ÃƒÂªtre positive")
    private Integer renewalPeriodMonths = 12;

    @Min(value = 0, message = "Le prÃƒÂ©avis de rÃƒÂ©siliation doit ÃƒÂªtre positif")
    private Integer noticePeriodDays = 30;

    @Size(max = 2000, message = "La description ne peut pas dÃƒÂ©passer 2000 caractÃƒÂ¨res")
    @Column(length = 2000)
    private String description;

    @Size(max = 2000, message = "Les termes et conditions ne peuvent pas dÃƒÂ©passer 2000 caractÃƒÂ¨res")
    @Column(length = 2000)
    private String termsAndConditions;

    @Size(max = 1000, message = "Les notes ne peuvent pas dÃƒÂ©passer 1000 caractÃƒÂ¨res")
    @Column(length = 1000)
    private String notes;

    @Size(max = 100, message = "Le nom du signataire ne peut pas dÃƒÂ©passer 100 caractÃƒÂ¨res")
    @Column(length = 100)
    private String clientSignatory;

    @Size(max = 100, message = "Le nom du signataire MagScÃƒÂ¨ne ne peut pas dÃƒÂ©passer 100 caractÃƒÂ¨res")
    @Column(length = 100)
    private String magsceneSignatory;

    @Size(max = 500, message = "Le chemin du fichier ne peut pas dÃƒÂ©passer 500 caractÃƒÂ¨res")
    @Column(length = 500)
    private String contractFilePath;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    @JsonIgnore
    private Contact clientContact;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContractItem> contractItems = new ArrayList<>();

    // Enums
    public enum ContractType {
        MAINTENANCE("Maintenance"),
        RENTAL("Location"),
        SERVICE("Prestation de service"),
        SUPPORT("Support technique"),
        SUPPLY("Fourniture matÃƒÂ©riel"),
        MIXED("Mixte");

        private final String displayName;

        ContractType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ContractStatus {
        DRAFT("Brouillon"),
        PENDING_SIGNATURE("En attente signature"),
        ACTIVE("Actif"),
        SUSPENDED("Suspendu"),
        TERMINATED("RÃƒÂ©siliÃƒÂ©"),
        EXPIRED("ExpirÃƒÂ©"),
        COMPLETED("TerminÃƒÂ©");

        private final String displayName;

        ContractStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BillingFrequency {
        MONTHLY("Mensuel"),
        QUARTERLY("Trimestriel"),
        SEMI_ANNUAL("Semestriel"),
        ANNUAL("Annuel"),
        ONE_TIME("Ponctuel"),
        ON_DELIVERY("Ãƒâ‚¬ la livraison");

        private final String displayName;

        BillingFrequency(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentTerms {
        IMMEDIATE("ImmÃƒÂ©diat"),
        NET_15("15 jours nets"),
        NET_30("30 jours nets"),
        NET_45("45 jours nets"),
        NET_60("60 jours nets"),
        END_OF_MONTH("Fin de mois"),
        END_OF_MONTH_15("Fin de mois + 15 jours");

        private final String displayName;

        PaymentTerms(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public Contract() {
    }

    public Contract(String contractNumber, String title, ContractType type, Client client) {
        this.contractNumber = contractNumber;
        this.title = title;
        this.type = type;
        this.client = client;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContractType getType() {
        return type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(LocalDate signatureDate) {
        this.signatureDate = signatureDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public BigDecimal getInvoicedAmount() {
        return invoicedAmount;
    }

    public void setInvoicedAmount(BigDecimal invoicedAmount) {
        this.invoicedAmount = invoicedAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public BillingFrequency getBillingFrequency() {
        return billingFrequency;
    }

    public void setBillingFrequency(BillingFrequency billingFrequency) {
        this.billingFrequency = billingFrequency;
    }

    public PaymentTerms getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(PaymentTerms paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public Boolean getIsAutoRenewable() {
        return isAutoRenewable;
    }

    public void setIsAutoRenewable(Boolean isAutoRenewable) {
        this.isAutoRenewable = isAutoRenewable;
    }

    public Integer getRenewalPeriodMonths() {
        return renewalPeriodMonths;
    }

    public void setRenewalPeriodMonths(Integer renewalPeriodMonths) {
        this.renewalPeriodMonths = renewalPeriodMonths;
    }

    public Integer getNoticePeriodDays() {
        return noticePeriodDays;
    }

    public void setNoticePeriodDays(Integer noticePeriodDays) {
        this.noticePeriodDays = noticePeriodDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClientSignatory() {
        return clientSignatory;
    }

    public void setClientSignatory(String clientSignatory) {
        this.clientSignatory = clientSignatory;
    }

    public String getMagsceneSignatory() {
        return magsceneSignatory;
    }

    public void setMagsceneSignatory(String magsceneSignatory) {
        this.magsceneSignatory = magsceneSignatory;
    }

    public String getContractFilePath() {
        return contractFilePath;
    }

    public void setContractFilePath(String contractFilePath) {
        this.contractFilePath = contractFilePath;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Contact getClientContact() {
        return clientContact;
    }

    public void setClientContact(Contact clientContact) {
        this.clientContact = clientContact;
    }

    public List<ContractItem> getContractItems() {
        return contractItems;
    }

    public void setContractItems(List<ContractItem> contractItems) {
        this.contractItems = contractItems;
    }

    // MÃƒÂ©thodes utilitaires
    public String getDisplayName() {
        return contractNumber + " - " + title;
    }

    public boolean isActive() {
        return status == ContractStatus.ACTIVE;
    }

    public boolean isExpiring() {
        if (!isActive() || endDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return endDate.isBefore(thirtyDaysFromNow) || endDate.isEqual(thirtyDaysFromNow);
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public BigDecimal getCompletionPercentage() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (invoicedAmount == null) {
            return BigDecimal.ZERO;
        }
        return invoicedAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public void updateRemainingAmount() {
        if (totalAmount != null) {
            this.remainingAmount = totalAmount.subtract(invoicedAmount != null ? invoicedAmount : BigDecimal.ZERO);
        }
    }

    @PrePersist
    @PreUpdate
    private void updateCalculatedFields() {
        updateRemainingAmount();
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

