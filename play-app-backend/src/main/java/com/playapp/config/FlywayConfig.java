package com.playapp.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway 数据库迁移 — 启动时自动执行
 * Spring Boot 4 移除了 Flyway 自动配置，此处手动触发
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public CommandLineRunner flywayRunner(DataSource dataSource) {
        return args -> {
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
        };
    }
}
