# Configuration UTF-8 optimale pour MAGSAV-3.0
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

# Configuration des variables d'environnement pour Java
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.language=fr -Duser.country=FR -Dconsole.encoding=UTF-8"
$env:GRADLE_OPTS = "-Xmx2g -Dfile.encoding=UTF-8 -Duser.language=fr -Duser.country=FR"

Write-Host "Configuration UTF-8 appliquee pour MAGSAV-3.0" -ForegroundColor Green
Write-Host "Lancement de l'application desktop..." -ForegroundColor Yellow

# Compilation et lancement
& .\gradlew.bat :desktop-javafx:run --no-daemon --console=plain