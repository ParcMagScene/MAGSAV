package com.magsav.util;

import com.magsav.service.AddressService;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

/**
 * Utilitaire pour ajouter facilement l'autocomplétion d'adresse aux formulaires
 */
public class AddressAutocompleteUtil {
    
    private static final AddressService addressService = new AddressService();
    
    /**
     * Ajoute l'autocomplétion d'adresse à un TextField
     */
    public static void setupFor(TextField textField) {
        if (textField != null) {
            addressService.setupAddressAutocomplete(textField);
        }
    }
    
    /**
     * Ajoute l'autocomplétion d'adresse à un TextArea
     */
    public static void setupFor(TextArea textArea) {
        if (textArea != null) {
            addressService.setupAddressAutocompleteForTextArea(textArea);
        }
    }
    
    /**
     * Valide si une adresse semble être française
     */
    public static boolean isValidFrenchAddress(String address) {
        return addressService.isValidFrenchAddress(address);
    }
}