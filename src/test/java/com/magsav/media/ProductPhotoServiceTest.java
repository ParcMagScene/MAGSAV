package com.magsav.media;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magsav.repo.ProductRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProductPhotoServiceTest {
  private String oldProp;

  @BeforeEach
  void setup() {
    oldProp = System.getProperty("magsav.photos.dir");
  }

  @AfterEach
  void cleanup() {
    if (oldProp == null) {
      System.clearProperty("magsav.photos.dir");
    } else {
      System.setProperty("magsav.photos.dir", oldProp);
    }
  }

  @Test
  void importAndAssignPhoto_bySerialNumber() throws Exception {
    Path tmp = Files.createTempDirectory("magsav-test-photos");
    System.setProperty("magsav.photos.dir", tmp.toString());
    ProductRepository repo = Mockito.mock(ProductRepository.class);
    ProductPhotoService svc = new ProductPhotoService(repo);

    Path src = Files.createTempFile("photo-src-", ".jpg");
    Files.writeString(src, "fake-image-content");

    String saved = svc.importAndAssignPhoto("SN123", null, src);

    assertThat(saved).isNotBlank();
    Path dest = Path.of(saved);
    assertThat(Files.exists(dest)).isTrue();
    verify(repo, times(1)).updatePhotoPath(eq("SN123"), eq(saved));
  }

  @Test
  void importAndAssignPhoto_byProductName() throws Exception {
    Path tmp = Files.createTempDirectory("magsav-test-photos");
    System.setProperty("magsav.photos.dir", tmp.toString());
    ProductRepository repo = Mockito.mock(ProductRepository.class);
    ProductPhotoService svc = new ProductPhotoService(repo);

    Path src = Files.createTempFile("photo-src-", ".png");
    Files.writeString(src, "fake-image-content");

    String saved = svc.importAndAssignPhoto("SN999", "Model X", src);

    assertThat(saved).isNotBlank();
    Path dest = Path.of(saved);
    assertThat(Files.exists(dest)).isTrue();
    verify(repo, times(1)).updatePhotoPathByProduit(eq("Model X"), eq(saved));
    verify(repo, never()).updatePhotoPath(anyString(), anyString());
  }
}
