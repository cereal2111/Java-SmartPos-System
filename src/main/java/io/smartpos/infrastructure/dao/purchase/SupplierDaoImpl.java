package io.smartpos.infrastructure.dao.purchase;

import io.smartpos.core.domain.purchase.Supplier;
import io.smartpos.infrastructure.dao.SupplierDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDaoImpl implements SupplierDao {

    private final DataSourceProvider dataSource;

    public SupplierDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Supplier supplier) {
        String sql = "INSERT INTO supplier (name, document, phone, email, address, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getDocument());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setBoolean(6, supplier.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    supplier.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving supplier", e);
        }
    }

    @Override
    public Supplier findById(int id) {
        String sql = "SELECT * FROM supplier WHERE supplier_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding supplier", e);
        }
        return null;
    }

    @Override
    public List<Supplier> findAllActive() {
        String sql = "SELECT * FROM supplier WHERE is_active = true ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listing suppliers", e);
        }
        return list;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("supplier_id"));
        s.setName(rs.getString("name"));
        s.setDocument(rs.getString("document"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}
