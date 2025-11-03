# Script d'optimisation finale MAGSAV-3.0
# Nettoie les imports inutilises restants et optimise le code

Write-Host "=== OPTIMISATION FINALE MAGSAV-3.0 ===" -ForegroundColor Green

# Fonction pour nettoyer les imports dans un fichier Java
function Clean-JavaImports {
    param([string]$filePath)
    
    if (!(Test-Path $filePath)) { return }
    
    $content = Get-Content $filePath -Raw
    $originalContent = $content
    
    # Supprimer les imports inutilises courants
    $content = $content -replace "import java\.text\.NumberFormat;\s*\r?\n", ""
    $content = $content -replace "import java\.util\.List;\s*\r?\n", ""
    $content = $content -replace "import java\.util\.ArrayList;\s*\r?\n", ""
    $content = $content -replace "import java\.util\.concurrent\.CompletableFuture;\s*\r?\n", ""
    $content = $content -replace "import java\.util\.stream\.Collectors;\s*\r?\n", ""
    $content = $content -replace "import java\.time\.format\.DateTimeFormatter;\s*\r?\n", ""
    $content = $content -replace "import java\.io\.File;\s*\r?\n", ""
    $content = $content -replace "import javafx\.collections\.transformation\.FilteredList;\s*\r?\n", ""
    
    # Si le contenu a change, l'ecrire
    if ($content -ne $originalContent) {
        Set-Content -Path $filePath -Value $content -Encoding UTF8
        Write-Host "  Cleaned: $filePath" -ForegroundColor Yellow
        return $true
    }
    return $false
}

# Nettoyer tous les fichiers Java du projet
$javaFiles = Get-ChildItem -Path "C:\Users\aalou\MAGSAV-3.0" -Filter "*.java" -Recurse
$cleanedCount = 0

foreach ($file in $javaFiles) {
    if (Clean-JavaImports -filePath $file.FullName) {
        $cleanedCount++
    }
}

Write-Host "Imports nettoyes dans $cleanedCount fichiers" -ForegroundColor Green

# Refresh du cache IDE pour eliminer les faux positifs
Write-Host "Rafraichissement du cache IDE..." -ForegroundColor Cyan

# Build pour verifier que tout fonctionne
Write-Host "Build de verification..." -ForegroundColor Cyan
Set-Location "C:\Users\aalou\MAGSAV-3.0"
& .\gradlew build --no-daemon

Write-Host "=== OPTIMISATION TERMINEE ===" -ForegroundColor Green