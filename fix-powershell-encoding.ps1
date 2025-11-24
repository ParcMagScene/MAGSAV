# MAGSAV 3.0 - Correction Automatique Encodage PowerShell
Write-Host "🔧 Correction automatique de l'encodage des scripts PowerShell" -ForegroundColor Cyan
Write-Host ""

$scriptsPath = Get-ChildItem -Path . -Filter "*.ps1" -Recurse -File | Where-Object {
    $_.FullName -notmatch "\\node_modules\\" -and 
    $_.FullName -notmatch "\\build\\" -and
    $_.FullName -notmatch "\\.gradle\\"
}

$fixedCount = 0
$utf8Bom = New-Object System.Text.UTF8Encoding $true

foreach ($script in $scriptsPath) {
    $bytes = [System.IO.File]::ReadAllBytes($script.FullName)
    
    # Vérifier si BOM UTF-8 manquant
    if (-not ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)) {
        Write-Host "🔧 Correction : $($script.Name)" -ForegroundColor Yellow
        
        try {
            # Lire le contenu
            $content = Get-Content -Path $script.FullName -Raw
            
            # Réécrire avec BOM UTF-8
            [System.IO.File]::WriteAllText($script.FullName, $content, $utf8Bom)
            
            Write-Host "   ✅ Corrigé" -ForegroundColor Green
            $fixedCount++
        }
        catch {
            Write-Host "   ❌ Erreur : $_" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📊 Résultats" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Total de scripts analysés : $($scriptsPath.Count)" -ForegroundColor White
Write-Host "✅ Scripts corrigés : $fixedCount" -ForegroundColor Green
Write-Host ""

if ($fixedCount -gt 0) {
    Write-Host "✅ Correction terminée avec succès !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Vérifiez les changements et commitez :" -ForegroundColor Yellow
    Write-Host "   git status" -ForegroundColor White
    Write-Host "   git add *.ps1" -ForegroundColor White
    Write-Host "   git commit -m 'fix: Correction encodage UTF-8 BOM scripts PowerShell'" -ForegroundColor White
}
else {
    Write-Host "✅ Aucune correction nécessaire" -ForegroundColor Green
}
