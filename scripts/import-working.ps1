$API_URL = "http://localhost:8080/api"
$CSV = ".\Exports LOCMAT\Inventaire_Complet_Avec_NS_Clean.csv"

Write-Host "Import equipements LOCMAT" -ForegroundColor Cyan

$data = Import-Csv -Path $CSV -Encoding UTF8
Write-Host "Lignes: $($data.Count)"

$count = 0
foreach ($row in $data) {
    $refCol = $row.PSObject.Properties | Where-Object { $_.Name -eq 'Réf' } | Select-Object -First 1
    $ref = if ($refCol) { $refCol.Value } else { $null }
    $nom = $row.Nom
    
    if ($ref -and $nom) {
        $equipment = @{
            name = $nom
            internalReference = $ref
            status = "AVAILABLE"
        }
        
        try {
            $json = $equipment | ConvertTo-Json -Compress
            Invoke-RestMethod -Uri "$API_URL/equipment" -Method Post -Body $json -ContentType "application/json; charset=utf-8" | Out-Null
            $count++
            if ($count % 100 -eq 0) { Write-Host "  $count..." }
        } catch {
            Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
            break
        }
    }
}

Write-Host "Importe: $count" -ForegroundColor Green
