# Script d'import des vehicules depuis LOCMAT/VEHICULES.xlsx
# Utilisation: .\import-vehicules.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== Import des vehicules depuis VEHICULES.xlsx ===" -ForegroundColor Cyan

# Verifier que le backend est accessible
try {
    $null = Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles" -Method Get -TimeoutSec 5
    Write-Host "[OK] Backend accessible" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Backend non accessible sur http://localhost:8080" -ForegroundColor Red
    Write-Host "  Lancez le backend avec: ./gradlew.bat :backend:bootRun" -ForegroundColor Yellow
    exit 1
}

# Chemin du fichier Excel
$excelPath = Join-Path $PSScriptRoot "LOCMAT\VEHICULES.xlsx"
if (-not (Test-Path $excelPath)) {
    # Essayer avec l'accent
    $excelPath = Join-Path $PSScriptRoot "LOCMAT\V*HICULES.xlsx"
    $files = Get-ChildItem -Path $excelPath -ErrorAction SilentlyContinue
    if ($files) {
        $excelPath = $files[0].FullName
    }
    else {
        Write-Host "[ERREUR] Fichier non trouve: LOCMAT\VEHICULES.xlsx" -ForegroundColor Red
        exit 1
    }
}

Write-Host "[OK] Fichier trouve: $excelPath" -ForegroundColor Green

# Lire le fichier Excel
Write-Host ""
Write-Host "Lecture du fichier Excel..." -ForegroundColor Yellow
$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$excel.DisplayAlerts = $false

try {
    $workbook = $excel.Workbooks.Open($excelPath)
    $sheet = $workbook.Sheets.Item(1)
    $rows = $sheet.UsedRange.Rows.Count
    $cols = $sheet.UsedRange.Columns.Count
    
    Write-Host "  Colonnes: $cols, Lignes: $rows" -ForegroundColor Gray
    
    # Lire les en-tetes
    $headers = @{}
    for ($i = 1; $i -le $cols; $i++) {
        $header = $sheet.Cells.Item(1, $i).Text.Trim()
        $headers[$header] = $i
    }
    
    Write-Host "  Colonnes trouvees: $($headers.Keys -join ', ')" -ForegroundColor Gray
    
    # Mapper les colonnes Excel vers les champs API
    $columnMapping = @{
        "Nom" = "name"
        "Type" = "type"
        "Immatriculation" = "licensePlate"
        "Marque" = "brand"
        "Couleur" = "color"
        "Modele" = "model"
        "Commentaire" = "notes"
        "Proprietaire" = "owner"
    }
    
    # Ajouter les variantes avec accents
    $columnMappingWithAccents = @{
        "Modèle" = "model"
        "Propriétaire" = "owner"
    }
    
    foreach ($key in $columnMappingWithAccents.Keys) {
        $columnMapping[$key] = $columnMappingWithAccents[$key]
    }
    
    # Collecter les donnees
    $vehiclesData = @()
    for ($row = 2; $row -le $rows; $row++) {
        $vehicleData = @{}
        
        foreach ($excelCol in $columnMapping.Keys) {
            if ($headers.ContainsKey($excelCol)) {
                $colIndex = $headers[$excelCol]
                $value = $sheet.Cells.Item($row, $colIndex).Text.Trim()
                $apiField = $columnMapping[$excelCol]
                $vehicleData[$apiField] = $value
            }
        }
        
        # Ignorer les lignes vides
        if ($vehicleData["name"] -and $vehicleData["name"] -ne "") {
            $vehiclesData += $vehicleData
        }
    }
    
    Write-Host "  $($vehiclesData.Count) vehicules trouves" -ForegroundColor Green
    
}
finally {
    $workbook.Close($false)
    $excel.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($excel) | Out-Null
}

# Demander confirmation
Write-Host ""
Write-Host "[ATTENTION] Cette operation va:" -ForegroundColor Yellow
Write-Host "   1. Supprimer TOUS les vehicules existants" -ForegroundColor Yellow
Write-Host "   2. Importer $($vehiclesData.Count) vehicules depuis le fichier Excel" -ForegroundColor Yellow
$confirm = Read-Host "Continuer? (O/N)"

if ($confirm -ne "O" -and $confirm -ne "o") {
    Write-Host "Import annule" -ForegroundColor Red
    exit 0
}

# Supprimer les vehicules existants
Write-Host ""
Write-Host "Suppression des vehicules existants..." -ForegroundColor Yellow
try {
    $deleteResult = Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles/all" -Method Delete
    Write-Host "  [OK] $($deleteResult.deleted) vehicules supprimes" -ForegroundColor Green
}
catch {
    Write-Host "  [ERREUR] lors de la suppression: $_" -ForegroundColor Red
    exit 1
}

# Importer les nouveaux vehicules
Write-Host ""
Write-Host "Import des vehicules..." -ForegroundColor Yellow
try {
    $jsonBody = $vehiclesData | ConvertTo-Json -Depth 10 -Compress
    Write-Host "  JSON preview: $($jsonBody.Substring(0, [Math]::Min(200, $jsonBody.Length)))..." -ForegroundColor Gray
    
    # Encoder en UTF-8
    $utf8Bytes = [System.Text.Encoding]::UTF8.GetBytes($jsonBody)
    
    $Headers = @{"Content-Type" = "application/json; charset=utf-8"}
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/vehicles/import" `
        -Method Post `
        -Headers $Headers `
        -Body $utf8Bytes `
        -UseBasicParsing
    
    $importResult = $response.Content | ConvertFrom-Json
    
    Write-Host "  [OK] $($importResult.imported) vehicules importes" -ForegroundColor Green
    if ($importResult.skipped -gt 0) {
        Write-Host "  [ATTENTION] $($importResult.skipped) vehicules ignores" -ForegroundColor Yellow
    }
    Write-Host "  Total en base: $($importResult.total) vehicules" -ForegroundColor Cyan
    
}
catch {
    Write-Host "  [ERREUR] lors de l'import: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Import termine avec succes ===" -ForegroundColor Green
