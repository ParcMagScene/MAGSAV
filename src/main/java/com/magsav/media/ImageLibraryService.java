package com.magsav.media;

import java.nio.file.*;

/**
 * Service utilitaire pour gérer la médiathèque d'images (photos produits et logos). Centralise les
 * répertoires et conventions de nommage afin d'éviter la duplication dans les contrôleurs.
 */
public final class ImageLibraryService {
  private ImageLibraryService() {}

  public static Path baseDir() {
    String prop = System.getProperty("magsav.photos.dir");
    if (prop != null && !prop.isBlank()) {
      return Path.of(prop);
    }
    String env = System.getenv("MAGSAV_PHOTOS_DIR");
    if (env != null && !env.isBlank()) {
      return Path.of(env);
    }
    return Path.of("photos");
  }

  public static Path productsDir() {
    return baseDir().resolve("products");
  }

  public static Path companyLogosDir() {
    return baseDir().resolve("logos").resolve("companies");
  }

  public static Path manufacturerLogosDir() {
    return baseDir().resolve("logos").resolve("manufacturers");
  }

  public static void ensureDirs() {
    try {
      Files.createDirectories(productsDir());
      Files.createDirectories(companyLogosDir());
      Files.createDirectories(manufacturerLogosDir());
    } catch (Exception ignored) {
    }
  }

  public static String normalizeExt(String filename, String fallback) {
    if (filename == null) {
      return fallback;
    }
    int dot = filename.lastIndexOf('.');
    String ext =
        dot > 0 && dot < filename.length() - 1 ? filename.substring(dot + 1).toLowerCase() : "";
    if (ext.isBlank()) {
      return fallback;
    }
    return ext;
  }

  public static Path productPhotoDest(String numeroSerie, String ext) {
    String safeSn = numeroSerie == null ? "unknown" : numeroSerie.replaceAll("[^A-Za-z0-9_-]", "_");
    String safeExt = ext == null || ext.isBlank() ? "png" : ext.toLowerCase();
    return productsDir().resolve("prod-" + safeSn + "." + safeExt);
  }

  public static Path copyToLibrary(Path source, Path dest) throws Exception {
    ensureDirs();
    Files.createDirectories(dest.getParent());
    return Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
  }
}
