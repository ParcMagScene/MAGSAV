package com.magsav.web.config;

import com.magsav.db.DB;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring Boot pour MAGSAV Utilise la même base SQLite que l'application CLI/JavaFX
 */
@Configuration
public class MAGSAVWebConfig {

  @Bean
  public DataSource dataSource() throws Exception {
    String dbUrl = "jdbc:sqlite:magsav.db";
    HikariDataSource dataSource = DB.init(dbUrl);
    DB.migrate(dataSource);
    return dataSource;
  }
}
