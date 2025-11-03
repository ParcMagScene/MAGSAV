# Test de connectivite Backend/Desktop MAGSAV-3.0
Write-Host "Test de connectivite avec le backend..." -ForegroundColor Cyan

# Test 1: Equipment endpoint
Write-Host "Test endpoint /api/equipment..." -ForegroundColor Yellow
try {
    $equipment = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -Method GET
    Write-Host "OK Equipment: $($equipment.Count) elements recuperes" -ForegroundColor Green
    if ($equipment.Count -gt 0) {
        Write-Host "   Exemple: $($equipment[0].name) - $($equipment[0].brand)" -ForegroundColor White
    }
} catch {
    Write-Host "ERREUR equipment: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Clients endpoint
Write-Host "`nTest endpoint /api/clients..." -ForegroundColor Yellow
try {
    $clients = Invoke-RestMethod -Uri "http://localhost:8080/api/clients" -Method GET
    Write-Host "OK Clients: $($clients.Count) elements recuperes" -ForegroundColor Green
    if ($clients.Count -gt 0) {
        Write-Host "   Exemple: $($clients[0].name)" -ForegroundColor White
    }
} catch {
    Write-Host "ERREUR clients: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: SAV endpoint
Write-Host "`nTest endpoint /api/sav..." -ForegroundColor Yellow
try {
    $sav = Invoke-RestMethod -Uri "http://localhost:8080/api/sav" -Method GET
    Write-Host "OK SAV: $($sav.Count) elements recuperes" -ForegroundColor Green
    if ($sav.Count -gt 0) {
        Write-Host "   Exemple: $($sav[0].reference) - $($sav[0].status)" -ForegroundColor White
    }
} catch {
    Write-Host "ERREUR SAV: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Vehicles endpoint
Write-Host "`nTest endpoint /api/vehicles..." -ForegroundColor Yellow
try {
    $vehicles = Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles" -Method GET
    Write-Host "OK Vehicles: $($vehicles.Count) elements recuperes" -ForegroundColor Green
    if ($vehicles.Count -gt 0) {
        Write-Host "   Exemple: $($vehicles[0].make) $($vehicles[0].model)" -ForegroundColor White
    }
} catch {
    Write-Host "ERREUR vehicles: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTest de connectivite termine !" -ForegroundColor Cyan