package com.magscene.magsav.backend.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Utilitaire pour importer les données depuis LOCMAT_Materiel.xlsx
 */
@Component
public class LocmatExcelImporter {
    
    public static class EquipmentData {
        public String reference;
        public String name;
        public String brand;
        public String model;
        public String category;
        public String serialNumber;
        public LocalDate purchaseDate;
        public BigDecimal purchasePrice;
        public String supplier;
        public String location;
        public String status;
        public String notes;
        
        @Override
        public String toString() {
            return String.format("%s - %s %s (Ref: %s)", name, brand, model, reference);
        }
    }
    
    /**
     * Importe le fichier Excel LOCMAT_Materiel.xlsx
     * 
     * @param filePath Chemin vers le fichier Excel
     * @return Liste des équipements importés
     * @throws IOException Si erreur de lecture du fichier
     */
    public List<EquipmentData> importFromExcel(String filePath) throws IOException {
        List<EquipmentData> equipmentList = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Lire la première ligne pour détecter les colonnes
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = buildColumnMap(headerRow);
            
            // Parcourir les lignes de données (à partir de la ligne 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                EquipmentData equipment = parseEquipmentRow(row, columnMap);
                if (equipment != null && equipment.reference != null) {
                    equipmentList.add(equipment);
                }
            }
        }
        
        return equipmentList;
    }
    
    /**
     * Construit une map des colonnes depuis l'en-tête
     */
    private Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        
        for (Cell cell : headerRow) {
            String header = getCellValueAsString(cell).toLowerCase().trim();
            
            // Mapping des colonnes possibles
            if (header.contains("ref") || header.equals("reference")) {
                map.put("reference", cell.getColumnIndex());
            } else if (header.contains("nom") || header.contains("désignation") || header.contains("designation")) {
                map.put("name", cell.getColumnIndex());
            } else if (header.contains("marque") || header.contains("brand")) {
                map.put("brand", cell.getColumnIndex());
            } else if (header.contains("modèle") || header.contains("modele") || header.contains("model")) {
                map.put("model", cell.getColumnIndex());
            } else if (header.contains("catégorie") || header.contains("categorie") || header.contains("category")) {
                map.put("category", cell.getColumnIndex());
            } else if (header.contains("série") || header.contains("serie") || header.contains("serial")) {
                map.put("serialNumber", cell.getColumnIndex());
            } else if (header.contains("achat") || header.contains("purchase")) {
                if (header.contains("date")) {
                    map.put("purchaseDate", cell.getColumnIndex());
                } else if (header.contains("prix") || header.contains("price")) {
                    map.put("purchasePrice", cell.getColumnIndex());
                }
            } else if (header.contains("fournisseur") || header.contains("supplier")) {
                map.put("supplier", cell.getColumnIndex());
            } else if (header.contains("emplacement") || header.contains("location")) {
                map.put("location", cell.getColumnIndex());
            } else if (header.contains("statut") || header.contains("état") || header.contains("status")) {
                map.put("status", cell.getColumnIndex());
            } else if (header.contains("notes") || header.contains("remarques") || header.contains("commentaire")) {
                map.put("notes", cell.getColumnIndex());
            }
        }
        
        return map;
    }
    
    /**
     * Parse une ligne de données en EquipmentData
     */
    private EquipmentData parseEquipmentRow(Row row, Map<String, Integer> columnMap) {
        EquipmentData equipment = new EquipmentData();
        
        equipment.reference = getCellValue(row, columnMap.get("reference"));
        equipment.name = getCellValue(row, columnMap.get("name"));
        equipment.brand = getCellValue(row, columnMap.get("brand"));
        equipment.model = getCellValue(row, columnMap.get("model"));
        equipment.category = getCellValue(row, columnMap.get("category"));
        equipment.serialNumber = getCellValue(row, columnMap.get("serialNumber"));
        equipment.purchaseDate = getCellValueAsDate(row, columnMap.get("purchaseDate"));
        equipment.purchasePrice = getCellValueAsBigDecimal(row, columnMap.get("purchasePrice"));
        equipment.supplier = getCellValue(row, columnMap.get("supplier"));
        equipment.location = getCellValue(row, columnMap.get("location"));
        equipment.status = getCellValue(row, columnMap.get("status"));
        equipment.notes = getCellValue(row, columnMap.get("notes"));
        
        return equipment;
    }
    
    private String getCellValue(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        return cell != null ? getCellValueAsString(cell) : null;
    }
    
    private LocalDate getCellValueAsDate(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {
            // Ignorer les erreurs de parsing
        }
        return null;
    }
    
    private BigDecimal getCellValueAsBigDecimal(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().replaceAll("[^0-9.,]", "");
                return new BigDecimal(value.replace(',', '.'));
            }
        } catch (Exception e) {
            // Ignorer les erreurs de parsing
        }
        return null;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
