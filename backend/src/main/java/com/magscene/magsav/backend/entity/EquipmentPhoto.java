package com.magscene.magsav.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une photo ou un document associé à un équipement
 */
@Entity
@Table(name = "equipment_photos")
public class EquipmentPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Équipement associé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @JsonBackReference
    private Equipment equipment;
    
    // Nom du fichier original
    @Column(nullable = false, length = 255)
    private String fileName;
    
    // Chemin de stockage du fichier
    @Column(nullable = false, length = 500)
    private String filePath;
    
    // Taille du fichier en octets
    private Long fileSize;
    
    // Type MIME du fichier
    @Column(length = 100)
    private String mimeType;
    
    // Description/légende de la photo
    @Column(length = 500)
    private String description;
    
    // Marquer comme photo principale
    @Column(nullable = false)
    private Boolean isPrimary = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public EquipmentPhoto() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public EquipmentPhoto(Equipment equipment, String fileName, String filePath) {
        this();
        this.equipment = equipment;
        this.fileName = fileName;
        this.filePath = filePath;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
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
    
    // Méthodes utilitaires
    
    /**
     * Vérifie si le fichier est une image
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * Vérifie si le fichier est un document
     */
    public boolean isDocument() {
        return !isImage();
    }
    
    /**
     * Formate la taille du fichier pour affichage
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Obtient l'extension du fichier
     */
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Obtient le nom sans extension
     */
    public String getFileNameWithoutExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "EquipmentPhoto{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", isPrimary=" + isPrimary +
                ", equipment=" + (equipment != null ? equipment.getName() : "null") +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquipmentPhoto photo = (EquipmentPhoto) obj;
        return id != null && id.equals(photo.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}