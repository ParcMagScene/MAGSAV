# Script de réparation urgente - Supprimer les BOM UTF-8 des fichiers Java

Write-Host "=== REPARATION URGENTE BOM UTF-8 ===" -ForegroundColor Red

# Liste des fichiers corrompus d'après l'erreur de compilation
$corruptedFiles = @(
    "backend\src\main\java\com\magscene\magsav\backend\controller\CategoryRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ClientController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ContactController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ContractController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\EquipmentPhotoRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\EquipmentRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\HealthController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\PersonnelController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ProjectController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ServiceRequestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\SupplierOrderController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\SupplierOrderItemController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\VehicleController.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Category.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Client.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Contract.java",
    "backend\src\main\java\com\magscene\magsav\backend\entity\Equipment.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\CategoryRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ClientRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ContactRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ContractRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\EquipmentPhotoRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\EquipmentRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\PersonnelRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ProjectRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ServiceRequestRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\SupplierOrderItemRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\SupplierOrderRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\VehicleRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\service\SavDataInitializer.java"
)

$fixedCount = 0
$rootPath = "C:\Users\aalou\MAGSAV-3.0"

foreach ($relativePath in $corruptedFiles) {
    $fullPath = Join-Path $rootPath $relativePath
    
    if (Test-Path $fullPath) {
        try {
            # Lire le fichier en binaire pour supprimer le BOM
            $bytes = [System.IO.File]::ReadAllBytes($fullPath)
            
            # Vérifier et supprimer le BOM UTF-8 (EF BB BF)
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                $bytesWithoutBOM = $bytes[3..($bytes.Length - 1)]
                [System.IO.File]::WriteAllBytes($fullPath, $bytesWithoutBOM)
                Write-Host "  Fixed BOM: $relativePath" -ForegroundColor Green
                $fixedCount++
            }
        }
        catch {
            Write-Host "  Error fixing: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "Repare $fixedCount fichiers corrompus" -ForegroundColor Green

# Test de compilation pour vérifier la réparation
Write-Host "Test de compilation..." -ForegroundColor Cyan
Set-Location $rootPath
& .\gradlew :backend:compileJava --no-daemon

Write-Host "=== REPARATION TERMINEE ===" -ForegroundColor Green