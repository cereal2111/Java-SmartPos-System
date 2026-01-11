/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.sale.Sale;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SaleDao {

    int save(
            Sale sale,
            Connection connection) throws SQLException;

    Sale findById(int saleId);

    void updateStatus(int saleId, String status, Connection connection) throws SQLException;

    List<Sale> findByDateRange(
            LocalDate start,
            LocalDate end);

    List<Sale> findRecent(int limit);
}
