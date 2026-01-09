# =============================================================
# Script d'import CSV LOCMAT vers MAGSAV 3.0
# Utilise l'API REST pour importer les données du fichier CSV
# =============================================================

param(
    [string]$CsvFile = "..\Exports LOCMAT\IMPORT_MAGSAV.csv",
    [string]$BackendUrl = "http://localhost:8080"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " IMPORT LOCMAT CSV - MAGSAV 3.0" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que le fichier existe
$FullPath = Resolve-Path $CsvFile -ErrorAction SilentlyContinue
if (-not $FullPath) {
    Write-Host "❌ Fichier non trouvé: $CsvFile" -ForegroundColor Red
    Write-Host "   Vérifiez le chemin du fichier CSV LOCMAT" -ForegroundColor Yellow
    exit 1
}

Write-Host "📄 Fichier CSV: $FullPath" -ForegroundColor Green
$FileInfo = Get-Item $FullPath
Write-Host "   Taille: $([math]::Round($FileInfo.Length / 1KB, 2)) KB" -ForegroundColor Gray

# Vérifier que le backend est accessible
Write-Host ""
Write-Host "🔌 Vérification du backend..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "$BackendUrl/api/equipment/stats" -Method GET -TimeoutSec 5
    Write-Host "✅ Backend accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend non accessible sur $BackendUrl" -ForegroundColor Red
    Write-Host "   Assurez-vous que le backend est démarré (./gradlew :backend:bootRun)" -ForegroundColor Yellow
    exit 1
}

# Demander confirmation avant l'import (DESTRUCTIF)
Write-Host ""
Write-Host "⚠️  ATTENTION: Cette opération va SUPPRIMER tous les équipements existants" -ForegroundColor Red
Write-Host "   et importer les données du fichier CSV LOCMAT." -ForegroundColor Yellow
Write-Host ""
$confirm = Read-Host "Continuer? (oui/non)"
if ($confirm -ne "oui") {
    Write-Host "❌ Import annulé" -ForegroundColor Yellow
    exit 0
}

# Préparer le fichier pour l'upload
Write-Host ""
Write-Host "📤 Envoi du fichier CSV au backend..." -ForegroundColor Yellow

try {
    # Créer le multipart form data
    $boundary = [System.Guid]::NewGuid().ToString()
    $FilePath = $FullPath.Path
    $FileName = [System.IO.Path]::GetFileName($FilePath)
    $FileBytes = [System.IO.File]::ReadAllBytes($FilePath)
    $FileContent = [System.Text.Encoding]::UTF8.GetString($FileBytes)
    
    $LF = "`r`n"
    $Body = (
        "--$boundary",
        "Content-Disposition: form-data; name=`"file`"; filename=`"$FileName`"",
        "Content-Type: text/csv",
        "",
        $FileContent,
        "--$boundary--"
    ) -join $LF
    
    $Headers = @{
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/equipment/import-locmat" -Method POST -Headers $Headers -Body $Body -TimeoutSec 120
    
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host " IMPORT TERMINÉ" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    
    if ($response.success) {
        Write-Host "✅ Succès!" -ForegroundColor Green
        Write-Host "   Équipements importés: $($response.imported)" -ForegroundColor Cyan
        Write-Host "   Lignes ignorées: $($response.skipped)" -ForegroundColor Yellow
        Write-Host "   Total lignes: $($response.totalLines)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Échec de l'import" -ForegroundColor Red
        Write-Host "   Erreur: $($response.error)" -ForegroundColor Red
    }
    
} catch {
    Write-Host ""
    Write-Host "❌ Erreur lors de l'import:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "💡 Conseil: Relancez l'application desktop pour voir les données importées" -ForegroundColor Cyan
