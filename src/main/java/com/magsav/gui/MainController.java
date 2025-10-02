package com.magsav.gui;

import static com.magsav.gui.util.UiAlerts.*;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.imports.CSVImporter;
import com.magsav.label.LabelService;
import com.magsav.media.AvatarService;
import com.magsav.model.Category;
import com.magsav.model.DossierSAV;
import com.magsav.model.ProductSummary;
import com.magsav.qr.QRCodeService;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.repo.ProductRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

public class MainController implements Initializable {

  // Tableau Interventions
  @FXML private TableView<DossierSAV> tableView;
  @FXML private TableColumn<DossierSAV, Long> colId;
  @FXML private TableColumn<DossierSAV, String> colCode;
  @FXML private TableColumn<DossierSAV, String> colProprietaire;
  @FXML private TableColumn<DossierSAV, String> colAppareil;
  @FXML private TableColumn<DossierSAV, String> colProbleme;
  @FXML private TableColumn<DossierSAV, String> colStatut;
  @FXML private TableColumn<DossierSAV, String> colDateCreation;
  @FXML private TableColumn<DossierSAV, String> colDateReparation;

  // Filtres Interventions
  @FXML private TextField iProductField;
  @FXML private TextField iSnField;
  @FXML private TextField iCodeField;
  @FXML private ComboBox<String> iStatusFilter;
  @FXML private ComboBox<Category> iCategoryCombo;
  @FXML private ComboBox<Category> iSubcategoryCombo;
  @FXML private DatePicker iEntreeFromPicker;
  @FXML private DatePicker iEntreeToPicker;
  @FXML private DatePicker iSortieFromPicker;
  @FXML private DatePicker iSortieToPicker;
  @FXML private Button iSearchButton;

  // Cadre Produits
  @FXML private TableView<ProductSummary> productsTable;
  @FXML private TableColumn<ProductSummary, String> colPProduit;
  @FXML private TableColumn<ProductSummary, String> colPSn;
  @FXML private TableColumn<ProductSummary, String> colPCount;
  @FXML private TableColumn<ProductSummary, String> colPLastIn;
  @FXML private TableColumn<ProductSummary, String> colPLastOut;

  // Filtres Produits
  @FXML private TextField pProductField;
  @FXML private TextField pSnField;
  @FXML private TextField pCodeField;
  @FXML private ComboBox<String> pStatusFilter;
  @FXML private ComboBox<Category> pCategoryCombo;
  @FXML private ComboBox<Category> pSubcategoryCombo;
  @FXML private Button pSearchButton;
  @FXML private TextField globalSearchField;
  @FXML private ListView<String> globalSuggestions;
  @FXML private Button btnClearSearch; // bouton croix pour vider la recherche

  // Menu
  @FXML private MenuButton menuButton;
  @FXML private MenuItem menuImportCsv;
  @FXML private MenuItem menuUsers;
  @FXML private MenuItem menuAdmin;
  @FXML private MenuItem menuCategories;
  @FXML private MenuItem menuPreferences;
  @FXML private MenuItem menuPurchases;
  @FXML private MenuItem menuRma;
  @FXML private MenuItem menuDocuments;
  @FXML private MenuItem menuPrint;
  @FXML private Button importButton; // peut être absent du FXML
  @FXML private Button statusChangeButton; // peut être absent du FXML
  @FXML private Button labelButton; // peut être absent du FXML
  @FXML private Button downloadLabelButton; // peut être absent du FXML
  @FXML private Button refreshButton;
  @FXML private Button btnNewProduct;
  @FXML private Button btnNewIntervention;
  @FXML private Label statusLabel;
  // Volet aperçu
  @FXML private Label previewTitle;
  @FXML private Label previewLine1;
  @FXML private Label previewLine2;
  @FXML private Label previewLine3;
  @FXML private javafx.scene.image.ImageView previewPhoto;
  @FXML private javafx.scene.image.ImageView previewQr;
  @FXML private javafx.scene.image.ImageView previewManufacturerLogo;
  // Nouveaux boutons dans le volet Aperçu
  @FXML private Button btnDownloadProductLabel;
  @FXML private Button btnOpenProduct;
  @FXML private Button btnOpenIntervention;

  private DossierSAVRepository dossierRepo;
  private CategoryRepository categoryRepo;
  private ProductRepository productRepo;
  private HikariDataSource dataSource;
  private CSVImporter csvImporter;
  private QRCodeService qrService;
  private LabelService labelService;
  private ObservableList<DossierSAV> dossiersList;
  private ObservableList<ProductSummary> productsList;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initializeServices();
    configureTableView();
    configureControls();
    registerGlobalAccelerators();
    loadCategories();
    loadProducts();
    loadInterventions();
  }

  private boolean openInterventionOnDoubleClick = true;
  private boolean darkTheme; // état effectif (après résolution de 'os')
  private String themePref = "light"; // 'light' | 'dark' | 'os'

  private void initializeServices() {
    try {
      Config config = new Config();
      Path configPath = Path.of("application.yml");
      if (configPath.toFile().exists()) {
        config = Config.load(configPath);
      }
      // Charger préférences UI (le double-clic produit est désormais toujours actif)
      this.openInterventionOnDoubleClick =
          config.getBoolean("ui.openInterventionOnDoubleClick", true);
      this.themePref = config.get("ui.theme", "light");
      this.darkTheme = resolveDarkTheme(this.themePref);

      String dbUrl = config.get("app.database.url", "jdbc:sqlite:magsav.db");
      if (!dbUrl.startsWith("jdbc:")) {
        dbUrl = "jdbc:sqlite:" + dbUrl;
      }

      this.dataSource = DB.init(dbUrl);

      this.dossierRepo = new DossierSAVRepository(this.dataSource);
      this.categoryRepo = new CategoryRepository(this.dataSource);
      this.productRepo = new ProductRepository(this.dataSource);
      this.csvImporter = new CSVImporter(dataSource);
      this.qrService = new QRCodeService();
      this.labelService = new LabelService();
      // Brancher menus opérations
      if (menuPurchases != null) {
        menuPurchases.setOnAction(e -> openPurchasesView());
      }
      if (menuRma != null) {
        menuRma.setOnAction(e -> openRmaView());
      }
      if (menuDocuments != null) {
        menuDocuments.setOnAction(e -> openDocumentsView());
      }
      if (menuPrint != null) {
        menuPrint.setOnAction(e -> openPrintView());
      }

      // Appliquer le thème une fois la scène prête
      Platform.runLater(() -> applyTheme(darkTheme));
      statusLabel.setText("Services initialisés");
    } catch (Exception e) {
      showError("Erreur d'initialisation", e.getMessage());
    }
  }

  private void openPurchasesView() {
    try {
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ops/purchases.fxml"));
      // injecter le DataSource du contrôleur via un controllerFactory
      loader.setControllerFactory(
          cls -> {
            try {
              if ("com.magsav.gui.ops.PurchasesController".equals(cls.getName())) {
                java.lang.reflect.Constructor<?> ctor =
                    cls.getDeclaredConstructor(javax.sql.DataSource.class);
                return ctor.newInstance(this.dataSource);
              }
              return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      var root = loader.load();
      var stage = new javafx.stage.Stage();
      stage.setTitle("Demandes de devis (Achats)");
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root, 900, 600));
      stage.show();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void openRmaView() {
    try {
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ops/rma.fxml"));
      loader.setControllerFactory(
          cls -> {
            try {
              if ("com.magsav.gui.ops.RmaController".equals(cls.getName())) {
                var ctor = cls.getDeclaredConstructor(javax.sql.DataSource.class);
                return ctor.newInstance(this.dataSource);
              }
              return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      var root = loader.load();
      var stage = new javafx.stage.Stage();
      stage.setTitle("Demandes RMA");
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root, 900, 600));
      stage.show();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void openDocumentsView() {
    try {
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ops/documents.fxml"));
      loader.setControllerFactory(
          cls -> {
            try {
              if ("com.magsav.gui.ops.DocumentsController".equals(cls.getName())) {
                var ctor = cls.getDeclaredConstructor(javax.sql.DataSource.class);
                return ctor.newInstance(this.dataSource);
              }
              return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      var root = loader.load();
      var stage = new javafx.stage.Stage();
      stage.setTitle("Documents");
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root, 1000, 600));
      stage.show();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void openPrintView() {
    try {
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ops/print.fxml"));
      var root = loader.load();
      var stage = new javafx.stage.Stage();
      stage.setTitle("Impressions / Étiquettes");
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root, 700, 400));
      stage.show();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void applyTheme(boolean dark) {
    try {
      var scene = statusLabel.getScene();
      if (scene == null) {
        return;
      }
      String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
      if (dark) {
        if (!scene.getStylesheets().contains(darkCss)) {
          scene.getStylesheets().add(darkCss);
        }
      } else {
        scene.getStylesheets().remove(darkCss);
      }
    } catch (Exception ignored) {
    }
  }

  private boolean resolveDarkTheme(String themePref) {
    return switch (themePref) {
      case "dark" -> true;
      case "os" -> detectOsDarkMode();
      default -> false;
    };
  }

  private boolean detectOsDarkMode() {
    try {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("mac")) {
        // macOS: lire AppleInterfaceStyle via defaults
        Process p =
            new ProcessBuilder("/usr/bin/defaults", "read", "-g", "AppleInterfaceStyle").start();
        try (java.io.BufferedReader r =
            new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
          String out = r.readLine();
          int code = p.waitFor();
          return code == 0 && out != null && "Dark".equalsIgnoreCase(out.trim());
        }
      }
      if (osName.contains("win")) {
        // Windows: AppsUseLightTheme = 0 => dark; 1 => light
        String key = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
        try {
          Process p = new ProcessBuilder("reg", "query", key, "/v", "AppsUseLightTheme").start();
          try (java.io.BufferedReader r =
              new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
            String line;
            String last = null;
            while ((line = r.readLine()) != null) {
              if (line.contains("AppsUseLightTheme")) {
                last = line;
              }
            }
            p.waitFor();
            if (last != null) {
              String[] parts = last.trim().split("\\s+");
              String val = parts[parts.length - 1];
              int v;
              try {
                v =
                    val.toLowerCase().startsWith("0x")
                        ? Integer.parseInt(val.substring(2), 16)
                        : Integer.parseInt(val);
              } catch (Exception ex) {
                v = 1;
              }
              return v == 0; // 0 = dark
            }
          }
        } catch (Exception ignore) {
        }
        // fallback: SystemUsesLightTheme (pour l'OS global)
        try {
          Process p = new ProcessBuilder("reg", "query", key, "/v", "SystemUsesLightTheme").start();
          try (java.io.BufferedReader r =
              new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
            String line;
            String last = null;
            while ((line = r.readLine()) != null) {
              if (line.contains("SystemUsesLightTheme")) {
                last = line;
              }
            }
            p.waitFor();
            if (last != null) {
              String[] parts = last.trim().split("\\s+");
              String val = parts[parts.length - 1];
              int v;
              try {
                v =
                    val.toLowerCase().startsWith("0x")
                        ? Integer.parseInt(val.substring(2), 16)
                        : Integer.parseInt(val);
              } catch (Exception ex) {
                v = 1;
              }
              return v == 0; // 0 = dark
            }
          }
        } catch (Exception ignore) {
        }
        return false;
      }
      if (osName.contains("nux")
          || osName.contains("nix")
          || osName.contains("aix")
          || osName.contains("bsd")) {
        // Linux/Unix: GNOME color-scheme (prefer-dark) ou gtk-theme contenant "-dark"; sinon
        // GTK_THEME env
        try {
          Process p =
              new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
                  .start();
          try (java.io.BufferedReader r =
              new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
            String out = r.readLine();
            p.waitFor();
            if (out != null && out.toLowerCase().contains("prefer-dark")) {
              return true;
            }
          }
        } catch (Exception ignore) {
        }
        try {
          Process p =
              new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme")
                  .start();
          try (java.io.BufferedReader r =
              new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
            String out = r.readLine();
            p.waitFor();
            if (out != null && out.toLowerCase().contains("dark")) {
              return true;
            }
          }
        } catch (Exception ignore) {
        }
        String gtkTheme = System.getenv("GTK_THEME");
        if (gtkTheme != null && gtkTheme.toLowerCase().contains("dark")) {
          return true;
        }
        return false;
      }
      // Autres OS: fallback clair
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private void configureTableView() {
    // Configuration des colonnes avec les bonnes propriétés du DossierSAV
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colCode.setCellValueFactory(
        cellData -> {
          String code = cellData.getValue().getCode();
          String display =
              code != null && !code.isBlank()
                  ? code.toUpperCase()
                  : ("ID" + cellData.getValue().getId());
          return new SimpleStringProperty(display);
        });
    colProprietaire.setCellValueFactory(new PropertyValueFactory<>("proprietaire"));

    // Colonne combinée pour l'appareil
    colAppareil.setCellValueFactory(
        cellData -> {
          DossierSAV dossier = cellData.getValue();
          String appareil =
              String.format("%s (%s)", dossier.getProduit(), dossier.getNumeroSerie());
          return new SimpleStringProperty(appareil);
        });

    colProbleme.setCellValueFactory(new PropertyValueFactory<>("panne"));
    colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
    colDateReparation.setCellValueFactory(new PropertyValueFactory<>("dateSortie"));

    // Liste observable pour la table
    dossiersList = FXCollections.observableArrayList();
    tableView.setItems(dossiersList);

    // Permettre la sélection
    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    // Double-clic interventions (préférence)
    tableView.setRowFactory(
        tv -> {
          TableRow<DossierSAV> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (!openInterventionOnDoubleClick) {
                  return;
                }
                if (event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2
                    && !row.isEmpty()) {
                  tableView.getSelectionModel().select(row.getIndex());
                  handleOpenIntervention();
                }
              });
          return row;
        });

    // Table Produits (si présente)
    if (productsTable != null) {
      colPProduit.setCellValueFactory(ps -> new SimpleStringProperty(ps.getValue().getProduit()));
      colPSn.setCellValueFactory(ps -> new SimpleStringProperty(ps.getValue().getNumeroSerie()));
      colPCount.setCellValueFactory(
          ps -> new SimpleStringProperty(Long.toString(ps.getValue().getInterventionsCount())));
      colPLastIn.setCellValueFactory(
          ps -> new SimpleStringProperty(toDateString(ps.getValue().getLastDateEntree())));
      colPLastOut.setCellValueFactory(
          ps -> new SimpleStringProperty(toDateString(ps.getValue().getLastDateSortie())));
      productsList = FXCollections.observableArrayList();
      productsTable.setItems(productsList);

      // Sélection d'un produit => filtrer interventions + remplir aperçu
      productsTable
          .getSelectionModel()
          .selectedItemProperty()
          .addListener(
              (obs, oldV, newV) -> {
                if (newV != null) {
                  // on force une seule sélection cohérente
                  if (tableView != null) {
                    tableView.getSelectionModel().clearSelection();
                  }
                  if (iProductField != null) {
                    iProductField.setText(newV.getProduit());
                  }
                  if (iSnField != null) {
                    iSnField.setText(newV.getNumeroSerie());
                  }
                  updateProductPreview(newV);
                  handleInterventionSearch();
                  // Activer les actions produit, désactiver intervention
                  if (btnDownloadProductLabel != null) {
                    btnDownloadProductLabel.setDisable(false);
                  }
                  if (btnOpenProduct != null) {
                    btnOpenProduct.setDisable(false);
                    btnOpenProduct.setVisible(true);
                    btnOpenProduct.setManaged(true);
                  }
                  if (btnOpenIntervention != null) {
                    btnOpenIntervention.setDisable(true);
                  }
                } else {
                  if (btnDownloadProductLabel != null) {
                    btnDownloadProductLabel.setDisable(true);
                  }
                  if (btnOpenProduct != null) {
                    btnOpenProduct.setDisable(true);
                    btnOpenProduct.setVisible(false);
                    btnOpenProduct.setManaged(false);
                  }
                }
              });

      // Double-clic sur une ligne produit => ouvrir la fiche produit (toujours actif)
      // + Menu contextuel clic droit: Ouvrir Fiche, Nouvelle Intervention (désactivée si une
      // intervention est en cours pour ce produit)
      productsTable.setRowFactory(
          tv -> {
            TableRow<ProductSummary> row = new TableRow<>();
            // Double clic
            row.setOnMouseClicked(
                event -> {
                  if (event.getButton() == MouseButton.PRIMARY
                      && event.getClickCount() == 2
                      && !row.isEmpty()) {
                    productsTable.getSelectionModel().select(row.getIndex());
                    handleOpenProduct();
                  }
                });

            // Menu contextuel
            ContextMenu ctx = new ContextMenu();
            MenuItem openItem = new MenuItem("Ouvrir Fiche");
            openItem.setOnAction(
                e -> {
                  if (!row.isEmpty()) {
                    productsTable.getSelectionModel().select(row.getIndex());
                    handleOpenProduct();
                  }
                });
            MenuItem newIntervItem = new MenuItem("Nouvelle Intervention");
            newIntervItem.setOnAction(
                e -> {
                  if (!row.isEmpty()) {
                    productsTable.getSelectionModel().select(row.getIndex());
                    openNewInterventionDialog();
                  }
                });
            ctx.getItems().addAll(openItem, newIntervItem);

            // Activer/désactiver selon l'état de la ligne
            row.itemProperty()
                .addListener(
                    (o, oldV, newV) -> {
                      if (newV == null) {
                        row.setContextMenu(null);
                        return;
                      }
                      row.setContextMenu(ctx);
                      // Par défaut, on autorise, puis on valide en arrière-plan
                      newIntervItem.setDisable(false);
                      String sn = newV.getNumeroSerie();
                      if (sn != null && !sn.isBlank()) {
                        // Vérifier en arrière-plan s'il existe une intervention non clôturée sur ce
                        // SN
                        Task<Boolean> t =
                            new Task<>() {
                              @Override
                              protected Boolean call() throws Exception {
                                try {
                                  List<DossierSAV> hist = dossierRepo.findAllByNumeroSerieExact(sn);
                                  if (hist.isEmpty()) {
                                    return false;
                                  }
                                  DossierSAV last = hist.get(0); // trié par date entrée desc
                                  String st = last.getStatut();
                                  boolean closedByStatus = isClosedStatus(st);
                                  boolean hasOutDate = last.getDateSortie() != null;
                                  // Considérer "ouverte" si non-fermée par statut ET pas de date de
                                  // sortie
                                  return !(closedByStatus || hasOutDate);
                                } catch (Exception ex) {
                                  return false; // En cas d'erreur, ne pas bloquer
                                }
                              }
                            };
                        t.setOnSucceeded(
                            ev -> {
                              Boolean hasOpen = t.getValue();
                              // Désactiver si une intervention est en cours/existante non clôturée
                              newIntervItem.setDisable(Boolean.TRUE.equals(hasOpen));
                            });
                        new Thread(t).start();
                      }
                    });

            // Ne montrer le menu contextuel que pour les lignes non vides
            row.emptyProperty()
                .addListener(
                    (obs, wasEmpty, isNowEmpty) -> {
                      if (isNowEmpty) {
                        row.setContextMenu(null);
                      } else {
                        row.setContextMenu(ctx);
                      }
                    });
            return row;
          });
    }
  }

  // Considérer une intervention comme clôturée si le statut correspond à "Terminé", "Annulé" ou
  // équivalents usuels
  private boolean isClosedStatus(String s) {
    if (s == null) {
      return false;
    }
    String t = s.toLowerCase().trim();
    // Normalisations rapides
    t = t.replace('é', 'e').replace('è', 'e');
    t = t.replace(' ', '_');
    return t.startsWith("termine") || t.startsWith("annule") || t.startsWith("livre");
  }

  private void configureControls() {
    // Statuts
    var statusItems =
        FXCollections.observableArrayList("Tous", "En attente", "En cours", "Terminé", "Annulé");
    if (iStatusFilter != null) {
      iStatusFilter.setItems(statusItems);
      iStatusFilter.setValue("Tous");
    }
    if (pStatusFilter != null) {
      pStatusFilter.setItems(statusItems);
      pStatusFilter.setValue("Tous");
    }

    // Recherche globale: suggestions et déclenchement
    if (globalSearchField != null && globalSuggestions != null) {
      globalSuggestions.setOnMouseClicked(
          e -> {
            if (e.getClickCount() == 2 && !globalSuggestions.getSelectionModel().isEmpty()) {
              String selection = globalSuggestions.getSelectionModel().getSelectedItem();
              globalSearchField.setText(selection);
              globalSuggestions.setVisible(false);
              globalSuggestions.setManaged(false);
              runGlobalSearch(selection);
            }
          });
      globalSearchField.setOnKeyPressed(
          e -> {
            switch (e.getCode()) {
              case DOWN -> {
                if (!globalSuggestions.getItems().isEmpty()) {
                  globalSuggestions.requestFocus();
                  globalSuggestions.getSelectionModel().select(0);
                }
              }
              case ESCAPE -> {
                globalSuggestions.setVisible(false);
                globalSuggestions.setManaged(false);
              }
              default -> {}
            }
          });
      globalSuggestions.setOnKeyPressed(
          e -> {
            switch (e.getCode()) {
              case ENTER -> {
                String sel = globalSuggestions.getSelectionModel().getSelectedItem();
                if (sel != null) {
                  globalSearchField.setText(sel);
                  globalSuggestions.setVisible(false);
                  globalSuggestions.setManaged(false);
                  runGlobalSearch(sel);
                }
              }
              case ESCAPE -> {
                globalSuggestions.setVisible(false);
                globalSuggestions.setManaged(false);
                globalSearchField.requestFocus();
              }
              default -> {}
            }
          });
      globalSearchField
          .textProperty()
          .addListener(
              (obs, o, n) -> {
                boolean hasText = n != null && !n.isBlank();
                if (btnClearSearch != null) {
                  btnClearSearch.setVisible(hasText);
                  btnClearSearch.setManaged(hasText);
                }
                if (n == null || n.isBlank()) {
                  globalSuggestions.setVisible(false);
                  globalSuggestions.setManaged(false);
                  return;
                }
                // debounce léger
                javafx.concurrent.Task<Void> t =
                    new javafx.concurrent.Task<>() {
                      @Override
                      protected Void call() throws Exception {
                        List<String> sugg = dossierRepo.searchSuggestions(n, 8);
                        Platform.runLater(
                            () -> {
                              globalSuggestions.getItems().setAll(sugg);
                              boolean vis = !sugg.isEmpty();
                              globalSuggestions.setVisible(vis);
                              globalSuggestions.setManaged(vis);
                            });
                        return null;
                      }
                    };
                new Thread(t).start();
              });
      globalSearchField.setOnAction(e -> runGlobalSearch(globalSearchField.getText()));

      if (btnClearSearch != null) {
        btnClearSearch.setOnAction(
            e -> {
              globalSearchField.clear();
              globalSuggestions.setVisible(false);
              globalSuggestions.setManaged(false);
              if (productsList != null) {
                productsList.clear();
              }
              if (dossiersList != null) {
                dossiersList.clear();
              }
              // Recharger listes par défaut
              loadProducts();
              loadInterventions();
              statusLabel.setText("Recherche réinitialisée");
            });
        // état initial
        btnClearSearch.setVisible(false);
        btnClearSearch.setManaged(false);
        btnClearSearch.setTooltip(new Tooltip("Effacer la recherche"));
      }
    }

    // Menu
    if (menuImportCsv != null) {
      menuImportCsv.setOnAction(e -> handleImport());
    }
    if (menuCategories != null) {
      menuCategories.setOnAction(e -> openCategoryDialog());
    }
    if (menuUsers != null) {
      menuUsers.setOnAction(
          e ->
              showInfo(
                  "Utilisateurs",
                  "La gestion des utilisateurs est disponible dans l'interface Web."));
    }
    if (menuAdmin != null) {
      menuAdmin.setOnAction(
          e ->
              showInfo("Administrateur", "Configuration avancée disponible dans l'interface Web."));
    }
    if (menuPreferences != null) {
      menuPreferences.setOnAction(e -> openPreferencesDialog());
    }

    // Événements des boutons
    if (pSearchButton != null) {
      pSearchButton.setOnAction(e -> handleProductSearch());
    }
    if (iSearchButton != null) {
      iSearchButton.setOnAction(e -> handleInterventionSearch());
    }
    if (importButton != null) {
      importButton.setOnAction(e -> handleImport());
    }
    if (statusChangeButton != null) {
      statusChangeButton.setOnAction(e -> handleStatusChange());
    }
    if (labelButton != null) {
      labelButton.setOnAction(e -> handleLabelGeneration());
    }
    if (downloadLabelButton != null) {
      downloadLabelButton.setOnAction(e -> handleLabelDownload());
    }
    if (btnDownloadProductLabel != null) {
      btnDownloadProductLabel.setOnAction(e -> handleProductLabelDownload());
      btnDownloadProductLabel.setDisable(true);
    }
    if (btnOpenProduct != null) {
      btnOpenProduct.setOnAction(e -> handleOpenProduct());
      btnOpenProduct.setDisable(true);
    }
    if (btnOpenIntervention != null) {
      btnOpenIntervention.setOnAction(e -> handleOpenIntervention());
      btnOpenIntervention.setDisable(true);
    }
    refreshButton.setOnAction(
        e -> {
          loadProducts();
          loadInterventions();
        });
    if (btnNewProduct != null) {
      btnNewProduct.setOnAction(e -> openNewProductDialog());
    }
    if (btnNewIntervention != null) {
      btnNewIntervention.setOnAction(e -> openNewInterventionDialog());
    }
    tableView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, o, n) -> {
              if (n != null) {
                if (productsTable != null) {
                  productsTable.getSelectionModel().clearSelection();
                }
                updateDossierPreview(n);
                if (btnOpenIntervention != null) {
                  btnOpenIntervention.setDisable(false);
                }
                // Ne pas afficher le bouton produit si aucun produit n'est sélectionné dans la
                // liste
                if (btnDownloadProductLabel != null) {
                  btnDownloadProductLabel.setDisable(false);
                }
                if (btnOpenProduct != null) {
                  btnOpenProduct.setDisable(true);
                  btnOpenProduct.setVisible(false);
                  btnOpenProduct.setManaged(false);
                }
              } else {
                if (btnOpenIntervention != null) {
                  btnOpenIntervention.setDisable(true);
                }
              }
            });
  }

  private void registerGlobalAccelerators() {
    // Enregistrer Cmd+, pour ouvrir Préférences (fonctionne sur macOS/Windows/Linux via SHORTCUT)
    Platform.runLater(
        () -> {
          try {
            var scene = statusLabel != null ? statusLabel.getScene() : null;
            if (scene == null) {
              return;
            }
            var combo = new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN);
            scene.getAccelerators().put(combo, this::openPreferencesDialog);
          } catch (Exception ignored) {
          }
        });
  }

  private void openNewProductDialog() {
    Dialog<ButtonType> dlg = new Dialog<>();
    dlg.setTitle("Nouveau produit");
    DialogPane pane = new DialogPane();
    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);
    grid.setPrefWidth(420);
    TextField tfName = new TextField();
    tfName.setPromptText("Nom du produit");
    TextField tfSn = new TextField();
    tfSn.setPromptText("Numéro de série (optionnel)");
    Label lbInfo = new Label();
    lbInfo.setWrapText(true);
    grid.addRow(0, new Label("Nom"), tfName);
    grid.addRow(1, new Label("N° de série"), tfSn);
    grid.add(lbInfo, 0, 2, 2, 1);
    pane.setContent(grid);
    pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    if (darkTheme) {
      try {
        String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
        pane.getStylesheets().add(darkCss);
      } catch (Exception ignore) {
      }
    }
    dlg.setDialogPane(pane);
    dlg.initOwner(statusLabel.getScene().getWindow());
    dlg.setResizable(true);
    final Button btnOk = (Button) pane.lookupButton(ButtonType.OK);
    btnOk.setDisable(true);
    tfName
        .textProperty()
        .addListener((o, ov, nv) -> btnOk.setDisable(nv == null || nv.trim().isBlank()));
    tfSn.textProperty()
        .addListener(
            (o, ov, nv) -> {
              String sn = nv != null ? nv.trim() : "";
              if (sn.isBlank()) {
                lbInfo.setText("");
                return;
              }
              Task<Boolean> t =
                  new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                      try (var c = dataSource.getConnection();
                          var ps =
                              c.prepareStatement("SELECT 1 FROM produits WHERE numero_serie=?")) {
                        ps.setString(1, sn);
                        try (var rs = ps.executeQuery()) {
                          return rs.next();
                        }
                      } catch (Exception ex) {
                        return false;
                      }
                    }
                  };
              t.setOnSucceeded(
                  ev ->
                      lbInfo.setText(
                          Boolean.TRUE.equals(t.getValue())
                              ? "Ce numéro de série existe déjà"
                              : ""));
              new Thread(t).start();
            });
    var res = dlg.showAndWait();
    if (res.isPresent() && res.get() == ButtonType.OK) {
      String name = tfName.getText() != null ? tfName.getText().trim() : null;
      String sn = tfSn.getText() != null ? tfSn.getText().trim() : null;
      if (name == null || name.isBlank()) {
        return;
      }
      statusLabel.setText("Création produit...");
      Task<Void> t =
          new Task<>() {
            @Override
            protected Void call() throws Exception {
              try (var c = dataSource.getConnection()) {
                // Si SN fourni et déjà présent, simplement mettre à jour le nom si vide
                if (sn != null && !sn.isBlank()) {
                  boolean exists;
                  try (var ps = c.prepareStatement("SELECT 1 FROM produits WHERE numero_serie=?")) {
                    ps.setString(1, sn);
                    try (var rs = ps.executeQuery()) {
                      exists = rs.next();
                    }
                  }
                  if (exists) {
                    try (var ps =
                        c.prepareStatement(
                            "UPDATE produits SET produit=COALESCE(?, produit) WHERE numero_serie=?")) {
                      ps.setString(1, name);
                      ps.setString(2, sn);
                      ps.executeUpdate();
                    }
                  } else {
                    // créer avec code unique
                    String pcode = genUniqueProductCode(c);
                    try (var ps =
                        c.prepareStatement(
                            "INSERT INTO produits(produit, numero_serie, code) VALUES(?,?,?)")) {
                      ps.setString(1, name);
                      ps.setString(2, sn);
                      ps.setString(3, pcode);
                      ps.executeUpdate();
                    }
                  }
                } else {
                  // SN vide: schéma autorise NULL → insérer avec numero_serie NULL
                  String pcode = genUniqueProductCode(c);
                  try (var ps =
                      c.prepareStatement(
                          "INSERT INTO produits(produit, numero_serie, code) VALUES(?,?,?)")) {
                    ps.setString(1, name);
                    ps.setNull(2, java.sql.Types.VARCHAR);
                    ps.setString(3, pcode);
                    ps.executeUpdate();
                  }
                }
              }
              return null;
            }

            @Override
            protected void succeeded() {
              Platform.runLater(
                  () -> {
                    statusLabel.setText("Produit créé");
                    loadProducts();
                  });
            }

            @Override
            protected void failed() {
              Platform.runLater(
                  () -> {
                    showError("Création produit", getException().getMessage());
                    statusLabel.setText("Erreur création produit");
                  });
            }
          };
      new Thread(t).start();
    }
  }

  private void openNewInterventionDialog() {
    Dialog<ButtonType> dlg = new Dialog<>();
    dlg.setTitle("Nouvelle intervention");
    DialogPane pane = new DialogPane();
    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);
    grid.setPrefWidth(560);
    ComboBox<String> cbName = new ComboBox<>();
    cbName.setEditable(true);
    cbName.setPromptText("Nom du produit");
    ComboBox<String> cbSn = new ComboBox<>();
    cbSn.setEditable(true);
    cbSn.setPromptText("Numéro de série");
    ComboBox<String> cbCode = new ComboBox<>();
    cbCode.setEditable(true);
    cbCode.setPromptText("Identifiant unique produit (AA1234)");
    Label lbHint = new Label();
    lbHint.setWrapText(true);
    grid.addRow(0, new Label("Nom"), cbName);
    grid.addRow(1, new Label("N° de série"), cbSn);
    grid.addRow(2, new Label("Code produit"), cbCode);
    grid.add(lbHint, 0, 3, 2, 1);
    pane.setContent(grid);
    pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    if (darkTheme) {
      try {
        String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
        pane.getStylesheets().add(darkCss);
      } catch (Exception ignore) {
      }
    }
    dlg.setDialogPane(pane);
    dlg.initOwner(statusLabel.getScene().getWindow());
    dlg.setResizable(true);
    final Button btnOk = (Button) pane.lookupButton(ButtonType.OK);
    btnOk.setDisable(false);
    // Validation stricte du code (AA1234): uppercase auto + désactiver OK si invalide
    Runnable validateForm =
        () -> {
          String codeTxt = cbCode.getEditor().getText();
          if (codeTxt != null && !codeTxt.isBlank()) {
            String up = codeTxt.toUpperCase();
            if (!up.matches("[A-Z]{2}[0-9]{4}")) {
              btnOk.setDisable(true);
              lbHint.setText("Code produit invalide (format AA1234)");
              return;
            }
          }
          btnOk.setDisable(false);
        };
    // Charger des noms produits existants
    try {
      Task<java.util.List<String>> loadNames =
          new Task<>() {
            @Override
            protected java.util.List<String> call() throws Exception {
              try (var c = dataSource.getConnection();
                  var ps =
                      c.prepareStatement(
                          "SELECT DISTINCT produit FROM produits WHERE produit IS NOT NULL AND TRIM(produit)<>'' ORDER BY produit")) {
                try (var rs = ps.executeQuery()) {
                  java.util.ArrayList<String> l = new java.util.ArrayList<>();
                  while (rs.next()) {
                    l.add(rs.getString(1));
                  }
                  return l;
                }
              }
            }
          };
      loadNames.setOnSucceeded(e -> cbName.getItems().setAll(loadNames.getValue()));
      new Thread(loadNames).start();
    } catch (Exception ignore) {
    }

    // Helpers pour charger les SN et codes selon le nom saisi
    Runnable refreshSnAndCodes =
        () -> {
          String nameTxt = cbName.getEditor().getText();
          String name = nameTxt != null ? nameTxt.trim() : "";
          if (name.isBlank()) {
            cbSn.getItems().clear();
            cbCode.getItems().clear();
            return;
          }
          Task<Void> t =
              new Task<>() {
                @Override
                protected Void call() throws Exception {
                  try (var c = dataSource.getConnection()) {
                    try (var ps =
                        c.prepareStatement(
                            "SELECT DISTINCT numero_serie FROM produits WHERE lower(produit)=lower(?) AND numero_serie IS NOT NULL AND TRIM(numero_serie)<>'' ORDER BY numero_serie")) {
                      ps.setString(1, name);
                      try (var rs = ps.executeQuery()) {
                        java.util.List<String> sns = new java.util.ArrayList<>();
                        while (rs.next()) {
                          sns.add(rs.getString(1));
                        }
                        Platform.runLater(
                            () ->
                              cbSn.getItems().setAll(sns));
                      }
                    }
                    try (var ps =
                        c.prepareStatement(
                            "SELECT DISTINCT code FROM produits WHERE lower(produit)=lower(?) AND code IS NOT NULL AND TRIM(code)<>'' ORDER BY code")) {
                      ps.setString(1, name);
                      try (var rs = ps.executeQuery()) {
                        java.util.List<String> codes = new java.util.ArrayList<>();
                        while (rs.next()) {
                          codes.add(rs.getString(1));
                        }
                        Platform.runLater(
                            () ->
                              cbCode.getItems().setAll(codes));
                      }
                    }
                  }
                  return null;
                }
              };
          new Thread(t).start();
        };
    cbName.getEditor().textProperty().addListener((o, ov, nv) -> refreshSnAndCodes.run());
    cbName.valueProperty().addListener((o, ov, nv) -> refreshSnAndCodes.run());

    // Hints dynamiques
    java.util.function.Consumer<String> check =
        v -> {
          String s = v != null ? v.trim() : "";
          if (s.isBlank()) {
            lbHint.setText("");
            return;
          }
          Task<Boolean> t =
              new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                  try (var c = dataSource.getConnection()) {
                    if (s.matches("[A-Za-z]{2}[0-9]{4}")) {
                      try (var ps =
                          c.prepareStatement("SELECT 1 FROM produits WHERE upper(code)=upper(?)")) {
                        ps.setString(1, s);
                        try (var rs = ps.executeQuery()) {
                          return rs.next();
                        }
                      }
                    }
                    try (var ps =
                        c.prepareStatement("SELECT 1 FROM produits WHERE numero_serie=?")) {
                      ps.setString(1, s);
                      try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                          return true;
                        }
                      }
                    }
                    try (var ps =
                        c.prepareStatement(
                            "SELECT 1 FROM produits WHERE lower(produit)=lower(?)")) {
                      ps.setString(1, s);
                      try (var rs = ps.executeQuery()) {
                        return rs.next();
                      }
                    }
                  } catch (Exception ex) {
                    return false;
                  }
                }
              };
          t.setOnSucceeded(
              ev ->
                  lbHint.setText(
                      Boolean.TRUE.equals(t.getValue())
                          ? "Produit trouvé"
                          : "Produit introuvable, il sera proposé à la création"));
          new Thread(t).start();
        };
    cbName.getEditor().textProperty().addListener((o, ov, nv) -> check.accept(nv));
    cbSn.getEditor().textProperty().addListener((o, ov, nv) -> check.accept(nv));
    cbCode
        .getEditor()
        .textProperty()
        .addListener(
            (o, ov, nv) -> {
              String up = nv != null ? nv.toUpperCase() : "";
              if (nv != null && !nv.equals(up)) {
                int caret = cbCode.getEditor().getCaretPosition();
                cbCode.getEditor().setText(up);
                cbCode.getEditor().positionCaret(Math.min(caret, up.length()));
              }
              check.accept(up);
              validateForm.run();
            });
    // Aussi valider quand une valeur est choisie dans la liste
    cbCode
        .valueProperty()
        .addListener(
            (o, ov, nv) -> {
              if (nv != null) {
                cbCode.getEditor().setText(nv.toUpperCase());
                validateForm.run();
              }
            });
    // Validation initiale
    validateForm.run();
    var res = dlg.showAndWait();
    if (res.isPresent() && res.get() == ButtonType.OK) {
      String name =
          cbName.getEditor().getText() != null ? cbName.getEditor().getText().trim() : null;
      String sn = cbSn.getEditor().getText() != null ? cbSn.getEditor().getText().trim() : null;
      String code =
          cbCode.getEditor().getText() != null ? cbCode.getEditor().getText().trim() : null;
      if (code != null && !code.isBlank()) {
        code = code.toUpperCase();
      }
      // Protection supplémentaire : si un code est saisi mais invalide, bloquer (ne pas régénérer
      // silencieusement)
      final String codeFinal = code;
      statusLabel.setText("Création intervention...");
      Task<Void> t =
          new Task<>() {
            @Override
            protected Void call() throws Exception {
              try (var c = dataSource.getConnection()) {
                if (codeFinal != null
                    && !codeFinal.isBlank()
                    && !codeFinal.matches("[A-Z]{2}[0-9]{4}")) {
                  throw new IllegalArgumentException(
                      "Code produit invalide. Format attendu AA1234");
                }
                // Résoudre un produit existant
                Long productId = null;
                String resolvedSn = null;
                String resolvedName = null;
                if (codeFinal != null && codeFinal.matches("[A-Z]{2}[0-9]{4}")) {
                  try (var ps =
                      c.prepareStatement(
                          "SELECT id,produit,numero_serie,code FROM produits WHERE upper(code)=upper(?)")) {
                    ps.setString(1, codeFinal);
                    try (var rs = ps.executeQuery()) {
                      if (rs.next()) {
                        productId = rs.getLong(1);
                        resolvedName = rs.getString(2);
                        resolvedSn = rs.getString(3);
                      }
                    }
                  }
                }
                if (productId == null && sn != null && !sn.isBlank()) {
                  try (var ps =
                      c.prepareStatement(
                          "SELECT id,produit,numero_serie,code FROM produits WHERE numero_serie=?")) {
                    ps.setString(1, sn);
                    try (var rs = ps.executeQuery()) {
                      if (rs.next()) {
                        productId = rs.getLong(1);
                        resolvedName = rs.getString(2);
                        resolvedSn = rs.getString(3);
                      }
                    }
                  }
                }
                // Ne faire le fallback par nom QUE si ni code ni sn fournis
                if (productId == null
                    && (codeFinal == null || codeFinal.isBlank())
                    && (sn == null || sn.isBlank())
                    && name != null
                    && !name.isBlank()) {
                  try (var ps =
                      c.prepareStatement(
                          "SELECT id,produit,numero_serie,code FROM produits WHERE lower(produit)=lower(?) ORDER BY created_at DESC LIMIT 1")) {
                    ps.setString(1, name);
                    try (var rs = ps.executeQuery()) {
                      if (rs.next()) {
                        productId = rs.getLong(1);
                        resolvedName = rs.getString(2);
                        resolvedSn = rs.getString(3);
                      }
                    }
                  }
                }

                // Créer le produit si introuvable
                if (productId == null) {
                  String newSn = sn != null && !sn.isBlank() ? sn : null;
                  String newName = name != null && !name.isBlank() ? name : "Produit";
                  String newCode =
                      codeFinal != null && codeFinal.matches("[A-Z]{2}[0-9]{4}")
                          ? codeFinal
                          : genUniqueProductCode(c);
                  try (var ps =
                      c.prepareStatement(
                          "INSERT INTO produits(produit, numero_serie, code) VALUES(?,?,?)",
                          Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, newName);
                    if (newSn == null) {
                      ps.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                      ps.setString(2, newSn);
                    }
                    ps.setString(3, newCode);
                    ps.executeUpdate();
                    try (var rs = ps.getGeneratedKeys()) {
                      if (rs.next()) {
                        productId = rs.getLong(1);
                      }
                    }
                  }
                  resolvedSn = newSn;
                  resolvedName = newName;
                }

                // Créer l'intervention minimaliste liée au produit (SN)
                String produitNom = name != null && !name.isBlank() ? name : resolvedName;
                String serie = sn != null && !sn.isBlank() ? sn : resolvedSn;
                // Si SN toujours vide, on laisse null (schéma autorise NULL)
                // Insérer l'intervention via repo pour bénéficier de la logique (code unique +
                // ensure product)
                DossierSAV dossier =
                    new DossierSAV(
                        null,
                        null,
                        produitNom,
                        serie,
                        null,
                        null,
                        "En attente",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
                dossierRepo.save(dossier);
              }
              return null;
            }

            @Override
            protected void succeeded() {
              Platform.runLater(
                  () -> {
                    statusLabel.setText("Intervention créée");
                    loadProducts();
                    loadInterventions();
                  });
            }

            @Override
            protected void failed() {
              Platform.runLater(
                  () -> {
                    showError("Nouvelle intervention", getException().getMessage());
                    statusLabel.setText("Erreur création intervention");
                  });
            }
          };
      new Thread(t).start();
    }
  }

  private String genUniqueProductCode(java.sql.Connection c) throws Exception {
    String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    java.util.Random rnd = new java.util.Random();
    while (true) {
      char a = letters.charAt(rnd.nextInt(letters.length()));
      char b = letters.charAt(rnd.nextInt(letters.length()));
      int num = rnd.nextInt(10000);
      String code =
          String.valueOf(a) + b + String.format("%04d", num);
      try (var ps = c.prepareStatement("SELECT 1 FROM produits WHERE code=?")) {
        ps.setString(1, code);
        try (var rs = ps.executeQuery()) {
          if (!rs.next()) {
            return code;
          }
        }
      }
    }
  }

  private void runGlobalSearch(String term) {
    if (term == null || term.isBlank()) {
      return;
    }
    statusLabel.setText("Recherche...");
    // Rechercher pour les produits et les interventions
    Task<List<ProductSummary>> tp =
        new Task<>() {
          @Override
          protected List<ProductSummary> call() throws Exception {
            return dossierRepo.searchProductsByTerm(term.trim());
          }
        };
    Task<List<DossierSAV>> ti =
        new Task<>() {
          @Override
          protected List<DossierSAV> call() throws Exception {
            return dossierRepo.searchInterventionsByTerm(term.trim());
          }
        };
    tp.setOnSucceeded(
        ev ->
            Platform.runLater(
                () -> {
                  if (productsList != null) {
                    productsList.setAll(tp.getValue());
                  }
                }));
    ti.setOnSucceeded(
        ev ->
            Platform.runLater(
                () -> {
                  if (dossiersList != null) {
                    dossiersList.setAll(ti.getValue());
                  }
                  statusLabel.setText(
                      String.format(
                          "Produits: %d, Interventions: %d",
                          productsList != null ? productsList.size() : 0,
                          dossiersList != null ? dossiersList.size() : 0));
                }));
    tp.setOnFailed(
        ev ->
            Platform.runLater(
                () -> showError("Recherche produits", tp.getException().getMessage())));
    ti.setOnFailed(
        ev ->
            Platform.runLater(
                () -> showError("Recherche interventions", ti.getException().getMessage())));
    new Thread(tp).start();
    new Thread(ti).start();
  }

  private void openPreferencesDialog() {
    try {
      var loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/preferences-dialog.fxml"));
      DialogPane pane = loader.load();
      Dialog<ButtonType> dlg = new Dialog<>();
      dlg.setTitle("Préférences");
      dlg.setDialogPane(pane);
      try {
        if (darkTheme) {
          String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
          if (!pane.getStylesheets().contains(darkCss)) {
            pane.getStylesheets().add(darkCss);
          }
        }
      } catch (Exception ignored) {
      }
      dlg.setResizable(false);
      dlg.initOwner(statusLabel.getScene().getWindow());
      var result = dlg.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        // Recharger la préférence en mémoire pour effet immédiat
        try {
          var cfg =
              java.nio.file.Path.of("application.yml").toFile().exists()
                  ? Config.load(java.nio.file.Path.of("application.yml"))
                  : new Config();
          // Le produit s'ouvre toujours au double-clic; ne relire que l'intervention
          this.openInterventionOnDoubleClick =
              cfg.getBoolean("ui.openInterventionOnDoubleClick", true);
          this.themePref = cfg.get("ui.theme", "light");
          boolean resolved = resolveDarkTheme(this.themePref);
          if (resolved != this.darkTheme) {
            this.darkTheme = resolved;
            applyTheme(this.darkTheme);
          }
        } catch (Exception ignored) {
        }
      }
    } catch (Exception ex) {
      showError("Préférences", ex.getMessage());
    }
  }

  private void openCategoryDialog() {
    try {
      Dialog<ButtonType> dlg = new Dialog<>();
      dlg.setTitle("Catégories");
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/category-dialog.fxml"));
      DialogPane pane = loader.load();
      CategoryDialogController ctrl = loader.getController();
      dlg.setDialogPane(pane);
      try {
        if (darkTheme) {
          String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
          if (!pane.getStylesheets().contains(darkCss)) {
            pane.getStylesheets().add(darkCss);
          }
        }
      } catch (Exception ignored) {
      }
      dlg.setResizable(true);
      dlg.showAndWait();
      ctrl.dispose();
    } catch (Exception ex) {
      showError("Catégories", ex.getMessage());
    }
  }

  private void loadCategories() {
    if (categoryRepo == null) {
      return;
    }
    try {
      List<Category> roots = categoryRepo.findRoots();
      StringConverter<Category> conv =
          new StringConverter<>() {
            @Override
            public String toString(Category c) {
              return c == null ? "" : c.name();
            }

            @Override
            public Category fromString(String s) {
              return null;
            }
          };
      if (pCategoryCombo != null) {
        pCategoryCombo.setConverter(conv);
        pCategoryCombo.setButtonCell(
            new ListCell<>() {
              @Override
              protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.name());
              }
            });
        pCategoryCombo.setCellFactory(
            cb ->
                new ListCell<>() {
                  @Override
                  protected void updateItem(Category c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "" : c.name());
                  }
                });
        pCategoryCombo.setItems(FXCollections.observableArrayList(roots));
        pCategoryCombo
            .valueProperty()
            .addListener((obs, o, n) -> fillSubcategories(n, pSubcategoryCombo));
      }
      if (iCategoryCombo != null) {
        iCategoryCombo.setConverter(conv);
        iCategoryCombo.setButtonCell(
            new ListCell<>() {
              @Override
              protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.name());
              }
            });
        iCategoryCombo.setCellFactory(
            cb ->
                new ListCell<>() {
                  @Override
                  protected void updateItem(Category c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "" : c.name());
                  }
                });
        iCategoryCombo.setItems(FXCollections.observableArrayList(roots));
        iCategoryCombo
            .valueProperty()
            .addListener((obs, o, n) -> fillSubcategories(n, iSubcategoryCombo));
      }
    } catch (Exception e) {
      // Non bloquant
    }
  }

  private void fillSubcategories(Category parent, ComboBox<Category> subCombo) {
    if (subCombo == null) {
      return;
    }
    if (parent == null) {
      subCombo.setItems(FXCollections.observableArrayList());
      subCombo.setValue(null);
      return;
    }
    try {
      List<Category> children = categoryRepo.findChildren(parent.id());
      StringConverter<Category> conv =
          new StringConverter<>() {
            @Override
            public String toString(Category c) {
              return c == null ? "" : c.name();
            }

            @Override
            public Category fromString(String s) {
              return null;
            }
          };
      subCombo.setConverter(conv);
      subCombo.setButtonCell(
          new ListCell<>() {
            @Override
            protected void updateItem(Category c, boolean empty) {
              super.updateItem(c, empty);
              setText(empty || c == null ? "" : c.name());
            }
          });
      subCombo.setCellFactory(
          cb ->
              new ListCell<>() {
                @Override
                protected void updateItem(Category c, boolean empty) {
                  super.updateItem(c, empty);
                  setText(empty || c == null ? "" : c.name());
                }
              });
      subCombo.setItems(FXCollections.observableArrayList(children));
    } catch (Exception e) {
      // ignore
    }
  }

  private String toDateString(LocalDate d) {
    return d == null ? "" : d.toString();
  }

  private void handleLabelDownload() {
    DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner une intervention.");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer l'étiquette PDF");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
    String suggested =
        String.format(
            "etiquette-intervention-%s.pdf",
            selected.getCode() != null && !selected.getCode().isBlank()
                ? selected.getCode()
                : ("ID" + selected.getId()));
    fileChooser.setInitialFileName(suggested);

    File dest = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
    if (dest == null) {
      return; // annulé
    }

    statusLabel.setText("Génération de l'étiquette...");

    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            Path tempDir = java.nio.file.Files.createTempDirectory("magsav-label");
            try {
              Long dossierId = selected.getId();
              String code = selected.getCode();
              String codeOrId = code != null && !code.isBlank() ? code : ("ID" + dossierId);
              String qrContent = String.format("MAGSAV:%s:%s", codeOrId, selected.getNumeroSerie());
              Path qrPath = tempDir.resolve("qr-" + codeOrId + ".png");

              // Générer le QR code
              qrService.generateToFile(qrContent, 256, qrPath);

              // Construire le titre
              String titre =
                  String.format(
                      "Intervention %s%n%s%n%s - SN:%s",
                      codeOrId,
                      selected.getProprietaire(),
                      selected.getProduit(),
                      selected.getNumeroSerie());

              // Générer directement le PDF à l'emplacement choisi
              Path pdfPath = dest.toPath();
              labelService.createSimpleLabel(pdfPath, titre, qrPath);
            } finally {
              // Nettoyage du répertoire temporaire
              try {
                java.nio.file.Files.walk(tempDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(
                        p -> {
                          try {
                            java.nio.file.Files.deleteIfExists(p);
                          } catch (Exception ignored) {
                          }
                        });
              } catch (Exception ignored) {
              }
            }
            return null;
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  showInfo(
                      "Étiquette enregistrée",
                      "Le fichier a été enregistré: " + dest.getAbsolutePath());
                  statusLabel.setText("Étiquette enregistrée");
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  showError("Erreur de génération", getException().getMessage());
                  statusLabel.setText("Erreur de génération");
                });
          }
        };

    new Thread(task).start();
  }

  private void loadInterventions() {
    statusLabel.setText("Chargement des interventions...");
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
                  statusLabel.setText(String.format("Chargé: %d interventions", getValue().size()));
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

  private void loadProducts() {
    if (productsTable == null) {
      return;
    }
    statusLabel.setText("Chargement des produits...");
    Task<List<ProductSummary>> t =
        new Task<>() {
          @Override
          protected List<ProductSummary> call() throws Exception {
            return dossierRepo.searchProducts(null, null, null, null, null, null);
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  productsList.clear();
                  productsList.addAll(getValue());
                  statusLabel.setText(String.format("Produits: %d", getValue().size()));
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () ->
                  showError("Erreur chargement produits", getException().getMessage()));
          }
        };
    new Thread(t).start();
  }

  private void handleInterventionSearch() {
    String produit = iProductField != null ? iProductField.getText().trim() : null;
    String numeroSerie = iSnField != null ? iSnField.getText().trim() : null;
    // Si les filtres n'existent plus (UI épurée), dériver depuis la sélection du tableau Produits
    if ((iProductField == null && iSnField == null)
        && productsTable != null
        && productsTable.getSelectionModel().getSelectedItem() != null) {
      ProductSummary sel = productsTable.getSelectionModel().getSelectedItem();
      produit = sel.getProduit();
      numeroSerie = sel.getNumeroSerie();
    }
    String code = iCodeField != null ? iCodeField.getText().trim() : null;
    String statut =
        iStatusFilter != null
                && iStatusFilter.getValue() != null
                && !"Tous".equals(iStatusFilter.getValue())
            ? iStatusFilter.getValue()
            : null;
    Long categoryId =
        iCategoryCombo != null && iCategoryCombo.getValue() != null
            ? iCategoryCombo.getValue().id()
            : null;
    Long subcategoryId =
        iSubcategoryCombo != null && iSubcategoryCombo.getValue() != null
            ? iSubcategoryCombo.getValue().id()
            : null;
    String dInFrom =
        iEntreeFromPicker != null && iEntreeFromPicker.getValue() != null
            ? iEntreeFromPicker.getValue().toString()
            : null;
    String dInTo =
        iEntreeToPicker != null && iEntreeToPicker.getValue() != null
            ? iEntreeToPicker.getValue().toString()
            : null;
    String dOutFrom =
        iSortieFromPicker != null && iSortieFromPicker.getValue() != null
            ? iSortieFromPicker.getValue().toString()
            : null;
    String dOutTo =
        iSortieToPicker != null && iSortieToPicker.getValue() != null
            ? iSortieToPicker.getValue().toString()
            : null;

    statusLabel.setText("Filtrage interventions...");
    final String fProduit = produit;
    final String fNumeroSerie = numeroSerie;
    final String fCode = code;
    final String fStatut = statut;
    final Long fCategoryId = categoryId;
    final Long fSubcategoryId = subcategoryId;
    final String fDInFrom = dInFrom;
    final String fDInTo = dInTo;
    final String fDOutFrom = dOutFrom;
    final String fDOutTo = dOutTo;
    Task<List<DossierSAV>> task =
        new Task<>() {
          @Override
          protected List<DossierSAV> call() throws Exception {
            return dossierRepo.searchInterventions(
                fProduit,
                fNumeroSerie,
                fCode,
                fCategoryId,
                fSubcategoryId,
                fStatut,
                fDInFrom,
                fDInTo,
                fDOutFrom,
                fDOutTo);
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  dossiersList.clear();
                  dossiersList.addAll(getValue());
                  statusLabel.setText(String.format("Interventions: %d", getValue().size()));
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(() -> showError("Erreur de filtre", getException().getMessage()));
          }
        };
    new Thread(task).start();
  }

  private void handleProductSearch() {
    if (productsTable == null) {
      return;
    }
    String produit = pProductField != null ? pProductField.getText().trim() : null;
    String numeroSerie = pSnField != null ? pSnField.getText().trim() : null;
    String code = pCodeField != null ? pCodeField.getText().trim() : null;
    String statut =
        pStatusFilter != null
                && pStatusFilter.getValue() != null
                && !"Tous".equals(pStatusFilter.getValue())
            ? pStatusFilter.getValue()
            : null;
    Long categoryId =
        pCategoryCombo != null && pCategoryCombo.getValue() != null
            ? pCategoryCombo.getValue().id()
            : null;
    Long subcategoryId =
        pSubcategoryCombo != null && pSubcategoryCombo.getValue() != null
            ? pSubcategoryCombo.getValue().id()
            : null;

    statusLabel.setText("Recherche produits...");
    Task<List<ProductSummary>> t =
        new Task<>() {
          @Override
          protected List<ProductSummary> call() throws Exception {
            return dossierRepo.searchProducts(
                produit, numeroSerie, code, categoryId, subcategoryId, statut);
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  productsList.clear();
                  productsList.addAll(getValue());
                  statusLabel.setText(String.format("Produits: %d", getValue().size()));
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> showError("Erreur recherche produits", getException().getMessage()));
          }
        };
    new Thread(t).start();
  }

  private void handleImport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Sélectionner un fichier CSV");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

    // L'UI peut ne pas contenir le bouton d'import : utiliser une fenêtre sûre comme fallback
    javafx.stage.Window ownerWindow = null;
    try {
      if (importButton != null && importButton.getScene() != null) {
        ownerWindow = importButton.getScene().getWindow();
      }
    } catch (Exception ignored) {
    }
    if (ownerWindow == null) {
      try {
        if (statusLabel != null && statusLabel.getScene() != null) {
          ownerWindow = statusLabel.getScene().getWindow();
        }
      } catch (Exception ignored) {
      }
    }

    File selectedFile =
        ownerWindow != null
            ? fileChooser.showOpenDialog(ownerWindow)
            : fileChooser.showOpenDialog(null);
    if (selectedFile != null) {
      statusLabel.setText("Import en cours...");

      Task<Void> task =
          new Task<>() {
            @Override
            protected Void call() throws Exception {
              csvImporter.importDossiersSAV(selectedFile.toPath());
              return null;
            }

            @Override
            protected void succeeded() {
              Platform.runLater(
                  () -> {
                    showInfo("Import réussi", "Les données ont été importées avec succès.");
                    statusLabel.setText("Import terminé");
                    loadProducts();
                    loadInterventions();
                  });
            }

            @Override
            protected void failed() {
              Platform.runLater(
                  () -> {
                    showError("Erreur d'import", getException().getMessage());
                    statusLabel.setText("Erreur d'import");
                  });
            }
          };

      new Thread(task).start();
    }
  }

  private void handleStatusChange() {
    DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
      return;
    }

    ChoiceDialog<String> dialog =
        new ChoiceDialog<>("En attente", "En attente", "En cours", "Terminé", "Annulé");
    dialog.setTitle("Changer le statut");
    dialog.setHeaderText("Intervention #" + selected.getId());
    dialog.setContentText("Nouveau statut:");

    dialog
        .showAndWait()
        .ifPresent(
            newStatus -> {
              statusLabel.setText("Mise à jour du statut...");

              Task<Void> task =
                  new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                      DossierSAV updated = selected.withStatut(newStatus);
                      if ("Terminé".equals(newStatus)) {
                        updated = updated.withDateSortie(java.time.LocalDate.now());
                      }
                      dossierRepo.update(updated);
                      return null;
                    }

                    @Override
                    protected void succeeded() {
                      Platform.runLater(
                          () -> {
                            statusLabel.setText("Statut mis à jour");
                            loadInterventions();
                          });
                    }

                    @Override
                    protected void failed() {
                      Platform.runLater(
                          () -> {
                            showError("Erreur de mise à jour", getException().getMessage());
                            statusLabel.setText("Erreur de mise à jour");
                          });
                    }
                  };

              new Thread(task).start();
            });
  }

  private void handleLabelGeneration() {
    DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner une intervention.");
      return;
    }

    statusLabel.setText("Génération de l'étiquette...");

    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            // Génération d'étiquette basée sur le modèle simplifié (post-import CSV)
            // Évite l'appel SAVService basé sur un autre schéma (dossiers/appareils/clients)
            java.nio.file.Path outDir = java.nio.file.Path.of("output");
            java.nio.file.Files.createDirectories(outDir);

            Long dossierId = selected.getId();
            String code = selected.getCode();
            String codeOrId = code != null && !code.isBlank() ? code : ("ID" + dossierId);
            String qrContent = String.format("MAGSAV:%s:%s", codeOrId, selected.getNumeroSerie());
            java.nio.file.Path qrPath = outDir.resolve("qr-" + codeOrId + ".png");
            java.nio.file.Path pdfPath = outDir.resolve("etiquette-" + codeOrId + ".pdf");

            // Générer le QR code
            qrService.generateToFile(qrContent, 256, qrPath);

            // Construire un titre lisible pour l'étiquette
            String titre =
                String.format(
                    "Intervention %s%n%s%n%s - SN:%s",
                    codeOrId,
                    selected.getProprietaire(),
                    selected.getProduit(),
                    selected.getNumeroSerie());

            // Générer le PDF d'étiquette
            labelService.createSimpleLabel(pdfPath, titre, qrPath);
            return null;
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  showInfo(
                      "Étiquette générée", "L'étiquette a été générée dans le dossier output/");
                  statusLabel.setText("Étiquette générée");
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  showError("Erreur de génération", getException().getMessage());
                  statusLabel.setText("Erreur de génération");
                });
          }
        };

    new Thread(task).start();
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

  private void updateProductPreview(ProductSummary p) {
    if (previewTitle == null) {
      return;
    }
    // Organisation: Catégorie, Sous-catégorie, Nom, Identifiant unique, N° de série, Photo produit
    previewTitle.setText("Produit");
    // Catégorie/Sous-catégorie: tenter via l'historique (exact SN -> LIKE SN -> par nom de produit)
    String catName = "—";
    String subName = "—";
    try {
      java.util.List<DossierSAV> hist = dossierRepo.findAllByNumeroSerieExact(p.getNumeroSerie());
      if (hist == null || hist.isEmpty()) {
        var byLike = dossierRepo.findByNumeroSerie(p.getNumeroSerie());
        if (byLike != null && !byLike.isEmpty()) {
          hist = byLike;
        }
      }
      if ((hist == null || hist.isEmpty()) && p.getProduit() != null && !p.getProduit().isBlank()) {
        try {
          hist =
              dossierRepo.searchInterventions(
                  p.getProduit(), null, null, null, null, null, null, null, null, null);
        } catch (Exception ignored) {
        }
      }
      if (hist != null && !hist.isEmpty()) {
        DossierSAV last = hist.get(0);
        Long cid = last.getCategoryId();
        Long sid = last.getSubcategoryId();
        if (cid != null) {
          var c = categoryRepo.findById(cid);
          if (c != null && c.name() != null && !c.name().isBlank()) {
            catName = c.name();
          }
        }
        if (sid != null) {
          var sc = categoryRepo.findById(sid);
          if (sc != null && sc.name() != null && !sc.name().isBlank()) {
            subName = sc.name();
          }
        }
      }
    } catch (Exception ignored) {
    }
    previewLine1.setText("Catégorie: " + (catName == null ? "" : catName));
    previewLine2.setText("Sous-catégorie: " + (subName == null ? "" : subName));
    String uniqueCode = null;
    try {
      uniqueCode = productRepo.findCodeByNumeroSerie(p.getNumeroSerie());
    } catch (Exception ignored) {
    }
    previewLine3.setText(
        "Nom: "
            + safe(p.getProduit())
            + (uniqueCode != null && !uniqueCode.isBlank()
                ? "\nCode: " + uniqueCode.toUpperCase()
                : "")
            + "\nSN: "
            + safe(p.getNumeroSerie()));
    // Photo
    loadAndSetProductPhoto(p.getNumeroSerie());
    // Logo fabricant
    try {
      String mname = productRepo.findManufacturerNameByNumeroSerie(p.getNumeroSerie());
      loadAndSetManufacturerLogo(mname);
    } catch (Exception ignored) {
      loadAndSetManufacturerLogo(null);
    }
    // QR
    loadAndSetProductQr(p.getNumeroSerie(), uniqueCode);
  }

  private void updateDossierPreview(DossierSAV d) {
    if (previewTitle == null) {
      return;
    }
    String code = d.getCode();
    String codeOrId = code != null && !code.isBlank() ? code.toUpperCase() : ("ID" + d.getId());
    previewTitle.setText("Intervention");
    String cat = resolveCategoryName(d.getCategoryId());
    String sub = resolveCategoryName(d.getSubcategoryId());
    previewLine1.setText("Catégorie: " + safe(cat));
    previewLine2.setText("Sous-catégorie: " + safe(sub));
    previewLine3.setText(
        "Nom: "
            + safe(d.getProduit())
            + "\nCode: "
            + codeOrId
            + "\nSN: "
            + safe(d.getNumeroSerie()));
    loadAndSetProductPhoto(d.getNumeroSerie());
    // Logo fabricant pour l'intervention
    try {
      String mname = productRepo.findManufacturerNameByNumeroSerie(d.getNumeroSerie());
      loadAndSetManufacturerLogo(mname);
    } catch (Exception ignored) {
      loadAndSetManufacturerLogo(null);
    }
    // QR
    loadAndSetInterventionQr(d.getNumeroSerie(), codeOrId);
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }

  private String resolveCategoryName(Long id) {
    if (id == null) {
      return null;
    }
    try {
      var c = categoryRepo.findById(id);
      return c != null ? c.name() : null;
    } catch (Exception e) {
      return null;
    }
  }

  private void loadAndSetProductPhoto(String numeroSerie) {
    try {
      String path = productRepo.findPhotoPathByNumeroSerie(numeroSerie);
      if (path != null && !path.isBlank()) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) {
          var img = new javafx.scene.image.Image(f.toURI().toString(), 200, 200, true, true);
          previewPhoto.setImage(img);
          return;
        }
      }
    } catch (Exception ignore) {
    }
    // Pas de photo, vider l'image
    previewPhoto.setImage(null);
  }

  private void loadAndSetProductQr(String numeroSerie, String codeOrNull) {
    if (previewQr == null) {
      return;
    }
    try {
      String codeOrSn =
          codeOrNull != null && !codeOrNull.isBlank() ? codeOrNull.toUpperCase() : numeroSerie;
      String content = String.format("MAGSAV:PROD:%s:%s", codeOrSn, numeroSerie);
      byte[] png = qrService.generateQRCode(content);
      var img = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(png));
      previewQr.setImage(img);
    } catch (Exception e) {
      previewQr.setImage(null);
    }
  }

  private void loadAndSetManufacturerLogo(String name) {
    if (previewManufacturerLogo == null) {
      return;
    }
    try {
      if (name == null || name.isBlank()) {
        previewManufacturerLogo.setImage(makeInitialsImage("?"));
        return;
      }
      var ds = this.dataSource;
      if (ds == null) {
        previewManufacturerLogo.setImage(makeInitialsImage(name));
        return;
      }
      var repo = new com.magsav.repo.ManufacturerRepository(ds);
      var opt = repo.findByName(name);
      if (opt.isEmpty()) {
        previewManufacturerLogo.setImage(makeInitialsImage(name));
        return;
      }
      String path = opt.get().logoPath();
      if (path == null || path.isBlank()) {
        previewManufacturerLogo.setImage(makeInitialsImage(name));
        return;
      }
      java.io.File f = new java.io.File(path);
      if (!f.exists()) {
        previewManufacturerLogo.setImage(makeInitialsImage(name));
        return;
      }
      var img = new javafx.scene.image.Image(f.toURI().toString(), 80, 24, true, true);
      previewManufacturerLogo.setImage(img);
    } catch (Exception e) {
      previewManufacturerLogo.setImage(makeInitialsImage(name));
    }
  }

  private javafx.scene.image.Image makeInitialsImage(String name) {
    try {
      String initials = AvatarService.computeInitials(name);
      int w = 80;
      int h = 24;
      java.awt.image.BufferedImage bi =
          new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
      java.awt.Graphics2D g = bi.createGraphics();
      g.setRenderingHint(
          java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
          java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(new java.awt.Color(0x42, 0x85, 0xF4)); // bleu
      g.fillRoundRect(0, 0, w, h, 8, 8);
      g.setColor(java.awt.Color.WHITE);
      g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
      java.awt.FontMetrics fm = g.getFontMetrics();
      int tw = fm.stringWidth(initials);
      int th = fm.getAscent();
      int x = (w - tw) / 2;
      int y = (h + th) / 2 - 3;
      g.drawString(initials, x, y);
      g.dispose();
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      javax.imageio.ImageIO.write(bi, "png", baos);
      return new javafx.scene.image.Image(new java.io.ByteArrayInputStream(baos.toByteArray()));
    } catch (Exception e) {
      return null;
    }
  }

  private void loadAndSetInterventionQr(String numeroSerie, String codeOrId) {
    if (previewQr == null) {
      return;
    }
    try {
      String content = String.format("MAGSAV:%s:%s", codeOrId, numeroSerie);
      byte[] png = qrService.generateQRCode(content);
      var img = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(png));
      previewQr.setImage(img);
    } catch (Exception e) {
      previewQr.setImage(null);
    }
  }

  // Bouton d'import photo supprimé de l'UI
  // Ancien handler supprimé
  /*@FXML private void handleImportProductPhoto() {
      // Importer une photo pour le produit sélectionné (dans la table Produits si dispo, sinon à partir de l'intervention sélectionnée)
      String numeroSerie = null;
      String produitName = null;
      if (productsTable != null && productsTable.getSelectionModel().getSelectedItem() != null) {
          numeroSerie = productsTable.getSelectionModel().getSelectedItem().getNumeroSerie();
          produitName = productsTable.getSelectionModel().getSelectedItem().getProduit();
      } else if (tableView != null && tableView.getSelectionModel().getSelectedItem() != null) {
          numeroSerie = tableView.getSelectionModel().getSelectedItem().getNumeroSerie();
          produitName = tableView.getSelectionModel().getSelectedItem().getProduit();
      }
      if (numeroSerie == null || numeroSerie.isBlank()) {
          showWarning("Aucune sélection", "Veuillez sélectionner un produit ou une intervention.");
          return;
      }

      FileChooser fc = new FileChooser();
      fc.setTitle("Choisir une photo produit");
      fc.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
      );
  File file = fc.showOpenDialog(statusLabel.getScene().getWindow());
      if (file == null) return;

      // Copier l'image dans un répertoire local pour référence stable
      try {
          java.nio.file.Path photosDir = java.nio.file.Path.of("photos");
          java.nio.file.Files.createDirectories(photosDir);
          String name = file.getName();
          String ext = "";
          int dot = name.lastIndexOf('.');
          if (dot > 0 && dot < name.length() - 1) {
              ext = name.substring(dot + 1).toLowerCase();
          }
          if (ext.isBlank()) ext = "png";
          java.nio.file.Path dest = photosDir.resolve("prod-" + numeroSerie.replaceAll("[^A-Za-z0-9_-]", "_") + "." + ext);
          java.nio.file.Files.copy(file.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          String photoAbs = dest.toAbsolutePath().toString();
          // Mettre à jour par nom de produit si disponible, sinon par SN
          if (produitName != null && !produitName.isBlank()) {
              try { productRepo.updatePhotoPathByProduit(produitName, photoAbs); } catch (Exception ignore) {}
          } else {
              productRepo.updatePhotoPath(numeroSerie, photoAbs);
          }
          // Rafraîchir l'aperçu
          loadAndSetProductPhoto(numeroSerie);
          showInfo("Photo importée", "Photo associée au produit (SN: " + numeroSerie + ")");
      } catch (Exception ex) {
          showError("Import photo", ex.getMessage());
      }
  }*/

  // Télécharger l'étiquette depuis l'aperçu produit
  @FXML
  private void handleProductLabelDownload() {
    if (productsTable == null || productsTable.getSelectionModel().getSelectedItem() == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un produit dans la liste.");
      return;
    }
    ProductSummary p = productsTable.getSelectionModel().getSelectedItem();
    String sn = p.getNumeroSerie();
    String code = null;
    try {
      code = productRepo.findCodeByNumeroSerie(sn);
    } catch (Exception ignore) {
    }
    String codeOrSn = code != null && !code.isBlank() ? code.toUpperCase() : sn;

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer l'étiquette produit");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
    fileChooser.setInitialFileName("etiquette-produit-" + codeOrSn + ".pdf");
    File dest = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
    if (dest == null) {
      return;
    }

    statusLabel.setText("Génération de l'étiquette produit...");
    final String fCodeOrSn = codeOrSn;
    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("magsav-label");
            try {
              String qrContent = String.format("MAGSAV:PROD:%s:%s", fCodeOrSn, sn);
              java.nio.file.Path qrPath = tempDir.resolve("qr-" + fCodeOrSn + ".png");
              qrService.generateToFile(qrContent, 256, qrPath);
              String titre = String.format("Produit %s%n%s%nSN:%s", fCodeOrSn, p.getProduit(), sn);
              labelService.createSimpleLabel(dest.toPath(), titre, qrPath);
            } finally {
              try {
                java.nio.file.Files.walk(tempDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(
                        path -> {
                          try {
                            java.nio.file.Files.deleteIfExists(path);
                          } catch (Exception ignored) {
                          }
                        });
              } catch (Exception ignored) {
              }
            }
            return null;
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  statusLabel.setText("Étiquette produit enregistrée");
                  showInfo("Étiquette", "Le fichier a été enregistré: " + dest.getAbsolutePath());
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  statusLabel.setText("Erreur de génération");
                  showError("Erreur", getException().getMessage());
                });
          }
        };
    new Thread(task).start();
  }

  @FXML
  private void handleOpenProduct() {
    ProductSummary p = null;
    if (productsTable != null) {
      p = productsTable.getSelectionModel().getSelectedItem();
    }
    // Fallback: si aucun produit sélectionné, tenter depuis l'intervention sélectionnée via SN +
    // recherche d'un résumé minimal
    if (p == null && tableView != null && tableView.getSelectionModel().getSelectedItem() != null) {
      DossierSAV d = tableView.getSelectionModel().getSelectedItem();
      String sn = d.getNumeroSerie();
      if (sn == null || sn.isBlank()) {
        showWarning(
            "Produit introuvable", "Cette intervention n'a pas de numéro de série associé.");
        return;
      }
      try {
        // Construire un ProductSummary minimal depuis le repo des dossiers
        var summaries = dossierRepo.searchProducts(null, sn, null, null, null, null);
        if (!summaries.isEmpty()) {
          p = summaries.get(0);
        }
      } catch (Exception ignored) {
      }
      if (p == null) {
        showWarning("Produit introuvable", "Aucun produit associé au numéro de série: " + sn);
        return;
      }
    }
    if (p == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un produit.");
      return;
    }
    try {
      var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/product-detail.fxml"));
      javafx.scene.Parent root = loader.load();
      ProductDetailController ctrl = loader.getController();
      ctrl.setData(p, productRepo, dataSource, qrService, labelService);
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Fiche produit");
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      // Appliquer thème sombre si actif
      try {
        if (darkTheme) {
          String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
          if (!scene.getStylesheets().contains(darkCss)) {
            scene.getStylesheets().add(darkCss);
          }
        }
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      // Appliquer prefs fenêtre produit
      applyWindowPrefs(stage, "ui.window.productDetail");
      stage.setOnCloseRequest(
          ev -> {
            try {
              saveWindowPrefs(stage, "ui.window.productDetail");
            } catch (Exception ignored) {
            }
          });
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.showAndWait();
    } catch (Exception ex) {
      showError("Fiche produit", ex.getMessage());
    }
  }

  @FXML
  private void handleOpenIntervention() {
    DossierSAV d = tableView.getSelectionModel().getSelectedItem();
    if (d == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner une intervention.");
      return;
    }
    try {
      var loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/intervention-detail.fxml"));
      javafx.scene.Parent root = loader.load();
      InterventionDetailController ctrl = loader.getController();
      ctrl.setData(d, categoryRepo, dossierRepo);
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Fiche intervention");
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      try {
        if (darkTheme) {
          String darkCss = getClass().getResource("/style/dark.css").toExternalForm();
          if (!scene.getStylesheets().contains(darkCss)) {
            scene.getStylesheets().add(darkCss);
          }
        }
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      // Appliquer prefs fenêtre intervention
      applyWindowPrefs(stage, "ui.window.interventionDetail");
      stage.setOnCloseRequest(
          ev -> {
            try {
              saveWindowPrefs(stage, "ui.window.interventionDetail");
            } catch (Exception ignored) {
            }
          });
      stage.initOwner(statusLabel.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.showAndWait();
    } catch (Exception ex) {
      showError("Fiche intervention", ex.getMessage());
    }
  }

  private void applyWindowPrefs(javafx.stage.Stage stage, String prefix) {
    try {
      java.nio.file.Path p = java.nio.file.Path.of("application.yml");
      com.magsav.config.Config cfg =
          p.toFile().exists() ? com.magsav.config.Config.load(p) : new com.magsav.config.Config();
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
      // Normalisation anti-offscreen
      try {
        double x = stage.getX();
        double y = stage.getY();
        double w = stage.getWidth();
        double h = stage.getHeight();
        if (w <= 0) {
          w = 700;
        }
        if (h <= 0) {
          h = 500;
        }
        var screens = javafx.stage.Screen.getScreensForRectangle(x, y, w, h);
        if (screens == null || screens.isEmpty()) {
          var v = javafx.stage.Screen.getPrimary().getVisualBounds();
          stage.setWidth(Math.min(w, v.getWidth()));
          stage.setHeight(Math.min(h, v.getHeight()));
          stage.setX(v.getMinX() + (v.getWidth() - stage.getWidth()) / 2);
          stage.setY(v.getMinY() + (v.getHeight() - stage.getHeight()) / 2);
        } else {
          var v = screens.get(0).getVisualBounds();
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

  private void saveWindowPrefs(javafx.stage.Stage stage, String prefix) throws Exception {
    java.nio.file.Path p = java.nio.file.Path.of("application.yml");
    com.magsav.config.Config cfg =
        p.toFile().exists() ? com.magsav.config.Config.load(p) : new com.magsav.config.Config();
    cfg.set(prefix + ".x", Double.toString(stage.getX()));
    cfg.set(prefix + ".y", Double.toString(stage.getY()));
    cfg.set(prefix + ".w", Double.toString(stage.getWidth()));
    cfg.set(prefix + ".h", Double.toString(stage.getHeight()));
    cfg.set(prefix + ".maximized", Boolean.toString(stage.isMaximized()));
    cfg.save(p);
  }
}
