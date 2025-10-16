package com.magsav.gui.controllers.affaires;

import com.magsav.model.affaires.Affaire;
import com.magsav.model.affaires.StatutAffaire;
import com.magsav.model.affaires.TypeAffaire;
import com.magsav.model.affaires.PrioriteAffaire;
import com.magsav.service.affaires.AffairesService;
import com.magsav.util.DialogUtils;
import com.magsav.db.DB;
import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des affaires commerciales
 */
public class AffairesController {
    
    private final AffairesService affairesService;
    private final ObservableList<Affaire> affairesData;
    
    // Composants UI
    private TableView<Affaire> tableAffaires;
    private TextField rechercheField;
    private ComboBox<String> filtreStatutCombo;
    private ComboBox<String> filtrePrioriteCombo;
    private ComboBox<String> filtreClientCombo;
    
    // Labels du volet de détail
    private Label detailReferenceLabel;
    private Label detailNomLabel;
    private Label detailClientLabel;
    private Label detailStatutLabel;
    private Label detailPrioriteLabel;
    private Label detailMontantLabel;
    private Label detailDatesLabel;
    private Label detailEquipeLabel;
    private Label detailDescriptionLabel;
    private Label detailNotesLabel;
    
    // Boutons du volet de détail
    private Button btnModifierDetail;
    private Button btnDevisDetail;
    private Button btnSupprimerDetail;
    
    // Formulaire
    private TextField referenceField;
    private TextField nomField;
    private TextArea descriptionArea;
    private ComboBox<String> statutCombo;
    private ComboBox<String> typeCombo;
    private ComboBox<String> prioriteCombo;
    private ComboBox<String> clientCombo;
    private TextField montantEstimeField;
    private DatePicker dateCreationPicker;
    private DatePicker dateEcheancePicker;
    private TextField commercialField;
    private TextField technicienField;
    private TextField chefProjetField;
    private TextArea notesArea;
    
    private Affaire affaireSelectionnee;
    
    public AffairesController() {
        this.affairesService = AffairesService.getInstance();
        this.affairesData = FXCollections.observableArrayList();
    }
    
    /**
     * Crée l'onglet de gestion des affaires avec volet de visualisation
     */
    public Tab creerOngletAffaires() {
        Tab ongletAffaires = new Tab("💼 Affaires");
        ongletAffaires.setClosable(false);
        
        // Utiliser le même pattern que les autres onglets
        VBox content = TabBuilderUtils.createTabContent();
        
        // Barre de recherche et filtres (sans l'entête encombrante)
        content.getChildren().add(creerBarreRecherche());
        
        // Créer le TableView des affaires directement et le stocker dans la variable de classe
        tableAffaires = creerTableViewAffaires();
        
        // Boutons d'action (après création du TableView pour les bindings)
        content.getChildren().add(creerBarreBoutons());
        
        // Volet de détail unifié
        DetailPane detailPane = DetailLayoutHelper.createAffaireVisualizationPane(() -> {
            if (affaireSelectionnee != null) {
                ouvrirFormulaireModification(affaireSelectionnee);
            }
        });
        
        // SplitPane comme les autres onglets pour occupation complète de l'espace
        SplitPane splitPane = TabBuilderUtils.createTableWithDetailPanel(tableAffaires, detailPane);
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configurer les événements de sélection avec le système unifié
        configurerEvenementsTableUnifie(tableAffaires, detailPane);
        
        ongletAffaires.setContent(content);
        
        // Charger les données initiales (maintenant que tableAffaires est assigné)
        rafraichirAffaires();
        
        return ongletAffaires;
    }
    
    /**
     * Crée la barre de recherche et filtres
     */
    private HBox creerBarreRecherche() {
        HBox barre = new HBox(10);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(5));
        
        // Champ de recherche
        rechercheField = new TextField();
        rechercheField.setPromptText("Rechercher par référence, nom ou client...");
        rechercheField.setPrefWidth(250);
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> filtrerAffaires());
        
        // Filtres
        filtreStatutCombo = new ComboBox<>();
        filtreStatutCombo.getItems().addAll("Tous", "PROSPECTION", "QUALIFIEE", "EN_COURS", "NEGOCIE", "GAGNEE", "PERDUE", "ANNULEE");
        filtreStatutCombo.setValue("Tous");
        filtreStatutCombo.setOnAction(e -> filtrerAffaires());
        
        filtrePrioriteCombo = new ComboBox<>();
        filtrePrioriteCombo.getItems().addAll("Toutes", "FAIBLE", "NORMALE", "HAUTE", "CRITIQUE");
        filtrePrioriteCombo.setValue("Toutes");
        filtrePrioriteCombo.setOnAction(e -> filtrerAffaires());
        
        filtreClientCombo = new ComboBox<>();
        filtreClientCombo.getItems().add("Tous les clients");
        filtreClientCombo.setValue("Tous les clients");
        chargerClients();
        filtreClientCombo.setOnAction(e -> filtrerAffaires());
        
        barre.getChildren().addAll(
            new Label("Recherche:"), rechercheField,
            new Label("Statut:"), filtreStatutCombo,
            new Label("Priorité:"), filtrePrioriteCombo,
            new Label("Client:"), filtreClientCombo
        );
        
        return barre;
    }
    
    /**
     * Crée le TableView des affaires (retourne directement le TableView)
     */
    private TableView<Affaire> creerTableViewAffaires() {
        TableView<Affaire> table = new TableView<>();
        table.setItems(affairesData);
        TabBuilderUtils.configureBasicTable(table);
        
        // Colonnes
        TableColumn<Affaire, String> refCol = new TableColumn<>("Référence");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(120);
        
        TableColumn<Affaire, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(200);
        
        TableColumn<Affaire, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        clientCol.setPrefWidth(150);
        
        TableColumn<Affaire, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData -> {
            StatutAffaire statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(statut != null ? statut.getLibelle() : "");
        });
        statutCol.setPrefWidth(120);
        statutCol.setCellFactory(col -> new TableCell<Affaire, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Affaire affaire = getTableView().getItems().get(getIndex());
                    if (affaire.getStatut() != null) {
                        setStyle("-fx-background-color: " + affaire.getStatut().getCouleur() + "40;");
                    }
                }
            }
        });
        
        TableColumn<Affaire, String> prioriteCol = new TableColumn<>("Priorité");
        prioriteCol.setCellValueFactory(cellData -> {
            PrioriteAffaire priorite = cellData.getValue().getPriorite();
            return new SimpleStringProperty(priorite != null ? priorite.getLibelle() : "");
        });
        prioriteCol.setPrefWidth(100);
        
        TableColumn<Affaire, String> montantCol = new TableColumn<>("Montant estimé");
        montantCol.setCellValueFactory(cellData -> {
            Double montant = cellData.getValue().getMontantEstime();
            return new SimpleStringProperty(montant != null ? String.format("%.2f €", montant) : "");
        });
        montantCol.setPrefWidth(120);
        
        TableColumn<Affaire, String> dateCol = new TableColumn<>("Date création");
        dateCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateCreation();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });
        dateCol.setPrefWidth(100);
        
        TableColumn<Affaire, String> echeanceCol = new TableColumn<>("Échéance");
        echeanceCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateEcheance();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });
        echeanceCol.setPrefWidth(100);
        
        table.getColumns().clear();
        table.getColumns().add(refCol);
        table.getColumns().add(nomCol);
        table.getColumns().add(clientCol);
        table.getColumns().add(statutCol);
        table.getColumns().add(prioriteCol);
        table.getColumns().add(montantCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(echeanceCol);
        
        return table;
    }
    
    /**
     * Configure les événements du tableau et la synchronisation avec le volet de détail
     */
    private void configurerEvenementsTable(TableView<Affaire> tableView, VBox voletDetail) {
        // Gestion de la sélection pour afficher les détails
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            affaireSelectionnee = newSelection;
            if (newSelection != null) {
                mettreAJourVoletDetailComplet(newSelection, voletDetail);
                voletDetail.setVisible(true);
                // Activer les boutons du volet de détail
                if (btnModifierDetail != null) btnModifierDetail.setDisable(false);
                if (btnDevisDetail != null) btnDevisDetail.setDisable(false);
                if (btnSupprimerDetail != null) btnSupprimerDetail.setDisable(false);
            } else {
                voletDetail.setVisible(false);
                // Désactiver les boutons du volet de détail
                if (btnModifierDetail != null) btnModifierDetail.setDisable(true);
                if (btnDevisDetail != null) btnDevisDetail.setDisable(true);
                if (btnSupprimerDetail != null) btnSupprimerDetail.setDisable(true);
            }
        });
        
        // Double-clic pour éditer
        tableView.setRowFactory(tv -> {
            TableRow<Affaire> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierAffaire();
                }
            });
            return row;
        });
    }
    
    /**
     * Crée la barre de boutons
     */
    private HBox creerBarreBoutons() {
        HBox barre = new HBox(10);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(10));
        
        Button nouvelleBtn = new Button("➕ Nouvelle Affaire");
        nouvelleBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        nouvelleBtn.setOnAction(e -> nouvelleAffaire());
        
        Button modifierBtn = new Button("✏️ Modifier");
        modifierBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        modifierBtn.setOnAction(e -> modifierAffaire());
        modifierBtn.disableProperty().bind(tableAffaires.getSelectionModel().selectedItemProperty().isNull());
        
        Button supprimerBtn = new Button("🗑️ Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        supprimerBtn.setOnAction(e -> supprimerAffaire());
        supprimerBtn.disableProperty().bind(tableAffaires.getSelectionModel().selectedItemProperty().isNull());
        
        Button devisBtn = new Button("📄 Devis");
        devisBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        devisBtn.setOnAction(e -> gererDevis());
        devisBtn.disableProperty().bind(tableAffaires.getSelectionModel().selectedItemProperty().isNull());
        
        Button actualiserBtn = new Button("🔄 Actualiser");
        actualiserBtn.setOnAction(e -> rafraichirAffaires());
        
        barre.getChildren().addAll(nouvelleBtn, modifierBtn, supprimerBtn, devisBtn, actualiserBtn);
        
        return barre;
    }
    
    /**
     * Charge les clients dans le combo
     */
    private void chargerClients() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nom_societe FROM societes ORDER BY nom_societe");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String client = rs.getLong("id") + " - " + rs.getString("nom_societe");
                filtreClientCombo.getItems().add(client);
            }
            
        } catch (Exception e) {
            DialogUtils.showError("Erreur", "Impossible de charger les clients", e.getMessage());
        }
    }
    
    /**
     * Rafraîchit la liste des affaires
     */
    public void rafraichirAffaires() {
        List<Affaire> affaires = affairesService.obtenirToutesLesAffaires();
        affairesData.clear();
        affairesData.addAll(affaires);
    }
    
    /**
     * Filtre les affaires selon les critères
     */
    private void filtrerAffaires() {
        String recherche = rechercheField.getText().toLowerCase();
        String statut = filtreStatutCombo.getValue();
        String priorite = filtrePrioriteCombo.getValue();
        
        List<Affaire> toutesAffaires = affairesService.obtenirToutesLesAffaires();
        
        List<Affaire> affairesFiltrees = toutesAffaires.stream()
            .filter(affaire -> {
                // Filtre de recherche
                if (!recherche.isEmpty()) {
                    String texteRecherche = (affaire.getReference() + " " + 
                                           affaire.getNom() + " " + 
                                           affaire.getClientNom()).toLowerCase();
                    if (!texteRecherche.contains(recherche)) {
                        return false;
                    }
                }
                
                // Filtre statut
                if (!"Tous".equals(statut) && !statut.equals(affaire.getStatut().name())) {
                    return false;
                }
                
                // Filtre priorité
                if (!"Toutes".equals(priorite) && !priorite.equals(affaire.getPriorite().name())) {
                    return false;
                }
                
                return true;
            })
            .toList();
        
        affairesData.clear();
        affairesData.addAll(affairesFiltrees);
    }
    
    /**
     * Crée une nouvelle affaire
     */
    private void nouvelleAffaire() {
        AffaireFormController formController = new AffaireFormController(null);
        Optional<Affaire> result = formController.afficherFormulaire();
        
        if (result.isPresent()) {
            Affaire nouvelleAffaire = result.get();
            Long id = affairesService.creerAffaire(nouvelleAffaire);
            
            if (id != null) {
                DialogUtils.showInformation("Succès", "Affaire créée avec succès");
                rafraichirAffaires();
            } else {
                DialogUtils.showError("Erreur", "Impossible de créer l'affaire", "Une erreur est survenue");
            }
        }
    }
    
    /**
     * Ouvre le formulaire de modification pour l'affaire donnée
     */
    private void ouvrirFormulaireModification(Affaire affaire) {
        if (affaire == null) return;
        
        AffaireFormController formController = new AffaireFormController(affaire);
        Optional<Affaire> result = formController.afficherFormulaire();
        
        if (result.isPresent()) {
            Affaire affaireModifiee = result.get();
            boolean success = affairesService.mettreAJourAffaire(affaireModifiee);
            
            if (success) {
                DialogUtils.showInformation("Succès", "Affaire modifiée avec succès");
                rafraichirAffaires();
            } else {
                DialogUtils.showError("Erreur", "Impossible de modifier l'affaire", "Une erreur est survenue");
            }
        }
    }

    /**
     * Configure les événements de sélection de table avec le système unifié
     */
    private void configurerEvenementsTableUnifie(TableView<Affaire> table, DetailPane detailPane) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            affaireSelectionnee = newSelection;
            if (newSelection != null && detailPane != null) {
                // Utilisation du système unifié pour créer les informations d'entité
                EntityInfo affaireInfo = DetailLayoutHelper.createEntityInfoFromAffaire(newSelection);
                detailPane.updateInfo(affaireInfo);
            } else if (detailPane != null) {
                // Affichage par défaut quand aucune sélection
                EntityInfo defaultInfo = new EntityInfo("Aucune affaire sélectionnée")
                        .status("-")
                        .description("Sélectionnez une affaire pour voir ses détails");
                
                detailPane.updateInfo(defaultInfo);
            }
        });
    }

    /**
     * Modifie l'affaire sélectionnée
     */
    private void modifierAffaire() {
        if (affaireSelectionnee == null) return;
        
        AffaireFormController formController = new AffaireFormController(affaireSelectionnee);
        Optional<Affaire> result = formController.afficherFormulaire();
        
        if (result.isPresent()) {
            Affaire affaireModifiee = result.get();
            
            if (affairesService.mettreAJourAffaire(affaireModifiee)) {
                DialogUtils.showInformation("Succès", "Affaire mise à jour avec succès");
                rafraichirAffaires();
            } else {
                DialogUtils.showError("Erreur", "Impossible de mettre à jour l'affaire", "Une erreur est survenue");
            }
        }
    }
    
    /**
     * Supprime l'affaire sélectionnée
     */
    private void supprimerAffaire() {
        if (affaireSelectionnee == null) return;
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'affaire");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'affaire \"" + 
                                   affaireSelectionnee.getNom() + "\" ?\n\nTous les devis associés seront également supprimés.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (affairesService.supprimerAffaire(affaireSelectionnee.getId())) {
                DialogUtils.showInformation("Succès", "Affaire supprimée avec succès");
                rafraichirAffaires();
            } else {
                DialogUtils.showError("Erreur", "Impossible de supprimer l'affaire", "Une erreur est survenue");
            }
        }
    }

    /**
     * Crée le volet de détail pour l'affichage des informations d'une affaire
     */
    private VBox creerVoletDetail() {
        VBox volet = new VBox(10);
        volet.setPadding(new Insets(10));
        volet.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");
        
        // Titre du volet
        Label titreVolet = new Label("📋 Détails de l'affaire");
        titreVolet.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        // Conteneur pour les détails
        VBox conteneurDetails = new VBox(8);
        conteneurDetails.setPadding(new Insets(10));
        conteneurDetails.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Labels pour afficher les informations (utiliser les variables de classe)
        detailReferenceLabel = new Label("Aucune affaire sélectionnée");
        detailReferenceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        detailNomLabel = new Label("");
        detailNomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        detailNomLabel.setWrapText(true);
        
        detailClientLabel = new Label("");
        detailClientLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");
        
        detailStatutLabel = new Label("");
        detailStatutLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12; -fx-text-fill: white;");
        
        detailPrioriteLabel = new Label("");
        detailPrioriteLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12;");
        
        detailMontantLabel = new Label("");
        detailMontantLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        
        detailDatesLabel = new Label("");
        detailDatesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        
        detailEquipeLabel = new Label("");
        detailEquipeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
        
        detailDescriptionLabel = new Label("");
        detailDescriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
        detailDescriptionLabel.setWrapText(true);
        detailDescriptionLabel.setMaxWidth(350);
        
        detailNotesLabel = new Label("");
        detailNotesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
        detailNotesLabel.setWrapText(true);
        detailNotesLabel.setMaxWidth(350);
        
        conteneurDetails.getChildren().addAll(
            detailReferenceLabel, detailNomLabel, detailClientLabel, detailStatutLabel, detailPrioriteLabel,
            detailMontantLabel, detailDatesLabel, detailEquipeLabel, detailDescriptionLabel, detailNotesLabel
        );
        
        // Actions rapides
        VBox actionsRapides = new VBox(5);
        actionsRapides.setPadding(new Insets(10));
        actionsRapides.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label titreActions = new Label("⚡ Actions rapides");
        titreActions.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        btnModifierDetail = new Button("✏️ Modifier");
        btnModifierDetail.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-cursor: hand;");
        btnModifierDetail.setPrefWidth(150);
        btnModifierDetail.setOnAction(e -> modifierAffaire());
        btnModifierDetail.setDisable(true);
        
        btnDevisDetail = new Button("📄 Gérer devis");
        btnDevisDetail.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");
        btnDevisDetail.setPrefWidth(150);
        btnDevisDetail.setOnAction(e -> gererDevis());
        btnDevisDetail.setDisable(true);
        
        btnSupprimerDetail = new Button("🗑️ Supprimer");
        btnSupprimerDetail.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
        btnSupprimerDetail.setPrefWidth(150);
        btnSupprimerDetail.setOnAction(e -> supprimerAffaire());
        btnSupprimerDetail.setDisable(true);
        
        actionsRapides.getChildren().addAll(titreActions, btnModifierDetail, btnDevisDetail, btnSupprimerDetail);
        
        volet.getChildren().addAll(titreVolet, conteneurDetails, actionsRapides);
        
        // Les événements de sélection seront configurés dans configurerEvenementsTable()
        
        return volet;
    }

    /**
     * Met à jour le volet de détail avec les informations de l'affaire sélectionnée
     */
    private void mettreAJourVoletDetail(Affaire affaire, Label referenceLabel, Label nomLabel, Label clientLabel, 
                                       Label statutLabel, Label prioriteLabel, Label montantLabel, Label datesLabel,
                                       Label equipeLabel, Label descriptionLabel, Label notesLabel) {
        if (affaire == null) return;
        
        referenceLabel.setText("Référence: " + (affaire.getReference() != null ? affaire.getReference() : "N/A"));
        nomLabel.setText(affaire.getNom() != null ? affaire.getNom() : "Nom non défini");
        clientLabel.setText("Client: " + (affaire.getClientNom() != null ? affaire.getClientNom() : "N/A"));
        
        if (affaire.getStatut() != null) {
            statutLabel.setText(affaire.getStatut().getLibelle());
            statutLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12; -fx-text-fill: white; -fx-background-color: " + affaire.getStatut().getCouleur() + ";");
        } else {
            statutLabel.setText("");
        }
        
        if (affaire.getPriorite() != null) {
            prioriteLabel.setText("Priorité: " + affaire.getPriorite().getLibelle());
            prioriteLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 12; -fx-background-color: " + affaire.getPriorite().getCouleur() + "40;");
        } else {
            prioriteLabel.setText("");
        }
        
        if (affaire.getMontantEstime() != null) {
            montantLabel.setText(String.format("Montant estimé: %.2f €", affaire.getMontantEstime()));
        } else {
            montantLabel.setText("Montant: Non défini");
        }
        
        String dates = "";
        if (affaire.getDateCreation() != null) {
            dates += "Créé le: " + affaire.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (affaire.getDateEcheance() != null) {
            dates += (dates.isEmpty() ? "" : "\n") + "Échéance: " + affaire.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        datesLabel.setText(dates);
        
        String equipe = "";
        if (affaire.getCommercialResponsable() != null) {
            equipe += "Commercial: " + affaire.getCommercialResponsable();
        }
        if (affaire.getTechnicienResponsable() != null) {
            equipe += (equipe.isEmpty() ? "" : "\n") + "Technicien: " + affaire.getTechnicienResponsable();
        }
        if (affaire.getChefProjet() != null) {
            equipe += (equipe.isEmpty() ? "" : "\n") + "Chef de projet: " + affaire.getChefProjet();
        }
        equipeLabel.setText(equipe);
        
        descriptionLabel.setText(affaire.getDescription() != null ? "Description: " + affaire.getDescription() : "");
        notesLabel.setText(affaire.getNotes() != null ? "Notes: " + affaire.getNotes() : "");
    }
    
    /**
     * Met à jour le volet de détail complet en utilisant les variables de classe
     */
    private void mettreAJourVoletDetailComplet(Affaire affaire, VBox voletDetail) {
        if (affaire == null || detailReferenceLabel == null) return;
        
        mettreAJourVoletDetail(affaire, detailReferenceLabel, detailNomLabel, detailClientLabel, detailStatutLabel,
                              detailPrioriteLabel, detailMontantLabel, detailDatesLabel, detailEquipeLabel, 
                              detailDescriptionLabel, detailNotesLabel);
    }
    
    /**
     * Gère les devis de l'affaire sélectionnée
     */
    private void gererDevis() {
        if (affaireSelectionnee == null) return;
        
        // TODO: Ouvrir la fenêtre de gestion des devis
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Information");
        info.setHeaderText("Gestion des devis");
        info.setContentText("La gestion des devis pour l'affaire \"" + affaireSelectionnee.getNom() + 
                           "\" sera implementée dans la prochaine étape.");
        info.showAndWait();
    }
}