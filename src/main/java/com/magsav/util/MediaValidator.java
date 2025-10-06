package com.magsav.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public final class MediaValidator {
  public static void ensureDirs() {
    List<Path> dirs = List.of(MediaPaths.photosDir(), MediaPaths.logosDir());
    for (Path d : dirs) {
      try {
        Files.createDirectories(d);
        if (isDebug()) System.out.println("[MAGSAV] Media dir ok: " + d);
      } catch (IOException e) {
        System.out.println("[MAGSAV] Media dir error: " + d + " -> " + e.getMessage());
      }
    }
  }

  private static boolean isDebug() {
    return Boolean.getBoolean("magsav.debug") || "1".equals(System.getenv("MAGSAV_DEBUG"));
  }

  private MediaValidator() {}
}