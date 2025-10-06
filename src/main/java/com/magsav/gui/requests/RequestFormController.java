package com.magsav.gui.requests;

import com.magsav.model.Societe;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import java.util.List;

public class RequestFormController {
  @FXML private ComboBox<Societe> cbFournisseur;
  @FXML private TextArea taComment;

  public void init(List<Societe> fournisseurs, Societe currentFournisseur, String commentaire) {
    cbFournisseur.getItems().setAll(fournisseurs);
    cbFournisseur.setButtonCell(new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(Societe item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : item.nom());
      }
    });
    cbFournisseur.setCellFactory(l -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(Societe item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : item.nom());
      }
    });
    if (currentFournisseur != null) cbFournisseur.setValue(currentFournisseur);
    taComment.setText(commentaire == null ? "" : commentaire);
  }

  public Long fournisseurId() { return cbFournisseur.getValue() == null ? null : cbFournisseur.getValue().id(); }
  public String commentaire() { return taComment.getText() == null ? "" : taComment.getText().trim(); }
}