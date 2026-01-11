/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.datasource;

import java.sql.Connection;

public interface DataSourceProvider {

    Connection getConnection();
}

