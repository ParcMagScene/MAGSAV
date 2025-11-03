# Script de nettoyage complet de l'encodage MAGSAV-3.0
param(
    [switch]$Fix = $false
)

Write-Host "🔧 Nettoyage encodage MAGSAV-3.0" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Configuration UTF-8
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[Console]::OutputEncoding = $utf8NoBom
[Console]::InputEncoding = $utf8NoBom
$OutputEncoding = $utf8NoBom

# Fichiers à corriger
$filesToFix = @(
    "backend\src\main\java\com\magscene\magsav\backend\service\DataInitializer.java",
    "backend\src\main\java\com\magscene\magsav\backend\MagsavApplication.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\MagsavDesktopApplication.java"
)

if ($Fix) {
    Write-Host "🔨 Correction des fichiers..." -ForegroundColor Yellow
    
    foreach ($file in $filesToFix) {
        if (Test-Path $file) {
            Write-Host "  Correction: $file" -ForegroundColor Cyan
            $content = Get-Content $file -Raw -Encoding UTF8
            $content | Out-File $file -Encoding utf8NoBOM -NoNewline
        }
    }
    
    Write-Host "✅ Fichiers corrigés!" -ForegroundColor Green
}

# Arrêter tous les processus Java
Write-Host "🛑 Arrêt processus Java..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Configuration environnement
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"
$env:GRADLE_OPTS = "-Xmx2g -Dfile.encoding=UTF-8"

Write-Host "🚀 Redémarrage avec encodage propre..." -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# Démarrage backend
Start-Process powershell -ArgumentList "-Command", "& '.\gradlew.bat' :backend:bootRun --no-daemon" -WindowStyle Minimized

# Attendre 5 secondes puis lancer desktop  
Start-Sleep 5
& .\gradlew.bat :desktop-javafx:run --no-daemon