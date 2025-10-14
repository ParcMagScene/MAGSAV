package com.magsav.gui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue mosaïque de sélection de logos
 */
public class LogoMosaicController {

    @FXML private TilePane tilePane;
    @FXML private TextField tfSearch;
    @FXML private Label lblInfo;
    @FXML private Button btnOk, btnCancel;
    
    private String selectedLogoPath;
    private Stage stage;
    private final Path logosDir;
    
    public LogoMosaicController() {
        this.logosDir = Paths.get(com.magsav.config.AppConfig.getMediaDirectory(), "logos");
    }
    
    @FXML
    private void initialize() {
        // Configuration du TilePane
        tilePane.setPrefColumns(4);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        
        // Listener pour la recherche
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldText, newText) -> loadLogos(newText));
        }
        
        // Charger les logos au démarrage
        loadLogos("");
        
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
    
    private void loadLogos(String filter) {
        tilePane.getChildren().clear();
        
        try {
            if (!Files.exists(logosDir)) {
                Files.createDirectories(logosDir);
                updateInfo("Aucun logo trouvé - dossier vide");
                return;
            }
            
            List<Path> imageFiles = Files.list(logosDir)
                .filter(Files::isRegularFile)
                .filter(this::isImageFile)
                .filter(path -> filter.isEmpty() || 
                    path.getFileName().toString().toLowerCase().contains(filter.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
            
            if (imageFiles.isEmpty()) {
                updateInfo(filter.isEmpty() ? "Aucun logo trouvé" : "Aucun logo correspondant au filtre");
                return;
            }
            
            for (Path imagePath : imageFiles) {
                addLogoTile(imagePath);
            }
            
            updateInfo(imageFiles.size() + " logo(s) trouvé(s)");
            
        } catch (IOException e) {
            updateInfo("Erreur lors du chargement des logos: " + e.getMessage());
        }
    }
    
    private void addLogoTile(Path imagePath) {
        try {
            // Créer l'ImageView pour le logo
            ImageView imageView = new ImageView();
            imageView.setFitWidth(120);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            // Charger l'image
            Image image = new Image(imagePath.toUri().toString(), 120, 80, true, true, true);
            imageView.setImage(image);
            
            // Créer un conteneur avec bordure pour la sélection
            StackPane container = new StackPane();
            container.getChildren().add(imageView);
            container.setStyle("-fx-border-color: transparent; -fx-border-width: 2; -fx-padding: 5;");
            
            // Ajouter le nom du fichier en tooltip
            String fileName = imagePath.getFileName().toString();
            Tooltip.install(container, new Tooltip(fileName));
            
            // Gérer la sélection
            container.setOnMouseClicked(this::handleLogoSelection);
            container.setUserData(imagePath.toString());
            
            tilePane.getChildren().add(container);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du logo " + imagePath + ": " + e.getMessage());
        }
    }
    
    private void handleLogoSelection(MouseEvent event) {
        StackPane clickedTile = (StackPane) event.getSource();
        
        // Désélectionner tous les autres tiles
        tilePane.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                ((StackPane) node).setStyle("-fx-border-color: transparent; -fx-border-width: 2; -fx-padding: 5;");
            }
        });
        
        // Sélectionner le tile cliqué
        clickedTile.setStyle("-fx-border-color: #0078d4; -fx-border-width: 2; -fx-padding: 5;");
        String absolutePath = (String) clickedTile.getUserData();
        Path logoPath = Paths.get(absolutePath);
        selectedLogoPath = logosDir.relativize(logoPath).toString();
        
        // Activer le bouton OK
        if (btnOk != null) {
            btnOk.setDisable(false);
        }
        
        updateInfo("Logo sélectionné: " + logoPath.getFileName().toString());
    }
    
    private boolean isImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || fileName.endsWith(".gif") || 
               fileName.endsWith(".svg");
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
        selectedLogoPath = null;
        if (stage != null) {
            stage.close();
        }
    }
    
    public String getSelectedLogoPath() {
        return selectedLogoPath;
    }
}