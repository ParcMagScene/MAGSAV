package com.magsav.media;

import com.magsav.repo.ManufacturerRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Résout le logo d'un fabricant à partir du nom: path du logo s'il existe; sinon un fallback PNG
 * basé sur les initiales.
 */
public class ManufacturerLogoService {
  private final ManufacturerRepository manufacturerRepository;
  private final AvatarService avatarService;

  public ManufacturerLogoService(DataSource ds) {
    this(new ManufacturerRepository(ds), new AvatarService());
  }

  public ManufacturerLogoService(
      ManufacturerRepository manufacturerRepository, AvatarService avatarService) {
    this.manufacturerRepository = manufacturerRepository;
    this.avatarService = avatarService;
  }

  public record LogoResult(Path pathOrNull, byte[] fallbackPngOrNull) {}

  /**
   * @return Path du logo si présent/sur disque; sinon bytes PNG d'un fallback; si name vide, tout
   *     est null.
   */
  public LogoResult resolveLogo(String name, int fallbackWidth, int fallbackHeight) {
    try {
      if (name == null || name.isBlank()) {
        return new LogoResult(null, null);
      }
      Optional<com.magsav.model.Manufacturer> opt = manufacturerRepository.findByName(name);
      if (opt.isEmpty()) {
        return new LogoResult(
            null, avatarService.renderInitialsPng(name, fallbackWidth, fallbackHeight));
      }
      String path = opt.get().logoPath();
      if (path == null || path.isBlank()) {
        return new LogoResult(
            null, avatarService.renderInitialsPng(name, fallbackWidth, fallbackHeight));
      }
      Path p = Path.of(path);
      if (!Files.exists(p)) {
        return new LogoResult(
            null, avatarService.renderInitialsPng(name, fallbackWidth, fallbackHeight));
      }
      return new LogoResult(p, null);
    } catch (Exception e) {
      try {
        return new LogoResult(
            null, avatarService.renderInitialsPng(name, fallbackWidth, fallbackHeight));
      } catch (Exception ignored) {
        return new LogoResult(null, null);
      }
    }
  }
}
