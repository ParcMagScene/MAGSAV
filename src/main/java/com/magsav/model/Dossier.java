package com.magsav.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Dossier(
    Long id,
    Long appareilId,
    String statut,
    String symptome,
    String commentaire,
    LocalDate dateEntree,
    LocalDate dateSortie,
    LocalDateTime createdAt) {
  public Dossier withId(Long id) {
    return new Dossier(
        id, appareilId, statut, symptome, commentaire, dateEntree, dateSortie, createdAt);
  }

  public Dossier withStatut(String statut) {
    return new Dossier(
        id, appareilId, statut, symptome, commentaire, dateEntree, dateSortie, createdAt);
  }

  public Dossier withDateSortie(LocalDate dateSortie) {
    return new Dossier(
        id, appareilId, statut, symptome, commentaire, dateEntree, dateSortie, createdAt);
  }
}
