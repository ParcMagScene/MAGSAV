package com.magsav.util;

import javafx.scene.control.*;
import javafx.scene.Node;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire générique pour le système de protection d'édition
 * Permet de mettre en place facilement un mode lecture/édition sur les formulaires
 */
public class EditModeManager {
    
    private boolean isEditMode = false;
    private final List<Node> editableControls = new ArrayList<>();
    private final Map<String, Object> originalValues = new HashMap<>();
    
    // Boutons de contrôle
    private Button btnEdit;
    private Button btnSave;
    private Button btnCancel;
    
    // Callbacks
    private Runnable onSave;
    private Runnable onCancel;
    private Runnable onModeChanged;
    
    /**
     * Constructeur
     */
    public EditModeManager() {
    }
    
    /**
     * Configure les boutons de contrôle
     */
    public EditModeManager setControlButtons(Button btnEdit, Button btnSave, Button btnCancel) {
        this.btnEdit = btnEdit;
        this.btnSave = btnSave;
        this.btnCancel = btnCancel;
        
        // Configurer les actions des boutons
        if (btnEdit != null) {
            btnEdit.setOnAction(e -> toggleEditMode());
        }
        if (btnSave != null) {
            btnSave.setOnAction(e -> saveChanges());
        }
        if (btnCancel != null) {
            btnCancel.setOnAction(e -> cancelChanges());
        }
        
        return this;
    }
    
    /**
     * Ajoute un contrôle modifiable à la gestion
     */
    public EditModeManager addEditableControl(Node control) {
        editableControls.add(control);
        return this;
    }
    
    /**
     * Ajoute plusieurs contrôles modifiables
     */
    public EditModeManager addEditableControls(Node... controls) {
        for (Node control : controls) {
            addEditableControl(control);
        }
        return this;
    }
    
    /**
     * Configure le callback de sauvegarde
     */
    public EditModeManager setOnSave(Runnable onSave) {
        this.onSave = onSave;
        return this;
    }
    
    /**
     * Configure le callback d'annulation
     */
    public EditModeManager setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }
    
    /**
     * Configure le callback de changement de mode
     */
    public EditModeManager setOnModeChanged(Runnable onModeChanged) {
        this.onModeChanged = onModeChanged;
        return this;
    }
    
    /**
     * Sauvegarde les valeurs actuelles des contrôles
     */
    public void saveCurrentValues() {
        originalValues.clear();
        for (Node control : editableControls) {
            String key = control.getId() != null ? control.getId() : control.toString();
            originalValues.put(key, getCurrentValue(control));
        }
    }
    
    /**
     * Restaure les valeurs originales des contrôles
     */
    public void restoreOriginalValues() {
        for (Node control : editableControls) {
            String key = control.getId() != null ? control.getId() : control.toString();
            Object originalValue = originalValues.get(key);
            if (originalValue != null) {
                setControlValue(control, originalValue);
            }
        }
    }
    
    /**
     * Initialise en mode lecture seule
     */
    public void initializeReadOnlyMode() {
        isEditMode = false;
        updateControls();
    }
    
    /**
     * Bascule entre mode lecture et édition
     */
    public void toggleEditMode() {
        if (!isEditMode) {
            // Sauvegarder les valeurs actuelles avant d'entrer en mode édition
            saveCurrentValues();
        }
        isEditMode = !isEditMode;
        updateControls();
        
        if (onModeChanged != null) {
            onModeChanged.run();
        }
    }
    
    /**
     * Active le mode édition
     */
    public void enableEditMode() {
        if (!isEditMode) {
            saveCurrentValues();
            isEditMode = true;
            updateControls();
            
            if (onModeChanged != null) {
                onModeChanged.run();
            }
        }
    }
    
    /**
     * Active le mode lecture seule
     */
    public void enableReadOnlyMode() {
        if (isEditMode) {
            isEditMode = false;
            updateControls();
            
            if (onModeChanged != null) {
                onModeChanged.run();
            }
        }
    }
    
    /**
     * Sauvegarde les changements
     */
    private void saveChanges() {
        if (onSave != null) {
            onSave.run();
        }
        
        // Sauvegarder les nouvelles valeurs comme originales
        saveCurrentValues();
        
        // Revenir en mode lecture
        isEditMode = false;
        updateControls();
    }
    
    /**
     * Annule les changements
     */
    private void cancelChanges() {
        // Restaurer les valeurs originales
        restoreOriginalValues();
        
        if (onCancel != null) {
            onCancel.run();
        }
        
        // Revenir en mode lecture
        isEditMode = false;
        updateControls();
    }
    
    /**
     * Met à jour l'état de tous les contrôles
     */
    private void updateControls() {
        // Mettre à jour les contrôles modifiables
        for (Node control : editableControls) {
            setControlEditable(control, isEditMode);
        }
        
        // Mettre à jour la visibilité des boutons
        if (btnEdit != null) btnEdit.setVisible(!isEditMode);
        if (btnSave != null) btnSave.setVisible(isEditMode);
        if (btnCancel != null) btnCancel.setVisible(isEditMode);
    }
    
    /**
     * Récupère la valeur actuelle d'un contrôle
     */
    private Object getCurrentValue(Node control) {
        if (control instanceof TextField) {
            return ((TextField) control).getText();
        } else if (control instanceof TextArea) {
            return ((TextArea) control).getText();
        } else if (control instanceof ComboBox) {
            return ((ComboBox<?>) control).getValue();
        } else if (control instanceof CheckBox) {
            return ((CheckBox) control).isSelected();
        }
        return null;
    }
    
    /**
     * Définit la valeur d'un contrôle
     */
    private void setControlValue(Node control, Object value) {
        if (control instanceof TextField && value instanceof String) {
            ((TextField) control).setText((String) value);
        } else if (control instanceof TextArea && value instanceof String) {
            ((TextArea) control).setText((String) value);
        } else if (control instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<Object> comboBox = (ComboBox<Object>) control;
            comboBox.setValue(value);
        } else if (control instanceof CheckBox && value instanceof Boolean) {
            ((CheckBox) control).setSelected((Boolean) value);
        }
    }
    
    /**
     * Active/désactive l'édition d'un contrôle
     */
    private void setControlEditable(Node control, boolean editable) {
        if (control instanceof TextField) {
            ((TextField) control).setEditable(editable);
        } else if (control instanceof TextArea) {
            ((TextArea) control).setEditable(editable);
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).setDisable(!editable);
        } else if (control instanceof CheckBox) {
            ((CheckBox) control).setDisable(!editable);
        } else {
            // Pour les autres types de contrôles, utiliser disable
            control.setDisable(!editable);
        }
    }
    
    /**
     * Retourne l'état actuel du mode d'édition
     */
    public boolean isEditMode() {
        return isEditMode;
    }
}