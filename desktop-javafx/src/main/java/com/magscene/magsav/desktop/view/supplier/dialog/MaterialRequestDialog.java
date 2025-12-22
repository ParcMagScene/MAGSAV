package com.magscene.magsav.desktop.view.supplier.dialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Dialogue pour créer/éditer une demande de matériel
 * Interface utilisateur complète suivant les standards MAGSAV
 */
public class MaterialRequestDialog extends Dialog<MaterialRequestDialog.RequestResult> {

    // Contrôles principaux
    private TextField titleField;
    private TextArea descriptionArea;
    private ComboBox<String> priorityCombo;
    private ComboBox<String> categoryCombo;
    private DatePicker neededByPicker;
    private TextField budgetField;
    private TextArea justificationArea;

    // Table des items de matériel
    private TableView<MaterialItem> itemsTable;
    private ObservableList<MaterialItem> items;

    // Mode d'édition
    private final boolean editMode;
    private final RequestResult originalRequest;

    /**
     * Constructeur pour nouveau matériel request
     */
    public MaterialRequestDialog(Stage owner) {
        this(owner, null);
    }

    /**
     * Constructeur pour édition
     */
    public MaterialRequestDialog(Stage owner, RequestResult existingRequest) {
        this.editMode = existingRequest != null;
        this.originalRequest = existingRequest;

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle(editMode ? "Modifier la demande de matériel" : "Nouvelle demande de matériel");
        setResizable(true);

        // Configuration du dialogue
        setupUI();
        setupResultConverter();

        // Chargement des données existantes si mode édition
        if (editMode && originalRequest != null) {
            loadExistingData();
        }

        // Validation
        setupValidation();
    }

    private void setupUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(800);
        root.setPrefHeight(600);

        // En-tête avec informations générales
        root.getChildren().add(createHeaderSection());

        // Section items de matériel
        root.getChildren().add(createItemsSection());

        // Section justification et budget
        root.getChildren().add(createDetailsSection());

        // Boutons
        getDialogPane().setContent(new ScrollPane(root));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Mémoriser la taille et position
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "material-request-dialog");

        // Appliquer le thème unifié
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        getDialogPane().getStyleClass().add("material-request-dialog");
    }

    private VBox createHeaderSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");

        Label headerLabel = new Label("Informations générales");
        headerLabel.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Titre de la demande
        grid.add(new Label("Titre *:"), 0, 0);
        titleField = new TextField();
        titleField.setPromptText("Ex: Éclairage LED pour spectacle Mozart");
        titleField.setPrefWidth(300);
        grid.add(titleField, 1, 0);

        // Priorité
        grid.add(new Label("Priorité *:"), 2, 0);
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("URGENTE", "HAUTE", "NORMALE", "BASSE");
        priorityCombo.setValue("NORMALE");
        grid.add(priorityCombo, 3, 0);

        // Description
        grid.add(new Label("Description *:"), 0, 1);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez précisément le matériel requis et son utilisation...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        GridPane.setColumnSpan(descriptionArea, 2);
        grid.add(descriptionArea, 1, 1);

        // Catégorie et date limite
        grid.add(new Label("Catégorie:"), 0, 2);
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("ÉCLAIRAGE", "SONORISATION", "STRUCTURE", "ÉLECTRICITÉ", "SÉCURITÉ", "AUTRE");
        categoryCombo.setValue("AUTRE");
        grid.add(categoryCombo, 1, 2);

        grid.add(new Label("Requis pour le:"), 2, 2);
        neededByPicker = new DatePicker();
        neededByPicker.setValue(LocalDate.now().plusWeeks(2));
        grid.add(neededByPicker, 3, 2);

        section.getChildren().addAll(headerLabel, grid);
        return section;
    }

    private VBox createItemsSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label itemsLabel = new Label("Articles demandés");
        itemsLabel.getStyleClass().add("section-title");

        Button addItemBtn = new Button("+ Ajouter article");
        addItemBtn.getStyleClass().add("btn-primary");
        addItemBtn.setOnAction(e -> addNewItem());

        Button removeItemBtn = new Button("Supprimer");
        removeItemBtn.getStyleClass().add("btn-secondary");
        removeItemBtn.setOnAction(e -> removeSelectedItem());

        header.getChildren().addAll(itemsLabel, new Region(), addItemBtn, removeItemBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

        // Table des items
        setupItemsTable();

        section.getChildren().addAll(header, itemsTable);
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        return section;
    }

    private void setupItemsTable() {
        items = FXCollections.observableArrayList();
        itemsTable = new TableView<>(items);
        itemsTable.setPrefHeight(200);
        itemsTable.setEditable(true);

        // Colonnes
        TableColumn<MaterialItem, String> nameCol = new TableColumn<>("Désignation");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setPrefWidth(200);

        TableColumn<MaterialItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setPrefWidth(250);

        TableColumn<MaterialItem, Integer> qtyCol = new TableColumn<>("Quantité");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        qtyCol.setCellFactory(createIntegerCellFactory());
        qtyCol.setPrefWidth(80);

        TableColumn<MaterialItem, String> unitCol = new TableColumn<>("Unité");
        unitCol.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        unitCol.setCellFactory(createComboBoxCellFactory(
                Arrays.asList("pièce", "mètre", "kg", "litre", "lot", "jeu")));
        unitCol.setPrefWidth(80);

        TableColumn<MaterialItem, String> supplierCol = new TableColumn<>("Fournisseur suggéré");
        supplierCol.setCellValueFactory(cellData -> cellData.getValue().suggestedSupplierProperty());
        supplierCol.setCellFactory(TextFieldTableCell.forTableColumn());
        supplierCol.setPrefWidth(150);

        itemsTable.getColumns().addAll(nameCol, descCol, qtyCol, unitCol, supplierCol);

        // Ajouter un item par défaut
        addNewItem();
    }

    private VBox createDetailsSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("dialog-section");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Budget estimé
        grid.add(new Label("Budget estimé (€):"), 0, 0);
        budgetField = new TextField();
        budgetField.setPromptText("1500.00");
        budgetField.setPrefWidth(150);
        grid.add(budgetField, 1, 0);

        // Justification
        grid.add(new Label("Justification:"), 0, 1);
        justificationArea = new TextArea();
        justificationArea.setPromptText("Expliquez pourquoi cette demande est nécessaire...");
        justificationArea.setPrefRowCount(3);
        justificationArea.setPrefWidth(400);
        GridPane.setColumnSpan(justificationArea, 3);
        grid.add(justificationArea, 1, 1);

        section.getChildren().add(grid);
        return section;
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createRequestResult();
            }
            return null;
        });
    }

    private void setupValidation() {
        // Validation en temps réel
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        BooleanProperty validProperty = new SimpleBooleanProperty();
        validProperty.bind(
                titleField.textProperty().isEmpty().not()
                        .and(descriptionArea.textProperty().isEmpty().not())
                        .and(priorityCombo.valueProperty().isNotNull()));

        okButton.disableProperty().bind(validProperty.not());
    }

    private void loadExistingData() {
        if (originalRequest == null)
            return;

        titleField.setText(originalRequest.getTitle());
        descriptionArea.setText(originalRequest.getDescription());
        priorityCombo.setValue(originalRequest.getPriority());
        categoryCombo.setValue(originalRequest.getCategory());

        if (originalRequest.getNeededBy() != null) {
            neededByPicker.setValue(originalRequest.getNeededBy());
        }

        if (originalRequest.getBudget() != null) {
            budgetField.setText(originalRequest.getBudget().toString());
        }

        justificationArea.setText(originalRequest.getJustification());

        // Charger les items
        items.clear();
        if (originalRequest.getItems() != null) {
            for (MaterialItem item : originalRequest.getItems()) {
                items.add(new MaterialItem(item));
            }
        }
    }

    private RequestResult createRequestResult() {
        RequestResult result = new RequestResult();
        result.setTitle(titleField.getText().trim());
        result.setDescription(descriptionArea.getText().trim());
        result.setPriority(priorityCombo.getValue());
        result.setCategory(categoryCombo.getValue());
        result.setNeededBy(neededByPicker.getValue());
        result.setJustification(justificationArea.getText().trim());

        // Budget
        try {
            if (!budgetField.getText().trim().isEmpty()) {
                result.setBudget(Double.parseDouble(budgetField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            // Ignore, budget restera null
        }

        // Items
        result.setItems(new ArrayList<>(items));

        return result;
    }

    private void addNewItem() {
        MaterialItem newItem = new MaterialItem();
        newItem.setName("Nouvel article");
        newItem.setQuantity(1);
        newItem.setUnit("pièce");
        items.add(newItem);

        // Sélectionner et éditer la nouvelle ligne
        itemsTable.getSelectionModel().selectLast();
        itemsTable.edit(items.size() - 1, itemsTable.getColumns().get(0));
    }

    private void removeSelectedItem() {
        MaterialItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            items.remove(selected);
        }
    }

    // Factory methods pour les cellules éditables
    private Callback<TableColumn<MaterialItem, Integer>, TableCell<MaterialItem, Integer>> createIntegerCellFactory() {
        return column -> new TableCell<MaterialItem, Integer>() {
            private TextField textField;

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(item.toString());
                        }
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                createTextField();
                setGraphic(textField);
                setText(null);
                textField.selectAll();
                textField.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem().toString());
                setGraphic(null);
            }

            private void createTextField() {
                textField = new TextField(getItem() != null ? getItem().toString() : "1");
                textField.setOnAction(evt -> commitEdit(Integer.parseInt(textField.getText())));
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        try {
                            commitEdit(Integer.parseInt(textField.getText()));
                        } catch (NumberFormatException e) {
                            cancelEdit();
                        }
                    }
                });
            }
        };
    }

    private Callback<TableColumn<MaterialItem, String>, TableCell<MaterialItem, String>> createComboBoxCellFactory(
            List<String> options) {
        return column -> new TableCell<MaterialItem, String>() {
            private ComboBox<String> comboBox;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (comboBox != null) {
                            comboBox.setValue(item);
                        }
                        setGraphic(comboBox);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                createComboBox();
                setGraphic(comboBox);
                setText(null);
                comboBox.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            private void createComboBox() {
                comboBox = new ComboBox<>(FXCollections.observableArrayList(options));
                comboBox.setValue(getItem());
                comboBox.setOnAction(evt -> commitEdit(comboBox.getValue()));
                comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(comboBox.getValue());
                    }
                });
            }
        };
    }

    // Classes de données
    public static class RequestResult {
        private String title;
        private String description;
        private String priority;
        private String category;
        private LocalDate neededBy;
        private Double budget;
        private String justification;
        private List<MaterialItem> items;

        // Constructeurs
        public RequestResult() {
            this.items = new ArrayList<>();
        }

        // Getters et setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public LocalDate getNeededBy() {
            return neededBy;
        }

        public void setNeededBy(LocalDate neededBy) {
            this.neededBy = neededBy;
        }

        public Double getBudget() {
            return budget;
        }

        public void setBudget(Double budget) {
            this.budget = budget;
        }

        public String getJustification() {
            return justification;
        }

        public void setJustification(String justification) {
            this.justification = justification;
        }

        public List<MaterialItem> getItems() {
            return items;
        }

        public void setItems(List<MaterialItem> items) {
            this.items = items;
        }
    }

    public static class MaterialItem {
        private StringProperty name = new SimpleStringProperty();
        private StringProperty description = new SimpleStringProperty();
        private IntegerProperty quantity = new SimpleIntegerProperty();
        private StringProperty unit = new SimpleStringProperty();
        private StringProperty suggestedSupplier = new SimpleStringProperty();

        public MaterialItem() {
        }

        public MaterialItem(MaterialItem other) {
            this.name.set(other.getName());
            this.description.set(other.getDescription());
            this.quantity.set(other.getQuantity());
            this.unit.set(other.getUnit());
            this.suggestedSupplier.set(other.getSuggestedSupplier());
        }

        // Properties
        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty descriptionProperty() {
            return description;
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public StringProperty unitProperty() {
            return unit;
        }

        public StringProperty suggestedSupplierProperty() {
            return suggestedSupplier;
        }

        // Getters et setters
        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }

        public String getUnit() {
            return unit.get();
        }

        public void setUnit(String unit) {
            this.unit.set(unit);
        }

        public String getSuggestedSupplier() {
            return suggestedSupplier.get();
        }

        public void setSuggestedSupplier(String suggestedSupplier) {
            this.suggestedSupplier.set(suggestedSupplier);
        }
    }
}