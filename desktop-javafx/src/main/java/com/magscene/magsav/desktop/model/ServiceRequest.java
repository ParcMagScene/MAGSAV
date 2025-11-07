package com.magscene.magsav.desktop.model;

import com.magscene.magsav.desktop.component.DetailPanelProvider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import com.magscene.magsav.desktop.component.DetailPanel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO pour ServiceRequest côté JavaFX
 */
public class ServiceRequest implements DetailPanelProvider {
    
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
    private String equipmentName;

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

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

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

    // Implémentation de DetailPanelProvider
    @Override
    public String getDetailTitle() {
        return title != null ? title : "Demande SAV sans titre";
    }

    @Override
    public String getDetailSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        
        if (type != null) {
            subtitle.append(getTypeIcon()).append(" ").append(type.name());
        }
        
        if (priority != null) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append(getPriorityIcon()).append(" ").append(priority.name());
        }
        
        if (assignedTechnician != null && !assignedTechnician.trim().isEmpty()) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append("Technicien: ").append(assignedTechnician);
        }
        
        return subtitle.toString();
    }

    @Override
    public Image getDetailImage() {
        // Pour l'instant, pas d'image spécifique pour les demandes SAV
        return null;
    }

    @Override
    public String getQRCodeData() {
        StringBuilder qrData = new StringBuilder();
        qrData.append("SAV|");
        qrData.append("ID:").append(id != null ? id : "").append("|");
        qrData.append("TITLE:").append(title != null ? title : "").append("|");
        qrData.append("TYPE:").append(type != null ? type.name() : "").append("|");
        qrData.append("STATUS:").append(status != null ? status.name() : "");
        return qrData.toString();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(8);
        
        if (requesterName != null && !requesterName.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Demandeur", requesterName));
        }
        
        if (requesterEmail != null && !requesterEmail.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Email", requesterEmail));
        }
        
        if (description != null && !description.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Description", description));
        }
        
        if (status != null) {
            content.getChildren().add(DetailPanel.createInfoRow("Statut", getStatusIcon() + " " + status.name()));
        }
        
        if (estimatedCost != null && estimatedCost > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Coût estimé", String.format("%.2f €", estimatedCost)));
        }
        
        if (actualCost != null && actualCost > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Coût réel", String.format("%.2f €", actualCost)));
        }
        
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            content.getChildren().add(DetailPanel.createInfoRow("Créé le", createdAt.format(formatter)));
        }
        
        if (resolvedAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            content.getChildren().add(DetailPanel.createInfoRow("Résolu le", resolvedAt.format(formatter)));
        }
        
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Notes de résolution", resolutionNotes));
        }
        
        return content;
    }

    @Override
    public String getDetailId() {
        return id != null ? id.toString() : "";
    }

    // Méthodes utilitaires pour les icônes
    private String getTypeIcon() {
        if (type == null) return "❓";
        switch (type) {
            case REPAIR: return "🔧";
            case MAINTENANCE: return "🛠️";
            case INSTALLATION: return "📦";
            case TRAINING: return "📚";
            case RMA: return "↩️";
            case WARRANTY: return "🛡️";
            default: return "❓";
        }
    }
    
    private String getPriorityIcon() {
        if (priority == null) return "❓";
        switch (priority) {
            case LOW: return "🟢";
            case MEDIUM: return "🟡";
            case HIGH: return "🟠";
            case URGENT: return "🔴";
            default: return "❓";
        }
    }
    
    private String getStatusIcon() {
        if (status == null) return "❓";
        switch (status) {
            case OPEN: return "📋";
            case IN_PROGRESS: return "⚙️";
            case WAITING_PARTS: return "⏳";
            case RESOLVED: return "✅";
            case CLOSED: return "🔒";
            case CANCELLED: return "❌";
            default: return "❓";
        }
    }
}
