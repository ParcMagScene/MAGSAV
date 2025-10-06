package com.magsav.gui;

import com.magsav.repo.ProductRepository;
import com.magsav.util.TableFx;
import com.magsav.util.Views;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NewInterventionController {
  @FXML private TextField tfProduit;
  @FXML private TextField tfSN;
  @FXML private TextField tfCode;
  @FXML private TextField tfFabricant;
  @FXML private TextField tfStatut;
  @FXML private TextArea taPanne;
  @FXML private TableView<ProductRepository.ProductRow> productsTable; // ajustez selon FXML

  public String produit()   { return v(tfProduit.getText()); }
  public String sn()        { return v(tfSN.getText()); }
  public String code()      { return v(tfCode.getText()); }
  public String fabricant() { return v(tfFabricant.getText()); }
  public String statut()    { return v(tfStatut.getText()); }
  public String panne()     { return v(taPanne.getText()); }

  public boolean isValid() { return !produit().isEmpty(); }
  private static String v(String s) { return s == null ? "" : s.trim(); }

  @FXML
  private void initialize() {
    if (productsTable != null) {
      TableFx.openOnDoubleClick(productsTable, ProductRepository.ProductRow::id, Views::openProductSheet);
    }
  }
}