package com.magscene.magsav.desktop.view.settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Interface de migration des marques depuis l'application desktop
 */
public class BrandMigrationView extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(BrandMigrationView.class);
    
    private final ApiService apiService;
    private final ObjectMapper objectMapper;
    
    // Composants UI
    private Label statusLabel;
    private TextArea reportArea;
    private Button generateReportButton;
    private Button migrateBrandsButton;
    private Button fullMigrationButton;
    private ProgressBar progressBar;
    private Label progressLabel;
    
    // Données du rapport
    private Map<String, Object> migrationReport;
    
    public BrandMigrationView() {
        this.apiService = new ApiService();
        this.objectMapper = new ObjectMapper();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // Charger le rapport initial
        generateReport();
    }
    
    private void initializeComponents() {
        // Titre
        Label titleLabel = new Label("Migration des Marques");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.getStyleClass().add("section-title");
        
        // Statut
        statusLabel = new Label("Prêt pour la migration");
        statusLabel.getStyleClass().add("status-label");
        
        // Zone de rapport
        reportArea = new TextArea();
        reportArea.setPrefRowCount(15);
        reportArea.setEditable(false);
        reportArea.getStyleClass().add("report-area");
        
        // Boutons
        generateReportButton = new Button("Générer Rapport");
        generateReportButton.getStyleClass().add("action-button");
        
        migrateBrandsButton = new Button("Migrer les Marques");
        migrateBrandsButton.getStyleClass().add("primary-button");
        
        fullMigrationButton = new Button("Migration Complète");
        fullMigrationButton.getStyleClass().add("danger-button");
        
        // Barre de progression
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        
        progressLabel = new Label("");
        progressLabel.setVisible(false);
        
        this.getChildren().addAll(
            titleLabel,
            createInfoSection(),
            statusLabel,
            reportArea,
            createButtonSection(),
            progressBar,
            progressLabel
        );
        
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.getStyleClass().add("brand-migration-view");
    }
    
    private VBox createInfoSection() {
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("info-section");
        infoBox.setPadding(new Insets(15));
        
        Label infoTitle = new Label("Information sur la Migration");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label infoText = new Label(
            "Cette migration centralise toutes les marques des équipements et véhicules\n" +
            "dans une table dédiée pour éviter les incohérences.\n\n" +
            "Étapes :\n" +
            "1. Générer un rapport pour voir les marques existantes\n" +
            "2. Migrer les marques vers la table centralisée\n" +
            "3. Mettre à jour les références (nécessite modification des entités)"
        );
        infoText.setWrapText(true);
        
        infoBox.getChildren().addAll(infoTitle, infoText);
        return infoBox;
    }
    
    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().addAll(
            generateReportButton,
            migrateBrandsButton,
            new Separator(),
            fullMigrationButton
        );
        return buttonBox;
    }
    
    private void setupLayout() {
        VBox.setVgrow(reportArea, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        generateReportButton.setOnAction(e -> generateReport());
        migrateBrandsButton.setOnAction(e -> migrateBrands());
        fullMigrationButton.setOnAction(e -> executeFullMigration());
    }
    
    private void generateReport() {
        Task<String> reportTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Génération du rapport de migration...");
                updateProgress(0.3, 1.0);
                
                String response = apiService.makeApiCall("/api/admin/brand-migration/report", "GET", null);
                updateProgress(0.7, 1.0);
                
                migrationReport = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                updateProgress(1.0, 1.0);
                
                return formatReport(migrationReport);
            }
        };
        
        reportTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                reportArea.setText(reportTask.getValue());
                updateStatusFromReport();
                hideProgress();
            });
        });
        
        reportTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                reportArea.setText("Erreur lors de la génération du rapport : " + reportTask.getException().getMessage());
                statusLabel.setText("Erreur lors du chargement du rapport");
                hideProgress();
            });
        });
        
        showProgress();
        progressBar.progressProperty().bind(reportTask.progressProperty());
        progressLabel.textProperty().bind(reportTask.messageProperty());
        
        Thread reportThread = new Thread(reportTask);
        reportThread.setDaemon(true);
        reportThread.start();
    }
    
    private void migrateBrands() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la Migration");
        confirmAlert.setHeaderText("Migration des Marques");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir migrer toutes les marques vers la table centralisée ?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        Task<String> migrationTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Migration des marques en cours...");
                updateProgress(0.5, 1.0);
                
                String response = apiService.makeApiCall("/api/admin/brand-migration/migrate-brands", "POST", null);
                Map<String, Object> result = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                
                updateProgress(1.0, 1.0);
                
                if (Boolean.TRUE.equals(result.get("success"))) {
                    return "Migration des marques réussie : " + result.get("message");
                } else {
                    throw new RuntimeException("Échec de la migration : " + result.get("message"));
                }
            }
        };
        
        migrationTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Migration Réussie");
                successAlert.setHeaderText("Marques Migrées");
                successAlert.setContentText(migrationTask.getValue());
                successAlert.showAndWait();
                
                // Rafraîchir le rapport
                generateReport();
                hideProgress();
            });
        });
        
        migrationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de Migration");
                errorAlert.setHeaderText("Échec de la Migration");
                errorAlert.setContentText("Erreur : " + migrationTask.getException().getMessage());
                errorAlert.showAndWait();
                hideProgress();
            });
        });
        
        showProgress();
        progressBar.progressProperty().bind(migrationTask.progressProperty());
        progressLabel.textProperty().bind(migrationTask.messageProperty());
        
        Thread migrationThread = new Thread(migrationTask);
        migrationThread.setDaemon(true);
        migrationThread.start();
    }
    
    private void executeFullMigration() {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("Migration Complète");
        warningAlert.setHeaderText("ATTENTION - Migration Complète");
        warningAlert.setContentText(
            "Cette opération va :\n" +
            "1. Migrer toutes les marques vers la table centralisée\n" +
            "2. Tenter de mettre à jour les références (peut échouer si les entités ne sont pas modifiées)\n\n" +
            "Continuer ?"
        );
        
        if (warningAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        Task<String> fullMigrationTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Migration complète en cours...");
                updateProgress(0.5, 1.0);
                
                String response = apiService.makeApiCall("/api/admin/brand-migration/full-migration", "POST", null);
                Map<String, Object> result = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                
                updateProgress(1.0, 1.0);
                
                if (Boolean.TRUE.equals(result.get("success"))) {
                    return "Migration complète réussie : " + result.get("message");
                } else {
                    throw new RuntimeException("Échec de la migration complète : " + result.get("message"));
                }
            }
        };
        
        fullMigrationTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Migration Complète Réussie");
                successAlert.setHeaderText("Migration Terminée");
                successAlert.setContentText(fullMigrationTask.getValue());
                successAlert.showAndWait();
                
                generateReport();
                hideProgress();
            });
        });
        
        fullMigrationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de Migration Complète");
                errorAlert.setHeaderText("Échec de la Migration");
                errorAlert.setContentText("Erreur : " + fullMigrationTask.getException().getMessage());
                errorAlert.showAndWait();
                hideProgress();
            });
        });
        
        showProgress();
        progressBar.progressProperty().bind(fullMigrationTask.progressProperty());
        progressLabel.textProperty().bind(fullMigrationTask.messageProperty());
        
        Thread migrationThread = new Thread(fullMigrationTask);
        migrationThread.setDaemon(true);
        migrationThread.start();
    }
    
    private String formatReport(Map<String, Object> report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RAPPORT DE MIGRATION DES MARQUES ===\n\n");
        
        sb.append("STATISTIQUES :\n");
        sb.append("- Marques dans les équipements : ").append(report.get("equipmentBrandCount")).append("\n");
        sb.append("- Marques dans les véhicules : ").append(report.get("vehicleBrandCount")).append("\n");
        sb.append("- Marques déjà centralisées : ").append(report.get("existingBrandCount")).append("\n");
        sb.append("- Total marques uniques : ").append(report.get("totalUniqueBrands")).append("\n\n");
        
        sb.append("MARQUES DES ÉQUIPEMENTS :\n");
        @SuppressWarnings("unchecked")
        List<String> equipmentBrands = (List<String>) report.get("equipmentBrands");
        if (equipmentBrands != null && !equipmentBrands.isEmpty()) {
            for (String brand : equipmentBrands) {
                sb.append("- ").append(brand).append("\n");
            }
        } else {
            sb.append("Aucune marque trouvée dans les équipements\n");
        }
        
        sb.append("\nMARQUES DES VÉHICULES :\n");
        @SuppressWarnings("unchecked")
        List<String> vehicleBrands = (List<String>) report.get("vehicleBrands");
        if (vehicleBrands != null && !vehicleBrands.isEmpty()) {
            for (String brand : vehicleBrands) {
                sb.append("- ").append(brand).append("\n");
            }
        } else {
            sb.append("Aucune marque trouvée dans les véhicules\n");
        }
        
        sb.append("\nMARQUES DÉJÀ CENTRALISÉES :\n");
        @SuppressWarnings("unchecked")
        List<String> existingBrands = (List<String>) report.get("existingBrands");
        if (existingBrands != null && !existingBrands.isEmpty()) {
            for (String brand : existingBrands) {
                sb.append("- ").append(brand).append("\n");
            }
        } else {
            sb.append("Aucune marque centralisée existante\n");
        }
        
        sb.append("\nTOUTES LES MARQUES UNIQUES À MIGRER :\n");
        @SuppressWarnings("unchecked")
        List<String> allBrands = (List<String>) report.get("allUniqueBrandsList");
        if (allBrands != null && !allBrands.isEmpty()) {
            for (String brand : allBrands) {
                sb.append("- ").append(brand).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private void updateStatusFromReport() {
        if (migrationReport != null) {
            Integer totalBrands = (Integer) migrationReport.get("totalUniqueBrands");
            Integer existingBrands = (Integer) migrationReport.get("existingBrandCount");
            
            if (totalBrands != null && existingBrands != null) {
                if (totalBrands.equals(existingBrands)) {
                    statusLabel.setText("✅ Toutes les marques sont déjà migrées");
                    migrateBrandsButton.setDisable(true);
                } else {
                    statusLabel.setText("⚠️ " + (totalBrands - existingBrands) + " marques à migrer");
                    migrateBrandsButton.setDisable(false);
                }
            }
        }
    }
    
    private void showProgress() {
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        generateReportButton.setDisable(true);
        migrateBrandsButton.setDisable(true);
        fullMigrationButton.setDisable(true);
    }
    
    private void hideProgress() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        generateReportButton.setDisable(false);
        // migrateBrandsButton sera réactivé selon le statut
        fullMigrationButton.setDisable(false);
    }
}