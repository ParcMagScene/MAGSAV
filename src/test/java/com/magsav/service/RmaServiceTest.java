package com.magsav.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magsav.model.RmaRequest;
import com.magsav.repo.RmaRequestRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RmaServiceTest {

  @Test
  void create_setsDefaultsAndSaves() throws Exception {
    RmaRequestRepository repo = mock(RmaRequestRepository.class);
    RmaService svc = new RmaService(repo);

    RmaRequest input =
        new RmaRequest(null, 1L, 10L, 100L, "P", "SN", "CODE", "reason", null, null, null, null);
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RmaRequest saved = svc.create(input);

    assertThat(saved.id()).isNull();
    assertThat(saved.status()).isEqualTo("draft");
    assertThat(saved.createdAt()).isNotNull();
    // updatedAt reste tel quel (null) à la création dans notre implémentation
    assertThat(saved.updatedAt()).isNull();
    verify(repo).save(any());
  }

  @Test
  void assignRmaNumber_updatesStatusAndNumber() throws Exception {
    RmaRequestRepository repo = mock(RmaRequestRepository.class);
    RmaService svc = new RmaService(repo);

    RmaRequest existing =
        new RmaRequest(
            5L,
            2L,
            20L,
            200L,
            "P2",
            "SN2",
            "C2",
            "r2",
            "draft",
            null,
            LocalDateTime.now().minusDays(1),
            null);
    when(repo.findById(5L)).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RmaRequest saved = svc.assignRmaNumber(5L, "RMA-123");

    assertThat(saved.id()).isEqualTo(5L);
    assertThat(saved.status()).isEqualTo("assigned");
    assertThat(saved.rmaNumber()).isEqualTo("RMA-123");
    assertThat(saved.createdAt()).isEqualTo(existing.createdAt());
    assertThat(saved.updatedAt()).isNotNull();
    verify(repo).save(any());
  }

  @Test
  void update_preservesCreatedAndStatus() throws Exception {
    RmaRequestRepository repo = mock(RmaRequestRepository.class);
    RmaService svc = new RmaService(repo);

    LocalDateTime created = LocalDateTime.now().minusDays(2);
    RmaRequest cur =
        new RmaRequest(7L, 1L, 10L, 100L, "P", "SN", "C", "r", "assigned", "RMA-9", created, null);
    when(repo.findById(7L)).thenReturn(Optional.of(cur));
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RmaRequest incoming =
        new RmaRequest(
            7L,
            3L,
            30L,
            300L,
            "NP",
            "NSN",
            "NC",
            "nr",
            "ignored",
            "IGN",
            LocalDateTime.now(),
            null);
    RmaRequest out = svc.update(incoming);

    assertThat(out.id()).isEqualTo(7L);
    assertThat(out.providerId()).isEqualTo(3L);
    assertThat(out.providerServiceId()).isEqualTo(30L);
    assertThat(out.manufacturerId()).isEqualTo(300L);
    assertThat(out.produit()).isEqualTo("NP");
    assertThat(out.numeroSerie()).isEqualTo("NSN");
    assertThat(out.codeProduit()).isEqualTo("NC");
    assertThat(out.reason()).isEqualTo("nr");

    // champs préservés
    assertThat(out.status()).isEqualTo("assigned");
    assertThat(out.rmaNumber()).isEqualTo("RMA-9");
    assertThat(out.createdAt()).isEqualTo(created);
    assertThat(out.updatedAt()).isNotNull();
  }

  @Test
  void update_throwsIfMissingOrNotFound() throws Exception {
    RmaRequestRepository repo = mock(RmaRequestRepository.class);
    RmaService svc = new RmaService(repo);

    assertThatThrownBy(
            () ->
                svc.update(
                    new RmaRequest(
                        null, null, null, null, null, null, null, null, null, null, null, null)))
        .isInstanceOf(IllegalArgumentException.class);

    when(repo.findById(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                svc.update(
                    new RmaRequest(
                        99L, null, null, null, null, null, null, null, null, null, null, null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void list_and_delete_delegatesToRepo() throws SQLException {
    RmaRequestRepository repo = mock(RmaRequestRepository.class);
    RmaService svc = new RmaService(repo);

    when(repo.findAll()).thenReturn(List.of());
    assertThat(svc.list()).isEmpty();
    verify(repo).findAll();

    svc.delete(1L);
    verify(repo).delete(1L);
  }
}
