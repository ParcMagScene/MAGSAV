package com.magscene.magsav.desktop.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.ServiceRequestDialog;
import com.magscene.magsav.desktop.model.ServiceRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.collections.transformation.FilteredList;
/**
 * Vue principale pour la gestion des demandes SAV
 * Interface JavaFX 21 avec TableView, recherche, filtres et statistiques
 */
public class ServiceRequestManagerView extends VBox {
    
    private final ApiService apiService;
    
    // Composants de l'interface
    private TableView<Map<String, Object>> table;
    private ObservableList<Map<String, Object>> allServiceRequests;
    private FilteredList<Map<String, Object>> filteredData;
    
    // ContrÃƒÂ´les de recherche et filtres
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> typeFilter;
    
    // Boutons d'action
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;
    
    // Labels de statistiques
    private Label totalLabel;
    private Label openLabel;
    private Label inProgressLabel;
    private Label resolvedLabel;
    
    public ServiceRequestManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.allServiceRequests = FXCollections.observableArrayList();
        this.filteredData = new FilteredList<>(allServiceRequests);
        
        initializeUI();
        loadServiceRequests();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        
        // Titre et header
        Label titleLabel = new Label("Gestion des Demandes SAV");
        titleLabel.getStyleClass().add("title-label");
        
        // Zone de recherche et filtres
        HBox searchAndFilters = createSearchAndFilters();
        
        // Boutons d'action
        HBox actionButtons = createActionButtons();
        
        // Conteneur pour la table
        VBox tableContainer = createTableContainer();
        
        // Statistiques
        HBox statsContainer = createStatsContainer();
        
        getChildren().addAll(titleLabel, searchAndFilters, actionButtons, tableContainer, statsContainer);
    }
    
    private HBox createSearchAndFilters() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px;");
        
        // Barre de recherche
        Label searchLabel = new Label("Recherche:");
        searchField = new TextField();
        searchField.setPromptText("Rechercher par titre, demandeur...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Filtre par statut
        Label statusLabel = new Label("Statut:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "Tous", "Ouverte", "En cours", "Attente piÃƒÂ¨ces", "RÃƒÂ©solue", "FermÃƒÂ©e", "AnnulÃƒÂ©e"
        );
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> applyFilters());
        
        // Filtre par prioritÃƒÂ©
        Label priorityLabel = new Label("PrioritÃƒÂ©:");
        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("Toutes", "Basse", "Moyenne", "Haute", "Urgente");
        priorityFilter.setValue("Toutes");
        priorityFilter.setOnAction(e -> applyFilters());
        
        // Filtre par type
        Label typeLabel = new Label("Type:");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(
            "Tous", "RÃƒÂ©paration", "Maintenance prÃƒÂ©ventive", "Installation", "Formation", "Retour marchandise", "Garantie"
        );
        typeFilter.setValue("Tous");
        typeFilter.setOnAction(e -> applyFilters());
        
        container.getChildren().addAll(
            searchLabel, searchField,
            new Separator(),
            statusLabel, statusFilter,
            priorityLabel, priorityFilter,
            typeLabel, typeFilter
        );
        
        return container;
    }
    
    private HBox createActionButtons() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        
        addButton = new Button("Nouvelle Demande");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openAddDialog());
        
        editButton = new Button("Modifier");
        editButton.getStyleClass().add("button-secondary");
        editButton.setDisable(true);
        editButton.setOnAction(e -> openEditDialog());
        
        deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedRequest());
        
        refreshButton = new Button("Actualiser");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> loadServiceRequests());
        
        container.getChildren().addAll(addButton, editButton, deleteButton, new Region(), refreshButton);
        HBox.setHgrow(container.getChildren().get(4), Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createTableContainer() {
        VBox container = new VBox(10);
        
        // CrÃƒÂ©ation de la table
        table = new TableView<>(filteredData);
        table.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    editButton.setDisable(false);
                    deleteButton.setDisable(false);
                    if (event.getClickCount() == 2) {
                        openEditDialog();
                    }
                }
            });
            return row;
        });
        
        // Colonne ID
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get("id").toString()));
        idCol.setPrefWidth(60);
        
        // Colonne Titre
        TableColumn<Map<String, Object>, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("title")));
        titleCol.setPrefWidth(250);
        
        // Colonne Type
        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("type")));
        typeCol.setPrefWidth(150);
        
        // Colonne Statut avec indicateur colorÃƒÂ©
        TableColumn<Map<String, Object>, HBox> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> {
            String status = (String) data.getValue().get("status");
            HBox statusBox = new HBox(8);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            
            Circle indicator = new Circle(6);
            indicator.setFill(getStatusColor(status));
            
            Label statusLabel = new Label(status);
            statusBox.getChildren().addAll(indicator, statusLabel);
            
            return new javafx.beans.property.SimpleObjectProperty<>(statusBox);
        });
        statusCol.setPrefWidth(120);
        
        // Colonne PrioritÃƒÂ© avec couleur
        TableColumn<Map<String, Object>, Label> priorityCol = new TableColumn<>("PrioritÃƒÂ©");
        priorityCol.setCellValueFactory(data -> {
            String priority = (String) data.getValue().get("priority");
            Label priorityLabel = new Label(priority);
            priorityLabel.setStyle("-fx-font-weight: bold; " + getPriorityStyle(priority));
            return new javafx.beans.property.SimpleObjectProperty<>(priorityLabel);
        });
        priorityCol.setPrefWidth(100);
        
        // Colonne Demandeur
        TableColumn<Map<String, Object>, String> requesterCol = new TableColumn<>("Demandeur");
        requesterCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("requesterName")));
        requesterCol.setPrefWidth(150);
        
        // Colonne Technicien
        TableColumn<Map<String, Object>, String> technicianCol = new TableColumn<>("Technicien");
        technicianCol.setCellValueFactory(data -> {
            Object technician = data.getValue().get("assignedTechnician");
            String techName = technician != null ? technician.toString() : "Non assignÃƒÂ©";
            return new javafx.beans.property.SimpleStringProperty(techName);
        });
        technicianCol.setPrefWidth(150);
        
        // Colonne Date de crÃƒÂ©ation
        TableColumn<Map<String, Object>, String> createdCol = new TableColumn<>("CrÃƒÂ©ÃƒÂ© le");
        createdCol.setCellValueFactory(data -> {
            Object createdAt = data.getValue().get("createdAt");
            if (createdAt != null) {
                // Format simple de la date
                String dateStr = createdAt.toString().substring(0, 10); // YYYY-MM-DD
                return new javafx.beans.property.SimpleStringProperty(dateStr);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        createdCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, titleCol, typeCol, statusCol, priorityCol, 
                                 requesterCol, technicianCol, createdCol);
        
        // Style de la table
        table.setPlaceholder(new Label("Aucune demande SAV trouvÃƒÂ©e"));
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        container.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return container;
    }
    
    private HBox createStatsContainer() {
        HBox container = new HBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 8px;");
        
        totalLabel = new Label("Total: 0");
        totalLabel.getStyleClass().add("stat-label");
        
        openLabel = new Label("Ouvertes: 0");
        openLabel.getStyleClass().add("stat-label");
        
        inProgressLabel = new Label("En cours: 0");
        inProgressLabel.getStyleClass().add("stat-label");
        
        resolvedLabel = new Label("RÃƒÂ©solues: 0");
        resolvedLabel.getStyleClass().add("stat-label");
        
        container.getChildren().addAll(totalLabel, openLabel, inProgressLabel, resolvedLabel);
        
        return container;
    }
    
    private void applyFilters() {
        filteredData.setPredicate(request -> {
            // Filtre de recherche
            String searchTerm = searchField.getText().toLowerCase().trim();
            if (!searchTerm.isEmpty()) {
                String title = ((String) request.get("title")).toLowerCase();
                String requester = request.get("requesterName") != null ? 
                    ((String) request.get("requesterName")).toLowerCase() : "";
                
                if (!title.contains(searchTerm) && !requester.contains(searchTerm)) {
                    return false;
                }
            }
            
            // Filtre par statut
            String statusValue = statusFilter.getValue();
            if (!"Tous".equals(statusValue)) {
                String requestStatus = (String) request.get("status");
                if (!statusValue.equals(requestStatus)) {
                    return false;
                }
            }
            
            // Filtre par prioritÃƒÂ©
            String priorityValue = priorityFilter.getValue();
            if (!"Toutes".equals(priorityValue)) {
                String requestPriority = (String) request.get("priority");
                if (!priorityValue.equals(requestPriority)) {
                    return false;
                }
            }
            
            // Filtre par type
            String typeValue = typeFilter.getValue();
            if (!"Tous".equals(typeValue)) {
                String requestType = (String) request.get("type");
                if (!typeValue.equals(requestType)) {
                    return false;
                }
            }
            
            return true;
        });
        
        updateStats();
    }
    
    private void updateStats() {
        int total = allServiceRequests.size();
        totalLabel.setText("Total: " + total);
        
        long openCount = allServiceRequests.stream()
            .mapToLong(r -> "Ouverte".equals(r.get("status")) ? 1 : 0)
            .sum();
        openLabel.setText("Ouvertes: " + openCount);
        
        long inProgressCount = allServiceRequests.stream()
            .mapToLong(r -> "En cours".equals(r.get("status")) ? 1 : 0)
            .sum();
        inProgressLabel.setText("En cours: " + inProgressCount);
        
        long resolvedCount = allServiceRequests.stream()
            .mapToLong(r -> "RÃƒÂ©solue".equals(r.get("status")) ? 1 : 0)
            .sum();
        resolvedLabel.setText("RÃƒÂ©solues: " + resolvedCount);
    }
    
    private void loadServiceRequests() {
        CompletableFuture<List<Object>> future = apiService.getServiceRequests();
        future.thenAccept(requests -> {
            Platform.runLater(() -> {
                allServiceRequests.clear();
                requests.forEach(req -> {
                    if (req instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> requestMap = (Map<String, Object>) req;
                        allServiceRequests.add(requestMap);
                    }
                });
                updateStats();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Impossible de charger les demandes SAV");
                alert.setContentText("Erreur: " + throwable.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
    
    private Color getStatusColor(String status) {
        return switch (status) {
            case "Ouverte" -> Color.web("#fd7e14"); // Orange
            case "En cours" -> Color.web("#0d6efd"); // Bleu
            case "Attente piÃƒÂ¨ces" -> Color.web("#ffc107"); // Jaune
            case "RÃƒÂ©solue" -> Color.web("#198754"); // Vert
            case "FermÃƒÂ©e" -> Color.web("#6c757d"); // Gris
            case "AnnulÃƒÂ©e" -> Color.web("#dc3545"); // Rouge
            default -> Color.web("#dee2e6"); // Gris clair
        };
    }
    
    private String getPriorityStyle(String priority) {
        return switch (priority) {
            case "Urgente" -> "-fx-text-fill: #dc3545;"; // Rouge
            case "Haute" -> "-fx-text-fill: #fd7e14;"; // Orange
            case "Moyenne" -> "-fx-text-fill: #0d6efd;"; // Bleu
            case "Basse" -> "-fx-text-fill: #198754;"; // Vert
            default -> "-fx-text-fill: #6c757d;"; // Gris
        };
    }
    
    private void openAddDialog() {
        ServiceRequestDialog dialog = new ServiceRequestDialog(null);
        dialog.showAndWait().ifPresent(serviceRequest -> {
            // Convertir ServiceRequest en Map pour l'API REST
            Map<String, Object> requestData = convertServiceRequestToMap(serviceRequest);
            
            CompletableFuture<Object> future = apiService.createServiceRequest(requestData);
            future.thenRun(() -> {
                Platform.runLater(() -> {
                    loadServiceRequests();
                    showAlert("SuccÃƒÂ¨s", "Demande SAV crÃƒÂ©ÃƒÂ©e avec succÃƒÂ¨s.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Impossible de crÃƒÂ©er la demande SAV");
                    alert.setContentText("Erreur: " + throwable.getMessage());
                    alert.showAndWait();
                });
                return null;
            });
        });
    }
    
    private void openEditDialog() {
        Map<String, Object> selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Convertir Map en ServiceRequest pour le dialogue
            ServiceRequest serviceRequest = convertMapToServiceRequest(selected);
            
            ServiceRequestDialog dialog = new ServiceRequestDialog(serviceRequest);
            dialog.showAndWait().ifPresent(updatedRequest -> {
                // Convertir ServiceRequest en Map pour l'API REST
                Map<String, Object> requestData = convertServiceRequestToMap(updatedRequest);
                
                Long id = Long.valueOf(selected.get("id").toString());
                CompletableFuture<Object> future = apiService.updateServiceRequest(id, requestData);
                future.thenRun(() -> {
                    Platform.runLater(() -> {
                        loadServiceRequests();
                        showAlert("SuccÃƒÂ¨s", "Demande SAV modifiÃƒÂ©e avec succÃƒÂ¨s.");
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Impossible de modifier la demande SAV");
                        alert.setContentText("Erreur: " + throwable.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
            });
        }
    }
    
    private void deleteSelectedRequest() {
        Map<String, Object> selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer la suppression");
            confirmation.setHeaderText("Supprimer la demande SAV");
            confirmation.setContentText("ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer cette demande ?\n" +
                                      "Titre: " + selected.get("title"));
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Long id = Long.valueOf(selected.get("id").toString());
                    CompletableFuture<Boolean> future = apiService.deleteServiceRequest(id);
                    future.thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                loadServiceRequests();
                                showAlert("SuccÃƒÂ¨s", "Demande SAV supprimÃƒÂ©e avec succÃƒÂ¨s.");
                            } else {
                                showAlert("Erreur", "Erreur lors de la suppression de la demande SAV.");
                            }
                        });
                    }).exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setHeaderText("Impossible de supprimer la demande SAV");
                            alert.setContentText("Erreur: " + throwable.getMessage());
                            alert.showAndWait();
                        });
                        return null;
                    });
                }
            });
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Map<String, Object> convertServiceRequestToMap(ServiceRequest serviceRequest) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("title", serviceRequest.getTitle());
        data.put("description", serviceRequest.getDescription());
        data.put("type", serviceRequest.getType().name());
        data.put("status", serviceRequest.getStatus().name());
        data.put("priority", serviceRequest.getPriority().name());
        data.put("requesterName", serviceRequest.getRequesterName());
        data.put("assignedTechnician", serviceRequest.getAssignedTechnician());
        data.put("requesterEmail", serviceRequest.getRequesterEmail());
        data.put("estimatedCost", serviceRequest.getEstimatedCost());
        data.put("resolutionNotes", serviceRequest.getResolutionNotes());
        return data;
    }
    
    private ServiceRequest convertMapToServiceRequest(Map<String, Object> map) {
        ServiceRequest request = new ServiceRequest();
        
        if (map.get("id") != null) {
            request.setId(Long.valueOf(map.get("id").toString()));
        }
        
        request.setTitle((String) map.get("title"));
        request.setDescription((String) map.get("description"));
        
        // Conversion des ÃƒÂ©numÃƒÂ©rations depuis les chaÃƒÂ®nes
        if (map.get("type") != null) {
            request.setType(ServiceRequest.ServiceRequestType.valueOf((String) map.get("type")));
        }
        
        if (map.get("status") != null) {
            request.setStatus(ServiceRequest.ServiceRequestStatus.valueOf((String) map.get("status")));
        }
        
        if (map.get("priority") != null) {
            request.setPriority(ServiceRequest.Priority.valueOf((String) map.get("priority")));
        }
        
        request.setRequesterName((String) map.get("requesterName"));
        request.setAssignedTechnician((String) map.get("assignedTechnician"));
        request.setRequesterEmail((String) map.get("requesterEmail"));
        request.setResolutionNotes((String) map.get("resolutionNotes"));
        
        if (map.get("estimatedCost") != null) {
            request.setEstimatedCost(Double.valueOf(map.get("estimatedCost").toString()));
        }
        
        return request;
    }
}

