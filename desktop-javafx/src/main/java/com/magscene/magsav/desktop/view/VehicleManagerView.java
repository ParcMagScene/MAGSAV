package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView;
import com.magscene.magsav.desktop.view.vehicle.VehicleListView;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue principale pour la gestion des véhicules avec onglets
 */
public class VehicleManagerView extends VBox {
    
    private final ApiService apiService;
    private CustomTabPane tabPane;
    private VehicleAvailabilityView availabilityTab;
    
    public VehicleManagerView(ApiService apiService) {
        System.out.println("🚐 DEBUG: Constructeur VehicleManagerView - début");
        this.apiService = apiService;
        System.out.println("🚐 DEBUG: ApiService assigné");
        
        try {
            initializeUI();
            System.out.println("🚐 DEBUG: Constructeur VehicleManagerView - terminé avec succès");
        } catch (Exception e) {
            System.err.println("❌ ERREUR dans constructeur VehicleManagerView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        try {
            System.out.println("🚐 DEBUG: initializeUI - début");
            
            // Configuration du conteneur principal
            setSpacing(0);
            setPadding(new Insets(10, 0, 10, 10)); // Padding : haut, droite, bas, gauche - zéro à droite pour coller le volet
            setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
            System.out.println("🚐 DEBUG: Configuration de base terminée");
            
            // TabPane principal avec toolbar intégrée
            System.out.println("🚐 DEBUG: Création du TabPane...");
            tabPane = createTabPane();
            System.out.println("🚐 DEBUG: TabPane créé");
            
            // NOUVEAU PATTERN : Intégrer une toolbar contextuelle sous les onglets
            if (tabPane != null) {
                HBox vehicleToolbar = createVehicleContextualToolbar();
                tabPane.setIntegratedToolbar(vehicleToolbar);
                System.out.println("🚐 DEBUG: Toolbar véhicules intégrée");
                System.out.println("🚐 DEBUG: TabPane a " + tabPane.getTabs().size() + " onglets");
            } else {
                System.err.println("❌ ERREUR: tabPane est null!");
            }
            
            // Assemblage
            System.out.println("🚐 DEBUG: Assemblage du layout...");
            getChildren().add(tabPane);
            VBox.setVgrow(tabPane, Priority.ALWAYS);
            System.out.println("🚐 DEBUG: TabPane ajouté à VehicleManagerView");
            System.out.println("🚐 DEBUG: VehicleManagerView a " + getChildren().size() + " enfants");
            System.out.println("🚐 DEBUG: initializeUI - terminé avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR dans initializeUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private CustomTabPane createTabPane() {
        try {
            System.out.println("🚐 DEBUG: createTabPane - début");
            CustomTabPane tabs = new CustomTabPane();
            System.out.println("🚐 DEBUG: CustomTabPane créé");
            
            // Onglet Liste des Véhicules
            System.out.println("🚐 DEBUG: Création VehicleListView...");
            VehicleListView vehicleListView = new VehicleListView(apiService);
            System.out.println("🚐 DEBUG: VehicleListView créé");
            
            CustomTabPane.CustomTab vehicleListTab = new CustomTabPane.CustomTab("Liste des Véhicules", vehicleListView, "📋");
            tabs.addTab(vehicleListTab);
            System.out.println("🚐 DEBUG: Onglet Liste des Véhicules ajouté");
            
            // Onglet Disponibilités
            System.out.println("🚐 DEBUG: Création VehicleAvailabilityView...");
            availabilityTab = new VehicleAvailabilityView(apiService);
            System.out.println("🚐 DEBUG: VehicleAvailabilityView créé");
            
            CustomTabPane.CustomTab availabilityTabItem = new CustomTabPane.CustomTab("Disponibilités", availabilityTab, "📅");
            tabs.addTab(availabilityTabItem);
            System.out.println("🚐 DEBUG: Onglet Disponibilités ajouté");
            
            // Sélectionner le premier onglet
            tabs.selectTab(0);
            System.out.println("🚐 DEBUG: Premier onglet sélectionné");
            
            System.out.println("🚐 DEBUG: createTabPane - terminé avec succès");
            return tabs;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR dans createTabPane: " + e.getMessage());
            e.printStackTrace();
            return new CustomTabPane(); // Retourner un TabPane vide en cas d'erreur
        }
    }
    
    // ========================================
    // 🛠️ TOOLBAR UNIFIÉE VÉHICULES; // ========================================
    
    /**
     * Crée la toolbar unifiée véhicules avec filtres + actions
     * Pattern standardisé : filtres à gauche, actions à droite
     */
    private HBox createVehicleContextualToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 15, 10, 15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; -fx-background-radius: 8; " +
                        "-fx-border-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-width: 1px; -fx-border-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);");
        
        // ========== SECTION FILTRES (Gauche) ==========
        
        // Recherche
        TextField searchField = ViewUtils.createSearchField("Nom, immatriculation, modèle...", 
            text -> {
                VehicleListView currentView = getCurrentVehicleListView();
                if (currentView != null) {
                    currentView.setSearchFilter(text);
                }
            });
        
        // Filtre Type
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "CAMION", "FOURGON", "REMORQUE", "UTILITAIRE");
        typeFilter.setValue("Tous");
        typeFilter.setPrefWidth(120);
        typeFilter.setOnAction(e -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.setTypeFilter(typeFilter.getValue());
            }
        });
        
        // Filtre Statut
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Disponible", "En Mission", "En Maintenance", "Hors Service");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.setStatusFilter(statusFilter.getValue());
            }
        });
        
        // Spacer pour séparer filtres et actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // ========== SECTION ACTIONS (Droite) ==========
        
        Button addBtn = ViewUtils.createAddButton("Ajouter", () -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.handleAddVehicle();
            }
        });
        
        Button editBtn = ViewUtils.createEditButton("Modifier", () -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.handleEditVehicle();
            }
        }, Bindings.createBooleanBinding(() -> false)); // Désactivation conditionnelle si nécessaire
        
        Button deleteBtn = ViewUtils.createDeleteButton("Supprimer", () -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.handleDeleteVehicle();
            }
        }, Bindings.createBooleanBinding(() -> false));
        
        Button refreshBtn = ViewUtils.createRefreshButton("Actualiser", () -> {
            VehicleListView currentView = getCurrentVehicleListView();
            if (currentView != null) {
                currentView.handleRefreshData();
            }
        });
        
        Button exportBtn = new Button("📊 Export");
        exportBtn.getStyleClass().add("action-button-secondary");
        exportBtn.setOnAction(e -> System.out.println("Export véhicules depuis toolbar unifiée"));
        
        // Assemblage de la toolbar
        toolbar.getChildren().addAll(
            searchField,
            typeFilter,
            statusFilter,
            spacer,
            addBtn,
            editBtn, 
            deleteBtn,
            refreshBtn,
            exportBtn
        );
        
        return toolbar;
    }
    
    /**
     * Récupère la vue VehicleListView active pour déléguer les actions
     */
    private VehicleListView getCurrentVehicleListView() {
        if (tabPane != null && tabPane.getSelectedTab() != null) {
            var selectedTab = tabPane.getSelectedTab();
            if (selectedTab.getContent() instanceof VehicleListView vehicleListView) {
                return vehicleListView;
            }
        }
        return null;
    }

}
