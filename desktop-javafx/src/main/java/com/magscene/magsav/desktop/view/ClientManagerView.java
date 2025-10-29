package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.dialog.clients.ClientDialog;
import com.magscene.magsav.desktop.model.Client;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.List;
import java.text.NumberFormat;
import java.util.concurrent.CompletableFuture;
/**
 * Interface JavaFX complÃƒÂ¨te pour la gestion des clients
 * FonctionnalitÃƒÂ©s : tableau dÃƒÂ©taillÃƒÂ©, recherche, filtres, CRUD, statistiques
 */
public class ClientManagerView extends VBox {
    
    private final ApiService apiService;
    private TableView<Client> clientTable;
    private ObservableList<Client> clientData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> categoryFilter;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;
    
    public ClientManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.clientData = FXCollections.observableArrayList();
        initializeUI();
        loadClientData();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        VBox header = createHeader();
        
        // Table des clients (crÃƒÂ©er AVANT les boutons)
        VBox tableContainer = createTableContainer();
        
        // Toolbar avec recherche et filtres (crÃƒÂ©er APRÃƒË†S la table)
        HBox toolbar = createToolbar();
        
        // Footer avec statistiques
        HBox footer = createFooter();
        
        getChildren().addAll(header, toolbar, tableContainer, footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("Ã°Å¸â€˜Â¥ Gestion des Clients");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Portefeuille clients Ã¢â‚¬Â¢ Relations commerciales Ã¢â‚¬Â¢ Suivi des contrats");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Recherche
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Ã°Å¸â€Â Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Nom de l'entreprise, email, SIRET...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterClients());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Ã°Å¸ÂÂ¢ Type");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Entreprise", "Administration", "Association", "Particulier");
        typeFilter.setValue("Tous");
        typeFilter.setPrefWidth(150);
        typeFilter.setOnAction(e -> filterClients());
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Ã°Å¸â€â€ž Statut");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Actif", "Inactif", "Prospect", "Suspendu");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> filterClients());
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Filtre par catÃƒÂ©gorie
        VBox categoryBox = new VBox(5);
        Label categoryLabel = new Label("Ã¢Â­Â CatÃƒÂ©gorie");
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Toutes", "Premium", "VIP", "Standard", "Basique");
        categoryFilter.setValue("Toutes");
        categoryFilter.setPrefWidth(120);
        categoryFilter.setOnAction(e -> filterClients());
        categoryBox.getChildren().addAll(categoryLabel, categoryFilter);
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("Ã¢Å¡Â¡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button addButton = new Button("Ã¢Å¾â€¢ Nouveau client");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        addButton.setOnAction(e -> addClient());
        
        Button editButton = new Button("Ã¢Å“ÂÃ¯Â¸Â Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        editButton.setOnAction(e -> editClient());
        editButton.disableProperty().bind(clientTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button viewButton = new Button("Ã°Å¸â€˜â‚¬ DÃƒÂ©tails");
        viewButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        viewButton.setOnAction(e -> viewClientDetails());
        viewButton.disableProperty().bind(clientTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button deleteButton = new Button("Ã°Å¸â€”â€˜Ã¯Â¸Â Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        deleteButton.setOnAction(e -> deleteClient());
        deleteButton.disableProperty().bind(clientTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshButton = new Button("Ã°Å¸â€â€ž Actualiser");
        refreshButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        refreshButton.setOnAction(e -> refreshData());
        
        buttonRow.getChildren().addAll(addButton, editButton, viewButton, deleteButton, refreshButton);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(searchBox, typeBox, statusBox, categoryBox, spacer, actionsBox);
        return toolbar;
    }
    
    private VBox createTableContainer() {
        VBox container = new VBox(10);
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(50, 50);
        
        // Tableau des clients
        clientTable = createClientTable();
        
        container.getChildren().addAll(loadingIndicator, clientTable);
        return container;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<Client> createClientTable() {
        TableView<Client> table = new TableView<>(clientData);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN);
        
        // Colonnes du tableau
        TableColumn<Client, String> statusCol = new TableColumn<>("Ã°Å¸â€œÅ ");
        statusCol.setPrefWidth(50);
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatusIcon() + cellData.getValue().getCategoryIcon()));
        statusCol.setResizable(false);
        
        TableColumn<Client, String> nameCol = new TableColumn<>("Nom de l'entreprise");
        nameCol.setPrefWidth(200);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        
        TableColumn<Client, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(120);
        typeCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType() != null ? 
                cellData.getValue().getType().getDisplayName() : ""));
        
        TableColumn<Client, String> statusTextCol = new TableColumn<>("Statut");
        statusTextCol.setPrefWidth(100);
        statusTextCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus() != null ? 
                cellData.getValue().getStatus().getDisplayName() : ""));
        
        TableColumn<Client, String> categoryCol = new TableColumn<>("CatÃƒÂ©gorie");
        categoryCol.setPrefWidth(100);
        categoryCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory() != null ? 
                cellData.getValue().getCategory().getDisplayName() : ""));
        
        TableColumn<Client, String> cityCol = new TableColumn<>("Ville");
        cityCol.setPrefWidth(120);
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        
        TableColumn<Client, String> phoneCol = new TableColumn<>("TÃƒÂ©lÃƒÂ©phone");
        phoneCol.setPrefWidth(120);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setPrefWidth(180);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Client, String> salesRepCol = new TableColumn<>("Commercial");
        salesRepCol.setPrefWidth(140);
        salesRepCol.setCellValueFactory(new PropertyValueFactory<>("assignedSalesRep"));
        
        TableColumn<Client, String> outstandingCol = new TableColumn<>("Encours");
        outstandingCol.setPrefWidth(100);
        outstandingCol.setCellValueFactory(cellData -> {
            BigDecimal outstanding = cellData.getValue().getOutstandingAmount();
            if (outstanding != null && outstanding.compareTo(BigDecimal.ZERO) > 0) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                return new SimpleStringProperty(currencyFormat.format(outstanding));
            }
            return new SimpleStringProperty("-");
        });
        
        // Style des colonnes
        statusCol.getStyleClass().add("center-column");
        typeCol.getStyleClass().add("center-column");
        statusTextCol.getStyleClass().add("center-column");
        categoryCol.getStyleClass().add("center-column");
        outstandingCol.getStyleClass().add("right-column");
        
        table.getColumns().addAll(statusCol, nameCol, typeCol, statusTextCol, 
                                 categoryCol, cityCol, phoneCol, emailCol, salesRepCol, outstandingCol);
        
        // Double-click pour ÃƒÂ©diter
        table.setRowFactory(tv -> {
            TableRow<Client> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editClient();
                }
            });
            return row;
        });
        
        return table;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        statsLabel = new Label("Ã°Å¸â€œÅ  Statistiques : Chargement...");
        statsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsLabel.setTextFill(Color.web("#2c3e50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label helpLabel = new Label("Ã°Å¸â€™Â¡ Double-clic pour modifier Ã¢â‚¬Â¢ Clic droit pour menu contextuel");
        helpLabel.setFont(Font.font("System", 12));
        helpLabel.setTextFill(Color.web("#7f8c8d"));
        
        footer.getChildren().addAll(statsLabel, spacer, helpLabel);
        return footer;
    }
    
    private void loadClientData() {
        setLoading(true);
        
        Task<List<Client>> task = new Task<List<Client>>() {
            @Override
            protected List<Client> call() throws Exception {
                // Appel API rÃƒÂ©el pour charger les clients
                return apiService.getAllClients();
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                clientData.setAll(task.getValue());
                filterClients();
                updateStatistics();
                setLoading(false);
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showErrorAlert("Erreur de chargement", 
                             "Impossible de charger les donnÃƒÂ©es des clients", 
                             task.getException().getMessage());
            });
        });
        
        new Thread(task).start();
    }
    
    private void filterClients() {
        // TODO: ImplÃƒÂ©menter le filtrage des clients
        updateStatistics();
    }
    
    private void updateStatistics() {
        int totalClients = clientData.size();
        long activeClients = clientData.stream()
            .filter(c -> c.getStatus() == Client.ClientStatus.ACTIVE)
            .count();
        long prospects = clientData.stream()
            .filter(c -> c.getStatus() == Client.ClientStatus.PROSPECT)
            .count();
        
        BigDecimal totalOutstanding = clientData.stream()
            .filter(c -> c.getOutstandingAmount() != null)
            .map(Client::getOutstandingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        
        String stats = String.format(
            "Ã°Å¸â€œÅ  %d clients Ã¢â‚¬Â¢ Ã¢Å“â€¦ %d actifs Ã¢â‚¬Â¢ Ã°Å¸Å½Â¯ %d prospects Ã¢â‚¬Â¢ Ã°Å¸â€™Â° %s d'encours",
            totalClients, activeClients, prospects, currencyFormat.format(totalOutstanding)
        );
        
        statsLabel.setText(stats);
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        clientTable.setVisible(!loading);
    }
    
    // Actions CRUD
    private void addClient() {
        ClientDialog dialog = new ClientDialog(apiService, null);
        dialog.showAndWait();
        
        if (dialog.getResult()) {
            setLoading(true);
            CompletableFuture.supplyAsync(() -> {
                try {
                    return apiService.createClient(dialog.getClient());
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showErrorAlert("Erreur crÃƒÂ©ation", "Impossible de crÃƒÂ©er le client", e.getMessage());
                    });
                    return null;
                }
            }).thenAccept(createdClient -> {
                if (createdClient != null) {
                    Platform.runLater(() -> {
                        clientData.add(createdClient);
                        updateStatistics();
                        setLoading(false);
                        showSuccessAlert("Client crÃƒÂ©ÃƒÂ©", "Le client " + createdClient.getCompanyName() + " a ÃƒÂ©tÃƒÂ© crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s.");
                    });
                }
            });
        }
    }
    
    private void editClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ClientDialog dialog = new ClientDialog(apiService, selected);
            dialog.showAndWait();
            
            if (dialog.getResult()) {
                setLoading(true);
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return apiService.updateClient(dialog.getClient());
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            setLoading(false);
                            showErrorAlert("Erreur modification", "Impossible de modifier le client", e.getMessage());
                        });
                        return null;
                    }
                }).thenAccept(updatedClient -> {
                    if (updatedClient != null) {
                        Platform.runLater(() -> {
                            // Remplacer l'ancien client dans la liste
                            int index = clientData.indexOf(selected);
                            if (index >= 0) {
                                clientData.set(index, updatedClient);
                            }
                            clientTable.refresh();
                            updateStatistics();
                            setLoading(false);
                            showSuccessAlert("Client modifiÃƒÂ©", "Le client " + updatedClient.getCompanyName() + " a ÃƒÂ©tÃƒÂ© modifiÃƒÂ© avec succÃƒÂ¨s.");
                        });
                    }
                });
            }
        }
    }
    
    private void viewClientDetails() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Ouvrir une fenÃƒÂªtre de dÃƒÂ©tails avec contacts et contrats
            System.out.println("Voir les dÃƒÂ©tails du client: " + selected.getCompanyName());
        }
    }
    
    private void deleteClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le client");
            alert.setHeaderText("ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer ce client ?");
            alert.setContentText("Cette action est irrÃƒÂ©versible.\nClient : " + selected.getCompanyName());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                setLoading(true);
                CompletableFuture.runAsync(() -> {
                    try {
                        apiService.deleteClient(selected.getId());
                        Platform.runLater(() -> {
                            clientData.remove(selected);
                            updateStatistics();
                            setLoading(false);
                            showSuccessAlert("Client supprimÃƒÂ©", "Le client " + selected.getCompanyName() + " a ÃƒÂ©tÃƒÂ© supprimÃƒÂ© avec succÃƒÂ¨s.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            setLoading(false);
                            showErrorAlert("Erreur suppression", "Impossible de supprimer le client", e.getMessage());
                        });
                    }
                });
            }
        }
    }
    
    private void refreshData() {
        loadClientData();
    }
    
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

