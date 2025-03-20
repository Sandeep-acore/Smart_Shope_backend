package com.smartshop.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        dataSource.setConnectionTimeout(20000);
        dataSource.setMinimumIdle(2);
        dataSource.setMaximumPoolSize(5);
        dataSource.setIdleTimeout(120000);
        dataSource.setMaxLifetime(180000);
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(10000);
        
        // Advanced PostgreSQL connection properties for Aiven
        Properties dsProps = new Properties();
        dsProps.setProperty("socketTimeout", "30");
        dsProps.setProperty("connectTimeout", "30");
        dsProps.setProperty("loginTimeout", "30");
        dsProps.setProperty("tcpKeepAlive", "true");
        dsProps.setProperty("ApplicationName", "SmartShop");
        dsProps.setProperty("reWriteBatchedInserts", "true");
        
        // Specific properties for Aiven PostgreSQL 16.8
        dsProps.setProperty("sslmode", "require");
        
        dataSource.setDataSourceProperties(dsProps);
        
        return dataSource;
    }
} 