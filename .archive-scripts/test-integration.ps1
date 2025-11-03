# Configuration UTF-8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🧪 Test d'intégration MAGSAV-3.0" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Test 1: Connectivité backend
Write-Host "`n1️⃣ Test connectivité backend..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($response.status -eq "UP") {
        Write-Host "✅ Backend accessible et opérationnel" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Backend répond mais statut: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Backend non accessible: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Endpoints API
Write-Host "`n2️⃣ Test endpoints API..." -ForegroundColor Yellow
$endpoints = @(
    "/api/personnel",
    "/api/equipment", 
    "/api/service-requests"
)

foreach ($endpoint in $endpoints) {
    try {
        $url = "http://localhost:8080$endpoint"
        $data = Invoke-RestMethod -Uri $url -TimeoutSec 5
        $count = if ($data -is [array]) { $data.Count } else { 1 }
        Write-Host "✅ $endpoint → $count éléments" -ForegroundColor Green
    } catch {
        Write-Host "❌ $endpoint → Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 3: Compilation JavaFX
Write-Host "`n3️⃣ Test compilation JavaFX..." -ForegroundColor Yellow
try {
    $buildResult = & .\gradlew.bat :desktop-javafx:compileJava --quiet
    Write-Host "✅ Compilation JavaFX réussie" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur compilation: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎯 Tests terminés!" -ForegroundColor Cyan