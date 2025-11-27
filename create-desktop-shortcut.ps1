# Script pour creer un raccourci bureau MAGSAV 3.0
$ErrorActionPreference = "Stop"

Write-Host "`n=== CREATION RACCOURCI MAGSAV 3.0 ===" -ForegroundColor Cyan

# Chemin du bureau
$desktopPath = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktopPath "MAGSAV 3.0.lnk"

# Creer le raccourci
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut($shortcutPath)

# Configuration du raccourci
$Shortcut.TargetPath = "powershell.exe"
$Shortcut.Arguments = "-ExecutionPolicy Bypass -NoProfile -File `"$PSScriptRoot\start-dev.ps1`""
$Shortcut.WorkingDirectory = $PSScriptRoot
$Shortcut.Description = "Lancer MAGSAV 3.0 (Backend + Desktop)"
$Shortcut.IconLocation = "shell32.dll,21"  # Icone ordinateur
$Shortcut.WindowStyle = 1  # Fenetre normale

# Sauvegarder
$Shortcut.Save()

Write-Host "[OK] Raccourci cree: $shortcutPath" -ForegroundColor Green

# Creer aussi un raccourci "Desktop seulement"
$shortcutDesktopOnly = Join-Path $desktopPath "MAGSAV Desktop.lnk"
$ShortcutDO = $WshShell.CreateShortcut($shortcutDesktopOnly)
$ShortcutDO.TargetPath = "powershell.exe"
$ShortcutDO.Arguments = "-ExecutionPolicy Bypass -NoProfile -File `"$PSScriptRoot\start-dev.ps1`" -DesktopOnly"
$ShortcutDO.WorkingDirectory = $PSScriptRoot
$ShortcutDO.Description = "Lancer MAGSAV Desktop uniquement"
$ShortcutDO.IconLocation = "shell32.dll,220"  # Icone application
$ShortcutDO.WindowStyle = 1
$ShortcutDO.Save()

Write-Host "[OK] Raccourci cree: $shortcutDesktopOnly" -ForegroundColor Green

Write-Host "`n=== RACCOURCIS CREES ===" -ForegroundColor Green
Write-Host "  1. MAGSAV 3.0.lnk      - Lance backend + desktop" -ForegroundColor White
Write-Host "  2. MAGSAV Desktop.lnk  - Lance desktop uniquement" -ForegroundColor White
Write-Host "`nVous pouvez maintenant lancer MAGSAV depuis votre bureau !`n" -ForegroundColor Cyan
