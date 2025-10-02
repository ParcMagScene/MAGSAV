package com.magsav.media;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AvatarServiceTest {
  @Test
  void computeInitials_basic() {
    AvatarService svc = new AvatarService();
    assertThat(svc.computeInitials("Acme Corporation")).isEqualTo("AC");
    assertThat(svc.computeInitials("  one ")).isEqualTo("O");
    assertThat(svc.computeInitials(null)).isEqualTo("?");
    assertThat(svc.computeInitials("   ")).isEqualTo("?");
  }

  @Test
  void renderInitialsPng_returnsBytes() throws Exception {
    AvatarService svc = new AvatarService();
    byte[] png = svc.renderInitialsPng("Acme", 80, 24);
    assertThat(png).isNotNull();
    assertThat(png.length).isGreaterThan(100); // devrait contenir une petite image PNG
    // Signature PNG
    assertThat(png[0]).isEqualTo((byte) 0x89);
    assertThat(png[1]).isEqualTo((byte) 0x50);
    assertThat(png[2]).isEqualTo((byte) 0x4E);
    assertThat(png[3]).isEqualTo((byte) 0x47);
  }
}
