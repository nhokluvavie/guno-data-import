package com.guno.etl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "com.guno.etl.repository")
@EntityScan(basePackages = "com.guno.etl.entity")
@EnableTransactionManagement
public class DatabaseConfig {

    @PostConstruct
    public void init() {
        log.info("Database configuration initialized");
        log.info("JPA repositories enabled for package: com.guno.etl.repository");
        log.info("Entity scan enabled for package: com.guno.etl.entity");
    }
}