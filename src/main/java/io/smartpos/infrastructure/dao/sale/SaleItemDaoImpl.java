/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.smartpos.infrastructure.dao.sale;

import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.infrastructure.dao.SaleItemDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SaleItemDaoImpl implements SaleItemDao {

    private final DataSourceProvider dataSource;

    public SaleItemDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(
            SaleItem item,
            Connection connection) throws SQLException {

        String sql = """
                INSERT INTO sale_item (
                    sale_id,
                    product_id,
                    quantity,
                    unit_price
                ) VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, item.getSaleId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPrice());

            stmt.executeUpdate();
        }
    }

    // ===== Batch insert =====
    @Override
    public void saveBatch(
            List<SaleItem> items,
            Connection connection) throws SQLException {

        if (items == null || items.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO sale_item (
                    sale_id,
                    product_id,
                    quantity,
                    unit_price
                ) VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (SaleItem item : items) {

                stmt.setInt(1, item.getSaleId());
                stmt.setInt(2, item.getProductId());
                stmt.setBigDecimal(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());

                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    @Override
    public List<SaleItem> findBySaleId(
            int saleId,
            Connection connection) throws SQLException {

        String sql = """
                SELECT
                    sale_id,
                    product_id,
                    quantity,
                    unit_price
                FROM sale_item
                WHERE sale_id = ?
                ORDER BY sale_item_id
                """;

        List<SaleItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    SaleItem item = new SaleItem();
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));

                    items.add(item);
                }
            }
        }

        return items;
    }

    // ===== Batch loading =====
    @Override
    public List<SaleItem> findBySaleIds(
            List<Integer> saleIds,
            Connection connection) throws SQLException {

        if (saleIds == null || saleIds.isEmpty()) {
            return List.of();
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int i = 0; i < saleIds.size(); i++) {
            placeholders.add("?");
        }

        String sql = """
                SELECT
                    sale_id,
                    product_id,
                    quantity,
                    unit_price
                FROM sale_item
                WHERE sale_id IN (""" + placeholders + """
                )
                ORDER BY sale_id, sale_item_id
                """;

        List<SaleItem> items = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            int index = 1;
            for (Integer saleId : saleIds) {
                stmt.setInt(index++, saleId);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    SaleItem item = new SaleItem();
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));

                    items.add(item);
                }
            }
        }

        return items;
    }

}
