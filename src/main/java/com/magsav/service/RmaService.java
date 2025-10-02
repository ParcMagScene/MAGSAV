package com.magsav.service;

import com.magsav.model.RmaRequest;
import com.magsav.repo.RmaRequestRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RmaService {
  private final RmaRequestRepository repo;

  public RmaService(RmaRequestRepository repo) {
    this.repo = repo;
  }

  public RmaRequest create(RmaRequest r) throws SQLException {
    RmaRequest init =
        new RmaRequest(
            null,
            r.providerId(),
            r.providerServiceId(),
            r.manufacturerId(),
            r.produit(),
            r.numeroSerie(),
            r.codeProduit(),
            r.reason(),
            r.status() == null ? "draft" : r.status(),
            r.rmaNumber(),
            r.createdAt() == null ? LocalDateTime.now() : r.createdAt(),
            r.updatedAt());
    return repo.save(init);
  }

  public RmaRequest assignRmaNumber(Long id, String rmaNumber) throws SQLException {
    Optional<RmaRequest> opt = repo.findById(id);
    if (opt.isEmpty()) {
      throw new IllegalArgumentException("RMA introuvable: " + id);
    }
    RmaRequest cur = opt.get();
    RmaRequest upd =
        new RmaRequest(
            cur.id(),
            cur.providerId(),
            cur.providerServiceId(),
            cur.manufacturerId(),
            cur.produit(),
            cur.numeroSerie(),
            cur.codeProduit(),
            cur.reason(),
            "assigned",
            rmaNumber,
            cur.createdAt(),
            LocalDateTime.now());
    return repo.save(upd);
  }

  /**
   * Met à jour les champs principaux d'une demande RMA (service, fabricant, produit, SN, code,
   * motif). Conserve createdAt et le statut existant, met à jour updatedAt.
   */
  public RmaRequest update(RmaRequest r) throws SQLException {
    if (r.id() == null) {
      throw new IllegalArgumentException("ID requis pour la mise à jour RMA");
    }
    Optional<RmaRequest> opt = repo.findById(r.id());
    if (opt.isEmpty()) {
      throw new IllegalArgumentException("RMA introuvable: " + r.id());
    }
    RmaRequest cur = opt.get();
    RmaRequest upd =
        new RmaRequest(
            cur.id(),
            r.providerId(),
            r.providerServiceId(),
            r.manufacturerId(),
            r.produit(),
            r.numeroSerie(),
            r.codeProduit(),
            r.reason(),
            cur.status(),
            cur.rmaNumber(),
            cur.createdAt(),
            LocalDateTime.now());
    return repo.save(upd);
  }

  public void delete(Long id) throws SQLException {
    repo.delete(id);
  }

  public List<RmaRequest> list() throws SQLException {
    return repo.findAll();
  }
}
