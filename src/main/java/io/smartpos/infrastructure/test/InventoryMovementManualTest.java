/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.smartpos.infrastructure.test;

import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.dao.inventory.InventoryMovementDaoImpl;
//import io.smartpos.infrastructure.test.SimpleDataSourceProvider;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.Connection;

public class InventoryMovementManualTest {

    public static void main(String[] args) {

        DataSourceProvider dataSource =
                new SimpleDataSourceProvider();

        InventoryMovementDao inventoryDao =
                new InventoryMovementDaoImpl();

        try (Connection connection = dataSource.getConnection()) {

            inventoryDao.registerEntry(
                    1, // product_id existente
                    new BigDecimal("10.00"),
                    "PURCHASE",
                    connection
            );

            inventoryDao.registerExit(
                    1,
                    new BigDecimal("2.00"),
                    "SALE",
                    connection
            );

            System.out.println("Inventory movement test executed successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


