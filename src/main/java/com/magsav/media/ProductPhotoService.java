package com.magsav.media;

import com.magsav.repo.ProductRepository;
import java.nio.file.Path;

/** Service centralisant l'import et l'association de photos produit. */
public class ProductPhotoService {
  private final ProductRepository productRepository;

  public ProductPhotoService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  /**
   * Copie la photo dans la librairie et met à jour la DB pour le SN donné. Si un nom de produit est
   * fourni et non vide, applique la photo à tous les enregistrements de ce nom.
   *
   * @return chemin absolu sauvegardé en DB
   */
  public String importAndAssignPhoto(String numeroSerie, String produitOrNull, Path sourceFile)
      throws Exception {
    if (numeroSerie == null || numeroSerie.isBlank()) {
      throw new IllegalArgumentException("numeroSerie manquant");
    }
    if (sourceFile == null) {
      throw new IllegalArgumentException("fichier source manquant");
    }

    String ext = ImageLibraryService.normalizeExt(sourceFile.getFileName().toString(), "png");
    Path dest = ImageLibraryService.productPhotoDest(numeroSerie, ext);
    ImageLibraryService.copyToLibrary(sourceFile, dest);
    String photoAbs = dest.toAbsolutePath().toString();
    if (produitOrNull != null && !produitOrNull.isBlank()) {
      try {
        productRepository.updatePhotoPathByProduit(produitOrNull, photoAbs);
      } catch (Exception ignore) {
      }
    } else {
      productRepository.updatePhotoPath(numeroSerie, photoAbs);
    }
    return photoAbs;
  }
}
