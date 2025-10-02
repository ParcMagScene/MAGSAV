package com.magsav.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Chargement minimaliste d'un fichier YAML clé/valeur très simple (pas de dépendance SnakeYAML pour
 * rester léger). Supporte seulement structure à deux niveaux: section: \n clé: valeur Pour un usage
 * avancé, intégrer SnakeYAML plus tard.
 */
public class Config {
  private final Map<String, String> values = new HashMap<>();

  public static Config load(Path path) throws IOException {
    Config c = new Config();
    if (Files.notExists(path)) {
      return c; // silencieux si absent
    }
    try (InputStream in = Files.newInputStream(path)) {
      String currentPrefix = null;
      for (String line : new String(in.readAllBytes()).split("\n")) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        if (!line.startsWith(" ") && trimmed.endsWith(":")) {
          currentPrefix = trimmed.substring(0, trimmed.length() - 1);
        } else if (line.startsWith("  ") && trimmed.contains(":")) {
          int idx = trimmed.indexOf(":");
          String k = trimmed.substring(0, idx).trim();
          String v = trimmed.substring(idx + 1).trim();
          if (v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
          }
          String fullKey = currentPrefix != null ? currentPrefix + "." + k : k;
          c.values.put(fullKey, v);
        }
      }
    }
    return c;
  }

  public String get(String key, String defaultVal) {
    return values.getOrDefault(key, defaultVal);
  }

  public boolean getBoolean(String key, boolean defaultVal) {
    String v = values.get(key);
    if (v == null) {
      return defaultVal;
    }
    return "true".equalsIgnoreCase(v)
        || "1".equals(v)
        || "yes".equalsIgnoreCase(v)
        || "on".equalsIgnoreCase(v);
  }

  public void set(String key, String value) {
    if (key == null) {
      return;
    }
    if (value == null) {
      values.remove(key);
    } else {
      values.put(key, value);
    }
  }

  public void save(Path path) throws IOException {
    // Réécrire un YAML simple, en regroupant par préfixe avant le '.'
    Map<String, Map<String, String>> grouped = new HashMap<>();
    for (Map.Entry<String, String> e : values.entrySet()) {
      String full = e.getKey();
      String prefix = "root";
      String leaf = full;
      int dot = full.indexOf('.');
      if (dot > 0) {
        prefix = full.substring(0, dot);
        leaf = full.substring(dot + 1);
      }
      grouped.computeIfAbsent(prefix, k -> new HashMap<>()).put(leaf, e.getValue());
    }
    StringBuilder sb = new StringBuilder();
    // Écrire chaque section
    for (String section : grouped.keySet()) {
      if (!"root".equals(section)) {
        sb.append(section).append(":\n");
        for (Map.Entry<String, String> kv : grouped.get(section).entrySet()) {
          sb.append("  ")
              .append(kv.getKey())
              .append(": ")
              .append(formatYamlScalar(kv.getValue()))
              .append("\n");
        }
      } else {
        for (Map.Entry<String, String> kv : grouped.get(section).entrySet()) {
          sb.append(kv.getKey()).append(": ").append(formatYamlScalar(kv.getValue())).append("\n");
        }
      }
    }
    Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
    Files.writeString(path, sb.toString());
  }

  private String formatYamlScalar(String v) {
    if (v == null) {
      return "";
    }
    // Si c'est un booléen ou un nombre simple, pas de guillemets
    if (v.matches("(?i)^(true|false|yes|no|on|off)$") || v.matches("^-?\\d+(\\.\\d+)?$")) {
      return v.toLowerCase();
    }
    // Sinon, entourer de quotes si espaces ou caractères spéciaux
    if (v.contains("#") || v.contains(":") || v.contains(" ")) {
      return '"' + v.replace("\"", "\\\"") + '"';
    }
    return v;
  }
}
