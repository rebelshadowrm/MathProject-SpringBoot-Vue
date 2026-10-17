package com.group.mathproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration(proxyBeanMethods = false)
@Profile("portfolio-demo")
public class PortfolioDemoConfiguration {
    @Bean
    public EmbeddedDatabase dataSource(Environment environment) {
        // Separate Flyway connection settings can bypass this datasource. Fail before connecting.
        for (String setting : new String[]{"url", "user", "password", "driver-class-name"}) {
            if (environment.containsProperty("spring.flyway." + setting)) {
                throw new IllegalStateException("Remove spring.flyway." + setting + " for the portfolio-demo profile");
            }
        }
        // Deliberately ignore SPRING_DATASOURCE_* supplied by an attached Heroku add-on.
        // Each application context gets a clean database, with no files or external service.
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }
}
