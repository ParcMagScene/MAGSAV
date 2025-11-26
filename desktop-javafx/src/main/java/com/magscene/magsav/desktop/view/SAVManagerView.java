package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.sav.RepairTrackingView;
import com.magscene.magsav.desktop.view.sav.RMAManagementView;
import com.magscene.magsav.desktop.view.sav.TechnicianPlanningView;
import com.magscene.magsav.desktop.component.CustomTabPane;
// import com.magscene.magsav.desktop.view.sav.QRCodeScannerView; // Temporairement désactivé
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue principale du module SAV intégrant toutes les fonctionnalités développées
 * Onglets : Suivi Réparations, Gestion RMA, Planning Techniciens, Scanner QR
 */
public class SAVManagerView extends BorderPane {

    private final ApiService apiService;
    private CustomTabPane customTabPane;

    // Vues SAV spécialisées
    private RepairTrackingView repairTrackingView;
    private RMAManagementView rmaManagementView;
    private TechnicianPlanningView technicianPlanningView;
    // private QRCodeScannerView qrCodeScannerView; // Temporairement désactivé

    public SAVManagerView(ApiService apiService) {
        this.apiService = apiService;
        initialize();
        setupLayout();
    }

    private void initialize() {
        // Initialisation des vues spécialisées
        repairTrackingView = new RepairTrackingView();
        rmaManagementView = new RMAManagementView();
        technicianPlanningView = new TechnicianPlanningView();
        // Note: QRCodeScannerView sera réactivé après correction; // qrCodeScannerView
        // = new QRCodeScannerView();
    }

    private void setupLayout() {
        // Header du module SAV
        VBox header = createHeader();

        // Toolbar séparée comme dans la référence
        HBox toolbar = createUnifiedToolbar();

        // TopContainer comme référence
        VBox topContainer = new VBox(header, toolbar);
        setTop(topContainer);

        // CustomTabPane principal avec toutes les fonctionnalités SAV
        customTabPane = createCustomTabPane();
        setCenter(customTabPane);

        // Style CSS
        getStyleClass().add("sav-manager-view");
        setPadding(new Insets(5));
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
    }

    private VBox createHeader() {
        VBox header = new VBox(10); // STANDARD : 10px spacing comme référence
        header.setPadding(new Insets(0, 0, 20, 0));

        // Pas de titre - déjà dans le header principal de l'application

        return header;
    }

    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle(
            "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #8B91FF; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);");
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Titre, description, demandeur...", text -> {});
        TextField searchField = (TextField) searchBox.getChildren().get(1);
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);

        // Filtre par statut
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut", 
            new String[]{"Tous", "Ouverte", "En cours", "En attente pièces", "Résolue", "Fermée", "Annulée"}, 
            "Tous", value -> {});

        // Filtre par priorité
        VBox priorityBox = ViewUtils.createFilterBox("⚡ Priorité", 
            new String[]{"Toutes", "Urgente", "Élevée", "Moyenne", "Faible"}, 
            "Toutes", value -> {});

        // Filtre par type
        VBox typeBox = ViewUtils.createFilterBox("🔧 Type", 
            new String[]{"Tous types", "Réparation", "Maintenance", "Installation", "Formation", "RMA", "Garantie"}, 
            "Tous types", value -> {});

        // Boutons d'action avec ViewUtils
        Button newRequestBtn = ViewUtils.createAddButton("📝 Nouvelle Demande", this::createNewServiceRequest);
        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> editSelectedRequest());
        Button exportBtn = new Button("📊 Exporter");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> exportData());

        Button emergencyBtn = new Button("🚨 Urgente");
        emergencyBtn.getStyleClass().add("btn-urgent");
        emergencyBtn.setOnAction(e -> createEmergencyRequest());

        Button refreshBtn = ViewUtils.createRefreshButton("🔄 Actualiser", this::refresh);

        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox, typeBox, spacer, 
            newRequestBtn, editBtn, exportBtn, emergencyBtn, refreshBtn);
        return toolbar;
    }

    private CustomTabPane createCustomTabPane() {
        CustomTabPane customTabs = new CustomTabPane();
        customTabs.getStyleClass().add("sav-custom-tab-pane");

        // Onglet 1: Suivi des Réparations
        CustomTabPane.CustomTab repairTab = new CustomTabPane.CustomTab(
                "Suivi Réparations",
                repairTrackingView,
                "🔧");
        customTabs.addTab(repairTab);

        // Onglet 2: Gestion RMA
        CustomTabPane.CustomTab rmaTab = new CustomTabPane.CustomTab(
                "Gestion RMA",
                rmaManagementView,
                "📦");
        customTabs.addTab(rmaTab);

        // Onglet 3: Scanner QR (temporairement désactivé); // CustomTabPane.CustomTab
        // scannerTab = new CustomTabPane.CustomTab(
        // "Scanner Inventaire",
        // qrCodeScannerView,
        // "📱"
        // );
        // customTabs.addTab(scannerTab);

        // Sélectionner le premier onglet par défaut
        customTabs.selectTab(0);

        System.out.println("✅ CustomTabPane créé pour SAV avec boutons de navigation personnalisés");

        return customTabs;
    }

    /**
     * Accès aux vues spécialisées pour intégration externe
     */
    public RepairTrackingView getRepairTrackingView() {
        return repairTrackingView;
    }

    public RMAManagementView getRMAManagementView() {
        return rmaManagementView;
    }

    // public QRCodeScannerView getQRCodeScannerView() {
    // return qrCodeScannerView;
    // }

    /**
     * Sélectionner un onglet spécifique par programme
     */
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < customTabPane.getTabs().size()) {
            customTabPane.selectTab(tabIndex);
        }
    }

    /**
     * Actions du toolbar unifié
     */
    private void createNewServiceRequest() {
        // TODO: Ouvrir dialogue de création d'une nouvelle demande SAV
        System.out.println("Création d'une nouvelle demande SAV");
    }

    private void createEmergencyRequest() {
        // TODO: Ouvrir dialogue de création d'une demande urgente
        System.out.println("Création d'une demande urgente");
    }

    private void showStatistics() {
        // TODO: Afficher les statistiques du SAV
        System.out.println("Affichage des statistiques SAV");
    }

    /**
     * Modifier la demande sélectionnée dans l'onglet actif
     */
    private void editSelectedRequest() {
        CustomTabPane.CustomTab selectedTab = customTabPane.getSelectedTab();
        if (selectedTab != null) {
            if (selectedTab.getText().equals("Suivi Réparations") && repairTrackingView != null) {
                // Déléguer à la vue de suivi des réparations
                repairTrackingView.editSelectedRequest();
            } else {
                // Pour les autres onglets, afficher un message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Modification");
                alert.setHeaderText("Modification de demande");
                alert.setContentText("Fonctionnalité de modification disponible dans l'onglet 'Suivi Réparations'");
                alert.showAndWait();
            }
        }
    }

    /**
     * Exporter les données de l'onglet actif
     */
    private void exportData() {
        CustomTabPane.CustomTab selectedTab = customTabPane.getSelectedTab();
        if (selectedTab != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export");
            alert.setHeaderText("Export des données");
            alert.setContentText("Fonctionnalité d'export pour l'onglet : " + selectedTab.getText());
            alert.showAndWait();
        }
    }

    /**
     * Rafraîchir toutes les vues SAV
     */
    public void refresh() {
        if (repairTrackingView != null) {
            // Appeler les méthodes de rafraîchissement de chaque vue; // Ces méthodes
            // seront ajoutées aux vues individuelles
        }
    }

    /**
     * Méthode pour sélectionner et afficher une intervention SAV par nom (utilisée
     * par la recherche globale)
     */
    public void selectAndViewIntervention(String interventionName) {
        if (interventionName == null || interventionName.trim().isEmpty()) {
            return;
        }

        // Sélectionner l'onglet "Suivi Réparations" par défaut
        if (customTabPane != null) {
            customTabPane.selectTab(0); // Premier onglet = Suivi Réparations
        }

        // Déléguer à la vue de suivi des réparations
        if (repairTrackingView != null) {
            // TODO: Implémenter la recherche dans RepairTrackingView; //
            // repairTrackingView.selectAndViewIntervention(interventionName);
        }
    }
}
