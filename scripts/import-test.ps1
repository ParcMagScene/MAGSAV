$API_URL = "http://localhost:8080/api"
$CSV = ".\Exports LOCMAT\Inventaire_Complet_Avec_NS_Clean.csv"

Write-Host "Import test" -ForegroundColor Cyan

$data = Import-Csv -Path $CSV -Encoding UTF8
Write-Host "Lignes: $($data.Count)"

$count = 0
$debug = 0
foreach ($row in $data) {
    $ref = $row.'Réf'
    $nom = $row.Nom
    
    if ($debug -lt 3) {
        Write-Host "DEBUG: Ref='$ref' Nom='$nom' Test=$($ref -and $nom)"
        $debug++
    }
    
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

Write-Host "Importé: $count" -ForegroundColor Green
