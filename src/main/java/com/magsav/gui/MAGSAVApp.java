package com.magsav.gui;

import static com.magsav.gui.util.UiAlerts.*;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.gui.menu.GestionMenu;
import com.magsav.gui.util.WindowPrefs;
import com.magsav.imports.CSVImporter;
import com.magsav.model.DossierSAV;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.service.SAVService;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.MenuBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MAGSAVApp extends Application {

  private DossierSAVRepository dossierRepo;
  private SAVService savService;
  private CSVImporter csvImporter;
  private HikariDataSource dataSource;

  private TableView<DossierSAV> tableView;
  private TextField searchField;
  private ComboBox<String> statusFilter;
  private Label statusLabel;
  private ObservableList<DossierSAV> dossiersList;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
    Parent root = loader.load();
    MAGSAVController controller = loader.getController();
    stage.setScene(new Scene(root));
    stage.show();

    // Appliquer/Enregistrer les prefs fenêtre au lieu des méthodes dupliquées:
    WindowPrefs.apply(stage, "ui.window.main");

    Platform.runLater(controller::loadData);
  }

  private void applyWindowPrefs(Stage stage, String prefix) {
    try {
      java.nio.file.Path p = java.nio.file.Path.of("application.yml");
      Config cfg = p.toFile().exists() ? Config.load(p) : new Config();
      String sx = cfg.get(prefix + ".x", null);
      String sy = cfg.get(prefix + ".y", null);
      String sw = cfg.get(prefix + ".w", null);
      String sh = cfg.get(prefix + ".h", null);
      boolean maximized = cfg.getBoolean(prefix + ".maximized", false);
      if (sx != null && sy != null) {
        try {
          stage.setX(Double.parseDouble(sx));
        } catch (Exception ignored) {
        }
        try {
          stage.setY(Double.parseDouble(sy));
        } catch (Exception ignored) {
        }
      }
      if (sw != null && sh != null) {
        try {
          stage.setWidth(Double.parseDouble(sw));
        } catch (Exception ignored) {
        }
        try {
          stage.setHeight(Double.parseDouble(sh));
        } catch (Exception ignored) {
        }
      }
      // Normaliser pour éviter les fenêtres hors écran
      try {
        double x = stage.getX();
        double y = stage.getY();
        double w = stage.getWidth();
        double h = stage.getHeight();
        if (w <= 0) {
          w = 800;
        }
        if (h <= 0) {
          h = 600;
        }
        var screens = Screen.getScreensForRectangle(x, y, w, h);
        if (screens == null || screens.isEmpty()) {
          Rectangle2D v = Screen.getPrimary().getVisualBounds();
          stage.setWidth(Math.min(w, v.getWidth()));
          stage.setHeight(Math.min(h, v.getHeight()));
          stage.setX(v.getMinX() + (v.getWidth() - stage.getWidth()) / 2);
          stage.setY(v.getMinY() + (v.getHeight() - stage.getHeight()) / 2);
        } else {
          // Clamper dans le visuel du premier écran trouvé
          Rectangle2D v = screens.get(0).getVisualBounds();
          double nx = Math.max(v.getMinX(), Math.min(stage.getX(), v.getMaxX() - stage.getWidth()));
          double ny =
              Math.max(v.getMinY(), Math.min(stage.getY(), v.getMaxY() - stage.getHeight()));
          stage.setX(nx);
          stage.setY(ny);
        }
      } catch (Exception ignored) {
      }
      stage.setMaximized(maximized);
    } catch (Exception ignored) {
    }
  }

  private void saveWindowPrefs(Stage stage, String prefix) throws Exception {
    java.nio.file.Path p = java.nio.file.Path.of("application.yml");
    Config cfg = p.toFile().exists() ? Config.load(p) : new Config();
    cfg.set(prefix + ".x", Double.toString(stage.getX()));
    cfg.set(prefix + ".y", Double.toString(stage.getY()));
    cfg.set(prefix + ".w", Double.toString(stage.getWidth()));
    cfg.set(prefix + ".h", Double.toString(stage.getHeight()));
    cfg.set(prefix + ".maximized", Boolean.toString(stage.isMaximized()));
    cfg.save(p);
  }

  private void initializeServices() throws Exception {
    Config config = new Config();
    Path configPath = Path.of("application.yml");
    if (configPath.toFile().exists()) {
      config = Config.load(configPath);
    }

    String dbUrl = config.get("app.database.url", "jdbc:sqlite:magsav.db");
    if (!dbUrl.startsWith("jdbc:")) {
      dbUrl = "jdbc:sqlite:" + dbUrl;
    }

    this.dataSource = DB.init(dbUrl);
    // S'assurer que le schéma est en place pour les environnements frais
    try {
      DB.migrate(this.dataSource);
    } catch (Exception ex) {
      System.err.println("Avertissement: échec migration DB: " + ex.getMessage());
    }

    this.dossierRepo = new DossierSAVRepository(this.dataSource);
    this.savService = new SAVService(this.dataSource);
    this.csvImporter = new CSVImporter(this.dataSource);
  }

  private boolean isSmokeMode() {
    // Détection via argument CLI "--smoke" ou propriété système magsav.smoke=true
    boolean sys = Boolean.getBoolean("magsav.smoke");
    boolean arg =
        getParameters() != null && getParameters().getRaw().stream().anyMatch("--smoke"::equals);
    return sys || arg;
  }

  private void runSmoke(Stage stage) {
    System.out.println("[SMOKE_GUI] Démarrage du smoke test JavaFX...");
    try {
      initializeServices();
      // Accès minimal: compter les interventions et afficher 1er code produit si dispo
      List<DossierSAV> all = new DossierSAVRepository(this.dataSource).findAll();
      int count = all != null ? all.size() : 0;
      String info = count > 0 ? (all.get(0).getCode() + "/" + all.get(0).getNumeroSerie()) : "none";
      System.out.println("[SMOKE_GUI] Dossiers chargés: " + count + " (exemple: " + info + ")");
      System.out.println("SMOKE_GUI_OK");
      // Quitter proprement
      Platform.exit();
    } catch (Exception ex) {
      System.err.println("SMOKE_GUI_FAIL: " + ex.getMessage());
      ex.printStackTrace();
      // Quitter avec code d'erreur
      Platform.exit();
      System.exit(1);
    } finally {
      if (this.dataSource != null) {
        try {
          this.dataSource.close();
        } catch (Exception ignore) {
        }
      }
    }
  }

  private VBox createTopPanel() {
    VBox topPanel = new VBox(10);
    topPanel.setPadding(new Insets(10));

    Label titleLabel = new Label("MAGSAV - Gestion SAV");
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    HBox searchPanel = new HBox(10);
    searchPanel.setAlignment(Pos.CENTER_LEFT);

    searchField = new TextField();
    searchField.setPromptText("Recherche...");
    searchField.setPrefWidth(200);

    statusFilter =
        new ComboBox<>(
            FXCollections.observableArrayList(
                "Tous", "En attente", "En cours", "Terminé", "Annulé"));
    statusFilter.setValue("Tous");

    Button searchButton = new Button("Rechercher");
    searchButton.setOnAction(e -> handleSearch());

    searchPanel
        .getChildren()
        .addAll(
            new Label("Recherche:"), searchField, new Label("Statut:"), statusFilter, searchButton);

    topPanel.getChildren().addAll(titleLabel, searchPanel);
    return topPanel;
  }

  private TableView<DossierSAV> createTableView() {
    tableView = new TableView<>();

    TableColumn<DossierSAV, Long> colId = new TableColumn<>("ID");
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colId.setPrefWidth(60);

    TableColumn<DossierSAV, String> colProprietaire = new TableColumn<>("Propriétaire");
    colProprietaire.setCellValueFactory(new PropertyValueFactory<>("proprietaire"));
    colProprietaire.setPrefWidth(150);

    TableColumn<DossierSAV, String> colAppareil = new TableColumn<>("Appareil");
    colAppareil.setCellValueFactory(
        cellData -> {
          DossierSAV dossier = cellData.getValue();
          String appareil =
              String.format("%s (%s)", dossier.getProduit(), dossier.getNumeroSerie());
          return new SimpleStringProperty(appareil);
        });
    colAppareil.setPrefWidth(200);

    TableColumn<DossierSAV, String> colProbleme = new TableColumn<>("Problème");
    colProbleme.setCellValueFactory(new PropertyValueFactory<>("panne"));
    colProbleme.setPrefWidth(250);

    TableColumn<DossierSAV, String> colStatut = new TableColumn<>("Statut");
    colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    colStatut.setPrefWidth(100);

    TableColumn<DossierSAV, String> colDateCreation = new TableColumn<>("Date création");
    colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
    colDateCreation.setPrefWidth(120);

    TableColumn<DossierSAV, String> colDateReparation = new TableColumn<>("Date réparation");
    colDateReparation.setCellValueFactory(new PropertyValueFactory<>("dateSortie"));
    colDateReparation.setPrefWidth(120);

    // Correction de l'avertissement unchecked - ajout explicite des colonnes
    tableView.getColumns().add(colId);
    tableView.getColumns().add(colProprietaire);
    tableView.getColumns().add(colAppareil);
    tableView.getColumns().add(colProbleme);
    tableView.getColumns().add(colStatut);
    tableView.getColumns().add(colDateCreation);
    tableView.getColumns().add(colDateReparation);

    dossiersList = FXCollections.observableArrayList();
    tableView.setItems(dossiersList);

    return tableView;
  }

  private HBox createBottomPanel() {
    HBox bottomPanel = new HBox(10);
    bottomPanel.setPadding(new Insets(10));
    bottomPanel.setAlignment(Pos.CENTER_LEFT);

    Button importButton = new Button("📁 Importer CSV");
    importButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    importButton.setOnAction(e -> handleImport());

    Button statusButton = new Button("📝 Changer Statut");
    statusButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
    statusButton.setOnAction(e -> handleStatusChange());

    Button labelButton = new Button("🏷️ Générer Étiquette");
    labelButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
    labelButton.setOnAction(e -> handleLabelGeneration());

    Button refreshButton = new Button("⟳ Actualiser");
    refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
    refreshButton.setOnAction(e -> loadData());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    statusLabel = new Label("Prêt");

    bottomPanel
        .getChildren()
        .addAll(importButton, statusButton, labelButton, refreshButton, spacer, statusLabel);
    return bottomPanel;
  }

  private void loadData() {
    statusLabel.setText("Chargement des données...");
    if (dossierRepo == null) { // garde-fou si UI appelle avant init
      try {
        initializeServices();
      } catch (Exception ex) {
        showError("Erreur d'initialisation", ex.getMessage());
        statusLabel.setText("Erreur d'initialisation");
        return;
      }
    }

    Task<List<DossierSAV>> task =
        new Task<>() {
          @Override
          protected List<DossierSAV> call() throws Exception {
            return dossierRepo.findAll();
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  dossiersList.clear();
                  dossiersList.addAll(getValue());
                  statusLabel.setText(String.format("Chargé: %d dossiers", getValue().size()));
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  showError("Erreur de chargement", getException().getMessage());
                  statusLabel.setText("Erreur de chargement");
                });
          }
        };

    new Thread(task).start();
  }

  private void handleSearch() {
    // Implémentation simplifiée pour le test
    statusLabel.setText("Recherche en cours...");
    loadData(); // Pour l'instant, on recharge tout
  }

  private void handleImport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Sélectionner un fichier CSV");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

    File selectedFile = fileChooser.showOpenDialog(tableView.getScene().getWindow());
    if (selectedFile != null) {
      statusLabel.setText("Import en cours...");
      showInfo(
          "Import",
          "Fonctionnalité d'import disponible - fichier sélectionné: " + selectedFile.getName());
      statusLabel.setText("Import simulé");
    }
  }

  private void handleStatusChange() {
    DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
      return;
    }

    showInfo(
        "Changement de statut", "Fonctionnalité disponible pour le dossier #" + selected.getId());
  }

  private void handleLabelGeneration() {
    DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
      return;
    }

    showInfo(
        "Génération d'étiquette", "Fonctionnalité disponible pour le dossier #" + selected.getId());
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showWarning(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private static void ensureGestionMenu(Parent root) {
    if (root == null) {
      return;
    }
    MenuBar bar = findFirstMenuBar(root);
    if (bar == null) {
      bar = new MenuBar();
      if (root instanceof BorderPane bp) {
        Node top = bp.getTop();
        if (top == null) {
          bp.setTop(bar);
        } else if (top instanceof VBox vbox) {
          vbox.getChildren().add(0, bar);
        } else {
          VBox v = new VBox(bar, top);
          bp.setTop(v);
        }
      } else {
        Scene sc = root.getScene();
        if (sc != null) {
          VBox container = new VBox(bar, root);
          sc.setRoot(container);
        }
      }
    }
    boolean present =
        bar.getMenus().stream().anyMatch(m -> "Gestion".equalsIgnoreCase(m.getText()));
    if (!present) {
      bar.getMenus().add(GestionMenu.build());
    }
  }

  // Déplace le menu top-level "Gestion" sous le menu principal (Menu/Fichier/Principal/Accueil,
  // sinon 1er)
  private static void integrateGestionIntoMainMenu(javafx.stage.Stage stage) {
    if (stage == null) {
      return;
    }
    javafx.scene.Scene scene = stage.getScene();
    if (scene == null) {
      return;
    }
    javafx.scene.Parent root = scene.getRoot();
    if (root == null) {
      return;
    }

    javafx.scene.control.MenuBar bar = findOrCreateMenuBar(root);
    if (bar == null) {
      return;
    }

    // Chercher un menu principal
    javafx.scene.control.Menu main = findMainMenu(bar);
    if (main == null) {
      main = new javafx.scene.control.Menu("Menu");
      bar.getMenus().add(0, main);
    }

    // Trouver un menu top-level "Gestion"
    javafx.scene.control.Menu gestionTop = null;
    for (javafx.scene.control.Menu m : bar.getMenus()) {
      if ("Gestion".equalsIgnoreCase(m.getText())) {
        gestionTop = m;
        break;
      }
    }

    // Éviter les doublons: “Gestion” déjà sous le menu principal
    for (javafx.scene.control.MenuItem it : main.getItems()) {
      if (it instanceof javafx.scene.control.Menu m && "Gestion".equalsIgnoreCase(m.getText())) {
        return;
      }
    }

    // Déterminer le menu à insérer comme sous-menu
    javafx.scene.control.Menu toInsert =
        gestionTop != null ? gestionTop : com.magsav.gui.menu.GestionMenu.build();

    // Retirer du top-level si nécessaire
    if (gestionTop != null) {
      bar.getMenus().remove(gestionTop);
    }

    // Ajouter un séparateur si utile
    if (!main.getItems().isEmpty()
        && !(main.getItems().get(main.getItems().size() - 1)
            instanceof javafx.scene.control.SeparatorMenuItem)) {
      main.getItems().add(new javafx.scene.control.SeparatorMenuItem());
    }

    // Insérer "Gestion" comme sous-menu
    main.getItems().add(toInsert);
  }

  // Trouve un MenuBar existant ou en crée un en haut si possible
  private static javafx.scene.control.MenuBar findOrCreateMenuBar(javafx.scene.Parent root) {
    javafx.scene.control.MenuBar mb = findFirstMenuBar(root);
    if (mb != null) {
      return mb;
    }

    mb = new javafx.scene.control.MenuBar();
    if (root instanceof javafx.scene.layout.BorderPane bp) {
      javafx.scene.Node top = bp.getTop();
      if (top == null) {
        bp.setTop(mb);
      } else if (top instanceof javafx.scene.layout.VBox vbox) {
        vbox.getChildren().add(0, mb);
      } else {
        javafx.scene.layout.VBox v = new javafx.scene.layout.VBox();
        v.getChildren().add(mb);
        v.getChildren().add(top);
        bp.setTop(v);
      }
      return mb;
    }

    javafx.scene.Scene sc = root.getScene();
    if (sc != null) {
      javafx.scene.layout.VBox container = new javafx.scene.layout.VBox();
      container.getChildren().add(mb);
      container.getChildren().add(root);
      sc.setRoot(container);
      return mb;
    }
    return null;
  }

  // Recherche en profondeur le premier MenuBar
  private static javafx.scene.control.MenuBar findFirstMenuBar(javafx.scene.Parent root) {
    java.util.Deque<javafx.scene.Parent> stack = new java.util.ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      javafx.scene.Parent p = stack.pop();
      if (p instanceof javafx.scene.control.MenuBar mb) {
        return mb;
      }
      for (javafx.scene.Node n : p.getChildrenUnmodifiable()) {
        if (n instanceof javafx.scene.control.MenuBar mb2) {
          return mb2;
        }
        if (n instanceof javafx.scene.Parent pr) {
          stack.push(pr);
        }
      }
    }
    return null;
  }

  // Choisit le menu "principal" parmi quelques libellés, sinon le 1er
  private static javafx.scene.control.Menu findMainMenu(javafx.scene.control.MenuBar bar) {
    if (bar.getMenus().isEmpty()) {
      return null;
    }
    java.util.List<String> preferred =
        java.util.Arrays.asList("Menu", "Fichier", "Principal", "Accueil", "Main", "File");
    for (String name : preferred) {
      for (javafx.scene.control.Menu m : bar.getMenus()) {
        if (name.equalsIgnoreCase(m.getText())) {
          return m;
        }
      }
    }
    // Par défaut, retourner le 1er menu
    return bar.getMenus().get(0);
  }

  @Override
  public void stop() {
    try {
      if (this.dataSource != null) {
        this.dataSource.close();
      }
    } catch (Exception ignored) {
    }
  }

  public static void main(String[] args) {
    // Configuration très agressive pour macOS Apple Silicon avec JavaFX 17
    System.setProperty("prism.order", "sw");
    System.setProperty("prism.verbose", "false");
    System.setProperty("prism.forceGPU", "false");
    System.setProperty("prism.forcePowerOfTwo", "false");
    System.setProperty("prism.useTouchInput", "false");

    // Désactiver complètement l'accélération et les optimisations
    System.setProperty("glass.accessible.force", "false");
    System.setProperty("com.apple.macos.useScreenMenuBar", "false");
    System.setProperty("javafx.platform.macos.useInheritedChannels", "false");
    System.setProperty("javafx.animation.fullspeed", "false");
    System.setProperty("javafx.pulseLogger", "false");

    // Mode de compatibilité maximale
    System.setProperty("prism.allowhidpi", "false");
    System.setProperty("glass.win.uiScale", "1.0");
    System.setProperty("glass.gtk.uiScale", "1.0");

    // Désactiver les animations et effets
    System.setProperty("javafx.animation.pulse", "60");
    System.setProperty("javafx.scene.control.skin.MSAA", "false");

    System.out.println(
        "Démarrage MAGSAV avec JavaFX "
            + System.getProperty("javafx.version", "17")
            + " sur macOS");
    System.out.println("Mode de compatibilité Apple Silicon activé");

    try {
      // Si on passe --smoke, propager en propriété système pour scripts/outillage
      for (String a : args) {
        if ("--smoke".equals(a)) {
          System.setProperty("magsav.smoke", "true");
        }
      }
      launch(args);
    } catch (Exception e) {
      System.err.println("Erreur lors du lancement de l'interface graphique :");
      e.printStackTrace();
      System.err.println("\nLe problème NSTrackingRectTag est connu sur macOS Apple Silicon.");
      System.err.println("Utilisez l'interface CLI avec: ./gradlew runCli");
    }
  }
}
