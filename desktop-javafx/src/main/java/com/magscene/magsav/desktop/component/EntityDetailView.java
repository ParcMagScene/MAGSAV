package com.magscene.magsav.desktop.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
// import com.magscene.magsav.desktop.utils.QRCodeGenerator;

import java.util.List;
import java.util.Map;

/**
 * Vue de détail unifiée pour toutes les entités avec thème sombre
 * Compatible avec le thème #142240/#142240/#8B91FF
 */
public class EntityDetailView extends Stage {

    // Constantes couleurs du thème sombre unifié
    public static final String PRIMARY_BG = "#142240";
    public static final String SECONDARY_BG = "#142240";
    public static final String ACCENT_COLOR = "#8B91FF";
    public static final String TEXT_PRIMARY = "#FFFFFF";
    public static final String TEXT_SECONDARY = "#B8C5E6";
    public static final String BORDER_COLOR = "#2A3A5C";

    // Composants principaux
    private VBox mainContainer;
    private HBox headerContainer;
    private VBox contentContainer;

    // Header
    private Label titleLabel;
    private Label subtitleLabel;
    private Label typeLabel;

    // Sections principales
    private VBox imageSection;
    private VBox infoSection;
    private VBox qrSection;
    private VBox actionsSection;

    // Composants dynamiques
    private ImageView mainImageView;
    private ImageView qrCodeView;
    private VBox dynamicInfoContainer;
    private HBox actionButtonsContainer;

    /**
     * Constructeur principal
     */
    public EntityDetailView(String entityType) {
        initializeWindow(entityType);
        initializeComponents(entityType);
        setupLayout();
        applyStyling();
    }

    private void initializeWindow(String entityType) {
        setTitle("Détails - " + entityType);
        setWidth(600);
        setHeight(700);
        setResizable(true);
        initModality(Modality.APPLICATION_MODAL);
        centerOnScreen();
    }

    private void initializeComponents(String entityType) {
        // Labels d'header
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(TEXT_PRIMARY));

        subtitleLabel = new Label();
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web(TEXT_SECONDARY));

        typeLabel = new Label();
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeLabel.setTextFill(Color.web(ACCENT_COLOR));
        typeLabel.setStyle(
                "-fx-background-color: " + SECONDARY_BG + "; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-padding: 4 12 4 12; " +
                        "-fx-border-color: " + ACCENT_COLOR + "; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 12px;");

        // Image principale
        mainImageView = new ImageView();
        mainImageView.setFitWidth(150);
        mainImageView.setFitHeight(150);
        mainImageView.setPreserveRatio(true);
        mainImageView.setStyle("");

        // QR Code
        qrCodeView = new ImageView();
        qrCodeView.setFitWidth(120);
        qrCodeView.setFitHeight(120);
        qrCodeView.setPreserveRatio(true);

        // Container pour informations dynamiques
        dynamicInfoContainer = new VBox(8);

        // Container pour boutons d'action
        actionButtonsContainer = new HBox(10);
        actionButtonsContainer.setAlignment(Pos.CENTER);
    }

    private void setupLayout() {
        // Header section
        VBox headerTexts = new VBox(5);
        headerTexts.getChildren().addAll(titleLabel, subtitleLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        headerContainer = new HBox(15);
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        headerContainer.setPadding(new Insets(20));
        headerContainer.getChildren().addAll(headerTexts, headerSpacer, typeLabel);

        // Image section
        imageSection = new VBox(15);
        imageSection.setAlignment(Pos.CENTER);
        imageSection.setPadding(new Insets(0, 20, 20, 20));
        imageSection.getChildren().add(mainImageView);

        // Info section avec titre
        Label infoTitle = createSectionTitle("📋 Informations");

        infoSection = new VBox(12);
        infoSection.setPadding(new Insets(0, 20, 20, 20));
        infoSection.getChildren().addAll(infoTitle, dynamicInfoContainer);

        // QR Code section
        Label qrTitle = createSectionTitle("📱 QR Code");

        qrSection = new VBox(12);
        qrSection.setAlignment(Pos.CENTER);
        qrSection.setPadding(new Insets(0, 20, 20, 20));
        qrSection.getChildren().addAll(qrTitle, qrCodeView);

        // Actions section
        Label actionsTitle = createSectionTitle("⚡ Actions");

        actionsSection = new VBox(12);
        actionsSection.setPadding(new Insets(0, 20, 20, 20));
        actionsSection.getChildren().addAll(actionsTitle, actionButtonsContainer);

        // Container principal du contenu
        contentContainer = new VBox();
        contentContainer.getChildren().addAll(
                imageSection,
                infoSection,
                qrSection,
                actionsSection);

        // ScrollPane pour le contenu
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: " + PRIMARY_BG + "; -fx-background-color: " + PRIMARY_BG + ";");

        // Container principal
        mainContainer = new VBox();
        mainContainer.getChildren().addAll(headerContainer, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Scene
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/styles/entity-details.css").toExternalForm());
        setScene(scene);
    }

    private void applyStyling() {
        // Style du container principal
        mainContainer.setStyle(
                "-fx-background-color: " + PRIMARY_BG + "; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-width: 1px;");

        // Style du header
        headerContainer.setStyle(
                "-fx-background-color: " + SECONDARY_BG + "; " +
                        "-fx-border-color: " + BORDER_COLOR + "; " +
                        "-fx-border-width: 0 0 1px 0;");

        // Style du contenu
        contentContainer.setStyle("-fx-background-color: " + PRIMARY_BG + ";");
    }

    private Label createSectionTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        title.setPadding(new Insets(8, 0, 8, 0));
        title.setStyle(
                "-fx-border-color: " + ACCENT_COLOR + "; " +
                        "-fx-border-width: 0 0 1px 0;");
        return title;
    }

    /**
     * Définit les informations principales de l'entité
     */
    public void setEntityInfo(String title, String subtitle, String type) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        typeLabel.setText(type.toUpperCase());
    }

    /**
     * Définit l'image principale de l'entité
     */
    public void setMainImage(Image image) {
        if (image != null) {
            mainImageView.setImage(image);
            imageSection.setVisible(true);
        } else {
            imageSection.setVisible(false);
        }
    }

    /**
     * Définit l'image par défaut selon le type d'entité
     */
    public void setDefaultImage(String entityType) {
        String imagePath = getDefaultImagePath(entityType);
        try {
            if (imagePath != null) {
                Image defaultImage = new Image(getClass().getResourceAsStream(imagePath));
                setMainImage(defaultImage);
            }
        } catch (Exception e) {
            // Image par défaut non trouvée, masquer la section
            imageSection.setVisible(false);
        }
    }

    /**
     * Ajoute une ligne d'information avec label et valeur
     */
    public void addInfoRow(String label, String value) {
        addInfoRow(label, value, false);
    }

    /**
     * Ajoute une ligne d'information avec mise en évidence optionnelle
     */
    public void addInfoRow(String label, String value, boolean highlight) {
        HBox infoRow = createInfoRow(label, value, highlight);
        dynamicInfoContainer.getChildren().add(infoRow);
    }

    /**
     * Ajoute un séparateur visuel
     */
    public void addSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: " + BORDER_COLOR + ";");
        separator.setOpacity(0.3);
        dynamicInfoContainer.getChildren().add(separator);
    }

    /**
     * Génère et affiche le QR Code pour l'entité
     */
    public void generateQRCode(String data) {
        try {
            // TODO: Implement QRCodeGenerator; // Image qrImage =
            // QRCodeGenerator.generateQRCode(data, 120);
            // qrCodeView.setImage(qrImage);
            qrSection.setVisible(false); // Masqué temporairement
        } catch (Exception e) {
            qrSection.setVisible(false);
        }
    }

    /**
     * Ajoute un bouton d'action
     */
    public void addActionButton(String text, String style, Runnable action) {
        Button button = createActionButton(text, style, action);
        actionButtonsContainer.getChildren().add(button);
    }

    /**
     * Vide toutes les informations dynamiques
     */
    public void clearDynamicContent() {
        dynamicInfoContainer.getChildren().clear();
        actionButtonsContainer.getChildren().clear();
    }

    /**
     * Crée une ligne d'information formatée
     */
    private HBox createInfoRow(String label, String value, boolean highlight) {
        Label labelControl = new Label(label + ":");
        labelControl.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelControl.setTextFill(Color.web(TEXT_SECONDARY));
        labelControl.setPrefWidth(120);

        Label valueControl = new Label(value != null ? value : "Non renseigné");
        valueControl.setFont(Font.font("System", FontWeight.NORMAL, 13));
        valueControl.setTextFill(Color.web(highlight ? ACCENT_COLOR : TEXT_PRIMARY));
        valueControl.setWrapText(true);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 12, 6, 12));
        row.getChildren().addAll(labelControl, valueControl);

        if (highlight) {
            row.setStyle(
                    "-fx-background-color: " + SECONDARY_BG + "; " +
                            "-fx-background-radius: 4px; " +
                            "-fx-border-color: " + ACCENT_COLOR + "; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 4px;");
        } else {
            row.setStyle("-fx-background-color: transparent;");
        }

        HBox.setHgrow(valueControl, Priority.ALWAYS);
        return row;
    }

    /**
     * Crée un bouton d'action stylistique
     */
    private Button createActionButton(String text, String style, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("System", FontWeight.BOLD, 12));
        button.setPrefWidth(120);
        button.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });

        // Styles prédéfinis
        switch (style.toLowerCase()) {
            case "primary":
                button.setStyle(
                        "-fx-background-color: " + ACCENT_COLOR + "; " +
                                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-border-radius: 6px; " +
                                "-fx-cursor: hand;");
                break;
            case "success":
                button.setStyle(
                        "-fx-background-color: #4CAF50; " +
                                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-border-radius: 6px; " +
                                "-fx-cursor: hand;");
                break;
            case "danger":
                button.setStyle(
                        "-fx-background-color: #f44336; " +
                                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-border-radius: 6px; " +
                                "-fx-cursor: hand;");
                break;
            default: // secondary
                button.setStyle(
                        "-fx-background-color: " + SECONDARY_BG + "; " +
                                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-border-color: " + BORDER_COLOR + "; " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 6px; " +
                                "-fx-cursor: hand;");
        }

        // Effet hover
        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));

        return button;
    }

    /**
     * Retourne l'icône correspondant au type d'entité
     */
    private String getEntityIcon(String entityType) {
        switch (entityType.toLowerCase()) {
            case "equipment":
                return "⚙️";
            case "vehicle":
                return "🚐";
            case "personnel":
                return "👤";
            case "client":
                return "🏢";
            case "contract":
                return "📄";
            case "service":
                return "🔧";
            default:
                return "📋";
        }
    }

    /**
     * Retourne le chemin de l'image par défaut selon le type
     */
    private String getDefaultImagePath(String entityType) {
        switch (entityType.toLowerCase()) {
            case "equipment":
                return "/images/default-equipment.png";
            case "vehicle":
                return "/images/default-vehicle.png";
            case "personnel":
                return "/images/default-person.png";
            case "client":
                return "/images/default-client.png";
            default:
                return null;
        }
    }

    /**
     * Méthode utilitaire pour créer une fiche à partir d'une Map
     */
    public static EntityDetailView createFromMap(String entityType, Map<String, Object> data) {
        EntityDetailView detail = new EntityDetailView(entityType);

        // Informations principales
        String title = (String) data.getOrDefault("name", "Sans nom");
        String subtitle = (String) data.getOrDefault("description", "");
        detail.setEntityInfo(title, subtitle, entityType);

        // Image par défaut
        detail.setDefaultImage(entityType);

        // Ajout automatique des champs
        List<String> excludeFields = List.of("id", "name", "description", "image");
        data.forEach((key, value) -> {
            if (!excludeFields.contains(key) && value != null) {
                String formattedKey = formatFieldName(key);
                detail.addInfoRow(formattedKey, value.toString());
            }
        });

        // QR Code si ID disponible
        if (data.containsKey("id")) {
            String qrData = entityType + "_" + data.get("id");
            // detail.generateQRCode(qrData); // TODO: Implement QR codes
        }

        return detail;
    }

    /**
     * Formate le nom d'un champ pour l'affichage
     */
    private static String formatFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1).replaceAll("([A-Z])", " $1");
    }
}