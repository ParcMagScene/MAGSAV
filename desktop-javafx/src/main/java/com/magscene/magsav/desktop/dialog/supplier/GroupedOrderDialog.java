package com.magscene.magsav.desktop.dialog.supplier;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

public class GroupedOrderDialog {
    private final Stage dialog;
    private final TextField referenceField;
    private final ComboBox<Map<String, Object>> supplierCombo;
    private final TextField thresholdField;
    private final DatePicker targetDatePicker;
    private final TextArea notesArea;
    private final CheckBox autoValidateCheckbox;
    
    private Map<String, Object> result;
    private boolean isEdit;
    private final ApiService apiService;

    public GroupedOrderDialog(Map<String, Object> orderData) {
        this.isEdit = orderData != null;
        this.apiService = new ApiService();
        this.dialog = new Stage();
        
        String title = isEdit ? "Modifier la commande groupée" : "Nouvelle commande groupée";
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        
        referenceField = new TextField();
        referenceField.setPromptText("Généré automatiquement");
        referenceField.setDisable(true);
        
        supplierCombo = new ComboBox<>();
        supplierCombo.setPrefWidth(350);
        supplierCombo.setButtonCell(createSupplierListCell());
        supplierCombo.setCellFactory(lv -> createSupplierListCell());
        supplierCombo.setPromptText("Sélectionner un fournisseur");
        
        thresholdField = new TextField();
        thresholdField.setPromptText("500.00");
        
        targetDatePicker = new DatePicker();
        targetDatePicker.setValue(LocalDate.now().plusMonths(1));
        
        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        
        autoValidateCheckbox = new CheckBox("Valider automatiquement au seuil");
        autoValidateCheckbox.setSelected(false);
        
        loadSuppliersData();
        
        if (isEdit) {
            populateFields(orderData);
        }
        
        VBox mainLayout = createLayout();
        Scene scene = new Scene(mainLayout, 550, 450);
        dialog.setScene(scene);
        UnifiedThemeManager.getInstance().applyThemeToScene(scene);
    }
    
    private VBox createLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label titleLabel = new Label(isEdit ? "Modifier la commande groupée" : "Nouvelle commande groupée");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        
        grid.add(new Label("Référence:"), 0, row);
        grid.add(referenceField, 1, row++);
        
        grid.add(new Label("Fournisseur *:"), 0, row);
        grid.add(supplierCombo, 1, row++);
        
        grid.add(new Label("Seuil (€):"), 0, row);
        grid.add(thresholdField, 1, row++);
        
        grid.add(new Label("Date cible:"), 0, row);
        grid.add(targetDatePicker, 1, row++);
        
        grid.add(new Label(""), 0, row);
        grid.add(autoValidateCheckbox, 1, row++);
        
        VBox notesBox = new VBox(5);
        notesBox.getChildren().addAll(
            new Label("Notes:"),
            notesArea
        );
        
        HBox buttonBox = createButtonBox();
        
        layout.getChildren().addAll(titleLabel, grid, notesBox, buttonBox);
        return layout;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
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
                result = createOrderFromFields();
                dialog.close();
            }
        });
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        return buttonBox;
    }
    
    private void populateFields(Map<String, Object> orderData) {
        if (orderData.containsKey("reference")) {
            referenceField.setText((String) orderData.get("reference"));
        }
        
        if (orderData.containsKey("supplierName")) {
            selectSupplierByName((String) orderData.get("supplierName"));
        }
        
        if (orderData.containsKey("threshold")) {
            Object thresholdObj = orderData.get("threshold");
            if (thresholdObj != null) {
                thresholdField.setText(thresholdObj.toString());
            }
        }
        
        if (orderData.containsKey("notes")) {
            notesArea.setText((String) orderData.get("notes"));
        }
    }
    
    private boolean validateFields() {
        if (supplierCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un fournisseur");
            return false;
        }
        
        String thresholdText = thresholdField.getText().trim();
        if (!thresholdText.isEmpty()) {
            try {
                Double.parseDouble(thresholdText);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le seuil doit être un nombre valide");
                return false;
            }
        }
        
        return true;
    }
    
    private Map<String, Object> createOrderFromFields() {
        Map<String, Object> order = new java.util.HashMap<>();
        
        Map<String, Object> supplier = supplierCombo.getValue();
        if (supplier != null) {
            order.put("supplierId", supplier.get("id"));
            order.put("supplierName", supplier.get("companyName"));
        }
        
        String thresholdText = thresholdField.getText().trim();
        if (!thresholdText.isEmpty()) {
            try {
                order.put("threshold", Double.parseDouble(thresholdText));
            } catch (NumberFormatException e) {
                order.put("threshold", 0.0);
            }
        }
        
        if (targetDatePicker.getValue() != null) {
            order.put("targetDate", targetDatePicker.getValue().toString());
        }
        
        order.put("notes", notesArea.getText());
        order.put("autoValidate", autoValidateCheckbox.isSelected());
        
        return order;
    }
    
    private void loadSuppliersData() {
        try {
            List<Map<String, Object>> suppliers = apiService.getAll("suppliers");
            
            if (suppliers != null && !suppliers.isEmpty()) {
                System.out.println(" " + suppliers.size() + " fournisseurs chargés depuis la DB");
                supplierCombo.getItems().setAll(suppliers);
            } else {
                System.out.println(" Aucun fournisseur trouvé dans la DB");
            }
        } catch (Exception e) {
            System.err.println(" Erreur lors du chargement des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private ListCell<Map<String, Object>> createSupplierListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> supplier, boolean empty) {
                super.updateItem(supplier, empty);
                if (empty || supplier == null) {
                    setText(null);
                } else {
                    String companyName = (String) supplier.getOrDefault("companyName", "N/A");
                    String category = (String) supplier.getOrDefault("category", "");
                    
                    if (!category.isEmpty()) {
                        setText(companyName + " (" + category + ")");
                    } else {
                        setText(companyName);
                    }
                }
            }
        };
    }
    
    private void selectSupplierByName(String supplierName) {
        if (supplierName == null || supplierName.isEmpty()) {
            return;
        }
        
        for (Map<String, Object> supplier : supplierCombo.getItems()) {
            String companyName = (String) supplier.get("companyName");
            if (supplierName.equals(companyName)) {
                supplierCombo.setValue(supplier);
                break;
            }
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Map<String, Object> showAndWait() {
        dialog.showAndWait();
        return result;
    }
}