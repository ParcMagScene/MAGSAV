package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.view.base.BaseManagerView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Vue de gestion des contrats
 */
public class ContractManagerView extends BaseManagerView<Object> {
    
    private final ApiService apiService;
    private TableView<ContractData> contractTable;
    private ObservableList<ContractData> contractList = FXCollections.observableArrayList();
    
    public ContractManagerView(ApiService apiService) {
        super();
        this.apiService = apiService;
        createTestData();
    }
    
    @Override
    protected String getModuleName() {
        return "Contrats";
    }
    
    @Override
    protected String getViewCssClass() {
        return "contract-manager";
    }
    
    @Override
    protected Pane createMainContent() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        
        // Barre de filtres
        HBox filterBar = createFilterBar();
        
        // Tableau des contrats
        createContractTable();
        
        mainContainer.getChildren().addAll(filterBar, contractTable);
        VBox.setVgrow(contractTable, Priority.ALWAYS);
        
        return mainContainer;
    }
    
    private HBox createFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(5));
        
        Label filterLabel = new Label("Filtrer par type:");
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Location", "Maintenance", "Prestation", "Vente");
        typeFilter.setValue("Tous");
        
        Label statusLabel = new Label("Statut:");
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Actif", "En attente", "Expiré", "Résilié");
        statusFilter.setValue("Tous");
        
        filterBar.getChildren().addAll(filterLabel, typeFilter, statusLabel, statusFilter);
        
        return filterBar;
    }
    
    private void createContractTable() {
        contractTable = new TableView<>();
        contractTable.setItems(contractList);
        contractTable.getStyleClass().add("data-table");
        
        // Colonne Référence
        TableColumn<ContractData, String> refCol = new TableColumn<>("Référence");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(120);
        
        // Colonne Client
        TableColumn<ContractData, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientCol.setPrefWidth(200);
        
        // Colonne Type
        TableColumn<ContractData, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);
        
        // Colonne Début
        TableColumn<ContractData, String> startCol = new TableColumn<>("Début");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(100);
        
        // Colonne Fin
        TableColumn<ContractData, String> endCol = new TableColumn<>("Fin");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(100);
        
        // Colonne Montant
        TableColumn<ContractData, String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);
        
        // Colonne Statut
        TableColumn<ContractData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        contractTable.getColumns().addAll(refCol, clientCol, typeCol, startCol, endCol, amountCol, statusCol);
    }
    
    private void createTestData() {
        contractList.addAll(
            new ContractData("CTR-001", "Théâtre Municipal", "Location", "01/01/2025", "31/12/2025", "12 000 €", "✅ Actif"),
            new ContractData("CTR-002", "Festival d'Été", "Prestation", "15/06/2025", "31/08/2025", "25 000 €", "✅ Actif"),
            new ContractData("CTR-003", "Salle Polyvalente", "Maintenance", "01/01/2025", "31/12/2025", "8 000 €", "✅ Actif"),
            new ContractData("CTR-004", "Entreprise Events Pro", "Location", "01/11/2024", "31/10/2025", "15 000 €", "⏳ En attente"),
            new ContractData("CTR-005", "Concert Hall", "Vente", "20/09/2024", "20/09/2024", "45 000 €", "✅ Actif"),
            new ContractData("CTR-006", "Mairie Centre", "Prestation", "01/03/2024", "31/05/2024", "18 000 €", "❌ Expiré"),
            new ContractData("CTR-007", "Studio Prod", "Maintenance", "01/01/2024", "31/12/2024", "6 500 €", "❌ Expiré"),
            new ContractData("CTR-008", "Association Culturelle", "Location", "15/09/2025", "15/12/2025", "9 000 €", "✅ Actif"),
            new ContractData("CTR-009", "Opéra National", "Prestation", "01/01/2025", "30/06/2025", "35 000 €", "✅ Actif"),
            new ContractData("CTR-010", "Centre Congrès", "Maintenance", "01/07/2025", "31/12/2025", "11 000 €", "⏳ En attente")
        );
        
        updateStatus(contractList.size() + " contrat(s) chargé(s)");
    }
    
    @Override
    protected void addCustomToolbarItems(ToolBar toolbar) {
        Button renewButton = new Button("🔄 Renouveler");
        Button exportButton = new Button("📄 Exporter");
        
        renewButton.setOnAction(e -> {
            ContractData selected = contractTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateStatus("Renouvellement du contrat " + selected.getReference());
                showAlert("Renouvellement", "Fonctionnalité de renouvellement - À implémenter");
            }
        });
        
        toolbar.getItems().addAll(new Separator(), renewButton, exportButton);
    }
    
    @Override
    protected void initializeContent() {
        updateStatus(contractList.size() + " contrat(s) chargé(s)");
    }
    
    @Override
    protected void handleAdd() {
        updateStatus("Création d'un nouveau contrat...");
        showAlert("Nouveau contrat", "Formulaire de création de contrat - À implémenter");
    }
    
    @Override
    protected void handleEdit() {
        ContractData selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun contrat sélectionné");
            return;
        }
        updateStatus("Édition du contrat " + selected.getReference());
        showAlert("Édition", "Formulaire d'édition du contrat " + selected.getReference() + " - À implémenter");
    }
    
    @Override
    protected void handleDelete() {
        ContractData selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun contrat sélectionné");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le contrat");
        confirm.setContentText("Confirmer la suppression du contrat " + selected.getReference() + " ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                contractList.remove(selected);
                updateStatus("Contrat supprimé");
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
    
    // Classe interne pour les données de contrat
    public static class ContractData {
        private final String reference;
        private final String clientName;
        private final String type;
        private final String startDate;
        private final String endDate;
        private final String amount;
        private final String status;
        
        public ContractData(String reference, String clientName, String type, String startDate,
                          String endDate, String amount, String status) {
            this.reference = reference;
            this.clientName = clientName;
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.amount = amount;
            this.status = status;
        }
        
        public String getReference() { return reference; }
        public String getClientName() { return clientName; }
        public String getType() { return type; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}
