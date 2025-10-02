package com.magsav.media;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/** Génère des avatars/badges d'initiales sous forme d'image PNG (sans dépendre de JavaFX). */
public class AvatarService {
  /** Calcule 1 à 2 lettres d'initiales à partir d'un nom. */
  public static String computeInitials(String name) {
    if (name == null || name.isBlank()) {
      return "?";
    }
    String[] parts = name.trim().split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length && sb.length() < 2; i++) {
      String p = parts[i];
      if (!p.isBlank()) {
        sb.append(Character.toUpperCase(p.charAt(0)));
      }
    }
    if (sb.length() == 0) {
      sb.append('?');
    }
    return sb.toString();
  }

  /**
   * Rend un badge PNG (arrondi) avec les initiales calculées pour le nom donné. Couleurs par
   * défaut: fond bleu (#4285F4), texte blanc, police SansSerif 14 en gras.
   */
  public byte[] renderInitialsPng(String name, int width, int height) throws Exception {
    String initials = computeInitials(name);
    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setColor(new Color(0x42, 0x85, 0xF4));
    g.fillRoundRect(0, 0, width, height, 8, 8);
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 14));
    FontMetrics fm = g.getFontMetrics();
    int tw = fm.stringWidth(initials);
    int th = fm.getAscent();
    int x = (width - tw) / 2;
    int y = (height + th) / 2 - 3;
    g.drawString(initials, x, y);
    g.dispose();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(bi, "png", baos);
      return baos.toByteArray();
    }
  }
}
