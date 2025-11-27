# MAGSAV 3.0 - Script de demarrage ROBUSTE
# Force l'utilisation du backend - Plus de donnees de test !

param(
    [switch]$BackendOnly,
    [switch]$DesktopOnly
)

$ErrorActionPreference = "Stop"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  MAGSAV 3.0 - MODE BACKEND OBLIGATOIRE" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Fonction pour arreter un processus sur un port
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
            Write-Host "[WARN] Impossible d'arreter $proc" -ForegroundColor Yellow
        }
    }
}

# Fonction pour tester le backend
function Test-BackendReady {
    param([int]$MaxRetries = 60, [int]$Timeout = 2)
    
    Write-Host "[WAIT] Attente du backend (max $MaxRetries x $Timeout sec)..." -ForegroundColor Cyan
    
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $response = Invoke-RestMethod "http://localhost:8080/api/suppliers" -TimeoutSec $Timeout -ErrorAction Stop
            Write-Host "[OK] Backend operationnel - $($response.Count) fournisseurs" -ForegroundColor Green
            return $true
        }
        catch {
            if ($i -eq 1) {
                Write-Host "[INFO] Backend en demarrage..." -ForegroundColor Gray
            }
            if ($i % 5 -eq 0) {
                Write-Host "  Tentative $i/$MaxRetries..." -ForegroundColor Gray
            }
            Start-Sleep $Timeout
        }
    }
    
    Write-Host "[ERREUR] Backend non disponible apres $($MaxRetries * $Timeout) secondes" -ForegroundColor Red
    return $false
}

# ETAPE 1: Cleanup
Write-Host "[CLEANUP] Nettoyage des processus..." -ForegroundColor Yellow
Stop-ProcessOnPort 8080
Get-Process | Where-Object { $_.ProcessName -match "java" } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 2
Write-Host ""

# ETAPE 2: Demarrer le BACKEND (obligatoire)
if (-not $DesktopOnly) {
    Write-Host "[BACKEND] Demarrage du backend Spring Boot..." -ForegroundColor Cyan
    Write-Host "[INFO] Le backend DOIT demarrer avant le desktop" -ForegroundColor Yellow
    
    Start-Process powershell -ArgumentList "-ExecutionPolicy Bypass -NoExit -Command `"cd '$PWD'; Write-Host 'MAGSAV Backend - Spring Boot' -ForegroundColor Green; .\gradlew :backend:bootRun`"" -WindowStyle Normal
    
    if (-not (Test-BackendReady)) {
        Write-Host "`n[ERREUR CRITIQUE] Backend non disponible !" -ForegroundColor Red
        Write-Host "[INFO] Le desktop NE PEUT PAS fonctionner sans backend" -ForegroundColor Yellow
        Write-Host "[ACTION] Verifiez les logs du backend dans le terminal" -ForegroundColor Yellow
        exit 1
    }
    
    # Test de tous les endpoints
    Write-Host "`n[TEST] Verification de tous les endpoints..." -ForegroundColor Cyan
    $endpoints = @("suppliers", "service-requests", "equipment", "material-requests", "grouped-orders")
    $allOk = $true
    
    foreach ($ep in $endpoints) {
        try {
            $data = Invoke-RestMethod "http://localhost:8080/api/$ep" -TimeoutSec 3
            Write-Host "  [OK] /api/$ep : $($data.Count) items" -ForegroundColor Green
        }
        catch {
            Write-Host "  [WARN] /api/$ep : Erreur" -ForegroundColor Yellow
            $allOk = $false
        }
    }
    
    if (-not $allOk) {
        Write-Host "`n[WARN] Certains endpoints ont des erreurs mais on continue..." -ForegroundColor Yellow
    }
    
    Write-Host ""
}

# ETAPE 3: Demarrer le DESKTOP
if (-not $BackendOnly) {
    Write-Host "[DESKTOP] Demarrage de l'interface JavaFX..." -ForegroundColor Cyan
    Write-Host "[INFO] Le desktop va se connecter au backend" -ForegroundColor Yellow
    
    if ($DesktopOnly) {
        # Mode desktop seul - verifier que le backend existe
        try {
            $test = Invoke-RestMethod "http://localhost:8080/api/suppliers" -TimeoutSec 2
            Write-Host "[OK] Backend deja actif - $($test.Count) fournisseurs" -ForegroundColor Green
        }
        catch {
            Write-Host "[ERREUR] Backend non disponible !" -ForegroundColor Red
            Write-Host "[ACTION] Lancez d'abord: .\start-magsav.ps1 -BackendOnly" -ForegroundColor Yellow
            exit 1
        }
    }
    
    # Lancer le desktop
    Start-Process powershell -ArgumentList "-ExecutionPolicy Bypass -NoExit -Command `"cd '$PWD'; Write-Host 'MAGSAV Desktop - JavaFX' -ForegroundColor Green; .\gradlew :desktop-javafx:run`"" -WindowStyle Normal
    Start-Sleep 3
    Write-Host "[OK] Desktop lance" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "       DEMARRAGE TERMINE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Backend:  http://localhost:8080" -ForegroundColor White
if (-not $BackendOnly) {
    Write-Host "Desktop:  Interface JavaFX lancee" -ForegroundColor White
}
Write-Host "`n[IMPORTANT] Le desktop utilise UNIQUEMENT" -ForegroundColor Yellow
Write-Host "            les donnees du backend !" -ForegroundColor Yellow
Write-Host "`nPour arreter: Fermez les terminaux ou Ctrl+C" -ForegroundColor Gray
Write-Host "========================================`n" -ForegroundColor Cyan