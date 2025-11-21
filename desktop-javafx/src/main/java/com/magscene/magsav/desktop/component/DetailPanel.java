package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.QRCodeGenerator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Volet de détails qui apparaît par glissement depuis la droite
 * pour afficher les informations détaillées d'un item sélectionné
 */
public class DetailPanel extends VBox {
    
    private static final double PANEL_WIDTH = 400;
    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    
    private boolean isVisible = false;
    private TranslateTransition slideAnimation;
    
    // Composants principaux
    private VBox headerSection;
    private VBox imageSection;
    private VBox infoSection;
    private VBox qrCodeSection;
    private Button closeButton;
    
    // Éléments de contenu
    private Label titleLabel;
    private Label subtitleLabel;
    private ImageView mainImageView;
    private ImageView qrCodeView;
    private VBox dynamicInfoContainer;
    
    public DetailPanel() {
        initializeComponents();
        setupLayout();
        setupAnimation();
        setupStyling();
        
        // Initialement caché à droite
        setTranslateX(PANEL_WIDTH);
        setVisible(false);
    }
    
    private void initializeComponents() {
        // Header avec titre et bouton fermer
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#ffffff"));
        titleLabel.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; -fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-text-fill: #ffffff;");
        
        subtitleLabel = new Label();
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#B8BCC8"));
        subtitleLabel.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-text-fill: #B8BCC8;");
        
        closeButton = new Button("✕");
        closeButton.setFont(Font.font("System", FontWeight.BOLD, 16));
        closeButton.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 15; " +
            "-fx-min-width: 30; " +
            "-fx-min-height: 30; " +
            "-fx-max-width: 30; " +
            "-fx-max-height: 30;"
        );
        closeButton.setOnAction(e -> hide());
        
        // Section image/avatar
        mainImageView = new ImageView();
        mainImageView.setFitWidth(120);
        mainImageView.setFitHeight(120);
        mainImageView.setPreserveRatio(true);
        mainImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        
        // Section QR Code
        qrCodeView = new ImageView();
        qrCodeView.setFitWidth(100);
        qrCodeView.setFitHeight(100);
        qrCodeView.setPreserveRatio(true);
        
        // Container pour informations dynamiques
        dynamicInfoContainer = new VBox(8);
    }
    
    private void setupLayout() {
        // Header
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(
            new VBox(2, titleLabel, subtitleLabel), 
            spacer, 
            closeButton
        );
        
        headerSection = new VBox(5);
        headerSection.setPadding(new Insets(15, 15, 10, 15));
        headerSection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; -fx-background-radius: 8 8 0 0;");
        headerSection.getChildren().add(headerBox);
        
        // Section image
        imageSection = new VBox(10);
        imageSection.setAlignment(Pos.CENTER);
        imageSection.setPadding(new Insets(15));
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
            qrCodeSection
        );
        
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
            "-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
            "-fx-border-color: #8B91FF; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 5px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, -5, 0);"
        );
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
        titleLabel.setText(title != null ? title : "");
        subtitleLabel.setText(subtitle != null ? subtitle : "");
        
        // Image principale
        if (mainImage != null) {
            mainImageView.setImage(mainImage);
            imageSection.setVisible(true);
            imageSection.setManaged(true);
        } else {
            // Image par défaut ou icône
            mainImageView.setImage(createDefaultImage());
            imageSection.setVisible(true);
            imageSection.setManaged(true);
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
     * Met à jour le contenu avec animation bidirectionnelle (sortie + entrée depuis la droite)
     */
    public void updateContentWithAnimation(String title, String subtitle, Image mainImage, String qrCodeData, VBox infoContent) {
        if (!isVisible) {
            // Si le volet n'est pas visible, l'afficher directement avec le nouveau contenu
            updateContent(title, subtitle, mainImage, qrCodeData, infoContent);
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
            updateContent(title, subtitle, mainImage, qrCodeData, infoContent);
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
