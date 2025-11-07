#!/usr/bin/env pwsh
# Script d'unification CSS MAGSAV-3.0

$DesktopPath = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\resources\styles"

Write-Host "UNIFICATION CSS MAGSAV-3.0 - RESOLUTION DES CONFLITS" -ForegroundColor Yellow

# 1. Sauvegarde
$BackupPath = "$DesktopPath\_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $BackupPath -Force | Out-Null
Write-Host "Sauvegarde dans: $BackupPath" -ForegroundColor Cyan

Get-ChildItem -Path $DesktopPath -Filter "*.css" | ForEach-Object {
    Copy-Item $_.FullName -Destination "$BackupPath\$($_.Name)" -Force
    Write-Host "   Sauvegarde: $($_.Name)" -ForegroundColor Green
}

# 2. CSS unifie
$UnifiedCSS = @"
/* MAGSAV-3.0 - CSS UNIFIE HARMONISATION TOTALE */

/* Variables CSS harmoniques */
:root {
    --bg-primary: #091326;
    --bg-secondary: #142240;
    --text-primary: #7DD3FC;
    --text-secondary: #5F65D9;
    --border-color: #1D2659;
    --accent-color: #6B71F2;
}

/* Reset complet */
*, *:before, *:after {
    -fx-base: var(--bg-primary) !important;
    -fx-background: var(--bg-primary) !important;
    -fx-background-color: var(--bg-primary) !important;
    -fx-control-inner-background: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* Structure principale */
.root, .scene, .parent, .region {
    -fx-font-family: "Segoe UI", Arial, sans-serif !important;
    -fx-font-size: 12px !important;
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* Composants avec fond secondaire */
.text-field, .text-area, .combo-box, .choice-box, 
.date-picker, .spinner, .password-field {
    -fx-base: var(--bg-secondary) !important;
    -fx-background: var(--bg-secondary) !important;
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
    -fx-border-color: var(--border-color) !important;
    -fx-background-radius: 4px !important;
}

/* BARRE DE RECHERCHE GLOBALE - HARMONISATION TOTALE */
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
}

/* Force tous les sous-elements de la recherche */
.global-search-field .text,
.global-search-field .content,
.global-search-field > * {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-fill: var(--text-primary) !important;
}

.global-search-field:focused,
.global-search-field:hover {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
}

/* Header */
.header .text-field,
.header .text-area,
.menu-bar .text-field,
.menu-bar .text-area {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-control-inner-background: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* Navigation */
.sidebar, .navigation, .left-panel {
    -fx-background-color: var(--bg-primary) !important;
    -fx-min-width: 220px !important;
    -fx-max-width: 220px !important;
    -fx-pref-width: 220px !important;
}

/* Boutons */
.button {
    -fx-background-color: var(--accent-color) !important;
    -fx-text-fill: white !important;
    -fx-background-radius: 4px !important;
    -fx-padding: 8px 16px !important;
}

.button:hover {
    -fx-background-color: derive(var(--accent-color), 20%) !important;
}

/* Tableaux */
.table-view {
    -fx-background-color: var(--bg-primary) !important;
    -fx-control-inner-background: var(--bg-primary) !important;
    -fx-control-inner-background-alt: var(--bg-secondary) !important;
}

.table-row-cell {
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.table-row-cell:odd {
    -fx-background-color: var(--bg-secondary) !important;
}

/* Couleurs harmoniques graphiques */
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

/* Scrollbars */
.scroll-bar {
    -fx-background-color: var(--bg-primary) !important;
}

.scroll-bar .thumb {
    -fx-background-color: var(--bg-secondary) !important;
}

/* Tabs */
.tab-pane {
    -fx-background-color: var(--bg-primary) !important;
}

.tab {
    -fx-background-color: var(--bg-secondary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

.tab:selected {
    -fx-background-color: var(--accent-color) !important;
    -fx-text-fill: white !important;
}

/* Menus */
.menu-bar {
    -fx-background-color: var(--bg-primary) !important;
}

.menu, .menu-item {
    -fx-background-color: var(--bg-primary) !important;
    -fx-text-fill: var(--text-primary) !important;
}

/* Suppression bordures */
.split-pane-divider {
    -fx-background-color: transparent !important;
}

.scroll-pane {
    -fx-background-color: var(--bg-primary) !important;
    -fx-border-color: transparent !important;
}
"@

# 3. Ecrire le fichier
Write-Output $UnifiedCSS | Out-File -FilePath "$DesktopPath\theme-dark-ultra.css" -Encoding UTF8 -Force
Write-Host "Mis a jour: theme-dark-ultra.css" -ForegroundColor Green

# 4. Desactiver fichiers conflictuels
$ConflictingFiles = @("application-base.css", "theme-dark.css", "magsav-theme.css")

foreach ($File in $ConflictingFiles) {
    $FilePath = Join-Path $DesktopPath $File
    if (Test-Path $FilePath) {
        $NewName = "$File.disabled"
        Move-Item $FilePath "$DesktopPath\$NewName" -Force
        Write-Host "Desactive: $File" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "STRUCTURE CSS UNIFIEE TERMINEE!" -ForegroundColor Green
Write-Host "Redemarrage application requis!" -ForegroundColor Red