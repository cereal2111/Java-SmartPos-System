/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.sale.SaleItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SaleItemDao {

        void save(
                        SaleItem item,
                        Connection connection) throws SQLException;

        void saveBatch(
                        List<SaleItem> items,
                        Connection connection) throws SQLException;

        List<SaleItem> findBySaleId(
                        int saleId,
                        Connection connection) throws SQLException;

        List<SaleItem> findBySaleIds(
                        List<Integer> saleIds,
                        Connection connection) throws SQLException;
}
