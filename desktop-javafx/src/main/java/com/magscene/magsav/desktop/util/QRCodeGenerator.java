package com.magscene.magsav.desktop.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Générateur de QR Code utilisant ZXing pour l'affichage dans les volets de détails
 */
public class QRCodeGenerator {
    
    private static final int DEFAULT_SIZE = 100;
    
    /**
     * Génère une image QR Code pour les données fournies avec ZXing
     */
    public static Image generateQRCode(String data, int size) {
        if (data == null || data.trim().isEmpty()) {
            return generatePlaceholderQR(size);
        }
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            
            WritableImage image = new WritableImage(size, size);
            PixelWriter writer = image.getPixelWriter();
            
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    Color color = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                    writer.setColor(x, y, color);
                }
            }
            
            return image;
        } catch (WriterException e) {
            System.err.println("Erreur génération QR Code: " + e.getMessage());
            return generatePlaceholderQR(size);
        }
    }
    
    public static Image generateQRCode(String data) {
        return generateQRCode(data, DEFAULT_SIZE);
    }
    
    /**
     * Génère un QR Code de placeholder en cas d'erreur
     */
    private static Image generatePlaceholderQR(int size) {
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        
        // Remplir de blanc avec un X noir pour indiquer l'absence de données
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                boolean isBlack = (x == y) || (x == size - 1 - y); // X diagonal
                Color color = isBlack ? Color.GRAY : Color.WHITE;
                writer.setColor(x, y, color);
            }
        }
        
        return image;
    }
}
