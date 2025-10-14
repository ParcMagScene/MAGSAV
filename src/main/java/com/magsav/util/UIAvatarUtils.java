package com.magsav.util;

import com.magsav.service.AvatarService;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

/**
 * Utilitaires pour créer des composants d'interface avec avatars et images par défaut
 */
public class UIAvatarUtils {
    
    private static final AvatarService avatarService = AvatarService.getInstance();
    
    /**
     * Crée un composant utilisateur avec avatar et nom
     */
    public static HBox createUserComponent(String username, String fullName, double avatarSize) {
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar
        ImageView avatar = avatarService.createAvatarImageView(username, avatarSize);
        
        // Labels
        VBox textBox = new VBox(2);
        Label nameLabel = new Label(fullName != null ? fullName : username);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label usernameLabel = new Label("@" + username);
        usernameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        textBox.getChildren().addAll(nameLabel, usernameLabel);
        userBox.getChildren().addAll(avatar, textBox);
        
        return userBox;
    }
    
    /**
     * Crée un composant utilisateur compact (juste avatar + nom)
     */
    public static HBox createCompactUserComponent(String username, double avatarSize) {
        HBox userBox = new HBox(6);
        userBox.setAlignment(Pos.CENTER_LEFT);
        
        ImageView avatar = avatarService.createAvatarImageView(username, avatarSize);
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 11px;");
        
        userBox.getChildren().addAll(avatar, nameLabel);
        return userBox;
    }
    
    /**
     * Crée un composant produit avec image par défaut si nécessaire
     */
    public static HBox createProductComponent(String productName, String imagePath, double imageSize) {
        HBox productBox = new HBox(8);
        productBox.setAlignment(Pos.CENTER_LEFT);
        
        // Image du produit ou image par défaut
        ImageView productImage = avatarService.createImageViewOrDefault(
            imagePath, 
            AvatarService.DefaultImageType.PRODUCT_IMAGE, 
            imageSize, 
            imageSize
        );
        
        Label nameLabel = new Label(productName);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        productBox.getChildren().addAll(productImage, nameLabel);
        return productBox;
    }
    
    /**
     * Crée un composant entreprise avec logo par défaut si nécessaire
     */
    public static HBox createCompanyComponent(String companyName, String logoPath, double logoSize) {
        HBox companyBox = new HBox(8);
        companyBox.setAlignment(Pos.CENTER_LEFT);
        
        // Logo de l'entreprise ou logo par défaut
        ImageView companyLogo = avatarService.createImageViewOrDefault(
            logoPath, 
            AvatarService.DefaultImageType.COMPANY_LOGO, 
            logoSize, 
            logoSize
        );
        
        Label nameLabel = new Label(companyName);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        companyBox.getChildren().addAll(companyLogo, nameLabel);
        return companyBox;
    }
    
    /**
     * Met à jour un ImageView existant avec une image ou l'image par défaut
     */
    public static void setImageOrDefault(ImageView imageView, String imagePath, 
                                       AvatarService.DefaultImageType defaultType, 
                                       double width, double height) {
        imageView.setImage(avatarService.loadImageOrDefault(imagePath, defaultType));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }
    
    /**
     * Crée un sélecteur d'avatar pour un utilisateur
     */
    public static VBox createAvatarSelector(String currentUsername, AvatarChangeCallback callback) {
        VBox selector = new VBox(10);
        selector.setAlignment(Pos.CENTER);
        
        Label title = new Label("Choisir un avatar :");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        HBox avatarsBox = new HBox(10);
        avatarsBox.setAlignment(Pos.CENTER);
        
        // Afficher tous les avatars disponibles
        for (int i = 0; i < avatarService.getAvatarCount(); i++) {
            final int avatarIndex = i;
            ImageView avatar = avatarService.createAvatarImageView((long) i, 48);
            
            // Style de sélection
            avatar.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            
            // Gestionnaire de clic
            avatar.setOnMouseClicked(e -> {
                if (callback != null) {
                    callback.onAvatarChanged(avatarIndex);
                }
                // Mettre en surbrillance l'avatar sélectionné
                avatarsBox.getChildren().forEach(node -> {
                    if (node instanceof ImageView) {
                        node.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                    }
                });
                avatar.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,100,255,0.6), 8, 0, 0, 3);");
            });
            
            avatarsBox.getChildren().add(avatar);
        }
        
        selector.getChildren().addAll(title, avatarsBox);
        return selector;
    }
    
    /**
     * Interface callback pour les changements d'avatar
     */
    @FunctionalInterface
    public interface AvatarChangeCallback {
        void onAvatarChanged(int avatarIndex);
    }
}