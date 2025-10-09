package com.magsav.gui.categories;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SubcategoriesController implements Initializable {

    private final CategoryRepository repo = new CategoryRepository();
    private final ObservableList<Category> subcategoryItems = FXCollections.observableArrayList();
    private Category parentCategory;

    @FXML private Label lblParentCategory;
    @FXML private TableView<Category> table;
    @FXML private TableColumn<Category, Long> colId;
    @FXML private TableColumn<Category, String> colNom;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des colonnes
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().id()).asObject());
        colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom()));
        
        // Liaison des données
        table.setItems(subcategoryItems);
    }

    public void setParentCategory(Category parent) {
        this.parentCategory = parent;
        if (lblParentCategory != null) {
            lblParentCategory.setText("Sous-catégories de : " + parent.nom());
        }
        refreshSubcategories();
    }

    private void refreshSubcategories() {
        if (parentCategory == null) return;
        
        try {
            // Utiliser la méthode dédiée pour récupérer les sous-catégories
            List<Category> subcategories = repo.findSubcategories(parentCategory.id());
            subcategoryItems.clear();
            subcategoryItems.addAll(subcategories);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des sous-catégories: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onAddSubcategory() {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
            Parent root = l.load();
            CategoryFormController ctl = l.getController();
            
            // Préparer la liste avec seulement la catégorie parent
            List<Category> categories = List.of(parentCategory);
            ctl.initForSubcategory(categories, parentCategory);

            Dialog<ButtonType> d = new Dialog<>();
            d.setTitle("Ajouter une sous-catégorie à " + parentCategory.nom());
            d.getDialogPane().setContent(root);
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> res = d.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) return;

            String name = ctl.name();
            if (name.length() < 2) { 
                new Alert(Alert.AlertType.WARNING, "Nom requis (≥ 2 caractères)").showAndWait(); 
                return; 
            }

            repo.insertSubcategory(parentCategory.id(), name);
            
            new Alert(Alert.AlertType.INFORMATION, "Sous-catégorie « " + name + " » ajoutée avec succès").showAndWait();
            refreshSubcategories();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onDeleteSubcategory() {
        Category selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une sous-catégorie à supprimer").showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer la sous-catégorie « " + selected.nom() + " » ?");
        confirmation.setContentText("Cette action supprimera définitivement la sous-catégorie.\n\nCette action ne peut pas être annulée.");
        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            boolean success = repo.delete(selected.id());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Sous-catégorie supprimée avec succès").showAndWait();
                refreshSubcategories();
            } else {
                new Alert(Alert.AlertType.ERROR, "Aucune sous-catégorie n'a été supprimée").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onClose() {
        // Fermer la fenêtre
        if (table != null && table.getScene() != null && table.getScene().getWindow() != null) {
            table.getScene().getWindow().hide();
        }
    }
}