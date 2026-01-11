/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.purchase.PurchaseItem;
import java.sql.Connection;

public interface PurchaseItemDao {

    void save(
        PurchaseItem item,
        Connection connection
    );
}
