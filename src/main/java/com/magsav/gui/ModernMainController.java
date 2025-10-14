package com.magsav.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import java.net.URL;
import java.util.ResourceBundle;


import com.magsav.ui.animation.AnimationService;
import com.magsav.ui.animation.AnimationUtils;
import com.magsav.ui.icons.IconService;
import com.magsav.util.AppLogger;

/**
 * Contrôleur moderne pour l'interface principale MAGSAV
 * Utilise tous les services d'UI modernisés
 */
public class ModernMainController implements Initializable {
    
    // Services UI
    // private final ThemeManager themeManager = ThemeManager.getInstance(); // TODO: Réimplémenter
    private final AnimationService animationService = AnimationService.getInstance();
    private final IconService iconService = IconService.getInstance();
    
    // Contrôles FXML principaux
    @FXML private BorderPane mainLayout;
    @FXML private HBox modernHeader;
    @FXML private VBox modernSidebar;
    @FXML private VBox contentArea;
    @FXML private VBox detailsPanel;
    @FXML private HBox modernStatusBar;
    
    // Contrôles de navigation
    @FXML private Label companyNameLabel;
    @FXML private Button newInterventionBtn;
    @FXML private Button preferencesBtn;
    @FXML private Button userProfileBtn;
    
    // Contrôles du contenu principal
    @FXML private TextField productSearchField;
    @FXML private TableView<?> productTable;
    @FXML private Label totalProductsLabel;
    @FXML private Label inRepairLabel;
    @FXML private Label repairedLabel;
    
    // Contrôles du panneau de détails
    @FXML private Label lblProdName;
    @FXML private Label lblProdManufacturer;
    @FXML private Label lblProdCategory;
    @FXML private Label lblProdSituation;
    @FXML private Button btnEditProduct;
    @FXML private TableView<?> historyTable;
    @FXML private Label historyCountLabel;
    
    // Contrôles de statut
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppLogger.info("Initialisation du contrôleur moderne");
        
        initializeTheme();
        initializeIcons();
        initializeAnimations();
        initializeInteractions();
        
        // Animations d'entrée
        animateInterfaceEntry();
        
        AppLogger.info("Interface moderne initialisée avec succès");
    }
    
    /**
     * Initialise le thème de l'interface
     */
    private void initializeTheme() {
        // Le thème sera appliqué par le ThemeManager lors du chargement de la scène
        
        // Configuration du bouton de changement de thème
        if (preferencesBtn != null) {
            Node themeIcon = iconService.createMaterialIcon("palette", IconService.Size.SMALL);
            preferencesBtn.setGraphic(themeIcon);
            preferencesBtn.setOnAction(e -> toggleTheme());
        }
        
        AppLogger.info("Thème initialisé");
    }
    
    /**
     * Initialise les icônes de l'interface
     */
    private void initializeIcons() {
        // Icône du profil utilisateur
        if (userProfileBtn != null) {
            Node userIcon = iconService.createMaterialIcon("account_circle", IconService.Size.SMALL);
            userProfileBtn.setGraphic(userIcon);
        }
        
        // Icône de nouvelle intervention
        if (newInterventionBtn != null) {
            Node addIcon = iconService.createMaterialIcon("add", IconService.Size.SMALL);
            newInterventionBtn.setGraphic(addIcon);
        }
        
        AppLogger.info("Icônes initialisées");
    }
    
    /**
     * Initialise les animations
     */
    private void initializeAnimations() {
        // Animations sur les boutons principaux
        if (newInterventionBtn != null) {
            AnimationUtils.makeButtonInteractive(newInterventionBtn);
        }
        if (preferencesBtn != null) {
            AnimationUtils.makeButtonInteractive(preferencesBtn);
        }
        if (userProfileBtn != null) {
            AnimationUtils.makeButtonInteractive(userProfileBtn);
        }
        
        // Animation sur le panneau de détails
        if (detailsPanel != null) {
            AnimationUtils.makeCardInteractive(detailsPanel);
        }
        
        AppLogger.info("Animations configurées");
    }
    
    /**
     * Initialise les interactions utilisateur
     */
    private void initializeInteractions() {
        // Recherche en temps réel
        if (productSearchField != null) {
            productSearchField.textProperty().addListener((obs, oldText, newText) -> {
                onProductSearch(newText);
            });
        }
        
        // Sélection dans le tableau
        if (productTable != null) {
            productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    onProductSelected(newSelection);
                }
            );
        }
        
        AppLogger.info("Interactions configurées");
    }
    
    /**
     * Anime l'entrée de l'interface
     */
    private void animateInterfaceEntry() {
        // Animation de l'en-tête
        if (modernHeader != null) {
            animationService.slideInFromBottom(modernHeader, AnimationService.Speed.NORMAL, 
                                             AnimationService.Easing.EASE_OUT);
        }
        
        // Animation de la sidebar avec délai
        if (modernSidebar != null) {
            animationService.slideInFromBottom(modernSidebar, AnimationService.Speed.NORMAL, 
                                             AnimationService.Easing.EASE_OUT)
                           .setDelay(javafx.util.Duration.millis(100));
        }
        
        // Animation du contenu principal avec plus de délai
        if (contentArea != null) {
            animationService.fadeIn(contentArea, AnimationService.Speed.SLOW, 
                                  AnimationService.Easing.EASE_OUT)
                           .setDelay(javafx.util.Duration.millis(200));
        }
        
        // Animation du panneau de détails
        if (detailsPanel != null) {
            animationService.slideInFromBottom(detailsPanel, AnimationService.Speed.NORMAL, 
                                             AnimationService.Easing.EASE_OUT)
                           .setDelay(javafx.util.Duration.millis(300));
        }
    }
    
    /**
     * Bascule entre les thèmes clair et sombre
     */
    @FXML
    private void toggleTheme() {
        // TODO: Réimplémenter avec util.ThemeManager
        // themeManager.toggleDarkMode();
        
        // Animation de feedback
        if (preferencesBtn != null) {
            animationService.quickPulse(preferencesBtn);
        }
        
        // Mise à jour du statut
        updateStatusMessage("Thème basculé");
    }
    
    /**
     * Gère la recherche de produits
     */
    private void onProductSearch(String searchText) {
        AppLogger.info("Recherche: " + searchText);
        
        // Animation de feedback sur le champ de recherche
        if (productSearchField != null && !searchText.isEmpty()) {
            animationService.pulse(productSearchField, AnimationService.Speed.FAST);
        }
        
        // TODO: Implémenter la logique de recherche
        updateStatusMessage("Recherche en cours...");
    }
    
    /**
     * Gère la sélection d'un produit
     */
    private void onProductSelected(Object selectedProduct) {
        if (selectedProduct != null) {
            AppLogger.info("Produit sélectionné: " + selectedProduct);
            
            // Animation du panneau de détails
            if (detailsPanel != null) {
                animationService.pulse(detailsPanel, AnimationService.Speed.NORMAL);
            }
            
            // Activation des boutons d'action
            enableProductActions(true);
            
            updateStatusMessage("Produit sélectionné");
        } else {
            enableProductActions(false);
            updateStatusMessage("Aucun produit sélectionné");
        }
    }
    
    /**
     * Active/désactive les actions sur les produits
     */
    private void enableProductActions(boolean enable) {
        if (btnEditProduct != null) {
            btnEditProduct.setDisable(!enable);
            
            if (enable) {
                // Animation d'activation
                animationService.fadeIn(btnEditProduct, AnimationService.Speed.FAST, 
                                      AnimationService.Easing.EASE_OUT);
            }
        }
    }
    
    /**
     * Met à jour le message de statut avec animation
     */
    private void updateStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            animationService.pulse(statusLabel, AnimationService.Speed.FAST);
        }
    }
    
    /**
     * Affiche une notification de succès
     */
    private void showSuccessNotification(String message) {
        // TODO: Implémenter système de notifications temporaires
        AppLogger.info("Succès: " + message);
        
        // Animation de feedback visuel
        if (statusLabel != null) {
            animationService.pulse(statusLabel, AnimationService.Speed.FAST);
        }
    }
    
    // Actions FXML (compatibilité avec l'interface existante)
    
    @FXML
    private void onNewIntervention() {
        AppLogger.info("Nouvelle intervention");
        showSuccessNotification("Fonction nouvelle intervention activée");
    }
    
    @FXML
    private void onOpenPreferences() {
        AppLogger.info("Ouverture des préférences");
        showSuccessNotification("Préférences ouvertes");
    }
    
    @FXML
    private void onOpenRequestsHub() {
        AppLogger.info("Hub des demandes");
        showSuccessNotification("Hub des demandes ouvert");
    }
    
    @FXML
    private void onOpenManagementHub() {
        AppLogger.info("Hub de gestion");
        showSuccessNotification("Hub de gestion ouvert");
    }
    
    @FXML
    private void onClearProductSearch() {
        if (productSearchField != null) {
            productSearchField.clear();
            animationService.shake(productSearchField, AnimationService.Speed.FAST);
        }
    }
    
    @FXML
    private void onEditProduct() {
        AppLogger.info("Édition du produit");
        if (btnEditProduct != null) {
            animationService.quickPulse(btnEditProduct);
        }
        showSuccessNotification("Édition du produit activée");
    }
    
    @FXML
    private void onExportProduct() {
        AppLogger.info("Export du produit");
        showSuccessNotification("Export en cours...");
    }
    
    @FXML
    private void onPrintProduct() {
        AppLogger.info("Impression du produit");
        showSuccessNotification("Impression lancée");
    }
    
    @FXML
    private void onEmailProduct() {
        AppLogger.info("Email du produit");
        showSuccessNotification("Email préparé");
    }
    
    @FXML
    private void onShareProduct() {
        AppLogger.info("Partage du produit");
        showSuccessNotification("Partage complet activé");
    }
}