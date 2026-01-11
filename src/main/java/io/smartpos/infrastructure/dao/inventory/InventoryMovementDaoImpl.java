package io.smartpos.infrastructure.dao.inventory;

import io.smartpos.infrastructure.dao.InventoryMovementDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class InventoryMovementDaoImpl implements InventoryMovementDao {

    @Override
    public void registerEntry(
            int productId,
            BigDecimal quantity,
            String reference,
            Connection connection) throws SQLException {

        String sql = """
                    INSERT INTO inventory_movement
                        (product_id, movement_type, quantity, reference_type)
                    VALUES (?, 'IN', ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setBigDecimal(2, quantity);
            stmt.setString(3, reference);
            stmt.executeUpdate();
        }
    }

    @Override
    public void registerExit(
            int productId,
            BigDecimal quantity,
            String reference,
            Connection connection) throws SQLException {

        String sql = """
                    INSERT INTO inventory_movement
                        (product_id, movement_type, quantity, reference_type)
                    VALUES (?, 'OUT', ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setBigDecimal(2, quantity);
            stmt.setString(3, reference);
            stmt.executeUpdate();
        }
    }

    @Override
    public BigDecimal getCurrentStock(
            int productId,
            Connection connection) throws SQLException {

        String sql = """
                    SELECT COALESCE(
                        SUM(
                            CASE
                                WHEN movement_type = 'IN' THEN quantity
                                WHEN movement_type = 'OUT' THEN -quantity
                                ELSE 0
                            END
                        ), 0
                    )
                    FROM inventory_movement
                    WHERE product_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
                return BigDecimal.ZERO;
            }
        }
    }

    // ===== Batch stock loading =====
    @Override
    public Map<Integer, BigDecimal> getCurrentStockByProductIds(
            List<Integer> productIds,
            Connection connection) throws SQLException {

        Map<Integer, BigDecimal> stockByProduct = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            return stockByProduct;
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int i = 0; i < productIds.size(); i++) {
            placeholders.add("?");
        }

        String sql = """
                SELECT
                    product_id,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN movement_type = 'IN' THEN quantity
                                WHEN movement_type = 'OUT' THEN -quantity
                                ELSE 0
                            END
                        ), 0
                    ) AS stock
                FROM inventory_movement
                WHERE product_id IN (""" + placeholders + """
                    )
                    GROUP BY product_id
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            int index = 1;
            for (Integer productId : productIds) {
                stmt.setInt(index++, productId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stockByProduct.put(
                            rs.getInt("product_id"),
                            rs.getBigDecimal("stock"));
                }
            }
        }

        return stockByProduct;
    }

    // ===== Batch inventory exit =====
    @Override
    public void registerExitBatch(
            Map<Integer, BigDecimal> quantityByProduct,
            String reference,
            Connection connection) throws SQLException {

        if (quantityByProduct == null || quantityByProduct.isEmpty()) {
            return;
        }

        String sql = """
                    INSERT INTO inventory_movement
                        (product_id, movement_type, quantity, reference_type)
                    VALUES (?, 'OUT', ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (Map.Entry<Integer, BigDecimal> entry : quantityByProduct.entrySet()) {

                stmt.setInt(1, entry.getKey());
                stmt.setBigDecimal(2, entry.getValue());
                stmt.setString(3, reference);
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }
}
