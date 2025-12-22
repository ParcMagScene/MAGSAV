package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.AvatarView;
import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * Vue de gestion des médias (Photos, Logos, Avatars)
 * Permet de visualiser, ajouter, supprimer et organiser les fichiers médias
 */
public class MediaManagerView extends BorderPane {
    
    private final MediaService mediaService;
    @SuppressWarnings("unused")
    private final ApiService apiService;  // Conservé pour utilisation future
    
    // Composants pour les onglets
    private FlowPane photosGrid;
    private FlowPane logosGrid;
    private FlowPane avatarsGrid;
    
    // Compteurs
    private Label photosCountLabel;
    private Label logosCountLabel;
    private Label avatarsCountLabel;
    
    // Recherche
    private TextField searchField;
    private String currentFilter = "";
    
    // Cache des codes LOCMAT pour l'autocomplétion
    private List<LocmatItem> locmatCache = new ArrayList<>();
    
    /**
     * Représente un équipement avec son code LOCMAT pour l'autocomplétion
     */
    private static class LocmatItem {
        final String locmatCode;
        final String name;
        final String brand;
        
        LocmatItem(String locmatCode, String name, String brand) {
            this.locmatCode = locmatCode != null ? locmatCode.replace("*", "").trim() : "";
            this.name = name != null ? name : "";
            this.brand = brand != null ? brand : "";
        }
        
        @Override
        public String toString() {
            return locmatCode + " - " + name + (brand.isEmpty() ? "" : " (" + brand + ")");
        }
        
        public String getCleanLocmatCode() {
            return locmatCode;
        }
    }
    
    public MediaManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.mediaService = MediaService.getInstance();
        
        getStyleClass().add("media-manager-view");
        
        initializeUI();
        loadAllMedia();
        loadLocmatCache(); // Charger les codes LOCMAT en arrière-plan
    }
    
    private void initializeUI() {
        // Toolbar en haut
        setTop(createToolbar());
        
        // Contenu principal avec onglets
        setCenter(createTabbedContent());
        
        // Barre de statut en bas
        setBottom(createStatusBar());
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("unified-toolbar");
        
        // Titre
        Label titleLabel = new Label("🖼️ Gestion des Médias");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6B71F2;");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Recherche
        searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentFilter = newVal.toLowerCase();
            refreshAllGrids();
        });
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        // Bouton ouvrir le dossier
        Button openFolderBtn = new Button("📁");
        openFolderBtn.setTooltip(new Tooltip("Ouvrir le dossier des médias"));
        openFolderBtn.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; -fx-background-radius: 4;");
        openFolderBtn.setOnAction(e -> openMediaFolder());
        
        // Bouton actualiser
        Button refreshBtn = new Button("🔄");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 4;");
        refreshBtn.setOnAction(e -> loadAllMedia());
        
        toolbar.getChildren().addAll(titleLabel, spacer1, searchField, spacer2, openFolderBtn, refreshBtn);
        
        return toolbar;
    }
    
    private CustomTabPane createTabbedContent() {
        CustomTabPane tabPane = new CustomTabPane();
        
        // Onglet Photos
        CustomTabPane.CustomTab photosTab = new CustomTabPane.CustomTab(
            "Photos", createPhotosPanel(), "📷"
        );
        tabPane.addTab(photosTab);
        
        // Onglet Logos
        CustomTabPane.CustomTab logosTab = new CustomTabPane.CustomTab(
            "Logos", createLogosPanel(), "🏷️"
        );
        tabPane.addTab(logosTab);
        
        // Onglet Avatars
        CustomTabPane.CustomTab avatarsTab = new CustomTabPane.CustomTab(
            "Avatars", createAvatarsPanel(), "👤"
        );
        tabPane.addTab(avatarsTab);
        
        tabPane.selectTab(0);
        
        return tabPane;
    }
    
    private VBox createPhotosPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        
        // Toolbar spécifique
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button addPhotoBtn = new Button("➕ Ajouter des photos");
        addPhotoBtn.getStyleClass().add("action-button-primary");
        addPhotoBtn.setOnAction(e -> addPhotos());
        
        photosCountLabel = new Label("0 photos");
        photosCountLabel.setStyle("-fx-text-fill: #6C757D;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(addPhotoBtn, spacer, photosCountLabel);
        
        // Grille de photos
        photosGrid = new FlowPane();
        photosGrid.setHgap(15);
        photosGrid.setVgap(15);
        photosGrid.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(photosGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        panel.getChildren().addAll(toolbar, scrollPane);
        
        return panel;
    }
    
    private VBox createLogosPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        
        // Toolbar spécifique
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button addLogoBtn = new Button("➕ Ajouter des logos");
        addLogoBtn.getStyleClass().add("action-button-primary");
        addLogoBtn.setOnAction(e -> addLogos());
        
        logosCountLabel = new Label("0 logos");
        logosCountLabel.setStyle("-fx-text-fill: #6C757D;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(addLogoBtn, spacer, logosCountLabel);
        
        // Grille de logos
        logosGrid = new FlowPane();
        logosGrid.setHgap(15);
        logosGrid.setVgap(15);
        logosGrid.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(logosGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        panel.getChildren().addAll(toolbar, scrollPane);
        
        return panel;
    }
    
    private VBox createAvatarsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        
        // Toolbar spécifique
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button addAvatarBtn = new Button("➕ Ajouter un avatar");
        addAvatarBtn.getStyleClass().add("action-button-primary");
        addAvatarBtn.setOnAction(e -> addAvatars());
        
        Button initDefaultBtn = new Button("🎨 Générer avatars par défaut");
        initDefaultBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 4;");
        initDefaultBtn.setTooltip(new Tooltip("Créer 10 avatars par défaut avec différentes couleurs"));
        initDefaultBtn.setOnAction(e -> initializeDefaultAvatars());
        
        avatarsCountLabel = new Label("0 avatars");
        avatarsCountLabel.setStyle("-fx-text-fill: #6C757D;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(addAvatarBtn, initDefaultBtn, spacer, avatarsCountLabel);
        
        // Grille d'avatars
        avatarsGrid = new FlowPane();
        avatarsGrid.setHgap(15);
        avatarsGrid.setVgap(15);
        avatarsGrid.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(avatarsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        panel.getChildren().addAll(toolbar, scrollPane);
        
        return panel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #DEE2E6; -fx-border-width: 1 0 0 0;");
        
        Label pathLabel = new Label("📁 " + mediaService.getPhotosPath().getParent().toString());
        pathLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 11px;");
        
        statusBar.getChildren().add(pathLabel);
        
        return statusBar;
    }
    
    // ========================================
    // CHARGEMENT DES MÉDIAS
    // ========================================
    
    private void loadAllMedia() {
        loadPhotos();
        loadLogos();
        loadAvatars();
    }
    
    private void refreshAllGrids() {
        loadPhotos();
        loadLogos();
        loadAvatars();
    }
    
    private void loadPhotos() {
        Platform.runLater(() -> {
            photosGrid.getChildren().clear();
            
            List<File> photos = mediaService.listPhotos();
            int count = 0;
            
            for (File photo : photos) {
                if (matchesFilter(photo.getName())) {
                    photosGrid.getChildren().add(createPhotoThumbnail(photo));
                    count++;
                }
            }
            
            photosCountLabel.setText(count + " photo" + (count > 1 ? "s" : ""));
        });
    }
    
    private void loadLogos() {
        Platform.runLater(() -> {
            logosGrid.getChildren().clear();
            
            List<File> logos = mediaService.listLogos();
            int count = 0;
            
            for (File logo : logos) {
                if (matchesFilter(logo.getName())) {
                    logosGrid.getChildren().add(createLogoThumbnail(logo));
                    count++;
                }
            }
            
            logosCountLabel.setText(count + " logo" + (count > 1 ? "s" : ""));
        });
    }
    
    private void loadAvatars() {
        Platform.runLater(() -> {
            avatarsGrid.getChildren().clear();
            
            List<File> avatars = mediaService.listAvatars();
            int count = 0;
            
            for (File avatar : avatars) {
                if (matchesFilter(avatar.getName())) {
                    avatarsGrid.getChildren().add(createAvatarThumbnail(avatar));
                    count++;
                }
            }
            
            avatarsCountLabel.setText(count + " avatar" + (count > 1 ? "s" : ""));
        });
    }
    
    private boolean matchesFilter(String name) {
        if (currentFilter == null || currentFilter.isEmpty()) {
            return true;
        }
        return name.toLowerCase().contains(currentFilter);
    }
    
    // ========================================
    // CRÉATION DES VIGNETTES
    // ========================================
    
    private VBox createPhotoThumbnail(File photoFile) {
        return createMediaThumbnail(photoFile, "photo", 120);
    }
    
    private VBox createLogoThumbnail(File logoFile) {
        return createMediaThumbnail(logoFile, "logo", 100);
    }
    
    private VBox createAvatarThumbnail(File avatarFile) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px; -fx-cursor: hand;");
        
        // Utiliser AvatarView pour afficher
        AvatarView avatarView = new AvatarView(60);
        avatarView.setAvatarPath(avatarFile.getName());
        avatarView.setEditable(false);
        
        // Nom du fichier
        String fileName = avatarFile.getName();
        if (fileName.length() > 15) {
            fileName = fileName.substring(0, 12) + "...";
        }
        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #495057;");
        nameLabel.setMaxWidth(80);
        
        container.getChildren().addAll(avatarView, nameLabel);
        
        // Menu contextuel
        ContextMenu contextMenu = createMediaContextMenu(avatarFile, "avatar");
        container.setOnContextMenuRequested(e -> contextMenu.show(container, e.getScreenX(), e.getScreenY()));
        
        // Effet hover
        container.setOnMouseEntered(e -> container.setStyle("-fx-background-color: #E9ECEF; -fx-background-radius: 8px; -fx-cursor: hand;"));
        container.setOnMouseExited(e -> container.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px; -fx-cursor: hand;"));
        
        return container;
    }
    
    private VBox createMediaThumbnail(File file, String type, double size) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px; -fx-cursor: hand;");
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        try {
            Image image = new Image(file.toURI().toString(), size, size, true, true, true);
            imageView.setImage(image);
        } catch (Exception e) {
            // Image par défaut en cas d'erreur
            imageView.setStyle("-fx-background-color: #DEE2E6;");
        }
        
        // Nom du fichier
        String fileName = file.getName();
        if (fileName.length() > 18) {
            fileName = fileName.substring(0, 15) + "...";
        }
        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #495057;");
        nameLabel.setMaxWidth(size);
        
        container.getChildren().addAll(imageView, nameLabel);
        
        // Menu contextuel
        ContextMenu contextMenu = createMediaContextMenu(file, type);
        container.setOnContextMenuRequested(e -> contextMenu.show(container, e.getScreenX(), e.getScreenY()));
        
        // Double-clic pour ouvrir
        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openFile(file);
            }
        });
        
        // Effet hover
        container.setOnMouseEntered(e -> container.setStyle("-fx-background-color: #E9ECEF; -fx-background-radius: 8px; -fx-cursor: hand;"));
        container.setOnMouseExited(e -> container.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8px; -fx-cursor: hand;"));
        
        // Tooltip avec le nom complet
        Tooltip.install(container, new Tooltip(file.getName() + "\n" + formatFileSize(file.length())));
        
        return container;
    }
    
    private ContextMenu createMediaContextMenu(File file, String type) {
        ContextMenu menu = new ContextMenu();
        
        MenuItem openItem = new MenuItem("📂 Ouvrir");
        openItem.setOnAction(e -> openFile(file));
        
        MenuItem renameItem = new MenuItem("✏️ Renommer");
        renameItem.setOnAction(e -> renameMedia(file, type));
        
        MenuItem copyNameItem = new MenuItem("📋 Copier le nom");
        copyNameItem.setOnAction(e -> copyToClipboard(file.getName()));
        
        // Options de rotation
        MenuItem rotateRightItem = new MenuItem("↩️ Pivoter 90° droite");
        rotateRightItem.setOnAction(e -> rotateImage(file, type, 90));
        
        MenuItem rotateLeftItem = new MenuItem("↪️ Pivoter 90° gauche");
        rotateLeftItem.setOnAction(e -> rotateImage(file, type, -90));
        
        MenuItem rotate180Item = new MenuItem("🔃 Pivoter 180°");
        rotate180Item.setOnAction(e -> rotateImage(file, type, 180));
        
        // Option d'assignation uniquement pour les photos
        MenuItem assignItem = new MenuItem("🔗 Assigner à un LOCMAT...");
        assignItem.setOnAction(e -> showAssignToLocmatDialog(file));
        
        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        deleteItem.setOnAction(e -> deleteMedia(file, type));
        
        menu.getItems().addAll(openItem, renameItem, copyNameItem);
        
        // Ajouter les options de rotation
        Menu rotateMenu = new Menu("🔄 Pivoter");
        rotateMenu.getItems().addAll(rotateRightItem, rotateLeftItem, rotate180Item);
        menu.getItems().add(rotateMenu);
        
        // Ajouter l'option d'assignation seulement pour les photos
        if ("photo".equals(type)) {
            menu.getItems().add(assignItem);
        }
        
        menu.getItems().addAll(new SeparatorMenuItem(), deleteItem);
        
        return menu;
    }
    
    // ========================================
    // ACTIONS
    // ========================================
    
    private void addPhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ajouter des photos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp", "*.avif"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            copyFilesToFolder(files, mediaService.getPhotosPath());
            loadPhotos();
        }
    }
    
    private void addLogos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ajouter des logos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.svg", "*.webp"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            copyFilesToFolder(files, mediaService.getLogosPath());
            loadLogos();
        }
    }
    
    private void addAvatars() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ajouter des avatars");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            copyFilesToFolder(files, mediaService.getAvatarsPath());
            loadAvatars();
        }
    }
    
    private void initializeDefaultAvatars() {
        mediaService.initializeDefaultAvatars();
        
        // Attendre un peu que les avatars soient générés
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            Platform.runLater(this::loadAvatars);
        }).start();
        
        showInfo("Avatars par défaut", "10 avatars par défaut ont été générés dans le dossier Avatars.");
    }
    
    private void renameMedia(File file, String type) {
        // Obtenir le nom actuel sans extension
        String currentName = file.getName();
        int lastDot = currentName.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? currentName.substring(0, lastDot) : currentName;
        
        TextInputDialog dialog = new TextInputDialog(nameWithoutExt);
        dialog.setTitle("Renommer le fichier");
        dialog.setHeaderText("Entrez le nouveau nom du fichier");
        dialog.setContentText("Nouveau nom:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(nameWithoutExt)) {
                File renamedFile = mediaService.renameMediaFile(file, newName.trim());
                if (renamedFile != null) {
                    // Rafraîchir l'affichage
                    switch (type) {
                        case "photo" -> loadPhotos();
                        case "logo" -> loadLogos();
                        case "avatar" -> loadAvatars();
                    }
                } else {
                    showError("Impossible de renommer le fichier. Vérifiez qu'un fichier avec ce nom n'existe pas déjà.");
                }
            }
        });
    }
    
    /**
     * Pivote une image du nombre de degrés spécifié et sauvegarde le résultat
     * @param file Le fichier image à pivoter
     * @param type Le type de média (photo, logo, avatar)
     * @param degrees L'angle de rotation en degrés (90, -90, 180)
     */
    private void rotateImage(File file, String type, int degrees) {
        try {
            // Lire l'image originale
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                showError("Impossible de lire l'image: format non supporté");
                return;
            }
            
            // Calculer les dimensions de l'image pivotée
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int newWidth, newHeight;
            
            if (degrees == 90 || degrees == -90 || degrees == 270 || degrees == -270) {
                // Rotation 90° : inverser largeur et hauteur
                newWidth = height;
                newHeight = width;
            } else {
                // Rotation 180° : dimensions inchangées
                newWidth = width;
                newHeight = height;
            }
            
            // Créer une nouvelle image avec les dimensions appropriées
            BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, 
                originalImage.getType() != 0 ? originalImage.getType() : BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2d = rotatedImage.createGraphics();
            
            // Appliquer la transformation
            AffineTransform transform = new AffineTransform();
            transform.translate(newWidth / 2.0, newHeight / 2.0);
            transform.rotate(Math.toRadians(degrees));
            transform.translate(-width / 2.0, -height / 2.0);
            
            g2d.setTransform(transform);
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
            
            // Déterminer le format de sortie à partir de l'extension
            String fileName = file.getName().toLowerCase();
            String format = "png"; // Par défaut
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                format = "jpg";
            } else if (fileName.endsWith(".gif")) {
                format = "gif";
            } else if (fileName.endsWith(".bmp")) {
                format = "bmp";
            } else if (fileName.endsWith(".webp")) {
                format = "webp";
            }
            
            // Pour les formats avec transparence (PNG, GIF), garder le format
            // Pour JPEG, convertir si l'image a un canal alpha
            if ("jpg".equals(format) && rotatedImage.getColorModel().hasAlpha()) {
                BufferedImage jpegImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D jpegG2d = jpegImage.createGraphics();
                jpegG2d.drawImage(rotatedImage, 0, 0, java.awt.Color.WHITE, null);
                jpegG2d.dispose();
                rotatedImage = jpegImage;
            }
            
            // Sauvegarder l'image pivotée
            ImageIO.write(rotatedImage, format, file);
            
            // Vider le cache d'images du MediaService pour forcer le rechargement
            mediaService.clearCache();
            
            System.out.println("🔄 Image pivotée de " + degrees + "°: " + file.getName());
            
            // Rafraîchir l'affichage
            Platform.runLater(() -> {
                switch (type) {
                    case "photo" -> loadPhotos();
                    case "logo" -> loadLogos();
                    case "avatar" -> loadAvatars();
                }
            });
            
        } catch (IOException e) {
            showError("Erreur lors de la rotation de l'image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAssignToLocmatDialog(File photoFile) {
        // Recharger le cache si vide
        if (locmatCache.isEmpty()) {
            System.out.println("⚠️ Cache LOCMAT vide, rechargement...");
            loadLocmatCache();
        }
        System.out.println("📋 Ouverture dialogue assignation avec " + locmatCache.size() + " codes LOCMAT");
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Assigner à un équipement");
        dialog.setHeaderText("Assigner " + photoFile.getName() + " à un code LOCMAT");
        
        // Boutons
        ButtonType assignButtonType = new ButtonType("Assigner", ButtonBar.ButtonData.OK_DONE);
        ButtonType renameAndAssignButtonType = new ButtonType("Renommer et Assigner", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, renameAndAssignButtonType, ButtonType.CANCEL);
        
        // Contenu
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(450);
        
        Label infoLabel = new Label("Recherchez le code LOCMAT de l'équipement :");
        
        // ComboBox avec autocomplétion
        ComboBox<LocmatItem> locmatComboBox = new ComboBox<>();
        locmatComboBox.setEditable(true);
        locmatComboBox.setPromptText("Tapez pour rechercher (ex: ULXD2, DXR12...)");
        locmatComboBox.setPrefWidth(400);
        locmatComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Remplir avec les données du cache
        System.out.println("📋 Remplissage ComboBox avec " + locmatCache.size() + " codes");
        locmatComboBox.getItems().addAll(locmatCache);
        
        // Convertisseur pour afficher et récupérer les valeurs
        locmatComboBox.setConverter(new StringConverter<LocmatItem>() {
            @Override
            public String toString(LocmatItem item) {
                return item != null ? item.toString() : "";
            }
            
            @Override
            public LocmatItem fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                // Rechercher dans le cache
                return locmatCache.stream()
                    .filter(item -> item.toString().equals(string) || 
                                   item.locmatCode.equalsIgnoreCase(string.trim()))
                    .findFirst()
                    .orElse(new LocmatItem(string.trim(), "", "")); // Permettre un code manuel
            }
        });
        
        // Filtrage progressif lors de la saisie
        TextField editor = locmatComboBox.getEditor();
        
        // Variable pour stocker la sélection actuelle
        final LocmatItem[] selectedItem = {null};
        
        // Listener pour capturer la sélection AVANT le changement de texte
        locmatComboBox.setOnAction(event -> {
            LocmatItem item = locmatComboBox.getSelectionModel().getSelectedItem();
            if (item != null) {
                selectedItem[0] = item;
                System.out.println("✅ Sélection capturée: " + item.locmatCode);
            }
        });
        
        // Listener pour gérer la sélection via les touches
        locmatComboBox.setOnHidden(event -> {
            LocmatItem item = locmatComboBox.getSelectionModel().getSelectedItem();
            if (item != null) {
                selectedItem[0] = item;
                editor.setText(item.toString());
                System.out.println("✅ Dropdown fermé, sélection: " + item.locmatCode);
            }
        });
        
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            // Ne pas filtrer si une sélection vient d'être faite
            if (selectedItem[0] != null && newVal != null && newVal.equals(selectedItem[0].toString())) {
                return;
            }
            
            if (newVal == null || newVal.isEmpty()) {
                locmatComboBox.getItems().setAll(locmatCache);
                return;
            }
            
            String searchText = newVal.toLowerCase().replace("*", "");
            List<LocmatItem> filtered = locmatCache.stream()
                .filter(item -> {
                    String code = item.locmatCode.toLowerCase();
                    String name = item.name.toLowerCase();
                    String brand = item.brand.toLowerCase();
                    return code.contains(searchText) || 
                           name.contains(searchText) || 
                           brand.contains(searchText);
                })
                .sorted((a, b) -> {
                    // Priorité aux codes commençant par la recherche
                    boolean aStarts = a.locmatCode.toLowerCase().startsWith(searchText);
                    boolean bStarts = b.locmatCode.toLowerCase().startsWith(searchText);
                    if (aStarts && !bStarts) return -1;
                    if (!aStarts && bStarts) return 1;
                    return a.locmatCode.compareToIgnoreCase(b.locmatCode);
                })
                .limit(20) // Limiter pour la performance
                .collect(Collectors.toList());
            
            Platform.runLater(() -> {
                locmatComboBox.getItems().setAll(filtered);
                if (!filtered.isEmpty() && !locmatComboBox.isShowing()) {
                    locmatComboBox.show();
                }
            });
        });
        
        // Label pour afficher le nombre de résultats
        Label resultCountLabel = new Label(locmatCache.size() + " équipements disponibles");
        resultCountLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 11px;");
        
        CheckBox renameCheckBox = new CheckBox("Renommer le fichier avec le code LOCMAT");
        renameCheckBox.setSelected(false);
        
        content.getChildren().addAll(infoLabel, locmatComboBox, resultCountLabel, renameCheckBox);
        dialog.getDialogPane().setContent(content);
        
        // Focus sur le champ de saisie
        Platform.runLater(() -> {
            editor.requestFocus();
            editor.selectAll();
        });
        
        // Résultat
        dialog.setResultConverter(buttonType -> {
            if (buttonType == assignButtonType || buttonType == renameAndAssignButtonType) {
                // Priorité à la sélection capturée
                if (selectedItem[0] != null) {
                    System.out.println("✅ Utilisation sélection capturée: " + selectedItem[0].locmatCode);
                    return selectedItem[0].getCleanLocmatCode();
                }
                // Sinon essayer la valeur de la ComboBox
                LocmatItem selected = locmatComboBox.getValue();
                if (selected != null) {
                    return selected.getCleanLocmatCode();
                }
                // Sinon essayer de trouver dans le cache par le texte saisi
                String text = editor.getText();
                if (text != null && !text.isEmpty()) {
                    String searchText = text.toLowerCase().replace("*", "").trim();
                    // Chercher une correspondance exacte dans le cache
                    Optional<LocmatItem> match = locmatCache.stream()
                        .filter(item -> item.locmatCode.equalsIgnoreCase(searchText) ||
                                       item.toString().equalsIgnoreCase(text))
                        .findFirst();
                    if (match.isPresent()) {
                        return match.get().getCleanLocmatCode();
                    }
                    // Sinon utiliser le texte tel quel
                    return searchText;
                }
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(locmatCode -> {
            if (!locmatCode.trim().isEmpty()) {
                String cleanCode = locmatCode.replace("*", "").trim();
                if (renameCheckBox.isSelected()) {
                    // Renommer et assigner
                    File renamedFile = mediaService.renamePhotoForLocmat(photoFile, cleanCode);
                    if (renamedFile != null) {
                        showInfo("Assignation réussie", 
                            "Photo renommée en '" + renamedFile.getName() + "' et assignée au code LOCMAT: " + cleanCode);
                        loadPhotos();
                    } else {
                        showError("Impossible de renommer le fichier.");
                    }
                } else {
                    // Juste assigner
                    mediaService.assignPhotoToLocmat(cleanCode, photoFile.getName());
                    showInfo("Assignation réussie", 
                        "Photo '" + photoFile.getName() + "' assignée au code LOCMAT: " + cleanCode);
                }
            }
        });
    }
    
    /**
     * Charge les codes LOCMAT depuis l'API en arrière-plan
     * Utilise un appel HTTP direct pour s'assurer d'avoir les vraies données du backend
     */
    private void loadLocmatCache() {
        System.out.println("🔄 Chargement du cache LOCMAT depuis le backend...");
        
        CompletableFuture.runAsync(() -> {
            try {
                java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/equipment"))
                    .GET()
                    .build();
                
                java.net.http.HttpResponse<String> response = httpClient.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
                
                if (response.statusCode() == 200) {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, Object>> equipments = objectMapper.readValue(response.body(), 
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                    
                    Set<String> seenCodes = new HashSet<>();
                    List<LocmatItem> items = new ArrayList<>();
                    
                    System.out.println("📊 Équipements reçus du backend: " + equipments.size());
                    
                    int idx = 0;
                    for (Map<String, Object> eq : equipments) {
                        String locmat = (String) eq.get("internalReference");
                        String name = (String) eq.get("name");
                        String brand = (String) eq.get("brand");
                        
                        // Debug: afficher les 5 premiers équipements
                        if (idx < 5) {
                            System.out.println("   🔍 Équipement #" + idx + ": locmat='" + locmat + "', name='" + name + "'");
                        }
                        idx++;
                        
                        // Nettoyer le code LOCMAT (supprimer les *)
                        if (locmat != null && !locmat.isEmpty()) {
                            String cleanCode = locmat.replace("*", "").trim();
                            
                            // Éviter les doublons
                            if (!cleanCode.isEmpty() && !seenCodes.contains(cleanCode.toUpperCase())) {
                                seenCodes.add(cleanCode.toUpperCase());
                                items.add(new LocmatItem(cleanCode, name, brand));
                            }
                        }
                    }
                    
                    // Trier par code LOCMAT
                    items.sort((a, b) -> a.locmatCode.compareToIgnoreCase(b.locmatCode));
                    
                    final int totalProcessed = idx;
                    final List<LocmatItem> finalItems = items;
                    Platform.runLater(() -> {
                        locmatCache = finalItems;
                        System.out.println("📋 Cache LOCMAT chargé: " + finalItems.size() + " codes uniques sur " + totalProcessed + " équipements");
                        if (finalItems.size() > 0) {
                            System.out.println("   Premiers codes: " + finalItems.subList(0, Math.min(5, finalItems.size())));
                        }
                    });
                } else {
                    System.err.println("⚠️ Erreur HTTP chargement cache LOCMAT: " + response.statusCode());
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Erreur chargement cache LOCMAT: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
    
    private void copyFilesToFolder(List<File> files, Path targetFolder) {
        int copied = 0;
        for (File file : files) {
            try {
                Path target = targetFolder.resolve(file.getName());
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (IOException e) {
                System.err.println("Erreur copie " + file.getName() + ": " + e.getMessage());
            }
        }
        System.out.println("✅ " + copied + " fichier(s) copié(s)");
    }
    
    private void deleteMedia(File file, String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer ce fichier ?");
        alert.setContentText(file.getName());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Files.delete(file.toPath());
                System.out.println("🗑️ Fichier supprimé: " + file.getName());
                
                // Rafraîchir l'affichage
                switch (type) {
                    case "photo" -> loadPhotos();
                    case "logo" -> loadLogos();
                    case "avatar" -> loadAvatars();
                }
            } catch (IOException e) {
                showError("Impossible de supprimer le fichier: " + e.getMessage());
            }
        }
    }
    
    private void openMediaFolder() {
        try {
            Desktop.getDesktop().open(mediaService.getPhotosPath().getParent().toFile());
        } catch (IOException e) {
            showError("Impossible d'ouvrir le dossier: " + e.getMessage());
        }
    }
    
    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            showError("Impossible d'ouvrir le fichier: " + e.getMessage());
        }
    }
    
    private void copyToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
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
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
