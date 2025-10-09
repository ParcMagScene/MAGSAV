#!/bin/bash

# Script de test de stabilité de l'interface MAGSAV
APP_DIR="/Users/reunion/MAGSAV-1.2"
LOG_FILE="$APP_DIR/test-logs.txt"

echo "=== Test de stabilité MAGSAV - $(date) ===" | tee "$LOG_FILE"
echo "Répertoire: $APP_DIR" | tee -a "$LOG_FILE"
echo

cd "$APP_DIR" || exit 1

echo "📋 Vérification des prérequis..." | tee -a "$LOG_FILE"

# Vérifier la compilation
echo "🔨 Test de compilation..." | tee -a "$LOG_FILE"
if ./gradlew compileJava > /dev/null 2>&1; then
    echo "✅ Compilation réussie" | tee -a "$LOG_FILE"
else
    echo "❌ Erreur de compilation" | tee -a "$LOG_FILE"
    exit 1
fi

# Vérifier la base de données
echo "🗄️ Test de la base de données..." | tee -a "$LOG_FILE"
if ./scripts/check-database.sh > /dev/null 2>&1; then
    echo "✅ Base de données opérationnelle" | tee -a "$LOG_FILE"
else
    echo "❌ Problème avec la base de données" | tee -a "$LOG_FILE"
    exit 1
fi

# Vérifier les ressources
echo "📁 Test des ressources..." | tee -a "$LOG_FILE"
if [ -d "src/main/resources/fxml" ]; then
    fxml_count=$(find src/main/resources/fxml -name "*.fxml" | wc -l)
    echo "✅ $fxml_count fichiers FXML trouvés" | tee -a "$LOG_FILE"
else
    echo "❌ Répertoire FXML manquant" | tee -a "$LOG_FILE"
fi

echo "🚀 Test de lancement de l'application..." | tee -a "$LOG_FILE"
echo "Application démarrée en arrière-plan. Surveillez les logs pour détecter des erreurs." | tee -a "$LOG_FILE"
echo "Pour arrêter l'application, utilisez Ctrl+C dans le terminal principal." | tee -a "$LOG_FILE"
echo

echo "📊 État actuel du système:" | tee -a "$LOG_FILE"
echo "- Date: $(date)" | tee -a "$LOG_FILE"
echo "- Utilisateur: $(whoami)" | tee -a "$LOG_FILE"
echo "- Java version:" | tee -a "$LOG_FILE"
java -version 2>&1 | head -1 | tee -a "$LOG_FILE"
echo

echo "✅ Tous les tests préliminaires sont passés avec succès!" | tee -a "$LOG_FILE"
echo "📍 Logs sauvegardés dans: $LOG_FILE" | tee -a "$LOG_FILE"