package com.magscene.magsav.backend.service;

import com.magscene.magsav.backend.entity.Category;
import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.repository.CategoryRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour l'import des données LOCMAT depuis Excel
 */
@Service
public class LocmatImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocmatImportService.class);
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // UID basés sur les catégories (3 lettres + 4 chiffres)
    // Cache pour les compteurs d'UID par préfixe (évite les requêtes répétitives)
    private Map<String, Integer> uidCounterCache = new HashMap<>();
    
    /**
     * Importer les données depuis le fichier Excel LOCMAT
     */
    public ImportResult importLocmatData(MultipartFile file) throws IOException {
        logger.info("🚀 Début import LOCMAT - Fichier: {}, Taille: {} bytes", file.getOriginalFilename(), file.getSize());
        ImportResult result = new ImportResult();
        
        // Réinitialiser le cache des UIDs au début de chaque import
        uidCounterCache.clear();
        
        try (InputStream inputStream = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            
            logger.debug("📊 Workbook ouvert, nombre de sheets: {}", workbook.getNumberOfSheets());
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            logger.debug("📋 Sheet sélectionnée: {}, Nombre de lignes: {}", sheet.getSheetName(), sheet.getLastRowNum() + 1);
            
            // Ignorer la ligne d'en-tête
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                logger.debug("📝 En-tête ignorée: {} colonnes", headerRow.getLastCellNum());
            }
            
            Map<String, Category> categoryCache = new HashMap<>();
            Map<String, Category> subCategoryCache = new HashMap<>();
            Map<String, Category> brandCache = new HashMap<>();
            Map<String, Category> ownerCache = new HashMap<>();
            
            int rowNumber = 2; // Commence à 2 car ligne 1 = en-tête
            
            int totalRows = sheet.getLastRowNum();
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Log progress every 50 rows
                if (rowNumber % 50 == 0) {
                    logger.info("🔄 Progression: {}/{} lignes traitées ({}%)", rowNumber - 1, totalRows, ((rowNumber - 1) * 100) / totalRows);
                }
                
                try {
                    logger.debug("📝 Traitement ligne {}", rowNumber);
                    LocmatRow locmatRow = parseRow(row);
                    if (locmatRow != null && locmatRow.isValid()) {
                        processLocmatRow(locmatRow, categoryCache, subCategoryCache, 
                                      brandCache, ownerCache, result);
                        logger.debug("✅ Ligne {} traitée avec succès", rowNumber);
                    } else {
                        logger.debug("⏭️ Ligne {} vide ou invalide, ignorée", rowNumber);
                    }
                } catch (Exception e) {
                    String errorMsg = "Ligne " + rowNumber + ": " + e.getMessage();
                    logger.error("❌ {}", errorMsg, e);
                    result.addError(errorMsg);
                }
                
                rowNumber++;
            }
            
            logger.info("✅ Import terminé - Succès: {}, Erreurs: {}", result.getSuccessCount(), result.getErrors().size());
            
        } catch (IOException e) {
            logger.error("❌ Erreur lecture fichier Excel: {}", e.getMessage(), e);
            throw new IOException("Erreur lors de la lecture du fichier Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Erreur inattendue durant l'import: {}", e.getMessage(), e);
            result.addError("Erreur inattendue: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parser une ligne Excel en objet LocmatRow
     */
    private LocmatRow parseRow(Row row) {
        if (row == null || isEmptyRow(row)) {
            return null;
        }
        
        LocmatRow locmatRow = new LocmatRow();
        
        // Code Locmat (colonne A) - nettoyer les caractères '*'
        String rawCode = getCellValueAsString(row.getCell(0));
        locmatRow.codeLocmat = rawCode != null ? rawCode.replace("*", "").trim() : null;
        
        // Catégorie (colonne B)
        locmatRow.categorie = getCellValueAsString(row.getCell(1));
        
        // Sous-catégorie (colonne C)
        locmatRow.sousCategorie = getCellValueAsString(row.getCell(2));
        
        // Description (colonne D)
        locmatRow.description = getCellValueAsString(row.getCell(3));
        
        // Marque (colonne E)
        locmatRow.marque = getCellValueAsString(row.getCell(4));
        
        // Propriétaire (colonne F)
        locmatRow.proprietaire = getCellValueAsString(row.getCell(5));
        
        // NumSerie (colonne G)
        locmatRow.numSerie = getCellValueAsString(row.getCell(6));
        
        // Quantité (colonne H)
        locmatRow.quantite = getCellValueAsInteger(row.getCell(7));
        
        return locmatRow;
    }
    
    /**
     * Traiter une ligne LOCMAT et créer les équipements correspondants
     */
    private void processLocmatRow(LocmatRow row, Map<String, Category> categoryCache,
                                Map<String, Category> subCategoryCache,
                                Map<String, Category> brandCache,
                                Map<String, Category> ownerCache,
                                ImportResult result) {
        
        try {
            // Créer ou récupérer les catégories
            Category mainCategory = getOrCreateCategory(row.categorie, null, categoryCache, "Catégorie principale");
            Category subCategory = null;
            
            if (row.sousCategorie != null && !row.sousCategorie.trim().isEmpty()) {
                subCategory = getOrCreateCategory(row.sousCategorie, mainCategory, subCategoryCache, "Sous-catégorie");
            }
            
            // Créer ou récupérer les marques comme catégories
            Category brandCategory = null;
            if (row.marque != null && !row.marque.trim().isEmpty()) {
                brandCategory = getOrCreateCategory("Marque: " + row.marque, null, brandCache, "Marque");
            }
            
            // Créer ou récupérer les propriétaires comme catégories
            Category ownerCategory = null;
            if (row.proprietaire != null && !row.proprietaire.trim().isEmpty()) {
                ownerCategory = getOrCreateCategory("Propriétaire: " + row.proprietaire, null, ownerCache, "Propriétaire");
            }
            
            // Logique métier LOCMAT :
            // - Si équipement sérialisé (a un numéro de série) → 1 équipement par ligne
            // - Si équipement non sérialisé avec quantité → 1 ligne avec la quantité stockée
            // - Si pas de série ET pas de quantité → 1 équipement avec quantité = 0
            
            boolean hasSeries = row.numSerie != null && !row.numSerie.trim().isEmpty() && !row.numSerie.equals("N/A");
            int quantity = row.quantite != null && row.quantite > 0 ? row.quantite : 0;
            
            if (hasSeries) {
                // Équipement sérialisé : 1 seul équipement, ignore la quantité
                Equipment equipment = new Equipment();
                
                // Informations de base
                equipment.setName(row.description != null ? row.description : "Équipement LOCMAT");
                equipment.setDescription(buildDescription(row, 1, 1));
                equipment.setBrand(row.marque);
                equipment.setInternalReference(row.codeLocmat);
                equipment.setSerialNumber(row.numSerie);
                
                createSingleEquipment(equipment, row, mainCategory, subCategory, brandCategory, ownerCategory, result);
                
            } else {
                // Équipement non sérialisé : 1 ligne avec quantité stockée
                Equipment equipment = new Equipment();
                
                // Informations de base  
                equipment.setName(row.description != null ? row.description : "Équipement LOCMAT");
                equipment.setDescription(buildDescriptionWithQuantity(row, quantity));
                equipment.setBrand(row.marque);
                equipment.setInternalReference(row.codeLocmat);
                // Pas de numéro de série pour les équipements non sérialisés
                
                createSingleEquipment(equipment, row, mainCategory, subCategory, brandCategory, ownerCategory, result);
            }
        } catch (Exception e) {
            String errorMsg = "Erreur lors du traitement de l'équipement '" + row.description + "': " + e.getMessage();
            logger.error("❌ {}", errorMsg, e);
            result.addError(errorMsg);
        }
    }
    
    /**
     * Créer un seul équipement avec toutes ses propriétés
     */
    private void createSingleEquipment(Equipment equipment, LocmatRow row, Category mainCategory, 
                                     Category subCategory, Category brandCategory, 
                                     Category ownerCategory, ImportResult result) {
        try {
            // Générer UID :
            // - Pour MAG SCENE : basé sur la catégorie (3 lettres catégorie + 4 chiffres)
            // - Pour autres propriétaires : basé sur le propriétaire (3 lettres propriétaire + 4 chiffres)
            String uid = generateUID(row.categorie, row.proprietaire);
            
            // QR Code = UID
            equipment.setQrCode(uid);
            
            // Associer les catégories
            if (subCategory != null) {
                equipment.setCategoryEntity(subCategory);
                equipment.setCategory(subCategory.getName());
            } else {
                equipment.setCategoryEntity(mainCategory);
                equipment.setCategory(mainCategory.getName());
            }
            
            // Statut par défaut
            equipment.setStatus(Equipment.Status.AVAILABLE);
            
            // Notes avec informations LOCMAT
            equipment.setNotes(buildNotes(row, brandCategory, ownerCategory));
            
            // Dates
            equipment.setCreatedAt(LocalDateTime.now());
            equipment.setUpdatedAt(LocalDateTime.now());
            
            // Sauvegarder l'équipement
            equipment = equipmentRepository.save(equipment);
            logger.debug("💾 Équipement sauvegardé: {} (ID: {})", equipment.getName(), equipment.getId());
            result.incrementSuccess();
            
        } catch (Exception e) {
            result.addError("Erreur lors de la création de l'équipement: " + e.getMessage());
        }
    }
    
    /**
     * Créer ou récupérer une catégorie
     */
    private Category getOrCreateCategory(String name, Category parent, Map<String, Category> cache, String type) {
        String cacheKey = (parent != null ? parent.getId() + ":" : "") + name;
        
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        // Chercher si elle existe déjà
        Optional<Category> existingCategory = categoryRepository.findByNameAndParent(name, parent);
        
        if (existingCategory.isPresent()) {
            cache.put(cacheKey, existingCategory.get());
            return existingCategory.get();
        }
        
        // Créer une nouvelle catégorie
        Category newCategory = new Category(name);
        newCategory.setDescription("Catégorie créée automatiquement depuis l'import LOCMAT (" + type + ")");
        newCategory.setParent(parent);
        newCategory.setColor(getDefaultColorForType(type));
        newCategory.setIcon(getDefaultIconForType(type));
        
        newCategory = categoryRepository.save(newCategory);
        cache.put(cacheKey, newCategory);
        
        return newCategory;
    }
    
    /**
     * Générer un UID (3 lettres + 4 chiffres)
     * - Pour MAG SCENE : basé sur la catégorie
     * - Pour autres propriétaires : basé sur les 3 premières lettres du propriétaire
     */
    private String generateUID(String category, String proprietaire) {
        String prefix;
        
        // Vérifier si c'est MAG SCENE ou autre propriétaire
        boolean isMagScene = proprietaire == null || 
                            proprietaire.trim().isEmpty() || 
                            proprietaire.trim().toUpperCase().contains("MAG") ||
                            proprietaire.trim().toUpperCase().equals("MAG SCENE");
        
        if (isMagScene) {
            // MAG SCENE : préfixe basé sur la catégorie
            prefix = getCategoryPrefix(category);
        } else {
            // Autre propriétaire : préfixe basé sur les 3 premières lettres du propriétaire
            prefix = getOwnerPrefix(proprietaire);
        }
        
        // Utiliser le cache pour obtenir le prochain numéro
        if (!uidCounterCache.containsKey(prefix)) {
            // Première fois pour ce préfixe : récupérer le max depuis la DB
            Integer maxNum = equipmentRepository.findMaxUidNumberByPrefix(prefix);
            uidCounterCache.put(prefix, maxNum != null ? maxNum + 1 : 1);
        }
        
        int counter = uidCounterCache.get(prefix);
        String uid = prefix + String.format("%04d", counter);
        
        // Incrémenter le compteur pour le prochain appel
        uidCounterCache.put(prefix, counter + 1);
        
        return uid;
    }
    
    /**
     * Obtenir le préfixe de 3 lettres basé sur le propriétaire
     */
    private String getOwnerPrefix(String proprietaire) {
        if (proprietaire == null || proprietaire.trim().isEmpty()) {
            return "EXT"; // EXTerne par défaut
        }
        
        String owner = proprietaire.trim().toUpperCase()
                .replaceAll("[^A-Z]", ""); // Garder uniquement les lettres
        
        if (owner.length() >= 3) {
            return owner.substring(0, 3);
        } else if (owner.length() > 0) {
            // Compléter avec des X si moins de 3 lettres
            return (owner + "XXX").substring(0, 3);
        } else {
            return "EXT";
        }
    }
    
    /**
     * Obtenir le préfixe de 3 lettres basé sur la catégorie
     */
    private String getCategoryPrefix(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "GEN"; // GENéral
        }
        
        String cat = category.trim().toUpperCase();
        
        // Mapping des catégories vers les préfixes
        return switch (cat) {
            case "AUDIO", "SON", "SONORISATION" -> "SON";
            case "ECLAIRAGE", "LUMIERE", "LIGHTING" -> "LUM";
            case "VIDEO", "VIDÉO" -> "VID";
            case "STRUCTURE", "TRUSS" -> "STR";
            case "CONSOLE", "MIXAGE" -> "MIX";
            case "MICROPHONE", "MICRO" -> "MIC";
            case "PROJECTEUR", "SPOT" -> "PRO";
            case "CÂBLE", "CABLE" -> "CAB";
            case "AMPLIFICATEUR", "AMPLI" -> "AMP";
            case "ENCEINTE", "HAUT-PARLEUR" -> "ENC";
            case "EFFETS", "EFFET" -> "EFX";
            case "TRANSPORT", "FLIGHT" -> "TRA";
            case "ACCESSOIRE", "DIVERS" -> "ACC";
            default -> {
                // Générer un préfixe à partir des 3 premières lettres
                if (cat.length() >= 3) {
                    yield cat.substring(0, 3);
                } else {
                    yield (cat + "XXX").substring(0, 3);
                }
            }
        };
    }
    

    
    /**
     * Construire la description détaillée (legacy pour équipements sérialisés)
     */
    private String buildDescription(LocmatRow row, int itemNumber, int totalQuantity) {
        StringBuilder desc = new StringBuilder();
        
        desc.append(row.description != null ? row.description : "Équipement LOCMAT");
        
        if (totalQuantity > 1) {
            desc.append(" (").append(itemNumber).append("/").append(totalQuantity).append(")");
        }
        
        if (row.codeLocmat != null && !row.codeLocmat.trim().isEmpty()) {
            desc.append("\nCode LOCMAT: ").append(row.codeLocmat);
        }
        
        return desc.toString();
    }
    
    /**
     * Construire la description pour équipement avec quantité
     */
    private String buildDescriptionWithQuantity(LocmatRow row, int quantity) {
        StringBuilder desc = new StringBuilder();
        
        desc.append(row.description != null ? row.description : "Équipement LOCMAT");
        
        if (quantity > 1) {
            desc.append(" (Quantité: ").append(quantity).append(")");
        } else if (quantity == 0) {
            desc.append(" (Non quantifié)");
        }
        
        if (row.codeLocmat != null && !row.codeLocmat.trim().isEmpty()) {
            desc.append("\nCode LOCMAT: ").append(row.codeLocmat);
        }
        
        return desc.toString();
    }
    
    /**
     * Construire les notes avec toutes les informations LOCMAT
     */
    private String buildNotes(LocmatRow row, Category brandCategory, Category ownerCategory) {
        StringBuilder notes = new StringBuilder("=== IMPORT LOCMAT ===\n");
        
        if (row.codeLocmat != null) notes.append("Code LOCMAT: ").append(row.codeLocmat).append("\n");
        if (row.categorie != null) notes.append("Catégorie: ").append(row.categorie).append("\n");
        if (row.sousCategorie != null) notes.append("Sous-catégorie: ").append(row.sousCategorie).append("\n");
        if (row.marque != null) notes.append("Marque: ").append(row.marque).append("\n");
        if (row.proprietaire != null) notes.append("Propriétaire: ").append(row.proprietaire).append("\n");
        
        notes.append("UID généré automatiquement basé sur la catégorie\n");
        notes.append("QR Code = UID pour identification unique\n");
        notes.append("Importé le: ").append(LocalDateTime.now()).append("\n");
        
        return notes.toString();
    }
    
    /**
     * Obtenir une couleur par défaut selon le type de catégorie
     */
    private String getDefaultColorForType(String type) {
        return switch (type) {
            case "Catégorie principale" -> "#2196F3"; // Bleu
            case "Sous-catégorie" -> "#4CAF50";      // Vert
            case "Marque" -> "#FF9800";              // Orange
            case "Propriétaire" -> "#9C27B0";       // Violet
            default -> "#757575";                    // Gris
        };
    }
    
    /**
     * Obtenir une icône par défaut selon le type de catégorie
     */
    private String getDefaultIconForType(String type) {
        return switch (type) {
            case "Catégorie principale" -> "category";
            case "Sous-catégorie" -> "subdirectory_arrow_right";
            case "Marque" -> "business";
            case "Propriétaire" -> "person";
            default -> "folder";
        };
    }
    
    // Méthodes utilitaires pour Excel
    
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i < 8; i++) { // 8 colonnes attendues
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue().trim();
                }
            }
            default -> null;
        };
    }
    
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String value = cell.getStringCellValue().trim();
                    yield value.isEmpty() ? null : Integer.parseInt(value);
                }
                case FORMULA -> (int) cell.getNumericCellValue();
                default -> null;
            };
        } catch (NumberFormatException e) {
            return 1; // Valeur par défaut si erreur de parsing
        }
    }
    
    /**
     * Classe interne pour représenter une ligne LOCMAT
     */
    private static class LocmatRow {
        String codeLocmat;
        String categorie;
        String sousCategorie;
        String description;
        String marque;
        String proprietaire;
        String numSerie;
        Integer quantite;
        
        boolean isValid() {
            return description != null && !description.trim().isEmpty() &&
                   categorie != null && !categorie.trim().isEmpty();
        }
    }
    
    /**
     * Obtenir le nombre total d'équipements
     */
    public long getTotalEquipmentCount() {
        return equipmentRepository.count();
    }
    
    /**
     * Obtenir le nombre total d'imports (simulé pour l'instant)
     */
    public int getTotalImportCount() {
        // TODO: Implémenter un compteur d'imports persistant
        return 0;
    }
    
    /**
     * Obtenir la date du dernier import (simulé pour l'instant)
     */
    public String getLastImportDate() {
        // TODO: Implémenter le tracking des dates d'import
        return null;
    }
    
    /**
     * Valider un fichier LOCMAT sans l'importer
     */
    public ValidationResult validateFile(MultipartFile file) throws IOException {
        ValidationResult result = new ValidationResult();
        
        try (InputStream inputStream = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Ignorer la ligne d'en-tête
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            int totalRows = 0;
            int validRows = 0;
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                totalRows++;
                
                LocmatRow locmatRow = parseRow(row);
                if (locmatRow != null && locmatRow.isValid()) {
                    validRows++;
                } else {
                    result.addError("Ligne " + (row.getRowNum() + 1) + ": Données invalides");
                }
            }
            
            result.setRowCount(totalRows);
            result.setValidRowCount(validRows);
            result.setValid(validRows > 0 && result.getErrors().isEmpty());
            result.setMessage(validRows + "/" + totalRows + " lignes valides");
            
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("Erreur lors de la validation: " + e.getMessage());
            result.addError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Prévisualiser les données d'un fichier LOCMAT
     */
    public PreviewResult previewFile(MultipartFile file, int maxRows) throws IOException {
        PreviewResult result = new PreviewResult();
        List<Map<String, Object>> previewData = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // En-têtes
            List<String> columns = Arrays.asList("Code LOCMAT", "Catégorie", "Sous-catégorie", "Description", "Marque", "Propriétaire", "Numéro série");
            result.setColumns(columns);
            
            // Ignorer la ligne d'en-tête
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            int totalRows = 0;
            int previewRows = 0;
            
            while (rowIterator.hasNext() && previewRows < maxRows) {
                Row row = rowIterator.next();
                totalRows++;
                
                LocmatRow locmatRow = parseRow(row);
                if (locmatRow != null) {
                    Map<String, Object> rowData = new HashMap<>();
                    rowData.put("Code LOCMAT", locmatRow.codeLocmat);
                    rowData.put("Catégorie", locmatRow.categorie);
                    rowData.put("Sous-catégorie", locmatRow.sousCategorie);
                    rowData.put("Description", locmatRow.description);
                    rowData.put("Marque", locmatRow.marque);
                    rowData.put("Propriétaire", locmatRow.proprietaire);
                    rowData.put("Numéro série", locmatRow.numSerie);
                    
                    previewData.add(rowData);
                    previewRows++;
                }
            }
            
            // Compter le reste des lignes
            while (rowIterator.hasNext()) {
                rowIterator.next();
                totalRows++;
            }
            
            result.setData(previewData);
            result.setTotalRows(totalRows);
            result.setPreviewRows(previewRows);
            
        } catch (Exception e) {
            result.setData(new ArrayList<>());
            result.setTotalRows(0);
            result.setPreviewRows(0);
        }
        
        return result;
    }
    
    /**
     * Classe pour les résultats d'import
     */
    public static class ImportResult {
        private int successCount = 0;
        private final List<String> errors = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("Import terminé: ").append(successCount).append(" équipements créés");
            
            if (hasErrors()) {
                summary.append(", ").append(errors.size()).append(" erreurs");
            }
            
            return summary.toString();
        }
    }
    
    /**
     * Classe pour les résultats de validation
     */
    public static class ValidationResult {
        private boolean valid = false;
        private String message = "";
        private int rowCount = 0;
        private int validRowCount = 0;
        private final List<String> errors = new ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getRowCount() { return rowCount; }
        public void setRowCount(int rowCount) { this.rowCount = rowCount; }
        
        public int getValidRowCount() { return validRowCount; }
        public void setValidRowCount(int validRowCount) { this.validRowCount = validRowCount; }
        
        public List<String> getErrors() { return errors; }
        public void addError(String error) { errors.add(error); }
    }
    
    /**
     * Classe pour les résultats de prévisualisation
     */
    public static class PreviewResult {
        private List<Map<String, Object>> data = new ArrayList<>();
        private int totalRows = 0;
        private int previewRows = 0;
        private List<String> columns = new ArrayList<>();
        
        public List<Map<String, Object>> getData() { return data; }
        public void setData(List<Map<String, Object>> data) { this.data = data; }
        
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        
        public int getPreviewRows() { return previewRows; }
        public void setPreviewRows(int previewRows) { this.previewRows = previewRows; }
        
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
    }
}