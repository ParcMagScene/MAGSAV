package com.magsav.service;

import com.magsav.db.DB;
import com.magsav.util.NameNormalizer;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class MediaService {

  public enum MediaType { PHOTO_PRODUIT, LOGO_FABRICANT }

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

  // Import d’un fichier vers le bon dossier avec nom normalisé
  public static Path importOne(MediaType type, Path source, String keyName) throws IOException {
    if (source == null || !Files.exists(source)) throw new IOException("Fichier introuvable: " + source);
    String key = NameNormalizer.normalize(
        (keyName == null || keyName.isBlank())
            ? stripExt(source.getFileName().toString())
            : keyName
    );
    String ext = getExt(source.getFileName().toString()); // on conserve l’extension
    Path target = switch (type) {
      case PHOTO_PRODUIT -> photosDir().resolve(key + "." + ext);
      case LOGO_FABRICANT -> logosDir().resolve(key + "." + ext);
    };
    Files.createDirectories(target.getParent());
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  // Import en lot: si defaultKey est fourni, il sera appliqué à tous (sinon: nom de fichier)
  public static List<Path> importMany(MediaType type, List<Path> files, String defaultKey) throws IOException {
    List<Path> out = new ArrayList<>();
    for (Path f : files) out.add(importOne(type, f, defaultKey));
    return out;
  }

  private static String stripExt(String name) {
    int i = name.lastIndexOf('.');
    return i > 0 ? name.substring(0, i) : name;
  }
  private static String getExt(String name) {
    int i = name.lastIndexOf('.');
    return i > 0 ? name.substring(i + 1).toLowerCase() : "png";
  }

  private MediaService() {}
}