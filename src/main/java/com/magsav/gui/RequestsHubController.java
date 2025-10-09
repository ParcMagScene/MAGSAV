package com.magsav.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.magsav.service.NavigationService;

import java.net.URL;
import java.util.ResourceBundle;

public class RequestsHubController implements Initializable {

    // ========== COMPOSANTS FXML ==========
    
    @FXML private TabPane mainTabPane;
    
    // Onglet Demandes de Matériel
    @FXML private TableView<EquipmentRequestRow> equipmentRequestsTable;
    @FXML private TableColumn<EquipmentRequestRow, Integer> colEquipId;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipDate;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipDemandeur;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipMatériel;
    @FXML private TableColumn<EquipmentRequestRow, Integer> colEquipQuantité;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipStatut;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipPriorité;
    @FXML private TableColumn<EquipmentRequestRow, String> colEquipCommentaires;
    @FXML private TextField equipmentSearchField;
    @FXML private Button btnEditEquipmentRequest;
    @FXML private Button btnDeleteEquipmentRequest;
    @FXML private Label lblEquipTotal;
    @FXML private Label lblEquipEnAttente;
    @FXML private Label lblEquipApprouvées;
    @FXML private Label lblEquipRefusées;
    
    // Onglet Demandes de Pièces
    @FXML private TableView<PartRequestRow> partRequestsTable;
    @FXML private TableColumn<PartRequestRow, Integer> colPartId;
    @FXML private TableColumn<PartRequestRow, String> colPartDate;
    @FXML private TableColumn<PartRequestRow, String> colPartDemandeur;
    @FXML private TableColumn<PartRequestRow, String> colPartPièce;
    @FXML private TableColumn<PartRequestRow, String> colPartRéférence;
    @FXML private TableColumn<PartRequestRow, Integer> colPartQuantité;
    @FXML private TableColumn<PartRequestRow, String> colPartStatut;
    @FXML private TableColumn<PartRequestRow, String> colPartUrgence;
    @FXML private TextField partSearchField;
    @FXML private Button btnEditPartRequest;
    @FXML private Button btnDeletePartRequest;
    @FXML private Label lblPartTotal;
    @FXML private Label lblPartEnCours;
    @FXML private Label lblPartLivrées;
    @FXML private Label lblPartAnnulées;
    
    // Onglet Interventions
    @FXML private TableView<InterventionRow> interventionsTable;
    @FXML private TableColumn<InterventionRow, Integer> colInterId;
    @FXML private TableColumn<InterventionRow, String> colInterDate;
    @FXML private TableColumn<InterventionRow, String> colInterTechnicien;
    @FXML private TableColumn<InterventionRow, String> colInterClient;
    @FXML private TableColumn<InterventionRow, String> colInterProduit;
    @FXML private TableColumn<InterventionRow, String> colInterType;
    @FXML private TableColumn<InterventionRow, String> colInterStatut;
    @FXML private TableColumn<InterventionRow, String> colInterDurée;
    @FXML private TextField interventionSearchField;
    @FXML private ComboBox<String> cmbInterventionFilter;
    @FXML private Button btnEditIntervention;
    @FXML private Button btnViewInterventionDetails;
    @FXML private Button btnCloseIntervention;
    @FXML private Label lblInterTotal;
    @FXML private Label lblInterPlanifiées;
    @FXML private Label lblInterEnCours;
    @FXML private Label lblInterTerminées;
    
    // Onglet Techniciens
    @FXML private TableView<TechnicianRow> techniciansTable;
    @FXML private TableColumn<TechnicianRow, Integer> colTechId;
    @FXML private TableColumn<TechnicianRow, String> colTechNom;
    @FXML private TableColumn<TechnicianRow, String> colTechPrénom;
    @FXML private TableColumn<TechnicianRow, String> colTechSpécialité;
    @FXML private TableColumn<TechnicianRow, String> colTechNiveau;
    @FXML private TableColumn<TechnicianRow, String> colTechStatut;
    @FXML private TableColumn<TechnicianRow, String> colTechDernièreActivité;
    @FXML private TableColumn<TechnicianRow, String> colTechDroits;
    @FXML private TextField technicianSearchField;
    @FXML private Button btnEditTechnicianRights;
    @FXML private Button btnInheritRights;
    @FXML private Button btnTechnicianActivityReport;
    @FXML private CheckBox chkConsultation;
    @FXML private CheckBox chkCréationDemandes;
    @FXML private CheckBox chkModificationProduits;
    @FXML private CheckBox chkGestionStock;
    @FXML private CheckBox chkRapports;
    @FXML private CheckBox chkAdministration;
    @FXML private Label lblTechTotal;
    @FXML private Label lblTechActifs;
    @FXML private Label lblTechInactifs;
    @FXML private Label lblTechEnFormation;
    
    // ========== DONNÉES ==========
    
    private ObservableList<EquipmentRequestRow> equipmentRequests = FXCollections.observableArrayList();
    private ObservableList<PartRequestRow> partRequests = FXCollections.observableArrayList();
    private ObservableList<InterventionRow> interventions = FXCollections.observableArrayList();
    private ObservableList<TechnicianRow> technicians = FXCollections.observableArrayList();
    
    // ========== INITIALISATION ==========
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEquipmentRequestsTable();
        setupPartRequestsTable();
        setupInterventionsTable();
        setupTechniciansTable();
        setupInterventionFilter();
        loadAllData();
        setupTableSelectionListeners();
    }
    
    private void setupEquipmentRequestsTable() {
        colEquipId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().id()));
        colEquipDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date()));
        colEquipDemandeur.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().demandeur()));
        colEquipMatériel.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().matériel()));
        colEquipQuantité.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().quantité()));
        colEquipStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().statut()));
        colEquipPriorité.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().priorité()));
        colEquipCommentaires.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().commentaires()));
        
        equipmentRequestsTable.setItems(equipmentRequests);
    }
    
    private void setupPartRequestsTable() {
        colPartId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().id()));
        colPartDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date()));
        colPartDemandeur.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().demandeur()));
        colPartPièce.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().pièce()));
        colPartRéférence.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().référence()));
        colPartQuantité.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().quantité()));
        colPartStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().statut()));
        colPartUrgence.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().urgence()));
        
        partRequestsTable.setItems(partRequests);
    }
    
    private void setupInterventionsTable() {
        colInterId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().id()));
        colInterDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().date()));
        colInterTechnicien.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().technicien()));
        colInterClient.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().client()));
        colInterProduit.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().produit()));
        colInterType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().type()));
        colInterStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().statut()));
        colInterDurée.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().durée()));
        
        interventionsTable.setItems(interventions);
    }
    
    private void setupTechniciansTable() {
        colTechId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().id()));
        colTechNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().nom()));
        colTechPrénom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().prénom()));
        colTechSpécialité.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().spécialité()));
        colTechNiveau.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().niveau()));
        colTechStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().statut()));
        colTechDernièreActivité.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().dernièreActivité()));
        colTechDroits.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().droits()));
        
        techniciansTable.setItems(technicians);
    }
    
    private void setupInterventionFilter() {
        cmbInterventionFilter.setItems(FXCollections.observableArrayList(
            "Tous", "Planifiée", "En cours", "Terminée", "Annulée"
        ));
        cmbInterventionFilter.setValue("Tous");
    }
    
    private void setupTableSelectionListeners() {
        // Demandes de matériel
        equipmentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditEquipmentRequest.setDisable(!hasSelection);
            btnDeleteEquipmentRequest.setDisable(!hasSelection);
        });
        
        // Demandes de pièces
        partRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditPartRequest.setDisable(!hasSelection);
            btnDeletePartRequest.setDisable(!hasSelection);
        });
        
        // Interventions
        interventionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditIntervention.setDisable(!hasSelection);
            btnViewInterventionDetails.setDisable(!hasSelection);
            btnCloseIntervention.setDisable(!hasSelection);
        });
        
        // Techniciens
        techniciansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditTechnicianRights.setDisable(!hasSelection);
            btnInheritRights.setDisable(!hasSelection);
            btnTechnicianActivityReport.setDisable(!hasSelection);
            
            if (newSelection != null) {
                loadTechnicianRights(newSelection);
            }
        });
    }
    
    private void loadAllData() {
        loadEquipmentRequestsData();
        loadPartRequestsData();
        loadInterventionsData();
        loadTechniciansData();
        updateAllStatistics();
    }
    
    private void loadEquipmentRequestsData() {
        equipmentRequests.clear();
        // Données de test
        equipmentRequests.addAll(
            new EquipmentRequestRow(1, "15/12/2024", "Jean Dupont", "Oscilloscope", 1, "En attente", "Haute", "Urgence réparation"),
            new EquipmentRequestRow(2, "14/12/2024", "Marie Martin", "Multimètre", 2, "Approuvée", "Moyenne", "Stock insuffisant"),
            new EquipmentRequestRow(3, "13/12/2024", "Pierre Durand", "Fer à souder", 5, "En attente", "Basse", "Maintenance atelier"),
            new EquipmentRequestRow(4, "12/12/2024", "Sophie Bernard", "Générateur", 1, "Refusée", "Haute", "Budget dépassé"),
            new EquipmentRequestRow(5, "11/12/2024", "Luc Moreau", "Alimentation", 3, "Approuvée", "Moyenne", "Projet client")
        );
    }
    
    private void loadPartRequestsData() {
        partRequests.clear();
        // Données de test
        partRequests.addAll(
            new PartRequestRow(1, "15/12/2024", "Tech1", "Condensateur", "C100-47µF", 10, "En cours", "Critique"),
            new PartRequestRow(2, "14/12/2024", "Tech2", "Résistance", "R220-1kΩ", 50, "Livrée", "Normale"),
            new PartRequestRow(3, "13/12/2024", "Tech3", "Circuit intégré", "IC-TL081", 5, "En cours", "Haute"),
            new PartRequestRow(4, "12/12/2024", "Tech1", "Diode", "D1N4007", 20, "Annulée", "Basse"),
            new PartRequestRow(5, "11/12/2024", "Tech4", "Transistor", "BC547", 15, "Livrée", "Normale")
        );
    }
    
    private void loadInterventionsData() {
        interventions.clear();
        // Données de test
        interventions.addAll(
            new InterventionRow(1, "15/12/2024", "Jean Dupont", "Client A", "Oscilloscope Tek", "Réparation", "En cours", "2h30"),
            new InterventionRow(2, "14/12/2024", "Marie Martin", "Client B", "Multimètre Fluke", "Maintenance", "Terminée", "1h15"),
            new InterventionRow(3, "13/12/2024", "Pierre Durand", "Client C", "Générateur HP", "Installation", "Planifiée", "4h00"),
            new InterventionRow(4, "12/12/2024", "Sophie Bernard", "Client D", "Alimentation Agilent", "Diagnostic", "Terminée", "45min"),
            new InterventionRow(5, "11/12/2024", "Luc Moreau", "Client E", "Analyseur Keysight", "Étalonnage", "En cours", "3h20")
        );
    }
    
    private void loadTechniciansData() {
        technicians.clear();
        // Données de test
        technicians.addAll(
            new TechnicianRow(1, "Dupont", "Jean", "Électronique", "Senior", "Actif", "15/12/2024", "Admin, Gestion, Rapports"),
            new TechnicianRow(2, "Martin", "Marie", "Informatique", "Confirmé", "Actif", "14/12/2024", "Consultation, Demandes"),
            new TechnicianRow(3, "Durand", "Pierre", "Mécanique", "Junior", "En formation", "13/12/2024", "Consultation"),
            new TechnicianRow(4, "Bernard", "Sophie", "Test & Mesure", "Expert", "Actif", "12/12/2024", "Admin, Gestion, Stock"),
            new TechnicianRow(5, "Moreau", "Luc", "Calibration", "Senior", "Inactif", "10/12/2024", "Consultation, Rapports")
        );
    }
    
    private void updateAllStatistics() {
        updateEquipmentStatistics();
        updatePartStatistics();
        updateInterventionStatistics();
        updateTechnicianStatistics();
    }
    
    private void updateEquipmentStatistics() {
        int total = equipmentRequests.size();
        long enAttente = equipmentRequests.stream().filter(r -> "En attente".equals(r.statut())).count();
        long approuvées = equipmentRequests.stream().filter(r -> "Approuvée".equals(r.statut())).count();
        long refusées = equipmentRequests.stream().filter(r -> "Refusée".equals(r.statut())).count();
        
        lblEquipTotal.setText("Total: " + total);
        lblEquipEnAttente.setText("En attente: " + enAttente);
        lblEquipApprouvées.setText("Approuvées: " + approuvées);
        lblEquipRefusées.setText("Refusées: " + refusées);
    }
    
    private void updatePartStatistics() {
        int total = partRequests.size();
        long enCours = partRequests.stream().filter(r -> "En cours".equals(r.statut())).count();
        long livrées = partRequests.stream().filter(r -> "Livrée".equals(r.statut())).count();
        long annulées = partRequests.stream().filter(r -> "Annulée".equals(r.statut())).count();
        
        lblPartTotal.setText("Total: " + total);
        lblPartEnCours.setText("En cours: " + enCours);
        lblPartLivrées.setText("Livrées: " + livrées);
        lblPartAnnulées.setText("Annulées: " + annulées);
    }
    
    private void updateInterventionStatistics() {
        int total = interventions.size();
        long planifiées = interventions.stream().filter(r -> "Planifiée".equals(r.statut())).count();
        long enCours = interventions.stream().filter(r -> "En cours".equals(r.statut())).count();
        long terminées = interventions.stream().filter(r -> "Terminée".equals(r.statut())).count();
        
        lblInterTotal.setText("Total: " + total);
        lblInterPlanifiées.setText("Planifiées: " + planifiées);
        lblInterEnCours.setText("En cours: " + enCours);
        lblInterTerminées.setText("Terminées: " + terminées);
    }
    
    private void updateTechnicianStatistics() {
        int total = technicians.size();
        long actifs = technicians.stream().filter(r -> "Actif".equals(r.statut())).count();
        long inactifs = technicians.stream().filter(r -> "Inactif".equals(r.statut())).count();
        long enFormation = technicians.stream().filter(r -> "En formation".equals(r.statut())).count();
        
        lblTechTotal.setText("Total: " + total);
        lblTechActifs.setText("Actifs: " + actifs);
        lblTechInactifs.setText("Inactifs: " + inactifs);
        lblTechEnFormation.setText("En formation: " + enFormation);
    }
    
    private void loadTechnicianRights(TechnicianRow technician) {
        // Simuler le chargement des droits basé sur la chaîne droits
        String droits = technician.droits().toLowerCase();
        chkConsultation.setSelected(droits.contains("consultation"));
        chkCréationDemandes.setSelected(droits.contains("demandes"));
        chkModificationProduits.setSelected(droits.contains("modification"));
        chkGestionStock.setSelected(droits.contains("stock"));
        chkRapports.setSelected(droits.contains("rapports"));
        chkAdministration.setSelected(droits.contains("admin"));
    }
    
    // ========== ACTIONS DEMANDES DE MATÉRIEL ==========
    
    @FXML
    private void onAddEquipmentRequest() {
        NavigationService.showInfoDialog("Nouvelle Demande de Matériel", "Ouverture du formulaire de demande...");
    }
    
    @FXML
    private void onEditEquipmentRequest() {
        EquipmentRequestRow selected = equipmentRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Modifier Demande", "Modification de la demande #" + selected.id());
        }
    }
    
    @FXML
    private void onDeleteEquipmentRequest() {
        EquipmentRequestRow selected = equipmentRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (NavigationService.showConfirmDialog("Confirmer Suppression", 
                "Êtes-vous sûr de vouloir supprimer la demande #" + selected.id() + " ?")) {
                equipmentRequests.remove(selected);
                updateEquipmentStatistics();
            }
        }
    }
    
    @FXML
    private void onSearchEquipment() {
        String query = equipmentSearchField.getText().toLowerCase().trim();
        // Implémentation de la recherche
        NavigationService.showInfoDialog("Recherche", "Recherche: " + query);
    }
    
    @FXML
    private void onRefreshEquipmentRequests() {
        loadEquipmentRequestsData();
        updateEquipmentStatistics();
    }
    
    // ========== ACTIONS DEMANDES DE PIÈCES ==========
    
    @FXML
    private void onAddPartRequest() {
        NavigationService.showInfoDialog("Nouvelle Demande de Pièce", "Ouverture du formulaire de demande...");
    }
    
    @FXML
    private void onEditPartRequest() {
        PartRequestRow selected = partRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Modifier Demande", "Modification de la demande #" + selected.id());
        }
    }
    
    @FXML
    private void onDeletePartRequest() {
        PartRequestRow selected = partRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (NavigationService.showConfirmDialog("Confirmer Suppression", 
                "Êtes-vous sûr de vouloir supprimer la demande #" + selected.id() + " ?")) {
                partRequests.remove(selected);
                updatePartStatistics();
            }
        }
    }
    
    @FXML
    private void onSearchParts() {
        String query = partSearchField.getText().toLowerCase().trim();
        NavigationService.showInfoDialog("Recherche", "Recherche pièces: " + query);
    }
    
    @FXML
    private void onRefreshPartRequests() {
        loadPartRequestsData();
        updatePartStatistics();
    }
    
    // ========== ACTIONS INTERVENTIONS ==========
    
    @FXML
    private void onAddIntervention() {
        NavigationService.showInfoDialog("Nouvelle Intervention", "Ouverture du formulaire d'intervention...");
    }
    
    @FXML
    private void onEditIntervention() {
        InterventionRow selected = interventionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Modifier Intervention", "Modification de l'intervention #" + selected.id());
        }
    }
    
    @FXML
    private void onViewInterventionDetails() {
        InterventionRow selected = interventionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Détails Intervention", "Détails de l'intervention #" + selected.id());
        }
    }
    
    @FXML
    private void onCloseIntervention() {
        InterventionRow selected = interventionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (NavigationService.showConfirmDialog("Clôturer Intervention", 
                "Êtes-vous sûr de vouloir clôturer l'intervention #" + selected.id() + " ?")) {
                NavigationService.showInfoDialog("Intervention Clôturée", "L'intervention a été clôturée avec succès.");
                loadInterventionsData(); // Recharger pour refléter le changement
                updateInterventionStatistics();
            }
        }
    }
    
    @FXML
    private void onSearchInterventions() {
        String query = interventionSearchField.getText().toLowerCase().trim();
        NavigationService.showInfoDialog("Recherche", "Recherche interventions: " + query);
    }
    
    // ========== ACTIONS TECHNICIENS ==========
    
    @FXML
    private void onAddTechnician() {
        NavigationService.showInfoDialog("Nouveau Technicien", "Ouverture du formulaire de technicien...");
    }
    
    @FXML
    private void onEditTechnicianRights() {
        TechnicianRow selected = techniciansTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Modifier Droits", "Modification des droits de " + selected.nom() + " " + selected.prénom());
        }
    }
    
    @FXML
    private void onInheritRights() {
        TechnicianRow selected = techniciansTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Hériter Droits", "Héritage des droits pour " + selected.nom() + " " + selected.prénom());
        }
    }
    
    @FXML
    private void onTechnicianActivityReport() {
        TechnicianRow selected = techniciansTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Rapport Activité", "Génération du rapport pour " + selected.nom() + " " + selected.prénom());
        }
    }
    
    @FXML
    private void onSearchTechnicians() {
        String query = technicianSearchField.getText().toLowerCase().trim();
        NavigationService.showInfoDialog("Recherche", "Recherche techniciens: " + query);
    }
    
    @FXML
    private void onSaveRights() {
        TechnicianRow selected = techniciansTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            NavigationService.showInfoDialog("Droits Sauvegardés", "Les droits ont été sauvegardés pour " + selected.nom() + " " + selected.prénom());
        }
    }
    
    @FXML
    private void onResetRights() {
        chkConsultation.setSelected(false);
        chkCréationDemandes.setSelected(false);
        chkModificationProduits.setSelected(false);
        chkGestionStock.setSelected(false);
        chkRapports.setSelected(false);
        chkAdministration.setSelected(false);
    }
    
    @FXML
    private void onCopyRights() {
        NavigationService.showInfoDialog("Copier Droits", "Sélectionnez le technicien de destination...");
    }
    
    // ========== FERMETURE ==========
    
    @FXML
    private void onClose() {
        Stage stage = (Stage) mainTabPane.getScene().getWindow();
        stage.close();
    }
    
    // ========== CLASSES DE DONNÉES ==========
    
    public record EquipmentRequestRow(
        int id, String date, String demandeur, String matériel, 
        int quantité, String statut, String priorité, String commentaires
    ) {}
    
    public record PartRequestRow(
        int id, String date, String demandeur, String pièce, 
        String référence, int quantité, String statut, String urgence
    ) {}
    
    public record InterventionRow(
        int id, String date, String technicien, String client, 
        String produit, String type, String statut, String durée
    ) {}
    
    public record TechnicianRow(
        int id, String nom, String prénom, String spécialité, 
        String niveau, String statut, String dernièreActivité, String droits
    ) {}
}