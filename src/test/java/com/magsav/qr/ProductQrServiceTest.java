package com.magsav.qr;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProductQrServiceTest {
  @Test
  void buildQrContent_ok() {
    ProductQrService svc = new ProductQrService(new QRCodeService());
    String content = svc.buildQrContent("ABC123", "SN001");
    assertThat(content).isEqualTo("MAGSAV:PROD:ABC123:SN001");
  }

  @Test
  void generateQrPng_bytes() throws Exception {
    ProductQrService svc = new ProductQrService(new QRCodeService());
    byte[] png = svc.generateQrPng("CODE", "SN");
    assertThat(png).isNotNull();
    assertThat(png.length).isGreaterThan(100);
    // Signature PNG
    assertThat(png[0]).isEqualTo((byte) 0x89);
    assertThat(png[1]).isEqualTo((byte) 0x50);
    assertThat(png[2]).isEqualTo((byte) 0x4E);
    assertThat(png[3]).isEqualTo((byte) 0x47);
  }
}
