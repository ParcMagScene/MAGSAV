package com.magscene.magsav.desktop.service;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Service de gestion des préférences de fenêtres
 * Sauvegarde et restaure la taille et position des fenêtres
 */
public class WindowPreferencesService {

    private static final String PREFS_DIR = System.getProperty("user.home") + "/.magsav";
    private static final String PREFS_FILE = "window-preferences.properties";
    private static WindowPreferencesService instance;

    private final Properties properties;
    private final Path prefsPath;

    private WindowPreferencesService() {
        this.properties = new Properties();
        this.prefsPath = Paths.get(PREFS_DIR, PREFS_FILE);
        loadPreferences();
    }

    public static WindowPreferencesService getInstance() {
        if (instance == null) {
            instance = new WindowPreferencesService();
        }
        return instance;
    }

    /**
     * Charge les préférences depuis le fichier
     */
    private void loadPreferences() {
        try {
            // Créer le répertoire si nécessaire
            Files.createDirectories(Paths.get(PREFS_DIR));

            if (Files.exists(prefsPath)) {
                try (InputStream input = Files.newInputStream(prefsPath)) {
                    properties.load(input);
                    System.out.println("📁 Préférences fenêtre chargées: " + prefsPath);
                }
            } else {
                System.out.println("📁 Aucune préférence fenêtre trouvée, utilisation des valeurs par défaut");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Erreur lors du chargement des préférences: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde les préférences dans le fichier
     */
    private void savePreferences() {
        try {
            Files.createDirectories(Paths.get(PREFS_DIR));

            try (OutputStream output = Files.newOutputStream(prefsPath)) {
                properties.store(output, "MAGSAV Window Preferences");
                System.out.println("💾 Préférences fenêtre sauvegardées");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde la position et taille d'une fenêtre
     */
    public void saveWindowBounds(Stage stage, String windowId) {
        if (stage == null || windowId == null) {
            return;
        }

        properties.setProperty(windowId + ".x", String.valueOf(stage.getX()));
        properties.setProperty(windowId + ".y", String.valueOf(stage.getY()));
        properties.setProperty(windowId + ".width", String.valueOf(stage.getWidth()));
        properties.setProperty(windowId + ".height", String.valueOf(stage.getHeight()));
        properties.setProperty(windowId + ".maximized", String.valueOf(stage.isMaximized()));

        savePreferences();
    }

    /**
     * Restaure la position et taille d'une fenêtre
     */
    public void restoreWindowBounds(Stage stage, String windowId, double defaultWidth, double defaultHeight) {
        if (stage == null || windowId == null) {
            return;
        }

        try {
            String xStr = properties.getProperty(windowId + ".x");
            String yStr = properties.getProperty(windowId + ".y");
            String widthStr = properties.getProperty(windowId + ".width");
            String heightStr = properties.getProperty(windowId + ".height");
            String maximizedStr = properties.getProperty(windowId + ".maximized");

            if (widthStr != null && heightStr != null) {
                double width = Double.parseDouble(widthStr);
                double height = Double.parseDouble(heightStr);

                // Valider les dimensions (min 400x300, max taille écran)
                width = Math.max(400, Math.min(width, javafx.stage.Screen.getPrimary().getBounds().getWidth()));
                height = Math.max(300, Math.min(height, javafx.stage.Screen.getPrimary().getBounds().getHeight()));

                stage.setWidth(width);
                stage.setHeight(height);

                System.out.println("📐 Taille restaurée: " + width + "x" + height);
            } else {
                stage.setWidth(defaultWidth);
                stage.setHeight(defaultHeight);
            }

            if (xStr != null && yStr != null) {
                double x = Double.parseDouble(xStr);
                double y = Double.parseDouble(yStr);

                // Valider la position (doit être visible à l'écran)
                x = Math.max(0, Math.min(x, javafx.stage.Screen.getPrimary().getBounds().getWidth() - 100));
                y = Math.max(0, Math.min(y, javafx.stage.Screen.getPrimary().getBounds().getHeight() - 100));

                stage.setX(x);
                stage.setY(y);

                System.out.println("📍 Position restaurée: (" + x + ", " + y + ")");
            }

            if (maximizedStr != null && Boolean.parseBoolean(maximizedStr)) {
                stage.setMaximized(true);
                System.out.println("🔳 Fenêtre maximisée");
            }

        } catch (NumberFormatException e) {
            System.err.println("⚠️ Erreur de format dans les préférences, utilisation des valeurs par défaut");
            stage.setWidth(defaultWidth);
            stage.setHeight(defaultHeight);
        }
    }

    /**
     * Configure les listeners pour sauvegarder automatiquement lors des changements
     */
    public void setupAutoSave(Stage stage, String windowId) {
        if (stage == null || windowId == null) {
            return;
        }

        // Timer pour éviter les sauvegardes trop fréquentes
        final javafx.animation.PauseTransition saveDelay = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(500));
        saveDelay.setOnFinished(event -> saveWindowBounds(stage, windowId));

        // Sauvegarder lors de la fermeture (immédiat)
        stage.setOnCloseRequest(event -> {
            saveDelay.stop();
            saveWindowBounds(stage, windowId);
        });

        // Sauvegarder lors des changements de taille (avec délai)
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized()) {
                saveDelay.playFromStart();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized()) {
                saveDelay.playFromStart();
            }
        });

        // Sauvegarder lors des changements de position (avec délai)
        stage.xProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized()) {
                saveDelay.playFromStart();
            }
        });

        stage.yProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized()) {
                saveDelay.playFromStart();
            }
        });

        // Sauvegarder lors du changement d'état maximisé (immédiat)
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            saveDelay.stop();
            saveWindowBounds(stage, windowId);
        });

        System.out.println("🔄 Auto-sauvegarde activée pour: " + windowId);
    }

    /**
     * Efface les préférences d'une fenêtre spécifique
     */
    public void clearWindowPreferences(String windowId) {
        properties.remove(windowId + ".x");
        properties.remove(windowId + ".y");
        properties.remove(windowId + ".width");
        properties.remove(windowId + ".height");
        properties.remove(windowId + ".maximized");
        savePreferences();
    }

    /**
     * Efface toutes les préférences
     */
    public void clearAllPreferences() {
        properties.clear();
        savePreferences();
    }

    /**
     * Configure un Dialog pour qu'il mémorise sa taille et position
     * @param dialogPane Le DialogPane du Dialog
     * @param dialogId Identifiant unique du dialog (ex: "vehicle-dialog", "equipment-dialog")
     */
    public void setupDialogMemory(DialogPane dialogPane, String dialogId) {
        if (dialogPane == null || dialogId == null) {
            return;
        }

        // Attendre que le dialog soit affiché pour accéder à sa fenêtre
        dialogPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                javafx.stage.Window window = newScene.getWindow();
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;
                    
                    // Restaurer les dimensions au démarrage
                    Platform.runLater(() -> {
                        // Positionner le dialog sur le même écran que le owner
                        positionDialogOnOwnerScreen(stage);
                        
                        restoreWindowBounds(stage, dialogId, stage.getWidth(), stage.getHeight());
                    });
                    
                    // Configuration auto-save
                    setupAutoSave(stage, dialogId);
                }
            }
        });
    }

    /**
     * Positionne un dialog sur le même écran que sa fenêtre owner
     * @param dialogStage Le stage du dialog à positionner
     */
    private void positionDialogOnOwnerScreen(javafx.stage.Stage dialogStage) {
        javafx.stage.Window owner = dialogStage.getOwner();
        if (owner == null) {
            return;
        }

        // Trouver l'écran contenant le owner
        double ownerCenterX = owner.getX() + owner.getWidth() / 2;
        double ownerCenterY = owner.getY() + owner.getHeight() / 2;
        
        javafx.stage.Screen ownerScreen = null;
        for (javafx.stage.Screen screen : javafx.stage.Screen.getScreens()) {
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            if (bounds.contains(ownerCenterX, ownerCenterY)) {
                ownerScreen = screen;
                break;
            }
        }
        
        if (ownerScreen == null) {
            ownerScreen = javafx.stage.Screen.getPrimary();
        }
        
        // Centrer le dialog sur l'écran du owner
        javafx.geometry.Rectangle2D screenBounds = ownerScreen.getVisualBounds();
        double dialogWidth = dialogStage.getWidth() > 0 ? dialogStage.getWidth() : 600;
        double dialogHeight = dialogStage.getHeight() > 0 ? dialogStage.getHeight() : 400;
        
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - dialogWidth) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - dialogHeight) / 2;
        
        dialogStage.setX(centerX);
        dialogStage.setY(centerY);
    }
}
