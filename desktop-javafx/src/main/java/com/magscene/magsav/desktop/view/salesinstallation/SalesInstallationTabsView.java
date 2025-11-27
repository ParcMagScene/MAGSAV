package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vue avec onglets pour Ventes et Installations
 * Regroupe les Projets et les Contrats avec toolbar adaptative
 */
public class SalesInstallationTabsView extends VBox {

    private final ApiService apiService;
    private CustomTabPane tabPane;
    private HBox adaptiveToolbar;
    private ProjectManagerView projectsView;
    private ContractManagerView contractsView;
    private Node projectsToolbar;
    private Node contractsToolbar;

    public SalesInstallationTabsView(ApiService apiService) {
        this.apiService = apiService;
        initialize();
    }

    private void initialize() {
        // Configuration de la vue principale - UTILISE ThemeConstants
        this.getStyleClass().add("sales-installation-tabs-view");
        this.setSpacing(0);
        this.setFillWidth(true);
        this.setPadding(ThemeConstants.PADDING_STANDARD); // 7px uniformisé

        try {
            // Toolbar adaptative en haut - UTILISE ThemeConstants
            adaptiveToolbar = new HBox();
            adaptiveToolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            adaptiveToolbar.setPadding(ThemeConstants.TOOLBAR_PADDING); // 10px standardisé
            adaptiveToolbar.getStyleClass().add(ThemeConstants.UNIFIED_TOOLBAR_CLASS);

            // Création du CustomTabPane
            System.out.println("🔨 Création CustomTabPane pour Ventes & Installations...");
            tabPane = new CustomTabPane();
            System.out.println("✅ CustomTabPane créé");

            // Onglet Projets (Ventes & Installations)
            System.out.println("🔨 Création onglet Projets...");
            projectsView = new ProjectManagerView(apiService);
            projectsToolbar = projectsView.getTop(); // Sauvegarder la toolbar
            projectsView.setTop(null); // Retirer la toolbar de la vue pour l'afficher en haut
            CustomTabPane.CustomTab projectsTab = new CustomTabPane.CustomTab("Projets", projectsView, "💼");
            tabPane.addTab(projectsTab);
            System.out.println("✅ Onglet Projets ajouté");

            // Onglet Contrats
            System.out.println("🔨 Création onglet Contrats...");
            contractsView = new ContractManagerView(apiService);
            contractsToolbar = contractsView.getTop(); // Sauvegarder la toolbar
            contractsView.setTop(null); // Retirer la toolbar de la vue pour l'afficher en haut
            CustomTabPane.CustomTab contractsTab = new CustomTabPane.CustomTab("Contrats", contractsView, "📋");
            tabPane.addTab(contractsTab);
            System.out.println("✅ Onglet Contrats ajouté");

            // Écouter les changements d'onglet pour mettre à jour la toolbar
            tabPane.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
                updateToolbarForSelectedTab(newTab);
            });

            // Initialiser la toolbar avec le premier onglet
            updateToolbarForSelectedTab(tabPane.getSelectedTab());

            System.out.println("✅ CustomTabPane configuré avec 2 onglets");

            // Assemblage : Toolbar adaptative puis TabPane
            this.getChildren().addAll(adaptiveToolbar, tabPane);
            VBox.setVgrow(tabPane, Priority.ALWAYS);

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la création des onglets:");
            e.printStackTrace();
        }

        System.out.println("✅ Vue Ventes et Installations avec toolbar adaptative créée");
    }

    /**
     * Met à jour la toolbar en fonction de l'onglet sélectionné
     */
    private void updateToolbarForSelectedTab(CustomTabPane.CustomTab selectedTab) {
        if (selectedTab == null)
            return;

        adaptiveToolbar.getChildren().clear();

        String tabText = selectedTab.getText();
        if (tabText.contains("Projets")) {
            // Copier le CONTENU de la toolbar au lieu de la toolbar elle-même
            if (projectsToolbar != null && projectsToolbar instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox sourceToolbar = (javafx.scene.layout.HBox) projectsToolbar;
                adaptiveToolbar.getChildren().addAll(sourceToolbar.getChildren());
            }
        } else if (tabText.contains("Contrats")) {
            // Copier le CONTENU de la toolbar au lieu de la toolbar elle-même
            if (contractsToolbar != null && contractsToolbar instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox sourceToolbar = (javafx.scene.layout.HBox) contractsToolbar;
                adaptiveToolbar.getChildren().addAll(sourceToolbar.getChildren());
            }
        }
    }

    /**
     * Obtenir le CustomTabPane pour accès externe si nécessaire
     */
    public CustomTabPane getTabPane() {
        return tabPane;
    }
}
