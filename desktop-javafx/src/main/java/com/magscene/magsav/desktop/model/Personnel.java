package com.magscene.magsav.desktop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant un membre du personnel
 */
public class Personnel {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String contractType;
    private String qualification;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private Double hourlyRate;
    private Boolean active;
    private String notes;
    private String avatarPath; // Chemin vers l'avatar dans le dossier Avatars
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Personnel() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public LocalDate getTerminationDate() { return terminationDate; }
    public void setTerminationDate(LocalDate terminationDate) { this.terminationDate = terminationDate; }
    
    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return getFullName() + " - " + position;
    }
}
