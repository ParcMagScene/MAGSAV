package com.magscene.magsav.desktop.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Générateur simple de QR Code pour l'affichage dans les volets de détails
 * Note: Ceci est une implémentation basique pour la démonstration
 * Pour une utilisation en production, il faudrait utiliser une vraie librairie comme ZXing
 */
public class QRCodeGenerator {
    
    private static final int DEFAULT_SIZE = 100;
    
    /**
     * Génère une image QR Code simple pour les données fournies
     * Note: Ceci est une version simplifiée pour la démonstration
     */
    public static Image generateQRCode(String data, int size) {
        if (data == null || data.trim().isEmpty()) {
            return generatePlaceholderQR(size);
        }
        
        return generatePlaceholderQR(size);
    }
    
    public static Image generateQRCode(String data) {
        return generateQRCode(data, DEFAULT_SIZE);
    }
    
    /**
     * Génère un QR Code de placeholder avec un motif simple
     */
    private static Image generatePlaceholderQR(int size) {
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        
        // Pattern simple en damier pour simuler un QR Code
        int blockSize = size / 10;
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                boolean isBlack = false;
                
                // Bordure
                if (x < blockSize || x >= size - blockSize || 
                    y < blockSize || y >= size - blockSize) {
                    isBlack = true;
                }
                // Coins de positionnement
                else if ((x < 3 * blockSize && y < 3 * blockSize) ||
                         (x >= size - 3 * blockSize && y < 3 * blockSize) ||
                         (x < 3 * blockSize && y >= size - 3 * blockSize)) {
                    int relX = x % blockSize;
                    int relY = y % blockSize;
                    isBlack = (relX == 0 || relY == 0 || relX == blockSize - 1 || relY == blockSize - 1);
                }
                // Pattern central simple
                else if ((x / blockSize + y / blockSize) % 2 == 0) {
                    isBlack = true;
                }
                
                Color color = isBlack ? Color.BLACK : Color.WHITE;
                writer.setColor(x, y, color);
            }
        }
        
        return image;
    }
}
