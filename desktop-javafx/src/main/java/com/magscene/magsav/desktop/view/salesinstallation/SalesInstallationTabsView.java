package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.service.ApiService;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vue avec onglets pour Ventes et Installations
 * Regroupe les Projets et les Contrats
 */
public class SalesInstallationTabsView extends VBox {
    
    private final ApiService apiService;
    private TabPane tabPane;
    
    public SalesInstallationTabsView(ApiService apiService) {
        this.apiService = apiService;
        initialize();
    }
    
    private void initialize() {
        // Configuration de la vue principale
        this.getStyleClass().add("sales-installation-tabs-view");
        
        // Création du TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("sales-tabs");
        
        // Onglet Projets (Ventes & Installations)
        Tab projectsTab = new Tab("💼 Projets");
        projectsTab.setContent(new ProjectManagerView(apiService));
        
        // Onglet Contrats
        Tab contractsTab = new Tab("📋 Contrats");
        contractsTab.setContent(new ContractManagerView(apiService));
        
        // Ajout des onglets
        tabPane.getTabs().addAll(projectsTab, contractsTab);
        
        // Ajout du TabPane à la vue
        this.getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        System.out.println("✅ Vue Ventes et Installations avec onglets créée");
    }
    
    /**
     * Sélectionner un onglet spécifique
     */
    public void selectTab(int index) {
        if (index >= 0 && index < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(index);
        }
    }
    
    /**
     * Obtenir l'index de l'onglet sélectionné
     */
    public int getSelectedTabIndex() {
        return tabPane.getSelectionModel().getSelectedIndex();
    }
}
