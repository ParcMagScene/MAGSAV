# Script simple d'import CSV LOCMAT
$csvPath = Resolve-Path ".\Exports LOCMAT\Inventaire_Complet_Avec_NS.csv"
$url = "http://localhost:8080/api/equipment/import-locmat"

Write-Host "Import de $csvPath..." -ForegroundColor Cyan

try {
    $response = curl.exe -X POST -F "file=@$csvPath" $url | ConvertFrom-Json
    
    Write-Host "✅ Import terminé!" -ForegroundColor Green
    Write-Host "   Importés: $($response.imported)" -ForegroundColor Cyan
    Write-Host "   Ignorés: $($response.skipped)" -ForegroundColor Yellow
    Write-Host "   Total lignes: $($response.totalLines)" -ForegroundColor Gray
    
    if ($response.errors -and $response.errors.Count -gt 0) {
        Write-Host "⚠️  Erreurs ($($response.errors.Count)):" -ForegroundColor Yellow
        $response.errors | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
    }
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}
