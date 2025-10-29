package com.magscene.magsav.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * EntitÃƒÂ© reprÃƒÂ©sentant une catÃƒÂ©gorie d'ÃƒÂ©quipement avec hiÃƒÂ©rarchie
 */
@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    // CatÃƒÂ©gorie parent pour la hiÃƒÂ©rarchie
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Category parent;
    
    // Sous-catÃƒÂ©gories
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Category> children = new ArrayList<>();
    
    // Ãƒâ€°quipements dans cette catÃƒÂ©gorie
    @OneToMany(mappedBy = "categoryEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"categoryEntity", "hibernateLazyInitializer", "handler"})
    private List<Equipment> equipment = new ArrayList<>();
    
    // Couleur pour l'interface utilisateur (code hexadÃƒÂ©cimal)
    @Column(length = 7)
    private String color;
    
    // IcÃƒÂ´ne associÃƒÂ©e ÃƒÂ  la catÃƒÂ©gorie
    @Column(length = 100)
    private String icon;
    
    // Ordre d'affichage
    private Integer displayOrder = 0;
    
    // Statut actif/inactif
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Category() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Category(String name) {
        this();
        this.name = name;
    }
    
    public Category(String name, String description) {
        this(name);
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
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Category getParent() {
        return parent;
    }
    
    public void setParent(Category parent) {
        this.parent = parent;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }
    
    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }
    
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.updatedAt = LocalDateTime.now();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
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
    
    // MÃƒÂ©thodes utilitaires
    
    /**
     * Obtient le niveau de la catÃƒÂ©gorie dans la hiÃƒÂ©rarchie (0 = racine)
     */
    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }
    
    /**
     * Obtient le chemin complet de la catÃƒÂ©gorie (ex: "Ãƒâ€°clairage > Projecteurs > LED")
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }
    
    /**
     * VÃƒÂ©rifie si cette catÃƒÂ©gorie est racine (sans parent)
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * VÃƒÂ©rifie si cette catÃƒÂ©gorie a des enfants
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    /**
     * VÃƒÂ©rifie si cette catÃƒÂ©gorie a des ÃƒÂ©quipements
     */
    public boolean hasEquipment() {
        return equipment != null && !equipment.isEmpty();
    }
    
    /**
     * Ajoute une sous-catÃƒÂ©gorie
     */
    public void addChild(Category child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
    }
    
    /**
     * Supprime une sous-catÃƒÂ©gorie
     */
    public void removeChild(Category child) {
        if (children != null) {
            children.remove(child);
            child.setParent(null);
        }
    }
    
    /**
     * Ajoute un ÃƒÂ©quipement ÃƒÂ  cette catÃƒÂ©gorie
     */
    public void addEquipment(Equipment equipment) {
        if (this.equipment == null) {
            this.equipment = new ArrayList<>();
        }
        this.equipment.add(equipment);
        equipment.setCategoryEntity(this);
    }
    
    /**
     * Supprime un ÃƒÂ©quipement de cette catÃƒÂ©gorie
     */
    public void removeEquipment(Equipment equipment) {
        if (this.equipment != null) {
            this.equipment.remove(equipment);
            equipment.setCategoryEntity(null);
        }
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + getLevel() +
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id != null && id.equals(category.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

