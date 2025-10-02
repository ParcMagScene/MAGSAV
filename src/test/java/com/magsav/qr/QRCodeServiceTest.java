package com.magsav.qr;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class QRCodeServiceTest {
  @Test
  void generatesPng() throws Exception {
    QRCodeService svc = new QRCodeService();
    Path out = Path.of("build/test-qr.png");
    svc.generateToFile("test", 128, out);
    assertTrue(Files.exists(out));
    assertTrue(Files.size(out) > 0);
  }
}
