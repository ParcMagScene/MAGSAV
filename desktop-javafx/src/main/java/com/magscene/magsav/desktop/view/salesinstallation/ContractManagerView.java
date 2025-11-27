package com.magscene.magsav.desktop.view.salesinstallation;

import java.util.List;
import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Vue de gestion des contrats
 */
public class ContractManagerView extends BaseManagerView<Object> {

    private final ApiService apiService;
    private TableView<ContractData> contractTable;
    private ObservableList<ContractData> contractList;

    public ContractManagerView(ApiService apiService) {
        super();
        this.apiService = apiService;
        this.contractList = FXCollections.observableArrayList();

        // Lier la liste à la table maintenant que contractList est initialisé
        if (contractTable != null) {
            contractTable.setItems(contractList);
        }

        // Charger les données de manière asynchrone après la construction complète de
        // l'UI
        javafx.application.Platform.runLater(this::loadContracts);
    }

    @Override
    protected String getModuleName() {
        return "Contrats";
    }

    @Override
    protected String getViewCssClass() {
        return "contract-manager";
    }

    @Override
    protected Pane createMainContent() {
        // Tableau des contrats (filtres maintenant dans la toolbar unifiée)
        createContractTable();

        // Enveloppement du tableau dans DetailPanelContainer pour le volet de détail
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(contractTable);

        return containerWithDetail;
    }

    private void createContractTable() {
        contractTable = new TableView<>();
        // NE PAS lier ici car contractList n'est pas encore initialisé
        // Le binding sera fait dans le constructeur après initialisation de
        // contractList
        contractTable.setStyle("-fx-background-color: "
                + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getCurrentUIColor()
                + "; -fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1px; -fx-border-radius: 8px;");

        // Colonne Référence
        TableColumn<ContractData, String> refCol = new TableColumn<>("Référence");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(120);

        // Colonne Client
        TableColumn<ContractData, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientCol.setPrefWidth(200);

        // Colonne Type
        TableColumn<ContractData, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        // Colonne Début
        TableColumn<ContractData, String> startCol = new TableColumn<>("Début");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(100);

        // Colonne Fin
        TableColumn<ContractData, String> endCol = new TableColumn<>("Fin");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(100);

        // Colonne Montant
        TableColumn<ContractData, String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        // Colonne Statut
        TableColumn<ContractData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        contractTable.getColumns().addAll(refCol, clientCol, typeCol, startCol, endCol, amountCol, statusCol);

        // Style de sélection uniforme et double-clic
        contractTable.setRowFactory(tv -> {
            TableRow<ContractData> row = new TableRow<>();

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
                    handleEdit();
                }
            });
            return row;
        });
    }

    private void loadContracts() {
        // Tentative de chargement depuis le backend
        try {
            System.out.println("🔄 Tentative de chargement des contrats depuis le backend...");
            List<Map<String, Object>> backendContracts = apiService.getAll("contracts");

            if (backendContracts != null && !backendContracts.isEmpty()) {
                System.out.println("✅ Backend disponible - Chargement de " + backendContracts.size() + " contrats");
                contractList.clear();

                for (Map<String, Object> contractMap : backendContracts) {
                    String reference = (String) contractMap.getOrDefault("contractNumber", "N/A");

                    // Gestion du client
                    String clientName = "";
                    Object clientObj = contractMap.get("client");
                    if (clientObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> clientData = (Map<String, Object>) clientObj;
                        clientName = (String) clientData.getOrDefault("companyName", "");
                    }

                    String type = (String) contractMap.getOrDefault("type", "");
                    String startDate = contractMap.get("startDate") != null ? contractMap.get("startDate").toString()
                            : "";
                    String endDate = contractMap.get("endDate") != null ? contractMap.get("endDate").toString() : "";
                    String amount = contractMap.get("totalAmount") != null
                            ? contractMap.get("totalAmount").toString() + " €"
                            : "0 €";

                    // Mapper le statut
                    String status = mapContractStatus((String) contractMap.getOrDefault("status", "UNKNOWN"));

                    contractList.add(new ContractData(reference, clientName, type, startDate, endDate, amount, status));
                }

                System.out.println("✅ " + contractList.size() + " contrats chargés depuis le backend");
                updateStatus(contractList.size() + " contrat(s) chargé(s)");
                return;
            }
        } catch (Exception e) {
            System.err.println("❌ Backend indisponible pour les contrats: " + e.getMessage());
        }

        // Fallback sur données de test
        System.out.println("🔄 Chargement des contrats depuis données de test (mode hors-ligne)...");
        createTestData();
    }

    private String mapContractStatus(String backendStatus) {
        switch (backendStatus) {
            case "ACTIVE":
                return "✅ Actif";
            case "PENDING":
                return "⏳ En attente";
            case "EXPIRED":
                return "❌ Expiré";
            case "TERMINATED":
                return "❌ Résilié";
            default:
                return "⚠️ " + backendStatus;
        }
    }

    private void createTestData() {
        contractList.addAll(
                new ContractData("CTR-001", "Théâtre Municipal", "Location", "01/01/2025", "31/12/2025", "12 000 €",
                        "✅ Actif"),
                new ContractData("CTR-002", "Festival d'Été", "Prestation", "15/06/2025", "31/08/2025", "25 000 €",
                        "✅ Actif"),
                new ContractData("CTR-003", "Salle Polyvalente", "Maintenance", "01/01/2025", "31/12/2025", "8 000 €",
                        "✅ Actif"),
                new ContractData("CTR-004", "Entreprise Events Pro", "Location", "01/11/2024", "31/10/2025", "15 000 €",
                        "⏳ En attente"),
                new ContractData("CTR-005", "Concert Hall", "Vente", "20/09/2024", "20/09/2024", "45 000 €", "✅ Actif"),
                new ContractData("CTR-006", "Mairie Centre", "Prestation", "01/03/2024", "31/05/2024", "18 000 €",
                        "❌ Expiré"),
                new ContractData("CTR-007", "Studio Prod", "Maintenance", "01/01/2024", "31/12/2024", "6 500 €",
                        "❌ Expiré"),
                new ContractData("CTR-008", "Association Culturelle", "Location", "15/09/2025", "15/12/2025", "9 000 €",
                        "✅ Actif"),
                new ContractData("CTR-009", "Opéra National", "Prestation", "01/01/2025", "30/06/2025", "35 000 €",
                        "✅ Actif"),
                new ContractData("CTR-010", "Centre Congrès", "Maintenance", "01/07/2025", "31/12/2025", "11 000 €",
                        "⏳ En attente"));

        updateStatus(contractList.size() + " contrat(s) chargé(s)");
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Référence, client...", text -> performSearch(text));

        // 📋 Filtre type avec ViewUtils
        VBox typeBox = ViewUtils.createFilterBox("📋 Type",
                new String[] { "Tous types", "Location", "Maintenance", "SAV", "Prestation" },
                "Tous types", value -> loadContracts());

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Actif", "Expiré", "En attente", "Résilié" },
                "Tous statuts", value -> loadContracts());

        toolbar.getChildren().addAll(searchBox, typeBox, statusBox);
    }

    private void performSearch(String text) {
        updateStatus("Recherche: " + text);
        // TODO: Implémenter recherche
    }

    @Override
    protected void initializeContent() {
        // ⚠️ Ne rien faire si les champs ne sont pas encore initialisés
        // (cela arrive car super() appelle cette méthode AVANT que le constructeur de
        // ContractManagerView finisse)
        if (contractList == null || apiService == null) {
            System.out.println("⚠️ initializeContent() appelé trop tôt - champs non initialisés");
            return;
        }

        // Charger les données après que la table soit créée
        loadContracts();

        if (contractList != null && contractList.size() > 0) {
            updateStatus(contractList.size() + " contrat(s) chargé(s)");
        } else {
            updateStatus("Aucun contrat");
        }
    }

    @Override
    protected void handleAdd() {
        updateStatus("Création d'un nouveau contrat...");
        showAlert("Nouveau contrat", "Formulaire de création de contrat - À implémenter");
    }

    @Override
    protected void handleEdit() {
        ContractData selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun contrat sélectionné");
            return;
        }
        updateStatus("Édition du contrat " + selected.getReference());
        showAlert("Édition", "Formulaire d'édition du contrat " + selected.getReference() + " - À implémenter");
    }

    @Override
    protected void handleDelete() {
        ContractData selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun contrat sélectionné");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le contrat");
        confirm.setContentText("Confirmer la suppression du contrat " + selected.getReference() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                contractList.remove(selected);
                updateStatus("Contrat supprimé");
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
        updateStatus("Données rafraîchies");
    }

    // Classe interne pour les données de contrat
    public static class ContractData implements com.magscene.magsav.desktop.component.DetailPanelProvider {
        private final String reference;
        private final String clientName;
        private final String type;
        private final String startDate;
        private final String endDate;
        private final String amount;
        private final String status;

        public ContractData(String reference, String clientName, String type, String startDate,
                String endDate, String amount, String status) {
            this.reference = reference;
            this.clientName = clientName;
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.amount = amount;
            this.status = status;
        }

        public String getReference() {
            return reference;
        }

        public String getClientName() {
            return clientName;
        }

        public String getType() {
            return type;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getAmount() {
            return amount;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String getDetailTitle() {
            return reference + " - " + clientName;
        }

        @Override
        public String getDetailSubtitle() {
            return type + " • " + startDate + " → " + endDate;
        }

        @Override
        public javafx.scene.image.Image getDetailImage() {
            // Pas d'image pour les contrats
            return null;
        }

        @Override
        public String getDetailId() {
            return reference;
        }

        @Override
        public javafx.scene.layout.VBox getDetailInfoContent() {
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            // Grille d'informations
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(15);
            grid.setVgap(10);

            int row = 0;
            addDetailRow(grid, row++, "📋 Référence:", reference);
            addDetailRow(grid, row++, "👤 Client:", clientName);
            addDetailRow(grid, row++, "📄 Type:", type);
            addDetailRow(grid, row++, "📅 Début:", startDate);
            addDetailRow(grid, row++, "📅 Fin:", endDate);
            addDetailRow(grid, row++, "💰 Montant:", amount);
            addDetailRow(grid, row++, "🔹 Statut:", status);

            content.getChildren().add(grid);
            return content;
        }

        private void addDetailRow(javafx.scene.layout.GridPane grid, int row, String label, String value) {
            javafx.scene.control.Label labelNode = new javafx.scene.control.Label(label);
            labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 100px;");
            javafx.scene.control.Label valueNode = new javafx.scene.control.Label(value != null ? value : "N/A");

            grid.add(labelNode, 0, row);
            grid.add(valueNode, 1, row);
        }
    }
}
