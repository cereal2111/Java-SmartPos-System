/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.purchase.Purchase;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseDao {

    int save(
        Purchase purchase,
        Connection connection
    );

    Purchase findById(int purchaseId);

    List<Purchase> findByDateRange(
        LocalDate start,
        LocalDate end
    );
}
