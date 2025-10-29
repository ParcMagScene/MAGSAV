package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant une personne de contact au sein d'un client
 * Gestion des interlocuteurs et responsables dans les entreprises clientes
 */
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String lastName;

    @Size(max = 100, message = "Le poste ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String jobTitle;

    @Size(max = 100, message = "Le département ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String department;

    @Email(message = "L'email doit être valide")
    @Size(max = 200, message = "L'email ne peut pas dépasser 200 caractères")
    @Column(length = 200)
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(length = 20)
    private String phone;

    @Size(max = 20, message = "Le mobile ne peut pas dépasser 20 caractères")
    @Column(length = 20)
    private String mobile;

    @Size(max = 20, message = "Le téléphone direct ne peut pas dépasser 20 caractères")
    @Column(length = 20)
    private String directPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type = ContactType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status = ContactStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean isPrimary = false;

    @Column(nullable = false)
    private Boolean isDecisionMaker = false;

    @Column(nullable = false)
    private Boolean receiveMarketing = true;

    private LocalDate birthDate;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String notes;

    @Size(max = 200, message = "L'adresse LinkedIn ne peut pas dépasser 200 caractères")
    @Column(length = 200)
    private String linkedinProfile;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    // Enums
    public enum ContactType {
        STANDARD("Contact standard"),
        TECHNICAL("Contact technique"),
        FINANCIAL("Contact financier"),
        COMMERCIAL("Contact commercial"),
        DECISION_MAKER("Décisionnaire"),
        PROJECT_MANAGER("Chef de projet");

        private final String displayName;

        ContactType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ContactStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        ON_LEAVE("En congé"),
        LEFT_COMPANY("A quitté l'entreprise");

        private final String displayName;

        ContactStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public Contact() {
    }

    public Contact(String firstName, String lastName, String email, Client client) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.client = client;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDirectPhone() {
        return directPhone;
    }

    public void setDirectPhone(String directPhone) {
        this.directPhone = directPhone;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Boolean getIsDecisionMaker() {
        return isDecisionMaker;
    }

    public void setIsDecisionMaker(Boolean isDecisionMaker) {
        this.isDecisionMaker = isDecisionMaker;
    }

    public Boolean getReceiveMarketing() {
        return receiveMarketing;
    }

    public void setReceiveMarketing(Boolean receiveMarketing) {
        this.receiveMarketing = receiveMarketing;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLinkedinProfile() {
        return linkedinProfile;
    }

    public void setLinkedinProfile(String linkedinProfile) {
        this.linkedinProfile = linkedinProfile;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    // Méthodes utilitaires
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayText() {
        StringBuilder sb = new StringBuilder(getFullName());
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            sb.append(" - ").append(jobTitle);
        }
        if (department != null && !department.trim().isEmpty()) {
            sb.append(" (").append(department).append(")");
        }
        return sb.toString();
    }

    public String getPrimaryPhone() {
        if (mobile != null && !mobile.trim().isEmpty()) {
            return mobile;
        }
        if (directPhone != null && !directPhone.trim().isEmpty()) {
            return directPhone;
        }
        return phone;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", isPrimary=" + isPrimary +
                '}';
    }
}