package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.service.api.SupplierApiClient;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;
import com.magscene.magsav.desktop.view.supplier.dialog.MaterialRequestDialog;
import com.magsav.entities.MaterialRequest;
import com.magsav.enums.RequestStatus;
import com.magsav.enums.RequestContext;
import com.magsav.enums.RequestUrgency;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vue simplifiée de gestion des demandes de matériel pour test Phase 2
 */
public class MaterialRequestManagerViewSimple extends BaseManagerView<Object> {

    private TableView<RequestData> requestTable;
    private ObservableList<RequestData> requestList = FXCollections.observableArrayList();
    private SupplierApiClient apiClient = new SupplierApiClient();
    private com.magscene.magsav.desktop.service.ApiService apiService;
    private boolean useBackend = true; // Basculer entre backend et données test

    public MaterialRequestManagerViewSimple() {
        super();
        this.apiService = new com.magscene.magsav.desktop.service.ApiService();

        // Charger les données après initialisation de l'UI
        if (useBackend) {
            loadRequestsFromBackend();
        } else {
            createTestData();
        }
    }

    @Override
    protected String getModuleName() {
        return "Demandes de Matériel";
    }

    @Override
    protected String getViewCssClass() {
        return "material-request-manager";
    }

    @Override
    protected Pane createMainContent() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));

        // Statistiques rapides
        HBox statsBar = createStatsBar();

        // Table des demandes (pas de titre - déjà dans le header principal)
        createRequestTable();

        mainContainer.getChildren().addAll(statsBar, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        return mainContainer;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Référence, description...", text -> performSearch(text));
        
        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
            new String[]{"Tous statuts", "En attente", "Approuvée", "Rejetée", "Allouée", "Terminée"},
            "Tous statuts", value -> loadRequests());
        
        // ⚡ Filtre priorité avec ViewUtils
        VBox priorityBox = ViewUtils.createFilterBox("⚡ Priorité",
            new String[]{"Toutes priorités", "Urgente", "Haute", "Normale", "Basse"},
            "Toutes priorités", value -> loadRequests());
        
        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox);
    }
    
    private void performSearch(String text) {
        updateStatus("Recherche: " + text);
        // TODO: Implémenter recherche
    }
    
    private void loadRequests() {
        loadRequestsFromBackend();
    }

    @Override
    protected void initializeContent() {
        // Les données seront chargées après l'initialisation par
        // loadRequestsFromBackend() ou createTestData()
        if (requestList != null && !requestList.isEmpty()) {
            updateStatus(requestList.size() + " demande(s) chargée(s)");
        } else {
            updateStatus("Chargement...");
        }
    }

    private HBox createStatsBar() {
        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(10));
        statsBar.getStyleClass().add("stats-bar");

        Label pendingLabel = new Label("⏳ En attente: 2");
        Label approvedLabel = new Label("✅ Approuvées: 1");
        Label urgentLabel = new Label("🚨 Urgentes: 1");

        statsBar.getChildren().addAll(pendingLabel, new Separator(),
                approvedLabel, new Separator(), urgentLabel);

        return statsBar;
    }

    private void createRequestTable() {
        requestTable = new TableView<>();
        requestTable.setItems(requestList);
        requestTable.getStyleClass().add("data-table");

        // Colonne Référence
        TableColumn<RequestData, String> refCol = new TableColumn<>("Réf");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(80);

        // Colonne Description
        TableColumn<RequestData, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        // Colonne Demandeur
        TableColumn<RequestData, String> requesterCol = new TableColumn<>("Demandeur");
        requesterCol.setCellValueFactory(new PropertyValueFactory<>("requester"));
        requesterCol.setPrefWidth(120);

        // Colonne Priorité
        TableColumn<RequestData, String> priorityCol = new TableColumn<>("Priorité");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);

        // Colonne Statut
        TableColumn<RequestData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        // Colonne Date
        TableColumn<RequestData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);

        requestTable.getColumns().addAll(refCol, descCol, requesterCol, priorityCol, statusCol, dateCol);
    }

    private void createTestData() {
        requestList.addAll(
                new RequestData("DR-001", "Éclairage concert jazz", "Thomas Bernard", "🔴 Haute", "⏳ En attente",
                        "15/01/2024"),
                new RequestData("DR-002", "Réparation micros HF", "Sophie Lambert", "🚨 Urgent", "✅ Approuvée",
                        "14/01/2024"),
                new RequestData("DR-003", "Câblage sonorisation", "Marc Rousseau", "🟡 Moyenne", "📦 Allouée",
                        "12/01/2024"));
    }

    /**
     * Charge les demandes depuis le backend
     */
    private void loadRequestsFromBackend() {
        updateStatus("Chargement des demandes depuis le backend...");

        apiClient.getAllRequests().thenAccept(requests -> {
            Platform.runLater(() -> {
                requestList.clear();
                for (MaterialRequest request : requests) {
                    RequestData data = convertToRequestData(request);
                    requestList.add(data);
                }
                updateStatus(requestList.size() + " demande(s) chargée(s) depuis le backend");
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("⚠️ Erreur chargement backend: " + ex.getMessage());
                updateStatus("Backend indisponible - Utilisation des données de test");
                createTestData();
            });
            return null;
        });
    }

    /**
     * Convertit une entité MaterialRequest backend en RequestData pour affichage
     */
    private RequestData convertToRequestData(MaterialRequest request) {
        String reference = request.getRequestNumber();
        String description = request.getDescription();
        String requester = request.getRequesterName();
        String priority = formatPriority(request.getUrgency());
        String status = formatStatus(request.getStatus());
        String date = formatDate(request.getCreatedAt());

        RequestData data = new RequestData(reference, description, requester, priority, status, date);
        data.setBackendId(request.getId()); // Stocker l'ID backend
        return data;
    }

    private String formatPriority(Object urgency) {
        if (urgency == null)
            return "🟢 Basse";
        String urgencyStr = urgency.toString();
        switch (urgencyStr) {
            case "HIGH":
                return "🔴 Haute";
            case "URGENT":
                return "🚨 Urgent";
            case "MEDIUM":
                return "🟡 Moyenne";
            default:
                return "🟢 Basse";
        }
    }

    private String formatStatus(RequestStatus status) {
        if (status == null)
            return "⏳ En attente";
        switch (status) {
            case DRAFT:
                return "📝 Brouillon";
            case PENDING_APPROVAL:
                return "⏳ En attente";
            case APPROVED:
                return "✅ Approuvée";
            case REJECTED:
                return "❌ Rejetée";
            case INTEGRATED:
                return "📦 Intégrée";
            case PARTIALLY_DELIVERED:
                return "📫 Partiellement livrée";
            case COMPLETED:
                return "✔️ Terminée";
            case CANCELLED:
                return "🚫 Annulée";
            default:
                return status.toString();
        }
    }

    private String formatDate(Object dateTime) {
        if (dateTime == null)
            return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        // TODO: Parser correctement LocalDateTime quand la dépendance sera disponible
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Classe interne pour les données test
    public static class RequestData {
        private String reference;
        private String description;
        private String requester;
        private String priority;
        private String status;
        private String date;
        private Long backendId; // ID backend pour les appels API

        public RequestData(String reference, String description, String requester,
                String priority, String status, String date) {
            this.reference = reference;
            this.description = description;
            this.requester = requester;
            this.priority = priority;
            this.status = status;
            this.date = date;
        }

        // Getters pour PropertyValueFactory
        public String getReference() {
            return reference;
        }

        public String getDescription() {
            return description;
        }

        public String getRequester() {
            return requester;
        }

        public String getPriority() {
            return priority;
        }

        public String getStatus() {
            return status;
        }

        public String getDate() {
            return date;
        }

        // Setters
        public void setStatus(String status) {
            this.status = status;
        }

        // Méthode getRef (alias pour getReference)
        public String getRef() {
            return reference;
        }

        // Backend ID
        public Long getBackendId() {
            return backendId;
        }

        public void setBackendId(Long id) {
            this.backendId = id;
        }
    }

    // Méthodes abstraites du parent
    @Override
    protected void handleAdd() {
        updateStatus("Création d'une nouvelle demande...");
        showNewRequestDialog();
    }

    @Override
    protected void handleEdit() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune demande sélectionnée");
            return;
        }
        updateStatus("Modification de " + selected.getReference());
        showEditRequestDialog();
    }

    // Méthodes de dialogue avec backend
    private void showNewRequestDialog() {
        Stage dialogStage = new Stage();
        MaterialRequestDialog dialog = new MaterialRequestDialog(dialogStage);

        dialog.showAndWait().ifPresent(requestResult -> {
            if (useBackend) {
                createRequestInBackend(requestResult);
            } else {
                createRequestLocal(requestResult);
            }
        });
    }

    private void createRequestInBackend(MaterialRequestDialog.RequestResult requestResult) {
        updateStatus("Création de la demande dans le backend...");

        // Créer le DTO pour le backend
        SupplierApiClient.CreateRequestDTO dto = new SupplierApiClient.CreateRequestDTO();
        dto.requesterName = System.getProperty("user.name", "Utilisateur");
        dto.requesterEmail = System.getProperty("user.name", "user") + "@magscene.com";
        dto.context = parseContext(requestResult.getCategory());
        dto.description = requestResult.getDescription();
        dto.justification = requestResult.getJustification();
        dto.urgency = parseUrgency(requestResult.getPriority());
        dto.deliveryAddress = "Mag Scène - Entrepôt principal";

        apiClient.createRequest(dto)
                .thenAccept(createdRequest -> {
                    Platform.runLater(() -> {
                        // Ajouter les items si nécessaire
                        if (requestResult.getItems() != null && !requestResult.getItems().isEmpty()) {
                            addItemsToRequest(createdRequest.getId(), requestResult.getItems());
                        }

                        // Recharger la liste
                        loadRequestsFromBackend();
                        updateStatus("✅ Demande créée : " + createdRequest.getRequestNumber());
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showAlert("Erreur", "Impossible de créer la demande : " + ex.getMessage());
                        updateStatus("❌ Erreur création demande");
                    });
                    return null;
                });
    }

    private void createRequestLocal(MaterialRequestDialog.RequestResult requestResult) {
        String newRef = "REQ-" + (requestList.size() + 1);
        RequestData newRequest = new RequestData(
                newRef,
                requestResult.getDescription(),
                System.getProperty("user.name", "Utilisateur"),
                requestResult.getPriority(),
                "⏳ En attente",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        requestList.add(newRequest);
        updateStatus("Nouvelle demande créée : " + newRef);
    }

    private void addItemsToRequest(Long requestId, List<MaterialRequestDialog.MaterialItem> items) {
        for (MaterialRequestDialog.MaterialItem item : items) {
            apiClient.addFreeItem(
                    requestId,
                    item.getName(),
                    item.getName() + " - " + item.getDescription(),
                    item.getQuantity(),
                    null // Prix estimé non disponible dans le dialogue
            ).exceptionally(ex -> {
                System.err.println("⚠️ Erreur ajout item: " + ex.getMessage());
                return null;
            });
        }
    }

    private com.magsav.enums.RequestContext parseContext(String category) {
        if (category == null)
            return com.magsav.enums.RequestContext.EVENT;
        switch (category.toUpperCase()) {
            case "SPECTACLE":
            case "PRODUCTION":
            case "EVENT":
                return com.magsav.enums.RequestContext.EVENT;
            case "MAINTENANCE":
                return com.magsav.enums.RequestContext.MAINTENANCE;
            case "RENOUVELLEMENT":
            case "STOCK":
                return com.magsav.enums.RequestContext.STOCK;
            case "DÉVELOPPEMENT":
            case "RESEARCH":
                return com.magsav.enums.RequestContext.RESEARCH;
            case "SALES":
                return com.magsav.enums.RequestContext.SALES;
            case "INSTALLATION":
                return com.magsav.enums.RequestContext.INSTALLATION;
            case "SAV":
                return com.magsav.enums.RequestContext.SAV;
            case "TRAINING":
                return com.magsav.enums.RequestContext.TRAINING;
            default:
                return com.magsav.enums.RequestContext.OTHER;
        }
    }

    private com.magsav.enums.RequestUrgency parseUrgency(String priority) {
        if (priority == null)
            return com.magsav.enums.RequestUrgency.NORMAL;
        if (priority.contains("Urgent") || priority.contains("🚨")) {
            return com.magsav.enums.RequestUrgency.URGENT;
        } else if (priority.contains("Haute") || priority.contains("🔴") || priority.contains("High")) {
            return com.magsav.enums.RequestUrgency.HIGH;
        } else if (priority.contains("Basse") || priority.contains("🟢") || priority.contains("Low")) {
            return com.magsav.enums.RequestUrgency.LOW;
        }
        return com.magsav.enums.RequestUrgency.NORMAL;
    }

    private void showEditRequestDialog() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une demande à modifier.");
            return;
        }

        // TODO: Implémenter l'édition complète
        // Nécessite de charger les données depuis le backend et créer un RequestResult
        showAlert("Fonction en développement",
                "L'édition complète sera disponible prochainement.\n" +
                        "Utilisez pour l'instant l'approbation/rejet pour modifier le statut.");

        updateStatus("Édition non implémentée : " + selected.getReference());

        /*
         * Future implementation:
         * if (useBackend && selected.getBackendId() != null) {
         * apiClient.getRequestById(selected.getBackendId())
         * .thenAccept(request -> {
         * Platform.runLater(() -> {
         * // Créer RequestResult depuis MaterialRequest
         * // Ouvrir dialogue en mode édition
         * // Sauvegarder via PUT
         * });
         * });
         * }
         */
    }

    @Override
    protected void handleDelete() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune demande sélectionnée");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la demande");
        confirm.setContentText("Confirmer la suppression de " + selected.getReference() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                requestList.remove(selected);
                updateStatus("Demande supprimée");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void refresh() {
        super.refresh();
        if (useBackend) {
            loadRequestsFromBackend();
        } else {
            updateStatus("Données rafraîchies");
        }
    }

    // Méthodes d'actions avec backend
    private void approveSelectedRequest() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une demande à approuver.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Approuver la demande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir approuver la demande " + selected.getReference() + " ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (useBackend && selected.getBackendId() != null) {
                    // Appeler le backend
                    updateStatus("Approbation en cours...");
                    apiClient.approveRequest(selected.getBackendId(), "Admin")
                            .thenRun(() -> Platform.runLater(() -> {
                                selected.setStatus("✅ Approuvée");
                                requestTable.refresh();
                                updateStatus("Demande approuvée : " + selected.getReference());
                            }))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> {
                                    showAlert("Erreur", "Impossible d'approuver la demande : " + ex.getMessage());
                                    updateStatus("Erreur lors de l'approbation");
                                });
                                return null;
                            });
                } else {
                    // Mode local
                    selected.setStatus("✅ Approuvée");
                    requestTable.refresh();
                    updateStatus("Demande approuvée : " + selected.getReference());
                }
            }
        });
    }

    private void rejectSelectedRequest() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une demande à rejeter.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motif de rejet");
        dialog.setHeaderText("Rejeter la demande " + selected.getReference());
        dialog.setContentText("Motif du rejet:");

        dialog.showAndWait().ifPresent(motif -> {
            if (!motif.trim().isEmpty()) {
                if (useBackend && selected.getBackendId() != null) {
                    // Appeler le backend
                    updateStatus("Rejet en cours...");
                    apiClient.rejectRequest(selected.getBackendId(), motif)
                            .thenRun(() -> Platform.runLater(() -> {
                                selected.setStatus("❌ Rejetée");
                                requestTable.refresh();
                                updateStatus("Demande rejetée : " + selected.getReference() + " - " + motif);
                            }))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> {
                                    showAlert("Erreur", "Impossible de rejeter la demande : " + ex.getMessage());
                                    updateStatus("Erreur lors du rejet");
                                });
                                return null;
                            });
                } else {
                    // Mode local
                    selected.setStatus("❌ Rejetée");
                    requestTable.refresh();
                    updateStatus("Demande rejetée : " + selected.getReference() + " - " + motif);
                }
            }
        });
    }

    private void allocateSelectedRequest() {
        RequestData selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une demande à allouer.");
            return;
        }

        if (!"APPROUVEE".equals(selected.getStatus())) {
            showAlert("Statut invalide", "Seules les demandes approuvées peuvent être allouées.");
            return;
        }

        selected.setStatus("ALLOUEE");
        requestTable.refresh();
        updateStatus("Demande allouée : " + selected.getReference());
    }
}