# =============================================================
# Script d'import CSV VÉHICULES vers MAGSAV 3.0
# Utilise l'API REST pour importer les véhicules
# =============================================================

param(
    [string]$CsvFile = "..\Exports LOCMAT\VÉHICULES.csv",
    [string]$BackendUrl = "http://localhost:8080"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " IMPORT VÉHICULES CSV - MAGSAV 3.0" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que le fichier existe
if (-not (Test-Path $CsvFile)) {
    $CsvFile = "C:\Users\aalou\MAGSAV-3.0\Exports LOCMAT\VÉHICULES.csv"
}

if (-not (Test-Path $CsvFile)) {
    Write-Host "❌ Fichier non trouvé: $CsvFile" -ForegroundColor Red
    exit 1
}

Write-Host "📄 Fichier CSV: $CsvFile" -ForegroundColor Green
$FileInfo = Get-Item $CsvFile
Write-Host "   Taille: $([math]::Round($FileInfo.Length / 1KB, 2)) KB" -ForegroundColor Gray

# Vérifier que le backend est accessible
Write-Host ""
Write-Host "🔌 Vérification du backend..." -ForegroundColor Yellow
try {
    $health = Invoke-WebRequest -Uri "$BackendUrl/actuator/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Backend accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend non accessible sur $BackendUrl" -ForegroundColor Red
    Write-Host "   Assurez-vous que le backend est démarré (./gradlew :backend:bootRun)" -ForegroundColor Yellow
    exit 1
}

# Lire le CSV
Write-Host ""
Write-Host "📖 Lecture du fichier CSV..." -ForegroundColor Yellow
try {
    $csvData = Import-Csv -Path $CsvFile -Encoding UTF8 -Delimiter ','
    Write-Host "✅ $($csvData.Count) véhicules trouvés dans le CSV" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur de lecture du CSV: $_" -ForegroundColor Red
    exit 1
}

# Mapper les données vers le format API
Write-Host ""
Write-Host "🔄 Conversion des données..." -ForegroundColor Yellow

$vehicles = @()
foreach ($row in $csvData) {
    $vehicle = @{
        name = $row.Nom
        type = $row.Type
        licensePlate = $row.Immatriculation
        brand = $row.Marque
        color = $row.Couleur
        model = $row.Modèle
        notes = $row.Commentaire
        owner = $row.Propriétaire
        status = "AVAILABLE"
    }
    
    # Retirer les champs vides
    $cleanVehicle = @{}
    foreach ($key in $vehicle.Keys) {
        $value = $vehicle[$key]
        if ($value -and $value.ToString().Trim() -ne "") {
            $cleanVehicle[$key] = $value.ToString().Trim()
        }
    }
    
    $vehicles += $cleanVehicle
}

Write-Host "✅ $($vehicles.Count) véhicules prêts à l'import" -ForegroundColor Green

# Afficher un aperçu
if ($vehicles.Count -gt 0) {
    Write-Host ""
    Write-Host "📋 Aperçu du premier véhicule:" -ForegroundColor Cyan
    $vehicles[0] | ConvertTo-Json | Write-Host
}

# Demander confirmation
Write-Host ""
Write-Host "⚠️  Cette opération va supprimer tous les véhicules existants" -ForegroundColor Yellow
Write-Host "   et importer $($vehicles.Count) nouveaux véhicules." -ForegroundColor Yellow
Write-Host ""
$confirm = Read-Host "Continuer? (oui/non)"
if ($confirm -ne "oui") {
    Write-Host "❌ Import annulé" -ForegroundColor Yellow
    exit 0
}

# Import via API
Write-Host ""
Write-Host "🚀 Import en cours..." -ForegroundColor Yellow

try {
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    $body = $vehicles | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod `
        -Uri "$BackendUrl/api/vehicles/import" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -TimeoutSec 30
    
    Write-Host ""
    Write-Host "✅ Import terminé avec succès!" -ForegroundColor Green
    Write-Host "   Véhicules importés: $($response.imported)" -ForegroundColor Green
    Write-Host "   Véhicules ignorés: $($response.skipped)" -ForegroundColor Gray
    
    if ($response.errors -and $response.errors.Count -gt 0) {
        Write-Host ""
        Write-Host "⚠️  Erreurs rencontrées:" -ForegroundColor Yellow
        foreach ($error in $response.errors) {
            Write-Host "   - $error" -ForegroundColor Yellow
        }
    }
    
} catch {
    Write-Host ""
    Write-Host "❌ Erreur lors de l'import: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Détails de l'erreur:" -ForegroundColor Yellow
    $_ | Format-List * -Force | Out-String | Write-Host
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " ✅ IMPORT TERMINÉ" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
