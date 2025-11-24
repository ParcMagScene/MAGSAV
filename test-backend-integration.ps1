# Script de Test IntÃ©gration Backend MAGSAV
# DÃ©marre backend + frontend pour tester la communication REST

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MAGSAV 3.0 - Test IntÃ©gration Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# VÃ©rifier que nous sommes dans le bon rÃ©pertoire
if (!(Test-Path ".\gradlew.bat")) {
    Write-Host "âŒ Erreur: gradlew.bat non trouvÃ©" -ForegroundColor Red
    Write-Host "   ExÃ©cutez ce script depuis la racine du projet MAGSAV-3.0" -ForegroundColor Yellow
    exit 1
}

Write-Host "ðŸ“‹ Ã‰tape 1/4 : Compilation du projet..." -ForegroundColor Green
./gradlew build -x test --quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "âŒ Ã‰chec de la compilation" -ForegroundColor Red
    exit 1
}
Write-Host "âœ… Compilation rÃ©ussie" -ForegroundColor Green
Write-Host ""

Write-Host "ðŸ“‹ Ã‰tape 2/4 : DÃ©marrage du backend Spring Boot..." -ForegroundColor Green
Write-Host "   ðŸŒ URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host "   ðŸ“¡ Endpoints: /api/material-requests, /api/suppliers, etc." -ForegroundColor Cyan

# DÃ©marrer le backend dans un nouveau terminal
$backendJob = Start-Process powershell -ArgumentList `
    "-NoExit", `
    "-Command", `
    "cd '$PWD' ; Write-Host '=== MAGSAV Backend ===' -ForegroundColor Green ; ./gradlew :backend:bootRun" `
    -PassThru

Write-Host "âœ… Backend dÃ©marrÃ© (PID: $($backendJob.Id))" -ForegroundColor Green
Write-Host "   Attente de 20 secondes pour initialisation..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "ðŸ“‹ Ã‰tape 3/4 : VÃ©rification du backend..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -TimeoutSec 5 -UseBasicParsing
    Write-Host "âœ… Backend opÃ©rationnel (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "âš ï¸  Backend pas encore prÃªt, mais on continue..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ðŸ“‹ Ã‰tape 4/4 : Lancement de l'application Desktop..." -ForegroundColor Green
Write-Host ""
Write-Host "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—" -ForegroundColor Cyan
Write-Host "â•‘  ðŸ“ TEST Ã€ EFFECTUER :                                 â•‘" -ForegroundColor Cyan
Write-Host "â•‘                                                        â•‘" -ForegroundColor Cyan
Write-Host "â•‘  1. L'application va se lancer                        â•‘" -ForegroundColor White
Write-Host "â•‘  2. Cliquez sur 'Fournisseurs & Demandes'            â•‘" -ForegroundColor White
Write-Host "â•‘  3. VÃ©rifiez le message de statut :                  â•‘" -ForegroundColor White
Write-Host "â•‘     âœ… 'X demande(s) chargÃ©e(s) depuis le backend'    â•‘" -ForegroundColor Green
Write-Host "â•‘     âš ï¸  'Backend indisponible - donnÃ©es test'         â•‘" -ForegroundColor Yellow
Write-Host "â•‘  4. Testez approbation/rejet d'une demande           â•‘" -ForegroundColor White
Write-Host "â•‘                                                        â•‘" -ForegroundColor Cyan
Write-Host "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•" -ForegroundColor Cyan
Write-Host ""
Write-Host "Appuyez sur une touche pour lancer l'application..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Lancer l'application desktop
Write-Host ""
Write-Host "ðŸš€ Lancement MAGSAV Desktop..." -ForegroundColor Green
./gradlew :desktop-javafx:run

# Cleanup
Write-Host ""
Write-Host "ðŸ“‹ ArrÃªt des services..." -ForegroundColor Yellow
try {
    Stop-Process -Id $backendJob.Id -Force -ErrorAction SilentlyContinue
    Write-Host "âœ… Backend arrÃªtÃ©" -ForegroundColor Green
} catch {
    Write-Host "âš ï¸  Impossible d'arrÃªter le backend (PID: $($backendJob.Id))" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "âœ… Test terminÃ© !" -ForegroundColor Green
