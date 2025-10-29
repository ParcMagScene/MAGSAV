package com.magscene.magsav.desktop.model;

import java.time.LocalDateTime;

/**
 * DTO pour ServiceRequest côté JavaFX
 */
public class ServiceRequest {
    
    public enum ServiceRequestType {
        REPAIR, MAINTENANCE, INSTALLATION, TRAINING, RMA, WARRANTY
    }
    
    public enum ServiceRequestStatus {
        OPEN, IN_PROGRESS, WAITING_PARTS, RESOLVED, CLOSED, CANCELLED
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    private Long id;
    private String title;
    private String description;
    private ServiceRequestType type;
    private ServiceRequestStatus status;
    private Priority priority;
    private String requesterName;
    private String requesterEmail;
    private String assignedTechnician;
    private Double estimatedCost;
    private Double actualCost;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime updatedAt;
    private Equipment equipment;

    // Constructeurs
    public ServiceRequest() {}

    public ServiceRequest(Long id, String title, String description, ServiceRequestType type,
                         ServiceRequestStatus status, Priority priority, String requesterName, 
                         String requesterEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.priority = priority;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceRequestType getType() { return type; }
    public void setType(ServiceRequestType type) { this.type = type; }

    public ServiceRequestStatus getStatus() { return status; }
    public void setStatus(ServiceRequestStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }

    public String getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(String assignedTechnician) { this.assignedTechnician = assignedTechnician; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { this.actualCost = actualCost; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}