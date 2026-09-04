package org.example.shortenurl.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DataSourceConfig {

    @Bean("dsProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("ds")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.configuration")
    public DataSource dataSource(
            @Qualifier("dsProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            @Qualifier("ds") DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
