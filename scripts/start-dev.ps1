# =============================================================
# MAGSAV-3.0 - Script de démarrage développement
# Démarre le backend Spring Boot et le frontend React
# =============================================================

param(
    [switch]$BackendOnly,
    [switch]$FrontendOnly,
    [switch]$Clean
)

$ErrorActionPreference = "Continue"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " MAGSAV-3.0 - Démarrage Développement" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Nettoyage si demandé
if ($Clean) {
    Write-Host "🧹 Nettoyage des processus et locks..." -ForegroundColor Yellow
    Get-Process -Name java,javaw,node -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 2
    Remove-Item -Path "backend/data/*.lock","backend/data/*.trace.db" -Force -ErrorAction SilentlyContinue
    Write-Host "✅ Nettoyage terminé" -ForegroundColor Green
    Write-Host ""
}

# Démarrage du backend
if (-not $FrontendOnly) {
    Write-Host "🚀 Démarrage du backend Spring Boot..." -ForegroundColor Cyan
    Write-Host "   Port: 8080" -ForegroundColor Gray
    Write-Host "   H2 Console: http://localhost:8080/h2-console" -ForegroundColor Gray
    Write-Host ""
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; ./gradlew.bat :backend:bootRun --console=plain"
    
    Write-Host "⏳ Attente démarrage backend (15s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    # Vérifier que le backend répond
    try {
        $health = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✅ Backend démarré et accessible" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Backend démarré mais pas encore prêt (normal)" -ForegroundColor Yellow
    }
    Write-Host ""
}

# Démarrage du frontend
if (-not $BackendOnly) {
    Write-Host "🌐 Démarrage du frontend React..." -ForegroundColor Cyan
    Write-Host "   Port: 3000" -ForegroundColor Gray
    Write-Host "   URL: http://localhost:3000" -ForegroundColor Gray
    Write-Host ""
    
    $env:BROWSER = "none"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\web-frontend'; npm start"
    
    Write-Host "⏳ Attente démarrage frontend (10s)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    Write-Host "✅ Frontend démarré" -ForegroundColor Green
    Write-Host ""
}

Write-Host "=====================================" -ForegroundColor Green
Write-Host " ✨ MAGSAV-3.0 est prêt!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "📱 Frontend: http://localhost:3000" -ForegroundColor Cyan
Write-Host "🔧 Backend:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "💾 H2 Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour arrêter: .\scripts\stop-dev.ps1" -ForegroundColor Yellow
Write-Host ""
