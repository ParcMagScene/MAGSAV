# Script minimal pour supprimer seulement le BOM UTF-8 sans réecrire le contenu

Write-Host "=== SUPPRESSION BOM UTF-8 MINIMALE ===" -ForegroundColor Yellow

# Liste des fichiers corrompus identifiés par l'erreur
$bomFiles = @(
    "backend\src\main\java\com\magscene\magsav\backend\controller\CategoryRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ClientController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ContactController.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Category.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Client.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Contract.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Equipment.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\CategoryRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ClientRepository.java"
)

$fixedCount = 0
foreach ($relativePath in $bomFiles) {
    $fullPath = "C:\Users\aalou\MAGSAV-3.0\$relativePath"
    
    if (Test-Path $fullPath) {
        try {
            # Lire le contenu en binaire
            $bytes = [System.IO.File]::ReadAllBytes($fullPath)
            
            # Supprimer seulement le BOM s'il existe (EF BB BF)
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                $cleanBytes = $bytes[3..($bytes.Length - 1)]
                [System.IO.File]::WriteAllBytes($fullPath, $cleanBytes)
                Write-Host "  Removed BOM: $($relativePath -replace '.*\\', '')" -ForegroundColor Green
                $fixedCount++
            }
        }
        catch {
            Write-Host "  Error: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "BOM supprime dans $fixedCount fichiers" -ForegroundColor Green

# Test rapide de compilation
Write-Host "Test compilation..." -ForegroundColor Cyan
& .\gradlew :backend:compileJava --no-daemon -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCES - Build backend OK!" -ForegroundColor Green
} else {
    Write-Host "Erreurs compilation restantes..." -ForegroundColor Yellow
}

Write-Host "=== SUPPRESSION BOM TERMINEE ===" -ForegroundColor Yellow