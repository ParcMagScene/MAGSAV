package com.magsav.gui;

import com.magsav.model.Commande;
import com.magsav.model.LigneCommande;
import com.magsav.repo.CommandeRepository;
import com.magsav.service.NavigationService;
import com.magsav.util.Views;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Contrôleur principal pour la gestion des commandes
 */
public class CommandesController implements Initializable {

    // Composants FXML - En-tête et outils
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbFournisseur;
    @FXML private Button btnNouvelleCommande;
    @FXML private Button btnRechercher;
    @FXML private Button btnFiltrer;
    @FXML private Button btnReset;
    @FXML private Button btnActualiser;

    // Statistiques
    @FXML private Label lblTotalCommandes;
    @FXML private Label lblCommandesEnAttente;
    @FXML private Label lblCommandesLivrees;
    @FXML private Label lblMontantTotal;

    // Onglets
    @FXML private TabPane tabPane;

    // Onglet Liste des commandes
    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, String> colNumero;
    @FXML private TableColumn<Commande, String> colFournisseur;
    @FXML private TableColumn<Commande, String> colDateCommande;
    @FXML private TableColumn<Commande, String> colStatut;
    @FXML private TableColumn<Commande, String> colType;
    @FXML private TableColumn<Commande, String> colMontantTTC;
    @FXML private TableColumn<Commande, String> colDateLivraison;
    @FXML private TableColumn<Commande, Integer> colNbLignes;
    @FXML private TableColumn<Commande, Void> colActions;

    // Actions commandes
    @FXML private Button btnVoirCommande;
    @FXML private Button btnModifierCommande;
    @FXML private Button btnDupliquerCommande;
    @FXML private Button btnSupprimerCommande;

    // Onglet Suivi des livraisons
    @FXML private TableView<Commande> tableLivraisons;
    @FXML private TableColumn<Commande, String> colLivNumero;
    @FXML private TableColumn<Commande, String> colLivFournisseur;
    @FXML private TableColumn<Commande, String> colLivDatePrevue;
    @FXML private TableColumn<Commande, String> colLivTransporteur;
    @FXML private TableColumn<Commande, String> colLivNumeroSuivi;
    @FXML private TableColumn<Commande, String> colLivStatut;
    @FXML private TableColumn<Commande, Void> colLivActions;

    @FXML private Button btnActualiserLivraisons;
    @FXML private Button btnMarquerLivree;
    @FXML private Button btnMettreAJourSuivi;

    // Onglet Statistiques
    @FXML private GridPane gridStatuts;
    @FXML private GridPane gridFinances;
    @FXML private TableView<Map<String, Object>> tableFournisseurs;
    @FXML private TableColumn<Map<String, Object>, String> colFournisseurNom;
    @FXML private TableColumn<Map<String, Object>, Integer> colFournisseurNbCommandes;
    @FXML private TableColumn<Map<String, Object>, String> colFournisseurMontant;

    // Barre de statut
    @FXML private Label lblStatus;
    @FXML private Label lblNombreCommandes;

    // Services et données
    private CommandeRepository commandeRepository;
    private NavigationService navigationService;
    private ObservableList<Commande> commandes;
    private FilteredList<Commande> commandesFiltrees;
    private NumberFormat currencyFormat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        commandeRepository = new CommandeRepository();
        // NavigationService utilise des méthodes statiques
        commandes = FXCollections.observableArrayList();
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

        // Configuration des composants
        setupTableCommandes();
        setupTableLivraisons();
        setupTableFournisseurs();
        setupFiltres();
        setupEventHandlers();

        // Chargement initial des données
        chargerCommandes();
        chargerStatistiques();
        
        updateStatus("Prêt");
    }

    private void setupTableCommandes() {
        // Configuration des colonnes
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCommande"));
        colFournisseur.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
        
        colDateCommande.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateCommande();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });
        
        colStatut.setCellValueFactory(cellData -> {
            Commande.StatutCommande statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(statut != null ? statut.getDisplayName() : "");
        });
        
        colType.setCellValueFactory(cellData -> {
            Commande.TypeCommande type = cellData.getValue().getType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "");
        });
        
        colMontantTTC.setCellValueFactory(cellData -> {
            BigDecimal montant = cellData.getValue().getMontantTTC();
            return new SimpleStringProperty(montant != null ? currencyFormat.format(montant) : "0,00 €");
        });
        
        colDateLivraison.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getResumeLivraison());
        });
        
        colNbLignes.setCellValueFactory(new PropertyValueFactory<>("nombreLignes"));
        
        // Colonne actions
        colActions.setCellFactory(col -> new TableCell<Commande, Void>() {
            private final Button btnAction = new Button("Actions");
            private final ContextMenu contextMenu = new ContextMenu();
            
            {
                btnAction.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px;");
                
                MenuItem voirItem = new MenuItem("👁 Voir");
                MenuItem modifierItem = new MenuItem("✏️ Modifier");
                MenuItem dupliquerItem = new MenuItem("📋 Dupliquer");
                MenuItem supprimerItem = new MenuItem("🗑 Supprimer");
                
                voirItem.setOnAction(e -> voirCommande(getTableRow().getItem()));
                modifierItem.setOnAction(e -> modifierCommande(getTableRow().getItem()));
                dupliquerItem.setOnAction(e -> dupliquerCommande(getTableRow().getItem()));
                supprimerItem.setOnAction(e -> supprimerCommande(getTableRow().getItem()));
                
                contextMenu.getItems().addAll(voirItem, modifierItem, dupliquerItem, supprimerItem);
                btnAction.setContextMenu(contextMenu);
                
                btnAction.setOnAction(e -> contextMenu.show(btnAction, 0, btnAction.getHeight()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAction);
            }
        });
        
        // Configuration générale du tableau
        tableCommandes.setRowFactory(tv -> {
            TableRow<Commande> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldCommande, newCommande) -> {
                if (newCommande != null) {
                    String style = getStyleForStatut(newCommande.getStatut());
                    row.setStyle(style);
                }
            });
            return row;
        });
        
        // Sélection
        tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnVoirCommande.setDisable(!hasSelection);
            btnModifierCommande.setDisable(!hasSelection || !newSelection.peutEtreModifiee());
            btnDupliquerCommande.setDisable(!hasSelection);
            btnSupprimerCommande.setDisable(!hasSelection || !newSelection.peutEtreModifiee());
        });
    }

    private void setupTableLivraisons() {
        colLivNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCommande"));
        colLivFournisseur.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
        
        colLivDatePrevue.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateLivraisonPrevue();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Non définie");
        });
        
        colLivTransporteur.setCellValueFactory(new PropertyValueFactory<>("transporteur"));
        colLivNumeroSuivi.setCellValueFactory(new PropertyValueFactory<>("numeroSuivi"));
        
        colLivStatut.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatut().getDisplayName());
        });
        
        // Actions livraisons
        colLivActions.setCellFactory(col -> new TableCell<Commande, Void>() {
            private final Button btnMarquer = new Button("✅");
            private final Button btnSuivi = new Button("📦");
            private final HBox buttons = new HBox(5, btnMarquer, btnSuivi);
            
            {
                btnMarquer.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSuivi.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                buttons.setAlignment(Pos.CENTER);
                
                btnMarquer.setOnAction(e -> marquerCommandeLivree(getTableRow().getItem()));
                btnSuivi.setOnAction(e -> mettreAJourSuivi(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        // Sélection livraisons
        tableLivraisons.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnMarquerLivree.setDisable(!hasSelection);
            btnMettreAJourSuivi.setDisable(!hasSelection);
        });
    }

    private void setupTableFournisseurs() {
        colFournisseurNom.setCellValueFactory(cellData -> {
            return new SimpleStringProperty((String) cellData.getValue().get("nom"));
        });
        
        colFournisseurNbCommandes.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<>((Integer) cellData.getValue().get("nbCommandes"));
        });
        
        colFournisseurMontant.setCellValueFactory(cellData -> {
            BigDecimal montant = (BigDecimal) cellData.getValue().get("montantTotal");
            return new SimpleStringProperty(currencyFormat.format(montant));
        });
    }

    private void setupFiltres() {
        // Configuration des ComboBox
        cbStatut.getItems().addAll(
            "Tous les statuts",
            "Brouillon", "Validée", "Envoyée", "Confirmée", 
            "Expédiée", "Livrée", "Reçue", "Facturée", "Annulée"
        );
        cbStatut.setValue("Tous les statuts");
        
        // Les fournisseurs seront chargés dynamiquement
        cbFournisseur.setValue("Tous les fournisseurs");
        
        // Filtrage en temps réel
        commandesFiltrees = new FilteredList<>(commandes);
        tableCommandes.setItems(commandesFiltrees);
        
        // Listeners pour filtrage automatique
        txtRecherche.textProperty().addListener((obs, oldText, newText) -> appliquerFiltres());
        cbStatut.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        cbFournisseur.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
    }

    private void setupEventHandlers() {
        // Gestionnaires d'événements pour les boutons
        btnNouvelleCommande.setOnAction(e -> onNouvelleCommande());
        btnRechercher.setOnAction(e -> onRechercher());
        btnFiltrer.setOnAction(e -> onFiltrer());
        btnReset.setOnAction(e -> onResetFiltres());
        btnActualiser.setOnAction(e -> onActualiser());
        
        btnVoirCommande.setOnAction(e -> onVoirCommande());
        btnModifierCommande.setOnAction(e -> onModifierCommande());
        btnDupliquerCommande.setOnAction(e -> onDupliquerCommande());
        btnSupprimerCommande.setOnAction(e -> onSupprimerCommande());
        
        btnActualiserLivraisons.setOnAction(e -> onActualiserLivraisons());
        btnMarquerLivree.setOnAction(e -> onMarquerLivree());
        btnMettreAJourSuivi.setOnAction(e -> onMettreAJourSuivi());
    }

    // === MÉTHODES D'ACTION ===

    @FXML
    private void onNouvelleCommande() {
        updateStatus("Création d'une nouvelle commande...");
        Views.openInNewWindow(Views.COMMANDE_FORM, "Nouvelle Commande");
    }

    @FXML
    private void onRechercher() {
        appliquerFiltres();
    }

    @FXML
    private void onFiltrer() {
        appliquerFiltres();
    }

    @FXML
    private void onResetFiltres() {
        txtRecherche.clear();
        cbStatut.setValue("Tous les statuts");
        cbFournisseur.setValue("Tous les fournisseurs");
        appliquerFiltres();
        updateStatus("Filtres réinitialisés");
    }

    @FXML
    private void onActualiser() {
        chargerCommandes();
        chargerStatistiques();
        updateStatus("Données actualisées");
    }

    @FXML
    private void onVoirCommande() {
        Commande commande = tableCommandes.getSelectionModel().getSelectedItem();
        if (commande != null) {
            voirCommande(commande);
        }
    }

    @FXML
    private void onModifierCommande() {
        Commande commande = tableCommandes.getSelectionModel().getSelectedItem();
        if (commande != null) {
            modifierCommande(commande);
        }
    }

    @FXML
    private void onDupliquerCommande() {
        Commande commande = tableCommandes.getSelectionModel().getSelectedItem();
        if (commande != null) {
            dupliquerCommande(commande);
        }
    }

    @FXML
    private void onSupprimerCommande() {
        Commande commande = tableCommandes.getSelectionModel().getSelectedItem();
        if (commande != null) {
            supprimerCommande(commande);
        }
    }

    @FXML
    private void onActualiserLivraisons() {
        chargerCommandesEnAttenteLivraison();
    }

    @FXML
    private void onMarquerLivree() {
        Commande commande = tableLivraisons.getSelectionModel().getSelectedItem();
        if (commande != null) {
            marquerCommandeLivree(commande);
        }
    }

    @FXML
    private void onMettreAJourSuivi() {
        Commande commande = tableLivraisons.getSelectionModel().getSelectedItem();
        if (commande != null) {
            mettreAJourSuivi(commande);
        }
    }

    // === MÉTHODES PRIVÉES ===

    private void chargerCommandes() {
        Task<ObservableList<Commande>> task = new Task<ObservableList<Commande>>() {
            @Override
            protected ObservableList<Commande> call() throws Exception {
                updateMessage("Chargement des commandes...");
                return commandeRepository.findAll();
            }
            
            @Override
            protected void succeeded() {
                commandes.setAll(getValue());
                Platform.runLater(() -> {
                    lblNombreCommandes.setText(commandes.size() + " commande(s)");
                    updateStatus("Commandes chargées");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur de chargement", "Impossible de charger les commandes : " + getException().getMessage());
                    updateStatus("Erreur de chargement");
                });
            }
        };
        
        updateStatus("Chargement en cours...");
        new Thread(task).start();
    }

    private void chargerCommandesEnAttenteLivraison() {
        Task<ObservableList<Commande>> task = new Task<ObservableList<Commande>>() {
            @Override
            protected ObservableList<Commande> call() throws Exception {
                return commandeRepository.findCommandesEnAttenteLivraison();
            }
            
            @Override
            protected void succeeded() {
                tableLivraisons.setItems(getValue());
                Platform.runLater(() -> updateStatus("Livraisons actualisées"));
            }
        };
        
        new Thread(task).start();
    }

    private void chargerStatistiques() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Statistiques par statut
                Map<String, Integer> statsStatuts = commandeRepository.getStatistiquesParStatut();
                
                // Montant total du mois
                LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
                LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                BigDecimal montantMois = commandeRepository.getMontantTotalCommandes(debutMois, finMois);
                
                Platform.runLater(() -> {
                    mettreAJourStatistiquesUI(statsStatuts, montantMois);
                });
                
                return null;
            }
        };
        
        new Thread(task).start();
    }

    private void mettreAJourStatistiquesUI(Map<String, Integer> statsStatuts, BigDecimal montantMois) {
        // Mise à jour des labels de statistiques
        int total = statsStatuts.values().stream().mapToInt(Integer::intValue).sum();
        int enAttente = statsStatuts.getOrDefault("EXPEDIE", 0) + statsStatuts.getOrDefault("CONFIRMEE", 0);
        int livrees = statsStatuts.getOrDefault("LIVREE", 0) + statsStatuts.getOrDefault("RECUE", 0);
        
        lblTotalCommandes.setText(String.valueOf(total));
        lblCommandesEnAttente.setText(String.valueOf(enAttente));
        lblCommandesLivrees.setText(String.valueOf(livrees));
        lblMontantTotal.setText(currencyFormat.format(montantMois));
        
        // Mise à jour du grid des statuts
        gridStatuts.getChildren().clear();
        int row = 0;
        for (Map.Entry<String, Integer> entry : statsStatuts.entrySet()) {
            Label lblStatut = new Label(entry.getKey());
            Label lblCount = new Label(entry.getValue().toString());
            lblCount.setFont(Font.font(null, FontWeight.BOLD, 14));
            
            gridStatuts.add(lblStatut, 0, row);
            gridStatuts.add(lblCount, 1, row);
            row++;
        }
    }

    private void appliquerFiltres() {
        commandesFiltrees.setPredicate(commande -> {
            // Filtre de recherche
            String recherche = txtRecherche.getText();
            if (recherche != null && !recherche.isEmpty()) {
                String rechercheMinuscule = recherche.toLowerCase();
                if (!commande.getNumeroCommande().toLowerCase().contains(rechercheMinuscule) &&
                    !commande.getFournisseurNom().toLowerCase().contains(rechercheMinuscule)) {
                    return false;
                }
            }
            
            // Filtre par statut
            String statutFiltre = cbStatut.getValue();
            if (statutFiltre != null && !"Tous les statuts".equals(statutFiltre)) {
                if (!commande.getStatut().getDisplayName().equals(statutFiltre)) {
                    return false;
                }
            }
            
            // Filtre par fournisseur
            String fournisseurFiltre = cbFournisseur.getValue();
            if (fournisseurFiltre != null && !"Tous les fournisseurs".equals(fournisseurFiltre)) {
                if (!commande.getFournisseurNom().equals(fournisseurFiltre)) {
                    return false;
                }
            }
            
            return true;
        });
        
        lblNombreCommandes.setText(commandesFiltrees.size() + " commande(s)");
    }

    private void voirCommande(Commande commande) {
        // TODO: Ouvrir la vue détaillée de la commande
        updateStatus("Affichage de la commande " + commande.getNumeroCommande());
    }

    private void modifierCommande(Commande commande) {
        // TODO: Ouvrir le formulaire d'édition
        updateStatus("Modification de la commande " + commande.getNumeroCommande());
        // TODO: Implémenter l'ouverture avec paramètre commande
        Views.openInNewWindow(Views.COMMANDE_FORM, "Modifier Commande");
    }

    private void dupliquerCommande(Commande commande) {
        // TODO: Créer une copie de la commande
        updateStatus("Duplication de la commande " + commande.getNumeroCommande());
    }

    private void supprimerCommande(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la commande");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande " + commande.getNumeroCommande() + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return commandeRepository.delete(commande.getId());
                }
                
                @Override
                protected void succeeded() {
                    if (getValue()) {
                        Platform.runLater(() -> {
                            commandes.remove(commande);
                            updateStatus("Commande supprimée");
                        });
                    } else {
                        Platform.runLater(() -> {
                            showError("Erreur", "Impossible de supprimer la commande");
                        });
                    }
                }
            };
            
            new Thread(task).start();
        }
    }

    private void marquerCommandeLivree(Commande commande) {
        commande.setStatut(Commande.StatutCommande.LIVREE);
        commande.setDateLivraisonReelle(LocalDate.now());
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return commandeRepository.save(commande);
            }
            
            @Override
            protected void succeeded() {
                if (getValue()) {
                    Platform.runLater(() -> {
                        tableLivraisons.refresh();
                        updateStatus("Commande marquée comme livrée");
                    });
                }
            }
        };
        
        new Thread(task).start();
    }

    private void mettreAJourSuivi(Commande commande) {
        // TODO: Ouvrir dialogue de mise à jour du suivi
        updateStatus("Mise à jour du suivi pour " + commande.getNumeroCommande());
    }

    private String getStyleForStatut(Commande.StatutCommande statut) {
        if (statut == null) return "";
        
        String color = statut.getColor();
        return "-fx-background-color: " + color + "22; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 3;";
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