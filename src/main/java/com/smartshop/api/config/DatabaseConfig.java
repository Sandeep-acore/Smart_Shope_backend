package com.smartshop.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Primary
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        // Set database connection properties
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        
        // Connection pool settings
        dataSource.setConnectionTimeout(30000);
        dataSource.setMinimumIdle(2);
        dataSource.setMaximumPoolSize(5);
        dataSource.setIdleTimeout(300000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(20000);
        
        // Advanced PostgreSQL connection properties for Aiven
        Properties dsProps = new Properties();
        dsProps.setProperty("socketTimeout", "30");
        dsProps.setProperty("connectTimeout", "30");
        dsProps.setProperty("loginTimeout", "30");
        dsProps.setProperty("tcpKeepAlive", "true");
        dsProps.setProperty("ApplicationName", "SmartShop");
        dsProps.setProperty("reWriteBatchedInserts", "true");
        
        // Specific properties for Aiven PostgreSQL
        dsProps.setProperty("sslmode", "require");
        
        dataSource.setDataSourceProperties(dsProps);
        
        return dataSource;
    }
    
    /**
     * Additional configuration for Aiven PostgreSQL in production
     */
    @Bean
    @Profile("prod")
    public Properties aivenPostgresProperties() {
        Properties props = new Properties();
        props.setProperty("sslmode", "require");
        props.setProperty("tcpKeepAlive", "true");
        props.setProperty("ApplicationName", "SmartShop-Prod");
        return props;
    }
} 