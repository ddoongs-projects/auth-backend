package com.ddoongs.auth.storage.db.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.ddoongs.auth.storage.db.core")
@EnableJpaRepositories(basePackages = "com.ddoongs.auth.storage.db.core")
@Configuration
public class PersistenceJpaConfig {}
