package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.user.User;
import java.util.List;

public interface UserDao {
    void save(User user);

    void update(User user);

    User findById(int id);

    User findByUsername(String username);

    List<User> findAllActive();
}
