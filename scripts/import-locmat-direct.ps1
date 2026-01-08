# Script d'import LOCMAT via API
# Usage: .\import-locmat-direct.ps1

param(
    [string]$CsvFile = "Exports LOCMAT\Inventaire_Complet_Avec_NS.csv",
    [string]$ApiUrl = "http://localhost:8080/api/equipment/import-locmat"
)

Write-Host "=== IMPORT LOCMAT VIA API ===" -ForegroundColor Cyan
Write-Host ""

# Vérification du fichier CSV
if (-not (Test-Path $CsvFile)) {
    Write-Host "❌ Fichier CSV non trouvé: $CsvFile" -ForegroundColor Red
    exit 1
}

$lines = (Get-Content $CsvFile | Measure-Object -Line).Lines
Write-Host "✅ Fichier CSV trouvé: $lines lignes ($(($lines - 1)) équipements)" -ForegroundColor Green

# Vérification du backend
Write-Host "`nVérification du backend..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
    Write-Host "✅ Backend opérationnel (Status: $($health.status))" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend non accessible!" -ForegroundColor Red
    Write-Host "   Assurez-vous que le backend est démarré sur le port 8080" -ForegroundColor Yellow
    Write-Host "   Commande: ./gradlew.bat :backend:bootRun" -ForegroundColor Gray
    exit 1
}

# Préparation de la requête multipart
Write-Host "`nPréparation de l'upload..." -ForegroundColor Yellow

$filePath = (Resolve-Path $CsvFile).Path
$fileName = Split-Path $filePath -Leaf

# Utilisation de curl si disponible (plus fiable pour multipart/form-data)
if (Get-Command curl.exe -ErrorAction SilentlyContinue) {
    Write-Host "Utilisation de curl pour l'upload..." -ForegroundColor Gray
    Write-Host ""
    
    $curlResult = curl.exe -X POST $ApiUrl `
        -F "file=@$filePath" `
        -H "Accept: application/json" `
        --max-time 120 `
        --silent `
        --show-error `
        --write-out "`n%{http_code}" 2>&1
    
    $statusCode = $curlResult[-1]
    $response = $curlResult[0..($curlResult.Length-2)] -join "`n"
    
    Write-Host ""
    if ($statusCode -eq 200) {
        Write-Host "✅ IMPORT RÉUSSI!" -ForegroundColor Green
        Write-Host "`nRéponse du serveur:" -ForegroundColor Cyan
        Write-Host $response
        
        # Conversion JSON si possible
        try {
            $jsonResponse = $response | ConvertFrom-Json
            Write-Host "`n📊 Statistiques:" -ForegroundColor Cyan
            Write-Host "   Importés: $($jsonResponse.imported)" -ForegroundColor Green
            Write-Host "   Ignorés: $($jsonResponse.skipped)" -ForegroundColor Yellow
            Write-Host "   Total lignes: $($jsonResponse.totalLines)" -ForegroundColor Gray
            if ($jsonResponse.errors -and $jsonResponse.errors.Count -gt 0) {
                Write-Host "`n⚠️ Erreurs:" -ForegroundColor Yellow
                $jsonResponse.errors | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
            }
        } catch {
            # Si ce n'est pas du JSON, afficher tel quel
        }
    } else {
        Write-Host "❌ Erreur HTTP $statusCode" -ForegroundColor Red
        Write-Host $response -ForegroundColor Red
    }
} else {
    # Fallback sur Invoke-WebRequest (PowerShell natif)
    Write-Host "Utilisation de Invoke-WebRequest..." -ForegroundColor Gray
    
    try {
        Add-Type -AssemblyName System.Net.Http
        
        $httpClient = New-Object System.Net.Http.HttpClient
        $httpClient.Timeout = [TimeSpan]::FromSeconds(120)
        
        $content = New-Object System.Net.Http.MultipartFormDataContent
        $fileStream = [System.IO.File]::OpenRead($filePath)
        $fileContent = New-Object System.Net.Http.StreamContent($fileStream)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("text/csv")
        
        $content.Add($fileContent, "file", $fileName)
        
        Write-Host ""
        $response = $httpClient.PostAsync($ApiUrl, $content).Result
        $responseContent = $response.Content.ReadAsStringAsync().Result
        
        $fileStream.Close()
        $httpClient.Dispose()
        
        if ($response.IsSuccessStatusCode) {
            Write-Host "✅ IMPORT RÉUSSI!" -ForegroundColor Green
            Write-Host "`nRéponse du serveur:" -ForegroundColor Cyan
            
            try {
                $jsonResponse = $responseContent | ConvertFrom-Json
                Write-Host "`n📊 Statistiques:" -ForegroundColor Cyan
                Write-Host "   Importés: $($jsonResponse.imported)" -ForegroundColor Green
                Write-Host "   Ignorés: $($jsonResponse.skipped)" -ForegroundColor Yellow
                Write-Host "   Total lignes: $($jsonResponse.totalLines)" -ForegroundColor Gray
                if ($jsonResponse.errors -and $jsonResponse.errors.Count -gt 0) {
                    Write-Host "`n⚠️ Erreurs:" -ForegroundColor Yellow
                    $jsonResponse.errors | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
                }
            } catch {
                Write-Host $responseContent
            }
        } else {
            Write-Host "❌ Erreur HTTP $($response.StatusCode)" -ForegroundColor Red
            Write-Host $responseContent -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Erreur lors de l'upload:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Vérification finale
Write-Host "`n=== VÉRIFICATION ===" -ForegroundColor Cyan
try {
    $equipment = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -Method Get -TimeoutSec 10
    Write-Host "✅ Nombre d'équipements dans la base: $($equipment.Count)" -ForegroundColor Green
    
    if ($equipment.Count -gt 2000) {
        Write-Host "🎉 Import complet réussi!" -ForegroundColor Green
    } elseif ($equipment.Count -gt 6) {
        Write-Host "⚠️ Import partiel ($($equipment.Count) équipements)" -ForegroundColor Yellow
    } else {
        Write-Host "⚠️ Seuls les équipements de test sont présents" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ Impossible de vérifier le nombre d'équipements" -ForegroundColor Yellow
}

Write-Host ""
