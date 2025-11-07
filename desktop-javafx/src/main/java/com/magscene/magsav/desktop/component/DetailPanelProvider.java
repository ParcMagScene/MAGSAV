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
     * @return Les données pour générer le QR Code
     */
    String getQRCodeData();
    
    /**
     * @return Un VBox contenant les informations détaillées à afficher
     */
    VBox getDetailInfoContent();
    
    /**
     * @return L'identifiant unique de l'item
     */
    String getDetailId();
}