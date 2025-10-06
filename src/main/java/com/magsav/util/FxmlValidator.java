package com.magsav.util;

import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.List;

public final class FxmlValidator {
  private static final List<String> FXMLS = List.of(
    "/fxml/societes/manufacturer_form.fxml",
    "/fxml/hub_clients.fxml",
    "/fxml/new_intervention.fxml",
    "/fxml/hub_users.fxml",
    "/fxml/products/categories.fxml",
    "/fxml/products/category_form.fxml",
    "/fxml/categories.fxml",
    "/fxml/category_form.fxml",
    "/fxml/requests_parts.fxml",
    "/fxml/hub_societes.fxml",
    "/fxml/hub_products.fxml",
    "/fxml/main.fxml",
    "/fxml/request_form.fxml",
    "/fxml/requests_equipment.fxml",
    "/fxml/clients.fxml",
    "/fxml/manufacturers.fxml",
    "/fxml/external_sav.fxml",
    "/fxml/request_item_form.fxml",
    "/fxml/product_detail.fxml",
    "/fxml/suppliers.fxml",
    "/fxml/hub_config.fxml",
    "/fxml/categories/category_form.fxml",
    "/fxml/interventions/intervention_detail.fxml",
    "/fxml/interventions/intervention_form.fxml",
    "/fxml/hub_demandes.fxml",
    "/fxml/societes/manufacturer_detail.fxml"
  );

  public static void validateAll() {
    if (!isDebug()) return;
    System.out.println("[MAGSAV] FXML validation (debug)...");
    int ok = 0, miss = 0, fail = 0;
    for (String path : FXMLS) {
      try {
        URL url = FxmlValidator.class.getResource(path);
        if (url == null) { System.out.println("  [MISS] " + path); miss++; continue; }
        new FXMLLoader(url).load(); // charge et vérifie
        ok++;
      } catch (Exception ex) {
        System.out.println("  [FAIL] " + path + " -> " + ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage()));
        fail++;
      }
    }
    System.out.println("[MAGSAV] FXML validation: ok=" + ok + ", missing=" + miss + ", failed=" + fail);
  }

  private static boolean isDebug() {
    return Boolean.getBoolean("magsav.debug")
        || "1".equals(System.getenv("MAGSAV_DEBUG"))
        || "1".equals(System.getenv("MAGSAV_VALIDATE_FXML"));
  }

  private FxmlValidator() {}
}