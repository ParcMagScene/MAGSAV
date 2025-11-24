# MAGSAV 3.0 - Validation Encodage Scripts PowerShell
Write-Host "🔍 Validation de l'encodage des scripts PowerShell" -ForegroundColor Cyan
Write-Host ""

$scriptsPath = Get-ChildItem -Path . -Filter "*.ps1" -Recurse -File | Where-Object {
    $_.FullName -notmatch "\\node_modules\\" -and 
    $_.FullName -notmatch "\\build\\" -and
    $_.FullName -notmatch "\\.gradle\\"
}

$issues = @()
$validCount = 0

foreach ($script in $scriptsPath) {
    $bytes = [System.IO.File]::ReadAllBytes($script.FullName)
    
    # Vérifier BOM UTF-8 (EF BB BF)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "✅ $($script.Name)" -ForegroundColor Green
        $validCount++
    } else {
        Write-Host "❌ $($script.Name) - Pas de BOM UTF-8" -ForegroundColor Red
        $issues += [PSCustomObject]@{
            File = $script.FullName
            Issue = "Manque BOM UTF-8"
        }
    }
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📊 Résultats" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Total de scripts analysés : $($scriptsPath.Count)" -ForegroundColor White
Write-Host "✅ Scripts valides : $validCount" -ForegroundColor Green
Write-Host "❌ Scripts avec problèmes : $($issues.Count)" -ForegroundColor Red

if ($issues.Count -gt 0) {
    Write-Host ""
    Write-Host "🔧 Correction automatique disponible :" -ForegroundColor Yellow
    Write-Host "   .\fix-powershell-encoding.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "Liste des fichiers à corriger :" -ForegroundColor Yellow
    $issues | ForEach-Object { Write-Host "   - $($_.File)" -ForegroundColor Gray }
    exit 1
} else {
    Write-Host ""
    Write-Host "✅ Tous les scripts PowerShell ont le bon encodage !" -ForegroundColor Green
    exit 0
}
