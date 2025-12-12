package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.util.QRCodeGenerator;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Volet de détails qui apparaît par glissement depuis la droite
 * pour afficher les informations détaillées d'un item sélectionné
 * Supporte l'affichage des photos d'équipements et logos de marques
 */
public class DetailPanel extends VBox {

    private static final double PANEL_WIDTH = 400;
    private static final Duration ANIMATION_DURATION = Duration.millis(300);

    private boolean isVisible = false;
    private TranslateTransition slideAnimation;
    
    // Service médias pour les photos et logos
    private final MediaService mediaService;

    // Composants principaux
    private VBox headerSection;
    private VBox imageSection;
    private VBox infoSection;
    private VBox qrCodeSection;

    // Éléments de contenu
    private Label titleLabel;
    private Label subtitleLabel;
    private ImageView mainImageView;
    private ImageView qrCodeView;
    private VBox dynamicInfoContainer;
    
    // Éléments du header compact
    private ImageView headerPhotoView;
    private ImageView headerLogoView;

    public DetailPanel() {
        this.mediaService = new MediaService();
        initializeComponents();
        setupLayout();
        setupAnimation();
        setupStyling();

        // Initialement caché à droite
        setTranslateX(PANEL_WIDTH);
        setVisible(false);
    }

    private void initializeComponents() {
        // Header avec titre
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#ffffff"));
        titleLabel.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-text-fill: #ffffff;");

        subtitleLabel = new Label();
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#B8BCC8"));
        subtitleLabel.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-text-fill: #B8BCC8;");

        // Section image/avatar - Photo de l'équipement
        mainImageView = new ImageView();
        mainImageView.setFitWidth(180);
        mainImageView.setFitHeight(180);
        mainImageView.setPreserveRatio(true);
        mainImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // Section QR Code
        qrCodeView = new ImageView();
        qrCodeView.setFitWidth(100);
        qrCodeView.setFitHeight(100);
        qrCodeView.setPreserveRatio(true);

        // Container pour informations dynamiques
        dynamicInfoContainer = new VBox(8);
        
        // ImageViews pour le header compact
        headerPhotoView = new ImageView();
        headerPhotoView.setFitWidth(50);
        headerPhotoView.setFitHeight(50);
        headerPhotoView.setPreserveRatio(true);
        
        headerLogoView = new ImageView();
        headerLogoView.setFitWidth(50);
        headerLogoView.setFitHeight(30);
        headerLogoView.setPreserveRatio(true);
    }

    private void setupLayout() {
        // Photo miniature dans le header
        StackPane headerPhotoContainer = new StackPane();
        headerPhotoContainer.setMinSize(50, 50);
        headerPhotoContainer.setMaxSize(50, 50);
        headerPhotoContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 6;");
        headerPhotoContainer.getChildren().add(headerPhotoView);
        
        // Logo de marque dans le header
        StackPane headerLogoContainer = new StackPane();
        headerLogoContainer.setMinSize(50, 30);
        headerLogoContainer.setMaxSize(50, 30);
        headerLogoContainer.getChildren().add(headerLogoView);
        
        // Header
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(
                headerPhotoContainer,
                new VBox(2, titleLabel, subtitleLabel),
                spacer,
                headerLogoContainer);

        headerSection = new VBox(5);
        headerSection.setPadding(new Insets(15, 15, 10, 15));
        headerSection.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-background-radius: 8 8 0 0;");
        headerSection.getChildren().add(headerBox);

        // Section image (photo de l'équipement uniquement - logo dans le header)
        imageSection = new VBox(10);
        imageSection.setAlignment(Pos.CENTER);
        imageSection.setPadding(new Insets(15));
        imageSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px;");
        
        imageSection.getChildren().add(mainImageView);

        // Section informations
        Label infoTitle = new Label("📋 Informations");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web("#2c3e50"));

        infoSection = new VBox(10);
        infoSection.setPadding(new Insets(0, 15, 15, 15));
        infoSection.getChildren().addAll(infoTitle, dynamicInfoContainer);

        // Section QR Code
        Label qrTitle = new Label("🔳 QR Code");
        qrTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        qrTitle.setTextFill(Color.web("#2c3e50"));

        qrCodeSection = new VBox(10);
        qrCodeSection.setAlignment(Pos.CENTER);
        qrCodeSection.setPadding(new Insets(0, 15, 15, 15));
        qrCodeSection.getChildren().addAll(qrTitle, qrCodeView);

        // Conteneur principal avec scroll
        VBox contentContainer = new VBox();
        contentContainer.getChildren().addAll(
                headerSection,
                imageSection,
                infoSection,
                qrCodeSection);

        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void setupAnimation() {
        slideAnimation = new TranslateTransition(ANIMATION_DURATION, this);
        slideAnimation.setOnFinished(e -> {
            if (!isVisible) {
                setVisible(false);
            }
        });
    }

    private void setupStyling() {
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);
        setStyle(
                "-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                        "-fx-border-color: #8B91FF; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, -5, 0);");
    }

    /**
     * Affiche le volet avec animation de glissement depuis la droite
     */
    public void show() {
        if (!isVisible) {
            setVisible(true);
            isVisible = true;

            slideAnimation.setFromX(PANEL_WIDTH);
            slideAnimation.setToX(0);
            slideAnimation.play();
        }
    }

    /**
     * Cache le volet avec animation de glissement vers la droite
     */
    public void hide() {
        if (isVisible) {
            isVisible = false;

            slideAnimation.setFromX(0);
            slideAnimation.setToX(PANEL_WIDTH);
            slideAnimation.play();
        }
    }

    /**
     * Met à jour le contenu du volet avec les données d'un item
     */
    public void updateContent(String title, String subtitle, Image mainImage, String qrCodeData, VBox infoContent) {
        updateContent(title, subtitle, mainImage, qrCodeData, infoContent, null, null, null);
    }
    
    /**
     * Met à jour le contenu du volet avec support des photos et logos
     * @param title Titre de l'élément
     * @param subtitle Sous-titre
     * @param mainImage Image principale (si déjà chargée)
     * @param qrCodeData Données du QR code
     * @param infoContent Contenu des informations
     * @param photoPath Chemin de la photo (pour chargement depuis MediaService)
     * @param brandName Nom de la marque (pour charger le logo)
     * @param equipmentImage Image de l'équipement pré-chargée (optionnel)
     */
    public void updateContent(String title, String subtitle, Image mainImage, String qrCodeData, 
                              VBox infoContent, String photoPath, String brandName, Image equipmentImage) {
        titleLabel.setText(title != null ? title : "");
        subtitleLabel.setText(subtitle != null ? subtitle : "");

        // Image principale - Photo de l'équipement
        Image imageToShow = equipmentImage;
        if (imageToShow == null && photoPath != null && !photoPath.isEmpty()) {
            imageToShow = mediaService.loadEquipmentPhoto(photoPath, 180, 180);
        }
        if (imageToShow == null) {
            imageToShow = mainImage;
        }
        
        // Miniature dans le header
        if (imageToShow != null) {
            headerPhotoView.setImage(imageToShow);
        } else {
            headerPhotoView.setImage(null);
        }
        
        if (imageToShow != null) {
            mainImageView.setImage(imageToShow);
            imageSection.setVisible(true);
            imageSection.setManaged(true);
        } else {
            // Image par défaut ou icône
            mainImageView.setImage(createDefaultImage());
            imageSection.setVisible(true);
            imageSection.setManaged(true);
        }
        
        // Logo de la marque (uniquement dans le header)
        if (brandName != null && !brandName.isEmpty()) {
            Image brandLogo = mediaService.getBrandLogo(brandName, 60, 40);
            if (brandLogo != null) {
                headerLogoView.setImage(brandLogo);
            } else {
                headerLogoView.setImage(null);
            }
        } else {
            headerLogoView.setImage(null);
        }

        // QR Code - N'afficher que si les données ne sont pas vides
        if (qrCodeData != null && !qrCodeData.trim().isEmpty()) {
            qrCodeView.setImage(generateDefaultQRCode(qrCodeData));
            qrCodeSection.setVisible(true);
            qrCodeSection.setManaged(true);
        } else {
            // Pas de QR Code - masquer la section
            qrCodeSection.setVisible(false);
            qrCodeSection.setManaged(false);
        }

        // Informations dynamiques
        dynamicInfoContainer.getChildren().clear();
        if (infoContent != null) {
            dynamicInfoContainer.getChildren().addAll(infoContent.getChildren());
        }
    }

    /**
     * Met à jour le contenu avec animation bidirectionnelle (sortie + entrée depuis
     * la droite)
     */
    public void updateContentWithAnimation(String title, String subtitle, Image mainImage, String qrCodeData,
            VBox infoContent) {
        updateContentWithAnimation(title, subtitle, mainImage, qrCodeData, infoContent, null, null, null);
    }
    
    /**
     * Met à jour le contenu avec animation bidirectionnelle et support des photos/logos
     */
    public void updateContentWithAnimation(String title, String subtitle, Image mainImage, String qrCodeData,
            VBox infoContent, String photoPath, String brandName, Image equipmentImage) {
        if (!isVisible) {
            // Si le volet n'est pas visible, l'afficher directement avec le nouveau contenu
            updateContent(title, subtitle, mainImage, qrCodeData, infoContent, photoPath, brandName, equipmentImage);
            show();
            return;
        }

        // Animation de sortie vers la droite
        TranslateTransition slideOut = new TranslateTransition(ANIMATION_DURATION, this);
        slideOut.setFromX(0);
        slideOut.setToX(PANEL_WIDTH);

        // Animation d'entrée depuis la droite
        TranslateTransition slideIn = new TranslateTransition(ANIMATION_DURATION, this);
        slideIn.setFromX(PANEL_WIDTH);
        slideIn.setToX(0);

        // Entre les deux animations, mettre à jour le contenu
        slideOut.setOnFinished(e -> {
            updateContent(title, subtitle, mainImage, qrCodeData, infoContent, photoPath, brandName, equipmentImage);
            slideIn.play();
        });

        // Lancer l'animation de sortie
        slideOut.play();
    }

    /**
     * Crée une image par défaut si aucune image n'est fournie
     */
    private Image createDefaultImage() {
        // Pour l'instant, retourne null - à remplacer par une vraie image par défaut
        return null;
    }

    /**
     * Génère un QR Code simple par défaut
     */
    private Image generateDefaultQRCode(String data) {
        return QRCodeGenerator.generateQRCode(data != null ? data : "Default QR");
    }

    /**
     * Crée une ligne d'information avec label et valeur
     */
    public static HBox createInfoRow(String label, String value) {
        Label labelNode = new Label(label + ":");
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#34495e"));
        labelNode.setPrefWidth(100);

        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setFont(Font.font("System", FontWeight.NORMAL, 12));
        valueNode.setTextFill(Color.web("#2c3e50"));
        valueNode.setWrapText(true);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(labelNode, valueNode);

        return row;
    }

    public boolean isShowing() {
        return isVisible;
    }
}
