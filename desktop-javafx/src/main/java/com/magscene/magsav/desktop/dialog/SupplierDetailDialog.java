package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog de visualisation/édition d'un fournisseur
 * S'ouvre en mode lecture seule, bouton Modifier pour basculer en édition
 * Style unifié avec les autres DetailDialogs (Equipment, Personnel, Vehicle)
 */
@SuppressWarnings("unused")
public class SupplierDetailDialog extends Dialog<Map<String, Object>> {
    
    private final ApiService apiService;
    private final Map<String, Object> supplierData;
    
    private boolean editMode = false;
    private Button editSaveButton;
    private VBox contentBox;
    
    // ButtonTypes pour les boutons du bas
    private ButtonType editButtonType;
    private ButtonType closeButtonType;
    
    // Champs éditables - Informations générales
    private TextField nameField, contactPersonField, emailField, phoneField;
    private TextField websiteField, siretField;
    private TextArea addressArea;
    private ComboBox<String> statusCombo;
    
    // Champs éditables - Services
    private CheckBox savCheckBox, rmaCheckBox, partsCheckBox, equipmentCheckBox;
    
    // Champs éditables - Conditions commerciales
    private TextField paymentTermsField, deliveryTermsField, minimumOrderField;
    private TextField freeShippingThresholdField, handlingFeeField;
    private TextArea notesArea;
    
    public SupplierDetailDialog(ApiService apiService, Map<String, Object> supplier) {
        this.apiService = apiService;
        this.supplierData = new HashMap<>(supplier);
        
        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Fournisseur");
        setHeaderText(null);
        
        // Bouton Modifier/Enregistrer en bas du dialog
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(700, 600);
        getDialogPane().setMinWidth(650);
        getDialogPane().setMinHeight(550);
        
        // Appliquer le thème unifié
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        // Styliser et configurer le bouton Modifier
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? supplierData : null);
    }
    
    private boolean editButtonInitialized = false;
    
    private void setupEditButton() {
        Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
        if (editButton != null) {
            editSaveButton = editButton;
            updateEditButtonStyle(editButton);
            
            if (!editButtonInitialized) {
                editButtonInitialized = true;
                editButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                    e.consume();
                    toggleEditMode();
                });
            }
        }
    }
    
    private void updateEditButtonStyle(Button editButton) {
        String buttonStyle = editMode 
            ? ThemeConstants.DIALOG_SAVE_BUTTON_STYLE
            : ThemeConstants.DIALOG_EDIT_BUTTON_STYLE;
        
        editButton.setText(editMode ? "Enregistrer" : "Modifier");
        editButton.setStyle(buttonStyle);
        editButton.setMinWidth(120);
        editButton.setPrefWidth(120);
        
        final String finalButtonStyle = buttonStyle;
        editButton.setOnMouseEntered(e -> editButton.setStyle(
            finalButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        ));
        editButton.setOnMouseExited(e -> editButton.setStyle(finalButtonStyle));
    }
    
    private void setupCloseButton() {
        Button closeButton = (Button) getDialogPane().lookupButton(closeButtonType);
        if (closeButton != null) {
            closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE);
            closeButton.setMinWidth(100);
            closeButton.setPrefWidth(100);
            
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE));
        }
    }
    
    private void createContent() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        // Header avec infos fournisseur
        mainLayout.setTop(createHeader());
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        rebuildContent();
        
        scrollPane.setContent(contentBox);
        mainLayout.setCenter(scrollPane);
        
        getDialogPane().setContent(mainLayout);
    }
    
    private void rebuildContent() {
        contentBox.getChildren().clear();
        contentBox.getChildren().addAll(
            createSection("🏢 Informations générales", createGeneralSection()),
            createSection("📞 Contact", createContactSection()),
            createSection("🛠️ Services proposés", createServicesSection()),
            createSection("💰 Conditions commerciales", createCommercialSection()),
            createSection("📝 Notes", createNotesSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_SUPPLIER_STYLE);
        
        // Icône
        Label icon = new Label("🏭");
        icon.setFont(Font.font(40));
        
        // Infos centrales
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String name = getStringValue("name");
        Label nameLabel = new Label(!name.isEmpty() ? name : "Fournisseur sans nom");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);
        
        String contactPerson = getStringValue("contactPerson");
        String email = getStringValue("email");
        Label subtitleLabel = new Label(
            (contactPerson != null && !contactPerson.isEmpty() ? "Contact: " + contactPerson : "") + 
            (contactPerson != null && !contactPerson.isEmpty() && email != null && !email.isEmpty() ? " • " : "") + 
            (email != null && !email.isEmpty() ? email : "")
        );
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web("#bdc3c7"));
        
        // Statut
        boolean active = getBooleanValue("active");
        Label statusLabel = new Label("Statut: " + (active ? "Actif" : "Inactif"));
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setTextFill(active ? Color.web("#2ecc71") : Color.web("#e74c3c"));
        
        infoBox.getChildren().addAll(nameLabel, subtitleLabel, statusLabel);
        
        // Services à droite
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        List<String> services = getActiveServices();
        if (!services.isEmpty()) {
            Label servicesLabel = new Label(String.join(" • ", services));
            servicesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            servicesLabel.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 10; -fx-background-radius: 15;");
            servicesLabel.setTextFill(Color.WHITE);
            rightBox.getChildren().add(servicesLabel);
        }
        
        header.getChildren().addAll(icon, infoBox, rightBox);
        return header;
    }
    
    private List<String> getActiveServices() {
        List<String> services = new ArrayList<>();
        if (getBooleanValue("hasAfterSalesService")) services.add("SAV");
        if (getBooleanValue("hasRMAService")) services.add("RMA");
        if (getBooleanValue("hasPartsService")) services.add("Pièces");
        if (getBooleanValue("hasEquipmentService")) services.add("Matériel");
        return services;
    }
    
    private void toggleEditMode() {
        if (editMode) {
            saveChanges();
            saveToApi();
            editMode = false;
        } else {
            editMode = true;
        }
        
        setupEditButton();
        getDialogPane().setContent(null);
        createContent();
    }
    
    private void saveChanges() {
        if (nameField != null) supplierData.put("name", nameField.getText().trim());
        if (contactPersonField != null) supplierData.put("contactPerson", contactPersonField.getText().trim());
        if (emailField != null) supplierData.put("email", emailField.getText().trim());
        if (phoneField != null) supplierData.put("phone", phoneField.getText().trim());
        if (websiteField != null) supplierData.put("website", websiteField.getText().trim());
        if (siretField != null) supplierData.put("siret", siretField.getText().trim());
        if (addressArea != null) supplierData.put("address", addressArea.getText().trim());
        if (notesArea != null) supplierData.put("notes", notesArea.getText().trim());
        
        // Status
        if (statusCombo != null && statusCombo.getValue() != null) {
            supplierData.put("active", "ACTIF".equals(statusCombo.getValue()));
        }
        
        // Services
        if (savCheckBox != null) supplierData.put("hasAfterSalesService", savCheckBox.isSelected());
        if (rmaCheckBox != null) supplierData.put("hasRMAService", rmaCheckBox.isSelected());
        if (partsCheckBox != null) supplierData.put("hasPartsService", partsCheckBox.isSelected());
        if (equipmentCheckBox != null) supplierData.put("hasEquipmentService", equipmentCheckBox.isSelected());
        
        // Conditions commerciales
        if (paymentTermsField != null) supplierData.put("paymentTerms", paymentTermsField.getText().trim());
        if (deliveryTermsField != null) supplierData.put("deliveryTerms", deliveryTermsField.getText().trim());
        
        if (minimumOrderField != null) {
            try {
                supplierData.put("minimumOrder", Double.parseDouble(minimumOrderField.getText().trim()));
            } catch (NumberFormatException e) {
                supplierData.put("minimumOrder", 0.0);
            }
        }
        if (freeShippingThresholdField != null) {
            try {
                supplierData.put("freeShippingThreshold", Double.parseDouble(freeShippingThresholdField.getText().trim()));
            } catch (NumberFormatException e) {
                supplierData.put("freeShippingThreshold", 0.0);
            }
        }
        if (handlingFeeField != null) {
            try {
                supplierData.put("handlingFee", Double.parseDouble(handlingFeeField.getText().trim()));
            } catch (NumberFormatException e) {
                supplierData.put("handlingFee", 0.0);
            }
        }
    }
    
    private void saveToApi() {
        Object id = supplierData.get("id");
        if (id != null && apiService != null) {
            Long supplierId = Long.valueOf(id.toString());
            apiService.updateSupplier(supplierId, supplierData)
                .thenAccept(result -> {
                    System.out.println("✅ Fournisseur " + supplierId + " mis à jour avec succès");
                })
                .exceptionally(ex -> {
                    System.err.println("❌ Erreur mise à jour fournisseur: " + ex.getMessage());
                    return null;
                });
        }
    }
    
    // ========== Sections de contenu ==========
    
    private VBox createSection(String title, Region content) {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 18; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        section.getChildren().addAll(titleLabel, content);
        return section;
    }
    
    private GridPane createGeneralSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom - Statut
        grid.add(createFieldLabel("Nom"), 0, row);
        if (editMode) {
            nameField = new TextField(getStringValue("name"));
            nameField.setPrefWidth(250);
            grid.add(nameField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("name")), 1, row);
        }
        
        grid.add(createFieldLabel("Statut"), 2, row);
        if (editMode) {
            statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIF", "INACTIF"));
            statusCombo.setValue(getBooleanValue("active") ? "ACTIF" : "INACTIF");
            statusCombo.setPrefWidth(120);
            grid.add(statusCombo, 3, row);
        } else {
            grid.add(createValueLabel(getBooleanValue("active") ? "Actif" : "Inactif"), 3, row);
        }
        row++;
        
        // SIRET - Site web
        grid.add(createFieldLabel("SIRET"), 0, row);
        if (editMode) {
            siretField = new TextField(getStringValue("siret"));
            siretField.setPrefWidth(180);
            grid.add(siretField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("siret")), 1, row);
        }
        
        grid.add(createFieldLabel("Site web"), 2, row);
        if (editMode) {
            websiteField = new TextField(getStringValue("website"));
            websiteField.setPrefWidth(200);
            grid.add(websiteField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("website")), 3, row);
        }
        row++;
        
        // Adresse
        grid.add(createFieldLabel("Adresse"), 0, row);
        if (editMode) {
            addressArea = new TextArea(getStringValue("address"));
            addressArea.setPrefRowCount(2);
            addressArea.setPrefWidth(450);
            GridPane.setColumnSpan(addressArea, 3);
            grid.add(addressArea, 1, row);
        } else {
            Label addressLabel = createValueLabel(getStringValue("address"));
            addressLabel.setWrapText(true);
            GridPane.setColumnSpan(addressLabel, 3);
            grid.add(addressLabel, 1, row);
        }
        
        return grid;
    }
    
    private GridPane createContactSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Contact - Email
        grid.add(createFieldLabel("Contact"), 0, row);
        if (editMode) {
            contactPersonField = new TextField(getStringValue("contactPerson"));
            contactPersonField.setPrefWidth(200);
            grid.add(contactPersonField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("contactPerson")), 1, row);
        }
        
        grid.add(createFieldLabel("Email"), 2, row);
        if (editMode) {
            emailField = new TextField(getStringValue("email"));
            emailField.setPrefWidth(220);
            grid.add(emailField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("email")), 3, row);
        }
        row++;
        
        // Téléphone
        grid.add(createFieldLabel("Téléphone"), 0, row);
        if (editMode) {
            phoneField = new TextField(getStringValue("phone"));
            phoneField.setPrefWidth(150);
            grid.add(phoneField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("phone")), 1, row);
        }
        
        return grid;
    }
    
    private VBox createServicesSection() {
        VBox container = new VBox(10);
        
        if (editMode) {
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(20);
            flowPane.setVgap(10);
            
            savCheckBox = new CheckBox("Service après-vente (SAV)");
            savCheckBox.setSelected(getBooleanValue("hasAfterSalesService"));
            
            rmaCheckBox = new CheckBox("Retours marchandises (RMA)");
            rmaCheckBox.setSelected(getBooleanValue("hasRMAService"));
            
            partsCheckBox = new CheckBox("Pièces détachées");
            partsCheckBox.setSelected(getBooleanValue("hasPartsService"));
            
            equipmentCheckBox = new CheckBox("Matériel / Équipement");
            equipmentCheckBox.setSelected(getBooleanValue("hasEquipmentService"));
            
            flowPane.getChildren().addAll(savCheckBox, rmaCheckBox, partsCheckBox, equipmentCheckBox);
            container.getChildren().add(flowPane);
        } else {
            List<String> services = getActiveServices();
            if (services.isEmpty()) {
                container.getChildren().add(createValueLabel("Aucun service"));
            } else {
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(8);
                flowPane.setVgap(8);
                
                for (String service : services) {
                    Label tag = new Label(getServiceFullName(service));
                    tag.setStyle(ThemeConstants.TAG_STYLE);
                    tag.setFont(Font.font("Segoe UI", 11));
                    flowPane.getChildren().add(tag);
                }
                
                container.getChildren().add(flowPane);
            }
        }
        
        return container;
    }
    
    private String getServiceFullName(String shortName) {
        switch (shortName) {
            case "SAV": return "Service après-vente";
            case "RMA": return "Retours marchandises";
            case "Pièces": return "Pièces détachées";
            case "Matériel": return "Matériel / Équipement";
            default: return shortName;
        }
    }
    
    private GridPane createCommercialSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Conditions de paiement - Livraison
        grid.add(createFieldLabel("Paiement"), 0, row);
        if (editMode) {
            paymentTermsField = new TextField(getStringValue("paymentTerms"));
            paymentTermsField.setPromptText("Ex: 30 jours fin de mois");
            paymentTermsField.setPrefWidth(200);
            grid.add(paymentTermsField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("paymentTerms")), 1, row);
        }
        
        grid.add(createFieldLabel("Livraison"), 2, row);
        if (editMode) {
            deliveryTermsField = new TextField(getStringValue("deliveryTerms"));
            deliveryTermsField.setPromptText("Ex: Franco de port");
            deliveryTermsField.setPrefWidth(200);
            grid.add(deliveryTermsField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("deliveryTerms")), 3, row);
        }
        row++;
        
        // Commande minimum - Seuil franco
        grid.add(createFieldLabel("Min. commande"), 0, row);
        if (editMode) {
            minimumOrderField = new TextField(formatDouble(getDoubleValue("minimumOrder")));
            minimumOrderField.setPromptText("0.00");
            minimumOrderField.setPrefWidth(100);
            grid.add(new HBox(5, minimumOrderField, new Label("€")), 1, row);
        } else {
            grid.add(createValueLabel(formatCurrency(getDoubleValue("minimumOrder"))), 1, row);
        }
        
        grid.add(createFieldLabel("Seuil franco"), 2, row);
        if (editMode) {
            freeShippingThresholdField = new TextField(formatDouble(getDoubleValue("freeShippingThreshold")));
            freeShippingThresholdField.setPromptText("0.00");
            freeShippingThresholdField.setPrefWidth(100);
            grid.add(new HBox(5, freeShippingThresholdField, new Label("€")), 3, row);
        } else {
            grid.add(createValueLabel(formatCurrency(getDoubleValue("freeShippingThreshold"))), 3, row);
        }
        row++;
        
        // Frais de manutention
        grid.add(createFieldLabel("Frais manuten."), 0, row);
        if (editMode) {
            handlingFeeField = new TextField(formatDouble(getDoubleValue("handlingFee")));
            handlingFeeField.setPromptText("0.00");
            handlingFeeField.setPrefWidth(100);
            grid.add(new HBox(5, handlingFeeField, new Label("€")), 1, row);
        } else {
            grid.add(createValueLabel(formatCurrency(getDoubleValue("handlingFee"))), 1, row);
        }
        
        return grid;
    }
    
    private VBox createNotesSection() {
        VBox container = new VBox(5);
        
        if (editMode) {
            notesArea = new TextArea(getStringValue("notes"));
            notesArea.setPrefRowCount(3);
            notesArea.setPromptText("Notes additionnelles...");
            container.getChildren().add(notesArea);
        } else {
            String notes = getStringValue("notes");
            Label notesLabel = createValueLabel(notes.isEmpty() ? "Aucune note" : notes);
            notesLabel.setWrapText(true);
            container.getChildren().add(notesLabel);
        }
        
        return container;
    }
    
    // ========== Utilitaires ==========
    
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#7f8c8d"));
        label.setMinWidth(130);
        return label;
    }
    
    private Label createValueLabel(String value) {
        Label label = new Label(value != null && !value.isEmpty() ? value : "-");
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web("#2c3e50"));
        label.setWrapText(true);
        return label;
    }
    
    private String getStringValue(String key) {
        Object value = supplierData.get(key);
        return value != null ? value.toString() : "";
    }
    
    private boolean getBooleanValue(String key) {
        Object value = supplierData.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return "true".equalsIgnoreCase((String) value);
        return false;
    }
    
    private double getDoubleValue(String key) {
        Object value = supplierData.get(key);
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    private String formatDouble(double value) {
        if (value == 0.0) return "";
        return String.format("%.2f", value);
    }
    
    private String formatCurrency(double value) {
        if (value == 0.0) return "-";
        return String.format("%.2f €", value);
    }
}
