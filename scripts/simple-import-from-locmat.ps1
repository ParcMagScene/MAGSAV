# Script d'import simplifié depuis LOCMAT
$ErrorActionPreference = "Stop"
$API_URL = "http://localhost:8080/api"
$EQUIPMENT_CSV = ".\Exports LOCMAT\Inventaire_Complet_Avec_NS_Clean.csv"
$VEHICLES_CSV = ".\Exports LOCMAT\VÉHICULES.csv"

Write-Host "`n=== IMPORT LOCMAT - MAGSAV 3.0 ===`n" -ForegroundColor Cyan

# Vérification backend
try {
    Invoke-RestMethod -Uri "$API_URL/equipment/stats" -Method Get -ErrorAction Stop | Out-Null
    Write-Host "[OK] Backend opérationnel" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] Backend non accessible" -ForegroundColor Red
    exit 1
}

# ================== IMPORT ÉQUIPEMENTS ==================
if (Test-Path $EQUIPMENT_CSV) {
    Write-Host "`n--- IMPORT DES ÉQUIPEMENTS ---`n" -ForegroundColor Yellow
    
    $equipmentData = Import-Csv -Path $EQUIPMENT_CSV -Encoding UTF8 -Delimiter ','
    Write-Host "Fichier chargé: $($equipmentData.Count) lignes"
    
    $imported = 0
    $errors = 0
    $skipped = 0
    
    foreach ($row in $equipmentData) {
        try {
            $ref = if ($row.'Réf') { $row.'Réf'.ToString().Trim() } else { "" }
            $nom = if ($row.Nom) { $row.Nom.ToString().Trim() } else { "" }
            
            if (-not $ref -or -not $nom) { 
                $skipped++
                continue 
            }
            
            # Conversion quantites
            $qteStock = if ($row.'Qté en stock' -and $row.'Qté en stock' -match '\d+') { [int]$row.'Qté en stock' } else { 0 }
            $qteSortie = if ($row.'Qté sortie' -and $row.'Qté sortie' -match '\d+') { [int]$row.'Qté sortie' } else { 0 }
            $qteReparation = if ($row.'Qté en réparation' -and $row.'Qté en réparation' -match '\d+') { [int]$row.'Qté en réparation' } else { 0 }
            $qteRebus = if ($row.'Qté  au Rebus' -and $row.'Qté  au Rebus' -match '\d+') { [int]$row.'Qté  au Rebus' } else { 0 }
            
            # Détermination statut
            $status = "AVAILABLE"
            if ($qteRebus -gt 0) { $status = "RETIRED" }
            elseif ($qteReparation -gt 0) { $status = "MAINTENANCE" }
            elseif ($qteSortie -gt 0) { $status = "IN_USE" }
            
            $equipment = @{
                name = $nom
                internalReference = $ref
                category = if ($row.Famille) { $row.Famille.ToString().Trim() } else { $null }
                subCategory = if ($row.'Sous-famille') { $row.'Sous-famille'.ToString().Trim() } else { $null }
                specificCategory = if ($row.'Catégorie') { $row.'Catégorie'.ToString().Trim() } else { $null }
                status = $status
                location = if ($row.ZONE) { $row.ZONE.ToString().Trim() } else { $null }
                serialNumber = if ($row.'Numéro de série') { $row.'Numéro de série'.ToString().Trim() } else { $null }
                quantityInStock = $qteStock
                quantityOut = $qteSortie
                quantityInRepair = $qteReparation
                quantityInScrap = $qteRebus
            }
            
            $json = $equipment | ConvertTo-Json -Depth 10 -Compress
            Invoke-RestMethod -Uri "$API_URL/equipment" -Method Post `
                -Body $json -ContentType "application/json; charset=utf-8" | Out-Null
            
            $imported++
            if ($imported % 100 -eq 0) {
                Write-Host "  Importé: $imported équipements..." -ForegroundColor Gray
            }
            
        } catch {
            $errors++
            if ($errors -le 3) {
                Write-Host "  Erreur: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
    
    Write-Host "`n[OK] Équipements importés: $imported" -ForegroundColor Green
    if ($skipped -gt 0) { Write-Host "[!] Ignorés (sans ref/nom): $skipped" -ForegroundColor Gray }
    if ($errors -gt 0) { Write-Host "[!] Erreurs: $errors" -ForegroundColor Yellow }
}

# ================== IMPORT VÉHICULES ==================
if (Test-Path $VEHICLES_CSV) {
    Write-Host "`n--- IMPORT DES VÉHICULES ---`n" -ForegroundColor Yellow
    
    $vehicleData = Import-Csv -Path $VEHICLES_CSV -Encoding UTF8 -Delimiter ','
    Write-Host "Fichier chargé: $($vehicleData.Count) lignes"
    
    $imported = 0
    $errors = 0
    
    foreach ($row in $vehicleData) {
        try {
            $nom = if ($row.Nom) { $row.Nom.Trim() } else { "" }
            if (-not $nom) { continue }
            
            $typeStr = if ($row.Type) { $row.Type.Trim() } else { "Autre" }
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
            
            $vehicle = @{
                name = $nom
                licensePlate = if ($row.Immatriculation) { $row.Immatriculation.ToString().Trim() } else { $null }
                brand = if ($row.Marque) { $row.Marque.ToString().Trim() } else { $null }
                model = if ($row.'Modèle') { $row.'Modèle'.ToString().Trim() } else { $null }
                color = if ($row.Couleur) { $row.Couleur.ToString().Trim() } else { $null }
                owner = if ($row.'Propriétaire') { $row.'Propriétaire'.ToString().Trim() } else { $null }
                type = $vehicleType
                status = "AVAILABLE"
                notes = if ($row.Commentaire) { $row.Commentaire.ToString().Trim() } else { $null }
            }
            
            $json = $vehicle | ConvertTo-Json -Depth 10 -Compress
            Invoke-RestMethod -Uri "$API_URL/vehicles" -Method Post `
                -Body $json -ContentType "application/json; charset=utf-8" | Out-Null
            
            $imported++
            
        } catch {
            $errors++
            if ($errors -le 3) {
                Write-Host "  Erreur vehicule: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
    
    Write-Host "`n[OK] Véhicules importés: $imported" -ForegroundColor Green
    if ($errors -gt 0) { Write-Host "[!] Erreurs: $errors" -ForegroundColor Yellow }
}

Write-Host "`n=== IMPORT TERMINÉ ===`n" -ForegroundColor Cyan
