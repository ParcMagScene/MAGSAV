package com.magsav.entities;

/**
 * Énumération des formats de catalogue supportés
 */
public enum CatalogFormat {
    EXCEL("Excel (.xlsx, .xls)"),
    PDF("PDF"),
    CSV("CSV"),
    XML("XML"),
    JSON("JSON"),
    CUSTOM_ALGAM("Format Algam"),
    CUSTOM_AXENTE("Format Axente"),
    CUSTOM_WURTH("Format Würth"),
    CUSTOM_OTHER("Format personnalisé");
    
    private final String displayName;
    
    CatalogFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public boolean isCustomFormat() {
        return name().startsWith("CUSTOM_");
    }
}