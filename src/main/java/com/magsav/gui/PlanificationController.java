package com.magsav.gui;

import com.magsav.model.Planification;
import com.magsav.model.Technicien;
import com.magsav.repo.PlanificationRepository;
import com.magsav.repo.TechnicienRepository;
import com.magsav.service.google.GoogleIntegrationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la planification des interventions
 */
public class PlanificationController implements Initializable {

    // === COMPOSANTS FXML ===

    // En-tête et navigation
    @FXML private Label lblTitre;
    @FXML private Label lblStatus;
    @FXML private DatePicker dpSemaine;
    @FXML private Button btnSemainePrec;
    @FXML private Button btnSemaineSuiv;
    @FXML private Button btnAujourdhui;

    // Filtres
    @FXML private ComboBox<Technicien> cbTechnicien;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField txtRecherche;
    @FXML private Button btnFiltrer;
    @FXML private Button btnResetFiltres;

    // Onglets
    @FXML private TabPane tabPane;

    // Onglet Planning hebdomadaire
    @FXML private GridPane gridPlanning;
    @FXML private ScrollPane scrollPlanning;

    // Onglet Liste des planifications
    @FXML private TableView<Planification> tablePlanifications;
    @FXML private TableColumn<Planification, String> colDateHeure;
    @FXML private TableColumn<Planification, String> colTechnicien;
    @FXML private TableColumn<Planification, String> colClient;
    @FXML private TableColumn<Planification, String> colType;
    @FXML private TableColumn<Planification, String> colStatut;
    @FXML private TableColumn<Planification, String> colDuree;
    @FXML private TableColumn<Planification, Void> colActions;

    // Actions planification
    @FXML private Button btnNouvelleIntervention;
    @FXML private Button btnModifierPlanification;
    @FXML private Button btnSupprimerPlanification;
    @FXML private Button btnMarquerTerminee;

    // Onglet Disponibilités
    @FXML private TableView<Technicien> tableTechniciens;
    @FXML private TableColumn<Technicien, String> colTechNom;
    @FXML private TableColumn<Technicien, String> colTechStatut;
    @FXML private TableColumn<Technicien, String> colTechDisponibilite;
    @FXML private TableColumn<Technicien, Integer> colTechNbInterventions;
    @FXML private TableColumn<Technicien, String> colTechChargeJour;

    // Statistiques
    @FXML private Label lblTotalInterventions;
    @FXML private Label lblInterventionsPlanifiees;
    @FXML private Label lblInterventionsTerminees;
    @FXML private Label lblTauxOccupation;

    // === PROPRIÉTÉS ===

    private PlanificationRepository planificationRepository;
    private TechnicienRepository technicienRepository;
    private GoogleIntegrationService googleService;
    
    private ObservableList<Planification> planifications;
    private ObservableList<Technicien> techniciens;
    
    private LocalDate semaineCourante;
    private DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        planificationRepository = new PlanificationRepository();
        technicienRepository = new TechnicienRepository();
        googleService = GoogleIntegrationService.getInstance();
        
        planifications = FXCollections.observableArrayList();
        techniciens = FXCollections.observableArrayList();
        
        semaineCourante = LocalDate.now();

        // Configuration des composants
        setupComboBoxes();
        setupTablePlanifications();
        setupTableTechniciens();
        setupPlanningGrid();
        setupEventHandlers();

        // Chargement initial des données
        chargerTechniciens();
        chargerPlanifications();
        afficherSemaine();
        calculerStatistiques();
        
        updateStatus("Planning chargé");
    }

    private void setupComboBoxes() {
        // Techniciens
        cbTechnicien.setConverter(new StringConverter<Technicien>() {
            @Override
            public String toString(Technicien technicien) {
                return technicien != null ? technicien.getNom() + " " + technicien.getPrenom() : "";
            }

            @Override
            public Technicien fromString(String string) {
                return null;
            }
        });

        // Statuts
        cbStatut.setItems(FXCollections.observableArrayList(
            "Tous les statuts",
            "Planifiée", "Confirmée", "En cours", "Terminée", "Reportée", "Annulée"
        ));
        cbStatut.setValue("Tous les statuts");
    }

    private void setupTablePlanifications() {
        // Configuration des colonnes
        colDateHeure.setCellValueFactory(cellData -> {
            Planification p = cellData.getValue();
            String dateHeure = p.getDatePlanifiee(); // Utilise la propriété existante
            return new SimpleStringProperty(dateHeure);
        });
        
        colTechnicien.setCellValueFactory(new PropertyValueFactory<>("technicienNom"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeIntervention"));
        
        colStatut.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatut().getDisplayName());
        });
        
        colDuree.setCellValueFactory(cellData -> {
            int duree = cellData.getValue().getDureeEstimee();
            return new SimpleStringProperty(duree + "h");
        });
        
        // Actions
        colActions.setCellFactory(col -> new TableCell<Planification, Void>() {
            private final Button btnAction = new Button("Actions");
            private final ContextMenu contextMenu = new ContextMenu();
            
            {
                btnAction.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px;");
                
                MenuItem voirItem = new MenuItem("👁 Voir");
                MenuItem modifierItem = new MenuItem("✏️ Modifier");
                MenuItem terminerItem = new MenuItem("✅ Terminer");
                MenuItem supprimerItem = new MenuItem("🗑 Supprimer");
                
                voirItem.setOnAction(e -> voirPlanification(getTableRow().getItem()));
                modifierItem.setOnAction(e -> modifierPlanification(getTableRow().getItem()));
                terminerItem.setOnAction(e -> marquerTerminee(getTableRow().getItem()));
                supprimerItem.setOnAction(e -> supprimerPlanification());
                
                contextMenu.getItems().addAll(voirItem, modifierItem, terminerItem, supprimerItem);
                btnAction.setContextMenu(contextMenu);
                
                btnAction.setOnAction(e -> contextMenu.show(btnAction, 0, btnAction.getHeight()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAction);
            }
        });
        
        // Configuration du tableau
        tablePlanifications.setItems(planifications);
        
        // Sélection
        tablePlanifications.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnModifierPlanification.setDisable(!hasSelection);
            btnSupprimerPlanification.setDisable(!hasSelection);
            btnMarquerTerminee.setDisable(!hasSelection || 
                newSelection.getStatut() == Planification.StatutPlanification.TERMINE);
        });
    }

    private void setupTableTechniciens() {
        colTechNom.setCellValueFactory(cellData -> {
            Technicien t = cellData.getValue();
            return new SimpleStringProperty(t.getNom() + " " + t.getPrenom());
        });
        
        colTechStatut.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatut().getDisplayName());
        });
        
        colTechDisponibilite.setCellValueFactory(cellData -> {
            return new SimpleStringProperty("Disponible"); // Valeur par défaut
        });
        
        colTechNbInterventions.setCellValueFactory(cellData -> {
            // TODO: Calculer le nombre d'interventions du jour
            return new SimpleObjectProperty<>(0);
        });
        
        colTechChargeJour.setCellValueFactory(cellData -> {
            return new SimpleStringProperty("0%"); // TODO: Calculer la charge
        });
        
        tableTechniciens.setItems(techniciens);
    }

    private void setupPlanningGrid() {
        // Configuration du planning hebdomadaire
        gridPlanning.setHgap(5);
        gridPlanning.setVgap(5);
        
        // Initialisation de la date picker
        dpSemaine.setValue(semaineCourante);
        dpSemaine.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                semaineCourante = newValue;
                afficherSemaine();
            }
        });
    }

    private void setupEventHandlers() {
        // Navigation semaine
        btnSemainePrec.setOnAction(e -> onSemainePrec());
        btnSemaineSuiv.setOnAction(e -> onSemaineSuiv());
        btnAujourdhui.setOnAction(e -> onAujourdhui());
        
        // Actions planification
        btnNouvelleIntervention.setOnAction(e -> onNouvelleIntervention());
        btnModifierPlanification.setOnAction(e -> onModifierPlanification());
        btnSupprimerPlanification.setOnAction(e -> onSupprimerPlanification());
        btnMarquerTerminee.setOnAction(e -> onMarquerTerminee());
        
        // Filtres
        btnFiltrer.setOnAction(e -> onFiltrer());
        btnResetFiltres.setOnAction(e -> onResetFiltres());
        
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((obs, oldText, newText) -> appliquerFiltres());
        cbTechnicien.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        cbStatut.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
    }

    // === MÉTHODES D'ACTION ===

    @FXML
    private void onSemainePrec() {
        semaineCourante = semaineCourante.minusWeeks(1);
        dpSemaine.setValue(semaineCourante);
        afficherSemaine();
    }

    @FXML
    private void onSemaineSuiv() {
        semaineCourante = semaineCourante.plusWeeks(1);
        dpSemaine.setValue(semaineCourante);
        afficherSemaine();
    }

    @FXML
    private void onAujourdhui() {
        semaineCourante = LocalDate.now();
        dpSemaine.setValue(semaineCourante);
        afficherSemaine();
    }

    @FXML
    private void onNouvelleIntervention() {
        // TODO: Ouvrir le formulaire de nouvelle intervention
        updateStatus("Création d'une nouvelle intervention...");
    }

    @FXML
    private void onModifierPlanification() {
        Planification planification = tablePlanifications.getSelectionModel().getSelectedItem();
        if (planification != null) {
            modifierPlanification(planification);
        }
    }

    @FXML
    private void onSupprimerPlanification() {
        supprimerPlanification();
    }

    @FXML
    private void onMarquerTerminee() {
        Planification planification = tablePlanifications.getSelectionModel().getSelectedItem();
        if (planification != null) {
            marquerTerminee(planification);
        }
    }

    @FXML
    private void onFiltrer() {
        appliquerFiltres();
    }

    @FXML
    private void onResetFiltres() {
        txtRecherche.clear();
        cbTechnicien.setValue(null);
        cbStatut.setValue("Tous les statuts");
        appliquerFiltres();
        updateStatus("Filtres réinitialisés");
    }

    // === MÉTHODES GOOGLE CALENDAR ===

    @FXML
    private void onSyncGoogle() {
        onSynchroniserCalendrier();
    }

    @FXML
    private void onSynchroniserCalendrier() {
        if (!googleService.isCalendarAvailable()) {
            showError("Service Google Calendar non configuré", 
                "Veuillez configurer Google Calendar dans les paramètres.");
            return;
        }

        Task<Void> syncTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Synchronisation avec Google Calendar...");
                // Synchroniser toutes les planifications avec Google Calendar
                List<Planification> planifications = planificationRepository.findAll();
                for (Planification p : planifications) {
                    googleService.syncPlanification(p).get();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateStatus("Synchronisation Google Calendar terminée");
                    chargerPlanifications();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur de synchronisation", 
                        "Erreur lors de la synchronisation avec Google Calendar: " + 
                        getException().getMessage());
                });
            }
        };

        new Thread(syncTask).start();
    }

    private void synchroniserPlanificationAvecCalendar(Planification planification) {
        if (!googleService.isCalendarAvailable()) {
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                googleService.syncPlanification(planification).get();
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Erreur sync Google Calendar: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    private void supprimerEvenementCalendar(Planification planification) {
        if (!googleService.isCalendarAvailable() || 
            planification.getGoogleEventId() == null) {
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                googleService.deletePlanification(planification.getGoogleEventId()).get();
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Erreur suppression Google Calendar: " + getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    // === MÉTHODES PRIVÉES ===

    private void chargerTechniciens() {
        Task<ObservableList<Technicien>> task = new Task<ObservableList<Technicien>>() {
            @Override
            protected ObservableList<Technicien> call() throws Exception {
                return technicienRepository.findAll();
            }
            
            @Override
            protected void succeeded() {
                techniciens.setAll(getValue());
                cbTechnicien.setItems(FXCollections.observableArrayList(techniciens));
                Platform.runLater(() -> updateStatus("Techniciens chargés"));
            }
        };
        
        new Thread(task).start();
    }

    private void chargerPlanifications() {
        Task<ObservableList<Planification>> task = new Task<ObservableList<Planification>>() {
            @Override
            protected ObservableList<Planification> call() throws Exception {
                LocalDate debut = semaineCourante.with(java.time.DayOfWeek.MONDAY);
                LocalDate fin = debut.plusDays(6);
                return planificationRepository.findPlanificationsSemaine(debut, fin);
            }
            
            @Override
            protected void succeeded() {
                planifications.setAll(getValue());
                Platform.runLater(() -> {
                    afficherPlanningGrid();
                    calculerStatistiques();
                    updateStatus("Planifications chargées");
                });
            }
        };
        
        new Thread(task).start();
    }

    private void afficherSemaine() {
        LocalDate debut = semaineCourante.with(java.time.DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);
        
        lblTitre.setText("Planning du " + debut.format(formatDate) + " au " + fin.format(formatDate));
        
        chargerPlanifications();
    }

    private void afficherPlanningGrid() {
        gridPlanning.getChildren().clear();
        
        // En-têtes des jours
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        LocalDate debut = semaineCourante.with(java.time.DayOfWeek.MONDAY);
        
        for (int i = 0; i < 7; i++) {
            Label lblJour = new Label(jours[i] + " " + debut.plusDays(i).format(formatDate));
            lblJour.setStyle("-fx-font-weight: bold; -fx-padding: 10px;");
            gridPlanning.add(lblJour, i, 0);
        }
        
        // Créneaux horaires (8h-18h)
        for (int heure = 8; heure <= 18; heure++) {
            Label lblHeure = new Label(String.format("%02d:00", heure));
            lblHeure.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
            gridPlanning.add(lblHeure, 0, heure - 7);
            
            // Cases pour chaque jour
            for (int jour = 1; jour <= 7; jour++) {
                VBox caseHoraire = new VBox();
                caseHoraire.setStyle("-fx-border-color: #ddd; -fx-min-height: 40px; -fx-padding: 2px;");
                
                // Recherche des interventions pour ce créneau (version simplifiée)
                LocalDate dateJour = debut.plusDays(jour - 1);
                for (Planification p : planifications) {
                    // TODO: Améliorer la logique de correspondance date/heure avec la propriété datePlanifiee
                    if (p.getDatePlanifiee().contains(dateJour.toString())) {
                        Label lblIntervention = new Label(p.getClientNom() + " - " + p.getTechnicienNom());
                        lblIntervention.setStyle("-fx-background-color: " + p.getStatut().getColor() + 
                                               "; -fx-text-fill: white; -fx-padding: 2px; -fx-font-size: 10px;");
                        caseHoraire.getChildren().add(lblIntervention);
                    }
                }
                
                gridPlanning.add(caseHoraire, jour, heure - 7);
            }
        }
    }

    private void appliquerFiltres() {
        // TODO: Implémenter le filtrage des planifications
        updateStatus("Filtres appliqués");
    }

    private void calculerStatistiques() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int total = planifications.size();
                int planifiees = (int) planifications.stream()
                    .filter(p -> p.getStatut() == Planification.StatutPlanification.PLANIFIE ||
                                p.getStatut() == Planification.StatutPlanification.EN_COURS)
                    .count();
                int terminees = (int) planifications.stream()
                    .filter(p -> p.getStatut() == Planification.StatutPlanification.TERMINE)
                    .count();
                
                double tauxOccupation = techniciens.isEmpty() ? 0 : 
                    (double) total / (techniciens.size() * 7) * 100; // 7 jours
                
                Platform.runLater(() -> {
                    lblTotalInterventions.setText(String.valueOf(total));
                    lblInterventionsPlanifiees.setText(String.valueOf(planifiees));
                    lblInterventionsTerminees.setText(String.valueOf(terminees));
                    lblTauxOccupation.setText(String.format("%.1f%%", tauxOccupation));
                });
                
                return null;
            }
        };
        
        new Thread(task).start();
    }

    private void voirPlanification(Planification planification) {
        updateStatus("Affichage de l'intervention : " + planification.getClientNom());
    }

    private void modifierPlanification(Planification planification) {
        updateStatus("Modification de l'intervention : " + planification.getClientNom());
    }

    private void marquerTerminee(Planification planification) {
        planification.setStatut(Planification.StatutPlanification.TERMINE);
        planification.setDateFinReel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return planificationRepository.save(planification);
            }
            
            @Override
            protected void succeeded() {
                if (getValue()) {
                    // Synchroniser avec Google Calendar
                    synchroniserPlanificationAvecCalendar(planification);
                    
                    Platform.runLater(() -> {
                        tablePlanifications.refresh();
                        afficherPlanningGrid();
                        calculerStatistiques();
                        updateStatus("Intervention marquée comme terminée");
                    });
                }
            }
        };
        
        new Thread(task).start();
    }

    private void supprimerPlanification() {
        Planification planification = tablePlanifications.getSelectionModel().getSelectedItem();
        if (planification == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la planification");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette planification ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Supprimer aussi de Google Calendar
                    if (planification.getGoogleEventId() != null && !planification.getGoogleEventId().isEmpty()) {
                        supprimerEvenementCalendar(planification);
                    }
                    return planificationRepository.delete(planification.getId());
                }
                
                @Override
                protected void succeeded() {
                    if (getValue()) {
                        Platform.runLater(() -> {
                            planifications.remove(planification);
                            afficherPlanningGrid();
                            calculerStatistiques();
                            updateStatus("Planification supprimée");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showError("Erreur", "Impossible de supprimer la planification");
                        });
                    }
                }
            };
            
            new Thread(task).start();
        }
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}