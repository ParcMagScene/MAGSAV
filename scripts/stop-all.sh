#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."
cd "${PROJECT_ROOT}"

# Pour l’instant, seule l’application web tourne en arrière-plan.
# L’UI JavaFX s’arrête en fermant la fenêtre. On délègue donc à stop-web.
exec scripts/stop-web.sh
