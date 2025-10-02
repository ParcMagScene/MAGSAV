package com.magsav.model;

import java.time.LocalDate;

/**
 * Représente une vue synthétique d'un produit (appareil identifié par son nom et son numéro de
 * série) avec des informations agrégées sur ses interventions.
 */
public class ProductSummary {
  private final String produit;
  private final String numeroSerie;
  private final long interventionsCount;
  private final LocalDate lastDateEntree;
  private final LocalDate lastDateSortie;

  public ProductSummary(
      String produit,
      String numeroSerie,
      long interventionsCount,
      LocalDate lastDateEntree,
      LocalDate lastDateSortie) {
    this.produit = produit;
    this.numeroSerie = numeroSerie;
    this.interventionsCount = interventionsCount;
    this.lastDateEntree = lastDateEntree;
    this.lastDateSortie = lastDateSortie;
  }

  public String getProduit() {
    return produit;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public long getInterventionsCount() {
    return interventionsCount;
  }

  public LocalDate getLastDateEntree() {
    return lastDateEntree;
  }

  public LocalDate getLastDateSortie() {
    return lastDateSortie;
  }
}
