package com.magsav.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class MediaPaths {
  public static Path baseDir() {
    String env = System.getenv("MAGSAV_MEDIA_DIR");
    if (env != null && !env.isBlank()) return Paths.get(env);
    return Paths.get(com.magsav.config.AppConfig.getMediaDirectory());
  }
  public static Path photosDir() { return baseDir().resolve("photos"); }
  public static Path logosDir()  { return baseDir().resolve("logos"); }
  private MediaPaths() {}
}