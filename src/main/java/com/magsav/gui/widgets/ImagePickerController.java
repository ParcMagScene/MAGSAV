package com.magsav.gui.widgets;

import java.io.File;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImagePickerController implements Initializable {
  @FXML private TextField searchField;
  @FXML private FlowPane tiles;
  @FXML private Button btnSelect;

  private Path libraryDir;
  private Path selected;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tiles.setPadding(new Insets(10));
  }

  public void setLibraryDir(Path dir) {
    this.libraryDir = dir;
    try {
      Files.createDirectories(dir);
    } catch (Exception ignore) {
    }
    reload();
  }

  public Path getSelected() {
    return selected;
  }

  private void reload() {
    tiles.getChildren().clear();
    if (libraryDir == null) {
      return;
    }
    String q = searchField != null ? searchField.getText() : null;
    try {
      DirectoryStream.Filter<Path> filter =
          p -> {
            String n = p.getFileName().toString().toLowerCase();
            boolean img =
                n.endsWith(".png")
                    || n.endsWith(".jpg")
                    || n.endsWith(".jpeg")
                    || n.endsWith(".gif");
            if (!img) {
              return false;
            }
            if (q == null || q.isBlank()) {
              return true;
            }
            return n.contains(q.toLowerCase());
          };
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(libraryDir, filter)) {
        for (Path p : ds) addTile(p);
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void addTile(Path imagePath) {
    Image img = new Image(imagePath.toUri().toString(), 140, 140, true, true);
    ImageView iv = new ImageView(img);
    iv.setFitWidth(140);
    iv.setFitHeight(140);
    iv.setPreserveRatio(true);
    StackPane card = new StackPane(iv);
    card.setPadding(new Insets(3));
    card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4;");
    card.setOnMouseClicked(
        evt -> {
          if (evt.getButton() == MouseButton.PRIMARY) {
            this.selected = imagePath;
            highlight(card);
            if (evt.getClickCount() == 2) {
              handleSelect();
            }
          }
        });
    tiles.getChildren().add(card);
  }

  private void highlight(StackPane selCard) {
    for (var n : tiles.getChildren())
      n.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4;");
    selCard.setStyle(
        "-fx-border-color: #2e7d32; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4;");
  }

  @FXML
  private void handleImport() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Importer des images");
    fc.getExtensionFilters()
        .addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File file = fc.showOpenDialog(tiles.getScene().getWindow());
    if (file == null) {
      return;
    }
    try {
      Files.createDirectories(libraryDir);
      String base = file.getName();
      Path dest = libraryDir.resolve(base);
      int i = 1;
      while (Files.exists(dest)) {
        String name = base;
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        dest = libraryDir.resolve(stem + "-" + (i++) + ext);
      }
      Files.copy(file.toPath(), dest);
      this.selected = dest;
      reload();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  @FXML
  private void handleSelect() {
    Stage st = (Stage) tiles.getScene().getWindow();
    st.setUserData(selected);
    st.close();
  }

  @FXML
  private void handleCancel() {
    Stage st = (Stage) tiles.getScene().getWindow();
    st.setUserData(null);
    st.close();
  }

  @FXML
  private void handleSearch() {
    reload();
  }
}
