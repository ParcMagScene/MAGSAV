package com.magscene.magsav.desktop.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération de QR Codes pour les équipements MAG SCENE.
 * Utilise la bibliothèque ZXing pour créer des QR codes de haute qualité.
 */
public class QRCodeService {
    
    private static final int DEFAULT_SIZE = 300;
    private static final int PRINT_SIZE = 600;
    private static final String QR_CODE_PREFIX = "MAGSAV:";
    
    private static QRCodeService instance;
    
    private QRCodeService() {}
    
    public static synchronized QRCodeService getInstance() {
        if (instance == null) {
            instance = new QRCodeService();
        }
        return instance;
    }
    
    /**
     * Génère une image QR code pour un UID donné.
     * @param uid L'identifiant unique de l'équipement (ex: "SON-001234")
     * @return Image JavaFX du QR code
     */
    public Image generateQRCodeImage(String uid) {
        return generateQRCodeImage(uid, DEFAULT_SIZE);
    }
    
    /**
     * Génère une image QR code avec une taille spécifique.
     * @param uid L'identifiant unique de l'équipement
     * @param size Taille en pixels (largeur = hauteur)
     * @return Image JavaFX du QR code
     */
    public Image generateQRCodeImage(String uid, int size) {
        try {
            String qrContent = QR_CODE_PREFIX + uid;
            BufferedImage bufferedImage = generateQRCodeBufferedImage(qrContent, size);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            System.err.println("Erreur lors de la génération du QR code pour " + uid + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Génère un QR code et le sauvegarde dans un fichier PNG.
     * @param uid L'identifiant unique de l'équipement
     * @param outputFile Fichier de destination
     * @return true si succès, false sinon
     */
    public boolean saveQRCodeToFile(String uid, File outputFile) {
        return saveQRCodeToFile(uid, outputFile, PRINT_SIZE);
    }
    
    /**
     * Génère un QR code et le sauvegarde dans un fichier PNG avec une taille spécifique.
     * @param uid L'identifiant unique de l'équipement
     * @param outputFile Fichier de destination
     * @param size Taille en pixels
     * @return true si succès, false sinon
     */
    public boolean saveQRCodeToFile(String uid, File outputFile, int size) {
        try {
            String qrContent = QR_CODE_PREFIX + uid;
            BufferedImage image = generateQRCodeBufferedImage(qrContent, size);
            ImageIO.write(image, "PNG", outputFile);
            return true;
        } catch (WriterException | IOException e) {
            System.err.println("Erreur lors de la sauvegarde du QR code pour " + uid + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Génère plusieurs QR codes et les sauvegarde dans un répertoire.
     * @param uids Liste des UIDs à générer
     * @param outputDir Répertoire de destination
     * @return Nombre de QR codes générés avec succès
     */
    public int generateBulkQRCodes(java.util.List<String> uids, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        int successCount = 0;
        for (String uid : uids) {
            File outputFile = new File(outputDir, uid + ".png");
            if (saveQRCodeToFile(uid, outputFile)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * Vérifie si un UID est un équipement MAG SCENE (commence par une catégorie standard).
     * Les équipements MAG SCENE ont un UID format: CAT-NNNNNN (ex: SON-001234)
     * Les équipements externes ont un format: XXX-NNNNNN (ex: ARS-001234 pour ARSUD)
     */
    public boolean isMagSceneEquipment(String uid) {
        if (uid == null || uid.isEmpty()) {
            return false;
        }
        
        // Liste des préfixes de catégories MAG SCENE
        String[] magSceneCategories = {
            "SON", "ECL", "VID", "STR", "ENE", "DIV", "OUT", "VEH", "STO", "CAB", "INF"
        };
        
        String prefix = uid.length() >= 3 ? uid.substring(0, 3).toUpperCase() : "";
        
        for (String category : magSceneCategories) {
            if (prefix.equals(category)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Génère un BufferedImage du QR code.
     */
    private BufferedImage generateQRCodeBufferedImage(String content, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        
        // Configuration des couleurs (noir sur blanc)
        MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
    }
    
    /**
     * Génère le contenu encodé dans le QR code pour un équipement.
     * Format: MAGSAV:UID
     */
    public String getQRCodeContent(String uid) {
        return QR_CODE_PREFIX + uid;
    }
    
    /**
     * Extrait l'UID depuis le contenu scanné d'un QR code.
     * @param scannedContent Le contenu scanné
     * @return L'UID ou null si le format est invalide
     */
    public String extractUidFromScan(String scannedContent) {
        if (scannedContent == null || !scannedContent.startsWith(QR_CODE_PREFIX)) {
            return null;
        }
        return scannedContent.substring(QR_CODE_PREFIX.length());
    }
    
    /**
     * Retourne le chemin par défaut pour sauvegarder les QR codes.
     */
    public Path getDefaultQRCodeDirectory() {
        String userHome = System.getProperty("user.home");
        Path qrDir = Path.of(userHome, "magsav", "qrcodes");
        try {
            Files.createDirectories(qrDir);
        } catch (IOException e) {
            System.err.println("Impossible de créer le répertoire QR codes: " + e.getMessage());
        }
        return qrDir;
    }
}
