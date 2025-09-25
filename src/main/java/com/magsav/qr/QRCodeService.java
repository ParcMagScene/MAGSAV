package com.magsav.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QRCodeService {
    public void generateToFile(String content, int size, Path outputPng) throws Exception {
        Files.createDirectories(outputPng.getParent());
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
        MatrixToImageWriter.writeToPath(matrix, "PNG", outputPng);
    }
}
