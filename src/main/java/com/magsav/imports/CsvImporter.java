package com.magsav.imports;

import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.IdService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvImporter {
  private final ProductRepository productRepo;
  private final InterventionRepository interRepo;
  private final SocieteRepository socRepo;

  public CsvImporter(ProductRepository productRepo, InterventionRepository interRepo, SocieteRepository socRepo) {
    this.productRepo = productRepo;
    this.interRepo = interRepo;
    this.socRepo = socRepo;
  }

  public static record Result(int rows, int interventions, int products, List<String> errors, boolean dryRun) {}
  
  public Result importFile(Path path) throws IOException { 
    return importFile(path, false); 
  }
  
  public Result importFile(Path path, boolean dryRun) throws IOException {
    int rows = 0, interCreated = 0, prodCreated = 0;
    List<String> errors = new ArrayList<>();

    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String headerLine = br.readLine();
      if (headerLine == null) throw new IOException("Fichier vide");
      
      char sep = detectSeparator(headerLine);
      List<String> headers = parseLine(headerLine, sep);
      Map<String, Integer> idx = mapHeaders(headers);
      
      if (!idx.containsKey("produit") && !idx.containsKey("no_de_serie")) {
        throw new IOException("Le CSV doit contenir au moins une colonne 'produit' ou 'no_de_serie'");
      }

      String line;
      while ((line = br.readLine()) != null) {
        rows++;
        if (line.trim().isEmpty()) continue;
        List<String> cols = parseLine(line, sep);

        try {
          String produit = get(cols, idx, "produit").trim();
          String sn = get(cols, idx, "no_de_serie").trim();
          String proprietaire = get(cols, idx, "proprietaire").trim();
          String panne = get(cols, idx, "panne").trim();
          String statutRaw = opt(cols, idx, "status", "Ouverte").trim();
          String statut = mapInterventionStatus(statutRaw);
          String situationRaw = opt(cols, idx, "situation", "").trim();
          String situation = mapProductSituation(situationRaw);
          String detecteur = opt(cols, idx, "detecteur", "").trim();
          String dateEntreeRaw = opt(cols, idx, "date_entree", "").trim();
          String dateSortieRaw = opt(cols, idx, "date_sortie", "").trim();
          String suiviNo = opt(cols, idx, "no_suivi", "").trim();

          if (produit.isEmpty() && sn.isEmpty()) {
            errors.add("Ligne " + rows + ": PRODUIT ou N° de série manquant");
            continue;
          }

          String uid = IdService.generateUniqueUid(productRepo);

          Long productId = null;
          if (!sn.isEmpty()) {
            var bySn = productRepo.findBySN(sn);
            if (bySn.isPresent()) {
              productId = bySn.get().id();
            } else {
              if (!dryRun) {
                productId = productRepo.insert(null, produit.isEmpty() ? "(inconnu)" : produit, sn, null, uid, situation);
              } else {
                productId = -1L;
              }
              prodCreated++;
            }
          } else {
            if (!dryRun) {
              productId = productRepo.insert(null, produit, "", null, uid, situation);
            } else {
              productId = -1L;
            }
            prodCreated++;
          }

          String ownerType;
          Long ownerSocieteId = null;
          String pnorm = normalize(proprietaire);
          if (pnorm.isEmpty() || pnorm.equals("mag_scene") || pnorm.equals("magscene")) {
            ownerType = "MAG_SCENE";
          } else if (pnorm.equals("particulier")) {
            ownerType = "PARTICULIER";
          } else {
            ownerType = "SOCIETE";
            var found = socRepo.findByNameAndType(proprietaire, "CLIENT");
            if (found.isPresent()) {
              ownerSocieteId = found.get().id();
            } else {
              if (!dryRun) {
                ownerSocieteId = socRepo.insert("CLIENT", proprietaire, null, null, null, null);
              } else {
                ownerSocieteId = -1L;
              }
            }
          }

          String dateEntree = parseDate(dateEntreeRaw);
          String dateSortie = parseDate(dateSortieRaw);

          if (!dryRun) {
            interRepo.insertFromImport(productId, statut, panne, detecteur, dateEntree, dateSortie, suiviNo, ownerType, ownerSocieteId);
          }
          interCreated++;

        } catch (Exception ex) {
          errors.add("Ligne " + rows + ": " + ex.getMessage());
        }
      }
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Erreur import: " + e.getMessage(), e);
    }

    return new Result(rows, interCreated, prodCreated, errors, dryRun);
  }

  private static String normalize(String s) {
    if (s == null) return "";
    String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    n = n.toLowerCase(Locale.ROOT).trim();
    n = n.replaceAll("[^a-z0-9]+", "_");
    return n.replaceAll("^_+|_+$", "");
  }

  private static char detectSeparator(String line) {
    int sc = line.split(";", -1).length;
    int cc = line.split(",", -1).length;
    return sc >= cc ? ';' : ',';
  }

  private static String mapInterventionStatus(String s) {
    String n = normalize(s);
    if (n.isEmpty()) return "Ouverte";
    if (n.matches("(ouverte|ouvert|open|encours|en_cours|nouveau|pending|progress)")) return "Ouverte";
    if (n.matches("(fermee|ferme|closed|close|cloture|cloturee)")) return "Fermée";
    return "Ouverte";
  }

  private static String mapProductSituation(String s) {
    if (s == null || s.isBlank()) return null;
    String n = normalize(s);
    return switch (n) {
      case "en_stock", "stock" -> "En stock";
      case "sav_mag", "sav_interne", "atelier", "atelier_mag" -> "SAV Mag";
      case "sav_externe", "savext", "externe" -> "SAV Externe";
      case "vendu", "vendue", "sold" -> "Vendu";
      case "dechet", "poubelle", "scrap" -> "Déchet";
      default -> "En stock";
    };
  }

  private static Map<String, Integer> mapHeaders(List<String> headers) {
    Map<String, Integer> idx = new HashMap<>();
    for (int i = 0; i < headers.size(); i++) {
      idx.putIfAbsent(headerKey(headers.get(i)), i);
    }
    
    alias(idx, headers, "produit", List.of("product", "nom_produit", "nom", "libelle", "designation"));
    alias(idx, headers, "no_de_serie", List.of("sn", "serial", "n_serie", "numero_serie", "serie"));
    alias(idx, headers, "proprietaire", List.of("owner", "client", "societe"));
    alias(idx, headers, "panne", List.of("defaut", "probleme", "description", "desc"));
    alias(idx, headers, "no_suivi", List.of("n_suivi", "tracking", "tracking_no", "suivi"));
    alias(idx, headers, "status", List.of("statut", "etat_intervention", "state"));
    alias(idx, headers, "situation", List.of("etat", "state", "status_produit", "statut_produit"));
    alias(idx, headers, "detecteur", List.of("detector", "technicien", "diagnostic_par"));
    alias(idx, headers, "date_entree", List.of("entree", "date_debut", "debut", "entry_date"));
    alias(idx, headers, "date_sortie", List.of("sortie", "date_fin", "fin", "exit_date"));
    
    return idx;
  }

  private static void alias(Map<String, Integer> idx, List<String> headers, String canonical, List<String> alts) {
    if (idx.containsKey(canonical)) return;
    for (int i = 0; i < headers.size(); i++) {
      String k = headerKey(headers.get(i));
      if (alts.contains(k)) { 
        idx.put(canonical, i); 
        return; 
      }
    }
  }

  private static String headerKey(String h) { 
    return normalize(h).replaceAll("^n_o?_", "no_"); 
  }

  private static String get(List<String> cols, Map<String, Integer> idx, String key) {
    Integer i = idx.get(key);
    if (i == null || i < 0 || i >= cols.size()) return "";
    return cols.get(i);
  }

  private static String opt(List<String> cols, Map<String, Integer> idx, String key, String def) {
    String v = get(cols, idx, key);
    return v == null || v.isEmpty() ? def : v;
  }

  private static String parseDate(String s) {
    if (s == null || s.isBlank()) return null;
    s = s.trim();
    List<DateTimeFormatter> fmts = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/uuuu"),
        DateTimeFormatter.ofPattern("d/M/uuuu"),
        DateTimeFormatter.ofPattern("dd-MM-uuuu"),
        DateTimeFormatter.ofPattern("d-M-uuuu")
    );
    for (var f : fmts) {
      try { 
        return LocalDate.parse(s, f).toString(); 
      } catch (DateTimeParseException ignored) {}
    }
    try { 
      return LocalDate.parse(s.substring(0, 10)).toString(); 
    } catch (Exception e) { 
      return null; 
    }
  }

  private static List<String> parseLine(String line, char sep) {
    List<String> out = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { 
          sb.append('"'); 
          i++; 
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == sep && !inQuotes) {
        out.add(sb.toString().trim()); 
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    out.add(sb.toString().trim());
    return out;
  }
}
