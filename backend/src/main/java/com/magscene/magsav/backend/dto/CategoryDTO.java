package com.magscene.magsav.backend.dto;

import com.magscene.magsav.backend.entity.Category;
import java.time.LocalDateTime;

/**
 * DTO pour transférer les données de catégorie sans les relations JPA complexes
 */
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentId;
    
    // Constructeur vide
    public CategoryDTO() {}
    
    // Constructeur depuis Category entity
    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.color = category.getColor();
        this.icon = category.getIcon();
        this.displayOrder = category.getDisplayOrder();
        this.active = category.getActive();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
        // Éviter la référence circulaire - juste l'ID du parent
        this.parentId = category.getParent() != null ? category.getParent().getId() : null;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}