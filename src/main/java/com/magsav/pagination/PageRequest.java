package com.magsav.pagination;

/** Classe utilitaire pour la pagination des listes */
public class PageRequest {
  private int page;
  private int size;
  private String sortBy;
  private String sortDirection;

  public PageRequest(int page, int size) {
    this(page, size, "id", "asc");
  }

  public PageRequest(int page, int size, String sortBy, String sortDirection) {
    this.page = Math.max(0, page);
    this.size = Math.max(1, Math.min(100, size)); // Limite à 100 éléments par page
    this.sortBy = sortBy != null ? sortBy : "id";
    this.sortDirection = "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public int getOffset() {
    return page * size;
  }

  public String getSortClause() {
    return sortBy + " " + sortDirection.toUpperCase();
  }
}
