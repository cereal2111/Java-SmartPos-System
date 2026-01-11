package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.cash.CashSession;
import java.util.Optional;

public interface CashSessionDao {
    void save(CashSession session);

    void update(CashSession session);

    Optional<CashSession> findOpenSessionByUserId(int userId);

    Optional<CashSession> findById(int id);
}
