package com.magsav.gui.categories;

import javafx.fxml.FXML;

public class MinimalCategoriesController {
    
    @FXML
    private void initialize() {
        System.out.println("DEBUG: MinimalCategoriesController - initialize() appelé");
    }
    
    @FXML
    private void onAdd() {
        System.out.println("DEBUG: onAdd appelé");
    }
    
    @FXML
    private void onEdit() {
        System.out.println("DEBUG: onEdit appelé");
    }
    
    @FXML
    private void onDelete() {
        System.out.println("DEBUG: onDelete appelé");
    }
}