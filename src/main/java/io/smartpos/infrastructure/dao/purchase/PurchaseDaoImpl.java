package io.smartpos.infrastructure.dao.purchase;

import io.smartpos.core.domain.purchase.Purchase;
import io.smartpos.infrastructure.dao.PurchaseDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDaoImpl implements PurchaseDao {

    private final DataSourceProvider dataSource;

    public PurchaseDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int save(Purchase purchase, Connection connection) {
        String sql = """
                    INSERT INTO purchase (supplier_id, purchase_date, total_amount)
                    VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, purchase.getSupplierId());
            ps.setDate(2, Date.valueOf(LocalDate.now())); // Default to now if null or force now? Logic says service
                                                          // should set it or DAO sets it.
                                                          // Purchase entity has date. Service validates? No.
                                                          // Let's use purchase.getPurchaseDate() if present, else now.

            Date date = (purchase.getPurchaseDate() != null)
                    ? Date.valueOf(purchase.getPurchaseDate())
                    : Date.valueOf(LocalDate.now());

            ps.setDate(2, date);
            ps.setBigDecimal(3, purchase.getTotalAmount());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Failed to retrieve generated purchase ID");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving purchase", e);
        }
    }

    @Override
    public Purchase findById(int purchaseId) {
        String sql = """
                    SELECT purchase_id, supplier_id, purchase_date, total_amount
                    FROM purchase
                    WHERE purchase_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, purchaseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Purchase p = new Purchase();
                    p.setId(rs.getInt("purchase_id"));
                    p.setSupplierId(rs.getInt("supplier_id"));
                    p.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                    p.setTotalAmount(rs.getBigDecimal("total_amount"));
                    return p;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding purchase by ID", e);
        }
        return null; // Not found
    }

    @Override
    public List<Purchase> findByDateRange(LocalDate start, LocalDate end) {
        String sql = """
                    SELECT purchase_id, supplier_id, purchase_date, total_amount
                    FROM purchase
                    WHERE purchase_date BETWEEN ? AND ?
                    ORDER BY purchase_date DESC
                """;

        List<Purchase> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Purchase p = new Purchase();
                    p.setId(rs.getInt("purchase_id"));
                    p.setSupplierId(rs.getInt("supplier_id"));
                    p.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                    p.setTotalAmount(rs.getBigDecimal("total_amount"));
                    list.add(p);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding purchases by date range", e);
        }
        return list;
    }
}
