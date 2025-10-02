package com.magsav.model;

import java.time.LocalDateTime;

public record Appareil(
    Long id,
    Long clientId,
    String marque,
    String modele,
    String sn,
    String accessoires,
    LocalDateTime createdAt) {
  public Appareil withId(Long id) {
    return new Appareil(id, clientId, marque, modele, sn, accessoires, createdAt);
  }
}
