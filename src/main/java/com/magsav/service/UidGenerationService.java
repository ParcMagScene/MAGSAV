package com.magsav.service;

import com.magsav.repo.ProductRepository;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Service pour générer des UID uniques et leurs QR codes correspondants
 */
public class UidGenerationService {
    
    private static final int UID_LENGTH = 7; // Format actuel: 3 lettres + 4 chiffres
    private static final Random random = new SecureRandom();
    
    private final ProductRepository productRepo;
    
    public UidGenerationService() {
        this.productRepo = new ProductRepository();
    }
    
    public UidGenerationService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }
    
    /**
     * Génère un UID unique pour un nouveau produit
     * Format: 3 lettres aléatoires + 4 chiffres (ex: ABC1234)
     * 
     * @return Un UID unique non existant dans la base
     */
    public String generateUniqueUid() {
        String uid;
        int attempts = 0;
        final int maxAttempts = 1000; // Protection contre boucle infinie
        
        do {
            uid = generateRandomUid();
            attempts++;
            
            if (attempts > maxAttempts) {
                throw new RuntimeException("Impossible de générer un UID unique après " + maxAttempts + " tentatives");
            }
            
        } while (productRepo.existsUid(uid));
        
        return uid;
    }
    
    /**
     * Génère un UID aléatoire au format ABC1234
     */
    private String generateRandomUid() {
        StringBuilder uid = new StringBuilder(UID_LENGTH);
        
        // 3 premières lettres
        for (int i = 0; i < 3; i++) {
            char letter = (char) ('A' + random.nextInt(26));
            uid.append(letter);
        }
        
        // 4 chiffres
        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(10);
            uid.append(digit);
        }
        
        return uid.toString();
    }
    
    /**
     * Valide le format d'un UID
     * 
     * @param uid L'UID à valider
     * @return true si le format est valide (3 lettres + 4 chiffres)
     */
    public boolean isValidUidFormat(String uid) {
        if (uid == null || uid.length() != UID_LENGTH) {
            return false;
        }
        
        // Vérifier les 3 premières lettres
        for (int i = 0; i < 3; i++) {
            char c = uid.charAt(i);
            if (!Character.isLetter(c) || !Character.isUpperCase(c)) {
                return false;
            }
        }
        
        // Vérifier les 4 derniers chiffres
        for (int i = 3; i < 7; i++) {
            char c = uid.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Normalise un UID (met en majuscules, supprime les espaces)
     */
    public String normalizeUid(String uid) {
        if (uid == null) return null;
        return uid.trim().toUpperCase();
    }
    
    /**
     * Vérifie si un UID existe déjà dans la base
     */
    public boolean uidExists(String uid) {
        if (uid == null || uid.trim().isEmpty()) return false;
        return productRepo.existsUid(normalizeUid(uid));
    }
    
    /**
     * Génère le nom du fichier QR code pour un UID donné
     * 
     * @param uid L'UID du produit
     * @return Le nom du fichier QR code (ex: "ABC1234.png")
     */
    public String getQrCodeFileName(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID ne peut pas être vide");
        }
        
        return normalizeUid(uid) + ".png";
    }
    
    /**
     * Génère le chemin complet du fichier QR code
     * 
     * @param uid L'UID du produit
     * @return Le chemin complet vers le fichier QR code
     */
    public String getQrCodePath(String uid) {
        String fileName = getQrCodeFileName(uid);
        return "medias/qrcodes/" + fileName;
    }
    
    /**
     * Record pour représenter un nouveau produit avec UID généré
     */
    public record NewProductWithUid(
        String uid,
        String qrCodeFileName,
        String qrCodePath
    ) {}
    
    /**
     * Génère un nouvel UID avec les informations QR code associées
     */
    public NewProductWithUid generateNewProductUid() {
        String uid = generateUniqueUid();
        String qrFileName = getQrCodeFileName(uid);
        String qrPath = getQrCodePath(uid);
        
        return new NewProductWithUid(uid, qrFileName, qrPath);
    }
}