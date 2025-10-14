package com.magsav.gui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.magsav.service.ImageNormalizationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue mosaïque de sélection de photos
 */
public class PhotoMosaicController {

    @FXML private TilePane tilePane;
    @FXML private TextField tfSearch;
    @FXML private Label lblInfo;
    @FXML private Button btnOk, btnCancel;
    
    private String selectedPhotoPath;
    private Stage stage;
    private final Path photosDir;
    private final ImageNormalizationService imageService = new ImageNormalizationService();
    
    public PhotoMosaicController() {
        this.photosDir = Paths.get(com.magsav.config.AppConfig.getMediaDirectory(), "photos");
    }
    
    @FXML
    private void initialize() {
        // Configuration du TilePane
        tilePane.setPrefColumns(4);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        
        // Listener pour la recherche
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldText, newText) -> loadPhotos(newText));
        }
        
        // Charger les photos au démarrage
        loadPhotos("");
        
        // Boutons
        if (btnOk != null) {
            btnOk.setDisable(true); // Désactivé jusqu'à sélection
            btnOk.setOnAction(e -> confirmSelection());
        }
        
        if (btnCancel != null) {
            btnCancel.setOnAction(e -> cancelSelection());
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    private void loadPhotos(String filter) {
        tilePane.getChildren().clear();
        
        try {
            if (!Files.exists(photosDir)) {
                Files.createDirectories(photosDir);
                updateInfo("Aucune photo trouvée - dossier vide");
                return;
            }
            
            List<Path> imageFiles = Files.list(photosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(path -> filter.isEmpty() || 
                    path.getFileName().toString().toLowerCase().contains(filter.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
            
            if (imageFiles.isEmpty()) {
                updateInfo(filter.isEmpty() ? "Aucune photo trouvée" : "Aucune photo correspondant au filtre");
                return;
            }
            
            for (Path imagePath : imageFiles) {
                addPhotoTile(imagePath);
            }
            
            updateInfo(imageFiles.size() + " photo(s) trouvée(s)");
            
        } catch (IOException e) {
            updateInfo("Erreur lors du chargement des photos: " + e.getMessage());
        }
    }
    
    private boolean isImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif") ||
               fileName.endsWith(".bmp") || fileName.endsWith(".tiff");
    }
    
    private void addPhotoTile(Path imagePath) {
        try {
            // Essayer de charger la vignette normalisée d'abord
            String fileName = imagePath.getFileName().toString();
            Image image = imageService.loadImageForDisplay(fileName, ImageNormalizationService.ImageSize.THUMBNAIL);
            
            if (image == null) {
                // Fallback vers l'image originale si la vignette n'existe pas
                image = new Image(imagePath.toUri().toString(), 150, 150, true, true, true);
            }
            
            // Créer l'ImageView avec préservation du ratio
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(140);  // Légèrement plus petit pour laisser de l'espace
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);  // Garde les proportions
            imageView.setSmooth(true);
            
            // Centrer l'image dans un conteneur de taille fixe
            StackPane imageContainer = new StackPane();
            imageContainer.setPrefSize(150, 150);
            imageContainer.setMaxSize(150, 150);
            imageContainer.setMinSize(150, 150);
            imageContainer.getChildren().add(imageView);
            imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");
            
            // Créer le conteneur avec label
            ToggleButton tileButton = new ToggleButton();
            tileButton.setGraphic(imageContainer);
            tileButton.setText(imagePath.getFileName().toString());
            tileButton.setContentDisplay(ContentDisplay.TOP);
            tileButton.getStyleClass().add("photo-tile");
            tileButton.setPrefSize(170, 200);  // Ajusté pour le nouveau conteneur
            
            // Gestion de la sélection
            tileButton.setOnAction(e -> selectPhoto(imagePath, tileButton));
            
            tilePane.getChildren().add(tileButton);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image " + imagePath + ": " + e.getMessage());
        }
    }
    
    private void selectPhoto(Path imagePath, ToggleButton selectedButton) {
        // Désélectionner les autres boutons
        tilePane.getChildren().stream()
            .filter(node -> node instanceof ToggleButton)
            .map(node -> (ToggleButton) node)
            .filter(btn -> btn != selectedButton)
            .forEach(btn -> btn.setSelected(false));
        
        if (selectedButton.isSelected()) {
            selectedPhotoPath = photosDir.relativize(imagePath).toString();
            btnOk.setDisable(false);
            updateInfo("Photo sélectionnée: " + imagePath.getFileName());
        } else {
            selectedPhotoPath = null;
            btnOk.setDisable(true);
            updateInfo("Aucune photo sélectionnée");
        }
    }
    
    private void updateInfo(String message) {
        if (lblInfo != null) {
            lblInfo.setText(message);
        }
    }
    
    private void confirmSelection() {
        if (stage != null) {
            stage.close();
        }
    }
    
    private void cancelSelection() {
        selectedPhotoPath = null;
        if (stage != null) {
            stage.close();
        }
    }
    
    public String getSelectedPhotoPath() {
        return selectedPhotoPath;
    }
}