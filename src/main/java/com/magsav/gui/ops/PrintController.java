package com.magsav.gui.ops;

import com.magsav.config.Config;
import com.magsav.label.LabelService;
import com.magsav.qr.QRCodeService;
import com.magsav.service.PrintService;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class PrintController implements Initializable {
  @FXML private ComboBox<String> cbKind;
  @FXML private TextArea taText;
  @FXML private TextField tfQr;
  @FXML private CheckBox cbUseLogo;
  @FXML private Button btnGenerate;

  private final PrintService printService =
      new PrintService(new LabelService(), new QRCodeService());

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    cbKind.getItems().setAll("Etiquette produit", "Etiquette dossier", "Autre");
    cbKind.setValue("Etiquette produit");
    btnGenerate.setOnAction(e -> generate());
  }

  private void generate() {
    try {
      String text = taText.getText();
      if (text == null) {
        text = "";
      }
      String qrData = tfQr.getText();
      Path outDir = Paths.get("output");
      Path logo = null;
      if (cbUseLogo.isSelected()) {
        try {
          Config cfg = new Config();
          java.nio.file.Path cfgPath = Paths.get("application.yml");
          if (cfgPath.toFile().exists()) {
            cfg = Config.load(cfgPath);
          }
          String logoPath = cfg.get("company.logo.path", null);
          if (logoPath != null && !logoPath.isBlank()) {
            logo = Paths.get(logoPath);
          }
        } catch (Exception ignore) {
        }
      }
      var result = printService.generateLabel(text, qrData, logo, outDir);
      Path pdf = result.pdf();
      Alert a =
          new Alert(
              Alert.AlertType.INFORMATION, "PDF généré: " + pdf.toAbsolutePath(), ButtonType.OK);
      a.setHeaderText("Impression terminée");
      a.showAndWait();
    } catch (Exception ex) {
      Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
      a.setHeaderText("Erreur d'impression");
      a.showAndWait();
    }
  }
}
