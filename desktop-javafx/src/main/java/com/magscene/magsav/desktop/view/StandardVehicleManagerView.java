package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.view.base.AbstractManagerView;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView;
import com.magscene.magsav.desktop.view.vehicle.VehicleListView;
import com.magscene.magsav.desktop.util.ViewUtils;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;

/**
 * Gestionnaire des véhicules - VERSION STANDARDISÉE
 * Hérite d'AbstractManagerView pour respecter l'architecture uniforme
 * 
 * STRUCTURE AUTOMATIQUE :
 * - Top: Toolbar (recherche + filtres + actions) - délégués aux onglets 
 * - Center: CustomTabPane avec onglets véhicules
 * 
 * NOTE: Cette vue utilise les onglets comme conteneur principal,
 * les toolbars sont gérées par chaque onglet individuellement
 */
public class StandardVehicleManagerView extends AbstractManagerView {
    
    // ========================================
    // 🚐 COMPOSANTS SPÉCIFIQUES VÉHICULES; // ========================================
    
    private CustomTabPane tabPane;
    private VehicleListView vehicleListView;
    private VehicleAvailabilityView availabilityView;
    
    // ========================================
    // 🏗️ CONSTRUCTEUR; // ========================================
    
    public StandardVehicleManagerView(ApiService apiService) {
        super(apiService);
        System.out.println("🚐 StandardVehicleManagerView initialisé");
    }
    
    // ========================================
    // 📊 IMPLÉMENTATION ABSTRAITE OBLIGATOIRE; // ========================================
    
    @Override
    protected String getViewCssClass() {
        return "vehicle-manager";
    }
    
    @Override
    protected String getSearchPromptText() {
        // La recherche sera déléguée aux onglets individuels
        return "Recherche déléguée aux onglets...";
    }
    
    @Override
    protected void initializeContent() {
        // Création des vues d'onglets
        Platform.runLater(() -> {
            try {
                System.out.println("🚐 DEBUG: Création des vues d'onglets...");
                vehicleListView = new VehicleListView(apiService);
                availabilityView = new VehicleAvailabilityView(apiService);
                System.out.println("🚐 DEBUG: Vues d'onglets créées avec succès");
            } catch (Exception e) {
                System.err.println("❌ Erreur création vues véhicules: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    @Override
    protected void createFilters() {
        // NOUVEAU PATTERN : Créer une toolbar contextuelle pour les véhicules; // qui sera intégrée sous les onglets du CustomTabPane; // Plus de masquage - on crée une toolbar spécifique véhicules
        createVehicleToolbar();
    }
    
    @Override
    protected void createActions() {
        // IMPORTANT: Pour les vues à onglets, les actions sont gérées; // individuellement par chaque onglet; // 
        // Pas d'actions globales au niveau du manager principal; // Les boutons "Ajouter véhicule", "Modifier", etc. sont dans VehicleListView; // Les actions de planning sont dans VehicleAvailabilityView
    }
    
    @Override
    protected Region createCenterContent() {
        // Création du TabPane principal
        createTabPane();
        
        // NOUVEAU PATTERN : Intégrer la toolbar véhicules sous les onglets
        if (tabPane != null) {
            HBox vehicleToolbar = createVehicleContextualToolbar();
            tabPane.setIntegratedToolbar(vehicleToolbar);
        }
        
        return tabPane;
    }
    
    @Override
    protected void onSearchTextChanged(String searchText) {
        // La recherche est déléguée aux onglets individuels; // Chaque onglet gère sa propre recherche selon son contexte; // On pourrait propager la recherche aux onglets actifs si nécessaire :
        // if (tabPane != null) {
        //     // Propager aux onglets...
        // }
    }
    
    // ========================================
    // �️ TOOLBAR CONTEXTUELLE VÉHICULES; // ========================================
    
    private void createVehicleToolbar() {
        // Placeholder pour la logique de toolbar véhicules
        System.out.println("🚐 Toolbar véhicules contextuelle préparée");
    }
    
    private HBox createVehicleContextualToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // toolbar supprimé - Style géré par CSS
        Button addVehicleBtn = ViewUtils.createAddButton("🚐 Nouveau Véhicule", () -> {
            System.out.println("Ajout nouveau véhicule");
        });
        
        Button planningBtn = new Button("📅 Planning Global");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        planningBtn.setOnAction(e -> System.out.println("Ouverture planning global"));
        
        Button maintenanceBtn = new Button("🔧 Maintenance");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        maintenanceBtn.setOnAction(e -> System.out.println("Gestion maintenance"));
        
        Button exportBtn = new Button("📊 Export");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportBtn.setOnAction(e -> System.out.println("Export véhicules"));
        
        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(
            new Label("🚐 Actions Véhicules:"),
            addVehicleBtn,
            planningBtn, 
            maintenanceBtn,
            spacer,
            exportBtn
        );
        
        return toolbar;
    }
    
    // ========================================
    // �🗂️ GESTION DES ONGLETS; // ========================================
    
    private void createTabPane() {
        try {
            System.out.println("🚐 DEBUG: Création CustomTabPane...");
            tabPane = new CustomTabPane();
            
            // Attendre que les vues soient créées
            Platform.runLater(() -> {
                if (vehicleListView != null && availabilityView != null) {
                    setupTabs();
                } else {
                    // Retry si les vues ne sont pas encore prêtes
                    Platform.runLater(() -> {
                        if (vehicleListView != null && availabilityView != null) {
                            setupTabs();
                        } else {
                            System.err.println("⚠️ Impossible de créer les onglets véhicules - vues non disponibles");
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création TabPane véhicules: " + e.getMessage());
            e.printStackTrace();
            // Fallback - TabPane vide
            tabPane = new CustomTabPane();
        }
    }
    
    private void setupTabs() {
        try {
            System.out.println("🚐 DEBUG: Configuration des onglets...");
            
            // 📋 Onglet Liste des Véhicules
            CustomTabPane.CustomTab vehicleListTab = new CustomTabPane.CustomTab(
                "Liste des Véhicules", 
                vehicleListView, 
                "📋"
            );
            tabPane.addTab(vehicleListTab);
            System.out.println("🚐 DEBUG: Onglet Liste des Véhicules ajouté");
            
            // 📅 Onglet Disponibilités  
            CustomTabPane.CustomTab availabilityTab = new CustomTabPane.CustomTab(
                "Disponibilités", 
                availabilityView, 
                "📅"
            );
            tabPane.addTab(availabilityTab);
            System.out.println("🚐 DEBUG: Onglet Disponibilités ajouté");
            
            // Sélectionner le premier onglet par défaut
            tabPane.selectTab(0);
            System.out.println("🚐 DEBUG: Onglets configurés avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur configuration onglets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========================================
    // 🛠️ UTILITAIRES SPÉCIFIQUES VÉHICULES; // ========================================
    
    /**
     * Masque la toolbar globale car les actions sont gérées par les onglets individuels
     */
    @SuppressWarnings("unused")
    private void hideToolbar() {
        Platform.runLater(() -> {
            if (standardToolbar != null) {
                standardToolbar.setVisible(false);
                standardToolbar.setManaged(false);
                System.out.println("🚐 DEBUG: Toolbar globale masquée - actions déléguées aux onglets");
            }
        });
    }
    
    /**
     * Accès au TabPane pour interactions externes si nécessaire
     */
    public CustomTabPane getTabPane() {
        return tabPane;
    }
    
    /**
     * Accès à la vue liste des véhicules
     */
    public VehicleListView getVehicleListView() {
        return vehicleListView;
    }
    
    /**
     * Accès à la vue des disponibilités
     */
    public VehicleAvailabilityView getAvailabilityView() {
        return availabilityView;
    }
    
    /**
     * Sélectionne un onglet spécifique par index
     */
    public void selectTab(int tabIndex) {
        if (tabPane != null) {
            Platform.runLater(() -> tabPane.selectTab(tabIndex));
        }
    }
    
    /**
     * Sélectionne l'onglet Liste des véhicules
     */
    public void showVehicleList() {
        selectTab(0);
    }
    
    /**
     * Sélectionne l'onglet Disponibilités
     */
    public void showAvailabilities() {
        selectTab(1);
    }
    
    @Override
    protected void refresh() {
        super.refresh();
        
        // Rafraîchir les onglets individuels
        Platform.runLater(() -> {
            if (vehicleListView != null) {
                // Assurez-vous que VehicleListView a une méthode refresh()
                try {
                    vehicleListView.getClass().getMethod("refresh").invoke(vehicleListView);
                } catch (Exception e) {
                    System.out.println("ℹ️ VehicleListView.refresh() non disponible");
                }
            }
            
            if (availabilityView != null) {
                // Assurez-vous que VehicleAvailabilityView a une méthode refresh()
                try {
                    availabilityView.getClass().getMethod("refresh").invoke(availabilityView);
                } catch (Exception e) {
                    System.out.println("ℹ️ VehicleAvailabilityView.refresh() non disponible");
                }
            }
        });
        
        System.out.println("🚐 StandardVehicleManagerView rafraîchi");
    }
}

/**
 * 📝 NOTES D'ARCHITECTURE POUR VEHICLEMANAGERVIEW
 * 
 * ✅ STANDARDISATION RÉUSSIE :
 * - Hérite d'AbstractManagerView → Structure BorderPane automatique
 * - Toolbar globale masquée → Actions déléguées aux onglets individuels  
 * - CustomTabPane en contenu central → Cohérent avec le pattern existant
 * - Debug et gestion d'erreurs conservés → Stabilité maintenue
 * 
 * 🎯 PARTICULARITÉS DE CETTE VUE :
 * - Vue "conteneur" avec onglets spécialisés (contrairement aux autres managers)
 * - Pas de filtres/actions globaux → Chaque onglet gère ses propres outils
 * - Structure hybride : AbstractManagerView + CustomTabPane
 * 
 * 🔄 DÉLÉGATION DES RESPONSABILITÉS :
 * - VehicleListView → CRUD véhicules, filtres par type/statut, recherche
 * - VehicleAvailabilityView → Planning, disponibilités, réservations
 * - StandardVehicleManagerView → Navigation entre onglets uniquement
 * 
 * 💡 EVOLUTION POSSIBLE :
 * - Ajouter toolbar globale avec actions communes (Exporter, Imprimer...)  
 * - Synchroniser recherche globale avec onglets actifs
 * - Ajouter notifications entre onglets (ex: véhicule modifié → rafraîchir planning)
 */