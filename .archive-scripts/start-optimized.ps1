# MAGSAV-3.0 - Lancement optimise avec encodage correct
param(
    [switch]$Backend = $false,
    [switch]$Desktop = $false,
    [switch]$Both = $false
)

# Configuration UTF-8 complete
chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

# Variables d'environnement Java optimisees
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Dconsole.encoding=UTF-8"
$env:GRADLE_OPTS = "-Xmx2g -Dfile.encoding=UTF-8"

Write-Host "MAGSAV-3.0 - Demarrage optimise" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

# Arret processus existants
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "Arret de $($javaProcs.Count) processus Java existants..." -ForegroundColor Yellow
    $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep 2
}

if ($Backend -or $Both) {
    Write-Host "Demarrage Backend Spring Boot..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-Command", "`$env:JAVA_TOOL_OPTIONS='$($env:JAVA_TOOL_OPTIONS)'; Set-Location '$PWD'; & '.\gradlew.bat' :backend:bootRun --no-daemon" -WindowStyle Minimized
    Start-Sleep 3
}

if ($Desktop -or $Both -or (-not $Backend)) {
    Write-Host "Demarrage Application Desktop JavaFX..." -ForegroundColor Cyan
    & .\gradlew.bat :desktop-javafx:run --no-daemon
}

Write-Host "Application MAGSAV-3.0 demarree avec succes !" -ForegroundColor Green