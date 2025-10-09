# Guide de Déploiement MAGSAV 1.2

## Vue d'ensemble

Ce guide détaille les procédures de déploiement, d'installation et de maintenance de MAGSAV 1.2 en environnement de production.

## Prérequis système

### Environnement d'exécution

#### Configuration minimale
- **OS** : Windows 10+, macOS 11+, ou Linux Ubuntu 20.04+
- **Java** : OpenJDK 21 ou Oracle JDK 21
- **RAM** : 1 GB minimum, 2 GB recommandé
- **Stockage** : 500 MB pour l'application + espace données
- **Résolution** : 1024x768 minimum, 1920x1080 recommandé

#### Configuration recommandée
- **RAM** : 4 GB ou plus
- **Stockage** : SSD avec 2 GB d'espace libre
- **CPU** : 2 cœurs minimum
- **Résolution** : 1920x1080 ou supérieure

### Vérification des prérequis

```bash
# Vérification version Java
java -version
# Doit afficher : openjdk version "21.x.x" ou java version "21.x.x"

# Vérification JavaFX (si installation séparée)
java --module-path /path/to/javafx/lib --add-modules javafx.controls --version

# Vérification espace disque
df -h  # Linux/macOS
dir    # Windows
```

## Package de déploiement

### Structure du package

```
MAGSAV-1.2/
├── bin/                     # Scripts de lancement
│   ├── magsav.sh           # Script Linux/macOS
│   ├── magsav.bat          # Script Windows
│   └── magsav-debug.sh     # Mode debug
├── lib/                     # Bibliothèques Java
│   ├── magsav-1.2.jar     # Application principale
│   ├── javafx-*.jar       # JavaFX (si inclus)
│   ├── sqlite-*.jar       # Driver SQLite
│   └── ...                # Autres dépendances
├── config/                  # Configuration par défaut
│   ├── application.properties
│   ├── logging.properties
│   └── ui.properties
├── docs/                    # Documentation
│   ├── Manuel_Utilisateur.md
│   ├── Documentation_Technique.md
│   └── Guide_Deploiement.md
├── templates/               # Modèles et exemples
│   ├── import_products.csv
│   └── import_societes.csv
└── README.md               # Instructions d'installation
```

### Création du package

```bash
# Dans le répertoire du projet
./gradlew distZip

# Le package sera créé dans
ls build/distributions/MAGSAV-1.2.zip
```

## Installation

### Installation automatique (recommandée)

#### Sur Linux/macOS
```bash
# 1. Extraction
unzip MAGSAV-1.2.zip
cd MAGSAV-1.2

# 2. Configuration des permissions
chmod +x bin/magsav.sh
chmod +x bin/magsav-debug.sh

# 3. Installation optionnelle dans /opt
sudo mv MAGSAV-1.2 /opt/
sudo ln -s /opt/MAGSAV-1.2/bin/magsav.sh /usr/local/bin/magsav

# 4. Test de l'installation
magsav --version
```

#### Sur Windows
```cmd
# 1. Extraction dans C:\Program Files\MAGSAV-1.2

# 2. Ajout au PATH (optionnel)
# Ajouter C:\Program Files\MAGSAV-1.2\bin au PATH système

# 3. Test de l'installation
magsav.bat --version
```

### Installation manuelle

#### Configuration Java
```bash
# Variable JAVA_HOME (si nécessaire)
export JAVA_HOME=/path/to/java21
export PATH=$JAVA_HOME/bin:$PATH

# Vérification
java -version
```

#### Configuration des répertoires
```bash
# Création des répertoires de données
mkdir -p ~/.magsav/{data,config,logs,backup}

# Copie de la configuration par défaut
cp config/* ~/.magsav/config/

# Permissions appropriées
chmod 755 ~/.magsav
chmod 644 ~/.magsav/config/*
```

## Configuration

### Configuration de base

#### Fichier application.properties
```properties
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
```

#### Variables d'environnement

```bash
# Linux/macOS : ~/.bashrc ou ~/.zshrc
export MAGSAV_HOME=$HOME/.magsav
export MAGSAV_CONFIG=$MAGSAV_HOME/config
export MAGSAV_DATA=$MAGSAV_HOME/data
export MAGSAV_LOGS=$MAGSAV_HOME/logs

# Windows : Variables système
MAGSAV_HOME=%USERPROFILE%\.magsav
MAGSAV_CONFIG=%MAGSAV_HOME%\config
MAGSAV_DATA=%MAGSAV_HOME%\data
MAGSAV_LOGS=%MAGSAV_HOME%\logs
```

### Configuration avancée

#### Optimisation mémoire
```bash
# Scripts de lancement avec options JVM
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=50 \
     -Dfile.encoding=UTF-8 \
     -jar lib/magsav-1.2.jar
```

#### Configuration réseau (déploiement partagé)
```properties
# Base de données partagée (exemple)
db.path=//serveur/shared/magsav/data/magsav.db
db.backup.path=//serveur/shared/magsav/backup

# Configuration lock file pour multi-utilisateur
db.lock.enabled=true
db.lock.timeout=30s
```

## Scripts de lancement

### Script Linux/macOS (magsav.sh)
```bash
#!/bin/bash

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_HOME="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$APP_HOME/lib/magsav-1.2.jar"

# Variables d'environnement
export MAGSAV_HOME="${MAGSAV_HOME:-$HOME/.magsav}"
export MAGSAV_CONFIG="$MAGSAV_HOME/config"

# Création des répertoires si nécessaire
mkdir -p "$MAGSAV_HOME"/{config,data,logs,backup}

# Copie de la configuration par défaut si nécessaire
if [ ! -f "$MAGSAV_CONFIG/application.properties" ]; then
    cp -r "$APP_HOME/config/"* "$MAGSAV_CONFIG/"
fi

# Options JVM
JVM_OPTS="-Xms512m -Xmx2g"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=50"
JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8"
JVM_OPTS="$JVM_OPTS -Dmagsav.home=$MAGSAV_HOME"

# Arguments spéciaux
case "$1" in
    --version)
        java -jar "$JAR_FILE" --version
        exit 0
        ;;
    --help)
        echo "Usage: $0 [--version|--help|--debug]"
        exit 0
        ;;
    --debug)
        JVM_OPTS="$JVM_OPTS -Dlog.level=DEBUG -Dlog.sql.enabled=true"
        shift
        ;;
esac

# Vérification Java
if ! command -v java &> /dev/null; then
    echo "Erreur: Java n'est pas installé ou pas dans le PATH"
    exit 1
fi

# Vérification version Java
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "Erreur: Java 21 ou supérieur requis (version détectée: $JAVA_VERSION)"
    exit 1
fi

# Lancement de l'application
cd "$APP_HOME"
exec java $JVM_OPTS -jar "$JAR_FILE" "$@"
```

### Script Windows (magsav.bat)
```batch
@echo off
setlocal enabledelayedexpansion

:: Configuration
set SCRIPT_DIR=%~dp0
set APP_HOME=%SCRIPT_DIR%..
set JAR_FILE=%APP_HOME%\lib\magsav-1.2.jar

:: Variables d'environnement
if not defined MAGSAV_HOME set MAGSAV_HOME=%USERPROFILE%\.magsav
set MAGSAV_CONFIG=%MAGSAV_HOME%\config

:: Création des répertoires
if not exist "%MAGSAV_HOME%" mkdir "%MAGSAV_HOME%"
if not exist "%MAGSAV_HOME%\config" mkdir "%MAGSAV_HOME%\config"
if not exist "%MAGSAV_HOME%\data" mkdir "%MAGSAV_HOME%\data"
if not exist "%MAGSAV_HOME%\logs" mkdir "%MAGSAV_HOME%\logs"
if not exist "%MAGSAV_HOME%\backup" mkdir "%MAGSAV_HOME%\backup"

:: Copie configuration par défaut
if not exist "%MAGSAV_CONFIG%\application.properties" (
    xcopy /E /I "%APP_HOME%\config\*" "%MAGSAV_CONFIG%\"
)

:: Options JVM
set JVM_OPTS=-Xms512m -Xmx2g
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:MaxGCPauseMillis=50
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=UTF-8
set JVM_OPTS=%JVM_OPTS% -Dmagsav.home=%MAGSAV_HOME%

:: Arguments spéciaux
if "%1"=="--version" (
    java -jar "%JAR_FILE%" --version
    exit /b 0
)
if "%1"=="--help" (
    echo Usage: %0 [--version^|--help^|--debug]
    exit /b 0
)
if "%1"=="--debug" (
    set JVM_OPTS=%JVM_OPTS% -Dlog.level=DEBUG -Dlog.sql.enabled=true
    shift
)

:: Vérification Java
java -version >nul 2>&1
if errorlevel 1 (
    echo Erreur: Java n'est pas installe ou pas dans le PATH
    exit /b 1
)

:: Lancement
cd /d "%APP_HOME%"
java %JVM_OPTS% -jar "%JAR_FILE%" %*
```

## Tests de déploiement

### Tests automatisés
```bash
#!/bin/bash
# test-deployment.sh

echo "=== Tests de déploiement MAGSAV 1.2 ==="

# Test 1: Version Java
echo "Test 1: Vérification Java..."
if java -version 2>&1 | grep -q "21"; then
    echo "✓ Java 21 détecté"
else
    echo "✗ Java 21 requis"
    exit 1
fi

# Test 2: Lancement application
echo "Test 2: Test de lancement..."
timeout 10s ./bin/magsav.sh --version
if [ $? -eq 0 ]; then
    echo "✓ Application démarre correctement"
else
    echo "✗ Problème au lancement"
    exit 1
fi

# Test 3: Création des répertoires
echo "Test 3: Structure des répertoires..."
if [ -d "$HOME/.magsav/config" ]; then
    echo "✓ Répertoires créés"
else
    echo "✗ Répertoires manquants"
    exit 1
fi

# Test 4: Configuration
echo "Test 4: Fichiers de configuration..."
if [ -f "$HOME/.magsav/config/application.properties" ]; then
    echo "✓ Configuration présente"
else
    echo "✗ Configuration manquante"
    exit 1
fi

# Test 5: Base de données
echo "Test 5: Initialisation base de données..."
if [ -f "$HOME/.magsav/data/magsav.db" ]; then
    echo "✓ Base de données initialisée"
else
    echo "i Base de données sera créée au premier lancement"
fi

echo "=== Tests terminés avec succès ==="
```

### Tests manuels

#### Checklist de déploiement
- [ ] Java 21 installé et accessible
- [ ] Application démarre sans erreur
- [ ] Interface utilisateur s'affiche correctement
- [ ] Base de données se crée automatiquement
- [ ] Configuration chargée depuis les fichiers
- [ ] Logs écrits dans le bon répertoire
- [ ] Sauvegarde automatique configurée
- [ ] Performance satisfaisante (< 2s au démarrage)

#### Tests fonctionnels
1. **Création de données**
   - Créer un fabricant
   - Créer un produit
   - Créer une intervention

2. **Recherche et navigation**
   - Rechercher un produit
   - Naviguer entre les sections
   - Exporter des données

3. **Configuration**
   - Modifier les paramètres
   - Redémarrer l'application
   - Vérifier persistance des changements

## Maintenance

### Sauvegarde

#### Configuration automatique
```properties
# Dans application.properties
db.backup.enabled=true
db.backup.interval=24h
db.backup.retention=7
db.backup.compression=true
```

#### Script de sauvegarde manuelle
```bash
#!/bin/bash
# backup-magsav.sh

BACKUP_DIR="$HOME/.magsav/backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="magsav_backup_$TIMESTAMP.tar.gz"

echo "Création sauvegarde MAGSAV..."

# Arrêt de l'application si en cours
pkill -f "magsav-1.2.jar"

# Création de l'archive
tar -czf "$BACKUP_DIR/$BACKUP_FILE" \
    -C "$HOME/.magsav" \
    data config logs

echo "Sauvegarde créée: $BACKUP_FILE"

# Nettoyage anciennes sauvegardes (> 7 jours)
find "$BACKUP_DIR" -name "magsav_backup_*.tar.gz" -mtime +7 -delete

echo "Sauvegarde terminée"
```

### Mise à jour

#### Procédure de mise à jour
```bash
#!/bin/bash
# update-magsav.sh

echo "=== Mise à jour MAGSAV ==="

# 1. Sauvegarde avant mise à jour
echo "Sauvegarde des données..."
./backup-magsav.sh

# 2. Arrêt de l'application
echo "Arrêt de l'application..."
pkill -f "magsav-1.2.jar"

# 3. Sauvegarde de l'ancienne version
echo "Sauvegarde ancienne version..."
mv /opt/MAGSAV-1.2 /opt/MAGSAV-1.2.old

# 4. Installation nouvelle version
echo "Installation nouvelle version..."
unzip MAGSAV-1.3.zip -d /opt/
chmod +x /opt/MAGSAV-1.3/bin/magsav.sh

# 5. Migration configuration si nécessaire
echo "Migration configuration..."
# Copier les fichiers de config personnalisés si nécessaire

# 6. Test de la nouvelle version
echo "Test nouvelle version..."
/opt/MAGSAV-1.3/bin/magsav.sh --version

echo "Mise à jour terminée"
```

### Monitoring

#### Script de monitoring
```bash
#!/bin/bash
# monitor-magsav.sh

LOG_FILE="$HOME/.magsav/logs/application.log"
PID_FILE="/tmp/magsav.pid"

# Vérification processus actif
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ! ps -p $PID > /dev/null; then
        echo "ALERTE: MAGSAV n'est plus actif (PID $PID)"
        rm "$PID_FILE"
    fi
fi

# Vérification taille logs
if [ -f "$LOG_FILE" ]; then
    SIZE=$(stat -f%z "$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_FILE")
    if [ $SIZE -gt 50000000 ]; then  # 50MB
        echo "ALERTE: Fichier log volumineux (${SIZE} bytes)"
    fi
fi

# Vérification espace disque
USAGE=$(df "$HOME/.magsav" | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $USAGE -gt 90 ]; then
    echo "ALERTE: Espace disque faible (${USAGE}% utilisé)"
fi

# Vérification erreurs récentes
if [ -f "$LOG_FILE" ]; then
    ERRORS=$(tail -100 "$LOG_FILE" | grep -c "ERROR\|CRITICAL")
    if [ $ERRORS -gt 5 ]; then
        echo "ALERTE: $ERRORS erreurs détectées récemment"
    fi
fi
```

## Dépannage

### Problèmes courants

#### Application ne démarre pas
```bash
# Vérification détaillée
java -Dlog.level=DEBUG -jar lib/magsav-1.2.jar

# Logs détaillés
tail -f ~/.magsav/logs/application.log

# Vérification permissions
ls -la ~/.magsav/
```

#### Performance dégradée
```bash
# Monitoring mémoire
jcmd <pid> VM.info
jcmd <pid> GC.run

# Compactage base de données
sqlite3 ~/.magsav/data/magsav.db "VACUUM;"

# Nettoyage cache
rm -rf ~/.magsav/cache/*
```

#### Corruption de données
```bash
# Vérification intégrité DB
sqlite3 ~/.magsav/data/magsav.db "PRAGMA integrity_check;"

# Restauration depuis sauvegarde
cp ~/.magsav/backup/magsav_backup_latest.tar.gz /tmp/
cd /tmp && tar -xzf magsav_backup_latest.tar.gz
cp -r data/* ~/.magsav/data/
```

### Support et logs

#### Collection d'informations support
```bash
#!/bin/bash
# collect-support-info.sh

SUPPORT_DIR="/tmp/magsav-support-$(date +%Y%m%d_%H%M%S)"
mkdir -p "$SUPPORT_DIR"

echo "Collection informations support..."

# Informations système
uname -a > "$SUPPORT_DIR/system-info.txt"
java -version > "$SUPPORT_DIR/java-info.txt" 2>&1

# Configuration
cp -r ~/.magsav/config "$SUPPORT_DIR/"

# Logs récents (derniers 1000 lignes)
tail -1000 ~/.magsav/logs/application.log > "$SUPPORT_DIR/application-recent.log"
tail -1000 ~/.magsav/logs/error.log > "$SUPPORT_DIR/error-recent.log" 2>/dev/null

# Statistiques base de données
sqlite3 ~/.magsav/data/magsav.db ".schema" > "$SUPPORT_DIR/db-schema.txt"
sqlite3 ~/.magsav/data/magsav.db "SELECT COUNT(*) as products FROM produits;" > "$SUPPORT_DIR/db-stats.txt"

# Archive
tar -czf "${SUPPORT_DIR}.tar.gz" -C /tmp "$(basename "$SUPPORT_DIR")"
rm -rf "$SUPPORT_DIR"

echo "Archive support créée: ${SUPPORT_DIR}.tar.gz"
```

---

*Guide de Déploiement MAGSAV 1.2*
*Version 1.0 - Janvier 2024*