# Script de Test Intégration Backend MAGSAV
# Démarre backend + frontend pour tester la communication REST

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MAGSAV 3.0 - Test Integration Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que nous sommes dans le bon répertoire
if (!(Test-Path ".\gradlew.bat")) {
    Write-Host "Erreur: gradlew.bat non trouve" -ForegroundColor Red
    Write-Host "Executez ce script depuis la racine du projet MAGSAV-3.0" -ForegroundColor Yellow
    exit 1
}

Write-Host "Etape 1/4 : Compilation du projet..." -ForegroundColor Green
./gradlew build -x test --quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "Echec de la compilation" -ForegroundColor Red
    exit 1
}
Write-Host "Compilation reussie" -ForegroundColor Green
Write-Host ""

Write-Host "Etape 2/4 : Demarrage du backend Spring Boot..." -ForegroundColor Green
Write-Host "URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Endpoints: /api/material-requests, /api/suppliers, etc." -ForegroundColor Cyan

# Démarrer le backend dans un nouveau terminal
$backendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD' ; Write-Host 'MAGSAV Backend' -ForegroundColor Green ; ./gradlew :backend:bootRun" -PassThru

Write-Host "Backend demarre (PID: $($backendJob.Id))" -ForegroundColor Green
Write-Host "Attente de 20 secondes pour initialisation..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "Etape 3/4 : Verification du backend..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -TimeoutSec 5 -UseBasicParsing
    Write-Host "Backend operationnel (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "Backend pas encore pret, mais on continue..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Etape 4/4 : Lancement de l'application Desktop..." -ForegroundColor Green
Write-Host ""
Write-Host "TEST A EFFECTUER :" -ForegroundColor Cyan
Write-Host "1. L'application va se lancer" -ForegroundColor White
Write-Host "2. Cliquez sur 'Fournisseurs & Demandes'" -ForegroundColor White
Write-Host "3. Verifiez le message de statut :" -ForegroundColor White
Write-Host "   - X demande(s) chargee(s) depuis le backend" -ForegroundColor Green
Write-Host "   - Backend indisponible - donnees test" -ForegroundColor Yellow
Write-Host "4. Testez approbation/rejet d'une demande" -ForegroundColor White
Write-Host ""
Write-Host "Appuyez sur une touche pour lancer l'application..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Lancer l'application desktop
Write-Host ""
Write-Host "Lancement MAGSAV Desktop..." -ForegroundColor Green
./gradlew :desktop-javafx:run

# Cleanup
Write-Host ""
Write-Host "Arret des services..." -ForegroundColor Yellow
try {
    Stop-Process -Id $backendJob.Id -Force -ErrorAction SilentlyContinue
    Write-Host "Backend arrete" -ForegroundColor Green
} catch {
    Write-Host "Impossible d'arreter le backend (PID: $($backendJob.Id))" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Test termine !" -ForegroundColor Green
