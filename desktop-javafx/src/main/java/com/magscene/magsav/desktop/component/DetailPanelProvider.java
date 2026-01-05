package com.magscene.magsav.desktop.component;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Interface pour les objets qui peuvent être affichés dans le volet de détails
 */
public interface DetailPanelProvider {

    /**
     * @return Le titre principal à afficher
     */
    String getDetailTitle();

    /**
     * @return Le sous-titre ou description courte
     */
    String getDetailSubtitle();

    /**
     * @return L'image principale (photo, avatar, icône)
     */
    Image getDetailImage();

    /**
     * @return Les données pour générer le QR Code (null si pas de QR code)
     */
    default String getQRCodeData() {
        return null; // Par défaut, pas de QR code
    }

    /**
     * @return Un VBox contenant les informations détaillées à afficher
     */
    VBox getDetailInfoContent();

    /**
     * @return L'identifiant unique de l'item
     */
    String getDetailId();
    
    /**
     * @return Le nom de la marque pour charger le logo (null si pas de logo)
     */
    default String getBrandName() {
        return null;
    }
    
    /**
     * @return Le chemin/identifiant de la photo (LOCMAT ou nom) pour MediaService
     */
    default String getPhotoPath() {
        return null;
    }
}
