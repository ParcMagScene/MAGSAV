package com.magsav.gui;

import com.magsav.model.RequestItem;
import com.magsav.db.DB;
import com.magsav.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contrôleur pour afficher les détails d'une demande
 */
public class RequestDetailController implements Initializable {
    
    // Labels d'information
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;
    @FXML private Label lblId;
    @FXML private Label lblType;
    @FXML private Label lblPriority;
    @FXML private Label lblAssignedTo;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblValidatedAt;
    @FXML private Label lblRequesterName;
    @FXML private Label lblRequesterEmail;
    @FXML private Label lblRequesterPhone;
    @FXML private Label lblSociete;
    @FXML private Label lblEstimatedCost;
    @FXML private Label lblActualCost;
    
    // Zones de texte
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtComments;
    
    // Table des éléments
    @FXML private TableView<RequestItem> tableItems;
    @FXML private TableColumn<RequestItem, String> colItemRef;
    @FXML private TableColumn<RequestItem, Integer> colItemQty;
    @FXML private TableColumn<RequestItem, String> colItemDescription;
    
    // Boutons
    @FXML private Button btnEdit;
    @FXML private Button btnValidate;
    @FXML private Button btnReject;
    @FXML private Button btnClose;
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private long requestId;
    private RequestData currentRequest;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupButtons();
    }
    
    /**
     * Configuration de la table des éléments
     */
    private void setupTable() {
        colItemRef.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().ref()));
        colItemQty.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().qty()));
        colItemDescription.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().description()));
    }
    
    /**
     * Configuration des boutons
     */
    private void setupButtons() {
        btnEdit.setOnAction(e -> onEdit());
        btnValidate.setOnAction(e -> onValidate());
        btnReject.setOnAction(e -> onReject());
    }
    
    /**
     * Charge les données d'une demande
     */
    public void loadRequest(long requestId) {
        this.requestId = requestId;
        
        try {
            currentRequest = loadRequestData(requestId);
            if (currentRequest != null) {
                displayRequestData();
                loadRequestItems();
            } else {
                showError("Demande non trouvée", "Aucune demande trouvée avec l'ID: " + requestId);
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement de la demande: " + e.getMessage(), e);
            showError("Erreur", "Impossible de charger les données de la demande: " + e.getMessage());
        }
    }
    
    /**
     * Affiche les données de la demande dans l'interface
     */
    private void displayRequestData() {
        lblTitle.setText(currentRequest.title);
        lblId.setText(String.valueOf(currentRequest.id));
        lblType.setText(formatType(currentRequest.type));
        lblStatus.setText(formatStatus(currentRequest.status));
        
        // Style du statut
        updateStatusStyle(currentRequest.status);
        
        lblPriority.setText(formatPriority(currentRequest.priority));
        lblAssignedTo.setText(currentRequest.assignedTo != null ? currentRequest.assignedTo : "Non assigné");
        
        lblCreatedAt.setText(formatDateTime(currentRequest.createdAt));
        lblValidatedAt.setText(currentRequest.validatedAt != null ? formatDateTime(currentRequest.validatedAt) : "-");
        
        lblRequesterName.setText(currentRequest.requesterName != null ? currentRequest.requesterName : "-");
        lblRequesterEmail.setText(currentRequest.requesterEmail != null ? currentRequest.requesterEmail : "-");
        lblRequesterPhone.setText(currentRequest.requesterPhone != null ? currentRequest.requesterPhone : "-");
        lblSociete.setText(currentRequest.societeName != null ? currentRequest.societeName : "-");
        
        txtDescription.setText(currentRequest.description != null ? currentRequest.description : "");
        txtComments.setText(currentRequest.comments != null ? currentRequest.comments : "");
        
        lblEstimatedCost.setText(currentRequest.estimatedCost != null ? 
            currencyFormat.format(currentRequest.estimatedCost) : "-");
        lblActualCost.setText(currentRequest.actualCost != null ? 
            currencyFormat.format(currentRequest.actualCost) : "-");
    }
    
    /**
     * Met à jour le style du label de statut
     */
    private void updateStatusStyle(String status) {
        lblStatus.getStyleClass().clear();
        lblStatus.getStyleClass().add("status-label");
        
        switch (status) {
            case "EN_ATTENTE":
                lblStatus.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "EN_COURS":
                lblStatus.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "VALIDEE":
                lblStatus.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "REFUSEE":
                lblStatus.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "TERMINEE":
                lblStatus.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            default:
                lblStatus.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
        }
    }
    
    /**
     * Charge les éléments de la demande
     */
    private void loadRequestItems() {
        try {
            List<RequestItem> items = loadRequestItemsData(requestId);
            ObservableList<RequestItem> itemList = FXCollections.observableArrayList(items);
            tableItems.setItems(itemList);
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des éléments de la demande: " + e.getMessage(), e);
        }
    }
    
    /**
     * Charge les données d'une demande depuis la base de données
     */
    private RequestData loadRequestData(long requestId) throws SQLException {
        String sql = """
            SELECT r.id, r.type, r.title, r.description, r.status, r.priority,
                   r.requester_name, r.requester_email, r.requester_phone,
                   r.assigned_to, r.estimated_cost, r.actual_cost, r.comments,
                   r.created_at, r.updated_at, r.validated_at, r.completed_at,
                   s.nom_societe as societe_name
            FROM requests r
            LEFT JOIN societes s ON r.societe_id = s.id
            WHERE r.id = ?
            """;
            
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, requestId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RequestData(
                        rs.getLong("id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("priority"),
                        rs.getString("requester_name"),
                        rs.getString("requester_email"),
                        rs.getString("requester_phone"),
                        rs.getString("assigned_to"),
                        parseDoubleOrZero(rs.getString("estimated_cost")),
                        parseDoubleOrZero(rs.getString("actual_cost")),
                        rs.getString("comments"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getString("validated_at"),
                        rs.getString("completed_at"),
                        rs.getString("societe_name")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Charge les éléments d'une demande
     */
    private List<RequestItem> loadRequestItemsData(long requestId) throws SQLException {
        List<RequestItem> items = new ArrayList<>();
        String sql = "SELECT id, request_id, reference, quantity, description FROM request_items WHERE request_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, requestId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new RequestItem(
                        rs.getLong("id"),
                        rs.getLong("request_id"),
                        rs.getString("reference"),
                        rs.getInt("quantity"),
                        rs.getString("description")
                    ));
                }
            }
        }
        return items;
    }
    
    /**
     * Formate le type de demande
     */
    private String formatType(String type) {
        return switch (type) {
            case "INTERVENTION" -> "Intervention";
            case "PIECES" -> "Pièces";
            case "MATERIEL" -> "Matériel";
            case "SAV_EXTERNE" -> "SAV Externe";
            case "DEVIS" -> "Devis";
            case "PRIX" -> "Prix";
            default -> type;
        };
    }
    
    /**
     * Formate le statut
     */
    private String formatStatus(String status) {
        return switch (status) {
            case "EN_ATTENTE" -> "En attente";
            case "EN_COURS" -> "En cours";
            case "VALIDEE" -> "Validée";
            case "REFUSEE" -> "Refusée";
            case "TERMINEE" -> "Terminée";
            default -> status;
        };
    }
    
    /**
     * Formate la priorité
     */
    private String formatPriority(String priority) {
        return switch (priority != null ? priority : "NORMALE") {
            case "BASSE" -> "Basse";
            case "NORMALE" -> "Normale";
            case "HAUTE" -> "Haute";
            case "URGENTE" -> "Urgente";
            default -> priority;
        };
    }
    
    /**
     * Formate une date/heure
     */
    private String formatDateTime(String dateTime) {
        if (dateTime == null) return "-";
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime.replace(" ", "T"));
            return dt.format(dateFormatter);
        } catch (Exception e) {
            return dateTime;
        }
    }
    
    /**
     * Actions des boutons
     */
    @FXML
    private void onEdit() {
        // TODO: Implémenter l'édition de la demande
        showInfo("Édition", "Fonctionnalité d'édition en cours de développement");
    }
    
    @FXML
    private void onValidate() {
        // TODO: Implémenter la validation de la demande
        showInfo("Validation", "Fonctionnalité de validation en cours de développement");
    }
    
    @FXML
    private void onReject() {
        // TODO: Implémenter le rejet de la demande
        showInfo("Rejet", "Fonctionnalité de rejet en cours de développement");
    }
    
    @FXML
    private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche un message d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Classe pour stocker les données de la demande
     */
    private record RequestData(
        long id,
        String type,
        String title,
        String description,
        String status,
        String priority,
        String requesterName,
        String requesterEmail,
        String requesterPhone,
        String assignedTo,
        Double estimatedCost,
        Double actualCost,
        String comments,
        String createdAt,
        String updatedAt,
        String validatedAt,
        String completedAt,
        String societeName
    ) {}
    
    /**
     * Parse un string en double, retourne 0.0 si le parsing échoue ou si la valeur est null
     */
    @FXML
    private void saveRequest() {
        // TODO: Implémenter la sauvegarde de la demande
        System.out.println("Sauvegarde demande");
    }
    
    @FXML
    private void cancel() {
        // Fermer la fenêtre
        if (btnEdit != null && btnEdit.getScene() != null) {
            btnEdit.getScene().getWindow().hide();
        }
    }

    private static double parseDoubleOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}