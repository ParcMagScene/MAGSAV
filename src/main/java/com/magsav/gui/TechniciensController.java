package com.magsav.gui;

import com.magsav.model.Technicien;
import com.magsav.repo.TechnicienRepository;
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

/**
 * Contrôleur pour la gestion des techniciens Mag Scene
 */
public class TechniciensController {
    
    @FXML private TableView<Technicien> tableviewTechniciens;
    @FXML private TableColumn<Technicien, String> colNom;
    @FXML private TableColumn<Technicien, String> colPrenom;
    @FXML private TableColumn<Technicien, String> colFonction;
    @FXML private TableColumn<Technicien, String> colEmail;
    @FXML private TableColumn<Technicien, String> colTelephone;
    @FXML private TableColumn<Technicien, String> colAdresse;
    @FXML private TableColumn<Technicien, String> colPermis;
    @FXML private TableColumn<Technicien, String> colStatut;
    
    @FXML private TextField textFieldSearch;
    @FXML private ComboBox<String> comboBoxFonction;
    @FXML private ComboBox<Technicien.StatutTechnicien> comboBoxStatut;
    
    @FXML private Label labelNomComplet;
    @FXML private Label labelFonction;
    @FXML private Label labelEmail;
    @FXML private Label labelTelephone;
    @FXML private Label labelTelephoneUrgence;
    @FXML private Label labelAdresseComplete;
    @FXML private Label labelPermisConduire;
    @FXML private TextArea textAreaHabilitations;
    @FXML private TextArea textAreaSpecialites;
    @FXML private TextArea textAreaNotes;
    
    @FXML private Button buttonNouveau;
    @FXML private Button buttonModifier;
    @FXML private Button buttonSupprimer;
    @FXML private Button buttonRafraichir;
    
    private TechnicienRepository technicienRepository;
    private ObservableList<Technicien> techniciens;
    private Technicien technicienSelectionne;
    
    @FXML
    private void initialize() {
        technicienRepository = new TechnicienRepository();
        
        setupTableColumns();
        setupFilters();
        setupSelectionListener();
        
        chargerTechniciens();
    }
    
    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colFonction.setCellValueFactory(new PropertyValueFactory<>("fonction"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colPermis.setCellValueFactory(new PropertyValueFactory<>("permisConduire"));
        colStatut.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatut().getDisplayName()));
        
        // Adresse complète
        colAdresse.setCellValueFactory(cellData -> {
            Technicien tech = cellData.getValue();
            String adresse = tech.getAdresse() + ", " + tech.getCodePostal() + " " + tech.getVille();
            return new SimpleStringProperty(adresse);
        });
        
        // Ajuster la largeur des colonnes
        colNom.setPrefWidth(100);
        colPrenom.setPrefWidth(100);
        colFonction.setPrefWidth(150);
        colEmail.setPrefWidth(200);
        colTelephone.setPrefWidth(120);
        colAdresse.setPrefWidth(200);
        colPermis.setPrefWidth(100);
        colStatut.setPrefWidth(100);
    }
    
    private void setupFilters() {
        // Filtres par fonction
        comboBoxFonction.getItems().addAll(
            "Tous",
            "Technicien Distribution",
            "Technicien Lumière", 
            "Technicien Structure",
            "Technicien Son",
            "Chauffeur PL",
            "Chauffeur SPL",
            "Stagiaire"
        );
        comboBoxFonction.setValue("Tous");
        
        // Filtres par statut
        comboBoxStatut.getItems().addAll(Technicien.StatutTechnicien.values());
        comboBoxStatut.setValue(null);
        
        // Listeners pour les filtres
        textFieldSearch.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        comboBoxFonction.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        comboBoxStatut.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
    }
    
    private void setupSelectionListener() {
        tableviewTechniciens.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                technicienSelectionne = newSelection;
                afficherDetailsTechnicien(newSelection);
                
                // Activer/désactiver les boutons
                boolean selectionExists = newSelection != null;
                buttonModifier.setDisable(!selectionExists);
                buttonSupprimer.setDisable(!selectionExists);
            }
        );
    }
    
    private void chargerTechniciens() {
        try {
            techniciens = technicienRepository.findAll();
            tableviewTechniciens.setItems(techniciens);
            
            System.out.println("✅ " + techniciens.size() + " techniciens chargés");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des techniciens: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger les techniciens");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void appliquerFiltres() {
        if (techniciens == null) return;
        
        ObservableList<Technicien> filtres = techniciens.filtered(technicien -> {
            // Filtre par recherche (nom, prénom, email)
            String recherche = textFieldSearch.getText();
            if (recherche != null && !recherche.trim().isEmpty()) {
                String rechercheLC = recherche.toLowerCase();
                boolean matchRecherche = 
                    technicien.getNom().toLowerCase().contains(rechercheLC) ||
                    technicien.getPrenom().toLowerCase().contains(rechercheLC) ||
                    (technicien.getEmail() != null && technicien.getEmail().toLowerCase().contains(rechercheLC));
                
                if (!matchRecherche) return false;
            }
            
            // Filtre par fonction
            String fonction = comboBoxFonction.getValue();
            if (fonction != null && !"Tous".equals(fonction)) {
                if (!fonction.equals(technicien.getFonction())) return false;
            }
            
            // Filtre par statut
            Technicien.StatutTechnicien statut = comboBoxStatut.getValue();
            if (statut != null) {
                if (!statut.equals(technicien.getStatut())) return false;
            }
            
            return true;
        });
        
        tableviewTechniciens.setItems(filtres);
    }
    
    private void afficherDetailsTechnicien(Technicien technicien) {
        if (technicien == null) {
            // Vider les champs de détail
            labelNomComplet.setText("");
            labelFonction.setText("");
            labelEmail.setText("");
            labelTelephone.setText("");
            labelTelephoneUrgence.setText("");
            labelAdresseComplete.setText("");
            labelPermisConduire.setText("");
            textAreaHabilitations.setText("");
            textAreaSpecialites.setText("");
            textAreaNotes.setText("");
            return;
        }
        
        // Remplir les champs de détail
        labelNomComplet.setText(technicien.getNomComplet());
        labelFonction.setText(technicien.getFonction());
        labelEmail.setText(technicien.getEmail());
        labelTelephone.setText(technicien.getTelephone());
        labelTelephoneUrgence.setText(technicien.getTelephoneUrgence());
        
        String adresseComplete = technicien.getAdresse() + "\n" + 
                               technicien.getCodePostal() + " " + technicien.getVille();
        labelAdresseComplete.setText(adresseComplete);
        labelPermisConduire.setText(technicien.getPermisConduire());
        
        // Formater les habilitations JSON en texte lisible
        textAreaHabilitations.setText(formaterHabilitations(technicien.getHabilitations()));
        textAreaSpecialites.setText(formaterSpecialites(technicien.getSpecialites()));
        textAreaNotes.setText(technicien.getNotes());
    }
    
    private String formaterHabilitations(String habilitationsJson) {
        if (habilitationsJson == null || habilitationsJson.trim().isEmpty()) {
            return "Aucune habilitation enregistrée";
        }
        
        // Simple formatage pour affichage (remplacer par un parser JSON si nécessaire)
        return habilitationsJson
            .replace("[", "")
            .replace("]", "")
            .replace("{", "• ")
            .replace("}", "\n")
            .replace("\"nom\":", "")
            .replace("\"dateObtention\":", " - Obtenu le: ")
            .replace("\"dateValidite\":", " - Valide jusqu'au: ")
            .replace("\"organisme\":", " - Organisme: ")
            .replace("\"", "")
            .replace(",", "");
    }
    
    private String formaterSpecialites(String specialitesJson) {
        if (specialitesJson == null || specialitesJson.trim().isEmpty()) {
            return "Aucune spécialité enregistrée";
        }
        
        return specialitesJson
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "")
            .replace(",", "\n• ");
    }
    
    @FXML
    private void handleNouveau() {
        // TODO: Ouvrir formulaire de création de technicien
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À venir");
        alert.setHeaderText("Fonctionnalité de création de technicien");
        alert.setContentText("Cette fonctionnalité sera implémentée prochainement.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleModifier() {
        if (technicienSelectionne == null) return;
        
        // TODO: Ouvrir formulaire de modification
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À venir");
        alert.setHeaderText("Modification de " + technicienSelectionne.getNomComplet());
        alert.setContentText("Cette fonctionnalité sera implémentée prochainement.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleSupprimer() {
        if (technicienSelectionne == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer le technicien " + technicienSelectionne.getNomComplet() + " ?");
        confirmAlert.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Implémenter la suppression
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("À venir");
            alert.setHeaderText("Suppression confirmée");
            alert.setContentText("Cette fonctionnalité sera implémentée prochainement.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleRafraichir() {
        chargerTechniciens();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Actualisation");
        alert.setHeaderText("Données rechargées");
        alert.setContentText(techniciens.size() + " techniciens trouvés.");
        alert.showAndWait();
    }
}