#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

FILE="src/main/java/com/magsav/gui/MAGSAVApp.java"
[[ -f "$FILE" ]] || { echo "Introuvable: $FILE"; exit 1; }

pick_backup() {
  # Priorité aux sauvegardes pré-fix avec timestamp, puis .bak
  ls -1t "$FILE".pre-fix.*.bak 2>/dev/null || true
  if [[ -f "$FILE.bak" ]]; then echo "$FILE.bak"; fi
}

try_compile() {
  ./gradlew -q compileJava >/dev/null 2>&1
}

echo "[1/4] Vérification compilation actuelle…"
if ! try_compile; then
  echo "[2/4] Restauration depuis une sauvegarde compilable…"
  restored=0
  for b in $(pick_backup); do
    echo " - Essai: $b"
    cp "$b" "$FILE"
    if try_compile; then
      echo "   OK: restauration compilable."
      restored=1
      break
    fi
  done
  if [[ $restored -eq 0 ]]; then
    echo "Aucune sauvegarde compilable. Abandon (partagez la fin du fichier pour patch ciblé)."
    exit 1
  fi
else
  echo "Compilation OK, pas de restauration nécessaire."
fi

echo "[3/4] Injection de l’appel MenuBarInstaller.install(...) dans start(Stage …)"
STAGE_VAR="$(sed -n 's/.*void[[:space:]]\+start[[:space:]]*(.*Stage[[:space:]]\+\([a-zA-Z_][a-zA-Z0-9_]*\).*/\1/p' "$FILE" | head -n1 || true)"
[[ -z "${STAGE_VAR:-}" ]] && STAGE_VAR="stage"

if ! grep -q 'MenuBarInstaller\.install' "$FILE"; then
  TMP="$(mktemp)"
  awk -v s="$STAGE_VAR" '
    BEGIN{inserted=0}
    {
      print
      if (!inserted && $0 ~ s "\\.show\\(\\)\\s*;") {
        print "        javafx.application.Platform.runLater(() -> com.magsav.gui.menu.MenuBarInstaller.install(" s ".getScene()));"
        inserted=1
      }
    }
    END{
      if (!inserted) {
        # seconde passe après setScene(...)
        # signaler via code de sortie spécial; on fera une seconde passe en shell
      }
    }
  ' "$FILE" > "$TMP"
  if ! grep -q 'MenuBarInstaller\.install' "$TMP"; then
    awk -v s="$STAGE_VAR" '
      BEGIN{inserted=0}
      {
        print
        if (!inserted && $0 ~ s "\\.setScene\\(.+\\)\\s*;") {
          print "        javafx.application.Platform.runLater(() -> com.magsav.gui.menu.MenuBarInstaller.install(" s ".getScene()));"
          inserted=1
        }
      }
    ' "$FILE" > "$TMP"
  fi
  mv "$TMP" "$FILE"
else
  echo "Appel déjà présent, aucune réinjection."
fi

echo "[4/4] Build + run rapide"
./gradlew -q compileJava
./gradlew -q run || true
echo "Terminé ✅"