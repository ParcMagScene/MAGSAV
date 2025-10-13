package com.magsav.gui;

import com.magsav.model.Vehicule;
import com.magsav.model.Vehicule.TypeVehicule;
import com.magsav.model.Vehicule.StatutVehicule;
import com.magsav.repo.VehiculeRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des véhicules dans l'interface JavaFX
 */
public class VehiculeController implements Initializable {
    
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<StatutVehicule> cmbStatutFiltre;
    @FXML private ComboBox<TypeVehicule> cmbTypeFiltre;
    @FXML private Label lblNombreVehicules;
    
    // Statistiques
    @FXML private Label lblStatsDisponibles;
    @FXML private Label lblStatsEnService;
    @FXML private Label lblStatsMaintenance;
    @FXML private Label lblStatsHorsService;
    
    // Table et colonnes
    @FXML private TableView<Vehicule> tableVehicules;
    @FXML private TableColumn<Vehicule, String> colImmatriculation;
    @FXML private TableColumn<Vehicule, TypeVehicule> colType;
    @FXML private TableColumn<Vehicule, String> colMarque;
    @FXML private TableColumn<Vehicule, String> colModele;
    @FXML private TableColumn<Vehicule, Integer> colAnnee;
    @FXML private TableColumn<Vehicule, Integer> colKilometrage;
    @FXML private TableColumn<Vehicule, StatutVehicule> colStatut;
    @FXML private TableColumn<Vehicule, String> colLocationExterne;
    @FXML private TableColumn<Vehicule, String> colDateCreation;
    @FXML private TableColumn<Vehicule, Void> colActions;
    
    // Boutons d'action
    @FXML private Button btnNouveau;
    @FXML private Button btnActualiser;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVoirDetails;
    @FXML private Button btnChangerStatut;
    @FXML private Button btnExporter;
    @FXML private Button btnImporter;
    
    private final VehiculeRepository vehiculeRepository = new VehiculeRepository();
    private ObservableList<Vehicule> vehicules;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        setupEventHandlers();
        loadVehicules();
        updateStatistiques();
    }
    
    /**
     * Configuration de la table des véhicules
     */
    private void setupTable() {
        // Configuration des colonnes
        colImmatriculation.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));
        colKilometrage.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Colonne location externe avec formatage
        colLocationExterne.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isLocationExterne() ? "Oui" : "Non"));
        
        // Colonne date création formatée
        colDateCreation.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getDateCreation();
            if (date != null && date.length() > 10) {
                return new SimpleStringProperty(date.substring(0, 10)); // YYYY-MM-DD seulement
            }
            return new SimpleStringProperty(date);
        });
        
        // Colonne d'actions avec boutons
        colActions.setCellFactory(col -> new TableCell<Vehicule, Void>() {
            private final Button btnQuickEdit = new Button("✏️");
            private final Button btnQuickStatus = new Button("🔄");
            
            {
                btnQuickEdit.setOnAction(e -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(vehicule);
                });
                
                btnQuickStatus.setOnAction(e -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    changerStatutRapide(vehicule);
                });
                
                btnQuickEdit.setTooltip(new Tooltip("Modifier"));
                btnQuickStatus.setTooltip(new Tooltip("Changer statut"));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(5, btnQuickEdit, btnQuickStatus));
                }
            }
        });
        
        // Formatage conditionnel des lignes selon le statut
        tableVehicules.setRowFactory(tv -> new TableRow<Vehicule>() {
            @Override
            protected void updateItem(Vehicule vehicule, boolean empty) {
                super.updateItem(vehicule, empty);
                if (empty || vehicule == null) {
                    setStyle("");
                } else {
                    switch (vehicule.getStatut()) {
                        case DISPONIBLE:
                            setStyle("-fx-background-color: #e8f5e8;");
                            break;
                        case EN_SERVICE:
                            setStyle("-fx-background-color: #fff3cd;");
                            break;
                        case MAINTENANCE:
                            setStyle("-fx-background-color: #f8d7da;");
                            break;
                        case HORS_SERVICE:
                            setStyle("-fx-background-color: #d1ecf1;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Configuration des filtres
     */
    private void setupFilters() {
        // ComboBox statut avec option "Tous"
        cmbStatutFiltre.getItems().add(null); // "Tous"
        cmbStatutFiltre.getItems().addAll(StatutVehicule.values());
        cmbStatutFiltre.setConverter(new javafx.util.StringConverter<StatutVehicule>() {
            @Override
            public String toString(StatutVehicule statut) {
                return statut == null ? "Tous les statuts" : statut.getDisplayName();
            }
            
            @Override
            public StatutVehicule fromString(String string) {
                return null; // Non utilisé
            }
        });
        
        // ComboBox type avec option "Tous"
        cmbTypeFiltre.getItems().add(null); // "Tous"
        cmbTypeFiltre.getItems().addAll(TypeVehicule.values());
        cmbTypeFiltre.setConverter(new javafx.util.StringConverter<TypeVehicule>() {
            @Override
            public String toString(TypeVehicule type) {
                return type == null ? "Tous les types" : type.getDisplayName();
            }
            
            @Override
            public TypeVehicule fromString(String string) {
                return null; // Non utilisé
            }
        });
    }
    
    /**
     * Configuration des gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Activation/désactivation des boutons selon la sélection
        tableVehicules.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnModifier.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
            btnVoirDetails.setDisable(!hasSelection);
            btnChangerStatut.setDisable(!hasSelection);
        });
    }
    
    /**
     * Charge la liste des véhicules
     */
    private void loadVehicules() {
        vehicules = vehiculeRepository.findAll();
        tableVehicules.setItems(vehicules);
        lblNombreVehicules.setText(vehicules.size() + " véhicule(s)");
    }
    
    /**
     * Met à jour les statistiques
     */
    private void updateStatistiques() {
        Map<String, Integer> stats = vehiculeRepository.getStatistiques();
        lblStatsDisponibles.setText("Disponibles: " + stats.getOrDefault("disponibles", 0));
        lblStatsEnService.setText("En service: " + stats.getOrDefault("en_service", 0));
        lblStatsMaintenance.setText("Maintenance: " + stats.getOrDefault("maintenance", 0));
        lblStatsHorsService.setText("Hors service: " + stats.getOrDefault("hors_service", 0));
    }
    
    // === GESTIONNAIRES D'ÉVÉNEMENTS ===
    
    @FXML
    private void onNouveauVehicule() {
        ouvrirFormulaireCreation();
    }
    
    @FXML
    private void onActualiser() {
        loadVehicules();
        updateStatistiques();
        // Réinitialiser les filtres
        txtRecherche.clear();
        cmbStatutFiltre.setValue(null);
        cmbTypeFiltre.setValue(null);
    }
    
    @FXML
    private void onRechercheChanged() {
        appliquerFiltres();
    }
    
    @FXML
    private void onStatutFiltreChanged() {
        appliquerFiltres();
    }
    
    @FXML
    private void onTypeFiltreChanged() {
        appliquerFiltres();
    }
    
    @FXML
    private void onTableClick() {
        // Double-clic pour modifier
        if (tableVehicules.getSelectionModel().getSelectedItem() != null) {
            // Géré par les boutons d'action dans les cellules
        }
    }
    
    @FXML
    private void onModifierVehicule() {
        Vehicule vehicule = tableVehicules.getSelectionModel().getSelectedItem();
        if (vehicule != null) {
            ouvrirFormulaireModification(vehicule);
        }
    }
    
    @FXML
    private void onSupprimerVehicule() {
        Vehicule vehicule = tableVehicules.getSelectionModel().getSelectedItem();
        if (vehicule != null) {
            supprimerVehicule(vehicule);
        }
    }
    
    @FXML
    private void onVoirDetails() {
        Vehicule vehicule = tableVehicules.getSelectionModel().getSelectedItem();
        if (vehicule != null) {
            afficherDetailsVehicule(vehicule);
        }
    }
    
    @FXML
    private void onChangerStatut() {
        Vehicule vehicule = tableVehicules.getSelectionModel().getSelectedItem();
        if (vehicule != null) {
            changerStatutRapide(vehicule);
        }
    }
    
    @FXML
    private void onExporter() {
        // TODO: Implémenter l'export CSV/Excel
        showInfo("Export", "Fonctionnalité d'export en cours de développement");
    }
    
    @FXML
    private void onImporter() {
        // TODO: Implémenter l'import CSV/Excel
        showInfo("Import", "Fonctionnalité d'import en cours de développement");
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Applique les filtres de recherche et de statut
     */
    private void appliquerFiltres() {
        String recherche = txtRecherche.getText();
        StatutVehicule statutFiltre = cmbStatutFiltre.getValue();
        TypeVehicule typeFiltre = cmbTypeFiltre.getValue();
        
        ObservableList<Vehicule> vehiculesFiltres;
        
        if ((recherche == null || recherche.trim().isEmpty()) && 
            statutFiltre == null && typeFiltre == null) {
            // Aucun filtre, charger tous
            vehiculesFiltres = vehiculeRepository.findAll();
        } else {
            // Appliquer les filtres
            if (recherche != null && !recherche.trim().isEmpty()) {
                vehiculesFiltres = vehiculeRepository.search(recherche);
            } else {
                vehiculesFiltres = vehiculeRepository.findAll();
            }
            
            // Filtrer par statut si nécessaire
            if (statutFiltre != null) {
                vehiculesFiltres.removeIf(v -> v.getStatut() != statutFiltre);
            }
            
            // Filtrer par type si nécessaire
            if (typeFiltre != null) {
                vehiculesFiltres.removeIf(v -> v.getTypeVehicule() != typeFiltre);
            }
        }
        
        tableVehicules.setItems(vehiculesFiltres);
        lblNombreVehicules.setText(vehiculesFiltres.size() + " véhicule(s)");
    }
    
    /**
     * Ouvre le formulaire de création d'un nouveau véhicule
     */
    private void ouvrirFormulaireCreation() {
        ouvrirFormulaire(null);
    }
    
    /**
     * Ouvre le formulaire de modification d'un véhicule
     */
    private void ouvrirFormulaireModification(Vehicule vehicule) {
        ouvrirFormulaire(vehicule);
    }
    
    /**
     * Ouvre le formulaire de véhicule (création ou modification)
     */
    private void ouvrirFormulaire(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vehicules/vehicule_form.fxml"));
            Parent root = loader.load();
            
            VehiculeFormController controller = loader.getController();
            controller.setVehicule(vehicule);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle(vehicule == null ? "Nouveau Véhicule" : "Modifier Véhicule");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de véhicule");
        }
    }
    
    /**
     * Supprime un véhicule après confirmation
     */
    private void supprimerVehicule(Vehicule vehicule) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer véhicule");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le véhicule " + vehicule.getDisplayName() + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (vehiculeRepository.delete(vehicule.getId())) {
                loadVehicules();
                updateStatistiques();
                showInfo("Suppression", "Véhicule supprimé avec succès");
            } else {
                showError("Erreur", "Impossible de supprimer le véhicule");
            }
        }
    }
    
    /**
     * Change rapidement le statut d'un véhicule
     */
    private void changerStatutRapide(Vehicule vehicule) {
        ChoiceDialog<StatutVehicule> dialog = new ChoiceDialog<>(vehicule.getStatut(), StatutVehicule.values());
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Nouveau statut pour " + vehicule.getDisplayName());
        dialog.setContentText("Choisir le statut:");
        
        Optional<StatutVehicule> result = dialog.showAndWait();
        result.ifPresent(nouveauStatut -> {
            vehicule.setStatut(nouveauStatut);
            if (vehiculeRepository.save(vehicule)) {
                loadVehicules();
                updateStatistiques();
                showInfo("Statut modifié", "Le statut du véhicule a été mis à jour");
            } else {
                showError("Erreur", "Impossible de modifier le statut");
            }
        });
    }
    
    /**
     * Affiche les détails d'un véhicule
     */
    private void afficherDetailsVehicule(Vehicule vehicule) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du véhicule");
        alert.setHeaderText(vehicule.getDisplayName());
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(vehicule.getId()).append("\n");
        details.append("Immatriculation: ").append(vehicule.getImmatriculation()).append("\n");
        details.append("Type: ").append(vehicule.getTypeVehicule() != null ? vehicule.getTypeVehicule().getDisplayName() : "-").append("\n");
        details.append("Marque: ").append(vehicule.getMarque() != null ? vehicule.getMarque() : "-").append("\n");
        details.append("Modèle: ").append(vehicule.getModele() != null ? vehicule.getModele() : "-").append("\n");
        details.append("Année: ").append(vehicule.getAnnee() != 0 ? vehicule.getAnnee() : "-").append("\n");
        details.append("Kilométrage: ").append(vehicule.getKilometrage()).append(" km\n");
        details.append("Statut: ").append(vehicule.getStatut() != null ? vehicule.getStatut().getDisplayName() : "-").append("\n");
        details.append("Location externe: ").append(vehicule.isLocationExterne() ? "Oui" : "Non").append("\n");
        details.append("Créé le: ").append(vehicule.getDateCreation() != null ? vehicule.getDateCreation() : "-").append("\n");
        details.append("Modifié le: ").append(vehicule.getDateModification() != null ? vehicule.getDateModification() : "-").append("\n");
        
        if (vehicule.getNotes() != null && !vehicule.getNotes().trim().isEmpty()) {
            details.append("\nNotes:\n").append(vehicule.getNotes());
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    /**
     * Méthode appelée par le formulaire après sauvegarde
     */
    public void onVehiculesSaved() {
        loadVehicules();
        updateStatistiques();
    }
    
    // === MÉTHODES D'AFFICHAGE ===
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}