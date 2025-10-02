package com.magsav.service;

import com.magsav.label.LabelService;
import com.magsav.qr.QRCodeService;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrintService {
  public record PrintResult(Path qrPng, Path pdf) {}

  private final LabelService labelService;
  private final QRCodeService qrService;

  public PrintService() {
    this(new LabelService(), new QRCodeService());
  }

  public PrintService(LabelService labelService, QRCodeService qrService) {
    this.labelService = labelService;
    this.qrService = qrService;
  }

  /**
   * Génère un QR (ou un QR vierge si données absentes) et une étiquette PDF correspondante.
   *
   * @param title Texte multi-lignes de l'étiquette
   * @param qrData Données du QR; si null/vide, un QR vierge minimal est utilisé
   * @param logoOrNull Logo optionnel
   * @param outDir Dossier de sortie (créé si nécessaire)
   */
  public PrintResult generateLabel(String title, String qrData, Path logoOrNull, Path outDir)
      throws Exception {
    Files.createDirectories(outDir);
    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

    Path qrPng;
    if (qrData != null && !qrData.isBlank()) {
      qrPng = outDir.resolve("qr-" + ts + ".png");
      qrService.generateToFile(qrData, 256, qrPng);
    } else {
      qrPng = outDir.resolve("qr-blank.png");
      if (!Files.exists(qrPng)) {
        qrService.generateToFile(" ", 64, qrPng);
      }
    }

    Path pdf = outDir.resolve("etiquette-" + ts + ".pdf");
    labelService.createLabel(pdf, title, qrPng, logoOrNull);
    return new PrintResult(qrPng, pdf);
  }
}
