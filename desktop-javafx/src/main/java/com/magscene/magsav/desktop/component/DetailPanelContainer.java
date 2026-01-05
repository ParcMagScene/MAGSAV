package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.view.equipment.EquipmentItem;
import com.magscene.magsav.desktop.view.VehicleItem;

import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Conteneur qui intègre automatiquement un volet de détails
 * à une TableView ou ListView existante
 * Supporte l'affichage des photos et logos de marques
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

        // Le volet de détails a une largeur fixe mais n'est visible que quand
        // nécessaire
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
                if (newSelection != null) {
                    if (newSelection instanceof DetailPanelProvider) {
                        showDetailPanel((DetailPanelProvider) newSelection);
                    }
                    // Sinon afficher le volet sera géré automatiquement par les classes wrapper des
                    // vues
                } else {
                    hideDetailPanel();
                }
            });
        }
        // Si le contenu est une ListView
        else if (contentRegion instanceof ListView) {
            ListView<Object> listView = (ListView<Object>) contentRegion;

            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    if (newSelection instanceof DetailPanelProvider) {
                        showDetailPanel((DetailPanelProvider) newSelection);
                    }
                    // Sinon afficher le volet sera géré automatiquement par les classes wrapper des
                    // vues
                } else {
                    hideDetailPanel();
                }
            });
        }
    }

    private void showDetailPanel(DetailPanelProvider provider) {
        // Si c'est un EquipmentItem, utiliser les informations de photo et logo
        if (provider instanceof com.magscene.magsav.desktop.view.sav.SAVRequestItem) {
            com.magscene.magsav.desktop.view.sav.SAVRequestItem savItem = (com.magscene.magsav.desktop.view.sav.SAVRequestItem) provider;
            detailPanel.updateContentWithAnimation(
                    provider.getDetailTitle(),
                    provider.getDetailSubtitle(),
                    provider.getDetailImage(),
                    provider.getQRCodeData(),
                    provider.getDetailInfoContent(),
                    provider.getPhotoPath(),
                    provider.getBrandName(),
                    null);

            // Ajout du bouton Valider si admin et demande à valider (statut OPEN)
            if (com.magscene.magsav.desktop.service.CurrentUser.isAdmin() && savItem.canBeValidated()) {
                javafx.scene.control.Button validateBtn = new javafx.scene.control.Button("Valider");
                validateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 18; -fx-background-radius: 6;");
                validateBtn.setOnAction(e -> {
                    // TODO: Appeler la méthode de validation (à implémenter selon votre logique)
                    System.out.println("[ADMIN] Validation de la demande SAV " + savItem.getId());
                    // Exemple : savService.validateSAVRequest(savItem.getId());
                });
                // Ajout du bouton en bas du panneau de détail
                detailPanel.getChildren().add(validateBtn);
            }
        } else if (provider instanceof EquipmentItem) {
            EquipmentItem item = (EquipmentItem) provider;
            // Utiliser l'animation bidirectionnelle pour un effet de transition fluide
            detailPanel.updateContentWithAnimation(
                    provider.getDetailTitle(),
                    provider.getDetailSubtitle(),
                    provider.getDetailImage(),
                    provider.getQRCodeData(),
                    provider.getDetailInfoContent(),
                    item.getPhotoPath(),
                    item.getBrand(),
                    item.getEquipmentImage());
        } else if (provider instanceof VehicleItem) {
            // Si c'est un VehicleItem, utiliser photo (gauche) et logo (droite)
            VehicleItem vehicle = (VehicleItem) provider;
            detailPanel.updateContentWithAnimation(
                    provider.getDetailTitle(),
                    provider.getDetailSubtitle(),
                    provider.getDetailImage(),
                    provider.getQRCodeData(),
                    provider.getDetailInfoContent(),
                    vehicle.getPhotoPath(),
                    vehicle.getBrand(),
                    vehicle.getVehicleImage(180, 140));  // Taille plus grande pour le volet
        } else {
            // Pour les autres types (SAV, etc.), utiliser les méthodes de l'interface
            // qui peuvent retourner null par défaut ou être surchargées
            String brandName = provider.getBrandName();
            String photoPath = provider.getPhotoPath();
            
            if (brandName != null || photoPath != null) {
                // Provider avec marque/photo (SAVRequestItem par exemple)
                detailPanel.updateContentWithAnimation(
                        provider.getDetailTitle(),
                        provider.getDetailSubtitle(),
                        provider.getDetailImage(),
                        provider.getQRCodeData(),
                        provider.getDetailInfoContent(),
                        photoPath,
                        brandName,
                        null);  // Pas d'image pré-chargée
            } else {
                // Utiliser l'animation bidirectionnelle pour un effet de transition fluide
                detailPanel.updateContentWithAnimation(
                        provider.getDetailTitle(),
                        provider.getDetailSubtitle(),
                        provider.getDetailImage(),
                        provider.getQRCodeData(),
                        provider.getDetailInfoContent());
            }
        }
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
