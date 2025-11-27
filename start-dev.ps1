# MAGSAV 3.0 - Script de demarrage fiable
# Gere automatiquement backend + frontend + desktop

param(
    [switch]$SkipBackend,
    [switch]$SkipDesktop,
    [switch]$DesktopOnly
)

$ErrorActionPreference = "Continue"
$ConfirmPreference = "None"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "MAGSAV 3.0 - Demarrage Automatique" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Fonction pour tuer un processus sur un port
function Stop-ProcessOnPort {
    param([int]$Port)
    $procs = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | 
    Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($proc in $procs) {
        try {
            Stop-Process -Id $proc -Force -ErrorAction Stop
            Write-Host "[OK] Processus $proc arrete (port $Port)" -ForegroundColor Green
            Start-Sleep 1
        }
        catch {
            Write-Host "[WARN] Impossible d'arreter le processus $proc" -ForegroundColor Yellow
        }
    }
}

# Fonction pour attendre qu'un port soit libre
function Wait-PortFree {
    param([int]$Port, [int]$MaxWait = 10)
    $waited = 0
    while ((Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue) -and ($waited -lt $MaxWait)) {
        Write-Host "[WAIT] Port $Port occupe, attente..." -ForegroundColor Yellow
        Start-Sleep 1
        $waited++
    }
    return $waited -lt $MaxWait
}

# Fonction pour tester si le backend repond
function Test-Backend {
    param([int]$MaxRetries = 30)
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $response = Invoke-RestMethod "http://localhost:8080/api/suppliers" -TimeoutSec 2 -ErrorAction Stop
            Write-Host "[OK] Backend operationnel - $($response.Count) fournisseurs" -ForegroundColor Green
            return $true
        }
        catch {
            if ($i -eq 1) {
                Write-Host "[WAIT] Attente demarrage backend..." -ForegroundColor Cyan
            }
            Write-Host "  Tentative $i/$MaxRetries..." -ForegroundColor Gray
            Start-Sleep 2
        }
    }
    return $false
}

# ETAPE 1: Nettoyer les ports
Write-Host "[CLEANUP] Nettoyage des ports..." -ForegroundColor Cyan
Stop-ProcessOnPort 8080
if (-not $DesktopOnly) {
    Stop-ProcessOnPort 3000
}
Write-Host ""

# ETAPE 2: Demarrer le backend
if (-not $SkipBackend -and -not $DesktopOnly) {
    Write-Host "[BACKEND] Demarrage du backend Spring Boot..." -ForegroundColor Cyan
    
    if (-not (Wait-PortFree 8080)) {
        Write-Host "[ERROR] Port 8080 toujours occupe" -ForegroundColor Red
        exit 1
    }
    
    # Demarrer en arriere-plan
    Start-Process powershell -ArgumentList "-ExecutionPolicy Bypass -NoProfile -Command `"cd '$PWD'; ./gradlew :backend:bootRun`"" -WindowStyle Minimized
    
    Write-Host "[WAIT] Test de disponibilite du backend..." -ForegroundColor Cyan
    if (-not (Test-Backend)) {
        Write-Host "[ERROR] Backend non disponible apres 60 secondes" -ForegroundColor Red
        Write-Host "[INFO] Verifiez les logs Gradle" -ForegroundColor Yellow
        exit 1
    }
    
    # Tester tous les endpoints
    Write-Host "[TEST] Verification des endpoints..." -ForegroundColor Cyan
    try {
        $sav = Invoke-RestMethod "http://localhost:8080/api/service-requests" -TimeoutSec 3
        Write-Host "  [OK] SAV: $($sav.Count) demandes" -ForegroundColor Green
        
        $eq = Invoke-RestMethod "http://localhost:8080/api/equipment" -TimeoutSec 3
        Write-Host "  [OK] Equipements: $($eq.Count) items" -ForegroundColor Green
        
        $mr = Invoke-RestMethod "http://localhost:8080/api/material-requests" -TimeoutSec 3
        Write-Host "  [OK] Demandes materiel: $($mr.Count) demandes" -ForegroundColor Green
        
        $go = Invoke-RestMethod "http://localhost:8080/api/grouped-orders" -TimeoutSec 3
        Write-Host "  [OK] Commandes groupees: $($go.Count) commandes" -ForegroundColor Green
    }
    catch {
        Write-Host "  [WARN] Certains endpoints en erreur: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    Write-Host ""
}

# ETAPE 3: Demarrer le desktop
if (-not $SkipDesktop) {
    Write-Host "[DESKTOP] Demarrage de l'application JavaFX..." -ForegroundColor Cyan
    
    if ($DesktopOnly) {
        # Mode synchrone si desktop seul
        ./gradlew :desktop-javafx:run
    }
    else {
        # Mode arriere-plan dans un terminal visible
        Start-Process powershell -ArgumentList "-ExecutionPolicy Bypass -NoExit -Command `"cd '$PWD'; Write-Host 'Demarrage MAGSAV Desktop...' -ForegroundColor Green; ./gradlew :desktop-javafx:run`"" -WindowStyle Normal
        Start-Sleep 2
        Write-Host "[OK] Application desktop lancee" -ForegroundColor Green
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "DEMARRAGE TERMINE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Backend:  http://localhost:8080" -ForegroundColor White
Write-Host "Desktop:  Application JavaFX lancee" -ForegroundColor White
Write-Host "`nPour arreter:" -ForegroundColor Yellow
Write-Host "  Stop-ProcessOnPort 8080" -ForegroundColor Gray
Write-Host "========================================`n" -ForegroundColor Cyan
