# Script de standardisation des espacements pour tous les modules MAGSAV
# Réduit l'espace entre toolbars et contenu en appliquant SpacingManager

Write-Host "🔧 Standardisation des espacements MAGSAV - Réduction toolbar-to-content gaps" -ForegroundColor Cyan

$views = @(
    "VehicleManagerView.java",
    "ClientManagerView.java", 
    "ProjectManagerView.java",
    "RepairTrackingView.java",
    "RMAView.java",
    "TechnicianPlanningView.java",
    "ContractManagerView.java"
)

foreach ($view in $views) {
    $filePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\$view"
    
    if (Test-Path $filePath) {
        Write-Host "📝 Traitement : $view" -ForegroundColor Yellow
        
        $content = Get-Content $filePath -Raw -Encoding UTF8
        
        # Ajouter l'import SpacingManager si nécessaire
        if ($content -notmatch "import.*SpacingManager") {
            $content = $content -replace "(import com\.magscene\.magsav\.desktop\.theme\.ThemeManager;)", "`$1`nimport com.magscene.magsav.desktop.theme.SpacingManager;"
        }
        
        # Replacements standardisés - ordre d'application important
        $replacements = @(
            # Vue principale
            @{ Pattern = "setSpacing\(1[05]\)"; Replacement = "setSpacing(SpacingManager.SPACING_MINIMAL)" },
            @{ Pattern = "setPadding\(new Insets\([52]0?\)\)"; Replacement = "setPadding(SpacingManager.MAIN_VIEW_PADDING)" },
            
            # Headers - Réduction de l'espacement  
            @{ Pattern = "setPadding\(new Insets\(0, 0, [12]0, 0\)\)"; Replacement = "setPadding(SpacingManager.HEADER_PADDING)" },
            @{ Pattern = "new VBox\(1[5-9]|20\)"; Replacement = "new VBox(SpacingManager.SPACING_COMPACT)" },
            
            # Toolbars - Standardisation compacte
            @{ Pattern = "setPadding\(new Insets\(1[05]\)\)"; Replacement = "setPadding(SpacingManager.TOOLBAR_PADDING)" },
            @{ Pattern = "new HBox\(1[5-9]|20\)"; Replacement = "new HBox(SpacingManager.SPACING_NORMAL)" }
        )
        
        # Appliquer tous les replacements dans l'ordre
        foreach ($replacement in $replacements) {
            $content = $content -replace $replacement.Pattern, $replacement.Replacement
        }
        
        # Sauvegarder avec encodage correct
        $content | Set-Content $filePath -Encoding UTF8 -NoNewline
        
        Write-Host "✅ $view - Espacements standardisés" -ForegroundColor Green
    } else {
        Write-Host "⚠️  $view - Fichier introuvable" -ForegroundColor Red
    }
}

Write-Host "`n🎯 Standardisation terminée - Espacements compacts appliqués" -ForegroundColor Cyan
Write-Host "Benefices :" -ForegroundColor White
Write-Host "  - Toolbar padding: 5px (au lieu de 10-15px)" -ForegroundColor Gray
Write-Host "  - Header padding: bottom 10px (au lieu de 15-20px)" -ForegroundColor Gray  
Write-Host "  - Vue principale: 2px spacing (au lieu de 10-15px)" -ForegroundColor Gray
Write-Host "  - Interface plus compacte et professionnelle" -ForegroundColor Gray