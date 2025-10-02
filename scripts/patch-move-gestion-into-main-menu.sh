#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

FILE="src/main/java/com/magsav/gui/MAGSAVApp.java"
[[ -f "$FILE" ]] || { echo "Introuvable: $FILE"; exit 1; }

cp "$FILE" "$FILE.bak"

# 1) Insérer les méthodes juste avant la dernière '}' si absentes
if ! grep -q 'integrateGestionIntoMainMenu2(' "$FILE"; then
  awk '
    { lines[NR]=$0 }
    END {
      lastclose=0
      for (i=NR; i>=1; i--) { if (lines[i] ~ /\}/) { lastclose=i; break } }
      for (i=1; i<=NR; i++) {
        if (i==lastclose) {
          print "    // === Ajout auto: déplacement du menu Gestion dans le menu principal ===";
          print "    private static void integrateGestionIntoMainMenu2(javafx.stage.Stage stage) {";
          print "        if (stage == null) return;";
          print "        javafx.scene.Scene scene = stage.getScene();";
          print "        if (scene == null) return;";
          print "        javafx.scene.Parent root = scene.getRoot();";
          print "        if (root == null) return;";
          print "        javafx.scene.control.MenuBar bar = findOrCreateMenuBar2(root);";
          print "        if (bar == null) return;";
          print "        javafx.scene.control.Menu main = findMainMenu2(bar);";
          print "        if (main == null) { main = new javafx.scene.control.Menu(\"Menu\"); bar.getMenus().add(0, main); }";
          print "        javafx.scene.control.Menu gestionTop = null;";
          print "        for (javafx.scene.control.Menu m : bar.getMenus()) { if (\"Gestion\".equalsIgnoreCase(m.getText())) { gestionTop = m; break; } }";
          print "        for (javafx.scene.control.MenuItem it : main.getItems()) { if (it instanceof javafx.scene.control.Menu m && \"Gestion\".equalsIgnoreCase(m.getText())) return; }";
          print "        javafx.scene.control.Menu toInsert = (gestionTop != null) ? gestionTop : com.magsav.gui.menu.GestionMenu.build();";
          print "        if (gestionTop != null) { bar.getMenus().remove(gestionTop); }";
          print "        if (!main.getItems().isEmpty() && !(main.getItems().get(main.getItems().size()-1) instanceof javafx.scene.control.SeparatorMenuItem)) {";
          print "            main.getItems().add(new javafx.scene.control.SeparatorMenuItem());";
          print "        }";
          print "        main.getItems().add(toInsert);";
          print "    }";
          print "";
          print "    private static javafx.scene.control.MenuBar findOrCreateMenuBar2(javafx.scene.Parent root) {";
          print "        javafx.scene.control.MenuBar mb = findFirstMenuBar2(root);";
          print "        if (mb != null) return mb;";
          print "        mb = new javafx.scene.control.MenuBar();";
          print "        if (root instanceof javafx.scene.layout.BorderPane bp) {";
          print "            javafx.scene.Node top = bp.getTop();";
          print "            if (top == null) { bp.setTop(mb); }";
          print "            else if (top instanceof javafx.scene.layout.VBox vbox) { vbox.getChildren().add(0, mb); }";
          print "            else { javafx.scene.layout.VBox v = new javafx.scene.layout.VBox(); v.getChildren().add(mb); v.getChildren().add(top); bp.setTop(v); }";
          print "            return mb;";
          print "        }";
          print "        javafx.scene.Scene sc = root.getScene();";
          print "        if (sc != null) {";
          print "            javafx.scene.layout.VBox container = new javafx.scene.layout.VBox();";
          print "            container.getChildren().add(mb);";
          print "            container.getChildren().add(root);";
          print "            sc.setRoot(container);";
          print "            return mb;";
          print "        }";
          print "        return null;";
          print "    }";
          print "";
          print "    private static javafx.scene.control.MenuBar findFirstMenuBar2(javafx.scene.Parent root) {";
          print "        java.util.Deque<javafx.scene.Parent> stack = new java.util.ArrayDeque<>();";
          print "        stack.push(root);";
          print "        while (!stack.isEmpty()) {";
          print "            javafx.scene.Parent p = stack.pop();";
          print "            if (p instanceof javafx.scene.control.MenuBar mb) return mb;";
          print "            for (javafx.scene.Node n : p.getChildrenUnmodifiable()) {";
          print "                if (n instanceof javafx.scene.control.MenuBar mb2) return mb2;";
          print "                if (n instanceof javafx.scene.Parent pr) stack.push(pr);";
          print "            }";
          print "        }";
          print "        return null;";
          print "    }";
          print "";
          print "    private static javafx.scene.control.Menu findMainMenu2(javafx.scene.control.MenuBar bar) {";
          print "        if (bar.getMenus().isEmpty()) return null;";
          print "        java.util.List<String> preferred = java.util.Arrays.asList(\"Menu\", \"Fichier\", \"Principal\", \"Accueil\", \"Main\", \"File\");";
          print "        for (String name : preferred) { for (javafx.scene.control.Menu m : bar.getMenus()) { if (name.equalsIgnoreCase(m.getText())) return m; } }";
          print "        return bar.getMenus().get(0);";
          print "    }";
          print "    // === Fin ajout auto ===";
        }
        print lines[i];
      }
    }
  ' "$FILE" > "$FILE.tmp"
  mv "$FILE.tmp" "$FILE"
fi

# 2) Ajouter l’appel après stage.show() (sinon après setScene) s’il n’existe pas
if ! grep -q 'integrateGestionIntoMainMenu2(stage)' "$FILE"; then
  awk '
    BEGIN { inserted=0 }
    {
      print
      if (!inserted && $0 ~ /stage\.show\(\)\s*;/) {
        print "        javafx.application.Platform.runLater(() -> com.magsav.gui.MAGSAVApp.integrateGestionIntoMainMenu2(stage));"
        inserted=1
      }
    }
    END {
      if (!inserted) {
        # seconde passe: insérer après la première setScene(...)
      }
    }
  ' "$FILE" > "$FILE.tmp1"

  if ! grep -q 'integrateGestionIntoMainMenu2(stage)' "$FILE.tmp1"; then
    awk '
      BEGIN { inserted=0 }
      {
        print
        if (!inserted && $0 ~ /stage\.setScene\(.+\)\s*;/) {
          print "        javafx.application.Platform.runLater(() -> com.magsav.gui.MAGSAVApp.integrateGestionIntoMainMenu2(stage));"
          inserted=1
        }
      }
    ' "$FILE.tmp1" > "$FILE.tmp2"
    mv "$FILE.tmp2" "$FILE"
    rm -f "$FILE.tmp1"
  else
    mv "$FILE.tmp1" "$FILE"
  fi
fi

echo "Patch appliqué. Sauvegarde: $FILE.bak"

# 3) Build/Run
./gradlew -q test || true
./gradlew -q run || true