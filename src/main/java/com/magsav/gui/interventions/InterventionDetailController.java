package com.magsav.gui.interventions;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.magsav.model.InterventionRow;
import java.net.URL;
import java.util.ResourceBundle;

public class InterventionDetailController implements Initializable {
  @FXML private Label lbHeader, lbProduct, lbSerial, lbDetector;
  @FXML private TextArea taDesc, taPrediag;
  @FXML private ComboBox<String> cbNext;
  
  // Boutons de contrôle d'édition
  @FXML private Button btnEdit;
  @FXML private Button btnSave;
  @FXML private Button btnCancel;

  private long interventionId;
  
  // État d'édition
  private boolean isEditMode = false;
  
  // Valeurs originales pour l'annulation
  private String originalPrediag;
  private String originalSuite;

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
    
    // Initialiser en mode lecture seule
    isEditMode = false;
    updateEditableControls();
  }

  public void load(long id) { 
    this.interventionId = id;
    InterventionRow intervention = new com.magsav.repo.InterventionRepository().findById(id);
    if (intervention != null) {
      if (taDesc != null) taDesc.setText(intervention.panne());
      if (lbHeader != null) lbHeader.setText("Intervention #" + intervention.id());
      
      // Sauvegarder les valeurs originales pour l'annulation
      originalPrediag = taPrediag != null ? taPrediag.getText() : "";
      originalSuite = cbNext != null ? cbNext.getValue() : "";
    }
  }
  
  public void loadProductInfo(com.magsav.repo.ProductRepository.ProductRow product) {
    if (product != null) {
      // Préremplir les champs avec les informations du produit
      if (lbProduct != null) {
        lbProduct.setText(product.nom());
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
  
  @FXML
  private void onToggleEdit() {
    isEditMode = !isEditMode;
    updateEditableControls();
  }
  
  @FXML
  private void onSaveChanges() {
    if (save()) {
      // Sauvegarder les nouvelles valeurs comme originales
      originalPrediag = taPrediag != null ? taPrediag.getText() : "";
      originalSuite = cbNext != null ? cbNext.getValue() : "";
      
      // Revenir en mode lecture
      isEditMode = false;
      updateEditableControls();
      
      // Afficher confirmation
      Alert success = new Alert(Alert.AlertType.INFORMATION);
      success.setTitle("Modifications sauvegardées");
      success.setHeaderText("Intervention mise à jour");
      success.setContentText("Les modifications ont été sauvegardées avec succès.");
      // Appliquer le thème dark
      success.getDialogPane().getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      success.showAndWait();
    }
  }
  
  @FXML
  private void onCancelEdit() {
    // Restaurer les valeurs originales
    if (taPrediag != null) taPrediag.setText(originalPrediag);
    if (cbNext != null) cbNext.setValue(originalSuite);
    
    // Revenir en mode lecture
    isEditMode = false;
    updateEditableControls();
  }
  
  /**
   * Met à jour l'état des contrôles selon le mode d'édition
   */
  private void updateEditableControls() {
    // Activer/désactiver les champs modifiables
    if (taPrediag != null) taPrediag.setEditable(isEditMode);
    if (cbNext != null) cbNext.setDisable(!isEditMode);
    
    // Contrôler la visibilité des boutons de contrôle
    if (btnEdit != null) btnEdit.setVisible(!isEditMode);
    if (btnSave != null) btnSave.setVisible(isEditMode);
    if (btnCancel != null) btnCancel.setVisible(isEditMode);
  }
}