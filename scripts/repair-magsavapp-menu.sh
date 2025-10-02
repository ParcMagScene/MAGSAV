#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

FILE="src/main/java/com/magsav/gui/MAGSAVApp.java"
[[ -f "$FILE" ]] || { echo "Introuvable: $FILE"; exit 1; }

backup() { cp "$FILE" "$FILE.pre-fix.$(date +%s).bak"; }

# 0) Restaurer depuis .bak si disponible et si le fichier contient un ajout précédent incomplet
if [[ -f "$FILE.bak" ]]; then
  echo "Restauration depuis sauvegarde: $FILE.bak"
  cp "$FILE.bak" "$FILE"
fi

backup

# 1) Détecter le nom du paramètre Stage (ex: stage, primaryStage)
STAGE_VAR="$(sed -n 's/.*void[[:space:]]\+start[[:space:]]*(.*Stage[[:space:]]\+\([a-zA-Z_][a-zA-Z0-9_]*\).*/\1/p' "$FILE" | head -n1 || true)"
[[ -z "${STAGE_VAR:-}" ]] && STAGE_VAR="stage"

# 2) Insérer la classe interne utilitaire avant la dernière }
if ! grep -q 'BEGIN AUTO: GestionMenu Integration' "$FILE"; then
  awk '
    { lines[NR]=$0 }
    END {
      lastclose=0
      for (i=NR; i>=1; i--) { if (lines[i] ~ /^\s*\}\s*$/) { lastclose=i; break } }
      if (lastclose==0) { print "Erreur: pas d accolade fermante trouvée." > "/dev/stderr"; exit 1 }
      for (i=1; i<=NR; i++) {
        if (i==lastclose) {
          print "    // BEGIN AUTO: GestionMenu Integration"
          print "    static final class MenuIntegration {"
          print "        static void integrate(javafx.stage.Stage stage) {"
          print "            if (stage == null) return;"
          print "            javafx.scene.Scene scene = stage.getScene();"
          print "            if (scene == null) return;"
          print "            javafx.scene.Parent root = scene.getRoot();"
          print "            if (root == null) return;"
          print "            javafx.scene.control.MenuBar bar = findOrCreateMenuBar(root);"
          print "            if (bar == null) return;"
          print "            javafx.scene.control.Menu main = findMainMenu(bar);"
          print "            if (main == null) { main = new javafx.scene.control.Menu(\"Menu\"); bar.getMenus().add(0, main); }"
          print "            javafx.scene.control.Menu gestionTop = null;"
          print "            for (javafx.scene.control.Menu m : bar.getMenus()) { if (\"Gestion\".equalsIgnoreCase(m.getText())) { gestionTop = m; break; } }"
          print "            for (javafx.scene.control.MenuItem it : main.getItems()) { if (it instanceof javafx.scene.control.Menu m && \"Gestion\".equalsIgnoreCase(m.getText())) return; }"
          print "            javafx.scene.control.Menu toInsert = (gestionTop != null) ? gestionTop : com.magsav.gui.menu.GestionMenu.build();"
          print "            if (gestionTop != null) { bar.getMenus().remove(gestionTop); }"
          print "            if (!main.getItems().isEmpty() && !(main.getItems().get(main.getItems().size()-1) instanceof javafx.scene.control.SeparatorMenuItem)) {"
          print "                main.getItems().add(new javafx.scene.control.SeparatorMenuItem());"
          print "            }"
          print "            main.getItems().add(toInsert);"
          print "        }"
          print ""
          print "        private static javafx.scene.control.MenuBar findOrCreateMenuBar(javafx.scene.Parent root) {"
          print "            javafx.scene.control.MenuBar mb = findFirstMenuBar(root);"
          print "            if (mb != null) return mb;"
          print "            mb = new javafx.scene.control.MenuBar();"
          print "            if (root instanceof javafx.scene.layout.BorderPane bp) {"
          print "                javafx.scene.Node top = bp.getTop();"
          print "                if (top == null) { bp.setTop(mb); }"
          print "                else if (top instanceof javafx.scene.layout.VBox vbox) { vbox.getChildren().add(0, mb); }"
          print "                else { javafx.scene.layout.VBox v = new javafx.scene.layout.VBox(); v.getChildren().add(mb); v.getChildren().add(top); bp.setTop(v); }"
          print "                return mb;"
          print "            }"
          print "            javafx.scene.Scene sc = root.getScene();"
          print "            if (sc != null) {"
          print "                javafx.scene.layout.VBox container = new javafx.scene.layout.VBox();"
          print "                container.getChildren().add(mb);"
          print "                container.getChildren().add(root);"
          print "                sc.setRoot(container);"
          print "                return mb;"
          print "            }"
          print "            return null;"
          print "        }"
          print ""
          print "        private static javafx.scene.control.MenuBar findFirstMenuBar(javafx.scene.Parent root) {"
          print "            java.util.Deque<javafx.scene.Parent> stack = new java.util.ArrayDeque<>();"
          print "            stack.push(root);"
          print "            while (!stack.isEmpty()) {"
          print "                javafx.scene.Parent p = stack.pop();"
          print "                if (p instanceof javafx.scene.control.MenuBar mb) return mb;"
          print "                for (javafx.scene.Node n : p.getChildrenUnmodifiable()) {"
          print "                    if (n instanceof javafx.scene.control.MenuBar mb2) return mb2;"
          print "                    if (n instanceof javafx.scene.Parent pr) stack.push(pr);"
          print "                }"
          print "            }"
          print "            return null;"
          print "        }"
          print ""
          print "        private static javafx.scene.control.Menu findMainMenu(javafx.scene.control.MenuBar bar) {"
          print "            if (bar.getMenus().isEmpty()) return null;"
          print "            java.util.List<String> preferred = java.util.Arrays.asList(\"Menu\", \"Fichier\", \"Principal\", \"Accueil\", \"Main\", \"File\");"
          print "            for (String name : preferred) {"
          print "                for (javafx.scene.control.Menu m : bar.getMenus()) {"
          print "                    if (name.equalsIgnoreCase(m.getText())) return m;"
          print "                }"
          print "            }"
          print "            return bar.getMenus().get(0);"
          print "        }"
          print "    }"
          print "    // END AUTO: GestionMenu Integration"
        }
        print lines[i];
      }
    }
  ' "$FILE" > "$FILE.tmp"
  mv "$FILE.tmp" "$FILE"
fi

# 3) Insérer l’appel après <StageVar>.show(); sinon après setScene(...)
if ! grep -q "MenuIntegration.integrate($STAGE_VAR)" "$FILE"; then
  awk -v s="$STAGE_VAR" '
    BEGIN { inserted=0 }
    {
      print
      if (!inserted && $0 ~ s "\\.show\\(\\)\\s*;") {
        print "        javafx.application.Platform.runLater(() -> MenuIntegration.integrate(" s "));"
        inserted=1
      }
    }
  ' "$FILE" > "$FILE.tmp1"
  if ! grep -q "MenuIntegration.integrate($STAGE_VAR)" "$FILE.tmp1"; then
    awk -v s="$STAGE_VAR" '
      BEGIN { inserted=0 }
      {
        print
        if (!inserted && $0 ~ s "\\.setScene\\(.+\\)\\s*;") {
          print "        javafx.application.Platform.runLater(() -> MenuIntegration.integrate(" s "));"
          inserted=1
        }
      }
    ' "$FILE" > "$FILE.tmp2"
    mv "$FILE.tmp2" "$FILE"
    rm -f "$FILE.tmp1" || true
  else
    mv "$FILE.tmp1" "$FILE"
  fi
fi

echo "Patch OK. Vérification compilation..."
./gradlew -q compileJava && echo "Compilation OK ✅" || { echo "Compilation KO ❌"; exit 1; }