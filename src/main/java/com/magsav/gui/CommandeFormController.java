package com.magsav.gui;

import com.magsav.model.Commande;
import com.magsav.model.LigneCommande;
import com.magsav.model.Societe;
import com.magsav.repo.CommandeRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.google.GoogleIntegrationService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le formulaire de création/modification de commandes
 */
public class CommandeFormController implements Initializable {

    // === COMPOSANTS FXML ===

    // En-tête du formulaire
    @FXML private Label lblTitre;
    @FXML private Label lblStatus;

    // Informations générales
    @FXML private TextField txtNumeroCommande;
    @FXML private ComboBox<Commande.TypeCommande> cbTypeCommande;
    @FXML private ComboBox<Societe> cbFournisseur;
    @FXML private DatePicker dpDateCommande;
    @FXML private ComboBox<Commande.StatutCommande> cbStatut;
    @FXML private TextField txtReference;
    @FXML private TextArea taDescription;
    @FXML private CheckBox chkUrgente;

    // Dates et livraison
    @FXML private DatePicker dpDateLivraisonPrevue;
    @FXML private TextField txtAdresseLivraison;
    @FXML private TextField txtTransporteur;
    @FXML private TextField txtNumeroSuivi;
    @FXML private DatePicker dpDateLivraisonReelle;

    // Conditions commerciales
    @FXML private ComboBox<String> cbConditionsPaiement;
    @FXML private TextField txtRemise;
    @FXML private TextField txtFraisPort;
    @FXML private TextField txtTauxTVA;
    @FXML private CheckBox chkFacturee;

    // Tableau des lignes de commande
    @FXML private TableView<LigneCommande> tableLignes;
    @FXML private TableColumn<LigneCommande, String> colProduitReference;
    @FXML private TableColumn<LigneCommande, String> colProduitNom;
    @FXML private TableColumn<LigneCommande, Integer> colQuantite;
    @FXML private TableColumn<LigneCommande, BigDecimal> colPrixUnitaire;
    @FXML private TableColumn<LigneCommande, BigDecimal> colRemiseLigne;
    @FXML private TableColumn<LigneCommande, BigDecimal> colMontantHT;
    @FXML private TableColumn<LigneCommande, String> colStatutLigne;
    @FXML private TableColumn<LigneCommande, Void> colActionsLigne;

    // Boutons pour les lignes
    @FXML private Button btnAjouterLigne;
    @FXML private Button btnSupprimerLigne;
    @FXML private Button btnModifierLigne;

    // Totaux
    @FXML private Label lblMontantHT;
    @FXML private Label lblMontantRemise;
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
    private SocieteRepository societeRepository;
    private GoogleIntegrationService googleService;
    private Commande commandeCourante;
    private ObservableList<LigneCommande> lignesCommande;
    private NumberFormat currencyFormat;
    private boolean modeEdition = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        commandeRepository = new CommandeRepository();
        societeRepository = new SocieteRepository();
        googleService = GoogleIntegrationService.getInstance();
        lignesCommande = FXCollections.observableArrayList();
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

        // Configuration des composants
        setupComboBoxes();
        setupTableLignes();
        setupValidation();
        setupEventHandlers();
        setupBindings();

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
                return null; // Pas de conversion inverse nécessaire
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

        // Fournisseurs
        cbFournisseur.setConverter(new StringConverter<Societe>() {
            @Override
            public String toString(Societe societe) {
                return societe != null ? societe.nom() : ""; // Record Societe utilise nom()
            }

            @Override
            public Societe fromString(String string) {
                return null;
            }
        });

        // Conditions de paiement
        cbConditionsPaiement.setItems(FXCollections.observableArrayList(
            "Comptant",
            "30 jours net",
            "45 jours net", 
            "60 jours net",
            "30 jours fin de mois",
            "45 jours fin de mois",
            "Virement à réception"
        ));
        cbConditionsPaiement.setValue("30 jours net");

        // Chargement des fournisseurs
        chargerFournisseurs();
    }

    private void setupTableLignes() {
        // Configuration des colonnes
        colProduitReference.setCellValueFactory(new PropertyValueFactory<>("produitReference"));
        colProduitNom.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteCommandee"));
        colQuantite.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQuantite.setOnEditCommit(event -> {
            event.getRowValue().setQuantiteCommandee(event.getNewValue());
            calculerTotaux();
        });
        
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaireHT"));
        colPrixUnitaire.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colPrixUnitaire.setOnEditCommit(event -> {
            event.getRowValue().setPrixUnitaireHT(event.getNewValue());
            calculerTotaux();
        });
        
        colRemiseLigne.setCellValueFactory(new PropertyValueFactory<>("tauxTVA"));
        colRemiseLigne.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colRemiseLigne.setOnEditCommit(event -> {
            event.getRowValue().setTauxTVA(event.getNewValue().doubleValue());
            calculerTotaux();
        });
        
        colMontantHT.setCellValueFactory(cellData -> {
            LigneCommande ligne = cellData.getValue();
            return new SimpleObjectProperty<>(ligne.getMontantHT());
        });
        
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
        tableLignes.setEditable(true);
        
        // Sélection
        tableLignes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnSupprimerLigne.setDisable(newSelection == null);
            btnModifierLigne.setDisable(newSelection == null);
        });
    }

    private void setupValidation() {
        // Validation des champs obligatoires
        txtNumeroCommande.textProperty().addListener((obs, oldText, newText) -> validerFormulaire());
        cbFournisseur.valueProperty().addListener((obs, oldValue, newValue) -> validerFormulaire());
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
        
        // Changements automatiques
        cbStatut.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && commandeCourante != null) {
                commandeCourante.setStatut(newValue);
            }
        });
    }

    private void setupBindings() {
        // Liaison avec les propriétés de la commande
        if (commandeCourante != null) {
            // Binding bidirectionnel pour les champs éditables
            txtNumeroCommande.textProperty().bindBidirectional(commandeCourante.numeroCommandeProperty());
            txtReference.textProperty().bindBidirectional(commandeCourante.referenceProperty());
            taDescription.textProperty().bindBidirectional(commandeCourante.descriptionProperty());
            chkUrgente.selectedProperty().bindBidirectional(commandeCourante.urgenteProperty());
            txtAdresseLivraison.textProperty().bindBidirectional(commandeCourante.adresseLivraisonProperty());
            txtTransporteur.textProperty().bindBidirectional(commandeCourante.transporteurProperty());
            txtNumeroSuivi.textProperty().bindBidirectional(commandeCourante.numeroSuiviProperty());
            chkFacturee.selectedProperty().bindBidirectional(commandeCourante.factureeProperty());
            
            // Binding pour les dates
            dpDateCommande.valueProperty().bindBidirectional(commandeCourante.dateCommandeProperty());
            dpDateLivraisonPrevue.valueProperty().bindBidirectional(commandeCourante.dateLivraisonPrevueProperty());
            dpDateLivraisonReelle.valueProperty().bindBidirectional(commandeCourante.dateLivraisonReelleProperty());
            
            // Binding pour les ComboBox
            cbTypeCommande.valueProperty().bindBidirectional(commandeCourante.typeProperty());
            cbStatut.valueProperty().bindBidirectional(commandeCourante.statutProperty());
            cbFournisseur.valueProperty().bindBidirectional(commandeCourante.fournisseurProperty());
            
            // Binding pour les montants (lecture seule)
            lblMontantHT.textProperty().bind(Bindings.createStringBinding(() -> {
                return currencyFormat.format(commandeCourante.getMontantHT());
            }, commandeCourante.montantHTProperty()));
            
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
        // TODO: Ouvrir dialogue de sélection de produit
        LigneCommande nouvelleLigne = new LigneCommande();
        nouvelleLigne.setCommande(commandeCourante);
        nouvelleLigne.setProduitReference("REF" + (lignesCommande.size() + 1));
        nouvelleLigne.setProduitNom("Nouveau produit");
        nouvelleLigne.setQuantiteCommandee(1);
        nouvelleLigne.setPrixUnitaire(BigDecimal.ZERO);
        nouvelleLigne.setRemise(BigDecimal.ZERO);
        nouvelleLigne.setStatut(LigneCommande.StatutLigne.COMMANDEE);
        
        lignesCommande.add(nouvelleLigne);
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
            // TODO: Ouvrir dialogue d'édition de ligne
            updateStatus("Modification de ligne non implémentée");
        }
    }

    // === MÉTHODES PUBLIQUES ===

    public void setCommande(Commande commande) {
        this.commandeCourante = commande;
        this.modeEdition = true;
        
        if (commande != null) {
            lblTitre.setText("Modifier Commande - " + commande.getNumeroCommande());
            
            // Chargement des lignes de commande
            Task<ObservableList<LigneCommande>> task = new Task<ObservableList<LigneCommande>>() {
                @Override
                protected ObservableList<LigneCommande> call() throws Exception {
                    return commandeRepository.findLignesCommande(commande.getId());
                }
                
                @Override
                protected void succeeded() {
                    lignesCommande.setAll(getValue());
                    calculerTotaux();
                    Platform.runLater(() -> updateStatus("Commande chargée pour modification"));
                }
            };
            
            new Thread(task).start();
            
            // Re-setup des bindings avec la nouvelle commande
            setupBindings();
        }
    }

    // === MÉTHODES PRIVÉES ===

    private void nouvelleCommande() {
        commandeCourante = new Commande();
        commandeCourante.setNumeroCommande(genererNumeroCommande());
        commandeCourante.setDateCommande(LocalDate.now());
        commandeCourante.setType(Commande.TypeCommande.STANDARD);
        commandeCourante.setStatut(Commande.StatutCommande.BROUILLON);
        commandeCourante.setUrgente(false);
        commandeCourante.setFacturee(false);
        commandeCourante.setMontantHT(BigDecimal.ZERO);
        commandeCourante.setMontantTTC(BigDecimal.ZERO);
        
        lignesCommande.clear();
        modeEdition = false;
        
        lblTitre.setText("Nouvelle Commande");
        setupBindings();
    }

    private void chargerFournisseurs() {
        Task<ObservableList<Societe>> task = new Task<ObservableList<Societe>>() {
            @Override
            protected ObservableList<Societe> call() throws Exception {
                List<Societe> fournisseurs = societeRepository.findByType("FOURNISSEUR");
                return FXCollections.observableList(fournisseurs);
            }
            
            @Override
            protected void succeeded() {
                cbFournisseur.setItems(getValue());
                Platform.runLater(() -> updateStatus("Fournisseurs chargés"));
            }
        };
        
        new Thread(task).start();
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        if (txtNumeroCommande.getText() == null || txtNumeroCommande.getText().trim().isEmpty()) {
            erreurs.append("- Le numéro de commande est obligatoire\n");
        }
        
        if (cbFournisseur.getValue() == null) {
            erreurs.append("- Le fournisseur est obligatoire\n");
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
        // Mise à jour de la commande avec les lignes
        commandeCourante.getLignes().clear();
        commandeCourante.getLignes().addAll(lignesCommande);
        
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
                        
                        // Envoyer email de confirmation si Google Gmail est activé
                        CommandeFormController.this.envoyerEmailConfirmationCommande();
                        
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

    /**
     * Envoie un email de confirmation de commande via Gmail
     */
    private void envoyerEmailConfirmationCommande() {
        if (!googleService.isGmailAvailable() || commandeCourante.getFournisseur() == null) {
            return;
        }

        String emailFournisseur = commandeCourante.getFournisseur().email();
        if (emailFournisseur == null || emailFournisseur.trim().isEmpty()) {
            return;
        }

        Task<Void> emailTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Envoi email de confirmation...");
                
                String nomFournisseur = commandeCourante.getFournisseur().nom();
                String numeroCommande = commandeCourante.getNumeroCommande();
                String montantTotal = currencyFormat.format(commandeCourante.getMontantTTC());
                String dateCommande = commandeCourante.getDateCommande().toString();
                
                // Utiliser la méthode sendOrderConfirmation du GoogleGmailService
                googleService.sendOrderConfirmation(
                    emailFournisseur,
                    nomFournisseur,
                    numeroCommande,
                    montantTotal,
                    dateCommande
                ).get();
                
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateStatus("Email de confirmation envoyé à " + emailFournisseur);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Erreur envoi email: " + getException().getMessage());
                });
            }
        };

        new Thread(emailTask).start();
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
        calculerTotaux();
        updateStatus("Ligne supprimée");
    }

    private void calculerTotaux() {
        BigDecimal montantHT = BigDecimal.ZERO;
        BigDecimal montantRemise = BigDecimal.ZERO;
        
        for (LigneCommande ligne : lignesCommande) {
            BigDecimal montantLigne = ligne.getMontantHT();
            montantHT = montantHT.add(montantLigne);
            
            if (ligne.getRemise() != null) {
                BigDecimal remiseLigne = ligne.getPrixUnitaire()
                    .multiply(new BigDecimal(ligne.getQuantiteCommandee()))
                    .multiply(ligne.getRemise())
                    .divide(new BigDecimal(100));
                montantRemise = montantRemise.add(remiseLigne);
            }
        }
        
        // Application de la remise globale
        String remiseGlobale = txtRemise.getText();
        if (remiseGlobale != null && !remiseGlobale.isEmpty()) {
            try {
                BigDecimal remise = new BigDecimal(remiseGlobale);
                BigDecimal montantRemiseGlobale = montantHT.multiply(remise).divide(new BigDecimal(100));
                montantRemise = montantRemise.add(montantRemiseGlobale);
            } catch (NumberFormatException e) {
                // Ignore si format invalide
            }
        }
        
        // Calcul du montant après remise
        BigDecimal montantApresRemise = montantHT.subtract(montantRemise);
        
        // Frais de port
        BigDecimal fraisPort = BigDecimal.ZERO;
        String fraisPortText = txtFraisPort.getText();
        if (fraisPortText != null && !fraisPortText.isEmpty()) {
            try {
                fraisPort = new BigDecimal(fraisPortText);
            } catch (NumberFormatException e) {
                // Ignore si format invalide
            }
        }
        
        // TVA
        BigDecimal tauxTVA = new BigDecimal("20"); // 20% par défaut
        String tauxTVAText = txtTauxTVA.getText();
        if (tauxTVAText != null && !tauxTVAText.isEmpty()) {
            try {
                tauxTVA = new BigDecimal(tauxTVAText);
            } catch (NumberFormatException e) {
                // Garde la valeur par défaut
            }
        }
        
        BigDecimal montantTVA = montantApresRemise.add(fraisPort).multiply(tauxTVA).divide(new BigDecimal(100));
        BigDecimal montantTTC = montantApresRemise.add(fraisPort).add(montantTVA);
        
        // Mise à jour de l'objet commande
        commandeCourante.setMontantHT(montantHT);
        commandeCourante.setMontantTTC(montantTTC);
        
        // Mise à jour de l'interface
        lblMontantRemise.setText(currencyFormat.format(montantRemise));
        lblMontantTVA.setText(currencyFormat.format(montantTVA));
    }

    private String genererNumeroCommande() {
        // Format : CMD-YYYYMM-XXXX
        LocalDate maintenant = LocalDate.now();
        String prefix = "CMD-" + maintenant.getYear() + String.format("%02d", maintenant.getMonthValue());
        
        // TODO: Récupérer le prochain numéro séquentiel depuis la base
        int sequence = 1;
        
        return prefix + "-" + String.format("%04d", sequence);
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