# MAGSAV-3.0 - Verification des données backend
Write-Host "🔍 VERIFICATION DES DONNEES BACKEND MAGSAV-3.0" -ForegroundColor Green
Write-Host "============================================================"

$baseUrl = "http://localhost:8080/api"

# Test de base - Equipements
try {
    Write-Host "📦 Test Equipements..." -NoNewline
    $equipment = Invoke-RestMethod -Uri "$baseUrl/equipment" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($equipment.Count) équipements" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur équipements" -ForegroundColor Red
}

# Test Clients
try {
    Write-Host "👥 Test Clients..." -NoNewline
    $clients = Invoke-RestMethod -Uri "$baseUrl/clients" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($clients.Count) clients" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur clients" -ForegroundColor Red
}

# Test Véhicules  
try {
    Write-Host "🚐 Test Véhicules..." -NoNewline
    $vehicles = Invoke-RestMethod -Uri "$baseUrl/vehicles" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($vehicles.Count) véhicules" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur véhicules" -ForegroundColor Red
}

# Test Projets
try {
    Write-Host "💼 Test Projets..." -NoNewline
    $projects = Invoke-RestMethod -Uri "$baseUrl/projects" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($projects.Count) projets" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur projets" -ForegroundColor Red
}

# Test SAV
try {
    Write-Host "🔧 Test Demandes SAV..." -NoNewline
    $sav = Invoke-RestMethod -Uri "$baseUrl/service-requests" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($sav.Count) demandes SAV" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur SAV" -ForegroundColor Red
}

# Test Catégories
try {
    Write-Host "🏷️  Test Catégories..." -NoNewline
    $categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method GET -TimeoutSec 10
    Write-Host " ✅ $($categories.Count) catégories" -ForegroundColor Green
} catch {
    Write-Host " ❌ Erreur catégories" -ForegroundColor Red
}

Write-Host ""
Write-Host "✅ VERIFICATION TERMINEE" -ForegroundColor Green