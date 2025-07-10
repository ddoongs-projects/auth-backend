package com.ddoongs.auth.storage.db.core.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreDataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "storage.datasource.core")
  public DataSourceProperties coreDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "coreDataSource")
  public DataSource coreDataSource() {
    return coreDataSourceProperties()
        .initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }
}
