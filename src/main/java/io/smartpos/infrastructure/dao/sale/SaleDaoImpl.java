/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package io.smartpos.infrastructure.dao.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.infrastructure.dao.SaleDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDaoImpl implements SaleDao {

    private final DataSourceProvider dataSource;

    public SaleDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(Sale sale, Connection connection) throws SQLException {

        if (sale.getSaleDate() == null) {
            throw new IllegalArgumentException("saleDate must not be null");
        }

        if (sale.getTotalAmount() == null) {
            throw new IllegalArgumentException("totalAmount must not be null");
        }

        String sql = """
                INSERT INTO sale (
                    user_id,
                    customer_id,
                    sale_date,
                    total_amount
                ) VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sale.getUserId());
            ps.setInt(2, sale.getCustomerId());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(sale.getSaleDate()));
            ps.setBigDecimal(4, sale.getTotalAmount());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Failed to retrieve generated sale ID");
        }
    }

    @Override
    public Sale findById(int saleId) {

        String sql = """
                SELECT
                    sale_id,
                    user_id,
                    customer_id,
                    sale_date,
                    total_amount,
                    status
                FROM sale
                WHERE sale_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Sale sale = new Sale();
                sale.setId(rs.getInt("sale_id"));
                sale.setUserId(rs.getInt("user_id"));
                sale.setCustomerId(rs.getInt("customer_id"));
                sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                sale.loadTotalAmount(rs.getBigDecimal("total_amount"));
                sale.setStatus(rs.getString("status"));

                return sale;
            }

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Error finding sale by ID: " + saleId,
                    ex);
        }
    }

    @Override
    public List<Sale> findByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        String sql = """
                SELECT
                    sale_id,
                    user_id,
                    customer_id,
                    sale_date,
                    total_amount,
                    status
                FROM sale
                WHERE sale_date BETWEEN ? AND ?
                ORDER BY sale_date DESC, sale_id DESC
                """;

        List<Sale> sales = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("sale_id"));
                    sale.setUserId(rs.getInt("user_id"));
                    sale.setCustomerId(rs.getInt("customer_id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    sale.loadTotalAmount(rs.getBigDecimal("total_amount"));
                    sale.setStatus(rs.getString("status"));
                    sales.add(sale);
                }
            }

            return sales;

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Error finding sales between dates",
                    ex);
        }
    }

    /**
     * Paginated version of findByDateRange.
     */
    public List<Sale> findByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            int limit,
            int offset) {

        String sql = """
                SELECT
                    sale_id,
                    customer_id,
                    sale_date
                FROM sale
                WHERE sale_date BETWEEN ? AND ?
                ORDER BY sale_date ASC, sale_id ASC
                LIMIT ? OFFSET ?
                """;

        List<Sale> sales = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ps.setInt(3, limit);
            ps.setInt(4, offset);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Sale sale = new Sale();
                    sale.setId(rs.getInt("sale_id"));
                    sale.setCustomerId(rs.getInt("customer_id"));
                    sale.setSaleDate(
                            rs.getTimestamp("sale_date").toLocalDateTime());

                    sales.add(sale);
                }
            }

            return sales;

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Error finding paginated sales between dates",
                    ex);
        }
    }

    @Override
    public void updateStatus(int saleId, String status, Connection connection) throws SQLException {
        String sql = "UPDATE sale SET status = ? WHERE sale_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, saleId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Sale> findRecent(int limit) {
        String sql = """
                SELECT
                    sale_id,
                    user_id,
                    customer_id,
                    sale_date,
                    total_amount,
                    status
                FROM sale
                ORDER BY sale_date DESC, sale_id DESC
                LIMIT ?
                """;
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("sale_id"));
                    sale.setUserId(rs.getInt("user_id"));
                    sale.setCustomerId(rs.getInt("customer_id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    sale.loadTotalAmount(rs.getBigDecimal("total_amount"));
                    sale.setStatus(rs.getString("status"));
                    sales.add(sale);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error finding recent sales", ex);
        }
        return sales;
    }
}
