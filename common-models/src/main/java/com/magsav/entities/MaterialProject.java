package com.magsav.entities;

import com.magsav.enums.ProjectType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un projet matériel (Phase 1-5 supplier integration)
 * Note: Renommée de "Project" en "MaterialProject" pour éviter collision avec backend.entity.Project
 */
@Entity
@Table(name = "material_projects")
public class MaterialProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "project_code")
    private String projectCode;
    
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    private ProjectType type;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Constructeurs
    public MaterialProject() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = ProjectStatus.ACTIVE;
    }
    
    public MaterialProject(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public ProjectStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProjectStatus status) {
        this.status = status;
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
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public ProjectType getType() {
        return type;
    }
    
    public void setType(ProjectType type) {
        this.type = type;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "MaterialProject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", projectCode='" + projectCode + '\'' +
                ", status=" + status +
                '}';
    }
    
    /**
     * Statut d'un projet
     */
    public enum ProjectStatus {
        ACTIVE("Actif"),
        COMPLETED("Terminé"),
        SUSPENDED("Suspendu"),
        CANCELLED("Annulé");
        
        private final String displayName;
        
        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

