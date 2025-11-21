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
// Equipment simple pour relation
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;

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
    private boolean isReadOnlyMode;
    private final ApiService apiService;

    public ServiceRequestDialog(ServiceRequest serviceRequest) {
        this(serviceRequest, false); // Par défaut en mode édition
    }

    public ServiceRequestDialog(ServiceRequest serviceRequest, boolean readOnlyMode) {
        this.isEdit = serviceRequest != null;
        this.isReadOnlyMode = readOnlyMode;
        this.apiService = new ApiService();
        this.dialog = new Stage();
        
        // Configuration de base du dialogue
        String title;
        if (isReadOnlyMode) {
            title = "Détails de la demande SAV";
        } else {
            title = isEdit ? "Modifier la demande SAV" : "Nouvelle demande SAV";
        }
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        
        // Creation des champs
        titleField = new TextField();
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        
        // ComboBoxes avec les valeurs d'enumeration
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Reparation", "Maintenance", "Installation", "Formation", "RMA", "Garantie"
        ));
        
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Ouverte", "En cours", "En attente de pieces", "Resolue", "Fermee", "Annulee"
        ));
        
        priorityCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Faible", "Moyenne", "Elevee", "Urgente"
        ));
        
        // ComboBoxes pour la selection depuis la DB
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
        
        // Chargement des donnees depuis la DB
        loadPersonnelData();
        loadEquipmentData();
        
        // Pre-remplir les champs si c'est une edition
        if (isEdit) {
            populateFields(serviceRequest);
        } else {
            // Valeurs par defaut pour une nouvelle demande
            statusCombo.setValue("Ouverte");
            priorityCombo.setValue("Moyenne");
            scheduledDatePicker.setValue(LocalDate.now());
        }
        
        // Désactiver les champs en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }
        
        // Creation du layout
        VBox mainLayout = createLayout();
        
        // Configuration de la scene
        Scene scene = new Scene(mainLayout, 650, 750);
        dialog.setScene(scene);
        
        // Appliquer le thème dark au dialogue
        ThemeManager.getInstance().applyThemeToScene(scene);
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
        
        mainGrid.add(new Label("Priorite *:"), 0, row);
        mainGrid.add(priorityCombo, 1, row++);
        
        mainGrid.add(new Label("Demandeur *:"), 0, row);
        mainGrid.add(requesterCombo, 1, row++);
        
        mainGrid.add(new Label("Technicien:"), 0, row);
        mainGrid.add(technicianCombo, 1, row++);
        
        mainGrid.add(new Label("Date prevue:"), 0, row);
        mainGrid.add(scheduledDatePicker, 1, row++);
        
        mainGrid.add(new Label("Equipement:"), 0, row);
        mainGrid.add(equipmentCombo, 1, row++);
        
        mainGrid.add(new Label("Lieu:"), 0, row);
        mainGrid.add(locationField, 1, row++);
        
        mainGrid.add(new Label("Contact:"), 0, row);
        mainGrid.add(contactInfoField, 1, row++);
        
        mainGrid.add(new Label("Cout estime:"), 0, row);
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
        
        if (isReadOnlyMode) {
            // Mode lecture seule : bouton Modifier + Fermer
            Button closeButton = new Button("Fermer");
            closeButton.getStyleClass().add("action-button-secondary");
            closeButton.setOnAction(e -> {
                result = null;
                dialog.close();
            });
            
            if (isEdit) {
                Button editButton = new Button("Modifier");
                editButton.getStyleClass().add("action-button-primary");
                editButton.setOnAction(e -> {
                    // Passer en mode édition
                    isReadOnlyMode = false;
                    setFieldsEditable();
                    
                    // Reconstruire l'interface
                    VBox mainLayout = createLayout();
                    Scene scene = new Scene(mainLayout, 650, 750);
                    dialog.setScene(scene);
                    ThemeManager.getInstance().applyThemeToScene(scene);
                    dialog.setTitle("Modifier la demande SAV");
                });
                
                buttonBox.getChildren().addAll(closeButton, editButton);
            } else {
                buttonBox.getChildren().add(closeButton);
            }
        } else {
            // Mode édition : bouton Annuler + Enregistrer
            Button cancelButton = new Button("Annuler");
            cancelButton.getStyleClass().add("action-button-secondary");
            cancelButton.setOnAction(e -> {
                result = null;
                dialog.close();
            });
            
            Button saveButton = new Button(isEdit ? "Enregistrer" : "Créer");
            saveButton.getStyleClass().add("action-button-success");
            saveButton.setDefaultButton(true);
            saveButton.setOnAction(e -> {
                if (validateFields()) {
                    result = createServiceRequestFromFields();
                    dialog.close();
                }
            });
            
            buttonBox.getChildren().addAll(cancelButton, saveButton);
        }
        
        return buttonBox;
    }
    
    private void populateFields(ServiceRequest serviceRequest) {
        titleField.setText(serviceRequest.getTitle());
        descriptionArea.setText(serviceRequest.getDescription() != null ? serviceRequest.getDescription() : "");
        
        // Mapping des enumerations vers les valeurs d'affichage
        typeCombo.setValue(mapTypeEnumToDisplay(serviceRequest.getType()));
        statusCombo.setValue(mapStatusEnumToDisplay(serviceRequest.getStatus()));
        priorityCombo.setValue(mapPriorityEnumToDisplay(serviceRequest.getPriority()));
        
        // Sélectionner le demandeur dans la ComboBox basé sur le nom
        selectPersonnelByName(requesterCombo, serviceRequest.getRequesterName());
        
        // Sélectionner le technicien dans la ComboBox basé sur le nom
        selectPersonnelByName(technicianCombo, serviceRequest.getAssignedTechnician());
        
        // Le ServiceRequest n'a pas de scheduledDate, on utilise la date de creation par defaut
        scheduledDatePicker.setValue(LocalDate.now());
        
        notesArea.setText(serviceRequest.getResolutionNotes() != null ? serviceRequest.getResolutionNotes() : "");
        
        // Sélectionner l'équipement dans la ComboBox
        selectEquipmentByName(equipmentCombo, serviceRequest.getEquipmentName());
        locationField.setText("");
        contactInfoField.setText(serviceRequest.getRequesterEmail() != null ? serviceRequest.getRequesterEmail() : "");
        
        if (serviceRequest.getEstimatedCost() != null) {
            costField.setText(serviceRequest.getEstimatedCost().toString());
        }
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        // Reset des styles d'erreur précédents
        titleField.setStyle("");
        typeCombo.setStyle("");
        statusCombo.setStyle("");
        priorityCombo.setStyle("");
        requesterCombo.setStyle("");
        descriptionArea.setStyle("");
        costField.setStyle("");
        scheduledDatePicker.setStyle("");
        
        // Validation titre
        if (getStringOrNull(titleField.getText()) == null) {
            errors.append("• Le titre est obligatoire\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation type
        if (typeCombo.getValue() == null) {
            errors.append("• Le type d'intervention est obligatoire\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation statut
        if (statusCombo.getValue() == null) {
            errors.append("• Le statut est obligatoire\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation priorité
        if (priorityCombo.getValue() == null) {
            errors.append("• La priorité est obligatoire\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation demandeur
        if (requesterCombo.getValue() == null) {
            errors.append("• Le demandeur est obligatoire\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation description (recommandée pour SAV)
        if (getStringOrNull(descriptionArea.getText()) == null || 
            descriptionArea.getText().trim().length() < 10) {
            errors.append("• Une description détaillée (min. 10 caractères) est recommandée pour le SAV\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        // Validation coût si renseigné
        if (!costField.getText().trim().isEmpty()) {
            try {
                double cost = Double.parseDouble(costField.getText().trim());
                if (cost < 0) {
                    errors.append("• Le coût estimé ne peut pas être négatif\n");
                    // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                }
            } catch (NumberFormatException e) {
                errors.append("• Le coût doit être un nombre valide (ex: 125.50)\n");
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
            }
        }
        
        // Validation date
        if (scheduledDatePicker.getValue() != null && 
            scheduledDatePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date d'intervention ne peut pas être dans le passé\n");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("🔍 Validation des données SAV");
            alert.setHeaderText("Veuillez vérifier les informations saisies :");
            alert.setContentText(errors.toString());
            alert.getDialogPane().setPrefWidth(500);
            alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
            
            // Ajouter un bouton pour continuer malgré les avertissements
            ButtonType continueBtn = new ButtonType("Continuer malgré tout", ButtonBar.ButtonData.OK_DONE);
            ButtonType fixBtn = new ButtonType("Corriger", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(continueBtn, fixBtn);
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == continueBtn;
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
        // Recuperation du demandeur selectionne
        if (requesterCombo.getValue() != null) {
            Map<String, Object> selectedRequester = requesterCombo.getValue();
            String firstName = (String) selectedRequester.get("firstName");
            String lastName = (String) selectedRequester.get("lastName");
            request.setRequesterName(firstName + " " + lastName);
            request.setRequesterEmail((String) selectedRequester.get("email"));
        }
        
        // Recuperation du technicien selectionne
        if (technicianCombo.getValue() != null) {
            Map<String, Object> selectedTechnician = technicianCombo.getValue();
            String firstName = (String) selectedTechnician.get("firstName");
            String lastName = (String) selectedTechnician.get("lastName");
            request.setAssignedTechnician(firstName + " " + lastName);
        }
        
        // Recuperation de l'equipement selectionne
        if (equipmentCombo.getValue() != null) {
            Map<String, Object> selectedEquipment = equipmentCombo.getValue();
            // Adapter selon l'API ServiceRequest backend
            request.setEquipmentName(getEquipmentName(selectedEquipment));
        }

        // Les notes vont dans resolutionNotes
        request.setResolutionNotes(getStringOrNull(notesArea.getText()));        // L'email de contact dans requesterEmail
        request.setRequesterEmail(getStringOrNull(contactInfoField.getText()));
        
        if (!costField.getText().trim().isEmpty()) {
            try {
                request.setEstimatedCost(Double.parseDouble(costField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignore, deja valide
            }
        }
        
        // Si c'est une nouvelle demande, definir les dates de creation
        if (!isEdit) {
            LocalDateTime now = LocalDateTime.now();
            request.setCreatedAt(now);
            request.setUpdatedAt(now);
        }
        
        return request;
    }
    
    // Methodes utilitaires pour le mapping des enumerations
    private String mapTypeEnumToDisplay(ServiceRequestType type) {
        if (type == null) return null;
        return switch (type) {
            case REPAIR -> "Reparation";
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
            case "Reparation" -> ServiceRequestType.REPAIR;
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
            case WAITING_PARTS -> "En attente de pieces";
            case RESOLVED -> "Resolue";
            case CLOSED -> "Fermee";
            case CANCELLED -> "Annulee";
        };
    }
    
    private ServiceRequestStatus mapDisplayStatusToEnum(String display) {
        if (display == null) return null;
        return switch (display) {
            case "Ouverte" -> ServiceRequestStatus.OPEN;
            case "En cours" -> ServiceRequestStatus.IN_PROGRESS;
            case "En attente de pieces" -> ServiceRequestStatus.WAITING_PARTS;
            case "Resolue" -> ServiceRequestStatus.RESOLVED;
            case "Fermee" -> ServiceRequestStatus.CLOSED;
            case "Annulee" -> ServiceRequestStatus.CANCELLED;
            default -> null;
        };
    }
    
    private String mapPriorityEnumToDisplay(ServiceRequest.Priority priority) {
        if (priority == null) return null;
        return switch (priority) {
            case LOW -> "Faible";
            case MEDIUM -> "Moyenne";
            case HIGH -> "Elevee";
            case URGENT -> "Urgente";
        };
    }
    
    private ServiceRequest.Priority mapDisplayPriorityToEnum(String display) {
        if (display == null) return null;
        return switch (display) {
            case "Faible" -> ServiceRequest.Priority.LOW;
            case "Moyenne" -> ServiceRequest.Priority.MEDIUM;
            case "Elevee" -> ServiceRequest.Priority.HIGH;
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
                alert.setHeaderText("Impossible de charger les equipements");
                alert.setContentText("Erreur: " + throwable.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }

    /**
     * Sélectionne un personnel dans une ComboBox basé sur le nom
     */
    private void selectPersonnelByName(ComboBox<Map<String, Object>> comboBox, String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        
        // Attendre que les données soient chargées puis rechercher
        Platform.runLater(() -> {
            new Thread(() -> {
                // Attendre un peu que les données soient chargées
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                
                Platform.runLater(() -> {
                    ObservableList<Map<String, Object>> items = comboBox.getItems();
                    if (items != null) {
                        for (Map<String, Object> personnel : items) {
                            String fullName = getPersonnelFullName(personnel);
                            if (fullName != null && fullName.toLowerCase().contains(name.toLowerCase())) {
                                comboBox.setValue(personnel);
                                break;
                            }
                        }
                    }
                });
            }).start();
        });
    }

    /**
     * Sélectionne un équipement dans une ComboBox basé sur le nom
     */
    private void selectEquipmentByName(ComboBox<Map<String, Object>> comboBox, String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        
        // Attendre que les données soient chargées puis rechercher
        Platform.runLater(() -> {
            new Thread(() -> {
                // Attendre un peu que les données soient chargées
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                
                Platform.runLater(() -> {
                    ObservableList<Map<String, Object>> items = comboBox.getItems();
                    if (items != null) {
                        for (Map<String, Object> equipment : items) {
                            String equipmentName = getEquipmentName(equipment);
                            if (equipmentName != null && equipmentName.toLowerCase().contains(name.toLowerCase())) {
                                comboBox.setValue(equipment);
                                break;
                            }
                        }
                    }
                });
            }).start();
        });
    }

    /**
     * Extrait le nom complet d'un objet personnel
     */
    private String getPersonnelFullName(Map<String, Object> personnel) {
        if (personnel == null) return null;
        
        String firstName = (String) personnel.get("firstName");
        String lastName = (String) personnel.get("lastName");
        String fullName = (String) personnel.get("fullName");
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        } else if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        
        return (String) personnel.get("name"); // fallback
    }

    /**
     * Extrait le nom d'un objet équipement
     */
    private String getEquipmentName(Map<String, Object> equipment) {
        if (equipment == null) return null;
        
        String name = (String) equipment.get("name");
        if (name != null) return name;
        
        String designation = (String) equipment.get("designation");
        if (designation != null) return designation;
        
        return (String) equipment.get("label"); // fallback
    }

    /**
     * Désactive tous les champs pour le mode lecture seule
     */
    private void setFieldsReadOnly() {
        titleField.setDisable(true);
        descriptionArea.setDisable(true);
        typeCombo.setDisable(true);
        statusCombo.setDisable(true);
        priorityCombo.setDisable(true);
        requesterCombo.setDisable(true);
        technicianCombo.setDisable(true);
        equipmentCombo.setDisable(true);
        scheduledDatePicker.setDisable(true);
        notesArea.setDisable(true);
        locationField.setDisable(true);
        contactInfoField.setDisable(true);
        costField.setDisable(true);
    }
    
    /**
     * Active tous les champs pour le mode éditable
     */
    private void setFieldsEditable() {
        titleField.setDisable(false);
        descriptionArea.setDisable(false);
        typeCombo.setDisable(false);
        statusCombo.setDisable(false);
        priorityCombo.setDisable(false);
        requesterCombo.setDisable(false);
        technicianCombo.setDisable(false);
        equipmentCombo.setDisable(false);
        scheduledDatePicker.setDisable(false);
        notesArea.setDisable(false);
        locationField.setDisable(false);
        contactInfoField.setDisable(false);
        costField.setDisable(false);
    }

    public Optional<ServiceRequest> showAndWait() {
        dialog.showAndWait();
        return Optional.ofNullable(result);
    }
}

