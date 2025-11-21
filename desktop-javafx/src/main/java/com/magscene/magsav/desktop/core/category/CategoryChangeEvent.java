package com.magscene.magsav.desktop.core.category;

/**
 * Événement de changement de catégorie
 * 
 * @version 3.0.0-refactored
 */
public class CategoryChangeEvent {
    
    private final CategoryType type;
    private final CategoryAction action;
    private final Category category;
    private final long timestamp;
    
    public CategoryChangeEvent(CategoryType type, CategoryAction action, Category category) {
        this.type = type;
        this.action = action;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }
    
    public CategoryType getType() {
        return type;
    }
    
    public CategoryAction getAction() {
        return action;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("CategoryChangeEvent{type=%s, action=%s, category=%s, timestamp=%d}",
                type, action, category.getName(), timestamp);
    }
}