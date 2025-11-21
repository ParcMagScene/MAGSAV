package com.magscene.magsav.desktop.view.base;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Vue de base pour tous les gestionnaires (Equipment, SAV, Clients, etc.)
 * Fournit une structure standard avec toolbar, contenu principal et statusbar
 */
public abstract class BaseManagerView<T> extends BorderPane {
    protected ToolBar toolbar;
    protected Pane mainContent;
    protected HBox statusBar;
    protected Label statusLabel;
    
    public BaseManagerView() {
        initializeLayout();
        setupStyling();
        initializeContent();
    }
    
    private void initializeLayout() {
        // Toolbar en haut
        toolbar = createToolbar();
        setTop(toolbar);
        
        // Contenu principal au centre
        mainContent = createMainContent();
        setCenter(mainContent);
        
        // Barre de statut en bas
        statusBar = createStatusBar();
        setBottom(statusBar);
    }
    
    /**
     * Crée la barre d'outils avec les actions principales
     */
    protected ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.getStyleClass().add("manager-toolbar");
        
        // Boutons standard
        Button btnAdd = new Button("➕ Ajouter");
        Button btnEdit = new Button("✏️ Modifier");
        Button btnDelete = new Button("🗑️ Supprimer");
        Button btnRefresh = new Button("🔄 Actualiser");
        
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> handleRefresh());
        
        toolbar.getItems().addAll(
            btnAdd, btnEdit, btnDelete,
            new Separator(),
            btnRefresh
        );
        
        // Ajouter les boutons spécifiques au module
        addCustomToolbarItems(toolbar);
        
        return toolbar;
    }
    
    /**
     * Crée le contenu principal (à implémenter dans les classes filles)
     */
    protected abstract Pane createMainContent();
    
    /**
     * Crée la barre de statut
     */
    protected HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("manager-statusbar");
        
        statusLabel = new Label("Prêt");
        statusLabel.getStyleClass().add("status-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label moduleLabel = new Label(getModuleName());
        moduleLabel.getStyleClass().add("module-label");
        
        statusBar.getChildren().addAll(statusLabel, spacer, moduleLabel);
        return statusBar;
    }
    
    /**
     * Configuration du style CSS
     */
    protected void setupStyling() {
        getStyleClass().add("base-manager-view");
        getStyleClass().add(getViewCssClass());
    }
    
    // === Méthodes abstraites à implémenter ===
    
    /**
     * Retourne le nom du module pour affichage
     */
    protected abstract String getModuleName();
    
    /**
     * Retourne la classe CSS spécifique à cette vue
     */
    protected abstract String getViewCssClass();
    
    /**
     * Initialise le contenu spécifique de la vue
     */
    protected abstract void initializeContent();
    
    // === Actions par défaut (à surcharger si nécessaire) ===
    
    protected void handleAdd() {
        updateStatus("Action: Ajouter " + getModuleName());
    }
    
    protected void handleEdit() {
        updateStatus("Action: Modifier " + getModuleName());
    }
    
    protected void handleDelete() {
        updateStatus("Action: Supprimer " + getModuleName());
    }
    
    protected void handleRefresh() {
        updateStatus("Actualisation en cours...");
        refresh();
        updateStatus("Actualisation terminée");
    }
    
    /**
     * Ajoute des boutons personnalisés à la toolbar
     */
    protected void addCustomToolbarItems(ToolBar toolbar) {
        // Implémentation par défaut vide; // Les classes filles peuvent surcharger pour ajouter leurs boutons
    }
    
    /**
     * Met à jour le message de statut
     */
    protected void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Récupère un service via ApplicationContext
     */
    protected <S> S getService(Class<S> serviceClass) {
        return com.magscene.magsav.desktop.core.di.ApplicationContext.getInstance().getInstance(serviceClass);
    }
    
    /**
     * Rafraîchit les données de la vue
     */
    public void refresh() {
        System.out.println("🔄 Rafraîchissement: " + getModuleName());
    }
}