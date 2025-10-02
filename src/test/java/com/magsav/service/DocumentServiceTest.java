package com.magsav.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magsav.repo.DocumentRepository;
import java.nio.file.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;

class DocumentServiceTest {

  private Path tempRoot;

  @BeforeEach
  void setup() throws Exception {
    tempRoot = Files.createTempDirectory("magsav-docs-test-");
  }

  @AfterEach
  void teardown() throws Exception {
    if (tempRoot != null) {
      try (var s = Files.walk(tempRoot)) {
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
  void importAndIndex_normalizesAndCopies() throws Exception {
    DocumentRepository repo = mock(DocumentRepository.class);
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    DocumentService svc = new DocumentService(repo, tempRoot);

    Path src = Files.createTempFile("src-", ".pdf");
    Files.writeString(src, "PDF");

    Path target =
        svc.importAndIndex(
            src,
            DocumentService.DocType.FACT,
            "ACME",
            LocalDate.of(2024, 10, 23),
            null,
            null,
            null,
            null,
            null);

    assertThat(target).exists();
    assertThat(target.getFileName().toString()).matches("N-\\(FACT\\)-ACME-20241023\\.pdf");
    assertThat(Files.readString(target)).isEqualTo("PDF");

    // second import with same name should suffix
    Path target2 =
        svc.importAndIndex(
            src,
            DocumentService.DocType.FACT,
            "ACME",
            LocalDate.of(2024, 10, 23),
            null,
            null,
            null,
            null,
            null);
    assertThat(target2.getFileName().toString()).matches("N-\\(FACT\\)-ACME-20241023-1\\.pdf");
  }

  @Test
  void importAndIndex_detectsSupplierAndDate() throws Exception {
    DocumentRepository repo = mock(DocumentRepository.class);
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    DocumentService svc = new DocumentService(repo, tempRoot);
    Path src = Files.createTempFile("ACME-2023-06-05-", ".png");
    Files.writeString(src, "IMG");
    Path target =
        svc.importAndIndex(
            src, DocumentService.DocType.RMA, null, null, null, null, null, null, null);

    assertThat(target.getFileName().toString()).startsWith("N-(RMA)-ACME-20230605");
    assertThat(Files.readString(target)).isEqualTo("IMG");
  }
}
