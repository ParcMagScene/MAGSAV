package com.magsav.gui.components;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Factory pour créer des panneaux de détail et de visualisation unifiés
 * Centralise la création des composants d'affichage pour tous les types d'entités
 */
public class DetailPaneFactory {
    
    /**
     * Type de panneau à créer
     */
    public enum PaneType {
        VISUALIZATION,  // Volet de visualisation avec bouton "OUVRIR"
        DETAIL         // Fiche détaillée avec boutons d'édition
    }
    
    /**
     * Configuration pour créer un panneau de détail/visualisation
     */
    public static class PaneConfig {
        private String title;
        private String entityType;
        private PaneType type = PaneType.VISUALIZATION;
        private boolean showImage = true;
        private boolean showQrCode = false;
        private Runnable onOpen;
        private Runnable onEdit;
        private Runnable onDelete;
        private double imageWidth = 120;
        private double imageHeight = 120;
        
        public PaneConfig(String title, String entityType) {
            this.title = title;
            this.entityType = entityType;
        }
        
        // Builder pattern methods
        public PaneConfig type(PaneType type) { this.type = type; return this; }
        public PaneConfig showImage(boolean show) { this.showImage = show; return this; }
        public PaneConfig showQrCode(boolean show) { this.showQrCode = show; return this; }
        public PaneConfig onOpen(Runnable action) { this.onOpen = action; return this; }
        public PaneConfig onEdit(Runnable action) { this.onEdit = action; return this; }
        public PaneConfig onDelete(Runnable action) { this.onDelete = action; return this; }
        public PaneConfig imageSize(double width, double height) { 
            this.imageWidth = width; 
            this.imageHeight = height; 
            return this; 
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getEntityType() { return entityType; }
        public PaneType getType() { return type; }
        public boolean isShowImage() { return showImage; }
        public boolean isShowQrCode() { return showQrCode; }
        public Runnable getOnOpen() { return onOpen; }
        public Runnable getOnEdit() { return onEdit; }
        public Runnable getOnDelete() { return onDelete; }
        public double getImageWidth() { return imageWidth; }
        public double getImageHeight() { return imageHeight; }
    }
    
    /**
     * Structure de données pour les informations d'une entité
     */
    public static class EntityInfo {
        private String name;
        private String reference;
        private String category;
        private String status;
        private String description;
        private String imagePath;
        private String qrCodePath;
        
        public EntityInfo(String name) {
            this.name = name;
        }
        
        // Builder pattern methods
        public EntityInfo reference(String ref) { this.reference = ref; return this; }
        public EntityInfo category(String cat) { this.category = cat; return this; }
        public EntityInfo status(String stat) { this.status = stat; return this; }
        public EntityInfo description(String desc) { this.description = desc; return this; }
        public EntityInfo imagePath(String path) { this.imagePath = path; return this; }
        public EntityInfo qrCodePath(String path) { this.qrCodePath = path; return this; }
        
        // Getters
        public String getName() { return name; }
        public String getReference() { return reference; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public String getImagePath() { return imagePath; }
        public String getQrCodePath() { return qrCodePath; }
    }
    
    /**
     * Crée un panneau de détail ou de visualisation unifié
     */
    public static DetailPane createPane(PaneConfig config) {
        return new DetailPane(config);
    }
    
    /**
     * Classe représentant un panneau de détail/visualisation unifié
     */
    public static class DetailPane extends VBox {
        
        private final PaneConfig config;
        private Label titleLabel;
        private ImageView entityImage;
        private ImageView qrCodeImage;
        private VBox infoContainer;
        private HBox buttonContainer;
        private Button openButton;
        private Button editButton;
        private Button deleteButton;
        
        // Labels pour les informations courantes
        private Label nameLabel;
        private Label referenceLabel;
        private Label categoryLabel;
        private Label statusLabel;
        private Label descriptionLabel;
        
        private DetailPane(PaneConfig config) {
            this.config = config;
            initializePane();
            createComponents();
            layoutComponents();
            setupStyles();
        }
        
        private void initializePane() {
            setSpacing(12);
            setPrefWidth(320);
            setPadding(new Insets(16));
            getStyleClass().add("detail-pane");
        }
        
        private void createComponents() {
            // Titre
            titleLabel = new Label(config.getTitle());
            titleLabel.getStyleClass().add("detail-title");
            
            // Images
            if (config.isShowImage()) {
                entityImage = new ImageView();
                entityImage.setFitWidth(config.getImageWidth());
                entityImage.setFitHeight(config.getImageHeight());
                entityImage.setPreserveRatio(true);
                entityImage.getStyleClass().add("entity-image");
            }
            
            if (config.isShowQrCode()) {
                qrCodeImage = new ImageView();
                qrCodeImage.setFitWidth(80);
                qrCodeImage.setFitHeight(80);
                qrCodeImage.setPreserveRatio(true);
                qrCodeImage.getStyleClass().add("qr-code-image");
            }
            
            // Container pour les informations
            infoContainer = new VBox(8);
            infoContainer.getStyleClass().add("info-container");
            
            // Labels d'information
            nameLabel = createInfoLabel("Nom", "-");
            referenceLabel = createInfoLabel("Référence", "-");
            categoryLabel = createInfoLabel("Catégorie", "-");
            statusLabel = createInfoLabel("Statut", "-");
            descriptionLabel = createInfoLabel("Description", "-");
            
            // Boutons
            buttonContainer = new HBox(8);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.getStyleClass().add("button-container");
            
            if (config.getType() == PaneType.VISUALIZATION) {
                openButton = new Button("OUVRIR");
                openButton.getStyleClass().addAll("primary-button", "open-button");
                if (config.getOnOpen() != null) {
                    openButton.setOnAction(e -> config.getOnOpen().run());
                }
            } else {
                editButton = new Button("Modifier");
                editButton.getStyleClass().addAll("primary-button", "edit-button");
                if (config.getOnEdit() != null) {
                    editButton.setOnAction(e -> config.getOnEdit().run());
                }
                
                deleteButton = new Button("Supprimer");
                deleteButton.getStyleClass().addAll("danger-button", "delete-button");
                if (config.getOnDelete() != null) {
                    deleteButton.setOnAction(e -> config.getOnDelete().run());
                }
            }
        }
        
        private void layoutComponents() {
            getChildren().add(titleLabel);
            
            // Zone média (image + QR code)
            if (config.isShowImage() || config.isShowQrCode()) {
                HBox mediaBox = new HBox(10);
                mediaBox.setAlignment(Pos.CENTER);
                mediaBox.getStyleClass().add("media-box");
                
                if (config.isShowImage() && entityImage != null) {
                    VBox imageBox = new VBox(5);
                    imageBox.setAlignment(Pos.CENTER);
                    imageBox.getChildren().addAll(entityImage, new Label("Photo"));
                    mediaBox.getChildren().add(imageBox);
                }
                
                if (config.isShowQrCode() && qrCodeImage != null) {
                    VBox qrBox = new VBox(5);
                    qrBox.setAlignment(Pos.CENTER);
                    qrBox.getChildren().addAll(qrCodeImage, new Label("QR Code"));
                    mediaBox.getChildren().add(qrBox);
                }
                
                getChildren().add(mediaBox);
            }
            
            // Informations
            infoContainer.getChildren().addAll(
                nameLabel, referenceLabel, categoryLabel, statusLabel, descriptionLabel
            );
            getChildren().add(infoContainer);
            
            // Spacer flexible
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            getChildren().add(spacer);
            
            // Boutons
            if (config.getType() == PaneType.VISUALIZATION && openButton != null) {
                buttonContainer.getChildren().add(openButton);
            } else if (config.getType() == PaneType.DETAIL) {
                if (editButton != null) buttonContainer.getChildren().add(editButton);
                if (deleteButton != null) buttonContainer.getChildren().add(deleteButton);
            }
            
            if (!buttonContainer.getChildren().isEmpty()) {
                getChildren().add(buttonContainer);
            }
        }
        
        private void setupStyles() {
            // Les styles CSS seront définis dans le fichier CSS principal
        }
        
        private Label createInfoLabel(String labelText, String valueText) {
            VBox container = new VBox(2);
            
            Label label = new Label(labelText + " :");
            label.getStyleClass().add("info-label");
            
            Label value = new Label(valueText);
            value.getStyleClass().add("info-value");
            value.setWrapText(true);
            
            container.getChildren().addAll(label, value);
            container.getStyleClass().add("info-field");
            
            // Retourner le label de valeur pour pouvoir le mettre à jour
            return value;
        }
        
        /**
         * Met à jour les informations affichées dans le panneau
         */
        public void updateInfo(EntityInfo entityInfo) {
            if (entityInfo.getName() != null) {
                nameLabel.setText(entityInfo.getName());
            }
            if (entityInfo.getReference() != null) {
                referenceLabel.setText(entityInfo.getReference());
            }
            if (entityInfo.getCategory() != null) {
                categoryLabel.setText(entityInfo.getCategory());
            }
            if (entityInfo.getStatus() != null) {
                statusLabel.setText(entityInfo.getStatus());
            }
            if (entityInfo.getDescription() != null) {
                descriptionLabel.setText(entityInfo.getDescription());
            }
            
            // Mise à jour des images si nécessaire  
            // TODO: Implémenter le chargement d'images
        }
        
        /**
         * Active/désactive les boutons
         */
        public void setButtonsDisabled(boolean disabled) {
            if (openButton != null) openButton.setDisable(disabled);
            if (editButton != null) editButton.setDisable(disabled);
            if (deleteButton != null) deleteButton.setDisable(disabled);
        }
        
        // Getters pour accéder aux composants si nécessaire
        public ImageView getEntityImage() { return entityImage; }
        public ImageView getQrCodeImage() { return qrCodeImage; }
        public Button getOpenButton() { return openButton; }
        public Button getEditButton() { return editButton; }
        public Button getDeleteButton() { return deleteButton; }
    }
}