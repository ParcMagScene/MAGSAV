package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
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
        // Note: QRCodeScannerView sera réactivé après correction; // qrCodeScannerView = new QRCodeScannerView();
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
        
        Label title = new Label("🔧 SAV & Interventions");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(StandardColors.getTextColor()));
        
        header.getChildren().add(title); // SEUL le titre dans header
        return header;
    }

    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10)); // EXACTEMENT comme Ventes & Installations
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // toolbar supprimé - Style géré par CSS
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField searchField = new TextField();
        searchField.setPromptText("Titre, description, demandeur...");
        searchField.setPrefWidth(250);
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("📊 Statut");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Ouverte", "En cours", "En attente pièces", "Résolue", "Fermée", "Annulée");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(150);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Filtre par priorité
        VBox priorityBox = new VBox(5);
        Label priorityLabel = new Label("⚡ Priorité");
        priorityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("Toutes", "Urgente", "Élevée", "Moyenne", "Faible");
        priorityFilter.setValue("Toutes");
        priorityFilter.setPrefWidth(120);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        priorityBox.getChildren().addAll(priorityLabel, priorityFilter);
        
        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("🔧 Type");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "Réparation", "Maintenance", "Installation", "Formation", "RMA", "Garantie");
        typeFilter.setValue("Tous types");
        typeFilter.setPrefWidth(140);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button newRequestBtn = new Button("📝 Nouvelle Demande");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        newRequestBtn.setOnAction(e -> createNewServiceRequest());
        
        Button editBtn = new Button("✏️ Modifier");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        editBtn.setOnAction(e -> editSelectedRequest());
        
        Button exportBtn = new Button("📊 Exporter");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportBtn.setOnAction(e -> exportData());
        
        Button emergencyBtn = new Button("🚨 Urgente");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        emergencyBtn.setOnAction(e -> createEmergencyRequest());
        
        Button refreshBtn = new Button("🔄 Actualiser");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        refreshBtn.setOnAction(e -> refresh());
        
        buttonRow.getChildren().addAll(newRequestBtn, editBtn, exportBtn, emergencyBtn, refreshBtn);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox, typeBox, spacer, actionsBox);
        return toolbar;
    }
    
    private CustomTabPane createCustomTabPane() {
        CustomTabPane customTabs = new CustomTabPane();
        customTabs.getStyleClass().add("sav-custom-tab-pane");
        
        // Onglet 1: Suivi des Réparations
        CustomTabPane.CustomTab repairTab = new CustomTabPane.CustomTab(
            "Suivi Réparations", 
            repairTrackingView, 
            "🔧"
        );
        customTabs.addTab(repairTab);
        
        // Onglet 2: Gestion RMA
        CustomTabPane.CustomTab rmaTab = new CustomTabPane.CustomTab(
            "Gestion RMA", 
            rmaManagementView, 
            "📦"
        );
        customTabs.addTab(rmaTab);
        
        // Onglet 3: Scanner QR (temporairement désactivé); // CustomTabPane.CustomTab scannerTab = new CustomTabPane.CustomTab(
        //     "Scanner Inventaire", 
        //     qrCodeScannerView, 
        //     "📱"
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
    //     return qrCodeScannerView;
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
            // Appeler les méthodes de rafraîchissement de chaque vue; // Ces méthodes seront ajoutées aux vues individuelles
        }
    }
    
    /**
     * Méthode pour sélectionner et afficher une intervention SAV par nom (utilisée par la recherche globale)
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
            // TODO: Implémenter la recherche dans RepairTrackingView; // repairTrackingView.selectAndViewIntervention(interventionName);
        }
    }
}
