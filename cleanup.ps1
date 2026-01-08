# Script de nettoyage MAGSAV-3.0
Write-Host "NETTOYAGE PROJET MAGSAV-3.0" -ForegroundColor Cyan

$filesToDelete = @(
    "AUDIT-COMPLET-2026-01-06.md",
    "backend-error.log",
    "backend-output.log",
    "BILAN-FINAL-API.md",
    "EXPORT-IMPORT-DOC.md",
    "MIGRATION-WEB-ONLY.md",
    "NETTOYAGE.md",
    "NOUVEAUX-ENDPOINTS-API.md",
    "PHASE-2-COMPLETE.md",
    "PLANNING-CONTROLLER-DOC.md",
    "PROGRESSION-REFACTORISATION.md",
    "RAPPORT-IMPORT-LOCMAT.md",
    "RAPPORT-NETTOYAGE-REFACTORISATION.md",
    "RESUME-FINAL.md",
    "VALIDATION-PHASE-3.md",
    "sav-test.json",
    "service-requests-api-response.json",
    "test-output.json",
    "test-sav.json",
    "test_import.csv",
    "test_import.xlsx",
    "IMPORT_MAGSAV.xlsx",
    "update-vehicle-photos.sql",
    "start-dev-full.ps1",
    "start-magsav-full.ps1"
)

$deleted = 0
$totalSize = 0

Write-Host "Suppression fichiers obsoletes..." -ForegroundColor Yellow

foreach ($file in $filesToDelete) {
    if (Test-Path $file) {
        $size = (Get-Item $file).Length
        $totalSize += $size
        Remove-Item $file -Force
        Write-Host "OK: $file" -ForegroundColor Green
        $deleted++
    }
}

Write-Host ""
Write-Host "RESULTATS" -ForegroundColor Cyan
Write-Host "Fichiers supprimes: $deleted" -ForegroundColor Green
Write-Host "Espace libere: $([math]::Round($totalSize/1MB, 2)) MB" -ForegroundColor Green
Write-Host "Nettoyage termine" -ForegroundColor Green

