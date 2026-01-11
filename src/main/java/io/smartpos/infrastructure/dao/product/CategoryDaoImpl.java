package io.smartpos.infrastructure.dao.product;

import io.smartpos.core.domain.product.Category;
import io.smartpos.infrastructure.dao.CategoryDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDaoImpl implements CategoryDao {

    private final DataSourceProvider dataSource;

    public CategoryDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Category category) {
        String sql = "INSERT INTO category (name, is_active, image_url) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isActive());
            ps.setString(3, category.getImageUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    category.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving category", e);
        }
    }

    @Override
    public void update(Category category) {
        String sql = "UPDATE category SET name = ?, is_active = ?, image_url = ? WHERE category_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isActive());
            ps.setString(3, category.getImageUrl());
            ps.setInt(4, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    @Override
    public Category findById(int id) {
        String sql = "SELECT * FROM category WHERE category_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding category", e);
        }
        return null;
    }

    @Override
    public List<Category> findAllActive() {
        String sql = "SELECT * FROM category WHERE is_active = true ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listing categories", e);
        }
        return list;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("category_id"));
        c.setName(rs.getString("name"));
        c.setActive(rs.getBoolean("is_active"));
        c.setImageUrl(rs.getString("image_url"));
        return c;
    }
}
