#!/usr/bin/env bash
set -euo pipefail

# Démarre le web en arrière-plan puis l'UI JavaFX au premier plan
# Options:
#   --no-gui : ne démarre que le serveur web en arrière-plan et sort immédiatement

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."
cd "${PROJECT_ROOT}"

NO_GUI=false
if [[ "${1:-}" == "--no-gui" ]]; then
	NO_GUI=true
fi

LOG_DIR="./build/logs"
mkdir -p "$LOG_DIR"
WEB_LOG="$LOG_DIR/web.log"

echo "[MAGSAV] Démarrage du serveur web en arrière-plan..."
./gradlew runWeb >"$WEB_LOG" 2>&1 &
WEB_PID=$!
echo "$WEB_PID" > .web.pid
echo "[MAGSAV] Web PID: $WEB_PID (logs: $WEB_LOG)"

if [[ "$NO_GUI" == true ]]; then
	echo "[MAGSAV] Mode --no-gui activé. Serveur web lancé en arrière-plan."
	exit 0
fi

echo "[MAGSAV] Démarrage de l'UI JavaFX..."
./gradlew runGui

echo "[MAGSAV] UI JavaFX terminée. Le serveur web peut toujours tourner (PID $WEB_PID)."