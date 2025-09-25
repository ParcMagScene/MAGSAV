package com.magsav.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class DB implements AutoCloseable {
    private final HikariDataSource ds;

    private DB(HikariDataSource ds) { this.ds = ds; }

    public static HikariDataSource init(String jdbcUrl) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setMaximumPoolSize(4);
        cfg.setPoolName("magsav-pool");
        return new HikariDataSource(cfg);
    }

    public static void migrate(DataSource ds) throws Exception {
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("PRAGMA foreign_keys = ON");
            String sql = readResource("schema.sql");
            for (String stmt : sql.split(";\\s*\n")) {
                String s = stmt.trim();
                if (!s.isEmpty()) {
                    c.createStatement().execute(s);
                }
            }
        }
    }

    private static String readResource(String name) throws Exception {
        try (var in = DB.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) throw new IllegalStateException("Ressource introuvable: " + name);
            try (var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
                return sb.toString();
            }
        }
    }

    public static DataSource wrap(HikariDataSource ds) { return ds; }

    @Override
    public void close() { ds.close(); }
}
