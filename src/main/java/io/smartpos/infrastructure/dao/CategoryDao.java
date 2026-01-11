package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.product.Category;
import java.util.List;

public interface CategoryDao {
    void save(Category category);

    void update(Category category);

    Category findById(int id);

    List<Category> findAllActive();
}
