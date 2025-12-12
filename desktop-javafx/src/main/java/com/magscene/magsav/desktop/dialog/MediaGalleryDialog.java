package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Dialog mosaïque pour sélectionner des photos ou logos
 * depuis les dossiers Medias MAGSAV/Photos et Medias MAGSAV/Logos
 */
public class MediaGalleryDialog extends Dialog<MediaGalleryDialog.MediaSelection> {
    
    public enum MediaType {
        PHOTO("Photos d'équipements", "Photos"),
        LOGO("Logos de marques", "Logos");
        
        private final String title;
        private final String folder;
        
        MediaType(String title, String folder) {
            this.title = title;
            this.folder = folder;
        }
        
        public String getTitle() { return title; }
        public String getFolder() { return folder; }
    }
    
    /**
     * Résultat de la sélection dans la galerie
     */
    public static class MediaSelection {
        private final File selectedFile;
        private final boolean applyToAll;
        private final String matchingField; // "name" ou "locmatCode"
        
        public MediaSelection(File selectedFile, boolean applyToAll, String matchingField) {
            this.selectedFile = selectedFile;
            this.applyToAll = applyToAll;
            this.matchingField = matchingField;
        }
        
        public File getSelectedFile() { return selectedFile; }
        public boolean isApplyToAll() { return applyToAll; }
        public String getMatchingField() { return matchingField; }
    }
    
    private static final int THUMBNAIL_SIZE = 100;
    private static final int COLUMNS = 5;
    
    private final MediaService mediaService;
    private final MediaType mediaType;
    private final String equipmentName;
    private final String locmatCode;
    
    private FlowPane galleryPane;
    private TextField searchField;
    private Label statusLabel;
    private File selectedFile;
    private ImageView selectedThumbnail;
    private CheckBox applyToAllCheckbox;
    private RadioButton matchByNameRadio;
    private RadioButton matchByCodeRadio;
    private VBox applyToAllOptions;
    
    public MediaGalleryDialog(MediaService mediaService, MediaType mediaType, 
                              String equipmentName, String locmatCode) {
        this.mediaService = mediaService;
        this.mediaType = mediaType;
        this.equipmentName = equipmentName;
        this.locmatCode = locmatCode;
        
        initDialog();
        createContent();
        loadGallery();
    }
    
    private void initDialog() {
        setTitle("Sélectionner - " + mediaType.getTitle());
        setHeaderText("Choisissez une image dans la galerie");
        initStyle(StageStyle.DECORATED);
        
        // Boutons
        ButtonType selectButton = new ButtonType("Sélectionner", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(selectButton, cancelButton);
        
        // Désactiver le bouton Sélectionner tant qu'aucune image n'est sélectionnée
        getDialogPane().lookupButton(selectButton).setDisable(true);
        
        // Taille du dialogue
        getDialogPane().setPrefSize(700, 600);
        getDialogPane().setMinWidth(600);
        getDialogPane().setMinHeight(500);
        
        // Thème
        String currentTheme = UnifiedThemeManager.getInstance().getCurrentTheme();
        if ("dark".equals(currentTheme)) {
            getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/theme-dark-ultra.css").toExternalForm()
            );
        }
        
        // Result converter
        setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE && selectedFile != null) {
                String matchField = null;
                if (applyToAllCheckbox.isSelected()) {
                    matchField = matchByNameRadio.isSelected() ? "name" : "locmatCode";
                }
                return new MediaSelection(selectedFile, applyToAllCheckbox.isSelected(), matchField);
            }
            return null;
        });
    }
    
    private void createContent() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(15));
        
        // Barre de recherche
        HBox searchBar = createSearchBar();
        
        // Galerie avec scroll
        galleryPane = new FlowPane(10, 10);
        galleryPane.setPadding(new Insets(10));
        galleryPane.setAlignment(Pos.TOP_LEFT);
        
        ScrollPane scrollPane = new ScrollPane(galleryPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(350);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: #f8f9fa;");
        
        // Options "Appliquer à tous"
        VBox applyOptions = createApplyToAllOptions();
        
        // Statut
        statusLabel = new Label("Chargement...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.GRAY);
        
        mainContainer.getChildren().addAll(searchBar, scrollPane, applyOptions, statusLabel);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getDialogPane().setContent(mainContainer);
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("🔍 Rechercher:");
        searchField = new TextField();
        searchField.setPromptText("Filtrer par nom...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterGallery(newVal));
        
        Button refreshButton = new Button("↻ Actualiser");
        refreshButton.setOnAction(e -> loadGallery());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchBar.getChildren().addAll(searchLabel, searchField, spacer, refreshButton);
        
        return searchBar;
    }
    
    private VBox createApplyToAllOptions() {
        applyToAllOptions = new VBox(8);
        applyToAllOptions.setPadding(new Insets(10));
        applyToAllOptions.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-color: #f0f0f0; -fx-background-radius: 5px;");
        
        // Checkbox principal
        applyToAllCheckbox = new CheckBox("Appliquer cette photo à tous les équipements similaires");
        applyToAllCheckbox.setFont(Font.font("System", FontWeight.BOLD, 12));
        applyToAllCheckbox.setOnAction(e -> updateApplyOptions());
        
        // Radio buttons pour le critère de correspondance
        ToggleGroup matchGroup = new ToggleGroup();
        
        matchByNameRadio = new RadioButton("Équipements avec le même nom: " + 
            (equipmentName != null ? "\"" + equipmentName + "\"" : "N/A"));
        matchByNameRadio.setToggleGroup(matchGroup);
        matchByNameRadio.setSelected(true);
        matchByNameRadio.setDisable(true);
        
        matchByCodeRadio = new RadioButton("Équipements avec le même code LocMat: " + 
            (locmatCode != null ? "\"" + locmatCode + "\"" : "N/A"));
        matchByCodeRadio.setToggleGroup(matchGroup);
        matchByCodeRadio.setDisable(true);
        
        // Désactiver si pas de code LocMat
        if (locmatCode == null || locmatCode.isEmpty()) {
            matchByCodeRadio.setDisable(true);
            matchByCodeRadio.setText("Équipements avec le même code LocMat: (non disponible)");
        }
        
        VBox radioBox = new VBox(5);
        radioBox.setPadding(new Insets(0, 0, 0, 20));
        radioBox.getChildren().addAll(matchByNameRadio, matchByCodeRadio);
        
        applyToAllOptions.getChildren().addAll(applyToAllCheckbox, radioBox);
        
        // Cacher si c'est pour les logos (pas d'option appliquer à tous)
        if (mediaType == MediaType.LOGO) {
            applyToAllOptions.setVisible(false);
            applyToAllOptions.setManaged(false);
        }
        
        return applyToAllOptions;
    }
    
    private void updateApplyOptions() {
        boolean enabled = applyToAllCheckbox.isSelected();
        matchByNameRadio.setDisable(!enabled);
        matchByCodeRadio.setDisable(!enabled || locmatCode == null || locmatCode.isEmpty());
    }
    
    private void loadGallery() {
        galleryPane.getChildren().clear();
        statusLabel.setText("Chargement des images...");
        
        CompletableFuture.runAsync(() -> {
            List<File> files = mediaType == MediaType.PHOTO 
                ? mediaService.listPhotos() 
                : mediaService.listLogos();
            
            Platform.runLater(() -> {
                for (File file : files) {
                    VBox thumbnail = createThumbnail(file);
                    galleryPane.getChildren().add(thumbnail);
                }
                statusLabel.setText(files.size() + " image(s) disponible(s)");
            });
        });
    }
    
    private VBox createThumbnail(File file) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5));
        container.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        container.setPrefSize(THUMBNAIL_SIZE + 20, THUMBNAIL_SIZE + 40);
        container.setMaxSize(THUMBNAIL_SIZE + 20, THUMBNAIL_SIZE + 40);
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(true);
        
        // Charger l'image en arrière-plan
        CompletableFuture.runAsync(() -> {
            try {
                Image image = new Image(file.toURI().toString(), THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true, false);
                Platform.runLater(() -> imageView.setImage(image));
            } catch (Exception e) {
                System.err.println("Erreur chargement miniature: " + e.getMessage());
            }
        });
        
        // Nom du fichier (tronqué)
        String fileName = file.getName();
        if (fileName.length() > 15) {
            fileName = fileName.substring(0, 12) + "...";
        }
        Label nameLabel = new Label(fileName);
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        nameLabel.setMaxWidth(THUMBNAIL_SIZE);
        nameLabel.setWrapText(false);
        
        container.getChildren().addAll(imageView, nameLabel);
        
        // Événement de clic
        container.setOnMouseClicked(e -> {
            selectThumbnail(container, imageView, file);
            if (e.getClickCount() == 2) {
                // Double-clic = validation
                ((Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0))).fire();
            }
        });
        
        // Effet hover
        container.setOnMouseEntered(e -> {
            if (selectedFile != file) {
                container.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            }
        });
        
        container.setOnMouseExited(e -> {
            if (selectedFile != file) {
                container.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            }
        });
        
        // Tooltip avec le nom complet
        Tooltip.install(container, new Tooltip(file.getName()));
        
        return container;
    }
    
    private void selectThumbnail(VBox container, ImageView imageView, File file) {
        // Désélectionner l'ancienne
        if (selectedThumbnail != null && selectedThumbnail.getParent() instanceof VBox) {
            VBox oldContainer = (VBox) selectedThumbnail.getParent();
            oldContainer.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            oldContainer.setEffect(null);
        }
        
        // Sélectionner la nouvelle
        selectedFile = file;
        selectedThumbnail = imageView;
        container.setStyle("-fx-background-color: #bbdefb; -fx-border-color: #1976d2; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0.1, 0.47, 0.82, 0.5));
        shadow.setRadius(10);
        container.setEffect(shadow);
        
        // Activer le bouton de sélection
        getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0)).setDisable(false);
        
        statusLabel.setText("Sélectionné: " + file.getName());
    }
    
    private void filterGallery(String filter) {
        String lowerFilter = filter.toLowerCase().trim();
        
        for (javafx.scene.Node node : galleryPane.getChildren()) {
            if (node instanceof VBox) {
                VBox container = (VBox) node;
                // Récupérer le label avec le nom
                Optional<javafx.scene.Node> labelNode = container.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .findFirst();
                
                if (labelNode.isPresent()) {
                    Label label = (Label) labelNode.get();
                    
                    // Utiliser le texte du label pour le filtrage
                    boolean visible = lowerFilter.isEmpty() || 
                        label.getText().toLowerCase().contains(lowerFilter);
                    
                    container.setVisible(visible);
                    container.setManaged(visible);
                }
            }
        }
    }
}
