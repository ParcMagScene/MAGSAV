package com.magsav.model;

public record Category(long id, String nom, Long parentId) {
  @Override public String toString() { return nom; }
}