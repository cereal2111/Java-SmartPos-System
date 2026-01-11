package io.smartpos.infrastructure.dao.product;

import io.smartpos.core.domain.product.UnitOfMeasure;
import io.smartpos.infrastructure.dao.UnitOfMeasureDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitOfMeasureDaoImpl implements UnitOfMeasureDao {

    private final DataSourceProvider dataSource;

    public UnitOfMeasureDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UnitOfMeasure findById(int id) {
        String sql = "SELECT * FROM unit_of_measure WHERE unit_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding unit", e);
        }
        return null;
    }

    @Override
    public List<UnitOfMeasure> findAll() {
        String sql = "SELECT * FROM unit_of_measure ORDER BY name";
        List<UnitOfMeasure> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listing units", e);
        }
        return list;
    }

    private UnitOfMeasure mapRow(ResultSet rs) throws SQLException {
        UnitOfMeasure u = new UnitOfMeasure();
        u.setId(rs.getInt("unit_id"));
        u.setName(rs.getString("name"));
        u.setAbbreviation(rs.getString("abbreviation"));
        return u;
    }
}
