package com.magsav.gui.interventions;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.net.URL;
import java.util.ResourceBundle;

public class InterventionDetailController implements Initializable {
  @FXML private Label lbHeader, lbProduct, lbSerial, lbDetector;
  @FXML private TextArea taDesc, taPrediag;
  @FXML private ComboBox<String> cbNext;

  private long interventionId;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Initialiser la ComboBox "Suite envisagée" avec les choix disponibles
    if (cbNext != null) {
      cbNext.setItems(FXCollections.observableArrayList(
        "Diagnostique MAG",
        "Réparation MAG", 
        "Demande de RMA",
        "Déchetterie",
        "Vente",
        "Sortie pour pièces"
      ));
      // Permettre l'édition libre si nécessaire
      cbNext.setEditable(false);
    }
  }

  public void load(long id) { 
    this.interventionId = id; 
    // TODO: Charger les données de l'intervention depuis la base de données
    // et préremplir cbNext avec la valeur sauvegardée
  }
  
  public void loadProductInfo(com.magsav.repo.ProductRepository.ProductRow product) {
    if (product != null) {
      // Préremplir les champs avec les informations du produit
      if (lbProduct != null) {
        lbProduct.setText(product.nom() + " (" + product.code() + ")");
      }
      if (lbSerial != null) {
        lbSerial.setText(product.sn() != null ? product.sn() : "");
      }
      if (lbDetector != null) {
        lbDetector.setText(product.fabricant() != null ? product.fabricant() : "");
      }
    }
  }
  
  public String getSelectedSuite() {
    return cbNext != null ? cbNext.getValue() : null;
  }
  
  public void setSelectedSuite(String suite) {
    if (cbNext != null && suite != null) {
      cbNext.setValue(suite);
    }
  }
  
  public boolean save() { 
    // TODO: Sauvegarder la valeur de cbNext.getValue() en base de données
    String suiteEnvisagee = getSelectedSuite();
    System.out.println("Suite envisagée sélectionnée: " + suiteEnvisagee);
    return true; 
  }
}