package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.dialog.ContractDialog;
import com.magscene.magsav.desktop.model.Contract;
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
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
/**
 * Interface JavaFX complÃƒÂ¨te pour la gestion des contrats
 * FonctionnalitÃƒÂ©s : tableau dÃƒÂ©taillÃƒÂ©, recherche, filtres, CRUD, statistiques
 */
public class ContractManagerView extends VBox {
    
    private final ApiService apiService;
    private TableView<Contract> contractTable;
    private ObservableList<Contract> contractData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> clientFilter;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public ContractManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.contractData = FXCollections.observableArrayList();
        initializeUI();
        loadContractData();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        setFillWidth(true);
        
        // Titre
        Label titleLabel = new Label("Ã°Å¸â€œâ€¹ Gestion des Contrats");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);
        
        // Tableau des contrats (crÃƒÂ©ÃƒÂ© d'abord car utilisÃƒÂ© dans createSearchAndFilters)
        contractTable = createContractTable();
        
        // Zone de recherche et filtres
        VBox searchBox = createSearchAndFilters();
        
        // Statistiques
        statsLabel = new Label();
        statsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsLabel.setTextFill(Color.DARKGREEN);
        
        // Indicateur de chargement
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(50, 50);
        
        // Empilement du tableau et de l'indicateur
        StackPane tableStack = new StackPane();
        tableStack.getChildren().addAll(contractTable, loadingIndicator);
        VBox.setVgrow(tableStack, Priority.ALWAYS);
        
        getChildren().addAll(titleLabel, searchBox, statsLabel, tableStack);
    }
    
    private VBox createSearchAndFilters() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        
        // PremiÃƒÂ¨re ligne : recherche et filtres
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Recherche
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Ã°Å¸â€Â Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Rechercher par numÃƒÂ©ro, titre, client...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, old, text) -> filterContracts());
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Ã°Å¸ÂÂ·Ã¯Â¸Â Type");
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Maintenance", "Location", "Prestation de service", "Support technique", "Fourniture matÃƒÂ©riel", "Mixte");
        typeFilter.setValue("Tous");
        typeFilter.setPrefWidth(150);
        typeFilter.setOnAction(e -> filterContracts());
        typeBox.getChildren().addAll(typeLabel, typeFilter);
        
        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Ã°Å¸â€œÅ  Statut");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Brouillon", "En attente signature", "Actif", "Suspendu", "RÃƒÂ©siliÃƒÂ©", "ExpirÃƒÂ©", "TerminÃƒÂ©");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(150);
        statusFilter.setOnAction(e -> filterContracts());
        statusBox.getChildren().addAll(statusLabel, statusFilter);
        
        // Filtre par client
        VBox clientBox = new VBox(5);
        Label clientLabel = new Label("Ã°Å¸â€˜Â¥ Client");
        clientLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        clientFilter = new ComboBox<>();
        clientFilter.getItems().add("Tous les clients");
        clientFilter.setValue("Tous les clients");
        clientFilter.setPrefWidth(200);
        clientFilter.setOnAction(e -> filterContracts());
        clientBox.getChildren().addAll(clientLabel, clientFilter);
        
        topRow.getChildren().addAll(searchBox, typeBox, statusBox, clientBox);
        
        // DeuxiÃƒÂ¨me ligne : boutons d'action
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        Button addButton = new Button("Ã¢Å¾â€¢ Nouveau contrat");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        addButton.setOnAction(e -> addContract());
        
        Button editButton = new Button("Ã¢Å“ÂÃ¯Â¸Â Modifier");
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        editButton.setOnAction(e -> editContract());
        editButton.disableProperty().bind(contractTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button viewButton = new Button("Ã°Å¸â€˜â‚¬ DÃƒÂ©tails");
        viewButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        viewButton.setOnAction(e -> viewContractDetails());
        viewButton.disableProperty().bind(contractTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button deleteButton = new Button("Ã°Å¸â€”â€˜Ã¯Â¸Â Supprimer");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        deleteButton.setOnAction(e -> deleteContract());
        deleteButton.disableProperty().bind(contractTable.getSelectionModel().selectedItemProperty().isNull());
        
        Button refreshButton = new Button("Ã°Å¸â€â€ž Actualiser");
        refreshButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        refreshButton.setOnAction(e -> refreshData());
        
        bottomRow.getChildren().addAll(addButton, editButton, viewButton, deleteButton, refreshButton);
        
        container.getChildren().addAll(topRow, bottomRow);
        return container;
    }
    
    private TableView<Contract> createContractTable() {
        TableView<Contract> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Colonnes du tableau
        TableColumn<Contract, String> statusCol = new TableColumn<>("Ã°Å¸â€œÅ ");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusIcon()));
        statusCol.setPrefWidth(40);
        statusCol.setMaxWidth(40);
        statusCol.setMinWidth(40);
        statusCol.setResizable(false);
        
        TableColumn<Contract, String> numberCol = new TableColumn<>("NumÃƒÂ©ro");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("contractNumber"));
        numberCol.setPrefWidth(120);
        
        TableColumn<Contract, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<Contract, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getType() != null ? data.getValue().getType().getDisplayName() : ""));
        typeCol.setPrefWidth(120);
        
        TableColumn<Contract, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientCol.setPrefWidth(150);
        
        TableColumn<Contract, String> statusNameCol = new TableColumn<>("Statut");
        statusNameCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getStatus() != null ? data.getValue().getStatus().getDisplayName() : ""));
        statusNameCol.setPrefWidth(120);
        
        TableColumn<Contract, String> startDateCol = new TableColumn<>("DÃƒÂ©but");
        startDateCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getStartDate() != null ? data.getValue().getStartDate().format(dateFormat) : ""));
        startDateCol.setPrefWidth(90);
        
        TableColumn<Contract, String> endDateCol = new TableColumn<>("Fin");
        endDateCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getEndDate() != null ? data.getValue().getEndDate().format(dateFormat) : ""));
        endDateCol.setPrefWidth(90);
        
        TableColumn<Contract, String> totalAmountCol = new TableColumn<>("Montant total");
        totalAmountCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getTotalAmount() != null ? currencyFormat.format(data.getValue().getTotalAmount()) : ""));
        totalAmountCol.setPrefWidth(120);
        
        TableColumn<Contract, String> progressCol = new TableColumn<>("Avancement");
        progressCol.setCellValueFactory(data -> {
            double progress = data.getValue().getCompletionPercentage();
            return new SimpleStringProperty(String.format("%.1f%%", progress));
        });
        progressCol.setPrefWidth(90);
        
        table.getColumns().addAll(statusCol, numberCol, titleCol, typeCol, clientCol, 
            statusNameCol, startDateCol, endDateCol, totalAmountCol, progressCol);
        
        // Double-clic pour ÃƒÂ©diter
        table.setRowFactory(tv -> {
            TableRow<Contract> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editContract();
                }
            });
            return row;
        });
        
        table.setItems(contractData);
        return table;
    }
    
    private void loadContractData() {
        setLoading(true);
        
        Task<List<Contract>> task = new Task<List<Contract>>() {
            @Override
            protected List<Contract> call() throws Exception {
                return apiService.getAllContracts();
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                contractData.setAll(task.getValue());
                filterContracts();
                updateStatistics();
                setLoading(false);
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showErrorAlert("Erreur de chargement", 
                             "Impossible de charger les donnÃƒÂ©es des contrats", 
                             task.getException().getMessage());
            });
        });
        
        new Thread(task).start();
    }
    
    private void filterContracts() {
        // TODO: ImplÃƒÂ©menter le filtrage des contrats
        updateStatistics();
    }
    
    private void updateStatistics() {
        int totalContracts = contractData.size();
        long activeContracts = contractData.stream()
            .filter(c -> c.getStatus() == Contract.ContractStatus.ACTIVE)
            .count();
        long expiringContracts = contractData.stream()
            .filter(Contract::isExpiringSoon)
            .count();
        
        BigDecimal totalValue = contractData.stream()
            .filter(c -> c.getTotalAmount() != null)
            .map(Contract::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        statsLabel.setText(String.format(
            "Ã°Å¸â€œÅ  Total: %d contrats | Ã¢Å“â€¦ Actifs: %d | Ã¢ÂÂ° Expirent bientÃƒÂ´t: %d | Ã°Å¸â€™Â° Valeur totale: %s",
            totalContracts, activeContracts, expiringContracts, currencyFormat.format(totalValue)
        ));
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        contractTable.setVisible(!loading);
    }
    
    // Actions CRUD
    private void addContract() {
        ContractDialog dialog = new ContractDialog(apiService);
        Optional<Contract> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            Contract newContract = result.get();
            
            // CrÃƒÂ©er la tÃƒÂ¢che pour sauvegarder le contrat
            Task<Contract> createTask = new Task<>() {
                @Override
                protected Contract call() throws Exception {
                    return apiService.createContract(newContract);
                }
            };
            
            createTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    Contract createdContract = createTask.getValue();
                    contractData.add(createdContract);
                    contractTable.getSelectionModel().select(createdContract);
                    contractTable.scrollTo(createdContract);
                    updateStatistics();
                    showSuccessAlert("SuccÃƒÂ¨s", "Le contrat a ÃƒÂ©tÃƒÂ© crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s !");
                });
            });
            
            createTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    Throwable exception = createTask.getException();
                    showErrorAlert("Erreur", "Impossible de crÃƒÂ©er le contrat", exception.getMessage());
                });
            });
            
            setLoading(true);
            Thread createThread = new Thread(createTask);
            createThread.setDaemon(true);
            createThread.start();
        }
    }
    
    private void editContract() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ContractDialog dialog = new ContractDialog(apiService, selected);
            Optional<Contract> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                Contract updatedContract = result.get();
                
                // CrÃƒÂ©er la tÃƒÂ¢che pour mettre ÃƒÂ  jour le contrat
                Task<Contract> updateTask = new Task<>() {
                    @Override
                    protected Contract call() throws Exception {
                        return apiService.updateContract(updatedContract);
                    }
                };
                
                updateTask.setOnSucceeded(e -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        Contract savedContract = updateTask.getValue();
                        
                        // Remplacer dans la liste
                        int index = contractData.indexOf(selected);
                        if (index >= 0) {
                            contractData.set(index, savedContract);
                            contractTable.getSelectionModel().select(savedContract);
                        }
                        
                        updateStatistics();
                        showSuccessAlert("SuccÃƒÂ¨s", "Le contrat a ÃƒÂ©tÃƒÂ© mis ÃƒÂ  jour avec succÃƒÂ¨s !");
                    });
                });
                
                updateTask.setOnFailed(e -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        Throwable exception = updateTask.getException();
                        showErrorAlert("Erreur", "Impossible de mettre ÃƒÂ  jour le contrat", exception.getMessage());
                    });
                });
                
                setLoading(true);
                Thread updateThread = new Thread(updateTask);
                updateThread.setDaemon(true);
                updateThread.start();
            }
        }
    }
    
    private void viewContractDetails() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Ouvrir une fenÃƒÂªtre de dÃƒÂ©tails avec articles et historique
            showInfoAlert("DÃƒÂ©tails du contrat", 
                "Contrat: " + selected.getDisplayName() + "\n" +
                "Client: " + selected.getClientName() + "\n" +
                "Statut: " + (selected.getStatus() != null ? selected.getStatus().getDisplayName() : "N/A") + "\n" +
                "Montant: " + (selected.getTotalAmount() != null ? currencyFormat.format(selected.getTotalAmount()) : "N/A"));
        }
    }
    
    private void deleteContract() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le contrat");
            alert.setHeaderText("ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer ce contrat ?");
            alert.setContentText("Cette action est irrÃƒÂ©versible.\nContrat : " + selected.getDisplayName());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                setLoading(true);
                CompletableFuture.runAsync(() -> {
                    try {
                        apiService.deleteContract(selected.getId());
                        Platform.runLater(() -> {
                            contractData.remove(selected);
                            updateStatistics();
                            setLoading(false);
                            showSuccessAlert("Contrat supprimÃƒÂ©", "Le contrat " + selected.getDisplayName() + " a ÃƒÂ©tÃƒÂ© supprimÃƒÂ© avec succÃƒÂ¨s.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            setLoading(false);
                            showErrorAlert("Erreur suppression", "Impossible de supprimer le contrat", e.getMessage());
                        });
                    }
                });
            }
        }
    }
    
    private void refreshData() {
        loadContractData();
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
    
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

