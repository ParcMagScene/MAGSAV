# Script d'analyse des espacements entre modules MAGSAV
# Compare les paddings avec le module de référence Ventes & Installations

Write-Host "🔍 Analyse comparative des espacements - Référence: Ventes & Installations" -ForegroundColor Cyan

# Module de référence
$referenceModule = "ProjectManagerView.java"
$referencePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\salesinstallation\$referenceModule"

Write-Host "`n📋 CONFIGURATION DE RÉFÉRENCE (Ventes & Installations):" -ForegroundColor Yellow

if (Test-Path $referencePath) {
    $refContent = Get-Content $referencePath -Raw
    
    # Extraction des patterns de référence
    if ($refContent -match "VBox topContainer = new VBox\(([^)]+)\)") {
        Write-Host "  • topContainer: VBox($($Matches[1]))" -ForegroundColor Green
    }
    
    if ($refContent -match "toolbar\.setPadding\(new Insets\(([^)]+)\)\)") {
        Write-Host "  • toolbar padding: Insets($($Matches[1]))" -ForegroundColor Green
    }
    
    if ($refContent -match "filterBar\.setPadding\(new Insets\(([^)]+)\)\)") {
        Write-Host "  • filterBar padding: Insets($($Matches[1]))" -ForegroundColor Green
    }
    
    if ($refContent -match "header\.setPadding\(new Insets\(([^)]+)\)\)") {
        Write-Host "  • header padding: Insets($($Matches[1]))" -ForegroundColor Green
    }
}

Write-Host "`n🔍 ANALYSE DES AUTRES MODULES:" -ForegroundColor Yellow

$modules = @(
    "EquipmentManagerView.java",
    "PersonnelManagerView.java", 
    "VehicleManagerView.java",
    "ClientManagerView.java",
    "ContractManagerView.java"
)

foreach ($module in $modules) {
    $modulePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\$module"
    
    Write-Host "`n📄 $module" -ForegroundColor White
    
    if (Test-Path $modulePath) {
        $content = Get-Content $modulePath -Raw
        
        # Vérifier l'architecture
        if ($content -match "extends BorderPane") {
            Write-Host "  ✅ Architecture: BorderPane" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Architecture: NON-BorderPane" -ForegroundColor Red
        }
        
        # Vérifier topContainer
        if ($content -match "VBox topContainer = new VBox\(([^)]+)\)") {
            $topContainerConfig = $Matches[1]
            if ($topContainerConfig -notmatch '\d+' -or $topContainerConfig -eq 'header' -or $topContainerConfig -match 'header,') {
                Write-Host "  ✅ topContainer: VBox($topContainerConfig) - SANS espacement" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  topContainer: VBox($topContainerConfig) - AVEC espacement" -ForegroundColor Red
            }
        }
        
        # Vérifier les paddings
        $paddings = [regex]::Matches($content, "\.setPadding\(new Insets\(([^)]+)\)\)")
        foreach ($padding in $paddings) {
            $paddingValue = $padding.Groups[1].Value
            Write-Host "  • padding trouvé: Insets($paddingValue)" -ForegroundColor Gray
        }
        
        # Vérifier les VBox avec espacement
        $vboxSpacings = [regex]::Matches($content, "new VBox\((\d+)\)")
        foreach ($spacing in $vboxSpacings) {
            $spacingValue = $spacing.Groups[1].Value
            if ([int]$spacingValue -gt 0) {
                Write-Host "  ⚠️  VBox avec espacement: VBox($spacingValue)" -ForegroundColor Yellow
            }
        }
        
    } else {
        Write-Host "  ❌ Fichier introuvable" -ForegroundColor Red
    }
}

Write-Host "`n💡 RECOMMANDATIONS:" -ForegroundColor Cyan
Write-Host "• Tous les topContainer doivent être: VBox(header, toolbar) SANS paramètre spacing" -ForegroundColor White
Write-Host "• Toolbar padding: new Insets(10)" -ForegroundColor White
Write-Host "• FilterBar padding: new Insets(0, 10, 10, 10)" -ForegroundColor White
Write-Host "• Header padding: new Insets(0, 0, 20, 0)" -ForegroundColor White