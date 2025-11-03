# Verification rapide MAGSAV-3.0
Write-Host "Status MAGSAV-3.0" -ForegroundColor Green
Write-Host "=================" -ForegroundColor Green

# Processus Java actifs
$javaProcesses = Get-Process | Where-Object {$_.ProcessName -eq "java"}
Write-Host "Processus Java actifs: $($javaProcesses.Count)" -ForegroundColor Cyan

if ($javaProcesses.Count -gt 0) {
    foreach ($proc in $javaProcesses) {
        Write-Host "  PID: $($proc.Id) - Memoire: $([math]::Round($proc.WorkingSet/1MB, 1)) MB" -ForegroundColor Yellow
    }
}

# Test connectivite backend
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/equipment" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "Backend: OK - $($response.Count) equipements" -ForegroundColor Green
} catch {
    Write-Host "Backend: Non accessible" -ForegroundColor Red
}

Write-Host "`nApplication prete !" -ForegroundColor Green