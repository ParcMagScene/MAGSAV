package com.magsav.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Chargement minimaliste d'un fichier YAML clé/valeur très simple (pas de dépendance SnakeYAML pour rester léger).
 * Supporte seulement structure à deux niveaux: section: \n  clé: valeur
 * Pour un usage avancé, intégrer SnakeYAML plus tard.
 */
public class Config {
    private final Map<String, String> values = new HashMap<>();

    public static Config load(Path path) throws IOException {
        Config c = new Config();
        if (Files.notExists(path)) return c; // silencieux si absent
        try (InputStream in = Files.newInputStream(path)) {
            String currentPrefix = null;
            for (String line : new String(in.readAllBytes()).split("\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
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
}
