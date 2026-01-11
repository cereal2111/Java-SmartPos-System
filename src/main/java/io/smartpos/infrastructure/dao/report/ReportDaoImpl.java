package io.smartpos.infrastructure.dao.report;

import io.smartpos.core.reporting.DailyRevenueReport;
import io.smartpos.core.reporting.SaleReport;
import io.smartpos.core.reporting.StockReport;
import io.smartpos.core.reporting.TopProductReport;
import io.smartpos.infrastructure.dao.ReportDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl implements ReportDao {

    private final DataSourceProvider dataSource;

    public ReportDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<StockReport> getCurrentStock() {
        // Using the new view v_current_stock
        String sql = "SELECT * FROM v_current_stock ORDER BY product_name";

        List<StockReport> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                StockReport report = new StockReport();
                report.setProductId(rs.getInt("product_id"));
                report.setProductName(rs.getString("product_name"));
                report.setQuantity(rs.getBigDecimal("current_stock"));
                list.add(report);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error generating stock report", e);
        }
        return list;
    }

    @Override
    public List<SaleReport> getSalesByDateRange(LocalDate start, LocalDate end) {
        String sql = """
                    SELECT sale_id, sale_date, total_amount
                    FROM sale
                    WHERE sale_date BETWEEN ? AND ?
                    ORDER BY sale_date DESC
                """;

        List<SaleReport> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleReport report = new SaleReport();
                    report.setSaleId(rs.getInt("sale_id"));
                    report.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime().toLocalDate());
                    report.setTotal(rs.getBigDecimal("total_amount"));
                    list.add(report);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error generating sale report", e);
        }
        return list;
    }

    @Override
    public List<TopProductReport> getTopSellingProducts(int limit) {
        String sql = """
                    SELECT
                        p.product_id,
                        p.name,
                        SUM(si.quantity) as total_sold
                    FROM sale_item si
                    JOIN product p ON si.product_id = p.product_id
                    GROUP BY p.product_id, p.name
                    ORDER BY total_sold DESC
                    LIMIT ?
                """;

        List<TopProductReport> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductReport report = new TopProductReport();
                    report.setProductId(rs.getInt("product_id"));
                    report.setProductName(rs.getString("name"));
                    report.setTotalQuantity(rs.getBigDecimal("total_sold"));
                    list.add(report);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error generating top products report", e);
        }
        return list;
    }

    @Override
    public List<DailyRevenueReport> getDailyRevenue(LocalDate start, LocalDate end) {
        String sql = """
                    SELECT
                        DATE(sale_date) as d,
                        SUM(total_amount) as total
                    FROM sale
                    WHERE sale_date BETWEEN ? AND ?
                    GROUP BY d
                    ORDER BY d
                """;

        List<DailyRevenueReport> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyRevenueReport report = new DailyRevenueReport();
                    report.setDate(rs.getDate("d").toLocalDate());
                    report.setAmount(rs.getBigDecimal("total"));
                    list.add(report);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error generating daily revenue report", e);
        }
        return list;
    }

    @Override
    public java.util.Map<String, java.math.BigDecimal> getSalesByCashier(LocalDate start, LocalDate end) {
        String sql = """
                    SELECT u.username, SUM(s.total_amount) as total
                    FROM sale s
                    JOIN users u ON s.user_id = u.user_id
                    WHERE s.sale_date BETWEEN ? AND ? AND s.status = 'REGISTERED'
                    GROUP BY u.username
                """;
        java.util.Map<String, java.math.BigDecimal> results = new java.util.HashMap<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.atTime(23, 59, 59)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.put(rs.getString("username"), rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
