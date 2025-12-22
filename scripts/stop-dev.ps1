# MAGSAV 3.0 - Script d'arret propre

$ErrorActionPreference = "Continue"
$ConfirmPreference = "None"

Write-Host "`n[STOP] Arret de MAGSAV 3.0..." -ForegroundColor Cyan

# Fonction pour tuer un processus sur un port
function Stop-ProcessOnPort {
    param([int]$Port, [string]$Name)
    $procs = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | 
             Select-Object -ExpandProperty OwningProcess -Unique
    if ($procs) {
        foreach ($proc in $procs) {
            try {
                Stop-Process -Id $proc -Force -ErrorAction Stop
                Write-Host "[OK] $Name arrete (PID: $proc)" -ForegroundColor Green
            } catch {
                Write-Host "[WARN] Impossible d'arreter $Name (PID: $proc)" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "[INFO] $Name n'est pas en cours d'execution" -ForegroundColor Gray
    }
}

# Arreter le backend
Stop-ProcessOnPort 8080 "Backend Spring Boot"

# Arreter les processus Gradle
$gradleProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue | 
               Where-Object { $_.CommandLine -like "*gradle*" -or $_.CommandLine -like "*desktop-javafx*" }
foreach ($proc in $gradleProcs) {
    try {
        Stop-Process -Id $proc.Id -Force -ErrorAction Stop
        Write-Host "[OK] Processus Gradle arrete (PID: $($proc.Id))" -ForegroundColor Green
    } catch {
        Write-Host "[WARN] Impossible d'arreter le processus Gradle" -ForegroundColor Yellow
    }
}

# Arreter les processus JavaFX
$javafxProcs = Get-Process -Name "javaw" -ErrorAction SilentlyContinue
foreach ($proc in $javafxProcs) {
    try {
        Stop-Process -Id $proc.Id -Force -ErrorAction Stop
        Write-Host "[OK] Application JavaFX arretee (PID: $($proc.Id))" -ForegroundColor Green
    } catch {
        Write-Host "[WARN] Impossible d'arreter JavaFX" -ForegroundColor Yellow
    }
}

Write-Host "[DONE] Arret termine`n" -ForegroundColor Green
