# Script pour déplacer toutes les toolbars dans les headers des ManagerView
Write-Host "🔧 Déplacement des barres de recherche dans les headers..." -ForegroundColor Green

# Liste des fichiers ManagerView à traiter
$managerFiles = @(
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ContractManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ServiceRequestManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\salesinstallation\ProjectManagerView.java"
)

foreach ($file in $managerFiles) {
    $fullPath = Join-Path $PSScriptRoot $file
    if (Test-Path $fullPath) {
        Write-Host "📝 Traitement: $file" -ForegroundColor Cyan
        
        # Lire le contenu du fichier
        $content = Get-Content $fullPath -Raw
        
        # Pattern 1: Déplacer la toolbar du layout vers le header
        $pattern1 = '(\s+)// Header\s+VBox header = createHeader\(\);\s+(.+?\s+)// Toolbar[^\n]*\n\s+HBox toolbar = createToolbar\(\);\s+(.+?\s+)getChildren\(\)\.addAll\(header, toolbar,'
        $replacement1 = '$1// Header avec toolbar intégrée$1VBox header = createHeader();$1$2$3getChildren().addAll(header,'
        
        if ($content -match $pattern1) {
            $content = $content -replace $pattern1, $replacement1
            Write-Host "  ✅ Layout modifié" -ForegroundColor Green
        }
        
        # Pattern 2: Modifier la méthode createHeader pour inclure la toolbar
        $pattern2 = '(\s+private VBox createHeader\(\) \{\s+VBox header = new VBox\()10(\);\s+header\.setPadding\(new Insets\(0, 0, 20, 0\)\);\s+Label title = new Label\([^;]+\);\s+[^}]+header\.getChildren\(\)\.add\(title\);\s+return header;\s+\})'
        $replacement2 = '$115$2' + "`n`n        // Toolbar avec recherche et filtres intégrée dans le header`n        HBox toolbar = createToolbar();`n`n        header.getChildren().addAll(title, toolbar);`n        return header;`n    }"
        
        if ($content -match $pattern2) {
            $content = $content -replace $pattern2, $replacement2
            Write-Host "  ✅ Header modifié" -ForegroundColor Green
        }
        
        # Écrire le contenu modifié
        Set-Content $fullPath $content -Encoding UTF8
        Write-Host "  💾 Fichier sauvegardé" -ForegroundColor Yellow
    } else {
        Write-Host "⚠️  Fichier non trouvé: $file" -ForegroundColor Red
    }
}

Write-Host "`n🎉 Modifications terminées!" -ForegroundColor Green