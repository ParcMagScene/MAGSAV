package com.magsav.ui.components;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Gestionnaire de validation des formulaires JavaFX
 * Permet de valider les champs et d'afficher des messages d'erreur visuels
 */
public class FormValidator {
    
    private final List<ValidationRule> rules = new ArrayList<>();
    private final List<Control> errorControls = new ArrayList<>();
    
    /**
     * Règle de validation pour un contrôle
     */
    public static class ValidationRule {
        private final Control control;
        private final Function<Control, String> valueExtractor;
        private final Predicate<String> validator;
        private final String errorMessage;
        
        public ValidationRule(Control control, Function<Control, String> valueExtractor, 
                             Predicate<String> validator, String errorMessage) {
            this.control = control;
            this.valueExtractor = valueExtractor;
            this.validator = validator;
            this.errorMessage = errorMessage;
        }
        
        public Control getControl() { return control; }
        public String getValue() { return valueExtractor.apply(control); }
        public boolean isValid() { return validator.test(getValue()); }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Ajoute une règle de validation pour un TextField
     */
    public FormValidator addTextFieldRule(TextField field, Predicate<String> validator, String errorMessage) {
        rules.add(new ValidationRule(field, c -> ((TextField) c).getText(), validator, errorMessage));
        return this;
    }
    
    /**
     * Ajoute une règle de validation pour un TextArea
     */
    public FormValidator addTextAreaRule(TextArea area, Predicate<String> validator, String errorMessage) {
        rules.add(new ValidationRule(area, c -> ((TextArea) c).getText(), validator, errorMessage));
        return this;
    }
    
    /**
     * Ajoute une règle de validation pour un ComboBox
     */
    @SuppressWarnings("unchecked")
    public <T> FormValidator addComboBoxRule(ComboBox<T> combo, Predicate<T> validator, String errorMessage) {
        rules.add(new ValidationRule(combo, 
            c -> {
                ComboBox<T> cb = (ComboBox<T>) c;
                return cb.getValue() != null ? cb.getValue().toString() : "";
            },
            value -> validator.test(combo.getValue()),
            errorMessage));
        return this;
    }
    
    /**
     * Valide tous les champs du formulaire
     * @return ValidationResult contenant le statut et les erreurs
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        clearVisualErrors();
        
        for (ValidationRule rule : rules) {
            if (!rule.isValid()) {
                errors.add(rule.getErrorMessage());
                applyErrorStyle(rule.getControl());
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Valide et affiche les erreurs si nécessaire
     * @return true si la validation est réussie
     */
    public boolean validateAndShow() {
        ValidationResult result = validate();
        if (!result.isValid()) {
            AlertManager.showWarning("Erreurs de validation", 
                "Veuillez corriger les erreurs suivantes :\n\n• " + 
                String.join("\n• ", result.getErrors()));
        }
        return result.isValid();
    }
    
    /**
     * Applique un style visuel d'erreur au contrôle
     */
    private void applyErrorStyle(Control control) {
        // Bordure rouge
        control.setStyle("-fx-border-color: #ff6b6b; -fx-border-width: 2px; -fx-border-radius: 3px;");
        
        // Ombre rouge
        DropShadow errorEffect = new DropShadow();
        errorEffect.setColor(Color.valueOf("#ff6b6b"));
        errorEffect.setRadius(5);
        errorEffect.setSpread(0.3);
        control.setEffect(errorEffect);
        
        errorControls.add(control);
    }
    
    /**
     * Supprime les styles d'erreur de tous les contrôles
     */
    public void clearVisualErrors() {
        for (Control control : errorControls) {
            control.setStyle("");
            control.setEffect(null);
        }
        errorControls.clear();
    }
    
    /**
     * Résultat de validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public String getFirstError() { return errors.isEmpty() ? null : errors.get(0); }
    }
    
    /**
     * Validateurs prédéfinis courants
     */
    public static class Validators {
        
        public static Predicate<String> required() {
            return value -> value != null && !value.trim().isEmpty();
        }
        
        public static Predicate<String> minLength(int minLength) {
            return value -> value != null && value.trim().length() >= minLength;
        }
        
        public static Predicate<String> maxLength(int maxLength) {
            return value -> value == null || value.trim().length() <= maxLength;
        }
        
        public static Predicate<String> lengthBetween(int min, int max) {
            return value -> {
                if (value == null) return min == 0;
                int length = value.trim().length();
                return length >= min && length <= max;
            };
        }
        
        public static Predicate<String> email() {
            Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            return value -> value == null || value.trim().isEmpty() || pattern.matcher(value.trim()).matches();
        }
        
        public static Predicate<String> phone() {
            Pattern pattern = Pattern.compile("^[+]?[0-9\\s\\-\\(\\)\\.]{8,20}$");
            return value -> value == null || value.trim().isEmpty() || pattern.matcher(value.trim()).matches();
        }
        
        public static Predicate<String> numeric() {
            return value -> {
                if (value == null || value.trim().isEmpty()) return true;
                try {
                    Double.parseDouble(value.trim());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }
        
        public static Predicate<String> positiveNumber() {
            return value -> {
                if (!numeric().test(value)) return false;
                if (value == null || value.trim().isEmpty()) return true;
                try {
                    double d = Double.parseDouble(value.trim());
                    return d > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }
        
        public static Predicate<String> regex(String pattern) {
            Pattern p = Pattern.compile(pattern);
            return value -> value == null || value.trim().isEmpty() || p.matcher(value.trim()).matches();
        }
        
        /**
         * Validateurs spécifiques à MAGSAV
         */
        public static class MAGSAV {
            
            public static Predicate<String> productUID() {
                Pattern pattern = Pattern.compile("^[A-Z]{3}\\d{4}$");
                return value -> value != null && pattern.matcher(value.trim().toUpperCase()).matches();
            }
            
            public static Predicate<String> productName() {
                return lengthBetween(2, 100);
            }
            
            public static Predicate<String> societeName() {
                return lengthBetween(2, 100);
            }
            
            public static Predicate<String> categoryName() {
                return lengthBetween(2, 50);
            }
            
            public static Predicate<String> serialNumber() {
                return value -> value == null || value.trim().isEmpty() || 
                               (value.trim().length() >= 3 && value.trim().length() <= 50);
            }
        }
    }
    
    /**
     * Builder pour créer facilement des formulaires validés
     */
    public static class Builder {
        private final FormValidator validator = new FormValidator();
        
        public Builder requiredTextField(TextField field, String fieldName) {
            validator.addTextFieldRule(field, Validators.required(), 
                fieldName + " est obligatoire");
            return this;
        }
        
        public Builder textField(TextField field, String fieldName, int minLength, int maxLength) {
            validator.addTextFieldRule(field, Validators.lengthBetween(minLength, maxLength),
                fieldName + " doit contenir entre " + minLength + " et " + maxLength + " caractères");
            return this;
        }
        
        public Builder emailField(TextField field) {
            validator.addTextFieldRule(field, Validators.email(),
                "Format d'email invalide");
            return this;
        }
        
        public Builder phoneField(TextField field) {
            validator.addTextFieldRule(field, Validators.phone(),
                "Format de téléphone invalide");
            return this;
        }
        
        public Builder productNameField(TextField field) {
            validator.addTextFieldRule(field, Validators.MAGSAV.productName(),
                "Le nom du produit doit contenir entre 2 et 100 caractères");
            return this;
        }
        
        public Builder productUIDField(TextField field) {
            validator.addTextFieldRule(field, Validators.MAGSAV.productUID(),
                "L'UID doit respecter le format ABC1234 (3 lettres + 4 chiffres)");
            return this;
        }
        
        public Builder requiredComboBox(ComboBox<?> combo, String fieldName) {
            validator.addComboBoxRule(combo, value -> value != null,
                "Veuillez sélectionner " + fieldName);
            return this;
        }
        
        public FormValidator build() {
            return validator;
        }
    }
}