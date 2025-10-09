package com.magsav.ui.components;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Composant de table améliorée avec fonctionnalités intégrées
 * Recherche, tri, sélection, et actions contextuelles
 */
public class EnhancedTableView<T> {
    
    private final TableView<T> tableView;
    private final FilteredList<T> filteredData;
    private final SortedList<T> sortedData;
    private final ObservableList<T> sourceData;
    private final TextField searchField;
    
    private Consumer<T> onDoubleClick;
    private Consumer<T> onEdit;
    private Consumer<T> onDelete;
    private Predicate<T> searchFilter;
    
    /**
     * Constructeur
     */
    public EnhancedTableView() {
        this.sourceData = FXCollections.observableArrayList();
        this.filteredData = new FilteredList<>(sourceData);
        this.sortedData = new SortedList<>(filteredData);
        this.tableView = new TableView<>(sortedData);
        this.searchField = new TextField();
        
        setupTable();
        setupSearch();
    }
    
    /**
     * Configuration initiale de la table
     */
    private void setupTable() {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            
            // Double-clic
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && onDoubleClick != null) {
                    onDoubleClick.accept(row.getItem());
                }
            });
            
            // Menu contextuel
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("Modifier");
            editItem.setOnAction(e -> {
                if (onEdit != null && row.getItem() != null) {
                    onEdit.accept(row.getItem());
                }
            });
            
            MenuItem deleteItem = new MenuItem("Supprimer");
            deleteItem.setOnAction(e -> {
                if (onDelete != null && row.getItem() != null) {
                    onDelete.accept(row.getItem());
                }
            });
            
            contextMenu.getItems().addAll(editItem, deleteItem);
            
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            
            return row;
        });
        
        // Lier le tri à la table
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
    }
    
    /**
     * Configuration de la recherche
     */
    private void setupSearch() {
        searchField.setPromptText("Rechercher...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                
                if (searchFilter != null) {
                    return searchFilter.test(item);
                }
                
                // Recherche par défaut (toString)
                return item.toString().toLowerCase().contains(newValue.toLowerCase());
            });
        });
    }
    
    /**
     * Ajoute une colonne de texte simple
     */
    public EnhancedTableView<T> addColumn(String title, Function<T, String> valueExtractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(valueExtractor.apply(cellData.getValue())));
        tableView.getColumns().add(column);
        return this;
    }
    
    /**
     * Ajoute une colonne avec propriété JavaFX
     */
    public EnhancedTableView<T> addPropertyColumn(String title, String propertyName) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        tableView.getColumns().add(column);
        return this;
    }
    
    /**
     * Ajoute une colonne avec cellule personnalisée
     */
    public <U> EnhancedTableView<T> addCustomColumn(String title, Function<T, U> valueExtractor, 
                                                   Callback<TableColumn<T, U>, TableCell<T, U>> cellFactory) {
        TableColumn<T, U> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(valueExtractor.apply(cellData.getValue())));
        column.setCellFactory(cellFactory);
        tableView.getColumns().add(column);
        return this;
    }
    
    /**
     * Ajoute une colonne de boutons d'action
     */
    public EnhancedTableView<T> addActionColumn(String title, String buttonText, Consumer<T> action) {
        TableColumn<T, Void> column = new TableColumn<>(title);
        column.setCellFactory(col -> new TableCell<T, Void>() {
            private final Button button = new Button(buttonText);
            
            {
                button.setOnAction(e -> {
                    T item = getTableView().getItems().get(getIndex());
                    action.accept(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
        tableView.getColumns().add(column);
        return this;
    }
    
    /**
     * Définit les données de la table
     */
    public EnhancedTableView<T> setData(ObservableList<T> data) {
        sourceData.setAll(data);
        return this;
    }
    
    /**
     * Ajoute des données à la table
     */
    @SafeVarargs
    public final EnhancedTableView<T> addData(T... items) {
        sourceData.addAll(items);
        return this;
    }
    
    /**
     * Vide la table
     */
    public EnhancedTableView<T> clearData() {
        sourceData.clear();
        return this;
    }
    
    /**
     * Rafraîchit la table
     */
    public EnhancedTableView<T> refresh() {
        tableView.refresh();
        return this;
    }
    
    /**
     * Définit le filtre de recherche personnalisé
     */
    public EnhancedTableView<T> setSearchFilter(Predicate<T> filter) {
        this.searchFilter = filter;
        return this;
    }
    
    /**
     * Définit l'action de double-clic
     */
    public EnhancedTableView<T> onDoubleClick(Consumer<T> action) {
        this.onDoubleClick = action;
        return this;
    }
    
    /**
     * Définit l'action d'édition
     */
    public EnhancedTableView<T> onEdit(Consumer<T> action) {
        this.onEdit = action;
        return this;
    }
    
    /**
     * Définit l'action de suppression
     */
    public EnhancedTableView<T> onDelete(Consumer<T> action) {
        this.onDelete = action;
        return this;
    }
    
    /**
     * Obtient l'élément sélectionné
     */
    public T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Sélectionne un élément
     */
    public EnhancedTableView<T> selectItem(T item) {
        tableView.getSelectionModel().select(item);
        return this;
    }
    
    /**
     * Accesseurs
     */
    public TableView<T> getTableView() { return tableView; }
    public TextField getSearchField() { return searchField; }
    public ObservableList<T> getSourceData() { return sourceData; }
    public FilteredList<T> getFilteredData() { return filteredData; }
    
    /**
     * Builder pour création fluide
     */
    public static class Builder<T> {
        private final EnhancedTableView<T> table = new EnhancedTableView<>();
        
        public Builder<T> column(String title, Function<T, String> valueExtractor) {
            table.addColumn(title, valueExtractor);
            return this;
        }
        
        public Builder<T> propertyColumn(String title, String propertyName) {
            table.addPropertyColumn(title, propertyName);
            return this;
        }
        
        public <U> Builder<T> customColumn(String title, Function<T, U> valueExtractor,
                                          Callback<TableColumn<T, U>, TableCell<T, U>> cellFactory) {
            table.addCustomColumn(title, valueExtractor, cellFactory);
            return this;
        }
        
        public Builder<T> actionColumn(String title, String buttonText, Consumer<T> action) {
            table.addActionColumn(title, buttonText, action);
            return this;
        }
        
        public Builder<T> searchFilter(Predicate<T> filter) {
            table.setSearchFilter(filter);
            return this;
        }
        
        public Builder<T> onDoubleClick(Consumer<T> action) {
            table.onDoubleClick(action);
            return this;
        }
        
        public Builder<T> onEdit(Consumer<T> action) {
            table.onEdit(action);
            return this;
        }
        
        public Builder<T> onDelete(Consumer<T> action) {
            table.onDelete(action);
            return this;
        }
        
        public EnhancedTableView<T> build() {
            return table;
        }
    }
    
    /**
     * Factory pour les tables MAGSAV courantes
     */
    public static class MAGSAV {
        
        /**
         * Table pour les produits
         */
        public static EnhancedTableView<com.magsav.repo.ProductRepository.ProductRow> createProductTable() {
            return new Builder<com.magsav.repo.ProductRepository.ProductRow>()
                .column("ID", p -> String.valueOf(p.id()))
                .column("Nom", p -> p.nom())

                .column("Fabricant", p -> p.fabricant())
                .column("SN", p -> p.sn())
                .searchFilter(product -> {
                    String search = ""; // Sera mis à jour par le TextField
                    return product.nom().toLowerCase().contains(search) ||
                           (product.fabricant() != null && product.fabricant().toLowerCase().contains(search));
                })
                .build();
        }
        
        /**
         * Table pour les interventions
         */
        public static EnhancedTableView<com.magsav.model.InterventionRow> createInterventionTable() {
            return new Builder<com.magsav.model.InterventionRow>()
                .column("ID", i -> String.valueOf(i.id()))
                .column("Statut", i -> i.statut())
                .column("Panne", i -> i.panne())
                .column("Date entrée", i -> i.dateEntree())
                .column("Date sortie", i -> i.dateSortie())
                .searchFilter(intervention -> {
                    String search = ""; // Sera mis à jour par le TextField
                    return (intervention.statut() != null && intervention.statut().toLowerCase().contains(search)) ||
                           (intervention.panne() != null && intervention.panne().toLowerCase().contains(search));
                })
                .build();
        }
        
        /**
         * Table pour les sociétés (fabricants, fournisseurs, etc.)
         */
        public static EnhancedTableView<com.magsav.model.Societe> createSocieteTable() {
            return new Builder<com.magsav.model.Societe>()
                .column("ID", s -> String.valueOf(s.id()))
                .column("Nom", s -> s.nom())
                .column("Type", s -> s.type())
                .column("Email", s -> s.email())
                .column("Téléphone", s -> s.phone())
                .searchFilter(societe -> {
                    String search = ""; // Sera mis à jour par le TextField
                    return societe.nom().toLowerCase().contains(search) ||
                           (societe.email() != null && societe.email().toLowerCase().contains(search));
                })
                .build();
        }
    }
}