package com.magsav.gui.dialogs;

import com.magsav.service.ShareService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Dialogues pour le système de partage MAGSAV
 */
public class ShareDialogs {
    
    /**
     * Dialogue de saisie d'adresse email
     */
    public static Optional<String> showEmailInputDialog(String defaultEmail) {
        TextInputDialog dialog = new TextInputDialog(defaultEmail);
        dialog.setTitle("Adresse Email");
        dialog.setHeaderText("Partage par email");
        dialog.setContentText("Adresse email destinataire:");
        
        // Validation en temps réel
        TextField emailField = dialog.getEditor();
        ButtonType okButton = dialog.getDialogPane().getButtonTypes()
            .stream()
            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
            .findFirst()
            .orElse(ButtonType.OK);
        
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(okButton);
        
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            okBtn.setDisable(!ShareService.isValidEmail(newVal));
        });
        
        // Validation initiale
        okBtn.setDisable(!ShareService.isValidEmail(emailField.getText()));
        
        return dialog.showAndWait();
    }
    
    /**
     * Dialogue de configuration email Gmail
     */
    public static Optional<EmailConfig> showEmailConfigDialog() {
        Dialog<EmailConfig> dialog = new Dialog<>();
        dialog.setTitle("Configuration Email");
        dialog.setHeaderText("Configuration Gmail pour MAGSAV");
        
        // Boutons
        ButtonType configButtonType = new ButtonType("Configurer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(configButtonType, ButtonType.CANCEL);
        
        // Interface
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField emailField = new TextField();
        emailField.setPromptText("votre.email@gmail.com");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe d'application (16 caractères)");
        
        grid.add(new Label("Email Gmail:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Mot de passe app:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        // Instructions
        TextArea instructions = new TextArea();
        instructions.setText("""
            Configuration Gmail requise:
            
            1. Activez l'authentification à 2 facteurs sur votre compte Google
            2. Allez dans "Sécurité" → "Mots de passe des applications"
            3. Générez un mot de passe d'application pour "MAGSAV"
            4. Utilisez ce mot de passe (16 caractères) ci-dessus
            
            ⚠️ N'utilisez JAMAIS votre mot de passe principal Gmail ici !
            """);
        instructions.setEditable(false);
        instructions.setPrefRowCount(6);
        instructions.setMaxHeight(120);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(grid, instructions);
        dialog.getDialogPane().setContent(content);
        
        // Validation
        Button configButton = (Button) dialog.getDialogPane().lookupButton(configButtonType);
        configButton.setDisable(true);
        
        Runnable validation = () -> {
            boolean emailValid = ShareService.isValidEmail(emailField.getText());
            boolean passwordValid = passwordField.getText().length() >= 10; // Minimum raisonnable
            configButton.setDisable(!(emailValid && passwordValid));
        };
        
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validation.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validation.run());
        
        // Résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == configButtonType) {
                return new EmailConfig(emailField.getText(), passwordField.getText());
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Dialogue de progression pour les opérations longues
     */
    public static ProgressDialog showProgressDialog(String title, String message) {
        return new ProgressDialog(title, message);
    }
    
    /**
     * Affiche un dialogue de résultat de partage
     */
    public static void showShareResultDialog(ShareService.ShareResult result, String productName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résultat du Partage");
        alert.setHeaderText("Partage de: " + productName);
        
        StringBuilder content = new StringBuilder();
        content.append("Résultats:\n\n");
        content.append("📄 Export: ").append(result.exportSuccess ? "✅ Réussi" : "❌ Échec").append("\n");
        content.append("📧 Email: ").append(result.emailSuccess ? "✅ Envoyé" : "❌ Échec").append("\n");
        content.append("🖨️ Impression: ").append(result.printSuccess ? "✅ Ouvert" : "❌ Échec").append("\n");
        
        if (result.exportFile != null) {
            content.append("\n📁 Fichier: ").append(result.exportFile.getFileName());
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * Dialogue de succès simple
     */
    public static void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ Opération réussie");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Dialogue d'erreur avec détails
     */
    public static void showErrorDialog(String title, String message, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌ Erreur");
        alert.setContentText(message);
        
        // Si exception fournie, ajouter les détails
        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();
            
            Label label = new Label("Détails de l'erreur:");
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
            
            alert.getDialogPane().setExpandableContent(expContent);
        }
        
        alert.showAndWait();
    }
    
    /**
     * Dialogue de confirmation
     */
    public static boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Confirmation");
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Configuration email simple
     */
    public static class EmailConfig {
        public final String email;
        public final String password;
        
        public EmailConfig(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    
    /**
     * Dialogue de progression personnalisé
     */
    public static class ProgressDialog {
        private final Alert alert;
        private final ProgressBar progressBar;
        private final Label messageLabel;
        
        public ProgressDialog(String title, String initialMessage) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText("Opération en cours...");
            
            VBox content = new VBox(10);
            messageLabel = new Label(initialMessage);
            progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(300);
            
            content.getChildren().addAll(messageLabel, progressBar);
            alert.getDialogPane().setContent(content);
            
            // Supprimer les boutons par défaut
            alert.getDialogPane().getButtonTypes().clear();
        }
        
        public void show() {
            Platform.runLater(() -> alert.show());
        }
        
        public void updateProgress(double progress) {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
        
        public void updateMessage(String message) {
            Platform.runLater(() -> messageLabel.setText(message));
        }
        
        public void close() {
            Platform.runLater(() -> alert.close());
        }
    }
}