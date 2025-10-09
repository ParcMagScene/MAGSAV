package com.magsav.util;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/**
 * Outil de diagnostic pour analyser la structure des fichiers CSV
 */
public class CsvDiagnosticTool {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java CsvDiagnosticTool <fichier.csv>");
            System.exit(1);
        }
        
        Path csvPath = Paths.get(args[0]);
        if (!Files.exists(csvPath)) {
            System.err.println("Fichier non trouvé: " + csvPath);
            System.exit(1);
        }
        
        analyzeCsv(csvPath);
    }
    
    public static void analyzeCsv(Path csvPath) {
        System.out.println("=== DIAGNOSTIC CSV ===");
        System.out.println("Fichier: " + csvPath.getFileName());
        System.out.println();
        
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("❌ Fichier vide");
                return;
            }
            
            // Détection du séparateur
            char separator = detectSeparator(headerLine);
            System.out.println("Séparateur détecté: '" + separator + "'");
            
            // Analyse des en-têtes
            List<String> headers = parseLine(headerLine, separator);
            System.out.println("Nombre de colonnes: " + headers.size());
            System.out.println();
            
            System.out.println("📋 EN-TÊTES:");
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i).trim();
                System.out.printf("  Colonne %d: '%s'%n", i + 1, header);
                
                // Vérifier si c'est une colonne connue
                String normalized = header.toLowerCase().replace("_", "").replace("-", "").replace(" ", "");
                if (normalized.contains("serie") || normalized.contains("serial") || normalized.equals("sn")) {
                    System.out.println("    ✅ Colonne détectée comme numéro de série");
                }
                if (normalized.contains("produit") || normalized.contains("product") || normalized.contains("nom")) {
                    System.out.println("    ✅ Colonne détectée comme produit");
                }
            }
            
            System.out.println();
            System.out.println("📊 EXEMPLES DE DONNÉES:");
            
            // Lire les 3 premières lignes de données
            String line;
            int lineCount = 1;
            while ((line = br.readLine()) != null && lineCount <= 3) {
                if (line.trim().isEmpty()) continue;
                
                List<String> cols = parseLine(line, separator);
                System.out.println("  Ligne " + lineCount + ":");
                
                for (int i = 0; i < Math.min(cols.size(), headers.size()); i++) {
                    String value = cols.get(i).trim();
                    if (value.length() > 30) {
                        value = value.substring(0, 27) + "...";
                    }
                    System.out.printf("    %s: '%s'%n", headers.get(i), value);
                }
                System.out.println();
                lineCount++;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'analyse: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static char detectSeparator(String line) {
        int commas = countOccurrences(line, ',');
        int semicolons = countOccurrences(line, ';');
        int tabs = countOccurrences(line, '\t');
        
        if (semicolons > commas && semicolons > tabs) return ';';
        if (tabs > commas && tabs > semicolons) return '\t';
        return ',';
    }
    
    private static int countOccurrences(String str, char ch) {
        return (int) str.chars().filter(c -> c == ch).count();
    }
    
    private static List<String> parseLine(String line, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == separator && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result;
    }
}