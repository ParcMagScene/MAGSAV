package com.magscene.magsav.desktop.view.base;

import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Vue de base pour tous les gestionnaires (Equipment, SAV, Clients, etc.)
 * Fournit une structure standard avec toolbar, contenu principal et statusbar
 */
public abstract class BaseManagerView<T> extends BorderPane {
    protected HBox toolbar;
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

        // Barre de statut SUPPRIMÉE (superflu)
        // statusBar = createStatusBar();
        // setBottom(statusBar);
    }

    /**
     * Crée la barre d'outils avec les actions principales (organisation Client)
     */
    protected HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("unified-toolbar");

        // Boutons d'action avec ViewUtils (comme dans ClientManagerView)
        Button btnAdd = ViewUtils.createAddButton("➕ Ajouter", this::handleAdd);
        Button btnEdit = ViewUtils.createEditButton("✏️ Modifier", this::handleEdit, null);
        Button btnDelete = ViewUtils.createDeleteButton("🗑️ Supprimer", this::handleDelete, null);

        // ActionsBox à droite comme dans ClientManagerView
        VBox actionsBox = ViewUtils.createActionsBox("⚡ Actions", btnAdd, btnEdit, btnDelete);

        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Ajouter les éléments personnalisés au début (filtres, recherche, etc.)
        addCustomToolbarItems(toolbar);

        // Ajouter spacer et actions à la fin
        toolbar.getChildren().addAll(spacer, actionsBox);

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
        setPadding(new Insets(7));
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
     * Ajoute des boutons personnalisés à la toolbar (HBox)
     */
    protected void addCustomToolbarItems(HBox toolbar) {
        // Implémentation par défaut vide; // Les classes filles peuvent surcharger pour
        // ajouter leurs boutons
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