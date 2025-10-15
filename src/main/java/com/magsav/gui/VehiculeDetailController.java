package com.magsav.gui;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contrôleur pour afficher les détails d'un véhicule
 */
public class VehiculeDetailController implements Initializable {
    
    // Labels d'information
    @FXML private Label lblTitle;
    @FXML private Label lblStatut;
    @FXML private Label lblId;
    @FXML private Label lblImmatriculation;
    @FXML private Label lblType;
    @FXML private Label lblMarque;
    @FXML private Label lblModele;
    @FXML private Label lblAnnee;
    @FXML private Label lblKilometrage;
    @FXML private Label lblCarburant;
    @FXML private Label lblPuissance;
    @FXML private Label lblPtac;
    @FXML private Label lblControleTechnique;
    @FXML private Label lblAssurance;
    @FXML private Label lblVisitePeriodique;
    @FXML private Label lblRevision;
    
    // Zone de texte
    @FXML private TextArea txtNotes;
    
    // Table des planifications
    @FXML private TableView<PlanificationRow> tablePlanifications;
    @FXML private TableColumn<PlanificationRow, String> colPlanifDate;
    @FXML private TableColumn<PlanificationRow, String> colPlanifTechnicien;
    @FXML private TableColumn<PlanificationRow, String> colPlanifIntervention;
    @FXML private TableColumn<PlanificationRow, String> colPlanifStatut;
    
    // Boutons
    @FXML private Button btnEdit;
    @FXML private Button btnMaintenance;
    @FXML private Button btnPlanifier;
    @FXML private Button btnClose;
    
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private int vehiculeId;
    private VehiculeData currentVehicule;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupButtons();
    }
    
    /**
     * Configuration de la table des planifications
     */
    private void setupTable() {
        colPlanifDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().date()));
        colPlanifTechnicien.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().technicien()));
        colPlanifIntervention.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().intervention()));
        colPlanifStatut.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().statut()));
    }
    
    /**
     * Configuration des boutons
     */
    private void setupButtons() {
        btnEdit.setOnAction(e -> onEdit());
        btnMaintenance.setOnAction(e -> onMaintenance());
        btnPlanifier.setOnAction(e -> onPlanifier());
    }
    
    /**
     * Charge les données d'un véhicule
     */
    public void loadVehicule(int vehiculeId) {
        this.vehiculeId = vehiculeId;
        
        try {
            currentVehicule = loadVehiculeData(vehiculeId);
            if (currentVehicule != null) {
                displayVehiculeData();
                loadPlanifications();
            } else {
                showError("Véhicule non trouvé", "Aucun véhicule trouvé avec l'ID: " + vehiculeId);
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement du véhicule: " + e.getMessage(), e);
            showError("Erreur", "Impossible de charger les données du véhicule: " + e.getMessage());
        }
    }
    
    /**
     * Affiche les données du véhicule dans l'interface
     */
    private void displayVehiculeData() {
        lblTitle.setText("Détails du véhicule - " + currentVehicule.immatriculation);
        lblId.setText(String.valueOf(currentVehicule.id));
        lblImmatriculation.setText(currentVehicule.immatriculation);
        lblType.setText(formatType(currentVehicule.typeVehicule));
        lblMarque.setText(currentVehicule.marque != null ? currentVehicule.marque : "-");
        lblModele.setText(currentVehicule.modele != null ? currentVehicule.modele : "-");
        lblAnnee.setText(currentVehicule.annee != null ? String.valueOf(currentVehicule.annee) : "-");
        
        lblKilometrage.setText(numberFormat.format(currentVehicule.kilometrage) + " km");
        lblCarburant.setText(currentVehicule.carburant != null ? currentVehicule.carburant : "-");
        lblPuissance.setText(currentVehicule.puissance != null ? currentVehicule.puissance + " ch" : "-");
        lblPtac.setText(currentVehicule.ptac != null ? numberFormat.format(currentVehicule.ptac) + " kg" : "-");
        
        // Dates de contrôles
        lblControleTechnique.setText(formatDate(currentVehicule.controleTechnique));
        lblAssurance.setText(formatDate(currentVehicule.assurance));
        lblVisitePeriodique.setText(formatDate(currentVehicule.visitePeriodique));
        lblRevision.setText(formatDate(currentVehicule.revision));
        
        txtNotes.setText(currentVehicule.notes != null ? currentVehicule.notes : "Aucune note");
        
        // Style du statut
        updateStatutStyle(currentVehicule.statut);
    }
    
    /**
     * Met à jour le style du label de statut
     */
    private void updateStatutStyle(String statut) {
        lblStatut.setText(formatStatut(statut));
        
        switch (statut) {
            case "DISPONIBLE":
                lblStatut.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "EN_MISSION":
                lblStatut.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "MAINTENANCE":
                lblStatut.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "HORS_SERVICE":
                lblStatut.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            case "RESERVE":
                lblStatut.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
                break;
            default:
                lblStatut.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 3px;");
        }
    }
    
    /**
     * Charge les planifications du véhicule
     */
    private void loadPlanifications() {
        try {
            List<PlanificationRow> planifications = loadPlanificationsData(vehiculeId);
            ObservableList<PlanificationRow> planifList = FXCollections.observableArrayList(planifications);
            tablePlanifications.setItems(planifList);
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des planifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Charge les données d'un véhicule depuis la base de données
     */
    private VehiculeData loadVehiculeData(int vehiculeId) throws SQLException {
        String sql = """
            SELECT id, immatriculation, type_vehicule, marque, modele, annee, 
                   kilometrage, statut, notes
            FROM vehicules
            WHERE id = ?
            """;
            
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new VehiculeData(
                        rs.getInt("id"),
                        rs.getString("immatriculation"),
                        rs.getString("type_vehicule"),
                        rs.getString("marque"),
                        rs.getString("modele"),
                        rs.getObject("annee", Integer.class),
                        rs.getInt("kilometrage"),
                        null, // carburant
                        null, // puissance
                        null, // ptac
                        rs.getString("statut"),
                        null, // controle_technique
                        null, // assurance
                        null, // visite_periodique
                        null, // revision
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Charge les planifications d'un véhicule
     */
    private List<PlanificationRow> loadPlanificationsData(int vehiculeId) throws SQLException {
        List<PlanificationRow> planifications = new ArrayList<>();
        
        String sql = """
            SELECT p.date_planifiee, p.type_intervention, p.statut, i.id as intervention_id
            FROM planifications p
            LEFT JOIN interventions i ON p.intervention_id = i.id
            WHERE p.vehicule_id = ?
            ORDER BY p.date_planifiee DESC
            LIMIT 10
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    planifications.add(new PlanificationRow(
                        formatDate(rs.getString("date_planifiee")),
                        rs.getString("type_intervention") != null ? rs.getString("type_intervention") : "Non défini",
                        rs.getInt("intervention_id") != 0 ? String.valueOf(rs.getInt("intervention_id")) : "-",
                        formatStatut(rs.getString("statut"))
                    ));
                }
            }
        }
        
        return planifications;
    }
    
    /**
     * Formate le type de véhicule
     */
    private String formatType(String type) {
        return switch (type) {
            case "VL" -> "Véhicule Léger";
            case "PL" -> "Poids Lourd";
            case "SPL" -> "Super Poids Lourd";
            case "REMORQUE" -> "Remorque";
            case "SCENE_MOBILE" -> "Scène Mobile";
            default -> type;
        };
    }
    
    /**
     * Formate le statut
     */
    private String formatStatut(String statut) {
        return switch (statut) {
            case "DISPONIBLE" -> "Disponible";
            case "EN_MISSION" -> "En mission";
            case "MAINTENANCE" -> "Maintenance";
            case "HORS_SERVICE" -> "Hors service";
            case "RESERVE" -> "Réservé";
            default -> statut;
        };
    }
    
    /**
     * Formate une date
     */
    private String formatDate(String date) {
        if (date == null) return "-";
        try {
            LocalDate d = LocalDate.parse(date);
            
            // Vérifier si la date est proche de l'expiration
            LocalDate today = LocalDate.now();
            LocalDate warningDate = today.plusDays(30); // Alerte 30 jours avant
            
            String formattedDate = d.format(dateFormatter);
            
            if (d.isBefore(today)) {
                return "⚠️ " + formattedDate + " (EXPIRÉ)";
            } else if (d.isBefore(warningDate)) {
                return "⚠️ " + formattedDate + " (Bientôt)";
            } else {
                return "✅ " + formattedDate;
            }
        } catch (Exception e) {
            return date;
        }
    }
    
    /**
     * Actions des boutons
     */
    @FXML
    private void onEdit() {
        showInfo("Édition", "Fonctionnalité d'édition véhicule en cours de développement");
    }
    
    @FXML
    private void onMaintenance() {
        showInfo("Maintenance", "Fonctionnalité de maintenance en cours de développement");
    }
    
    @FXML
    private void onPlanifier() {
        showInfo("Planification", "Fonctionnalité de planification en cours de développement");
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
     * Record pour stocker les données du véhicule
     */
    private record VehiculeData(
        int id,
        String immatriculation,
        String typeVehicule,
        String marque,
        String modele,
        Integer annee,
        int kilometrage,
        String carburant,
        Integer puissance,
        Integer ptac,
        String statut,
        String controleTechnique,
        String assurance,
        String visitePeriodique,
        String revision,
        String notes
    ) {}
    
    @FXML
    private void saveVehicule() {
        // TODO: Implémenter la sauvegarde du véhicule
        System.out.println("Sauvegarde véhicule");
    }
    
    @FXML
    private void cancel() {
        // Fermer la fenêtre
        if (lblId != null && lblId.getScene() != null) {
            lblId.getScene().getWindow().hide();
        }
    }

    /**
     * Record pour les planifications
     */
    private record PlanificationRow(
        String date,
        String technicien,
        String intervention,
        String statut
    ) {}
}