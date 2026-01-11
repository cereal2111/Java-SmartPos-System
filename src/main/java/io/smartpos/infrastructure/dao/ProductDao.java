/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.product.Product;
import java.util.List;

public interface ProductDao {

    void save(Product product);

    void update(Product product);

    Product findById(int productId);

    Product findByCode(String code);

    List<Product> findAllActive();
}
