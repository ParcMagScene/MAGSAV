package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Repair pour la gestion des réparations SAV
 */
@Entity
@Table(name = "repairs")
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de réparation est obligatoire")
    @Column(name = "repair_number", unique = true, nullable = false)
    private String repairNumber;

    @NotBlank(message = "L'équipement est obligatoire")
    @Column(name = "equipment_name", nullable = false)
    private String equipmentName;

    @Column(name = "equipment_serial_number")
    private String equipmentSerialNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    @Column(name = "status", nullable = false)
    private RepairStatus status = RepairStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private RepairPriority priority = RepairPriority.NORMAL;

    @Column(name = "problem_description", length = 2000)
    private String problemDescription;

    @Column(name = "diagnosis", length = 2000)
    private String diagnosis;

    @Column(name = "solution", length = 2000)
    private String solution;

    @Column(name = "technician_name")
    private String technicianName;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_contact")
    private String customerContact;

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    @Column(name = "actual_cost")
    private Double actualCost;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Column(name = "actual_duration_hours")
    private Integer actualDurationHours;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "parts_needed", length = 1000)
    private String partsNeeded;

    @Column(name = "warranty_covered")
    private Boolean warrantyCovered = false;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RepairStatus {
        IN_PROGRESS("En cours"),
        WAITING_PARTS("En attente de pièces"),
        COMPLETED("Effectuée"),
        CANCELLED("Annulée"),

        // ANCIENS STATUTS - À SUPPRIMER APRÈS MIGRATION COMPLÈTE
        DIAGNOSTIC("En cours"), // Alias vers IN_PROGRESS
        INITIATED("En cours"), // Alias vers IN_PROGRESS
        DIAGNOSED("En cours"), // Alias vers IN_PROGRESS
        ON_HOLD("En attente de pièces"); // Alias vers WAITING_PARTS

        private final String displayName;

        RepairStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RepairPriority {
        LOW("Basse"),
        NORMAL("Normale"),
        HIGH("Haute"),
        URGENT("Urgente");

        private final String displayName;

        RepairPriority(String displayName) {
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

    public String getRepairNumber() {
        return repairNumber;
    }

    public void setRepairNumber(String repairNumber) {
        this.repairNumber = repairNumber;
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

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public RepairPriority getPriority() {
        return priority;
    }

    public void setPriority(RepairPriority priority) {
        this.priority = priority;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
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

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Double getActualCost() {
        return actualCost;
    }

    public void setActualCost(Double actualCost) {
        this.actualCost = actualCost;
    }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public Integer getActualDurationHours() {
        return actualDurationHours;
    }

    public void setActualDurationHours(Integer actualDurationHours) {
        this.actualDurationHours = actualDurationHours;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getPartsNeeded() {
        return partsNeeded;
    }

    public void setPartsNeeded(String partsNeeded) {
        this.partsNeeded = partsNeeded;
    }

    public Boolean getWarrantyCovered() {
        return warrantyCovered;
    }

    public void setWarrantyCovered(Boolean warrantyCovered) {
        this.warrantyCovered = warrantyCovered;
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
