package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView;
import com.magscene.magsav.desktop.view.vehicle.VehicleListView;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Vue principale pour la gestion des véhicules avec onglets
 */
public class VehicleManagerView extends VBox {

    private final ApiService apiService;
    private CustomTabPane tabPane;
    private VehicleAvailabilityView availabilityTab;
    private HBox adaptiveToolbar;
    private VehicleListView vehicleListView;

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

            // Configuration du conteneur principal - Utilise
            // ThemeConstants.PADDING_STANDARD
            setSpacing(0);
            setPadding(ThemeConstants.PADDING_STANDARD);
            setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
            System.out.println("🚐 DEBUG: Configuration de base terminée");

            // Toolbar adaptative en haut - Utilise ThemeConstants
            adaptiveToolbar = new HBox();
            adaptiveToolbar.setAlignment(Pos.CENTER_LEFT);
            adaptiveToolbar.setPadding(ThemeConstants.TOOLBAR_PADDING);
            adaptiveToolbar.getStyleClass().add(ThemeConstants.UNIFIED_TOOLBAR_CLASS);

            // TabPane en dessous
            System.out.println("🚐 DEBUG: Création du TabPane...");
            tabPane = createTabPane();
            System.out.println("🚐 DEBUG: TabPane créé avec " + tabPane.getTabs().size() + " onglets");

            // Écouter les changements d'onglet
            tabPane.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
                updateToolbarForSelectedTab(newTab);
            });

            // Initialiser la toolbar avec le premier onglet
            updateToolbarForSelectedTab(tabPane.getSelectedTab());

            // Assemblage : Toolbar adaptative puis TabPane
            System.out.println("🚐 DEBUG: Assemblage du layout...");
            getChildren().addAll(adaptiveToolbar, tabPane);
            VBox.setVgrow(tabPane, Priority.ALWAYS);
            System.out.println("🚐 DEBUG: initializeUI - terminé avec succès");

        } catch (Exception e) {
            System.err.println("❌ ERREUR dans initializeUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateToolbarForSelectedTab(CustomTabPane.CustomTab selectedTab) {
        if (selectedTab == null)
            return;

        adaptiveToolbar.getChildren().clear();

        String tabText = selectedTab.getText();
        if (tabText.contains("Liste")) {
            // Toolbar pour Liste des Véhicules
            HBox toolbar = createVehicleContextualToolbar();
            adaptiveToolbar.getChildren().add(toolbar);
            HBox.setHgrow(toolbar, Priority.ALWAYS);
        } else if (tabText.contains("Disponibilités")) {
            // Toolbar simple pour Disponibilités
            HBox toolbar = createAvailabilityToolbar();
            adaptiveToolbar.getChildren().add(toolbar);
            HBox.setHgrow(toolbar, Priority.ALWAYS);
        }
    }

    private HBox createAvailabilityToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // Pas de padding ni de classe CSS ici car déjà présents dans adaptiveToolbar

        Button refreshBtn = ViewUtils.createRefreshButton("Actualiser", () -> {
            System.out.println("Actualiser disponibilités");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(refreshBtn, spacer);
        return toolbar;
    }

    private CustomTabPane createTabPane() {
        try {
            System.out.println("🚐 DEBUG: createTabPane - début");
            CustomTabPane tabs = new CustomTabPane();
            System.out.println("🚐 DEBUG: CustomTabPane créé");

            // Onglet Liste des Véhicules
            System.out.println("🚐 DEBUG: Création VehicleListView...");
            vehicleListView = new VehicleListView(apiService);
            System.out.println("🚐 DEBUG: VehicleListView créé");

            CustomTabPane.CustomTab vehicleListTab = new CustomTabPane.CustomTab("Liste des Véhicules", vehicleListView,
                    "📋");
            tabs.addTab(vehicleListTab);
            System.out.println("🚐 DEBUG: Onglet Liste des Véhicules ajouté");

            // Onglet Disponibilités
            System.out.println("🚐 DEBUG: Création VehicleAvailabilityView...");
            availabilityTab = new VehicleAvailabilityView(apiService);
            System.out.println("🚐 DEBUG: VehicleAvailabilityView créé");

            CustomTabPane.CustomTab availabilityTabItem = new CustomTabPane.CustomTab("Disponibilités", availabilityTab,
                    "📅");
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
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // Pas de padding ni de classe CSS ici car déjà présents dans adaptiveToolbar

        // ========== SECTION FILTRES (Gauche) ==========

        // Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Nom, immatriculation, modèle...",
                text -> {
                    VehicleListView currentView = getCurrentVehicleListView();
                    if (currentView != null) {
                        currentView.setSearchFilter(text);
                    }
                });

        // Filtre Type avec ViewUtils
        VBox typeBox = ViewUtils.createFilterBox("🚗 Type",
                new String[] { "Tous", "CAMION", "FOURGON", "REMORQUE", "UTILITAIRE" },
                "Tous",
                selectedValue -> {
                    VehicleListView currentView = getCurrentVehicleListView();
                    if (currentView != null) {
                        currentView.setTypeFilter(selectedValue);
                    }
                });

        // Filtre Statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous", "Disponible", "En Mission", "En Maintenance", "Hors Service" },
                "Tous",
                selectedValue -> {
                    VehicleListView currentView = getCurrentVehicleListView();
                    if (currentView != null) {
                        currentView.setStatusFilter(selectedValue);
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

        // Assemblage de la toolbar avec VBox pour les filtres
        toolbar.getChildren().addAll(
                searchBox,
                typeBox,
                statusBox,
                spacer,
                addBtn,
                editBtn,
                deleteBtn,
                refreshBtn,
                exportBtn);

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
