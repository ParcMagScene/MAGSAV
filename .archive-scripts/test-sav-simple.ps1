# Test création SAV simplifié
$nouveauSAV = @{
    title = "Test SAV Simple"
    description = "Test de création"
    type = "REPAIR" 
    priority = "MEDIUM"
    status = "OPEN"
    requesterName = "Jean Test"
    requesterEmail = "test@magscene.com"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Test création SAV sans équipement..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/service-requests" -Method POST -Body $nouveauSAV -ContentType "application/json"
    Write-Host "✅ Succès! ID: $($response.id)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Détails: $responseBody" -ForegroundColor Red
    }
}