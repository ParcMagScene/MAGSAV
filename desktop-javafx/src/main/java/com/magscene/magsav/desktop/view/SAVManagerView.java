package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.view.sav.RepairTrackingView;
import com.magscene.magsav.desktop.view.sav.RMAManagementView;
import com.magscene.magsav.desktop.view.sav.TechnicianPlanningView;
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
        
        // Toolbar séparée comme dans la référence
        HBox toolbar = createUnifiedToolbar();
        
        // TopContainer comme référence
        VBox topContainer = new VBox(header, toolbar);
        setTop(topContainer);
        
        // TabPane principal avec toutes les fonctionnalités SAV
        tabPane = createTabPane();
        setCenter(tabPane);
        
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
        title.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().add(title); // SEUL le titre dans header
        return header;
    }

    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10)); // EXACTEMENT comme Ventes & Installations
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #142240; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Recherche globale
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        TextField searchField = new TextField();
        searchField.setPromptText("Titre, description, demandeur...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC; -fx-border-color: #7DD3FC; -fx-border-radius: 4;");
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("📊 Statut");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Ouverte", "En cours", "En attente pièces", "Résolue", "Fermée", "Annulée");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(150);
        statusFilter.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC;");
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Filtre par priorité
        VBox priorityBox = new VBox(5);
        Label priorityLabel = new Label("⚡ Priorité");
        priorityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("Toutes", "Urgente", "Élevée", "Moyenne", "Faible");
        priorityFilter.setValue("Toutes");
        priorityFilter.setPrefWidth(120);
        priorityFilter.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC;");
        priorityBox.getChildren().addAll(priorityLabel, priorityFilter);
        
        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("🔧 Type");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous types", "Réparation", "Maintenance", "Installation", "Formation", "RMA", "Garantie");
        typeFilter.setValue("Tous types");
        typeFilter.setPrefWidth(140);
        typeFilter.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC;");
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button newRequestBtn = new Button("📝 Nouvelle Demande");
        newRequestBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4;");
        newRequestBtn.setOnAction(e -> createNewServiceRequest());
        
        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 4;");
        editBtn.setOnAction(e -> editSelectedRequest());
        
        Button exportBtn = new Button("📊 Exporter");
        exportBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 4;");
        exportBtn.setOnAction(e -> exportData());
        
        Button emergencyBtn = new Button("🚨 Urgente");
        emergencyBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4;");
        emergencyBtn.setOnAction(e -> createEmergencyRequest());
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4;");
        refreshBtn.setOnAction(e -> refresh());
        
        buttonRow.getChildren().addAll(newRequestBtn, editBtn, exportBtn, emergencyBtn, refreshBtn);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox, typeBox, spacer, actionsBox);
        return toolbar;
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
        
        // Onglet 3: Scanner QR (temporairement désactivé)
        // Tab scannerTab = new Tab("📱 Scanner Inventaire");
        // scannerTab.setContent(qrCodeScannerView);
        // scannerTab.getStyleClass().add("sav-tab");
        
        tabs.getTabs().addAll(repairTab, rmaTab);
        
        // Sélectionner le premier onglet par défaut
        tabs.getSelectionModel().select(0);
        
        return tabs;
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
        if (tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(tabIndex);
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
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            if (selectedTab.getText().equals("🔧 Suivi Réparations") && repairTrackingView != null) {
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
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
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
            // Appeler les méthodes de rafraîchissement de chaque vue
            // Ces méthodes seront ajoutées aux vues individuelles
        }
    }
}