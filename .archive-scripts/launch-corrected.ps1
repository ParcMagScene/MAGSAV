# Script de lancement final avec corrections encodage
chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"
$env:GRADLE_OPTS = "-Xmx2g -Dfile.encoding=UTF-8"

Write-Host "MAGSAV-3.0 - Lancement avec corrections encodage completes" -ForegroundColor Green

# Arret processus existants
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Lancement
& .\gradlew.bat :desktop-javafx:run --no-daemon