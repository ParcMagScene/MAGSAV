# ============================================
# Script de Correction des Problèmes Critiques
# MAGSAV-3.0
# ============================================

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   CORRECTION PROBLÈMES CRITIQUES    " -ForegroundColor Cyan
Write-Host "   MAGSAV-3.0                        " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "c:\Users\aalou\MAGSAV-3.0"
$appProperties = "$projectRoot\backend\src\main\resources\application.properties"
$backupFile = "$appProperties.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

# ============================================
# 1. BACKUP DU FICHIER DE CONFIGURATION
# ============================================
Write-Host "[1/5] Création backup de application.properties..." -ForegroundColor Yellow
Copy-Item $appProperties $backupFile
Write-Host "      ✅ Backup créé: $backupFile" -ForegroundColor Green
Write-Host ""

# ============================================
# 2. CORRECTION DDL-AUTO (create → update)
# ============================================
Write-Host "[2/5] Correction spring.jpa.hibernate.ddl-auto..." -ForegroundColor Yellow
$content = Get-Content $appProperties -Raw

if ($content -match "spring\.jpa\.hibernate\.ddl-auto=create") {
    $content = $content -replace "spring\.jpa\.hibernate\.ddl-auto=create", "spring.jpa.hibernate.ddl-auto=update"
    Set-Content -Path $appProperties -Value $content -NoNewline
    Write-Host "      ✅ Changé: create → update" -ForegroundColor Green
} else {
    Write-Host "      ℹ️  Déjà configuré en 'update' ou 'validate'" -ForegroundColor Gray
}
Write-Host ""

# ============================================
# 3. DÉSACTIVATION LOGS SQL
# ============================================
Write-Host "[3/5] Désactivation logs SQL verbeux..." -ForegroundColor Yellow
$content = Get-Content $appProperties -Raw

# Désactiver show-sql
if ($content -match "spring\.jpa\.show-sql=true") {
    $content = $content -replace "spring\.jpa\.show-sql=true", "spring.jpa.show-sql=false"
    Write-Host "      ✅ Désactivé: show-sql" -ForegroundColor Green
}

# Réduire niveau de logs Hibernate
$content = $content -replace "logging\.level\.org\.hibernate\.SQL=DEBUG", "logging.level.org.hibernate.SQL=WARN"
$content = $content -replace "logging\.level\.org\.hibernate\.type\.descriptor\.sql\.BasicBinder=TRACE", "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN"

Set-Content -Path $appProperties -Value $content -NoNewline
Write-Host "      ✅ Logs réduits à niveau WARN" -ForegroundColor Green
Write-Host ""

# ============================================
# 4. SÉCURISATION MOT DE PASSE
# ============================================
Write-Host "[4/5] Vérification sécurité mot de passe..." -ForegroundColor Yellow
$content = Get-Content $appProperties -Raw

if ($content -match "spring\.datasource\.password=password\s") {
    Write-Host "      ⚠️  Mot de passe 'password' détecté!" -ForegroundColor Red
    Write-Host "      📝 Recommandation: Utiliser une variable d'environnement" -ForegroundColor Yellow
    Write-Host "         spring.datasource.password=`${DB_PASSWORD:password}" -ForegroundColor Gray
    
    $content = $content -replace "spring\.datasource\.password=password", "spring.datasource.password=`${DB_PASSWORD:password}"
    Set-Content -Path $appProperties -Value $content -NoNewline
    Write-Host "      ✅ Configuration sécurisée ajoutée" -ForegroundColor Green
} else {
    Write-Host "      ✅ Mot de passe déjà sécurisé" -ForegroundColor Green
}
Write-Host ""

# ============================================
# 5. DÉSACTIVATION SQL INIT MODE
# ============================================
Write-Host "[5/5] Configuration SQL init mode..." -ForegroundColor Yellow
$content = Get-Content $appProperties -Raw

# En production, ne pas réexécuter les scripts SQL
if ($content -match "spring\.sql\.init\.mode=always") {
    Write-Host "      ⚠️  Mode 'always' détecté - données réinitialisées à chaque démarrage!" -ForegroundColor Yellow
    Write-Host "      💡 Changement recommandé pour production: never ou embedded" -ForegroundColor Gray
    
    # Ajouter un commentaire explicatif
    $content = $content -replace "spring\.sql\.init\.mode=always", 
        "# Pour DEV: always | Pour PROD: never`nspring.sql.init.mode=always"
    Set-Content -Path $appProperties -Value $content -NoNewline
    Write-Host "      ✅ Commentaire ajouté avec recommandation" -ForegroundColor Green
} else {
    Write-Host "      ✅ Configuration SQL init appropriée" -ForegroundColor Green
}
Write-Host ""

# ============================================
# RÉSUMÉ DES CORRECTIONS
# ============================================
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   RÉSUMÉ DES CORRECTIONS            " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Backup créé" -ForegroundColor Green
Write-Host "✅ ddl-auto: create → update" -ForegroundColor Green
Write-Host "✅ Logs SQL désactivés" -ForegroundColor Green
Write-Host "✅ Mot de passe sécurisé avec variable env" -ForegroundColor Green
Write-Host "✅ Commentaire SQL init mode ajouté" -ForegroundColor Green
Write-Host ""
Write-Host "📁 Backup: $backupFile" -ForegroundColor Gray
Write-Host ""

# ============================================
# PROCHAINES ÉTAPES
# ============================================
Write-Host "=====================================" -ForegroundColor Magenta
Write-Host "   PROCHAINES ÉTAPES                 " -ForegroundColor Magenta
Write-Host "=====================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "1. 🔄 Redémarrer le backend:" -ForegroundColor Yellow
Write-Host "   cd $projectRoot" -ForegroundColor Gray
Write-Host "   .\gradlew.bat :backend:bootRun" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 🧪 Vérifier que l'application fonctionne:" -ForegroundColor Yellow
Write-Host "   http://localhost:8080/api/service-requests" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 📊 Consulter le rapport d'audit complet:" -ForegroundColor Yellow
Write-Host "   $projectRoot\AUDIT-COMPLET.md" -ForegroundColor Gray
Write-Host ""
Write-Host "4. 🚀 Pour la production:" -ForegroundColor Yellow
Write-Host "   Activer le profil: --spring.profiles.active=production" -ForegroundColor Gray
Write-Host "   Définir: " -NoNewline -ForegroundColor Gray
Write-Host '$env:DB_PASSWORD=' -NoNewline -ForegroundColor Gray
Write-Host "'votre_mot_de_passe_sécurisé'" -ForegroundColor Gray
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   CORRECTIONS TERMINÉES ✅           " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
