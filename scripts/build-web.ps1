# MAGSAV 3.0 - Script de build simplifié (Web Only)
# Build backend + frontend pour production

param(
    [switch]$SkipTests,
    [switch]$Clean
)

$ErrorActionPreference = "Stop"

Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "  MAGSAV 3.0 - Build Production" -ForegroundColor Yellow
Write-Host "======================================`n" -ForegroundColor Cyan

$startTime = Get-Date

# Nettoyage si demandé
if ($Clean) {
    Write-Host "🧹 Nettoyage des builds précédents..." -ForegroundColor Cyan
    if (Test-Path "backend/build") { Remove-Item -Recurse -Force "backend/build" }
    if (Test-Path "web-frontend/build") { Remove-Item -Recurse -Force "web-frontend/build" }
    if (Test-Path "common-models/build") { Remove-Item -Recurse -Force "common-models/build" }
    Write-Host "✓ Nettoyage terminé" -ForegroundColor Green
}

# Build Backend
Write-Host "`n📦 Build du backend..." -ForegroundColor Cyan
$gradleArgs = "build"
if ($SkipTests) {
    $gradleArgs += " -x test"
}

try {
    & ./gradlew.bat $gradleArgs --console=plain
    Write-Host "✓ Backend compilé avec succès" -ForegroundColor Green
}
catch {
    Write-Host "✗ Erreur lors du build backend" -ForegroundColor Red
    exit 1
}

# Build Frontend
Write-Host "`n🌐 Build du frontend..." -ForegroundColor Cyan
Set-Location web-frontend

# Installation des dépendances
if (-not (Test-Path "node_modules") -or $Clean) {
    Write-Host "📥 Installation des dépendances npm..." -ForegroundColor Yellow
    npm install
}

# Type-check
Write-Host "🔍 Vérification TypeScript..." -ForegroundColor Cyan
npm run type-check
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erreurs TypeScript détectées" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Write-Host "✓ TypeScript OK" -ForegroundColor Green

# Build production
Write-Host "📦 Build React production..." -ForegroundColor Cyan
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erreur lors du build React" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Write-Host "✓ Frontend compilé avec succès" -ForegroundColor Green

Set-Location ..

# Résumé
$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "  Build terminé avec succès !" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "⏱  Durée : $($duration.ToString('mm\:ss'))" -ForegroundColor White
Write-Host "📦 Backend JAR : backend/build/libs/" -ForegroundColor White
Write-Host "🌐 Frontend    : web-frontend/build/" -ForegroundColor White
Write-Host ""
