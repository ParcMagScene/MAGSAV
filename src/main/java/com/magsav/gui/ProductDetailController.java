package com.magsav.gui;

import com.magsav.model.InterventionRow;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.repo.CategoryRepository;
import com.magsav.gui.dialogs.PhotoMosaicController;
import com.magsav.service.ImageNormalizationService;
import com.magsav.service.DataChangeNotificationService;
import com.magsav.service.DataChangeEvent;
import com.magsav.util.EditModeManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

import java.nio.file.*;
import java.util.List;

public class ProductDetailController {

  @FXML private Label lblName, lblCode, lblSN, lblUID, lblSituation, lblOwner;
  @FXML private ImageView imgPhoto, imgLogo, imgQr;

  @FXML private TableView<InterventionRow> historyTable;
  @FXML private TableColumn<InterventionRow, Long> colId;
  @FXML private TableColumn<InterventionRow, String> colStatut, colPanne, colEntree, colSortie;

  @FXML private ComboBox<String> cbManufacturer;
  @FXML private ComboBox<String> cbCategory;
  @FXML private TextField tfNewCategoryOrSubcategory;
  
  // Boutons de contrôle d'édition
  @FXML private Button btnEdit;
  @FXML private Button btnSave;
  @FXML private Button btnCancel;
  @FXML private Button btnChoosePhoto;
  @FXML private Button btnAddCategory;

  private final ProductRepository prodRepo = new ProductRepository();
  private final SocieteRepository societeRepo = new SocieteRepository();
  private final CategoryRepository categoryRepo = new CategoryRepository();
  private final ImageNormalizationService imageService = new ImageNormalizationService();
  private long productId;
  private String originalPhotoFilename; // Pour traquer les changements de photo
  private String currentPhotoFilename;  // Photo actuellement sélectionnée (peut différer de la BDD)
  
  // État d'édition (temporaire)
  private boolean isEditMode = false;
  
  // Gestionnaire de mode d'édition
  private EditModeManager editManager;

  @FXML
  private void initialize() {
    if (cbManufacturer != null) {
      // Utiliser la liste officielle des fabricants depuis la table societes
      cbManufacturer.getItems().setAll(
        societeRepo.findByType("FABRICANT").stream()
          .map(s -> s.nom())
          .toList()
      );
    }
    if (cbCategory != null) { 
      cbCategory.setEditable(true); 
      cbCategory.getItems().setAll(getAllCategories()); 
    }

    // Initialisation des colonnes de l’historique
    if (historyTable != null) {
      colId.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().id()));
      colStatut.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().statut()));
      colPanne.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().panne()));
      colEntree.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().dateEntree()));
      colSortie.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().dateSortie()));

      // Double-clic pour ouvrir la fiche d’intervention
      historyTable.setRowFactory(tv -> {
  TableRow<InterventionRow> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
          if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
            InterventionRow intervention = row.getItem();
            com.magsav.util.Views.openInterventionDetail(intervention.id(), null);
          }
        });
        return row;
      });
    }
    
    // Initialiser en mode lecture seule
    isEditMode = false;
    updateEditableControls();
  }

  public void setProductId(long id) { this.productId = id; load(); }

  private void load() {
    var opt = prodRepo.findDetailedById(productId);
    if (opt.isEmpty()) return;
    var p = opt.get();
    if (lblName != null) lblName.setText(nz(p.nom()));

    if (lblSN != null) lblSN.setText(nz(p.sn()));
    if (lblUID != null) lblUID.setText(nz(p.uid()));
    if (lblSituation != null) lblSituation.setText(nz(p.situation()));

    // Sauvegarder la photo originale pour détecter les changements
    originalPhotoFilename = p.photo();
    currentPhotoFilename = p.photo();  // Initialiser avec la photo actuelle
    
    // Charger la photo du produit
    System.out.println("DEBUG: Photo récupérée de la BDD pour le produit: " + p.photo());
    loadProductPhoto(p.photo());
    
    // Charger le logo du fabricant
    loadManufacturerLogo(p.fabricant());
    
    // Charger le QR code
    loadQrCode(p.uid());

    if (cbManufacturer != null) {
      String manu = p.fabricant();
      if (manu != null && !manu.isBlank() && !cbManufacturer.getItems().contains(manu)) cbManufacturer.getItems().add(manu);
      cbManufacturer.setValue(manu);
    }

    if (cbCategory != null) {
      cbCategory.getItems().setAll(getAllCategories());
      // Pour un produit ayant une sous-catégorie, afficher la sous-catégorie dans la ComboBox
      String categoryToShow = p.subcategory() != null && !p.subcategory().trim().isEmpty() 
                            ? p.subcategory() 
                            : p.category();
      cbCategory.setValue(formatCategoryForDisplay(categoryToShow));
    }
    if (historyTable != null) {
  List<InterventionRow> interventions = new com.magsav.repo.InterventionRepository().findByProductId(productId);
      historyTable.setItems(FXCollections.observableArrayList(interventions));
    }
  }

  private void loadProductPhoto(String photoFilename) {
    if (imgPhoto == null) return;
    if (photoFilename == null || photoFilename.trim().isEmpty()) {
      System.out.println("DEBUG: Aucune photo à charger (photoFilename null ou vide)");
      javafx.application.Platform.runLater(() -> imgPhoto.setImage(null));
      return;
    }
    try {
      System.out.println("DEBUG: Chargement de la photo normalisée: " + photoFilename);
      
      // Utiliser le service de normalisation pour charger l'image optimisée
      Image image = imageService.loadImageForDisplay(photoFilename, ImageNormalizationService.ImageSize.MEDIUM);
      
      if (image != null) {
        System.out.println("DEBUG: Image normalisée chargée avec succès");
        javafx.application.Platform.runLater(() -> {
          imgPhoto.setImage(null); // Effacer l'ancienne
          imgPhoto.setImage(image); // Charger la nouvelle
          System.out.println("DEBUG: Image définie dans l'ImageView");
        });
      } else {
        System.out.println("DEBUG: Échec du chargement de l'image normalisée, tentative avec chemin direct");
        // Fallback vers l'ancien système si l'image normalisée n'existe pas
        Path photoPath = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "photos", photoFilename);
        if (Files.exists(photoPath)) {
          javafx.application.Platform.runLater(() -> {
            imgPhoto.setImage(null);
            String imageUrl = photoPath.toUri().toString() + "?t=" + System.currentTimeMillis();
            Image fallbackImage = new Image(imageUrl, true);
            imgPhoto.setImage(fallbackImage);
            System.out.println("DEBUG: Image fallback définie dans l'ImageView");
          });
        } else {
          System.out.println("DEBUG: Photo introuvable: " + photoPath);
          javafx.application.Platform.runLater(() -> imgPhoto.setImage(null));
        }
      }
    } catch (Exception ex) {
      System.out.println("DEBUG: Erreur lors du chargement de la photo: " + ex.getMessage());
      javafx.application.Platform.runLater(() -> imgPhoto.setImage(null));
    }
  }

  private void loadManufacturerLogo(String manufacturerName) {
    if (imgLogo == null) return;
    if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
      imgLogo.setImage(null);
      return;
    }
    try {
      Path logosDir = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "logos");
      String base = slug(manufacturerName);
      String[] exts = { ".png", ".jpg", ".jpeg", ".svg", ".gif" };
      
      // D'abord essayer avec le slug exact
      for (String ext : exts) {
        Path logoPath = logosDir.resolve(base + ext);
        if (Files.exists(logoPath)) {
          imgLogo.setImage(new Image(logoPath.toUri().toString(), true));
          System.out.println("DEBUG: Logo chargé: " + logoPath);
          return;
        }
      }
      
      // Si pas trouvé, chercher des fichiers qui contiennent le nom du fabricant
      try {
        List<Path> matchingLogos = Files.list(logosDir)
            .filter(Files::isRegularFile)
            .filter(path -> {
              String fileName = path.getFileName().toString().toLowerCase();
              String cleanManufacturer = manufacturerName.toLowerCase().replaceAll("[^a-z0-9]", "");
              String cleanFileName = fileName.replaceAll("[^a-z0-9]", "");
              return cleanFileName.contains(cleanManufacturer) || cleanManufacturer.contains(cleanFileName.split("\\.")[0]);
            })
            .collect(java.util.stream.Collectors.toList());
            
        if (!matchingLogos.isEmpty()) {
          Path logoPath = matchingLogos.get(0); // Prendre le premier match
          imgLogo.setImage(new Image(logoPath.toUri().toString(), true));
          System.out.println("DEBUG: Logo trouvé par correspondance: " + logoPath);
          return;
        }
      } catch (Exception e) {
        System.out.println("DEBUG: Erreur lors de la recherche de logo: " + e.getMessage());
      }
      
      System.out.println("DEBUG: Aucun logo trouvé pour: " + manufacturerName + " (slug: " + base + ")");
      imgLogo.setImage(null);
    } catch (Exception ex) {
      System.out.println("DEBUG: Erreur lors du chargement du logo: " + ex.getMessage());
      imgLogo.setImage(null);
    }
  }

  private void loadQrCode(String uid) {
    if (imgQr == null) return;
    if (uid == null || uid.trim().isEmpty()) {
      imgQr.setImage(null);
      return;
    }
    try {
      javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
        @Override
        protected Void call() throws Exception {
          try {
            Path qrPath = com.magsav.service.QrCodeService.ensureQrPng(uid);
            if (Files.exists(qrPath)) {
              javafx.application.Platform.runLater(() -> {
                imgQr.setImage(new Image(qrPath.toUri().toString(), true));
                System.out.println("DEBUG: QR code chargé dans ProductDetailController: " + qrPath);
              });
            } else {
              javafx.application.Platform.runLater(() -> imgQr.setImage(null));
            }
          } catch (Exception e) {
            System.out.println("DEBUG: Erreur génération QR code dans ProductDetailController: " + e.getMessage());
            javafx.application.Platform.runLater(() -> imgQr.setImage(null));
          }
          return null;
        }
      };
      new Thread(task).start();
    } catch (Exception ex) {
      System.out.println("DEBUG: Erreur lors du chargement du QR code dans ProductDetailController: " + ex.getMessage());
      imgQr.setImage(null);
    }
  }

  private static String slug(String s) {
    if (s == null) return "unknown";
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    return out.replaceAll("^_+|_+$", "");
  }

  @FXML
  private void onChangeManufacturer() {
    if (!isEditMode) return; // Ne pas permettre la modification si pas en mode édition
    if (productId <= 0 || cbManufacturer == null) return;
    String selected = cbManufacturer.getValue();
    String trimmed = selected == null ? null : selected.trim();
    
    // Charger le logo du fabricant sélectionné
    loadManufacturerLogo(trimmed);
    
    // Note: La propagation des changements se fait maintenant uniquement lors de "Valider les changements"
  }

  @FXML
  private void onChangeCategory() {
    if (!isEditMode) return; // Ne pas permettre la modification si pas en mode édition
    // Note: La propagation des changements se fait maintenant uniquement lors de "Valider les changements"
  }

  @FXML
  private void onChoosePhoto() {
    if (!isEditMode) return; // Ne pas permettre la modification si pas en mode édition
    if (productId <= 0 || lblName == null || lblName.getScene() == null) return;
    // Ouvrir directement la mosaïque des photos
    chooseFromLibrary();
  }

  private void chooseFromLibrary() {
    try {
      // Utiliser la vue mosaïque pour la sélection
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/photo_mosaic.fxml"));
      Parent root = loader.load();
      
      PhotoMosaicController mosaicController = loader.getController();
      
      Stage stage = new Stage();
      stage.setTitle("Choisir une photo - Vue mosaïque");
      
      Scene scene = new Scene(root, 800, 600);
      // Appliquer le thème dark
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      stage.setScene(scene);
      
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.initOwner(lblName.getScene().getWindow());
      
      mosaicController.setStage(stage);
      
      stage.showAndWait();
      
      String selectedPhoto = mosaicController.getSelectedPhotoPath();
      if (selectedPhoto != null && !selectedPhoto.trim().isEmpty()) {
        // Mettre à jour seulement l'affichage local, la propagation se fera via "Valider les changements"
        currentPhotoFilename = selectedPhoto;
        loadProductPhoto(selectedPhoto);
      }
      
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la vue mosaïque: " + ex.getMessage()).showAndWait();
    }
  }

  private void importPhoto() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Importer une photo");
    fc.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
        new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
    );
    var file = fc.showOpenDialog(lblName.getScene().getWindow());
    if (file == null) return;
    
    try {
      // Créer un nom de fichier basé sur le nom du produit et l'ID
      String productName = nz(lblName.getText());
      String baseName = productName.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "") + "_" + productId;
      
      System.out.println("DEBUG: Normalisation de l'image importée: " + file.getName());
      
      // Normaliser l'image (crée toutes les tailles nécessaires)
      String normalizedFileName = imageService.normalizeImage(file.toPath(), baseName);
      
      System.out.println("DEBUG: Image normalisée avec succès: " + normalizedFileName);
      
      // Mettre à jour seulement l'affichage local, la propagation se fera via "Valider les changements"
      currentPhotoFilename = normalizedFileName;
      loadProductPhoto(normalizedFileName);
      
    } catch (Exception ex) {
      System.err.println("Erreur lors de la normalisation: " + ex.getMessage());
      ex.printStackTrace();
      new Alert(Alert.AlertType.ERROR, "Erreur lors de l'import et de la normalisation: " + ex.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onAddCategoryOrSubcategory() {
    if (!isEditMode) return; // Ne pas permettre la modification si pas en mode édition
    String name = emptyToNull(tfNewCategoryOrSubcategory == null ? null : tfNewCategoryOrSubcategory.getText());
    
    if (name == null) { 
      new Alert(Alert.AlertType.WARNING, "Saisissez un nom.").showAndWait(); 
      return; 
    }
    
    // Ajouter comme catégorie principale par défaut
    addNewCategory(name);
    
    // Vider le champ de saisie
    if (tfNewCategoryOrSubcategory != null) tfNewCategoryOrSubcategory.clear();
  }
  
  private void addNewCategory(String newCat) {
    // Ajouter à la table categories pour unification
    try {
      com.magsav.repo.CategoryRepository categoryRepo = new com.magsav.repo.CategoryRepository();
      categoryRepo.insert(newCat, null);
      
      // Synchronisation automatique après ajout
      try {
        com.magsav.util.CategorySyncTool.syncCategories();
      } catch (Exception syncError) {
        System.out.println("Avertissement: Synchronisation automatique échouée: " + syncError.getMessage());
      }
    } catch (Exception e) {
      System.out.println("Avertissement: Impossible d'ajouter à la table categories: " + e.getMessage());
    }
    
    // Ajouter la nouvelle catégorie à la ComboBox si elle n'existe pas déjà
    if (cbCategory != null) {
      cbCategory.getItems().setAll(getAllCategories()); // Refresh the entire list
      cbCategory.setValue(formatCategoryForDisplay(newCat));
    }
  }

  @FXML
  private void onCreateIntervention() {
    openWindow("Nouvelle intervention", "/fxml/interventions/forms/new_intervention.fxml");
  }

  @FXML
  private void onExportPdf() {
    new Alert(Alert.AlertType.INFORMATION, "Fonction d'export PDF à implémenter").showAndWait();
  }

  @FXML
  private void onValidateChanges() {
    if (productId <= 0) return;
    
    var productOpt = prodRepo.findDetailedById(productId);
    if (productOpt.isEmpty()) return;
    var currentProduct = productOpt.get();
    
    boolean hasChanges = false;
    StringBuilder changesDescription = new StringBuilder();
    
    // Vérifier les changements du fabricant
    if (cbManufacturer != null && cbManufacturer.getValue() != null) {
      String newManufacturer = cbManufacturer.getValue().trim();
      String currentManufacturer = currentProduct.fabricant();
      if (currentManufacturer == null) currentManufacturer = "";
      
      if (!newManufacturer.equals(currentManufacturer)) {
        hasChanges = true;
        changesDescription.append("• Fabricant: ").append(currentManufacturer.isEmpty() ? "(vide)" : currentManufacturer)
                          .append(" → ").append(newManufacturer.isEmpty() ? "(vide)" : newManufacturer).append("\n");
      }
    }
    
    // Vérifier les changements de catégorie
    if (cbCategory != null && cbCategory.getValue() != null) {
      String newCategory = extractCategoryName(cbCategory.getValue());
      String currentCategory = currentProduct.category();
      if (currentCategory == null) currentCategory = "";
      if (newCategory == null) newCategory = "";
      
      if (!newCategory.equals(currentCategory)) {
        hasChanges = true;
        changesDescription.append("• Catégorie: ").append(currentCategory.isEmpty() ? "(vide)" : currentCategory)
                          .append(" → ").append(newCategory.isEmpty() ? "(vide)" : newCategory).append("\n");
      }
    }
    
    // Vérifier les changements de photo
    String currentPhoto = currentPhotoFilename; // Utiliser la photo sélectionnée localement
    if (currentPhoto == null) currentPhoto = "";
    if (originalPhotoFilename == null) originalPhotoFilename = "";
    
    if (!originalPhotoFilename.equals(currentPhoto)) {
      hasChanges = true;
      changesDescription.append("• Photo: ").append(originalPhotoFilename.isEmpty() ? "(aucune)" : originalPhotoFilename)
                        .append(" → ").append(currentPhoto.isEmpty() ? "(aucune)" : currentPhoto).append("\n");
    }
    
    if (!hasChanges) {
      Alert info = new Alert(Alert.AlertType.INFORMATION);
      info.setTitle("Aucun changement");
      info.setHeaderText("Pas de modifications détectées");
      info.setContentText("Aucune modification n'a été détectée sur ce produit.");
      info.showAndWait();
      return;
    }
    
    // Demander confirmation pour appliquer les changements
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Appliquer les changements");
    confirm.setHeaderText("Propagation des modifications");
    confirm.setContentText(String.format(
        "Les modifications suivantes ont été détectées :\n\n%s\n" +
        "Comment voulez-vous appliquer ces changements ?",
        changesDescription.toString()
    ));
    
    ButtonType applyToAll = new ButtonType("Appliquer à tous les produits du même nom");
    ButtonType applyToOne = new ButtonType("Appliquer à ce produit uniquement");
    ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
    confirm.getButtonTypes().setAll(applyToAll, applyToOne, cancel);
    
    var result = confirm.showAndWait();
    if (result.isPresent()) {
      if (result.get() == applyToAll) {
        // Appliquer les changements à tous les produits du même nom
        if (cbManufacturer != null && cbManufacturer.getValue() != null) {
          String newManufacturer = emptyToNull(cbManufacturer.getValue().trim());
          prodRepo.updateFabricantForSameNameByProduct(productId, newManufacturer);
        }
        
        if (cbCategory != null) {
          String cat = cbCategory.getValue() != null ? extractCategoryName(cbCategory.getValue()) : null;
          prodRepo.updateCategoryNamesForSameNameByProduct(productId, emptyToNull(cat), null);
        }
        
        // Appliquer les changements de photo si nécessaire
        if (currentPhotoFilename != null && !currentPhotoFilename.equals(originalPhotoFilename)) {
          prodRepo.updatePhotoForSameNameByProduct(productId, currentPhotoFilename);
        }
        
        // Recharger l'affichage
        load();
        
        // Notifier les autres fenêtres que les données ont changé
        DataChangeNotificationService.getInstance().notifyDataChanged(
          new DataChangeEvent(DataChangeEvent.Type.PRODUCT_UPDATED, 
            "Fabricant mis à jour pour tous les produits du même nom")
        );
        
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Changements appliqués");
        success.setHeaderText("Modifications sauvegardées");
        success.setContentText("Les changements ont été appliqués à tous les produits du même nom.");
        success.showAndWait();
        
      } else if (result.get() == applyToOne) {
        // Appliquer les changements à ce produit uniquement
        if (cbManufacturer != null && cbManufacturer.getValue() != null) {
          String newManufacturer = emptyToNull(cbManufacturer.getValue().trim());
          prodRepo.updateFabricantById(productId, newManufacturer);
        }
        
        if (cbCategory != null) {
          String cat = cbCategory.getValue() != null ? extractCategoryName(cbCategory.getValue()) : null;
          prodRepo.updateCategoryNamesById(productId, emptyToNull(cat), null);
        }
        
        // Appliquer les changements de photo si nécessaire
        if (currentPhotoFilename != null && !currentPhotoFilename.equals(originalPhotoFilename)) {
          prodRepo.updatePhotoById(productId, currentPhotoFilename);
        }
        
        // Recharger l'affichage
        load();
        
        // Notifier les autres fenêtres que les données ont changé
        DataChangeNotificationService.getInstance().notifyDataChanged(
          new DataChangeEvent(DataChangeEvent.Type.PRODUCT_UPDATED, 
            "Fabricant mis à jour pour le produit ID: " + productId)
        );
        
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Changements appliqués");
        success.setHeaderText("Modifications sauvegardées");
        success.setContentText("Les changements ont été appliqués à ce produit uniquement.");
        success.showAndWait();
        
        // Revenir en mode lecture après sauvegarde
        isEditMode = false;
        updateEditableControls();
        
      } else {
        // L'utilisateur a annulé, remettre les valeurs d'origine
        load();
      }
    } else {
      // L'utilisateur a fermé la dialog, remettre les valeurs d'origine
      load();
    }
  }

  @FXML
  private void onToggleEdit() {
    isEditMode = !isEditMode;
    updateEditableControls();
  }
  
  @FXML
  private void onCancelEdit() {
    isEditMode = false;
    updateEditableControls();
    // Recharger les données originales
    load();
  }
  
  /**
   * Met à jour l'état des contrôles selon le mode d'édition
   */
  private void updateEditableControls() {
    // Activer/désactiver les champs modifiables
    if (cbManufacturer != null) cbManufacturer.setDisable(!isEditMode);
    if (cbCategory != null) cbCategory.setDisable(!isEditMode);
    if (tfNewCategoryOrSubcategory != null) tfNewCategoryOrSubcategory.setDisable(!isEditMode);
    
    // Activer/désactiver les boutons d'action
    if (btnChoosePhoto != null) btnChoosePhoto.setDisable(!isEditMode);
    if (btnAddCategory != null) btnAddCategory.setDisable(!isEditMode);
    
    // Contrôler la visibilité des boutons de contrôle
    if (btnEdit != null) btnEdit.setVisible(!isEditMode);
    if (btnSave != null) btnSave.setVisible(isEditMode);
    if (btnCancel != null) btnCancel.setVisible(isEditMode);
  }

  // Ouvre une fenêtre à partir d'un FXML
  private void openWindow(String title, String fxmlPath) {
    try {
      var url = getClass().getResource(fxmlPath);
      if (url == null) {
        new Alert(Alert.AlertType.ERROR, "FXML introuvable: " + fxmlPath).showAndWait();
        return;
      }
      var loader = new FXMLLoader(url);
      Parent root = loader.load();
      var stage = new Stage();
      stage.setTitle(title);
      Preferences prefs = Preferences.userNodeForPackage(ProductDetailController.class);
      String key = title.replaceAll("[^a-zA-Z0-9]", "_");
      double width = prefs.getDouble(key + ".width", 800);
      double height = prefs.getDouble(key + ".height", 600);
      
      Scene scene = new Scene(root, width, height);
      // Appliquer le thème dark
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      stage.setScene(scene);
      
      stage.setOnCloseRequest(e -> {
        prefs.putDouble(key + ".width", stage.getWidth());
        prefs.putDouble(key + ".height", stage.getHeight());
      });
      stage.show();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture: " + e.getMessage()).showAndWait();
    }
  }

  // Méthodes pour récupérer toutes les catégories depuis les deux systèmes
  private java.util.List<String> getAllCategories() {
    java.util.Set<String> allCategories = new java.util.LinkedHashSet<>();
    
    // Ajouter les catégories de la table 'categories' (système unifié) avec hiérarchie
    try {
      var categories = categoryRepo.findAll();
      System.out.println("DEBUG: Nombre de catégories récupérées: " + categories.size());
      
      // D'abord les catégories principales
      var mainCategories = categories.stream()
          .filter(cat -> cat.parentId() == null)
          .sorted((a, b) -> a.nom().compareTo(b.nom()))
          .toList();
      
      System.out.println("DEBUG: Catégories principales: " + mainCategories.stream().map(c -> c.nom()).toList());
      
      // Afficher chaque catégorie principale immédiatement suivie de ses sous-catégories
      for (var parentCat : mainCategories) {
        // D'abord la catégorie principale
        allCategories.add("📁 " + parentCat.nom());
        
        // Puis ses sous-catégories avec indentation
        var subCategories = categories.stream()
            .filter(cat -> cat.parentId() != null && cat.parentId().equals(parentCat.id()))
            .sorted((a, b) -> a.nom().compareTo(b.nom()))
            .toList();
        
        System.out.println("DEBUG: Sous-catégories de " + parentCat.nom() + ": " + 
            subCategories.stream().map(c -> c.nom()).toList());
            
        for (var subCat : subCategories) {
          allCategories.add("  └─ 📄 " + subCat.nom());
          
          // Puis les sous-sous-catégories avec double indentation
          var subSubCategories = categories.stream()
              .filter(cat -> cat.parentId() != null && cat.parentId().equals(subCat.id()))
              .sorted((a, b) -> a.nom().compareTo(b.nom()))
              .toList();
          
          System.out.println("DEBUG: Sous-sous-catégories de " + subCat.nom() + ": " + 
              subSubCategories.stream().map(c -> c.nom()).toList());
          
          subSubCategories.stream()
              .map(cat -> "    └─ ⚡ " + cat.nom())
              .forEach(allCategories::add);
        }
      }
    } catch (Exception e) {
      System.out.println("Avertissement: Impossible de charger les catégories unifiées: " + e.getMessage());
      e.printStackTrace();
    }
    
    // Ajouter les catégories de la table 'produits' (système legacy)
    try {
      var legacyCategories = prodRepo.listDistinctCategories();
      System.out.println("DEBUG: Catégories legacy ajoutées: " + legacyCategories);
      legacyCategories.forEach(allCategories::add);
    } catch (Exception e) {
      System.out.println("Avertissement: Impossible de charger les catégories legacy: " + e.getMessage());
    }
    
    System.out.println("DEBUG: Liste finale des catégories: " + new java.util.ArrayList<>(allCategories));
    
    return new java.util.ArrayList<>(allCategories);
  }
  
  // Méthode utilitaire pour convertir un nom de catégorie vers sa représentation avec icônes
  private String formatCategoryForDisplay(String categoryName) {
    if (categoryName == null || categoryName.trim().isEmpty()) return null;
    
    try {
      var categories = categoryRepo.findAll();
      
      // Chercher d'abord si c'est une catégorie principale
      var mainCategory = categories.stream()
          .filter(cat -> cat.parentId() == null && cat.nom().equals(categoryName))
          .findFirst();
      
      if (mainCategory.isPresent()) {
        return "📁 " + categoryName;
      }
      
      // Sinon chercher si c'est une sous-catégorie
      var subCategory = categories.stream()
          .filter(cat -> cat.parentId() != null && cat.nom().equals(categoryName))
          .findFirst();
      
      if (subCategory.isPresent()) {
        return "  └─ 📄 " + categoryName;
      }
      
    } catch (Exception e) {
      System.out.println("Erreur lors du formatage de la catégorie: " + e.getMessage());
    }
    
    // Si pas trouvé dans le système unifié, retourner tel quel (legacy)
    return categoryName;
  }

  // Méthode utilitaire pour extraire le nom réel de la catégorie (sans icônes ni indentation)
  private String extractCategoryName(String displayValue) {
    if (displayValue == null) return null;
    
    // Supprimer les icônes et l'indentation
    String cleaned = displayValue
        .replace("📁 ", "")
        .replace("📄 ", "")
        .replace("  └─ ", "")
        .trim();
    
    return cleaned.isEmpty() ? null : cleaned;
  }

  private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
  private static String nz(String s) { return s == null ? "" : s; }
}