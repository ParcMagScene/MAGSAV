package com.magsav.model;

public record Category(Long id, String name, Long parentId) {
  public Category withId(Long newId) {
    return new Category(newId, name, parentId);
  }
}
