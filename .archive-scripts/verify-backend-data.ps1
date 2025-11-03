# Script de vérification complète des données MAGSAV-3.0 Backend
# Teste tous les endpoints pour s'assurer que les données sont bien générées

Write-Host "🔍 MAGSAV-3.0 - Vérification Complète des Données Backend" -ForegroundColor Green
Write-Host ("=" * 60)

$baseUrl = "http://localhost:8080/api"
$endpoints = @(
    @{Name="Équipements"; Url="$baseUrl/equipment"},
    @{Name="Catégories"; Url="$baseUrl/categories"},  
    @{Name="Clients"; Url="$baseUrl/clients"},
    @{Name="Contrats"; Url="$baseUrl/contracts"},
    @{Name="Contacts"; Url="$baseUrl/contacts"},
    @{Name="Véhicules"; Url="$baseUrl/vehicles"},
    @{Name="Projets"; Url="$baseUrl/projects"},
    @{Name="Demandes SAV"; Url="$baseUrl/service-requests"},
    @{Name="Personnel"; Url="$baseUrl/personnel"},
    @{Name="Commandes Fournisseurs"; Url="$baseUrl/supplier-orders"}
)

$totalRecords = 0
$successCount = 0

foreach ($endpoint in $endpoints) {
    try {
        Write-Host "📊 Test: $($endpoint.Name)" -NoNewline
        
        $response = Invoke-RestMethod -Uri $endpoint.Url -Method GET -TimeoutSec 10
        $count = if ($response -is [array]) { $response.Count } else { 1 }
        
        if ($count -gt 0) {
            Write-Host " ✅ $count enregistrements" -ForegroundColor Green
            $totalRecords += $count
            $successCount++
            
            # Afficher un échantillon des données
            if ($response -is [array] -and $response.Count -gt 0) {
                $sample = $response[0]
                $properties = $sample.PSObject.Properties | Select-Object -First 3 Name
                Write-Host "   Échantillon: " -NoNewline -ForegroundColor Gray
                foreach ($prop in $properties) {
                    if ($sample.$($prop.Name)) {
                        Write-Host "$($prop.Name)=$($sample.$($prop.Name)) " -NoNewline -ForegroundColor Gray
                    }
                }
                Write-Host ""
            }
        } else {
            Write-Host " ⚠️  Aucune donnée" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host " ❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📈 Résumé de la Vérification:" -ForegroundColor Cyan
Write-Host "   • Endpoints testés: $($endpoints.Count)"
Write-Host "   • Endpoints avec données: $successCount"
Write-Host "   • Total des enregistrements: $totalRecords"

if ($successCount -eq $endpoints.Count) {
    Write-Host "🎉 SUCCÈS: Toutes les tables contiennent des données !" -ForegroundColor Green
} elseif ($successCount -gt 0) {
    Write-Host "⚠️  PARTIEL: $successCount/$($endpoints.Count) tables avec données" -ForegroundColor Yellow
} else {
    Write-Host "❌ ÉCHEC: Aucune table accessible ou backend non disponible" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔗 Backend Status: " -NoNewline
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "OPÉRATIONNEL ✅" -ForegroundColor Green
} catch {
    try {
        # Fallback si actuator n'est pas disponible
        Invoke-RestMethod -Uri "$baseUrl/equipment" -Method GET -TimeoutSec 5 | Out-Null
        Write-Host "OPÉRATIONNEL ✅" -ForegroundColor Green
    } catch {
        Write-Host "INACCESSIBLE ❌" -ForegroundColor Red
    }
}