package com.magsav.util;

import com.magsav.gui.ProductDetailController;
import com.magsav.gui.RequestDetailController;
import com.magsav.gui.UserDetailController;
import com.magsav.gui.VehiculeDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public final class Views {
  
  // Constantes pour les vues FXML
  public static final String COMMANDE_FORM = "/fxml/commandes/commande_form.fxml";
  
  private Views() {}

  public static void openProductSheet(long productId) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/products/details/product_detail.fxml"));
      Parent root = l.load();
      ProductDetailController ctl = l.getController();
      ctl.setProductId(productId);
      Stage st = new Stage();
      st.setTitle("Produit #" + productId);
        // Restaure la taille précédente
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        double width = prefs.getDouble("productSheet.width", 800);
        double height = prefs.getDouble("productSheet.height", 600);
        
        Scene scene = new Scene(root, width, height);
        // Appliquer le thème dark
        ThemeManager.applyDarkTheme(scene);
        
        st.setScene(scene);
        st.setOnCloseRequest(e -> {
          prefs.putDouble("productSheet.width", st.getWidth());
          prefs.putDouble("productSheet.height", st.getHeight());
        });
      st.show();
    } catch (Exception e) {
      throw new RuntimeException("Ouverture fiche produit: " + e.getMessage(), e);
    }
  }

  public static void openManufacturer(String fabricant) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Fabricant: " + fabricant);
    // Appliquer le thème dark
    ThemeManager.applyDarkTheme(alert);
    alert.show();
  }

  public static void openInNewWindow(String fxmlPath, String windowTitle) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource(fxmlPath));
      Parent root = l.load();
      Stage st = new Stage();
      st.setTitle(windowTitle);
        // Utilise le nom du FXML comme clé
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        String key = fxmlPath.replaceAll("[^a-zA-Z0-9]", "_");
        double width = prefs.getDouble(key + ".width", 800);
        double height = prefs.getDouble(key + ".height", 600);
        
        Scene scene = new Scene(root, width, height);
        // Appliquer le thème dark
        ThemeManager.applyDarkTheme(scene);
        
        st.setScene(scene);
        st.setOnCloseRequest(e -> {
          prefs.putDouble(key + ".width", st.getWidth());
          prefs.putDouble(key + ".height", st.getHeight());
        });
      st.show();
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture de la fenêtre: " + e.getMessage());
      // Appliquer le thème dark
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }

  public static void openInterventionDetail(long interventionId) {
    openInterventionDetail(interventionId, null);
  }

  public static void openInterventionDetail(long interventionId, com.magsav.repo.ProductRepository.ProductRow product) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/interventions/details/intervention_detail.fxml"));
      Parent root = l.load();
      var controller = l.getController();
      
      // Si le contrôleur a une méthode setInterventionId, l'appeler
      if (controller instanceof com.magsav.gui.InterventionDetailController) {
        var detailController = (com.magsav.gui.InterventionDetailController) controller;
        detailController.setInterventionId(interventionId);
      }
      
      Stage st = new Stage();
      st.setTitle("Intervention #" + interventionId + " - Édition");
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        double width = prefs.getDouble("interventionDetail.width", 900);
        double height = prefs.getDouble("interventionDetail.height", 650);
        
        Scene scene = new Scene(root, width, height);
        // Appliquer le thème dark
        ThemeManager.applyDarkTheme(scene);
        
        st.setScene(scene);
        st.setOnCloseRequest(e -> {
          prefs.putDouble("interventionDetail.width", st.getWidth());
          prefs.putDouble("interventionDetail.height", st.getHeight());
        });
      st.show();
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture de l'intervention: " + e.getMessage());
      // Appliquer le thème dark
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }

  public static void openPreferencesWithTab(String fxmlPath, String windowTitle, String selectedTab) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource(fxmlPath));
      Parent root = l.load();
      
      // Obtenir le contrôleur et sélectionner l'onglet spécifié
      Object controller = l.getController();
      if (controller instanceof com.magsav.gui.PreferencesController) {
        ((com.magsav.gui.PreferencesController) controller).selectTab(selectedTab);
      }
      
      Stage st = new Stage();
      st.setTitle(windowTitle);
      String key = fxmlPath.replaceAll("[^a-zA-Z0-9]", "_");
      java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Views.class);
      double width = prefs.getDouble(key + ".width", 900);
      double height = prefs.getDouble(key + ".height", 700);
      
      Scene scene = new Scene(root, width, height);
      ThemeManager.applyDarkTheme(scene);
      
      st.setScene(scene);
      st.setOnCloseRequest(e -> {
        prefs.putDouble(key + ".width", st.getWidth());
        prefs.putDouble(key + ".height", st.getHeight());
      });
      st.show();
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture des préférences: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'un client par ID
   */
  public static void openClientDetail(Long clientId) {
    try {
      // Charger le client depuis la base
      com.magsav.repo.CompanyRepository repo = new com.magsav.repo.CompanyRepository(com.magsav.db.DB.getConnection());
      java.util.Optional<com.magsav.model.Company> clientOpt = repo.findById(clientId);
      
      if (!clientOpt.isPresent()) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Client non trouvé avec l'ID: " + clientId);
        ThemeManager.applyDarkTheme(alert);
        alert.showAndWait();
        return;
      }
      
      openClientDetail(clientOpt.get());
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement du client: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'un client
   */
  public static void openClientDetail(com.magsav.model.Company client) {
    try {
      // Créer une fenêtre de détail simple en Java pur
      Stage st = new Stage();
      st.setTitle("Client: " + client.getName());
      
      // Créer le contenu de la fenêtre
      javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
      root.setSpacing(10);
      root.setPadding(new javafx.geometry.Insets(20));
      root.getStyleClass().add("main-content");
      
      // Titre
      javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Détails du client");
      titleLabel.getStyleClass().add("content-title");
      root.getChildren().add(titleLabel);
      
      // Informations
      root.getChildren().addAll(
        new javafx.scene.control.Label("Nom: " + client.getName()),
        new javafx.scene.control.Label("Type: " + client.getType()),
        new javafx.scene.control.Label("Email: " + (client.getEmail() != null ? client.getEmail() : "N/A")),
        new javafx.scene.control.Label("Téléphone: " + (client.getPhone() != null ? client.getPhone() : "N/A")),
        new javafx.scene.control.Label("Adresse: " + (client.getAddress() != null ? client.getAddress() : "N/A")),
        new javafx.scene.control.Label("Ville: " + (client.getCity() != null ? client.getCity() : "N/A"))
      );
      
      // Bouton fermer
      javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Fermer");
      closeBtn.getStyleClass().addAll("button", "button-secondary");
      closeBtn.setOnAction(e -> st.close());
      
      javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox();
      buttonBox.getChildren().add(closeBtn);
      buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
      root.getChildren().add(buttonBox);
      
      // Restaurer la taille précédente
      java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Views.class);
      double width = prefs.getDouble("clientDetail.width", 600);
      double height = prefs.getDouble("clientDetail.height", 400);
      
      javafx.scene.Scene scene = new javafx.scene.Scene(root, width, height);
      com.magsav.util.ThemeManager.applyDarkTheme(scene);
      
      st.setScene(scene);
      st.setOnCloseRequest(e -> {
        prefs.putDouble("clientDetail.width", st.getWidth());
        prefs.putDouble("clientDetail.height", st.getHeight());
      });
      st.show();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche client: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'un fabricant par ID
   */
  public static void openManufacturerDetail(Long manufacturerId) {
    try {
      // Charger le fabricant depuis la base
      com.magsav.repo.CompanyRepository repo = new com.magsav.repo.CompanyRepository(com.magsav.db.DB.getConnection());
      java.util.Optional<com.magsav.model.Company> manufacturerOpt = repo.findById(manufacturerId);
      
      if (!manufacturerOpt.isPresent()) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Fabricant non trouvé avec l'ID: " + manufacturerId);
        ThemeManager.applyDarkTheme(alert);
        alert.showAndWait();
        return;
      }
      
      openManufacturerDetail(manufacturerOpt.get());
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement du fabricant: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'un fabricant
   */
  public static void openManufacturerDetail(com.magsav.model.Company manufacturer) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/societes/details/manufacturer_detail.fxml"));
      Parent root = l.load();
      
      // Configurer le contrôleur avec le fabricant
      com.magsav.gui.societes.ManufacturerDetailController controller = l.getController();
      controller.setManufacturer(manufacturer);
      
      Stage st = new Stage();
      st.setTitle("Fabricant: " + manufacturer.getName());
      
      // Restaurer la taille précédente
      java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Views.class);
      double width = prefs.getDouble("manufacturerDetail.width", 900);
      double height = prefs.getDouble("manufacturerDetail.height", 700);
      
      Scene scene = new Scene(root, width, height);
      ThemeManager.applyDarkTheme(scene);
      
      st.setScene(scene);
      st.setOnCloseRequest(e -> {
        prefs.putDouble("manufacturerDetail.width", st.getWidth());
        prefs.putDouble("manufacturerDetail.height", st.getHeight());
      });
      st.show();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche fabricant: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'une société par ID
   */
  public static void openCompanyDetail(Long companyId) {
    try {
      // Charger la société depuis la base
      com.magsav.repo.CompanyRepository repo = new com.magsav.repo.CompanyRepository(com.magsav.db.DB.getConnection());
      java.util.Optional<com.magsav.model.Company> companyOpt = repo.findById(companyId);
      
      if (!companyOpt.isPresent()) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Société non trouvée avec l'ID: " + companyId);
        ThemeManager.applyDarkTheme(alert);
        alert.showAndWait();
        return;
      }
      
      openCompanyDetail(companyOpt.get());
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de la société: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  /**
   * Ouvre la fiche détail d'une société
   */
  public static void openCompanyDetail(com.magsav.model.Company company) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/societes/details/company_detail.fxml"));
      Parent root = l.load();
      
      // Configurer le contrôleur avec la société
      com.magsav.gui.societes.CompanyDetailController controller = l.getController();
      controller.setCompany(company);
      
      Stage st = new Stage();
      st.setTitle("Société: " + company.getName());
      
      // Restaurer la taille précédente
      java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(Views.class);
      double width = prefs.getDouble("companyDetail.width", 800);
      double height = prefs.getDouble("companyDetail.height", 600);
      
      Scene scene = new Scene(root, width, height);
      ThemeManager.applyDarkTheme(scene);
      
      st.setScene(scene);
      st.setOnCloseRequest(e -> {
        prefs.putDouble("companyDetail.width", st.getWidth());
        prefs.putDouble("companyDetail.height", st.getHeight());
      });
      st.show();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche société: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  public static void openRequestDetail(long requestId) {
    try {
      FXMLLoader loader = new FXMLLoader(Views.class.getResource("/fxml/requests/details/request_detail.fxml"));
      Parent root = loader.load();
      
      RequestDetailController controller = loader.getController();
      controller.loadRequest(requestId);
      
      Stage stage = new Stage();
      stage.setTitle("Détails de la demande #" + requestId);
      stage.setScene(new Scene(root));
      stage.initModality(Modality.APPLICATION_MODAL);
      
      // Appliquer le thème
      ThemeManager.applyDarkTheme(stage.getScene());
      
      // Taille et position de la fenêtre
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setMinWidth(600);
      stage.setMinHeight(500);
      
      // Centrer la fenêtre
      stage.centerOnScreen();
      
      stage.showAndWait();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche demande: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  public static void openUserDetail(Integer userId) {
    try {
      FXMLLoader loader = new FXMLLoader(Views.class.getResource("/fxml/user_detail.fxml"));
      Parent root = loader.load();
      
      UserDetailController controller = loader.getController();
      controller.loadUser(userId);
      
      Stage stage = new Stage();
      stage.setTitle("Détails de l'utilisateur #" + userId);
      stage.setScene(new Scene(root));
      stage.initModality(Modality.APPLICATION_MODAL);
      
      // Appliquer le thème
      ThemeManager.applyDarkTheme(stage.getScene());
      
      // Taille et position de la fenêtre
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setMinWidth(600);
      stage.setMinHeight(500);
      
      // Centrer la fenêtre
      stage.centerOnScreen();
      
      stage.showAndWait();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche utilisateur: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
  public static void openVehiculeDetail(int vehiculeId) {
    try {
      FXMLLoader loader = new FXMLLoader(Views.class.getResource("/fxml/vehicules/details/vehicule_detail.fxml"));
      Parent root = loader.load();
      
      VehiculeDetailController controller = loader.getController();
      controller.loadVehicule(vehiculeId);
      
      Stage stage = new Stage();
      stage.setTitle("Détails du véhicule #" + vehiculeId);
      stage.setScene(new Scene(root));
      stage.initModality(Modality.APPLICATION_MODAL);
      
      // Appliquer le thème
      ThemeManager.applyDarkTheme(stage.getScene());
      
      // Taille et position de la fenêtre
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setMinWidth(600);
      stage.setMinHeight(500);
      
      // Centrer la fenêtre
      stage.centerOnScreen();
      
      stage.showAndWait();
      
    } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fiche véhicule: " + e.getMessage());
      ThemeManager.applyDarkTheme(alert);
      alert.showAndWait();
      e.printStackTrace();
    }
  }
}