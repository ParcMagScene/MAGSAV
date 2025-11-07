package com.magscene.magsav.common.model;

/**
 * Classe Equipment simplifiée pour les relations entre modules
 */
public class Equipment {
    private Long id;
    
    public Equipment() {}
    
    public Equipment(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}