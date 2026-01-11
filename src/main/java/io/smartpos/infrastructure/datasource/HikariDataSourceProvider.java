/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.smartpos.infrastructure.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HikariDataSourceProvider implements DataSourceProvider {

    private static final HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            props.load(
                HikariDataSourceProvider.class
                    .getClassLoader()
                    .getResourceAsStream("application.properties")
            );

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            //config.setDriverClassName("db.driver");

            config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.size"))
            );
            config.setMinimumIdle(
                Integer.parseInt(props.getProperty("db.pool.minIdle"))
            );
            config.setMaxLifetime(
                Long.parseLong(props.getProperty("db.pool.maxLifetime"))
            );
            config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.connectionTimeout"))
            );

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(HikariDataSourceProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

