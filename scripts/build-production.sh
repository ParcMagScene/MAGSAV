#!/bin/bash

# Script de build optimisé pour la production MAGSAV 1.2
# Optimise les performances et prépare pour la distribution

set -e

echo "=== Build de production MAGSAV 1.2 ==="

# Nettoyage complet
echo "Nettoyage des builds précédents..."
./gradlew clean

# Configuration de build optimisé
export GRADLE_OPTS="-Xmx4g -XX:+UseG1GC"

# Build avec optimisations
echo "Compilation optimisée pour la production..."
./gradlew build \
    -Pproduction=true \
    -PminifyJar=true \
    -PoptimizeCode=true \
    --parallel \
    --build-cache \
    --configuration-cache

# Tests d'intégration complets
echo "Exécution des tests d'intégration..."
./gradlew test --tests "*IntegrationTest" --info

# Vérification de la qualité du code
echo "Vérification de la qualité du code..."
./gradlew check

# Génération de la documentation
echo "Génération de la documentation JavaDoc..."
./gradlew javadoc

# Création du JAR optimisé
echo "Création du JAR de production..."
./gradlew shadowJar

# Vérification de la taille du JAR
JAR_SIZE=$(du -h build/libs/*.jar | tail -1 | cut -f1)
echo "Taille du JAR final: $JAR_SIZE"

# Test de démarrage rapide
echo "Test de démarrage de l'application..."
timeout 30s java -jar build/libs/MAGSAV-*.jar --version

echo ""
echo "=== Build de production terminé avec succès ==="
echo "JAR principal: $(ls build/libs/MAGSAV-*.jar)"
echo "Taille: $JAR_SIZE"
echo "Documentation: build/docs/javadoc/"
echo ""
echo "Prêt pour création du package de déploiement !"
echo "Exécutez: ./scripts/build-deployment-package.sh"