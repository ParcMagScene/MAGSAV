package com.magsav;

import com.magsav.db.DB;
import com.magsav.util.FxmlValidator;
import com.magsav.util.MediaValidator;
import com.magsav.util.MediaAudit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    DB.init();
    MediaValidator.ensureDirs();
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
    stage.setTitle("MAGSAV 1.2");
    stage.setScene(new Scene(root, 1000, 700));
    stage.show();
    FxmlValidator.validateAll();
    MediaAudit.report();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
