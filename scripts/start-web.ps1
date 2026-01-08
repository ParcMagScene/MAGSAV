# MAGSAV 3.0 - Script de démarrage simplifié (Web Only)
# Démarre le backend API + frontend React

param(
    [switch]$BackendOnly,
    [switch]$FrontendOnly
)

$ErrorActionPreference = "Stop"

Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "  MAGSAV 3.0 - Application Web" -ForegroundColor Yellow
Write-Host "======================================`n" -ForegroundColor Cyan

# Fonction pour arrêter un processus sur un port
function Stop-ProcessOnPort {
    param([int]$Port)
    $procs = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | 
        Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($proc in $procs) {
        try {
            Stop-Process -Id $proc -Force -ErrorAction Stop
            Write-Host "✓ Processus $proc arrêté (port $Port)" -ForegroundColor Green
            Start-Sleep 1
        }
        catch {
            Write-Host "⚠ Impossible d'arrêter le processus $proc" -ForegroundColor Yellow
        }
    }
}

# Fonction pour tester le backend
function Test-BackendReady {
    param([int]$MaxRetries = 30)
    
    Write-Host "⏳ Attente du backend..." -ForegroundColor Cyan
    
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $response = Invoke-RestMethod "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction Stop
            if ($response.status -eq "UP") {
                Write-Host "✓ Backend opérationnel" -ForegroundColor Green
                return $true
            }
        }
        catch {
            if ($i % 5 -eq 0) {
                Write-Host "  Tentative $i/$MaxRetries..." -ForegroundColor Gray
            }
            Start-Sleep -Seconds 2
        }
    }
    
    Write-Host "✗ Timeout : Backend non accessible" -ForegroundColor Red
    return $false
}

# Nettoyage des processus existants
Write-Host "`n📋 Nettoyage des processus..." -ForegroundColor Cyan
Stop-ProcessOnPort 8080  # Backend
Stop-ProcessOnPort 3000  # Frontend

# Démarrage du backend
if (-not $FrontendOnly) {
    Write-Host "`n🚀 Démarrage du backend..." -ForegroundColor Cyan
    $backendJob = Start-Job -ScriptBlock {
        Set-Location $using:PWD
        & ./gradlew.bat :backend:bootRun --console=plain
    }
    
    if (Test-BackendReady) {
        Write-Host "✓ Backend démarré : http://localhost:8080" -ForegroundColor Green
        Write-Host "  📚 Swagger UI   : http://localhost:8080/swagger-ui.html" -ForegroundColor Gray
        Write-Host "  📊 Console H2   : http://localhost:8080/h2-console" -ForegroundColor Gray
    } else {
        Write-Host "✗ Erreur lors du démarrage du backend" -ForegroundColor Red
        exit 1
    }
}

# Démarrage du frontend
if (-not $BackendOnly) {
    Write-Host "`n🌐 Démarrage du frontend..." -ForegroundColor Cyan
    
    # Vérifier si node_modules existe
    if (-not (Test-Path "web-frontend/node_modules")) {
        Write-Host "📦 Installation des dépendances npm..." -ForegroundColor Yellow
        Set-Location web-frontend
        npm install
        Set-Location ..
    }
    
    $frontendJob = Start-Job -ScriptBlock {
        Set-Location "$using:PWD/web-frontend"
        $env:BROWSER = 'none'
        npm start
    }
    
    Start-Sleep -Seconds 8
    
    try {
        $frontendTest = Test-NetConnection -ComputerName localhost -Port 3000 -WarningAction SilentlyContinue
        if ($frontendTest.TcpTestSucceeded) {
            Write-Host "✓ Frontend démarré : http://localhost:3000" -ForegroundColor Green
        } else {
            Write-Host "⚠ Frontend en cours de démarrage..." -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "⚠ Vérification du frontend impossible" -ForegroundColor Yellow
    }
}

# Résumé
Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "  Application MAGSAV prête !" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan

if (-not $FrontendOnly) {
    Write-Host "🔧 Backend  : http://localhost:8080" -ForegroundColor White
}
if (-not $BackendOnly) {
    Write-Host "🌐 Frontend : http://localhost:3000" -ForegroundColor White
}

Write-Host "`n💡 Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Yellow
Write-Host ""

# Attendre que les jobs se terminent
if ($backendJob) { Wait-Job $backendJob | Out-Null }
if ($frontendJob) { Wait-Job $frontendJob | Out-Null }
