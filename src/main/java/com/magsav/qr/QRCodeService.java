package com.magsav.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class QRCodeService {
  public void generateToFile(String content, int size, Path outputPng) throws Exception {
    Files.createDirectories(outputPng.getParent());
    BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
    MatrixToImageWriter.writeToPath(matrix, "PNG", outputPng);
  }

  /** Génère un QR code PNG en mémoire et retourne les octets PNG. */
  public byte[] generateQRCode(String data) {
    try {
      int size = 256;
      BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
      }
    } catch (Exception e) {
      throw new RuntimeException("Erreur génération QR", e);
    }
  }
}
