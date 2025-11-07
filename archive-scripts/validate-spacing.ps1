# Validation des espacements standardisés dans MAGSAV-3.0
# Vérifie que SpacingManager est correctement appliqué

Write-Host "🔍 Validation des espacements standardisés MAGSAV" -ForegroundColor Cyan

$totalFiles = 0
$validatedFiles = 0
$errors = @()

$viewFiles = Get-ChildItem -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view" -Recurse -Filter "*.java"

foreach ($file in $viewFiles) {
    $totalFiles++
    Write-Host "📄 Analyse : $($file.Name)" -ForegroundColor Yellow
    
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    
    # Vérifications
    $hasSpacingImport = $content -match "import.*SpacingManager"
    $usesSpacingManager = $content -match "SpacingManager\."
    $hasOldInsets = $content -match "new Insets\(\d+\)"
    $hasOldSpacing = $content -match "setSpacing\(\d+\)"
    
    # Status pour ce fichier
    $status = "✅ CONFORME"
    $details = @()
    
    if ($hasOldInsets) {
        $status = "⚠️  À CORRIGER"
        $details += "- Utilise encore 'new Insets(n)' directement"
    }
    
    if ($hasOldSpacing) {
        $status = "⚠️  À CORRIGER" 
        $details += "- Utilise encore 'setSpacing(n)' avec valeurs brutes"
    }
    
    if ($usesSpacingManager) {
        $validatedFiles++
        $details += "- SpacingManager correctement utilisé"
    } elseif ($hasSpacingImport) {
        $details += "- Import SpacingManager présent mais non utilisé"
    } else {
        $details += "- SpacingManager non importé"
    }
    
    Write-Host "  $status" -ForegroundColor $(if ($status -like "*CONFORME*") { "Green" } else { "Red" })
    
    foreach ($detail in $details) {
        Write-Host "    $detail" -ForegroundColor Gray
    }
    
    if ($status -like "*CORRIGER*") {
        $errors += $file.Name
    }
}

Write-Host "`n📊 RÉSUMÉ DE LA VALIDATION" -ForegroundColor Cyan
Write-Host "═══════════════════════════" -ForegroundColor Cyan
Write-Host "Fichiers analysés : $totalFiles" -ForegroundColor White
Write-Host "Fichiers validés : $validatedFiles" -ForegroundColor Green
Write-Host "Fichiers à corriger : $($errors.Count)" -ForegroundColor $(if ($errors.Count -eq 0) { "Green" } else { "Red" })

if ($errors.Count -gt 0) {
    Write-Host "`n⚠️  Fichiers nécessitant des corrections :" -ForegroundColor Red
    foreach ($errorFile in $errors) {
        Write-Host "   - $errorFile" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n🎉 Tous les fichiers utilisent SpacingManager correctement !" -ForegroundColor Green
}

Write-Host "`n🎯 BÉNÉFICES DE LA STANDARDISATION :" -ForegroundColor Cyan
Write-Host "• Interface plus compacte et professionnelle" -ForegroundColor White
Write-Host "• Espacement réduit entre toolbars et contenu" -ForegroundColor White  
Write-Host "• Cohérence visuelle sur tous les modules" -ForegroundColor White
Write-Host "• Maintenance centralisée des espacements" -ForegroundColor White