package com.magsav.gui.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire CSS centralisé pour MAGSAV
 * Simplifie l'application des styles et assure la cohérence visuelle
 */
public class CSSManager {
    
    // Constantes de couleurs du thème dark
    public static final String PRIMARY_BACKGROUND = "#1e3a5f";
    public static final String SECONDARY_BACKGROUND = "#1a1a1a";
    public static final String ACCENT_BACKGROUND = "#2c5282";
    public static final String BORDER_COLOR = "#333333";
    public static final String TEXT_PRIMARY = "#ffffff";
    public static final String TEXT_SECONDARY = "#cccccc";
    public static final String ACCENT_COLOR = "#4a90e2";
    public static final String SUCCESS_COLOR = "#4CAF50";
    public static final String ERROR_COLOR = "#dc3545";
    public static final String WARNING_COLOR = "#ffc107";
    
    // Fichier CSS unifié consolidé
    private static final String MAIN_CSS = "/css/simple-dark.css";
    
    // Instance singleton
    private static CSSManager instance;
    private Map<String, String> dynamicStyles = new HashMap<>();
    
    private CSSManager() {}
    
    public static CSSManager getInstance() {
        if (instance == null) {
            instance = new CSSManager();
        }
        return instance;
    }
    
    /**
     * Applique le thème complet à une Scene
     */
    public void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        try {
            URL cssResource = getClass().getResource(MAIN_CSS);
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
                System.out.println("CSS chargé avec succès: " + MAIN_CSS);
            } else {
                System.err.println("ERREUR: CSS non trouvé: " + MAIN_CSS);
                // Fallback : appliquer des styles de base
                applyBasicStyles(scene);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement CSS: " + e.getMessage());
            applyBasicStyles(scene);
        }
    }
    
    /**
     * Applique le thème à un Stage (applique automatiquement à sa Scene)
     */
    public void applyTheme(Stage stage) {
        if (stage.getScene() != null) {
            applyTheme(stage.getScene());
        }
    }
    
    /**
     * Ajoute les classes CSS appropriées à un Node selon son type
     */
    public void applyComponentStyle(Node node, String... additionalClasses) {
        node.getStyleClass().removeIf(cls -> cls.startsWith("css-"));
        
        // Classes automatiques selon le type de composant
        String baseClass = getBaseClassForNode(node);
        if (baseClass != null) {
            node.getStyleClass().add(baseClass);
        }
        
        // Classes additionnelles
        if (additionalClasses != null) {
            Arrays.stream(additionalClasses)
                .filter(cls -> cls != null && !cls.trim().isEmpty())
                .forEach(cls -> node.getStyleClass().add(cls));
        }
    }
    
    /**
     * Style pour les labels de titre
     */
    public void styleTitle(Node node) {
        applyComponentStyle(node, "title-label");
    }
    
    /**
     * Style pour les labels de sous-titre
     */
    public void styleSubtitle(Node node) {
        applyComponentStyle(node, "subtitle-label");
    }
    
    /**
     * Style pour les boutons primaires
     */
    public void stylePrimaryButton(Node node) {
        applyComponentStyle(node, "btn-primary");
    }
    
    /**
     * Style pour les boutons secondaires
     */
    public void styleSecondaryButton(Node node) {
        applyComponentStyle(node, "btn-secondary");
    }
    
    /**
     * Style pour les boutons de danger
     */
    public void styleDangerButton(Node node) {
        applyComponentStyle(node, "btn-danger");
    }
    
    /**
     * Style pour les conteneurs de préférences
     */
    public void stylePreferencesContainer(Node node) {
        applyComponentStyle(node, "preferences-container");
    }
    
    /**
     * Style pour les sections de préférences
     */
    public void stylePreferencesSection(Node node) {
        applyComponentStyle(node, "preferences-section");
    }
    
    /**
     * Style pour les labels de statut (succès)
     */
    public void styleSuccessLabel(Node node) {
        applyComponentStyle(node, "status-success");
    }
    
    /**
     * Style pour les labels de statut (erreur)
     */
    public void styleErrorLabel(Node node) {
        applyComponentStyle(node, "status-error");
    }
    
    /**
     * Style pour les cartes de dashboard
     */
    public void styleDashboardCard(Node node) {
        applyComponentStyle(node, "dashboard-card");
    }
    
    /**
     * Style pour les séparateurs
     */
    public void styleSeparator(Node node) {
        applyComponentStyle(node, "separator");
    }
    
    /**
     * Détermine la classe CSS de base selon le type de Node
     */
    private String getBaseClassForNode(Node node) {
        String className = node.getClass().getSimpleName().toLowerCase();
        
        switch (className) {
            case "label":
                return "css-label";
            case "button":
                return "css-button";
            case "textfield":
                return "css-textfield";
            case "textarea":
                return "css-textarea";
            case "combobox":
                return "css-combobox";
            case "checkbox":
                return "css-checkbox";
            case "radiobutton":
                return "css-radiobutton";
            case "vbox":
                return "css-vbox";
            case "hbox":
                return "css-hbox";
            case "scrollpane":
                return "css-scrollpane";
            case "tabpane":
                return "css-tabpane";
            case "tab":
                return "css-tab";
            case "tableview":
                return "css-tableview";
            case "treeview":
                return "css-treeview";
            default:
                return null;
        }
    }
    
    /**
     * Applique un style personnalisé avec couleur dynamique
     */
    public void applyCustomStyle(Node node, String property, String value) {
        String styleKey = node.getId() + "-" + property;
        String style = String.format("-%s: %s;", property, value);
        dynamicStyles.put(styleKey, style);
        
        // Applique le style immédiatement
        String currentStyle = node.getStyle();
        if (currentStyle == null) currentStyle = "";
        
        // Supprime l'ancien style pour cette propriété s'il existe
        currentStyle = currentStyle.replaceAll("-" + property + ":[^;]*;", "");
        
        // Ajoute le nouveau style
        node.setStyle(currentStyle + style);
    }
    
    /**
     * Applique une couleur de texte
     */
    public void setTextColor(Node node, String color) {
        applyCustomStyle(node, "fx-text-fill", color);
    }
    
    /**
     * Applique une couleur de fond
     */
    public void setBackgroundColor(Node node, String color) {
        applyCustomStyle(node, "fx-background-color", color);
    }
    
    /**
     * Applique une couleur de bordure
     */
    public void setBorderColor(Node node, String color) {
        applyCustomStyle(node, "fx-border-color", color);
    }
    
    /**
     * Nettoie tous les styles inline et ne garde que les classes CSS
     */
    public void cleanInlineStyles(Parent parent) {
        cleanNodeStyles(parent);
        parent.getChildrenUnmodifiable().forEach(child -> {
            if (child instanceof Parent) {
                cleanInlineStyles((Parent) child);
            } else {
                cleanNodeStyles(child);
            }
        });
    }
    
    private void cleanNodeStyles(Node node) {
        // Supprime les styles inline
        node.setStyle("");
        
        // Applique les classes appropriées
        applyComponentStyle(node);
    }
    
    /**
     * Initialise le système CSS pour une nouvelle fenêtre
     */
    public void initializeWindow(Stage stage, String windowType) {
        // Applique le thème de base
        applyTheme(stage);
        
        // Ajoute une classe spécifique au type de fenêtre
        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            stage.getScene().getRoot().getStyleClass().add("window-" + windowType.toLowerCase());
        }
    }
    
    /**
     * Configuration des couleurs des onglets (pour les préférences)
     */
    public void configureTabColors(String defaultColor, String selectedColor) {
        dynamicStyles.put("tab-default-color", defaultColor);
        dynamicStyles.put("tab-selected-color", selectedColor);
        
        // Application immédiate à toutes les fenêtres ouvertes
        applyTabColorsToAllWindows(defaultColor, selectedColor);
    }
    
    /**
     * Applique les couleurs d'onglets à toutes les fenêtres ouvertes
     */
    private void applyTabColorsToAllWindows(String defaultColor, String selectedColor) {
        try {
            // Récupère toutes les fenêtres JavaFX ouvertes
            javafx.stage.Window.getWindows().forEach(window -> {
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;
                    if (stage.getScene() != null) {
                        applyTabColorsToScene(stage.getScene(), defaultColor, selectedColor);
                    }
                }
            });
        } catch (Exception e) {
            // Log silencieux pour éviter les erreurs dans l'UI
        }
    }
    
    /**
     * Applique les couleurs d'onglets à une scène spécifique
     */
    private void applyTabColorsToScene(javafx.scene.Scene scene, String defaultColor, String selectedColor) {
        if (scene.getRoot() != null) {
            // Ajoute la classe pour les couleurs personnalisées
            scene.getRoot().getStyleClass().add("custom-tab-colors");
            
            // Applique les styles directement aux TabPanes
            applyTabColorsToNode(scene.getRoot(), defaultColor, selectedColor);
        }
    }
    
    /**
     * Applique récursivement les couleurs d'onglets aux nodes
     */
    private void applyTabColorsToNode(javafx.scene.Node node, String defaultColor, String selectedColor) {
        if (node instanceof javafx.scene.control.TabPane) {
            javafx.scene.control.TabPane tabPane = (javafx.scene.control.TabPane) node;
            
            // Applique les styles aux onglets avec priorité maximale
            tabPane.getTabs().forEach(tab -> {
                // Style par défaut avec !important pour forcer l'application
                String baseStyle = String.format(
                    "-fx-background-color: %s !important; -fx-text-fill: #ffffff !important; " +
                    "-fx-background-radius: 0 !important; -fx-border-radius: 0 !important;", 
                    defaultColor
                );
                tab.setStyle(baseStyle);
                
                // Écoute les changements de sélection
                tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                    if (oldTab != null) {
                        oldTab.setStyle(baseStyle);
                    }
                    if (newTab != null) {
                        String selectedStyle = String.format(
                            "-fx-background-color: %s !important; -fx-text-fill: #ffffff !important; " +
                            "-fx-background-radius: 0 !important; -fx-border-radius: 0 !important;", 
                            selectedColor
                        );
                        newTab.setStyle(selectedStyle);
                    }
                });
                
                // Applique le style sélectionné si c'est l'onglet actuel
                if (tab == tabPane.getSelectionModel().getSelectedItem()) {
                    String selectedStyle = String.format(
                        "-fx-background-color: %s !important; -fx-text-fill: #ffffff !important; " +
                        "-fx-background-radius: 0 !important; -fx-border-radius: 0 !important;", 
                        selectedColor
                    );
                    tab.setStyle(selectedStyle);
                }
            });
        }
        
        // Recherche récursive dans les enfants
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            parent.getChildrenUnmodifiable().forEach(child -> 
                applyTabColorsToNode(child, defaultColor, selectedColor)
            );
        }
    }
    
    /**
     * Réinitialise tous les styles aux valeurs par défaut
     */
    public void resetToDefaults() {
        dynamicStyles.clear();
    }
    
    /**
     * Applique des styles de base en cas d'échec du chargement CSS
     */
    private void applyBasicStyles(Scene scene) {
        // Styles de base en dur pour fallback
        String basicStyles = """
            .root {
                -fx-base: #1e3a5f;
                -fx-background: #1e3a5f;
                -fx-text-fill: #ffffff;
            }
            .sidebar {
                -fx-background-color: #1e3a5f;
            }
            .tab-pane .tab {
                -fx-background-color: #1e3a5f;
                -fx-text-fill: #ffffff;
            }
            .tab-pane .tab:selected {
                -fx-background-color: #666666;
            }
            """;
        
        // Création d'une stylesheet temporaire en mémoire
        scene.getRoot().setStyle(basicStyles);
        System.out.println("Styles de base appliqués en fallback");
    }
}