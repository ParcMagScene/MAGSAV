package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Entité ServiceRequest pour le module SAV
 * Entity JPA pour le backend Spring Boot
 */
@Entity
@Table(name = "service_request")
public class ServiceRequest {
    
    // Énumérations internes
    public enum Priority {
        LOW("Basse"),
        MEDIUM("Moyenne"), 
        HIGH("Haute"),
        URGENT("Urgente");
        
        private final String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum ServiceRequestStatus {
        // Statuts pour les demandes
        OPEN("Ouverte"),
        VALIDATED("Validée"),

        // Statuts pour les interventions
        IN_PROGRESS("En cours"),
        WAITING_PARTS("Attente pièces"),
        RESOLVED("Résolue"),
        CANCELLED("Annulée"),
        EXTERNAL("Externe"),
        CLOSED("Fermée");
        
        private final String displayName;
        
        ServiceRequestStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum ServiceRequestType {
        REPAIR("Réparation"),
        MAINTENANCE("Maintenance préventive"),
        INSTALLATION("Installation"),
        TRAINING("Formation"),
        RMA("Retour marchandise"),
        WARRANTY("Garantie");
        
        private final String displayName;
        
        ServiceRequestType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    // Attributs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200)
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)")
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ServiceRequestType type;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
    
    @Column(name = "requester_name")
    private String requesterName;
    
    @Column(name = "requester_email")
    @Email(message = "Email invalide")
    private String requesterEmail;
    
    @Column(name = "assigned_technician")
    private String assignedTechnician;
    
    @Column(name = "estimated_cost")
    private Double estimatedCost;
    
    @Column(name = "actual_cost")
    private Double actualCost;
    
    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Column(name = "resolved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    // Constructeurs
    public ServiceRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ServiceRequest(String title, ServiceRequestType type, Equipment equipment) {
        this();
        this.title = title;
        this.type = type;
        this.equipment = equipment;
    }
    
    // Getters et Setters avec mise à jour automatique de updatedAt
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        updateTimestamp();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        updateTimestamp();
    }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { 
        this.priority = priority;
        updateTimestamp();
    }
    
    public ServiceRequestStatus getStatus() { return status; }
    public void setStatus(ServiceRequestStatus status) { 
        this.status = status;
        if (status == ServiceRequestStatus.RESOLVED || status == ServiceRequestStatus.CLOSED) {
            this.resolvedAt = LocalDateTime.now();
        }
        updateTimestamp();
    }
    
    public ServiceRequestType getType() { return type; }
    public void setType(ServiceRequestType type) { 
        this.type = type;
        updateTimestamp();
    }
    
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { 
        this.equipment = equipment;
        updateTimestamp();
    }
    
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { 
        this.requesterName = requesterName;
        updateTimestamp();
    }
    
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { 
        this.requesterEmail = requesterEmail;
        updateTimestamp();
    }
    
    public String getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(String assignedTechnician) { 
        this.assignedTechnician = assignedTechnician;
        updateTimestamp();
    }
    
    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { 
        this.estimatedCost = estimatedCost;
        updateTimestamp();
    }
    
    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { 
        this.actualCost = actualCost;
        updateTimestamp();
    }
    
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { 
        this.resolutionNotes = resolutionNotes;
        updateTimestamp();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTimestamp();
    }
    
    @Override
    public String toString() {
        return "ServiceRequest{id=%d, title='%s', type=%s, status=%s}"
            .formatted(id, title, type, status);
    }
}
