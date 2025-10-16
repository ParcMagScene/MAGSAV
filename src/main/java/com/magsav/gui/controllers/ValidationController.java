package com.magsav.gui.controllers;

import com.magsav.service.RequestToOrderWorkflowService;
import com.magsav.util.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contrôleur spécialisé pour la gestion de l'interface de validation des demandes
 * Extrait du MainController pour améliorer la maintenabilité
 */
public class ValidationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);
    
    private final RequestToOrderWorkflowService workflowService;
    private final Connection dbConnection;
    
    // Composants UI
    private TableView<PendingRequest> validationTable;
    private VBox validationInfoBox;
    private Button validateRequestBtn;
    private Button rejectRequestBtn;
    private Button detailsRequestBtn;
    
    // Données
    private ObservableList<PendingRequest> pendingRequests;
    private PendingRequest selectedRequest;
    
    /**
     * Constructeur
     */
    public ValidationController(RequestToOrderWorkflowService workflowService, Connection dbConnection) {
        this.workflowService = workflowService;
        this.dbConnection = dbConnection;
        this.pendingRequests = FXCollections.observableArrayList();
    }
    
    /**
     * Crée l'onglet de validation des demandes
     */
    public Tab createValidationTab() {
        Tab validationTab = new Tab("✅ Validation Admin");
        validationTab.setClosable(false);
        
        VBox validationContent = new VBox(10);
        validationContent.setPadding(new Insets(15));
        validationContent.getStyleClass().add("content-pane");
        
        // Titre
        Label titleLabel = new Label("Validation des Demandes en Attente");
        titleLabel.getStyleClass().addAll("title-label", "validation-title");
        
        // Table des demandes
        validationTable = createPendingRequestsTable();
        
        // Panel d'informations et actions
        HBox actionPanel = createValidationPanel();
        
        validationContent.getChildren().addAll(titleLabel, validationTable, actionPanel);
        VBox.setVgrow(validationTable, Priority.ALWAYS);
        
        validationTab.setContent(validationContent);
        
        // Charger les données
        loadPendingRequests();
        
        return validationTab;
    }
    
    /**
     * Crée la table des demandes en attente
     */
    private TableView<PendingRequest> createPendingRequestsTable() {
        TableView<PendingRequest> table = new TableView<>();
        table.getStyleClass().add("validation-table");
        
        // Colonnes
        TableColumn<PendingRequest, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);
        
        TableColumn<PendingRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(100);
        
        TableColumn<PendingRequest, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(200);
        
        TableColumn<PendingRequest, String> requesterCol = new TableColumn<>("Demandeur");
        requesterCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequesterName()));
        requesterCol.setPrefWidth(150);
        
        TableColumn<PendingRequest, String> priorityCol = new TableColumn<>("Priorité");
        priorityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        priorityCol.setPrefWidth(100);
        
        TableColumn<PendingRequest, String> costCol = new TableColumn<>("Coût Estimé");
        costCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getEstimatedCost() != null ? 
            String.format("%.2f €", data.getValue().getEstimatedCost()) : "N/A"
        ));
        costCol.setPrefWidth(120);
        
        TableColumn<PendingRequest, String> dateCol = new TableColumn<>("Date Création");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt()));
        dateCol.setPrefWidth(150);
        
        table.getColumns().add(idCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(requesterCol);
        table.getColumns().add(priorityCol);
        table.getColumns().add(costCol);
        table.getColumns().add(dateCol);
        
        // Sélection
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> onRequestSelected(newSelection)
        );
        
        // Double-clic pour détails
        table.setRowFactory(tv -> {
            TableRow<PendingRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showRequestDetails(row.getItem());
                }
            });
            return row;
        });
        
        table.setItems(pendingRequests);
        return table;
    }
    
    /**
     * Crée le panel de validation avec informations et boutons d'action
     */
    private HBox createValidationPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(10));
        panel.getStyleClass().add("validation-panel");
        
        // Zone d'informations
        validationInfoBox = new VBox(5);
        validationInfoBox.getStyleClass().add("info-box");
        validationInfoBox.setPrefWidth(400);
        
        Label infoLabel = new Label("Sélectionnez une demande pour voir les détails");
        infoLabel.getStyleClass().add("info-text");
        validationInfoBox.getChildren().add(infoLabel);
        
        // Boutons d'action
        VBox buttonBox = new VBox(10);
        buttonBox.setPrefWidth(200);
        
        validateRequestBtn = new Button("✅ Valider et Créer Commande");
        validateRequestBtn.getStyleClass().addAll("action-button", "validate-button");
        validateRequestBtn.setMaxWidth(Double.MAX_VALUE);
        validateRequestBtn.setDisable(true);
        validateRequestBtn.setOnAction(e -> validateSelectedRequest());
        
        rejectRequestBtn = new Button("❌ Rejeter la Demande");
        rejectRequestBtn.getStyleClass().addAll("action-button", "reject-button");
        rejectRequestBtn.setMaxWidth(Double.MAX_VALUE);
        rejectRequestBtn.setDisable(true);
        rejectRequestBtn.setOnAction(e -> rejectSelectedRequest());
        
        detailsRequestBtn = new Button("📋 Voir Détails");
        detailsRequestBtn.getStyleClass().addAll("action-button", "details-button");
        detailsRequestBtn.setMaxWidth(Double.MAX_VALUE);
        detailsRequestBtn.setDisable(true);
        detailsRequestBtn.setOnAction(e -> showRequestDetails(selectedRequest));
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.getStyleClass().addAll("action-button", "refresh-button");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> loadPendingRequests());
        
        buttonBox.getChildren().addAll(validateRequestBtn, rejectRequestBtn, detailsRequestBtn, refreshBtn);
        
        panel.getChildren().addAll(validationInfoBox, buttonBox);
        HBox.setHgrow(validationInfoBox, Priority.ALWAYS);
        
        return panel;
    }
    
    /**
     * Gestionnaire de sélection d'une demande
     */
    private void onRequestSelected(PendingRequest request) {
        selectedRequest = request;
        
        if (request != null) {
            // Activer les boutons
            validateRequestBtn.setDisable(false);
            rejectRequestBtn.setDisable(false);
            detailsRequestBtn.setDisable(false);
            
            // Afficher les informations
            updateInfoBox(request);
        } else {
            // Désactiver les boutons
            validateRequestBtn.setDisable(true);
            rejectRequestBtn.setDisable(true);
            detailsRequestBtn.setDisable(true);
            
            // Effacer les informations
            validationInfoBox.getChildren().clear();
            Label infoLabel = new Label("Sélectionnez une demande pour voir les détails");
            infoLabel.getStyleClass().add("info-text");
            validationInfoBox.getChildren().add(infoLabel);
        }
    }
    
    /**
     * Met à jour la zone d'informations avec les détails de la demande
     */
    private void updateInfoBox(PendingRequest request) {
        validationInfoBox.getChildren().clear();
        
        Label titleLabel = new Label("📋 " + request.getTitle());
        titleLabel.getStyleClass().addAll("info-title", request.getPriority().toLowerCase() + "-priority");
        
        Label typeLabel = new Label("Type: " + request.getType());
        Label requesterLabel = new Label("Demandeur: " + request.getRequesterName());
        Label priorityLabel = new Label("Priorité: " + request.getPriority());
        Label costLabel = new Label("Coût estimé: " + 
            (request.getEstimatedCost() != null ? String.format("%.2f €", request.getEstimatedCost()) : "N/A"));
        Label dateLabel = new Label("Créée le: " + request.getCreatedAt());
        
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            Label descLabel = new Label("Description:");
            descLabel.getStyleClass().add("info-label");
            TextArea descArea = new TextArea(request.getDescription());
            descArea.setEditable(false);
            descArea.setPrefRowCount(3);
            descArea.getStyleClass().add("description-area");
            
            validationInfoBox.getChildren().addAll(titleLabel, typeLabel, requesterLabel, priorityLabel, 
                costLabel, dateLabel, descLabel, descArea);
        } else {
            validationInfoBox.getChildren().addAll(titleLabel, typeLabel, requesterLabel, priorityLabel, 
                costLabel, dateLabel);
        }
        
        // Styliser les labels
        typeLabel.getStyleClass().add("info-text");
        requesterLabel.getStyleClass().add("info-text");
        priorityLabel.getStyleClass().add("info-text");
        costLabel.getStyleClass().add("info-text");
        dateLabel.getStyleClass().add("info-text");
    }
    
    /**
     * Charge les demandes en attente depuis la base de données
     */
    private void loadPendingRequests() {
        try {
            pendingRequests.clear();
            
            String sql = """
                SELECT id, type, title, description, priority, requester_name, requester_email, 
                       estimated_cost, created_at 
                FROM requests 
                WHERE status = 'EN_ATTENTE' 
                ORDER BY 
                    CASE priority 
                        WHEN 'URGENTE' THEN 1
                        WHEN 'HAUTE' THEN 2
                        WHEN 'NORMALE' THEN 3
                        WHEN 'BASSE' THEN 4
                        ELSE 5
                    END,
                    created_at DESC
                """;
            
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    PendingRequest request = new PendingRequest(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("requester_name"),
                        rs.getString("requester_email"),
                        rs.getDouble("estimated_cost"),
                        rs.getString("created_at")
                    );
                    pendingRequests.add(request);
                }
            }
            
            logger.info("Chargé {} demandes en attente de validation", pendingRequests.size());
            
        } catch (SQLException e) {
            logger.error("Erreur lors du chargement des demandes en attente", e);
            DialogUtils.showErrorAlert("Erreur", "Impossible de charger les demandes en attente");
        }
    }
    
    /**
     * Valide la demande sélectionnée et crée la commande fournisseur
     */
    private void validateSelectedRequest() {
        if (selectedRequest == null) return;
        
        String confirmMessage = String.format(
            "Confirmer la validation de la demande:\n\n" +
            "• %s\n" +
            "• Demandeur: %s\n" +
            "• Coût estimé: %s\n\n" +
            "Cette action créera automatiquement la commande fournisseur.",
            selectedRequest.getTitle(),
            selectedRequest.getRequesterName(),
            selectedRequest.getEstimatedCost() != null ? 
                String.format("%.2f €", selectedRequest.getEstimatedCost()) : "N/A"
        );
        
        if (DialogUtils.showConfirmationAlert("Validation Demande", confirmMessage)) {
            try {
                workflowService.validateRequestAndCreateOrders((long) selectedRequest.getId(), "Admin validation");
                DialogUtils.showInfo("Succès", "Demande validée et commande créée avec succès");
                loadPendingRequests(); // Recharger la liste
                
            } catch (Exception e) {
                logger.error("Erreur lors de la validation de la demande " + selectedRequest.getId(), e);
                DialogUtils.showErrorAlert("Erreur", "Erreur lors de la validation: " + e.getMessage());
            }
        }
    }
    
    /**
     * Rejette la demande sélectionnée
     */
    private void rejectSelectedRequest() {
        if (selectedRequest == null) return;
        
        String confirmMessage = String.format(
            "Confirmer le rejet de la demande:\n\n" +
            "• %s\n" +
            "• Demandeur: %s\n\n" +
            "Cette action marquera la demande comme refusée.",
            selectedRequest.getTitle(),
            selectedRequest.getRequesterName()
        );
        
        if (DialogUtils.showConfirmationAlert("Rejet Demande", confirmMessage)) {
            try {
                String sql = "UPDATE requests SET status = 'REFUSEE', updated_at = datetime('now') WHERE id = ?";
                try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                    stmt.setInt(1, selectedRequest.getId());
                    stmt.executeUpdate();
                }
                
                DialogUtils.showInfo("Succès", "Demande rejetée avec succès");
                loadPendingRequests(); // Recharger la liste
                
            } catch (SQLException e) {
                logger.error("Erreur lors du rejet de la demande " + selectedRequest.getId(), e);
                DialogUtils.showErrorAlert("Erreur", "Erreur lors du rejet: " + e.getMessage());
            }
        }
    }
    
    /**
     * Affiche les détails complets d'une demande
     */
    private void showRequestDetails(PendingRequest request) {
        if (request == null) return;
        
        try {
            // Charger les items de la demande
            String sql = """
                SELECT ri.name, ri.description, ri.quantity, ri.unit_price, ri.total_price, ri.item_type
                FROM request_items ri 
                WHERE ri.request_id = ?
                ORDER BY ri.id
                """;
            
            StringBuilder details = new StringBuilder();
            details.append("=== DÉTAILS DE LA DEMANDE ===\n\n");
            details.append("ID: ").append(request.getId()).append("\n");
            details.append("Type: ").append(request.getType()).append("\n");
            details.append("Titre: ").append(request.getTitle()).append("\n");
            details.append("Demandeur: ").append(request.getRequesterName()).append("\n");
            details.append("Email: ").append(request.getRequesterEmail()).append("\n");
            details.append("Priorité: ").append(request.getPriority()).append("\n");
            details.append("Coût estimé: ").append(
                request.getEstimatedCost() != null ? String.format("%.2f €", request.getEstimatedCost()) : "N/A"
            ).append("\n");
            details.append("Date création: ").append(request.getCreatedAt()).append("\n\n");
            
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                details.append("Description:\n").append(request.getDescription()).append("\n\n");
            }
            
            details.append("=== ÉLÉMENTS DEMANDÉS ===\n\n");
            
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setInt(1, request.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    
                    if (!rs.next()) {
                        details.append("Aucun élément trouvé pour cette demande.\n");
                    } else {
                        int itemCount = 1;
                        do {
                            details.append(itemCount++).append(". ");
                            details.append(rs.getString("name"));
                            
                            String description = rs.getString("description");
                            if (description != null && !description.trim().isEmpty()) {
                                details.append("\n   Description: ").append(description);
                            }
                            
                            details.append("\n   Type: ").append(rs.getString("item_type"));
                            details.append("\n   Quantité: ").append(rs.getInt("quantity"));
                            
                            double unitPrice = rs.getDouble("unit_price");
                            if (unitPrice > 0) {
                                details.append("\n   Prix unitaire: ").append(String.format("%.2f €", unitPrice));
                                double totalPrice = rs.getDouble("total_price");
                                details.append("\n   Prix total: ").append(String.format("%.2f €", totalPrice));
                            }
                            
                            details.append("\n\n");
                            
                        } while (rs.next());
                    }
                }
            }
            
            // Afficher dans une dialog avec zone de texte
            Dialog<Void> detailsDialog = new Dialog<>();
            detailsDialog.setTitle("Détails de la Demande #" + request.getId());
            detailsDialog.setHeaderText(request.getTitle());
            
            TextArea textArea = new TextArea(details.toString());
            textArea.setEditable(false);
            textArea.setPrefRowCount(25);
            textArea.setPrefColumnCount(80);
            textArea.getStyleClass().add("details-text-area");
            
            detailsDialog.getDialogPane().setContent(textArea);
            detailsDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            DialogUtils.applyDarkTheme(detailsDialog);
            detailsDialog.showAndWait();
            
        } catch (SQLException e) {
            logger.error("Erreur lors de l'affichage des détails de la demande " + request.getId(), e);
            DialogUtils.showErrorAlert("Erreur", "Impossible d'afficher les détails de la demande");
        }
    }
    
    /**
     * Classe représentant une demande en attente
     */
    public static class PendingRequest {
        private final int id;
        private final String type;
        private final String title;
        private final String description;
        private final String priority;
        private final String requesterName;
        private final String requesterEmail;
        private final Double estimatedCost;
        private final String createdAt;
        
        public PendingRequest(int id, String type, String title, String description, String priority,
                             String requesterName, String requesterEmail, Double estimatedCost, String createdAt) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.requesterName = requesterName;
            this.requesterEmail = requesterEmail;
            this.estimatedCost = estimatedCost;
            this.createdAt = createdAt;
        }
        
        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getPriority() { return priority; }
        public String getRequesterName() { return requesterName; }
        public String getRequesterEmail() { return requesterEmail; }
        public Double getEstimatedCost() { return estimatedCost; }
        public String getCreatedAt() { return createdAt; }
    }
}