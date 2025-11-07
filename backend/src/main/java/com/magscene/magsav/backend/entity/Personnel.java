package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Personnel pour la gestion des employés et intervenants
 * Entity JPA pour le backend Spring Boot
 */
@Entity
@Table(name = "personnel")
public class Personnel {
    
    // Énumérations internes
    public enum PersonnelType {
        EMPLOYEE("Employé"),
        FREELANCE("Freelance"),
        INTERN("Stagiaire"),
        TEMPORARY("Intérimaire"),
        PERFORMER("Intermittent du spectacle");
        
        private final String displayName;
        
        PersonnelType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum PersonnelStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        ON_LEAVE("En congé"),
        TERMINATED("Terminé");
        
        private final String displayName;
        
        PersonnelStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Email(message = "Email invalide")
    @Column(name = "email", unique = true)
    private String email;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Format de téléphone invalide")
    @Column(name = "phone")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PersonnelType type = PersonnelType.EMPLOYEE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PersonnelStatus status = PersonnelStatus.ACTIVE;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "specialties", length = 500)
    private String specialties; // Stocké comme texte séparé par des virgules
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Personnel() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Personnel(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        updateTimestamp();
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        updateTimestamp();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email;
        updateTimestamp();
    }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone;
        updateTimestamp();
    }
    
    public PersonnelType getType() { return type; }
    public void setType(PersonnelType type) { 
        this.type = type;
        updateTimestamp();
    }
    
    public PersonnelStatus getStatus() { return status; }
    public void setStatus(PersonnelStatus status) { 
        this.status = status;
        updateTimestamp();
    }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { 
        this.jobTitle = jobTitle;
        updateTimestamp();
    }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { 
        this.department = department;
        updateTimestamp();
    }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { 
        this.hireDate = hireDate;
        updateTimestamp();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes;
        updateTimestamp();
    }
    
    public String getSpecialties() { return specialties; }
    public void setSpecialties(String specialties) { 
        this.specialties = specialties;
        updateTimestamp();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTimestamp();
    }
    
    /**
     * Retourne le nom complet de la personne
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Retourne une représentation pour les ComboBox
     */
    public String getDisplayText() {
        StringBuilder display = new StringBuilder(getFullName());
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            display.append(" - ").append(jobTitle);
        }
        if (department != null && !department.trim().isEmpty()) {
            display.append(" (").append(department).append(")");
        }
        return display.toString();
    }
    
    @Override
    public String toString() {
        return "Personnel{id=%d, fullName='%s', email='%s', type=%s}"
            .formatted(id, getFullName(), email, type);
    }
}