# Script d'import complet depuis les exports LOCMAT
# Importe équipements (avec catégories) et véhicules

$ErrorActionPreference = "Stop"
$ProgressPreference = 'SilentlyContinue'

# Configuration
$API_URL = "http://localhost:8080/api"
$EQUIPMENT_CSV = ".\Exports LOCMAT\Inventaire_Complet_Avec_NS.csv"
$VEHICLES_CSV = ".\Exports LOCMAT\VÉHICULES.csv"

# Couleurs pour l'affichage
function Write-Success { param($msg) Write-Host "✓ $msg" -ForegroundColor Green }
function Write-Info { param($msg) Write-Host "ℹ $msg" -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host "⚠ $msg" -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host "✗ $msg" -ForegroundColor Red }

Write-Host "`n╔══════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║   IMPORT COMPLET DEPUIS LOCMAT - MAGSAV 3.0      ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════╝`n" -ForegroundColor Magenta

# Vérification du backend
Write-Info "Vérification du backend..."
try {
    $response = Invoke-RestMethod -Uri "$API_URL/equipment/stats" -Method Get -ErrorAction Stop
    Write-Success "Backend opérationnel"
} catch {
    Write-Error "Backend non accessible sur $API_URL"
    Write-Host "Veuillez démarrer le backend avec: .\gradlew.bat :backend:bootRun"
    exit 1
}

# ================== IMPORT DES CATÉGORIES ET ÉQUIPEMENTS ==================

if (Test-Path $EQUIPMENT_CSV) {
    Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  IMPORT DES ÉQUIPEMENTS ET CATÉGORIES" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan
    
    # Lecture du CSV avec l'encodage correct
    $equipmentData = Import-Csv -Path $EQUIPMENT_CSV -Encoding UTF8 -Delimiter ','
    
    $count = $equipmentData.Count
    Write-Info "Fichier chargé: $count lignes"
    
    # Extraction des catégories uniques (Famille, Sous-famille, Catégorie)
    $categories = @{}
    
    foreach ($row in $equipmentData) {
        $famille = $row.Famille?.Trim()
        $sousFamille = $row.'Sous-famille'?.Trim()
        $categorie = $row.'Catégorie'?.Trim()
        
        if ($famille) {
            if (-not $categories.ContainsKey($famille)) {
                $categories[$famille] = @{
                    SousFamilles = @{}
                }
            }
            
            if ($sousFamille) {
                if (-not $categories[$famille].SousFamilles.ContainsKey($sousFamille)) {
                    $categories[$famille].SousFamilles[$sousFamille] = @()
                }
                
                if ($categorie -and $categorie -notin $categories[$famille].SousFamilles[$sousFamille]) {
                    $categories[$famille].SousFamilles[$sousFamille] += $categorie
                }
            }
        }
    }
    
    Write-Info ("Catégories trouvées: " + $categories.Count + " familles")
    
    # Import des équipements
    $imported = 0
    $errors = 0
    $skipped = 0
    
    foreach ($row in $equipmentData) {
        try {
            $ref = $row.Réf?.Trim()
            $nom = $row.Nom?.Trim()
            
            if (-not $ref -or -not $nom) {
                $skipped++
                continue
            }
            
            # Conversion des quantités
            $qteStock = if ($row.'Qté en stock') { [int]$row.'Qté en stock' } else { 0 }
            $qteTransfert = if ($row.'Qté en transfert') { [int]$row.'Qté en transfert' } else { 0 }
            $qteSortie = if ($row.'Qté sortie') { [int]$row.'Qté sortie' } else { 0 }
            $qteReparation = if ($row.'Qté en réparation') { [int]$row.'Qté en réparation' } else { 0 }
            $qteManquant = if ($row.'Qté en manquant') { [int]$row.'Qté en manquant' } else { 0 }
            $qteRebus = if ($row.'Qté au rebus') { [int]$row.'Qté au rebus' } else { 0 }
            
            # Conversion des prix
            $prixAchat = if ($row."Prix d'achat") {
                try { [double]($row."Prix d'achat" -replace ',', '.') } catch { $null }
            } else { $null }
            
            $valeur = if ($row.Valeur) {
                try { [double]($row.Valeur -replace ',', '.') } catch { $null }
            } else { $null }
            
            # Détermination du statut basé sur les quantités
            $status = "AVAILABLE"
            if ($qteRebus -gt 0) { $status = "RETIRED" }
            elseif ($qteReparation -gt 0) { $status = "MAINTENANCE" }
            elseif ($qteSortie -gt 0) { $status = "IN_USE" }
            elseif ($qteStock -eq 0 -and $qteManquant -gt 0) { $status = "OUT_OF_ORDER" }
            
            # Construction de l'objet équipement
            $equipment = @{
                name = $nom
                internalReference = $ref
                category = $row.Famille?.Trim()
                subCategory = $row.'Sous-famille'?.Trim()
                specificCategory = $row.'Catégorie'?.Trim()
                status = $status
                location = $row.ZONE?.Trim()
                zone = $row.Zone?.Trim()
                serialNumber = $row.'Numéro de série'?.Trim()
                purchasePrice = $prixAchat
                insuranceValue = $valeur
                quantityInStock = $qteStock
                quantityInTransfer = $qteTransfert
                quantityOut = $qteSortie
                quantityInRepair = $qteReparation
                quantityMissing = $qteManquant
                quantityInScrap = $qteRebus
                sourceSheet = $row.Feuille?.Trim()
            }
            
            # Appel API pour créer l'équipement
            $json = $equipment | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri "$API_URL/equipment" -Method Post `
                -Body $json -ContentType "application/json; charset=utf-8"
            
            $imported++
            if ($imported % 50 -eq 0) {
                Write-Info "  Importé: $imported équipements..."
            }
            
        } catch {
            $errors++
            if ($errors -le 5) {
                Write-Warning "Erreur sur '$nom': $($_.Exception.Message)"
            }
        }
    }
    
    Write-Host ""
    Write-Success "Équipements importés: $imported"
    if ($skipped -gt 0) { Write-Warning "Lignes ignorées: $skipped" }
    if ($errors -gt 0) { Write-Warning "Erreurs rencontrées: $errors" }
    
} else {
    Write-Warning "Fichier équipements non trouvé: $EQUIPMENT_CSV"
}

# ================== IMPORT DES VÉHICULES ==================

if (Test-Path $VEHICLES_CSV) {
    Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  IMPORT DES VÉHICULES" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan
    
    $vehicleData = Import-Csv -Path $VEHICLES_CSV -Encoding UTF8 -Delimiter ','
    
    $count = $vehicleData.Count
    Write-Info "Fichier chargé: $count lignes"
    
    $imported = 0
    $errors = 0
    $skipped = 0
    
    foreach ($row in $vehicleData) {
        try {
            $nom = $row.Nom?.Trim()
            $immat = $row.Immatriculation?.Trim()
            
            if (-not $nom) {
                $skipped++
                continue
            }
            
            # Détermination du type de véhicule
            $typeStr = $row.Type?.Trim()
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
            
            # Construction de l'objet véhicule
            $vehicle = @{
                name = $nom
                licensePlate = $immat
                brand = $row.Marque?.Trim()
                model = $row.'Modèle'?.Trim()
                color = $row.Couleur?.Trim()
                owner = $row.Propriétaire?.Trim()
                type = $vehicleType
                status = "AVAILABLE"
                notes = $row.Commentaire?.Trim()
            }
            
            # Appel API pour créer le véhicule
            $json = $vehicle | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri "$API_URL/vehicles" -Method Post `
                -Body $json -ContentType "application/json; charset=utf-8"
            
            $imported++
            if ($imported % 10 -eq 0) {
                Write-Info "  Importé: $imported véhicules..."
            }
            
        } catch {
            $errors++
            if ($errors -le 5) {
                Write-Warning "Erreur sur '$nom': $($_.Exception.Message)"
            }
        }
    }
    
    Write-Host ""
    Write-Success "Véhicules importés: $imported"
    if ($skipped -gt 0) { Write-Warning "Lignes ignorées: $skipped" }
    if ($errors -gt 0) { Write-Warning "Erreurs rencontrées: $errors" }
    
} else {
    Write-Warning "Fichier véhicules non trouvé: $VEHICLES_CSV"
}

# ================== RÉSUMÉ ==================

Write-Host "`n╔══════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║              IMPORT TERMINÉ                      ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════╝`n" -ForegroundColor Green

Write-Host "Pour visualiser les données:"
Write-Host "  Frontend: http://localhost:3000/equipment" -ForegroundColor Cyan
Write-Host "  Frontend: http://localhost:3000/vehicles" -ForegroundColor Cyan
