package com.magsav.gui;

import com.magsav.model.Company;
import com.magsav.model.InterventionRow;
import com.magsav.repo.CompanyRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.NavigationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientDetailController implements Initializable {
    
    // Références aux composants FXML
    @FXML private ImageView imgClientLogo;
    @FXML private Label lblClientName;
    @FXML private Label lblClientType;
    @FXML private Label lblClientId;
    
    // Boutons d'action
    @FXML private Button btnEdit;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    // Champs d'informations
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtWebsite;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPostalCode;
    @FXML private TextField txtCity;
    @FXML private TextField txtCountry;
    @FXML private TextArea txtNotes;
    
    // Tableau des interventions
    @FXML private TableView<InterventionRow> interventionsTable;
    @FXML private TableColumn<InterventionRow, String> colInterventionId;
    @FXML private TableColumn<InterventionRow, String> colInterventionProduit;
    @FXML private TableColumn<InterventionRow, String> colInterventionStatut;
    @FXML private TableColumn<InterventionRow, String> colInterventionDate;
    @FXML private TableColumn<InterventionRow, String> colInterventionPanne;
    
    // Tableau des contacts (pour l'instant, on peut le laisser vide)
    @FXML private TableView<Object> contactsTable;
    @FXML private TableColumn<Object, String> colContactName;
    @FXML private TableColumn<Object, String> colContactPosition;
    @FXML private TableColumn<Object, String> colContactEmail;
    @FXML private TableColumn<Object, String> colContactPhone;
    @FXML private TableColumn<Object, String> colContactNotes;
    
    // Labels de statut
    @FXML private Label lblStatus;
    @FXML private Label lblLastModified;
    
    // Modèle de données
    private Company client;
    
    // Repositories
    private CompanyRepository companyRepository;
    private InterventionRepository interventionRepository;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            companyRepository = new CompanyRepository(com.magsav.db.DB.getConnection());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du CompanyRepository: " + e.getMessage());
        }
        interventionRepository = new InterventionRepository();
        
        // Configuration des colonnes du tableau des interventions
        setupInterventionsTable();
        
        // Configuration des colonnes du tableau des contacts (pour plus tard)
        setupContactsTable();
        
        System.out.println("ClientDetailController initialisé");
    }
    
    /**
     * Configure les colonnes du tableau des interventions
     */
    private void setupInterventionsTable() {
        colInterventionId.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().id())));
        
        colInterventionProduit.setCellValueFactory(cellData -> {
            String produit = cellData.getValue().produitNom();
            return new SimpleStringProperty(produit != null ? produit : "N/A");
        });
        
        colInterventionStatut.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().statut()));
        
        colInterventionDate.setCellValueFactory(cellData -> {
            String dateEntree = cellData.getValue().dateEntree();
            if (dateEntree != null) {
                return new SimpleStringProperty(dateEntree);
            }
            return new SimpleStringProperty("N/A");
        });
        
        colInterventionPanne.setCellValueFactory(cellData -> {
            String panne = cellData.getValue().panne();
            return new SimpleStringProperty(panne != null ? panne : "N/A");
        });
        
        // Double-clic pour ouvrir le détail de l'intervention
        interventionsTable.setRowFactory(tv -> {
            TableRow<InterventionRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    InterventionRow intervention = row.getItem();
                    System.out.println("Double-clic sur intervention: " + intervention.id());
                    NavigationService.openInterventionDetail(intervention.id());
                }
            });
            return row;
        });
    }
    
    /**
     * Configure les colonnes du tableau des contacts
     */
    private void setupContactsTable() {
        colContactName.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colContactPosition.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colContactEmail.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colContactPhone.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colContactNotes.setCellValueFactory(cellData -> new SimpleStringProperty(""));
    }
    
    /**
     * Définit le client à afficher
     */
    public void setClient(Company client) {
        this.client = client;
        loadClientData();
        loadInterventions();
    }
    
    /**
     * Charge les données du client dans l'interface
     */
    private void loadClientData() {
        if (client == null) return;
        
        // Informations de base
        lblClientName.setText(client.getName());
        lblClientType.setText("Type: " + (client.getType() != null ? client.getType().toString() : "Client"));
        lblClientId.setText("ID: #" + client.getId());
        
        // Champs d'édition
        txtName.setText(client.getName());
        txtEmail.setText(client.getEmail());
        txtPhone.setText(client.getPhone());
        txtWebsite.setText(client.getWebsite());
        txtAddress.setText(client.getAddress());
        txtPostalCode.setText(client.getPostalCode());
        txtCity.setText(client.getCity());
        txtCountry.setText(client.getCountry());
        txtNotes.setText(client.getDescription());
        
        // Statut
        lblStatus.setText("Client chargé: " + client.getName());
        lblLastModified.setText("Dernière modification: " + 
            (client.getUpdatedAt() != null ? 
                client.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                "jamais"));
    }
    
    /**
     * Charge les interventions du client
     */
    private void loadInterventions() {
        if (client == null) return;
        
        try {
            List<InterventionRow> interventions = interventionRepository.findByClientId(client.getId());
            ObservableList<InterventionRow> interventionsList = FXCollections.observableArrayList(interventions);
            interventionsTable.setItems(interventionsList);
            
            System.out.println("Chargé " + interventions.size() + " interventions pour le client " + client.getName());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des interventions: " + e.getMessage());
            lblStatus.setText("Erreur lors du chargement des interventions");
        }
    }
    
    /**
     * Active le mode édition
     */
    @FXML
    private void onEdit() {
        setFieldsEditable(true);
        
        btnEdit.setDisable(true);
        btnSave.setDisable(false);
        btnCancel.setDisable(false);
        
        lblStatus.setText("Mode édition activé");
    }
    
    /**
     * Sauvegarde les modifications
     */
    @FXML
    private void onSave() {
        if (client == null) return;
        
        try {
            // Mise à jour des données du client
            client.setName(txtName.getText());
            client.setEmail(txtEmail.getText());
            client.setPhone(txtPhone.getText());
            client.setWebsite(txtWebsite.getText());
            client.setAddress(txtAddress.getText());
            client.setPostalCode(txtPostalCode.getText());
            client.setCity(txtCity.getText());
            client.setCountry(txtCountry.getText());
            client.setDescription(txtNotes.getText());
            
            // Sauvegarde en base
            companyRepository.save(client);
            
            // Mise à jour de l'interface
            loadClientData();
            
            setFieldsEditable(false);
            
            btnEdit.setDisable(false);
            btnSave.setDisable(true);
            btnCancel.setDisable(true);
            
            lblStatus.setText("Modifications sauvegardées");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
            lblStatus.setText("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    /**
     * Annule les modifications
     */
    @FXML
    private void onCancel() {
        loadClientData(); // Recharge les données originales
        
        setFieldsEditable(false);
        
        btnEdit.setDisable(false);
        btnSave.setDisable(true);
        btnCancel.setDisable(true);
        
        lblStatus.setText("Modifications annulées");
    }
    
    /**
     * Active/désactive l'édition des champs
     */
    private void setFieldsEditable(boolean editable) {
        txtName.setDisable(!editable);
        txtEmail.setDisable(!editable);
        txtPhone.setDisable(!editable);
        txtWebsite.setDisable(!editable);
        txtAddress.setDisable(!editable);
        txtPostalCode.setDisable(!editable);
        txtCity.setDisable(!editable);
        txtCountry.setDisable(!editable);
        txtNotes.setDisable(!editable);
    }
    
    /**
     * Actualise les données
     */
    @FXML
    private void onRefresh() {
        if (client != null) {
            try {
                java.util.Optional<Company> updatedClientOpt = companyRepository.findById(client.getId());
                if (updatedClientOpt.isPresent()) {
                    setClient(updatedClientOpt.get());
                    lblStatus.setText("Données actualisées");
                } else {
                    lblStatus.setText("Client non trouvé lors de l'actualisation");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'actualisation: " + e.getMessage());
                lblStatus.setText("Erreur lors de l'actualisation");
            }
        }
    }
    
    /**
     * Ferme la fenêtre
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) lblClientName.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Ouvre le formulaire de nouvelle intervention
     */
    @FXML
    private void onNewIntervention() {
        System.out.println("Création d'une nouvelle intervention pour: " + client.getName());
        // TODO: Implémenter l'ouverture du formulaire d'intervention
        lblStatus.setText("Fonctionnalité en cours de développement");
    }
    
    /**
     * Ajoute un nouveau contact
     */
    @FXML
    private void onAddContact() {
        System.out.println("Ajout d'un nouveau contact pour: " + client.getName());
        // TODO: Implémenter l'ajout de contacts
        lblStatus.setText("Fonctionnalité en cours de développement");
    }
}