package com.magsav.gui.requests;

import com.magsav.model.RequestItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RequestItemFormController {
  @FXML private TextField tfRef;
  @FXML private TextField tfQty;
  @FXML private TextArea taDesc;

  public void init(RequestItem current) {
    if (current != null) {
      tfRef.setText(current.ref());
      tfQty.setText(String.valueOf(current.qty()));
      taDesc.setText(current.description());
    }
  }

  public boolean isValid() {
    return ref().length() >= 2 && qty() > 0;
  }

  public String ref() { return v(tfRef.getText()); }
  public int qty() {
    try { return Integer.parseInt(v(tfQty.getText()).isEmpty() ? "1" : v(tfQty.getText())); }
    catch (NumberFormatException e) { return -1; }
  }
  public String description() { return v(taDesc.getText()); }

  private static String v(String s) { return s == null ? "" : s.trim(); }
}