package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.dialog.ContractDialog;
import com.magscene.magsav.desktop.model.Contract;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Locale;
import java.util.Optional;
import java.util.List;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
/**
 * Interface JavaFX complete pour la gestion des contrats
 * Fonctionnalites : tableau detaille, recherche, filtres, CRUD, statistiques
 */
public class ContractManagerView extends BorderPane {
    
    private final ApiService apiService;
    private TableView<Contract> contractTable;
    private ObservableList<Contract> contractData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> clientFilter;
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
        // BorderPane n'a pas de setSpacing - architecture comme Ventes et Installations
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Tableau des contrats (créer EN PREMIER car utilisé dans createSearchAndFilters)
        contractTable = createContractTable();

        // Indicateur de chargement
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(50, 50);
        
        // Enveloppement du tableau dans DetailPanelContainer pour le volet de détail
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(contractTable);
        
        // Empilement du container et de l'indicateur
        StackPane tableStack = new StackPane();
        tableStack.getChildren().addAll(containerWithDetail, loadingIndicator);
        
        // Layout principal - Structure simplifiée sans containers imbriqués
        HBox toolbar = createSearchAndFilters();
        
        // Center: Directement le tableau avec le volet de détails
        setTop(toolbar);
        setCenter(containerWithDetail);
    }

    private HBox createSearchAndFilters() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle(
            "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #8B91FF; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);");
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Numéro, titre, client...", text -> filterContracts());
        searchField = (TextField) searchBox.getChildren().get(1);
        searchField.setPrefWidth(280);
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        
        // Filtre par type
        VBox typeBox = ViewUtils.createFilterBox("📋 Type",
            new String[]{"Tous", "Maintenance", "Location", "Prestation de service", "Support technique", "Fourniture materiel", "Mixte"},
            "Tous", value -> filterContracts());
        if (typeBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) typeBox.getChildren().get(1);
            typeFilter = combo;
            typeFilter.setPrefWidth(150);
        }
        
        // Filtre par statut
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
            new String[]{"Tous", "Brouillon", "En attente signature", "Actif", "Suspendu", "Resilie", "Expire", "Termine"},
            "Tous", value -> filterContracts());
        if (statusBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) statusBox.getChildren().get(1);
            statusFilter = combo;
            statusFilter.setPrefWidth(150);
        }
        
        // Filtre par client
        VBox clientBox = ViewUtils.createFilterBox("👤 Client",
            new String[]{"Tous les clients"},
            "Tous les clients", value -> filterContracts());
        if (clientBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) clientBox.getChildren().get(1);
            clientFilter = combo;
            clientFilter.setPrefWidth(180);
        }
        
        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        Button addButton = ViewUtils.createAddButton("➕ Nouveau contrat", this::addContract);
        Button editButton = ViewUtils.createEditButton("✏️ Modifier", this::editContract,
            contractTable.getSelectionModel().selectedItemProperty().isNull());
        Button viewButton = ViewUtils.createDetailsButton("👀 Détails", this::viewContractDetails,
            contractTable.getSelectionModel().selectedItemProperty().isNull());
        Button deleteButton = ViewUtils.createDeleteButton("🗑️ Supprimer", this::deleteContract,
            contractTable.getSelectionModel().selectedItemProperty().isNull());
        Button refreshButton = ViewUtils.createRefreshButton("🔄 Actualiser", this::refreshData);
        
        buttonRow.getChildren().addAll(addButton, editButton, viewButton, deleteButton, refreshButton);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer pour pousser les éléments
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(searchBox, typeBox, statusBox, clientBox, spacer, actionsBox);
        return toolbar;
    }
    
    private TableView<Contract> createContractTable() {
        TableView<Contract> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // table supprimé - Style géré par CSS
        TableColumn<Contract, String> statusCol = new TableColumn<>("📋");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusIcon()));
        statusCol.setPrefWidth(40);
        statusCol.setMaxWidth(40);
        statusCol.setMinWidth(40);
        statusCol.setResizable(false);
        
        TableColumn<Contract, String> numberCol = new TableColumn<>("Numero");
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
        
        TableColumn<Contract, String> startDateCol = new TableColumn<>("Debut");
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
        
        // Ajout individuel des colonnes pour éviter les warnings de generic array
        table.getColumns().add(statusCol);
        table.getColumns().add(numberCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(clientCol);
        table.getColumns().add(statusNameCol);
        table.getColumns().add(startDateCol);
        table.getColumns().add(endDateCol);
        table.getColumns().add(totalAmountCol);
        table.getColumns().add(progressCol);
        
        // Double-clic pour editer
        table.setRowFactory(tv -> {
            TableRow<Contract> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 1px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openContractDetails();
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
                // Appel API pour récupérer les contrats simulés
                List<Object> contractObjects = apiService.getAllContracts().get();
                List<Contract> contracts = new ArrayList<>();
                
                // Conversion des objets en entités Contract
                for (Object obj : contractObjects) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> contractMap = (Map<String, Object>) obj;
                        Contract contract = convertMapToContract(contractMap);
                        contracts.add(contract);
                    }
                }
                
                return contracts;
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                contractData.setAll(task.getValue());
                filterContracts();
                setLoading(false);
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                showErrorAlert("Erreur de chargement", 
                             "Impossible de charger les donnees des contrats", 
                             task.getException().getMessage());
            });
        });
        
        new Thread(task).start();
    }
    
    private void filterContracts() {
        String searchText = searchField.getText().toLowerCase().trim();
        String typeValue = typeFilter.getValue();
        String statusValue = statusFilter.getValue();
        String clientValue = clientFilter.getValue();
        
        ObservableList<Contract> filteredData = FXCollections.observableArrayList();
        
        for (Contract contract : contractData) {
            // Filtre de recherche
            boolean matchesSearch = searchText.isEmpty() || 
                (contract.getContractNumber() != null && contract.getContractNumber().toLowerCase().contains(searchText)) ||
                (contract.getTitle() != null && contract.getTitle().toLowerCase().contains(searchText)) ||
                (contract.getDescription() != null && contract.getDescription().toLowerCase().contains(searchText)) ||
                (contract.getClientName() != null && contract.getClientName().toLowerCase().contains(searchText));
                
            // Filtre par type
            boolean matchesType = "Tous".equals(typeValue);
            if (!matchesType) {
                String contractType = contract.getType() != null ? contract.getType().toString() : "";
                matchesType = typeValue.equals(contractType);
            }
                
            // Filtre par statut  
            boolean matchesStatus = "Tous".equals(statusValue);
            if (!matchesStatus) {
                String contractStatus = contract.getStatus() != null ? contract.getStatus().toString() : "";
                matchesStatus = statusValue.equals(contractStatus);
            }
                
            // Filtre par client
            boolean matchesClient = "Tous les clients".equals(clientValue);
            if (!matchesClient) {
                String clientName = contract.getClientName() != null ? contract.getClientName() : "";
                matchesClient = clientValue.equals(clientName);
            }
                
            if (matchesSearch && matchesType && matchesStatus && matchesClient) {
                filteredData.add(contract);
            }
        }
        
        contractTable.setItems(filteredData);
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
            
            // Creer la tache pour sauvegarder le contrat
            Task<Contract> createTask = new Task<>() {
                @Override
                protected Contract call() throws Exception {
                    // TODO: Implémentation temporaire
                    return newContract;
                }
            };
            
            createTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    Contract createdContract = createTask.getValue();
                    contractData.add(createdContract);
                    contractTable.getSelectionModel().select(createdContract);
                    contractTable.scrollTo(createdContract);
                    showSuccessAlert("Succes", "Le contrat a ete cree avec succes !");
                });
            });
            
            createTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    Throwable exception = createTask.getException();
                    showErrorAlert("Erreur", "Impossible de creer le contrat", exception.getMessage());
                });
            });
            
            setLoading(true);
            Thread createThread = new Thread(createTask);
            createThread.setDaemon(true);
            createThread.start();
        }
    }
    
    /**
     * Ouvre la fiche détaillée d'un contrat en mode lecture seule
     */
    private void openContractDetails() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ContractDialog dialog = new ContractDialog(apiService, selected, true); // true = mode lecture seule
            dialog.showAndWait().ifPresent(result -> {
                // Si des modifications ont été apportées, rafraîchir la liste
                if (result != null) {
                    loadContractData();
                }
            });
        }
    }

    private void editContract() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ContractDialog dialog = new ContractDialog(apiService, selected, false); // false = mode édition
            Optional<Contract> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                Contract updatedContract = result.get();
                
                // Creer la tache pour mettre a jour le contrat
                Task<Contract> updateTask = new Task<>() {
                    @Override
                    protected Contract call() throws Exception {
                        // TODO: Implémentation temporaire
                        return updatedContract;
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
                        
                        showSuccessAlert("Succes", "Le contrat a ete mis a jour avec succes !");
                    });
                });
                
                updateTask.setOnFailed(e -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        Throwable exception = updateTask.getException();
                        showErrorAlert("Erreur", "Impossible de mettre a jour le contrat", exception.getMessage());
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
            // TODO: Ouvrir une fenetre de details avec articles et historique
            showInfoAlert("Details du contrat", 
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
            alert.setHeaderText("Etes-vous sur de vouloir supprimer ce contrat ?");
            alert.setContentText("Cette action est irreversible.\nContrat : " + selected.getDisplayName());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                setLoading(true);
                CompletableFuture.runAsync(() -> {
                    try {
                        apiService.deleteContract(selected.getId());
                        Platform.runLater(() -> {
                            contractData.remove(selected);
                            setLoading(false);
                            showSuccessAlert("Contrat supprime", "Le contrat " + selected.getDisplayName() + " a ete supprime avec succes.");
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
    
    /**
     * Convertit une Map en objet Contract
     */
    private Contract convertMapToContract(Map<String, Object> contractMap) {
        Contract contract = new Contract();
        
        if (contractMap.get("id") != null) {
            contract.setId(((Number) contractMap.get("id")).longValue());
        }
        if (contractMap.get("description") != null) {
            contract.setDescription((String) contractMap.get("description"));
        }
        if (contractMap.get("status") != null) {
            String statusStr = (String) contractMap.get("status");
            Contract.ContractStatus status = Contract.ContractStatus.ACTIVE;
            try {
                if ("ACTIF".equals(statusStr) || "ACTIVE".equals(statusStr)) {
                    status = Contract.ContractStatus.ACTIVE;
                } else if ("SIGNE".equals(statusStr) || "SIGNED".equals(statusStr)) {
                    status = Contract.ContractStatus.ACTIVE;
                } else if ("EN_COURS".equals(statusStr) || "PENDING".equals(statusStr)) {
                    status = Contract.ContractStatus.PENDING_SIGNATURE;
                } else if ("TERMINE".equals(statusStr) || "COMPLETED".equals(statusStr)) {
                    status = Contract.ContractStatus.COMPLETED;
                } else if ("SUSPENDU".equals(statusStr) || "SUSPENDED".equals(statusStr)) {
                    status = Contract.ContractStatus.SUSPENDED;
                }
            } catch (Exception e) {
                // Garde la valeur par défaut
            }
            contract.setStatus(status);
        }
        if (contractMap.get("type") != null) {
            String typeStr = (String) contractMap.get("type");
            Contract.ContractType type = Contract.ContractType.SERVICE;
            try {
                if ("PRESTATION".equals(typeStr) || "SERVICE".equals(typeStr)) {
                    type = Contract.ContractType.SERVICE;
                } else if ("MAINTENANCE".equals(typeStr)) {
                    type = Contract.ContractType.MAINTENANCE;
                } else if ("LOCATION".equals(typeStr) || "RENTAL".equals(typeStr)) {
                    type = Contract.ContractType.RENTAL;
                }
            } catch (Exception e) {
                // Garde la valeur par défaut
            }
            contract.setType(type);
        }
        
        // Ajout des champs manquants
        if (contractMap.get("numero") != null) {
            contract.setContractNumber((String) contractMap.get("numero"));
        } else if (contractMap.get("contractNumber") != null) {
            contract.setContractNumber((String) contractMap.get("contractNumber"));
        }
        
        if (contractMap.get("title") != null) {
            contract.setTitle((String) contractMap.get("title"));
        }
        
        if (contractMap.get("client") != null) {
            contract.setClientName((String) contractMap.get("client"));
        } else if (contractMap.get("clientName") != null) {
            contract.setClientName((String) contractMap.get("clientName"));
        }
        
        if (contractMap.get("dateDebut") != null) {
            String dateStr = (String) contractMap.get("dateDebut");
            try {
                contract.setStartDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignore les erreurs de parsing de date
            }
        } else if (contractMap.get("startDate") != null) {
            String dateStr = (String) contractMap.get("startDate");
            try {
                contract.setStartDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignore les erreurs de parsing de date
            }
        }
        
        if (contractMap.get("dateFin") != null) {
            String dateStr = (String) contractMap.get("dateFin");
            try {
                contract.setEndDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignore les erreurs de parsing de date
            }
        } else if (contractMap.get("endDate") != null) {
            String dateStr = (String) contractMap.get("endDate");
            try {
                contract.setEndDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignore les erreurs de parsing de date
            }
        }
        
        if (contractMap.get("montant") != null) {
            Object montantObj = contractMap.get("montant");
            if (montantObj instanceof Number) {
                contract.setTotalAmount(BigDecimal.valueOf(((Number) montantObj).doubleValue()));
            }
        } else if (contractMap.get("totalAmount") != null) {
            Object montantObj = contractMap.get("totalAmount");
            if (montantObj instanceof Number) {
                contract.setTotalAmount(BigDecimal.valueOf(((Number) montantObj).doubleValue()));
            }
        } else if (contractMap.get("amount") != null) {
            Object montantObj = contractMap.get("amount");
            if (montantObj instanceof Number) {
                contract.setTotalAmount(BigDecimal.valueOf(((Number) montantObj).doubleValue()));
            }
        }
        
        return contract;
    }
}

