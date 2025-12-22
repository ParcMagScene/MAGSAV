package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.service.MediaService;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.function.Consumer;

/**
 * Composant Avatar réutilisable pour afficher et gérer les avatars
 * - Génère un avatar par défaut avec les initiales
 * - Affiche une photo si disponible
 * - Permet de changer l'avatar via clic droit ou double-clic
 */
public class AvatarView extends StackPane {
    
    private final MediaService mediaService;
    
    // Propriétés de l'avatar
    private String personId;
    private String firstName;
    private String lastName;
    private String avatarPath;
    
    // Composants visuels
    private Circle backgroundCircle;
    private Text initialsText;
    private ImageView photoView;
    private Circle clipCircle;
    
    // Configuration
    private double size = 60;
    private boolean editable = false;
    private Consumer<String> onAvatarChanged;
    
    /**
     * Constructeur par défaut
     */
    public AvatarView() {
        this(60);
    }
    
    /**
     * Constructeur avec taille personnalisée
     */
    public AvatarView(double size) {
        this.size = size;
        this.mediaService = MediaService.getInstance();
        
        initializeComponents();
        setupInteractions();
    }
    
    /**
     * Initialise les composants visuels
     */
    private void initializeComponents() {
        setAlignment(Pos.CENTER);
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        
        // Cercle de fond pour l'avatar par défaut
        backgroundCircle = new Circle(size / 2);
        backgroundCircle.setFill(Color.web("#6B71F2"));
        
        // Texte des initiales
        initialsText = new Text("?");
        initialsText.setFill(Color.WHITE);
        initialsText.setFont(Font.font("Segoe UI", FontWeight.BOLD, size * 0.4));
        
        // ImageView pour la photo
        photoView = new ImageView();
        photoView.setFitWidth(size);
        photoView.setFitHeight(size);
        photoView.setPreserveRatio(true);
        photoView.setSmooth(true);
        photoView.setVisible(false);
        
        // Clip circulaire pour la photo
        clipCircle = new Circle(size / 2);
        clipCircle.setCenterX(size / 2);
        clipCircle.setCenterY(size / 2);
        photoView.setClip(clipCircle);
        
        // Effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(4);
        shadow.setOffsetY(2);
        setEffect(shadow);
        
        // Ajouter les composants
        getChildren().addAll(backgroundCircle, initialsText, photoView);
        
        // Style CSS
        getStyleClass().add("avatar-view");
    }
    
    /**
     * Configure les interactions (clic, menu contextuel)
     */
    private void setupInteractions() {
        // Menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem changePhotoItem = new MenuItem("📷 Changer la photo");
        changePhotoItem.setOnAction(e -> selectNewPhoto());
        
        MenuItem resetAvatarItem = new MenuItem("🔄 Revenir à l'avatar par défaut");
        resetAvatarItem.setOnAction(e -> resetToDefault());
        
        contextMenu.getItems().addAll(changePhotoItem, resetAvatarItem);
        
        // Afficher menu au clic droit si éditable
        setOnContextMenuRequested(e -> {
            if (editable) {
                contextMenu.show(this, e.getScreenX(), e.getScreenY());
            }
        });
        
        // Double-clic pour changer la photo
        setOnMouseClicked(e -> {
            if (editable && e.getClickCount() == 2) {
                selectNewPhoto();
            }
        });
        
        // Curseur main si éditable
        setOnMouseEntered(e -> {
            if (editable) {
                setStyle("-fx-cursor: hand;");
                setOpacity(0.9);
            }
        });
        
        setOnMouseExited(e -> {
            setStyle("");
            setOpacity(1.0);
        });
    }
    
    /**
     * Configure l'avatar pour une personne
     */
    public void setPerson(String personId, String firstName, String lastName, String avatarPath) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarPath = avatarPath;
        
        updateDisplay();
    }
    
    /**
     * Met à jour l'affichage de l'avatar
     */
    private void updateDisplay() {
        // Essayer de charger une photo existante
        if (avatarPath != null && !avatarPath.isEmpty() && !avatarPath.startsWith("default_")) {
            Image photo = mediaService.loadAvatar(avatarPath, size * 2); // *2 pour meilleure qualité
            if (photo != null && !photo.isError()) {
                showPhoto(photo);
                return;
            }
        }
        
        // Sinon afficher l'avatar par défaut avec initiales
        showDefaultAvatar();
    }
    
    /**
     * Affiche une photo
     */
    private void showPhoto(Image photo) {
        photoView.setImage(photo);
        photoView.setVisible(true);
        backgroundCircle.setVisible(false);
        initialsText.setVisible(false);
        
        // Tooltip
        Tooltip.install(this, new Tooltip(getFullName() + "\n(Double-clic pour changer)"));
    }
    
    /**
     * Affiche l'avatar par défaut avec les initiales
     */
    private void showDefaultAvatar() {
        photoView.setVisible(false);
        backgroundCircle.setVisible(true);
        initialsText.setVisible(true);
        
        // Mettre à jour les initiales
        String initials = mediaService.getInitials(firstName, lastName);
        initialsText.setText(initials);
        
        // Mettre à jour la couleur
        String colorHex = mediaService.generateAvatarColor(personId != null ? personId : firstName + lastName);
        backgroundCircle.setFill(Color.web(colorHex));
        
        // Tooltip
        if (editable) {
            Tooltip.install(this, new Tooltip(getFullName() + "\n(Double-clic pour ajouter une photo)"));
        } else {
            Tooltip.install(this, new Tooltip(getFullName()));
        }
    }
    
    /**
     * Ouvre un sélecteur de fichier pour changer la photo
     */
    private void selectNewPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo pour l'avatar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        // Ouvrir dans le dossier Avatars par défaut
        File avatarsDir = mediaService.getAvatarsPath().toFile();
        if (avatarsDir.exists()) {
            fileChooser.setInitialDirectory(avatarsDir);
        }
        
        Window window = getScene() != null ? getScene().getWindow() : null;
        File selectedFile = fileChooser.showOpenDialog(window);
        
        if (selectedFile != null) {
            try {
                // Copier la photo comme avatar
                String newAvatarPath = mediaService.copyPhotoAsAvatar(
                    selectedFile, 
                    personId != null ? personId : "unknown",
                    getFullName()
                );
                
                this.avatarPath = newAvatarPath;
                updateDisplay();
                
                // Notifier le changement
                if (onAvatarChanged != null) {
                    onAvatarChanged.accept(newAvatarPath);
                }
                
                System.out.println("👤 Avatar changé: " + newAvatarPath);
                
            } catch (Exception e) {
                System.err.println("Erreur changement avatar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Revient à l'avatar par défaut (initiales)
     */
    private void resetToDefault() {
        // Supprimer l'avatar photo s'il existe
        if (avatarPath != null && !avatarPath.startsWith("default_")) {
            mediaService.deleteAvatar(avatarPath);
        }
        
        // Générer un nouvel avatar par défaut
        this.avatarPath = mediaService.generateDefaultAvatar(
            personId != null ? personId : "unknown",
            firstName,
            lastName
        );
        
        updateDisplay();
        
        // Notifier le changement
        if (onAvatarChanged != null) {
            onAvatarChanged.accept(avatarPath);
        }
        
        System.out.println("👤 Avatar réinitialisé aux initiales");
    }
    
    /**
     * Retourne le nom complet
     */
    private String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            name.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName);
        }
        return name.length() > 0 ? name.toString() : "Inconnu";
    }
    
    // ========================================
    // ACCESSEURS ET MUTATEURS
    // ========================================
    
    public void setSize(double size) {
        this.size = size;
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        backgroundCircle.setRadius(size / 2);
        photoView.setFitWidth(size);
        photoView.setFitHeight(size);
        clipCircle.setRadius(size / 2);
        clipCircle.setCenterX(size / 2);
        clipCircle.setCenterY(size / 2);
        initialsText.setFont(Font.font("Segoe UI", FontWeight.BOLD, size * 0.4));
        updateDisplay();
    }
    
    public double getAvatarSize() {
        return size;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setOnAvatarChanged(Consumer<String> callback) {
        this.onAvatarChanged = callback;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
        updateDisplay();
    }
    
    /**
     * Crée un avatar simple pour affichage dans une liste (non éditable)
     */
    public static AvatarView createListAvatar(String firstName, String lastName, String avatarPath) {
        AvatarView avatar = new AvatarView(32);
        avatar.setPerson(null, firstName, lastName, avatarPath);
        avatar.setEditable(false);
        return avatar;
    }
    
    /**
     * Crée un avatar pour un formulaire (éditable)
     */
    public static AvatarView createFormAvatar(String personId, String firstName, String lastName, String avatarPath) {
        AvatarView avatar = new AvatarView(80);
        avatar.setPerson(personId, firstName, lastName, avatarPath);
        avatar.setEditable(true);
        return avatar;
    }
    
    /**
     * Crée un grand avatar pour un profil
     */
    public static AvatarView createProfileAvatar(String personId, String firstName, String lastName, String avatarPath) {
        AvatarView avatar = new AvatarView(120);
        avatar.setPerson(personId, firstName, lastName, avatarPath);
        avatar.setEditable(true);
        return avatar;
    }
}
