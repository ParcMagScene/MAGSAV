package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.view.sav.RepairTrackingView;
import com.magscene.magsav.desktop.view.sav.RMAManagementView;
import com.magscene.magsav.desktop.view.sav.TechnicianPlanningView;
// import com.magscene.magsav.desktop.view.sav.QRCodeScannerView; // Temporairement désactivé
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Vue principale du module SAV intégrant toutes les fonctionnalités développées
 * Onglets : Suivi Réparations, Gestion RMA, Planning Techniciens, Scanner QR
 */
public class SAVManagerView extends BorderPane {
    
    private final ApiService apiService;
    private TabPane tabPane;
    
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
        // Note: QRCodeScannerView sera réactivé après correction
        // qrCodeScannerView = new QRCodeScannerView();
    }
    
    private void setupLayout() {
        // Header du module SAV
        VBox header = createHeader();
        setTop(header);
        
        // TabPane principal avec toutes les fonctionnalités SAV
        tabPane = createTabPane();
        setCenter(tabPane);
        
        // Style CSS
        getStyleClass().add("sav-manager-view");
        setPadding(new Insets(10));
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.getStyleClass().add("module-header");
        
        Label title = new Label("🔧 Service Après Vente (SAV)");
        title.getStyleClass().add("module-title");
        
        Label subtitle = new Label("Gestion complète des interventions, réparations, RMA et planning techniciens");
        subtitle.getStyleClass().add("module-subtitle");
        
        // Boutons d'action rapide
        ToolBar quickActions = new ToolBar();
        
        Button btnNewRequest = new Button("📝 Nouvelle Demande");
        btnNewRequest.getStyleClass().add("action-button-primary");
        btnNewRequest.setOnAction(e -> createNewServiceRequest());
        
        Button btnEmergency = new Button("🚨 Intervention Urgente");
        btnEmergency.getStyleClass().add("action-button-emergency");
        btnEmergency.setOnAction(e -> createEmergencyRequest());
        
        Button btnStats = new Button("📊 Statistiques");
        btnStats.getStyleClass().add("action-button-secondary");
        btnStats.setOnAction(e -> showStatistics());
        
        quickActions.getItems().addAll(btnNewRequest, btnEmergency, new Separator(), btnStats);
        
        header.getChildren().addAll(title, subtitle, quickActions);
        return header;
    }
    
    private TabPane createTabPane() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("sav-tab-pane");
        
        // Onglet 1: Suivi des Réparations
        Tab repairTab = new Tab("🔧 Suivi Réparations");
        repairTab.setContent(repairTrackingView);
        repairTab.getStyleClass().add("sav-tab");
        
        // Onglet 2: Gestion RMA
        Tab rmaTab = new Tab("📦 Gestion RMA");
        rmaTab.setContent(rmaManagementView);
        rmaTab.getStyleClass().add("sav-tab");
        
        // Onglet 3: Planning Techniciens
        Tab planningTab = new Tab("👤 Planning Techniciens");
        planningTab.setContent(technicianPlanningView);
        planningTab.getStyleClass().add("sav-tab");
        
        // Onglet 4: Scanner QR (temporairement désactivé)
        // Tab scannerTab = new Tab("📱 Scanner Inventaire");
        // scannerTab.setContent(qrCodeScannerView);
        // scannerTab.getStyleClass().add("sav-tab");
        
        tabs.getTabs().addAll(repairTab, rmaTab, planningTab);
        
        // Sélectionner le premier onglet par défaut
        tabs.getSelectionModel().select(0);
        
        return tabs;
    }
    
    /**
     * Créer une nouvelle demande de service
     */
    private void createNewServiceRequest() {
        // Basculer vers l'onglet suivi réparations et créer nouvelle demande
        tabPane.getSelectionModel().select(0);
        if (repairTrackingView != null) {
            // Déclencher la création d'une nouvelle demande dans RepairTrackingView
            repairTrackingView.createNewServiceRequest();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Module indisponible");
            alert.setHeaderText("Suivi des réparations non initialisé");
            alert.setContentText("Le module de suivi des réparations n'est pas disponible.");
            alert.show();
        }
    }
    
    /**
     * Créer une intervention d'urgence
     */
    private void createEmergencyRequest() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Intervention Urgente");
        alert.setHeaderText("Création d'une intervention d'urgence");
        alert.setContentText("Cette fonctionnalité créera automatiquement une demande prioritaire " +
                "et notifiera immédiatement les techniciens disponibles.");
        
        // Basculer vers planning techniciens pour assignation immédiate
        alert.showAndWait().ifPresent(response -> {
            tabPane.getSelectionModel().select(2); // Planning techniciens
        });
    }
    
    /**
     * Afficher les statistiques SAV
     */
    private void showStatistics() {
        Alert stats = new Alert(Alert.AlertType.INFORMATION);
        stats.setTitle("Statistiques SAV");
        stats.setHeaderText("📊 Tableau de bord SAV");
        
        // Contenu simulé - à remplacer par de vraies données
        stats.setContentText(
            "📈 Demandes en cours: 12\n" +
            "✅ Réparations terminées: 45\n" +
            "📦 RMA en traitement: 8\n" +
            "👤 Techniciens actifs: 6\n" +
            "⏱️ Temps moyen de résolution: 2.3 jours"
        );
        
        stats.showAndWait();
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
    
    public TechnicianPlanningView getTechnicianPlanningView() {
        return technicianPlanningView;
    }
    
    // public QRCodeScannerView getQRCodeScannerView() {
    //     return qrCodeScannerView;
    // }
    
    /**
     * Sélectionner un onglet spécifique par programme
     */
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(tabIndex);
        }
    }
    
    /**
     * Rafraîchir toutes les vues SAV
     */
    public void refresh() {
        if (repairTrackingView != null) {
            // Appeler les méthodes de rafraîchissement de chaque vue
            // Ces méthodes seront ajoutées aux vues individuelles
        }
    }
}