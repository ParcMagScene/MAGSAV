package com.magsav.gui.util;

import com.magsav.config.Config; // ajuster le package si besoin
import java.nio.file.Path;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class WindowPrefs {
  private WindowPrefs() {}

  public static void apply(Stage stage, String prefix) {
    try {
      Path p = Path.of("application.yml");
      Config cfg = p.toFile().exists() ? Config.load(p) : new Config();
      String sx = cfg.get(prefix + ".x", null);
      String sy = cfg.get(prefix + ".y", null);
      String sw = cfg.get(prefix + ".w", null);
      String sh = cfg.get(prefix + ".h", null);
      boolean maximized = cfg.getBoolean(prefix + ".maximized", false);

      if (sx != null && sy != null) {
        try {
          stage.setX(Double.parseDouble(sx));
        } catch (Exception ignored) {
        }
        try {
          stage.setY(Double.parseDouble(sy));
        } catch (Exception ignored) {
        }
      }
      if (sw != null && sh != null) {
        try {
          stage.setWidth(Double.parseDouble(sw));
        } catch (Exception ignored) {
        }
        try {
          stage.setHeight(Double.parseDouble(sh));
        } catch (Exception ignored) {
        }
      }

      try {
        double w = stage.getWidth();
        double h = stage.getHeight();
        if (w <= 0 || h <= 0) {
          Rectangle2D v = Screen.getPrimary().getVisualBounds();
          stage.setWidth(Math.min(w <= 0 ? 800 : w, v.getWidth()));
          stage.setHeight(Math.min(h <= 0 ? 600 : h, v.getHeight()));
          stage.setX(v.getMinX() + (v.getWidth() - stage.getWidth()) / 2);
          stage.setY(v.getMinY() + (v.getHeight() - stage.getHeight()) / 2);
        } else {
          var screens =
              Screen.getScreensForRectangle(
                  stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
          if (screens == null || screens.isEmpty()) {
            Rectangle2D v = Screen.getPrimary().getVisualBounds();
            double nx =
                Math.max(v.getMinX(), Math.min(stage.getX(), v.getMaxX() - stage.getWidth()));
            double ny =
                Math.max(v.getMinY(), Math.min(stage.getY(), v.getMaxY() - stage.getHeight()));
            stage.setX(nx);
            stage.setY(ny);
          }
        }
      } catch (Exception ignored) {
      }
      stage.setMaximized(maximized);
    } catch (Exception ignored) {
    }
  }

  public static void save(Stage stage, String prefix) {
    try {
      Path p = Path.of("application.yml");
      Config cfg = p.toFile().exists() ? Config.load(p) : new Config();
      cfg.set(prefix + ".x", Double.toString(stage.getX()));
      cfg.set(prefix + ".y", Double.toString(stage.getY()));
      cfg.set(prefix + ".w", Double.toString(stage.getWidth()));
      cfg.set(prefix + ".h", Double.toString(stage.getHeight()));
      cfg.set(prefix + ".maximized", Boolean.toString(stage.isMaximized()));
      cfg.save(p);
    } catch (Exception ignored) {
    }
  }
}
