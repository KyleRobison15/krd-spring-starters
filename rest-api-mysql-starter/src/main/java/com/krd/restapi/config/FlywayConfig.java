package com.krd.restapi.config;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Flyway database migrations.
 * Provides sensible defaults for migration behavior.
 */
@Configuration
@ConditionalOnClass(FluentConfiguration.class)
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer flywayCustomizer() {
        return configuration -> {
            // Baseline on migrate - allows Flyway to work with existing databases
            configuration.baselineOnMigrate(true);

            // Validate migrations on startup
            configuration.validateOnMigrate(true);

            // Don't clean database in production (safety)
            configuration.cleanDisabled(true);
        };
    }
}
