package com.magscene.magsav.desktop.model;

import com.magscene.magsav.desktop.component.DetailPanelProvider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import com.magscene.magsav.desktop.component.DetailPanel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO Contract pour l'interface JavaFX Desktop
 * Correspond aux données de l'entité Contract du backend
 */
public class Contract implements DetailPanelProvider {
    
    // Énumérations pour les propriétés du contrat
    public enum ContractType {
        MAINTENANCE("Maintenance"),
        RENTAL("Location"),
        SERVICE("Prestation de service"),
        SUPPORT("Support technique"),
        SUPPLY("Fourniture matériel"),
        MIXED("Mixte");

        private final String displayName;

        ContractType(String displayName) {
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

    public enum ContractStatus {
        DRAFT("Brouillon"),
        PENDING_SIGNATURE("En attente signature"),
        ACTIVE("Actif"),
        SUSPENDED("Suspendu"),
        TERMINATED("Résilié"),
        EXPIRED("Expiré"),
        COMPLETED("Terminé");

        private final String displayName;

        ContractStatus(String displayName) {
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

    public enum BillingFrequency {
        MONTHLY("Mensuel"),
        QUARTERLY("Trimestriel"),
        SEMI_ANNUAL("Semestriel"),
        ANNUAL("Annuel"),
        ONE_TIME("Ponctuel"),
        ON_DELIVERY("À la livraison");

        private final String displayName;

        BillingFrequency(String displayName) {
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

    public enum PaymentTerms {
        IMMEDIATE("Immédiat"),
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

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Propriétés du contrat
    private Long id;
    private String contractNumber;
    private String title;
    private ContractType type;
    private ContractStatus status;
    
    // Dates
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signatureDate;
    
    // Montants
    private BigDecimal totalAmount;
    private BigDecimal monthlyAmount;
    private BigDecimal invoicedAmount;
    private BigDecimal remainingAmount;
    
    // Facturation
    private BillingFrequency billingFrequency;
    private PaymentTerms paymentTerms;
    
    // Renouvellement
    private Boolean isAutoRenewable;
    private Integer renewalPeriodMonths;
    private Integer noticePeriodDays;
    
    // Informations textuelles
    private String description;
    private String termsAndConditions;
    private String notes;
    private String clientSignatory;
    private String magsceneSignatory;
    private String contractFilePath;
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations
    private Long clientId;
    private String clientName;
    private Long contactId;
    private String contactName;

    // Constructeurs
    public Contract() {}

    public Contract(String contractNumber, String title, ContractType type) {
        this.contractNumber = contractNumber;
        this.title = title;
        this.type = type;
        this.status = ContractStatus.DRAFT;
        this.isAutoRenewable = false;
        this.renewalPeriodMonths = 12;
        this.noticePeriodDays = 30;
        this.billingFrequency = BillingFrequency.MONTHLY;
        this.paymentTerms = PaymentTerms.NET_30;
        this.invoicedAmount = BigDecimal.ZERO;
    }

    // Méthodes utilitaires
    public String getStatusIcon() {
        if (status == null) return "❓";
        return switch (status) {
            case DRAFT -> "📝";
            case PENDING_SIGNATURE -> "✍️";
            case ACTIVE -> "✅";
            case SUSPENDED -> "⏸️";
            case TERMINATED -> "❌";
            case EXPIRED -> "⏰";
            case COMPLETED -> "✔️";
        };
    }

    public String getTypeIcon() {
        if (type == null) return "📄";
        return switch (type) {
            case MAINTENANCE -> "🔧";
            case RENTAL -> "📦";
            case SERVICE -> "🛠️";
            case SUPPORT -> "💻";
            case SUPPLY -> "📦";
            case MIXED -> "🔀";
        };
    }

    public String getBillingIcon() {
        if (billingFrequency == null) return "💰";
        return switch (billingFrequency) {
            case MONTHLY -> "📅";
            case QUARTERLY -> "📊";
            case SEMI_ANNUAL -> "📈";
            case ANNUAL -> "🗓️";
            case ONE_TIME -> "💳";
            case ON_DELIVERY -> "🚚";
        };
    }

    public String getDisplayName() {
        return contractNumber + " - " + title;
    }

    public BigDecimal getAvailableAmount() {
        if (totalAmount == null || invoicedAmount == null) {
            return BigDecimal.ZERO;
        }
        return totalAmount.subtract(invoicedAmount);
    }

    public double getCompletionPercentage() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (invoicedAmount == null) {
            return 0.0;
        }
        return invoicedAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public boolean isActive() {
        return ContractStatus.ACTIVE.equals(status);
    }

    public boolean isExpiringSoon() {
        if (endDate == null || !isActive()) {
            return false;
        }
        return endDate.isBefore(LocalDate.now().plusMonths(1));
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ContractType getType() { return type; }
    public void setType(ContractType type) { this.type = type; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getSignatureDate() { return signatureDate; }
    public void setSignatureDate(LocalDate signatureDate) { this.signatureDate = signatureDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(BigDecimal monthlyAmount) { this.monthlyAmount = monthlyAmount; }

    public BigDecimal getInvoicedAmount() { return invoicedAmount; }
    public void setInvoicedAmount(BigDecimal invoicedAmount) { this.invoicedAmount = invoicedAmount; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public BillingFrequency getBillingFrequency() { return billingFrequency; }
    public void setBillingFrequency(BillingFrequency billingFrequency) { this.billingFrequency = billingFrequency; }

    public PaymentTerms getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(PaymentTerms paymentTerms) { this.paymentTerms = paymentTerms; }

    public Boolean getIsAutoRenewable() { return isAutoRenewable; }
    public void setIsAutoRenewable(Boolean isAutoRenewable) { this.isAutoRenewable = isAutoRenewable; }

    public Integer getRenewalPeriodMonths() { return renewalPeriodMonths; }
    public void setRenewalPeriodMonths(Integer renewalPeriodMonths) { this.renewalPeriodMonths = renewalPeriodMonths; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getClientSignatory() { return clientSignatory; }
    public void setClientSignatory(String clientSignatory) { this.clientSignatory = clientSignatory; }

    public String getMagsceneSignatory() { return magsceneSignatory; }
    public void setMagsceneSignatory(String magsceneSignatory) { this.magsceneSignatory = magsceneSignatory; }

    public String getContractFilePath() { return contractFilePath; }
    public void setContractFilePath(String contractFilePath) { this.contractFilePath = contractFilePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contract contract = (Contract) obj;
        return id != null && id.equals(contract.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Implémentation de DetailPanelProvider
    @Override
    public String getDetailTitle() {
        return title != null ? title : "Contrat sans titre";
    }

    @Override
    public String getDetailSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        
        if (type != null) {
            subtitle.append(getTypeIcon()).append(" ").append(type.getDisplayName());
        }
        
        if (status != null) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append(getStatusIcon()).append(" ").append(status.getDisplayName());
        }
        
        if (contractNumber != null) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append("N° ").append(contractNumber);
        }
        
        return subtitle.toString();
    }

    @Override
    public Image getDetailImage() {
        // Pour l'instant, pas d'image spécifique pour les contrats
        return null;
    }

    @Override
    public String getQRCodeData() {
        return ""; // Pas de QR code pour les contrats
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(8);
        
        if (contractNumber != null && !contractNumber.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("N° Contrat", contractNumber));
        }
        
        if (description != null && !description.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Description", description));
        }
        
        if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Montant total", String.format("%.2f €", totalAmount)));
        }
        
        if (monthlyAmount != null && monthlyAmount.compareTo(BigDecimal.ZERO) > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Montant mensuel", String.format("%.2f €", monthlyAmount)));
        }
        
        if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            content.getChildren().add(DetailPanel.createInfoRow("Date début", startDate.format(formatter)));
        }
        
        if (endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            content.getChildren().add(DetailPanel.createInfoRow("Date fin", endDate.format(formatter)));
        }
        
        if (billingFrequency != null) {
            content.getChildren().add(DetailPanel.createInfoRow("Fréquence", billingFrequency.getDisplayName()));
        }
        
        if (isAutoRenewable != null && isAutoRenewable) {
            content.getChildren().add(DetailPanel.createInfoRow("Renouvellement", "Automatique"));
        }
        
        if (notes != null && !notes.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Notes", notes));
        }
        
        return content;
    }

    @Override
    public String getDetailId() {
        return id != null ? id.toString() : "";
    }
}