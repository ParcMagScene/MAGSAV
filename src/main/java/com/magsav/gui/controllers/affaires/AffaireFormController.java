package com.magsav.gui.controllers.affaires;

import com.magsav.model.affaires.Affaire;
import com.magsav.model.affaires.StatutAffaire;
import com.magsav.model.affaires.TypeAffaire;
import com.magsav.model.affaires.PrioriteAffaire;
import com.magsav.service.affaires.AffairesService;
import com.magsav.db.DB;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Contrôleur de formulaire pour la création/modification d'affaires
 */
public class AffaireFormController {
    
    private final Affaire affaireOriginale;
    private final AffairesService affairesService;
    
    // Composants du formulaire
    private TextField referenceField;
    private TextField nomField;
    private TextArea descriptionArea;
    private ComboBox<StatutAffaire> statutCombo;
    private ComboBox<TypeAffaire> typeCombo;
    private ComboBox<PrioriteAffaire> prioriteCombo;
    private ComboBox<ClientInfo> clientCombo;
    private TextField montantEstimeField;
    private DatePicker dateCreationPicker;
    private DatePicker dateEcheancePicker;
    private TextField commercialField;
    private TextField technicienField;
    private TextField chefProjetField;
    private TextArea notesArea;
    private TextArea commentairesArea;
    
    private Stage stage;
    private boolean valide = false;
    
    public AffaireFormController(Affaire affaire) {
        this.affaireOriginale = affaire;
        this.affairesService = AffairesService.getInstance();
    }
    
    /**
     * Affiche le formulaire et retourne l'affaire créée/modifiée
     */
    public Optional<Affaire> afficherFormulaire() {
        stage = new Stage();
        stage.setTitle(affaireOriginale == null ? "Nouvelle Affaire" : "Modifier Affaire");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Titre
        Label titre = new Label(affaireOriginale == null ? "Créer une nouvelle affaire" : "Modifier l'affaire");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Formulaire dans un ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(creerFormulaire());
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        // Boutons
        HBox boutons = creerBoutons();
        
        root.getChildren().addAll(titre, scrollPane, boutons);
        
        Scene scene = new Scene(root, 600, 700);
        stage.setScene(scene);
        
        // Remplir les champs si modification
        if (affaireOriginale != null) {
            remplirChamps();
        } else {
            // Valeurs par défaut pour nouvelle affaire
            referenceField.setText(affairesService.genererReferenceAffaire());
            dateCreationPicker.setValue(LocalDate.now());
            statutCombo.setValue(StatutAffaire.PROSPECTION);
            prioriteCombo.setValue(PrioriteAffaire.NORMALE);
        }
        
        stage.showAndWait();
        
        return valide ? Optional.of(construireAffaire()) : Optional.empty();
    }
    
    /**
     * Crée le formulaire principal
     */
    private VBox creerFormulaire() {
        VBox formulaire = new VBox(10);
        formulaire.setPadding(new Insets(10));
        
        // Section informations générales
        formulaire.getChildren().add(creerSectionInfosGenerales());
        
        // Séparateur
        formulaire.getChildren().add(new Separator());
        
        // Section commercial
        formulaire.getChildren().add(creerSectionCommercial());
        
        // Séparateur
        formulaire.getChildren().add(new Separator());
        
        // Section équipe
        formulaire.getChildren().add(creerSectionEquipe());
        
        // Séparateur
        formulaire.getChildren().add(new Separator());
        
        // Section notes
        formulaire.getChildren().add(creerSectionNotes());
        
        return formulaire;
    }
    
    /**
     * Section informations générales
     */
    private VBox creerSectionInfosGenerales() {
        VBox section = new VBox(10);
        
        Label sectionTitre = new Label("Informations générales");
        sectionTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        section.getChildren().add(sectionTitre);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Référence
        grid.add(new Label("Référence *:"), 0, 0);
        referenceField = new TextField();
        referenceField.setDisable(affaireOriginale != null); // Non modifiable en modification
        grid.add(referenceField, 1, 0);
        
        // Nom
        grid.add(new Label("Nom *:"), 0, 1);
        nomField = new TextField();
        grid.add(nomField, 1, 1);
        
        // Client
        grid.add(new Label("Client:"), 0, 2);
        clientCombo = new ComboBox<>();
        clientCombo.setPrefWidth(200);
        chargerClients();
        grid.add(clientCombo, 1, 2);
        
        // Statut
        grid.add(new Label("Statut *:"), 0, 3);
        statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll(StatutAffaire.values());
        statutCombo.setCellFactory(param -> new ListCell<StatutAffaire>() {
            @Override
            protected void updateItem(StatutAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        statutCombo.setButtonCell(new ListCell<StatutAffaire>() {
            @Override
            protected void updateItem(StatutAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        grid.add(statutCombo, 1, 3);
        
        // Type
        grid.add(new Label("Type:"), 0, 4);
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TypeAffaire.values());
        typeCombo.setCellFactory(param -> new ListCell<TypeAffaire>() {
            @Override
            protected void updateItem(TypeAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        typeCombo.setButtonCell(new ListCell<TypeAffaire>() {
            @Override
            protected void updateItem(TypeAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        grid.add(typeCombo, 1, 4);
        
        // Priorité
        grid.add(new Label("Priorité *:"), 0, 5);
        prioriteCombo = new ComboBox<>();
        prioriteCombo.getItems().addAll(PrioriteAffaire.values());
        prioriteCombo.setCellFactory(param -> new ListCell<PrioriteAffaire>() {
            @Override
            protected void updateItem(PrioriteAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        prioriteCombo.setButtonCell(new ListCell<PrioriteAffaire>() {
            @Override
            protected void updateItem(PrioriteAffaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        grid.add(prioriteCombo, 1, 5);
        
        // Description
        grid.add(new Label("Description:"), 0, 6);
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 6);
        
        section.getChildren().add(grid);
        return section;
    }
    
    /**
     * Section commercial
     */
    private VBox creerSectionCommercial() {
        VBox section = new VBox(10);
        
        Label sectionTitre = new Label("Informations commerciales");
        sectionTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        section.getChildren().add(sectionTitre);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Montant estimé
        grid.add(new Label("Montant estimé (€):"), 0, 0);
        montantEstimeField = new TextField();
        grid.add(montantEstimeField, 1, 0);
        
        // Date création
        grid.add(new Label("Date création *:"), 0, 1);
        dateCreationPicker = new DatePicker();
        grid.add(dateCreationPicker, 1, 1);
        
        // Date échéance
        grid.add(new Label("Date échéance:"), 0, 2);
        dateEcheancePicker = new DatePicker();
        grid.add(dateEcheancePicker, 1, 2);
        
        section.getChildren().add(grid);
        return section;
    }
    
    /**
     * Section équipe
     */
    private VBox creerSectionEquipe() {
        VBox section = new VBox(10);
        
        Label sectionTitre = new Label("Équipe projet");
        sectionTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        section.getChildren().add(sectionTitre);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Commercial responsable
        grid.add(new Label("Commercial responsable:"), 0, 0);
        commercialField = new TextField();
        grid.add(commercialField, 1, 0);
        
        // Technicien responsable
        grid.add(new Label("Technicien responsable:"), 0, 1);
        technicienField = new TextField();
        grid.add(technicienField, 1, 1);
        
        // Chef de projet
        grid.add(new Label("Chef de projet:"), 0, 2);
        chefProjetField = new TextField();
        grid.add(chefProjetField, 1, 2);
        
        section.getChildren().add(grid);
        return section;
    }
    
    /**
     * Section notes
     */
    private VBox creerSectionNotes() {
        VBox section = new VBox(10);
        
        Label sectionTitre = new Label("Notes et commentaires");
        sectionTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        section.getChildren().add(sectionTitre);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Notes
        grid.add(new Label("Notes:"), 0, 0);
        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        grid.add(notesArea, 1, 0);
        
        // Commentaires internes
        grid.add(new Label("Commentaires internes:"), 0, 1);
        commentairesArea = new TextArea();
        commentairesArea.setPrefRowCount(3);
        grid.add(commentairesArea, 1, 1);
        
        section.getChildren().add(grid);
        return section;
    }
    
    /**
     * Crée les boutons
     */
    private HBox creerBoutons() {
        HBox boutons = new HBox(10);
        boutons.setStyle("-fx-alignment: center;");
        
        Button validerBtn = new Button("Valider");
        validerBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20;");
        validerBtn.setOnAction(e -> valider());
        
        Button annulerBtn = new Button("Annuler");
        annulerBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20;");
        annulerBtn.setOnAction(e -> stage.close());
        
        boutons.getChildren().addAll(validerBtn, annulerBtn);
        
        return boutons;
    }
    
    /**
     * Charge les clients dans le combo
     */
    private void chargerClients() {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nom_societe FROM societes ORDER BY nom_societe");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ClientInfo client = new ClientInfo(rs.getLong("id"), rs.getString("nom_societe"));
                clientCombo.getItems().add(client);
            }
            
            // Configuration de l'affichage
            clientCombo.setCellFactory(param -> new ListCell<ClientInfo>() {
                @Override
                protected void updateItem(ClientInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
            clientCombo.setButtonCell(new ListCell<ClientInfo>() {
                @Override
                protected void updateItem(ClientInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des clients: " + e.getMessage());
        }
    }
    
    /**
     * Remplit les champs avec les données de l'affaire
     */
    private void remplirChamps() {
        if (affaireOriginale == null) return;
        
        referenceField.setText(affaireOriginale.getReference());
        nomField.setText(affaireOriginale.getNom());
        descriptionArea.setText(affaireOriginale.getDescription());
        statutCombo.setValue(affaireOriginale.getStatut());
        typeCombo.setValue(affaireOriginale.getType());
        prioriteCombo.setValue(affaireOriginale.getPriorite());
        
        // Client
        if (affaireOriginale.getClientId() != null) {
            clientCombo.getItems().stream()
                .filter(client -> client.getId().equals(affaireOriginale.getClientId()))
                .findFirst()
                .ifPresent(client -> clientCombo.setValue(client));
        }
        
        if (affaireOriginale.getMontantEstime() != null) {
            montantEstimeField.setText(affaireOriginale.getMontantEstime().toString());
        }
        
        dateCreationPicker.setValue(affaireOriginale.getDateCreation());
        dateEcheancePicker.setValue(affaireOriginale.getDateEcheance());
        
        commercialField.setText(affaireOriginale.getCommercialResponsable());
        technicienField.setText(affaireOriginale.getTechnicienResponsable());
        chefProjetField.setText(affaireOriginale.getChefProjet());
        notesArea.setText(affaireOriginale.getNotes());
        commentairesArea.setText(affaireOriginale.getCommentairesInternes());
    }
    
    /**
     * Valide le formulaire
     */
    private void valider() {
        // Vérifications obligatoires
        if (referenceField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La référence est obligatoire");
            return;
        }
        
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire");
            return;
        }
        
        if (statutCombo.getValue() == null) {
            showAlert("Erreur", "Le statut est obligatoire");
            return;
        }
        
        if (prioriteCombo.getValue() == null) {
            showAlert("Erreur", "La priorité est obligatoire");
            return;
        }
        
        if (dateCreationPicker.getValue() == null) {
            showAlert("Erreur", "La date de création est obligatoire");
            return;
        }
        
        // Validation du montant
        if (!montantEstimeField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(montantEstimeField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le montant estimé doit être un nombre valide");
                return;
            }
        }
        
        valide = true;
        stage.close();
    }
    
    /**
     * Construit l'objet Affaire à partir du formulaire
     */
    private Affaire construireAffaire() {
        Affaire affaire = affaireOriginale != null ? affaireOriginale : new Affaire();
        
        affaire.setReference(referenceField.getText().trim());
        affaire.setNom(nomField.getText().trim());
        affaire.setDescription(descriptionArea.getText().trim());
        affaire.setStatut(statutCombo.getValue());
        affaire.setType(typeCombo.getValue());
        affaire.setPriorite(prioriteCombo.getValue());
        
        // Client
        ClientInfo clientSelectionne = clientCombo.getValue();
        if (clientSelectionne != null) {
            affaire.setClientId(clientSelectionne.getId());
            affaire.setClientNom(clientSelectionne.getNom());
        }
        
        // Montant
        String montantStr = montantEstimeField.getText().trim();
        if (!montantStr.isEmpty()) {
            try {
                affaire.setMontantEstime(Double.parseDouble(montantStr));
            } catch (NumberFormatException e) {
                // Déjà validé
            }
        }
        
        affaire.setDateCreation(dateCreationPicker.getValue());
        affaire.setDateEcheance(dateEcheancePicker.getValue());
        
        affaire.setCommercialResponsable(commercialField.getText().trim());
        affaire.setTechnicienResponsable(technicienField.getText().trim());
        affaire.setChefProjet(chefProjetField.getText().trim());
        affaire.setNotes(notesArea.getText().trim());
        affaire.setCommentairesInternes(commentairesArea.getText().trim());
        
        return affaire;
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Classe pour les informations client
     */
    private static class ClientInfo {
        private final Long id;
        private final String nom;
        
        public ClientInfo(Long id, String nom) {
            this.id = id;
            this.nom = nom;
        }
        
        public Long getId() { return id; }
        public String getNom() { return nom; }
        
        @Override
        public String toString() {
            return nom;
        }
    }
}