package com.magsav.gui;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magsav.label.LabelService;
import com.magsav.media.ManufacturerLogoService;
import com.magsav.model.ProductSummary;
import com.magsav.qr.QRCodeService;
import com.magsav.repo.ProductRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProductDetailControllerFxTest {
  @BeforeAll
  static void initToolkit() throws Exception {
    // Initialise la plateforme JavaFX
    CountDownLatch latch = new CountDownLatch(1);
    Platform.startup(latch::countDown);
    latch.await(3, TimeUnit.SECONDS);
  }

  private ProductDetailController loadController() throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product-detail.fxml"));
    Parent root = loader.load();
    assertThat(root).isNotNull();
    ProductDetailController ctrl = loader.getController();
    assertThat(ctrl).isNotNull();
    return ctrl;
  }

  @Test
  void init_setsCodeAndQr_andNoLogoWhenNoManufacturer() throws Exception {
    ProductDetailController ctrl = loadController();
    ProductRepository repo = Mockito.mock(ProductRepository.class);
    when(repo.findCodeByNumeroSerie("SN1")).thenReturn("CODE1");
    when(repo.findManufacturerNameByNumeroSerie("SN1")).thenReturn(null);
    when(repo.findPhotoPathByNumeroSerie("SN1")).thenReturn(null);

    ProductSummary p = new ProductSummary("Model A", "SN1", 0, null, null);
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          ctrl.setData(p, repo, null, new QRCodeService(), new LabelService());
          latch.countDown();
        });
    assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();

    CountDownLatch check = new CountDownLatch(1);
    final String[] codeText = new String[1];
    final boolean[] qrPresent = new boolean[1];
    final boolean[] logoPresent = new boolean[1];
    Platform.runLater(
        () -> {
          codeText[0] = ctrl.getLblCode().getText();
          qrPresent[0] = ctrl.getImgQr().getImage() != null;
          logoPresent[0] = ctrl.getImgManufacturerLogo().getImage() != null;
          check.countDown();
        });
    assertThat(check.await(3, TimeUnit.SECONDS)).isTrue();
    assertThat(codeText[0]).isEqualTo("CODE1".toUpperCase());
    assertThat(qrPresent[0]).isTrue();
    assertThat(logoPresent[0]).isFalse();
  }

  @Test
  void logoFallback_whenInjectedServiceProvidesPng() throws Exception {
    ProductDetailController ctrl = loadController();
    ProductRepository repo = Mockito.mock(ProductRepository.class);
    when(repo.findCodeByNumeroSerie("SN2")).thenReturn(null);
    when(repo.findManufacturerNameByNumeroSerie("SN2")).thenReturn("Acme");
    when(repo.findPhotoPathByNumeroSerie("SN2")).thenReturn(null);

    // Service logo qui retourne toujours un fallback PNG
    ManufacturerLogoService fake =
        new ManufacturerLogoService(null) {
          @Override
          public LogoResult resolveLogo(String name, int w, int h) {
            try {
              byte[] png = new com.magsav.media.AvatarService().renderInitialsPng(name, w, h);
              return new LogoResult(null, png);
            } catch (Exception e) {
              return new LogoResult(null, null);
            }
          }
        };
    ctrl.setManufacturerLogoService(fake);

    ProductSummary p = new ProductSummary("Model B", "SN2", 0, null, null);
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          ctrl.setData(p, repo, null, new QRCodeService(), new LabelService());
          latch.countDown();
        });
    assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();

    CountDownLatch check = new CountDownLatch(1);
    final boolean[] logoPresent = new boolean[1];
    Platform.runLater(
        () -> {
          logoPresent[0] = ctrl.getImgManufacturerLogo().getImage() != null;
          check.countDown();
        });
    assertThat(check.await(3, TimeUnit.SECONDS)).isTrue();
    assertThat(logoPresent[0]).isTrue();
  }
}
