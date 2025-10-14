package com.magsav.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

/**
 * Fabrique de cellules personnalisées pour les tableaux avec support des icônes Mag Scène
 */
public class CustomTableCellFactory {
    
    /**
     * Crée une cellule de tableau qui affiche le type de société avec une icône appropriée
     * Utilise l'icône GIF statique pour Mag Scène
     */
    public static <T> TableCell<T, String> createCompanyTypeCell() {
        return new TableCell<T, String>() {
            private final HBox container = new HBox(5);
            private final ImageView iconView = new ImageView();
            private final Label textLabel = new Label();
            
            {
                container.setAlignment(Pos.CENTER_LEFT);
                iconView.setFitWidth(16);
                iconView.setFitHeight(16);
                container.getChildren().addAll(iconView, textLabel);
            }
            
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                
                if (empty || type == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String displayText;
                    Image icon = null;
                    
                    switch (type) {
                        case "CLIENT" -> {
                            displayText = "👥 Client";
                        }
                        case "MANUFACTURER" -> {
                            displayText = "🏭 Fabricant";
                        }
                        case "SUPPLIER" -> {
                            displayText = "📦 Fournisseur";
                        }
                        case "COLLABORATOR" -> {
                            displayText = "🤝 Collaborateur";
                        }
                        case "PARTICULIER" -> {
                            displayText = "👤 Particulier";
                        }
                        case "OWN_COMPANY" -> {
                            displayText = "Mag Scène";
                            // Essayer de charger l'icône GIF statique
                            icon = GifLogoManager.createMagSceneListIcon(16);
                        }
                        case "ADMINISTRATION" -> {
                            displayText = "🏛️ Administration";
                        }
                        default -> {
                            displayText = type;
                        }
                    }
                    
                    textLabel.setText(displayText);
                    
                    if (icon != null) {
                        iconView.setImage(icon);
                        iconView.setVisible(true);
                        // Pour Mag Scène, ne pas afficher l'émoji dans le texte
                        if ("OWN_COMPANY".equals(type)) {
                            textLabel.getStyleClass().add("mag-scene-text");
                            textLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
                        }
                    } else {
                        iconView.setImage(null);
                        iconView.setVisible(false);
                    }
                    
                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }
    
    /**
     * Crée une cellule de tableau simple avec style personnalisé pour Mag Scène
     * Version allégée sans icône personnalisée mais avec style spécial
     */
    public static <T> TableCell<T, String> createStyledCompanyTypeCell() {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String displayText = switch (type) {
                        case "CLIENT" -> "👥 Client";
                        case "MANUFACTURER" -> "🏭 Fabricant";
                        case "SUPPLIER" -> "📦 Fournisseur";
                        case "COLLABORATOR" -> "🤝 Collaborateur";
                        case "PARTICULIER" -> "👤 Particulier";
                        case "OWN_COMPANY" -> "🏠 Mag Scène";
                        case "ADMINISTRATION" -> "🏛️ Administration";
                        default -> type;
                    };
                    
                    setText(displayText);
                    
                    // Style spécial pour Mag Scène
                    if ("OWN_COMPANY".equals(type)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3; -fx-background-color: #e3f2fd;");
                    } else {
                        setStyle("");
                    }
                }
            }
        };
    }
}