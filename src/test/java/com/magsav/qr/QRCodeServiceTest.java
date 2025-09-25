package com.magsav.qr;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
