#!/usr/bin/env pwsh
# Script d'unification de la structure CSS MAGSAV-3.0
# Résolution des conflits et harmonisation totale

$DesktopPath = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\resources\styles"

Write-Host "🔧 UNIFICATION CSS MAGSAV-3.0 - RÉSOLUTION DES CONFLITS" -ForegroundColor Yellow

# 1. Sauvegarde des fichiers CSS existants
$BackupPath = "$DesktopPath\_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $BackupPath -Force | Out-Null
Write-Host "📦 Sauvegarde dans: $BackupPath" -ForegroundColor Cyan

Get-ChildItem -Path $DesktopPath -Filter "*.css" | ForEach-Object {
    Copy-Item $_.FullName -Destination "$BackupPath\$($_.Name)" -Force
    Write-Host "   ✅ Sauvegardé: $($_.Name)" -ForegroundColor Green
}

# 2. Créer le CSS unifié principal avec harmonisation #142240
$UnifiedCSS = @"
/* MAGSAV-3.0 - CSS UNIFIÉ - HARMONISATION TOTALE */
/* Version unifiée avec correction barre de recherche globale */

/* ===== RESET TOTAL ET HARMONISATION ===== */

/* Variables CSS harmoniques */
:root {
    --bg-primary: #091326;
    --bg-secondary: #142240;
    --text-primary: #7DD3FC;
    --text-secondary: #5F65D9;
    --border-color: #1D2659;
    --accent-color: #6B71F2;
}

/* Reset complet - Force TOUS les éléments */
*, *:before, *:after {
    -fx-base: var(--bg-primary) !important;
    -fx-background: var(--bg-primary) !important;
    -fx-background-color: var(--bg-primary) !important;
    -fx-control-inner-background: var(--bg-primary) !important;
    -fx-control-inner-background-alt: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-text-base-color: var(--text-primary) !important;
}

/* ===== STRUCTURE PRINCIPALE ===== */
.root, .scene, .parent, .region {
    -fx-font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif !important;
    -fx-font-size: 12px !important;
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* ===== COMPOSANTS AVEC FOND SECONDAIRE ===== */
.text-field, .text-area, .text-input, .combo-box, .choice-box, 
.date-picker, .spinner, .password-field {
    -fx-base: var(--bg-secondary) !important;
    -fx-background: var(--bg-secondary) !important;
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-border-color: var(--border-color) !important;
    -fx-border-width: 1px !important;
    -fx-background-radius: 4px !important;
}

/* ===== BARRE DE RECHERCHE GLOBALE - HARMONISATION TOTALE ===== */
/* Conteneur de la recherche */
.search-container, .global-search-container {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-background-radius: 25px !important;
    -fx-padding: 5px 15px !important;
    -fx-border-color: transparent !important;
}

/* Champ de recherche globale - FORCE LE FOND #142240 */
.global-search-field {
    -fx-base: var(--bg-secondary) !important;
    -fx-background: var(--bg-secondary) !important;
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-control-inner-background-alt: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-prompt-text-fill: var(--text-secondary) !important;
    -fx-border-color: transparent !important;
    -fx-background-insets: 0 !important;
    -fx-background-radius: 0 !important;
    -fx-focus-color: transparent !important;
    -fx-faint-focus-color: transparent !important;
    -fx-text-box-border: transparent !important;
}

/* Force TOUS les sous-éléments de la recherche */
.global-search-field .text,
.global-search-field .content,
.global-search-field > * {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-fill: var(--text-primary) !important;
}

/* États focus et hover */
.global-search-field:focused,
.global-search-field:hover {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-border-color: var(--border-color) !important;
}

/* Header et barre d'outils */
.header .text-field,
.header .text-area,
.menu-bar .text-field,
.menu-bar .text-area {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* ===== NAVIGATION ET SIDEBAR ===== */
.sidebar, .navigation, .left-panel {
    -fx-background-color: var(--bg-primary) !important;
    -fx-padding: 10px !important;
    -fx-min-width: 220px !important;
    -fx-max-width: 220px !important;
    -fx-pref-width: 220px !important;
}

.sidebar .label, .navigation .label {
    -fx-text-fill: var(--text-primary) !important;
    -fx-background-color: transparent !important;
}

/* ===== BOUTONS ===== */
.button {
    -fx-background-color: var(--accent-color) !important;
    -fx-text-fill: white !important;
    -fx-background-radius: 4px !important;
    -fx-border-color: transparent !important;
    -fx-padding: 8px 16px !important;
    -fx-font-weight: normal !important;
}

.button:hover {
    -fx-background-color: derive(var(--accent-color), 20%) !important;
}

.button:pressed {
    -fx-background-color: derive(var(--accent-color), -10%) !important;
}

/* ===== TABLEAUX ===== */
.table-view, .list-view, .tree-view {
    -fx-background-color: var(--bg-primary) !important;
    -fx-control-inner-background: var(--bg-primary) !important;
    -fx-control-inner-background-alt: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-border-color: var(--border-color) !important;
}

.table-view .column-header {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-border-color: var(--border-color) !important;
}

.table-row-cell {
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.table-row-cell:odd {
    -fx-background-color: var(--bg-secondary) !important;
}

.table-row-cell:selected {
    -fx-background-color: var(--accent-color) !important;
    -fx-text-fill: white !important;
}

/* ===== COULEURS HARMONIQUES POUR GRAPHIQUES ===== */
/* Palette harmonique carrée */
.chart-series-line.series0 { -fx-stroke: #6B71F2; }
.chart-series-area-fill.series0 { -fx-fill: #6B71F2; }
.chart-pie-slice.data0 { -fx-pie-color: #6B71F2; }

.chart-series-line.series1 { -fx-stroke: #F26BA6; }
.chart-series-area-fill.series1 { -fx-fill: #F26BA6; }
.chart-pie-slice.data1 { -fx-pie-color: #F26BA6; }

.chart-series-line.series2 { -fx-stroke: #A6F26B; }
.chart-series-area-fill.series2 { -fx-fill: #A6F26B; }
.chart-pie-slice.data2 { -fx-pie-color: #A6F26B; }

.chart-series-line.series3 { -fx-stroke: #6BF2A6; }
.chart-series-area-fill.series3 { -fx-fill: #6BF2A6; }
.chart-pie-slice.data3 { -fx-pie-color: #6BF2A6; }

.chart-series-line.series4 { -fx-stroke: #8A7DD3; }
.chart-series-area-fill.series4 { -fx-fill: #8A7DD3; }
.chart-pie-slice.data4 { -fx-pie-color: #8A7DD3; }

/* ===== SUPPRESSION DES STYLES PAR DÉFAUT JAVAFX ===== */
.root .text-field {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
}

/* ===== SCROLLBARS ===== */
.scroll-bar {
    -fx-background-color: var(--bg-primary) !important;
}

.scroll-bar .thumb {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-background-radius: 4px !important;
}

.scroll-bar .track {
    -fx-background-color: var(--bg-primary) !important;
}

/* ===== TABS ===== */
.tab-pane {
    -fx-background-color: var(--bg-primary) !important;
    -fx-border-color: var(--border-color) !important;
}

.tab {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.tab:selected {
    -fx-background-color: var(--accent-color) !important;
    -fx-text-fill: white !important;
}

/* ===== MENUS ===== */
.menu-bar {
    -fx-background-color: var(--bg-primary) !important;
    -fx-border-color: var(--border-color) !important;
}

.menu {
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.menu-item {
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.menu-item:hover {
    -fx-background-color: var(--bg-secondary) !important;
}

/* ===== SUPRESSION DE TOUTES LES BORDURES INUTILES ===== */
.split-pane-divider {
    -fx-background-color: transparent !important;
    -fx-border-color: transparent !important;
}

.scroll-pane {
    -fx-background-color: var(--bg-primary) !important;
    -fx-border-color: transparent !important;
}

/* ===== FINALISATION - FORCE ABSOLUTE ===== */
/* Override JavaFX par défaut avec !important absolu */
.text-field {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
}

.global-search-field {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
}
"@

Write-Output $UnifiedCSS | Out-File -FilePath "$DesktopPath\theme-dark-ultra-unified.css" -Encoding UTF8 -Force
Write-Host "✅ Créé: theme-dark-ultra-unified.css" -ForegroundColor Green

# 3. Remplacer le contenu du fichier theme-dark-ultra.css existant
Write-Output $UnifiedCSS | Out-File -FilePath "$DesktopPath\theme-dark-ultra.css" -Encoding UTF8 -Force
Write-Host "✅ Mis à jour: theme-dark-ultra.css avec CSS unifié" -ForegroundColor Green

# 4. Supprimer/renommer les CSS conflictuels
$ConflictingFiles = @(
    "application-base.css",
    "theme-dark.css", 
    "magsav-theme.css"
)

foreach ($File in $ConflictingFiles) {
    $FilePath = Join-Path $DesktopPath $File
    if (Test-Path $FilePath) {
        $NewName = "$File.disabled"
        Move-Item $FilePath "$DesktopPath\$NewName" -Force
        Write-Host "🚫 Désactivé: $File → $NewName" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎯 STRUCTURE CSS UNIFIÉE TERMINÉE!" -ForegroundColor Green
Write-Host "📝 Sauvegarde: $BackupPath" -ForegroundColor Cyan
Write-Host "✨ CSS principal: theme-dark-ultra.css (unifié)" -ForegroundColor Cyan
Write-Host "🔧 Fichiers conflictuels désactivés" -ForegroundColor Yellow
Write-Host ""
Write-Host "📋 Actions effectuées:" -ForegroundColor White
Write-Host "   • Sauvegarde complète des CSS existants" -ForegroundColor Gray
Write-Host "   • Création du CSS unifié avec variables CSS" -ForegroundColor Gray
Write-Host "   • Harmonisation barre de recherche globale (#142240)" -ForegroundColor Gray
Write-Host "   • Désactivation des fichiers conflictuels" -ForegroundColor Gray
Write-Host "   • Conservation des couleurs harmoniques pour graphiques" -ForegroundColor Gray
Write-Host ""
Write-Host "⚡ Redémarrage de l'application requis!" -ForegroundColor Red