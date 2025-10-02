#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

pick_fxml() {
  # 1) Si l'utilisateur force via env
  if [[ -n "${MENU_FXML:-}" && -f "$MENU_FXML" ]]; then
    echo "$MENU_FXML"
    return
  fi
  # 2) FXML avec MenuBar + fx:controller
  while IFS= read -r f; do
    if grep -q "<MenuBar" "$f" && grep -q 'fx:controller=' "$f"; then
      echo "$f"; return
    fi
  done < <(find src/main/resources -type f -name '*.fxml' 2>/dev/null | sort)
  # 3) FXML avec MenuBar (sans fx:controller)
  while IFS= read -r f; do
    if grep -q "<MenuBar" "$f"; then
      echo "$f"; return
    fi
  done < <(find src/main/resources -type f -name '*.fxml' 2>/dev/null | sort)
  # 4) Échec
  echo ""
}

FXML_FILE="$(pick_fxml)"
if [[ -z "${FXML_FILE:-}" ]]; then
  echo "Aucun FXML avec <MenuBar> trouvé sous src/main/resources"
  echo "Indice: export MENU_FXML=src/main/resources/.../MainView.fxml puis relancez le script."
  exit 1
fi
echo "FXML ciblé: $FXML_FILE"

# Chercher fx:controller (peut être vide)
CONTROLLER="$(sed -n 's/.*fx:controller=\"\([^\"]\+\)\".*/\1/p' "$FXML_FILE" | head -n1 || true)"
CTRL_PATH=""
if [[ -n "${CONTROLLER:-}" ]]; then
  CTRL_PATH="src/main/java/$(echo "$CONTROLLER" | tr '.' '/')".java
  [[ -f "$CTRL_PATH" ]] || CTRL_PATH=""
fi
if [[ -n "$CONTROLLER" && -z "$CTRL_PATH" ]]; then
  echo "Avertissement: fx:controller=$CONTROLLER mais fichier Java introuvable."
fi

# Construire les lignes de MenuItem (avec onAction si contrôleur dispo)
if [[ -n "$CTRL_PATH" ]]; then
  MI1='<MenuItem text="Fabricants" onAction="#openManufacturers"/>'
  MI2='<MenuItem text="Fournisseurs" onAction="#openSuppliers"/>'
  MI3='<MenuItem text="SAV externe" onAction="#openExternalSav"/>'
else
  MI1='<MenuItem text="Fabricants"/>'
  MI2='<MenuItem text="Fournisseurs"/>'
  MI3='<MenuItem text="SAV externe"/>'
fi

# Ne pas dupliquer
if grep -q '<Menu text="Gestion"' "$FXML_FILE"; then
  echo "Menu 'Gestion' déjà présent."
else
  TMP="$(mktemp)"
  if grep -q '<menus>' "$FXML_FILE"; then
    # Injecter sous <menus>
    awk -v mi1="$MI1" -v mi2="$MI2" -v mi3="$MI3" '{
      print;
      if (!done && /<menus>/) {
        print "        <Menu text=\"Gestion\">"
        print "          <items>"
        print "            " mi1
        print "            " mi2
        print "            " mi3
        print "          </items>"
        print "        </Menu>"
        done=1
      }
    }' "$FXML_FILE" > "$TMP"
    mv "$TMP" "$FXML_FILE"
    echo "Menu 'Gestion' injecté (sous <menus>)."
  elif grep -q '</MenuBar>' "$FXML_FILE"; then
    # Ajouter un bloc <menus> avant </MenuBar>
    awk -v mi1="$MI1" -v mi2="$MI2" -v mi3="$MI3" '{
      if (!done && /<\/MenuBar>/) {
        print "    <menus>"
        print "        <Menu text=\"Gestion\">"
        print "          <items>"
        print "            " mi1
        print "            " mi2
        print "            " mi3
        print "          </items>"
        print "        </Menu>"
        print "    </menus>"
        done=1
      }
      print
    }' "$FXML_FILE" > "$TMP"
    mv "$TMP" "$FXML_FILE"
    echo "Menu 'Gestion' injecté (création de <menus>)."
  else
    echo "Impossible d'injecter: pas de <menus> ni </MenuBar> dans $FXML_FILE"
    exit 1
  fi
fi

# Ajouter les handlers dans le contrôleur si disponible
if [[ -n "$CTRL_PATH" ]]; then
  if grep -Eq 'openManufacturers|openSuppliers|openExternalSav' "$CTRL_PATH"; then
    echo "Handlers déjà présents dans $CTRL_PATH."
  else
    LAST_LINE="$(awk '/^\s*}\s*$/ {n=NR} END{print n+0}' "$CTRL_PATH")"
    [[ "$LAST_LINE" -gt 0 ]] || { echo "Fin de classe introuvable pour $CTRL_PATH"; exit 1; }
    TMP="$(mktemp)"
    awk -v last="$LAST_LINE" 'NR==last{
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
    }{ print }' "$CTRL_PATH" > "$TMP"
    mv "$TMP" "$CTRL_PATH"
    echo "Handlers ajoutés dans $CTRL_PATH."
  fi
else
  echo "Aucun contrôleur détecté: MenuItems ajoutés sans onAction (aucun risque au runtime)."
  echo "Pour ajouter les actions: export MENU_FXML=path/to.fxml et relancez le script."
fi

echo "OK ✅"