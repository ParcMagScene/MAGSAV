package com.magscene.magsav.desktop.component;

import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;

/**
 * Conteneur qui intègre automatiquement un volet de détails
 * à une TableView ou ListView existante
 */
public class DetailPanelContainer extends StackPane {
    
    private HBox mainContainer;
    private Region contentRegion;
    private DetailPanel detailPanel;
    
    public DetailPanelContainer(Region contentRegion) {
        this.contentRegion = contentRegion;
        this.detailPanel = new DetailPanel();
        
        setupContainer();
        setupListeners();
    }
    
    private void setupContainer() {
        mainContainer = new HBox();
        
        // Le contenu principal prend tout l'espace disponible
        HBox.setHgrow(contentRegion, Priority.ALWAYS);
        
        // Le volet de détails a une largeur fixe mais n'est visible que quand nécessaire
        detailPanel.setPrefWidth(400);
        detailPanel.setMaxWidth(400);
        detailPanel.setMinWidth(400);
        
        mainContainer.getChildren().addAll(contentRegion, detailPanel);
        getChildren().add(mainContainer);
    }
    
    @SuppressWarnings("unchecked")
    private void setupListeners() {
        // Si le contenu est une TableView
        if (contentRegion instanceof TableView) {
            TableView<Object> tableView = (TableView<Object>) contentRegion;
            
            tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && newSelection instanceof DetailPanelProvider) {
                    showDetailPanel((DetailPanelProvider) newSelection);
                } else if (newSelection == null) {
                    hideDetailPanel();
                }
            });
        }
        // Si le contenu est une ListView
        else if (contentRegion instanceof ListView) {
            ListView<Object> listView = (ListView<Object>) contentRegion;
            
            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && newSelection instanceof DetailPanelProvider) {
                    showDetailPanel((DetailPanelProvider) newSelection);
                } else if (newSelection == null) {
                    hideDetailPanel();
                }
            });
        }
    }
    
    private void showDetailPanel(DetailPanelProvider provider) {
        detailPanel.updateContent(
            provider.getDetailTitle(),
            provider.getDetailSubtitle(), 
            provider.getDetailImage(),
            null, // Le QR code sera généré automatiquement
            provider.getDetailInfoContent()
        );
        detailPanel.show();
    }
    
    private void hideDetailPanel() {
        detailPanel.hide();
    }
    
    public DetailPanel getDetailPanel() {
        return detailPanel;
    }
    
    public Region getContentRegion() {
        return contentRegion;
    }
    
    /**
     * Méthode utilitaire pour encapsuler facilement une TableView
     */
    public static DetailPanelContainer wrapTableView(TableView<?> tableView) {
        return new DetailPanelContainer(tableView);
    }
    
    /**
     * Méthode utilitaire pour encapsuler facilement une ListView
     */
    public static DetailPanelContainer wrapListView(ListView<?> listView) {
        return new DetailPanelContainer(listView);
    }
}