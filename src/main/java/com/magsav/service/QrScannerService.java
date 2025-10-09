package com.magsav.service;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service pour scanner les QR codes ou permettre la saisie manuelle d'UID
 * Pour l'instant, simulation par saisie manuelle en attendant l'intégration d'une vraie caméra
 */
public class QrScannerService {
    
    private static final Pattern UID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");
    
    /**
     * Simule le scan d'un QR code par une saisie manuelle
     * Dans une version future, ceci pourrait être remplacé par un vrai scanner de caméra
     * 
     * @return L'UID scanné ou saisi, ou Optional.empty() si annulé
     */
    public Optional<String> scanQrCode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Scanner QR Code");
        dialog.setHeaderText("Scan du QR Code du produit");
        dialog.setContentText("Scannez le QR Code ou saisissez l'UID manuellement:");
        
        // Configuration de la boîte de dialogue
        dialog.getEditor().setPromptText("Ex: ABC1234");
        dialog.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Conversion automatique en majuscules
            if (newText != null && !newText.equals(newText.toUpperCase())) {
                dialog.getEditor().setText(newText.toUpperCase());
            }
        });
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String uid = result.get().trim().toUpperCase();
            
            // Validation du format
            if (isValidUidFormat(uid)) {
                return Optional.of(uid);
            } else {
                showFormatError();
                return scanQrCode(); // Redemander si format invalide
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Valide le format d'un UID
     */
    private boolean isValidUidFormat(String uid) {
        return uid != null && UID_PATTERN.matcher(uid).matches();
    }
    
    /**
     * Affiche une erreur de format
     */
    private void showFormatError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Format invalide");
        alert.setHeaderText("Format UID incorrect");
        alert.setContentText("L'UID doit être au format: 3 lettres majuscules + 4 chiffres (ex: ABC1234)");
        alert.showAndWait();
    }
    
    /**
     * Permet la saisie manuelle d'un UID avec validation
     * 
     * @param title Titre de la boîte de dialogue
     * @param prompt Texte d'invite
     * @return L'UID saisi ou Optional.empty() si annulé
     */
    public Optional<String> saisirUidManuellement(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText("Saisie manuelle d'UID");
        dialog.setContentText(prompt);
        
        dialog.getEditor().setPromptText("Ex: ABC1234");
        dialog.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.equals(newText.toUpperCase())) {
                dialog.getEditor().setText(newText.toUpperCase());
            }
        });
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String uid = result.get().trim().toUpperCase();
            
            if (isValidUidFormat(uid)) {
                return Optional.of(uid);
            } else {
                showFormatError();
                return saisirUidManuellement(title, prompt);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Vérifie si un UID a un format valide (sans accès à la base de données)
     * 
     * @param uid L'UID à vérifier
     * @return true si le format est correct
     */
    public boolean hasValidFormat(String uid) {
        return isValidUidFormat(uid);
    }
}