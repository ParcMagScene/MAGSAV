package com.magsav.gui.controllers;

import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory;
import com.magsav.gui.components.DetailPaneFactory.DetailPane;
import com.magsav.gui.components.DetailPaneFactory.EntityInfo;
import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.model.InterventionRow;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.Refreshable;
import com.magsav.util.AppLogger;

import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Orientation;
import javafx.collections.FXCollections;

import java.util.List;

/**
 * Contrôleur dédié à la section Interventions
 * Gère les onglets Liste, Nouvelle, En cours, Terminées
 */
public class InterventionsController implements Refreshable {
    
    // Repositories
    private final InterventionRepository interventionRepository = new InterventionRepository();
    
    // Tables pour chaque onglet
    private TableView<InterventionRow> listeTable;
    private TableView<InterventionRow> enCoursTable;
    private TableView<InterventionRow> termineesTable;
    
    // Volet de détail unifié
    private DetailPane listeDetailPane;
    private DetailPane enCoursDetailPane;
    private DetailPane termineesDetailPane;
    
    /**
     * Crée l'onglet Liste des interventions
     */
    public Tab createInterventionsListTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        listeTable = createInterventionTable();
        
        // Créer le volet de détail avec le système unifié
        listeDetailPane = DetailLayoutHelper.createInterventionVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(listeTable);
        VBox.setVgrow(listeTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, listeDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(listeTable, listeDetailPane);
        
        // Charger les données
        loadListeData();
        
        Tab tab = TabBuilderUtils.createBasicTab("Liste");
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Nouvelle intervention
     */
    public Tab createNewInterventionTab() {
        VBox content = new VBox(10);
        Label label = new Label("Nouvelle intervention - Interface à implémenter");
        content.getChildren().add(label);
        
        Tab tab = TabBuilderUtils.createBasicTab("Nouvelle");
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Interventions en cours
     */
    public Tab createInterventionsEnCoursTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        enCoursTable = createInterventionTable();
        
        // Créer le volet de détail avec le système unifié
        enCoursDetailPane = DetailLayoutHelper.createInterventionVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(enCoursTable);
        VBox.setVgrow(enCoursTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, enCoursDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(enCoursTable, enCoursDetailPane);
        
        // Charger les données
        loadEnCoursData();
        
        Tab tab = TabBuilderUtils.createBasicTab("En cours");
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Interventions terminées
     */
    public Tab createInterventionsTermineesTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        termineesTable = createInterventionTable();
        
        // Créer le volet de détail avec le système unifié
        termineesDetailPane = DetailLayoutHelper.createInterventionVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(termineesTable);
        VBox.setVgrow(termineesTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, termineesDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(termineesTable, termineesDetailPane);
        
        // Charger les données
        loadTermineesData();
        
        Tab tab = TabBuilderUtils.createBasicTab("Terminées");
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée une table standard pour les interventions
     */
    private TableView<InterventionRow> createInterventionTable() {
        TableView<InterventionRow> table = new TableView<>();
        
        // Configuration des colonnes selon InterventionRow(long id, String produitNom, String statut, String panne, String dateEntree, String dateSortie)
        TableColumn<InterventionRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id())));
        idCol.setPrefWidth(60);
        
        TableColumn<InterventionRow, String> produitCol = new TableColumn<>("Produit");
        produitCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().produitNom() != null ? data.getValue().produitNom() : ""));
        produitCol.setPrefWidth(200);
        
        TableColumn<InterventionRow, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().statut() != null ? data.getValue().statut() : ""));
        statusCol.setPrefWidth(100);
        
        TableColumn<InterventionRow, String> panneCol = new TableColumn<>("Panne");
        panneCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().panne() != null ? data.getValue().panne() : ""));
        panneCol.setPrefWidth(120);
        
        TableColumn<InterventionRow, String> dateEntreeCol = new TableColumn<>("Date entrée");
        dateEntreeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().dateEntree() != null ? data.getValue().dateEntree() : ""));
        dateEntreeCol.setPrefWidth(100);
        
        TableColumn<InterventionRow, String> dateSortieCol = new TableColumn<>("Date sortie");
        dateSortieCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().dateSortie() != null ? data.getValue().dateSortie() : ""));
        dateSortieCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, produitCol, statusCol, panneCol, dateEntreeCol, dateSortieCol);
        
        return table;
    }
    
    /**
     * Configure la sélection de table pour mettre à jour le volet de détail
     */
    private void setupTableSelection(TableView<InterventionRow> table, DetailPane detailPane) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && detailPane != null) {
                // Utilisation du système unifié pour créer les informations d'entité
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromIntervention(newSelection);
                detailPane.updateInfo(entityInfo);
            } else if (detailPane != null) {
                // Affichage par défaut quand aucune sélection
                EntityInfo defaultInfo = new EntityInfo("Aucune intervention sélectionnée")
                        .status("-")
                        .description("Sélectionnez une intervention pour voir ses détails");
                
                detailPane.updateInfo(defaultInfo);
            }
        });
    }
    
    /**
     * Charge les données pour l'onglet Liste
     */
    private void loadListeData() {
        if (listeTable != null) {
            List<InterventionRow> interventions = interventionRepository.findAllWithProductName();
            listeTable.setItems(FXCollections.observableArrayList(interventions));
            
            // Initialiser le volet de détail avec info par défaut
            if (listeDetailPane != null) {
                EntityInfo defaultInfo = new EntityInfo("Aucune intervention sélectionnée")
                        .status("-")
                        .description("Sélectionnez une intervention pour voir ses détails");
                
                listeDetailPane.updateInfo(defaultInfo);
            }
        }
    }
    
    /**
     * Charge les données pour l'onglet En cours
     */
    private void loadEnCoursData() {
        if (enCoursTable != null) {
            List<InterventionRow> interventions = interventionRepository.findAllWithProductName()
                    .stream()
                    .filter(i -> "En cours".equals(i.statut()) || "EN_COURS".equals(i.statut()))
                    .collect(java.util.stream.Collectors.toList());
            enCoursTable.setItems(FXCollections.observableArrayList(interventions));
            
            // Initialiser le volet de détail avec info par défaut
            if (enCoursDetailPane != null) {
                EntityInfo defaultInfo = new EntityInfo("Aucune intervention sélectionnée")
                        .status("-")
                        .description("Sélectionnez une intervention pour voir ses détails");
                
                enCoursDetailPane.updateInfo(defaultInfo);
            }
        }
    }
    
    /**
     * Charge les données pour l'onglet Terminées
     */
    private void loadTermineesData() {
        if (termineesTable != null) {
            List<InterventionRow> interventions = interventionRepository.findAllWithProductName()
                    .stream()
                    .filter(i -> "Terminée".equals(i.statut()) || "TERMINEE".equals(i.statut()))
                    .collect(java.util.stream.Collectors.toList());
            termineesTable.setItems(FXCollections.observableArrayList(interventions));
            
            // Initialiser le volet de détail avec info par défaut
            if (termineesDetailPane != null) {
                EntityInfo defaultInfo = new EntityInfo("Aucune intervention sélectionnée")
                        .status("-")
                        .description("Sélectionnez une intervention pour voir ses détails");
                
                termineesDetailPane.updateInfo(defaultInfo);
            }
        }
    }
    
    /**
     * Rafraîchit toutes les tables du contrôleur
     */
    @Override
    public void refreshAllTables() {
        if (listeTable != null) {
            loadListeData();
        }
        if (enCoursTable != null) {
            loadEnCoursData();
        }
        if (termineesTable != null) {
            loadTermineesData();
        }
        
        // Informer les autres contrôleurs du rafraîchissement
        RefreshManager.getInstance().notifyOthers(this);
    }
    
    @Override
    public String getComponentName() {
        return "InterventionsController";
    }
}