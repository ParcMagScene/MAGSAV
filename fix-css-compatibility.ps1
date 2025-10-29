# Script pour ajouter les propriétés CSS standard aux propriétés JavaFX
# Cela réduit les avertissements de compatibilité

$cssFile = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\resources\styles\magsav-theme.css"

# Lire le contenu du fichier
$content = Get-Content $cssFile -Raw

# Mapping des propriétés JavaFX vers CSS standard
$replacements = @{
    '-fx-background-color:([^;]+);' = '-fx-background-color:$1; background-color:$1;'
    '-fx-font-size:([^;]+);' = '-fx-font-size:$1; font-size:$1;'
    '-fx-font-weight:([^;]+);' = '-fx-font-weight:$1; font-weight:$1;'
    '-fx-border-color:([^;]+);' = '-fx-border-color:$1; border-color:$1;'
    '-fx-border-width:([^;]+);' = '-fx-border-width:$1; border-width:$1;'
    '-fx-border-radius:([^;]+);' = '-fx-border-radius:$1; border-radius:$1;'
    '-fx-padding:([^;]+);' = '-fx-padding:$1; padding:$1;'
    '-fx-cursor:([^;]+);' = '-fx-cursor:$1; cursor:$1;'
}

# Appliquer les remplacements
foreach ($pattern in $replacements.Keys) {
    $replacement = $replacements[$pattern]
    $content = [regex]::Replace($content, $pattern, $replacement)
}

# Sauvegarder le fichier avec encodage UTF-8 sans BOM
[System.IO.File]::WriteAllText($cssFile, $content, [System.Text.UTF8Encoding]::new($false))

Write-Host "Propriétés CSS standard ajoutées avec succès!" -ForegroundColor Green