package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité RMA (Return Merchandise Authorization) pour la gestion des retours
 * matériel
 */
@Entity
@Table(name = "rmas")
public class RMA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro RMA est obligatoire")
    @Column(name = "rma_number", unique = true, nullable = false)
    private String rmaNumber;

    @NotBlank(message = "L'équipement est obligatoire")
    @Column(name = "equipment_name", nullable = false)
    private String equipmentName;

    @Column(name = "equipment_serial_number")
    private String equipmentSerialNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le motif est obligatoire")
    @Column(name = "reason", nullable = false)
    private RMAReasonType reason;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    @Column(name = "status", nullable = false)
    private RMAStatus status = RMAStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private RMAPriority priority = RMAPriority.NORMAL;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_contact")
    private String customerContact;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "shipment_date")
    private LocalDate shipmentDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "estimated_value")
    private Double estimatedValue;

    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "repair_cost")
    private Double repairCost;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "replacement_equipment")
    private String replacementEquipment;

    @Column(name = "analysis_notes", length = 2000)
    private String analysisNotes;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RMAStatus {
        INITIATED("Initié"),
        AUTHORIZED("Autorisé"),
        IN_TRANSIT_RETURN("En transit retour"),
        RECEIVED("Reçu"),
        UNDER_ANALYSIS("En cours d'analyse"),
        REPAIRED("Réparé"),
        REPLACED("Remplacé"),
        REFUNDED("Remboursé"),
        REJECTED("Refusé"),
        CANCELLED("Annulé"),
        COMPLETED("Terminé");

        private final String displayName;

        RMAStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RMAReasonType {
        MANUFACTURING_DEFECT("Défaut de fabrication"),
        TRANSPORT_DAMAGE("Dommage transport"),
        NON_COMPLIANCE("Non-conformité"),
        WARRANTY_END("Fin de garantie"),
        UPGRADE("Upgrade"),
        ORDER_ERROR("Erreur commande"),
        CUSTOMER_REQUEST("Demande client"),
        OTHER("Autre");

        private final String displayName;

        RMAReasonType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RMAPriority {
        LOW("Basse"),
        NORMAL("Normale"),
        HIGH("Haute"),
        URGENT("Urgente");

        private final String displayName;

        RMAPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentSerialNumber() {
        return equipmentSerialNumber;
    }

    public void setEquipmentSerialNumber(String equipmentSerialNumber) {
        this.equipmentSerialNumber = equipmentSerialNumber;
    }

    public RMAReasonType getReason() {
        return reason;
    }

    public void setReason(RMAReasonType reason) {
        this.reason = reason;
    }

    public RMAStatus getStatus() {
        return status;
    }

    public void setStatus(RMAStatus status) {
        this.status = status;
    }

    public RMAPriority getPriority() {
        return priority;
    }

    public void setPriority(RMAPriority priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDate getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(LocalDate shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Double getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(Double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public Double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Double getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(Double repairCost) {
        this.repairCost = repairCost;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReplacementEquipment() {
        return replacementEquipment;
    }

    public void setReplacementEquipment(String replacementEquipment) {
        this.replacementEquipment = replacementEquipment;
    }

    public String getAnalysisNotes() {
        return analysisNotes;
    }

    public void setAnalysisNotes(String analysisNotes) {
        this.analysisNotes = analysisNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
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
}
