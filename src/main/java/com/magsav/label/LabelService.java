package com.magsav.label;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LabelService {
    public void createSimpleLabel(Path outputPdf, String title, Path qrPng) throws IOException {
        Files.createDirectories(outputPdf.getParent());
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A6);
            doc.addPage(page);

            PDImageXObject qr = PDImageXObject.createFromFile(qrPng.toString(), doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Gestion multi-lignes
                String[] lines = title.split("\\n");
                float startX = 40f;
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
                float qrSize = 150f;
                cs.drawImage(qr, 40, 40, qrSize, qrSize);
            }

            doc.save(outputPdf.toFile());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
