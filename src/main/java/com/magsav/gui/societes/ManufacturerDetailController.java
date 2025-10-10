package com.magsav.gui.societes;

import com.magsav.model.Company;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.ResourceBundle;

public class ManufacturerDetailController implements Initializable {
    
    // Références aux composants FXML (noms dans le FXML existant)
    @FXML private ImageView imgLogo;
    @FXML private Label lblName;
    @FXML private Label lblCount;
    @FXML private TableView<Object> table;
    
    // Modèle de données
    private Company manufacturer;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ManufacturerDetailController initialisé");
    }
    
    /**
     * Définit le fabricant à afficher
     */
    public void setManufacturer(Company manufacturer) {
        this.manufacturer = manufacturer;
        loadManufacturerData();
    }
    
    /**
     * Charge les données du fabricant dans l'interface
     */
    private void loadManufacturerData() {
        if (manufacturer == null) return;
        
        // Informations de base
        if (lblName != null) {
            lblName.setText(manufacturer.getName());
        }
        if (lblCount != null) {
            lblCount.setText("Fabricant ID: " + manufacturer.getId());
        }
        
        System.out.println("Données du fabricant chargées: " + manufacturer.getName());
    }
    
    /**
     * Action pour choisir un logo (requis par le FXML)
     */
    @FXML
    private void onChooseLogo() {
        System.out.println("Sélection de logo pour: " + (manufacturer != null ? manufacturer.getName() : "fabricant inconnu"));
        // TODO: Implémenter la sélection de logo
    }
}