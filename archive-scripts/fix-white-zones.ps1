# Script PowerShell pour corriger automatiquement toutes les zones blanches hardcodées 
# dans les modules MAGSAV-3.0

$moduleFiles = @(
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java", 
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ContractManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\salesinstallation\ProjectManagerView.java"
)

foreach ($file in $moduleFiles) {
    $fullPath = "c:\Users\aalou\MAGSAV-3.0\$file"
    
    if (Test-Path $fullPath) {
        Write-Host "📝 Correction du fichier: $file" -ForegroundColor Cyan
        
        # Lecture du contenu
        $content = Get-Content -Path $fullPath -Raw
        
        # Ajout de l'import ThemeManager si nécessaire
        if ($content -notmatch "import com\.magscene\.magsav\.desktop\.theme\.ThemeManager;") {
            $content = $content -replace "(import com\.magscene\.magsav\.desktop\.service\.ApiService;)", "`$1`nimport com.magscene.magsav.desktop.theme.ThemeManager;"
            Write-Host "  ✅ Import ThemeManager ajouté" -ForegroundColor Green
        }
        
        # Remplacement des couleurs hardcodées
        $replacements = @{
            "setStyle\(`"-fx-background-color: #f8f9fa;`"\)" = "setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentBackgroundColor() + `";`")"
            "this\.setStyle\(`"-fx-background-color: #f8f9fa;`"\)" = "this.setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentBackgroundColor() + `";`")"
            "setStyle\(`"-fx-background-color: white;" = "setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentUIColor() + `";"
            "setStyle\(`"-fx-background-color: #f8f9fa;" = "setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentBackgroundColor() + `";"
            "setStyle\(`"-fx-background-color: #e9ecef;" = "setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentSecondaryColor() + `";"
            "setStyle\(`"-fx-background-color: #ecf0f1;" = "setStyle(`"-fx-background-color: `" + ThemeManager.getInstance().getCurrentSecondaryColor() + `";"
        }
        
        $changesMade = 0
        foreach ($pattern in $replacements.Keys) {
            $replacement = $replacements[$pattern]
            if ($content -match $pattern) {
                $content = $content -replace $pattern, $replacement
                $changesMade++
                Write-Host "  ✅ Remplacement effectué: $pattern" -ForegroundColor Green
            }
        }
        
        # Sauvegarde si des modifications ont été faites
        if ($changesMade -gt 0) {
            Set-Content -Path $fullPath -Value $content -Encoding UTF8
            Write-Host "  💾 Fichier sauvegardé avec $changesMade modifications" -ForegroundColor Yellow
        } else {
            Write-Host "  ℹ️ Aucune modification nécessaire" -ForegroundColor Blue
        }
        
    } else {
        Write-Host "❌ Fichier non trouvé: $fullPath" -ForegroundColor Red
    }
}

Write-Host "`n🎯 Correction automatique terminée!" -ForegroundColor Magenta