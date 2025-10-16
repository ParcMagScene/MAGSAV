package com.magsav.gui.controllers;

import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory.DetailPane;
import com.magsav.gui.components.DetailPaneFactory.EntityInfo;
import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.dto.UserRow;
import com.magsav.service.data.DataServiceManager;
import com.magsav.service.Refreshable;
import com.magsav.util.AppLogger;
import com.magsav.service.NavigationService;

import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Orientation;
import javafx.collections.FXCollections;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur des utilisateurs avec volets de visualisation
 */
public class UsersController implements Refreshable {
    
    private final DataServiceManager dataManager = DataServiceManager.getInstance();
    
    // Tables pour chaque onglet
    private TableView<UserRow> technicienTable;
    private TableView<UserRow> adminTable;
    private TableView<UserRow> administrateursTable;
    private TableView<UserRow> allUsersTable;
    
    // Volets de détail unifiés
    private DetailPane technicienDetailPane;
    private DetailPane adminDetailPane;
    private DetailPane administrateursDetailPane;
    private DetailPane allUsersDetailPane;
    
    public Tab createTechnicienUsersTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        technicienTable = new TableView<>();
        technicienTable.getStyleClass().add("dark-table-view");
        setupUserTableColumns(technicienTable);
        
        // Créer le volet de détail avec le système unifié
        technicienDetailPane = DetailLayoutHelper.createUserVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(technicienTable);
        VBox.setVgrow(technicienTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, technicienDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(technicienTable, technicienDetailPane);
        
                // Charger les données des techniciens
        loadUsersData(technicienTable, "Technicien Mag Scène");
        
        Tab tab = TabBuilderUtils.createBasicTab("🔧 Techniciens");
        tab.setContent(content);
        return tab;
    }
    
    public Tab createAdminUsersTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        adminTable = new TableView<>();
        adminTable.getStyleClass().add("dark-table-view");
        setupUserTableColumns(adminTable);
        
        // Créer le volet de détail avec le système unifié
        adminDetailPane = DetailLayoutHelper.createUserVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(adminTable);
        VBox.setVgrow(adminTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, adminDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(adminTable, adminDetailPane);
        
        // Charger les données (collaborateurs et intermittents)
        loadCollaborateursData(adminTable);
        
        Tab tab = TabBuilderUtils.createBasicTab("👨‍💼 Collaborateurs");
        tab.setContent(content);
        return tab;
    }
    
    public Tab createAdministrateursUsersTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        administrateursTable = new TableView<>();
        administrateursTable.getStyleClass().add("dark-table-view");
        setupUserTableColumns(administrateursTable);
        
        // Créer le volet de détail avec le système unifié
        administrateursDetailPane = DetailLayoutHelper.createUserVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(administrateursTable);
        VBox.setVgrow(administrateursTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, administrateursDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(administrateursTable, administrateursDetailPane);
        
        // Charger les données
        loadUsersData(administrateursTable, "Administrateur");
        
        Tab tab = TabBuilderUtils.createBasicTab("⚙️ Administrateurs");
        tab.setContent(content);
        return tab;
    }
    
    public Tab createAllUsersTab() {
        VBox content = new VBox(10);
        
        // Créer la table
        allUsersTable = new TableView<>();
        allUsersTable.getStyleClass().add("dark-table-view");
        setupUserTableColumns(allUsersTable);
        
        // Créer le volet de détail avec le système unifié
        allUsersDetailPane = DetailLayoutHelper.createUserVisualizationPane(() -> {
            // Action d'ouverture - placeholder
        });
        
        // Conteneur principal avec séparateur
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configuration des volets
        VBox leftPane = new VBox(10);
        leftPane.getChildren().add(allUsersTable);
        VBox.setVgrow(allUsersTable, Priority.ALWAYS);
        
        splitPane.getItems().addAll(leftPane, allUsersDetailPane);
        splitPane.setDividerPositions(0.6);
        
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration de la sélection
        setupTableSelection(allUsersTable, allUsersDetailPane);
        
        // Charger les données
        loadUsersData(allUsersTable, null);
        
        Tab tab = TabBuilderUtils.createBasicTab("👥 Tous");
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Configure la sélection de table pour mettre à jour le volet de détail
     */
    private void setupTableSelection(TableView<UserRow> table, DetailPane detailPane) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && detailPane != null) {
                // Utilisation du système unifié pour créer les informations d'entité
                EntityInfo userInfo = DetailLayoutHelper.createEntityInfoFromUser(newSelection);
                detailPane.updateInfo(userInfo);
            } else if (detailPane != null) {
                // Affichage par défaut quand aucune sélection
                EntityInfo defaultInfo = new EntityInfo("Aucun utilisateur sélectionné")
                        .status("-")
                        .description("Sélectionnez un utilisateur pour voir ses détails");
                
                detailPane.updateInfo(defaultInfo);
            }
        });
    }
    
    private void setupUserTableColumns(TableView<UserRow> table) {
        TableColumn<UserRow, String> nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getNom()));
        nomColumn.setPrefWidth(120);
        
        TableColumn<UserRow, String> prenomColumn = new TableColumn<>("Prénom");
        prenomColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getPrenom()));
        prenomColumn.setPrefWidth(120);
        
        TableColumn<UserRow, String> roleColumn = new TableColumn<>("Rôle");
        roleColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getRole()));
        roleColumn.setPrefWidth(150);
        
        TableColumn<UserRow, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(Optional.ofNullable(cellData.getValue().getEmail()).orElse("Non renseigné")));
        emailColumn.setPrefWidth(200);
        
        TableColumn<UserRow, String> statutColumn = new TableColumn<>("Statut");
        statutColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getStatut()));
        statutColumn.setPrefWidth(80);
        
        table.getColumns().addAll(nomColumn, prenomColumn, roleColumn, emailColumn, statutColumn);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Double-clic pour fiche détaillée
        table.setRowFactory(tv -> {
            TableRow<UserRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    UserRow user = row.getItem();
                    AppLogger.info("Double-clic sur utilisateur ID: {}", user.getId());
                    NavigationService.openUserDetail((int) user.getId());
                }
            });
            return row;
        });
    }
    
    private void loadUsersData(TableView<UserRow> table, String roleFilter) {
        try {
            List<UserRow> allUsers = dataManager.getUserService().loadUsersFromDatabase();
            
            // Debug: afficher tous les rôles trouvés
            AppLogger.info("🔍 Rôles trouvés dans la base de données:");
            for (UserRow user : allUsers) {
                AppLogger.info("  - Utilisateur '{}': rôle = '{}'", user.getNom(), user.getRole());
            }
            
            List<UserRow> users;
            
            if (roleFilter == null) {
                users = allUsers;
            } else {
                users = allUsers.stream()
                    .filter(user -> roleFilter.equals(user.getRole()))
                    .toList();
            }
            
            table.setItems(FXCollections.observableArrayList(users));
            AppLogger.info("Chargement de {} utilisateurs (rôle: {})", 
                users.size(), roleFilter != null ? roleFilter : "TOUS");
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement: " + e.getMessage(), e);
            table.setItems(FXCollections.observableArrayList());
        }
    }
    
    private void loadCollaborateursData(TableView<UserRow> table) {
        try {
            List<UserRow> users = dataManager.getUserService().loadUsersFromDatabase()
                .stream()
                .filter(user -> "Collaborateur".equals(user.getRole()) || "Intermittent".equals(user.getRole()))
                .toList();
            
            table.setItems(FXCollections.observableArrayList(users));
            AppLogger.info("Chargement de {} collaborateurs/intermittents", users.size());
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des collaborateurs: " + e.getMessage(), e);
            table.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Rafraîchit toutes les tables du contrôleur
     */
    @Override
    public void refreshAllTables() {
        AppLogger.info("🔄 DEBUG UsersController - Rafraîchissement de toutes les tables demandé");
        try {
            if (technicienTable != null) {
                AppLogger.info("🔄 DEBUG UsersController - Chargement des données techniciens");
                loadUsersData(technicienTable, "Technicien Mag Scène");
            }
            if (adminTable != null) {
                AppLogger.info("🔄 DEBUG UsersController - Chargement des données collaborateurs");
                loadCollaborateursData(adminTable);
            }
            if (administrateursTable != null) {
                AppLogger.info("🔄 DEBUG UsersController - Chargement des données administrateurs");
                loadUsersData(administrateursTable, "Administrateur");
            }
            if (allUsersTable != null) {
                AppLogger.info("🔄 DEBUG UsersController - Chargement de tous les utilisateurs");
                loadUsersData(allUsersTable, null);
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du rafraîchissement d'UsersController: " + e.getMessage(), e);
        }
        AppLogger.info("✅ DEBUG UsersController - Toutes les tables rafraîchies");
    }
    
    @Override
    public String getComponentName() {
        return "UsersController";
    }
}
