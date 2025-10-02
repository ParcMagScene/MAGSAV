package com.magsav.media;

import static org.assertj.core.api.Assertions.*;

import com.magsav.model.Manufacturer;
import com.magsav.repo.ManufacturerRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StubManufacturerRepository extends ManufacturerRepository {
  private final List<Manufacturer> data;

  public StubManufacturerRepository(List<Manufacturer> data) {
    super(null);
    this.data = data;
  }

  @Override
  public Optional<Manufacturer> findByName(String name) {
    return data.stream().filter(m -> m.name().equalsIgnoreCase(name)).findFirst();
  }

  @Override
  public List<Manufacturer> findAll() {
    return data;
  }
}

public class ManufacturerLogoServiceTest {
  @Test
  void resolveLogo_fallback_when_missing() throws Exception {
    ManufacturerLogoService svc =
        new ManufacturerLogoService(new StubManufacturerRepository(List.of()), new AvatarService());
    var res = svc.resolveLogo("Acme", 80, 24);
    assertThat(res.pathOrNull()).isNull();
    assertThat(res.fallbackPngOrNull()).isNotNull();
    assertThat(res.fallbackPngOrNull().length).isGreaterThan(100);
  }

  @Test
  void resolveLogo_path_when_exists() throws Exception {
    Path tmp = Files.createTempFile("logo-", ".png");
    Files.writeString(tmp, "PNG");
    var repo =
        new StubManufacturerRepository(
            List.of(new Manufacturer(1L, "Acme", null, null, null, tmp.toString())));
    ManufacturerLogoService svc = new ManufacturerLogoService(repo, new AvatarService());
    var res = svc.resolveLogo("Acme", 80, 24);
    assertThat(res.pathOrNull()).isEqualTo(tmp);
    assertThat(res.fallbackPngOrNull()).isNull();
  }
}
