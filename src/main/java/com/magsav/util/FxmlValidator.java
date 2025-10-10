package com.magsav.util;

import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.List;

public final class FxmlValidator {
  private static final List<String> FXMLS = List.of(
    "/fxml/societes/forms/manufacturer_form.fxml",
    "/fxml/societes/hubs/hub_clients.fxml",
    "/fxml/interventions/forms/new_intervention.fxml",
    "/fxml/hub_users.fxml",
    "/fxml/categories/categories.fxml",
    "/fxml/categories/category_form.fxml",
    "/fxml/requests/lists/requests_parts.fxml",
    "/fxml/societes/hubs/hub_societes.fxml",
    "/fxml/products/hubs/hub_products.fxml",
    "/fxml/main.fxml",
    "/fxml/requests/forms/request_form.fxml",
    "/fxml/requests/lists/requests_equipment.fxml",
    "/fxml/clients.fxml",
    "/fxml/societes/lists/manufacturers.fxml",
    "/fxml/societes/lists/external_sav.fxml",
    "/fxml/requests/forms/request_item_form.fxml",
    "/fxml/products/details/product_detail.fxml",
    "/fxml/societes/lists/suppliers.fxml",
    "/fxml/hub_config.fxml",
    "/fxml/interventions/intervention_detail.fxml",
    "/fxml/interventions/intervention_form.fxml",
    "/fxml/requests/hubs/hub_demandes.fxml",
    "/fxml/societes/details/manufacturer_detail.fxml"
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