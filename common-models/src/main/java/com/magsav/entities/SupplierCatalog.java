package com.magsav.entities;

import com.magsav.enums.ImportStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un catalogue de produits d'un fournisseur
 */
@Entity
@Table(name = "supplier_catalogs")
public class SupplierCatalog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 50)
    private String version;
    
    @Column(name = "import_date")
    private LocalDateTime importDate;
    
    @Column(name = "file_path", length = 500)
    private String filePath; // Chemin du fichier original
    
    @Column(name = "file_name", length = 200)
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", length = 20)
    private CatalogFormat fileFormat;
    
    @Column(name = "items_count")
    private Integer itemsCount = 0;
    
    @Column(name = "import_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ImportStatus importStatus = ImportStatus.PENDING;
    
    @Column(name = "import_log", columnDefinition = "TEXT")
    private String importLog;
    
    @Column(name = "active")
    private boolean active = true;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @OneToMany(mappedBy = "catalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CatalogItem> items = new HashSet<>();
    
    // Constructeurs
    public SupplierCatalog() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.importDate = LocalDateTime.now();
    }
    
    public SupplierCatalog(Supplier supplier, String name) {
        this();
        this.supplier = supplier;
        this.name = name;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public CatalogFormat getFileFormat() { return fileFormat; }
    public void setFileFormat(CatalogFormat fileFormat) { this.fileFormat = fileFormat; }
    
    public Integer getItemsCount() { return itemsCount; }
    public void setItemsCount(Integer itemsCount) { this.itemsCount = itemsCount; }
    
    public ImportStatus getImportStatus() { return importStatus; }
    public void setImportStatus(ImportStatus importStatus) { this.importStatus = importStatus; }
    
    public String getImportLog() { return importLog; }
    public void setImportLog(String importLog) { this.importLog = importLog; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<CatalogItem> getItems() { return items; }
    public void setItems(Set<CatalogItem> items) { this.items = items; }
    
    // Méthodes utilitaires
    public void addItem(CatalogItem item) {
        items.add(item);
        item.setCatalog(this);
        updateItemsCount();
    }
    
    public void removeItem(CatalogItem item) {
        items.remove(item);
        item.setCatalog(null);
        updateItemsCount();
    }
    
    public void updateItemsCount() {
        this.itemsCount = items.size();
    }
    
    public boolean isImportCompleted() {
        return importStatus == ImportStatus.COMPLETED;
    }
    
    public boolean isImportFailed() {
        return importStatus == ImportStatus.FAILED;
    }
    
    public String getDisplayName() {
        return supplier != null ? supplier.getName() + " - " + name : name;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierCatalog)) return false;
        SupplierCatalog catalog = (SupplierCatalog) o;
        return id != null && id.equals(catalog.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}