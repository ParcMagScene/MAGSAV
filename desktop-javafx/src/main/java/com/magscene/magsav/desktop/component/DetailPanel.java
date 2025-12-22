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
        this.mediaService = MediaService.getInstance();
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
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(280);

        subtitleLabel = new Label();
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#E8E9FF"));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(280);

        // Section image/avatar - Photo de l'équipement
        mainImageView = new ImageView();
        mainImageView.setFitWidth(120);
        mainImageView.setFitHeight(120);
        mainImageView.setPreserveRatio(true);
        mainImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // Section QR Code
        qrCodeView = new ImageView();
        qrCodeView.setFitWidth(80);
        qrCodeView.setFitHeight(80);
        qrCodeView.setPreserveRatio(true);

        // Container pour informations dynamiques
        dynamicInfoContainer = new VBox(6);
        dynamicInfoContainer.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-background-radius: 5; -fx-text-fill: #2c3e50;");
        dynamicInfoContainer.setMinHeight(100);
        
        // ImageViews pour le header - Photo plus grande (70x70)
        headerPhotoView = new ImageView();
        headerPhotoView.setFitWidth(70);
        headerPhotoView.setFitHeight(70);
        headerPhotoView.setPreserveRatio(true);
        
        // Logo de marque (taille standard)
        headerLogoView = new ImageView();
        headerLogoView.setFitWidth(60);
        headerLogoView.setFitHeight(40);
        headerLogoView.setPreserveRatio(true);
    }

    private void setupLayout() {
        // Photo dans le header (à gauche) - plus grande
        StackPane headerPhotoContainer = new StackPane();
        headerPhotoContainer.setMinSize(70, 70);
        headerPhotoContainer.setMaxSize(70, 70);
        headerPhotoContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 6;");
        headerPhotoContainer.getChildren().add(headerPhotoView);
        
        // Logo de marque dans le header (à droite)
        StackPane headerLogoContainer = new StackPane();
        headerLogoContainer.setMinSize(60, 40);
        headerLogoContainer.setMaxSize(60, 40);
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

        headerSection = new VBox(3);
        headerSection.setPadding(new Insets(10, 12, 8, 12));
        headerSection.setStyle("-fx-background-color: " + ThemeConstants.PRIMARY_COLOR
                + "; -fx-background-radius: 8 8 0 0;");
        headerSection.getChildren().add(headerBox);

        // Section image (photo de l'équipement uniquement - logo dans le header)
        imageSection = new VBox(5);
        imageSection.setAlignment(Pos.CENTER);
        imageSection.setPadding(new Insets(10));
        imageSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px;");
        
        imageSection.getChildren().add(mainImageView);

        // Section informations
        Label infoTitle = new Label("📋 Informations");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web("#2c3e50"));

        infoSection = new VBox(8);
        infoSection.setPadding(new Insets(10, 12, 10, 12));
        infoSection.setStyle("-fx-background-color: #f5f6fa;");
        infoSection.getChildren().addAll(infoTitle, dynamicInfoContainer);
        // S'assurer que la section info est toujours visible
        infoSection.setVisible(true);
        infoSection.setManaged(true);

        // Section QR Code
        Label qrTitle = new Label("🔳 QR Code");
        qrTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        qrTitle.setTextFill(Color.web("#2c3e50"));

        qrCodeSection = new VBox(6);
        qrCodeSection.setAlignment(Pos.CENTER);
        qrCodeSection.setPadding(new Insets(0, 12, 10, 12));
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
     * @param preloadedImage Image pré-chargée (photo équipement ou véhicule, optionnel)
     */
    public void updateContent(String title, String subtitle, Image mainImage, String qrCodeData, 
                              VBox infoContent, String photoPath, String brandName, Image preloadedImage) {
        titleLabel.setText(title != null ? title : "");
        subtitleLabel.setText(subtitle != null ? subtitle : "");

        // Image principale - Utiliser l'image pré-chargée en priorité
        // Si pas d'image pré-chargée, utiliser mainImage (de getDetailImage())
        Image imageToShow = preloadedImage;
        if (imageToShow == null) {
            imageToShow = mainImage;
        }
        
        // Photo dans le header (à gauche) - afficher si disponible
        if (imageToShow != null) {
            headerPhotoView.setImage(imageToShow);
        } else {
            headerPhotoView.setImage(null);
        }
        
        // Section image centrale - toujours masquée (photo uniquement dans le header)
        // La photo est affichée dans le header à gauche, pas besoin de duplication
        imageSection.setVisible(false);
        imageSection.setManaged(false);
        
        // Logo de la marque (dans le header à droite)
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

        // Informations dynamiques - Recréer les éléments pour éviter les problèmes de parent unique
        dynamicInfoContainer.getChildren().clear();
        if (infoContent != null && !infoContent.getChildren().isEmpty()) {
            System.out.println("📋 DetailPanel: Ajout de " + infoContent.getChildren().size() + " éléments d'info");
            
            // Créer une copie profonde de chaque élément
            for (javafx.scene.Node node : new java.util.ArrayList<>(infoContent.getChildren())) {
                javafx.scene.Node copiedNode = deepCopyNode(node);
                if (copiedNode != null) {
                    dynamicInfoContainer.getChildren().add(copiedNode);
                }
            }
            System.out.println("✅ DetailPanel: " + dynamicInfoContainer.getChildren().size() + " éléments ajoutés au volet");
        } else {
            System.out.println("⚠️ DetailPanel: infoContent vide ou null");
        }
    }
    
    /**
     * Crée une copie profonde d'un Node pour éviter les problèmes de parent unique
     */
    private javafx.scene.Node deepCopyNode(javafx.scene.Node node) {
        if (node instanceof Label) {
            Label original = (Label) node;
            Label copy = new Label(original.getText());
            // Police lisible
            copy.setFont(Font.font("System", 12));
            // Couleur visible forcée
            copy.setTextFill(Color.web("#2c3e50"));
            copy.setWrapText(true);
            copy.setMaxWidth(350);
            // Copier le style si présent
            if (original.getStyle() != null && !original.getStyle().isEmpty()) {
                copy.setStyle(original.getStyle() + "; -fx-text-fill: #2c3e50;");
            } else {
                copy.setStyle("-fx-text-fill: #2c3e50;");
            }
            // Debug: afficher le texte copié
            System.out.println("   📝 Label copié: " + original.getText());
            return copy;
        } else if (node instanceof HBox) {
            HBox original = (HBox) node;
            HBox copy = new HBox(original.getSpacing());
            copy.setAlignment(original.getAlignment());
            copy.setPadding(original.getPadding());
            if (original.getStyle() != null && !original.getStyle().isEmpty()) {
                copy.setStyle(original.getStyle());
            }
            for (javafx.scene.Node child : original.getChildren()) {
                javafx.scene.Node copiedChild = deepCopyNode(child);
                if (copiedChild != null) {
                    copy.getChildren().add(copiedChild);
                }
            }
            return copy;
        } else if (node instanceof VBox) {
            VBox original = (VBox) node;
            VBox copy = new VBox(original.getSpacing());
            copy.setAlignment(original.getAlignment());
            copy.setPadding(original.getPadding());
            if (original.getStyle() != null && !original.getStyle().isEmpty()) {
                copy.setStyle(original.getStyle());
            }
            for (javafx.scene.Node child : original.getChildren()) {
                javafx.scene.Node copiedChild = deepCopyNode(child);
                if (copiedChild != null) {
                    copy.getChildren().add(copiedChild);
                }
            }
            return copy;
        } else if (node instanceof javafx.scene.layout.GridPane) {
            javafx.scene.layout.GridPane original = (javafx.scene.layout.GridPane) node;
            javafx.scene.layout.GridPane copy = new javafx.scene.layout.GridPane();
            copy.setHgap(original.getHgap());
            copy.setVgap(original.getVgap());
            copy.setAlignment(original.getAlignment());
            copy.setPadding(original.getPadding());
            if (original.getStyle() != null && !original.getStyle().isEmpty()) {
                copy.setStyle(original.getStyle());
            }
            // Copier les contraintes de colonnes
            copy.getColumnConstraints().addAll(original.getColumnConstraints());
            copy.getRowConstraints().addAll(original.getRowConstraints());
            // Copier les enfants avec leurs positions
            for (javafx.scene.Node child : original.getChildren()) {
                javafx.scene.Node copiedChild = deepCopyNode(child);
                if (copiedChild != null) {
                    Integer rowIndex = javafx.scene.layout.GridPane.getRowIndex(child);
                    Integer colIndex = javafx.scene.layout.GridPane.getColumnIndex(child);
                    Integer rowSpan = javafx.scene.layout.GridPane.getRowSpan(child);
                    Integer colSpan = javafx.scene.layout.GridPane.getColumnSpan(child);
                    copy.add(copiedChild, 
                        colIndex != null ? colIndex : 0, 
                        rowIndex != null ? rowIndex : 0,
                        colSpan != null ? colSpan : 1,
                        rowSpan != null ? rowSpan : 1);
                }
            }
            return copy;
        } else if (node instanceof Region) {
            // Pour les spacers et autres Region
            Region original = (Region) node;
            Region copy = new Region();
            copy.setMinWidth(original.getMinWidth());
            copy.setMinHeight(original.getMinHeight());
            copy.setPrefWidth(original.getPrefWidth());
            copy.setPrefHeight(original.getPrefHeight());
            copy.setMaxWidth(original.getMaxWidth());
            copy.setMaxHeight(original.getMaxHeight());
            if (original.getStyle() != null && !original.getStyle().isEmpty()) {
                copy.setStyle(original.getStyle());
            }
            // Préserver HGrow/VGrow si définis
            Priority hgrow = HBox.getHgrow(original);
            Priority vgrow = VBox.getVgrow(original);
            if (hgrow != null) HBox.setHgrow(copy, hgrow);
            if (vgrow != null) VBox.setVgrow(copy, vgrow);
            return copy;
        } else {
            // Pour les autres types, on essaie de les cloner ou on retourne null
            System.out.println("⚠️ Type de node non supporté pour copie: " + node.getClass().getSimpleName());
            return null;
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
    @SuppressWarnings("unused")  // Conservée pour usage futur
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
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 11));
        labelNode.setTextFill(Color.web("#34495e"));
        labelNode.setMinWidth(90);
        labelNode.setPrefWidth(90);

        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setFont(Font.font("System", FontWeight.NORMAL, 11));
        valueNode.setTextFill(Color.web("#2c3e50"));
        valueNode.setWrapText(true);

        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(labelNode, valueNode);

        return row;
    }

    public boolean isShowing() {
        return isVisible;
    }
}
