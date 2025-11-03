# Test de connectivité Backend/Desktop MAGSAV-3.0
Write-Host "🧪 Test de connectivité avec le backend..." -ForegroundColor Cyan

# Test 1: Equipment endpoint
Write-Host "📦 Test endpoint /api/equipment..." -ForegroundColor Yellow
try {
    $equipment = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -Method GET
    Write-Host "✅ Equipment: $($equipment.Count) éléments récupérés" -ForegroundColor Green
    if ($equipment.Count -gt 0) {
        Write-Host "   Exemple: $($equipment[0].name) - $($equipment[0].brand)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erreur equipment: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Clients endpoint
Write-Host "`n👥 Test endpoint /api/clients..." -ForegroundColor Yellow
try {
    $clients = Invoke-RestMethod -Uri "http://localhost:8080/api/clients" -Method GET
    Write-Host "✅ Clients: $($clients.Count) éléments récupérés" -ForegroundColor Green
    if ($clients.Count -gt 0) {
        Write-Host "   Exemple: $($clients[0].name)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erreur clients: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: SAV endpoint
Write-Host "`n🔧 Test endpoint /api/sav..." -ForegroundColor Yellow
try {
    $sav = Invoke-RestMethod -Uri "http://localhost:8080/api/sav" -Method GET
    Write-Host "✅ SAV: $($sav.Count) éléments récupérés" -ForegroundColor Green
    if ($sav.Count -gt 0) {
        Write-Host "   Exemple: $($sav[0].reference) - $($sav[0].status)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erreur SAV: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Vehicles endpoint
Write-Host "`n🚗 Test endpoint /api/vehicles..." -ForegroundColor Yellow
try {
    $vehicles = Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles" -Method GET
    Write-Host "✅ Vehicles: $($vehicles.Count) éléments récupérés" -ForegroundColor Green
    if ($vehicles.Count -gt 0) {
        Write-Host "   Exemple: $($vehicles[0].make) $($vehicles[0].model)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erreur vehicles: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Personnel endpoint
Write-Host "`n👤 Test endpoint /api/personnel..." -ForegroundColor Yellow
try {
    $personnel = Invoke-RestMethod -Uri "http://localhost:8080/api/personnel" -Method GET
    Write-Host "✅ Personnel: $($personnel.Count) éléments récupérés" -ForegroundColor Green
    if ($personnel.Count -gt 0) {
        Write-Host "   Exemple: $($personnel[0].firstName) $($personnel[0].lastName)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Erreur personnel: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 Test de connectivité terminé !" -ForegroundColor Cyan