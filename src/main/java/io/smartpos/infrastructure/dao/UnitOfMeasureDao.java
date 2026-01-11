package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.product.UnitOfMeasure;
import java.util.List;

public interface UnitOfMeasureDao {
    UnitOfMeasure findById(int id);

    List<UnitOfMeasure> findAll();
}
