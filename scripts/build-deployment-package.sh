#!/bin/bash

# Script de création du package de déploiement MAGSAV 1.2
# Usage: ./build-deployment-package.sh [version]

set -e

VERSION=${1:-"1.2"}
BUILD_DIR="build/deployment"
PACKAGE_NAME="MAGSAV-${VERSION}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=== Création du package de déploiement MAGSAV ${VERSION} ==="

# Nettoyage
echo "Nettoyage des fichiers temporaires..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/$PACKAGE_NAME"

# Build de l'application
echo "Compilation de l'application..."
./gradlew clean build -x test

# Copie des fichiers principaux
echo "Copie des fichiers de l'application..."

# JAR principal
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/lib"
cp build/libs/*.jar "$BUILD_DIR/$PACKAGE_NAME/lib/"

# Dépendances (si gradle configuration disponible)
if [ -d "build/dependencies" ]; then
    cp build/dependencies/*.jar "$BUILD_DIR/$PACKAGE_NAME/lib/"
fi

# Configuration par défaut
echo "Copie de la configuration..."
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/config"
cat > "$BUILD_DIR/$PACKAGE_NAME/config/application.properties" << 'EOF'
# Configuration MAGSAV 1.2 - Production

# Base de données
db.path=${user.home}/.magsav/data/magsav.db
db.backup.enabled=true
db.backup.path=${user.home}/.magsav/backup
db.backup.interval=24h
db.backup.retention=7

# Cache
cache.enabled=true
cache.max.size=100MB
cache.manufacturers.ttl=1h
cache.categories.ttl=30m
cache.products.ttl=15m

# Performance
performance.monitoring=true
performance.slow.threshold=1000ms
performance.very.slow.threshold=5000ms
performance.alerts.enabled=true

# Interface utilisateur
ui.theme=light
ui.font.size=normal
ui.notifications.duration=5s
ui.validation.realtime=true
ui.window.width=1200
ui.window.height=800
ui.window.maximized=false

# Logging
log.level=INFO
log.file.enabled=true
log.file.path=${user.home}/.magsav/logs
log.file.max.size=10MB
log.file.max.files=10

# Sécurité
security.backup.encryption=false
security.audit.enabled=true
security.session.timeout=24h
EOF

cat > "$BUILD_DIR/$PACKAGE_NAME/config/logging.properties" << 'EOF'
# Configuration logging MAGSAV 1.2

# Root logger
.level=INFO
.handlers=java.util.logging.FileHandler,java.util.logging.ConsoleHandler

# File handler
java.util.logging.FileHandler.pattern=${user.home}/.magsav/logs/application_%g.log
java.util.logging.FileHandler.limit=10485760
java.util.logging.FileHandler.count=10
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.FileHandler.level=INFO

# Console handler
java.util.logging.ConsoleHandler.level=WARNING
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter

# Formatage
java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] %4$s: %5$s %n

# Loggers spécifiques
com.magsav.level=INFO
com.magsav.service.level=INFO
com.magsav.repo.level=INFO
EOF

# Scripts de lancement
echo "Création des scripts de lancement..."
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/bin"

# Script Linux/macOS
cat > "$BUILD_DIR/$PACKAGE_NAME/bin/magsav.sh" << 'EOF'
#!/bin/bash

# Script de lancement MAGSAV 1.2
# Détection automatique de l'environnement et configuration optimisée

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_HOME="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$APP_HOME/lib/magsav-1.2.jar"

# Variables d'environnement
export MAGSAV_HOME="${MAGSAV_HOME:-$HOME/.magsav}"
export MAGSAV_CONFIG="$MAGSAV_HOME/config"
export MAGSAV_DATA="$MAGSAV_HOME/data"
export MAGSAV_LOGS="$MAGSAV_HOME/logs"
export MAGSAV_BACKUP="$MAGSAV_HOME/backup"

# Fonction de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Création des répertoires
log "Initialisation de l'environnement MAGSAV..."
mkdir -p "$MAGSAV_HOME"/{config,data,logs,backup,cache}

# Copie de la configuration par défaut si nécessaire
if [ ! -f "$MAGSAV_CONFIG/application.properties" ]; then
    log "Installation de la configuration par défaut..."
    cp -r "$APP_HOME/config/"* "$MAGSAV_CONFIG/"
fi

# Détection de la mémoire disponible
TOTAL_MEM=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || sysctl -n hw.memsize 2>/dev/null | awk '{print int($1/1024/1024)}' || echo "2048")
if [ "$TOTAL_MEM" -gt 4096 ]; then
    MAX_HEAP="2g"
    INIT_HEAP="1g"
elif [ "$TOTAL_MEM" -gt 2048 ]; then
    MAX_HEAP="1g"
    INIT_HEAP="512m"
else
    MAX_HEAP="512m"
    INIT_HEAP="256m"
fi

# Options JVM optimisées
JVM_OPTS="-Xms$INIT_HEAP -Xmx$MAX_HEAP"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=50"
JVM_OPTS="$JVM_OPTS -XX:+UseStringDeduplication"
JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8"
JVM_OPTS="$JVM_OPTS -Djava.awt.headless=false"
JVM_OPTS="$JVM_OPTS -Dmagsav.home=$MAGSAV_HOME"
JVM_OPTS="$JVM_OPTS -Dmagsav.version=1.2"

# Gestion des arguments
DEBUG_MODE=false
PROFILE_MODE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --version)
            java -jar "$JAR_FILE" --version 2>/dev/null || echo "MAGSAV 1.2"
            exit 0
            ;;
        --help)
            cat << HELP
Usage: $0 [OPTIONS]

Options:
    --version       Afficher la version
    --help          Afficher cette aide
    --debug         Mode debug avec logs détaillés
    --profile       Mode profiling performance
    --config=PATH   Répertoire de configuration personnalisé
    --data=PATH     Répertoire de données personnalisé

Variables d'environnement:
    MAGSAV_HOME     Répertoire racine MAGSAV (défaut: ~/.magsav)
    JAVA_HOME       Répertoire d'installation Java

Exemples:
    $0                          # Lancement normal
    $0 --debug                  # Mode debug
    $0 --config=/opt/magsav     # Configuration personnalisée
HELP
            exit 0
            ;;
        --debug)
            DEBUG_MODE=true
            JVM_OPTS="$JVM_OPTS -Dlog.level=DEBUG -Dlog.sql.enabled=true"
            shift
            ;;
        --profile)
            PROFILE_MODE=true
            JVM_OPTS="$JVM_OPTS -XX:+FlightRecorder"
            JVM_OPTS="$JVM_OPTS -XX:StartFlightRecording=duration=60s,filename=$MAGSAV_LOGS/profile.jfr"
            shift
            ;;
        --config=*)
            export MAGSAV_CONFIG="${1#*=}"
            shift
            ;;
        --data=*)
            export MAGSAV_DATA="${1#*=}"
            shift
            ;;
        *)
            log "Argument inconnu: $1"
            exit 1
            ;;
    esac
done

# Vérifications système
log "Vérification de l'environnement..."

# Vérification Java
if ! command -v java &> /dev/null; then
    log "ERREUR: Java n'est pas installé ou pas dans le PATH"
    log "Installez Java 21 ou supérieur et ajoutez-le au PATH"
    exit 1
fi

# Vérification version Java
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    log "ERREUR: Java 21 ou supérieur requis (version détectée: $JAVA_VERSION)"
    log "Téléchargez Java 21 depuis https://adoptium.net/"
    exit 1
fi

# Vérification fichier JAR
if [ ! -f "$JAR_FILE" ]; then
    log "ERREUR: Fichier JAR introuvable: $JAR_FILE"
    exit 1
fi

# Vérification espace disque
AVAILABLE_SPACE=$(df "$MAGSAV_HOME" 2>/dev/null | tail -1 | awk '{print $4}' || echo "0")
if [ "$AVAILABLE_SPACE" -lt 100000 ]; then  # 100MB en KB
    log "ATTENTION: Espace disque faible ($(($AVAILABLE_SPACE/1024))MB disponible)"
fi

# Gestion du processus existant
PID_FILE="$MAGSAV_HOME/magsav.pid"
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        log "ATTENTION: Une instance MAGSAV est déjà en cours (PID: $OLD_PID)"
        read -p "Voulez-vous l'arrêter ? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            kill "$OLD_PID"
            sleep 2
        else
            log "Arrêt du lancement"
            exit 1
        fi
    fi
fi

# Lancement de l'application
log "Lancement de MAGSAV 1.2..."
log "Mémoire allouée: $INIT_HEAP - $MAX_HEAP"
log "Mode debug: $DEBUG_MODE"
log "Mode profiling: $PROFILE_MODE"

cd "$APP_HOME"

# Enregistrement du PID
echo $$ > "$PID_FILE"

# Nettoyage à la sortie
trap 'rm -f "$PID_FILE"' EXIT

# Lancement avec gestion des erreurs
if $DEBUG_MODE; then
    log "Mode debug activé - logs détaillés"
    exec java $JVM_OPTS -jar "$JAR_FILE" "$@"
else
    exec java $JVM_OPTS -jar "$JAR_FILE" "$@" 2>> "$MAGSAV_LOGS/error.log"
fi
EOF

chmod +x "$BUILD_DIR/$PACKAGE_NAME/bin/magsav.sh"

# Script Windows
cat > "$BUILD_DIR/$PACKAGE_NAME/bin/magsav.bat" << 'EOF'
@echo off
setlocal enabledelayedexpansion

:: Configuration
set SCRIPT_DIR=%~dp0
set APP_HOME=%SCRIPT_DIR%..
set JAR_FILE=%APP_HOME%\lib\magsav-1.2.jar

:: Variables d'environnement
if not defined MAGSAV_HOME set MAGSAV_HOME=%USERPROFILE%\.magsav
set MAGSAV_CONFIG=%MAGSAV_HOME%\config
set MAGSAV_DATA=%MAGSAV_HOME%\data
set MAGSAV_LOGS=%MAGSAV_HOME%\logs
set MAGSAV_BACKUP=%MAGSAV_HOME%\backup

:: Fonction de logging
call :log "Initialisation de l'environnement MAGSAV..."

:: Création des répertoires
if not exist "%MAGSAV_HOME%" mkdir "%MAGSAV_HOME%"
if not exist "%MAGSAV_CONFIG%" mkdir "%MAGSAV_CONFIG%"
if not exist "%MAGSAV_DATA%" mkdir "%MAGSAV_DATA%"
if not exist "%MAGSAV_LOGS%" mkdir "%MAGSAV_LOGS%"
if not exist "%MAGSAV_BACKUP%" mkdir "%MAGSAV_BACKUP%"

:: Copie configuration par défaut
if not exist "%MAGSAV_CONFIG%\application.properties" (
    call :log "Installation de la configuration par défaut..."
    xcopy /E /I "%APP_HOME%\config\*" "%MAGSAV_CONFIG%\"
)

:: Options JVM
set JVM_OPTS=-Xms512m -Xmx2g
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:MaxGCPauseMillis=50
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=UTF-8
set JVM_OPTS=%JVM_OPTS% -Dmagsav.home=%MAGSAV_HOME%
set JVM_OPTS=%JVM_OPTS% -Dmagsav.version=1.2

:: Gestion des arguments
set DEBUG_MODE=false

:parse_args
if "%1"=="" goto :args_done
if "%1"=="--version" (
    java -jar "%JAR_FILE%" --version 2>nul || echo MAGSAV 1.2
    exit /b 0
)
if "%1"=="--help" (
    echo Usage: %0 [OPTIONS]
    echo.
    echo Options:
    echo     --version       Afficher la version
    echo     --help          Afficher cette aide
    echo     --debug         Mode debug avec logs détaillés
    exit /b 0
)
if "%1"=="--debug" (
    set DEBUG_MODE=true
    set JVM_OPTS=%JVM_OPTS% -Dlog.level=DEBUG -Dlog.sql.enabled=true
    shift
    goto :parse_args
)
shift
goto :parse_args

:args_done

:: Vérifications système
call :log "Vérification de l'environnement..."

:: Vérification Java
java -version >nul 2>&1
if errorlevel 1 (
    call :log "ERREUR: Java n'est pas installé ou pas dans le PATH"
    pause
    exit /b 1
)

:: Vérification fichier JAR
if not exist "%JAR_FILE%" (
    call :log "ERREUR: Fichier JAR introuvable: %JAR_FILE%"
    pause
    exit /b 1
)

:: Lancement de l'application
call :log "Lancement de MAGSAV 1.2..."
call :log "Mode debug: %DEBUG_MODE%"

cd /d "%APP_HOME%"

if "%DEBUG_MODE%"=="true" (
    call :log "Mode debug activé - logs détaillés"
    java %JVM_OPTS% -jar "%JAR_FILE%" %*
) else (
    java %JVM_OPTS% -jar "%JAR_FILE%" %* 2>>"%MAGSAV_LOGS%\error.log"
)

exit /b %errorlevel%

:log
echo [%date% %time%] %~1
exit /b

EOF

# Documentation
echo "Copie de la documentation..."
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/docs"
if [ -d "docs" ]; then
    cp docs/*.md "$BUILD_DIR/$PACKAGE_NAME/docs/"
fi

# Templates et exemples
echo "Création des templates..."
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/templates"
cat > "$BUILD_DIR/$PACKAGE_NAME/templates/import_products.csv" << 'EOF'
code,nom,sn,fabricant,uid,situation
ABC123,Produit Example 1,SN001,Fabricant A,MAGSAV-001,En service
DEF456,Produit Example 2,SN002,Fabricant B,MAGSAV-002,En maintenance
EOF

cat > "$BUILD_DIR/$PACKAGE_NAME/templates/import_societes.csv" << 'EOF'
type,nom,email,phone,adresse,notes
FABRICANT,Fabricant Example,contact@fabricant.com,0123456789,123 Rue Example,Notes exemple
CLIENT,Client Example,client@example.com,0987654321,456 Avenue Test,Client important
EOF

# README principal
cat > "$BUILD_DIR/$PACKAGE_NAME/README.md" << 'EOF'
# MAGSAV 1.2 - Installation

## Installation rapide

### Linux/macOS
```bash
# 1. Extraction
unzip MAGSAV-1.2.zip
cd MAGSAV-1.2

# 2. Configuration des permissions
chmod +x bin/magsav.sh

# 3. Lancement
./bin/magsav.sh
```

### Windows
```cmd
# 1. Extraction dans le répertoire souhaité
# 2. Lancement
bin\magsav.bat
```

## Prérequis

- Java 21 ou supérieur
- 1 GB RAM minimum, 2 GB recommandé
- 500 MB espace disque libre

## Configuration

L'application crée automatiquement la structure suivante :
- Configuration: `~/.magsav/config/`
- Données: `~/.magsav/data/`
- Logs: `~/.magsav/logs/`
- Sauvegardes: `~/.magsav/backup/`

## Support

Consultez la documentation dans le répertoire `docs/` :
- Manuel utilisateur: `docs/Manuel_Utilisateur.md`
- Documentation technique: `docs/Documentation_Technique.md`
- Guide de déploiement: `docs/Guide_Deploiement.md`

## Version

MAGSAV 1.2 - Janvier 2024
EOF

# Scripts utilitaires
echo "Création des scripts utilitaires..."
mkdir -p "$BUILD_DIR/$PACKAGE_NAME/tools"

cat > "$BUILD_DIR/$PACKAGE_NAME/tools/backup.sh" << 'EOF'
#!/bin/bash
# Script de sauvegarde MAGSAV

MAGSAV_HOME="${MAGSAV_HOME:-$HOME/.magsav}"
BACKUP_DIR="$MAGSAV_HOME/backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="magsav_backup_$TIMESTAMP.tar.gz"

echo "Création sauvegarde MAGSAV..."
mkdir -p "$BACKUP_DIR"

tar -czf "$BACKUP_DIR/$BACKUP_FILE" \
    -C "$MAGSAV_HOME" \
    data config logs

echo "Sauvegarde créée: $BACKUP_FILE"
echo "Emplacement: $BACKUP_DIR/$BACKUP_FILE"

# Nettoyage anciennes sauvegardes (> 7 jours)
find "$BACKUP_DIR" -name "magsav_backup_*.tar.gz" -mtime +7 -delete 2>/dev/null || true

echo "Sauvegarde terminée"
EOF

chmod +x "$BUILD_DIR/$PACKAGE_NAME/tools/backup.sh"

# Script de test d'installation
cat > "$BUILD_DIR/$PACKAGE_NAME/tools/test-installation.sh" << 'EOF'
#!/bin/bash
# Test d'installation MAGSAV

echo "=== Test d'installation MAGSAV 1.2 ==="

# Test Java
echo "Test 1: Vérification Java..."
if java -version 2>&1 | grep -q "21\|2[2-9]\|[3-9][0-9]"; then
    echo "✓ Java 21+ détecté"
else
    echo "✗ Java 21 ou supérieur requis"
    exit 1
fi

# Test lancement
echo "Test 2: Test de lancement..."
if timeout 10s ./bin/magsav.sh --version >/dev/null 2>&1; then
    echo "✓ Application démarre correctement"
else
    echo "✗ Problème au lancement"
    exit 1
fi

# Test structure
echo "Test 3: Structure des répertoires..."
if [ -d "$HOME/.magsav/config" ]; then
    echo "✓ Répertoires créés"
else
    echo "✗ Répertoires manquants"
    exit 1
fi

echo "=== Installation validée ==="
EOF

chmod +x "$BUILD_DIR/$PACKAGE_NAME/tools/test-installation.sh"

# Création de l'archive finale
echo "Création de l'archive de déploiement..."
cd "$BUILD_DIR"
zip -r "../MAGSAV-${VERSION}-${TIMESTAMP}.zip" "$PACKAGE_NAME/"

# Création du lien vers la dernière version
cd ..
ln -sf "MAGSAV-${VERSION}-${TIMESTAMP}.zip" "MAGSAV-${VERSION}-latest.zip"

echo ""
echo "=== Package de déploiement créé avec succès ==="
echo "Archive: build/MAGSAV-${VERSION}-${TIMESTAMP}.zip"
echo "Lien: build/MAGSAV-${VERSION}-latest.zip"
echo "Taille: $(du -h build/MAGSAV-${VERSION}-${TIMESTAMP}.zip | cut -f1)"
echo ""
echo "Contenu du package:"
find "$BUILD_DIR/$PACKAGE_NAME" -type f | head -20
echo ""
echo "Package prêt pour déploiement!"