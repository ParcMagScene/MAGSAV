package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * Entité représentant un projet d'installation ou de vente
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du projet est obligatoire")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String projectNumber; // Numéro de projet unique

    @NotNull(message = "Le type de projet est obligatoire")
    @Enumerated(EnumType.STRING)
    private ProjectType type;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @NotNull(message = "La priorité est obligatoire")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Client information
    @NotBlank(message = "Le nom du client est obligatoire")
    @Column(nullable = false, length = 200)
    private String clientName;

    @Column(length = 200)
    private String clientContact;

    @Column(length = 100)
    private String clientEmail;

    @Column(length = 50)
    private String clientPhone;

    @Column(columnDefinition = "TEXT")
    private String clientAddress;

    // Relation avec le client (optionnelle pour compatibilité)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // Dates importantes
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    // Informations financières
    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    @Column(name = "estimated_amount", precision = 10, scale = 2)
    private BigDecimal estimatedAmount;

    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "remaining_amount", precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    // Localisation
    @Column(length = 200)
    private String venue; // Lieu d'installation

    @Column(columnDefinition = "TEXT")
    private String venueAddress;

    @Column(length = 200)
    private String venueContact;

    // Responsables
    @Column(name = "project_manager", length = 100)
    private String projectManager;

    @Column(name = "technical_manager", length = 100)
    private String technicalManager;

    @Column(name = "sales_representative", length = 100)
    private String salesRepresentative;

    // Équipe assignée (relation avec Personnel)
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_team", 
                    joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "personnel_id")
    private Set<Long> teamMemberIds = new HashSet<>();

    // Équipements requis (relation avec Equipment)
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_equipment", 
                    joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "equipment_id")
    private Set<Long> equipmentIds = new HashSet<>();

    // Documents et fichiers
    @Column(name = "quote_pdf_path")
    private String quotePdfPath;

    @Column(name = "contract_pdf_path")
    private String contractPdfPath;

    @Column(name = "technical_sheet_path")
    private String technicalSheetPath;

    // Notes et observations
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String technicalNotes;

    @Column(columnDefinition = "TEXT")
    private String clientRequirements;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Énumérations
    public enum ProjectType {
        SALE,           // Vente simple
        INSTALLATION,   // Installation complète
        RENTAL,         // Location
        MAINTENANCE,    // Contrat de maintenance
        EVENT,          // Événement ponctuel
        SERVICE         // Prestation de service
    }

    public enum ProjectStatus {
        DRAFT,          // Brouillon
        QUOTED,         // Devis envoyé
        NEGOTIATION,    // En négociation
        CONFIRMED,      // Confirmé
        IN_PROGRESS,    // En cours
        INSTALLED,      // Installé
        COMPLETED,      // Terminé
        CANCELLED,      // Annulé
        ON_HOLD        // En attente
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    // Constructors
    public Project() {}

    public Project(String name, ProjectType type, ProjectStatus status, String clientName) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.clientName = clientName;
        this.priority = Priority.MEDIUM;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProjectNumber() { return projectNumber; }
    public void setProjectNumber(String projectNumber) { this.projectNumber = projectNumber; }

    public ProjectType getType() { return type; }
    public void setType(ProjectType type) { this.type = type; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientContact() { return clientContact; }
    public void setClientContact(String clientContact) { this.clientContact = clientContact; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientAddress() { return clientAddress; }
    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getVenueContact() { return venueContact; }
    public void setVenueContact(String venueContact) { this.venueContact = venueContact; }

    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }

    public String getTechnicalManager() { return technicalManager; }
    public void setTechnicalManager(String technicalManager) { this.technicalManager = technicalManager; }

    public String getSalesRepresentative() { return salesRepresentative; }
    public void setSalesRepresentative(String salesRepresentative) { this.salesRepresentative = salesRepresentative; }

    public Set<Long> getTeamMemberIds() { return teamMemberIds; }
    public void setTeamMemberIds(Set<Long> teamMemberIds) { this.teamMemberIds = teamMemberIds; }

    public Set<Long> getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(Set<Long> equipmentIds) { this.equipmentIds = equipmentIds; }

    public String getQuotePdfPath() { return quotePdfPath; }
    public void setQuotePdfPath(String quotePdfPath) { this.quotePdfPath = quotePdfPath; }

    public String getContractPdfPath() { return contractPdfPath; }
    public void setContractPdfPath(String contractPdfPath) { this.contractPdfPath = contractPdfPath; }

    public String getTechnicalSheetPath() { return technicalSheetPath; }
    public void setTechnicalSheetPath(String technicalSheetPath) { this.technicalSheetPath = technicalSheetPath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTechnicalNotes() { return technicalNotes; }
    public void setTechnicalNotes(String technicalNotes) { this.technicalNotes = technicalNotes; }

    public String getClientRequirements() { return clientRequirements; }
    public void setClientRequirements(String clientRequirements) { this.clientRequirements = clientRequirements; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", projectNumber='" + projectNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}