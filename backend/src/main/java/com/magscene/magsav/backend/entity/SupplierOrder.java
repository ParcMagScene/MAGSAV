package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * Entité représentant une commande fournisseur
 */
@Entity
@Table(name = "supplier_orders")
public class SupplierOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de commande est obligatoire")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull(message = "Le type de commande est obligatoire")
    @Enumerated(EnumType.STRING)
    private OrderType type;

    // Informations fournisseur
    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "supplier_contact", length = 100)
    private String supplierContact;

    @Column(name = "supplier_email", length = 100)
    private String supplierEmail;

    @Column(name = "supplier_phone", length = 50)
    private String supplierPhone;

    @Column(name = "supplier_address", columnDefinition = "TEXT")
    private String supplierAddress;

    // Informations commande
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "purchase_reason", columnDefinition = "TEXT")
    private String purchaseReason;

    // Projet associé (optionnel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // Responsable commande
    @Column(name = "ordered_by", length = 100)
    private String orderedBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    // Dates importantes
    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    // Informations financières
    @DecimalMin(value = "0.0", message = "Le montant HT doit être positif")
    @Column(name = "amount_ht", precision = 10, scale = 2)
    private BigDecimal amountHT;

    @DecimalMin(value = "0.0", message = "Le montant TTC doit être positif")
    @Column(name = "amount_ttc", precision = 10, scale = 2)
    private BigDecimal amountTTC;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate = BigDecimal.valueOf(20.0); // TVA par défaut 20%

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    // Termes de paiement
    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    // Livraison
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_contact", length = 100)
    private String deliveryContact;

    @Column(name = "delivery_phone", length = 50)
    private String deliveryPhone;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    // Articles commandés (relation avec SupplierOrderItem)
    @OneToMany(mappedBy = "supplierOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SupplierOrderItem> items = new HashSet<>();

    // Documents
    @Column(name = "purchase_order_pdf")
    private String purchaseOrderPdf;

    @Column(name = "invoice_pdf")
    private String invoicePdf;

    @Column(name = "delivery_receipt_pdf")
    private String deliveryReceiptPdf;

    // Notes et observations
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Énumérations
    public enum OrderStatus {
        DRAFT,          // Brouillon
        PENDING,        // En attente d'approbation
        APPROVED,       // Approuvée
        ORDERED,        // Commandée
        PARTIALLY_RECEIVED, // Partiellement reçue
        RECEIVED,       // Reçue
        CANCELLED,      // Annulée
        RETURNED        // Retournée
    }

    public enum OrderType {
        EQUIPMENT,      // Équipement
        SUPPLIES,       // Fournitures
        SERVICE,        // Service
        MAINTENANCE,    // Maintenance
        RENTAL,         // Location
        OTHER          // Autre
    }

    // Constructors
    public SupplierOrder() {}

    public SupplierOrder(String orderNumber, String supplierName, OrderType type) {
        this.orderNumber = orderNumber;
        this.supplierName = supplierName;
        this.type = type;
        this.status = OrderStatus.DRAFT;
        this.orderDate = LocalDate.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }

    public String getSupplierEmail() { return supplierEmail; }
    public void setSupplierEmail(String supplierEmail) { this.supplierEmail = supplierEmail; }

    public String getSupplierPhone() { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }

    public String getSupplierAddress() { return supplierAddress; }
    public void setSupplierAddress(String supplierAddress) { this.supplierAddress = supplierAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPurchaseReason() { return purchaseReason; }
    public void setPurchaseReason(String purchaseReason) { this.purchaseReason = purchaseReason; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public LocalDate getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }

    public BigDecimal getAmountHT() { return amountHT; }
    public void setAmountHT(BigDecimal amountHT) { this.amountHT = amountHT; }

    public BigDecimal getAmountTTC() { return amountTTC; }
    public void setAmountTTC(BigDecimal amountTTC) { this.amountTTC = amountTTC; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryContact() { return deliveryContact; }
    public void setDeliveryContact(String deliveryContact) { this.deliveryContact = deliveryContact; }

    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

    public Set<SupplierOrderItem> getItems() { return items; }
    public void setItems(Set<SupplierOrderItem> items) { this.items = items; }

    public String getPurchaseOrderPdf() { return purchaseOrderPdf; }
    public void setPurchaseOrderPdf(String purchaseOrderPdf) { this.purchaseOrderPdf = purchaseOrderPdf; }

    public String getInvoicePdf() { return invoicePdf; }
    public void setInvoicePdf(String invoicePdf) { this.invoicePdf = invoicePdf; }

    public String getDeliveryReceiptPdf() { return deliveryReceiptPdf; }
    public void setDeliveryReceiptPdf(String deliveryReceiptPdf) { this.deliveryReceiptPdf = deliveryReceiptPdf; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "SupplierOrder{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", status=" + status +
                ", supplierName='" + supplierName + '\'' +
                ", type=" + type +
                '}';
    }
}