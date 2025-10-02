package com.magsav.web.controller;

import com.magsav.repo.ProductRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductController {
  private final ProductRepository productRepo;
  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

  @Autowired
  public ProductController(DataSource dataSource) {
    this.productRepo = new ProductRepository(dataSource);
  }

  @GetMapping("/product/{sn}/photo")
  public ResponseEntity<Resource> getPhoto(@PathVariable("sn") String numeroSerie)
      throws Exception {
    String path = productRepo.findPhotoPathByNumeroSerie(numeroSerie);
    if (path == null || path.isBlank()) {
      return placeholder();
    }
    Path p = Path.of(path);
    if (!Files.exists(p)) {
      return placeholder();
    }
    String mime = Files.probeContentType(p);
    if (mime == null) {
      mime = MediaType.IMAGE_PNG_VALUE;
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, public")
        .contentType(MediaType.parseMediaType(mime))
        .body(new FileSystemResource(p));
  }

  @PostMapping(value = "/product/{sn}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String uploadPhoto(
      @PathVariable("sn") String numeroSerie, @RequestParam("file") MultipartFile file)
      throws Exception {
    if (file == null || file.isEmpty()) {
      return redirectError("Fichier invalide");
    }

    // Taille maximale
    if (file.getSize() > MAX_FILE_SIZE) {
      return redirectError("Fichier trop volumineux (max 5MB)");
    }

    // Lire les octets pour validation de signature
    byte[] bytes = file.getBytes();
    if (bytes.length == 0) {
      return redirectError("Fichier vide");
    }

    // Détecter le type d'image par signature
    ImageType type = detectImageType(bytes);
    if (type == ImageType.UNKNOWN) {
      return redirectError("Type de fichier non autorisé (PNG ou JPEG uniquement)");
    }

    // Créer le dossier destination
    Path photosDir = Path.of("photos");
    Files.createDirectories(photosDir);

    // Chemin de sortie avec extension normalisée
    String safeSn = numeroSerie.replaceAll("[^A-Za-z0-9_-]", "_");
    String ext = type == ImageType.PNG ? "png" : "jpg";
    Path dest = photosDir.resolve("prod-" + safeSn + "." + ext);

    // Écrire le fichier validé
    Files.write(dest, bytes);
    productRepo.updatePhotoPath(numeroSerie, dest.toAbsolutePath().toString());
    return "redirect:/?view=products&success=Photo%20import%C3%A9e";
  }

  private String redirectError(String message) {
    String encoded =
        message
            .replace(" ", "%20")
            .replace("é", "%C3%A9")
            .replace("è", "%C3%A8")
            .replace("ê", "%C3%AA")
            .replace("à", "%C3%A0")
            .replace("û", "%C3%BB")
            .replace("ù", "%C3%B9")
            .replace("ô", "%C3%B4")
            .replace("î", "%C3%AE")
            .replace("ï", "%C3%AF")
            .replace("ç", "%C3%A7");
    return "redirect:/?view=products&error=" + encoded;
  }

  private enum ImageType {
    PNG,
    JPEG,
    UNKNOWN
  }

  private ImageType detectImageType(byte[] bytes) {
    if (bytes.length >= 8) {
      // PNG signature: 89 50 4E 47 0D 0A 1A 0A
      int[] sig = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
      boolean png = true;
      for (int i = 0; i < sig.length; i++) {
        if ((bytes[i] & 0xFF) != sig[i]) {
          png = false;
          break;
        }
      }
      if (png) {
        return ImageType.PNG;
      }
    }
    if (bytes.length >= 3) {
      // JPEG SOI: FF D8 FF
      if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
        return ImageType.JPEG;
      }
    }
    return ImageType.UNKNOWN;
  }

  private ResponseEntity<Resource> placeholder() throws IOException {
    // 1x1 transparent PNG
    byte[] png =
        new byte[] {
          (byte) 0x89,
          (byte) 0x50,
          (byte) 0x4E,
          (byte) 0x47,
          (byte) 0x0D,
          (byte) 0x0A,
          (byte) 0x1A,
          (byte) 0x0A,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x0D,
          (byte) 0x49,
          (byte) 0x48,
          (byte) 0x44,
          (byte) 0x52,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x01,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x01,
          (byte) 0x08,
          (byte) 0x06,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x1F,
          (byte) 0x15,
          (byte) 0xC4,
          (byte) 0x89,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x0A,
          (byte) 0x49,
          (byte) 0x44,
          (byte) 0x41,
          (byte) 0x54,
          (byte) 0x78,
          (byte) 0x9C,
          (byte) 0x63,
          (byte) 0x00,
          (byte) 0x01,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x05,
          (byte) 0x00,
          (byte) 0x01,
          (byte) 0x0D,
          (byte) 0x0A,
          (byte) 0x2D,
          (byte) 0xB4,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x00,
          (byte) 0x49,
          (byte) 0x45,
          (byte) 0x4E,
          (byte) 0x44,
          (byte) 0xAE,
          (byte) 0x42,
          (byte) 0x60,
          (byte) 0x82
        };
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "max-age=60, public")
        .contentType(MediaType.IMAGE_PNG)
        .body(new ByteArrayResource(png));
  }
}
