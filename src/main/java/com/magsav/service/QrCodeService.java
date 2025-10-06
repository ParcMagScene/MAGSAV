package com.magsav.service;

import com.magsav.db.DB;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.*;
import java.util.Map;

public final class QrCodeService {
  public static Path storageDir() {
    return DB.dataDir().resolve("qrcodes");
  }

  public static Path ensureQrPng(String uid) throws Exception {
    if (uid == null || uid.isBlank()) throw new IllegalArgumentException("UID manquant");
    Path dir = storageDir();
    Files.createDirectories(dir);
    Path out = dir.resolve(uid + ".png");
    if (Files.exists(out)) return out;

    String url = "https://app.magsav.local/p/" + uid; // placeholder web (à implémenter)
    Map<EncodeHintType,Object> hints = Map.of(EncodeHintType.MARGIN, 1);
    BitMatrix m = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 280, 280, hints);
    MatrixToImageWriter.writeToPath(m, "PNG", out);
    return out;
  }

  private QrCodeService() {}
}