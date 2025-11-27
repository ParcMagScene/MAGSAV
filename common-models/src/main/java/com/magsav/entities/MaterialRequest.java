package com.magsav.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magsav.enums.RequestContext;
import com.magsav.enums.RequestStatus;
import com.magsav.enums.RequestUrgency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entité représentant une demande de matériel par un membre du personnel
 */
@Entity
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" }, ignoreUnknown = true)
@Table(name = "material_requests", indexes = {
        @Index(name = "idx_request_number", columnList = "request_number"),
        @Index(name = "idx_requester_email", columnList = "requester_email"),
        @Index(name = "idx_request_status", columnList = "status"),
        @Index(name = "idx_request_context", columnList = "context")
})
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, length = 50)
    private String requestNumber;

    // Demandeur
    @Column(name = "requester_name", nullable = false, length = 100)
    private String requesterName;

    @Column(name = "requester_email", nullable = false, length = 150)
    private String requesterEmail;

    @Column(name = "requester_department", length = 100)
    private String requesterDepartment;

    // Contexte de la demande
    @Enumerated(EnumType.STRING)
    @Column(name = "context", nullable = false, length = 20)
    private RequestContext context;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private MaterialProject project; // Pour affaires/installations

    @Column(name = "urgency", length = 20)
    @Enumerated(EnumType.STRING)
    private RequestUrgency urgency = RequestUrgency.NORMAL;

    // Description de la demande
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    // Statut et workflow
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Métadonnées
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @JsonIgnore
    @OneToMany(mappedBy = "materialRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MaterialRequestItem> items = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "materialRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderAllocation> orderAllocations = new HashSet<>();

    // Constructeurs
    public MaterialRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        generateRequestNumber();
    }

    public MaterialRequest(String requesterName, String requesterEmail, RequestContext context) {
        this();
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.context = context;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequesterDepartment() {
        return requesterDepartment;
    }

    public void setRequesterDepartment(String requesterDepartment) {
        this.requesterDepartment = requesterDepartment;
    }

    public RequestContext getContext() {
        return context;
    }

    public void setContext(RequestContext context) {
        this.context = context;
    }

    public MaterialProject getProject() {
        return project;
    }

    public void setProject(MaterialProject project) {
        this.project = project;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
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

    public Set<MaterialRequestItem> getItems() {
        return items;
    }

    public void setItems(Set<MaterialRequestItem> items) {
        this.items = items;
    }

    public Set<OrderAllocation> getOrderAllocations() {
        return orderAllocations;
    }

    public void setOrderAllocations(Set<OrderAllocation> orderAllocations) {
        this.orderAllocations = orderAllocations;
    }

    // Méthodes utilitaires
    private void generateRequestNumber() {
        this.requestNumber = "REQ-" + System.currentTimeMillis();
    }

    public void addItem(MaterialRequestItem item) {
        items.add(item);
        item.setMaterialRequest(this);
    }

    public void removeItem(MaterialRequestItem item) {
        items.remove(item);
        item.setMaterialRequest(null);
    }

    public void submitForApproval() {
        if (status == RequestStatus.DRAFT) {
            this.status = RequestStatus.PENDING_APPROVAL;
            this.submittedAt = LocalDateTime.now();
        }
    }

    public void approve(String approver) {
        this.status = RequestStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String rejectionReason) {
        this.status = RequestStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    public boolean canBeModified() {
        return status == RequestStatus.DRAFT;
    }

    public boolean isPendingApproval() {
        return status == RequestStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return status == RequestStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == RequestStatus.REJECTED;
    }

    public boolean requiresProject() {
        return context == RequestContext.SALES || context == RequestContext.INSTALLATION;
    }

    public int getTotalItemsCount() {
        return items.stream().mapToInt(MaterialRequestItem::getRequestedQuantity).sum();
    }

    public boolean hasUnallocatedItems() {
        return items.stream().anyMatch(item -> item.getRequestedQuantity() > item.getQuantityAllocated());
    }

    @Override
    public String toString() {
        return requestNumber + " - " + requesterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MaterialRequest))
            return false;
        MaterialRequest that = (MaterialRequest) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
