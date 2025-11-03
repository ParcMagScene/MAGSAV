#!/usr/bin/env powershell

Write-Host "🚀 Démarrage du backend MAGSAV-3.0..." -ForegroundColor Green

# Aller dans le répertoire du projet
Set-Location "C:\Users\aalou\MAGSAV-3.0"

# Variables
$BackendDir = "backend"
$JarFile = "backend\build\libs\backend-3.0.0.jar"

# Construire le JAR si nécessaire
if (-Not (Test-Path $JarFile)) {
    Write-Host "📦 Construction du JAR backend..." -ForegroundColor Yellow
    & .\gradlew :backend:bootJar --console=plain
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Échec de la construction" -ForegroundColor Red
        exit 1
    }
}

Write-Host "🔥 Démarrage du serveur Spring Boot..." -ForegroundColor Cyan

# Démarrer directement le JAR Spring Boot
& java -jar $JarFile --spring.profiles.active=development

Write-Host "✅ Backend arrêté" -ForegroundColor Green