package com.magscene.magsav.desktop.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Fiche de détail en lecture seule pour toutes les entités
 * Thème sombre unifié #142240/#142240/#8B91FF
 * Ouverture par défaut en lecture seule, sans onglets
 */
public class EntityReadOnlyDetailView extends Stage {
    
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
    private ScrollPane scrollPane;
    private VBox contentContainer;
    
    // Header
    private Label titleLabel;
    private Label subtitleLabel;
    private Label statusLabel;
    
    // Sections
    private VBox imageSection;
    private VBox infoSection;
    private VBox actionSection;
    
    // Images
    private ImageView mainImageView;
    
    // Containers dynamiques
    private VBox dynamicInfoContainer;
    private HBox actionButtonsContainer;
    
    // Mode édition
    private boolean isEditMode = false;
    
    public EntityReadOnlyDetailView(String entityType, Map<String, Object> data) {
        setupWindow();
        initializeComponents();
        setupLayout();
        setupStyling();
        populateData(entityType, data);
        
        // Par défaut en lecture seule
        setReadOnlyMode();
    }
    
    private void setupWindow() {
        setTitle("Détails");
        setWidth(600);
        setHeight(800);
        setResizable(true);
        initModality(Modality.APPLICATION_MODAL);
        
        // Centrage sur l'écran parent
        setX((javafx.stage.Screen.getPrimary().getBounds().getWidth() - 600) / 2);
        setY((javafx.stage.Screen.getPrimary().getBounds().getHeight() - 800) / 2);
    }
    
    private void initializeComponents() {
        // Container principal
        mainContainer = new VBox();
        
        // Header avec titre principal
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(TEXT_PRIMARY));
        
        subtitleLabel = new Label();
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web(TEXT_SECONDARY));
        
        statusLabel = new Label();
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web(ACCENT_COLOR));
        statusLabel.setStyle(
            "-fx-background-color: " + SECONDARY_BG + "; " +
            "-fx-padding: 4px 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-color: " + ACCENT_COLOR + "; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 12px;"
        );
        
        // Image principale
        mainImageView = new ImageView();
        mainImageView.setFitWidth(120);
        mainImageView.setFitHeight(120);
        mainImageView.setPreserveRatio(true);
        mainImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(139,145,255,0.3), 10, 0, 0, 5);");
        
        // Container pour informations dynamiques
        dynamicInfoContainer = new VBox(8);
        
        // Container pour boutons d'action (lecture seule par défaut)
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
        headerContainer.getChildren().addAll(headerTexts, headerSpacer, statusLabel);
        
        // Image section
        imageSection = new VBox(15);
        imageSection.setAlignment(Pos.CENTER);
        imageSection.setPadding(new Insets(20));
        imageSection.getChildren().add(mainImageView);
        
        // Info section avec scrolling
        Label infoTitle = createSectionTitle("📋 Informations");
        
        infoSection = new VBox(12);
        infoSection.setPadding(new Insets(0, 20, 20, 20));
        infoSection.getChildren().addAll(infoTitle, dynamicInfoContainer);
        
        // Action section (boutons)
        setupActionButtons();
        actionSection = new VBox(15);
        actionSection.setAlignment(Pos.CENTER);
        actionSection.setPadding(new Insets(20));
        actionSection.getChildren().add(actionButtonsContainer);
        
        // Container de contenu principal
        contentContainer = new VBox();
        contentContainer.getChildren().addAll(imageSection, infoSection, actionSection);
        
        // ScrollPane pour le contenu
        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Assembly final
        mainContainer.getChildren().addAll(headerContainer, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Scene
        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/styles/entity-detail.css").toExternalForm());
        setScene(scene);
    }
    
    private void setupStyling() {
        // Style principal
        mainContainer.setStyle("-fx-background-color: " + PRIMARY_BG + ";");
        
        // Style du header
        headerContainer.setStyle(
            "-fx-background-color: " + SECONDARY_BG + "; " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 0 0 1px 0;"
        );
        
        // Style du contenu
        contentContainer.setStyle("-fx-background-color: " + PRIMARY_BG + ";");
        
        // Style ScrollPane
        scrollPane.setStyle(
            "-fx-background: " + PRIMARY_BG + "; " +
            "-fx-background-color: " + PRIMARY_BG + ";"
        );
    }
    
    private void setupActionButtons() {
        // Bouton Modifier (mode lecture)
        Button editButton = new Button("✏️ Modifier");
        editButton.getStyleClass().addAll("entity-action-button", "edit-button");
        editButton.setOnAction(e -> toggleEditMode());
        
        // Bouton Fermer
        Button closeButton = new Button("✕ Fermer");
        closeButton.getStyleClass().addAll("entity-action-button", "close-button");
        closeButton.setOnAction(e -> close());
        
        // Bouton Sauvegarder (mode édition - initialement caché)
        Button saveButton = new Button("💾 Sauvegarder");
        saveButton.getStyleClass().addAll("entity-action-button", "save-button");
        saveButton.setOnAction(e -> saveChanges());
        saveButton.setVisible(false);
        
        // Bouton Annuler (mode édition - initialement caché)
        Button cancelButton = new Button("🔄 Annuler");
        cancelButton.getStyleClass().addAll("entity-action-button", "cancel-button");
        cancelButton.setOnAction(e -> cancelEdit());
        cancelButton.setVisible(false);
        
        actionButtonsContainer.getChildren().addAll(editButton, closeButton, saveButton, cancelButton);
    }
    
    private Label createSectionTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        title.setPadding(new Insets(8, 0, 8, 0));
        title.setStyle(
            "-fx-border-color: " + ACCENT_COLOR + "; " +
            "-fx-border-width: 0 0 2px 0;"
        );
        return title;
    }
    
    /**
     * Remplit la fiche avec les données de l'entité
     */
    private void populateData(String entityType, Map<String, Object> data) {
        // Informations principales
        String title = (String) data.getOrDefault("name", "Sans nom");
        String subtitle = (String) data.getOrDefault("description", "");
        String status = (String) data.getOrDefault("status", entityType.toUpperCase());
        
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        statusLabel.setText(status);
        
        // Image par défaut selon le type
        setDefaultImage(entityType);
        
        // Ajout des champs d'information
        addInfoFields(data);
        
        // Titre de la fenêtre
        setTitle("Détails - " + title);
    }
    
    /**
     * Ajoute tous les champs d'information de l'entité
     */
    private void addInfoFields(Map<String, Object> data) {
        List<String> excludeFields = List.of("id", "name", "description", "status", "image");
        
        data.entrySet().stream()
            .filter(entry -> !excludeFields.contains(entry.getKey()))
            .filter(entry -> entry.getValue() != null)
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String label = formatFieldName(entry.getKey());
                String value = formatFieldValue(entry.getValue());
                addInfoRow(label, value, false);
            });
    }
    
    /**
     * Ajoute une ligne d'information
     */
    private void addInfoRow(String label, String value, boolean highlight) {
        Label labelControl = new Label(label + ":");
        labelControl.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelControl.setTextFill(Color.web(TEXT_SECONDARY));
        labelControl.setPrefWidth(150);
        
        Label valueControl = new Label(value != null ? value : "Non renseigné");
        valueControl.setFont(Font.font("System", FontWeight.NORMAL, 13));
        valueControl.setTextFill(Color.web(highlight ? ACCENT_COLOR : TEXT_PRIMARY));
        valueControl.setWrapText(true);
        
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 15, 8, 15));
        row.getChildren().addAll(labelControl, valueControl);
        
        if (highlight) {
            row.setStyle(
                "-fx-background-color: " + SECONDARY_BG + "; " +
                "-fx-background-radius: 6px; " +
                "-fx-border-color: " + ACCENT_COLOR + "; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 6px;"
            );
        } else {
            row.setStyle(
                "-fx-background-color: rgba(20, 34, 64, 0.3); " +
                "-fx-background-radius: 4px;"
            );
        }
        
        HBox.setHgrow(valueControl, Priority.ALWAYS);
        dynamicInfoContainer.getChildren().add(row);
    }
    
    /**
     * Définit l'image par défaut selon le type d'entité
     */
    private void setDefaultImage(String entityType) {
        String imagePath = getDefaultImagePath(entityType);
        try {
            if (imagePath != null) {
                Image defaultImage = new Image(getClass().getResourceAsStream(imagePath));
                mainImageView.setImage(defaultImage);
            } else {
                // Image générique ou icône
                mainImageView.setVisible(false);
            }
        } catch (Exception e) {
            // En cas d'erreur, masquer l'image
            mainImageView.setVisible(false);
        }
    }
    
    /**
     * Active/désactive le mode édition
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateButtonsVisibility();
        
        if (isEditMode) {
            setEditMode();
        } else {
            setReadOnlyMode();
        }
    }
    
    /**
     * Passe en mode lecture seule
     */
    private void setReadOnlyMode() {
        isEditMode = false;
        updateButtonsVisibility();
        // Ici on pourrait désactiver les champs éditables
    }
    
    /**
     * Passe en mode édition
     */
    private void setEditMode() {
        isEditMode = true;
        updateButtonsVisibility();
        // Ici on pourrait activer les champs éditables
    }
    
    /**
     * Met à jour la visibilité des boutons selon le mode
     */
    private void updateButtonsVisibility() {
        actionButtonsContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String text = btn.getText();
                
                if (text.contains("Modifier") || text.contains("Fermer")) {
                    btn.setVisible(!isEditMode);
                } else if (text.contains("Sauvegarder") || text.contains("Annuler")) {
                    btn.setVisible(isEditMode);
                }
            }
        });
    }
    
    /**
     * Sauvegarde les modifications
     */
    private void saveChanges() {
        // TODO: Implémenter la sauvegarde
        System.out.println("Sauvegarde des modifications...");
        setReadOnlyMode();
    }
    
    /**
     * Annule l'édition
     */
    private void cancelEdit() {
        // TODO: Restaurer les valeurs originales
        System.out.println("Annulation des modifications...");
        setReadOnlyMode();
    }
    
    /**
     * Retourne le chemin de l'image par défaut selon le type
     */
    private String getDefaultImagePath(String entityType) {
        switch (entityType.toLowerCase()) {
            case "equipment": return "/images/default-equipment.png";
            case "vehicle": return "/images/default-vehicle.png";
            case "personnel": return "/images/default-person.png";
            case "client": return "/images/default-client.png";
            case "contract": return "/images/default-contract.png";
            case "service": return "/images/default-service.png";
            default: return null;
        }
    }
    
    /**
     * Formate le nom d'un champ pour l'affichage
     */
    private String formatFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + 
               fieldName.substring(1).replaceAll("([A-Z])", " $1");
    }
    
    /**
     * Formate la valeur d'un champ pour l'affichage
     */
    private String formatFieldValue(Object value) {
        if (value == null) return "Non renseigné";
        if (value instanceof String) return (String) value;
        if (value instanceof Number) return value.toString();
        if (value instanceof Boolean) return ((Boolean) value) ? "Oui" : "Non";
        return value.toString();
    }
    
    /**
     * Méthode statique pour créer et afficher une fiche de détail
     */
    public static void showDetailView(String entityType, Map<String, Object> data) {
        EntityReadOnlyDetailView detailView = new EntityReadOnlyDetailView(entityType, data);
        detailView.show();
    }
}