package com.magsav.service;

import com.magsav.model.PurchaseRFQ;
import com.magsav.repo.PurchaseRFQRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PurchaseService {
  private final PurchaseRFQRepository repo;

  public PurchaseService(PurchaseRFQRepository repo) {
    this.repo = repo;
  }

  public PurchaseRFQ createRFQ(PurchaseRFQ r) throws SQLException {
    PurchaseRFQ init =
        new PurchaseRFQ(
            null,
            r.providerId(),
            r.providerServiceId(),
            r.produit(),
            r.partNumber(),
            r.quantity(),
            r.status() == null ? "draft" : r.status(),
            r.requestedAt() == null ? LocalDateTime.now() : r.requestedAt(),
            r.respondedAt(),
            r.price(),
            r.currency(),
            r.notes());
    return repo.save(init);
  }

  public PurchaseRFQ markResponded(Long id, Double price, String currency) throws SQLException {
    Optional<PurchaseRFQ> opt = repo.findById(id);
    if (opt.isEmpty()) {
      throw new IllegalArgumentException("RFQ introuvable: " + id);
    }
    PurchaseRFQ cur = opt.get();
    PurchaseRFQ upd =
        new PurchaseRFQ(
            cur.id(),
            cur.providerId(),
            cur.providerServiceId(),
            cur.produit(),
            cur.partNumber(),
            cur.quantity(),
            "responded",
            cur.requestedAt(),
            LocalDateTime.now(),
            price,
            currency,
            cur.notes());
    return repo.save(upd);
  }

  public List<PurchaseRFQ> list() throws SQLException {
    return repo.findAll();
  }

  public PurchaseRFQ updateNotesAndQuantity(Long id, Integer quantity, String notes)
      throws SQLException {
    Optional<PurchaseRFQ> opt = repo.findById(id);
    if (opt.isEmpty()) {
      throw new IllegalArgumentException("RFQ introuvable: " + id);
    }
    PurchaseRFQ cur = opt.get();
    PurchaseRFQ upd =
        new PurchaseRFQ(
            cur.id(),
            cur.providerId(),
            cur.providerServiceId(),
            cur.produit(),
            cur.partNumber(),
            quantity,
            cur.status(),
            cur.requestedAt(),
            cur.respondedAt(),
            cur.price(),
            cur.currency(),
            notes);
    return repo.save(upd);
  }

  public void deleteRFQ(Long id) throws SQLException {
    repo.delete(id);
  }
}
