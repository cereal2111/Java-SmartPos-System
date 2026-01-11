package io.smartpos.infrastructure.dao.product;

import io.smartpos.core.domain.product.Product;
import io.smartpos.infrastructure.dao.ProductDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {

    private final DataSourceProvider dataSource;

    public ProductDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        String sql = """
                    INSERT INTO product (code, name, category_id, unit_id, sale_price, minimum_stock, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getCode());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getCategoryId());
            ps.setInt(4, product.getUnitId());
            ps.setBigDecimal(5, product.getPrice());
            ps.setBigDecimal(6, product.getMinimumStock());
            ps.setBoolean(7, product.isActive());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = """
                    UPDATE product
                    SET code = ?, name = ?, category_id = ?, unit_id = ?, sale_price = ?, minimum_stock = ?, is_active = ?
                    WHERE product_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getCode());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getCategoryId());
            ps.setInt(4, product.getUnitId());
            ps.setBigDecimal(5, product.getPrice());
            ps.setBigDecimal(6, product.getMinimumStock());
            ps.setBoolean(7, product.isActive());
            ps.setInt(8, product.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    @Override
    public Product findById(int productId) {
        String sql = """
                    SELECT p.*, c.name as category_name, u.name as unit_name, s.current_stock
                    FROM product p
                    JOIN category c ON p.category_id = c.category_id
                    JOIN unit_of_measure u ON p.unit_id = u.unit_id
                    LEFT JOIN v_current_stock s ON p.product_id = s.product_id
                    WHERE p.product_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by ID", e);
        }
        return null;
    }

    @Override
    public Product findByCode(String code) {
        String sql = """
                    SELECT p.*, c.name as category_name, u.name as unit_name, s.current_stock
                    FROM product p
                    JOIN category c ON p.category_id = c.category_id
                    JOIN unit_of_measure u ON p.unit_id = u.unit_id
                    LEFT JOIN v_current_stock s ON p.product_id = s.product_id
                    WHERE p.code = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by code", e);
        }
        return null;
    }

    @Override
    public List<Product> findAllActive() {
        String sql = """
                    SELECT p.*, c.name as category_name, u.name as unit_name, s.current_stock
                    FROM product p
                    JOIN category c ON p.category_id = c.category_id
                    JOIN unit_of_measure u ON p.unit_id = u.unit_id
                    LEFT JOIN v_current_stock s ON p.product_id = s.product_id
                    WHERE p.is_active = true
                    ORDER BY p.name ASC
                """;

        List<Product> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listing active products", e);
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("product_id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setUnitId(rs.getInt("unit_id"));
        p.setPrice(rs.getBigDecimal("sale_price"));
        p.setMinimumStock(rs.getBigDecimal("minimum_stock"));
        p.setActive(rs.getBoolean("is_active"));

        // Map descriptive names
        p.setCategoryName(rs.getString("category_name"));
        p.setUnitName(rs.getString("unit_name"));

        // Map current stock
        p.setCurrentStock(rs.getBigDecimal("current_stock"));

        return p;
    }
}
