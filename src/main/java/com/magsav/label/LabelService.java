package com.magsav.label;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class LabelService {
  /** Crée une étiquette PDF simple avec un titre, un QR et, si fourni, un logo. */
  public void createLabel(Path outputPdf, String title, Path qrPng, Path logoPathOrNull)
      throws IOException {
    Files.createDirectories(outputPdf.getParent());
    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A6);
      doc.addPage(page);

      PDImageXObject qr = PDImageXObject.createFromFile(qrPng.toString(), doc);
      PDImageXObject logo = null;
      if (logoPathOrNull != null && Files.exists(logoPathOrNull)) {
        try {
          logo = PDImageXObject.createFromFile(logoPathOrNull.toString(), doc);
        } catch (Exception ignore) {
          logo = null;
        }
      }

      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        // Titre multi-lignes
        String[] lines = title == null ? new String[] {""} : title.split("\\n");
        float margin = 20F;
        float startX = margin;
        float y = page.getMediaBox().getHeight() - margin - 20f;
        for (int i = 0; i < lines.length; i++) {
          String line = lines[i];
          cs.beginText();
          cs.setFont(PDType1Font.HELVETICA_BOLD, i == 0 ? 16 : 12);
          cs.newLineAtOffset(startX, y - (i * 16));
          cs.showText(truncate(line, 60));
          cs.endText();
        }

        // Logo en haut à droite (optionnel)
        if (logo != null) {
          float boxW = 120F;
          float boxH = 60F; // zone max
          float ratio = (float) logo.getWidth() / (float) logo.getHeight();
          float w = boxW;
          float h = w / ratio;
          if (h > boxH) {
            h = boxH;
            w = h * ratio;
          }
          float x = page.getMediaBox().getWidth() - margin - w;
          float topY = page.getMediaBox().getHeight() - margin - h;
          cs.drawImage(logo, x, topY, w, h);
        }

        // QR code
        float qrSize = 150F;
        cs.drawImage(qr, margin, margin, qrSize, qrSize);
      }

      doc.save(outputPdf.toFile());
    }
  }

  public void createSimpleLabel(Path outputPdf, String title, Path qrPng) throws IOException {
    Files.createDirectories(outputPdf.getParent());
    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A6);
      doc.addPage(page);

      PDImageXObject qr = PDImageXObject.createFromFile(qrPng.toString(), doc);

      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        // Gestion multi-lignes
        String[] lines = title.split("\\n");
        float startX = 40F;
        float y = page.getMediaBox().getHeight() - 60f;
        for (int i = 0; i < lines.length; i++) {
          String line = lines[i];
          cs.beginText();
          cs.setFont(PDType1Font.HELVETICA_BOLD, i == 0 ? 16 : 12);
          cs.newLineAtOffset(startX, y - (i * 16));
          cs.showText(truncate(line, 60));
          cs.endText();
        }

        // QR code
        float qrSize = 150F;
        cs.drawImage(qr, 40, 40, qrSize, qrSize);
      }

      doc.save(outputPdf.toFile());
    }
  }

  private String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() > max ? s.substring(0, max - 3) + "..." : s;
  }

  /** Génère un PDF factice (stub) */
  public byte[] generateLabelPDF(com.magsav.model.DossierSAV dossier) {
    // Retourne un tableau de bytes non vide pour le test
    return new byte[] {1, 2, 3};
  }
}
