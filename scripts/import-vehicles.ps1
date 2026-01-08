$API_URL = "http://localhost:8080/api"
$CSV = ".\Exports LOCMAT\VÉHICULES.csv"

Write-Host "Import vehicules LOCMAT" -ForegroundColor Cyan

$data = Import-Csv -Path $CSV -Encoding UTF8
Write-Host "Lignes: $($data.Count)"

$count = 0
foreach ($row in $data) {
    $nom = $row.Nom
    
    if ($nom) {
        $typeStr = if ($row.Type) { $row.Type } else { "Autre" }
        $vehicleType = switch -Regex ($typeStr) {
            "VL 20 ?m3" { "VL_20M3" }
            "VL 17 ?m3" { "VL_17M3" }
            "^VL$" { "VL" }
            "Fourgon" { "VAN" }
            "Camion" { "TRUCK" }
            "Porteur" { "PORTEUR" }
            "Tracteur" { "TRACTEUR" }
            "Semi" { "SEMI_REMORQUE" }
            "Scène|Scene" { "SCENE_MOBILE" }
            "Remorque" { "TRAILER" }
            "Voiture" { "CAR" }
            "Moto" { "MOTORCYCLE" }
            default { "OTHER" }
        }
        
        $modeleCol = $row.PSObject.Properties | Where-Object { $_.Name -like 'Mod*le' } | Select-Object -First 1
        $proprioCol = $row.PSObject.Properties | Where-Object { $_.Name -like 'Propri*taire' } | Select-Object -First 1
        
        $vehicle = @{
            name = $nom
            licensePlate = if ($row.Immatriculation) { $row.Immatriculation } else { $null }
            brand = if ($row.Marque) { $row.Marque } else { $null }
            model = if ($modeleCol) { $modeleCol.Value } else { $null }
            color = if ($row.Couleur) { $row.Couleur } else { $null }
            owner = if ($proprioCol) { $proprioCol.Value } else { $null }
            type = $vehicleType
            status = "AVAILABLE"
            notes = if ($row.Commentaire) { $row.Commentaire } else { $null }
        }
        
        try {
            $json = $vehicle | ConvertTo-Json -Compress
            Invoke-RestMethod -Uri "$API_URL/vehicles" -Method Post -Body $json -ContentType "application/json; charset=utf-8" | Out-Null
            $count++
        } catch {
            Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "Importe: $count vehicules" -ForegroundColor Green
