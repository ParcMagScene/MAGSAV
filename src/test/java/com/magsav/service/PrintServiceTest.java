package com.magsav.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magsav.label.LabelService;
import com.magsav.qr.QRCodeService;
import java.nio.file.*;
import org.junit.jupiter.api.*;

class PrintServiceTest {

  private Path out;

  @BeforeEach
  void setup() throws Exception {
    out = Files.createTempDirectory("magsav-print-test-");
  }

  @AfterEach
  void teardown() throws Exception {
    if (out != null) {
      try (var s = Files.walk(out)) {
        s.sorted((a, b) -> b.getNameCount() - a.getNameCount())
            .forEach(
                p -> {
                  try {
                    Files.deleteIfExists(p);
                  } catch (Exception ignored) {
                  }
                });
      }
    }
  }

  @Test
  void generate_withQrData_createsQrAndPdf() throws Exception {
    LabelService label = spy(new LabelService());
    QRCodeService qr = spy(new QRCodeService());
    PrintService svc = new PrintService(label, qr);

    var res = svc.generateLabel("Titre", "DATA", null, out);
    assertThat(res.qrPng()).exists();
    assertThat(res.pdf()).exists();
  }

  @Test
  void generate_withoutQrData_usesBlankQr() throws Exception {
    LabelService label = spy(new LabelService());
    QRCodeService qr = spy(new QRCodeService());
    PrintService svc = new PrintService(label, qr);

    var res = svc.generateLabel("Titre", "", null, out);
    assertThat(res.qrPng().getFileName().toString()).startsWith("qr-");
    assertThat(res.pdf()).exists();
  }
}
