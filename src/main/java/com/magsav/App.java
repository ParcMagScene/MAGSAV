package com.magsav;

import com.magsav.db.DB;
import com.magsav.service.DataCacheService;
import com.magsav.util.DatabaseInitializer;
import com.magsav.util.SimpleTestDataGenerator;

import com.magsav.util.MediaValidator;
import com.magsav.util.MediaAudit;
import com.magsav.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public class App extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    // S'assurer d'utiliser la base de données de production et non celle des tests
    DB.initForProduction();
    DB.diagnose(); // Diagnostic de la connexion

    // Initialiser les utilisateurs par défaut
    DatabaseInitializer.initialize();

    // Générer les données de test si la base est vide
    SimpleTestDataGenerator.generateTestData();

    // Vider le cache pour forcer le rechargement depuis la base de données
    DataCacheService.invalidateAllCache();
    System.out.println("Cache invalidé - rechargement depuis la base de données");

    MediaValidator.ensureDirs();
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/main_modern.fxml"));
    stage.setTitle("MAGSAV 1.2 - Mag Scène");

    // Préférences pour mémoriser/restaurer la taille de la fenêtre principale
    Preferences prefs = Preferences.userNodeForPackage(App.class);
    double width = prefs.getDouble("mainWindow.width", 1000);
    double height = prefs.getDouble("mainWindow.height", 700);
    Scene scene = new Scene(root, width, height);
    // Appliquer le thème dark unifié
    ThemeManager.applyDarkTheme(scene);
    stage.setScene(scene);

    // À la fermeture, sauvegarder la taille
    stage.setOnCloseRequest(e -> {
      prefs.putDouble("mainWindow.width", stage.getWidth());
      prefs.putDouble("mainWindow.height", stage.getHeight());
    });

    stage.show();
    MediaAudit.report();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
