package com.magsav.util;

import com.magsav.db.DB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class MediaAudit {

  public static void report() {
    if (!isDebug()) return;
    AppLogger.info("Démarrage de l'audit médias...");
    try (Connection c = DB.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT id, nom, fabricant, uid FROM produits ORDER BY id DESC LIMIT 5000")) {

      int total = 0, okLogo = 0, okPhoto = 0;
      List<String> missLogos = new ArrayList<>();
      List<String> missPhotos = new ArrayList<>();

      while (rs.next()) {
        total++;
        long id = rs.getLong("id");
        String nom = rs.getString("nom");
        String fabricant = rs.getString("fabricant");
        String uid = rs.getString("uid");

        boolean hasLogo = existsLogo(fabricant);
        boolean hasPhoto = existsPhoto(uid);

        if (hasLogo) okLogo++; else if (missLogos.size() < 15) missLogos.add("#" + id + " " + safe(nom) + " | fab=" + safe(fabricant));
        if (hasPhoto) okPhoto++; else if (missPhotos.size() < 15) missPhotos.add("#" + id + " " + safe(nom) + " | uid=" + safe(uid));
      }

      AppLogger.info("Audit médias - Produits: {}, Logos: {} ok / {} manquants, Photos: {} ok / {} manquantes", 
                    total, okLogo, (total - okLogo), okPhoto, (total - okPhoto));

      if (!missLogos.isEmpty()) {
        AppLogger.warn("Logos manquants (premiers 5): {} - Dossier: {}", 
                      missLogos.subList(0, Math.min(5, missLogos.size())), 
                      MediaPaths.logosDir());
      }
      if (!missPhotos.isEmpty()) {
        AppLogger.warn("Photos manquantes (premières 5): {} - Dossier: {}", 
                      missPhotos.subList(0, Math.min(5, missPhotos.size())), 
                      MediaPaths.photosDir());
      }
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'audit médias", e);
    }
  }

  private static boolean existsLogo(String fabricant) {
    if (fabricant == null || fabricant.isBlank()) return false;
    String base = slug(fabricant);
    return existsAny(MediaPaths.logosDir(), base);
  }

  private static boolean existsPhoto(String uid) {
    if (uid == null || uid.isBlank()) return false;
    String base = uid.trim();
    return existsAny(MediaPaths.photosDir(), base);
  }

  private static boolean existsAny(Path dir, String base) {
    String[] exts = { ".png", ".jpg", ".jpeg" };
    for (String ext : exts) {
      Path p = dir.resolve(base + ext);
      if (Files.exists(p)) return true;
    }
    return false;
  }

  private static String slug(String s) {
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    out = out.replaceAll("^_+|_+$", "");
    return out.isEmpty() ? "unknown" : out;
  }

  private static String safe(String s) { return s == null ? "" : s; }

  private static boolean isDebug() {
    return Boolean.getBoolean("magsav.debug") || "1".equals(System.getenv("MAGSAV_DEBUG"));
  }

  private MediaAudit() {}
}