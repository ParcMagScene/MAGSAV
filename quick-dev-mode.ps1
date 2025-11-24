# MAGSAV Dev Mode
Write-Host 'Activation Mode Dev...' -ForegroundColor Cyan
$global:ConfirmPreference = 'None'
$env:MAGSAV_DEV_MODE = 'ENABLED'
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
Write-Host 'Mode Dev ACTIVE' -ForegroundColor Green
