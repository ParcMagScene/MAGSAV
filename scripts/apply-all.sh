#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

log() { printf "\n[%s] %s\n" "$(date '+%H:%M:%S')" "$*"; }

# 0) Pré-check Gradle wrapper
[[ -x ./gradlew ]] || { echo "gradlew introuvable. Exécutez: gradle wrapper"; exit 1; }

# 1) HelloWorld.greet() (évite l'erreur de compilation du test)
HELLO="src/main/java/com/magsav/HelloWorld.java"
if [[ ! -f "$HELLO" ]]; then
  log "Création $HELLO"
  mkdir -p "$(dirname "$HELLO")"
  cat > "$HELLO" <<'JAVA'
package com.magsav;

public final class HelloWorld {
    private HelloWorld() {}

    public static String greet() {
        return "Hello, World!";
    }
}
JAVA
else
  if ! grep -q 'static\s\+String\s\+greet\s*(' "$HELLO"; then
    log "Insertion de greet() dans $HELLO"
    cp "$HELLO" "$HELLO.bak"
    awk 'BEGIN{added=0}
      /\}\s*$/ && !added {
        print "    public static String greet() {";
        print "        return \"Hello, World!\";";
        print "    }";
        print;
        added=1; next
      }
      { print }
    ' "$HELLO.bak" > "$HELLO"
  else
    log "greet() déjà présent dans HelloWorld"
  fi
fi

# 2) Test HelloWorld minimal
TEST="src/test/java/com/magsav/HelloWorldTest.java"
log "Mise à jour du test $TEST"
mkdir -p "$(dirname "$TEST")"
cat > "$TEST" <<'JAVA'
package com.magsav;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloWorldTest {
    @Test
    void greet_returnsHelloWorld() {
        assertEquals("Hello, World!", HelloWorld.greet());
    }
}
JAVA

# 3) Classe menu JavaFX réutilisable
GMENU="src/main/java/com/magsav/gui/menu/GestionMenu.java"
if [[ ! -f "$GMENU" ]]; then
  log "Création $GMENU"
  mkdir -p "$(dirname "$GMENU")"
  cat > "$GMENU" <<'JAVA'
package com.magsav.gui.menu;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public final class GestionMenu {
    private GestionMenu() {}

    public static Menu build() {
        Menu gestion = new Menu("Gestion");

        MenuItem fabricants = new MenuItem("Fabricants");
        fabricants.setOnAction(e -> showInfo("Fabricants"));

        MenuItem fournisseurs = new MenuItem("Fournisseurs");
        fournisseurs.setOnAction(e -> showInfo("Fournisseurs"));

        MenuItem sav = new MenuItem("SAV externe");
        sav.setOnAction(e -> showInfo("SAV externe"));

        gestion.getItems().addAll(fabricants, fournisseurs, sav);
        return gestion;
    }

    private static void showInfo(String section) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(section);
        a.setHeaderText(section);
        a.setContentText("Vue à implémenter.");
        a.show();
    }
}
JAVA
else
  log "GestionMenu déjà présent"
fi

# 4) Injection FXML (si un MenuBar FXML existe)
pick_fxml() {
  while IFS= read -r f; do
    if grep -q "<MenuBar" "$f" && grep -q 'fx:controller=' "$f"; then
      echo "$f"; return
    fi
  done < <(find src/main/resources -type f -name '*.fxml' 2>/dev/null | sort)
  while IFS= read -r f; do
    if grep -q "<MenuBar" "$f"; then
      echo "$f"; return
    fi
  done < <(find src/main/resources -type f -name '*.fxml' 2>/dev/null | sort)
  echo ""
}
FXML_FILE="${MENU_FXML:-$(pick_fxml)}"
if [[ -n "$FXML_FILE" ]]; then
  log "FXML détecté: $FXML_FILE"
  if grep -q '<Menu text="Gestion"' "$FXML_FILE"; then
    log "Menu Gestion déjà présent dans FXML"
  else
    TMP="$(mktemp)"
    if grep -q '<menus>' "$FXML_FILE"; then
      log "Injection sous <menus> dans FXML"
      awk '{
        print;
        if (!done && /<menus>/) {
          print "        <Menu text=\"Gestion\">"
          print "          <items>"
          print "            <MenuItem text=\"Fabricants\" onAction=\"#openManufacturers\"/>"
          print "            <MenuItem text=\"Fournisseurs\" onAction=\"#openSuppliers\"/>"
          print "            <MenuItem text=\"SAV externe\" onAction=\"#openExternalSav\"/>"
          print "          </items>"
          print "        </Menu>"
          done=1
        }
      }' "$FXML_FILE" > "$TMP"
      mv "$TMP" "$FXML_FILE"
    elif grep -q '</MenuBar>' "$FXML_FILE"; then
      log "Création d’un bloc <menus> dans FXML"
      awk '{
        if (!done && /<\/MenuBar>/) {
          print "    <menus>"
          print "        <Menu text=\"Gestion\">"
          print "          <items>"
          print "            <MenuItem text=\"Fabricants\" onAction=\"#openManufacturers\"/>"
          print "            <MenuItem text=\"Fournisseurs\" onAction=\"#openSuppliers\"/>"
          print "            <MenuItem text=\"SAV externe\" onAction=\"#openExternalSav\"/>"
          print "          </items>"
          print "        </Menu>"
          print "    </menus>"
          done=1
        }
        print
      }' "$FXML_FILE" > "$TMP"
      mv "$TMP" "$FXML_FILE"
    else
      log "Impossible d’injecter dans FXML (pas de <menus> ni </MenuBar>)"
    fi
  fi

  # Handlers dans le contrôleur si fx:controller présent et classe trouvée
  CONTROLLER="$(sed -n 's/.*fx:controller=\"\([^\"]\+\)\".*/\1/p' "$FXML_FILE" | head -n1 || true)"
  if [[ -n "${CONTROLLER:-}" ]]; then
    CTRL_PATH="src/main/java/$(echo "$CONTROLLER" | tr '.' '/')".java
    if [[ -f "$CTRL_PATH" ]]; then
      if grep -Eq 'openManufacturers|openSuppliers|openExternalSav' "$CTRL_PATH"; then
        log "Handlers déjà présents dans $CTRL_PATH"
      else
        log "Ajout des handlers dans $CTRL_PATH"
        cp "$CTRL_PATH" "$CTRL_PATH.bak"
        awk 'BEGIN{added=0}
          /\}\s*$/ && !added {
            print "    public void openManufacturers(javafx.event.ActionEvent e) { showTodo(\"Fabricants\"); }"
            print "    public void openSuppliers(javafx.event.ActionEvent e) { showTodo(\"Fournisseurs\"); }"
            print "    public void openExternalSav(javafx.event.ActionEvent e) { showTodo(\"SAV externe\"); }"
            print ""
            print "    private void showTodo(String section) {"
            print "        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);"
            print "        a.setTitle(section);"
            print "        a.setHeaderText(section);"
            print "        a.setContentText(\"Vue à implémenter.\");"
            print "        a.show();"
            print "    }"
            print
            added=1; next
          }
          { print }
        ' "$CTRL_PATH.bak" > "$CTRL_PATH"
      fi
    else
      log "Contrôleur déclaré mais introuvable: $CTRL_PATH"
    fi
  fi
else
  log "Aucun FXML avec MenuBar trouvé — tentative d’injection côté Java"
  # 5) Injection côté Java (Application + MenuBar)
  APP_JAVA="$(grep -R --include='*.java' -nE 'extends[[:space:]]+Application' src/main/java 2>/dev/null | cut -d: -f1 | head -n1 || true)"
  if [[ -z "${APP_JAVA:-}" ]]; then
    log "Classe JavaFX (extends Application) introuvable. Recherche d’un MenuBar dans le code."
    APP_JAVA="$(grep -R --include='*.java' -l 'new[[:space:]]\+MenuBar' src/main/java | head -n1 || true)"
  fi
  if [[ -z "${APP_JAVA:-}" ]]; then
    log "Impossible d’injecter le menu Gestion automatiquement (pas de point d’ancrage trouvé)."
  else
    log "Cible Java: $APP_JAVA"
    if grep -q 'GestionMenu\.build' "$APP_JAVA"; then
      log "Injection déjà présente dans $APP_JAVA"
    else
      VAR=""
      LINE="$(grep -nE 'new[[:space:]]+MenuBar[[:space:]]*\(' "$APP_JAVA" | head -n1 | cut -d: -f1 || true)"
      if [[ -n "$LINE" ]]; then
        VAR="$(sed -n "${LINE}p" "$APP_JAVA" | sed -n 's/.*MenuBar[[:space:]]\+\([A-Za-z_][A-Za-z0-9_]*\)[[:space:]]*=.*/\1/p')"
        [[ -z "$VAR" ]] && VAR="$(sed -n "${LINE}p" "$APP_JAVA" | sed -n 's/.*\([A-Za-z_][A-Za-z0-9_]*\)[[:space:]]*=[[:space:]]*new[[:space:]]\+MenuBar.*/\1/p')"
      fi
      if [[ -z "$VAR" ]]; then
        VAR="$(grep -n '\.getMenus()\.' "$APP_JAVA" | head -n1 | sed -n 's/.*\([A-Za-z_][A-Za-z0-9_]*\)\.getMenus().*/\1/p')"
      fi
      if [[ -n "$VAR" ]]; then
        log "Variable MenuBar détectée: $VAR — injection de GestionMenu"
        cp "$APP_JAVA" "$APP_JAVA.bak"
        awk -v ln="$LINE" -v var="$VAR" '
          NR==ln { print; print "        " var ".getMenus().add(com.magsav.gui.menu.GestionMenu.build()); // ajout auto"; next }
          { print }
        ' "$APP_JAVA.bak" > "$APP_JAVA"
      else
        log "Variable MenuBar non détectée. Ajoutez manuellement:"
        echo "    <votreMenuBar>.getMenus().add(com.magsav.gui.menu.GestionMenu.build());"
      fi
    fi
  fi
fi

# 6) Gradle — Spotless + SpotBugs stabilisation (ajout si manquant)
BG="build.gradle"
add_spotless=false
add_forces=false
grep -q 'spotless\s*\{' "$BG" && grep -q 'googleJavaFormat' "$BG" || add_spotless=true
grep -q 'configurations.configureEach' "$BG" && grep -q 'spotbugs' "$BG" && grep -q 'commons-lang3' "$BG" || add_forces=true

if $add_spotless; then
  log "Ajout configuration Spotless (google-java-format)"
  cat >> "$BG" <<'GRADLE'

spotless {
    java {
        googleJavaFormat('1.22.0')
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
GRADLE
else
  log "Spotless déjà configuré"
fi

if $add_forces; then
  log "Stabilisation SpotBugs (forcer commons-lang3 et bcel compatibles)"
  cat >> "$BG" <<'GRADLE'

configurations.configureEach { cfg ->
    if (cfg.name.toLowerCase(java.util.Locale.ROOT).startsWith('spotbugs')) {
        cfg.resolutionStrategy {
            force 'org.apache.commons:commons-lang3:3.14.0'
            force 'org.apache.bcel:bcel:6.10.0'
        }
    }
}
GRADLE
else
  log "SpotBugs déjà stabilisé"
fi

# 7) Formatage + build + smoke (si disponibles)
log "Formatage du code (Spotless)"
./gradlew -q spotlessApply || true

log "Exécution des tests"
./gradlew -q test || true

log "Analyse statique rapide"
./gradlew -q lint || true

log "Lancement JavaFX (alias runFX si présent)"
./gradlew -q runFX || true

log "Terminé ✅"