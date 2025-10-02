#!/usr/bin/env bash
set -euo pipefail

# Se placer à la racine du projet (même logique que start-all.sh)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."
cd "${PROJECT_ROOT}"

PID_FILE=".web.pid"
if [[ -f "$PID_FILE" ]]; then
  WEB_PID=$(cat "$PID_FILE" || true)
  if [[ -n "${WEB_PID:-}" ]] && ps -p "$WEB_PID" > /dev/null 2>&1; then
    echo "[MAGSAV] Arrêt du serveur web (PID $WEB_PID)..."
    kill "$WEB_PID" || true
    # Attendre que le process s'arrête (max ~10s)
    for i in {1..10}; do
      if ps -p "$WEB_PID" > /dev/null 2>&1; then
        sleep 1
      else
        break
      fi
    done
    # Si toujours vivant, forcer l'arrêt
    if ps -p "$WEB_PID" > /dev/null 2>&1; then
      echo "[MAGSAV] Le processus ne s'est pas arrêté, envoi de SIGKILL..."
      kill -9 "$WEB_PID" || true
    fi
    rm -f "$PID_FILE"
    echo "[MAGSAV] Serveur web arrêté."
  else
    echo "[MAGSAV] Aucun serveur web actif (PID introuvable ou process non présent)."
    rm -f "$PID_FILE"
  fi
else
  echo "[MAGSAV] Fichier PID introuvable. Rien à arrêter."
fi