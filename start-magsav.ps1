#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Lance l'application MAGSAV complète avec choix des composants

.DESCRIPTION
    Script de démarrage interactif pour MAGSAV 3.0
    - Backend Spring Boot sur port 8080
    - Frontend React sur port 3000
    - Desktop JavaFX (optionnel)
    
.PARAMETER Mode
    Mode de lancement: web (Backend+Frontend), desktop, full (tout)
    
.EXAMPLE
    ./start-magsav.ps1 -Mode web
    ./start-magsav.ps1 -Mode desktop
    ./start-magsav.ps1 -Mode full
    
.NOTES
    Auteur: MAGSAV Team
    Date: 6 janvier 2026
#>

param(
    [Parameter()]
    [ValidateSet("web", "desktop", "full")]
    [string]$Mode = ""
)

# Configuration
$ErrorActionPreference = "Stop"
$BackendPort = 8080
$FrontendPort = 3000
$WorkspaceRoot = $PSScriptRoot

Write-Host @"

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║           MAGSAV 3.0 - Démarrage Application             ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Cyan

# Menu interactif si pas de mode spécifié
if (-not $Mode) {
    Write-Host "Sélectionnez le mode de démarrage:`n" -ForegroundColor Yellow
    Write-Host "  1. 🌐 Web (Backend + Frontend)" -ForegroundColor Green
    Write-Host "  2. 💻 Desktop (Application JavaFX)" -ForegroundColor Blue
    Write-Host "  3. 🚀 Full Stack (Backend + Frontend + Desktop)" -ForegroundColor Magenta
    Write-Host "  4. ❌ Annuler`n" -ForegroundColor Red
    
    $choice = Read-Host "Votre choix (1-4)"
    
    switch ($choice) {
        "1" { $Mode = "web" }
        "2" { $Mode = "desktop" }
        "3" { $Mode = "full" }
        "4" { 
            Write-Host "`n❌ Démarrage annulé" -ForegroundColor Red
            exit 0 
        }
        default {
            Write-Host "`n❌ Choix invalide" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host "`n📋 Mode sélectionné: $($Mode.ToUpper())`n" -ForegroundColor Cyan

# Vérifier si les ports sont déjà utilisés
function Test-PortInUse {
    param([int]$Port)
    $connections = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue
    return $connections | Where-Object { $_.LocalPort -eq $Port }
}

# Vérifier les ports pour web et full
if ($Mode -eq "web" -or $Mode -eq "full") {
    Write-Host "🔍 Vérification des ports..." -ForegroundColor Yellow

    if (Test-PortInUse -Port $BackendPort) {
        Write-Host "⚠️  Port $BackendPort déjà utilisé (Backend déjà démarré ?)" -ForegroundColor Yellow
        $response = Read-Host "Continuer quand même ? (o/N)"
        if ($response -ne "o") {
            Write-Host "❌ Démarrage annulé" -ForegroundColor Red
            exit 1
        }
    }

    if (Test-PortInUse -Port $FrontendPort) {
        Write-Host "⚠️  Port $FrontendPort déjà utilisé (Frontend déjà démarré ?)" -ForegroundColor Yellow
        $response = Read-Host "Continuer quand même ? (o/N)"
        if ($response -ne "o") {
            Write-Host "❌ Démarrage annulé" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host "`n📦 Vérification des dépendances..." -ForegroundColor Yellow

# Vérifier Java (toujours nécessaire)
try {
    $javaVersion = java -version 2>&1 | Select-String -Pattern "version"
    Write-Host "  ✅ Java détecté: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Java non trouvé - Installation requise" -ForegroundColor Red
    exit 1
}

# Vérifier Node.js pour web et full
if ($Mode -eq "web" -or $Mode -eq "full") {
    try {
        $nodeVersion = node --version
        Write-Host "  ✅ Node.js détecté: $nodeVersion" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Node.js non trouvé - Installation requise" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🚀 Démarrage de l'application..." -ForegroundColor Cyan

$jobs = @()

# Démarrer selon le mode
switch ($Mode) {
    "web" {
        # Backend
        Write-Host "`n[Backend] Démarrage Spring Boot sur port $BackendPort..." -ForegroundColor Yellow
        $backendJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot'; Write-Host '════════════════════════════════════════' -ForegroundColor Blue; Write-Host '   MAGSAV Backend - Spring Boot' -ForegroundColor Blue; Write-Host '   Port: $BackendPort' -ForegroundColor Blue; Write-Host '════════════════════════════════════════' -ForegroundColor Blue; Write-Host ''; .\gradlew.bat :backend:bootRun --console=plain"
        ) -PassThru
        $jobs += $backendJob

        Start-Sleep -Seconds 3

        # Frontend
        Write-Host "`n[Frontend] Démarrage React Dev Server sur port $FrontendPort..." -ForegroundColor Yellow
        $frontendJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot\web-frontend'; Write-Host '════════════════════════════════════════' -ForegroundColor Magenta; Write-Host '   MAGSAV Frontend - React' -ForegroundColor Magenta; Write-Host '   Port: $FrontendPort' -ForegroundColor Magenta; Write-Host '════════════════════════════════════════' -ForegroundColor Magenta; Write-Host ''; npm start"
        ) -PassThru
        $jobs += $frontendJob
    }
    
    "desktop" {
        # Desktop seul
        Write-Host "`n[Desktop] Démarrage JavaFX Application..." -ForegroundColor Yellow
        $desktopJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot'; Write-Host '════════════════════════════════════════' -ForegroundColor Green; Write-Host '   MAGSAV Desktop - JavaFX 21' -ForegroundColor Green; Write-Host '════════════════════════════════════════' -ForegroundColor Green; Write-Host ''; .\gradlew.bat :desktop-javafx:run --console=plain"
        ) -PassThru
        $jobs += $desktopJob
    }
    
    "full" {
        # Backend
        Write-Host "`n[Backend] Démarrage Spring Boot sur port $BackendPort..." -ForegroundColor Yellow
        $backendJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot'; Write-Host '════════════════════════════════════════' -ForegroundColor Blue; Write-Host '   MAGSAV Backend - Spring Boot' -ForegroundColor Blue; Write-Host '   Port: $BackendPort' -ForegroundColor Blue; Write-Host '════════════════════════════════════════' -ForegroundColor Blue; Write-Host ''; .\gradlew.bat :backend:bootRun --console=plain"
        ) -PassThru
        $jobs += $backendJob

        Start-Sleep -Seconds 3

        # Frontend
        Write-Host "`n[Frontend] Démarrage React Dev Server sur port $FrontendPort..." -ForegroundColor Yellow
        $frontendJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot\web-frontend'; Write-Host '════════════════════════════════════════' -ForegroundColor Magenta; Write-Host '   MAGSAV Frontend - React' -ForegroundColor Magenta; Write-Host '   Port: $FrontendPort' -ForegroundColor Magenta; Write-Host '════════════════════════════════════════' -ForegroundColor Magenta; Write-Host ''; npm start"
        ) -PassThru
        $jobs += $frontendJob

        Start-Sleep -Seconds 2

        # Desktop
        Write-Host "`n[Desktop] Démarrage JavaFX Application..." -ForegroundColor Yellow
        $desktopJob = Start-Process powershell -ArgumentList @(
            "-NoExit",
            "-Command",
            "cd '$WorkspaceRoot'; Write-Host '════════════════════════════════════════' -ForegroundColor Green; Write-Host '   MAGSAV Desktop - JavaFX 21' -ForegroundColor Green; Write-Host '════════════════════════════════════════' -ForegroundColor Green; Write-Host ''; .\gradlew.bat :desktop-javafx:run --console=plain"
        ) -PassThru
        $jobs += $desktopJob
    }
}

Start-Sleep -Seconds 2

# Affichage des URLs selon le mode
switch ($Mode) {
    "web" {
        Write-Host @"

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  ✅ Application MAGSAV Web en cours de démarrage         ║
║                                                          ║
║  🌐 Backend:   http://localhost:$BackendPort                  ║
║  🌐 Frontend:  http://localhost:$FrontendPort                  ║
║                                                          ║
║  📝 Swagger:   http://localhost:$BackendPort/swagger-ui.html  ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Green
    }
    
    "desktop" {
        Write-Host @"

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  ✅ Application MAGSAV Desktop en cours de démarrage     ║
║                                                          ║
║  💻 L'interface JavaFX va s'ouvrir dans quelques         ║
║     secondes...                                          ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Green
    }
    
    "full" {
        Write-Host @"

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  ✅ Application MAGSAV COMPLÈTE en cours de démarrage    ║
║                                                          ║
║  🌐 Backend:   http://localhost:$BackendPort                  ║
║  🌐 Frontend:  http://localhost:$FrontendPort                  ║
║  💻 Desktop:   JavaFX Application                        ║
║                                                          ║
║  📝 Swagger:   http://localhost:$BackendPort/swagger-ui.html  ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Green
    }
}

Write-Host "⏳ Les terminaux vont s'ouvrir séparément`n" -ForegroundColor Gray

# Fonction pour vérifier si un service est prêt
function Wait-ForService {
    param(
        [string]$Url,
        [string]$Name,
        [int]$MaxAttempts = 30,
        [int]$DelaySeconds = 2
    )
    
    $attempt = 0
    while ($attempt -lt $MaxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "  ✅ $Name prêt !" -ForegroundColor Green
                return $true
            }
        } catch {
            # Service pas encore prêt
        }
        
        $attempt++
        Write-Host "  ⏳ $Name démarrage... ($attempt/$MaxAttempts)" -ForegroundColor Gray
        Start-Sleep -Seconds $DelaySeconds
    }
    
    Write-Host "  ⚠️  $Name timeout - Vérifiez les terminaux" -ForegroundColor Yellow
    return $false
}

# Attendre les services selon le mode
if ($Mode -eq "web" -or $Mode -eq "full") {
    Write-Host "`n🔍 Vérification des services..." -ForegroundColor Yellow
    Wait-ForService -Url "http://localhost:$BackendPort/actuator/health" -Name "Backend"
    Wait-ForService -Url "http://localhost:$FrontendPort" -Name "Frontend"
}

if ($Mode -eq "desktop") {
    Write-Host "`n💻 L'application Desktop s'ouvre..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
}

Write-Host @"

╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  🎉 MAGSAV 3.0 opérationnel !                            ║
║                                                          ║
║  🛑 Pour arrêter: Fermer les terminaux ou appuyer sur   ║
║     Entrée dans ce terminal                              ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Green

Write-Host "💡 Astuce: Gardez ce terminal ouvert pour surveiller le statut`n" -ForegroundColor Cyan

# Garder le script actif
Write-Host "Appuyez sur Entrée pour arrêter l'application..." -ForegroundColor Yellow
Read-Host

# Cleanup
Write-Host "`n🛑 Arrêt de l'application..." -ForegroundColor Yellow
foreach ($job in $jobs) {
    Stop-Process -Id $job.Id -Force -ErrorAction SilentlyContinue
}
Write-Host "✅ Application arrêtée" -ForegroundColor Green
