package com.magscene.magsav.backend.dto;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.entity.ServiceRequest;
import java.time.LocalDateTime;

public class ServiceRequestDTO {
    public Long id;
    public String title;
    public String description;
    public String priority;
    public String status;
    public String type;
    public String requesterName;
    public String requesterEmail;
    public String assignedTechnician;
    public Double estimatedCost;
    public Double actualCost;
    public String resolutionNotes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime resolvedAt;
    public EquipmentDTO equipment;

    // Constructeur vide pour mapping manuel
    public ServiceRequestDTO() {}

    public static class EquipmentDTO {
        public Long id;
        public String name;
        public String brand;
        public String category;
        public String serialNumber;
        public String locmatCode;
        public String model;
        public EquipmentDTO() {}
    }
}
