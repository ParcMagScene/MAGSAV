package com.magscene.magsav.desktop.view.sav;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.dialog.ServiceRequestDialog;
import com.magscene.magsav.desktop.model.ServiceRequest;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.util.AlertUtil;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * Interface avancée de suivi des réparations et interventions SAV
 * Permet un suivi détaillé de l'état des réparations avec historique
 */
public class RepairTrackingView extends BorderPane {

    private static final Logger logger = Logger.getLogger(RepairTrackingView.class.getName());
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApiService apiService;
    private final ObservableList<ServiceRequest> serviceRequests;
    private final TableView<ServiceRequest> requestsTable;

    // Les filtres et la recherche sont maintenant dans le toolbar parent
    // SAVManagerView

    public RepairTrackingView() {
        this.apiService = new ApiService();
        this.serviceRequests = FXCollections.observableArrayList();

        // Configuration principale - BorderPane n'a pas de setSpacing
        this.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + ";");

        // Initialisation des composants principaux
        this.requestsTable = createRequestsTable();

        // Construction de l'interface
        setupInterface();
        setupEventHandlers();

        // Chargement initial des données
        loadServiceRequests();
    }

    private void setupInterface() {
        // Toolbar unifiée avec actions
        HBox toolbar = createUnifiedToolbar();
        setTop(toolbar);

        // STRUCTURE SIMPLIFIÉE - Direct DetailPanelContainer comme vues standardisées
        // Plus de containers imbriqués inutiles
        // Configuration de la table (déplacée ici depuis createTableSection)
        requestsTable.setPrefHeight(400);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN);
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(requestsTable);

        // Configuration directe dans le BorderPane - INTERFACE ÉPURÉE
        setCenter(containerWithDetail);
    }

    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        toolbar.setPadding(new javafx.geometry.Insets(10));
        toolbar.setStyle(
                "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #8B91FF; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 8;");

        // Boutons d'action
        Button addBtn = new Button("➕ Nouvelle demande");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setOnAction(e -> openNewServiceRequestDialog());

        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.disableProperty().bind(requestsTable.getSelectionModel().selectedItemProperty().isNull());
        editBtn.setOnAction(e -> openEditServiceRequestDialog());

        Button viewBtn = new Button("👁️ Détails");
        viewBtn.getStyleClass().add("btn-details");
        viewBtn.disableProperty().bind(requestsTable.getSelectionModel().selectedItemProperty().isNull());
        viewBtn.setOnAction(e -> {
            ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected != null)
                openServiceRequestDetails(selected);
        });

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.disableProperty().bind(requestsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.setOnAction(e -> deleteServiceRequest());

        Button refreshBtn = ViewUtils.createRefreshButton("🔄 Actualiser", this::loadServiceRequests);

        Button exportBtn = new Button("📊 Exporter");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> exportToCSV());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        toolbar.getChildren().addAll(addBtn, editBtn, viewBtn, deleteBtn, spacer, exportBtn, refreshBtn);
        return toolbar;
    }

    // SUPPRESSION createHeaderSection() - Plus besoin de header avec containers
    // imbriqués; // SUPPRESSION createMainSection() - Plus de containers imbriqués
    // inutiles; // SUPPRESSION createTableSection() - Configuration directe dans
    // setupInterface(); // Méthode createActionsBar() supprimée - Les boutons sont
    // maintenant gérés; // par la toolbar principale dans SAVManagerView pour
    // éviter les doublons

    private TableView<ServiceRequest> createRequestsTable() {
        TableView<ServiceRequest> table = new TableView<>();
        table.setItems(serviceRequests);
        table.getStyleClass().add("equipment-table");

        // Colonne ID avec indicateur de priorité
        TableColumn<ServiceRequest, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(60);
        idCol.setCellValueFactory(data -> {
            ServiceRequest request = data.getValue();
            String priority = request.getPriority() != null ? request.getPriority().toString() : "MEDIUM";
            String icon = getPriorityIcon(priority);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + request.getId());
        });

        // Colonne Titre
        TableColumn<ServiceRequest, String> titleCol = new TableColumn<>("Titre");
        titleCol.setPrefWidth(200);
        titleCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        // Colonne Type
        TableColumn<ServiceRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(data -> {
            String type = data.getValue().getType() != null ? data.getValue().getType().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(type);
        });

        // Colonne Statut avec couleur
        TableColumn<ServiceRequest, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus() != null ? data.getValue().getStatus().toString() : "OPEN";
            return new javafx.beans.property.SimpleStringProperty(getStatusIcon(status) + " " + status);
        });

        // Colonne Demandeur
        TableColumn<ServiceRequest, String> requesterCol = new TableColumn<>("Demandeur");
        requesterCol.setPrefWidth(150);
        requesterCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRequesterName() != null ? data.getValue().getRequesterName() : "N/A"));

        // Colonne Technicien
        TableColumn<ServiceRequest, String> technicianCol = new TableColumn<>("Technicien");
        technicianCol.setPrefWidth(130);
        technicianCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAssignedTechnician() != null ? data.getValue().getAssignedTechnician()
                        : "Non assigné"));

        // Colonne Date création
        TableColumn<ServiceRequest, String> dateCol = new TableColumn<>("Créé le");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                String formattedDate = data.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Ajout individuel des colonnes pour éviter les warnings de generic array
        table.getColumns().add(idCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(requesterCol);
        table.getColumns().add(technicianCol);
        table.getColumns().add(dateCol);

        // Style du tableau et gestion du double-clic
        table.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<ServiceRequest>();

            // Gestion du double-clic pour ouvrir en mode lecture seule
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openServiceRequestDetails(row.getItem());
                }
            });

            // Méthode pour appliquer le style approprié
            Runnable updateStyle = () -> {
                if (row.isEmpty() || row.getItem() == null) {
                    row.setStyle("");
                    return;
                }

                // Priorité 1: Si sélectionné, couleur de sélection MAGSAV
                if (row.isSelected()) {
                    // Style de sélection plus visible avec bordure
                    row.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getSelectionColor() + "; "
                            +
                            "-fx-text-fill: " + UnifiedThemeManager.getInstance().getSelectionTextColor() + "; " +
                            "-fx-border-color: " + UnifiedThemeManager.getInstance().getSelectionBorderColor() + "; " +
                            "-fx-border-width: 2px;");
                    return;
                }

                // Priorité 2: Couleur selon le statut (seulement si pas sélectionné)
                ServiceRequest item = row.getItem();
                String status = item.getStatus() != null ? item.getStatus().toString() : "OPEN";

                switch (status) {
                    case "OPEN":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "IN_PROGRESS":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "RESOLVED":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "CLOSED":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    default:
                        row.setStyle("");
                }
            };

            // Mise à jour du style quand l'item change
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            // Mise à jour du style quand la sélection change
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> updateStyle.run());

            // Appel initial pour s'assurer que le style est appliqué
            updateStyle.run();

            return row;
        });

        return table;
    }

    private void setupEventHandlers() {
        // La gestion de la sélection et l'affichage des détails sont maintenant; //
        // automatiquement gérés par le DetailPanelContainer; // Les gestionnaires de
        // filtres sont maintenant dans le toolbar parent SAVManagerView
    }

    private void applyFilters() {
        // Les filtres sont maintenant dans le toolbar parent SAVManagerView; // Cette
        // méthode sera connectée aux filtres du parent quand nécessaire; // Plus de
        // mise à jour des statistiques - interface épurée
    }

    // L'affichage des détails est maintenant géré par le volet de visualisation; //
    // via l'implémentation DetailPanelProvider de ServiceRequest; // SUPPRESSION de
    // updateStatusSummary() - statistiques supprimées pour interface épurée

    private String getPriorityIcon(String priority) {
        switch (priority.toUpperCase()) {
            case "LOW":
                return "🟢";
            case "MEDIUM":
                return "🟡";
            case "HIGH":
                return "🟠";
            case "URGENT":
                return "🔴";
            default:
                return "⚪";
        }
    }

    private String getStatusIcon(String status) {
        switch (status.toUpperCase()) {
            case "OPEN":
                return "🔓";
            case "IN_PROGRESS":
                return "⚙️";
            case "WAITING_FOR_PARTS":
                return "📦";
            case "RESOLVED":
                return "✅";
            case "CLOSED":
                return "🔒";
            case "CANCELLED":
                return "❌";
            default:
                return "❓";
        }
    }

    private void loadServiceRequests() {
        // Chargement silencieux - plus d'indicateur dans les statistiques
        System.out.println("� Chargement des demandes SAV...");

        Task<List<ServiceRequest>> loadTask = new Task<List<ServiceRequest>>() {
            @Override
            protected List<ServiceRequest> call() throws Exception {
                // Appel asynchrone à l'API; // Simulation de données pour le moment
                return RepairTrackingView.this.createSimulatedServiceRequests();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<ServiceRequest> requests = getValue();
                    serviceRequests.clear();
                    if (requests != null) {
                        serviceRequests.addAll(requests);
                        System.out.println("🔧 SAV: " + requests.size() + " demandes chargées avec succès");
                    } else {
                        System.out.println("❌ SAV: Aucune demande reçue");
                    }
                    // Plus de mise à jour des statistiques - interface épurée
                    applyFilters(); // Réappliquer les filtres actuels
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    // Plus d'affichage des erreurs dans les statistiques - interface épurée
                    AlertUtil.showError("Erreur", "Impossible de charger les demandes SAV: " +
                            getException().getMessage());
                });
            }
        };

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Ouvre la fiche détaillée d'une demande SAV en mode lecture seule
     */
    private void openServiceRequestDetails(ServiceRequest request) {
        ServiceRequestDialog dialog = new ServiceRequestDialog(request, true); // true = mode lecture seule
        java.util.Optional<ServiceRequest> result = dialog.showAndWait();

        if (result.isPresent()) {
            // Si des modifications ont été apportées, rafraîchir la liste
            loadServiceRequests();
        }
    }

    /**
     * Ouvre le dialogue d'édition d'une demande SAV (appelé depuis la toolbar)
     */
    private void openServiceRequestDialog(ServiceRequest existingRequest) {
        ServiceRequestDialog dialog = new ServiceRequestDialog(existingRequest, false); // false = mode édition
        java.util.Optional<ServiceRequest> result = dialog.showAndWait();

        if (result.isPresent()) {
            // Sauvegarder via l'API puis recharger
            saveServiceRequest(result.get());
        }
    }

    private void saveServiceRequest(ServiceRequest request) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (request.getId() != null) {
                    // Modification
                    apiService.updateServiceRequest(request.getId(), request).get();
                } else {
                    // Création
                    apiService.createServiceRequest(request).get();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    loadServiceRequests(); // Recharger la liste
                    AlertUtil.showInfo("Succès", "Demande SAV sauvegardée avec succès");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    AlertUtil.showError("Erreur", "Impossible de sauvegarder la demande SAV: " +
                            getException().getMessage());
                });
            }
        };

        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    /**
     * Méthode publique pour exporter les données de réparation (appelée depuis
     * SAVManagerView)
     */
    public void exportToCSVPublic() {
        exportToCSV();
    }

    private void exportToCSV() {
        if (serviceRequests == null || serviceRequests.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export CSV", "Aucune donnée à exporter",
                    "La liste des demandes de réparation est vide.");
            return;
        }

        // Configuration du FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder l'export CSV - Suivi Réparations");
        fileChooser.setInitialFileName("repair_tracking_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");

        // Filtre pour fichiers CSV
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        // Obtenir le Stage parent depuis le Scene de ce VBox
        javafx.stage.Stage ownerStage = (javafx.stage.Stage) this.getScene().getWindow();
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            exportRepairDataToCSV(file);
        }
    }

    /**
     * Effectue l'export des données de réparation vers le fichier CSV spécifié
     */
    private void exportRepairDataToCSV(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Écriture des en-têtes CSV spécifiques aux réparations
            String headers = "ID,Titre,Type,Statut,Priorité,Demandeur,Email,Technicien Assigné,Date Création,Date Résolution,Coût Estimé,Coût Réel,Notes Résolution,Équipement,Description";
            writer.write(headers);
            writer.newLine();

            // Écriture des données
            for (ServiceRequest request : serviceRequests) {
                StringBuilder line = new StringBuilder();

                // ID
                line.append(escapeCSVField(request.getId() != null ? request.getId().toString() : ""));
                line.append(",");

                // Titre
                line.append(escapeCSVField(request.getTitle()));
                line.append(",");

                // Type
                line.append(escapeCSVField(request.getType() != null ? request.getType().toString() : ""));
                line.append(",");

                // Statut
                line.append(escapeCSVField(request.getStatus() != null ? request.getStatus().toString() : ""));
                line.append(",");

                // Priorité
                line.append(escapeCSVField(request.getPriority() != null ? request.getPriority().toString() : ""));
                line.append(",");

                // Demandeur
                line.append(escapeCSVField(request.getRequesterName()));
                line.append(",");

                // Email
                line.append(escapeCSVField(request.getRequesterEmail()));
                line.append(",");

                // Technicien assigné
                line.append(escapeCSVField(request.getAssignedTechnician()));
                line.append(",");

                // Date de création
                line.append(escapeCSVField(getFormattedDate(request.getCreatedAt())));
                line.append(",");

                // Date de résolution
                line.append(escapeCSVField(getFormattedDate(request.getResolvedAt())));
                line.append(",");

                // Coût estimé
                line.append(escapeCSVField(getFormattedCost(request.getEstimatedCost())));
                line.append(",");

                // Coût réel
                line.append(escapeCSVField(getFormattedCost(request.getActualCost())));
                line.append(",");

                // Notes de résolution
                line.append(escapeCSVField(request.getResolutionNotes()));
                line.append(",");

                // Équipement (si disponible)
                line.append(escapeCSVField(request.getEquipmentName()));
                line.append(",");

                // Description
                line.append(escapeCSVField(request.getDescription()));

                writer.write(line.toString());
                writer.newLine();
            }

            logger.log(Level.INFO, "Export CSV réussi: {0} demandes de réparation exportées vers {1}",
                    new Object[] { serviceRequests.size(), file.getAbsolutePath() });

            // Confirmation à l'utilisateur
            showAlert(Alert.AlertType.INFORMATION, "Export CSV", "Export terminé avec succès",
                    String.format("✅ %d demandes de réparation exportées vers:\n%s", serviceRequests.size(),
                            file.getName()));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'export CSV vers " + file.getAbsolutePath(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur d'Export", "Impossible d'exporter les données",
                    "Erreur lors de l'écriture du fichier CSV:\n" + e.getMessage());
        }
    }

    /**
     * Échappe les champs CSV en gérant les guillemets et virgules
     */
    private String escapeCSVField(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }

        // Si le champ contient des guillemets, virgules ou sauts de ligne, on l'entoure
        // de guillemets
        if (field.contains("\"") || field.contains(",") || field.contains("\n") || field.contains("\r")) {
            // Échapper les guillemets en les doublant
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return field;
    }

    /**
     * Formate un coût pour l'affichage CSV
     */
    private String getFormattedCost(Double cost) {
        if (cost == null)
            return "";

        try {
            return String.format("%.2f €", cost);
        } catch (Exception e) {
            return cost.toString();
        }
    }

    /**
     * Formate une date pour l'affichage CSV
     */
    private String getFormattedDate(LocalDateTime date) {
        if (date == null)
            return "";

        try {
            return date.format(CSV_DATE_FORMATTER);
        } catch (Exception e) {
            // Si le parsing échoue, retourner la valeur brute
            return date.toString();
        }
    }

    /**
     * Méthode utilitaire pour afficher les alertes
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Méthode publique pour créer une nouvelle demande depuis l'interface parent
     */
    public void createNewServiceRequest() {
        openServiceRequestDialog(null);
    }

    /**
     * Méthode publique pour rafraîchir les données
     */
    public void refreshData() {
        loadServiceRequests();
    }

    /**
     * Méthode publique pour modifier la demande sélectionnée
     */
    public void editSelectedRequest() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openServiceRequestDialog(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Modification impossible");
            alert.setContentText("Veuillez sélectionner une demande SAV à modifier.");
            alert.showAndWait();
        }
    }

    private void openNewServiceRequestDialog() {
        ServiceRequestDialog dialog = new ServiceRequestDialog(null);
        dialog.showAndWait();
        loadServiceRequests();
    }

    private void openEditServiceRequestDialog() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ServiceRequestDialog dialog = new ServiceRequestDialog(selected);
            dialog.showAndWait();
            loadServiceRequests();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Modification impossible");
            alert.setContentText("Veuillez sélectionner une demande SAV à modifier.");
            alert.showAndWait();
        }
    }

    private void deleteServiceRequest() {
        ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer la demande");
            confirm.setHeaderText("Confirmation de suppression");
            confirm.setContentText("Voulez-vous vraiment supprimer la demande: " + selected.getTitle() + " ?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    serviceRequests.remove(selected);
                }
            });
        }
    }

    private List<ServiceRequest> createSimulatedServiceRequests() {
        List<ServiceRequest> requests = new java.util.ArrayList<>();

        System.out.println("🔧 Création de données SAV simulées...");

        // Simulation de quelques demandes SAV
        ServiceRequest req1 = new ServiceRequest();
        req1.setId(1L);
        req1.setTitle("Panne éclairage scène principale");
        req1.setDescription("Plusieurs projecteurs ne fonctionnent plus sur la scène principale");
        req1.setType(ServiceRequest.ServiceRequestType.MAINTENANCE);
        req1.setStatus(ServiceRequest.ServiceRequestStatus.IN_PROGRESS);
        req1.setPriority(ServiceRequest.Priority.HIGH);
        req1.setRequesterName("Technicien A");
        req1.setAssignedTechnician("Expert Éclairage");
        req1.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
        requests.add(req1);

        ServiceRequest req2 = new ServiceRequest();
        req2.setId(2L);
        req2.setTitle("Installation nouveau système son");
        req2.setDescription("Demande d'installation d'une nouvelle console audio");
        req2.setType(ServiceRequest.ServiceRequestType.INSTALLATION);
        req2.setStatus(ServiceRequest.ServiceRequestStatus.OPEN);
        req2.setPriority(ServiceRequest.Priority.MEDIUM);
        req2.setRequesterName("Direction Technique");
        req2.setCreatedAt(java.time.LocalDateTime.now().minusHours(6));
        requests.add(req2);

        ServiceRequest req3 = new ServiceRequest();
        req3.setId(3L);
        req3.setTitle("Réparation caméra défaillante");
        req3.setDescription("Caméra n°5 présente des dysfonctionnements");
        req3.setType(ServiceRequest.ServiceRequestType.REPAIR);
        req3.setStatus(ServiceRequest.ServiceRequestStatus.RESOLVED);
        req3.setPriority(ServiceRequest.Priority.LOW);
        req3.setRequesterName("Opérateur Vidéo");
        req3.setAssignedTechnician("Spécialiste Caméra");
        req3.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
        req3.setResolvedAt(java.time.LocalDateTime.now().minusDays(1));
        requests.add(req3);

        System.out.println("✅ " + requests.size() + " demandes SAV simulées créées");

        return requests;
    }

    /**
     * Méthode publique appelée depuis la recherche globale pour sélectionner une
     * réparation
     */
    public void selectAndViewRepair(String repairName) {
        System.out.println("🔍 Recherche réparation: " + repairName + " dans " + serviceRequests.size() + " éléments");

        // Attendre que les données soient chargées si nécessaire
        if (serviceRequests.isEmpty()) {
            System.out.println("⏳ Données réparation non chargées, rechargement...");
            loadServiceRequests();
            // Programmer une nouvelle tentative après le chargement
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(() -> selectAndViewRepair(repairName));
                }).start();
            });
            return;
        }

        Platform.runLater(() -> {
            // Rechercher la réparation dans la liste
            boolean found = false;
            for (ServiceRequest request : serviceRequests) {
                if ((request.getTitle() != null &&
                        request.getTitle().toLowerCase().contains(repairName.toLowerCase())) ||
                        (request.getDescription() != null &&
                                request.getDescription().toLowerCase().contains(repairName.toLowerCase()))
                        ||
                        (request.getRequesterName() != null &&
                                request.getRequesterName().toLowerCase().contains(repairName.toLowerCase()))) {

                    // Sélectionner la réparation dans la table
                    requestsTable.getSelectionModel().select(request);
                    requestsTable.scrollTo(request);

                    // Afficher le détail dans le panneau
                    requestsTable.requestFocus();

                    found = true;
                    System.out.println("✅ Réparation trouvée et sélectionnée: " + request.getTitle());
                    break;
                }
            }

            if (!found) {
                System.out.println("❌ Réparation non trouvée: " + repairName);
            }
        });
    }
}
