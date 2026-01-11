package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.purchase.Supplier;
import java.util.List;

public interface SupplierDao {
    void save(Supplier supplier);

    Supplier findById(int id);

    List<Supplier> findAllActive();
}
