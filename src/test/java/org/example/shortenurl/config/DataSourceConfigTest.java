package org.example.shortenurl.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class DataSourceConfigTest {

    private final DataSourceConfig config = new DataSourceConfig();

    @Test
    void createsDataSourceProperties() {
        assertNotNull(config.dataSourceProperties());
    }

    @Test
    void buildsConfiguredDataSource() {
        DataSourceProperties properties = mock(DataSourceProperties.class);
        DataSourceBuilder<?> builder = mock(DataSourceBuilder.class);
        DataSource dataSource = mock(DataSource.class);
        doReturn(builder).when(properties).initializeDataSourceBuilder();
        doReturn(dataSource).when(builder).build();

        assertSame(dataSource, config.dataSource(properties));
    }

    @Test
    void createsNamedParameterJdbcTemplate() {
        DataSource dataSource = mock(DataSource.class);

        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(dataSource);

        assertSame(dataSource, template.getJdbcTemplate().getDataSource());
    }
}
