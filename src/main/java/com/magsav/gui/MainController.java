package com.magsav.gui;

import com.magsav.model.InterventionRow;
import com.magsav.repo.InterventionRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainController {
  @FXML private TableView<InterventionRow> table;
  @FXML private TableColumn<InterventionRow, String> colId;
  @FXML private TableColumn<InterventionRow, String> colProduit;
  @FXML private TableColumn<InterventionRow, String> colStatut;
  @FXML private TableColumn<InterventionRow, String> colPanne;
  @FXML private TableColumn<InterventionRow, String> colEntree;
  @FXML private TableColumn<InterventionRow, String> colSortie;

  private final InterventionRepository repo = new InterventionRepository();

  @FXML
  private void initialize() {
    repo.seedIfEmpty();
    colId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().id())));
    colProduit.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().produitNom()));
    colStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().statut()));
    colPanne.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().panne()));
    colEntree.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().dateEntree()));
    colSortie.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().dateSortie()));
    table.setItems(FXCollections.observableArrayList(repo.findAllWithProductName()));
  }
}