package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.view.base.BaseManagerView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue simplifiée de gestion des commandes groupées pour test Phase 2
 */
public class GroupedOrderManagerViewSimple extends BaseManagerView<Object> {
    
    private TableView<OrderData> orderTable;
    private ObservableList<OrderData> orderList = FXCollections.observableArrayList();
    
    public GroupedOrderManagerViewSimple() {
        super();
        
        // Initialiser les données test
        createTestData();
    }
    
    @Override
    protected String getModuleName() {
        return "Commandes Groupées";
    }
    
    @Override
    protected String getViewCssClass() {
        return "grouped-order-manager";
    }
    
    @Override
    protected Pane createMainContent() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        
        // Tableau de bord des seuils
        HBox dashboardBar = createDashboard();
        
        // Titre
        Label titleLabel = new Label("📦 Gestion des Commandes Groupées");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Table des commandes
        createOrderTable();
        
        mainContainer.getChildren().addAll(dashboardBar, titleLabel, orderTable);
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        
        return mainContainer;
    }
    
    @Override
    protected void addCustomToolbarItems(ToolBar toolbar) {
        Button validateButton = new Button("✅ Valider");
        Button optimizeButton = new Button("⚡ Optimiser");
        Button sendButton = new Button("📧 Envoyer");
        Button thresholdButton = new Button("💰 Seuils");
        
        toolbar.getItems().addAll(
            new Separator(),
            validateButton,
            optimizeButton,
            sendButton,
            new Separator(),
            thresholdButton
        );
    }
    
    @Override
    protected void initializeContent() {
        // Les données seront chargées après l'initialisation par createTestData()
        if (orderList != null && !orderList.isEmpty()) {
            updateStatus(orderList.size() + " commande(s) chargée(s)");
        } else {
            updateStatus("Chargement...");
        }
    }
    
    private HBox createDashboard() {
        HBox dashboard = new HBox(20);
        dashboard.setPadding(new Insets(10));
        dashboard.getStyleClass().add("threshold-dashboard");
        
        Label readyLabel = new Label("🎯 Seuils atteints: 1");
        Label pendingLabel = new Label("⏳ En attente: 2");
        Label savingsLabel = new Label("💰 Économies: 45 €");
        
        dashboard.getChildren().addAll(readyLabel, new Separator(), 
                                      pendingLabel, new Separator(), savingsLabel);
        
        return dashboard;
    }
    
    private void createOrderTable() {
        orderTable = new TableView<>();
        orderTable.setItems(orderList);
        orderTable.getStyleClass().add("data-table");
        
        // Colonne Référence
        TableColumn<OrderData, String> refCol = new TableColumn<>("Référence");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(100);
        
        // Colonne Fournisseur
        TableColumn<OrderData, String> supplierCol = new TableColumn<>("Fournisseur");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierCol.setPrefWidth(150);
        
        // Colonne Montant
        TableColumn<OrderData, String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);
        
        // Colonne Seuil
        TableColumn<OrderData, String> thresholdCol = new TableColumn<>("Seuil");
        thresholdCol.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        thresholdCol.setPrefWidth(120);
        
        // Colonne Statut
        TableColumn<OrderData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        
        // Colonne Date
        TableColumn<OrderData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);
        
        // Colonne Économies
        TableColumn<OrderData, String> savingsCol = new TableColumn<>("Économies");
        savingsCol.setCellValueFactory(new PropertyValueFactory<>("savings"));
        savingsCol.setPrefWidth(80);
        
        orderTable.getColumns().addAll(refCol, supplierCol, amountCol, thresholdCol, 
                                       statusCol, dateCol, savingsCol);
    }
    
    private void createTestData() {
        orderList.addAll(
            new OrderData("GC-001", "SonoMax Pro", "480.50 €", "✅ 96% (500€)", "⏳ À valider", "15/01", "20 €"),
            new OrderData("GC-002", "Éclairage Scène", "350.00 €", "✅ 117% (300€)", "✅ Validée", "14/01", "15 €"),
            new OrderData("GC-003", "TechService", "245.50 €", "⏳ 49% (500€)", "📧 Envoyée", "12/01", "10 €")
        );
        // Mettre à jour le statut après chargement
        Platform.runLater(() -> updateStatus(orderList.size() + " commande(s) chargée(s)"));
    }
    
    // Classe interne pour les données test
    public static class OrderData {
        private String reference;
        private String supplier;
        private String amount;
        private String threshold;
        private String status;
        private String date;
        private String savings;
        
        public OrderData(String reference, String supplier, String amount, String threshold,
                        String status, String date, String savings) {
            this.reference = reference;
            this.supplier = supplier;
            this.amount = amount;
            this.threshold = threshold;
            this.status = status;
            this.date = date;
            this.savings = savings;
        }
        
        // Getters pour PropertyValueFactory
        public String getReference() { return reference; }
        public String getSupplier() { return supplier; }
        public String getAmount() { return amount; }
        public String getThreshold() { return threshold; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getSavings() { return savings; }
    }
    
    // Méthodes abstraites du parent
    @Override
    protected void handleAdd() {
        updateStatus("Création d'une nouvelle commande groupée...");
        showAlert("Fonctionnalité", "Création de commande - À implémenter");
    }
    
    @Override
    protected void handleEdit() {
        OrderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune commande sélectionnée");
            return;
        }
        updateStatus("Consultation de " + selected.getReference());
        showAlert("Fonctionnalité", "Consultation de commande - À implémenter");
    }
    
    @Override
    protected void handleDelete() {
        OrderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune commande sélectionnée");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la commande");
        confirm.setContentText("Confirmer la suppression de " + selected.getReference() + " ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                orderList.remove(selected);
                updateStatus("Commande supprimée");
            }
        });
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void refresh() {
        super.refresh();
        updateStatus("Données rafraîchies");
    }
}