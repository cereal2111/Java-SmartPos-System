package io.smartpos.infrastructure.dao.cash;

import io.smartpos.core.domain.cash.CashSession;
import io.smartpos.infrastructure.dao.CashSessionDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.sql.*;
import java.util.Optional;

public class CashSessionDaoImpl implements CashSessionDao {

    private final DataSourceProvider dataSource;

    public CashSessionDaoImpl(DataSourceProvider dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(CashSession session) {
        String sql = "INSERT INTO cash_sessions (user_id, opened_at, opening_balance, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(session.getOpenedAt()));
            ps.setBigDecimal(3, session.getOpeningBalance());
            ps.setString(4, session.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    session.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving cash session", e);
        }
    }

    @Override
    public void update(CashSession session) {
        String sql = "UPDATE cash_sessions SET closed_at = ?, total_sales = ?, actual_cash = ?, status = ? WHERE session_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, session.getClosedAt() != null ? Timestamp.valueOf(session.getClosedAt()) : null);
            ps.setBigDecimal(2, session.getTotalSales());
            ps.setBigDecimal(3, session.getActualCash());
            ps.setString(4, session.getStatus());
            ps.setInt(5, session.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating cash session", e);
        }
    }

    @Override
    public Optional<CashSession> findOpenSessionByUserId(int userId) {
        String sql = "SELECT * FROM cash_sessions WHERE user_id = ? AND status = 'OPEN'";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding open session", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CashSession> findById(int id) {
        String sql = "SELECT * FROM cash_sessions WHERE session_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding session by id", e);
        }
        return Optional.empty();
    }

    private CashSession mapRow(ResultSet rs) throws SQLException {
        CashSession s = new CashSession();
        s.setId(rs.getInt("session_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setOpenedAt(rs.getTimestamp("opened_at").toLocalDateTime());
        if (rs.getTimestamp("closed_at") != null) {
            s.setClosedAt(rs.getTimestamp("closed_at").toLocalDateTime());
        }
        s.setOpeningBalance(rs.getBigDecimal("opening_balance"));
        s.setTotalSales(rs.getBigDecimal("total_sales"));
        s.setActualCash(rs.getBigDecimal("actual_cash"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
