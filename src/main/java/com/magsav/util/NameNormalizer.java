package com.magsav.util;

import java.text.Normalizer;

public final class NameNormalizer {
  public static String normalize(String s) {
    if (s == null) return "";
    String t = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .toLowerCase()
        .trim()
        .replaceAll("[^a-z0-9]+", "_")
        .replaceAll("^_+|_+$", "");
    return t;
  }
  private NameNormalizer() {}
}