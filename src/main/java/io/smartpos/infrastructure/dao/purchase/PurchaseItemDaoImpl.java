package io.smartpos.infrastructure.dao.purchase;

import io.smartpos.core.domain.purchase.PurchaseItem;
import io.smartpos.infrastructure.dao.PurchaseItemDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PurchaseItemDaoImpl implements PurchaseItemDao {

    @Override
    public void save(PurchaseItem item, Connection connection) {
        String sql = """
                    INSERT INTO purchase_item (purchase_id, product_id, quantity, unit_cost)
                    VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, item.getPurchaseId());
            ps.setInt(2, item.getProductId());
            ps.setBigDecimal(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitCost());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving purchase item", e);
        }
    }
}
