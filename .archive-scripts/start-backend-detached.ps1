# Script de démarrage du backend MAGSAV en mode détaché
Write-Host "🚀 Démarrage du backend MAGSAV-3.0..." -ForegroundColor Green

# Configuration Java
$env:JAVA_HOME = "C:\Users\aalou\.jdk\jdk-21.0.8"

# Démarrage en arrière-plan avec redirection des logs
Start-Process -WindowStyle Hidden -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "Set-Location 'C:\Users\aalou\MAGSAV-3.0'; .\gradlew :backend:bootRun"

Write-Host "✅ Backend démarré en arrière-plan" -ForegroundColor Green
Write-Host "🌐 API disponible sur: http://localhost:8080" -ForegroundColor Cyan
Write-Host "💾 Console H2: http://localhost:8080/h2-console" -ForegroundColor Yellow

# Attendre que le backend soit prêt
Write-Host "⏳ Vérification du démarrage..." -ForegroundColor Yellow
do {
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/projects" -UseBasicParsing -TimeoutSec 5
        $ready = $true
    } catch {
        $ready = $false
    }
} while (-not $ready)

Write-Host "✅ Backend opérationnel !" -ForegroundColor Green