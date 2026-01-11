/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package io.smartpos.services.sale;

import io.smartpos.infrastructure.datasource.DataSourceProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDataSourceProvider implements DataSourceProvider {

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3308/smartpos",
                    "developer",
                    "19Dpr@237isc"
            );
        } catch (SQLException ex) {
            throw new RuntimeException("Error getting database connection", ex);
        }

    }
}

