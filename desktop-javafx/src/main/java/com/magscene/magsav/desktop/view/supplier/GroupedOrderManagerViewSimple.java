package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.dialog.supplier.GroupedOrderDialog;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vue simplifiée de gestion des commandes groupées pour test Phase 2
 */
public class GroupedOrderManagerViewSimple extends BaseManagerView<Object> {

    private TableView<OrderData> orderTable;
    private ObservableList<OrderData> orderList; // Déclaration sans initialisation
    private com.magscene.magsav.desktop.service.ApiService apiService;

    public GroupedOrderManagerViewSimple() {
        super();
        this.apiService = new com.magscene.magsav.desktop.service.ApiService();

        // Charger depuis le backend
        loadOrdersFromBackend();
    }

    @Override
    protected String getModuleName() {
        return "Commandes Groupées";
    }

    @Override
    protected String getViewCssClass() {
        return "grouped-order-manager";
    }

    @Override
    protected Pane createMainContent() {
        VBox mainContainer = new VBox(10);

        // Tableau de bord des seuils
        HBox dashboardBar = createDashboard();

        // Table des commandes (pas de titre - déjà dans le header principal)
        createOrderTable();

        // Envelopper le tableau dans DetailPanelContainer pour le volet de détail
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(orderTable);

        mainContainer.getChildren().addAll(dashboardBar, containerWithDetail);
        VBox.setVgrow(containerWithDetail, Priority.ALWAYS);

        return mainContainer;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Référence, fournisseur...",
                text -> performSearch(text));

        // 📦 Filtre fournisseur avec ViewUtils
        VBox supplierBox = ViewUtils.createFilterBox("📦 Fournisseur",
                new String[] { "Tous fournisseurs", "Fournisseur A", "Fournisseur B", "Fournisseur C" },
                "Tous fournisseurs", value -> loadOrders());

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "En préparation", "Validée", "Envoyée", "Livrée" },
                "Tous statuts", value -> loadOrders());

        toolbar.getChildren().addAll(searchBox, supplierBox, statusBox);
    }

    private void performSearch(String text) {
        updateStatus("Recherche: " + text);
        if (text == null || text.isEmpty()) {
            orderTable.setItems(orderList);
            return;
        }
        String searchLower = text.toLowerCase();
        orderTable.setItems(orderList.filtered(order -> {
            String orderRef = order.getReference() != null ? order.getReference().toLowerCase() : "";
            String supplier = order.getSupplier() != null ? order.getSupplier().toLowerCase() : "";
            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "";
            return orderRef.contains(searchLower) || supplier.contains(searchLower) || status.contains(searchLower);
        }));
    }

    private void loadOrders() {
        loadOrdersFromBackend();
    }

    @Override
    protected void initializeContent() {
        // CRITICAL: Initialiser orderList ICI
        if (orderList == null) {
            orderList = FXCollections.observableArrayList();
            System.out.println("✅ orderList initialisé");
        }

        // Binding du tableau après création
        if (orderTable != null && orderList != null) {
            orderTable.setItems(orderList);
            System.out.println("🔗 Tableau GroupedOrders lié à orderList");
            
            // Lier les boutons Edit/Delete à la sélection du tableau
            bindSelectionToButtons(
                javafx.beans.binding.Bindings.createBooleanBinding(
                    () -> orderTable.getSelectionModel().getSelectedItem() == null,
                    orderTable.getSelectionModel().selectedItemProperty()
                )
            );
        }

        // Les données seront chargées après l'initialisation par createTestData()
        if (orderList != null && !orderList.isEmpty()) {
            updateStatus(orderList.size() + " commande(s) chargée(s)");
        } else {
            updateStatus("Chargement...");
        }
    }

    private HBox createDashboard() {
        HBox dashboard = new HBox(20);
        dashboard.setPadding(new Insets(10));
        dashboard.getStyleClass().add("threshold-dashboard");

        Label readyLabel = new Label("🎯 Seuils atteints: 1");
        Label pendingLabel = new Label("⏳ En attente: 2");
        Label savingsLabel = new Label("💰 Économies: 45 €");

        dashboard.getChildren().addAll(readyLabel, new Separator(),
                pendingLabel, new Separator(), savingsLabel);

        return dashboard;
    }

    private void createOrderTable() {
        orderTable = new TableView<>();
        orderTable.setItems(orderList);
        orderTable.setStyle("-fx-background-color: "
                + com.magscene.magsav.desktop.theme.ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1px; -fx-border-radius: 8px;");

        // Colonne Référence
        TableColumn<OrderData, String> refCol = new TableColumn<>("Référence");
        refCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        refCol.setPrefWidth(100);

        // Colonne Fournisseur
        TableColumn<OrderData, String> supplierCol = new TableColumn<>("Fournisseur");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierCol.setPrefWidth(150);

        // Colonne Montant
        TableColumn<OrderData, String> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        // Colonne Seuil
        TableColumn<OrderData, String> thresholdCol = new TableColumn<>("Seuil");
        thresholdCol.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        thresholdCol.setPrefWidth(120);

        // Colonne Statut
        TableColumn<OrderData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        // Colonne Date
        TableColumn<OrderData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        // Colonne Économies
        TableColumn<OrderData, String> savingsCol = new TableColumn<>("Économies");
        savingsCol.setCellValueFactory(new PropertyValueFactory<>("savings"));
        savingsCol.setPrefWidth(80);

        orderTable.getColumns().addAll(java.util.Arrays.asList(refCol, supplierCol, amountCol, thresholdCol,
                statusCol, dateCol, savingsCol));
    }

    private void loadOrdersFromBackend() {
        try {
            System.out.println("🔄 Tentative de chargement des commandes groupées depuis le backend...");
            java.util.List<java.util.Map<String, Object>> backendOrders = apiService.getAll("grouped-orders");

            if (backendOrders != null && !backendOrders.isEmpty()) {
                System.out.println("✅ Backend disponible - Chargement de " + backendOrders.size() + " commandes");
                orderList.clear();

                for (java.util.Map<String, Object> orderMap : backendOrders) {
                    String reference = (String) orderMap.getOrDefault("reference", "N/A");
                    String supplier = (String) orderMap.getOrDefault("supplierName", "N/A");
                    Object amountObj = orderMap.get("totalAmount");
                    String amount = amountObj != null ? String.format("%.2f €", ((Number) amountObj).doubleValue())
                            : "0.00 €";
                    String status = (String) orderMap.getOrDefault("status", "N/A");
                    String date = (String) orderMap.getOrDefault("orderDate", "N/A");

                    orderList.add(new OrderData(reference, supplier, amount, "✅ 100%", status, date, "0 €"));
                }
                Platform.runLater(() -> updateStatus(orderList.size() + " commande(s) chargée(s)"));
            } else {
                System.out.println("⚠️ Aucune commande dans le backend - Chargement données test");
                createTestData();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement commandes: " + e.getMessage());
            e.printStackTrace();
            createTestData();
        }
    }

    private void createTestData() {
        orderList.addAll(
                new OrderData("GC-001", "SonoMax Pro", "480.50 €", "✅ 96% (500€)", "⏳ À valider", "15/01", "20 €"),
                new OrderData("GC-002", "Éclairage Scène", "350.00 €", "✅ 117% (300€)", "✅ Validée", "14/01", "15 €"),
                new OrderData("GC-003", "TechService", "245.50 €", "⏳ 49% (500€)", "📧 Envoyée", "12/01", "10 €"));
        // Mettre à jour le statut après chargement
        Platform.runLater(() -> updateStatus(orderList.size() + " commande(s) chargée(s)"));
    }

    // Classe interne pour les données test
    public static class OrderData implements DetailPanelProvider {
        private String reference;
        private String supplier;
        private String amount;
        private String threshold;
        private String status;
        private String date;
        private String savings;

        public OrderData(String reference, String supplier, String amount, String threshold,
                String status, String date, String savings) {
            this.reference = reference;
            this.supplier = supplier;
            this.amount = amount;
            this.threshold = threshold;
            this.status = status;
            this.date = date;
            this.savings = savings;
        }

        // Getters pour PropertyValueFactory
        public String getReference() {
            return reference;
        }

        public String getSupplier() {
            return supplier;
        }

        public String getAmount() {
            return amount;
        }

        public String getThreshold() {
            return threshold;
        }

        public String getStatus() {
            return status;
        }

        public String getDate() {
            return date;
        }

        public String getSavings() {
            return savings;
        }

        // Implémentation de DetailPanelProvider
        @Override
        public String getDetailTitle() {
            return "Commande " + getReference();
        }

        @Override
        public String getDetailSubtitle() {
            return "Fournisseur: " + getSupplier();
        }

        @Override
        public Image getDetailImage() {
            return null;
        }

        @Override
        public String getQRCodeData() {
            return null; // Les commandes groupées n'ont pas de QR code
        }

        @Override
        public String getDetailId() {
            return getReference();
        }

        @Override
        public VBox getDetailInfoContent() {
            VBox content = new VBox(10);
            content.getChildren().addAll(
                    DetailPanel.createInfoRow("💰 Montant", getAmount()),
                    DetailPanel.createInfoRow("🎯 Seuil", getThreshold()),
                    DetailPanel.createInfoRow("📊 Statut", getStatus()),
                    DetailPanel.createInfoRow("📅 Date", getDate()),
                    DetailPanel.createInfoRow("💸 Économies", getSavings()));
            return content;
        }
    }

    // Méthodes abstraites du parent
    @Override
    protected void handleAdd() {
        updateStatus("Création d'une nouvelle commande groupée...");

        // Afficher le dialogue de création
        GroupedOrderDialog dialog = new GroupedOrderDialog(null);
        java.util.Map<String, Object> result = dialog.showAndWait();

        if (result != null) {
            // Ajouter la commande à la liste
            String supplier = (String) result.getOrDefault("supplierName", "N/A");
            String threshold = result.containsKey("threshold")
                    ? String.format("%.2f €", (Double) result.get("threshold"))
                    : "N/A";

            orderList.add(new OrderData(
                    "GC-" + (orderList.size() + 1),
                    supplier,
                    "0.00 €",
                    threshold,
                    "📝 Ouverte",
                    java.time.LocalDate.now().toString(),
                    "0 €"));

            updateStatus("Commande créée pour " + supplier);
        }
    }

    @Override
    protected void handleEdit() {
        OrderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune commande sélectionnée");
            return;
        }
        updateStatus("Consultation de " + selected.getReference());

        // Créer un Map avec les données pour le dialogue
        java.util.Map<String, Object> orderData = new java.util.HashMap<>();
        orderData.put("reference", selected.getReference());
        orderData.put("supplierName", selected.getSupplier());
        orderData.put("threshold", selected.getThreshold());

        GroupedOrderDialog dialog = new GroupedOrderDialog(orderData);
        java.util.Map<String, Object> result = dialog.showAndWait();

        if (result != null) {
            updateStatus("Commande modifiée");
        }
    }

    @Override
    protected void handleDelete() {
        OrderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucune commande sélectionnée");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la commande");
        confirm.setContentText("Confirmer la suppression de " + selected.getReference() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                orderList.remove(selected);
                updateStatus("Commande supprimée");
            }
        });
    }

    @Override
    public void refresh() {
        super.refresh();
        updateStatus("Données rafraîchies");
    }
}