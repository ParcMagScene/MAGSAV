package com.magscene.magsav.desktop.view.sav;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.core.di.ApplicationContext;
import com.magscene.magsav.desktop.core.search.GlobalSearchManager;
import com.magscene.magsav.desktop.core.search.SearchProvider;
import com.magscene.magsav.desktop.dialog.SAVDetailDialog;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.business.SAVService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Gestionnaire SAV refactorisé utilisant la nouvelle architecture
 * Remplace SAVManagerView et StandardSAVManagerView
 * Implémente SearchProvider pour la recherche globale
 */

public class NewSAVManagerView extends BaseManagerView<SAVRequestItem> implements SearchProvider {

    // Bloc static pour vérifier le chargement réel de la classe
    static {
        System.out.println("[STATIC BLOCK] >>> Chargement de la classe NewSAVManagerView (" + NewSAVManagerView.class.getClassLoader() + ")");
    }

    // Onglets personnalisés
    private com.magscene.magsav.desktop.component.CustomTabPane tabPane;
    // Table et données pour l'onglet Demandes
    private ObservableList<SAVRequestItem> demandesData;
    private TableView<SAVRequestItem> demandesTable;
    // Table et données pour l'onglet Interventions
    private ObservableList<SAVRequestItem> interventionsData;
    private TableView<SAVRequestItem> interventionsTable;

    // Bloc d'initialisation d'instance : toujours exécuté, même si le constructeur n'est pas appelé
    // Suppression du bloc d'instance : initialisation lazy dans createMainContent()

    // Le constructeur reste vide (ou peut être supprimé)
    public NewSAVManagerView() {
        // Vide
    }
    private SAVService savService;
    // Résultats de la dernière recherche globale
    private List<SearchResult> lastSearchResults = new ArrayList<>();
    private int lastResultCount = 0;
    // ID de la demande à mettre en évidence après création
    private String highlightRequestId = null;

    @Override
    protected void initializeContent() {
        System.out.println("🔧 NewSAVManagerView.initializeContent() - Début");
        this.savService = getService(SAVService.class);
        // TEST : Ajouter une ligne factice dans chaque tableau pour vérifier l'affichage
        if (demandesData.isEmpty()) {
            java.util.HashMap<String, Object> fakeDemande = new java.util.HashMap<>();
            fakeDemande.put("id", "FAKE-1");
            fakeDemande.put("title", "Test Demande");
            fakeDemande.put("type", "Test");
            fakeDemande.put("status", "Nouveau");
            fakeDemande.put("priority", "Normale");
            fakeDemande.put("createdAt", "2025-12-22");
            fakeDemande.put("assignedTechnician", "Aucun");
            demandesData.add(new SAVRequestItem(fakeDemande));
        }
        if (interventionsData.isEmpty()) {
            java.util.HashMap<String, Object> fakeInter = new java.util.HashMap<>();
            fakeInter.put("id", "FAKE-2");
            fakeInter.put("title", "Test Intervention");
            fakeInter.put("type", "Test");
            fakeInter.put("status", "Réparé");
            fakeInter.put("priority", "Haute");
            fakeInter.put("createdAt", "2025-12-21");
            fakeInter.put("assignedTechnician", "Aucun");
            interventionsData.add(new SAVRequestItem(fakeInter));
        }
        // Les TableView sont déjà initialisées dans le constructeur, elles ne peuvent pas être nulles
        if (demandesTable == null || interventionsTable == null) {
            System.err.println("[SAV] ERREUR FATALE : TableView null après construction !");
        }
        // Lier les boutons Edit/Delete à la sélection du tableau des demandes
        bindSelectionToButtons(
            javafx.beans.binding.Bindings.createBooleanBinding(
                () -> demandesTable.getSelectionModel().getSelectedItem() == null,
                demandesTable.getSelectionModel().selectedItemProperty()
            )
        );
        // Enregistrement comme fournisseur de recherche globale
        GlobalSearchManager.getInstance().registerSearchProvider(this);
        System.out.println("🔍 SAV enregistré comme SearchProvider");
        // Chargement initial des données pour les deux onglets
        loadSAVData();
    }

    @Override
    protected Pane createMainContent() {
        // Initialisation lazy des champs critiques
        if (demandesData == null) {
            System.out.println("[LAZY INIT] demandesData");
            demandesData = FXCollections.observableArrayList();
        }
        if (interventionsData == null) {
            System.out.println("[LAZY INIT] interventionsData");
            interventionsData = FXCollections.observableArrayList();
        }
        if (demandesTable == null) {
            System.out.println("[LAZY INIT] demandesTable");
            demandesTable = createSAVTable(demandesData, true);
        }
        if (interventionsTable == null) {
            System.out.println("[LAZY INIT] interventionsTable");
            interventionsTable = createSAVTable(interventionsData, false);
        }
        // Création du CustomTabPane avec deux onglets et volets de détails
        tabPane = new com.magscene.magsav.desktop.component.CustomTabPane();
        try {
            System.out.println("[DEBUG] demandesTable=" + demandesTable);
            System.out.println("[DEBUG] interventionsTable=" + interventionsTable);
            javafx.scene.control.Label demandesLabel = new javafx.scene.control.Label("DEMANDES SAV");
            javafx.scene.control.Label interventionsLabel = new javafx.scene.control.Label("INTERVENTIONS");
            if (demandesTable == null) System.err.println("[SAV] demandesTable est NULL !");
            if (interventionsTable == null) System.err.println("[SAV] interventionsTable est NULL !");
            // Volet de détails pour chaque table
            javafx.scene.layout.VBox demandesVBox = new javafx.scene.layout.VBox();
            demandesVBox.getChildren().add(demandesLabel);
            demandesVBox.setSpacing(8);
            javafx.scene.layout.Region demandesWithDetail = com.magscene.magsav.desktop.component.DetailPanelContainer.wrapTableView(demandesTable);
            demandesVBox.getChildren().add(demandesWithDetail);
            javafx.scene.layout.VBox interventionsVBox = new javafx.scene.layout.VBox();
            interventionsVBox.getChildren().add(interventionsLabel);
            interventionsVBox.setSpacing(8);
            javafx.scene.layout.Region interventionsWithDetail = com.magscene.magsav.desktop.component.DetailPanelContainer.wrapTableView(interventionsTable);
            interventionsVBox.getChildren().add(interventionsWithDetail);
            com.magscene.magsav.desktop.component.CustomTabPane.CustomTab demandesTab =
                new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab(
                    "Demandes",
                    demandesVBox,
                    "📝");
            com.magscene.magsav.desktop.component.CustomTabPane.CustomTab interventionsTab =
                new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab(
                    "Interventions",
                    interventionsVBox,
                    "🔧");
            tabPane.addTab(demandesTab);
            tabPane.addTab(interventionsTab);
            tabPane.selectTab(0);
            System.out.println("[DEBUG] Tabs ajoutés au tabPane (avec volets)");
            return tabPane;
        } catch (Exception e) {
            System.err.println("[SAV] Exception lors de la création du contenu principal : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "N° SAV, client, équipement...",
                text -> performSAVSearch(text, null, null));

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Nouveau", "En cours", "En attente pièces", "Réparé", "Irréparable",
                        "Fermé" },
                "Tous statuts", value -> loadSAVData());

        // ⚡ Filtre priorité avec ViewUtils
        VBox priorityBox = ViewUtils.createFilterBox("⚡ Priorité",
                new String[] { "Toutes priorités", "Urgente", "Haute", "Normale", "Basse" },
                "Toutes priorités", value -> loadSAVData());

        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox);
    }

    private TableView<SAVRequestItem> createSAVTable(ObservableList<SAVRequestItem> data, boolean editable) {
                // Colonne photo équipement
                TableColumn<SAVRequestItem, javafx.scene.image.Image> photoCol = new TableColumn<>("Photo");
                photoCol.setCellValueFactory(cellData -> {
                    String photoPath = cellData.getValue().getPhotoPath();
                    System.out.println("[DEBUG TABLE] getPhotoPath=" + photoPath);
                    javafx.scene.image.Image img = null;
                    if (photoPath != null && !photoPath.isEmpty()) {
                        img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath + ".jpg", 48, 36);
                        if (img == null) {
                            img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath, 48, 36);
                        }
                    }
                    if (img == null) {
                        System.out.println("[DEBUG TABLE] Aucune image trouvée pour " + photoPath);
                    }
                    return new javafx.beans.property.SimpleObjectProperty<>(img);
                });
                photoCol.setCellFactory(col -> new javafx.scene.control.TableCell<SAVRequestItem, javafx.scene.image.Image>() {
                    private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
                    {
                        imageView.setFitWidth(48);
                        imageView.setFitHeight(36);
                        imageView.setPreserveRatio(true);
                        setAlignment(javafx.geometry.Pos.CENTER);
                    }
                    @Override
                    protected void updateItem(javafx.scene.image.Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if (item != null) {
                                imageView.setImage(item);
                                setGraphic(imageView);
                            } else {
                                try {
                                    imageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/no-photo.png")));
                                } catch (Exception e) {
                                    // fallback : rien
                                }
                                setGraphic(imageView);
                            }
                        }
                    }
                });
                photoCol.setPrefWidth(60);
        TableView<SAVRequestItem> table = new TableView<>();
        table.setItems(data);
        table.getStyleClass().add("sav-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        javafx.scene.layout.VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        table.setMinHeight(400);
        table.setStyle("-fx-background-color: #fffbe6; -fx-border-color: #f26ba6; -fx-border-width: 2px;");

        // Colonnes spécifiques au SAV
        TableColumn<SAVRequestItem, String> idCol = new TableColumn<>("N° SAV");
        TableColumn<SAVRequestItem, String> titleCol = new TableColumn<>("Titre");
        TableColumn<SAVRequestItem, String> typeCol = new TableColumn<>("Type");
        TableColumn<SAVRequestItem, String> statusCol = new TableColumn<>("Statut");
        TableColumn<SAVRequestItem, String> priorityCol = new TableColumn<>("Priorité");
        TableColumn<SAVRequestItem, String> dateCol = new TableColumn<>("Date création");
        TableColumn<SAVRequestItem, String> technicianCol = new TableColumn<>("Technicien");
        // Colonnes équipement
        TableColumn<SAVRequestItem, String> equipmentNameCol = new TableColumn<>("Équipement");
        TableColumn<SAVRequestItem, String> locmatCol = new TableColumn<>("Code Locmat");
        TableColumn<SAVRequestItem, String> brandCol = new TableColumn<>("Marque");
        TableColumn<SAVRequestItem, String> categoryCol = new TableColumn<>("Catégorie");
        TableColumn<SAVRequestItem, String> serialCol = new TableColumn<>("N° de série");

        // Configuration des cellValueFactories
        idCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getId()));
        titleCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getTitle()));
        typeCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getType()));
        statusCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getStatus()));
        priorityCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getPriority()));
        dateCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getCreatedAt()));
        technicianCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getAssignedTechnician()));
        equipmentNameCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getEquipmentName()));
        locmatCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getEquipmentLocmat()));
        brandCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getEquipmentBrand()));
        categoryCol.setCellValueFactory(data2 -> new SimpleStringProperty(data2.getValue().getEquipmentCategory()));
        serialCol.setCellValueFactory(data2 -> {
            Object equipment = data2.getValue().getData().get("equipment");
            if (equipment instanceof java.util.Map) {
                Object serial = ((java.util.Map<?, ?>) equipment).get("serialNumber");
                return new SimpleStringProperty(serial != null ? serial.toString() : "");
            }
            Object serial = data2.getValue().getData().get("serialNumber");
            return new SimpleStringProperty(serial != null ? serial.toString() : "");
        });

        // Configuration des colonnes
        idCol.setPrefWidth(80);
        titleCol.setPrefWidth(200);
        typeCol.setPrefWidth(100);
        statusCol.setPrefWidth(100);
        priorityCol.setPrefWidth(90);
        dateCol.setPrefWidth(100);
        technicianCol.setPrefWidth(120);
        equipmentNameCol.setPrefWidth(180);
        locmatCol.setPrefWidth(110);
        brandCol.setPrefWidth(120);
        categoryCol.setPrefWidth(120);
        serialCol.setPrefWidth(120);

        table.getColumns().add(photoCol);
        table.getColumns().add(idCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(priorityCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(technicianCol);
        table.getColumns().add(equipmentNameCol);
        table.getColumns().add(locmatCol);
        table.getColumns().add(brandCol);
        table.getColumns().add(categoryCol);
        table.getColumns().add(serialCol);

        // Style de sélection uniforme
        table.setRowFactory(tv -> {
            TableRow<SAVRequestItem> row = new TableRow<>();
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    row.setStyle("-fx-background-color: " + ThemeConstants.SELECTION_BACKGROUND + "; " +
                            "-fx-text-fill: " + ThemeConstants.SELECTION_TEXT + "; " +
                            "-fx-border-color: " + ThemeConstants.SELECTION_BORDER + "; " +
                            "-fx-border-width: 1px;");
                } else {
                    row.setStyle("");
                }
            };
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            if (editable) {
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleEdit();
                    }
                });
            }
            return row;
        });
        table.setEditable(editable);
        return table;
    }

    private void loadSAVData() {
                            System.out.println("[DEBUG SAV] demandesData.size=" + demandesData.size());
                            System.out.println("[DEBUG SAV] interventionsData.size=" + interventionsData.size());
        updateStatus("Chargement des demandes et interventions SAV depuis le backend...");
        savService.loadAllSAVRequests().thenAccept(jsonResponse -> {
            Platform.runLater(() -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> savList = mapper.readValue(
                        jsonResponse,
                        new TypeReference<List<Map<String, Object>>>() {});
                    demandesData.clear();
                    interventionsData.clear();
                    for (Map<String, Object> map : savList) {
                        System.out.println("[DEBUG SAV] map=" + map);
                        String rawStatus = (String) map.get("status");
                        System.out.println("[DEBUG SAV] status=" + rawStatus);
                        SAVRequestItem item = new SAVRequestItem(map);
                        if (rawStatus == null) rawStatus = "";
                        // Statuts demandes : OPEN, VALIDATED
                        // Statuts interventions : IN_PROGRESS, WAITING_PARTS, RESOLVED, CANCELLED, EXTERNAL, CLOSED
                        if (rawStatus.equalsIgnoreCase("OPEN") || rawStatus.equalsIgnoreCase("VALIDATED")) {
                            demandesData.add(item);
                        } else if (
                            rawStatus.equalsIgnoreCase("IN_PROGRESS") ||
                            rawStatus.equalsIgnoreCase("WAITING_PARTS") ||
                            rawStatus.equalsIgnoreCase("RESOLVED") ||
                            rawStatus.equalsIgnoreCase("CANCELLED") ||
                            rawStatus.equalsIgnoreCase("EXTERNAL") ||
                            rawStatus.equalsIgnoreCase("CLOSED")
                        ) {
                            interventionsData.add(item);
                        }
                    }
                    // Suppression des refresh() inutiles : la TableView affichée observe déjà la liste
                    if (highlightRequestId != null) {
                        selectAndHighlightRequest(highlightRequestId);
                        highlightRequestId = null;
                    }
                    updateStatus("✅ " + demandesData.size() + " demandes en cours, " + interventionsData.size() + " interventions chargées");
                } catch (Exception e) {
                    System.err.println("❌ Erreur parsing JSON SAV: " + e.getMessage());
                    e.printStackTrace();
                    updateStatus("❌ Erreur lors du chargement des données: " + e.getMessage());
                }
            });
        }).exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("❌ Erreur backend SAV: " + error.getMessage());
                updateStatus("❌ Erreur lors du chargement depuis le backend: " + error.getMessage());
            });
            return null;
        });
    }

    private void performSAVSearch(String query, String status, String priority) {
        updateStatus("Recherche SAV: " + query);

        // TODO: Utiliser savService.searchSAVRequests()
        updateStatus("Recherche SAV effectuée (simulation)");
    }

    // Actions des boutons
    @SuppressWarnings("unused")
    private void handleChangeStatus() {
        TableView<SAVRequestItem> activeTable = getActiveTable();
        Object selected = activeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Changement de statut en cours...");
            // TODO: Ouvrir dialog de changement de statut
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    @SuppressWarnings("unused")
    private void handleAssignTechnician() {
        updateStatus("Attribution de technicien...");
        // TODO: Ouvrir dialog d'attribution
    }

    @SuppressWarnings("unused")
    private void handleAddNote() {
        updateStatus("Ajout de note...");
        // TODO: Ouvrir dialog de note
    }

    @SuppressWarnings("unused")
    private void handlePrintLabel() {
        updateStatus("Impression d'étiquette...");
        // TODO: Générer et imprimer l'étiquette
    }

    @SuppressWarnings("unused")
    private void handleGenerateQuote() {
        updateStatus("Génération de devis...");
        // TODO: Générer le devis
    }

    @Override
    protected void handleAdd() {
        updateStatus("Création d'une nouvelle demande SAV");
        
        // Ouvrir le dialogue de création
        ApiService apiService = ApplicationContext.getInstance().getInstance(ApiService.class);
        com.magscene.magsav.desktop.dialog.NewSAVRequestDialog dialog = 
            new com.magscene.magsav.desktop.dialog.NewSAVRequestDialog(apiService);
        dialog.initOwner(getScene().getWindow());
        
        dialog.showAndWait().ifPresent(requestData -> {
            // Soumettre la demande à l'API
            submitNewSAVRequest(requestData);
        });
    }
    
    /**
     * Soumet une nouvelle demande SAV à l'API backend
     */
    private void submitNewSAVRequest(java.util.Map<String, Object> requestData) {
        updateStatus("Envoi de la demande SAV...");

        new Thread(() -> {
            try {
                ApiService apiService = ApplicationContext.getInstance().getInstance(ApiService.class);

                // Préparer les données pour l'API
                java.util.Map<String, Object> apiData = new java.util.HashMap<>();
                apiData.put("title", requestData.get("title"));
                apiData.put("description", requestData.get("description"));
                apiData.put("priority", requestData.get("priority"));
                apiData.put("status", requestData.get("status"));
                apiData.put("type", requestData.get("type"));
                apiData.put("requesterName", requestData.get("requesterName"));

                if (requestData.get("requesterEmail") != null) {
                    apiData.put("requesterEmail", requestData.get("requesterEmail"));
                }

                // Si équipement existant, envoyer un objet equipment { id: ... }
                if (requestData.get("equipmentId") != null) {
                    Map<String, Object> equipmentObj = new HashMap<>();
                    equipmentObj.put("id", requestData.get("equipmentId"));
                    apiData.put("equipment", equipmentObj);
                    if (requestData.get("equipmentName") != null) {
                        apiData.put("equipmentName", requestData.get("equipmentName"));
                    }
                }


                // LOG DEBUG : afficher le JSON envoyé à l'API
                System.out.println("[DEBUG SAV] JSON envoyé à l'API : " + new org.json.JSONObject(apiData).toString(2));

                // Utiliser la méthode createServiceRequest de l'API
                Object response = apiService.createServiceRequest(apiData);

                // LOG DEBUG : afficher la réponse brute
                System.out.println("[DEBUG SAV] Réponse API création demande SAV : " + response);

                // Extraire l'ID de la réponse si possible
                String newRequestId = null;
                String newRequestStatus = null;
                if (response instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) response;
                    Object idObj = responseMap.get("id");
                    Object statusObj = responseMap.get("status");
                    if (idObj != null) {
                        newRequestId = String.valueOf(idObj);
                    }
                    if (statusObj != null) {
                        newRequestStatus = String.valueOf(statusObj);
                    }
                    System.out.println("[DEBUG SAV] ID créé=" + newRequestId + ", statut=" + newRequestStatus);
                }
                final String finalNewRequestId = newRequestId;

                javafx.application.Platform.runLater(() -> {
                    boolean isNewEquipmentRequest = Boolean.TRUE.equals(requestData.get("isNewEquipmentRequest"));

                    if (isNewEquipmentRequest) {
                        updateStatus("✅ Demande SAV créée - En attente de validation administrateur");
                        showInfoAlert("Demande créée", 
                            "Votre demande de SAV a été créée avec succès.\n\n" +
                            "⚠️ Comme vous avez demandé l'ajout d'un nouvel équipement, " +
                            "cette demande doit être validée par un administrateur avant traitement.");
                    } else {
                        updateStatus("✅ Demande SAV créée avec succès");
                        showInfoAlert("Demande créée", 
                            "Votre demande de SAV a été créée avec succès.\n" +
                            "Un technicien sera assigné prochainement.");
                    }

                    // Stocker l'ID pour mise en évidence après rafraîchissement
                    highlightRequestId = finalNewRequestId;

                    // Rafraîchir la liste
                    loadSAVData();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    updateStatus("❌ Erreur lors de la création de la demande");
                    showErrorAlert("Erreur", "Impossible de créer la demande SAV:\n" + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Sélectionne et met en évidence une demande SAV dans le tableau
     */
    private void selectAndHighlightRequest(String requestId) {
        if (requestId == null) return;
        // On ne met en évidence que dans l’onglet Demandes
        TableView<SAVRequestItem> table = demandesTable;
        ObservableList<SAVRequestItem> data = demandesData;
        if (table == null || data == null) return;
        for (int i = 0; i < data.size(); i++) {
            SAVRequestItem item = data.get(i);
            if (requestId.equals(item.getId())) {
                final int index = i;
                table.getSelectionModel().select(index);
                table.scrollTo(index);
                table.requestFocus();
                javafx.animation.Timeline flash = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, 
                        e -> table.setStyle("-fx-background-color: #d4edda;")),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), 
                        e -> table.setStyle("")),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(400), 
                        e -> table.setStyle("-fx-background-color: #d4edda;")),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), 
                        e -> table.setStyle(""))
                );
                flash.play();
                System.out.println("✨ Demande SAV #" + requestId + " mise en évidence");
                updateStatus("✨ Nouvelle demande SAV #" + requestId + " créée et affichée");
                break;
            }
        }
    }
    
    private void showInfoAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getScene().getWindow());
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getScene().getWindow());
        alert.showAndWait();
    }

    @Override
    protected void handleEdit() {
        TableView<SAVRequestItem> activeTable = getActiveTable();
        if (activeTable == null) {
            updateStatus("Erreur : TableView non initialisée");
            showErrorAlert("Erreur d'interface", "La table n'est pas prête. Veuillez réessayer après le chargement complet de la vue.");
            return;
        }
        SAVRequestItem selected = activeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Affichage de la demande SAV #" + selected.getId());
            String selectedId = selected.getId();
            ApiService apiService = ApplicationContext.getInstance().getInstance(ApiService.class);
            SAVDetailDialog detailDialog = new SAVDetailDialog(apiService, selected.getData());
            detailDialog.initOwner(getScene().getWindow());
            detailDialog.showAndWait();
            loadSAVData();
            if (selectedId != null) {
                javafx.application.Platform.runLater(() -> {
                    for (SAVRequestItem item : activeTable.getItems()) {
                        if (selectedId.equals(item.getId())) {
                            activeTable.getSelectionModel().select(item);
                            break;
                        }
                    }
                });
            }
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    @Override
    protected void handleDelete() {
        System.out.println("[DEBUG] handleDelete() appelé");
        TableView<SAVRequestItem> activeTable = getActiveTable();
        if (activeTable == null) {
            updateStatus("Erreur : TableView non initialisée");
            showErrorAlert("Erreur d'interface", "La table n'est pas prête. Veuillez réessayer après le chargement complet de la vue.");
            return;
        }
        SAVRequestItem selected = (SAVRequestItem) activeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Suppression de la demande SAV");
            System.out.println("[DEBUG] Suppression demandée pour : " + selected);
            // Demander confirmation
            boolean confirmed = ViewUtils.showConfirmationDialog("Supprimer la demande SAV ?", "Êtes-vous sûr de vouloir supprimer cette demande ? Cette action est irréversible.");
            if (!confirmed) return;
            Long id = null;
            try {
                id = Long.valueOf(selected.getId());
            } catch (Exception e) {
                showErrorAlert("Erreur", "ID de demande invalide : " + selected.getId());
                return;
            }
            savService.deleteSAVRequest(id).thenAccept(result -> {
                Platform.runLater(() -> {
                    updateStatus("Demande supprimée");
                    loadSAVData();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showErrorAlert("Erreur", "La suppression a échoué : " + ex.getMessage()));
                return null;
            });
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    @Override
    public void refresh() {
        loadSAVData();
    }

    /**
     * Implémentation de SearchProvider.getModuleName
     */
    @Override
    public String getModuleName() {
        return "SAV";
    }
    
    /**
     * Implémentation de SearchProvider.performSearch
     */
    @Override
    public void performSearch(String searchTerm) {
        lastSearchResults.clear();
        lastResultCount = 0;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }
        String term = searchTerm.toLowerCase().trim();
        // Recherche sur les deux listes (demandes + interventions)
        List<SAVRequestItem> allItems = new ArrayList<>();
        if (demandesData != null) allItems.addAll(demandesData);
        if (interventionsData != null) allItems.addAll(interventionsData);
        List<SAVRequestItem> matchingItems = allItems.stream()
                .filter(item -> {
                    String id = item.getId() != null ? item.getId().toLowerCase() : "";
                    String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                    String type = item.getType() != null ? item.getType().toLowerCase() : "";
                    String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
                    return id.contains(term) || title.contains(term) || 
                           type.contains(term) || status.contains(term);
                })
                .limit(10)
                .collect(Collectors.toList());
        lastResultCount = (int) allItems.stream()
                .filter(item -> {
                    String id = item.getId() != null ? item.getId().toLowerCase() : "";
                    String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                    String type = item.getType() != null ? item.getType().toLowerCase() : "";
                    String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
                    return id.contains(term) || title.contains(term) || 
                           type.contains(term) || status.contains(term);
                })
                .count();
        for (SAVRequestItem item : matchingItems) {
            String id = item.getId() != null ? item.getId() : String.valueOf(System.identityHashCode(item));
            String resultTitle = "SAV #" + id + (item.getStatus() != null ? " [" + item.getStatus() + "]" : "");
            String subtitle = item.getTitle() != null ? item.getTitle() : "";
            lastSearchResults.add(new SearchResult(id, resultTitle, subtitle, "SAV"));
        }
    }
        /**
         * Retourne la TableView active selon l’onglet sélectionné
         */
        private TableView<SAVRequestItem> getActiveTable() {
            if (tabPane == null) {
                System.err.println("[SAV] Erreur : tabPane non initialisé");
                showErrorAlert("Erreur d'initialisation", "L'interface SAV n'est pas encore prête (tabPane null).");
                return null;
            }
            if (tabPane.getSelectedTab() != null) {
                int idx = tabPane.getTabs().indexOf(tabPane.getSelectedTab());
                if (idx == 1) {
                    if (interventionsTable == null) {
                        System.err.println("[SAV] Erreur : interventionsTable non initialisée");
                        showErrorAlert("Erreur d'initialisation", "La table des interventions n'est pas prête.");
                        return null;
                    }
                    return interventionsTable;
                }
            }
            if (demandesTable == null) {
                System.err.println("[SAV] Erreur : demandesTable non initialisée");
                showErrorAlert("Erreur d'initialisation", "La table des demandes n'est pas prête.");
                return null;
            }
            return demandesTable;
        }
    
    /**
     * Implémentation de SearchProvider.getLastResultCount
     */
    @Override
    public int getLastResultCount() {
        return lastResultCount;
    }
    
    /**
     * Implémentation de SearchProvider.getLastResults
     */
    @Override
    public List<SearchResult> getLastResults() {
        return lastSearchResults;
    }

    @Override
    protected String getViewCssClass() {
        return "sav-manager-view";
    }

    // Méthodes de filtrage
    @SuppressWarnings("unused")
    private void applyFilters(String status, String priority, java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo) {
        updateStatus("Application des filtres...");
        // TODO: Implémenter le filtrage
        loadSAVData();
    }

    @SuppressWarnings("unused")
    private void resetFilters(ComboBox<String> statusFilter, ComboBox<String> priorityFilter,
            DatePicker dateFromPicker, DatePicker dateToPicker) {
        statusFilter.setValue("Tous statuts");
        priorityFilter.setValue("Toutes priorités");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        updateStatus("Filtres réinitialisés");
        loadSAVData();
    }

    @SuppressWarnings("unused")
    private void handleImportSAV() {
        updateStatus("Import SAV en cours...");
        // TODO: Implémenter l'import SAV
    }

    @SuppressWarnings("unused")
    private void handleExportSAV() {
        updateStatus("Export SAV en cours...");
        // TODO: Implémenter l'export SAV
    }

    @SuppressWarnings("unused")
    private void handleShowStatistics() {
        updateStatus("Affichage des statistiques SAV...");
        // savService.getSAVStatistics().thenAccept(stats -> {
        // Platform.runLater(() -> showStatisticsDialog(stats));
        // });
    }

    @SuppressWarnings("unused")
    private void handleGenerateReports() {
        updateStatus("Génération de rapports...");
        // TODO: Ouvrir dialog de génération de rapports
    }

    // Méthode redéfinie héritée de BaseManagerView - pas besoin de redéfinition
    // La méthode getService() est déjà disponible via BaseManagerView
}