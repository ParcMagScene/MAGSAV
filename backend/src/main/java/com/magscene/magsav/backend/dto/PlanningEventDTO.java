package com.magscene.magsav.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO pour les événements de planning unifié
 * Représente un événement (personnel ou véhicule) dans le planning global
 */
public class PlanningEventDTO {

    public enum EventType {
        PERSONNEL_ASSIGNMENT, // Affectation personnel sur un projet
        PERSONNEL_LEAVE, // Congé personnel
        PERSONNEL_TRAINING, // Formation
        VEHICLE_RESERVATION, // Réservation véhicule
        VEHICLE_MAINTENANCE, // Maintenance véhicule
        VEHICLE_RENTAL // Location externe
    }

    public enum EventStatus {
        CONFIRMED,
        TENTATIVE,
        CANCELLED
    }

    private Long id;
    private EventType type;
    private EventStatus status;

    // Ressource concernée
    private Long resourceId;
    private String resourceName;
    private String resourceType; // "PERSONNEL" ou "VEHICLE"

    // Dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Détails
    private String title;
    private String description;
    private String location;
    private String projectName;
    private Long projectId;

    // Conflits
    private boolean hasConflict;
    private String conflictDescription;

    // Métadonnées
    private String color; // Code couleur pour affichage
    private String createdBy;
    private LocalDateTime createdAt;

    // Constructeurs
    public PlanningEventDTO() {
    }

    public PlanningEventDTO(EventType type, Long resourceId, String resourceName, String resourceType,
            LocalDateTime startDate, LocalDateTime endDate, String title) {
        this.type = type;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.status = EventStatus.CONFIRMED;
        this.hasConflict = false;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public boolean isHasConflict() {
        return hasConflict;
    }

    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public String getConflictDescription() {
        return conflictDescription;
    }

    public void setConflictDescription(String conflictDescription) {
        this.conflictDescription = conflictDescription;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
