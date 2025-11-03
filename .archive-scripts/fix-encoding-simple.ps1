# Script de nettoyage complet de l'encodage MAGSAV-3.0
param(
    [switch]$Fix = $false
)

Write-Host "Nettoyage encodage MAGSAV-3.0" -ForegroundColor Green

# Configuration UTF-8
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[Console]::OutputEncoding = $utf8NoBom
$OutputEncoding = $utf8NoBom

# Arret processus Java
Write-Host "Arret processus Java..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Configuration environnement propre (anglais pour eviter les problemes)
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"
$env:GRADLE_OPTS = "-Xmx2g -Dfile.encoding=UTF-8"

Write-Host "Redemarrage avec encodage propre..." -ForegroundColor Green

# Demarrage backend en arriere-plan
Start-Process powershell -ArgumentList "-Command", "`$env:JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'; & '.\gradlew.bat' :backend:bootRun --no-daemon" -WindowStyle Minimized

# Attendre puis lancer desktop  
Start-Sleep 3
Write-Host "Lancement desktop..." -ForegroundColor Cyan
& .\gradlew.bat :desktop-javafx:run --no-daemon