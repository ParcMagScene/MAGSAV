package com.magsav.gui;

import com.magsav.model.Commande;
import com.magsav.model.LigneCommande;
import com.magsav.repo.CommandeRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur simplifié pour le formulaire de création/modification de commandes
 */
public class CommandeFormControllerSimple implements Initializable {

    // === COMPOSANTS FXML ===

    // En-tête du formulaire
    @FXML private Label lblTitre;
    @FXML private Label lblStatus;

    // Informations générales
    @FXML private TextField txtNumeroCommande;
    @FXML private ComboBox<Commande.TypeCommande> cbTypeCommande;
    @FXML private TextField txtFournisseurNom;
    @FXML private DatePicker dpDateCommande;
    @FXML private ComboBox<Commande.StatutCommande> cbStatut;
    @FXML private TextArea taCommentaires;

    // Dates et livraison
    @FXML private DatePicker dpDateLivraisonPrevue;
    @FXML private TextField txtAdresseLivraison;
    @FXML private TextField txtTransporteur;
    @FXML private TextField txtNumeroSuivi;
    @FXML private DatePicker dpDateLivraisonReelle;

    // Tableau des lignes de commande
    @FXML private TableView<LigneCommande> tableLignes;
    @FXML private TableColumn<LigneCommande, String> colProduitReference;
    @FXML private TableColumn<LigneCommande, String> colProduitNom;
    @FXML private TableColumn<LigneCommande, Integer> colQuantite;
    @FXML private TableColumn<LigneCommande, BigDecimal> colPrixUnitaire;
    @FXML private TableColumn<LigneCommande, BigDecimal> colMontantHT;
    @FXML private TableColumn<LigneCommande, String> colStatutLigne;
    @FXML private TableColumn<LigneCommande, Void> colActionsLigne;

    // Boutons pour les lignes
    @FXML private Button btnAjouterLigne;
    @FXML private Button btnSupprimerLigne;
    @FXML private Button btnModifierLigne;

    // Totaux
    @FXML private Label lblMontantHT;
    @FXML private Label lblMontantTVA;
    @FXML private Label lblMontantTTC;
    @FXML private Label lblNombreLignes;

    // Boutons d'action
    @FXML private Button btnEnregistrer;
    @FXML private Button btnEnregistrerEtFermer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnSupprimer;

    // === PROPRIÉTÉS ===

    private CommandeRepository commandeRepository;
    private Commande commandeCourante;
    private ObservableList<LigneCommande> lignesCommande;
    private NumberFormat currencyFormat;
    private boolean modeEdition = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        commandeRepository = new CommandeRepository();
        lignesCommande = FXCollections.observableArrayList();
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

        // Configuration des composants
        setupComboBoxes();
        setupTableLignes();
        setupValidation();
        setupEventHandlers();

        // Initialisation d'une nouvelle commande par défaut
        nouvelleCommande();
        
        updateStatus("Prêt pour création d'une nouvelle commande");
    }

    private void setupComboBoxes() {
        // Types de commande
        cbTypeCommande.setItems(FXCollections.observableArrayList(Commande.TypeCommande.values()));
        cbTypeCommande.setValue(Commande.TypeCommande.STANDARD);
        cbTypeCommande.setConverter(new StringConverter<Commande.TypeCommande>() {
            @Override
            public String toString(Commande.TypeCommande type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public Commande.TypeCommande fromString(String string) {
                return null;
            }
        });

        // Statuts de commande
        cbStatut.setItems(FXCollections.observableArrayList(Commande.StatutCommande.values()));
        cbStatut.setValue(Commande.StatutCommande.BROUILLON);
        cbStatut.setConverter(new StringConverter<Commande.StatutCommande>() {
            @Override
            public String toString(Commande.StatutCommande statut) {
                return statut != null ? statut.getDisplayName() : "";
            }

            @Override
            public Commande.StatutCommande fromString(String string) {
                return null;
            }
        });
    }

    private void setupTableLignes() {
        // Configuration des colonnes
        colProduitReference.setCellValueFactory(new PropertyValueFactory<>("produitReference"));
        colProduitNom.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteCommandee"));
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaireHT"));
        colMontantHT.setCellValueFactory(new PropertyValueFactory<>("montantHT"));
        
        colStatutLigne.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatutReception().getDisplayName());
        });
        
        // Actions sur les lignes
        colActionsLigne.setCellFactory(col -> new TableCell<LigneCommande, Void>() {
            private final Button btnSupprimer = new Button("🗑");
            
            {
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                btnSupprimer.setOnAction(e -> supprimerLigne(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSupprimer);
            }
        });
        
        // Configuration du tableau
        tableLignes.setItems(lignesCommande);
        
        // Sélection
        tableLignes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnSupprimerLigne.setDisable(newSelection == null);
            btnModifierLigne.setDisable(newSelection == null);
        });
    }

    private void setupValidation() {
        // Validation des champs obligatoires
        txtNumeroCommande.textProperty().addListener((obs, oldText, newText) -> validerFormulaire());
        txtFournisseurNom.textProperty().addListener((obs, oldText, newText) -> validerFormulaire());
        dpDateCommande.valueProperty().addListener((obs, oldValue, newValue) -> validerFormulaire());
    }

    private void setupEventHandlers() {
        // Boutons principaux
        btnEnregistrer.setOnAction(e -> onEnregistrer());
        btnEnregistrerEtFermer.setOnAction(e -> onEnregistrerEtFermer());
        btnAnnuler.setOnAction(e -> onAnnuler());
        btnSupprimer.setOnAction(e -> onSupprimer());
        
        // Boutons lignes
        btnAjouterLigne.setOnAction(e -> onAjouterLigne());
        btnSupprimerLigne.setOnAction(e -> onSupprimerLigne());
        btnModifierLigne.setOnAction(e -> onModifierLigne());
        
        // Binding des propriétés de la commande
        setupBindings();
    }

    private void setupBindings() {
        if (commandeCourante != null) {
            // Binding bidirectionnel pour les champs éditables
            txtNumeroCommande.textProperty().bindBidirectional(commandeCourante.numeroCommandeProperty());
            txtFournisseurNom.textProperty().bindBidirectional(commandeCourante.fournisseurNomProperty());
            taCommentaires.textProperty().bindBidirectional(commandeCourante.commentairesProperty());
            txtAdresseLivraison.textProperty().bindBidirectional(commandeCourante.adresseLivraisonProperty());
            txtTransporteur.textProperty().bindBidirectional(commandeCourante.transporteurProperty());
            txtNumeroSuivi.textProperty().bindBidirectional(commandeCourante.numeroSuiviProperty());
            
            // Binding pour les dates
            dpDateCommande.valueProperty().bindBidirectional(commandeCourante.dateCommandeProperty());
            dpDateLivraisonPrevue.valueProperty().bindBidirectional(commandeCourante.dateLivraisonPrevueProperty());
            dpDateLivraisonReelle.valueProperty().bindBidirectional(commandeCourante.dateLivraisonReelleProperty());
            
            // Binding pour les ComboBox
            cbTypeCommande.valueProperty().bindBidirectional(commandeCourante.typeProperty());
            cbStatut.valueProperty().bindBidirectional(commandeCourante.statutProperty());
            
            // Binding pour les montants (lecture seule)
            lblMontantHT.textProperty().bind(Bindings.createStringBinding(() -> {
                return currencyFormat.format(commandeCourante.getMontantHT());
            }, commandeCourante.montantHTProperty()));
            
            lblMontantTVA.textProperty().bind(Bindings.createStringBinding(() -> {
                return currencyFormat.format(commandeCourante.getMontantTVA());
            }, commandeCourante.montantTVAProperty()));
            
            lblMontantTTC.textProperty().bind(Bindings.createStringBinding(() -> {
                return currencyFormat.format(commandeCourante.getMontantTTC());
            }, commandeCourante.montantTTCProperty()));
            
            lblNombreLignes.textProperty().bind(Bindings.createStringBinding(() -> {
                return String.valueOf(lignesCommande.size()) + " ligne(s)";
            }, lignesCommande));
        }
    }

    // === MÉTHODES D'ACTION ===

    @FXML
    private void onEnregistrer() {
        if (validerFormulaire()) {
            enregistrerCommande(false);
        }
    }

    @FXML
    private void onEnregistrerEtFermer() {
        if (validerFormulaire()) {
            enregistrerCommande(true);
        }
    }

    @FXML
    private void onAnnuler() {
        if (confirmerAnnulation()) {
            fermerFenetre();
        }
    }

    @FXML
    private void onSupprimer() {
        if (confirmerSuppression()) {
            supprimerCommande();
        }
    }

    @FXML
    private void onAjouterLigne() {
        // Création d'une ligne de test
        LigneCommande nouvelleLigne = new LigneCommande();
        nouvelleLigne.setCommandeId(commandeCourante.getId());
        nouvelleLigne.setProduitReference("REF" + (lignesCommande.size() + 1));
        nouvelleLigne.setProduitNom("Nouveau produit");
        nouvelleLigne.setQuantiteCommandee(1);
        nouvelleLigne.setPrixUnitaireHT(BigDecimal.valueOf(10.0));
        nouvelleLigne.setStatutReception(LigneCommande.StatutReception.EN_ATTENTE);
        
        lignesCommande.add(nouvelleLigne);
        commandeCourante.ajouterLigne(nouvelleLigne);
        calculerTotaux();
        
        updateStatus("Ligne ajoutée");
    }

    @FXML
    private void onSupprimerLigne() {
        LigneCommande ligne = tableLignes.getSelectionModel().getSelectedItem();
        if (ligne != null) {
            supprimerLigne(ligne);
        }
    }

    @FXML
    private void onModifierLigne() {
        LigneCommande ligne = tableLignes.getSelectionModel().getSelectedItem();
        if (ligne != null) {
            updateStatus("Modification de ligne : " + ligne.getProduitNom());
        }
    }

    // === MÉTHODES PUBLIQUES ===

    public void setCommande(Commande commande) {
        this.commandeCourante = commande;
        this.modeEdition = true;
        
        if (commande != null) {
            lblTitre.setText("Modifier Commande - " + commande.getNumeroCommande());
            
            // Chargement des lignes de commande
            lignesCommande.setAll(commande.getLignes());
            calculerTotaux();
            
            // Re-setup des bindings avec la nouvelle commande
            setupBindings();
            updateStatus("Commande chargée pour modification");
        }
    }

    // === MÉTHODES PRIVÉES ===

    private void nouvelleCommande() {
        commandeCourante = new Commande();
        commandeCourante.setDateCommande(LocalDate.now());
        commandeCourante.setType(Commande.TypeCommande.STANDARD);
        commandeCourante.setStatut(Commande.StatutCommande.BROUILLON);
        commandeCourante.setMontantHT(BigDecimal.ZERO);
        commandeCourante.setMontantTTC(BigDecimal.ZERO);
        
        lignesCommande.clear();
        modeEdition = false;
        
        lblTitre.setText("Nouvelle Commande");
        setupBindings();
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        if (txtNumeroCommande.getText() == null || txtNumeroCommande.getText().trim().isEmpty()) {
            erreurs.append("- Le numéro de commande est obligatoire\n");
        }
        
        if (txtFournisseurNom.getText() == null || txtFournisseurNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom du fournisseur est obligatoire\n");
        }
        
        if (dpDateCommande.getValue() == null) {
            erreurs.append("- La date de commande est obligatoire\n");
        }
        
        if (lignesCommande.isEmpty()) {
            erreurs.append("- Au moins une ligne de commande est nécessaire\n");
        }
        
        boolean valide = erreurs.length() == 0;
        
        btnEnregistrer.setDisable(!valide);
        btnEnregistrerEtFermer.setDisable(!valide);
        
        if (!valide) {
            updateStatus("Erreurs de validation : " + erreurs.toString().replace("\n", " "));
        } else {
            updateStatus("Formulaire valide");
        }
        
        return valide;
    }

    private void enregistrerCommande(boolean fermerApres) {        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Enregistrement de la commande...");
                return commandeRepository.save(commandeCourante);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        updateStatus("Commande enregistrée avec succès");
                        if (fermerApres) {
                            fermerFenetre();
                        } else {
                            // Passage en mode édition
                            modeEdition = true;
                            lblTitre.setText("Modifier Commande - " + commandeCourante.getNumeroCommande());
                        }
                    } else {
                        showError("Erreur", "Impossible d'enregistrer la commande");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur", "Erreur lors de l'enregistrement : " + getException().getMessage());
                });
            }
        };
        
        updateStatus("Enregistrement en cours...");
        new Thread(task).start();
    }

    private boolean confirmerAnnulation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer l'annulation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Êtes-vous sûr de vouloir annuler ? Les modifications non sauvegardées seront perdues.");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private boolean confirmerSuppression() {
        if (!modeEdition) return false;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la commande");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement cette commande ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void supprimerCommande() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return commandeRepository.delete(commandeCourante.getId());
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        updateStatus("Commande supprimée");
                        fermerFenetre();
                    } else {
                        showError("Erreur", "Impossible de supprimer la commande");
                    }
                });
            }
        };
        
        new Thread(task).start();
    }

    private void supprimerLigne(LigneCommande ligne) {
        lignesCommande.remove(ligne);
        commandeCourante.supprimerLigne(ligne);
        calculerTotaux();
        updateStatus("Ligne supprimée");
    }

    private void calculerTotaux() {
        if (commandeCourante != null) {
            commandeCourante.recalculerMontants();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
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