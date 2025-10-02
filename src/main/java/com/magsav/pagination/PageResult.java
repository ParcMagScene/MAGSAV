package com.magsav.pagination;

import java.util.List;

/** Résultat paginé contenant les données et les métadonnées de pagination */
public class PageResult<T> {
  private final List<T> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;
  private final boolean hasNext;
  private final boolean hasPrevious;
  private final String sortBy;
  private final String sortDirection;

  public PageResult(
      List<T> content,
      int page,
      int size,
      long totalElements,
      String sortBy,
      String sortDirection) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / size);
    this.hasNext = page < totalPages - 1;
    this.hasPrevious = page > 0;
    this.sortBy = sortBy;
    this.sortDirection = sortDirection;
  }

  public List<T> getContent() {
    return content;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public boolean hasNext() {
    return hasNext;
  }

  public boolean hasPrevious() {
    return hasPrevious;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public boolean isEmpty() {
    return content == null || content.isEmpty();
  }

  public int getNumberOfElements() {
    return content != null ? content.size() : 0;
  }

  public boolean isFirst() {
    return page == 0;
  }

  public boolean isLast() {
    return page >= totalPages - 1;
  }

  public int getNextPage() {
    return hasNext ? page + 1 : page;
  }

  public int getPreviousPage() {
    return hasPrevious ? page - 1 : page;
  }
}
