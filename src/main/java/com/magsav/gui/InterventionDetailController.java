package com.magsav.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.magsav.model.InterventionRow;
import java.net.URL;
import java.util.ResourceBundle;

public class InterventionDetailController implements Initializable {
  @FXML private Label lblId;
  @FXML private TextField txtTitre;
  @FXML private ComboBox<String> cbClient, cbTechnicien, cbStatut;
  @FXML private DatePicker dpDate;
  @FXML private TextArea txtDescription, txtNotes;
  @FXML private Button btnSave, btnCancel;
  
  private long interventionId;
  
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Initialiser les ComboBox
    if (cbStatut != null) {
      cbStatut.setItems(FXCollections.observableArrayList(
        "PLANIFIEE", "EN_COURS", "TERMINEE", "ANNULEE"
      ));
    }
  }

  public void setInterventionId(long id) { 
    this.interventionId = id;
    load(); 
  }
  
  public void load() { 
    InterventionRow intervention = new com.magsav.repo.InterventionRepository().findById(interventionId);
    if (intervention != null) {
      if (lblId != null) lblId.setText(String.valueOf(intervention.id()));
      if (txtTitre != null) txtTitre.setText(intervention.produitNom());
      if (txtDescription != null) txtDescription.setText(intervention.panne());
      if (cbStatut != null) cbStatut.setValue(intervention.statut());
    }
  }
  
  @FXML
  private void saveIntervention() {
    // TODO: Implémenter la sauvegarde
    System.out.println("Sauvegarde intervention: " + interventionId);
  }
  
  @FXML
  private void cancel() {
    // Fermer la fenêtre
    btnCancel.getScene().getWindow().hide();
  }
}