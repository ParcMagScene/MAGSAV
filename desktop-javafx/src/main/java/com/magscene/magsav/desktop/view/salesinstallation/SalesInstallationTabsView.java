package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vue avec onglets pour Ventes et Installations
 * Regroupe les Projets et les Contrats
 */
public class SalesInstallationTabsView extends VBox {

    private final ApiService apiService;
    private CustomTabPane tabPane;

    public SalesInstallationTabsView(ApiService apiService) {
        this.apiService = apiService;
        initialize();
    }

    private void initialize() {
        // Configuration de la vue principale
        this.getStyleClass().add("sales-installation-tabs-view");
        this.setSpacing(0);
        this.setFillWidth(true);

        try {
            // Création du CustomTabPane (comme dans VehicleManagerView)
            System.out.println("🔨 Création CustomTabPane pour Ventes & Installations...");
            tabPane = new CustomTabPane();
            System.out.println("✅ CustomTabPane créé");

            // Onglet Projets (Ventes & Installations)
            System.out.println("🔨 Création onglet Projets...");
            ProjectManagerView projectsView = new ProjectManagerView(apiService);
            CustomTabPane.CustomTab projectsTab = new CustomTabPane.CustomTab("Projets", projectsView, "💼");
            tabPane.addTab(projectsTab);
            System.out.println("✅ Onglet Projets ajouté");

            // Onglet Contrats
            System.out.println("🔨 Création onglet Contrats...");
            ContractManagerView contractsView = new ContractManagerView(apiService);
            CustomTabPane.CustomTab contractsTab = new CustomTabPane.CustomTab("Contrats", contractsView, "📋");
            tabPane.addTab(contractsTab);
            System.out.println("✅ Onglet Contrats ajouté");

            System.out.println("✅ CustomTabPane configuré avec 2 onglets");

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la création des onglets:");
            e.printStackTrace();
        }

        // Ajout du CustomTabPane à la vue
        this.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        System.out.println("✅ Vue Ventes et Installations avec onglets CustomTabPane créée");
    }

    /**
     * Obtenir le CustomTabPane pour accès externe si nécessaire
     */
    public CustomTabPane getTabPane() {
        return tabPane;
    }
}
