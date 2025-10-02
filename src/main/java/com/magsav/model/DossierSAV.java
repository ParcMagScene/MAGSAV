package com.magsav.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DossierSAV {
  private Long id;
  private String code; // AA1234
  private String produit;
  private String numeroSerie;
  private String proprietaire;
  private String panne;
  private String statut;
  private String detecteur;
  private LocalDate dateEntree;
  private LocalDate dateSortie;
  private LocalDateTime createdAt;
  private Long categoryId;
  private Long subcategoryId;

  public DossierSAV(
      Long id,
      String code,
      String produit,
      String numeroSerie,
      String proprietaire,
      String panne,
      String statut,
      String detecteur,
      LocalDate dateEntree,
      LocalDate dateSortie,
      LocalDateTime createdAt) {
    this.id = id;
    this.code = code;
    this.produit = produit;
    this.numeroSerie = numeroSerie;
    this.proprietaire = proprietaire;
    this.panne = panne;
    this.statut = statut;
    this.detecteur = detecteur;
    this.dateEntree = dateEntree;
    this.dateSortie = dateSortie;
    this.createdAt = createdAt;
  }

  // Constructeur incluant catégories
  public DossierSAV(
      Long id,
      String code,
      String produit,
      String numeroSerie,
      String proprietaire,
      String panne,
      String statut,
      String detecteur,
      LocalDate dateEntree,
      LocalDate dateSortie,
      LocalDateTime createdAt,
      Long categoryId,
      Long subcategoryId) {
    this(
        id,
        code,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        statut,
        detecteur,
        dateEntree,
        dateSortie,
        createdAt);
    this.categoryId = categoryId;
    this.subcategoryId = subcategoryId;
  }

  // Constructeur pour création sans dates
  public static DossierSAV nouveau(
      String produit, String numeroSerie, String proprietaire, String panne, String detecteur) {
    return new DossierSAV(
        null,
        null,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        "recu",
        detecteur,
        LocalDate.now(),
        null,
        null,
        null,
        null);
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getProduit() {
    return produit;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public String getProprietaire() {
    return proprietaire;
  }

  public String getPanne() {
    return panne;
  }

  public String getStatut() {
    return statut;
  }

  public String getDetecteur() {
    return detecteur;
  }

  public LocalDate getDateEntree() {
    return dateEntree;
  }

  public LocalDate getDateSortie() {
    return dateSortie;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public Long getSubcategoryId() {
    return subcategoryId;
  }

  public DossierSAV withId(Long id) {
    return new DossierSAV(
        id,
        code,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        statut,
        detecteur,
        dateEntree,
        dateSortie,
        createdAt,
        categoryId,
        subcategoryId);
  }

  public DossierSAV withStatut(String statut) {
    return new DossierSAV(
        id,
        code,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        statut,
        detecteur,
        dateEntree,
        dateSortie,
        createdAt,
        categoryId,
        subcategoryId);
  }

  public DossierSAV withDateSortie(LocalDate dateSortie) {
    return new DossierSAV(
        id,
        code,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        statut,
        detecteur,
        dateEntree,
        dateSortie,
        createdAt,
        categoryId,
        subcategoryId);
  }

  public DossierSAV withCategories(Long categoryId, Long subcategoryId) {
    return new DossierSAV(
        id,
        code,
        produit,
        numeroSerie,
        proprietaire,
        panne,
        statut,
        detecteur,
        dateEntree,
        dateSortie,
        createdAt,
        categoryId,
        subcategoryId);
  }
}
