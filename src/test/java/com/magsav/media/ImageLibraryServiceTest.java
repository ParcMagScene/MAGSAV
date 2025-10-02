package com.magsav.media;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

class ImageLibraryServiceTest {

  private Path tempBase;

  @BeforeEach
  void setup() throws IOException {
    tempBase = Files.createTempDirectory("magsav-photos-test-");
    System.setProperty("magsav.photos.dir", tempBase.toString());
  }

  @AfterEach
  void teardown() throws IOException {
    System.clearProperty("magsav.photos.dir");
    if (tempBase != null && Files.exists(tempBase)) {
      try (var s = Files.walk(tempBase)) {
        s.sorted((a, b) -> b.getNameCount() - a.getNameCount())
            .forEach(
                p -> {
                  try {
                    Files.deleteIfExists(p);
                  } catch (IOException ignored) {
                  }
                });
      }
    }
  }

  @Test
  void normalizeExt_handlesMissingOrUppercase() {
    assertThat(ImageLibraryService.normalizeExt(null, "png")).isEqualTo("png");
    assertThat(ImageLibraryService.normalizeExt("file", "jpg")).isEqualTo("jpg");
    assertThat(ImageLibraryService.normalizeExt("photo.PNG", "png")).isEqualTo("png");
    assertThat(ImageLibraryService.normalizeExt("photo.jpeg", "png")).isEqualTo("jpeg");
  }

  @Test
  void productPhotoDest_buildsSafePath() {
    Path p1 = ImageLibraryService.productPhotoDest("SMKUP/1759:208588", "JPG");
    assertThat(p1.toString()).contains("products/prod-SMKUP_1759_208588.jpg");

    Path p2 = ImageLibraryService.productPhotoDest(null, "");
    assertThat(p2.toString()).endsWith("products/prod-unknown.png");
  }

  @Test
  void copyToLibrary_createsDirectoriesAndCopies() throws Exception {
    Path temp = Files.createTempFile("imglib-test-", ".png");
    Files.writeString(temp, "data");
    Path dest = ImageLibraryService.productPhotoDest("ABC", "png");

    Path copied = ImageLibraryService.copyToLibrary(temp, dest);
    assertThat(copied).exists();
    assertThat(Files.readString(copied)).isEqualTo("data");
  }
}
