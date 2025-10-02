package com.magsav.service;

import com.magsav.model.DocumentEntry;
import com.magsav.repo.DocumentRepository;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentService {
  private final DocumentRepository repo;
  private final Path root;

  public enum DocType {
    DEV,
    FACT,
    RMA,
    BL,
    BC
  }

  public DocumentService(DocumentRepository repo, Path root) throws IOException {
    this.repo = repo;
    this.root = root;
    for (DocType t : DocType.values()) {
      Files.createDirectories(root.resolve(folderFor(t)));
    }
  }

  public Path importAndIndex(
      Path sourceFile,
      DocType type,
      String supplierOpt,
      LocalDate dateOpt,
      String linkedCode,
      String linkedSn,
      Long linkedDossierId,
      Long linkedRfqId,
      Long linkedRmaId)
      throws Exception {
    String originalName = sourceFile.getFileName().toString();
    String supplier =
        supplierOpt != null && !supplierOpt.isBlank()
            ? supplierOpt
            : detectSupplier(originalName);
    LocalDate date = dateOpt != null ? dateOpt : detectDate(originalName);
    if (supplier == null || supplier.isBlank()) {
      supplier = "Inconnu";
    }
    if (date == null) {
      date = LocalDate.now();
    }
    String normalized = normalizeName(type, supplier, date, originalName);
    Path targetDir = root.resolve(folderFor(type));
    Path target = uniqueTarget(targetDir.resolve(normalized));
    Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
    repo.save(
        new DocumentEntry(
            null,
            type.name(),
            originalName,
            target.getFileName().toString(),
            root.relativize(target).toString(),
            linkedCode,
            linkedSn,
            linkedDossierId,
            linkedRfqId,
            linkedRmaId,
            LocalDateTime.now()));
    return target;
  }

  private static String folderFor(DocType type) {
    return switch (type) {
      case DEV -> "Devis";
      case FACT -> "Factures";
      case RMA -> "RMA";
      case BL -> "BL";
      case BC -> "BC";
    };
  }

  private static String normalizeName(
      DocType type, String supplier, LocalDate date, String original) {
    String base =
        "N-("
            + type.name()
            + ")-"
            + sanitize(supplier)
            + "-"
            + date.format(DateTimeFormatter.BASIC_ISO_DATE);
    String ext = ext(original);
    return base + (ext.isEmpty() ? "" : "." + ext);
  }

  private static String ext(String name) {
    int idx = name.lastIndexOf('.');
    return idx < 0 ? "" : name.substring(idx + 1);
  }

  private static String sanitize(String s) {
    return s.replaceAll("[^A-Za-z0-9._-]", "_");
  }

  private static Path uniqueTarget(Path desired) throws IOException {
    if (!Files.exists(desired)) {
      return desired;
    }
    String fn = desired.getFileName().toString();
    String name = fn;
    String ext = "";
    int i = fn.lastIndexOf('.');
    if (i > 0) {
      name = fn.substring(0, i);
      ext = fn.substring(i);
    }
    int n = 1;
    Path dir = desired.getParent();
    Path cand;
    do {
      cand = dir.resolve(name + "-" + n + ext);
      n++;
    } while (Files.exists(cand));
    return cand;
  }

  private static final Pattern SUPPLIER_PATTERN = Pattern.compile("([A-Z][A-Z0-9]{2,})");

  private static String detectSupplier(String name) {
    Matcher m = SUPPLIER_PATTERN.matcher(name.toUpperCase(Locale.ROOT));
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  private static final Pattern DATE_YMD = Pattern.compile("(20\\d{2})(\\d{2})(\\d{2})");
  private static final Pattern DATE_YMD_DASH = Pattern.compile("(20\\d{2})-(\\d{2})-(\\d{2})");
  private static final Pattern DATE_DMY_DASH = Pattern.compile("(\\d{2})-(\\d{2})-(20\\d{2})");
  private static final Pattern DATE_DMY = Pattern.compile("(\\d{2})(\\d{2})(20\\d{2})");

  private static LocalDate detectDate(String name) {
    String s = name;
    Matcher m;
    m = DATE_YMD.matcher(s);
    if (m.find()) {
      return toDate(m.group(1), m.group(2), m.group(3), true);
    }
    m = DATE_YMD_DASH.matcher(s);
    if (m.find()) {
      return toDate(m.group(1), m.group(2), m.group(3), true);
    }
    m = DATE_DMY_DASH.matcher(s);
    if (m.find()) {
      return toDate(m.group(3), m.group(2), m.group(1), true);
    }
    m = DATE_DMY.matcher(s);
    if (m.find()) {
      return toDate(m.group(3), m.group(2), m.group(1), true);
    }
    return null;
  }

  private static LocalDate toDate(String y, String m, String d, boolean strict) {
    try {
      return LocalDate.of(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d));
    } catch (Exception e) {
      return null;
    }
  }
}
