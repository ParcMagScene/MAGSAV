package com.magsav.db.migration;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
  int version();
  String description();
  void up(Connection c) throws SQLException;
}