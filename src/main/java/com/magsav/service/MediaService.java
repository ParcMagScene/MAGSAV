package com.magsav.service;

import com.magsav.db.DB;
import com.magsav.util.NameNormalizer;
import com.magsav.util.AppLogger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service avancé pour la gestion des médias avec cache et validation
 */
public final class MediaService {

  public enum MediaType { 
    PHOTO_PRODUIT("photos"), 
    LOGO_FABRICANT("logos"),
    QR_CODE("qrcodes");
    
    private final String directory;
    
    MediaType(String directory) {
      this.directory = directory;
    }
    
    public String getDirectory() {
      return directory;
    }
  }

  // Cache des chemins de fichiers pour éviter les accès disque répétés
  private static final Map<String, Optional<Path>> pathCache = new ConcurrentHashMap<>();
  
  // Extensions d'images supportées
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
    "jpg", "jpeg", "png", "gif", "bmp", "webp"
  );
  
  // Taille maximale des fichiers (10MB)
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  public static Path baseDir() throws IOException {
    Path base = DB.dataDir();
    Files.createDirectories(base);
    return base;
  }

  public static Path photosDir() throws IOException {
    Path p = baseDir().resolve("photos");
    Files.createDirectories(p);
    return p;
  }

  public static Path logosDir() throws IOException {
    Path p = baseDir().resolve("logos");
    Files.createDirectories(p);
    return p;
  }
  
  public static Path qrCodesDir() throws IOException {
    Path p = baseDir().resolve("qrcodes");
    Files.createDirectories(p);
    return p;
  }
  
  /**
   * Récupère le répertoire pour un type de média
   */
  public static Path getDirectoryForType(MediaType type) throws IOException {
    return switch (type) {
      case PHOTO_PRODUIT -> photosDir();
      case LOGO_FABRICANT -> logosDir();
      case QR_CODE -> qrCodesDir();
    };
  }
  
  /**
   * Valide un fichier média avant import
   */
  public static MediaValidationResult validateMediaFile(Path source) {
    if (source == null) {
      return new MediaValidationResult(false, "Fichier non spécifié");
    }
    
    if (!Files.exists(source)) {
      return new MediaValidationResult(false, "Fichier introuvable: " + source);
    }
    
    if (!Files.isRegularFile(source)) {
      return new MediaValidationResult(false, "Le chemin ne pointe pas vers un fichier: " + source);
    }
    
    // Vérification de l'extension
    String extension = getExt(source.getFileName().toString());
    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
      return new MediaValidationResult(false, 
        "Extension non supportée: " + extension + ". Extensions autorisées: " + SUPPORTED_EXTENSIONS);
    }
    
    // Vérification de la taille
    try {
      long size = Files.size(source);
      if (size > MAX_FILE_SIZE) {
        return new MediaValidationResult(false, 
          "Fichier trop volumineux: " + (size / 1024 / 1024) + "MB. Maximum autorisé: " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
      }
      
      if (size == 0) {
        return new MediaValidationResult(false, "Le fichier est vide");
      }
    } catch (IOException e) {
      return new MediaValidationResult(false, "Impossible de lire la taille du fichier: " + e.getMessage());
    }
    
    return new MediaValidationResult(true, "Fichier valide");
  }

  // Import d'un fichier vers le bon dossier avec nom normalisé et validation
  public static Path importOne(MediaType type, Path source, String keyName) throws IOException {
    AppLogger.info("service", "MediaService: Import fichier " + source + " vers " + type);
    
    // Validation du fichier
    MediaValidationResult validation = validateMediaFile(source);
    if (!validation.isValid()) {
      throw new IOException("Validation échouée: " + validation.message());
    }
    
    String key = NameNormalizer.normalize(
        (keyName == null || keyName.isBlank())
            ? stripExt(source.getFileName().toString())
            : keyName
    );
    String ext = getExt(source.getFileName().toString());
    
    Path targetDir = getDirectoryForType(type);
    Path target = targetDir.resolve(key + "." + ext);
    
    Files.createDirectories(target.getParent());
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    
    // Invalider le cache pour ce fichier
    invalidateCache(key, type);
    
    AppLogger.info("service", "MediaService: Fichier importé avec succès vers " + target);
    return target;
  }

  // Import en lot avec validation
  public static List<Path> importMany(MediaType type, List<Path> files, String defaultKey) throws IOException {
    AppLogger.info("service", "MediaService: Import en lot de " + files.size() + " fichiers");
    
    List<Path> successful = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    
    for (Path file : files) {
      try {
        Path imported = importOne(type, file, defaultKey);
        successful.add(imported);
      } catch (IOException e) {
        errors.add("Erreur import " + file + ": " + e.getMessage());
        AppLogger.warn("service", "MediaService: Échec import " + file + " - " + e.getMessage());
      }
    }
    
    if (!errors.isEmpty()) {
      AppLogger.warn("service", "MediaService: " + errors.size() + " erreurs lors de l'import en lot");
      // Pour l'instant, on continue même avec des erreurs
      // TODO: Décider si on doit lever une exception ou retourner les erreurs
    }
    
    AppLogger.info("service", "MediaService: Import en lot terminé - " + successful.size() + " réussis, " + errors.size() + " erreurs");
    return successful;
  }
  
  /**
   * Recherche un fichier média avec cache
   */
  public static Optional<Path> findMediaFile(MediaType type, String keyName) {
    if (keyName == null || keyName.trim().isEmpty()) {
      return Optional.empty();
    }
    
    String cacheKey = type + ":" + keyName;
    
    // Vérifier le cache d'abord
    Optional<Path> cached = pathCache.get(cacheKey);
    if (cached != null) {
      return cached;
    }
    
    try {
      Path directory = getDirectoryForType(type);
      
      // Rechercher avec différentes extensions
      for (String ext : SUPPORTED_EXTENSIONS) {
        Path candidate = directory.resolve(keyName + "." + ext);
        if (Files.exists(candidate)) {
          pathCache.put(cacheKey, Optional.of(candidate));
          return Optional.of(candidate);
        }
      }
      
      // Pas trouvé, mettre en cache le résultat négatif
      pathCache.put(cacheKey, Optional.empty());
      return Optional.empty();
      
    } catch (IOException e) {
      AppLogger.warn("service", "MediaService: Erreur recherche fichier " + keyName + " - " + e.getMessage());
      return Optional.empty();
    }
  }
  
  /**
   * Invalide le cache pour un fichier
   */
  public static void invalidateCache(String keyName, MediaType type) {
    String cacheKey = type + ":" + keyName;
    pathCache.remove(cacheKey);
    AppLogger.info("service", "MediaService: Cache invalidé pour " + cacheKey);
  }
  
  /**
   * Vide tout le cache
   */
  public static void clearCache() {
    pathCache.clear();
    AppLogger.info("service", "MediaService: Cache complètement vidé");
  }
  
  /**
   * Statistiques du cache
   */
  public static CacheStatistics getCacheStatistics() {
    long totalEntries = pathCache.size();
    long foundEntries = pathCache.values().stream()
      .mapToLong(opt -> opt.isPresent() ? 1 : 0)
      .sum();
    long notFoundEntries = totalEntries - foundEntries;
    
    return new CacheStatistics(totalEntries, foundEntries, notFoundEntries);
  }

  private static String stripExt(String name) {
    int i = name.lastIndexOf('.');
    return i > 0 ? name.substring(0, i) : name;
  }
  
  private static String getExt(String name) {
    int i = name.lastIndexOf('.');
    return i > 0 ? name.substring(i + 1).toLowerCase() : "png";
  }
  
  /**
   * Record pour les résultats de validation
   */
  public record MediaValidationResult(
    boolean isValid,
    String message
  ) {}
  
  /**
   * Record pour les statistiques du cache
   */
  public record CacheStatistics(
    long totalEntries,
    long foundEntries, 
    long notFoundEntries
  ) {}

  private MediaService() {}
}