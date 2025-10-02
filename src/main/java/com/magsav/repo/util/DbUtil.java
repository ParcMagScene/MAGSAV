package com.magsav.repo.util;

import java.sql.*;
import java.time.LocalDateTime;

public final class DbUtil {
  private DbUtil() {}

  public static void setNullableLong(PreparedStatement ps, int idx, Long v) throws SQLException {
    if (v == null) {
      ps.setNull(idx, Types.BIGINT);
    } else {
      ps.setLong(idx, v);
    }
  }

  public static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
    if (v == null) {
      ps.setNull(idx, Types.INTEGER);
    } else {
      ps.setInt(idx, v);
    }
  }

  public static void setNullableDouble(PreparedStatement ps, int idx, Double v)
      throws SQLException {
    if (v == null) {
      ps.setNull(idx, Types.DOUBLE);
    } else {
      ps.setDouble(idx, v);
    }
  }

  public static LocalDateTime ldt(ResultSet rs, int idx) throws SQLException {
    return readLdt(rs, idx);
  }

  public static LocalDateTime readLdt(ResultSet rs, int idx) throws SQLException {
    String v = rs.getString(idx);
    return v == null ? null : LocalDateTime.parse(v.replace(' ', 'T'));
  }

  public static String fmt(LocalDateTime ldt) {
    if (ldt == null) {
      return null;
    }
    return ldt.toString().replace('T', ' ');
  }
}
