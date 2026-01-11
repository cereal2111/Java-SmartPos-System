package io.smartpos.infrastructure.database;

import io.smartpos.infrastructure.datasource.DataSourceProvider;
import java.sql.*;
import java.util.logging.Logger;

public class DatabaseMigration {
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigration.class.getName());

    public static void migrate(DataSourceProvider dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null)
                return;

            LOGGER.info("Starting database migration...");

            // Add user_id to sale if missing
            addColumnIfNotExists(conn, "sale", "user_id", "INT NOT NULL DEFAULT 1");

            // Add status to sale if missing
            addColumnIfNotExists(conn, "sale", "status", "VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'");

            // Ensure sale_date column exists (might be created_at in older versions)
            ensureSaleDateColumn(conn);

            // Create cash_sessions
            createCashSessionsTable(conn);

            // Add image_url to category if missing
            addColumnIfNotExists(conn, "category", "image_url", "VARCHAR(255) NULL");

            // Ensure v_current_stock view exists
            createCurrentStockView(conn);

            LOGGER.info("Migration completed.");
        } catch (SQLException e) {
            LOGGER.severe("Migration failed: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(Connection conn, String table, String column, String definition)
            throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            if (!rs.next()) {
                String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    LOGGER.info("Added column " + column + " to table " + table);
                }
            }
        }
    }

    private static void ensureSaleDateColumn(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        boolean hasSaleDate = false;
        boolean hasCreatedAt = false;

        try (ResultSet rs = meta.getColumns(null, null, "sale", null)) {
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                if ("sale_date".equalsIgnoreCase(colName))
                    hasSaleDate = true;
                if ("created_at".equalsIgnoreCase(colName))
                    hasCreatedAt = true;
            }
        }

        if (!hasSaleDate) {
            if (hasCreatedAt) {
                // Rename created_at to sale_date
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(
                            "ALTER TABLE sale CHANGE COLUMN created_at sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    LOGGER.info("Renamed created_at to sale_date in sale table");
                }
            } else {
                // Simply add it
                addColumnIfNotExists(conn, "sale", "sale_date", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
        }
    }

    private static void createCashSessionsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS cash_sessions (" +
                "session_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "closed_at TIMESTAMP NULL, " +
                "opening_balance DECIMAL(12, 2) DEFAULT 0, " +
                "total_sales DECIMAL(12, 2) DEFAULT 0, " +
                "actual_cash DECIMAL(12, 2) DEFAULT 0, " +
                "status VARCHAR(20) DEFAULT 'OPEN', " +
                "FOREIGN KEY (user_id) REFERENCES users (user_id)" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createCurrentStockView(Connection conn) throws SQLException {
        String sql = """
                CREATE OR REPLACE VIEW v_current_stock AS
                SELECT
                    p.product_id,
                    p.name AS product_name,
                    COALESCE(
                        SUM(
                            CASE
                                WHEN m.movement_type = 'IN' THEN m.quantity
                                WHEN m.movement_type = 'OUT' THEN - m.quantity
                                ELSE 0
                            END
                        ),
                        0
                    ) AS current_stock
                FROM
                    product p
                    LEFT JOIN inventory_movement m ON p.product_id = m.product_id
                GROUP BY
                    p.product_id,
                    p.name
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.info("Created or updated v_current_stock view");
        }
    }
}
