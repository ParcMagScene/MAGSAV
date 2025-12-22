package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.QRCodeService;
import com.magscene.magsav.desktop.util.DialogUtils;
import com.magscene.magsav.desktop.view.equipment.EquipmentItem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog pour afficher et exporter les QR codes des équipements MAG SCENE.
 */
public class QRCodeDialog extends Stage {
    
    @SuppressWarnings("unused")
    private final List<EquipmentItem> equipments;
    private final QRCodeService qrCodeService;
    private final List<EquipmentItem> magSceneEquipments;
    
    private ImageView qrImageView;
    private Label uidLabel;
    private Label nameLabel;
    private ListView<EquipmentItem> equipmentListView;
    private int currentIndex = 0;
    
    public QRCodeDialog(List<EquipmentItem> equipments) {
        this.equipments = equipments;
        this.qrCodeService = QRCodeService.getInstance();
        
        // Filtrer uniquement les équipements MAG SCENE
        this.magSceneEquipments = equipments.stream()
                .filter(e -> qrCodeService.isMagSceneEquipment(e.getQrCode()))
                .collect(Collectors.toList());
        
        initializeDialog();
    }
    
    private void initializeDialog() {
        setTitle("QR Codes - Équipements MAG SCENE");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Panneau gauche : liste des équipements
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);
        
        // Panneau central : affichage QR code
        VBox centerPanel = createCenterPanel();
        root.setCenter(centerPanel);
        
        // Panneau bas : boutons d'action
        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);
        
        Scene scene = new Scene(root, 800, 600);
        setScene(scene);
        
        // Afficher le premier QR code si disponible
        if (!magSceneEquipments.isEmpty()) {
            displayQRCode(magSceneEquipments.get(0));
            equipmentListView.getSelectionModel().select(0);
        }
    }
    
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 15, 0, 0));
        panel.setPrefWidth(280);
        
        Label titleLabel = new Label("Équipements MAG SCENE");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label countLabel = new Label(magSceneEquipments.size() + " équipements");
        countLabel.setStyle("-fx-text-fill: #666;");
        
        equipmentListView = new ListView<>();
        equipmentListView.setPrefHeight(450);
        equipmentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EquipmentItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getQrCode() + " - " + item.getName());
                }
            }
        });
        
        equipmentListView.getItems().addAll(magSceneEquipments);
        equipmentListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        currentIndex = magSceneEquipments.indexOf(newVal);
                        displayQRCode(newVal);
                    }
                });
        
        panel.getChildren().addAll(titleLabel, countLabel, equipmentListView);
        return panel;
    }
    
    private VBox createCenterPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        uidLabel = new Label("");
        uidLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #2196F3;");
        
        nameLabel = new Label("");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(300);
        nameLabel.setAlignment(Pos.CENTER);
        
        qrImageView = new ImageView();
        qrImageView.setFitWidth(250);
        qrImageView.setFitHeight(250);
        qrImageView.setPreserveRatio(true);
        
        // Navigation
        HBox navButtons = new HBox(20);
        navButtons.setAlignment(Pos.CENTER);
        
        Button prevButton = new Button("◀ Précédent");
        prevButton.setOnAction(e -> navigatePrevious());
        
        Button nextButton = new Button("Suivant ▶");
        nextButton.setOnAction(e -> navigateNext());
        
        navButtons.getChildren().addAll(prevButton, nextButton);
        
        // Info sur le contenu du QR code
        Label infoLabel = new Label("Contenu: MAGSAV:[UID]");
        infoLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        
        panel.getChildren().addAll(uidLabel, nameLabel, qrImageView, navButtons, infoLabel);
        return panel;
    }
    
    private HBox createBottomPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(15, 0, 0, 0));
        panel.setAlignment(Pos.CENTER);
        
        Button exportCurrentButton = new Button("💾 Exporter ce QR code");
        exportCurrentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        exportCurrentButton.setOnAction(e -> exportCurrentQRCode());
        
        Button exportAllButton = new Button("📦 Exporter tous les QR codes");
        exportAllButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        exportAllButton.setOnAction(e -> exportAllQRCodes());
        
        Button closeButton = new Button("Fermer");
        closeButton.setOnAction(e -> close());
        
        panel.getChildren().addAll(exportCurrentButton, exportAllButton, closeButton);
        return panel;
    }
    
    private void displayQRCode(EquipmentItem item) {
        if (item == null) return;
        
        String uid = item.getQrCode();
        uidLabel.setText(uid);
        nameLabel.setText(item.getName() + (item.getBrand() != null && !item.getBrand().isEmpty() ? 
                " (" + item.getBrand() + ")" : ""));
        
        Image qrImage = qrCodeService.generateQRCodeImage(uid, 250);
        if (qrImage != null) {
            qrImageView.setImage(qrImage);
        }
    }
    
    private void navigatePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            equipmentListView.getSelectionModel().select(currentIndex);
            equipmentListView.scrollTo(currentIndex);
        }
    }
    
    private void navigateNext() {
        if (currentIndex < magSceneEquipments.size() - 1) {
            currentIndex++;
            equipmentListView.getSelectionModel().select(currentIndex);
            equipmentListView.scrollTo(currentIndex);
        }
    }
    
    private void exportCurrentQRCode() {
        if (magSceneEquipments.isEmpty() || currentIndex < 0) return;
        
        EquipmentItem item = magSceneEquipments.get(currentIndex);
        String uid = item.getQrCode();
        
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de destination");
        chooser.setInitialDirectory(qrCodeService.getDefaultQRCodeDirectory().toFile());
        
        File selectedDir = chooser.showDialog(this);
        if (selectedDir != null) {
            File outputFile = new File(selectedDir, uid + ".png");
            boolean success = qrCodeService.saveQRCodeToFile(uid, outputFile);
            
            if (success) {
                DialogUtils.showSuccess("Succès", 
                        "QR code exporté avec succès:\n" + outputFile.getAbsolutePath());
            } else {
                DialogUtils.showError("Erreur", 
                        "Impossible d'exporter le QR code.");
            }
        }
    }
    
    private void exportAllQRCodes() {
        if (magSceneEquipments.isEmpty()) {
            DialogUtils.showWarning("Attention", "Aucun équipement MAG SCENE à exporter.");
            return;
        }
        
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de destination");
        chooser.setInitialDirectory(qrCodeService.getDefaultQRCodeDirectory().toFile());
        
        File selectedDir = chooser.showDialog(this);
        if (selectedDir != null) {
            List<String> uids = magSceneEquipments.stream()
                    .map(EquipmentItem::getQrCode)
                    .filter(uid -> uid != null && !uid.isEmpty())
                    .collect(Collectors.toList());
            
            int successCount = qrCodeService.generateBulkQRCodes(uids, selectedDir);
            
            DialogUtils.showSuccess("Export terminé", 
                    successCount + " / " + uids.size() + " QR codes exportés avec succès dans:\n" + 
                    selectedDir.getAbsolutePath());
        }
    }
}
