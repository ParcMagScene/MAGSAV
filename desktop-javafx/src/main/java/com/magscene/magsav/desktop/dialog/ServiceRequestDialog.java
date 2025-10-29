package com.magscene.magsav.desktop.dialog;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;
import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.magscene.magsav.desktop.model.ServiceRequest;
import com.magscene.magsav.desktop.model.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.desktop.model.ServiceRequest.ServiceRequestType;
import com.magscene.magsav.desktop.model.Equipment;
import com.magscene.magsav.desktop.service.ApiService;

public class ServiceRequestDialog {
    private final Stage dialog;
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final ComboBox<String> typeCombo;
    private final ComboBox<String> statusCombo;
    private final ComboBox<String> priorityCombo;
    private final ComboBox<Map<String, Object>> requesterCombo;
    private final ComboBox<Map<String, Object>> technicianCombo;
    private final DatePicker scheduledDatePicker;
    private final TextArea notesArea;
    private final ComboBox<Map<String, Object>> equipmentCombo;
    private final TextField locationField;
    private final TextField contactInfoField;
    private final TextField costField;
    
    private ServiceRequest result;
    private boolean isEdit;
    private final ApiService apiService;

    public ServiceRequestDialog(ServiceRequest serviceRequest) {
        this.isEdit = serviceRequest != null;
        this.apiService = new ApiService();
        this.dialog = new Stage();
        
        // Configuration de base du dialogue
        dialog.setTitle(isEdit ? "Modifier la demande SAV" : "Nouvelle demande SAV");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        
        // CrÃƒÂ©ation des champs
        titleField = new TextField();
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        
        // ComboBoxes avec les valeurs d'ÃƒÂ©numÃƒÂ©ration
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "RÃƒÂ©paration", "Maintenance", "Installation", "Formation", "RMA", "Garantie"
        ));
        
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Ouverte", "En cours", "En attente de piÃƒÂ¨ces", "RÃƒÂ©solue", "FermÃƒÂ©e", "AnnulÃƒÂ©e"
        ));
        
        priorityCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Faible", "Moyenne", "Ãƒâ€°levÃƒÂ©e", "Urgente"
        ));
        
        // ComboBoxes pour la sÃƒÂ©lection depuis la DB
        requesterCombo = new ComboBox<>();
        requesterCombo.setPrefWidth(350);
        requesterCombo.setButtonCell(createPersonnelListCell());
        requesterCombo.setCellFactory(lv -> createPersonnelListCell());
        
        technicianCombo = new ComboBox<>();  
        technicianCombo.setPrefWidth(350);
        technicianCombo.setButtonCell(createPersonnelListCell());
        technicianCombo.setCellFactory(lv -> createPersonnelListCell());
        
        equipmentCombo = new ComboBox<>();
        equipmentCombo.setPrefWidth(350);
        equipmentCombo.setButtonCell(createEquipmentListCell());
        equipmentCombo.setCellFactory(lv -> createEquipmentListCell());
        
        scheduledDatePicker = new DatePicker();
        notesArea = new TextArea();
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);
        
        locationField = new TextField();
        contactInfoField = new TextField();
        costField = new TextField();
        
        // Chargement des donnÃƒÂ©es depuis la DB
        loadPersonnelData();
        loadEquipmentData();
        
        // PrÃƒÂ©-remplir les champs si c'est une ÃƒÂ©dition
        if (isEdit) {
            populateFields(serviceRequest);
        } else {
            // Valeurs par dÃƒÂ©faut pour une nouvelle demande
            statusCombo.setValue("Ouverte");
            priorityCombo.setValue("Moyenne");
            scheduledDatePicker.setValue(LocalDate.now());
        }
        
        // CrÃƒÂ©ation du layout
        VBox mainLayout = createLayout();
        
        // Configuration de la scÃƒÂ¨ne
        Scene scene = new Scene(mainLayout, 650, 750);
        dialog.setScene(scene);
    }
    
    private VBox createLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        // Titre du dialogue
        Label titleLabel = new Label(isEdit ? "Modifier la demande SAV" : "Nouvelle demande SAV");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Informations principales
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        
        int row = 0;
        mainGrid.add(new Label("Titre *:"), 0, row);
        mainGrid.add(titleField, 1, row++);
        
        mainGrid.add(new Label("Type *:"), 0, row);
        mainGrid.add(typeCombo, 1, row++);
        
        mainGrid.add(new Label("Statut *:"), 0, row);
        mainGrid.add(statusCombo, 1, row++);
        
        mainGrid.add(new Label("PrioritÃƒÂ© *:"), 0, row);
        mainGrid.add(priorityCombo, 1, row++);
        
        mainGrid.add(new Label("Demandeur *:"), 0, row);
        mainGrid.add(requesterCombo, 1, row++);
        
        mainGrid.add(new Label("Technicien:"), 0, row);
        mainGrid.add(technicianCombo, 1, row++);
        
        mainGrid.add(new Label("Date prÃƒÂ©vue:"), 0, row);
        mainGrid.add(scheduledDatePicker, 1, row++);
        
        mainGrid.add(new Label("Ãƒâ€°quipement:"), 0, row);
        mainGrid.add(equipmentCombo, 1, row++);
        
        mainGrid.add(new Label("Lieu:"), 0, row);
        mainGrid.add(locationField, 1, row++);
        
        mainGrid.add(new Label("Contact:"), 0, row);
        mainGrid.add(contactInfoField, 1, row++);
        
        mainGrid.add(new Label("CoÃƒÂ»t estimÃƒÂ©:"), 0, row);
        mainGrid.add(costField, 1, row++);
        
        // Configuration des colonnes
        mainGrid.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints(120));
        mainGrid.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints(400));
        
        // Description
        VBox descBox = new VBox(5);
        descBox.getChildren().addAll(
            new Label("Description:"),
            descriptionArea
        );
        
        // Notes
        VBox notesBox = new VBox(5);
        notesBox.getChildren().addAll(
            new Label("Notes techniques:"),
            notesArea
        );
        
        // Boutons
        HBox buttonBox = createButtonBox();
        
        layout.getChildren().addAll(titleLabel, mainGrid, descBox, notesBox, buttonBox);
        return layout;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            result = null;
            dialog.close();
        });
        
        Button saveButton = new Button(isEdit ? "Modifier" : "CrÃƒÂ©er");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> {
            if (validateFields()) {
                result = createServiceRequestFromFields();
                dialog.close();
            }
        });
        
        // Style des boutons
        cancelButton.setPrefWidth(80);
        saveButton.setPrefWidth(80);
        saveButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }
    
    private void populateFields(ServiceRequest serviceRequest) {
        titleField.setText(serviceRequest.getTitle());
        descriptionArea.setText(serviceRequest.getDescription() != null ? serviceRequest.getDescription() : "");
        
        // Mapping des ÃƒÂ©numÃƒÂ©rations vers les valeurs d'affichage
        typeCombo.setValue(mapTypeEnumToDisplay(serviceRequest.getType()));
        statusCombo.setValue(mapStatusEnumToDisplay(serviceRequest.getStatus()));
        priorityCombo.setValue(mapPriorityEnumToDisplay(serviceRequest.getPriority()));
        
        // TODO: SÃƒÂ©lectionner le demandeur dans la ComboBox basÃƒÂ© sur le nom
        // TODO: SÃƒÂ©lectionner le technicien dans la ComboBox basÃƒÂ© sur le nom
        
        // Le ServiceRequest n'a pas de scheduledDate, on utilise la date de crÃƒÂ©ation par dÃƒÂ©faut
        scheduledDatePicker.setValue(LocalDate.now());
        
        notesArea.setText(serviceRequest.getResolutionNotes() != null ? serviceRequest.getResolutionNotes() : "");
        
        // Ces champs ne sont pas dans le modÃƒÂ¨le actuel, on les laisse vides
        // TODO: SÃƒÂ©lectionner l'ÃƒÂ©quipement dans la ComboBox
        locationField.setText("");
        contactInfoField.setText(serviceRequest.getRequesterEmail() != null ? serviceRequest.getRequesterEmail() : "");
        
        if (serviceRequest.getEstimatedCost() != null) {
            costField.setText(serviceRequest.getEstimatedCost().toString());
        }
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (getStringOrNull(titleField.getText()) == null) {
            errors.append("- Le titre est obligatoire\n");
        }
        
        if (typeCombo.getValue() == null) {
            errors.append("- Le type est obligatoire\n");
        }
        
        if (statusCombo.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }
        
        if (priorityCombo.getValue() == null) {
            errors.append("- La prioritÃƒÂ© est obligatoire\n");
        }
        
        if (requesterCombo.getValue() == null) {
            errors.append("- Le demandeur est obligatoire\n");
        }
        
        // Validation du coÃƒÂ»t si renseignÃƒÂ©
        if (!costField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(costField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- Le coÃƒÂ»t doit ÃƒÂªtre un nombre valide\n");
            }
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private ServiceRequest createServiceRequestFromFields() {
        ServiceRequest request = new ServiceRequest();
        
        request.setTitle(titleField.getText().trim());
        request.setDescription(getStringOrNull(descriptionArea.getText()));
        request.setType(mapDisplayTypeToEnum(typeCombo.getValue()));
        request.setStatus(mapDisplayStatusToEnum(statusCombo.getValue()));
        request.setPriority(mapDisplayPriorityToEnum(priorityCombo.getValue()));
        // RÃƒÂ©cupÃƒÂ©ration du demandeur sÃƒÂ©lectionnÃƒÂ©
        if (requesterCombo.getValue() != null) {
            Map<String, Object> selectedRequester = requesterCombo.getValue();
            String firstName = (String) selectedRequester.get("firstName");
            String lastName = (String) selectedRequester.get("lastName");
            request.setRequesterName(firstName + " " + lastName);
            request.setRequesterEmail((String) selectedRequester.get("email"));
        }
        
        // RÃƒÂ©cupÃƒÂ©ration du technicien sÃƒÂ©lectionnÃƒÂ©
        if (technicianCombo.getValue() != null) {
            Map<String, Object> selectedTechnician = technicianCombo.getValue();
            String firstName = (String) selectedTechnician.get("firstName");
            String lastName = (String) selectedTechnician.get("lastName");
            request.setAssignedTechnician(firstName + " " + lastName);
        }
        
        // RÃƒÂ©cupÃƒÂ©ration de l'ÃƒÂ©quipement sÃƒÂ©lectionnÃƒÂ©
        if (equipmentCombo.getValue() != null) {
            Map<String, Object> selectedEquipment = equipmentCombo.getValue();
            // CrÃƒÂ©er un objet Equipment avec juste l'ID pour la relation @ManyToOne
            Equipment equipment = new Equipment();
            equipment.setId(((Number) selectedEquipment.get("id")).longValue());
            request.setEquipment(equipment);
        }

        // Les notes vont dans resolutionNotes
        request.setResolutionNotes(getStringOrNull(notesArea.getText()));        // L'email de contact dans requesterEmail
        request.setRequesterEmail(getStringOrNull(contactInfoField.getText()));
        
        if (!costField.getText().trim().isEmpty()) {
            try {
                request.setEstimatedCost(Double.parseDouble(costField.getText().trim()));
            } catch (NumberFormatException e) {
                // IgnorÃƒÂ©, dÃƒÂ©jÃƒÂ  validÃƒÂ©
            }
        }
        
        // Si c'est une nouvelle demande, dÃƒÂ©finir les dates de crÃƒÂ©ation
        if (!isEdit) {
            LocalDateTime now = LocalDateTime.now();
            request.setCreatedAt(now);
            request.setUpdatedAt(now);
        }
        
        return request;
    }
    
    // MÃƒÂ©thodes utilitaires pour le mapping des ÃƒÂ©numÃƒÂ©rations
    private String mapTypeEnumToDisplay(ServiceRequestType type) {
        if (type == null) return null;
        return switch (type) {
            case REPAIR -> "RÃƒÂ©paration";
            case MAINTENANCE -> "Maintenance";
            case INSTALLATION -> "Installation";
            case TRAINING -> "Formation";
            case RMA -> "RMA";
            case WARRANTY -> "Garantie";
        };
    }
    
    private ServiceRequestType mapDisplayTypeToEnum(String display) {
        if (display == null) return null;
        return switch (display) {
            case "RÃƒÂ©paration" -> ServiceRequestType.REPAIR;
            case "Maintenance" -> ServiceRequestType.MAINTENANCE;
            case "Installation" -> ServiceRequestType.INSTALLATION;
            case "Formation" -> ServiceRequestType.TRAINING;
            case "RMA" -> ServiceRequestType.RMA;
            case "Garantie" -> ServiceRequestType.WARRANTY;
            default -> null;
        };
    }
    
    private String mapStatusEnumToDisplay(ServiceRequestStatus status) {
        if (status == null) return null;
        return switch (status) {
            case OPEN -> "Ouverte";
            case IN_PROGRESS -> "En cours";
            case WAITING_PARTS -> "En attente de piÃƒÂ¨ces";
            case RESOLVED -> "RÃƒÂ©solue";
            case CLOSED -> "FermÃƒÂ©e";
            case CANCELLED -> "AnnulÃƒÂ©e";
        };
    }
    
    private ServiceRequestStatus mapDisplayStatusToEnum(String display) {
        if (display == null) return null;
        return switch (display) {
            case "Ouverte" -> ServiceRequestStatus.OPEN;
            case "En cours" -> ServiceRequestStatus.IN_PROGRESS;
            case "En attente de piÃƒÂ¨ces" -> ServiceRequestStatus.WAITING_PARTS;
            case "RÃƒÂ©solue" -> ServiceRequestStatus.RESOLVED;
            case "FermÃƒÂ©e" -> ServiceRequestStatus.CLOSED;
            case "AnnulÃƒÂ©e" -> ServiceRequestStatus.CANCELLED;
            default -> null;
        };
    }
    
    private String mapPriorityEnumToDisplay(ServiceRequest.Priority priority) {
        if (priority == null) return null;
        return switch (priority) {
            case LOW -> "Faible";
            case MEDIUM -> "Moyenne";
            case HIGH -> "Ãƒâ€°levÃƒÂ©e";
            case URGENT -> "Urgente";
        };
    }
    
    private ServiceRequest.Priority mapDisplayPriorityToEnum(String display) {
        if (display == null) return null;
        return switch (display) {
            case "Faible" -> ServiceRequest.Priority.LOW;
            case "Moyenne" -> ServiceRequest.Priority.MEDIUM;
            case "Ãƒâ€°levÃƒÂ©e" -> ServiceRequest.Priority.HIGH;
            case "Urgente" -> ServiceRequest.Priority.URGENT;
            default -> null;
        };
    }
    
    private String getStringOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    
    private ListCell<Map<String, Object>> createPersonnelListCell() {
        return new ListCell<Map<String, Object>>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String firstName = (String) item.get("firstName");
                    String lastName = (String) item.get("lastName");
                    String jobTitle = (String) item.get("jobTitle");
                    String department = (String) item.get("department");
                    
                    StringBuilder display = new StringBuilder();
                    if (firstName != null && lastName != null) {
                        display.append(firstName).append(" ").append(lastName);
                    }
                    if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                        display.append(" - ").append(jobTitle);
                    }
                    if (department != null && !department.trim().isEmpty()) {
                        display.append(" (").append(department).append(")");
                    }
                    setText(display.toString());
                }
            }
        };
    }
    
    private ListCell<Map<String, Object>> createEquipmentListCell() {
        return new ListCell<Map<String, Object>>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String name = (String) item.get("name");
                    String category = (String) item.get("category");
                    String brand = (String) item.get("brand");
                    String model = (String) item.get("model");
                    
                    StringBuilder display = new StringBuilder();
                    if (name != null) {
                        display.append(name);
                    }
                    if (category != null && !category.trim().isEmpty()) {
                        display.append(" [").append(category).append("]");
                    }
                    if (brand != null && !brand.trim().isEmpty()) {
                        display.append(" - ").append(brand);
                    }
                    if (model != null && !model.trim().isEmpty()) {
                        display.append(" ").append(model);
                    }
                    setText(display.toString());
                }
            }
        };
    }
    
    private void loadPersonnelData() {
        CompletableFuture<List<Object>> future = apiService.getActivePersonnel();
        future.thenAccept(personnel -> {
            Platform.runLater(() -> {
                ObservableList<Map<String, Object>> personnelList = FXCollections.observableArrayList();
                for (Object item : personnel) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> personnelMap = (Map<String, Object>) item;
                        personnelList.add(personnelMap);
                    }
                }
                requesterCombo.setItems(personnelList);
                technicianCombo.setItems(personnelList);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Impossible de charger le personnel");
                alert.setContentText("Erreur: " + throwable.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
    
    private void loadEquipmentData() {
        CompletableFuture<List<Object>> future = apiService.getEquipments();
        future.thenAccept(equipment -> {
            Platform.runLater(() -> {
                ObservableList<Map<String, Object>> equipmentList = FXCollections.observableArrayList();
                for (Object item : equipment) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> equipmentMap = (Map<String, Object>) item;
                        equipmentList.add(equipmentMap);
                    }
                }
                equipmentCombo.setItems(equipmentList);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Impossible de charger les ÃƒÂ©quipements");
                alert.setContentText("Erreur: " + throwable.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }

    public Optional<ServiceRequest> showAndWait() {
        dialog.showAndWait();
        return Optional.ofNullable(result);
    }
}

