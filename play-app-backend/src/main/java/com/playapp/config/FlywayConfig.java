package com.playapp.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Manual Flyway migration for Spring Boot 4.
 */
@Configuration
@ConditionalOnProperty(name = "flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public InitializingBean flywayMigrationInitializer(DataSource dataSource) {
        return () -> {
            log.info("Flyway migration: checking...");
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("1")
                    .load();
            int applied = flyway.migrate().migrationsExecuted;
            if (applied > 0) {
                log.info("Flyway migration: {} migration(s) applied", applied);
            } else {
                log.info("Flyway migration: up to date");
            }
            log.info("Flyway migration: completed");
        };
    }
}
