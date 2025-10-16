package com.magsav.gui.controllers;

import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.dto.RequestRow;
import com.magsav.service.data.DataServiceManager;
import com.magsav.service.data.UnifiedRequestService;
import com.magsav.service.Refreshable;
import com.magsav.service.NavigationService;
import com.magsav.util.AppLogger;
import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory.DetailPane;
import com.magsav.gui.components.DetailPaneFactory.EntityInfo;

import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.collections.FXCollections;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Contrôleur dédié à la section Demandes
 * Gère les onglets Pièces, Matériel, Interventions, Validées, Refusées
 */
public class DemandesController implements Refreshable {
    
    private final DataServiceManager dataManager = DataServiceManager.getInstance();
    private final UnifiedRequestService unifiedRequestService = new UnifiedRequestService();
    
    // Stocker les références aux tables pour pouvoir les rafraîchir
    private TableView<RequestRow> piecesTable;
    private TableView<RequestRow> materielTable;
    private TableView<RequestRow> interventionsTable;
    private TableView<RequestRow> valideesTable;
    private TableView<RequestRow> refuseesTable;
    
    // Panneau de détail unifié
    private DetailPane currentDetailPane;
    
    /**
     * Crée l'onglet Demandes de pièces
     */
    public Tab createDemandesPiecesTab() {
        Tab tab = TabBuilderUtils.createBasicTab("🔧 Pièces");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Demandes de pièces");
        title.getStyleClass().add("content-title");
        
        // Contrôles
        HBox controlsBox = createRequestControlsBox();
        
        // Volet de visualisation avec détails
        DetailPane visualizationPane = createVisualizationPane("PIECES");
        
        // Configuration de la table
        piecesTable = new TableView<>();
        piecesTable.getStyleClass().add("dark-table-view");
        setupRequestTableColumns(piecesTable);
        
        // Configuration du layout
        HBox mainLayout = new HBox(10);
        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().addAll(controlsBox, piecesTable);
        VBox.setVgrow(piecesTable, Priority.ALWAYS);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(leftPanel, visualizationPane);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        content.getChildren().addAll(title, mainLayout);
        
        // Chargement des données
        loadRequestsData(piecesTable, "PIECES");
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Demandes de matériel
     */
    public Tab createDemandesMaterielTab() {
        Tab tab = TabBuilderUtils.createBasicTab("📦 Matériel");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Demandes de matériel");
        title.getStyleClass().add("content-title");
        
        // Contrôles
        HBox controlsBox = createRequestControlsBox();
        
        // Volet de visualisation avec détails
        DetailPane visualizationPane = createVisualizationPane("MATERIEL");
        
        // Configuration de la table
        materielTable = new TableView<>();
        materielTable.getStyleClass().add("dark-table-view");
        setupRequestTableColumns(materielTable);
        
        // Configuration du layout
        HBox mainLayout = new HBox(10);
        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().addAll(controlsBox, materielTable);
        VBox.setVgrow(materielTable, Priority.ALWAYS);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(leftPanel, visualizationPane);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        content.getChildren().addAll(title, mainLayout);
        
        // Chargement des données
        loadRequestsData(materielTable, "MATERIEL");
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Demandes d'intervention
     */
    public Tab createDemandesInterventionsTab() {
        Tab tab = TabBuilderUtils.createBasicTab("🔧 Interventions");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Demandes d'intervention");
        title.getStyleClass().add("content-title");
        
        // Contrôles
        HBox controlsBox = createRequestControlsBox();
        
        // Volet de visualisation avec détails
        DetailPane visualizationPane = createVisualizationPane("INTERVENTIONS");
        
        // Configuration de la table
        interventionsTable = new TableView<>();
        interventionsTable.getStyleClass().add("dark-table-view");
        setupRequestTableColumns(interventionsTable);
        
        // Configuration du layout
        HBox mainLayout = new HBox(10);
        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().addAll(controlsBox, interventionsTable);
        VBox.setVgrow(interventionsTable, Priority.ALWAYS);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(leftPanel, visualizationPane);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        content.getChildren().addAll(title, mainLayout);
        
        // Chargement des données
        loadRequestsData(interventionsTable, "INTERVENTIONS");
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Demandes validées
     */
    public Tab createDemandesValideesTab() {
        Tab tab = TabBuilderUtils.createBasicTab("✅ Validées");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Demandes validées");
        title.getStyleClass().add("content-title");
        
        valideesTable = new TableView<>();
        valideesTable.getStyleClass().add("dark-table-view");
        setupRequestTableColumns(valideesTable);
        
        Label placeholder = new Label("Aucune demande validée");
        placeholder.getStyleClass().add("table-placeholder");
        valideesTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(valideesTable, Priority.ALWAYS);
        content.getChildren().addAll(title, valideesTable);
        
        // Chargement des données filtrées par statut
        loadRequestsDataByStatus(valideesTable, "VALIDEE");
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Demandes refusées
     */
    public Tab createDemandesRefuseesTab() {
        Tab tab = TabBuilderUtils.createBasicTab("❌ Refusées");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Demandes refusées");
        title.getStyleClass().add("content-title");
        
        refuseesTable = new TableView<>();
        refuseesTable.getStyleClass().add("dark-table-view");
        setupRequestTableColumns(refuseesTable);
        
        Label placeholder = new Label("Aucune demande refusée");
        placeholder.getStyleClass().add("table-placeholder");
        refuseesTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(refuseesTable, Priority.ALWAYS);
        content.getChildren().addAll(title, refuseesTable);
        
        // Chargement des données filtrées par statut
        loadRequestsDataByStatus(refuseesTable, "REFUSEE");
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée un volet de visualisation unifié avec le système centralisé
     */
    private DetailPane createVisualizationPane(String requestType) {
        // Créer le panneau avec le système unifié
        currentDetailPane = DetailLayoutHelper.createDemandeVisualizationPane(() -> {
            AppLogger.info("Ouverture de la fiche détaillée pour demande " + requestType);
            // TODO: Ouvrir la fiche détaillée correspondante
        });
        
        // Initialiser avec des informations par défaut
        EntityInfo defaultInfo = new EntityInfo("Aucune demande sélectionnée")
                .reference("Sélectionnez une ligne")
                .category(requestType)
                .status("En attente de sélection")
                .description("Double-cliquez sur une demande dans la table pour voir ses détails ici.");
                
        currentDetailPane.updateInfo(defaultInfo);
        
        return currentDetailPane;
    }
    
    private void setupRequestTableColumns(TableView<RequestRow> table) {
        TableColumn<RequestRow, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().id())));
        idColumn.setPrefWidth(60);
        
        TableColumn<RequestRow, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().type()));
        typeColumn.setPrefWidth(100);
        
        TableColumn<RequestRow, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().status()));
        statusColumn.setPrefWidth(100);
        
        TableColumn<RequestRow, String> fournisseurColumn = new TableColumn<>("Fournisseur");
        fournisseurColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(Optional.ofNullable(cellData.getValue().fournisseurNom()).orElse("Non défini")));
        fournisseurColumn.setPrefWidth(120);
        
        TableColumn<RequestRow, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().createdAt()));
        dateColumn.setPrefWidth(100);
        
        TableColumn<RequestRow, String> commentColumn = new TableColumn<>("Commentaire");
        commentColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(Optional.ofNullable(cellData.getValue().commentaire()).orElse("")));
        commentColumn.setPrefWidth(200);
        
        table.getColumns().addAll(Arrays.asList(idColumn, typeColumn, statusColumn, fournisseurColumn, dateColumn, commentColumn));
        
        // Configuration du placeholder
        Label placeholder = new Label("Aucune demande trouvée");
        placeholder.getStyleClass().add("table-placeholder");
        table.setPlaceholder(placeholder);
        
        // Listener de sélection pour mettre à jour le panneau de détail
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && currentDetailPane != null) {
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromRequest(newSelection);
                currentDetailPane.updateInfo(entityInfo);
            }
        });
        
        // Configuration du redimensionnement et double-clic pour fiche détaillée
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Double-clic pour ouvrir la fiche détaillée
        table.setRowFactory(tv -> {
            TableRow<RequestRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RequestRow request = row.getItem();
                    AppLogger.info("Double-clic sur demande ID: {}", request.id());
                    NavigationService.openRequestDetail(request.id());
                }
            });
            return row;
        });
    }
    
    private HBox createRequestControlsBox() {
        HBox controlsBox = new HBox(10);
        controlsBox.getStyleClass().add("controls-box");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.getStyleClass().add("search-field");
        
        Button searchBtn = new Button("🔍");
        searchBtn.getStyleClass().add("icon-button");
        
        controlsBox.getChildren().addAll(searchField, searchBtn);
        
        return controlsBox;
    }
    
    private void loadRequestsData(TableView<RequestRow> table, String requestType) {
        System.out.println("🔄 DEBUG DemandesController - Chargement type: " + requestType);
        
        try {
            // Utiliser le service unifié qui gère les trois types différemment
            List<RequestRow> requests = unifiedRequestService.loadRequestsByType(requestType);
            
            System.out.println("🔄 DEBUG DemandesController - Demandes reçues: " + requests.size());
            
            table.setItems(FXCollections.observableArrayList(requests));
            
            System.out.println("🔄 DEBUG DemandesController - Items ajoutés à la table: " + requests.size());
            
            AppLogger.info("Chargement de {} demandes de type {}", requests.size(), requestType);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des demandes: " + e.getMessage(), e);
            table.setItems(FXCollections.observableArrayList());
        }
    }
    
    private void loadRequestsDataByStatus(TableView<RequestRow> table, String status) {
        try {
            List<RequestRow> allRequests = new ArrayList<>();
            // Utiliser le service unifié pour charger tous les types
            allRequests.addAll(unifiedRequestService.loadRequestsByType("PIECES"));
            allRequests.addAll(unifiedRequestService.loadRequestsByType("MATERIEL"));
            allRequests.addAll(unifiedRequestService.loadRequestsByType("INTERVENTIONS"));
            
            List<RequestRow> filteredRequests = allRequests.stream()
                .filter(request -> status.equals(request.status()))
                .toList();
                
            table.setItems(FXCollections.observableArrayList(filteredRequests));
            
            AppLogger.info("Chargement de {} demandes avec statut {}", filteredRequests.size(), status);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des demandes: " + e.getMessage(), e);
            table.setItems(FXCollections.observableArrayList());
        }
    }
    
    @Override
    public void refreshAllTables() {
        AppLogger.info("Rafraîchissement des données des demandes");
        
        if (piecesTable != null) {
            loadRequestsData(piecesTable, "PIECES");
        }
        if (materielTable != null) {
            loadRequestsData(materielTable, "MATERIEL");
        }
        if (interventionsTable != null) {
            loadRequestsData(interventionsTable, "INTERVENTIONS");
        }
        if (valideesTable != null) {
            loadRequestsDataByStatus(valideesTable, "VALIDEE");
        }
        if (refuseesTable != null) {
            loadRequestsDataByStatus(refuseesTable, "REFUSEE");
        }
    }
    
    @Override
    public String getComponentName() {
        return "DemandesController";
    }
    
    @Override
    public boolean isReadyForRefresh() {
        return true;
    }
}
