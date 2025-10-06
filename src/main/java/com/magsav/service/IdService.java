package com.magsav.service;

import com.magsav.repo.ProductRepository;

import java.security.SecureRandom;

public final class IdService {
  private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final SecureRandom RND = new SecureRandom();

  public static String generateUid() {
    StringBuilder sb = new StringBuilder(7);
    for (int i = 0; i < 3; i++) sb.append(LETTERS.charAt(RND.nextInt(LETTERS.length())));
    int num = RND.nextInt(10000); // 0..9999
    sb.append(String.format("%04d", num));
    return sb.toString();
  }

  public static String generateUniqueUid(ProductRepository repo) {
    for (int tries = 0; tries < 1000; tries++) {
      String uid = generateUid();
      if (!repo.existsUid(uid)) return uid;
    }
    throw new IllegalStateException("Impossible de générer un UID unique");
  }

  private IdService() {}
}