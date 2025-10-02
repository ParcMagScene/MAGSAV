package com.magsav.qr;

/** Génération de QR code pour un produit. */
public class ProductQrService {
  private final QRCodeService qrCodeService;

  public ProductQrService() {
    this(new QRCodeService());
  }

  public ProductQrService(QRCodeService qrCodeService) {
    this.qrCodeService = qrCodeService;
  }

  /** Construit le contenu canonique du QR pour un produit. */
  public String buildQrContent(String codeOrSn, String numeroSerie) {
    return String.format("MAGSAV:PROD:%s:%s", codeOrSn, numeroSerie);
  }

  /** Génère les bytes PNG pour le QR du produit. */
  public byte[] generateQrPng(String codeOrSn, String numeroSerie) throws Exception {
    String content = buildQrContent(codeOrSn, numeroSerie);
    return qrCodeService.generateQRCode(content);
  }
}
