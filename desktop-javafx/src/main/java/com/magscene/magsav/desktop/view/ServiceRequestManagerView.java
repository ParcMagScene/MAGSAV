package com.magscene.magsav.desktop.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.dialog.ServiceRequestDialog;
import com.magscene.magsav.desktop.model.ServiceRequest;

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

    // Controles de recherche et filtres
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> typeFilter;

    // Boutons d'action (gérés localement dans createSearchAndFilters); // Labels de
    // statistiques
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
        setSpacing(10);
        setPadding(new Insets(5));

        // Zone de recherche, filtres et actions unifiée (pas de titre - déjà dans
        // header principal)
        HBox searchAndFilters = createSearchAndFilters();

        // Conteneur pour la table
        VBox tableContainer = createTableContainer();

        // Statistiques
        HBox statsContainer = createStatsContainer();

        getChildren().addAll(searchAndFilters, tableContainer, statsContainer);

        // Configuration des listeners après création de tous les composants
        setupListeners();
    }

    /**
     * Configure les listeners pour activer/désactiver les boutons selon la
     * sélection
     */
    private void setupListeners() {
        // Les boutons sont créés localement dans createSearchAndFilters(); // Pour
        // l'instant, le double-clic sur la table ouvre directement le dialogue
        // d'édition; // TODO: Améliorer la gestion des boutons si nécessaire
    }

    private HBox createSearchAndFilters() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(15));
        // container supprimé - Style géré par CSS
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("🔍 Recherche");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        searchField = new TextField();
        searchField.setPromptText("Rechercher par titre, demandeur...");
        searchField.setPrefWidth(300);
        // Style supprimé - géré par forceSearchFieldColors; // Force agressive des
        // couleurs pour contrer le CSS global
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchBox.getChildren().addAll(searchLabel, searchField);

        // Filtre par statut
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("📊 Statut");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
                "Tous", "Ouverte", "En cours", "Attente pieces", "Resolue", "Fermee", "Annulee");
        statusFilter.setValue("Tous");
        statusFilter.setPrefWidth(120);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        statusFilter.setOnAction(e -> applyFilters());
        statusBox.getChildren().addAll(statusLabel, statusFilter);

        // Filtre par priorite
        VBox priorityBox = new VBox(5);
        Label priorityLabel = new Label("⚡ Priorité");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        priorityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("Toutes", "Basse", "Moyenne", "Haute", "Urgente");
        priorityFilter.setValue("Toutes");
        priorityFilter.setPrefWidth(120);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        priorityFilter.setOnAction(e -> applyFilters());
        priorityBox.getChildren().addAll(priorityLabel, priorityFilter);

        // Filtre par type
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("🔧 Type");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(
                "Tous", "Reparation", "Maintenance preventive", "Installation", "Formation", "Retour marchandise",
                "Garantie");
        typeFilter.setValue("Tous");
        typeFilter.setPrefWidth(140);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        typeFilter.setOnAction(e -> applyFilters());
        typeBox.getChildren().addAll(typeLabel, typeFilter);

        // Boutons d'action
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox buttonRow = new HBox(10);
        Button newButton = new Button("➕ Nouvelle");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        newButton.setOnAction(e -> openAddDialog());

        Button editButton = new Button("✏️ Modifier");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        editButton.setDisable(true);
        editButton.setOnAction(e -> openEditDialog());

        Button exportButton = new Button("📊 Exporter");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportButton.setOnAction(e -> exportToCSV());

        Button refreshButton = new Button("🔄 Actualiser");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        refreshButton.setOnAction(e -> loadServiceRequests());

        // Le listener de sélection sera ajouté après la création de la table

        buttonRow.getChildren().addAll(newButton, editButton, exportButton, refreshButton);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);

        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(searchBox, statusBox, priorityBox, typeBox, spacer, actionsBox);
        return container;
    }

    private VBox createTableContainer() {
        VBox container = new VBox(10);

        // Creation de la table
        table = new TableView<>(filteredData);
        table.getStyleClass().add("sav-table");
        table.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();

            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance().getSelectionColor() + "; " +
                            "-fx-text-fill: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance().getSelectionTextColor()
                            + "; " +
                            "-fx-border-color: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance().getSelectionBorderColor()
                            + "; " +
                            "-fx-border-width: 2px;");
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
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openEditDialog();
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

        // Colonne Statut avec indicateur colore
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

        // Colonne Priorite avec couleur
        TableColumn<Map<String, Object>, Label> priorityCol = new TableColumn<>("Priorite");
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
            String techName = technician != null ? technician.toString() : "Non assigne";
            return new javafx.beans.property.SimpleStringProperty(techName);
        });
        technicianCol.setPrefWidth(150);

        // Colonne Date de creation
        TableColumn<Map<String, Object>, String> createdCol = new TableColumn<>("Cree le");
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
        table.setPlaceholder(new Label("Aucune demande SAV trouvee"));
        // Les boutons sont maintenant gérés dans createSearchAndFilters()

        container.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return container;
    }

    private HBox createStatsContainer() {
        HBox container = new HBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor()
                + "; -fx-background-radius: 8px;");

        totalLabel = new Label("Total: 0");
        totalLabel.getStyleClass().add("stat-label");

        openLabel = new Label("Ouvertes: 0");
        openLabel.getStyleClass().add("stat-label");

        inProgressLabel = new Label("En cours: 0");
        inProgressLabel.getStyleClass().add("stat-label");

        resolvedLabel = new Label("Resolues: 0");
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
                String requester = request.get("requesterName") != null
                        ? ((String) request.get("requesterName")).toLowerCase()
                        : "";

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

            // Filtre par priorite
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
                .mapToLong(r -> "Resolue".equals(r.get("status")) ? 1 : 0)
                .sum();
        resolvedLabel.setText("Resolues: " + resolvedCount);
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
        return Color.web(StandardColors.getStatusColor(status));
    }

    private String getPriorityStyle(String priority) {
        return "-fx-text-fill: " + StandardColors.getPriorityColor(priority) + ";";
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
                    showAlert("Succes", "Demande SAV creee avec succes.");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Impossible de creer la demande SAV");
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
                        showAlert("Succes", "Demande SAV modifiee avec succes.");
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
            confirmation.setContentText("Etes-vous sur de vouloir supprimer cette demande ?\n" +
                    "Titre: " + selected.get("title"));

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Long id = Long.valueOf(selected.get("id").toString());
                    CompletableFuture<Boolean> future = apiService.deleteServiceRequest(id);
                    future.thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                loadServiceRequests();
                                showAlert("Succes", "Demande SAV supprimee avec succes.");
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

        // Conversion des enumerations depuis les chaines
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

    /**
     * Exporte les demandes filtrées vers un fichier CSV
     */
    private void exportToCSV() {
        // TODO: Implémenter l'export CSV des demandes filtrées
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export CSV");
        alert.setHeaderText("Export des données");
        alert.setContentText("Fonctionnalité d'export CSV en cours de développement.\nNombre de demandes à exporter : "
                + filteredData.size());
        alert.showAndWait();
    }
}
