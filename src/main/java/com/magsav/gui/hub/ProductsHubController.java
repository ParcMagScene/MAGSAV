package com.magsav.gui.hub;

import com.magsav.repo.ProductRepository;
import com.magsav.util.TableFx;
import com.magsav.util.Views;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class ProductsHubController {
  @FXML private TableView<ProductRepository.ProductRow> table;

  @FXML
  private void initialize() {
    // ...existing code...
    TableFx.openOnDoubleClick(table, ProductRepository.ProductRow::id, Views::openProductSheet);
  }
  // ...existing code...
}