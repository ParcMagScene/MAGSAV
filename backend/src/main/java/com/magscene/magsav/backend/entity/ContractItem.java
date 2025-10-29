package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

/**
 * Entité représentant un article/ligne d'un contrat
 * Permet la décomposition détaillée des prestations contractuelles
 */
@Entity
@Table(name = "contract_items")
public class ContractItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'article est obligatoire")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String itemName;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "La quantité doit être positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    @Column(length = 20)
    private String unit = "Unité";

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être positif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType = ItemType.SERVICE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.ACTIVE;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String notes;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonIgnore
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    @JsonIgnore
    private Equipment equipment;

    // Enums
    public enum ItemType {
        SERVICE("Service"),
        EQUIPMENT("Équipement"),
        MAINTENANCE("Maintenance"),
        RENTAL("Location"),
        SPARE_PART("Pièce détachée"),
        TRAINING("Formation"),
        CONSULTING("Conseil"),
        TRANSPORT("Transport");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ItemStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        DELIVERED("Livré"),
        CANCELLED("Annulé");

        private final String displayName;

        ItemStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public ContractItem() {
    }

    public ContractItem(String itemName, BigDecimal quantity, BigDecimal unitPrice, Contract contract) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.contract = contract;
        calculateTotalPrice();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    // Méthodes utilitaires
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = quantity.multiply(unitPrice);
        }
    }

    public String getDisplayText() {
        StringBuilder sb = new StringBuilder(itemName);
        if (quantity != null) {
            sb.append(" (").append(quantity);
            if (unit != null && !unit.trim().isEmpty()) {
                sb.append(" ").append(unit);
            }
            sb.append(")");
        }
        return sb.toString();
    }

    @PrePersist
    @PreUpdate
    private void updateCalculatedFields() {
        calculateTotalPrice();
    }

    @Override
    public String toString() {
        return "ContractItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", itemType=" + itemType +
                ", status=" + status +
                '}';
    }
}