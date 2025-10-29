# Script pour nettoyer automatiquement les imports inutilisés
# Recherche et supprime les imports Java non utilisés les plus courants

Write-Host "Nettoyage automatique des imports inutilises..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path "c:\Users\aalou\MAGSAV-3.0" -Filter "*.java" -Recurse

$cleanedFiles = 0
$removedImports = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Liste des imports couramment inutilisés à vérifier et supprimer
    $unusedImports = @(
        'import java.util.List;',
        'import java.util.concurrent.CompletableFuture;',
        'import java.time.format.DateTimeFormatter;',
        'import javafx.stage.Stage;',
        'import java.text.NumberFormat;',
        'import java.util.Locale;',
        'import java.util.Optional;'
    )
    
    # Supprimer les imports identifiés comme inutilisés
    foreach ($import in $unusedImports) {
        if ($content -match [regex]::Escape($import)) {
            # Vérifier si l'import est réellement utilisé (recherche simple)
            $className = ($import -split '\.' | Select-Object -Last 1) -replace ';', ''
            
            # Recherche basique d'utilisation (peut être améliorée)
            if ($content -notmatch "\b$className\b" -or ($content -split "`n" | Where-Object { $_ -match "\b$className\b" }).Count -le 1) {
                $content = $content -replace [regex]::Escape($import) + "`r?`n", ""
                $removedImports++
            }
        }
    }
    
    # Sauvegarder si des changements ont été effectués
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
        $cleanedFiles++
        Write-Host "  Nettoye: $($file.Name)" -ForegroundColor Green
    }
}

Write-Host "`nNettoyage termine!" -ForegroundColor Green
Write-Host "Fichiers nettoyes: $cleanedFiles" -ForegroundColor Yellow
Write-Host "Imports supprimes: $removedImports" -ForegroundColor Yellow