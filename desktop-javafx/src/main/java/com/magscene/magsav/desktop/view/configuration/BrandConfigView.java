package com.magscene.magsav.desktop.view.configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue de configuration des marques dans le module Paramètres
 */
public class BrandConfigView extends BorderPane {

    private final ApiService apiService;
    private TableView<BrandItem> brandsTable;
    private ObservableList<BrandItem> brandsData;

    // Contrôles de recherche et filtres
    private TextField searchField;
    private ComboBox<String> statusFilter;

    // Contrôles d'édition
    private TextField nameField;
    private TextArea descriptionField;
    private TextField logoUrlField;
    private TextField countryField;
    private TextField websiteField;
    private CheckBox activeCheckBox;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private BrandItem currentEditingItem;

    public BrandConfigView(ApiService apiService) {
        this.apiService = apiService;
        this.brandsData = FXCollections.observableArrayList();

        initializeUI();
        loadBrandsData();
    }

    private void initializeUI() {
        setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");

        // Header
        VBox header = createHeader();
        setTop(header);

        // Content principal
        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));

        // Panneau gauche - Liste des marques
        VBox leftPanel = createBrandsList();
        leftPanel.setPrefWidth(500);

        // Panneau droite - Formulaire d'édition
        VBox rightPanel = createEditPanel();
        rightPanel.setPrefWidth(350);

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        setCenter(mainContent);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_SECONDARY + ";");

        Label titleLabel = new Label("🏷️ Gestion des Marques");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        Label subtitleLabel = new Label("Configuration centralisée des marques d'équipements et véhicules");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox createBrandsList() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                "-fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1; -fx-border-radius: 8;");

        // Toolbar avec recherche et filtres
        HBox toolbar = createToolbar();

        // Table des marques
        brandsTable = createBrandsTable();

        panel.getChildren().addAll(toolbar, brandsTable);
        VBox.setVgrow(brandsTable, Priority.ALWAYS);

        return panel;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));

        // Recherche
        Label searchLabel = new Label("🔍 Recherche :");
        searchField = new TextField();
        searchField.setPromptText("Nom ou description...");
        searchField.setPrefWidth(200);
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBrands());

        // Filtre statut
        Label statusLabel = new Label("Statut :");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Actives", "Inactives");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> filterBrands());

        // Bouton Nouvelle marque
        Button addButton = new Button("➕ Nouvelle Marque");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        addButton.setOnAction(e -> startNewBrand());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchLabel, searchField, statusLabel, statusFilter, spacer, addButton);
        return toolbar;
    }

    private TableView<BrandItem> createBrandsTable() {
        TableView<BrandItem> table = new TableView<>();
        table.setItems(brandsData);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                editBrand(newSelection);
            }
        });

        // Style de sélection uniforme
        table.setRowFactory(tv -> {
            TableRow<BrandItem> row = new TableRow<>();

            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    row.setStyle(
                            "-fx-background-color: "
                                    + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance()
                                            .getSelectionColor()
                                    + "; " +
                                    "-fx-text-fill: "
                                    + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance()
                                            .getSelectionTextColor()
                                    + "; " +
                                    "-fx-border-color: "
                                    + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance()
                                            .getSelectionBorderColor()
                                    + "; " +
                                    "-fx-border-width: 1px;");
                } else {
                    row.setStyle("");
                }
            };

            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            return row;
        });

        // Colonne Actif
        TableColumn<BrandItem, Boolean> activeCol = new TableColumn<>("Actif");
        activeCol.setCellValueFactory(data -> data.getValue().activeProperty());
        activeCol.setCellFactory(CheckBoxTableCell.forTableColumn(activeCol));
        activeCol.setEditable(true);
        activeCol.setPrefWidth(60);

        // Colonne Nom
        TableColumn<BrandItem, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        nameCol.setPrefWidth(150);

        // Colonne Description
        TableColumn<BrandItem, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        descriptionCol.setPrefWidth(200);

        // Colonne Pays
        TableColumn<BrandItem, String> countryCol = new TableColumn<>("Pays");
        countryCol.setCellValueFactory(data -> data.getValue().countryProperty());
        countryCol.setPrefWidth(100);

        table.getColumns().addAll(java.util.Arrays.asList(activeCol, nameCol, descriptionCol, countryCol));
        table.setEditable(true);

        return table;
    }

    private VBox createEditPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                "-fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1; -fx-border-radius: 8;");

        Label titleLabel = new Label("✏️ Édition de Marque");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Formulaire
        VBox form = new VBox(10);

        // Nom
        Label nameLabel = new Label("Nom * :");
        nameField = new TextField();
        nameField.setPromptText("Nom de la marque");

        // Description
        Label descLabel = new Label("Description :");
        descriptionField = new TextArea();
        descriptionField.setPromptText("Description de la marque");
        descriptionField.setPrefRowCount(3);

        // Logo URL
        Label logoLabel = new Label("URL Logo :");
        logoUrlField = new TextField();
        logoUrlField.setPromptText("https://...");

        // Pays
        Label countryLabel = new Label("Pays :");
        countryField = new TextField();
        countryField.setPromptText("Pays d'origine");

        // Site web
        Label websiteLabel = new Label("Site Web :");
        websiteField = new TextField();
        websiteField.setPromptText("https://...");

        // Actif
        activeCheckBox = new CheckBox("Marque active");
        activeCheckBox.setSelected(true);

        form.getChildren().addAll(
                nameLabel, nameField,
                descLabel, descriptionField,
                logoLabel, logoUrlField,
                countryLabel, countryField,
                websiteLabel, websiteField,
                activeCheckBox);

        // Boutons d'action
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        saveButton = new Button("💾 Enregistrer");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        saveButton.setOnAction(e -> saveBrand());

        cancelButton = new Button("❌ Annuler");
        cancelButton.setOnAction(e -> cancelEdit());

        deleteButton = new Button("🗑️ Supprimer");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        deleteButton.setOnAction(e -> deleteBrand());

        buttons.getChildren().addAll(saveButton, cancelButton, deleteButton);

        panel.getChildren().addAll(titleLabel, form, buttons);

        // Désactiver les boutons au début
        disableEditControls();

        return panel;
    }

    private void loadBrandsData() {
        try {
            List<Map<String, Object>> brands = apiService.getBrands();
            brandsData.clear();

            for (Map<String, Object> brandData : brands) {
                BrandItem item = new BrandItem();
                item.setId(((Number) brandData.get("id")).longValue());
                item.setName((String) brandData.get("name"));
                item.setDescription((String) brandData.get("description"));
                item.setLogoUrl((String) brandData.get("logoUrl"));
                item.setCountry((String) brandData.get("country"));
                item.setWebsite((String) brandData.get("website"));
                item.setActive((Boolean) brandData.getOrDefault("active", true));

                brandsData.add(item);
            }
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les marques : " + e.getMessage());
        }
    }

    private void filterBrands() {
        // Implémentation du filtrage sera ajoutée si nécessaire; // Pour l'instant, on
        // charge toutes les données
        loadBrandsData();
    }

    private void startNewBrand() {
        currentEditingItem = new BrandItem();
        clearForm();
        enableEditControls();
        nameField.requestFocus();
    }

    private void editBrand(BrandItem item) {
        currentEditingItem = item;
        populateForm(item);
        enableEditControls();
    }

    private void populateForm(BrandItem item) {
        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription());
        logoUrlField.setText(item.getLogoUrl());
        countryField.setText(item.getCountry());
        websiteField.setText(item.getWebsite());
        activeCheckBox.setSelected(item.getActive());
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        logoUrlField.clear();
        countryField.clear();
        websiteField.clear();
        activeCheckBox.setSelected(true);
    }

    private void enableEditControls() {
        nameField.setDisable(false);
        descriptionField.setDisable(false);
        logoUrlField.setDisable(false);
        countryField.setDisable(false);
        websiteField.setDisable(false);
        activeCheckBox.setDisable(false);
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(currentEditingItem.getId() == null);
    }

    private void disableEditControls() {
        nameField.setDisable(true);
        descriptionField.setDisable(true);
        logoUrlField.setDisable(true);
        countryField.setDisable(true);
        websiteField.setDisable(true);
        activeCheckBox.setDisable(true);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void saveBrand() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Erreur", "Le nom de la marque est obligatoire");
            return;
        }

        try {
            // Préparer les données
            currentEditingItem.setName(nameField.getText().trim());
            currentEditingItem.setDescription(descriptionField.getText().trim());
            currentEditingItem.setLogoUrl(logoUrlField.getText().trim());
            currentEditingItem.setCountry(countryField.getText().trim());
            currentEditingItem.setWebsite(websiteField.getText().trim());
            currentEditingItem.setActive(activeCheckBox.isSelected());

            // Appeler l'API
            if (currentEditingItem.getId() == null) {
                // Nouvelle marque
                apiService.createBrand(currentEditingItem.toMap());
            } else {
                // Mise à jour
                apiService.updateBrand(currentEditingItem.getId(), currentEditingItem.toMap());
            }

            // Recharger les données
            loadBrandsData();
            cancelEdit();
            showSuccess("Succès", "Marque enregistrée avec succès");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    private void deleteBrand() {
        if (currentEditingItem.getId() == null)
            return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la marque");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir supprimer la marque \"" + currentEditingItem.getName() + "\" ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                apiService.deleteBrand(currentEditingItem.getId());
                loadBrandsData();
                cancelEdit();
                showSuccess("Succès", "Marque supprimée avec succès");
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void cancelEdit() {
        currentEditingItem = null;
        clearForm();
        disableEditControls();
        brandsTable.getSelectionModel().clearSelection();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Classe interne pour représenter une marque dans la table
     */
    public static class BrandItem {
        private Long id;
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty description = new SimpleStringProperty();
        private final SimpleStringProperty logoUrl = new SimpleStringProperty();
        private final SimpleStringProperty country = new SimpleStringProperty();
        private final SimpleStringProperty website = new SimpleStringProperty();
        private final SimpleBooleanProperty active = new SimpleBooleanProperty();

        // Getters et Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public String getLogoUrl() {
            return logoUrl.get();
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl.set(logoUrl);
        }

        public SimpleStringProperty logoUrlProperty() {
            return logoUrl;
        }

        public String getCountry() {
            return country.get();
        }

        public void setCountry(String country) {
            this.country.set(country);
        }

        public SimpleStringProperty countryProperty() {
            return country;
        }

        public String getWebsite() {
            return website.get();
        }

        public void setWebsite(String website) {
            this.website.set(website);
        }

        public SimpleStringProperty websiteProperty() {
            return website;
        }

        public Boolean getActive() {
            return active.get();
        }

        public void setActive(Boolean active) {
            this.active.set(active);
        }

        public SimpleBooleanProperty activeProperty() {
            return active;
        }

        public Map<String, Object> toMap() {
            return Map.of(
                    "name", getName() != null ? getName() : "",
                    "description", getDescription() != null ? getDescription() : "",
                    "logoUrl", getLogoUrl() != null ? getLogoUrl() : "",
                    "country", getCountry() != null ? getCountry() : "",
                    "website", getWebsite() != null ? getWebsite() : "",
                    "active", getActive() != null ? getActive() : true);
        }
    }
}