# MAGSAV-3.0 Backend Starter
param([switch]$Clean = $false)

$JAVA_HOME = "C:\Users\aalou\.jdk\jdk-21.0.8"
$PROJECT_ROOT = "C:\Users\aalou\MAGSAV-3.0"
$PORT = 8080

Write-Host "Backend MAGSAV-3.0 Starter" -ForegroundColor Green

# Nettoyage si demande
if ($Clean) {
    Write-Host "Nettoyage..." -ForegroundColor Yellow
    Get-Process -Name "java*" -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 3
    Write-Host "Nettoyage termine" -ForegroundColor Green
}

# Verification port
$portInUse = Get-NetTCPConnection -LocalPort $PORT -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "Port occupe - Arret processus..." -ForegroundColor Yellow
    $process = Get-Process -Id $portInUse.OwningProcess -ErrorAction SilentlyContinue
    if ($process) { $process | Stop-Process -Force; Start-Sleep -Seconds 2 }
}

# Configuration environnement
$env:JAVA_HOME = $JAVA_HOME
Set-Location $PROJECT_ROOT

Write-Host "Demarrage backend..." -ForegroundColor Cyan

# Demarrage avec Start-Process pour isolation complete
$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = "cmd.exe"
$startInfo.Arguments = "/c gradlew.bat :backend:bootRun --no-daemon"
$startInfo.WorkingDirectory = $PROJECT_ROOT
$startInfo.UseShellExecute = $true
$startInfo.WindowStyle = "Normal"

$process = [System.Diagnostics.Process]::Start($startInfo)

Write-Host "Backend demarre - PID: $($process.Id)" -ForegroundColor Green
Write-Host "Verification connectivite..." -ForegroundColor Cyan

# Attente service disponible
$timeout = 45
$elapsed = 0
$connected = $false

while ($elapsed -lt $timeout -and -not $connected) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$PORT/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) { $connected = $true }
    } catch { }
    
    if ($elapsed % 9 -eq 0) {
        Write-Host "Attente... ${elapsed}s" -ForegroundColor Yellow
    }
}

if ($connected) {
    Write-Host "SUCCESS! Backend operationnel" -ForegroundColor Green
    Write-Host "URL: http://localhost:$PORT" -ForegroundColor Cyan
    Write-Host "H2 Console: http://localhost:$PORT/h2-console" -ForegroundColor Cyan
    Write-Host "PID Backend: $($process.Id)" -ForegroundColor Yellow
} else {
    Write-Host "Timeout - Verifiez manuellement" -ForegroundColor Yellow
    Write-Host "URL: http://localhost:$PORT" -ForegroundColor Cyan
}