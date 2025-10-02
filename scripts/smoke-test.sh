#!/usr/bin/env bash
set -euo pipefail

# Mini smoke test Web MAGSAV
# - démarre le serveur web sur 8080 (libère le port si nécessaire)
# - vérifie l'index et la présence de codes AA1234
# - effectue un login (user/password)
# - récupère le QR d'une intervention (id=1) et vérifie Content-Type image/png
# - vérifie aussi l'endpoint photo produit /product/{sn}/photo (Content-Type image/png + signature PNG)
# - arrête le serveur

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

log() { printf "[SMOKE] %s\n" "$*"; }
ok()  { printf "[SMOKE][OK] %s\n" "$*"; }
ko()  { printf "[SMOKE][KO] %s\n" "$*"; }

die() { ko "$*"; exit 1; }

PORT=${PORT:-8080}
TIMEOUT=${TIMEOUT:-30}
PNG_STRICT=${PNG_STRICT:-1} # 1 = vérifier la signature PNG 89 50 4E 47 0D 0A 1A 0A
COOKIES="/tmp/magsav.cookies"
COOKIES_ADMIN="/tmp/magsav.cookies.admin"
INDEX_OUT="/tmp/magsav-index.html"
QR_OUT="/tmp/magsav-qr.png"
HDR_OUT="/tmp/magsav-qr.hdr"
PHOTO_OUT="/tmp/magsav-photo.png"
PHOTO_HDR="/tmp/magsav-photo.hdr"
RUN_LOG="build/logs/web.log"
PID_FILE=".web.pid"

mkdir -p "$(dirname "$RUN_LOG")"

log "Libération du port ${PORT} si nécessaire..."
(lsof -iTCP:${PORT} -sTCP:LISTEN -n -P | awk 'NR>1{print $2}' | xargs -r kill -TERM || true)
sleep 1
(lsof -iTCP:${PORT} -sTCP:LISTEN -n -P | awk 'NR>1{print $2}' | xargs -r kill -KILL || true)

log "Démarrage du serveur web sur le port ${PORT}..."
./gradlew runWeb -Pport=${PORT} --no-daemon --console=plain >"$RUN_LOG" 2>&1 &
APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

# Attente active jusqu'à 200 OK
log "Attente de disponibilité de http://localhost:${PORT}/ (timeout ${TIMEOUT}s) ..."
for i in $(seq 1 "$TIMEOUT"); do
  code=$(curl -sS -o /dev/null -w "%{http_code}" http://localhost:${PORT}/ || true)
  if [[ "$code" == "200" || "$code" == "302" ]]; then ok "Serveur disponible (HTTP $code)"; break; fi
  sleep 1
  if [[ $i -eq "$TIMEOUT" ]]; then die "Le serveur ne répond pas après ${TIMEOUT}s"; fi
done

# Vérifier présence de codes AA1234 sur l'index
log "Téléchargement de l'index et recherche de codes AA1234..."
curl -sS http://localhost:${PORT}/ -o "$INDEX_OUT"
if grep -qE "[A-Z]{2}[0-9]{4}" "$INDEX_OUT"; then
  head -n 1 "$INDEX_OUT" >/dev/null # noop
  ok "Codes produits AA1234 détectés dans l'index"
else
  die "Aucun code AA1234 détecté dans l'index"
fi

# Login
log "Authentification (user/password)..."
# Récupérer le formulaire de login pour extraire le token CSRF
LOGIN_HTML="/tmp/magsav-login.html"
curl -sS -c "$COOKIES" "http://localhost:${PORT}/login" -o "$LOGIN_HTML"
# Extraire le nom et la valeur du champ CSRF (par défaut _csrf) de manière robuste
CSRF_NAME=$(grep -o 'name="[^"]*csrf[^"]*"' "$LOGIN_HTML" | head -n1 | sed -E 's/name="([^"]*)"/\1/')
if [[ -z "${CSRF_NAME:-}" ]]; then CSRF_NAME="_csrf"; fi
CSRF_VALUE=$(grep -o 'name="[^"]*csrf[^"]*"[^>]*value="[^"]*"' "$LOGIN_HTML" | head -n1 | sed -E 's/.*value="([^"]*)".*/\1/')
if [[ -z "${CSRF_VALUE:-}" ]]; then die "Impossible d'extraire le token CSRF sur /login"; fi
HTTP_LOGIN=$(curl -sS -b "$COOKIES" -c "$COOKIES" -d "${CSRF_NAME}=${CSRF_VALUE}&username=user&password=password" -X POST -o /dev/null -w "%{http_code}" http://localhost:${PORT}/login || true)
if [[ "$HTTP_LOGIN" != "302" ]]; then die "Login échoué (HTTP $HTTP_LOGIN)"; else ok "Login OK (HTTP 302)"; fi

# QR (id=1)
log "Récupération du QR pour /intervention/1/qr ..."
curl -sS -b "$COOKIES" -H "Accept: image/png" -D "$HDR_OUT" http://localhost:${PORT}/intervention/1/qr -o "$QR_OUT"
CT=$(awk 'BEGIN{IGNORECASE=1} /^Content-Type:/ {print $2}' "$HDR_OUT" | tr -d '\r')
if [[ "$CT" != "image/png" ]]; then die "Content-Type inattendu: $CT"; fi

if [[ ! -s "$QR_OUT" ]]; then die "QR vide"; fi

if [[ "$PNG_STRICT" == "1" ]]; then
  # Vérifier la signature PNG: 89 50 4E 47 0D 0A 1A 0A
  SIG=$(xxd -l 8 -g 1 "$QR_OUT" | awk 'NR==1{print $2$3$4$5$6$7$8$9}')
  if [[ "$SIG" != "89504e470d0a1a0a" && "$SIG" != "89504E470D0A1A0A" ]]; then
    die "Signature PNG invalide: $SIG"
  fi
fi

ok "QR OK (image/png, taille $(stat -f%z "$QR_OUT") octets)"

# Test endpoint photo produit
log "Détection d'un numéro de série produit depuis l'index..."
SN=$(grep -oE "/product/[^/]+/photo" "$INDEX_OUT" | head -n1 | sed -E 's#.*/product/([^/]+)/photo#\1#')
if [[ -z "${SN:-}" ]]; then
  SN="__unknown__" # provoque le placeholder côté serveur
  log "Aucun numéro de série détecté dans l'index, utilisation d'un placeholder: $SN"
else
  log "Numéro de série détecté: $SN"
fi

log "Récupération de la photo produit pour /product/$SN/photo ..."
curl -sS -H "Accept: image/png" -D "$PHOTO_HDR" "http://localhost:${PORT}/product/$SN/photo" -o "$PHOTO_OUT"
CT_PHOTO=$(awk 'BEGIN{IGNORECASE=1} /^Content-Type:/ {print $2}' "$PHOTO_HDR" | tr -d '\r')
if [[ "$CT_PHOTO" != "image/png" && "$CT_PHOTO" != "image/jpeg" && "$CT_PHOTO" != "image/jpg" ]]; then
  die "Content-Type inattendu pour photo: $CT_PHOTO"
fi
if [[ ! -s "$PHOTO_OUT" ]]; then die "Photo vide"; fi

if [[ "$PNG_STRICT" == "1" && "$CT_PHOTO" == "image/png" ]]; then
  SIG_PHOTO=$(xxd -l 8 -g 1 "$PHOTO_OUT" | awk 'NR==1{print $2$3$4$5$6$7$8$9}')
  if [[ "$SIG_PHOTO" != "89504e470d0a1a0a" && "$SIG_PHOTO" != "89504E470D0A1A0A" ]]; then
    die "Signature PNG invalide pour la photo: $SIG_PHOTO"
  fi
fi

ok "Photo produit OK (CT=$CT_PHOTO, taille $(stat -f%z "$PHOTO_OUT") octets)"

# Test upload photo (ADMIN)
log "Authentification (admin/admin) pour test d'upload..."
# Récupérer le token CSRF pour l'admin
LOGIN_HTML_ADM="/tmp/magsav-login-admin.html"
curl -sS -c "$COOKIES_ADMIN" "http://localhost:${PORT}/login" -o "$LOGIN_HTML_ADM"
CSRF_NAME_ADM=$(grep -o 'name="[^"]*csrf[^"]*"' "$LOGIN_HTML_ADM" | head -n1 | sed -E 's/name="([^"]*)"/\1/')
if [[ -z "${CSRF_NAME_ADM:-}" ]]; then CSRF_NAME_ADM="_csrf"; fi
CSRF_VALUE_ADM=$(grep -o 'name="[^"]*csrf[^"]*"[^>]*value="[^"]*"' "$LOGIN_HTML_ADM" | head -n1 | sed -E 's/.*value="([^"]*)".*/\1/')
if [[ -z "${CSRF_VALUE_ADM:-}" ]]; then die "Impossible d'extraire le token CSRF admin sur /login"; fi
HTTP_LOGIN_ADM=$(curl -sS -b "$COOKIES_ADMIN" -c "$COOKIES_ADMIN" -d "${CSRF_NAME_ADM}=${CSRF_VALUE_ADM}&username=admin&password=admin" -X POST -o /dev/null -w "%{http_code}" http://localhost:${PORT}/login || true)
if [[ "$HTTP_LOGIN_ADM" != "302" ]]; then die "Login admin échoué (HTTP $HTTP_LOGIN_ADM)"; else ok "Login admin OK (HTTP 302)"; fi

# Récupérer le token CSRF (post-login admin) depuis une page avec formulaire
CSRF_HTML_ADM="/tmp/magsav-csrf-admin.html"
curl -sS -b "$COOKIES_ADMIN" "http://localhost:${PORT}/categories" -o "$CSRF_HTML_ADM"
CSRF_NAME_ADM=$(grep -o 'name="[^"]*csrf[^"]*"' "$CSRF_HTML_ADM" | head -n1 | sed -E 's/name="([^"]*)"/\1/')
if [[ -z "${CSRF_NAME_ADM:-}" ]]; then CSRF_NAME_ADM="_csrf"; fi
CSRF_VALUE_ADM=$(grep -o 'name="[^"]*csrf[^"]*"[^>]*value="[^"]*"' "$CSRF_HTML_ADM" | head -n1 | sed -E 's/.*value="([^"]*)".*/\1/')
if [[ -z "${CSRF_VALUE_ADM:-}" ]]; then die "Impossible d'extraire le token CSRF admin après login"; fi

# Choisir un numéro de série dédié à l'upload de test (évite d'impacter des données visibles)
SN_UP="SMKUP-$(date +%s)"
BEFORE_OUT="/tmp/magsav-photo-before.png"
AFTER_OUT="/tmp/magsav-photo-after.png"
BEFORE_HDR="/tmp/magsav-photo-before.hdr"
AFTER_HDR="/tmp/magsav-photo-after.hdr"

log "Lecture de l'état AVANT upload pour /product/$SN_UP/photo ..."
curl -sS -H "Accept: image/png" -D "$BEFORE_HDR" "http://localhost:${PORT}/product/$SN_UP/photo" -o "$BEFORE_OUT"
SHA_BEFORE=$(shasum -a 256 "$BEFORE_OUT" | awk '{print $1}')

# Créer un petit PNG différent (hex altéré du placeholder) pour l'upload
UPLOAD_IMG="/tmp/magsav-upload-test.png"
cat > /tmp/_png_hex.txt <<'HEX'
89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000a49444154789c63000100000500010d0a2db40000000049454e44ae426083
HEX
xxd -r -p /tmp/_png_hex.txt > "$UPLOAD_IMG"
rm -f /tmp/_png_hex.txt

log "Upload de la photo de test (PNG) vers /product/$SN_UP/photo ..."
HTTP_UP=$(curl -sS -b "$COOKIES_ADMIN" -F "${CSRF_NAME_ADM}=${CSRF_VALUE_ADM}" -F "file=@$UPLOAD_IMG;type=image/png" -o /dev/null -w "%{http_code}" "http://localhost:${PORT}/product/$SN_UP/photo" || true)
if [[ "$HTTP_UP" != "302" ]]; then die "Upload échoué (HTTP $HTTP_UP)"; else ok "Upload OK (HTTP 302)"; fi

log "Lecture de l'état APRES upload pour /product/$SN_UP/photo ..."
curl -sS -H "Accept: image/png" -D "$AFTER_HDR" "http://localhost:${PORT}/product/$SN_UP/photo" -o "$AFTER_OUT"
SHA_AFTER=$(shasum -a 256 "$AFTER_OUT" | awk '{print $1}')

if [[ "$SHA_BEFORE" == "$SHA_AFTER" ]]; then
  die "Le contenu de la photo n'a pas changé après l'upload"
fi

ok "Upload ADMIN vérifié (hash avant/après différents)"

# Test upload JPEG (ADMIN)
SN_JPG="SMKUP-JPG-$(date +%s)"
BEFORE_JPG_OUT="/tmp/magsav-photo-before-jpg"
AFTER_JPG_OUT="/tmp/magsav-photo-after-jpg.jpg"
BEFORE_JPG_HDR="/tmp/magsav-photo-before-jpg.hdr"
AFTER_JPG_HDR="/tmp/magsav-photo-after-jpg.hdr"

log "Lecture de l'état AVANT upload (JPEG) pour /product/$SN_JPG/photo ..."
curl -sS -H "Accept: image/jpeg" -D "$BEFORE_JPG_HDR" "http://localhost:${PORT}/product/$SN_JPG/photo" -o "$BEFORE_JPG_OUT"
SHA_JPG_BEFORE=$(shasum -a 256 "$BEFORE_JPG_OUT" | awk '{print $1}')

# JPEG minimal (SOI/JFIF)
UPLOAD_JPG="/tmp/magsav-upload-test.jpg"
cat > /tmp/_jpg_hex.txt <<'HEX'
ffd8ffe000104a46494600010100000100010000ffdb00430001010101010101010101010101010101010101010101010101010101010101010101010101010101ffc00011080001000103012200021101031101ffc40014000100000000000000000000000000000000ffda0008010100003f00ffd9
HEX
xxd -r -p /tmp/_jpg_hex.txt > "$UPLOAD_JPG"
rm -f /tmp/_jpg_hex.txt

log "Upload JPEG de test vers /product/$SN_JPG/photo ..."
HTTP_UP_JPG=$(curl -sS -b "$COOKIES_ADMIN" -F "${CSRF_NAME_ADM}=${CSRF_VALUE_ADM}" -F "file=@$UPLOAD_JPG;type=image/jpeg" -o /dev/null -w "%{http_code}" "http://localhost:${PORT}/product/$SN_JPG/photo" || true)
if [[ "$HTTP_UP_JPG" != "302" ]]; then die "Upload JPEG échoué (HTTP $HTTP_UP_JPG)"; else ok "Upload JPEG OK (HTTP 302)"; fi

log "Lecture de l'état APRES upload (JPEG) pour /product/$SN_JPG/photo ..."
curl -sS -H "Accept: image/jpeg" -D "$AFTER_JPG_HDR" "http://localhost:${PORT}/product/$SN_JPG/photo" -o "$AFTER_JPG_OUT"
CT_JPG=$(awk 'BEGIN{IGNORECASE=1} /^Content-Type:/ {print $2}' "$AFTER_JPG_HDR" | tr -d '\r')
if [[ "$CT_JPG" != "image/jpeg" && "$CT_JPG" != "image/jpg" ]]; then die "Content-Type inattendu pour JPEG: $CT_JPG"; fi

SHA_JPG_AFTER=$(shasum -a 256 "$AFTER_JPG_OUT" | awk '{print $1}')
if [[ "$SHA_JPG_BEFORE" == "$SHA_JPG_AFTER" ]]; then die "La photo JPEG n'a pas changé après l'upload"; fi

# Vérifier signature JPEG (FF D8 FF)
SIG_JPG=$(xxd -l 3 -g 1 "$AFTER_JPG_OUT" | awk 'NR==1{print $2$3$4}')
if [[ "$SIG_JPG" != "ffd8ff" && "$SIG_JPG" != "FFD8FF" ]]; then die "Signature JPEG invalide (SOI): $SIG_JPG"; fi

# Arrêt
log "Arrêt du serveur (PID $APP_PID)..."
kill -TERM "$APP_PID" 2>/dev/null || true
sleep 1
kill -KILL "$APP_PID" 2>/dev/null || true
rm -f "$PID_FILE"
ok "Smoke test terminé avec succès"
