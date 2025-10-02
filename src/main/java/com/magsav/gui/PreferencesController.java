package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.gui.widgets.ImagePickerController;
import java.nio.file.Path;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;

public class PreferencesController {
  @FXML private CheckBox chkOpenInterventionOnDoubleClick;
  @FXML private RadioButton rbThemeLight;
  @FXML private RadioButton rbThemeDark;
  @FXML private RadioButton rbThemeOs;
  @FXML private DialogPane dialogPane;
  @FXML private ImageView imgCompanyLogo;
  @FXML private Label lblCompanyLogoPath;

  private Config config;

  @FXML
  public void initialize() {
    try {
      Path path = Path.of("application.yml");
      config = path.toFile().exists() ? Config.load(path) : new Config();
    } catch (Exception e) {
      config = new Config();
    }
    // Par défaut, activer aussi le double-clic pour les interventions (cohérent avec
    // MainController)
    boolean enabledInter = config.getBoolean("ui.openInterventionOnDoubleClick", true);
    chkOpenInterventionOnDoubleClick.setSelected(enabledInter);
    String theme = config.get("ui.theme", "light");
    switch (theme) {
      case "dark" -> rbThemeDark.setSelected(true);
      case "os" -> rbThemeOs.setSelected(true);
      default -> rbThemeLight.setSelected(true);
    }

    // Charger logo d'entreprise si présent
    try {
      String logoPath = config.get("company.logo.path", null);
      if (logoPath != null && !logoPath.isBlank()) {
        java.io.File f = new java.io.File(logoPath);
        if (f.exists()) {
          javafx.scene.image.Image img =
              new javafx.scene.image.Image(f.toURI().toString(), 160, 80, true, true);
          if (imgCompanyLogo != null) {
            imgCompanyLogo.setImage(img);
          }
          if (lblCompanyLogoPath != null) {
            lblCompanyLogoPath.setText(f.getAbsolutePath());
          }
        } else {
          if (lblCompanyLogoPath != null) {
            lblCompanyLogoPath.setText("(introuvable)");
          }
        }
      }
    } catch (Exception ignored) {
    }

    // Protéger en cas d'injection manquée du DialogPane
    javafx.scene.control.Button okBtn =
        dialogPane != null
            ? (javafx.scene.control.Button)
                dialogPane.lookupButton(javafx.scene.control.ButtonType.OK)
            : null;
    if (okBtn != null) {
      okBtn.addEventFilter(
          javafx.event.ActionEvent.ACTION,
          evt -> {
            try {
              // Rien à faire pour les produits: le double-clic est toujours actif
              config.set(
                  "ui.openInterventionOnDoubleClick",
                  Boolean.toString(chkOpenInterventionOnDoubleClick.isSelected()));
              String newTheme =
                  rbThemeDark.isSelected() ? "dark" : (rbThemeOs.isSelected() ? "os" : "light");
              config.set("ui.theme", newTheme);
              config.save(Path.of("application.yml"));
            } catch (Exception ex) {
              // rester silencieux, l'appelant peut afficher une alerte si nécessaire
            }
          });
    }
  }

  @FXML
  private void handleResetLayout() {
    try {
      // Supprimer toutes les clés ui.window.*
      // Notre Config minimaliste ne supporte pas le wildcard; on efface explicitement les clés
      // usuelles
      String[] windows =
          new String[] {
            "ui.window.main", "ui.window.productDetail", "ui.window.interventionDetail"
          };
      for (String w : windows) {
        config.set(w + ".x", null);
        config.set(w + ".y", null);
        config.set(w + ".w", null);
        config.set(w + ".h", null);
        config.set(w + ".maximized", null);
      }
      config.save(java.nio.file.Path.of("application.yml"));
      // Feedback simple
      javafx.scene.control.Alert a =
          new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
      a.setTitle("Disposition réinitialisée");
      a.setHeaderText(null);
      a.setContentText("La disposition des fenêtres sera réinitialisée au prochain démarrage.");
      a.showAndWait();
    } catch (Exception ignored) {
    }
  }

  @FXML
  private void handleChooseCompanyLogo() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/image-picker.fxml"));
      Parent root = loader.load();
      ImagePickerController ctrl = loader.getController();
      java.nio.file.Path lib = java.nio.file.Path.of("photos", "logos", "companies");
      ctrl.setLibraryDir(lib);
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Choisir un logo d'entreprise");
      Scene scene = new Scene(root);
      try {
        scene.getStylesheets().addAll(dialogPane.getScene().getStylesheets());
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      stage.initOwner(dialogPane.getScene().getWindow());
      stage.initModality(Modality.WINDOW_MODAL);
      stage.showAndWait();
      Object ud = stage.getUserData();
      if (ud instanceof java.nio.file.Path p) {
        String abs = p.toAbsolutePath().toString();
        try {
          javafx.scene.image.Image img =
              new javafx.scene.image.Image(p.toUri().toString(), 160, 80, true, true);
          imgCompanyLogo.setImage(img);
        } catch (Exception ignored) {
        }
        if (lblCompanyLogoPath != null) {
          lblCompanyLogoPath.setText(abs);
        }
        config.set("company.logo.path", abs);
        config.save(java.nio.file.Path.of("application.yml"));
      }
    } catch (Exception ignored) {
    }
  }

  @FXML
  private void handleRemoveCompanyLogo() {
    try {
      config.set("company.logo.path", null);
      config.save(java.nio.file.Path.of("application.yml"));
      if (imgCompanyLogo != null) {
        imgCompanyLogo.setImage(null);
      }
      if (lblCompanyLogoPath != null) {
        lblCompanyLogoPath.setText("");
      }
    } catch (Exception ignored) {
    }
  }
}
