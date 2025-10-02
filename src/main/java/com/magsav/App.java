package com.magsav;

import com.magsav.db.DB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class App extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    DB.init(); // initialise DB + schéma
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
    stage.setTitle("MAGSAV 1.2");
    stage.setScene(new Scene(root, 1000, 700));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
