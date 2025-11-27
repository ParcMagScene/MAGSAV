package com.magscene.magsav.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.magsav.entities.GroupedOrderStatus;
import com.magsav.enums.RequestUrgency;

/**
 * DTO pour GroupedOrder - évite les problèmes de sérialisation JSON
 */
public class GroupedOrderDTO {
    private Long id;
    private String orderNumber;
    private Long supplierId;
    private String supplierName;
    private GroupedOrderStatus status;
    private BigDecimal currentAmount;
    private boolean thresholdAlertSent;
    private boolean autoValidateOnThreshold;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String validationNotes;
    private String deliveryAddress;
    private LocalDateTime deliveryDate;
    private RequestUrgency urgency;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public GroupedOrderStatus getStatus() {
        return status;
    }

    public void setStatus(GroupedOrderStatus status) {
        this.status = status;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public boolean isThresholdAlertSent() {
        return thresholdAlertSent;
    }

    public void setThresholdAlertSent(boolean thresholdAlertSent) {
        this.thresholdAlertSent = thresholdAlertSent;
    }

    public boolean isAutoValidateOnThreshold() {
        return autoValidateOnThreshold;
    }

    public void setAutoValidateOnThreshold(boolean autoValidateOnThreshold) {
        this.autoValidateOnThreshold = autoValidateOnThreshold;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidationNotes() {
        return validationNotes;
    }

    public void setValidationNotes(String validationNotes) {
        this.validationNotes = validationNotes;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
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
}
