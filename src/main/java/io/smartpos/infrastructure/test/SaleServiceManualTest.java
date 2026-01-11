/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.smartpos.infrastructure.test;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.dao.SaleDao;
import io.smartpos.infrastructure.dao.SaleItemDao;
import io.smartpos.infrastructure.dao.inventory.InventoryMovementDaoImpl;
import io.smartpos.infrastructure.dao.sale.SaleDaoImpl;
import io.smartpos.infrastructure.dao.sale.SaleItemDaoImpl;
import io.smartpos.infrastructure.datasource.DataSourceProvider;
import io.smartpos.infrastructure.datasource.HikariDataSourceProvider;
import io.smartpos.services.sale.SaleService;
import io.smartpos.services.sale.SaleServiceImpl;

import java.time.LocalDate;
import java.util.List;

public class SaleServiceManualTest {

    public static void main(String[] args) {

        DataSourceProvider dataSourceProvider =
                new HikariDataSourceProvider();

        SaleDao saleDao =
                new SaleDaoImpl(dataSourceProvider);

        SaleItemDao saleItemDao =
                new SaleItemDaoImpl(dataSourceProvider);

        InventoryMovementDao inventoryDao =
        new InventoryMovementDaoImpl();

        SaleService saleService =
                new SaleServiceImpl(
                        saleDao,
                        saleItemDao,
                        inventoryDao,
                        dataSourceProvider
                );

        // ===== TEST DATE RANGE =====

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Sale> sales =
                saleService.findByDateRange(startDate, endDate);

        System.out.println(
                "----- SALES FOUND BETWEEN "
                        + startDate + " AND " + endDate + " -----"
        );

        for (Sale sale : sales) {

            System.out.println("----------------------------------");
            System.out.println("Sale ID: " + sale.getId());
            System.out.println("Customer ID: " + sale.getCustomerId());
            System.out.println("Sale Date: " + sale.getSaleDate());
            System.out.println("Total Amount: " + sale.getTotalAmount());

            for (SaleItem item : sale.getItems()) {
                System.out.println(
                        "  Product ID: " + item.getProductId()
                                + " | Qty: " + item.getQuantity()
                                + " | Unit Price: " + item.getUnitPrice()
                );
            }
        }

        System.out.println("----------------------------------");
        System.out.println("Total sales found: " + sales.size());
    }
}
