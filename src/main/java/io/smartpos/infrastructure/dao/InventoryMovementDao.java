package io.smartpos.infrastructure.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface InventoryMovementDao {

        void registerEntry(
                        int productId,
                        BigDecimal quantity,
                        String reference,
                        Connection connection) throws SQLException;

        void registerExit(
                        int productId,
                        BigDecimal quantity,
                        String reference,
                        Connection connection) throws SQLException;

        BigDecimal getCurrentStock(
                        int productId,
                        Connection connection) throws SQLException;

        // ===== Batch stock loading =====
        Map<Integer, BigDecimal> getCurrentStockByProductIds(
                        List<Integer> productIds,
                        Connection connection) throws SQLException;

        // ===== Batch inventory exit (NEW) =====
        void registerExitBatch(
                        Map<Integer, BigDecimal> quantityByProduct,
                        String reference,
                        Connection connection) throws SQLException;
}
