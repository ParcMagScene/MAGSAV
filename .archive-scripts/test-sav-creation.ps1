# Test création demande SAV
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🎯 Test création demande SAV" -ForegroundColor Green
Write-Host "=============================" -ForegroundColor Green

# Données de test pour nouvelle demande SAV
$nouveauSAV = @{
    title = "Test Demande SAV - Réparation Console"
    description = "Console audio défectueuse - canal droit inaudible"
    type = "REPAIR"
    priority = "MEDIUM" 
    requesterName = "Jean Dupont"
    requesterEmail = "jean.dupont@magscene.com"
    equipmentId = 1
    estimatedCost = 250.00
} | ConvertTo-Json -Depth 3

try {
    Write-Host "📝 Création demande SAV..." -ForegroundColor Yellow
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/service-requests" -Method POST -Body $nouveauSAV -ContentType "application/json"
    
    Write-Host "✅ Demande SAV créée avec succès!" -ForegroundColor Green
    Write-Host "   ID: $($response.id)" -ForegroundColor Cyan
    Write-Host "   Titre: $($response.title)" -ForegroundColor Cyan
    Write-Host "   Statut: $($response.status)" -ForegroundColor Cyan
    Write-Host "   Assigné à: $($response.assignedTechnician)" -ForegroundColor Cyan
    
    # Test récupération
    Write-Host "`n🔍 Vérification récupération..." -ForegroundColor Yellow
    $retrieved = Invoke-RestMethod -Uri "http://localhost:8080/api/service-requests/$($response.id)"
    
    if ($retrieved.title -eq $nouveauSAV.title) {
        Write-Host "✅ Récupération OK - Données cohérentes" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Problème cohérence données" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Erreur création SAV: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Détails: $($_.Exception.Response)" -ForegroundColor Red
}

Write-Host "`n🏁 Test terminé!" -ForegroundColor Cyan