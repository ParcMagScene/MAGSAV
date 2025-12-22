package com.magscene.magsav.desktop.view;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.core.navigation.SelectableView;
import com.magscene.magsav.desktop.dialog.PersonnelDetailDialog;
import com.magscene.magsav.desktop.dialog.PersonnelDialog;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.util.DialogUtils;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Interface JavaFX complete pour la gestion du personnel
 * Fonctionnalites : tableau detaille, recherche, filtres, CRUD, statistiques
 * Implémente SelectableView pour la sélection depuis la recherche globale
 */
public class PersonnelManagerView extends BorderPane implements SelectableView {

    private final ApiService apiService;
    private TableView<PersonnelItem> personnelTable;
    private ObservableList<PersonnelItem> personnelData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> departmentFilter;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;
    private Button editButton;
    private Button deleteButton;

    public PersonnelManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.personnelData = FXCollections.observableArrayList();
        initializeUI();
        loadPersonnelData();
    }

    private void initializeUI() {
        // Layout uniforme comme Ventes et Installations - utilise ThemeConstants
        setPadding(ThemeConstants.PADDING_STANDARD);
        this.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + ";");

        // Barre d'outils avec recherche et filtres - directement sans header
        HBox toolbar = createToolbar();

        // Zone principale avec tableau - EXACTEMENT comme référence
        createTableContainer();

        // Toolbar directement en haut - pas de marges supplémentaires
        setTop(toolbar);

        // Intégration du volet de visualisation pour le personnel
        DetailPanelContainer detailContainer = new DetailPanelContainer(personnelTable);
        setCenter(detailContainer);

        // Configuration finale après création de tous les composants
        setupButtonActivation();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(ThemeConstants.SPACING_MD);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(ThemeConstants.TOOLBAR_PADDING);
        toolbar.getStyleClass().add(ThemeConstants.UNIFIED_TOOLBAR_CLASS);

        // 🔍 Recherche avec ViewUtils
        Label searchLabel = ViewUtils.createSearchLabel("🔍 Recherche");
        searchField = ViewUtils.createSearchField("Nom, prénom, email...", text -> filterPersonnelData());
        VBox searchBox = new VBox(5, searchLabel, searchField);

        // 👤 Filtre type avec ViewUtils - on doit garder la référence au ComboBox
        Label typeLabel = new Label("👤 Type");
        typeLabel.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.SECONDARY_LABEL_STYLE);
        typeLabel.setFont(javafx.scene.text.Font.font(com.magscene.magsav.desktop.theme.ThemeConstants.FONT_FAMILY,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_WEIGHT_TITLE,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_SIZE_NORMAL));
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Employé", "Freelance", "Stagiaire", "Intérimaire",
                "Intermittent du spectacle");
        typeFilter.setValue("Tous");
        typeFilter.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.INPUT_FIELD_STYLE);
        com.magscene.magsav.desktop.util.ResponsiveUtils.makeComboResponsive(typeFilter);
        typeFilter.setOnAction(e -> filterPersonnelData());
        VBox typeBox = new VBox(5, typeLabel, typeFilter);

        // 📊 Filtre statut avec ViewUtils
        Label statusLabel = new Label("📊 Statut");
        statusLabel.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.SECONDARY_LABEL_STYLE);
        statusLabel.setFont(javafx.scene.text.Font.font(com.magscene.magsav.desktop.theme.ThemeConstants.FONT_FAMILY,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_WEIGHT_TITLE,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_SIZE_NORMAL));
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Actif", "Inactif", "En congé", "Terminé");
        statusFilter.setValue("Tous");
        statusFilter.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.INPUT_FIELD_STYLE);
        com.magscene.magsav.desktop.util.ResponsiveUtils.makeComboResponsive(statusFilter);
        statusFilter.setOnAction(e -> filterPersonnelData());
        VBox statusBox = new VBox(5, statusLabel, statusFilter);

        // 🏢 Filtre département avec ViewUtils
        Label deptLabel = new Label("🏢 Département");
        deptLabel.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.SECONDARY_LABEL_STYLE);
        deptLabel.setFont(javafx.scene.text.Font.font(com.magscene.magsav.desktop.theme.ThemeConstants.FONT_FAMILY,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_WEIGHT_TITLE,
                com.magscene.magsav.desktop.theme.ThemeConstants.FONT_SIZE_NORMAL));
        departmentFilter = new ComboBox<>();
        departmentFilter.getItems().add("Tous");
        departmentFilter.setValue("Tous");
        departmentFilter.setStyle(com.magscene.magsav.desktop.theme.ThemeConstants.INPUT_FIELD_STYLE);
        com.magscene.magsav.desktop.util.ResponsiveUtils.makeComboResponsive(departmentFilter);
        departmentFilter.setOnAction(e -> filterPersonnelData());
        VBox deptBox = new VBox(5, deptLabel, departmentFilter);

        // ⚡ Boutons d'action avec ViewUtils
        Button addButton = ViewUtils.createAddButton("➕ Ajouter", this::createNewPersonnel);

        editButton = new Button("✏️ Modifier");
        editButton.setDisable(true);
        editButton.setOnAction(e -> {
            PersonnelItem selected = personnelTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editPersonnel(selected);
            } else {
                showAlert("Selection requise", "Veuillez selectionner un membre du personnel a modifier.");
            }
        });

        deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> {
            PersonnelItem selected = personnelTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deletePersonnel(selected);
            } else {
                showAlert("Selection requise", "Veuillez selectionner un membre du personnel a supprimer.");
            }
        });

        VBox actionsBox = ViewUtils.createActionsBox("⚡ Actions", addButton, editButton, deleteButton);

        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchBox, typeBox, statusBox, deptBox, spacer, actionsBox);
        return toolbar;
    }

    private void createTableContainer() {
        // Indicateur de chargement
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(50, 50);

        // Configuration du tableau
        personnelTable = new TableView<>();
        personnelTable.setItems(personnelData);
        personnelTable.setRowFactory(tv -> {
            TableRow<PersonnelItem> row = new TableRow<>();

            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance().getSelectionColor()
                            + "; " +
                            "-fx-text-fill: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance()
                                    .getSelectionTextColor()
                            + "; " +
                            "-fx-border-color: "
                            + com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance()
                                    .getSelectionBorderColor()
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
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editPersonnel(row.getItem());
                }
            });
            return row;
        });

        createTableColumns();

        // Style du tableau uniforme avec bordures comme Clients
        personnelTable.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor()
                + "; -fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1px; -fx-border-radius: 8px;");
        personnelTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        personnelTable.setPrefHeight(400);

        // Plus besoin de container - tableau directement dans setCenter
    }

    private void setupButtonActivation() {
        // Activation des boutons Modifier et Supprimer basée sur la sélection
        personnelTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = newSelection != null;
            editButton.setDisable(!itemSelected);
            deleteButton.setDisable(!itemSelected);
        });
    }

    private void createTableColumns() {
        // Colonne ID
        TableColumn<PersonnelItem, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        // Colonne Nom complet
        TableColumn<PersonnelItem, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);

        // Colonne Email
        TableColumn<PersonnelItem, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        // Colonne Telephone
        TableColumn<PersonnelItem, String> phoneCol = new TableColumn<>("Telephone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);

        // Colonne Type
        TableColumn<PersonnelItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        typeCol.setCellFactory(column -> new TableCell<PersonnelItem, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(type);
                    // Couleurs selon le type
                    switch (type) {
                        case "Employe":
                            // Style gere par CSS
                            break;
                        case "Freelance":
                            // Style gere par CSS
                            break;
                        case "Stagiaire":
                            // Style gere par CSS
                            break;
                        case "Interimaire":
                            // Style gere par CSS
                            break;
                    }
                }
            }
        });

        // Colonne Statut
        TableColumn<PersonnelItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<PersonnelItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(status);
                    // Couleurs selon le statut
                    switch (status) {
                        case "Actif":
                            // Style gere par CSS
                            break;
                        case "Inactif":
                            // Style gere par CSS
                            break;
                        case "En conge":
                            // Style gere par CSS
                            break;
                        case "Termine":
                            // Style gere par CSS
                            break;
                    }
                }
            }
        });

        // Colonne Poste
        TableColumn<PersonnelItem, String> jobCol = new TableColumn<>("Poste");
        jobCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        jobCol.setPrefWidth(150);

        // Colonne Departement
        TableColumn<PersonnelItem, String> deptCol = new TableColumn<>("Departement");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        deptCol.setPrefWidth(120);

        // Colonne Date d'embauche
        TableColumn<PersonnelItem, String> hireDateCol = new TableColumn<>("Embauche");
        hireDateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        hireDateCol.setPrefWidth(100);

        // Colonne Spécialités
        TableColumn<PersonnelItem, String> specialtiesCol = new TableColumn<>("Spécialités");
        specialtiesCol.setCellValueFactory(new PropertyValueFactory<>("specialties"));
        specialtiesCol.setPrefWidth(150);

        personnelTable.getColumns().addAll(java.util.Arrays.asList(
                idCol, nameCol, emailCol, phoneCol,
                typeCol, statusCol, jobCol, deptCol, hireDateCol, specialtiesCol));
    }

    // Méthode createFooter() supprimée - Les boutons sont maintenant; // intégrés
    // dans la toolbar unifiée pour éviter les doublons

    private void loadPersonnelData() {
        loadingIndicator.setVisible(true);
        personnelTable.setDisable(true);

        CompletableFuture<List<Object>> future = apiService.getAllPersonnel();
        future.thenAccept(personnelList -> {
            Platform.runLater(() -> {
                personnelData.clear();

                for (Object item : personnelList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> personnelMap = (Map<String, Object>) item;
                        PersonnelItem personnelItem = createPersonnelItem(personnelMap);
                        personnelData.add(personnelItem);
                    }
                }

                updateDepartmentFilter();
                updateStatistics();
                loadingIndicator.setVisible(false);
                personnelTable.setDisable(false);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                personnelTable.setDisable(false);
                showAlert("Erreur de chargement",
                        "Impossible de charger les donnees du personnel : " + throwable.getMessage());
            });
            return null;
        });
    }

    private PersonnelItem createPersonnelItem(Map<String, Object> personnelMap) {
        PersonnelItem item = new PersonnelItem();

        item.setId(String.valueOf(personnelMap.get("id")));
        item.setFirstName(getStringValue(personnelMap, "firstName"));
        item.setLastName(getStringValue(personnelMap, "lastName"));
        item.setFullName(item.getFirstName() + " " + item.getLastName());
        item.setEmail(getStringValue(personnelMap, "email"));
        item.setPhone(getStringValue(personnelMap, "phone"));

        // Gestion des enumerations
        String type = getStringValue(personnelMap, "type");
        item.setType(convertTypeToDisplay(type));

        String status = getStringValue(personnelMap, "status");
        item.setStatus(convertStatusToDisplay(status));

        item.setJobTitle(getStringValue(personnelMap, "jobTitle"));
        item.setDepartment(getStringValue(personnelMap, "department"));

        // Date d'embauche
        String hireDate = getStringValue(personnelMap, "hireDate");
        if (hireDate != null && !hireDate.isEmpty()) {
            item.setHireDate(hireDate);
        }

        item.setNotes(getStringValue(personnelMap, "notes"));
        item.setSpecialties(getStringValue(personnelMap, "specialties"));

        // Stockage de la map originale pour les operations
        item.setOriginalData(personnelMap);

        return item;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private String convertTypeToDisplay(String type) {
        if (type == null)
            return "";
        switch (type.toUpperCase()) {
            case "EMPLOYEE":
                return "Employe";
            case "FREELANCE":
                return "Freelance";
            case "INTERN":
                return "Stagiaire";
            case "TEMPORARY":
                return "Interimaire";
            case "PERFORMER":
                return "Intermittent du spectacle";
            default:
                return type;
        }
    }

    private String convertStatusToDisplay(String status) {
        if (status == null)
            return "";
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "Actif";
            case "INACTIVE":
                return "Inactif";
            case "ON_LEAVE":
                return "En conge";
            case "TERMINATED":
                return "Termine";
            default:
                return status;
        }
    }

    private void updateDepartmentFilter() {
        // Recuperer la liste des departements uniques
        departmentFilter.getItems().clear();
        departmentFilter.getItems().add("Tous");

        personnelData.stream()
                .map(PersonnelItem::getDepartment)
                .filter(dept -> dept != null && !dept.trim().isEmpty())
                .distinct()
                .sorted()
                .forEach(dept -> departmentFilter.getItems().add(dept));

        if (!departmentFilter.getItems().contains(departmentFilter.getValue())) {
            departmentFilter.setValue("Tous");
        }
    }

    private void updateStatistics() {
        // Statistiques supprimées du header pour uniformiser l'affichage; // avec les
        // autres modules après unification des toolbars
    }

    private void filterPersonnelData() {
        String searchText = searchField.getText().toLowerCase().trim();
        String typeValue = typeFilter.getValue();
        String statusValue = statusFilter.getValue();
        String departmentValue = departmentFilter.getValue();

        ObservableList<PersonnelItem> filteredData = FXCollections.observableArrayList();

        for (PersonnelItem item : personnelData) {
            // Filtre de recherche
            boolean matchesSearch = searchText.isEmpty() ||
                    (item.getFullName() != null && item.getFullName().toLowerCase().contains(searchText)) ||
                    (item.getEmail() != null && item.getEmail().toLowerCase().contains(searchText)) ||
                    (item.getPhone() != null && item.getPhone().toLowerCase().contains(searchText)) ||
                    (item.getSpecialties() != null && item.getSpecialties().toLowerCase().contains(searchText));

            // Filtre par type
            boolean matchesType = "Tous".equals(typeValue) ||
                    (item.getType() != null && item.getType().equals(typeValue));

            // Filtre par statut
            boolean matchesStatus = "Tous".equals(statusValue) ||
                    (item.getStatus() != null && item.getStatus().equals(statusValue));

            // Filtre par département
            boolean matchesDepartment = "Tous".equals(departmentValue) ||
                    (item.getDepartment() != null && item.getDepartment().equals(departmentValue));

            if (matchesSearch && matchesType && matchesStatus && matchesDepartment) {
                filteredData.add(item);
            }
        }

        personnelTable.setItems(filteredData);
        // Mise à jour des statistiques si nécessaire
        if (statsLabel != null) {
            statsLabel.setText("Affichage: " + filteredData.size() + " / " + personnelData.size());
        }
    }

    private void createNewPersonnel() {
        PersonnelDialog dialog = new PersonnelDialog(null, apiService);
        Optional<Map<String, Object>> result = dialog.showAndWait();

        result.ifPresent(newPersonnelData -> {
            // Sauvegarder dans le backend puis recharger
            apiService.createPersonnel(newPersonnelData)
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Succès", "Personnel créé avec succès.");
                    loadPersonnelData();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("Erreur", "Erreur lors de la création : " + throwable.getMessage()));
                    return null;
                });
        });
    }

    private void editPersonnel(PersonnelItem personnel) {
        // Ouvrir le dialogue en mode lecture seule (comme pour les équipements)
        PersonnelDetailDialog dialog = new PersonnelDetailDialog(apiService, personnel.getOriginalData());
        Optional<Map<String, Object>> result = dialog.showAndWait();

        result.ifPresent(updatedData -> {
            // Sauvegarder dans le backend puis recharger
            Long personnelId = Long.valueOf(personnel.getId());
            apiService.updatePersonnel(personnelId, updatedData)
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Succès", "Personnel mis à jour avec succès.");
                    loadPersonnelData();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert("Erreur", "Erreur lors de la mise à jour : " + throwable.getMessage()));
                    return null;
                });
        });
    }

    private void deletePersonnel(PersonnelItem personnel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le membre du personnel");
        alert.setContentText("Etes-vous sur de vouloir supprimer " + personnel.getFullName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long personnelId = Long.valueOf(personnel.getId());

            CompletableFuture<Void> future = apiService.deletePersonnel(personnelId);
            future.thenRun(() -> Platform.runLater(() -> {
                showAlert("Succes", "Membre du personnel supprime avec succes.");
                loadPersonnelData();
            })).exceptionally(throwable -> {
                Platform.runLater(
                        () -> showAlert("Erreur", "Erreur lors de la suppression : " + throwable.getMessage()));
                return null;
            });
        }
    }

    private void showAlert(String title, String message) {
        DialogUtils.showInfo(title, message);
    }

    // Classe interne pour representer un element du personnel dans le tableau
    public static class PersonnelItem implements DetailPanelProvider {
        private SimpleStringProperty id = new SimpleStringProperty();
        private SimpleStringProperty firstName = new SimpleStringProperty();
        private SimpleStringProperty lastName = new SimpleStringProperty();
        private SimpleStringProperty fullName = new SimpleStringProperty();
        private SimpleStringProperty email = new SimpleStringProperty();
        private SimpleStringProperty phone = new SimpleStringProperty();
        private SimpleStringProperty type = new SimpleStringProperty();
        private SimpleStringProperty status = new SimpleStringProperty();
        private SimpleStringProperty jobTitle = new SimpleStringProperty();
        private SimpleStringProperty department = new SimpleStringProperty();
        private SimpleStringProperty hireDate = new SimpleStringProperty();
        private SimpleStringProperty notes = new SimpleStringProperty();
        private SimpleStringProperty specialties = new SimpleStringProperty();
        private Map<String, Object> originalData;

        // Getters et setters
        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public String getFullName() {
            return fullName.get();
        }

        public void setFullName(String fullName) {
            this.fullName.set(fullName);
        }

        public String getEmail() {
            return email.get();
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public String getPhone() {
            return phone.get();
        }

        public void setPhone(String phone) {
            this.phone.set(phone);
        }

        public String getType() {
            return type.get();
        }

        public void setType(String type) {
            this.type.set(type);
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public String getJobTitle() {
            return jobTitle.get();
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle.set(jobTitle);
        }

        public String getDepartment() {
            return department.get();
        }

        public void setDepartment(String department) {
            this.department.set(department);
        }

        public String getHireDate() {
            return hireDate.get();
        }

        public void setHireDate(String hireDate) {
            this.hireDate.set(hireDate);
        }

        public String getNotes() {
            return notes.get();
        }

        public void setNotes(String notes) {
            this.notes.set(notes);
        }

        public String getSpecialties() {
            return specialties.get();
        }

        public void setSpecialties(String specialties) {
            this.specialties.set(specialties);
        }

        public Map<String, Object> getOriginalData() {
            return originalData;
        }

        public void setOriginalData(Map<String, Object> originalData) {
            this.originalData = originalData;
        }

        // Implémentation de DetailPanelProvider
        @Override
        public String getDetailTitle() {
            return getFullName() != null && !getFullName().isEmpty() ? getFullName() : "Personnel sans nom";
        }

        @Override
        public String getDetailSubtitle() {
            StringBuilder subtitle = new StringBuilder();
            if (getJobTitle() != null && !getJobTitle().isEmpty()) {
                subtitle.append(getJobTitle());
            }

            if (getDepartment() != null && !getDepartment().isEmpty()) {
                if (subtitle.length() > 0) {
                    subtitle.append(" • ");
                }
                subtitle.append(getDepartment());
            }

            if (getType() != null && !getType().isEmpty()) {
                if (subtitle.length() > 0) {
                    subtitle.append(" • ");
                }
                subtitle.append(getType());
            }

            return subtitle.toString();
        }

        @Override
        public Image getDetailImage() {
            // Avatar selon le type de personnel
            String avatarType = "default";

            // Détermine le type d'avatar selon la fonction/département
            if (getJobTitle() != null) {
                String jobTitle = getJobTitle().toLowerCase();
                if (jobTitle.contains("technicien") || jobTitle.contains("ingénieur")) {
                    avatarType = "technician";
                } else if (jobTitle.contains("manager") || jobTitle.contains("responsable")
                        || jobTitle.contains("chef")) {
                    avatarType = "manager";
                } else if (jobTitle.contains("commercial") || jobTitle.contains("vente")) {
                    avatarType = "sales";
                } else if (jobTitle.contains("admin") || jobTitle.contains("secrétaire")) {
                    avatarType = "admin";
                }
            }

            try {
                return new Image(getClass().getResourceAsStream("/images/avatars/" + avatarType + ".png"));
            } catch (Exception e) {
                try {
                    return new Image(getClass().getResourceAsStream("/images/avatars/default.png"));
                } catch (Exception ex) {
                    return null;
                }
            }
        }

        @Override
        public String getQRCodeData() {
            return ""; // Pas de QR code pour le personnel
        }

        @Override
        public VBox getDetailInfoContent() {
            VBox content = new VBox(8);

            if (getEmail() != null && !getEmail().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Email", getEmail()));
            }

            if (getPhone() != null && !getPhone().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Téléphone", getPhone()));
            }

            if (getStatus() != null && !getStatus().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Statut", getStatus()));
            }

            if (getHireDate() != null && !getHireDate().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Date d'embauche", getHireDate()));
            }

            if (getSpecialties() != null && !getSpecialties().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Spécialités", getSpecialties()));
            }

            if (getNotes() != null && !getNotes().trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Notes", getNotes()));
            }

            return content;
        }

        @Override
        public String getDetailId() {
            return getId() != null ? getId() : "";
        }
    }

    /**
     * Méthode pour sélectionner et afficher un personnel par nom (utilisée par la
     * recherche globale)
     */
    public void selectAndViewPersonnel(String personnelName) {
        if (personnelName == null || personnelName.trim().isEmpty()) {
            return;
        }

        // Rechercher dans la liste
        for (PersonnelItem item : personnelData) {
            if (item.getFullName().toLowerCase().contains(personnelName.toLowerCase())) {
                // Sélectionner et faire défiler vers l'élément
                personnelTable.getSelectionModel().select(item);
                personnelTable.scrollTo(item);

                // Mettre à jour le filtre de recherche pour montrer le contexte
                if (searchField != null) {
                    searchField.setText(personnelName);
                }

                break;
            }
        }
    }
    
    // ===== Implémentation SelectableView =====
    
    @Override
    public boolean selectById(String id) {
        if (id == null || id.isEmpty() || personnelData == null) {
            return false;
        }
        
        // Réinitialiser les filtres
        if (searchField != null) searchField.clear();
        if (typeFilter != null) typeFilter.getSelectionModel().selectFirst();
        if (statusFilter != null) statusFilter.getSelectionModel().selectFirst();
        if (departmentFilter != null) departmentFilter.getSelectionModel().selectFirst();
        
        for (PersonnelItem personnel : personnelData) {
            if (id.equals(String.valueOf(personnel.getId()))) {
                Platform.runLater(() -> {
                    personnelTable.getSelectionModel().select(personnel);
                    personnelTable.scrollTo(personnel);
                    System.out.println("✅ Personnel sélectionné: " + personnel.getFullName() + " (ID: " + id + ")");
                });
                return true;
            }
        }
        
        System.out.println("⚠️ Personnel non trouvé avec ID: " + id);
        return false;
    }
    
    @Override
    public String getViewName() {
        return "Personnel";
    }

}
